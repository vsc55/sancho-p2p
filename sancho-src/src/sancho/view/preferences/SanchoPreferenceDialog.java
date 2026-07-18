package sancho.view.preferences;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import sancho.utility.VersionInfo;
import sancho.view.utility.SResources;

// PreferenceDialog whose window title and OK/Cancel buttons come from Sancho's own
// translations (SResources) instead of the English JFace defaults. The per-page
// Apply / Restore-Defaults buttons are relabelled in CPreferencePage.
public class SanchoPreferenceDialog extends PreferenceDialog {
   public SanchoPreferenceDialog(Shell parentShell, PreferenceManager manager) {
      super(parentShell, manager);
   }

   protected void configureShell(Shell shell) {
      super.configureShell(shell);
      shell.setText(VersionInfo.getName() + ": " + SResources.getString("menu.tools.preferences"));
   }

   protected void createButtonsForButtonBar(Composite parent) {
      super.createButtonsForButtonBar(parent);
      Button okButton = this.getButton(IDialogConstants.OK_ID);
      if (okButton != null) {
         okButton.setText(SResources.getString("b.ok"));
      }

      Button cancelButton = this.getButton(IDialogConstants.CANCEL_ID);
      if (cancelButton != null) {
         cancelButton.setText(SResources.getString("b.cancel"));
      }
   }
}
