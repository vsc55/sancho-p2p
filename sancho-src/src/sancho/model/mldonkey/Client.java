package sancho.model.mldonkey;

import gnu.trove.THash;
import gnu.trove.THashMap;
import gnu.trove.TIntObjectHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.swt.graphics.Image;
import sancho.core.ICore;
import sancho.model.mldonkey.enums.AbstractEnum;
import sancho.model.mldonkey.enums.EnumClientMode;
import sancho.model.mldonkey.enums.EnumClientType;
import sancho.model.mldonkey.enums.EnumHostState;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.model.mldonkey.utility.Addr;
import sancho.model.mldonkey.utility.HostState;
import sancho.model.mldonkey.utility.Kind;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.Tag;
import sancho.model.mldonkey.utility.UtilityFactory;
import sancho.utility.SwissArmy;
import sancho.view.utility.SResources;

public class Client extends AObjectO {
   public static final String RS_TRANSFERRING = SResources.getString("l.transferring");
   public static final String RS_RANK = SResources.getString("l.rank");
   public static final String RS_TRUE = SResources.getString("l.true");
   public static final String RS_FALSE = SResources.getString("l.false");
   protected static final String S_Q = " (Q: ";
   public static final int CONNECTED = 1;
   public static final int DISCONNECTED = 2;
   public static final int TRANSFERRING_ADD = 4;
   public static final int TRANSFERRING_REM = 8;
   public static final int CHANGED_AVAIL = 16;
   public static final int READ_CLIENT_FILE = 32;
   protected THash avail;
   protected int chatPort;
   protected THashMap clientFilesMap;
   protected EnumClientType enumClientType;
   protected int id;
   protected Kind kind;
   protected String name;
   protected EnumNetwork networkEnum;
   protected int rating;
   protected HostState state;
   protected Tag[] tag;
   protected EnumHostState stateEnum;
   protected EnumClientMode clientModeEnum;

   Client(ICore var1) {
      super(var1);
      this.state = UtilityFactory.getHostState(var1);
      this.kind = UtilityFactory.getKind(var1);
   }

   public void addAsFriend() {
      this.core.send((short)14, Integer.valueOf(this.getId()));
   }

   public void connect() {
   }

   public void disconnect() {
   }

   public boolean equals(Object var1) {
      return var1 instanceof Client && this.getId() == ((Client)var1).getId();
   }

   public Addr getAddr() {
      return this.kind.getAddr();
   }

   private TIntObjectHashMap getAvailMap() {
      if (this.avail == null) {
         this.avail = new TIntObjectHashMap();
      }

      return (TIntObjectHashMap)this.avail;
   }

   public String getClientActivity() {
      return this.getStateEnum() == EnumHostState.CONNECTED_DOWNLOADING ? RS_TRANSFERRING : RS_RANK + this.getStateRank();
   }

   public THashMap getClientFilesMap() {
      if (this.clientFilesMap == null) {
         this.clientFilesMap = new THashMap();
      }

      return this.clientFilesMap;
   }

   public synchronized Map getClientFilesResultMap(Object var1) {
      return this.clientFilesMap == null ? null : (Map)this.clientFilesMap.get(var1);
   }

   public synchronized EnumClientMode getClientModeEnum() {
      return this.clientModeEnum;
   }

   public int getConnectedTime() {
      return 0;
   }

   public String getConnectedTimeString() {
      return "";
   }

   public String getDetailedClientActivity() {
      EnumHostState var1 = this.getStateEnum();
      StringBuffer var2 = new StringBuffer();
      var2.append(var1.getName());
      if (var1 == EnumHostState.CONNECTED_DOWNLOADING && this.getStateFileNum() != -1) {
         var2.append("(");
         var2.append(this.getState().getFileNum());
         var2.append(")");
         return var2.toString().intern();
      } else if (var1 != EnumHostState.CONNECTED_DOWNLOADING && this.getStateRank() > 0) {
         var2.append(" (Q: ");
         var2.append(this.getState().getRank());
         var2.append(")");
         return var2.toString().intern();
      } else {
         return var2.toString().intern();
      }
   }

