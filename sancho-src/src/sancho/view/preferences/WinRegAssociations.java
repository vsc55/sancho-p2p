package sancho.view.preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import sancho.core.Sancho;
import sancho.utility.VersionInfo;

// Reads and writes the Windows file/URL-protocol associations Sancho can own. Centralizes
// the registry logic (both the query "is this already registered, and where?" and the
// .reg generation/import) so the Preferences page and the startup check share one source
// of truth. Kept free of SWT so it can run off the UI thread; the i18n/dialogs live in the
// callers (WinRegPreferencePage / AssociationChecker).
public class WinRegAssociations {
   // URL protocols Sancho can handle, each registered as <name>://.
   public static final String[] PROTOCOLS = {"ed2k", "magnet", "sig2dat", "sfdl"};
   // The .torrent file extension points at this ProgId, whose shell\open\command we own.
   public static final String TORRENT_PROGID = "bittorrent";
   private static final String REG_SZ = "REG_SZ";

   // Where an association currently lives and whether its command points at Sancho. User
   // (HKCU) takes precedence over machine (HKLM), matching how Windows resolves HKCR.
   public enum Level {
      NONE,
      SANCHO_USER,
      SANCHO_MACHINE,
      OTHER_USER,
      OTHER_MACHINE
   }

   private WinRegAssociations() {
   }

   public static boolean isWindows() {
      return VersionInfo.getSWTPlatform().equals("win32");
   }

   public static boolean isSancho(Level level) {
      return level == Level.SANCHO_USER || level == Level.SANCHO_MACHINE;
   }

   // One association Sancho can own, described by data (not code) so a single generic writer
   // handles both URL protocols and file extensions:
   //   label       display text (ed2k://, bittorrent (.torrent))
   //   extension   the file extension key that maps to the ProgId (.torrent), or null for a
   //               URL protocol (whose scheme name *is* the ProgId)
   //   progId      the Software\Classes key that holds shell\open\command (ed2k, bittorrent)
   //   description default value written on the ProgId key
   //   urlProtocol whether to declare the "URL Protocol" marker (true for URL schemes)
   public static class AssocItem {
      public final String label;
      public final String extension;
      public final String progId;
      public final String description;
      public final boolean urlProtocol;

      public AssocItem(String label, String extension, String progId, String description, boolean urlProtocol) {
         this.label = label;
         this.extension = extension;
         this.progId = progId;
         this.description = description;
         this.urlProtocol = urlProtocol;
      }
   }

   // A pending change for writeAndImport: register (true) or unregister (false) one item.
   public static class RegAction {
      public final AssocItem item;
      public final boolean register;

      public RegAction(AssocItem item, boolean register) {
         this.item = item;
         this.register = register;
      }
   }

   public static AssocItem protocolItem(String protocol) {
      return new AssocItem(protocol + "://", null, protocol, "URL: " + protocol + " Protocol", true);
   }

   public static AssocItem torrentItem() {
      return new AssocItem("bittorrent (.torrent)", ".torrent", TORRENT_PROGID, "TORRENT File", false);
   }

   // Every association Sancho can own, derived from PROTOCOLS plus the .torrent extension.
   public static AssocItem[] allItems() {
      AssocItem[] items = new AssocItem[PROTOCOLS.length + 1];

      for (int i = 0; i < PROTOCOLS.length; i++) {
         items[i] = protocolItem(PROTOCOLS[i]);
      }

      items[PROTOCOLS.length] = torrentItem();
      return items;
   }

   // Items with no association at all (Level.NONE) — the ones the startup check offers to
   // create. Items already owned by another application are deliberately left untouched.
   public static List<AssocItem> missingItems() {
      List<AssocItem> missing = new ArrayList<>();
      for (AssocItem item : allItems()) {
         if (level(item.progId) == Level.NONE) {
            missing.add(item);
         }
      }

      return missing;
   }

