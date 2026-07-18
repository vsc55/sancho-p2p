package sancho.view.utility;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import sancho.model.mldonkey.enums.EnumExtension;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;

public class SResources {
   private static ImageRegistry imageRegistry;
   private static Hashtable stringRegistry;
   private static Hashtable ext2NameRegistry;
   private static final String epDirectory = "e/";
   private static final String miscDirectory = "m/";
   private static final String networksDirectory = "n/";
   private static final String flagDirectory = "f/";
   public static final Class imagesClass = SResources.class$sancho$view$MainWindow == null
      ? (SResources.class$sancho$view$MainWindow = class$("sancho.view.MainWindow"))
      : SResources.class$sancho$view$MainWindow;
   public static final String imagesDirectory = "img/";
   public static final String S_ES = "";
   public static final String S_PERCENT = "%";
   public static final String S_SPACE = " ";
   public static final String S_SLASH2 = " / ";
   public static final String S_SLASH = "/";
   public static final String S_COLON = ": ";
   public static final String S_COMMA = ", ";
   public static final String S_OBS = " (";
   public static final String S_OB = "(";
   public static final String S_CB = ")";
   public static final String S_DASH = "-";
   public static final String S_NL = "\n";
   public static final String S_DOT = ".";
   public static final String[] SA_Z = new String[]{"z"};
   public static final String S_0 = "0";
   public static final String S_00 = "00";
   public static final String S_PNG = ".png";
   public static final String S_FTICON_UNKNOWN = "ft_unknown";
   public static final String S_FTICON_PDF = "ft_pdf";
   public static final String S_FTICON_INFO = "ft_info";
   public static final String S_FTICON_ARCHIVE = "ft_archive";
   public static final String S_FTICON_VIDEO = "ft_video";
   public static final String S_FTICON_DOCUMENT = "ft_document";
   public static final String S_FTICON_IMAGE = "ft_image";
   public static final String S_FTICON_SOUND = "ft_sound";
   public static final String S_FTICON_APPLICATION = "ft_application";
   private static final HashMap ppHashMap = new HashMap();
   // $VF: synthetic field
   static Class class$sancho$view$MainWindow;

   private SResources() {
   }

   public static synchronized Program getOSPreviewApp(String var0) {
      if (!ppHashMap.containsKey(var0)) {
         ppHashMap.put(var0, Program.findProgram(var0));
      }

      return (Program)ppHashMap.get(var0);
   }

   public static void initialize() {
      createImageRegistry();
   }

   public static String[] getPreviewApps(String var0) {
      ArrayList var1 = new ArrayList();
      String var2 = PreferenceLoader.loadString("previewExtensions");
      if (var2.equals("")) {
         return null;
      } else {
         StringTokenizer var3 = new StringTokenizer(var2, ";");
         String var4 = "";
         String var5 = "";

         while (var3.hasMoreTokens()) {
            var4 = var3.nextToken();
            if (var3.hasMoreTokens()) {
               var5 = var3.nextToken();
               if (var0.toLowerCase().endsWith(var4.toLowerCase())) {
                  var1.add(var5);
               }
            }
         }

         if (!PreferenceLoader.loadString("previewExecutable").equals("")) {
            var1.add(PreferenceLoader.loadString("previewExecutable"));
         }

         if (var1.size() == 0) {
            return null;
         } else {
            String[] var6 = new String[var1.size()];
            var1.toArray(var6);
            return var6;
         }
      }
   }

   public static String getDefaultIconString(String var0) {
      if (var0.equals("pdf")) {
         return "ft_pdf";
      } else if (var0.equals("nfo")) {
         return "ft_info";
      } else {
         EnumExtension var1 = EnumExtension.GET_EXT(var0);
         if (var1 == EnumExtension.ARCHIVE) {
            return "ft_archive";
         } else if (var1 == EnumExtension.VIDEO) {
            return "ft_video";
         } else if (var1 == EnumExtension.DOCUMENT) {
            return "ft_document";
         } else if (var1 == EnumExtension.IMAGE) {
            return "ft_image";
         } else if (var1 == EnumExtension.AUDIO) {
            return "ft_sound";
         } else {
            return var1 == EnumExtension.PROGRAM ? "ft_application" : "ft_unknown";
         }
      }
   }

