package sancho.view.utility;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import sancho.model.mldonkey.File;
import sancho.utility.VersionInfo;
import sancho.view.MainWindow;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;

public class DownloadCompleteDialog extends Dialog {
   MainWindow mainWindow;
   Text textInfo;

   public DownloadCompleteDialog(Shell var1, MainWindow var2) {
      super(var1);
      this.mainWindow = var2;
      this.setBlockOnOpen(false);
   }

   public int getShellStyle() {
      return super.getShellStyle() | 16 | 16384;
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setText(SResources.getString("l.downloadComplete"));
      var1.setImage(VersionInfo.getProgramIcon());
   }

   protected void constrainShellSize() {
      super.constrainShellSize();
      this.getShell().setBounds(PreferenceLoader.loadRectangle("downloadCompleteWindowBounds"));
   }

   public void addFile(File var1) {
      this.textInfo.append("[" + var1.getId() + "] " + var1.getName() + " - " + var1.getED2K() + this.textInfo.getLineDelimiter());
      this.getShell().setText(SResources.getString("l.downloadComplete") + " (" + (this.textInfo.getLineCount() - 1) + ")");
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      this.textInfo = new Text(var2, 2826);
      this.textInfo.setLayoutData(new GridData(1808));
      return var2;
   }

   protected void createButtonsForButtonBar(Composite var1) {
      this.createButton(var1, 0, SResources.getString("b.ok"), true);
   }

   public boolean close() {
      this.mainWindow.closeDownloadCompleteDialog();
      PreferenceConverter.setValue(PreferenceLoader.getPreferenceStore(), "downloadCompleteWindowBounds", this.getShell().getBounds());
      PreferenceLoader.saveStore();
      return super.close();
   }
}
