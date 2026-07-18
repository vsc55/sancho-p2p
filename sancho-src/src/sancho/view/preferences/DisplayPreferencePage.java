package sancho.view.preferences;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import sancho.view.utility.WidgetFactory;

public class DisplayPreferencePage extends CPreferencePage {
   public DisplayPreferencePage(String var1) {
      super(var1);
   }

   protected Control createContents(Composite var1) {
      Composite var2 = new Composite(var1, 0);
      var2.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      TabFolder var3 = new TabFolder(var2, 0);
      var3.setLayoutData(new GridData(1808));
      this.createGeneralTab(var3);
      this.createDownloadsTab(var3);
      this.createConsoleTab(var3);
      this.createServerTab(var3);
      this.createSearchTab(var3);
      this.createGraphsTab(var3);
      this.createClientsTab(var3);
      return var2;
   }

   protected void createGeneralTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "p.general", "preferences");
      this.setupFontEditor("headerFontData", "p.d.general.headerFont", var2);
      this.setupFontEditor("tableFontData", "p.d.general.tableFont", var2);
      this.setupColorEditor("tablesBackgroundColor", "p.d.general.tablesBackgroundColor", var2);
      this.createSeparator(var2);
      this.setupBooleanEditor("tableAlternateBGColors", "p.d.general.tableAlternateBGColors", var2);
      this.setupColorEditor("tableAlternateBGColor", "p.d.general.tableAlternateBGColor", var2);
      this.createSeparator(var2);
      this.setupBooleanEditor("tableSortIndicator", "p.d.general.tableSortIndicator", var2);
      this.createSeparator(var2);
      this.setupColorEditor("dndBackgroundColor", "p.d.general.dndBackgroundColor", var2);
      this.setupColorEditor("dndForegroundColor", "p.d.general.dndForegroundColor", var2);
      this.setupFontEditor("dndFontData", "p.d.general.dndFont", var2);
      this.setupIntegerEditor("dndWidth", "p.d.general.dndWidth", 5, 1000, var2);
      this.createSeparator(var2);
      this.setupBooleanEditor("humanReadable", "p.d.general.humanReadable", var2);
      this.setupBooleanEditor("maxMegabytes", "p.d.general.maxMegabytes", var2);
      this.setupIntegerEditor("humanReadableDecimals", "p.d.general.humanReadableDecimals", -1, 9, var2);
      this.setupBooleanEditor("verboseNumbers", "p.d.general.verboseNumbers", var2);
      this.createSeparator(var2);
      this.setupIntegerEditor("maxMenuItems", "p.d.general.maxMenuItems", 3, 100000, var2);
      this.createSeparator(var2);
      this.setupBooleanEditor("splashScreen", "p.d.general.splashScreen", var2);
      this.setupBooleanEditor("flatInterface", "p.d.general.flatInterface", var2);
      this.setupBooleanEditor("useGradient", "p.d.general.useGradient", var2);
      this.setupBooleanEditor("displayTableColors", "p.d.general.displayTableColors", var2);
      this.setupBooleanEditor("displayGridLines", "p.d.general.displayGridLines", var2);
      this.setupBooleanEditor("followSelection", "p.d.general.followSelection", var2);
      this.setCompositeLayout(var2);
   }

   protected void createDownloadsTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.transfers", "tab.transfers.buttonSmall");
      var2.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      var2.setLayoutData(new GridData(1808));
      TabFolder var3 = new TabFolder(var2, 1024);
      var3.setLayoutData(new GridData(1808));
      this.createDownloadsColorsTab(var3);
      this.createDownloadsFontsTab(var3);
      this.createDownloadsChunksTab(var3);
   }

   protected void createDownloadsColorsTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "l.colors");
      this.setupColorEditor("downloadsAvailableFileColor", "p.d.downloads.available", var2);
      this.setupColorEditor("downloadsUnAvailableFileColor", "p.d.downloads.unavailable", var2);
      this.setupColorEditor("downloadsPausedFileColor", "p.d.downloads.paused", var2);
      this.setupColorEditor("downloadsQueuedFileColor", "p.d.downloads.queued", var2);
      this.setupColorEditor("downloadsDownloadedFileColor", "p.d.downloads.downloaded", var2);
      this.setupColorEditor("downloadsRateAbove20FileColor", "p.d.downloads.rateAbove20", var2);
      this.setupColorEditor("downloadsRateAbove10FileColor", "p.d.downloads.rateAbove10", var2);
      this.setupColorEditor("downloadsRateAbove0FileColor", "p.d.downloads.rateAbove0", var2);
      this.setupColorEditor("downloadsContainsFakeColor", "p.d.downloads.containsFake", var2);
      this.setCompositeLayout(var2);
   }

   protected void createDownloadsChunksTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "l.chunkGraphs");
      String[] var3 = PreferenceLoader.getChunkStrings();

      for (int var4 = 0; var4 < var3.length; var4++) {
         String var5 = "chunk" + var3[var4].substring(0, 1).toUpperCase() + var3[var4].substring(1);
         String var6 = "p.d.chunk." + var3[var4];
         this.setupColorEditor(var5, var6, var2);
      }

      this.setCompositeLayout(var2);
   }

   protected void createDownloadsFontsTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "l.fonts");
      this.setupFontEditor("downloadsPausedFontData", "p.d.downloads.pausedFontData", var2);
      this.setupFontEditor("downloadsQueuedFontData", "p.d.downloads.queuedFontData", var2);
      this.setupFontEditor("downloadsDownloadedFontData", "p.d.downloads.downloadedFontData", var2);
      this.setupFontEditor("downloadsRateAbove20FontData", "p.d.downloads.rateAbove20FontData", var2);
      this.setupFontEditor("downloadsRateAbove10FontData", "p.d.downloads.rateAbove10FontData", var2);
      this.setupFontEditor("downloadsRateAbove0FontData", "p.d.downloads.rateAbove0FontData", var2);
      this.setCompositeLayout(var2);
   }

   protected void createConsoleTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.console", "tab.console.buttonSmall");
      this.setupFontEditor("consoleFontData", "p.d.console.font", var2);
      this.createSeparator(var2);
      this.setupColorEditor("consoleBackground", "p.d.console.background", var2);
      this.setupColorEditor("consoleForeground", "p.d.console.foreground", var2);
      this.setupColorEditor("consoleHighlight", "p.d.console.highlight", var2);
      this.setupColorEditor("consoleInputBackground", "p.d.console.inputBackground", var2);
      this.setupColorEditor("consoleInputForeground", "p.d.console.inputForeground", var2);
      this.createSeparator(var2);
      this.setupBooleanEditor("consoleUnderlineURLs", "p.d.console.underlineURLs", var2);
      this.setCompositeLayout(var2);
   }

   protected void createServerTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.servers", "tab.servers.buttonSmall");
      this.setupColorEditor("serverConnectedColor", "p.d.server.connected", var2);
      this.setupColorEditor("serverConnectingColor", "p.d.server.connecting", var2);
      this.setupColorEditor("serverDisconnectedColor", "p.d.server.disconnected", var2);
      this.setupColorEditor("addrBlockedColor", "p.d.general.addrBlockedColor", var2);
      this.setCompositeLayout(var2);
   }

   protected void createSearchTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.search", "tab.search.buttonSmall");
      this.setupColorEditor("resultAlreadyDownloadedColor", "p.d.search.alreadyDownloaded", var2);
      this.setupColorEditor("resultFakeColor", "p.d.search.fake", var2);
      this.setupColorEditor("resultDefaultColor", "p.d.search.default", var2);
      this.setCompositeLayout(var2);
   }

   protected void createGraphsTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "tab.statistics", "tab.statistics.buttonSmall");
      this.setupColorEditor("graphBackgroundColor", "p.d.graphs.background", var2);
      this.setupColorEditor("graphGridColor", "p.d.graphs.grid", var2);
      this.setupColorEditor("graphTextColor", "p.d.graphs.text", var2);
      this.setupColorEditor("graphUploadsColor1", "p.d.graphs.uploads1", var2);
      this.setupColorEditor("graphUploadsColor2", "p.d.graphs.uploads2", var2);
      this.setupColorEditor("graphDownloadsColor1", "p.d.graphs.downloads1", var2);
      this.setupColorEditor("graphDownloadsColor2", "p.d.graphs.downloads2", var2);
      this.setupColorEditor("graphLabelBackgroundColor", "p.d.graphs.labelBackground", var2);
      this.setupColorEditor("graphLabelTextColor", "p.d.graphs.labelText", var2);
      this.setupColorEditor("graphLabelLineColor", "p.d.graphs.labelLine", var2);
      this.setupColorEditor("networkEnabledColor", "p.d.stats.networkEnabled", var2);
      this.setCompositeLayout(var2);
   }

   protected void createClientsTab(TabFolder var1) {
      Composite var2 = this.createNewTab(var1, "l.clients", "tab.friends.buttonSmall");
      this.setupColorEditor("clientsDisconnectedColor", "p.d.clients.disconnectedColor", var2);
      this.setupColorEditor("clientsConnectedColor", "p.d.clients.connectedColor", var2);
      this.setupColorEditor("clientsHasFilesColor", "p.d.clients.hasFilesColor", var2);
      this.setCompositeLayout(var2);
   }

}