   public static synchronized Image getFileTypeImage(String var0) {
      var0 = var0.toLowerCase();
      String var1 = getProgramName(var0);
      if (var1 == null) {
         var1 = updateExt2NameRegistry(var0);
      }

      if (var1.equals("")) {
         return getImage(getDefaultIconString(var0));
      } else {
         Image var2 = getImage(var1);
         return var2 == null ? getImage(getDefaultIconString(var0)) : var2;
      }
   }

   public static synchronized ImageDescriptor getFileTypeImageDescriptor(String var0) {
      var0 = var0.toLowerCase();
      String var1 = getProgramName(var0);
      if (var1 == null) {
         var1 = updateExt2NameRegistry(var0);
      }

      if (var1.equals("")) {
         return getImageDescriptor(getDefaultIconString(var0));
      } else {
         ImageDescriptor var2 = getImageDescriptor(var1);
         return var2 == null ? getImageDescriptor(getDefaultIconString(var0)) : var2;
      }
   }

   public static String getProgramName(String var0) {
      if (ext2NameRegistry == null) {
         ext2NameRegistry = new Hashtable();
      }

      return (String)ext2NameRegistry.get(var0.toLowerCase());
   }

   public static void putProgramName(String var0, String var1) {
      if (ext2NameRegistry == null) {
         ext2NameRegistry = new Hashtable();
      }

      ext2NameRegistry.put(var0.toLowerCase(), var1);
   }

   public static String updateExt2NameRegistry(String var0) {
      String var1 = null;
      Program var2 = null;
      if (!var0.equals("")) {
         var2 = Program.findProgram(var0);
      }

      if (var2 != null) {
         var1 = var2.getName();
         if (getImage(var1) == null) {
            ImageData var3 = var2.getImageData();
            if (var3 != null) {
               if (var3.height != 16 && var3.width != 16) {
                  var3 = var3.scaledTo(16, 16);
               }

               ResourcesImageDescriptor var4 = new ResourcesImageDescriptor(var1, new Image(null, var3));
               putImage(var1, var4);
            }
         }
      } else {
         var1 = "";
      }

      putProgramName(var0, var1);
      return var1;
   }

   public static synchronized Image getImage(String var0) {
      return getImageRegistry().get(var0);
   }

   public static synchronized ImageDescriptor getImageDescriptor(String var0) {
      return getImageRegistry().getDescriptor(var0);
   }

   private static ImageRegistry getImageRegistry() {
      if (imageRegistry == null) {
         imageRegistry = new ImageRegistry();
      }

      return imageRegistry;
   }

   public static synchronized void putImage(String var0, Image var1) {
      try {
         getImageRegistry().put(var0, var1);
      } catch (IllegalArgumentException var3) {
         var3.printStackTrace();
      }
   }

   public static synchronized void putImage(String var0, ImageDescriptor var1) {
      try {
         getImageRegistry().put(var0, var1);
      } catch (IllegalArgumentException var3) {
         var3.printStackTrace();
      }
   }

   public static String getString(String var0) {
      String var1 = (String)stringRegistry.get(var0);
      return var1 == null ? var0 : var1.intern();
   }

