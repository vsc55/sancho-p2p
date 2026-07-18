package sancho.view.console;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.swt.widgets.Composite;
import sancho.core.Sancho;

public class MessageConsole extends Console {
   private static final SimpleDateFormat sdFormatter = new SimpleDateFormat("[HH:mm:ss] ");
   protected int clientId;

   public MessageConsole(Composite var1, int var2, int var3) {
      super(var1, var2);
      this.clientId = var3;
   }

   public void prefixAppend() {
      this.infoDisplay.append(this.getTimeStamp() + "> ");
   }

   public String getTimeStamp() {
      return sdFormatter.format(new Date());
   }

   public void sendMessage() {
      if (Sancho.hasCollectionFactory()) {
         Object[] var1 = new Object[]{Integer.valueOf(this.clientId), this.input.getText()};
         Sancho.send((short)43, var1);
      }
   }
}
