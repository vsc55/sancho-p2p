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
      for (int i = 0; i < fontArray.size(); i++) {
         Font font = (Font)fontArray.get(i);
         if (font != null && !font.isDisposed()) {
            font.dispose();
         }
      }

      for (int i = 0; i < colorArray.size(); i++) {
         Color color = (Color)colorArray.get(i);
         if (color != null && !color.isDisposed()) {
            color.dispose();
         }
      }
   }

   public static boolean contains(String name) {
      return preferenceStore.contains(name);
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
            File file = new File(prefFileName);
            if (!file.exists()) {
               File parentDir = new File(file.getParent());
               parentDir.mkdirs();
            }

            preferenceStore = new PreferenceStore(prefFileName);
         }
      }

      try {
         preferenceStore.load();
      } catch (IOException ioException) {
         preferenceStore.save();
      }
   }

   public static void initialize2() {
      preferenceStore = (PreferenceStore)setDefaults(preferenceStore);
   }

   public static boolean loadBoolean(String name) {
      return preferenceStore.contains(name) ? preferenceStore.getBoolean(name) : false;
   }

   public static RGB loadRGB(String name) {
      return preferenceStore.contains(name) ? PreferenceConverter.getColor(preferenceStore, name) : new RGB(0, 0, 0);
   }

   public static Color loadColor(String name) {
      if (!preferenceStore.contains(name)) {
         return null;
      } else {
         Color color = new Color(null, PreferenceConverter.getColor(preferenceStore, name));
         if (colorMap.containsKey(name) && !((Color)colorMap.get(name)).isDisposed()) {
            if (color.getRGB().equals(((Color)colorMap.get(name)).getRGB())) {
               color.dispose();
            } else {
               colorArray.add(color);
               colorMap.put(name, color);
            }
         } else {
            colorArray.add(color);
            colorMap.put(name, color);
         }

         return (Color)colorMap.get(name);
      }
   }

   public static Font loadFont(String name) {
      if (!preferenceStore.contains(name)) {
         return null;
      } else {
         Font font = new Font(null, PreferenceConverter.getFontDataArray(preferenceStore, name));
         if (fontMap.containsKey(name) && !((Font)fontMap.get(name)).isDisposed()) {
            if (font.getFontData()[0].equals(((Font)fontMap.get(name)).getFontData()[0])) {
               font.dispose();
            } else {
               fontArray.add(font);
               fontMap.put(name, font);
            }
         } else {
            fontArray.add(font);
            fontMap.put(name, font);
         }

         return (Font)fontMap.get(name);
      }
   }

   public static int loadInt(String name) {
      return preferenceStore.contains(name) ? preferenceStore.getInt(name) : 0;
   }

   public static int loadIntOrN1(String name) {
      return preferenceStore.contains(name) ? preferenceStore.getInt(name) : -1;
   }

   public static int loadOrientation(String name) {
      if (preferenceStore.contains(name)) {
         int orientation = preferenceStore.getInt(name);
         if (orientation == 512 || orientation == 256) {
            return orientation;
         }
      }

      return 256;
   }

   public static Rectangle loadRectangle(String name) {
      return preferenceStore.contains(name) ? PreferenceConverter.getRectangle(preferenceStore, name) : null;
   }

   public static String loadStringEnv(String name) {
      return SwissArmy.replaceEnvVars(loadString(name));
   }

   public static String loadString(String name) {
      return preferenceStore.contains(name) ? preferenceStore.getString(name).intern() : "";
   }

   public static String[] loadStringArray(String name) {
      StringTokenizer tokenizer = new StringTokenizer(loadString(name), ENTRY_SEPARATOR);
      int count = tokenizer.countTokens();
      String[] values = new String[count];

      for (int i = 0; i < count; i++) {
         values[i] = tokenizer.nextToken();
      }

      return values;
   }

   public static void setValue(String name, String[] values) {
      StringBuffer buffer = new StringBuffer();

      for (int i = 0; values != null && i < values.length; i++) {
         buffer.append(SwissArmy.replaceAll(values[i], ENTRY_SEPARATOR, ""));
         buffer.append(ENTRY_SEPARATOR);
      }

      preferenceStore.setValue(name, buffer.toString().intern());
   }

   public static void setValue(String name, String[] values, int count) {
      if (values.length > count) {
         String[] truncated = new String[count];

         for (int i = 0; i < count; i++) {
            truncated[i] = values[i];
         }

         values = truncated;
      }

      setValue(name, values);
   }

   public static void saveStore() {
      try {
         preferenceStore.save();
      } catch (IOException ioException) {
         ioException.printStackTrace();
      }
   }

   static void setDColor(IPreferenceStore store, Display display, String name, int colorId) {
      PreferenceConverter.setDefault(store, name, display.getSystemColor(colorId).getRGB());
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

   public static void setRGB(String name, RGB rgb) {
      if (preferenceStore != null) {
         PreferenceConverter.setValue(preferenceStore, name, rgb);
      }
   }

   static IPreferenceStore setDefaults(IPreferenceStore store) {
      Display display = Display.getDefault();
      FontData[] fontData = JFaceResources.getDefaultFont().getFontData();
      store.setDefault("initialized", false);
      store.setDefault("windowMaximized", false);
      store.setDefault("windowAlpha", 255);
      store.setDefault("windowStartMinimized", false);
      store.setDefault("windowStartTray", false);
      store.setDefault("coolbarLocked", true);
      store.setDefault("toolbarSmallButtons", true);
      store.setDefault("flatInterface", false);
      store.setDefault("splashScreen", true);
      store.setDefault("killCoreOnExit", false);
      store.setDefault("killSpawnedCoreOnExit", true);
      store.setDefault("killSpawnedCoreDelay", 60);
      store.setDefault("hostManagerOnStart", false);
      store.setDefault("downloadCompleteDialog", false);
      store.setDefault("downloadCompleteLog", true);
      store.setDefault("explorerExecutable", SWT.getPlatform().equals("win32") ? "explorer" : "");
      store.setDefault("explorerOpenFolder", "");
      store.setDefault("linkRipperShowAll", false);
      store.setDefault("dlFileDoubleClick", 0);
      store.setDefault("dlPercentDecimals", 0);
      store.setDefault("dlRateDecimals", 1);
      store.setDefault("dndBox", false);
      store.setDefault("consoleMaxLines", 300);
      store.setDefault("consoleUnderlineURLs", false);
      store.setDefault("maxMenuItems", 10);
      store.setDefault("connectionTimeout", 20);
      store.setDefault("mldonkey.InterestedInSources", true);
      store.setDefault("tableAlternateBGColors", false);
      setDColor(store, display, "tableAlternateBGColor", 29);
      setDColor(store, display, "tablesBackgroundColor", 25);
      store.setDefault("tableHilightSorted", false);
      store.setDefault("tableSortIndicator", true);   // was !"carbon" (removed SWT platform); always true now
      setDColor(store, display, "tableSortedColumnBGColor", 29);
      PreferenceConverter.setDefault(store, "downloadHistoryWindowBounds", new Rectangle(0, 0, 500, 400));
      PreferenceConverter.setDefault(store, "downloadCompleteWindowBounds", new Rectangle(0, 0, 320, 300));
      PreferenceConverter.setDefault(store, "windowBounds", new Rectangle(40, 40, 580, 420));
      PreferenceConverter.setDefault(store, "graphHistoryWindowBounds", new Rectangle(40, 40, 580, 420));
      PreferenceConverter.setDefault(store, "dndBoxWindowBounds", new Rectangle(-1, 0, 0, 0));
      setDColor(store, display, "dndBackgroundColor", 22);
      setDColor(store, display, "dndForegroundColor", 2);
      PreferenceConverter.setDefault(store, "dndFontData", fontData);
      store.setDefault("dndWidth", 15);
      setDColor(store, display, "consoleBackground", 2);
      setDColor(store, display, "consoleForeground", 15);
      setDColor(store, display, "consoleHighlight", 1);
      setDColor(store, display, "consoleInputBackground", 2);
      setDColor(store, display, "consoleInputForeground", 15);
      PreferenceConverter.setDefault(store, "consoleFontData", JFaceResources.getTextFont().getFontData());
      setDColor(store, display, "clientsDisconnectedColor", 2);
      setDColor(store, display, "clientsHasFilesColor", 6);
      setDColor(store, display, "clientsConnectedColor", 8);
      setDColor(store, display, "downloadsBackgroundColor", 25);
      setDColor(store, display, "downloadsAvailableFileColor", 2);
      PreferenceConverter.setDefault(store, "downloadsUnAvailableFileColor", new RGB(128, 128, 128));
      setDColor(store, display, "downloadsDownloadedFileColor", 9);
      setDColor(store, display, "downloadsQueuedFileColor", 15);
      setDColor(store, display, "downloadsPausedFileColor", 4);
      setDColor(store, display, "downloadsContainsFakeColor", 3);
      store.setDefault("dlIndicateFakes", true);
      PreferenceConverter.setDefault(store, "downloadsRateAbove20FileColor", new RGB(0, 160, 0));
      PreferenceConverter.setDefault(store, "downloadsRateAbove10FileColor", new RGB(0, 140, 0));
      PreferenceConverter.setDefault(store, "downloadsRateAbove0FileColor", new RGB(0, 110, 0));
      PreferenceConverter.setDefault(store, "downloadsRateAbove20FontData", fontData);
      PreferenceConverter.setDefault(store, "downloadsRateAbove10FontData", fontData);
      PreferenceConverter.setDefault(store, "downloadsRateAbove0FontData", fontData);
      PreferenceConverter.setDefault(store, "downloadsPausedFontData", fontData);
      PreferenceConverter.setDefault(store, "downloadsQueuedFontData", fontData);
      PreferenceConverter.setDefault(store, "downloadsDownloadedFontData", fontData);
      PreferenceConverter.setDefault(store, "chunkClientCColor2", new RGB(107, 81, 9));
      PreferenceConverter.setDefault(store, "chunkClientAColor0", new RGB(226, 225, 221));
      PreferenceConverter.setDefault(store, "chunkClientAColor1", new RGB(0, 150, 255));
      setDColor(store, display, "chunkClientAColor2", 7);
      PreferenceConverter.setDefault(store, "chunkFileCColor1", new RGB(128, 128, 128));
      PreferenceConverter.setDefault(store, "chunkFileCColor2", new RGB(107, 81, 9));
      setDColor(store, display, "chunkFileCColor3", 7);
      setDColor(store, display, "chunkFileAColor0", 3);
      PreferenceConverter.setDefault(store, "chunkFileIRGB", new RGB(0, 255, 255));
      PreferenceConverter.setDefault(store, "chunkProgress1", new RGB(15, 136, 0));
      PreferenceConverter.setDefault(store, "chunkProgress2", new RGB(41, 187, 26));
      setDColor(store, display, "chunkText", 1);
      store.setDefault("maxChunkGraphLength", 200);
      store.setDefault("displayChunkGraphPercent", false);
      store.setDefault("graphUploadsType", 0);
      store.setDefault("graphDownloadsType", 0);
      setDColor(store, display, "graphUploadsColor1", 3);
      PreferenceConverter.setDefault(store, "graphUploadsColor2", new RGB(125, 0, 0));
      setDColor(store, display, "graphDownloadsColor1", 5);
      PreferenceConverter.setDefault(store, "graphDownloadsColor2", new RGB(0, 125, 0));
      setDColor(store, display, "graphBackgroundColor", 2);
      PreferenceConverter.setDefault(store, "graphGridColor", new RGB(0, 128, 64));
      setDColor(store, display, "graphTextColor", 1);
      setDColor(store, display, "graphLabelBackgroundColor", 1);
      setDColor(store, display, "graphLabelLineColor", 7);
      setDColor(store, display, "graphLabelTextColor", 2);
      setDColor(store, display, "networkEnabledColor", 6);
      setDColor(store, display, "serverDisconnectedColor", 2);
      setDColor(store, display, "serverConnectingColor", 8);
      setDColor(store, display, "serverConnectedColor", 6);
      setDColor(store, display, "addrBlockedColor", 4);
      setDColor(store, display, "resultDefaultColor", 2);
      setDColor(store, display, "resultAlreadyDownloadedColor", 6);
      setDColor(store, display, "resultFakeColor", 4);
      store.setDefault("defaultWebBrowser", "");
      store.setDefault("consoleToolItems", 5);
      store.setDefault("consoleToolItem1", "cs");
      store.setDefault("consoleToolItem2", "version");
      store.setDefault("consoleToolItem3", "??");
      store.setDefault("consoleToolItem4", "log");
      store.setDefault("consoleToolItem5", "sysinfo");
      store.setDefault("webBrowserTabsOnTop", false);
      store.setDefault("webBrowserToolItems", 4);
      store.setDefault("webBrowserToolItem1", "http://btjunkie.org/");
      store.setDefault("webBrowserToolItem2", "http://mininova.org/");
      store.setDefault("webBrowserToolItem3", "http://eztv.it/");
      store.setDefault("webBrowserToolItem4", "http://isohunt.com/");
      store.setDefault("maxFavoriteLength", 50);
      store.setDefault("hm_0_hostname", "127.0.0.1");
      store.setDefault("hm_0_username", "admin");
      store.setDefault("hm_0_password", "");
      store.setDefault("hm_0_port", 4001);
      store.setDefault("hm_0_protocol", 0);
      store.setDefault("hm_0_ssh_host", "192.168.0.1");
      store.setDefault("hm_0_ssh_rhost", "127.0.0.1");
      store.setDefault("hm_0_ssh_port", 22);
      store.setDefault("hm_0_ssh_rport", 4001);
      store.setDefault("hm_0_ssh_lport", 4001);
      store.setDefault("hm_0_ssh_fwd_p", false);
      store.setDefault("hm_0_ssh_prport", 4080);
      store.setDefault("hm_0_ssh_plport", 4080);
      store.setDefault("refineFilterNegation", false);
      store.setDefault("refineFilterAlternates", false);
      store.setDefault("searchSaveEntries", true);
      store.setDefault("searchForceDownload", false);
      store.setDefault("searchTooltips", true);
      store.setDefault("searchTooltipsOffset", true);
      store.setDefault("searchFilterPornography", false);
      store.setDefault("searchFilterProfanity", false);
      store.setDefault("searchFormats", "exe;bin;img;gif;jpg;mkv;mp4");
      store.setDefault("maintainSortOrder", false);
      store.setDefault("updateDelay", 2);
      store.setDefault("graphUpdateDelay", 1);
      store.setDefault("statsDelay", 600);
      store.setDefault("useGradient", true);
      RGB rgb = display.getSystemColor(31).getRGB();
      Color color = WidgetFactory.changeColor(rgb, 10);
      PreferenceConverter.setDefault(store, "viewGradientColor", color.getRGB());
      color.dispose();
      store.setDefault("displayNodes", true);
      store.setDefault("displayChunkGraphs", false);
      store.setDefault("displayGridLines", true);
      store.setDefault("followSelection", false);
      store.setDefault("tableCellEditors", false);
      store.setDefault("displayTableColors", true);
      store.setDefault("coreExecutable", "");
      store.setDefault("useCombo", false);
      store.setDefault("minimizeOnClose", false);
      store.setDefault("systrayOnMinimize", false);
      store.setDefault("systrayEnabled", true);
      store.setDefault("systraySingleClick", true);
      store.setDefault("dragAndDrop", true);
      store.setDefault("humanReadable", true);
      store.setDefault("maxMegabytes", false);
      store.setDefault("humanReadableDecimals", -1);
      store.setDefault("verboseNumbers", true);
      store.setDefault("pollUpStats", true);
      store.setDefault("pollUploaders", true);
      store.setDefault("pollPending", false);
      store.setDefault("pollDelay", 5);
      store.setDefault("requestFileInfoDelay", 300);
      store.setDefault("resultsCTabFolderTabsOnTop", true);
      store.setDefault("roomsCTabFolderTabsOnTop", true);
      store.setDefault("messagesCTabFolderTabsOnTop", true);
      store.setDefault("webBrowserCTabFolderTabsOnTop", false);
      store.setDefault("downloadsShowTabs", false);
      store.setDefault("downloadsTabs", 1);
      store.setDefault("downloadsTab_0_Name", SResources.getString("l.all"));
      store.setDefault("downloadsTab_0_Filters", "");
      store.setDefault("clientsShowTabs", false);
      store.setDefault("clientsTabs", 1);
      store.setDefault("clientsTab_0_Name", SResources.getString("l.all"));
      store.setDefault("clientsTab_0_Filters", "");
      store.setDefault("friendsShowTabs", false);
      store.setDefault("friendsTabs", 1);
      store.setDefault("friendsTab_0_Name", SResources.getString("l.all"));
      store.setDefault("friendsTab_0_Filters", "");
      store.setDefault("roomsShowTabs", false);
      store.setDefault("roomsTabs", 1);
      store.setDefault("roomsTab_0_Name", SResources.getString("l.all"));
      store.setDefault("roomsTab_0_Filters", "");
      store.setDefault("uploadsShowTabs", false);
      store.setDefault("uploadsTabs", 1);
      store.setDefault("uploadsTab_0_Name", SResources.getString("l.all"));
      store.setDefault("uploadsTab_0_Filters", "");
      store.setDefault("serverShowTabs", true);
      store.setDefault("serverTabs", 2);
      store.setDefault("serverTab_0_Name", SResources.getString("e.state.connected"));
      store.setDefault("serverTab_0_Filters", "1,22,");
      store.setDefault("serverTab_1_Name", SResources.getString("l.all"));
      store.setDefault("serverTab_1_Filters", "");
      store.setDefault("autoReconnect", false);
      store.setDefault("autoReconnectDelay", 30);
      store.setDefault("useLastFile", false);
      store.setDefault("allowMultipleInstances", false);
      store.setDefault("downloadsFilterQueued", false);
      store.setDefault("downloadsFilterPaused", false);
      store.setDefault("autoCloseRooms", true);
      store.setDefault("autoOpenRooms", false);
      store.setDefault("previewExecutable", "");
      store.setDefault("previewWorkingDirectory", "");
      store.setDefault("previewUseHttp", false);
      store.setDefault("versionCheck", false);
      store.setDefault("versionCheckPopup", true);
      store.setDefault(
         "bwPresets",
         "max_hard_download_rate;max_hard_upload_rate;max_upload_slots;max_concurrent_downloads;max_opened_connections;max_connections_per_second;ED2K-max_indirect_connections;ED2K-max_udp_sends;"
      );
      store.setDefault("bwPreset1_max_hard_download_rate", 100);
      store.setDefault("bwPreset1_max_hard_upload_rate", 25);
      store.setDefault("bwPreset1_max_upload_slots", 15);
      store.setDefault("bwPreset1_max_concurrent_downloads", 60);
      store.setDefault("bwPreset1_max_opened_connections", 200);
      store.setDefault("bwPreset1_max_connections_per_second", 5);
      store.setDefault("bwPreset1_ED2K-max_indirect_connections", 100);
      store.setDefault("bwPreset1_ED2K-max_udp_sends", 10);
      store.setDefault("bwPreset2_max_hard_download_rate", 40);
      store.setDefault("bwPreset2_max_hard_upload_rate", 20);
      store.setDefault("bwPreset2_max_upload_slots", 10);
      store.setDefault("bwPreset2_max_concurrent_downloads", 25);
      store.setDefault("bwPreset2_max_opened_connections", 200);
      store.setDefault("bwPreset2_max_connections_per_second", 5);
      store.setDefault("bwPreset2_ED2K-max_indirect_connections", 100);
      store.setDefault("bwPreset2_ED2K-max_udp_sends", 10);
      store.setDefault("bwPreset3_max_hard_download_rate", 25);
      store.setDefault("bwPreset3_max_hard_upload_rate", 10);
      store.setDefault("bwPreset3_max_upload_slots", 5);
      store.setDefault("bwPreset3_max_concurrent_downloads", 10);
      store.setDefault("bwPreset3_max_opened_connections", 200);
      store.setDefault("bwPreset3_max_connections_per_second", 5);
      store.setDefault("bwPreset3_ED2K-max_indirect_connections", 100);
      store.setDefault("bwPreset3_ED2K-max_udp_sends", 10);
      PreferenceConverter.setDefault(store, "tableFontData", fontData);
      PreferenceConverter.setDefault(store, "headerFontData", fontData);
      store.setDefault("statisticsSashOrientation", 256);
      store.setDefault("statisticsSashMaximized", -1);
      store.setDefault("mainWindowSashOrientation", 512);
      store.setDefault("mainWindowSashMaximized", 0);
      store.setDefault("graphSashOrientation", 512);
      store.setDefault("graphSashMaximized", -1);
      store.setDefault("clientSashOrientation", 256);
      store.setDefault("clientSashMaximized", 0);
      store.setDefault("transferSashOrientation", 512);
      store.setDefault("transferSashMaximized", -1);
      store.setDefault("uploadsSashOrientation", 256);
      store.setDefault("uploadsSashMaximized", 0);
      store.setDefault("locale", "");
      store.setDefault("searchSashOrientation", 256);
      store.setDefault("searchSashMaximized", -1);
      PreferenceConverter.setDefault(store, "searchSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(store, "searchSashChild1", new Rectangle(0, 0, 5, 0));
      store.setDefault("serversSashOrientation", 512);
      store.setDefault("serversSashMaximized", -1);
      PreferenceConverter.setDefault(store, "serversSashChild0", new Rectangle(0, 0, 4, 0));
      PreferenceConverter.setDefault(store, "serversSashChild1", new Rectangle(0, 0, 1, 0));
      store.setDefault("filesMessagesSashOrientation", 512);
      store.setDefault("filesMessagesSashMaximized", -1);
      PreferenceConverter.setDefault(store, "filesMessagesSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(store, "filesMessagesSashChild1", new Rectangle(0, 0, 5, 0));
      store.setDefault("directoriesFilesSashOrientation", 256);
      store.setDefault("directoriesFilesSashMaximized", -1);
      PreferenceConverter.setDefault(store, "directoriesFilesSashChild0", new Rectangle(0, 0, 1, 0));
      PreferenceConverter.setDefault(store, "directoriesFilesSashChild1", new Rectangle(0, 0, 5, 0));
      store.setDefault("clientFilesSashOrientation", 256);
      store.setDefault("clientFilesSashMaximized", -1);
      PreferenceConverter.setDefault(store, "clientFilesSashChild0", new Rectangle(0, 0, 1, 0));
      PreferenceConverter.setDefault(store, "clientFilesSashChild1", new Rectangle(0, 0, 5, 0));
      store.setDefault("messagesSashOrientation", 256);
      store.setDefault("messagesSashMaximized", -1);
      PreferenceConverter.setDefault(store, "messagesSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(store, "messagesSashChild1", new Rectangle(0, 0, 5, 0));
      store.setDefault("roomsSashOrientation", 256);
      store.setDefault("roomsSashMaximized", -1);
      PreferenceConverter.setDefault(store, "roomsSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(store, "roomsSashChild1", new Rectangle(0, 0, 5, 0));
      store.setDefault("roomSashOrientation", 256);
      store.setDefault("roomSashMaximized", -1);
      PreferenceConverter.setDefault(store, "roomSashChild0", new Rectangle(0, 0, 2, 0));
      PreferenceConverter.setDefault(store, "roomSashChild1", new Rectangle(0, 0, 5, 0));
      store.setDefault("statistics.BitTorrentOrientation", 256);
      store.setDefault("statistics.BitTorrentMaximized", -1);
      store.setDefault("statistics.DonkeyOrientation", 256);
      store.setDefault("statistics.DonkeyMaximized", -1);
      store.setDefault("statistics.Donkey.0Orientation", 512);
      store.setDefault("statistics.Donkey.1Orientation", 512);
      store.setDefault("serverDynamicColumn", IDSelector.getID(1));
      store.setDefault("clientDirectoriesDynamicColumn", IDSelector.getID(0));
      store.setDefault("channelUsersDynamicColumn", IDSelector.getID(0));
      store.setDefault("fd.subFilesDynamicColumn", IDSelector.getID(0));
      store.setDefault(
         "serverStateFilters", EnumHostState.CONNECTING.getValue() | EnumHostState.CONNECTED_INITIATING.getValue() | EnumHostState.CONNECTED.getValue()
      );
      return store;
   }

   public static void setLocaleString(String name) {
      localeString = name;
   }

   public static String getHomeDirectory() {
      return homeDirectory;
   }

   public static void setHomeDirectory(String directory) {
      customHomeDir = true;
      homeDirectory = directory;
      String separator = System.getProperty("file.separator");
      if (!directory.endsWith(separator)) {
         homeDirectory = homeDirectory + separator;
      }
   }

   public static void setPrefFile(String fileName) {
      customPrefFile = true;
      prefFileName = fileName;
      preferenceStore = new PreferenceStore(fileName);
   }
}
