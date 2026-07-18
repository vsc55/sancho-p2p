package sancho.view.preferences;

import java.io.File;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;
import sancho.core.Sancho;
import sancho.utility.VersionInfo;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class RootPreferencePage extends CPreferencePage {
   public RootPreferencePage(String var1) {
      super(var1);
   }

   protected Control createContents(Composite var1) {
      Composite var2 = new Composite(var1, 0);
      var2.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      TabFolder var3 = new TabFolder(var2, 128);
      var3.setLayoutData(new GridData(1808));
      this.createGeneralTab(var3);
      this.createDownloadsTab(var3);
      this.createSearchTab(var3);
      this.createConsoleTab(var3);
      this.createGraphTab(var3);
      this.createRoomsTab(var3);
      this.createIRCTab(var3);
      this.createWebBrowserTab(var3);
      return var2;
   }

   protected void createGeneralTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "p.general", "preferences");
      String[] var3;
      if (VersionInfo.getOSPlatform().equals("Windows")) {
         var3 = new String[]{"*.exe;*.bat"};
      } else {
         var3 = new String[]{"*"};
      }

      this.createInformationLabel(var2, "p.coreExecutableInfo");
      this.setupFileEditor("coreExecutable", "p.r.general.coreExecutable", var3, var2);
      this.setupBooleanEditor("killSpawnedCoreOnExit", "p.r.general.killSpawnedCoreOnExit", var2);
      this.setupIntegerEditor("killSpawnedCoreDelay", "p.r.general.killSpawnedCoreDelay", 1, 1000, var2);
      this.createSeparator(var2);
      this.setupIntegerEditor("connectionTimeout", "p.r.general.connectionTimeout", 1, 10000, var2);
      this.createSeparator(var2);
      this.createInformationLabel(var2, "p.webBrowserInfo");
      this.setupFileEditor("defaultWebBrowser", "p.r.general.defaultBrowser", var3, var2);
      this.createSeparator(var2);
      this.createInformationLabel(var2, "p.localeInfo");
      Label var4 = new Label(var2, 0);
      var4.setText(SResources.getString("p.r.general.locale"));
      Combo var5 = new Combo(var2, 8);
      String var6 = PreferenceLoader.loadString("locale");
      var5.add("");
      String[] var7 = this.getLocales();

      for (int var8 = 0; var8 < var7.length; var8++) {
         var5.add(var7[var8]);
         if (var7[var8].equals(var6)) {
            var5.select(var8 + 1);
         }
      }

      var5.addSelectionListener(new RootPreferencePage$1(this, var5));
      this.createSeparator(var2);
      this.setupBooleanEditor("autoReconnect", "p.r.general.autoReconnect", var2);
      this.setupIntegerEditor("autoReconnectDelay", "p.r.general.autoReconnectDelay", 1, 10000, var2);
      this.createSeparator(var2);
      if (VersionInfo.hasTray()) {
         this.setupBooleanEditor("systrayEnabled", "p.r.general.systrayEnabled", var2);
         this.setupBooleanEditor("systraySingleClick", "p.r.general.systraySingleClick", var2);
         this.setupBooleanEditor("minimizeOnClose", "p.r.general.systrayOnClose", var2);
         this.setupBooleanEditor("systrayOnMinimize", "p.r.general.systrayOnMinimize", var2);
         this.setupBooleanEditor("windowStartTray", "p.r.general.startTray", var2);
      }

      this.setupBooleanEditor("windowStartMinimized", "p.r.general.startMinimized", var2);
      this.setupBooleanEditor("allowMultipleInstances", "p.r.general.multipleInstances", var2);
      this.setupBooleanEditor("hostManagerOnStart", "p.r.general.hostManagerOnStart", var2);
      this.setupBooleanEditor("useLastFile", "p.r.general.useLastFile", var2);
      this.setupBooleanEditor("killCoreOnExit", "p.r.general.killCoreOnExit", var2);
      this.setupBooleanEditor("versionCheck", "p.r.general.versionCheck", var2);
      this.setupBooleanEditor("versionCheckPopup", "p.r.general.versionCheckPopup", var2);
      this.setCompositeLayout(var2);
   }

   protected void createDownloadsTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.transfers", "tab.transfers.buttonSmall");
      var2.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      var2.setLayoutData(new GridData(1808));
      TabFolder var3 = new TabFolder(var2, 1024);
      var3.setLayoutData(new GridData(1808));
      this.createDownloadsGeneral(var3);
      this.createDownloadsPreview(var3);
      this.createDownloadsExplorer(var3);
      this.createDownloadsWebservices(var3);
   }

   protected void createDownloadsGeneral(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "p.general");
      this.createInformationLabel(var2, "p.delayInfo");
      this.setupIntegerEditor("updateDelay", "p.r.downloads.updateDelay", 0, 600, var2);
      this.createSeparator(var2);
      this.setupBooleanEditor("pollUpStats", "p.r.downloads.pollUpstats", var2);
      this.setupIntegerEditor("pollDelay", "p.r.downloads.pollDelay", 1, 600, var2);
      this.createSeparator(var2);
      this.setupIntegerEditor("requestFileInfoDelay", "p.r.downloads.requestFileInfoDelay", 0, 99999, var2);
      this.createSeparator(var2);
      Label var3 = new Label(var2, 0);
      var3.setText(SResources.getString("p.r.downloads.fileDoubleClick"));
      Combo var4 = new Combo(var2, 8);
      var4.setLayoutData(new GridData(768));
      var4.add(SResources.getString("l.expandBranch"));
      var4.add(SResources.getString("m.d.preview"));
      var4.add(SResources.getString("m.d.preview") + " (OS)");
      int var5 = PreferenceLoader.loadInt("dlFileDoubleClick");
      if (var5 >= 0 && var5 < var4.getItemCount()) {
         var4.select(var5);
      }

      var4.addSelectionListener(new RootPreferencePage$2(this));
      this.createSeparator(var2);
      this.setupIntegerEditor("dlPercentDecimals", "p.r.downloads.percentDecimals", 0, 5, var2);
      this.setupIntegerEditor("dlRateDecimals", "p.r.downloads.rateDecimals", 0, 5, var2);
      this.createSeparator(var2);
      this.setupBooleanEditor("displayChunkGraphs", "p.r.downloads.displayChunkGraphs", var2);
      this.setupBooleanEditor("displayChunkGraphPercent", "p.r.downloads.displayChunkGraphPercent", var2);
      this.setupIntegerEditor("maxChunkGraphLength", "p.r.downloads.maxChunkGraphLength", 0, 50000, var2);
      this.createSeparator(var2);
      this.setupBooleanEditor("tableCellEditors", "p.r.downloads.tableCellEditors", var2);
      this.setupBooleanEditor("dragAndDrop", "p.r.downloads.dragAndDrop", var2);
      this.setupBooleanEditor("maintainSortOrder", "p.r.downloads.maintainSortOrder", var2);
      this.setupBooleanEditor("dlIndicateFakes", "p.r.downloads.indicateFakes", var2);
      this.setupBooleanEditor("downloadCompleteDialog", "p.r.downloads.downloadCompleteDialog", var2);
      this.setupBooleanEditor("downloadCompleteLog", "p.r.downloads.downloadCompleteLog", var2);
      this.setupBooleanEditor("mldonkey.InterestedInSources", "p.r.downloads.interestedInSources", var2);
      this.setCompositeLayout(var2);
   }

   protected void createDownloadsPreview(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "m.d.preview");
      String[] var3;
      if (VersionInfo.getOSPlatform().equals("Windows")) {
         var3 = new String[]{"*.exe;*.bat"};
      } else {
         var3 = new String[]{"*"};
      }

      this.createInformationLabel(var2, "p.previewInfo");
      this.setupFileEditor("previewExecutable", "p.r.downloads.previewExecutable", var3, var2);
      this.setupDirectoryEditor("previewWorkingDirectory", "p.r.downloads.previewWorkingDirectory", var2);
      this.createSeparator(var2);
      ArrayList var4 = new ArrayList();
      ArrayList var5 = new ArrayList();
      Composite var7 = new Composite(var2, 0);
      GridData var6 = new GridData(768);
      var6.horizontalSpan = 3;
      var7.setLayoutData(var6);
      var7.setLayout(WidgetFactory.createGridLayout(2, 0, 0, 0, 0, false));
      Composite var8 = new Composite(var7, 0);
      var8.setLayoutData(new GridData(768));
      var8.setLayout(WidgetFactory.createGridLayout(5, 0, 0, 0, 5, false));
      Label var9 = new Label(var8, 0);
      var9.setText(SResources.getString("p.previewExtensionsInfo"));
      var6 = new GridData(768);
      var6.horizontalSpan = 5;
      var9.setLayoutData(var6);
      Label var10 = new Label(var8, 0);
      var10.setText(SResources.getString("l.ext"));
      Text var11 = new Text(var8, 2052);
      var11.setText("mp3");
      new Label(var8, 0).setText("=");
      Text var12 = new Text(var8, 2052);
      var12.setLayoutData(new GridData(768));
      Button var13 = new Button(var8, 0);
      var13.setText(SResources.getString("b.browse"));
      var13.setLayoutData(new GridData(768));
      var13.addSelectionListener(new RootPreferencePage$3(this, var12));
      var6 = new GridData(768);
      var6.horizontalSpan = 5;
      Composite var14 = new Composite(var8, 0);
      var14.setLayout(WidgetFactory.createGridLayout(2, 0, 0, 0, 0, false));
      var14.setLayoutData(var6);
      Button var15 = new Button(var14, 0);
      var15.setText(SResources.getString("b.add"));
      var15.setLayoutData(new GridData(768));
      Button var16 = new Button(var14, 0);
      var16.setText(SResources.getString("b.remove"));
      var16.setLayoutData(new GridData(768));
      List var17 = new List(var7, 2816);
      var6 = new GridData();
      var6.widthHint = 200;
      var6.heightHint = 75;
      var17.setLayoutData(var6);
      var15.addSelectionListener(new RootPreferencePage$4(this, var11, var12, var4, var5, var17));
      var16.addSelectionListener(new RootPreferencePage$5(this, var17, var4, var5));
      this.loadList(var17, var4, var5);
      this.createSeparator(var2);
      this.setupDirectoryEditor("previewDownloadDirectory", "p.r.downloads.previewDownloadDirectory", var2);
      this.createSeparator(var2);
      this.createInformationLabel(var2, "p.previewHttpInfo");
      this.setupBooleanEditor("previewUseHttp", "p.r.downloads.previewUseHttp", var2);
      this.setCompositeLayout(var2);
   }

   protected void createDownloadsExplorer(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "p.explorer");
      String[] var3;
      if (VersionInfo.getOSPlatform().equals("Windows")) {
         var3 = new String[]{"*.exe;*.bat"};
      } else {
         var3 = new String[]{"*"};
      }

      this.createInformationLabel(var2, "p.explorerInfo");
      this.setupFileEditor("explorerExecutable", "p.r.downloads.explorerExecutable", var3, var2);
      this.setupDirectoryEditor("explorerOpenFolder", "p.r.downloads.explorerOpenFolder", var2);
      this.setCompositeLayout(var2);
   }

   protected void createDownloadsWebservices(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "mi.webServices");

      for (int var3 = 1; var3 < 6; var3++) {
         this.setupStringEditor("webServiceName" + var3, var3 + " ", "p.r.downloads.webservice.name", '0', var2);
         this.setupStringEditor("webServiceURL" + var3, var3 + " ", "p.r.downloads.webservice.url", '0', var2);
         this.createSeparator(var2);
      }

      this.setCompositeLayout(var2);
   }

   protected void createSearchTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.search", "tab.search.buttonSmall");
      this.setupBooleanEditor("searchForceDownload", "p.r.search.forceDownload", var2);
      this.setupBooleanEditor("searchFilterPornography", "p.r.search.filterPornography", var2);
      this.setupBooleanEditor("searchFilterProfanity", "p.r.search.filterProfanity", var2);
      this.setupBooleanEditor("searchTooltips", "p.r.search.tooltips", var2);
      this.setupBooleanEditor("searchTooltipsOffset", "p.r.search.tooltipsOffset", var2);
      this.setupBooleanEditor("searchSaveEntries", "p.r.search.saveEntries", var2);
      this.setCompositeLayout(var2);
   }

   protected void createConsoleTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.console", "tab.console.buttonSmall");
      this.setupIntegerEditor("consoleMaxLines", "p.r.console.maxLines", 25, 10000, var2);
      this.createSeparator(var2);
      this.setupIntegerEditor("consoleToolItems", "p.r.console.toolItems", 0, 9, var2);
      this.createSeparator(var2);

      for (int var3 = 1; var3 < 10; var3++) {
         this.setupStringEditor("consoleToolItem" + var3, var3 + " ", "p.r.console.toolItem", '0', var2);
      }

      this.setCompositeLayout(var2);
   }

   protected void createGraphTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.statistics", "tab.statistics.buttonSmall");
      this.createInformationLabel(var2, "p.delayInfo");
      this.setupIntegerEditor("graphUpdateDelay", "p.r.graphs.updateDelay", 0, 600, var2);
      this.createSeparator(var2);
      this.setupIntegerEditor("statsDelay", "p.r.graphs.statsDelay", 1, 600, var2);
      this.setCompositeLayout(var2);
   }

   protected void createRoomsTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.rooms", "tab.rooms.buttonSmall");
      this.setupBooleanEditor("autoCloseRooms", "p.r.rooms.autoClose", var2);
      this.setupBooleanEditor("autoOpenRooms", "p.r.rooms.autoOpen", var2);
      this.setCompositeLayout(var2);
   }

   protected void createIRCTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "l.IRC", "irc");
      this.setupStringEditor("ircServer", "p.r.irc.ircServer", '0', var2);
      this.setupStringEditor("ircChannel", "p.r.irc.ircChannel", '0', var2);
      this.setupStringEditor("ircNickname", "p.r.irc.ircNickname", '0', var2);
      this.setupBooleanEditor("ircAutoConnect", "p.r.irc.autoConnect", var2);
      this.setCompositeLayout(var2);
   }

   protected void createWebBrowserTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.webbrowser", "tab.webbrowser.buttonSmall");
      var2.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      var2.setLayoutData(new GridData(1808));
      TabFolder var3 = new TabFolder(var2, 1024);
      var3.setLayoutData(new GridData(1808));
      this.createWebBrowserFavorites(var3);
      this.createWebBrowserToolItems(var3);
   }

   protected void createWebBrowserToolItems(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "l.toolItems");
      this.setupIntegerEditor("webBrowserToolItems", "p.r.webbrowser.toolItems", 0, 9, var2);
      this.createSeparator(var2);

      for (int var3 = 1; var3 < 10; var3++) {
         this.setupStringEditor("webBrowserToolItem" + var3, var3 + " ", "p.r.webbrowser.toolItem", '0', var2);
      }

      this.setCompositeLayout(var2);
   }

   protected void createWebBrowserFavorites(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "l.favorites");
      if (SWT.getPlatform().equals("win32")) {
         this.createInformationLabel(var2, "p.favoritesDirectoryInfo");
         this.setupDirectoryEditor("favoritesDirectory", "p.r.webbrowser.favoritesDirectory", var2);
      }

      this.setupFileEditor("bookmarksFile", "p.r.webbrowser.bookmarksFile", new String[]{"*.html", "*.htm"}, var2);
      this.setupFileEditor("adrFile", "p.r.webbrowser.adrFile", new String[]{"*.adr"}, var2);
      this.setupIntegerEditor("maxFavoriteLength", "p.r.webbrowser.maxFavoriteLength", 0, 500, var2);
      this.setCompositeLayout(var2);
   }

   protected String[] getLocales() {
      // Translations now ship on the classpath (inside the jar), so list those; also
      // include any sancho_<locale>.properties the user dropped in the home dir as an
      // override. (Previously this scanned only the home dir, so the list came up empty
      // once the bundled files moved into the jar.)
      TreeSet locales = new TreeSet();

      try {
         CodeSource codeSource = RootPreferencePage.class.getProtectionDomain().getCodeSource();
         if (codeSource != null && codeSource.getLocation() != null) {
            File classpathRoot = new File(codeSource.getLocation().toURI());
            if (classpathRoot.isFile()) {
               ZipFile jar = new ZipFile(classpathRoot);

               try {
                  Enumeration entries = jar.entries();

                  while (entries.hasMoreElements()) {
                     this.addLocale(locales, ((ZipEntry)entries.nextElement()).getName());
                  }
               } finally {
                  jar.close();
               }
            } else if (classpathRoot.isDirectory()) {
               this.addLocalesFromDir(locales, classpathRoot);
            }
         }
      } catch (Exception e) {
         Sancho.pDebug("getLocales: " + e);
      }

      this.addLocalesFromDir(locales, new File(VersionInfo.getHomeDirectory()));
      return (String[])locales.toArray(new String[locales.size()]);
   }

   private void addLocalesFromDir(Set locales, File dir) {
      File[] files = dir.listFiles(new RootPreferencePage$PropertiesFilter());
      if (files != null) {
         for (int i = 0; i < files.length; i++) {
            this.addLocale(locales, files[i].getName());
         }
      }
   }

   private void addLocale(Set locales, String fileName) {
      // Extract <locale> from a top-level "sancho_<locale>.properties"; skip the base
      // sancho.properties and any nested path such as sancho/version.properties.
      String prefix = VersionInfo.getName() + "_";
      String suffix = ".properties";
      if (fileName.indexOf(47) < 0 && fileName.indexOf(92) < 0 && fileName.startsWith(prefix) && fileName.endsWith(suffix)) {
         String locale = fileName.substring(prefix.length(), fileName.length() - suffix.length());
         if (locale.length() > 0) {
            locales.add(locale);
         }
      }
   }

   public void refreshList(List var1, ArrayList var2, ArrayList var3) {
      var1.removeAll();
      String[] var4 = new String[var2.size()];

      for (int var5 = 0; var5 < var2.size(); var5++) {
         var4[var5] = var2.get(var5) + " = " + var3.get(var5);
      }

      var1.setItems(var4);
      this.saveList(var2, var3);
   }

   public void saveList(ArrayList var1, ArrayList var2) {
      StringBuffer var3 = new StringBuffer();

      for (int var4 = 0; var4 < var1.size(); var4++) {
         var3.append(var1.get(var4));
         var3.append(";");
         var3.append(var2.get(var4));
         var3.append(";");
      }

      PreferenceLoader.getPreferenceStore().setValue("previewExtensions", var3.toString());
   }

   public void loadList(List var1, ArrayList var2, ArrayList var3) {
      String var4 = PreferenceLoader.loadString("previewExtensions");
      if (!var4.equals("")) {
         StringTokenizer var5 = new StringTokenizer(var4, ";");
         String var6 = "";
         String var7 = "";

         while (var5.hasMoreTokens()) {
            var6 = var5.nextToken();
            if (var5.hasMoreTokens()) {
               var7 = var5.nextToken();
               var1.add(var6 + " = " + var7);
               var2.add(var6);
               var3.add(var7);
            }
         }
      }
   }
}
