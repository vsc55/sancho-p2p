package sancho.model.mldonkey.utility;

import sancho.core.ICore;

public class ClientMessage {
   private int id;
   private String messageText;

   public synchronized int getId() {
      return this.id;
   }

   public synchronized String getText() {
      return this.messageText;
   }

   public synchronized void read(MessageBuffer var1) {
      this.id = var1.getInt32();
      this.messageText = var1.getString();
   }

   public static void sendMessage(ICore var0, int var1, String var2) {
      Object[] var3 = new Object[]{Integer.valueOf(var1), var2};
      var0.send((short)43, var3);
   }
}
