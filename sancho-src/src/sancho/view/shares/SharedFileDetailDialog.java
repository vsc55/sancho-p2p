package sancho.view.shares;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import sancho.model.mldonkey.SharedFile;
import sancho.utility.VersionInfo;
import sancho.view.transfer.fileDialog.SubfilesViewFrame;
import sancho.view.utility.SResources;

public class SharedFileDetailDialog extends Dialog {
   SharedFile sharedFile;

   protected SharedFileDetailDialog(Shell var1, SharedFile var2) {
      super(var1);
      this.sharedFile = var2;
   }

   protected void constrainShellSize() {
      super.constrainShellSize();
      Shell var1 = this.getShell();
      var1.setSize(400, 350);
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(VersionInfo.getProgramIcon());
      var1.setText(SResources.getString("l.file") + " " + this.sharedFile.getId() + " " + SResources.getString("l.details").toLowerCase());
   }

   protected void createButtonsForButtonBar(Composite var1) {
      this.createButton(var1, 0, IDialogConstants.OK_LABEL, true);
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      Composite var3 = new Composite(var2, 0);
      var3.setLayout(new FillLayout());
      var3.setLayoutData(new GridData(1808));
      new SubfilesViewFrame(var3, "l.subFiles", "up_arrow_blue", null, this.sharedFile);
      return var2;
   }
}
