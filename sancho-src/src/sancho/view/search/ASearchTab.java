package sancho.view.search;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import sancho.core.Sancho;
import sancho.model.mldonkey.Network;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.model.mldonkey.utility.SearchQuery;
import sancho.utility.MyObservable;
import sancho.utility.MyObserver;
import sancho.view.SearchTab;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.search.result.ResultViewFrame;
import sancho.view.utility.NoDuplicatesCombo;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public abstract class ASearchTab implements MyObserver {
   protected static String S_NO_NETWORK = SResources.getString("s.m.noNetwork");
   protected static String S_ALL_NETWORKS = SResources.getString("s.m.all");
   protected Combo networkCombo;
   private List networkList = new ArrayList();
   protected Combo searchCombo;
   protected SearchTab searchTab;
   protected List tempList = new ArrayList();
   protected List searchComboList = new ArrayList();
   protected ResultViewFrame viewFrame;
   protected Combo searchTypeCombo;

   public ASearchTab(ResultViewFrame resultViewFrame, SearchTab searchTab) {
      this.viewFrame = resultViewFrame;
      this.searchTab = searchTab;
      this.onConnect();
   }

   public void autoSearch(String searchText) {
      this.searchCombo.setText(searchText);
      this.performSearch();
   }

   public synchronized void addNetwork(SearchQuery query) {
      int index = this.networkCombo.getSelectionIndex();
      if (index >= 0 && index < this.networkList.size()) {
         EnumNetwork enumNetwork = (EnumNetwork)this.networkList.get(index);
         if (Sancho.hasCollectionFactory()) {
            Network network = Sancho.getCore().getNetworkCollection().getByEnum(enumNetwork);
            if (network != null) {
               query.setNetwork(network.getId());
            }
         }
      }
   }

   protected Combo createCombo(Composite composite, int style, String labelKey, String[] items) {
      Label label = new Label(composite, 0);
      label.setLayoutData(new GridData(256));
      label.setText(SResources.getString(labelKey));
      Combo combo = new Combo(composite, style | 4 | 2048);
      combo.setLayoutData(new GridData(768));
      combo.setItems(items);
      combo.select(0);
      return combo;
   }

   protected Combo createFileType(Composite composite) {
      String[] items = new String[]{
         "s.m.all", "s.m.audio", "s.m.video", "s.m.image", "s.m.document", "s.m.program", "s.m.archive", "s.m.cdimage", "s.m.emulecollection"
      };
      return this.createResCombo(composite, 8, "s.fileType", items);
   }

   protected Combo createIntegerCombo(Composite composite, int style, String labelKey, String[] items) {
      Combo combo = this.createCombo(composite, style, labelKey, items);
      combo.addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent event) {
            switch (event.keyCode) {
               case 8:
               case 127:
               case 16777219:
               case 16777220:
                  return;
               default:
                  try {
                     Integer.parseInt(String.valueOf(event.character));
                  } catch (NumberFormatException notANumber) {
                     event.doit = false;
                  }
            }
         }
      });
      return combo;
   }

   protected Composite createMainComposite(CTabFolder folder) {
      Composite composite = new Composite(folder, 0);
      composite.setLayout(WidgetFactory.createGridLayout(2, 7, 5, 5, 5, false));
      return composite;
   }

   protected void createNetworkCombo(Composite composite) {
      Label label = new Label(composite, 0);
      label.setLayoutData(new GridData(256));
      label.setText(SResources.getString("s.network"));
      this.networkCombo = new Combo(composite, 2060);
      this.networkCombo.setLayoutData(new GridData(768));
      this.syncNetworkCombo(true);
   }

   protected Combo createResCombo(Composite composite, int style, String labelKey, String[] items) {
      String[] localizedItems = new String[items.length];

      for (int i = 0; i < items.length; i++) {
         localizedItems[i] = SResources.getString(items[i]);
      }

      return this.createCombo(composite, style, labelKey, localizedItems);
   }

   protected Combo createSearchCombo(Composite composite, String labelKey) {
      Label label = new Label(composite, 0);
      label.setLayoutData(new GridData(256));
      label.setText(SResources.getString(labelKey));
      NoDuplicatesCombo combo = new NoDuplicatesCombo(composite, 2052);
      combo.setLayoutData(new GridData(768));
      combo.addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent event) {
            if (event.character == '\r' || event.character == 16777296) {
               ASearchTab.this.performSearch();
               NoDuplicatesCombo combo = (NoDuplicatesCombo)event.widget;
               combo.add(combo.getText(), 0);
               combo.setText("");
            }
         }
      });
      this.searchComboList.add(combo);
      return combo;
   }

   protected void clearAllSearchCombos() {
      for (int i = 0; i < this.searchComboList.size(); i++) {
         Combo combo = (Combo)this.searchComboList.get(i);
         combo.removeAll();
      }
   }

   protected Combo createSavedSearchCombo(Composite composite, String labelKey, final String prefKey) {
      Combo combo = this.createSearchCombo(composite, labelKey);
      if (PreferenceLoader.loadBoolean("searchSaveEntries")) {
         combo.setItems(PreferenceLoader.loadStringArray(prefKey + ".sArray"));
      }

      combo.addDisposeListener(new DisposeListener() {
         public void widgetDisposed(DisposeEvent event) {
            Combo combo = (Combo)event.widget;
            if (PreferenceLoader.loadBoolean("searchSaveEntries")) {
               PreferenceLoader.setValue(prefKey + ".sArray", combo.getItems(), 25);
            } else {
               PreferenceLoader.getPreferenceStore().setValue(prefKey + ".sArray", "");
            }
         }
      });
      return combo;
   }

   protected Combo createSearchTypeCombo(Composite composite) {
      String[] items = new String[]{"s.st.remote", "s.st.local", "s.st.subscribe"};
      return this.createResCombo(composite, 8, "s.searchType", items);
   }

   public void createSeparator(Composite composite) {
      Label label = new Label(composite, 258);
      GridData gridData = new GridData(768);
      gridData.horizontalSpan = 2;
      label.setLayoutData(gridData);
   }

   public abstract Control createTab(CTabFolder folder);

   public abstract String getText();

   // Called when preferences are applied; subclasses that show preference-driven
   // widgets (e.g. the advanced tab's format list) refresh them here. Default: no-op.
   public void updateDisplay() {
   }

   public void onConnect() {
      if (Sancho.hasCollectionFactory()) {
         this.viewFrame.getCore().getNetworkCollection().addObserver(this);
         if (this.networkCombo != null) {
            this.syncNetworkCombo(true);
         }
      }
   }

   public void parseFileType(Combo combo, SearchQuery query) {
      String[] fileTypes = new String[]{
         "s.m.all", "s.m.audio", "s.m.video", "s.m.image", "s.m.document", "s.m.program", "s.m.archive", "s.m.cdimage", "s.m.emulecollection"
      };
      switch (combo.getSelectionIndex()) {
         case 1:
            query.setMedia("Audio");
            break;
         case 2:
            query.setMedia("Video");
            break;
         case 3:
            query.setMedia("Image");
            break;
         case 4:
            query.setMedia("Doc");
            break;
         case 5:
            query.setMedia("Pro");
            break;
         case 6:
            query.setMedia("Arc");
            break;
         case 7:
            query.setMedia("Iso");
            break;
         case 8:
            query.setMedia("EmuleCollection");
      }
   }

   public void parseSearchType(Combo combo, SearchQuery query) {
      switch (combo.getSelectionIndex()) {
         case 1:
            query.setLocalSearch();
            break;
         case 2:
            query.setSubscribeSearch();
      }
   }

   public abstract void performSearch();

   public boolean setFocus() {
      return this.searchCombo.setFocus();
   }

   public synchronized void syncNetworkCombo(boolean force) {
      boolean changed = false;
      if (!force) {
         this.tempList.clear();
         if (!Sancho.hasCollectionFactory()) {
            return;
         }

         Network[] networks = this.viewFrame.getCore().getNetworkCollection().getNetworks();

         for (int i = 0; i < networks.length; i++) {
            Network network = networks[i];
            if (network.isEnabled() && network.isSearchable()) {
               this.tempList.add(network.getEnumNetwork());
            }
         }

         if (this.tempList.size() != this.networkList.size()) {
            changed = true;
         } else {
            for (int j = 0; j < this.tempList.size(); j++) {
               if (!this.networkList.contains(this.tempList.get(j))) {
                  changed = true;
                  break;
               }
            }

            if (!this.networkCombo.isEnabled() && this.tempList.size() == 1) {
               changed = true;
            }
         }
      }

      if (changed || force) {
         this.networkCombo.removeAll();
         this.networkList.clear();
         if (this.tempList.size() == 0) {
            if (!Sancho.hasCollectionFactory()) {
               return;
            }

            Network[] networks = this.viewFrame.getCore().getNetworkCollection().getNetworks();

            for (int i = 0; i < networks.length; i++) {
               Network network = networks[i];
               if (network.isEnabled() && network.isSearchable()) {
                  this.tempList.add(network.getEnumNetwork());
               }
            }
         }

         for (int i = 0; i < this.tempList.size(); i++) {
            EnumNetwork enumNetwork = (EnumNetwork)this.tempList.get(i);
            this.networkList.add(enumNetwork);
            Network network = Sancho.getCore().getNetworkCollection().getByEnum(enumNetwork);
            if (network != null) {
               this.networkCombo.add(network.getName());
            }
         }

         if (this.networkCombo.getItemCount() > 1) {
            this.networkCombo.add(S_ALL_NETWORKS);
         }

         this.networkCombo.select(this.networkCombo.getItemCount() - 1);
         if (!this.networkCombo.isEnabled()) {
            this.networkCombo.setEnabled(true);
         }

         if (!this.searchCombo.isEnabled()) {
            this.searchCombo.setText("");
            this.searchCombo.setEnabled(true);
         }

         if (!this.searchTab.isButtonEnabled()) {
            this.searchTab.setButtonEnabled(true);
         }
      }
   }

   public void update(MyObservable observable, Object data, int updateType) {
      if (this.networkCombo != null && !this.networkCombo.isDisposed()) {
         this.networkCombo.getDisplay().asyncExec(new Runnable() {
            public void run() {
               if (ASearchTab.this.networkCombo != null && !ASearchTab.this.networkCombo.isDisposed()) {
                  if (Sancho.hasCollectionFactory() && ASearchTab.this.viewFrame.getCore().getNetworkCollection().getEnabledAndSearchable() != 0) {
                     ASearchTab.this.syncNetworkCombo(false);
                  } else {
                     if (ASearchTab.this.searchCombo.isEnabled()) {
                        ASearchTab.this.searchCombo.setText(ASearchTab.S_NO_NETWORK);
                        ASearchTab.this.searchCombo.setEnabled(false);
                     }

                     if (ASearchTab.this.networkCombo.isEnabled()) {
                        ASearchTab.this.networkCombo.removeAll();
                        ASearchTab.this.networkCombo.setText(ASearchTab.S_NO_NETWORK);
                        ASearchTab.this.networkCombo.setEnabled(false);
                     }

                     if (ASearchTab.this.searchTab.isButtonEnabled()) {
                        ASearchTab.this.searchTab.setButtonEnabled(false);
                     }
                  }
               }
            }
         });
      }
   }

   public void dispose() {
      if (Sancho.hasCollectionFactory()) {
         this.viewFrame.getCore().getNetworkCollection().deleteObserver(this);
      }

      if (this.networkCombo != null && !this.networkCombo.isDisposed()) {
         this.networkCombo.dispose();
      }

      for (int i = 0; i < this.searchComboList.size(); i++) {
         Combo combo = (Combo)this.searchComboList.get(i);
         if (combo != null && !combo.isDisposed()) {
            combo.dispose();
         }
      }
   }
}
