package sancho.view;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import sancho.view.utility.NoDuplicatesCombo;
import sancho.view.utility.SResources;

class WebBrowserTab$8 extends CTabFolder2Adapter {
   // $VF: synthetic field
   private final WebBrowserTab this$0;

   WebBrowserTab$8(WebBrowserTab var1) {
      this.this$0 = var1;
   }

   public void close(CTabFolderEvent var1) {
      CTabItem var2 = (CTabItem)var1.item;
      Browser var3 = (Browser)var2.getData("browser");
      if (this.this$0.cTabFolder.getItemCount() == 1) {
         if (var3 != null && !var3.isDisposed()) {
            var3.setUrl("about:blank");
         }

         NoDuplicatesCombo var4 = this.this$0.getInputCombo(var2);
         if (var4 != null) {
            var4.setText("");
         }

         this.this$0.viewFrame.updateCLabelText(SResources.getString("tab.webbrowser"));
         var2.setText("blank");
         var1.doit = false;
      } else {
         if (var3 != null && !var3.isDisposed()) {
            var3.dispose();
         }
      }
   }
}
