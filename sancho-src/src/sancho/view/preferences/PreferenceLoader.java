package sancho.view.preferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import sancho.model.mldonkey.enums.EnumHostState;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.utility.IDSelector;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class PreferenceLoader {
   private static List colorArray = new ArrayList();
   private static Map colorMap = new Hashtable();
   private static List fontArray = new ArrayList();
   private static Map fontMap = new Hashtable();
   private static PreferenceStore preferenceStore = null;
   public static boolean customPrefFile;
   public static boolean customHomeDir;
   public static String jvm;
   private static String prefFileName;
   private static String localeString;
   private static String homeDirectory;
   public static String ENTRY_SEPARATOR = ";";

   private PreferenceLoader() {
   }

   public static void cleanUp() {
      for (int var0 = 0; var0 < fontArray.size(); var0++) {
         Font var1 = (Font)fontArray.get(var0);
         if (var1 != null && !var1.isDisposed()) {
            var1.dispose();
         }
      }

      for (int var3 = 0; var3 < colorArray.size(); var3++) {
         Color var2 = (Color)colorArray.get(var3);
         if (var2 != null && !var2.isDisposed()) {
            var2.dispose();
         }
      }
   }

   public static boolean contains(String var0) {
      return preferenceStore.contains(var0);
   }

   public static PreferenceStore getPreferenceStore() {
      return preferenceStore;
   }

   public static String getLocaleString() {
      return localeString;
   }

   public static String getPrefFile() {
      return prefFileName;
   }

   public static void initialize() throws IOException {
      if (preferenceStore == null) {
         prefFileName = VersionInfo.getName() + ".pref";
         if (new File(prefFileName).exists()) {
            preferenceStore = new PreferenceStore(prefFileName);
         } else {
            prefFileName = VersionInfo.getHomeDirectory() + prefFileName;
            File var0 = new File(prefFileName);
            if (!var0.exists()) {
               File var1 = new File(var0.getParent());
               var1.mkdirs();
            }

            preferenceStore = new PreferenceStore(prefFileName);
         }
      }

      try {
         preferenceStore.load();
      } catch (IOException var2) {
         preferenceStore.save();
      }
   }

   public static void initialize2() {
      preferenceStore = (PreferenceStore)setDefaults(preferenceStore);
   }

   public static boolean loadBoolean(String var0) {
      return preferenceStore.contains(var0) ? preferenceStore.getBoolean(var0) : false;
   }

   public static RGB loadRGB(String var0) {
      return preferenceStore.contains(var0) ? PreferenceConverter.getColor(preferenceStore, var0) : new RGB(0, 0, 0);
   }

   public static Color loadColor(String var0) {
      if (!preferenceStore.contains(var0)) {
         return null;
      } else {
         Color var1 = new Color(null, PreferenceConverter.getColor(preferenceStore, var0));
         if (colorMap.containsKey(var0) && !((Color)colorMap.get(var0)).isDisposed()) {
            if (var1.getRGB().equals(((Color)colorMap.get(var0)).getRGB())) {
               var1.dispose();
            } else {
               colorArray.add(var1);
               colorMap.put(var0, var1);
            }
         } else {
            colorArray.add(var1);
            colorMap.put(var0, var1);
         }

         return (Color)colorMap.get(var0);
      }
   }

   public static Font loadFont(String var0) {
      if (!preferenceStore.contains(var0)) {
         return null;
      } else {
         Font var1 = new Font(null, PreferenceConverter.getFontDataArray(preferenceStore, var0));
         if (fontMap.containsKey(var0) && !((Font)fontMap.get(var0)).isDisposed()) {
            if (var1.getFontData()[0].equals(((Font)fontMap.get(var0)).getFontData()[0])) {
               var1.dispose();
            } else {
               fontArray.add(var1);
               fontMap.put(var0, var1);
            }
         } else {
            fontArray.add(var1);
            fontMap.put(var0, var1);
         }

         return (Font)fontMap.get(var0);
      }
   }

   public static int loadInt(String var0) {
      return preferenceStore.contains(var0) ? preferenceStore.getInt(var0) : 0;
   }

   public static int loadIntOrN1(String var0) {
      return preferenceStore.contains(var0) ? preferenceStore.getInt(var0) : -1;
   }

   public static int loadOrientation(String var0) {
      if (preferenceStore.contains(var0)) {
         int var1 = preferenceStore.getInt(var0);
         if (var1 == 512 || var1 == 256) {
            return var1;
         }
      }

      return 256;
   }

   public static Rectangle loadRectangle(String var0) {
      return preferenceStore.contains(var0) ? PreferenceConverter.getRectangle(preferenceStore, var0) : null;
   }

   public static String loadStringEnv(String var0) {
      return SwissArmy.replaceEnvVars(loadString(var0));
   }

   public static String loadString(String var0) {
      return preferenceStore.contains(var0) ? preferenceStore.getString(var0).intern() : "";
   }

   public static String[] loadStringArray(String var0) {
      StringTokenizer var1 = new StringTokenizer(loadString(var0), ENTRY_SEPARATOR);
      int var2 = var1.countTokens();
      String[] var3 = new String[var2];

      for (int var4 = 0; var4 < var2; var4++) {
         var3[var4] = var1.nextToken();
      }

      return var3;
   }

   public static void setValue(String var0, String[] var1) {
      StringBuffer var2 = new StringBuffer();

      for (int var3 = 0; var1 != null && var3 < var1.length; var3++) {
         var2.append(SwissArmy.replaceAll(var1[var3], ENTRY_SEPARATOR, ""));
         var2.append(ENTRY_SEPARATOR);
      }

      preferenceStore.setValue(var0, var2.toString().intern());
   }

   public static void setValue(String var0, String[] var1, int var2) {
      if (var1.length > var2) {
         String[] var3 = new String[var2];

         for (int var4 = 0; var4 < var2; var4++) {
            var3[var4] = var1[var4];
         }

         var1 = var3;
      }

      setValue(var0, var1);
   }

   public static void saveStore() {
      try {
         preferenceStore.save();
      } catch (IOException var1) {
         var1.printStackTrace();
      }
   }

   static void setDColor(IPreferenceStore var0, Display var1, String var2, int var3) {
      PreferenceConverter.setDefault(var0, var2, var1.getSystemColor(var3).getRGB());
   }

   public static String[] getChunkStrings() {
      return new String[]{
         "clientCColor2",
         "clientAColor0",
         "clientAColor1",
         "clientAColor2",
         "fileCColor1",
         "fileCColor2",
         "fileCColor3",
         "fileAColor0",
         "fileIRGB",
         "progress1",
         "progress2",
         "text"
      };
   }

   public static void setRGB(String var0, RGB var1) {
      if (preferenceStore != null) {
         PreferenceConverter.setValue(preferenceStore, var0, var1);
      }
   }

   static IPreferenceStore setDefaults(IPreferenceStore var0) {
      Display var1 = Display.getDefault();
      FontData[] var2 = JFaceResources.getDefaultFont().getFontData();
      var0.setDefault("initialized", false);
      var0.setDefault("windowMaximized", false);
      var0.setDefault("windowAlpha", 255);
      var0.setDefault("windowStartMinimized", false);
      var0.setDefault("windowStartTray", false);
      var0.setDefault("coolbarLocked", true);
      var0.setDefault("toolbarSmallButtons", true);
      var0.setDefault("flatInterface", false);
      var0.setDefault("useGraident", true);
      var0.setDefault("splashScreen", true);
      var0.setDefault("killCoreOnExit", false);
      var0.setDefault("killSpawnedCoreOnExit", true);
      var0.setDefault("killSpawnedCoreDelay", 60);
      var0.setDefault("hostManagerOnStart", false);
      var0.setDefault("downloadCompleteDialog", false);
      var0.setDefault("downloadCompleteLog", true);
      var0.setDefault("explorerExecutable", SWT.getPlatform().equals("win32") ? "explorer" : "");
      var0.setDefault("explorerOpenFolder", "");
      var0.setDefault("linkRipperShowAll", false);
      var0.setDefault("dlFileDoubleClick", 0);
      var0.setDefault("dlPercentDecimals", 0);
      var0.setDefault("dlRateDecimals", 1);
      var0.setDefault("dndBox", false);
      var0.setDefault("ircAutoConnect", false);
      var0.setDefault("ircServer", "irc.freenode.net");
      var0.setDefault("ircNickname", SwissArmy.getRandomString(7));
      var0.setDefault("ircChannel", "#mldonkey");
      var0.setDefault("consoleMaxLines", 300);
      var0.setDefault("consoleUnderlineURLs", false);
      var0.setDefault("maxMenuItems", 10);
      var0.setDefault("connectionTimeout", 20);
      var0.setDefault("mldonkey.InterestedInSources", true);
      var0.setDefault("tableAlternateBGColors", false);
      setDColor(var0, var1, "tableAlternateBGColor", 29);
      setDColor(var0, var1, "tablesBackgroundColor", 25);
      var0.setDefault("tableHilightSorted", false);
      var0.setDefault("tableSortIndicator", true);   // was !"carbon" (removed SWT platform); always true now
      setDColor(var0, var1, "tableSortedColumnBGColor", 29);
      PreferenceConverter.setDefault(var0, "downloadHistoryWindowBounds", new Rectangle(0, 0, 500, 400));
      PreferenceConverter.setDefault(var0, "downloadCompleteWindowBounds", new Rectangle(0, 0, 320, 300));
      PreferenceConverter.setDefault(var0, "windowBounds", new Rectangle(40, 40, 580, 420));
      PreferenceConverter.setDefault(var0, "graphHistoryWindowBounds", new Rectangle(40, 40, 580, 420));
      PreferenceConverter.setDefault(var0, "dndBoxWindowBounds", new Rectangle(-1, 0, 0, 0));
      PreferenceConverter.setDefault(var0, "ircWindowBounds", new Rectangle(40, 40, 580, 420));
      setDColor(var0, var1, "dndBackgroundColor", 22);
      setDColor(var0, var1, "dndForegroundColor", 2);
      PreferenceConverter.setDefault(var0, "dndFontData", var2);
      var0.setDefault("dndWidth", 15);
      setDColor(var0, var1, "consoleBackground", 2);
      setDColor(var0, var1, "consoleForeground", 15);
      setDColor(var0, var1, "consoleHighlight", 1);
      setDColor(var0, var1, "consoleInputBackground", 2);
      setDColor(var0, var1, "consoleInputForeground", 15);
      PreferenceConverter.setDefault(var0, "consoleFontData", JFaceResources.getTextFont().getFontData());
      setDColor(var0, var1, "ircConsoleBackground", 25);
      setDColor(var0, var1, "ircConsoleForeground", 24);
      setDColor(var0, var1, "ircConsoleHighlight", 26);
      setDColor(var0, var1, "ircConsoleInputBackground", 25);
      setDColor(var0, var1, "ircConsoleInputForeground", 24);
      PreferenceConverter.setDefault(var0, "ircConsoleFontData", JFaceResources.getTextFont().getFontData());
      setDColor(var0, var1, "clientsDisconnectedColor", 2);
      setDColor(var0, var1, "clientsHasFilesColor", 6);
      setDColor(var0, var1, "clientsConnectedColor", 8);
      setDColor(var0, var1, "downloadsBackgroundColor", 25);
      setDColor(var0, var1, "downloadsAvailableFileColor", 2);
      PreferenceConverter.setDefault(var0, "downloadsUnAvailableFileColor", new RGB(128, 128, 128));
      setDColor(var0, var1, "downloadsDownloadedFileColor", 9);
      setDColor(var0, var1, "downloadsQueuedFileColor", 15);
      setDColor(var0, var1, "downloadsPausedFileColor", 4);
      setDColor(var0, var1, "downloadsContainsFakeColor", 3);
      var0.setDefault("dlIndicateFakes", true);
      PreferenceConverter.setDefault(var0, "downloadsRateAbove20FileColor", new RGB(0, 160, 0));
      PreferenceConverter.setDefault(var0, "downloadsRateAbove10FileColor", new RGB(0, 140, 0));
      PreferenceConverter.setDefault(var0, "downloadsRateAbove0FileColor", new RGB(0, 110, 0));
      PreferenceConverter.setDefault(var0, "downloadsRateAbove20FontData", var2);
      PreferenceConverter.setDefault(var0, "downloadsRateAbove10FontData", var2);
      PreferenceConverter.setDefault(var0, "downloadsRateAbove0FontData", var2);
      PreferenceConverter.setDefault(var0, "downloadsPausedFontData", var2);
      PreferenceConverter.setDefault(var0, "downloadsQueuedFontData", var2);
      PreferenceConverter.setDefault(var0, "downloadsDownloadedFontData", var2);
      PreferenceConverter.setDefault(var0, "chunkClientCColor2", new RGB(107, 81, 9));
      PreferenceConverter.setDefault(var0, "chunkClientAColor0", new RGB(226, 225, 221));
      PreferenceConverter.setDefault(var0, "chunkClientAColor1", new RGB(0, 150, 255));
      setDColor(var0, var1, "chunkClientAColor2", 7);
      PreferenceConverter.setDefault(var0, "chunkFileCColor1", new RGB(128, 128, 128));
      PreferenceConverter.setDefault(var0, "chunkFileCColor2", new RGB(107, 81, 9));
      setDColor(var0, var1, "chunkFileCColor3", 7);
      setDColor(var0, var1, "chunkFileAColor0", 3);
      PreferenceConverter.setDefault(var0, "chunkFileIRGB", new RGB(0, 255, 255));
      PreferenceConverter.setDefault(var0, "chunkProgress1", new RGB(15, 136, 0));
      PreferenceConverter.setDefault(var0, "chunkProgress2", new RGB(41, 187, 26));
      setDColor(var0, var1, "chunkText", 1);
      var0.setDefault("maxChunkGraphLength", 200);
      var0.setDefault("displayChunkGraphPercent", false);
      var0.setDefault("graphUploadsType", 0);
      var0.setDefault("graphDownloadsType", 0);
      setDColor(var0, var1, "graphUploadsColor1", 3);
      PreferenceConverter.setDefault(var0, "graphUploadsColor2", new RGB(125, 0, 0));
      setDColor(var0, var1, "graphDownloadsColor1", 5);
      PreferenceConverter.setDefault(var0, "graphDownloadsColor2", new RGB(0, 125, 0));
      setDColor(var0, var1, "graphBackgroundColor", 2);
      PreferenceConverter.setDefault(var0, "graphGridColor", new RGB(0, 128, 64));
      setDColor(var0, var1, "graphTextColor", 1);
      setDColor(var0, var1, "graphLabelBackgroundColor", 1);
      setDColor(var0, var1, "graphLabelLineColor", 7);
      setDColor(var0, var1, "graphLabelTextColor", 2);
      setDColor(var0, var1, "networkEnabledColor", 6);
      setDColor(var0, var1, "serverDisconnectedColor", 2);
      setDColor(var0, var1, "serverConnectingColor", 8);
      setDColor(var0, var1, "serverConnectedColor", 6);
      setDColor(var0, var1, "addrBlockedColor", 4);
      setDColor(var0, var1, "resultDefaultColor", 2);
      setDColor(var0, var1, "resultAlreadyDownloadedColor", 6);
      setDColor(var0, var1, "resultFakeColor", 4);
      setDColor(var0, var1, "ircInNickColor", 10);
      setDColor(var0, var1, "ircOutNickColor", 12);
      setDColor(var0, var1, "ircJoinColor", 6);
      setDColor(var0, var1, "ircPartColor", 4);
      setDColor(var0, var1, "ircModeColor", 8);
      var0.setDefault("defaultWebBrowser", "");
      var0.setDefault("consoleToolItems", 5);
      var0.setDefault("consoleToolItem1", "cs");
      var0.setDefault("consoleToolItem2", "version");
      var0.setDefault("consoleToolItem3", "??");
      var0.setDefault("consoleToolItem4", "log");
      var0.setDefault("consoleToolItem5", "sysinfo");
      var0.setDefault("webBrowserTabsOnTop", false);
      var0.setDefault("webBrowserToolItems", 4);
      var0.setDefault("webBrowserToolItem1", "http://btjunkie.org/");
      var0.setDefault("webBrowserToolItem2", "http://mininova.org/");
      var0.setDefault("webBrowserToolItem3", "http://eztv.it/");
      var0.setDefault("webBrowserToolItem4", "http://isohunt.com/");
      var0.setDefault("maxFavoriteLength", 50);
      var0.setDefault("hm_0_hostname", "127.0.0.1");
      var0.setDefault("hm_0_username", "admin");
      var0.setDefault("hm_0_password", "");
      var0.setDefault("hm_0_port", 4001);
      var0.setDefault("hm_0_protocol", 0);
      var0.setDefault("hm_0_ssh_host", "192.168.0.1");
      var0.setDefault("hm_0_ssh_rhost", "127.0.0.1");
      var0.setDefault("hm_0_ssh_port", 22);
      var0.setDefault("hm_0_ssh_rport", 4001);
      var0.setDefault("hm_0_ssh_lport", 4001);
      var0.setDefault("hm_0_ssh_fwd_p", false);
      var0.setDefault("hm_0_ssh_prport", 4080);
      var0.setDefault("hm_0_ssh_plport", 4080);
      var0.setDefault("refineFilterNegation", false);
      var0.setDefault("refineFilterAlternates", false);
      var0.setDefault("searchSaveEntries", true);
      var0.setDefault("searchForceDownload", false);
      var0.setDefault("searchTooltips", true);
      var0.setDefault("searchTooltipsOffset", true);
      var0.setDefault("searchFilterPornography", false);
      var0.setDefault("searchFilterProfanity", false);
      var0.setDefault("maintainSortOrder", false);
      var0.setDefault("updateDelay", 2);
      var0.setDefault("graphUpdateDelay", 1);
      var0.setDefault("statsDelay", 600);
      var0.setDefault("useGradient", true);
      RGB var3 = var1.getSystemColor(31).getRGB();
      Color var4 = WidgetFactory.changeColor(var3, 10);
      PreferenceConverter.setDefault(var0, "viewGradientColor", var4.getRGB());
      var4.dispose();
      var0.setDefault("displayNodes", true);
      var0.setDefault("displayChunkGraphs", false);
      var0.setDefault("displayGridLines", true);
      var0.setDefault("followSelection", false);
      var0.setDefault("tableCellEditors", false);
      var0.setDefault("displayTableColors", true);
      var0.setDefault("coreExecutable", "");
      var0.setDefault("useCombo", false);
      var0.setDefault("minimizeOnClose", false);
      var0.setDefault("systrayOnMinimize", false);
      var0.setDefault("systrayEnabled", true);
      var0.setDefault("systraySingleClick", true);
      var0.setDefault("dragAndDrop", true);
      var0.setDefault("humanReadable", true);
      var0.setDefault("maxMegabytes", false);
      var0.setDefault("humanReadableDecimals", -1);
      var0.setDefault("verboseNumbers", true);
      var0.setDefault("pollUpStats", true);
      var0.setDefault("pollUploaders", true);
      var0.setDefault("pollPending", false);
      var0.setDefault("pollDelay", 5);
      var0.setDefault("requestFileInfoDelay", 300);
      var0.setDefault("resultsCTabFolderTabsOnTop", true);
      var0.setDefault("roomsCTabFolderTabsOnTop", true);
      var0.setDefault("messagesCTabFolderTabsOnTop", true);
      var0.setDefault("webBrowserCTabFolderTabsOnTop", false);
      var0.setDefault("downloadsShowTabs", false);
      var0.setDefault("downloadsTabs", 1);
      var0.setDefault("downloadsTab_0_Name", SResources.getString("l.all"));
      var0.setDefault("downloadsTab_0_Filters", "");
      var0.setDefault("clientsShowTabs", false);
      var0.setDefault("clientsTabs", 1);
      var0.setDefault("clientsTab_0_Name", SResources.getString("l.all"));
      var0.setDefault("clientsTab_0_Filters", "");
      var0.setDefault("friendsShowTabs", false);
      var0.setDefault("friendsTabs", 1);
      var0.setDefault("friendsTab_0_Name", SResources.getString("l.all"));
      var0.setDefault("friendsTab_0_Filters", "");
      var0.setDefault("roomsShowTabs", false);
      var0.setDefault("roomsTabs", 1);
      var0.setDefault("roomsTab_0_Name", SResources.getString("l.all"));
      var0.setDefault("roomsTab_0_Filters", "");
      var0.setDefault("uploadsShowTabs", false);
      var0.setDefault("uploadsTabs", 1);
      var0.setDefault("uploadsTab_0_Name", SResources.getString("l.all"));
      var0.setDefault("uploadsTab_0_Filters", "");
      var0.setDefault("serverShowTabs", true);
      var0.setDefault("serverTabs", 2);
      var0.setDefault("serverTab_0_Name", SResources.getString("e.state.connected"));
      var0.setDefault("serverTab_0_Filters", "1,22,");
      var0.setDefault("serverTab_1_Name", SResources.getString("l.all"));
      var0.setDefault("serverTab_1_Filters", "");
      var0.setDefault("autoReconnect", false);
      var0.setDefault("autoReconnectDelay", 30);
      var0.setDefault("useLastFile", false);
      var0.setDefault("allowMultipleInstances", false);
      var0.setDefault("downloadsFilterQueued", false);
      var0.setDefault("downloadsFilterPaused", false);
      var0.setDefault("autoCloseRooms", true);
      var0.setDefault("autoOpenRooms", false);
      var0.setDefault("previewExecutable", "");
      var0.setDefault("previewWorkingDirectory", "");
      var0.setDefault("previewUseHttp", false);
      var0.setDefault("versionCheck", false);
      var0.setDefault("versionCheckPopup", true);
      var0.setDefault(
         "bwPresets",
         "max_hard_download_rate;max_hard_upload_rate;max_upload_slots;max_concurrent_downloads;max_opened_connections;max_connections_per_second;ED2K-max_indirect_connections;ED2K-max_udp_sends;"
      );
      var0.setDefault("bwPreset1_max_hard_download_rate", 100);
      var0.setDefault("bwPreset1_max_hard_upload_rate", 25);
      var0.setDefault("bwPreset1_max_upload_slots", 15);
      var0.setDefault("bwPreset1_max_concurrent_downloads", 60);
      var0.setDefault("bwPreset1_max_opened_connections", 200);
      var0.setDefault("bwPreset1_max_connections_per_second", 5);
      var0.setDefault("bwPreset1_ED2K-max_indirect_connections", 100);
      var0.setDefault("bwPreset1_ED2K-max_udp_sends", 10);
      var0.setDefault("bwPreset2_max_hard_download_rate", 40);
      var0.setDefault("bwPreset2_max_hard_upload_rate", 20);
      var0.setDefault("bwPreset2_max_upload_slots", 10);
      var0.setDefault("bwPreset2_max_concurrent_downloads", 25);
      var0.setDefault("bwPreset2_max_opened_connections", 200);
      var0.setDefault("bwPreset2_max_connections_per_second", 5);
      var0.setDefault("bwPreset2_ED2K-max_indirect_connections", 100);
      var0.setDefault("bwPreset2_ED2K-max_udp_sends", 10);
      var0.setDefault("bwPreset3_max_hard_download_rate", 25);
      var0.setDefault("bwPreset3_max_hard_upload_rate", 10);
      var0.setDefault("bwPreset3_max_upload_slots", 5);
      var0.setDefault("bwPreset3_max_concurrent_downloads", 10);
      var0.setDefault("bwPreset3_max_opened_connections", 200);
      var0.setDefault("bwPreset3_max_connections_per_second", 5);
      var0.setDefault("bwPreset3_ED2K-max_indirect_connections", 100);
      var0.setDefault("bwPreset3_ED2K-max_udp_sends", 10);
      PreferenceConverter.setDefault(var0, "tableFontData", var2);
      PreferenceConverter.setDefault(var0, "headerFontData", var2);
      var0.setDefault("statisticsSashOrientation", 256);
      var0.setDefault("statisticsSashMaximized", -1);
      var0.setDefault("mainWindowSashOrientation", 512);
      var0.setDefault("mainWindowSashMaximized", 0);
      var0.setDefault("graphSashOrientation", 512);
      var0.setDefault("graphSashMaximized", -1);
      var0.setDefault("clientSashOrientation", 256);
      var0.setDefault("clientSashMaximized", 0);
      var0.setDefault("transferSashOrientation", 512);
      var0.setDefault("transferSashMaximized", -1);
      var0.setDefault("uploadsSashOrientation", 256);
      var0.setDefault("uploadsSashMaximized", 0);
      var0.setDefault("locale", "");
      var0.setDefault("searchSashOrientation", 256);
      var0.setDefault("searchSashMaximized", -1);
      PreferenceConverter.setDefault(var0, "searchSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(var0, "searchSashChild1", new Rectangle(0, 0, 5, 0));
      var0.setDefault("serversSashOrientation", 512);
      var0.setDefault("serversSashMaximized", -1);
      PreferenceConverter.setDefault(var0, "serversSashChild0", new Rectangle(0, 0, 4, 0));
      PreferenceConverter.setDefault(var0, "serversSashChild1", new Rectangle(0, 0, 1, 0));
      var0.setDefault("filesMessagesSashOrientation", 512);
      var0.setDefault("filesMessagesSashMaximized", -1);
      PreferenceConverter.setDefault(var0, "filesMessagesSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(var0, "filesMessagesSashChild1", new Rectangle(0, 0, 5, 0));
      var0.setDefault("directoriesFilesSashOrientation", 256);
      var0.setDefault("directoriesFilesSashMaximized", -1);
      PreferenceConverter.setDefault(var0, "directoriesFilesSashChild0", new Rectangle(0, 0, 1, 0));
      PreferenceConverter.setDefault(var0, "directoriesFilesSashChild1", new Rectangle(0, 0, 5, 0));
      var0.setDefault("clientFilesSashOrientation", 256);
      var0.setDefault("clientFilesSashMaximized", -1);
      PreferenceConverter.setDefault(var0, "clientFilesSashChild0", new Rectangle(0, 0, 1, 0));
      PreferenceConverter.setDefault(var0, "clientFilesSashChild1", new Rectangle(0, 0, 5, 0));
      var0.setDefault("messagesSashOrientation", 256);
      var0.setDefault("messagesSashMaximized", -1);
      PreferenceConverter.setDefault(var0, "messagesSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(var0, "messagesSashChild1", new Rectangle(0, 0, 5, 0));
      var0.setDefault("ircSashOrientation", 256);
      var0.setDefault("ircSashMaximized", -1);
      PreferenceConverter.setDefault(var0, "ircSashChild0", new Rectangle(0, 0, 5, 0));
      PreferenceConverter.setDefault(var0, "ircSashChild1", new Rectangle(0, 0, 1, 0));
      var0.setDefault("roomsSashOrientation", 256);
      var0.setDefault("roomsSashMaximized", -1);
      PreferenceConverter.setDefault(var0, "roomsSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(var0, "roomsSashChild1", new Rectangle(0, 0, 5, 0));
      var0.setDefault("roomSashOrientation", 256);
      var0.setDefault("roomSashMaximized", -1);
      PreferenceConverter.setDefault(var0, "roomSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(var0, "roomSashChild1", new Rectangle(0, 0, 5, 0));
      var0.setDefault("statistics.BitTorrentOrientation", 256);
      var0.setDefault("statistics.BitTorrentMaximized", -1);
      var0.setDefault("statistics.DonkeyOrientation", 256);
      var0.setDefault("statistics.DonkeyMaximized", -1);
      var0.setDefault("statistics.Donkey.0Orientation", 512);
      var0.setDefault("statistics.Donkey.1Orientation", 512);
      var0.setDefault("serverDynamicColumn", IDSelector.getID(1));
      var0.setDefault("clientDirectoriesDynamicColumn", IDSelector.getID(0));
      var0.setDefault("channelUsersDynamicColumn", IDSelector.getID(0));
      var0.setDefault("fd.subFilesDynamicColumn", IDSelector.getID(0));
      var0.setDefault(
         "serverStateFilters", EnumHostState.CONNECTING.getValue() | EnumHostState.CONNECTED_INITIATING.getValue() | EnumHostState.CONNECTED.getValue()
      );
      return var0;
   }

   public static void setLocaleString(String var0) {
      localeString = var0;
   }

   public static String getHomeDirectory() {
      return homeDirectory;
   }

   public static void setHomeDirectory(String var0) {
      customHomeDir = true;
      homeDirectory = var0;
      String var1 = System.getProperty("file.separator");
      if (!var0.endsWith(var1)) {
         homeDirectory = homeDirectory + var1;
      }
   }

   public static void setPrefFile(String var0) {
      customPrefFile = true;
      prefFileName = var0;
      preferenceStore = new PreferenceStore(var0);
   }
}
