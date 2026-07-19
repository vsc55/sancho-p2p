package sancho.view.preferences;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import sancho.view.utility.WidgetFactory;

public class DisplayPreferencePage extends CPreferencePage {
   public DisplayPreferencePage(String title) {
      super(title);
   }

   protected Control createContents(Composite parent) {
      Composite composite = new Composite(parent, 0);
      composite.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      TabFolder tabFolder = new TabFolder(composite, 0);
      tabFolder.setLayoutData(new GridData(1808));
      this.createGeneralTab(tabFolder);
      this.createDownloadsTab(tabFolder);
      this.createConsoleTab(tabFolder);
      this.createServerTab(tabFolder);
      this.createSearchTab(tabFolder);
      this.createGraphsTab(tabFolder);
      this.createClientsTab(tabFolder);
      return composite;
   }

   protected void createGeneralTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "p.general", "preferences");
      this.setupFontEditor("headerFontData", "p.d.general.headerFont", composite);
      this.setupFontEditor("tableFontData", "p.d.general.tableFont", composite);
      this.setupColorEditor("tablesBackgroundColor", "p.d.general.tablesBackgroundColor", composite);
      this.createSeparator(composite);
      this.setupBooleanEditor("tableAlternateBGColors", "p.d.general.tableAlternateBGColors", composite);
      this.setupColorEditor("tableAlternateBGColor", "p.d.general.tableAlternateBGColor", composite);
      this.createSeparator(composite);
      this.setupBooleanEditor("tableSortIndicator", "p.d.general.tableSortIndicator", composite);
      this.createSeparator(composite);
      this.setupColorEditor("dndBackgroundColor", "p.d.general.dndBackgroundColor", composite);
      this.setupColorEditor("dndForegroundColor", "p.d.general.dndForegroundColor", composite);
      this.setupFontEditor("dndFontData", "p.d.general.dndFont", composite);
      this.setupIntegerEditor("dndWidth", "p.d.general.dndWidth", 5, 1000, composite);
      this.createSeparator(composite);
      this.setupBooleanEditor("humanReadable", "p.d.general.humanReadable", composite);
      this.setupBooleanEditor("maxMegabytes", "p.d.general.maxMegabytes", composite);
      this.setupIntegerEditor("humanReadableDecimals", "p.d.general.humanReadableDecimals", -1, 9, composite);
      this.setupBooleanEditor("verboseNumbers", "p.d.general.verboseNumbers", composite);
      this.createSeparator(composite);
      this.setupIntegerEditor("maxMenuItems", "p.d.general.maxMenuItems", 3, 100000, composite);
      this.createSeparator(composite);
      this.setupBooleanEditor("splashScreen", "p.d.general.splashScreen", composite);
      this.setupBooleanEditor("flatInterface", "p.d.general.flatInterface", composite);
      this.setupBooleanEditor("useGradient", "p.d.general.useGradient", composite);
      this.setupBooleanEditor("displayTableColors", "p.d.general.displayTableColors", composite);
      this.setupBooleanEditor("displayGridLines", "p.d.general.displayGridLines", composite);
      this.setupBooleanEditor("followSelection", "p.d.general.followSelection", composite);
      this.setCompositeLayout(composite);
   }

   protected void createDownloadsTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.transfers", "tab.transfers.buttonSmall");
      composite.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      composite.setLayoutData(new GridData(1808));
      TabFolder subTabFolder = new TabFolder(composite, 1024);
      subTabFolder.setLayoutData(new GridData(1808));
      this.createDownloadsColorsTab(subTabFolder);
      this.createDownloadsFontsTab(subTabFolder);
      this.createDownloadsChunksTab(subTabFolder);
   }

   protected void createDownloadsColorsTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "l.colors");
      this.setupColorEditor("downloadsAvailableFileColor", "p.d.downloads.available", composite);
      this.setupColorEditor("downloadsUnAvailableFileColor", "p.d.downloads.unavailable", composite);
      this.setupColorEditor("downloadsPausedFileColor", "p.d.downloads.paused", composite);
      this.setupColorEditor("downloadsQueuedFileColor", "p.d.downloads.queued", composite);
      this.setupColorEditor("downloadsDownloadedFileColor", "p.d.downloads.downloaded", composite);
      this.setupColorEditor("downloadsRateAbove20FileColor", "p.d.downloads.rateAbove20", composite);
      this.setupColorEditor("downloadsRateAbove10FileColor", "p.d.downloads.rateAbove10", composite);
      this.setupColorEditor("downloadsRateAbove0FileColor", "p.d.downloads.rateAbove0", composite);
      this.setupColorEditor("downloadsContainsFakeColor", "p.d.downloads.containsFake", composite);
      this.setCompositeLayout(composite);
   }

   protected void createDownloadsChunksTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "l.chunkGraphs");
      String[] chunkStrings = PreferenceLoader.getChunkStrings();

      for (int i = 0; i < chunkStrings.length; i++) {
         String prefKey = "chunk" + chunkStrings[i].substring(0, 1).toUpperCase() + chunkStrings[i].substring(1);
         String labelKey = "p.d.chunk." + chunkStrings[i];
         this.setupColorEditor(prefKey, labelKey, composite);
      }

      this.setCompositeLayout(composite);
   }

   protected void createDownloadsFontsTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "l.fonts");
      this.setupFontEditor("downloadsPausedFontData", "p.d.downloads.pausedFontData", composite);
      this.setupFontEditor("downloadsQueuedFontData", "p.d.downloads.queuedFontData", composite);
      this.setupFontEditor("downloadsDownloadedFontData", "p.d.downloads.downloadedFontData", composite);
      this.setupFontEditor("downloadsRateAbove20FontData", "p.d.downloads.rateAbove20FontData", composite);
      this.setupFontEditor("downloadsRateAbove10FontData", "p.d.downloads.rateAbove10FontData", composite);
      this.setupFontEditor("downloadsRateAbove0FontData", "p.d.downloads.rateAbove0FontData", composite);
      this.setCompositeLayout(composite);
   }

   protected void createConsoleTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.console", "tab.console.buttonSmall");
      this.setupFontEditor("consoleFontData", "p.d.console.font", composite);
      this.createSeparator(composite);
      this.setupColorEditor("consoleBackground", "p.d.console.background", composite);
      this.setupColorEditor("consoleForeground", "p.d.console.foreground", composite);
      this.setupColorEditor("consoleHighlight", "p.d.console.highlight", composite);
      this.setupColorEditor("consoleInputBackground", "p.d.console.inputBackground", composite);
      this.setupColorEditor("consoleInputForeground", "p.d.console.inputForeground", composite);
      this.createSeparator(composite);
      this.setupBooleanEditor("consoleUnderlineURLs", "p.d.console.underlineURLs", composite);
      this.setCompositeLayout(composite);
   }

   protected void createServerTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.servers", "tab.servers.buttonSmall");
      this.setupColorEditor("serverConnectedColor", "p.d.server.connected", composite);
      this.setupColorEditor("serverConnectingColor", "p.d.server.connecting", composite);
      this.setupColorEditor("serverDisconnectedColor", "p.d.server.disconnected", composite);
      this.setupColorEditor("addrBlockedColor", "p.d.general.addrBlockedColor", composite);
      this.setCompositeLayout(composite);
   }

   protected void createSearchTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.search", "tab.search.buttonSmall");
      this.setupColorEditor("resultAlreadyDownloadedColor", "p.d.search.alreadyDownloaded", composite);
      this.setupColorEditor("resultFakeColor", "p.d.search.fake", composite);
      this.setupColorEditor("resultDefaultColor", "p.d.search.default", composite);
      this.createSeparator(composite);
      this.setupStringEditor("searchFormats", "p.d.search.formats", '0', composite);
      this.setCompositeLayout(composite);
   }

   protected void createGraphsTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.statistics", "tab.statistics.buttonSmall");
      this.setupColorEditor("graphBackgroundColor", "p.d.graphs.background", composite);
      this.setupColorEditor("graphGridColor", "p.d.graphs.grid", composite);
      this.setupColorEditor("graphTextColor", "p.d.graphs.text", composite);
      this.setupColorEditor("graphUploadsColor1", "p.d.graphs.uploads1", composite);
      this.setupColorEditor("graphUploadsColor2", "p.d.graphs.uploads2", composite);
      this.setupColorEditor("graphDownloadsColor1", "p.d.graphs.downloads1", composite);
      this.setupColorEditor("graphDownloadsColor2", "p.d.graphs.downloads2", composite);
      this.setupColorEditor("graphLabelBackgroundColor", "p.d.graphs.labelBackground", composite);
      this.setupColorEditor("graphLabelTextColor", "p.d.graphs.labelText", composite);
      this.setupColorEditor("graphLabelLineColor", "p.d.graphs.labelLine", composite);
      this.setupColorEditor("networkEnabledColor", "p.d.stats.networkEnabled", composite);
      this.setCompositeLayout(composite);
   }

   protected void createClientsTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "l.clients", "tab.friends.buttonSmall");
      this.setupColorEditor("clientsDisconnectedColor", "p.d.clients.disconnectedColor", composite);
      this.setupColorEditor("clientsConnectedColor", "p.d.clients.connectedColor", composite);
      this.setupColorEditor("clientsHasFilesColor", "p.d.clients.hasFilesColor", composite);
      this.setCompositeLayout(composite);
   }

}
