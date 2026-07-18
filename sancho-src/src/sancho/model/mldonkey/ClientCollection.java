package sancho.model.mldonkey;

import gnu.trove.TIntArrayList;
import gnu.trove.TLongIntHashMap;
import sancho.core.ICore;
import sancho.core.Sancho;
import sancho.model.mldonkey.enums.EnumClientType;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.utility.ObjectMap;

public class ClientCollection extends ACollection_Int {
   private TLongIntHashMap cleanHistoryMap = new TLongIntHashMap();
   private ObjectMap friendsWeakMap = new ObjectMap(true);
   private ObjectMap pendingWeakMap = new ObjectMap(true);
   private ObjectMap uploadersWeakMap = new ObjectMap(true);

   ClientCollection(ICore var1) {
      super(var1);
   }

   public void clean(MessageBuffer var1) {
      int[] var2 = var1.getInt32List();
      synchronized (this) {
         this.cleanHistoryMap.put(System.currentTimeMillis(), var2.length);
      }

      this.retainEntries(new ACollection_Int$CleanIntMap(new TIntArrayList(var2)));
      this.cleanDeadClients();
   }

   public void cleanDeadClients() {
      this.retainEntries(new ClientCollection$CleanDeadClients(this));
   }

   public static void removeAllFriends(ICore var0) {
      var0.send((short)17);
   }

   public void clientFile(MessageBuffer var1) {
      int var2 = var1.getInt32();
      Client var3 = this.getClient(var2);
      if (var3 != null) {
         boolean var4 = var3.hasFiles();
         var3.readClientFile(var1);
         if (!var4) {
            this.updateWeak(var3);
         }
      } else {
         Sancho.pDebug("Client " + var2 + " not found");
      }
   }

   public void dispose() {
      this.friendsWeakMap.deleteObservers();
      this.uploadersWeakMap.deleteObservers();
      this.forEachValue(new ClientCollection$DisposeAll());
      super.dispose();
   }

   public Client getClient(int var1) {
      return (Client)super.get(var1);
   }

   public ObjectMap getFriendsWeakMap() {
      return this.friendsWeakMap;
   }

   public synchronized TLongIntHashMap getHistoryMap() {
      return this.cleanHistoryMap;
   }

   public ObjectMap getPendingWeakMap() {
      return this.pendingWeakMap;
   }

   public ObjectMap getUploadersWeakMap() {
      return this.uploadersWeakMap;
   }

   public void pending(MessageBuffer var1) {
      this.processMap(this.pendingWeakMap, var1);
   }

   public void processMap(ObjectMap var1, MessageBuffer var2) {
      int[] var3 = var2.getInt32List();

      for (int var4 = 0; var4 < var3.length; var4++) {
         this.core.send((short)36, Integer.valueOf(var3[var4]));
         if (this.containsKey(var3[var4])) {
            var1.addOrUpdate(this.get(var3[var4]));
         }
      }

      TIntArrayList var5 = new TIntArrayList(var3);
      Object[] var6 = var1.getKeyArray();

      for (int var7 = 0; var7 < var6.length; var7++) {
         Client var8 = (Client)var6[var7];
         if (!var5.contains(var8.getId())) {
            var1.remove(var8);
            if (var8.countObservers() == 0) {
               this.removeSource(var8.getId(), var8);
            }
         }
      }
   }

   public void read(MessageBuffer var1) {
      int var2 = var1.getInt32();
      Client var3 = (Client)this.get(var2);
      if (var3 != null) {
         var3.read(var2, var1);
      } else {
         var3 = this.core.getCollectionFactory().getClient();
         var3.read(var2, var1);
         this.put(var2, var3);
      }

      this.updateWeak(var3);
   }

   public void readUpdate(MessageBuffer var1) {
      int var2 = var1.getInt32();
      if (this.containsKey(var2)) {
         this.getClient(var2).readUpdate(var1);
      }
   }

   public void removeSource(int var1, Client var2) {
      if (!this.friendsWeakMap.containsKey(var2) && !this.uploadersWeakMap.containsKey(var2) && !this.pendingWeakMap.containsKey(var2)) {
         this.remove(var1);
      }
   }

   public void updateAvailability(MessageBuffer var1) {
      int var2 = var1.getInt32();
      int var3 = var1.getInt32();
      String var4 = var1.getString(false);
      File var5 = (File)this.core.getFileCollection().get(var2);
      Client var6 = (Client)this.get(var3);
      if (var6 != null && var5 != null) {
         var6.putAvail(var2, var4);
      }
   }

   public void updateUploaders(ICore var1) {
      synchronized (this.uploadersWeakMap) {
         Object[] var3 = this.uploadersWeakMap.getKeyArray();

         for (int var4 = 0; var4 < var3.length; var4++) {
            Client var5 = (Client)var3[var4];
            var1.send((short)36, Integer.valueOf(var5.getId()));
         }
      }
   }

   public void updateFriends(Client var1) {
      if (var1.getEnumClientType() == EnumClientType.FRIEND) {
         this.friendsWeakMap.addOrUpdate(var1);
      } else {
         this.friendsWeakMap.remove(var1);
      }
   }

   public void updateWeak(Client var1) {
      this.updateFriends(var1);
      if (this.core.getProtocol() < 23) {
         if (var1.isUploader() && var1.isConnected()) {
            this.uploadersWeakMap.addOrUpdate(var1);
         } else {
            this.uploadersWeakMap.remove(var1);
         }
      }
   }

   public void uploaders(MessageBuffer var1) {
      this.processMap(this.uploadersWeakMap, var1);
   }

   // $VF: synthetic method
   static ObjectMap access$000(ClientCollection var0) {
      return var0.friendsWeakMap;
   }

   // $VF: synthetic method
   static ObjectMap access$100(ClientCollection var0) {
      return var0.uploadersWeakMap;
   }

   // $VF: synthetic method
   static ObjectMap access$200(ClientCollection var0) {
      return var0.pendingWeakMap;
   }
}