   public long getDownloaded() {
      return 0L;
   }

   public String getDownloadedString() {
      return "";
   }

   public synchronized EnumClientType getEnumClientType() {
      return this.enumClientType;
   }

   public synchronized boolean isFriend() {
      return this.enumClientType == EnumClientType.FRIEND;
   }

   public synchronized EnumNetwork getEnumNetwork() {
      return this.networkEnum;
   }

   public synchronized String getFileAvailability(int var1) {
      return (String)this.getAvailMap().get(var1);
   }

   public synchronized String getFileAvailabilityPercentString(File var1) {
      float var2 = this.getFileAvailabilityPercent(var1);
      return var2 < 0.0F ? "" : SwissArmy.percentToString(var2);
   }

   public synchronized float getFileAvailabilityPercent(File var1) {
      int var2 = var1.getId();
      String var3 = this.getFileAvailability(var2);
      if (var3 == null) {
         return -1.0F;
      } else {
         String var4 = var1.getChunks();
         if (var3.length() > 0 && var3.length() == var4.length()) {
            int var5 = 0;
            int var6 = 0;

            for (int var7 = 0; var7 < var4.length(); var7++) {
               int var8 = var4.charAt(var7) - '0';
               int var9 = var3.charAt(var7) - '0';
               if (var8 < 2) {
                  var6++;
                  if (var9 > 0) {
                     var5++;
                  }
               }
            }

            return var6 == 0 ? -1.0F : (float)var5 / (float)var6 * 100.0F;
         } else {
            return -1.0F;
         }
      }
   }

   public synchronized Object[] getFileDirectories() {
      return this.clientFilesMap.keySet().toArray();
   }

   public Map getFirstResultMap() {
      synchronized (this) {
         String var2 = (String)this.getFileDirectories()[0];
         return this.getClientFilesResultMap(var2);
      }
   }

   public String getHash() {
      return this.kind.getHash();
   }

   public synchronized int getId() {
      return this.id;
   }

   public String getModeString() {
      return this.getClientModeEnum().getName();
   }

   public synchronized String getName() {
      return this.name != null ? this.name : "";
   }

   public synchronized Image getNameImage() {
      return this.isFriend() ? SResources.getImage("client_friend") : SResources.getImage("client");
   }

   public synchronized byte getSUI() {
      return 2;
   }

   public synchronized String getSUIString() {
      return SResources.getString("l.none");
   }

   public synchronized Image getSUIImage() {
      byte var1 = this.getSUI();
      switch (var1) {
         case 0:
            return SResources.getImage("bulb-red");
         case 1:
            return SResources.getImage("bulb-green");
         default:
            return SResources.getImage("bulb-grey");
      }
   }

   public int getNumChunks(int var1) {
      int var2 = 0;
      String var3 = this.getFileAvailability(var1);
      if (var3 != null) {
         for (int var4 = 0; var4 < var3.length(); var4++) {
            if (var3.charAt(var4) == '1') {
               var2++;
            }
         }
      }

      return var2;
   }

   public synchronized int getPort() {
      return this.kind.getPort();
   }

   public synchronized int getRating() {
      return this.rating;
   }

   public String getSoftware() {
      return "";
   }

   public Image getSoftwareImage() {
      return null;
   }

   public synchronized HostState getState() {
      return this.state;
   }

   public synchronized EnumHostState getStateEnum() {
      return this.stateEnum;
   }

   public synchronized int getStateFileNum() {
      return this.state.getFileNum();
   }

   public synchronized int getStateRank() {
      return this.state.getRank();
   }

   public long getUploaded() {
      return 0L;
   }

   public String getUploadedString() {
      return "";
   }

   public String getUploadFilename() {
      return "";
   }

   public synchronized boolean hasFiles() {
      return this.clientFilesMap != null;
   }

   public synchronized String hasFilesString() {
      return this.hasFiles() ? RS_TRUE : RS_FALSE;
   }

   public synchronized Image hasFilesImage() {
      return SResources.getImage(this.hasFiles() ? "bulb-green" : "bulb-red");
   }

   public int hashCode() {
      return this.getId();
   }

