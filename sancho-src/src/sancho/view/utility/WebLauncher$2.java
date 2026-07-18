package sancho.view.utility;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

class WebLauncher$2 implements Runnable {
   // $VF: synthetic field
   private final Display val$display;

   WebLauncher$2(Display var1) {
      this.val$display = var1;
   }

   public void run() {
      MessageBox var1 = new MessageBox(new Shell(this.val$display), 1);
      var1.setText(SResources.getString("l.webBrowserErrorTitle"));
      var1.setMessage(SResources.getString("l.webBrowserError"));
      var1.open();
   }
}