   // The current association level of a Software\Classes key ("ed2k", "bittorrent", ...).
   // A command naming our exe only counts while that exe is still there: uninstalling (or
   // moving) an install leaves the user-level key behind pointing at a deleted file, and
   // since HKCU shadows HKLM that stale entry also hides a perfectly good machine-level
   // registration (the one the MSI writes). Reporting it as NONE is both honest — the
   // association does not work — and useful: missingItems() then offers to recreate it.
   public static Level level(String progId) {
      String userCommand = queryCommand("HKCU", progId);
      if (userCommand != null) {
         if (!referencesSancho(userCommand)) {
            return Level.OTHER_USER;
         }

         return commandExeExists(userCommand) ? Level.SANCHO_USER : Level.NONE;
      }

      String machineCommand = queryCommand("HKLM", progId);
      if (machineCommand != null) {
         if (!referencesSancho(machineCommand)) {
            return Level.OTHER_MACHINE;
         }

         return commandExeExists(machineCommand) ? Level.SANCHO_MACHINE : Level.NONE;
      }

      return Level.NONE;
   }

   private static boolean referencesSancho(String command) {
      return command.toLowerCase().contains(VersionInfo.getName().toLowerCase() + ".exe");
   }

   // Whether the executable a shell\open\command would launch still exists on disk.
   private static boolean commandExeExists(String command) {
      String exe = commandExe(command);
      return exe != null && new File(exe).isFile();
   }

   // The executable out of a shell\open\command value: the first quoted token (the form we
   // and every installer write, since the path contains spaces), else the text up to the
   // first space. Returns null when the value is malformed.
   private static String commandExe(String command) {
      String trimmed = command.trim();
      if (trimmed.startsWith("\"")) {
         int closing = trimmed.indexOf('"', 1);
         return closing > 1 ? trimmed.substring(1, closing) : null;
      }

      int space = trimmed.indexOf(' ');
      return space > 0 ? trimmed.substring(0, space) : (trimmed.isEmpty() ? null : trimmed);
   }

   // Read the default value of <hive>\Software\Classes\<progId>\shell\open\command via
   // reg.exe. reg.exe prints the value after a language-independent "REG_SZ" token; returns
   // null when the key is missing (exit 1 / empty output) or we're not on Windows.
   private static String queryCommand(String hive, String progId) {
      if (!isWindows()) {
         return null;
      }

      String key = hive + "\\Software\\Classes\\" + progId + "\\shell\\open\\command";

      try {
         Process reg = Runtime.getRuntime().exec(new String[]{"reg.exe", "query", key, "/ve"});
         BufferedReader reader = new BufferedReader(new InputStreamReader(reg.getInputStream()));
         String value = null;
         String line;
         while ((line = reader.readLine()) != null) {
            int marker = line.indexOf(REG_SZ);
            if (marker >= 0) {
               value = line.substring(marker + REG_SZ.length()).trim();
            }
         }

         reader.close();
         reg.waitFor();
         return value == null || value.isEmpty() ? null : value;
      } catch (Exception error) {
         Sancho.pDebug("WinRegAssociations.queryCommand: " + error);
         return null;
      }
   }

   // Build a .reg for the requested actions and import it with reg.exe. perUser=true writes
   // HKEY_CURRENT_USER\Software\Classes (no elevation); false writes HKEY_CLASSES_ROOT (needs
   // administrator, and reg import then fails without it). Returns true when reg import
   // succeeds. The .reg goes to java.io.tmpdir, which is always writable (an installed build
   // runs from a non-writable Program Files working directory).
   public static boolean writeAndImport(List<RegAction> actions, boolean perUser) {
      if (!isWindows() || actions.isEmpty()) {
         return false;
      }

      File regFile = new File(System.getProperty("java.io.tmpdir"), VersionInfo.getName() + ".reg");

      try {
         String exe = resolveExe();
         String extra = createExtra();
         String root = perUser ? "HKEY_CURRENT_USER\\Software\\Classes" : "HKEY_CLASSES_ROOT";
         FileOutputStream fileOut = new FileOutputStream(regFile);
         PrintStream out = new PrintStream(fileOut);
         out.println("REGEDIT4");

         for (RegAction action : actions) {
            if (action.register) {
               writeRegister(out, root, action.item, exe, extra);
            } else {
               writeUnregister(out, root, action.item);
            }
         }

         out.close();
         // reg.exe (NOT regedit.exe, whose manifest forces elevation) runs at the caller's
         // level, so a per-user import succeeds without admin. "reg import" exits 0 on success.
         Process reg = Runtime.getRuntime().exec(new String[]{"reg.exe", "import", regFile.getAbsolutePath()});
         return reg.waitFor() == 0;
      } catch (Exception error) {
         Sancho.pDebug("WinRegAssociations.writeAndImport: " + error);
         return false;
      } finally {
         if (regFile.exists() && !Sancho.debug) {
            regFile.delete();
         }
      }
   }

