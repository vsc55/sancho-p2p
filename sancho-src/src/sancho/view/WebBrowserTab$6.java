package sancho.view;

import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import sancho.core.Sancho;
import sancho.view.utility.SResources;

class WebBrowserTab$6 implements LocationListener {
   // $VF: synthetic field
   private final WebBrowserTab this$0;

   WebBrowserTab$6(WebBrowserTab var1) {
      this.this$0 = var1;
   }

   public void changed(LocationEvent var1) {
      this.this$0.onChanged(var1);
   }

   public void changing(LocationEvent var1) {
      if (var1.location != null) {
         String var2 = var1.location;
         if (this.this$0.regex.matcher(var2).find()) {
            this.this$0.getMainWindow().getStatusline().setText(SResources.getString("l.sending") + var2);
            var1.doit = false;
            Sancho.send((short)8, var2);
         } else if (var1.top) {
            this.this$0.onChangingTop(var1);
         }
      }
   }
}
