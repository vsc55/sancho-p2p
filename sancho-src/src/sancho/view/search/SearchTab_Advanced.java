package sancho.view.search;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import sancho.core.Sancho;
import sancho.model.mldonkey.utility.SearchQuery;
import sancho.view.SearchTab;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.search.result.ResultTab;
import sancho.view.search.result.ResultViewFrame;
import sancho.view.utility.SResources;

public class SearchTab_Advanced extends ASearchTab2 {
   Combo fileType;

   public SearchTab_Advanced(ResultViewFrame viewFrame, SearchTab searchTab) {
      super(viewFrame, searchTab);
   }

   public Control createTab(CTabFolder tabFolder) {
      Composite composite = this.createMainComposite(tabFolder);
      this.searchCombo = this.createSavedSearchCombo(composite, "s.searchFor", "advancedSearchFor");
      this.createNetworkCombo(composite);
      this.fileType = this.createFileType(composite);
      this.searchTypeCombo = this.createSearchTypeCombo(composite);
      String[] values = this.buildFormatValues();
      this.formatCombo = this.createCombo(composite, 0, "s.format", values);
      this.createSeparator(composite);
      values = new String[]{"", "3", "5", "10", "25", "50"};
      this.minAvailCombo = this.createCombo(composite, 0, "s.minAvail", values);
      this.minCombo = this.createMinMaxCombo(composite, 0, "s.minSize");
      this.maxCombo = this.createMinMaxCombo(composite, 0, "s.maxSize");
      values = new String[]{"", "50", "100", "200", "400"};
      this.resultCombo = this.createIntegerCombo(composite, 0, "s.maxResults", values);
      composite.setData(this);
      return composite;
   }

   public String getText() {
      return SResources.getString("s.tab.advanced");
   }

   // Build the format dropdown contents from the (editable) "searchFormats" preference,
   // with a leading "" so the combo always has a "no format" entry.
   private String[] buildFormatValues() {
      String[] formats = PreferenceLoader.loadStringArray("searchFormats");
      String[] values = new String[formats.length + 1];
      values[0] = "";
      System.arraycopy(formats, 0, values, 1, formats.length);
      return values;
   }

   // Re-read the format list when preferences are applied, so edits show up without a
   // restart. Keep whatever the user currently has typed/selected in the combo.
   public void updateDisplay() {
      if (this.formatCombo != null && !this.formatCombo.isDisposed()) {
         String current = this.formatCombo.getText();
         this.formatCombo.setItems(this.buildFormatValues());
         this.formatCombo.setText(current);
      }
   }

   public void performSearch() {
      String searchText = this.searchCombo.getText();
      this.searchCombo.add(searchText, 0);
      if (!searchText.equals("") && Sancho.hasCollectionFactory()) {
         SearchQuery query = this.viewFrame.getCore().getCollectionFactory().getSearchQuery();
         query.setSearchString(searchText);
         this.parseFileType(this.fileType, query);
         this.parseSearchType(this.searchTypeCombo, query);
         this.addMinSizeToQuery(query, this.minCombo);
         this.addMaxSizeToQuery(query, this.maxCombo);
         this.addMaxResultsToQuery(query, this.resultCombo);
         if (!this.formatCombo.getText().equals("")) {
            query.setFormat(this.formatCombo.getText());
         }

         this.addNetwork(query);
         query.send();
         int searchId = query.getSearchId();
         this.viewFrame.getCore().getResultCollection().setMinAvail(searchId, this.parseMinAvail());
         new ResultTab(this.viewFrame, this.searchTab.getCTabFolder(), this.searchTab, searchId, searchText);
         this.searchCombo.setText("");
      }
   }
}
