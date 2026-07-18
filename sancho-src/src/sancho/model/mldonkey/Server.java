package sancho.model.mldonkey;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.swt.graphics.Image;
import sancho.core.ICore;
import sancho.model.mldonkey.enums.EnumHostState;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.model.mldonkey.utility.Addr;
import sancho.model.mldonkey.utility.HostState;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.Tag;
import sancho.model.mldonkey.utility.UtilityFactory;
import sancho.utility.SwissArmy;
import sancho.view.utility.SResources;

public class Server extends AObject {
   private static final String RS_UNKNOWN = SResources.getString("l.unknown");
   private static final String RS_HIGH_ID = SResources.getString("l.highID");
   private static final String RS_LOW_ID = SResources.getString("l.lowID");
   private static final String RS_TRUE = SResources.getString("l.true");
   private static final String RS_FALSE = SResources.getString("l.false");
   private Addr addr;
   private String description;
   private boolean preferred;
   private int id;
   private String name;
   private long numFiles;
   private long numUsers;
   private int port;
   private int score;
   private HostState state;
   private EnumHostState stateEnum;
   private Tag[] tagList;
   private Map userMap;
   private EnumNetwork networkEnum;

   Server(ICore var1) {
      super(var1);
      this.state = UtilityFactory.getHostState(var1);
      this.addr = UtilityFactory.getAddr(var1);
   }

   public synchronized void addUserInfo(User var1) {
      this.getUserMap().put(var1, null);
   }

   public void blacklist() {
      this.core.send((short)29, "bs " + this.getAddr().toString());
   }

   public void checkConnected(EnumHostState var1) {
      EnumHostState var2 = this.getStateEnum();
      if (var1 != null && var1 != var2) {
         if (var2 == EnumHostState.CONNECTED) {
            this.core.getServerCollection().setConnected(1);
         } else if (var1 == EnumHostState.CONNECTED) {
            this.core.getServerCollection().setConnected(-1);
         }
      }
   }

   private void checkRemovedState() {
      if (this.getStateEnum() == EnumHostState.REMOVE_HOST) {
         this.core.getServerCollection().remove(this);
      }
   }

   public void connect() {
      this.setState(EnumHostState.CONNECTING);
   }

   public void disconnect() {
      this.setState(EnumHostState.NOT_CONNECTED);
   }

   public boolean equals(Object var1) {
      return var1 instanceof Server && this.getId() == ((Server)var1).getId();
   }

   public Addr getAddr() {
      return this.addr;
   }

   public synchronized String getDescription() {
      return this.description != null && this.name != null && !this.name.equals("") ? this.description : "";
   }

   public String getHighLowIDString() {
      if (this.getEnumNetwork() == EnumNetwork.DONKEY && this.isConnected()) {
         return this.getState().getRank() == -2 ? RS_HIGH_ID : RS_LOW_ID;
      } else {
         return "";
      }
   }

   public Image getHighLowImage() {
      if (this.getEnumNetwork() == EnumNetwork.DONKEY && this.isConnected()) {
         boolean var1 = this.getState().getRank() == -2;
         return SResources.getImage(var1 ? "bulb-green" : "bulb-red");
      } else {
         return null;
      }
   }

   public synchronized int getId() {
      return this.id;
   }

   public String getLink() {
      return this.getEnumNetwork() == EnumNetwork.DONKEY
         ? "ed2k://|server|" + this.getAddr().toString() + "|" + this.getPort()
         : this.getAddr().toString() + ":" + this.getPort();
   }

   public synchronized String getName() {
      return this.name != null && !this.name.equals("") ? this.name : RS_UNKNOWN;
   }

   public synchronized long getNumFiles() {
      return this.numFiles;
   }

   public synchronized String getNumFilesString() {
      return SwissArmy.calcStringSizeGrouped(this.numFiles);
   }

   public synchronized long getNumUsers() {
      return this.numUsers;
   }

   public synchronized String getNumUsersString() {
      return SwissArmy.calcStringSizeGrouped(this.numUsers);
   }

   public synchronized int getPort() {
      return this.port;
   }

   public synchronized int getScore() {
      return this.score;
   }

   public void getServerUsers() {
      this.core.send((short)32, Integer.valueOf(this.getId()));
   }

   public HostState getState() {
      return this.state;
   }

   public synchronized EnumHostState getStateEnum() {
      return this.stateEnum;
   }

   public synchronized String getStateString() {
      return this.getAddr().isBlocked() ? SResources.getString("e.state.blocked") : this.getStateEnum().getName();
   }

   public synchronized Tag[] getTagList() {
      if (this.tagList != null && this.tagList.length != 0) {
         Tag[] var1 = new Tag[this.tagList.length];
         System.arraycopy(this.tagList, 0, var1, 0, this.tagList.length);
         return var1;
      } else {
         return new Tag[0];
      }
   }