   // Register one association: the optional extension->ProgId mapping, then the ProgId with
   // its description (and the "URL Protocol" marker for URL schemes) and the shell\open\command
   // that launches Sancho. Drives both protocols and the .torrent extension off AssocItem data.
   private static void writeRegister(PrintStream out, String root, AssocItem item, String exe, String extra) {
      if (item.extension != null) {
         out.println("[" + root + "\\" + item.extension + "]");
         out.println("@=\"" + item.progId + "\"");
      }

      out.println("[" + root + "\\" + item.progId + "]");
      out.println("@=\"" + item.description + "\"");
      if (item.urlProtocol) {
         out.println("\"URL Protocol\"=\"\"");
      }

      out.println("[" + root + "\\" + item.progId + "\\shell]");
      out.println("[" + root + "\\" + item.progId + "\\shell\\open]");
      out.println("[" + root + "\\" + item.progId + "\\shell\\open\\command]");
      printCommand(out, exe, extra);
   }

   // Unregister one association: delete the ProgId subtree (command, open, shell, ProgId) and,
   // for a file type, the extension key that pointed at it.
   private static void writeUnregister(PrintStream out, String root, AssocItem item) {
      out.println("[-" + root + "\\" + item.progId + "\\shell\\open\\command]");
      out.println("[-" + root + "\\" + item.progId + "\\shell\\open]");
      out.println("[-" + root + "\\" + item.progId + "\\shell]");
      out.println("[-" + root + "\\" + item.progId + "]");
      if (item.extension != null) {
         out.println("[-" + root + "\\" + item.extension + "]");
      }
   }

   private static void printCommand(PrintStream out, String exe, String extra) {
      out.println("@=\"\\\"" + exe + "\\\" " + extra + "\\\"-l\\\" \\\"%1\\\"\"");
   }

   // Best exe source when installed: jpackage sets jpackage.app-path to the launcher
   // (sancho.exe) full path. Falls back to user.dir/progname/classpath. Backslashes are
   // doubled for the .reg string value.
   private static String resolveExe() {
      String dir = System.getProperty("user.dir") + System.getProperty("file.separator");
      String progName = System.getProperty("gnu.gcj.progname");
      String exe = dir + VersionInfo.getName() + ".exe";
      if (progName != null && !progName.toLowerCase().endsWith("exe")) {
         progName = progName + ".exe";
      }

      if (!new File(exe).exists() && progName != null) {
         exe = progName;
      }

      String classPath = System.getProperty("java.class.path");
      if (classPath != null && classPath.toLowerCase().endsWith(".exe") && new File(classPath).exists()) {
         exe = classPath;
      }

      String appPath = System.getProperty("jpackage.app-path");
      if (appPath != null && appPath.toLowerCase().endsWith(".exe") && new File(appPath).exists()) {
         exe = appPath;
      }

      return exe.replace("\\", "\\\\");
   }

   // The extra launcher arguments (-r jvm / -c preffile / -j homedir) that must precede the
   // "-l %1" so a click-launched Sancho starts against the same config as this instance.
   private static String createExtra() {
      String extra = "";
      String prefFile = PreferenceLoader.getPrefFile();
      String homeDir = PreferenceLoader.getHomeDirectory();
      if (prefFile != null) {
         prefFile = prefFile.replace("\\", "\\\\");
      }

      if (homeDir != null) {
         if (homeDir.endsWith("\\")) {
            homeDir = homeDir.substring(0, homeDir.length() - 1);
         }

         homeDir = homeDir.replace("\\", "\\\\");
      }

      if (PreferenceLoader.jvm != null) {
         String jvmPath = PreferenceLoader.jvm.replace("\\", "\\\\");
         extra = extra + "\\\"-r\\\" \\\"" + jvmPath + "\\\" ";
      }

      if (PreferenceLoader.customPrefFile) {
         extra = extra + "\\\"-c\\\" \\\"" + prefFile + "\\\" ";
      }

      if (PreferenceLoader.customHomeDir) {
         extra = extra + "\\\"-j\\\" \\\"" + homeDir + "\\\" ";
      }

      return extra;
   }
}
