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

   public CPreferenceManager(PreferenceStore var1) {
      this.preferenceStore = var1;
      this.addNode(this.getRoot(), new RootPreferencePage(VersionInfo.getName() + ": " + SResources.getString("p.node.main")), "preferences");
      this.addNode(this.getRoot(), new DisplayPreferencePage(VersionInfo.getName() + ": " + SResources.getString("p.node.display")), "display");
      this.addWinRegistryPage(this.getRoot());
   }

   protected IPreferenceNode addNode(IPreferenceNode var1, CPreferencePage var2, String var3) {
      var2.setPreferenceStore(this.preferenceStore);
      PreferenceNode var4 = new ImagePreferenceNode(var2.getTitle(), var2, SResources.getImageDescriptor(var3));
      var1.add(var4);
      return var4;
   }

   protected void addWinRegistryPage(IPreferenceNode var1) {
      // Shown on Windows, plus in debug builds on any platform (so the page can be
      // previewed while developing). The actual registry shell-out is win32-guarded in
      // WinRegPreferencePage.updateRegistry, so a debug preview off Windows won't try to
      // launch regedit.exe.
      if (VersionInfo.getOSPlatform().equals("Windows") || Sancho.debug) {
         this.addNode(var1, new WinRegPreferencePage(VersionInfo.getName() + ": " + SResources.getString("p.node.windowsRegistry")), "regedit");
      }
   }

   public int open(Shell var1) {
      try {
         this.initialize(this.preferenceStore);
      } catch (IOException var3) {
         Sancho.pDebug("PM: " + var3);
      }

      this.prefDialog = new SanchoPreferenceDialog(var1, this);
      PreferenceDialog.setDefaultImage(VersionInfo.getProgramIcon());
      if (Sancho.hasCollectionFactory()) {
         this.createMLDonkeyOptions(Sancho.getCore());
      }

      return this.prefDialog.open();
   }

   private void createMLDonkeyOptions(ICore var1) {
      OptionCollection var2 = var1.getOptionCollection();
      MLDonkeyPreferenceStore var3 = new MLDonkeyPreferenceStore();
      var3.setInput(var2);
      HashMap var4 = new HashMap();
      HashMap var5 = new HashMap();
      MLDonkeyPreferencePage var6 = null;
      MLDonkeyPreferencePage var7 = new MLDonkeyPreferencePage(SResources.getString("l.all"), 1);
      var7.setPreferenceStore(var3);
      var7.setAllOptions();
      Iterator var8 = var2.keySet().iterator();

      while (var8.hasNext()) {
         Option var9 = (Option)var2.get(var8.next());
         String var10 = var9.getSection();
         String var11 = var9.getPlugin();
         var7.addOption(var9);
         if ((var10 != null || var11 != null) && (var10 == null || !var10.equalsIgnoreCase("other"))) {
            if (var10 != null) {
               this.addToMap(var4, var10, var3, var9);
            } else if (var11 != null) {
               this.addToMap(var5, var11, var3, var9);
            }
         } else {
            var6 = this.addAdvancedOption(var6, var9, var3);
         }
      }

      this.addSortedOptions(var4, this.getRoot());
      if (var5.size() != 0) {
         Object var12 = this.find("Networks");
         if (var12 == null) {
            MLDonkeyPreferencePage var13 = new MLDonkeyPreferencePage("Networks", 0);
            var12 = new ImagePreferenceNode("Networks", var13, SResources.getImageDescriptor("globe"));
            var13.setEmpty(true);
            this.addToRoot((IPreferenceNode)var12);
         }

         this.addSortedOptions(var5, (IPreferenceNode)var12);
      }

      if (var6 != null) {
         this.addToRoot(new ImagePreferenceNode("Advanced", var6, SResources.getImageDescriptor("bulb-small")));
      }

      this.addToRoot(new ImagePreferenceNode("All", var7, SResources.getImageDescriptor("exclamation")));
   }

   private void addSortedOptions(Map var1, IPreferenceNode var2) {
      String[] var3 = new String[var1.keySet().size()];
      var1.keySet().toArray(var3);
      Arrays.sort(var3, String.CASE_INSENSITIVE_ORDER);

      for (int var4 = 0; var4 < var3.length; var4++) {
         MLDonkeyPreferencePage var5 = (MLDonkeyPreferencePage)var1.get(var3[var4]);
         Hashtable var6 = new Hashtable();
         var6.put("bittorrent", "e.network.bittorrent.connected-16");
         var6.put("donkey", "e.network.donkey.connected-16");
         var6.put("fasttrack", "e.network.fasttrack.connected-16");
         var6.put("filetp", "e.network.filetp.connected-16");
         var6.put("gnutella", "e.network.gnutella.connected-16");
         var6.put("g2", "e.network.gnutella2.connected-16");
         var6.put("soulseek", "e.network.soulseek.connected-16");
         var6.put("opennap", "e.network.opennap.connected-16");
         var6.put("networks", "globe");
         var6.put("mail", "new-message");
         var6.put("debug", "info");
         var6.put("html mods", "tab.webbrowser.buttonSmall");
         var6.put("network config", "menu-connect");
         var6.put("bandwidth", "tab.transfers.buttonSmall");
         var6.put("paths", "file-explorer");
         var6.put("security", "lock");
         var6.put("download", "arrow-down-green");
         var6.put("interfaces", "display");
         var6.put("startup", "startup");
         String var7 = (String)var6.get(var3[var4].toLowerCase());
         if (var7 == null) {
            var7 = "preferences";
         }

         var2.add(new ImagePreferenceNode(var3[var4], var5, SResources.getImageDescriptor(var7)));
      }
   }

   private void addToMap(Map var1, String var2, MLDonkeyPreferenceStore var3, Option var4) {
      if (!var1.containsKey(var2)) {
         MLDonkeyPreferencePage var5 = new MLDonkeyPreferencePage(var2, 1);
         var1.put(var2, var5);
         var5.setPreferenceStore(var3);
      }

      ((MLDonkeyPreferencePage)var1.get(var2)).addOption(var4);
   }

   private MLDonkeyPreferencePage addAdvancedOption(MLDonkeyPreferencePage var1, Option var2, MLDonkeyPreferenceStore var3) {
      if (var1 == null) {
         var1 = new MLDonkeyPreferencePage(SResources.getString("l.advanced") + "*", 1);
         var1.setPreferenceStore(var3);
      }

      var1.addOption(var2);
      return var1;
   }

   public void initialize(PreferenceStore var1) throws IOException {
      try {
         var1.load();
      } catch (IOException var3) {
         var1.save();
         var1.load();
      }
   }
}
