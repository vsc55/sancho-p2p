package sancho.view;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import sancho.view.search.ASearchTab;
import sancho.view.search.SearchTab_Advanced;
import sancho.view.search.SearchTab_Audio;
import sancho.view.search.SearchTab_Simple;
import sancho.view.search.SearchViewFrame;
import sancho.view.search.result.ResultTableContentProvider;
import sancho.view.search.result.ResultViewFrame;
import sancho.view.utility.AbstractTab;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;
import sancho.view.viewer.GView;

public class SearchTab extends AbstractTab {
   private Composite composite;
   private CTabFolder resultsCTabFolder;
   private ResultViewFrame resultViewFrame;
   private Button searchButton;
   private CTabFolder searchCTabFolder;
   private ASearchTab[] searchTabs;

   public SearchTab(MainWindow mainWindow, String name) {
      super(mainWindow, name);
   }

   protected Button createButton(Composite parent, String name, SelectionAdapter selectionAdapter) {
      Button button = new Button(this.composite, 8);
      button.setText(SResources.getString(name));
      button.addSelectionListener(selectionAdapter);
      return button;
   }

   protected void createContents(Composite parent) {
      String sashName = "searchSash";
      SashForm sashForm = WidgetFactory.createSashForm(parent, sashName);
      this.createViewFrames(sashForm);
      WidgetFactory.loadSashForm(sashForm, sashName);
   }

   public void onConnect() {
      super.onConnect();

      for (int i = 0; i < this.searchTabs.length; i++) {
         this.searchTabs[i].onConnect();
      }
   }

   public void updateDisplay() {
      super.updateDisplay();
      if (this.searchTabs != null) {
         for (int i = 0; i < this.searchTabs.length; i++) {
            this.searchTabs[i].updateDisplay();
         }
      }
   }

   public void dispose() {
      if (this.searchTabs != null) {
         for (int i = 0; i < this.searchTabs.length; i++) {
            this.searchTabs[i].dispose();
         }
      }

      super.dispose();
   }

   public void autoSearch(String text) {
      this.searchTabs[0].autoSearch(text);
   }

   private void createLeftSash(Composite parent) {
      parent.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      this.searchCTabFolder = WidgetFactory.createCTabFolder(parent);
      this.searchCTabFolder.setLayoutData(new GridData(768));
      this.searchTabs = new ASearchTab[]{
         new SearchTab_Simple(this.resultViewFrame, this), new SearchTab_Advanced(this.resultViewFrame, this), new SearchTab_Audio(this.resultViewFrame, this)
      };

      for (int i = 0; i < this.searchTabs.length; i++) {
         CTabItem tabItem = new CTabItem(this.searchCTabFolder, 0);
         tabItem.setText(this.searchTabs[i].getText());
         Control control = this.searchTabs[i].createTab(this.searchCTabFolder);
         if (i == 0) {
            tabItem.setControl(control);
         }

         tabItem.setData("myControl", control);
         tabItem.setData(this.searchTabs[i]);
      }

      this.searchCTabFolder.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            CTabItem tabItem = (CTabItem)event.item;
            tabItem.setControl((Control)tabItem.getData("myControl"));

            for (int i = 0; i < SearchTab.this.searchCTabFolder.getItems().length; i++) {
               if (SearchTab.this.searchCTabFolder.getItems()[i] != tabItem) {
                  SearchTab.this.searchCTabFolder.getItems()[i].setControl(null);
               }
            }

            SearchTab.this.searchCTabFolder.getParent().layout();
         }
      });
      this.searchCTabFolder.setSelection(0);
      Composite composite = new Composite(parent, 0);
      composite.setLayout(WidgetFactory.createGridLayout(1, 2, 0, 0, 0, false));
      composite.setLayoutData(new GridData(768));
      Label label = new Label(composite, 258);
      label.setLayoutData(new GridData(768));
      Composite composite2 = new Composite(composite, 0);
      composite2.setLayout(WidgetFactory.createGridLayout(1, 7, 5, 0, 0, false));
      composite2.setLayoutData(new GridData(768));
      this.createSearchButton(composite2);
   }

   private void createRightSash(CTabFolder tabFolder) {
      this.resultsCTabFolder = tabFolder;
      this.resultsCTabFolder.setData(this);
      this.resultsCTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
         public void close(CTabFolderEvent event) {
            CTabItem tabItem = (CTabItem)event.item;
            Control control = tabItem.getControl();
            if (control != null && !control.isDisposed()) {
               control.dispose();
            }
         }
      });
      this.resultsCTabFolder.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            CTabItem tabItem = (CTabItem)event.item;
            GView view = (GView)tabItem.getData("gView");
            if (view == null) {
               SearchTab.this.resultViewFrame.updateCLabelText(SResources.getString("t.search.results"));
            } else {
               ResultTableContentProvider contentProvider = (ResultTableContentProvider)view.getContentProvider();
               contentProvider.updateHeaderLabel();
            }
         }
      });
   }

   private void createSearchButton(Composite parent) {
      this.composite = new Composite(parent, 0);
      GridData gridData = new GridData(768);
      gridData.heightHint = 40;
      gridData.horizontalSpan = 2;
      this.composite.setLayoutData(gridData);
      this.composite.setLayout(new FillLayout());
      this.searchButton = this.createButton(this.composite, "s.search", new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            SearchTab.this.getSearch().performSearch();
         }
      });
   }

   protected void createViewFrames(SashForm sashForm) {
      SearchViewFrame searchViewFrame = new SearchViewFrame(sashForm, "tab.search", "tab.search.buttonSmall", this);
      this.addViewFrame(searchViewFrame);
      this.resultViewFrame = new ResultViewFrame(sashForm, "t.search.results", "tab.search.buttonSmall", this);
      this.addViewFrame(this.resultViewFrame);
      this.createLeftSash(searchViewFrame.getChildComposite());
      this.createRightSash(this.resultViewFrame.getCTabFolder());
      searchViewFrame.setSearchTabs(this.searchTabs);
   }

   public CTabFolder getCTabFolder() {
      return this.resultsCTabFolder;
   }

   private ASearchTab getSearch() {
      CTabItem tabItem = this.searchCTabFolder.getSelection();
      return (ASearchTab)tabItem.getData();
   }

   public boolean isButtonEnabled() {
      return this.searchButton != null && !this.searchButton.isDisposed() ? this.searchButton.isEnabled() : false;
   }

   public void setActive() {
      super.setActive();
      this.getSearch().setFocus();
   }

   public void setButtonEnabled(boolean enabled) {
      if (this.searchButton != null && !this.searchButton.isDisposed()) {
         this.searchButton.setEnabled(enabled);
      }
   }
}
