package sancho.view.utility.dialogs;

import java.util.Arrays;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import sancho.core.Sancho;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

class BandwidthDialog$ConfigOptionsDialog extends Dialog {
   String[] foundList;
   List rightList;

   public BandwidthDialog$ConfigOptionsDialog(Shell var1, String[] var2) {
      super(var1);
      this.foundList = var2;
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(VersionInfo.getProgramIcon());
      var1.setText(SResources.getString("l.bandwidthConfigTitle"));
   }

   protected void createButtonsForButtonBar(Composite var1) {
      this.createButton(var1, 0, SResources.getString("b.ok"), true);
      this.createButton(var1, 1, SResources.getString("b.cancel"), false);
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      var2.setLayout(WidgetFactory.createGridLayout(4, 5, 5, 10, 5, false));
      if (!Sancho.hasCollectionFactory()) {
         return var2;
      } else {
         List var3 = new List(var2, 2818);
         String[] var4 = Sancho.getCore().getOptionCollection().getAllIntOptions();
         Arrays.sort(var4, new BandwidthDialog$StringComparator());
         var3.setItems(var4);
         GridData var5 = new GridData();
         var5.heightHint = 100;
         var3.setLayoutData(var5);
         Button var6 = new Button(var2, 8);
         var6.setImage(SResources.getImage("plus"));
         var6.setLayoutData(new GridData());
         this.rightList = new List(var2, 2818);
         var5 = new GridData();
         var5.heightHint = 100;
         this.rightList.setLayoutData(var5);
         this.rightList.setItems(this.foundList);
         Button var7 = new Button(var2, 8);
         var7.setImage(SResources.getImage("minus"));
         var7.setLayoutData(new GridData());
         var7.addSelectionListener(new BandwidthDialog$4(this));
         var6.addSelectionListener(new BandwidthDialog$5(this, var3));
         return var2;
      }
   }

   protected void buttonPressed(int var1) {
      if (var1 == 0 && this.rightList != null) {
         PreferenceLoader.setValue("bwPresets", this.rightList.getItems());
         PreferenceLoader.saveStore();
      }

      super.buttonPressed(var1);
   }
}