   private static void createImageRegistry() {
      ImageRegistry var0 = getImageRegistry();
      var0.put("splashScreen", createRawImage("splash.png"));
      var0.put("splashHighlight", createRawImage("splash-hl.png"));
      var0.put("about", createRawImage("about.png"));
      var0.put("welcome", createRawImage("welcome.png"));
      var0.put("ProgramIcon", createRawImage("icon.png"));
      var0.put("ProgramIcon-12", createRawImage("icon-12.png"));
      var0.put("ProgramIcon-128", createRawImage("icon-128.png"));
      var0.put("tray-16", createRawImage("tray-16.png"));
      var0.put("tray-22", createRawImage("tray-22.png"));
      var0.put("tray-icon", createRawImage("sancho.ico"));
      String[] var1 = new String[]{"statistics", "console", "transfers", "search", "servers", "friends", "shares", "rooms", "webbrowser"};

      for (int var2 = 0; var2 < var1.length; var2++) {
         String var3 = "tab." + var1[var2];
         var0.put(var3 + ".button", createRawImage(var1[var2] + ".png"));
         var0.put(var3 + ".buttonActive", createActiveImage(var0.getDescriptor(var3 + ".button")));
         var0.put(var3 + ".buttonSmall", createRawImage(var1[var2] + "-16.png"));
         var0.put(var3 + ".buttonSmallActive", createActiveImage(var0.getDescriptor("tab." + var1[var2] + ".buttonSmall")));
      }

      var0.put("FriendsButtonSmallBW", createRawImage("friends-16-bw.png"));
      var0.put("FriendsButtonSmallBWPlus", createRawImage("friends-16-bw-plus.png"));
      var0.put("FriendsButtonSmallPlus", createRawImage("friends-16-plus.png"));
      var0.put("rateDownArrow", createRawImage("down.png"));
      var0.put("rateUpArrow", createRawImage("up.png"));
      var0.put("RedCrossSmall", createRawImage("red_cross-12.png"));
      createNetworksIcons(var0);
      createMiscIcons(var0);
      createFlagIcons(var0);
      createEPIcons(var0);
   }

   public static void createMiscIcons(ImageRegistry var0) {
      String[] var1 = new String[]{
         "ep_unknown",
         "ep_transferring",
         "ep_noneeded",
         "ep_connecting",
         "ep_asking",
         "search_small",
         "search_complete",
         "up_arrow_blue",
         "up_arrow_green",
         "down_arrow_green",
         "down_arrow_yellow",
         "x",
         "x-light",
         "toggle",
         "heart",
         "irc",
         "page-forward",
         "page-back",
         "page-refresh",
         "page-stop",
         "jigle",
         "bitzi",
         "sharereactor",
         "info",
         "user",
         "group",
         "cancel",
         "resume",
         "pause",
         "preview",
         "verify",
         "commit",
         "commit_question",
         "edonkey",
         "globe",
         "rotate",
         "collapseall",
         "expandall",
         "display",
         "regedit",
         "lock",
         "arrow-down-green",
         "bulb-small",
         "startup",
         "new-message",
         "plus",
         "forward",
         "back",
         "plus-globe",
         "minus",
         "maximize",
         "restore",
         "table",
         "split-table",
         "copy",
         "home",
         "clear",
         "clear-12",
         "graph",
         "dropdown",
         "menu-disconnect",
         "menu-connect",
         "nuke",
         "cabinet",
         "exclamation",
         "preferences",
         "web-link",
         "web-link-12",
         "web-link-m",
         "web-link-o",
         "gun",
         "refine",
         "defprog",
         "http-add",
         "folder-12",
         "file-explorer",
         "brothers",
         "blizzard",
         "arctic",
         "avicora",
         "pando",
         "tuotu",
         "halite",
         "azureus",
         "mldonkey",
         "abc",
         "bittornado",
         "opera",
         "bitspirit",
         "mainline",
         "shareaza",
         "bitlord",
         "g3",
         "torrentstorm",
         "bitcomet",
         "utorrent",
         "bitpump",
         "qbittorrent",
         "imesh",
         "etomi",
         "hydranode",
         "osprey",
         "libtorrent",
         "ktorrent",
         "360share",
         "xolox",
         "ziptorrent",
         "xantorrent",
         "morph",
         "kazaa",
         "bearshare",
         "limewire",
         "retriever",
         "unix",
         "apache",
         "microsoft",
         "bitsonwheels",
         "lphant",
         "transmission",
         "exeem",
         "razorback2",
         "filehash",
         "magnet",
         "camera",
         "console-12",
         "client",
         "client_check",
         "client_friend",
         "client_friend_check",
         "checkmark",
         "bulb-green",
         "bulb-red",
         "bulb-grey",
         "xtorrent",
         "flashget",
         "deluge",
         "rufus",
         "bitrocket",
         "priorityhigh",
         "prioritylow",
         "prioritynormal",
         "filerating0",
         "filerating1",
         "filerating2",
         "filerating3",
         "filerating4",
         "filerating5",
         "dotrating0",
         "dotrating1",
         "dotrating2",
         "foxtorrent",
         "dotrating3",
         "dotrating4",
         "dotrating5",
         "blank-16",
         "comments",
         "rename",
         "priority",
         "earth",
         "updown",
         "target",
         "sort",
         "dynamic",
         "firewalled",
         "direct",
         "ares",
         "bittyrant",
         "btuga",
         "emugle",
         "ft_archive",
         "ft_document",
         "ft_folder",
         "ft_image",
         "ft_info",
         "ft_pdf",
         "ft_sound",
         "ft_unknown",
         "ft_video",
         "ft_application",
         "amule",
         "emule",
         "lmule",
         "hybrid",
         "cdonkey",
         "client_unknown",
         "vagaa",
         "artemis",
         "wyzo",
         "tribler",
         "symtorrent",
         "hurricane",
         "transparency"
      };

      for (int var2 = 0; var2 < var1.length; var2++) {
         var0.put(var1[var2], createID_M(var1[var2]));
      }

      for (int var3 = 1; var3 < 10; var3++) {
         var0.put(String.valueOf(var3), createID_M(String.valueOf(var3)));
      }
   }

