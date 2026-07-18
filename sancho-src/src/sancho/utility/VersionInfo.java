package sancho.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;

public class VersionInfo {
   private static String VERSION;
   private static final boolean WEB_SERVICES = true;
   private static final String BRAND = "";
   private static boolean isWin95;
   private static boolean oldWindows;
   private static boolean isGNU;

   public static boolean useWebServices() {
      return true;
   }

   public static String getBugPage() {
      return "http://sourceforge.net/tracker/?group_id=98050&atid=619889";
   }

   public static String getBruceHomePage() {
      return "http://www.fliptopbox.com/";
   }

   public static String getEmail() {
      return "rutger.ovidius@gmail.com";
   }

   public static boolean hasTray() {
      String var0 = SWT.getPlatform();
      return var0.equals("win32") || var0.equals("fox") || var0.equals("gtk");
   }

   public static boolean isOldWindows() {
      if (getOSPlatform().equals("Windows")) {
         String var0 = System.getProperty("os.version");
         if (var0 != null && (var0.equals("4.0") || var0.equals("4.10") || var0.equals("4.90") || var0.equals("5.0"))) {
            return true;
         }
      }

      return false;
   }

   public static Image getProgramIcon() {
      return oldWindows ? SResources.getImage("ProgramIcon") : SResources.getImage("ProgramIcon-128");
   }

   public static Image getTrayIcon() {
      String var0 = SWT.getPlatform();
      if (var0.equals("gtk")) {   // "motif" was a long-removed SWT platform
         return SResources.getImage("tray-22");
      } else {
         return oldWindows ? SResources.getImage("tray-16") : SResources.getImage("ProgramIcon-128");
      }
   }

   public static String getDownloadLogFile() {
      return getHomeDirectory() + getName() + ".dls";
   }

   public static String getUserHomeDirectory() {
      String var0;
      return (var0 = PreferenceLoader.getHomeDirectory()) != null ? var0 : System.getProperty("user.home");
   }

   public static String getHomeDirectory() {
      String var0;
      return (var0 = PreferenceLoader.getHomeDirectory()) != null
         ? var0
         : System.getProperty("user.home") + System.getProperty("file.separator") + "." + getName() + System.getProperty("file.separator");
   }

   public static String getHomePage() {
      return "http://sancho-gui.sourceforge.net";
   }

   public static String getHomePage2() {
      return "http://sancho.awardspace.com";
   }

   public static String getName() {
      return "sancho";
   }

   public static String getOSPlatform() {
      return System.getProperty("os.name").startsWith("Windows") ? "Windows" : System.getProperty("os.name");
   }

   public static String getShortHomePage() {
      return getHomePage2();
   }

   public static boolean isGNU() {
      return isGNU;
   }

   public static String getSWTPlatform() {
      String var0 = SWT.getPlatform();
      if (var0.equals("fox")) {
         return System.getProperty("os.name").length() > 7 && System.getProperty("os.name").startsWith("Windows") ? "win32-fox" : "fox";
      } else {
         return var0;
      }
   }

   public static boolean isWin95() {
      return isWin95;
   }

   public static String getVersion() {
      return VERSION;
   }

   public static String getBrand() {
      return "";
   }

   // Read the version filtered in from pom.xml (${project.version}) at build time,
   // instead of the number that was baked into the decompiled 0.9.4-59 source.
   private static String readBuildVersion() {
      try (InputStream in = VersionInfo.class.getResourceAsStream("/sancho/version.properties")) {
         if (in != null) {
            Properties p = new Properties();
            p.load(in);
            String v = p.getProperty("version");
            // Guard against the unfiltered placeholder (e.g. running unbuilt sources).
            if (v != null && v.length() > 0 && !v.startsWith("${")) {
               return v;
            }
         }
      } catch (IOException var2) {
      }

      return "0.9.4-dev";
   }

   static {
      VERSION = readBuildVersion();
      String var5 = System.getProperty("os.name");
      isWin95 = var5.equals("Windows 95") || var5.equals("Windows 98") || var5.equals("Windows ME");
      oldWindows = isOldWindows();
      isGNU = System.getProperty("java.vm.name").startsWith("GNU");
   }
}
