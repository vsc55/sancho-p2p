package sancho.model.mldonkey;

import sancho.core.ICore;
import sancho.model.mldonkey.utility.Addr;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.Tag;
import sancho.model.mldonkey.utility.UtilityFactory;
import sancho.utility.SwissArmy;

public class User extends AObject {
   private static String S_OSB = "[";
   private static String S_CSB = "] ";
   private Addr addr;
   private int id;
   private byte[] md4;
   private String name;
   private int port;
   private int serverId;
   private Tag[] tags;

   User(ICore var1) {
      super(var1);
      this.addr = UtilityFactory.getAddr(var1);
   }

   public void addAsFriend() {
      this.core.send((short)15, Integer.valueOf(this.getId()));
   }

   public Addr getAddr() {
      return this.addr;
   }

   public synchronized int getId() {
      return this.id;
   }

   public synchronized String getMd4() {
      return SwissArmy.calcStringOfMD4(this.md4);
   }

   public synchronized String getName() {
      return this.name != null ? this.name : "";
   }

   public synchronized int getPort() {
      return this.port;
   }

   public synchronized int getServerId() {
      return this.serverId;
   }

   public synchronized String getTagsString() {
      StringBuffer var1 = new StringBuffer();

      for (int var2 = 0; var2 < this.tags.length; var2++) {
         var1.append(S_OSB);
         var1.append(this.tags[var2]);
         var1.append(S_CSB);
      }

      return var1.toString();
   }

   public synchronized void read(int var1, MessageBuffer var2) {
      synchronized (this) {
         this.id = var1;
         this.md4 = var2.getMd4();
         this.name = var2.getString();
         this.addr.read(false, var2);
         this.port = var2.getUInt16();
         this.tags = var2.getTagList();
         this.serverId = var2.getInt32();
      }
   }

   public void read(MessageBuffer var1) {
      this.read(var1.getInt32(), var1);
   }
}