   public static void createFlagIcons(ImageRegistry var0) {
      String[] var1 = new String[]{
         "--",
         "a1",
         "a2",
         "ad",
         "ae",
         "af",
         "ag",
         "ai",
         "al",
         "am",
         "an",
         "ao",
         "ap",
         "aq",
         "ar",
         "as",
         "at",
         "au",
         "aw",
         "az",
         "ba",
         "bb",
         "bd",
         "be",
         "bf",
         "bg",
         "bh",
         "bi",
         "bj",
         "bm",
         "bn",
         "bo",
         "br",
         "bs",
         "bt",
         "bv",
         "bw",
         "by",
         "bz",
         "ca",
         "cc",
         "cd",
         "cf",
         "cg",
         "ch",
         "ci",
         "ck",
         "cl",
         "cm",
         "cn",
         "co",
         "cr",
         "cu",
         "cv",
         "cx",
         "cy",
         "cz",
         "de",
         "dj",
         "dk",
         "dm",
         "do",
         "dz",
         "ec",
         "ee",
         "eg",
         "eh",
         "er",
         "es",
         "et",
         "eu",
         "fi",
         "fj",
         "fk",
         "fm",
         "fo",
         "fr",
         "fx",
         "ga",
         "gb",
         "gd",
         "ge",
         "gf",
         "gh",
         "gi",
         "gl",
         "gm",
         "gn",
         "gp",
         "gq",
         "gr",
         "gs",
         "gt",
         "gu",
         "gw",
         "gy",
         "hk",
         "hm",
         "hn",
         "hr",
         "ht",
         "hu",
         "id",
         "ie",
         "il",
         "in",
         "io",
         "iq",
         "ir",
         "is",
         "it",
         "jm",
         "jo",
         "jp",
         "ke",
         "kg",
         "kh",
         "ki",
         "km",
         "kn",
         "kp",
         "kr",
         "kw",
         "ky",
         "kz",
         "la",
         "lb",
         "lc",
         "li",
         "lk",
         "lr",
         "ls",
         "lt",
         "lu",
         "lv",
         "ly",
         "ma",
         "mc",
         "md",
         "mg",
         "mh",
         "mk",
         "ml",
         "mm",
         "mn",
         "mo",
         "mp",
         "mq",
         "mr",
         "ms",
         "mt",
         "mu",
         "mv",
         "mw",
         "mx",
         "my",
         "mz",
         "na",
         "nc",
         "ne",
         "nf",
         "ng",
         "ni",
         "nl",
         "no",
         "np",
         "nr",
         "nu",
         "nz",
         "o1",
         "om",
         "pa",
         "pe",
         "pf",
         "pg",
         "ph",
         "pk",
         "pl",
         "pm",
         "pn",
         "pr",
         "ps",
         "pt",
         "pw",
         "py",
         "qa",
         "re",
         "ro",
         "ru",
         "rw",
         "sa",
         "sb",
         "sc",
         "sd",
         "se",
         "sg",
         "sh",
         "si",
         "sj",
         "sk",
         "sl",
         "sm",
         "sn",
         "so",
         "sr",
         "st",
         "sv",
         "sy",
         "sz",
         "tc",
         "td",
         "tf",
         "tg",
         "th",
         "tj",
         "tk",
         "tm",
         "tn",
         "to",
         "tp",
         "tr",
         "tt",
         "tv",
         "tw",
         "tz",
         "ua",
         "ug",
         "um",
         "us",
         "uy",
         "uz",
         "va",
         "vc",
         "ve",
         "vg",
         "vi",
         "vn",
         "vu",
         "wf",
         "ws",
         "ye",
         "yt",
         "yu",
         "za",
         "zm",
         "zr",
         "zw",
         "tl",
         "ax",
         "gg",
         "im",
         "je",
         "rs",
         "me"
      };

      for (int var2 = 0; var2 < var1.length; var2++) {
         var0.put("f_" + var1[var2], createID_F(var1[var2]));
      }
   }

