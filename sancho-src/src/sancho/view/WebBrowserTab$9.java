package sancho.view;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import sancho.view.utility.NoDuplicatesCombo;

class WebBrowserTab$9 extends SelectionAdapter {
   // $VF: synthetic field
   private final WebBrowserTab this$0;

   WebBrowserTab$9(WebBrowserTab var1) {
      this.this$0 = var1;
   }

   public void widgetSelected(SelectionEvent var1) {
      CTabItem var2 = (CTabItem)var1.item;
      Browser var3 = (Browser)var2.getData("browser");
      NoDuplicatesCombo var4 = this.this$0.getInputCombo(var2);
      if (var4 != null) {
         if (var3 != null && !var3.isDisposed()) {
            var4.setText(var3.getUrl());
         }

         var4.setFocus();
      }

      this.this$0.viewFrame.updateCLabelText(var2.getText());
   }
}
