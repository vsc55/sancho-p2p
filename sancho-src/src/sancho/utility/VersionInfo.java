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
      String platform = SWT.getPlatform();
      return platform.equals("win32") || platform.equals("fox") || platform.equals("gtk");
   }

   public static Image getProgramIcon() {
      return SResources.getImage("ProgramIcon-128");
   }

   public static Image getTrayIcon() {
      String platform = SWT.getPlatform();
      if (platform.equals("gtk")) {   // "motif" was a long-removed SWT platform
         return SResources.getImage("tray-22");
      } else {
         return SResources.getImage("ProgramIcon-128");
      }
   }

   public static String getDownloadLogFile() {
      return getHomeDirectory() + getName() + ".dls";
   }

   public static String getUserHomeDirectory() {
      String homeDirectory;
      return (homeDirectory = PreferenceLoader.getHomeDirectory()) != null ? homeDirectory : System.getProperty("user.home");
   }

   public static String getHomeDirectory() {
      String homeDirectory;
      return (homeDirectory = PreferenceLoader.getHomeDirectory()) != null
         ? homeDirectory
         : System.getProperty("user.home") + System.getProperty("file.separator") + "." + getName() + System.getProperty("file.separator");
   }

   public static String getHomePage() {
      return "http://sancho-gui.sourceforge.net";
   }

   public static String getHomePage2() {
      return "http://sancho.awardspace.com";
   }

   // Update check targets this fork's GitHub releases over HTTPS. The original
   // http://sancho.awardspace.com/version.php host is long dead.
   public static String getReleasesApiURL() {
      return "https://api.github.com/repos/vsc55/sancho-p2p/releases/latest";
   }

   public static String getReleasesPage() {
      return "https://github.com/vsc55/sancho-p2p/releases";
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
      String platform = SWT.getPlatform();
      if (platform.equals("fox")) {
         return System.getProperty("os.name").length() > 7 && System.getProperty("os.name").startsWith("Windows") ? "win32-fox" : "fox";
      } else {
         return platform;
      }
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
      } catch (IOException ioException) {
      }

      return "0.9.4-dev";
   }

   static {
      VERSION = readBuildVersion();
      isGNU = System.getProperty("java.vm.name").startsWith("GNU");
   }
}