   public static void createEPIcons(ImageRegistry var0) {
      var0.put("epRatingPoor", createID_E("ep_rating_poor"));
      var0.put("epRatingFair", createID_E("ep_rating_fair"));
      var0.put("epRatingGood", createID_E("ep_rating_good"));
      var0.put("epRatingExcellent", createID_E("ep_rating_excellent"));
      var0.put("epRatingFake", createID_E("ep_rating_fake"));
   }

   public static void createNetworksIcons(ImageRegistry var0) {
      String[] var1 = new String[]{"directconnect", "donkey", "gnutella", "gnutella2", "fasttrack", "soulseek", "opennap", "unknown"};

      for (int var3 = 0; var3 < var1.length; var3++) {
         String var2 = "e.network." + var1[var3];
         var0.put(var2 + ".connected", createID_N(var1[var3] + "_connected"));
         var0.put(var2 + ".connected-16", createID_N(var1[var3] + "_connected-16"));
         var0.put(var2 + ".disconnected", createID_N(var1[var3] + "_disconnected"));
         var0.put(var2 + ".disabled", createID_N(var1[var3] + "_disabled"));
         var0.put(var2 + ".badconnect", createID_N(var1[var3] + "_badconnect"));
      }

      var1 = new String[]{"bittorrent", "multinet", "filetp"};

      for (int var4 = 0; var4 < var1.length; var4++) {
         String var6 = "e.network." + var1[var4];
         var0.put(var6 + ".connected", createID_N(var1[var4] + "_connected"));
         var0.put(var6 + ".connected-16", createID_N(var1[var4] + "_connected-16"));
         var0.put(var6 + ".disabled", createID_N(var1[var4] + "_disabled"));
      }
   }

   public static Image createActiveImage(ImageDescriptor var0) {
      return createActiveImage(null, var0.getImageData());
   }

