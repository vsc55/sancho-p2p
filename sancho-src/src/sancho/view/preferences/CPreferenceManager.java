package sancho.view.preferences;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;
import sancho.core.ICore;
import sancho.core.Sancho;
import sancho.model.mldonkey.Option;
import sancho.model.mldonkey.OptionCollection;
import sancho.utility.VersionInfo;
import sancho.view.utility.SResources;

public class CPreferenceManager extends PreferenceManager {
   private PreferenceDialog prefDialog;
   private PreferenceStore preferenceStore;

   public CPreferenceManager(PreferenceStore store) {
      this.preferenceStore = store;
      this.addNode(this.getRoot(), new RootPreferencePage(VersionInfo.getName() + ": " + SResources.getString("p.node.main")), "preferences");
      this.addNode(this.getRoot(), new DisplayPreferencePage(VersionInfo.getName() + ": " + SResources.getString("p.node.display")), "display");
      this.addWinRegistryPage(this.getRoot());
   }

   protected IPreferenceNode addNode(IPreferenceNode parent, CPreferencePage page, String imageKey) {
      page.setPreferenceStore(this.preferenceStore);
      PreferenceNode node = new ImagePreferenceNode(page.getTitle(), page, SResources.getImageDescriptor(imageKey));
      parent.add(node);
      return node;
   }

   protected void addWinRegistryPage(IPreferenceNode parent) {
      // Shown on Windows, plus in debug builds on any platform (so the page can be
      // previewed while developing). The actual registry shell-out is win32-guarded in
      // WinRegPreferencePage.updateRegistry, so a debug preview off Windows won't try to
      // launch regedit.exe.
      if (VersionInfo.getOSPlatform().equals("Windows") || Sancho.debug) {
         this.addNode(parent, new WinRegPreferencePage(VersionInfo.getName() + ": " + SResources.getString("p.node.windowsRegistry")), "regedit");
      }
   }

   public int open(Shell shell) {
      try {
         this.initialize(this.preferenceStore);
      } catch (IOException ioException) {
         Sancho.pDebug("PM: " + ioException);
      }

      this.prefDialog = new SanchoPreferenceDialog(shell, this);
      PreferenceDialog.setDefaultImage(VersionInfo.getProgramIcon());
      if (Sancho.hasCollectionFactory()) {
         this.createMLDonkeyOptions(Sancho.getCore());
      }

      return this.prefDialog.open();
   }

   private void createMLDonkeyOptions(ICore core) {
      OptionCollection optionCollection = core.getOptionCollection();
      MLDonkeyPreferenceStore store = new MLDonkeyPreferenceStore();
      store.setInput(optionCollection);
      HashMap sectionMap = new HashMap();
      HashMap pluginMap = new HashMap();
      MLDonkeyPreferencePage advancedPage = null;
      MLDonkeyPreferencePage allPage = new MLDonkeyPreferencePage(this.corePrefix() + SResources.getString("l.all"), 1);
      allPage.setPreferenceStore(store);
      allPage.setAllOptions();
      Iterator iterator = optionCollection.keySet().iterator();

      while (iterator.hasNext()) {
         Option option = (Option)optionCollection.get(iterator.next());
         String section = option.getSection();
         String plugin = option.getPlugin();
         allPage.addOption(option);
         if ((section != null || plugin != null) && (section == null || !section.equalsIgnoreCase("other"))) {
            if (section != null) {
               this.addToMap(sectionMap, section, store, option);
            } else if (plugin != null) {
               this.addToMap(pluginMap, plugin, store, option);
            }
         } else {
            advancedPage = this.addAdvancedOption(advancedPage, option, store);
         }
      }

      this.addSortedOptions(sectionMap, this.getRoot());
      if (pluginMap.size() != 0) {
         Object networksNode = this.find("Networks");
         if (networksNode == null) {
            MLDonkeyPreferencePage networksPage = new MLDonkeyPreferencePage(this.sectionLabel("Networks"), 0);
            networksNode = new ImagePreferenceNode("Networks", networksPage, SResources.getImageDescriptor("globe"));
            networksPage.setEmpty(true);
            this.addToRoot((IPreferenceNode)networksNode);
         }

         this.addSortedOptions(pluginMap, (IPreferenceNode)networksNode);
      }

      if (advancedPage != null) {
         this.addToRoot(new ImagePreferenceNode("Advanced", advancedPage, SResources.getImageDescriptor("bulb-small")));
      }

      this.addToRoot(new ImagePreferenceNode("All", allPage, SResources.getImageDescriptor("exclamation")));
   }

