package sancho.model.mldonkey;

import gnu.trove.TIntArrayList;
import gnu.trove.TLongIntHashMap;
import java.net.InetAddress;
import sancho.core.ICore;
import sancho.core.Sancho;
import sancho.model.mldonkey.enums.EnumHostState;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.utility.SwissArmy;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;

public class ServerCollection extends ACollection_Int2 {
   private static final String RS_SERVERS = SResources.getString("tab.servers");
   private static final String RS_CONNECTED = SResources.getString("l.connected");
   private TLongIntHashMap cleanHistoryMap = new TLongIntHashMap();
   private boolean displayNodes;
   protected int numConnected;
   protected long donkeyNumServers;
   protected long donkeyNumUsers;
   protected long donkeyNumFiles;

   ServerCollection(ICore var1) {
      super(var1);
      this.updatePreferences();
   }

   public long getDonkeyNumServers() {
      return this.donkeyNumServers;
   }

   public String getDonkeyNumServersString() {
      return SwissArmy.calcStringSizeGrouped(this.donkeyNumServers);
   }

   public String getDonkeyNumFilesString() {
      return SwissArmy.calcStringSizeGrouped(this.donkeyNumFiles);
   }

   public String getDonkeyNumUsersString() {
      return SwissArmy.calcStringSizeGrouped(this.donkeyNumUsers);
   }

   public void addServer(Network var1, InetAddress var2, short var3) {
      if (var2 != null && var1 != null) {
         Object[] var4 = new Object[]{Integer.valueOf(var1.getId()), var2.getAddress(), Short.valueOf(var3)};
         this.core.send((short)54, var4);
      }
   }

   public void addServerList(String var1) {
      String var2 = var1.toLowerCase().endsWith(".met") ? "server.met" : "ocl";
      this.core.send((short)29, "urladd " + var2 + " " + var1);
   }

   public void clean(MessageBuffer var1) {
      int[] var2 = var1.getInt32List();
      synchronized (this) {
         this.cleanHistoryMap.put(System.currentTimeMillis(), var2.length);
      }

      this.retainEntries(new ACollection_Int$CleanIntMap(new TIntArrayList(var2)));
      this.setChanged();
      this.notifyObservers();
   }

   public void cleanOldServers() {
      this.core.send((short)2);
   }

   public void connectMore() {
      this.core.send((short)1);
   }

   public int getConnected() {
      return this.numConnected;
   }

   public int getConnected(EnumNetwork var1) {
      ServerCollection$CountNetworkConnected var2 = new ServerCollection$CountNetworkConnected(var1);
      this.forEachValue(var2);
      return var2.getCount();
   }

   public synchronized TLongIntHashMap getHistoryMap() {
      return this.cleanHistoryMap;
   }

   public Object[] getServers() {
      return this.getValues();
   }

   private void put(int var1, Server var2) {
      if (var2 != null) {
         super.put(var1, var2);
         this.addToAdded(var2);
         if (var2.isConnected()) {
            this.setConnected(1);
         }
      }
   }

   public void read(MessageBuffer var1) {
      int var2 = var1.getInt32();
      int var3 = var1.getInt32();
      Network var4 = (Network)this.core.getNetworkCollection().get(var3);
      if (var4 != null) {
         if (this.displayNodes || var4.hasServers()) {
            Server var5 = (Server)this.get(var2);
            if (var5 != null) {
               var5.read(var2, var3, var1);
               if (var5.getStateEnum() != EnumHostState.REMOVE_HOST) {
                  this.addToUpdated(var5);
               }
            } else {
               var5 = this.core.getCollectionFactory().getServer();
               var5.read(var2, var3, var1);
               if (var5.getStateEnum() != EnumHostState.REMOVE_HOST) {
                  this.put(var5.getId(), var5);
               }
            }

            this.setChanged();
            this.notifyObservers();
         }
      }
   }

   public void readUpdate(MessageBuffer var1) {
      Server var2 = (Server)this.get(var1.getInt32());
      if (var2 != null) {
         var2.readUpdate(var1);
         if (var2.getStateEnum() != EnumHostState.REMOVE_HOST) {
            this.addToUpdated(var2);
            this.setChanged();
            this.notifyObservers();
         }
      }
   }

   public synchronized String getHeaderString() {
      StringBuffer var1 = new StringBuffer(64);
      var1.append(RS_SERVERS);
      var1.append(": ");
      var1.append(this.getConnected());
      var1.append(" / ");
      var1.append(this.size());
      var1.append(" ");
      var1.append(RS_CONNECTED);
      this.updateDonkeyTotals();
      if (this.getDonkeyNumServers() > 0L) {
         var1.append(" ");
         var1.append(" ");
         var1.append(" ");
         var1.append(" ");
         var1.append("(");
         var1.append(EnumNetwork.DONKEY.getName());
         var1.append(": ");
         var1.append(this.getDonkeyNumServersString());
         var1.append("/");
         var1.append(this.getDonkeyNumUsersString());
         var1.append("/");
         var1.append(this.getDonkeyNumFilesString());
         var1.append(")");
      }

      return var1.toString();
   }

   public synchronized void updateDonkeyTotals() {
      ServerCollection$CountDonkeyTotals var1 = new ServerCollection$CountDonkeyTotals();
      this.forEachValue(var1);
      this.donkeyNumFiles = var1.getNumFiles();
      this.donkeyNumUsers = var1.getNumUsers();
      this.donkeyNumServers = var1.getNumServers();
   }

   public Object remove(int var1) {
      Server var2 = (Server)super.get(var1);
      if (var2 != null && var2.isConnected()) {
         this.setConnected(-1);
      }

      return super.remove(var1);
   }

   public void remove(Server var1) {
      super.remove(var1.getId());
      this.addToRemoved(var1);
      this.setChanged();
      this.notifyObservers();
   }

   public void removeAll(EnumNetwork var1) {
      this.retainEntries(new ServerCollection$RemoveNetworkServers(var1));
      this.setChanged();
      this.notifyObservers();
   }

   public void serverUser(MessageBuffer var1) {
      int var2 = var1.getInt32();
      Server var3 = (Server)this.get(var2);
      if (var3 != null) {
         var3.serverUser(var1);
         this.addToUpdated(var3);
      } else if (Sancho.debug) {
         Sancho.pDebug("su-nf:" + var2);
      }
   }

   public void setConnected(int var1) {
      this.numConnected += var1;
   }

   public void updatePreferences() {
      this.displayNodes = PreferenceLoader.loadBoolean("displayNodes");
   }
}
