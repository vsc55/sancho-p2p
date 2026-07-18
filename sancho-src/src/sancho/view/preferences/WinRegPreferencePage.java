package sancho.view.preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import sancho.core.Sancho;
import sancho.utility.VersionInfo;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class WinRegPreferencePage extends CPreferencePage {
   RegisterLink[] registerLinks;
   RegisterExtension[] registerExtensions;
   // false -> write machine-wide (HKEY_CLASSES_ROOT, needs administrator); default true ->
   // write per-user under HKEY_CURRENT_USER\Software\Classes, which needs no elevation.
   private boolean perUser = true;

   protected WinRegPreferencePage(String title) {
      super(title);
   }

   protected Control createContents(Composite parent) {
      Composite container = new Composite(parent, 0);
      container.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      final Button allUsersCheck = new Button(container, SWT.CHECK);
      allUsersCheck.setText(SResources.getString("b.regAllUsers"));
      allUsersCheck.setSelection(!this.perUser);
      allUsersCheck.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            WinRegPreferencePage.this.perUser = !allUsersCheck.getSelection();
         }
      });
      TabFolder tabFolder = new TabFolder(container, 128);
      tabFolder.setLayoutData(new GridData(1808));
      this.createProtocolTab(tabFolder);
      this.createFileExtensionsTab(tabFolder);
      return container;
   }

   protected void createFileExtensionsTab(TabFolder tabFolder) {
      Composite tab = this.createNewTab(tabFolder, "l.fileExtensions");
      tab.setLayout(WidgetFactory.createGridLayout(1, 5, 5, 5, 5, false));
      this.createInformationLabel(tab, "p.registerInfo");
      this.registerExtensions = new RegisterExtension[1];
      this.registerExtensions[0] = new RegisterExtension("bittorrent (.torrent)", tab);
      Button updateButton = new Button(tab, 0);
      updateButton.setLayoutData(new GridData(768));
      updateButton.setText(SResources.getString("b.updateRegistry"));
      updateButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            if (WinRegPreferencePage.this.changedExtPrefs()) {
               WinRegPreferencePage.this.createRegFile();
            }
         }
      });
      Point size = tab.computeSize(-1, -1);
      ((ScrolledComposite)tab.getParent()).setMinSize(size);
      tab.layout();
   }

   private boolean changedExtPrefs() {
      // Bound by the array actually indexed (registerExtensions, length 1) — the old
      // registerLinks.length (4) ran off the end and threw AIOOBE when the ".torrent"
      // option was left unchanged and "Update Registry" was clicked.
      for (int i = 0; i < this.registerExtensions.length; i++) {
         if (this.registerExtensions[i].getSelection() != 0) {
            return true;
         }
      }

      return false;
   }

   protected void createProtocolTab(TabFolder tabFolder) {
      Composite tab = this.createNewTab(tabFolder, "l.protocols");
      tab.setLayout(WidgetFactory.createGridLayout(1, 5, 5, 5, 5, false));
      this.registerLinks = new RegisterLink[4];
      this.registerLinks[0] = new RegisterLink("ed2k", tab);
      this.registerLinks[1] = new RegisterLink("magnet", tab);
      this.registerLinks[2] = new RegisterLink("sig2dat", tab);
      this.registerLinks[3] = new RegisterLink("sfdl", tab);
      Button updateButton = new Button(tab, 0);
      updateButton.setLayoutData(new GridData(768));
      updateButton.setText(SResources.getString("b.updateRegistry"));
      updateButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            if (WinRegPreferencePage.this.changedLinkPrefs()) {
               WinRegPreferencePage.this.createRegFile();
            }
         }
      });
      Point size = tab.computeSize(-1, -1);
      ((ScrolledComposite)tab.getParent()).setMinSize(size);
      tab.layout();
   }

   private boolean changedLinkPrefs() {
      for (int i = 0; i < this.registerLinks.length; i++) {
         if (this.registerLinks[i].getSelection() != 0) {
            return true;
         }
      }

      return false;
   }

   private void createRegFile() {
      String dir = System.getProperty("user.dir") + System.getProperty("file.separator");
      String progName = System.getProperty("gnu.gcj.progname");

      try {
         String regFile = dir + VersionInfo.getName() + ".reg";
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

         // Best source when installed: jpackage sets jpackage.app-path to the launcher
         // (sancho.exe) full path. (Running via `java -jar` it's unset, so the association
         // command only resolves to the real exe from an installed build.)
         String appPath = System.getProperty("jpackage.app-path");
         if (appPath != null && appPath.toLowerCase().endsWith(".exe") && new File(appPath).exists()) {
            exe = appPath;
         }

         // Double the backslashes for the .reg string value. (SwissArmy.replaceAll compiles
         // its "from" as a regex and the replacement swallowed the backslashes, so it left
         // the path with single backslashes that reg import then mangled.)
         exe = exe.replace("\\", "\\\\");
         FileOutputStream fileOut = new FileOutputStream(regFile);
         PrintStream out = new PrintStream(fileOut);
         out.println("REGEDIT4");

         for (int i = 0; i < this.registerLinks.length; i++) {
            switch (this.registerLinks[i].getSelection()) {
               case 1:
                  this.registerType(out, this.registerLinks[i].getText(), exe, this.createExtra());
                  break;
               case 2:
                  this.unregisterType(out, this.registerLinks[i].getText());
            }
         }

         for (int i = 0; i < this.registerExtensions.length; i++) {
            switch (this.registerExtensions[i].getSelection()) {
               case 1:
                  this.registerTorrent(out, exe, this.createExtra());
                  break;
               case 2:
                  this.unregisterTorrent(out);
            }
         }

         out.close();
         this.updateRegistry(regFile);
      } catch (Exception error) {
         Sancho.pDebug("createRegFile: " + error);
      }
   }

   private void updateRegistry(String regFilePath) {
      // regedit.exe is win32-only; off Windows (e.g. a debug preview of this page on
      // Linux/macOS) tell the user it's unsupported instead of failing to launch a
      // Windows binary.
      if (!VersionInfo.getSWTPlatform().equals("win32")) {
         MessageDialog.openInformation(this.getShell(), VersionInfo.getName(), SResources.getString("l.regeditWin32Only"));
      } else {
         // Use reg.exe, NOT regedit.exe: regedit.exe's manifest requires elevation, so
         // Runtime.exec fails it with ERROR_ELEVATION_REQUIRED even for a HKCU-only import.
         // reg.exe runs at the caller's level, so a per-user import succeeds without admin
         // (a machine-wide one still fails without it). "reg import" exits 0 on success,
         // non-zero (or throws) on failure — report the outcome.
         boolean updated = false;

         try {
            Process reg = Runtime.getRuntime().exec(new String[]{"reg.exe", "import", regFilePath});
            updated = reg.waitFor() == 0;
         } catch (Exception regError) {
            Sancho.pDebug("updateRegistry: " + regError);
         }

         if (updated) {
            MessageDialog.openInformation(this.getShell(), VersionInfo.getName(), SResources.getString("l.regUpdated"));
         } else {
            MessageDialog.openWarning(this.getShell(), VersionInfo.getName(), SResources.getString("l.regUpdateFailed"));
         }
      }

      if (!Sancho.debug) {
         File regFile = new File(regFilePath);
         if (regFile.exists()) {
            regFile.delete();
         }
      }
   }

   private String regRoot() {
      // Per-user (HKCU\Software\Classes) needs no administrator and HKCR still shows it;
      // "all users" uses machine-wide HKEY_CLASSES_ROOT, which regedit can only write when
      // elevated (a non-admin run then fails, and updateRegistry reports that).
      return this.perUser ? "HKEY_CURRENT_USER\\Software\\Classes" : "HKEY_CLASSES_ROOT";
   }

   private void registerTorrent(PrintStream out, String exe, String extra) {
      String root = this.regRoot();
      out.println("[" + root + "\\.torrent]");
      out.println("@=\"bittorrent\"");
      out.println("[" + root + "\\bittorrent]");
      out.println("@=\"TORRENT File\"");
      out.println("[" + root + "\\bittorrent\\shell]");
      out.println("@=\"open\"");
      out.println("[" + root + "\\bittorrent\\shell\\open]");
      out.println("[" + root + "\\bittorrent\\shell\\open\\command]");
      this.printCommand(out, exe, extra);
   }

   private void unregisterTorrent(PrintStream out) {
      String root = this.regRoot();
      out.println("[-" + root + "\\bittorrent\\shell\\open\\command]");
      out.println("[-" + root + "\\bittorrent\\shell\\open]");
      out.println("[-" + root + "\\bittorrent\\shell]");
      out.println("[-" + root + "\\bittorrent]");
      out.println("[-" + root + "\\.torrent]");
   }

   private void registerType(PrintStream out, String protocol, String exe, String extra) {
      String root = this.regRoot();
      out.println("[" + root + "\\" + protocol + "]");
      out.println("@=\"URL: " + protocol + " Protocol\"");
      out.println("\"URL Protocol\"=\"\"");
      out.println("[" + root + "\\" + protocol + "\\shell]");
      out.println("[" + root + "\\" + protocol + "\\shell\\open]");
      out.println("[" + root + "\\" + protocol + "\\shell\\open\\command]");
      this.printCommand(out, exe, extra);
   }

   private void printCommand(PrintStream out, String exe, String extra) {
      out.println("@=\"\\\"" + exe + "\\\" " + extra + "\\\"-l\\\" \\\"%1\\\"\"");
   }

   private String createExtra() {
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
         String jvmPath = PreferenceLoader.jvm;
         jvmPath = jvmPath.replace("\\", "\\\\");
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

   private void unregisterType(PrintStream out, String protocol) {
      String root = this.regRoot();
      out.println("[-" + root + "\\" + protocol + "\\shell\\open\\command]");
      out.println("[-" + root + "\\" + protocol + "\\shell\\open]");
      out.println("[-" + root + "\\" + protocol + "\\shell]");
      out.println("[-" + root + "\\" + protocol + "]");
   }

   // A three-way radio group (No change / Register / Unregister) for one protocol
   // (ed2k://, magnet:, …). Tracks the chosen action for the .reg generation.
   static class RegisterLink {
      public static final int NO_CHANGE = 0;
      public static final int REGISTER = 1;
      public static final int UNREGISTER = 2;
      private int selection;
      private String text;

      public RegisterLink(String text, Composite parent) {
         this.text = text;
         this.selection = 0;
         this.createContents(parent);
      }

      protected void createContents(Composite parent) {
         Group group = new Group(parent, 16);
         group.setLayoutData(new GridData(768));
         group.setLayout(WidgetFactory.createGridLayout(1, 5, 5, 5, 5, false));
         group.setText(this.text + "://");
         this.createButton(group, SResources.getString("b.noChange"), 0);
         this.createButton(group, SResources.getString("b.registerLink"), 1);
         this.createButton(group, SResources.getString("b.unregisterLink"), 2);
      }

      private void createButton(Group group, String label, final int type) {
         Button button = new Button(group, 16);
         button.setLayoutData(new GridData(768));
         button.setText(label);
         button.setSelection(type == 0);
         button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               RegisterLink.this.selection = type;
            }
         });
      }

      public int getSelection() {
         return this.selection;
      }

      public String getText() {
         return this.text;
      }
   }

   // Same three-way radio group, for a file-extension association (.torrent).
   static class RegisterExtension {
      public static final int NO_CHANGE = 0;
      public static final int REGISTER = 1;
      public static final int UNREGISTER = 2;
      private int selection;
      private String text;

      public RegisterExtension(String text, Composite parent) {
         this.text = text;
         this.selection = 0;
         this.createContents(parent);
      }

      protected void createContents(Composite parent) {
         Group group = new Group(parent, 16);
         group.setLayoutData(new GridData(768));
         group.setLayout(WidgetFactory.createGridLayout(1, 5, 5, 5, 5, false));
         group.setText(this.text);
         this.createButton(group, SResources.getString("b.noChange"), 0);
         this.createButton(group, SResources.getString("b.registerLink"), 1);
         this.createButton(group, SResources.getString("b.unregisterLink"), 2);
      }

      private void createButton(Group group, String label, final int type) {
         Button button = new Button(group, 16);
         button.setLayoutData(new GridData(768));
         button.setText(label);
         button.setSelection(type == 0);
         button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               RegisterExtension.this.selection = type;
            }
         });
      }

      public int getSelection() {
         return this.selection;
      }

      public String getText() {
         return this.text;
      }
   }
}