   protected synchronized Map getUserMap() {
      if (this.userMap == null) {
         this.userMap = Collections.synchronizedMap(new WeakHashMap());
      }

      return this.userMap;
   }

   public synchronized Object[] getUsers() {
      return SwissArmy.toArray(this.getUserMap().keySet());
   }

   public int hashCode() {
      return this.getId();
   }

   public synchronized boolean hasUsers() {
      return this.userMap != null;
   }

   public boolean isConnected() {
      return this.getStateEnum() == EnumHostState.CONNECTED;
   }

   public boolean isDisconnected() {
      return this.getStateEnum() == EnumHostState.NOT_CONNECTED;
   }

   public synchronized boolean isPreferred() {
      return this.preferred;
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

   public String getPreferredString() {
      if (this.getEnumNetwork() == EnumNetwork.DONKEY) {
         return this.isPreferred() ? RS_TRUE : RS_FALSE;
      } else {
         return "";
      }
   }

   public Image getPreferredImage() {
      return this.getEnumNetwork() == EnumNetwork.DONKEY ? SResources.getImage(this.isPreferred() ? "bulb-green" : "bulb-red") : null;
   }

   public void togglePreferred() {
      String var1 = "preferred " + (this.isPreferred() ? "false" : "true") + " " + this.getAddr().toString();
      this.core.send((short)29, var1);
   }

   public void rename(String var1) {
   }

   protected void read40(MessageBuffer var1) {
   }

   public synchronized String getVersion() {
      return "";
   }

   public synchronized long getMaxUsers() {
      return 0L;
   }

   public String getMaxUsersString() {
      return SwissArmy.calcStringSizeGrouped(this.getMaxUsers());
   }

   public synchronized long getSoftLimit() {
      return 0L;
   }

   public String getSoftLimitString() {
      return SwissArmy.calcStringSizeGrouped(this.getSoftLimit());
   }

   public synchronized long getHardLimit() {
      return 0L;
   }

   public String getHardLimitString() {
      return SwissArmy.calcStringSizeGrouped(this.getHardLimit());
   }

   public synchronized long getLowIDUsers() {
      return 0L;
   }

   public String getLowIDUsersString() {
      return SwissArmy.calcStringSizeGrouped(this.getLowIDUsers());
   }

   public synchronized int getPing() {
      return 0;
   }

   public String getPingString() {
      return SwissArmy.calcStringSizeGrouped((long)this.getPing());
   }

   public void read(int var1, int var2, MessageBuffer var3) {
      EnumHostState var4 = this.getStateEnum();
      synchronized (this) {
         this.id = var1;
         this.networkEnum = this.readNetworkEnum(var2);
         this.addr.read(var3);
         this.port = this.readPort(var3);
         this.score = var3.getInt32();
         this.tagList = var3.getTagList();
         this.numUsers = this.readNUsers(var3);
         this.numFiles = this.readNFiles(var3);
         this.stateEnum = this.state.read(var3);
         this.name = var3.getString();
         this.description = var3.getString();
         this.preferred = this.readPreferred(var3);
         this.read40(var3);
      }

      this.checkConnected(var4);
      this.checkRemovedState();
   }

   public void read(MessageBuffer var1) {
      this.read(var1.getInt32(), var1.getInt32(), var1);
   }

   protected EnumNetwork readNetworkEnum(int var1) {
      return this.core.getNetworkCollection().getNetworkEnum(var1);
   }

   protected boolean readPreferred(MessageBuffer var1) {
      return false;
   }

   protected long readNUsers(MessageBuffer var1) {
      return (long)var1.getInt32();
   }

   protected long readNFiles(MessageBuffer var1) {
      return (long)var1.getInt32();
   }

   protected int readPort(MessageBuffer var1) {
      return (int)((long)var1.getUInt16() & 65535L);
   }

   public void readUpdate(MessageBuffer var1) {
      EnumHostState var2 = this.getStateEnum();
      synchronized (this) {
         this.stateEnum = this.state.read(var1);
      }

      this.checkConnected(var2);
      this.checkRemovedState();
   }

   public void remove() {
      this.setState(EnumHostState.REMOVE_HOST);
   }

   public void serverUser(MessageBuffer var1) {
      User var2 = (User)this.core.getUserCollection().get(var1.getInt32());
      if (var2 != null) {
         this.addUserInfo(var2);
      }
   }

   public void setState(EnumHostState var1) {
      byte var2 = 0;
      if (var1 == EnumHostState.NOT_CONNECTED) {
         var2 = 22;
      } else if (var1 == EnumHostState.REMOVE_HOST) {
         var2 = 9;
      } else if (var1 == EnumHostState.CONNECTING || var1 == EnumHostState.CONNECTED) {
         var2 = 21;
      }

      if (var2 != 0) {
         this.core.send(var2, Integer.valueOf(this.getId()));
      }
   }
}
