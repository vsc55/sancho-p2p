package sancho.view.transfer.downloads;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import sancho.utility.VersionInfo;
import sancho.view.utility.CSpinner;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

class DownloadTreeMenuListener$PriorityInputDialog extends Dialog {
   int initialValue;
   int intValue;
   int incValue;
   String title;
   CSpinner spinner;
   CSpinner spinner2;
   Button okButton;

   public DownloadTreeMenuListener$PriorityInputDialog(Shell var1, String var2, int var3) {
      super(var1);
      this.initialValue = var3;
      this.title = var2;
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(VersionInfo.getProgramIcon());
      var1.setText(this.title);
   }

   protected void createButtonsForButtonBar(Composite var1) {
      this.okButton = this.createButton(var1, 0, SResources.getString("b.ok"), true);
      this.createButton(var1, 1, SResources.getString("b.cancel"), false);
      this.spinner.setFocus();
   }

   protected Button getOkButton() {
      return this.okButton;
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      var2.setLayout(WidgetFactory.createGridLayout(2, 5, 5, 10, 5, false));
      Scale var3 = new Scale(var2, 256);
      GridData var4 = new GridData(768);
      var4.widthHint = 300;
      var3.setLayoutData(var4);
      var3.setMinimum(0);
      var3.setMaximum(200);
      var3.setIncrement(1);
      var3.setPageIncrement(5);
      if (this.initialValue < -100) {
         var3.setSelection(0);
      } else if (this.initialValue > 100) {
         var3.setSelection(200);
      } else {
         var3.setSelection(this.initialValue + 100);
      }

      var3.addSelectionListener(new DownloadTreeMenuListener$3(this, var3));
      this.spinner = new CSpinner(var2, 2048);
      this.spinner.setMinimum(-200);
      this.spinner.setMaximum(200);
      this.spinner.setSelection(this.initialValue);
      this.spinner.addListener(31, new DownloadTreeMenuListener$4(this));
      Composite var5 = new Composite(var2, 0);
      var5.setLayout(WidgetFactory.createGridLayout(1, 25, 5, 10, 5, false));
      var4 = new GridData(768);
      var4.horizontalSpan = 2;
      var5.setLayoutData(var4);
      var5.setLayoutData(var4);
      Group var6 = new Group(var5, 0);
      var6.setLayout(WidgetFactory.createGridLayout(2, 5, 5, 10, 5, false));
      var4 = new GridData(768);
      var6.setLayoutData(var4);
      var6.setText(SResources.getString("m.d.priorityOptInc"));
      Scale var7 = new Scale(var6, 256);
      var7.setLayoutData(new GridData(768));
      var7.setMinimum(0);
      var7.setMaximum(40);
      var7.setIncrement(1);
      var7.setPageIncrement(5);
      var7.setSelection(20);
      var7.addSelectionListener(new DownloadTreeMenuListener$5(this, var7));
      this.spinner2 = new CSpinner(var6, 2048);
      this.spinner2.setMinimum(-20);
      this.spinner2.setMaximum(20);
      this.spinner2.setSelection(0);
      this.spinner2.addListener(31, new DownloadTreeMenuListener$6(this));
      return var2;
   }

   protected void onClose() {
      this.intValue = this.spinner.getSelection();
      this.incValue = this.spinner2.getSelection();
   }

   protected void buttonPressed(int var1) {
      this.onClose();
      super.buttonPressed(var1);
   }

   public int getIntValue() {
      return this.intValue;
   }

   public int getIncIntValue() {
      return this.incValue;
   }
}
