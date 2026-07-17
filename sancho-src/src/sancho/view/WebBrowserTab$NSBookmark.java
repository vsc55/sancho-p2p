package sancho.view;

import sancho.utility.regex.REMatch;
import org.eclipse.jface.action.Action;
import sancho.view.utility.SResources;

public class WebBrowserTab$NSBookmark extends Action {
   String href;
   // $VF: synthetic field
   private final WebBrowserTab this$0;

   public WebBrowserTab$NSBookmark(WebBrowserTab var1, String var2) {
      this.this$0 = var1;
      REMatch[] var3 = var1.bookmark_title.getAllMatches(var2);
      REMatch[] var4 = var1.bookmark_href.getAllMatches(var2);
      String var5 = "Unknown";
      if (var3.length == 1) {
         int var6 = var3[0].getStartIndex(1);
         int var7 = var3[0].getEndIndex(1);
         var5 = var2.substring(var6, var7);
      }

      if (var4.length == 1) {
         int var8 = var4[0].getStartIndex(1);
         int var9 = var4[0].getEndIndex(1);
         this.href = var2.substring(var8, var9);
      }

      this.setText(var1.formatTitle(var5));
      this.setImageDescriptor(SResources.getImageDescriptor("web-link-m"));
   }

   public void run() {
      if (this.href != null && !this.href.equals("")) {
         this.this$0.navigate(this.this$0.getSelectedBrowser(), this.href);
      }
   }
}
