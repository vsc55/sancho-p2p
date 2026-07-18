package sancho.model.mldonkey;

import org.eclipse.swt.graphics.Image;
import sancho.core.ICore;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.model.mldonkey.enums.EnumRoomState;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.utility.ObjectMap;

public class Room extends AObject {
   protected EnumRoomState roomState;
   protected String name;
   protected int id;
   protected EnumNetwork networkEnum;
   protected ObjectMap userMap = null;

   Room(ICore var1) {
      super(var1);
   }

   public synchronized String getName() {
      return this.name != null ? this.name : "";
   }

   public synchronized EnumRoomState getRoomState() {
      return this.roomState;
   }

   public int getNumUsers() {
      return -1;
   }

   public synchronized int getId() {
      return this.id;
   }

   public synchronized EnumNetwork getEnumNetwork() {
      return this.networkEnum;
   }

   public synchronized String getNetworkName() {
      return this.networkEnum.getName();
   }

   public synchronized Image getNetworkImage() {
      return this.networkEnum.getImage();
   }

   public void read(MessageBuffer var1) {
      this.read(var1.getInt32(), var1);
   }

   public void read(int var1, MessageBuffer var2) {
      synchronized (this) {
         this.id = var1;
         this.networkEnum = this.readNetworkEnum(var2);
         this.name = var2.getString();
         this.roomState = EnumRoomState.intToEnum(var2.getInt8());
      }
   }

   protected EnumNetwork readNetworkEnum(MessageBuffer var1) {
      return this.core.getNetworkCollection().getNetworkEnum(var1.getInt32());
   }

   public void addUser(User var1) {
      this.getUserMap().add(var1);
   }

   public void removeUser(User var1) {
      this.getUserMap().remove(var1);
   }

   public ObjectMap getUserMap() {
      if (this.userMap == null) {
         this.userMap = new ObjectMap(true);
      }

      return this.userMap;
   }

   public void close() {
      this.setRoomState(EnumRoomState.CLOSED);
   }

   public void open() {
      this.setRoomState(EnumRoomState.OPEN);
   }

   public void setRoomState(EnumRoomState var1) {
      Object[] var2 = new Object[]{Integer.valueOf(this.getId()), Byte.valueOf(var1.getByteValue())};
      this.core.send((short)48, var2);
   }
}