   private void addSortedOptions(Map pageMap, IPreferenceNode parent) {
      String[] keys = new String[pageMap.keySet().size()];
      pageMap.keySet().toArray(keys);
      Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);

      for (int i = 0; i < keys.length; i++) {
         MLDonkeyPreferencePage page = (MLDonkeyPreferencePage)pageMap.get(keys[i]);
         Hashtable iconMap = new Hashtable();
         iconMap.put("bittorrent", "e.network.bittorrent.connected-16");
         iconMap.put("donkey", "e.network.donkey.connected-16");
         iconMap.put("fasttrack", "e.network.fasttrack.connected-16");
         iconMap.put("filetp", "e.network.filetp.connected-16");
         iconMap.put("gnutella", "e.network.gnutella.connected-16");
         iconMap.put("g2", "e.network.gnutella2.connected-16");
         iconMap.put("soulseek", "e.network.soulseek.connected-16");
         iconMap.put("opennap", "e.network.opennap.connected-16");
         iconMap.put("networks", "globe");
         iconMap.put("mail", "new-message");
         iconMap.put("debug", "info");
         iconMap.put("html mods", "tab.webbrowser.buttonSmall");
         iconMap.put("network config", "menu-connect");
         iconMap.put("bandwidth", "tab.transfers.buttonSmall");
         iconMap.put("paths", "file-explorer");
         iconMap.put("security", "lock");
         iconMap.put("download", "arrow-down-green");
         iconMap.put("interfaces", "display");
         iconMap.put("startup", "startup");
         String imageKey = (String)iconMap.get(keys[i].toLowerCase());
         if (imageKey == null) {
            imageKey = "preferences";
         }

         parent.add(new ImagePreferenceNode(keys[i], page, SResources.getImageDescriptor(imageKey)));
      }
   }

   // Core option sections (Bandwidth, Debug, Networks, ...) come from the MLDonkey core in
   // English. Show them as "<Core>: <section>" — mirroring the "sancho:" pages — where both
   // the "Core" prefix and the section name are localizable, falling back to the core's raw
   // section name when we have no translation (plugins / networks / future core sections).
   private String sectionLabel(String coreSection) {
      String key = "p.node.section." + coreSection.toLowerCase().replace(' ', '_');
      String translated = SResources.getString(key);
      String name = translated.equals(key) ? coreSection : translated;
      return this.corePrefix() + name;
   }

   // The localizable "Core: " / "Nucleo: " prefix that groups every core-settings page.
   private String corePrefix() {
      return SResources.getString("p.node.section.prefix") + ": ";
   }

   private void addToMap(Map pageMap, String key, MLDonkeyPreferenceStore store, Option option) {
      if (!pageMap.containsKey(key)) {
         MLDonkeyPreferencePage page = new MLDonkeyPreferencePage(this.sectionLabel(key), 1);
         pageMap.put(key, page);
         page.setPreferenceStore(store);
      }

      ((MLDonkeyPreferencePage)pageMap.get(key)).addOption(option);
   }

   private MLDonkeyPreferencePage addAdvancedOption(MLDonkeyPreferencePage advancedPage, Option option, MLDonkeyPreferenceStore store) {
      if (advancedPage == null) {
         advancedPage = new MLDonkeyPreferencePage(this.corePrefix() + SResources.getString("l.advanced") + "*", 1);
         advancedPage.setPreferenceStore(store);
      }

      advancedPage.addOption(option);
      return advancedPage;
   }

   public void initialize(PreferenceStore store) throws IOException {
      try {
         store.load();
      } catch (IOException ioException) {
         store.save();
         store.load();
      }
   }
}
