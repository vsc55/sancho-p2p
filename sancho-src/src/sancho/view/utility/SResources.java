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

   public static synchronized Program getOSPreviewApp(String extension) {
      if (!ppHashMap.containsKey(extension)) {
         ppHashMap.put(extension, Program.findProgram(extension));
      }

      return (Program)ppHashMap.get(extension);
   }

   public static void initialize() {
      createImageRegistry();
   }

   public static String[] getPreviewApps(String fileName) {
      ArrayList apps = new ArrayList();
      String previewExtensions = PreferenceLoader.loadString("previewExtensions");
      if (previewExtensions.equals("")) {
         return null;
      } else {
         StringTokenizer tokenizer = new StringTokenizer(previewExtensions, ";");
         String extension = "";
         String app = "";

         while (tokenizer.hasMoreTokens()) {
            extension = tokenizer.nextToken();
            if (tokenizer.hasMoreTokens()) {
               app = tokenizer.nextToken();
               if (fileName.toLowerCase().endsWith(extension.toLowerCase())) {
                  apps.add(app);
               }
            }
         }

         if (!PreferenceLoader.loadString("previewExecutable").equals("")) {
            apps.add(PreferenceLoader.loadString("previewExecutable"));
         }

         if (apps.size() == 0) {
            return null;
         } else {
            String[] result = new String[apps.size()];
            apps.toArray(result);
            return result;
         }
      }
   }

   public static String getDefaultIconString(String extension) {
      if (extension.equals("pdf")) {
         return "ft_pdf";
      } else if (extension.equals("nfo")) {
         return "ft_info";
      } else {
         EnumExtension extType = EnumExtension.GET_EXT(extension);
         if (extType == EnumExtension.ARCHIVE) {
            return "ft_archive";
         } else if (extType == EnumExtension.VIDEO) {
            return "ft_video";
         } else if (extType == EnumExtension.DOCUMENT) {
            return "ft_document";
         } else if (extType == EnumExtension.IMAGE) {
            return "ft_image";
         } else if (extType == EnumExtension.AUDIO) {
            return "ft_sound";
         } else {
            return extType == EnumExtension.PROGRAM ? "ft_application" : "ft_unknown";
         }
      }
   }

   public static synchronized Image getFileTypeImage(String extension) {
      extension = extension.toLowerCase();
      String programName = getProgramName(extension);
      if (programName == null) {
         programName = updateExt2NameRegistry(extension);
      }

      if (programName.equals("")) {
         return getImage(getDefaultIconString(extension));
      } else {
         Image image = getImage(programName);
         return image == null ? getImage(getDefaultIconString(extension)) : image;
      }
   }

   public static synchronized ImageDescriptor getFileTypeImageDescriptor(String extension) {
      extension = extension.toLowerCase();
      String programName = getProgramName(extension);
      if (programName == null) {
         programName = updateExt2NameRegistry(extension);
      }

      if (programName.equals("")) {
         return getImageDescriptor(getDefaultIconString(extension));
      } else {
         ImageDescriptor descriptor = getImageDescriptor(programName);
         return descriptor == null ? getImageDescriptor(getDefaultIconString(extension)) : descriptor;
      }
   }

   public static String getProgramName(String extension) {
      if (ext2NameRegistry == null) {
         ext2NameRegistry = new Hashtable();
      }

      return (String)ext2NameRegistry.get(extension.toLowerCase());
   }

   public static void putProgramName(String extension, String name) {
      if (ext2NameRegistry == null) {
         ext2NameRegistry = new Hashtable();
      }

      ext2NameRegistry.put(extension.toLowerCase(), name);
   }

   public static String updateExt2NameRegistry(String extension) {
      String programName = null;
      Program program = null;
      if (!extension.equals("")) {
         program = Program.findProgram(extension);
      }

      if (program != null) {
         programName = program.getName();
         if (getImage(programName) == null) {
            ImageData imageData = program.getImageData();
            if (imageData != null) {
               if (imageData.height != 16 && imageData.width != 16) {
                  imageData = imageData.scaledTo(16, 16);
               }

               ResourcesImageDescriptor descriptor = new ResourcesImageDescriptor(programName, new Image(null, imageData));
               putImage(programName, descriptor);
            }
         }
      } else {
         programName = "";
      }

      putProgramName(extension, programName);
      return programName;
   }

   public static synchronized Image getImage(String name) {
      return getImageRegistry().get(name);
   }

   public static synchronized ImageDescriptor getImageDescriptor(String name) {
      return getImageRegistry().getDescriptor(name);
   }

   private static ImageRegistry getImageRegistry() {
      if (imageRegistry == null) {
         imageRegistry = new ImageRegistry();
      }

      return imageRegistry;
   }

   public static synchronized void putImage(String name, Image image) {
      try {
         getImageRegistry().put(name, image);
      } catch (IllegalArgumentException illegalArgument) {
         illegalArgument.printStackTrace();
      }
   }

   public static synchronized void putImage(String name, ImageDescriptor descriptor) {
      try {
         getImageRegistry().put(name, descriptor);
      } catch (IllegalArgumentException illegalArgument) {
         illegalArgument.printStackTrace();
      }
   }

   public static String getString(String key) {
      String value = (String)stringRegistry.get(key);
      return value == null ? key : value.intern();
   }

   private static void createImageRegistry() {
      ImageRegistry registry = getImageRegistry();
      registry.put("splashScreen", createRawImage("splash.png"));
      registry.put("splashHighlight", createRawImage("splash-hl.png"));
      registry.put("about", createRawImage("about.png"));
      registry.put("welcome", createRawImage("welcome.png"));
      registry.put("ProgramIcon", createRawImage("icon.png"));
      registry.put("ProgramIcon-12", createRawImage("icon-12.png"));
      registry.put("ProgramIcon-128", createRawImage("icon-128.png"));
      registry.put("tray-16", createRawImage("tray-16.png"));
      registry.put("tray-22", createRawImage("tray-22.png"));
      registry.put("tray-icon", createRawImage("sancho.ico"));
      String[] tabNames = new String[]{"statistics", "console", "transfers", "search", "servers", "friends", "shares", "rooms", "webbrowser"};

      for (int i = 0; i < tabNames.length; i++) {
         String key = "tab." + tabNames[i];
         registry.put(key + ".button", createRawImage(tabNames[i] + ".png"));
         registry.put(key + ".buttonActive", createActiveImage(registry.getDescriptor(key + ".button")));
         registry.put(key + ".buttonSmall", createRawImage(tabNames[i] + "-16.png"));
         registry.put(key + ".buttonSmallActive", createActiveImage(registry.getDescriptor("tab." + tabNames[i] + ".buttonSmall")));
      }

      registry.put("FriendsButtonSmallBW", createRawImage("friends-16-bw.png"));
      registry.put("FriendsButtonSmallBWPlus", createRawImage("friends-16-bw-plus.png"));
      registry.put("FriendsButtonSmallPlus", createRawImage("friends-16-plus.png"));
      registry.put("rateDownArrow", createRawImage("down.png"));
      registry.put("rateUpArrow", createRawImage("up.png"));
      registry.put("RedCrossSmall", createRawImage("red_cross-12.png"));
      createNetworksIcons(registry);
      createMiscIcons(registry);
      createFlagIcons(registry);
      createEPIcons(registry);
   }

   public static void createMiscIcons(ImageRegistry registry) {
      String[] names = new String[]{
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

      for (int i = 0; i < names.length; i++) {
         registry.put(names[i], createID_M(names[i]));
      }

      for (int digit = 1; digit < 10; digit++) {
         registry.put(String.valueOf(digit), createID_M(String.valueOf(digit)));
      }
   }

   public static void createFlagIcons(ImageRegistry registry) {
      String[] names = new String[]{
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

      for (int i = 0; i < names.length; i++) {
         registry.put("f_" + names[i], createID_F(names[i]));
      }
   }

   public static void createEPIcons(ImageRegistry registry) {
      registry.put("epRatingPoor", createID_E("ep_rating_poor"));
      registry.put("epRatingFair", createID_E("ep_rating_fair"));
      registry.put("epRatingGood", createID_E("ep_rating_good"));
      registry.put("epRatingExcellent", createID_E("ep_rating_excellent"));
      registry.put("epRatingFake", createID_E("ep_rating_fake"));
   }

   public static void createNetworksIcons(ImageRegistry registry) {
      String[] names = new String[]{"directconnect", "donkey", "gnutella", "gnutella2", "fasttrack", "soulseek", "opennap", "unknown"};

      for (int i = 0; i < names.length; i++) {
         String key = "e.network." + names[i];
         registry.put(key + ".connected", createID_N(names[i] + "_connected"));
         registry.put(key + ".connected-16", createID_N(names[i] + "_connected-16"));
         registry.put(key + ".disconnected", createID_N(names[i] + "_disconnected"));
         registry.put(key + ".disabled", createID_N(names[i] + "_disabled"));
         registry.put(key + ".badconnect", createID_N(names[i] + "_badconnect"));
      }

      names = new String[]{"bittorrent", "multinet", "filetp"};

      for (int i = 0; i < names.length; i++) {
         String key = "e.network." + names[i];
         registry.put(key + ".connected", createID_N(names[i] + "_connected"));
         registry.put(key + ".connected-16", createID_N(names[i] + "_connected-16"));
         registry.put(key + ".disabled", createID_N(names[i] + "_disabled"));
      }
   }

   public static Image createActiveImage(ImageDescriptor descriptor) {
      return createActiveImage(null, descriptor.getImageData());
   }

   // Build the "highlighted" variant of a tab button (shown on hover / when active) by
   // brightening every visible pixel and leaving the transparent ones alone.
   //
   // A pixel is transparent either via the palette's transparentPixel (indexed GIF/PNG)
   // OR via the alpha channel (32-bit RGBA PNG). The previous version only honoured
   // transparentPixel, so on modern SWT — which loads RGBA PNGs with an alpha channel and
   // transparentPixel = -1 — the fully transparent background counted as "visible" and got
   // painted with whatever RGB sat under the transparent pixels: a black or white box
   // behind the small transfers/search/console icons (the only 32-bit ones). Honouring the
   // alpha channel too, and writing the result as a fresh 32-bit image so per-pixel alpha
   // (including anti-aliased edges) is preserved, fixes it for both icon formats.
   public static Image createActiveImage(Display display, ImageData imageData) {
      PaletteData sourcePalette = imageData.palette;
      PaletteData rgbaPalette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
      ImageData active = new ImageData(imageData.width, imageData.height, 24, rgbaPalette);
      active.alphaData = new byte[imageData.width * imageData.height];

      for (int x = 0; x < imageData.width; x++) {
         for (int y = 0; y < imageData.height; y++) {
            int pixel = imageData.getPixel(x, y);
            int alpha = pixel == imageData.transparentPixel ? 0 : imageData.getAlpha(x, y);
            if (alpha == 0) {
               active.setAlpha(x, y, 0);
               continue;
            }

            Color color = WidgetFactory.changeColor(sourcePalette.getRGB(pixel), 20, 255);
            active.setPixel(x, y, rgbaPalette.getPixel(color.getRGB()));
            active.setAlpha(x, y, alpha);
            color.dispose();
         }
      }

      return new Image(display, active);
   }

   private static ImageDescriptor createRawImage(String path) {
      return new MyImageDescriptor(path);
   }

   private static ImageDescriptor createID_M(String name) {
      return createRawImage("m/" + name + ".png");
   }

   private static ImageDescriptor createID_F(String name) {
      return createRawImage("f/" + name + ".png");
   }

   private static ImageDescriptor createID_N(String name) {
      return createRawImage("n/" + name + ".png");
   }

   private static ImageDescriptor createID_E(String name) {
      return createRawImage("e/" + name + ".png");
   }

   // $VF: synthetic method
   static Class class$(String className) {
      try {
         return Class.forName(className);
      } catch (ClassNotFoundException notFound) {
         throw new NoClassDefFoundError(notFound.getMessage());
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
      } catch (Exception exception) {
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
