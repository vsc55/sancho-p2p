package sancho.view.utility;

import java.net.HttpURLConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import sancho.model.mldonkey.IPreview;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;

public class TransferDialog extends Dialog {
   boolean cancel;
   boolean cancelled;
   boolean transferring;
   String downloadDirectory;
   CLabel label1;
   CLabel label2;
   CLabel label3;
   IPreview iPreview;
   int[] subFileNums;

   public TransferDialog(Shell var1, IPreview var2, int[] var3, String var4) {
      super(new Shell());
      this.iPreview = var2;
      this.subFileNums = var3;
      this.downloadDirectory = var4;
      if (var3.length == 1 && var3[0] == -1) {
         String[] var5 = var2.getSubFileNames();
         if (var5 != null) {
            this.subFileNums = new int[var5.length];
            int var6 = 0;

            while (var6 < var5.length) {
               this.subFileNums[var6] = var6++;
            }
         }
      }

      this.setShellStyle(48 | (SWT.getPlatform().equals("fox") ? 0 : 0));
      this.setBlockOnOpen(false);
      this.startTransfer();
   }

   public boolean close() {
      // Signal cancel and close immediately. The transfer thread checks `cancel` each
      // read chunk and only touches widgets through updateLabel()/closeInThread(),
      // both isDisposed-guarded, so there's no need to busy-wait for it here — the old
      // loop slept up to 20 x 200ms = 4s on the UI thread, freezing the GUI on cancel.
      this.cancel = true;
      PreferenceStore var2 = PreferenceLoader.getPreferenceStore();
      PreferenceConverter.setValue(var2, "transferDialogWindowBounds", this.getShell().getBounds());
      return super.close();
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(VersionInfo.getProgramIcon());
      var1.setText(this.iPreview.getName());
   }

   protected void constrainShellSize() {
      super.constrainShellSize();
      Shell var1 = this.getShell();
      if (PreferenceLoader.contains("transferDialogWindowBounds")) {
         var1.setBounds(PreferenceLoader.loadRectangle("transferDialogWindowBounds"));
      } else {
         var1.setSize(300, 175);
         Point var2 = var1.getLocation();
         this.getShell().setLocation(var2.x - 200, var2.y);
      }
   }

   protected void createButtonsForButtonBar(Composite var1) {
      this.createButton(var1, 1, SResources.getString("b.cancel"), false);
   }

   protected void buttonPressed(int var1) {
      if (var1 == 1) {
         this.cancel = true;
      }

      super.buttonPressed(var1);
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      var2.setLayout(WidgetFactory.createGridLayout(2, 5, 5, 3, 2, false));
      new CLabel(var2, 0).setText(SResources.getString("td.file"));
      this.label1 = new CLabel(var2, 0);
      this.label1.setLayoutData(new GridData(768));
      new CLabel(var2, 0).setText(SResources.getString("td.destination"));
      this.label2 = new CLabel(var2, 0);
      new CLabel(var2, 0).setText(SResources.getString("td.status"));
      this.label2.setLayoutData(new GridData(768));
      this.label3 = new CLabel(var2, 0);
      this.label3.setLayoutData(new GridData(768));
      this.label1.setText(this.iPreview.getName());
      this.label2.setText(this.downloadDirectory);
      return var2;
   }

   public static long getLongLength(HttpURLConnection var0) {
      for (int var1 = 0; var1 < 20; var1++) {
         String var2 = var0.getHeaderFieldKey(var1);
         String var3 = var0.getHeaderField(var1);
         if (var3 == null) {
            break;
         }

         if (var2 != null && var2.toLowerCase().equals("content-length")) {
            return Long.valueOf(var3);
         }
      }

      return (long)var0.getContentLength();
   }

   public void updateLabel(CLabel var1, String var2) {
      Shell var3 = this.getShell();
      if (var3 != null && !var3.isDisposed()) {
         var3.getDisplay().asyncExec(new TransferDialog$1(this, var1, var2));
      }
   }

   public void closeInThread() {
      Shell var1 = this.getShell();
      if (var1 != null && !var1.isDisposed()) {
         var1.getDisplay().asyncExec(new TransferDialog$2(this));
      }
   }

   public void startTransfer() {
      TransferDialog$3 var1 = new TransferDialog$3(this);
      var1.start();
   }
}
