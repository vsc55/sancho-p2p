package sancho.view.mainWindow;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import sancho.utility.VersionInfo;
import sancho.view.utility.CSpinner;
import sancho.view.utility.WidgetFactory;
import sancho.view.utility.SResources;

class MenuBar$AlphaInputDialog extends Dialog {
   CSpinner spinner;
   Button okButton;
   Shell mainShell;

   public MenuBar$AlphaInputDialog(Shell var1) {
      super(var1);
      this.mainShell = var1;
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(VersionInfo.getProgramIcon());
      var1.setText(String.valueOf(this.mainShell.getAlpha() & 0xFF));
   }

   protected void createButtonsForButtonBar(Composite var1) {
      this.okButton = this.createButton(var1, 0, SResources.getString("b.ok"), true);
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
      var3.setMaximum(255);
      var3.setIncrement(1);
      var3.setPageIncrement(5);
      var3.setSelection(this.mainShell.getAlpha() & 0xFF);
      var3.addSelectionListener(new MenuBar$25(this, var3));
      return var2;
   }

   protected void buttonPressed(int var1) {
      super.buttonPressed(var1);
   }
}