   public boolean isConnected() {
      return this.isConnected(this.getStateEnum());
   }

   public boolean isConnected(AbstractEnum var1) {
      return var1 == EnumHostState.CONNECTED_DOWNLOADING
         || var1 == EnumHostState.CONNECTED_INITIATING
         || var1 == EnumHostState.CONNECTED_AND_QUEUED
         || var1 == EnumHostState.CONNECTED;
   }

   public boolean isTransferring() {
      return this.isTransferring(this.getStateEnum());
   }

   public boolean isTransferring(AbstractEnum var1) {
      return var1 == EnumHostState.CONNECTED_DOWNLOADING;
   }

   public boolean isTransferring(int var1) {
      return this.isTransferring() && this.getStateFileNum() == var1;
   }

   public boolean isUploader() {
      return false;
   }

   public boolean onChangedState(AbstractEnum var1) {
      boolean var2 = false;
      this.setChanged();
      byte var3 = 0;
      if (var1 != this.getStateEnum()) {
         if (this.isTransferring()) {
            if (this.isConnected(var1)) {
               var3 = 4;
            } else {
               var3 = 5;
            }
         } else if (this.isTransferring(var1)) {
            if (this.isConnected()) {
               var3 = 8;
            } else {
               var3 = 10;
            }
         } else if (this.isConnected(var1)) {
            if (!this.isConnected()) {
               var3 = 2;
            }
         } else if (this.isConnected()) {
            var3 = 1;
         }
      }

      if (var3 != 0) {
         var2 = true;
         this.notifyObservers(null, var3);
      }

      this.clearChanged();
      return var2;
   }

   public void putAvail(int var1, String var2) {
      synchronized (this) {
         this.getAvailMap().put(var1, var2);
      }

      this.setChanged();
      this.notifyObservers(null, 16);
   }

   public void read(int var1, MessageBuffer var2) {
      EnumHostState var3 = this.getStateEnum();
      EnumClientType var4 = this.getEnumClientType();
      boolean var5 = false;
      synchronized (this) {
         this.id = var1;
         this.networkEnum = this.core.getNetworkCollection().getNetworkEnum(var2.getInt32());
         this.clientModeEnum = this.kind.read(var2);
         this.stateEnum = this.state.read(var2);
         this.enumClientType = EnumClientType.byteToEnum(var2.getByte());
         this.tag = var2.getTagList();
         this.name = var2.getString();
         this.rating = var2.getInt32();
         var5 = this.readMore(var2);
      }

      this.onChangedType(var4);
      boolean var7 = this.onChangedState(var3);
      if (!var7 && var5) {
         this.setChanged();
         this.notifyObservers();
      }
   }

   protected void onChangedType(AbstractEnum var1) {
      if (var1 != null && var1 != this.getEnumClientType()) {
         this.core.getClientCollection().updateFriends(this);
      }
   }

   protected boolean readMore(MessageBuffer var1) {
      this.chatPort = var1.getInt32();
      return false;
   }

   public void read(MessageBuffer var1) {
      this.read(var1.getInt32(), var1);
   }

   public void readClientFile(MessageBuffer var1) {
      String var2 = var1.getString();
      int var3 = var1.getInt32();
      Result var4 = this.core.getResultCollection().getResult(var3);
      if (var4 != null) {
         synchronized (this) {
            THashMap var6 = this.getClientFilesMap();
            WeakHashMap var7;
            if (var6.containsKey(var2)) {
               var7 = (WeakHashMap)var6.get(var2);
            } else {
               var7 = new WeakHashMap();
               var6.put(var2, var7);
            }

            var7.put(var4, null);
         }

         this.setChanged();
         this.notifyObservers(null, 32);
      }
   }

   public void readUpdate(MessageBuffer var1) {
      EnumHostState var2 = this.getStateEnum();
      synchronized (this) {
         this.stateEnum = this.state.read(var1);
      }

      this.onChangedState(var2);
   }

   public void removeAsFriend() {
      this.core.send((short)16, Integer.valueOf(this.getId()));
   }

   public void requestClientFiles() {
      this.core.send((short)33, Integer.valueOf(this.getId()));
   }
}