   public static Image createActiveImage(Display var0, ImageData var1) {
      Image var2 = new Image(var0, var1);
      GC var3 = new GC(var2);
      PaletteData var4 = var1.palette;

      for (int var5 = 0; var5 < var1.width; var5++) {
         for (int var6 = 0; var6 < var1.height; var6++) {
            int var7 = var1.getPixel(var5, var6);
            RGB var8 = var4.getRGB(var7);
            if (var7 != var1.transparentPixel) {
               Color var9 = WidgetFactory.changeColor(var8, 20, 255);
               var3.setForeground(var9);
               var3.drawPoint(var5, var6);
               var9.dispose();
            }
         }
      }

      var3.dispose();
      return var2;
   }

   private static ImageDescriptor createRawImage(String var0) {
      return new MyImageDescriptor(var0);
   }

   private static ImageDescriptor createID_M(String var0) {
      return createRawImage("m/" + var0 + ".png");
   }

   private static ImageDescriptor createID_F(String var0) {
      return createRawImage("f/" + var0 + ".png");
   }

   private static ImageDescriptor createID_N(String var0) {
      return createRawImage("n/" + var0 + ".png");
   }

   private static ImageDescriptor createID_E(String var0) {
      return createRawImage("e/" + var0 + ".png");
   }

   // $VF: synthetic method
   static Class class$(String var0) {
      try {
         return Class.forName(var0);
      } catch (ClassNotFoundException var2) {
         throw new NoClassDefFoundError(var2.getMessage());
      }
   }

   static {
      String localeCode = PreferenceLoader.loadString("locale");
      if (PreferenceLoader.getLocaleString() != null) {
         localeCode = PreferenceLoader.getLocaleString();
      }

      // Language is driven by the "locale" preference (or the -locale option), never by
      // the OS locale: this Control disables ResourceBundle's default-locale fallback, so
      // an unset or unknown locale resolves to the bundled English base, not the system
      // one. Translations (sancho_<locale>.properties) ship on the classpath in the jar;
      // a matching file dropped in the home dir still overrides them.
      ResourceBundle.Control noOsFallback = new ResourceBundle.Control() {
         public Locale getFallbackLocale(String baseName, Locale locale) {
            return null;
         }
      };
      String homeDir = VersionInfo.getHomeDirectory();
      String bundleName = VersionInfo.getName();
      ClassLoader classpathLoader = SResources.class.getClassLoader();
      ResourceBundle bundle;
      try {
         if (localeCode == null || localeCode.equals("")) {
            bundle = ResourceBundle.getBundle(bundleName, Locale.ROOT, classpathLoader, noOsFallback);
         } else {
            String language = "";
            String country = "";
            String variant = "";
            StringTokenizer localeParts = new StringTokenizer(localeCode, "_");
            if (localeParts.countTokens() > 0) {
               language = localeParts.nextToken();
            }

            if (localeParts.countTokens() > 0) {
               country = localeParts.nextToken();
            }

            if (localeParts.countTokens() > 0) {
               variant = localeParts.nextToken();
            }

            Locale locale = new Locale(language, country, variant);
            File homeOverride = new File(homeDir + bundleName + "_" + localeCode + ".properties");
            if (homeOverride.exists()) {
               URLClassLoader homeLoader = new URLClassLoader(new URL[]{new URL("file:///" + homeDir)}, classpathLoader);
               bundle = ResourceBundle.getBundle(bundleName, locale, homeLoader, noOsFallback);
            } else {
               bundle = ResourceBundle.getBundle(bundleName, locale, classpathLoader, noOsFallback);
            }
         }
      } catch (Exception var11) {
         bundle = ResourceBundle.getBundle(bundleName, Locale.ROOT, classpathLoader, noOsFallback);
      }

      stringRegistry = new Hashtable();
      Enumeration keys = bundle.getKeys();

      while (keys.hasMoreElements()) {
         String key = (String)keys.nextElement();
         String value = bundle.getString(key);
         stringRegistry.put(key.intern(), value.intern());
      }
   }
}
