package sancho.view.preferences;

import java.io.File;
import java.io.FilenameFilter;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;
import sancho.core.Sancho;
import sancho.utility.VersionInfo;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class RootPreferencePage extends CPreferencePage {
   // The "popup on new version" editor + its tab, kept so its enabled state can track the
   // "notify me..." checkbox (including after Restore Defaults).
   private BooleanFieldEditor versionCheckPopupEditor;
   private Composite generalTabComposite;

   public RootPreferencePage(String title) {
      super(title);
   }

   protected void performDefaults() {
      super.performDefaults();
      // Restore Defaults reloads the checkboxes but fires no value event, so re-sync the
      // dependent "popup on new version" editor from the default of the "notify me..." option.
      if (this.versionCheckPopupEditor != null && this.generalTabComposite != null) {
         this.versionCheckPopupEditor.setEnabled(this.getPreferenceStore().getDefaultBoolean("versionCheck"), this.generalTabComposite);
      }
   }

   protected Control createContents(Composite parent) {
      Composite composite = new Composite(parent, 0);
      composite.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      TabFolder tabFolder = new TabFolder(composite, 128);
      tabFolder.setLayoutData(new GridData(1808));
      this.createGeneralTab(tabFolder);
      this.createDownloadsTab(tabFolder);
      this.createSearchTab(tabFolder);
      this.createConsoleTab(tabFolder);
      this.createGraphTab(tabFolder);
      this.createRoomsTab(tabFolder);
      this.createWebBrowserTab(tabFolder);
      return composite;
   }

   protected void createGeneralTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "p.general", "preferences");
      String[] filterExtensions;
      if (VersionInfo.getOSPlatform().equals("Windows")) {
         filterExtensions = new String[]{"*.exe;*.bat"};
      } else {
         filterExtensions = new String[]{"*"};
      }

      this.createInformationLabel(composite, "p.coreExecutableInfo");
      this.setupFileEditor("coreExecutable", "p.r.general.coreExecutable", filterExtensions, composite);
      this.setupBooleanEditor("killSpawnedCoreOnExit", "p.r.general.killSpawnedCoreOnExit", composite);
      this.setupIntegerEditor("killSpawnedCoreDelay", "p.r.general.killSpawnedCoreDelay", 1, 1000, composite);
      this.createSeparator(composite);
      this.setupIntegerEditor("connectionTimeout", "p.r.general.connectionTimeout", 1, 10000, composite);
      this.createSeparator(composite);
      this.createInformationLabel(composite, "p.webBrowserInfo");
      this.setupFileEditor("defaultWebBrowser", "p.r.general.defaultBrowser", filterExtensions, composite);
      this.createSeparator(composite);
      this.createInformationLabel(composite, "p.localeInfo");
      Label label = new Label(composite, 0);
      label.setText(SResources.getString("p.r.general.locale"));
      Combo combo = new Combo(composite, 8);
      String currentLocale = PreferenceLoader.loadString("locale");
      combo.add("");
      String[] locales = this.getLocales();

      for (int i = 0; i < locales.length; i++) {
         combo.add(locales[i]);
         if (locales[i].equals(currentLocale)) {
            combo.select(i + 1);
         }
      }

      combo.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            PreferenceLoader.getPreferenceStore().setValue("locale", combo.getItem(combo.getSelectionIndex()));
         }
      });
      this.createSeparator(composite);
      this.setupBooleanEditor("autoReconnect", "p.r.general.autoReconnect", composite);
      this.setupIntegerEditor("autoReconnectDelay", "p.r.general.autoReconnectDelay", 1, 10000, composite);
      this.createSeparator(composite);
      if (VersionInfo.hasTray()) {
         this.setupBooleanEditor("systrayEnabled", "p.r.general.systrayEnabled", composite);
         this.setupBooleanEditor("systraySingleClick", "p.r.general.systraySingleClick", composite);
         this.setupBooleanEditor("minimizeOnClose", "p.r.general.systrayOnClose", composite);
         this.setupBooleanEditor("systrayOnMinimize", "p.r.general.systrayOnMinimize", composite);
         this.setupBooleanEditor("windowStartTray", "p.r.general.startTray", composite);
      }

      this.setupBooleanEditor("windowStartMinimized", "p.r.general.startMinimized", composite);
      this.setupBooleanEditor("allowMultipleInstances", "p.r.general.multipleInstances", composite);
      this.setupBooleanEditor("hostManagerOnStart", "p.r.general.hostManagerOnStart", composite);
      this.setupBooleanEditor("useLastFile", "p.r.general.useLastFile", composite);
      this.setupBooleanEditor("killCoreOnExit", "p.r.general.killCoreOnExit", composite);
      BooleanFieldEditor versionCheckEditor = this.setupBooleanEditor("versionCheck", "p.r.general.versionCheck", composite);
      this.versionCheckPopupEditor = this.setupBooleanEditor("versionCheckPopup", "p.r.general.versionCheckPopup", composite);
      this.generalTabComposite = composite;
      // "Popup on new version" only has an effect when the automatic check is enabled, so grey
      // it out while "Notify me..." is off, and toggle it live when that checkbox changes.
      this.versionCheckPopupEditor.setEnabled(this.getPreferenceStore().getBoolean("versionCheck"), composite);
      versionCheckEditor.setPropertyChangeListener(new IPropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent event) {
            if (FieldEditor.VALUE.equals(event.getProperty())) {
               RootPreferencePage.this.versionCheckPopupEditor
                  .setEnabled(((Boolean)event.getNewValue()).booleanValue(), RootPreferencePage.this.generalTabComposite);
            }
         }
      });
      this.setCompositeLayout(composite);
   }

   protected void createDownloadsTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.transfers", "tab.transfers.buttonSmall");
      composite.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      composite.setLayoutData(new GridData(1808));
      TabFolder subTabFolder = new TabFolder(composite, 1024);
      subTabFolder.setLayoutData(new GridData(1808));
      this.createDownloadsGeneral(subTabFolder);
      this.createDownloadsPreview(subTabFolder);
      this.createDownloadsExplorer(subTabFolder);
      this.createDownloadsWebservices(subTabFolder);
   }

   protected void createDownloadsGeneral(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "p.general");
      this.createInformationLabel(composite, "p.delayInfo");
      this.setupIntegerEditor("updateDelay", "p.r.downloads.updateDelay", 0, 600, composite);
      this.createSeparator(composite);
      this.setupBooleanEditor("pollUpStats", "p.r.downloads.pollUpstats", composite);
      this.setupIntegerEditor("pollDelay", "p.r.downloads.pollDelay", 1, 600, composite);
      this.createSeparator(composite);
      this.setupIntegerEditor("requestFileInfoDelay", "p.r.downloads.requestFileInfoDelay", 0, 99999, composite);
      this.createSeparator(composite);
      Label label = new Label(composite, 0);
      label.setText(SResources.getString("p.r.downloads.fileDoubleClick"));
      Combo combo = new Combo(composite, 8);
      combo.setLayoutData(new GridData(768));
      combo.add(SResources.getString("l.expandBranch"));
      combo.add(SResources.getString("m.d.preview"));
      combo.add(SResources.getString("m.d.preview") + " (OS)");
      int doubleClick = PreferenceLoader.loadInt("dlFileDoubleClick");
      if (doubleClick >= 0 && doubleClick < combo.getItemCount()) {
         combo.select(doubleClick);
      }

      combo.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            Combo combo = (Combo)event.widget;
            PreferenceLoader.getPreferenceStore().setValue("dlFileDoubleClick", combo.getSelectionIndex());
         }
      });
      this.createSeparator(composite);
      this.setupIntegerEditor("dlPercentDecimals", "p.r.downloads.percentDecimals", 0, 5, composite);
      this.setupIntegerEditor("dlRateDecimals", "p.r.downloads.rateDecimals", 0, 5, composite);
      this.createSeparator(composite);
      this.setupBooleanEditor("displayChunkGraphs", "p.r.downloads.displayChunkGraphs", composite);
      this.setupBooleanEditor("displayChunkGraphPercent", "p.r.downloads.displayChunkGraphPercent", composite);
      this.setupIntegerEditor("maxChunkGraphLength", "p.r.downloads.maxChunkGraphLength", 0, 50000, composite);
      this.createSeparator(composite);
      this.setupBooleanEditor("tableCellEditors", "p.r.downloads.tableCellEditors", composite);
      this.setupBooleanEditor("dragAndDrop", "p.r.downloads.dragAndDrop", composite);
      this.setupBooleanEditor("maintainSortOrder", "p.r.downloads.maintainSortOrder", composite);
      this.setupBooleanEditor("dlIndicateFakes", "p.r.downloads.indicateFakes", composite);
      this.setupBooleanEditor("downloadCompleteDialog", "p.r.downloads.downloadCompleteDialog", composite);
      this.setupBooleanEditor("downloadCompleteLog", "p.r.downloads.downloadCompleteLog", composite);
      this.setupBooleanEditor("mldonkey.InterestedInSources", "p.r.downloads.interestedInSources", composite);
      this.setCompositeLayout(composite);
   }

   protected void createDownloadsPreview(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "m.d.preview");
      String[] filterExtensions;
      if (VersionInfo.getOSPlatform().equals("Windows")) {
         filterExtensions = new String[]{"*.exe;*.bat"};
      } else {
         filterExtensions = new String[]{"*"};
      }

      this.createInformationLabel(composite, "p.previewInfo");
      this.setupFileEditor("previewExecutable", "p.r.downloads.previewExecutable", filterExtensions, composite);
      this.setupDirectoryEditor("previewWorkingDirectory", "p.r.downloads.previewWorkingDirectory", composite);
      this.createSeparator(composite);
      ArrayList extensions = new ArrayList();
      ArrayList applications = new ArrayList();
      Composite container = new Composite(composite, 0);
      GridData gridData = new GridData(768);
      gridData.horizontalSpan = 3;
      container.setLayoutData(gridData);
      container.setLayout(WidgetFactory.createGridLayout(2, 0, 0, 0, 0, false));
      Composite extensionComposite = new Composite(container, 0);
      extensionComposite.setLayoutData(new GridData(768));
      extensionComposite.setLayout(WidgetFactory.createGridLayout(5, 0, 0, 0, 5, false));
      Label infoLabel = new Label(extensionComposite, 0);
      infoLabel.setText(SResources.getString("p.previewExtensionsInfo"));
      gridData = new GridData(768);
      gridData.horizontalSpan = 5;
      infoLabel.setLayoutData(gridData);
      Label extLabel = new Label(extensionComposite, 0);
      extLabel.setText(SResources.getString("l.ext"));
      Text extensionText = new Text(extensionComposite, 2052);
      extensionText.setText("mp3");
      new Label(extensionComposite, 0).setText("=");
      Text applicationText = new Text(extensionComposite, 2052);
      applicationText.setLayoutData(new GridData(768));
      Button browseButton = new Button(extensionComposite, 0);
      browseButton.setText(SResources.getString("b.browse"));
      browseButton.setLayoutData(new GridData(768));
      browseButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            FileDialog dialog = new FileDialog(applicationText.getShell(), 4);
            if (dialog.open() != null) {
               String path = dialog.getFilterPath() + System.getProperty("file.separator");
               path = path + dialog.getFileName();
               applicationText.setText(path);
            }
         }
      });
      gridData = new GridData(768);
      gridData.horizontalSpan = 5;
      Composite buttonComposite = new Composite(extensionComposite, 0);
      buttonComposite.setLayout(WidgetFactory.createGridLayout(2, 0, 0, 0, 0, false));
      buttonComposite.setLayoutData(gridData);
      Button addButton = new Button(buttonComposite, 0);
      addButton.setText(SResources.getString("b.add"));
      addButton.setLayoutData(new GridData(768));
      Button removeButton = new Button(buttonComposite, 0);
      removeButton.setText(SResources.getString("b.remove"));
      removeButton.setLayoutData(new GridData(768));
      List list = new List(container, 2816);
      gridData = new GridData();
      gridData.widthHint = 200;
      gridData.heightHint = 75;
      list.setLayoutData(gridData);
      addButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            if (!extensionText.getText().equals("") && !applicationText.getText().equals("")) {
               String extension = extensionText.getText();
               String application = applicationText.getText();
               extensions.add(extension);
               applications.add(application);
               RootPreferencePage.this.refreshList(list, extensions, applications);
            }
         }
      });
      removeButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            if (list.getSelectionIndex() != -1) {
               int index = list.getSelectionIndex();
               extensions.remove(index);
               applications.remove(index);
               RootPreferencePage.this.refreshList(list, extensions, applications);
            }
         }
      });
      this.loadList(list, extensions, applications);
      this.createSeparator(composite);
      this.setupDirectoryEditor("previewDownloadDirectory", "p.r.downloads.previewDownloadDirectory", composite);
      this.createSeparator(composite);
      this.createInformationLabel(composite, "p.previewHttpInfo");
      this.setupBooleanEditor("previewUseHttp", "p.r.downloads.previewUseHttp", composite);
      this.setCompositeLayout(composite);
   }

   protected void createDownloadsExplorer(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "p.explorer");
      String[] filterExtensions;
      if (VersionInfo.getOSPlatform().equals("Windows")) {
         filterExtensions = new String[]{"*.exe;*.bat"};
      } else {
         filterExtensions = new String[]{"*"};
      }

      this.createInformationLabel(composite, "p.explorerInfo");
      this.setupFileEditor("explorerExecutable", "p.r.downloads.explorerExecutable", filterExtensions, composite);
      this.setupDirectoryEditor("explorerOpenFolder", "p.r.downloads.explorerOpenFolder", composite);
      this.setCompositeLayout(composite);
   }

   protected void createDownloadsWebservices(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "mi.webServices");

      for (int i = 1; i < 6; i++) {
         this.setupStringEditor("webServiceName" + i, i + " ", "p.r.downloads.webservice.name", '0', composite);
         this.setupStringEditor("webServiceURL" + i, i + " ", "p.r.downloads.webservice.url", '0', composite);
         this.createSeparator(composite);
      }

      this.setCompositeLayout(composite);
   }

   protected void createSearchTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.search", "tab.search.buttonSmall");
      this.setupBooleanEditor("searchForceDownload", "p.r.search.forceDownload", composite);
      this.setupBooleanEditor("searchFilterPornography", "p.r.search.filterPornography", composite);
      this.setupBooleanEditor("searchFilterProfanity", "p.r.search.filterProfanity", composite);
      this.setupBooleanEditor("searchTooltips", "p.r.search.tooltips", composite);
      this.setupBooleanEditor("searchTooltipsOffset", "p.r.search.tooltipsOffset", composite);
      this.setupBooleanEditor("searchSaveEntries", "p.r.search.saveEntries", composite);
      this.setCompositeLayout(composite);
   }

   protected void createConsoleTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.console", "tab.console.buttonSmall");
      this.setupIntegerEditor("consoleMaxLines", "p.r.console.maxLines", 25, 10000, composite);
      this.createSeparator(composite);
      this.setupIntegerEditor("consoleToolItems", "p.r.console.toolItems", 0, 9, composite);
      this.createSeparator(composite);

      for (int i = 1; i < 10; i++) {
         this.setupStringEditor("consoleToolItem" + i, i + " ", "p.r.console.toolItem", '0', composite);
      }

      this.setCompositeLayout(composite);
   }

   protected void createGraphTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.statistics", "tab.statistics.buttonSmall");
      this.createInformationLabel(composite, "p.delayInfo");
      this.setupIntegerEditor("graphUpdateDelay", "p.r.graphs.updateDelay", 0, 600, composite);
      this.createSeparator(composite);
      this.setupIntegerEditor("statsDelay", "p.r.graphs.statsDelay", 1, 600, composite);
      this.setCompositeLayout(composite);
   }

   protected void createRoomsTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.rooms", "tab.rooms.buttonSmall");
      this.setupBooleanEditor("autoCloseRooms", "p.r.rooms.autoClose", composite);
      this.setupBooleanEditor("autoOpenRooms", "p.r.rooms.autoOpen", composite);
      this.setCompositeLayout(composite);
   }

   protected void createWebBrowserTab(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "tab.webbrowser", "tab.webbrowser.buttonSmall");
      composite.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      composite.setLayoutData(new GridData(1808));
      TabFolder subTabFolder = new TabFolder(composite, 1024);
      subTabFolder.setLayoutData(new GridData(1808));
      this.createWebBrowserFavorites(subTabFolder);
      this.createWebBrowserToolItems(subTabFolder);
   }

   protected void createWebBrowserToolItems(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "l.toolItems");
      this.setupIntegerEditor("webBrowserToolItems", "p.r.webbrowser.toolItems", 0, 9, composite);
      this.createSeparator(composite);

      for (int i = 1; i < 10; i++) {
         this.setupStringEditor("webBrowserToolItem" + i, i + " ", "p.r.webbrowser.toolItem", '0', composite);
      }

      this.setCompositeLayout(composite);
   }

   protected void createWebBrowserFavorites(TabFolder tabFolder) {
      Composite composite = this.createNewTab(tabFolder, "l.favorites");
      if (SWT.getPlatform().equals("win32")) {
         this.createInformationLabel(composite, "p.favoritesDirectoryInfo");
         this.setupDirectoryEditor("favoritesDirectory", "p.r.webbrowser.favoritesDirectory", composite);
      }

      this.setupFileEditor("bookmarksFile", "p.r.webbrowser.bookmarksFile", new String[]{"*.html", "*.htm"}, composite);
      this.setupFileEditor("adrFile", "p.r.webbrowser.adrFile", new String[]{"*.adr"}, composite);
      this.setupIntegerEditor("maxFavoriteLength", "p.r.webbrowser.maxFavoriteLength", 0, 500, composite);
      this.setCompositeLayout(composite);
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
      File[] files = dir.listFiles(new PropertiesFilter());
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

   public void refreshList(List list, ArrayList extensions, ArrayList applications) {
      list.removeAll();
      String[] items = new String[extensions.size()];

      for (int i = 0; i < extensions.size(); i++) {
         items[i] = extensions.get(i) + " = " + applications.get(i);
      }

      list.setItems(items);
      this.saveList(extensions, applications);
   }

   public void saveList(ArrayList extensions, ArrayList applications) {
      StringBuffer buffer = new StringBuffer();

      for (int i = 0; i < extensions.size(); i++) {
         buffer.append(extensions.get(i));
         buffer.append(";");
         buffer.append(applications.get(i));
         buffer.append(";");
      }

      PreferenceLoader.getPreferenceStore().setValue("previewExtensions", buffer.toString());
   }

   public void loadList(List list, ArrayList extensions, ArrayList applications) {
      String saved = PreferenceLoader.loadString("previewExtensions");
      if (!saved.equals("")) {
         StringTokenizer tokenizer = new StringTokenizer(saved, ";");
         String extension = "";
         String application = "";

         while (tokenizer.hasMoreTokens()) {
            extension = tokenizer.nextToken();
            if (tokenizer.hasMoreTokens()) {
               application = tokenizer.nextToken();
               list.add(extension + " = " + application);
               extensions.add(extension);
               applications.add(application);
            }
         }
      }
   }

   // Accepts sancho_*.properties translation files when enumerating available locales.
   static class PropertiesFilter implements FilenameFilter {
      public boolean accept(File dir, String name) {
         String lower = name.toLowerCase();
         return lower.startsWith(VersionInfo.getName()) && lower.endsWith(".properties");
      }
   }
}
