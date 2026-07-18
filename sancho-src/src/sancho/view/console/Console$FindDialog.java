package sancho.view.console;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

class Console$FindDialog extends Dialog {
   Text text;
   Label infoLabel;
   String lastFind;
   int lastPos;
   // $VF: synthetic field
   private final Console this$0;

   public Console$FindDialog(Console var1) {
      super(var1.infoDisplay.getShell());
      this.this$0 = var1;
      this.lastFind = "";
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(SResources.getImage("refine"));
      var1.setText(SResources.getString("l.find"));
   }

   protected void createButtonsForButtonBar(Composite var1) {
      this.createButton(var1, 0, SResources.getString("l.find"), true);
      this.createButton(var1, 1, SResources.getString("b.cancel"), false);
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      var2.setLayout(WidgetFactory.createGridLayout(2, 5, 5, 10, 5, false));
      Label var3 = new Label(var2, 0);
      var3.setText(SResources.getString("l.find") + ": ");
      var3.setLayoutData(new GridData(32));
      this.text = new Text(var2, 2052);
      this.text.setLayoutData(new GridData(768));
      this.text.addKeyListener(new Console$10(this));
      this.infoLabel = new Label(var2, 0);
      GridData var4 = new GridData(768);
      var4.horizontalSpan = 2;
      this.infoLabel.setLayoutData(var4);
      return var2;
   }

   protected void find() {
      if (this.text.getText().length() > 0) {
         String var1 = this.text.getText().toLowerCase();
         if (!this.lastFind.equals(var1)) {
            this.lastPos = 0;
         }

         this.lastFind = var1;
         String var2 = this.this$0.infoDisplay.getText().toLowerCase();
         int var3 = var2.indexOf(var1, this.lastPos);
         if (var3 != -1) {
            this.lastPos = var3 + 1;
            this.this$0.infoDisplay.setSelection(var3, var3 + var1.length());
            this.infoLabel.setText("");
         } else {
            this.infoLabel.setText(SResources.getString("l.stringNotFound"));
            this.lastPos = 0;
         }

         this.text.setFocus();
      }
   }

   protected void buttonPressed(int var1) {
      if (var1 == 0) {
         this.find();
      } else {
         super.buttonPressed(var1);
      }
   }
}
