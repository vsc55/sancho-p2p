package sancho.view;

import java.util.regex.Matcher;
import org.eclipse.jface.action.Action;
import sancho.view.utility.SResources;

public class WebBrowserTab$NSBookmark extends Action {
   String href;
   // $VF: synthetic field
   private final WebBrowserTab this$0;

   public WebBrowserTab$NSBookmark(WebBrowserTab var1, String var2) {
      this.this$0 = var1;
      Matcher var3 = var1.bookmark_title.matcher(var2);
      Matcher var4 = var1.bookmark_href.matcher(var2);
      String var5 = "Unknown";
      if (var3.find()) {
         int var6 = var3.start(1);
         int var7 = var3.end(1);
         if (!var3.find()) {
            var5 = var2.substring(var6, var7);
         }
      }

      if (var4.find()) {
         int var8 = var4.start(1);
         int var9 = var4.end(1);
         if (!var4.find()) {
            this.href = var2.substring(var8, var9);
         }
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
