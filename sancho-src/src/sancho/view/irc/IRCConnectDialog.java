package sancho.view.irc;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class IRCConnectDialog extends Dialog {
   Text server;
   Text channel;
   Text nick;

   public IRCConnectDialog(Shell var1) {
      super(var1);
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(VersionInfo.getProgramIcon());
      var1.setText(SResources.getString("l.connectToIRC"));
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      var2.setLayout(WidgetFactory.createGridLayout(2, 5, 5, 10, 5, false));
      this.server = this.createText(var2, "p.r.irc.ircServer", "ircServer");
      this.nick = this.createText(var2, "p.r.irc.ircNickname", "ircNickname");
      this.channel = this.createText(var2, "p.r.irc.ircChannel", "ircChannel");
      return var2;
   }

   protected Text createText(Composite var1, String var2, String var3) {
      Label var4 = new Label(var1, 0);
      var4.setLayoutData(new GridData(128));
      var4.setText(SResources.getString(var2));
      Text var5 = new Text(var1, 2048);
      var5.setText(PreferenceLoader.loadString(var3));
      var5.setLayoutData(new GridData(768));
      return var5;
   }

   protected void createButtonsForButtonBar(Composite var1) {
      // OK is the default button so pressing Enter in the fields accepts (was
      // Cancel, which made Enter cancel the connect).
      this.createButton(var1, 0, SResources.getString("b.ok"), true);
      this.createButton(var1, 1, SResources.getString("b.cancel"), false);
   }

   protected void buttonPressed(int var1) {
      // Only persist the edited nick/channel on OK; Cancel must discard them.
      if (var1 == IDialogConstants.OK_ID) {
         String var2 = this.channel.getText();
         String var3 = this.nick.getText();
         String var4 = this.server.getText().trim();
         if (var4.length() > 0) {
            PreferenceLoader.getPreferenceStore().setValue("ircServer", var4);
         }

         if (var2.startsWith("#") && var2.length() > 1) {
            PreferenceLoader.getPreferenceStore().setValue("ircChannel", var2);
         }

         if (var3.length() > 1) {
            PreferenceLoader.getPreferenceStore().setValue("ircNickname", var3);
         }

         PreferenceLoader.saveStore();
      }

      super.buttonPressed(var1);
   }
}
