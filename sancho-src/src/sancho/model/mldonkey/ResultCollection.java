package sancho.model.mldonkey;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import sancho.core.ICore;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.SearchWaiting;
import sancho.model.mldonkey.utility.UtilityFactory;
import sancho.utility.ObjectMap;
import sancho.view.preferences.PreferenceLoader;

public class ResultCollection extends ACollection_Int {
   private TIntIntHashMap minAvailMap;
   public boolean filterProfanity;
   public boolean filterPornography;
   public boolean verboseNumbers = true;
   private TIntObjectHashMap resultIntMap = new TIntObjectHashMap();
   private TIntArrayList closedList;
   private TIntObjectHashMap searchResultChart;
   public int cnt;

   public synchronized int getNumResults() {
      return this.resultIntMap.size();
   }

   ResultCollection(ICore var1) {
      super(var1);
      this.minAvailMap = new TIntIntHashMap();
      this.closedList = new TIntArrayList();
      this.searchResultChart = new TIntObjectHashMap();
   }

   public synchronized void closeSearch(int var1) {
      if (var1 < 0) {
         this.remove(var1);
      } else {
         this.closedList.add(var1);
         this.searchResultChart.remove(var1);
         this.remove(var1);
         Object[] var2 = new Object[]{Integer.valueOf(var1), Byte.valueOf((byte)1)};
         this.core.send((short)53, var2);
      }
   }

   public void download(Result var1, boolean var2) {
      int var3 = var1.getId();
      if (var3 < 0) {
         this.core.send((short)8, var1.getED2K());
      } else {
         Object[] var4 = new Object[]{var1.getNames(), Integer.valueOf(var3), Byte.valueOf((byte)(var2 ? 1 : 0))};
         this.core.send((short)50, var4);
      }
   }

   public synchronized boolean alreadyClosed(int var1) {
      return this.closedList.contains(var1);
   }

   public synchronized Result getResult(int var1) {
      return (Result)this.resultIntMap.get(var1);
   }

   protected boolean hasMinAvail(int var1, int var2) {
      synchronized (this.minAvailMap) {
         return var2 >= this.minAvailMap.get(var1);
      }
   }

   public void read(MessageBuffer var1) {
      int var2 = var1.getInt32();
      int var3 = var1.getInt32();
      Result var4 = null;
      synchronized (this) {
         if (this.resultIntMap.contains(var3)) {
            var4 = (Result)this.resultIntMap.get(var3);
            this.resultIntMap.remove(var3);
            TIntArrayList var10 = (TIntArrayList)this.searchResultChart.get(var2);
            if (var10 == null) {
               var10 = new TIntArrayList();
               this.searchResultChart.put(var2, var10);
            }

            if (!var10.contains(var3)) {
               var10.add(var3);
            }
         } else {
            TIntArrayList var6 = (TIntArrayList)this.searchResultChart.get(var2);
            if (var6 == null || !var6.contains(var3)) {
               return;
            }

            ObjectMap var7 = (ObjectMap)this.get(var2);
            if (var7 != null) {
               var4 = var7.getResult(var3);
            }
         }
      }

      if (var4 != null) {
         if (this.hasMinAvail(var2, var4.getAvail())) {
            boolean var11 = false;
            if (this.containsKey(var2)) {
               ((ObjectMap)this.get(var2)).addOrUpdate(var4);
               var11 = true;
            } else if (!this.alreadyClosed(var2)) {
               ObjectMap var12 = new ObjectMap(false);
               var12.add(var4);
               this.put(var2, var12);
               var11 = true;
            }

            if (var11) {
               this.setChanged();
               this.notifyObservers(this);
            }
         }
      }
   }

   public void resultInfo(MessageBuffer var1) {
      int var2 = var1.getInt32();
      Result var3 = (Result)this.resultMapGet(var2);
      if (var3 != null) {
         var3.read(var2, var1);
      } else {
         var3 = this.findResult(var2);
         if (var3 != null) {
            var3.read(var2, var1);
         } else {
            var3 = this.core.getCollectionFactory().getResult();
            var3.read(var2, var1);
            this.resultMapPut(var2, var3);
         }
      }
   }

   public synchronized Result findResult(int var1) {
      int[] var2 = this.searchResultChart.keys();

      for (int var3 = 0; var3 < var2.length; var3++) {
         int var4 = var2[var3];
         TIntArrayList var5 = (TIntArrayList)this.searchResultChart.get(var4);
         if (var5 != null && var5.contains(var1)) {
            ObjectMap var6 = (ObjectMap)this.get(var4);
            if (var6 != null) {
               Result var7 = var6.getResult(var1);
               if (var7 != null) {
                  return var7;
               }
            }
         }
      }

      return null;
   }

   public synchronized Object resultMapGet(int var1) {
      return this.resultIntMap.get(var1);
   }

   public synchronized boolean resultMapContains(int var1) {
      return this.resultIntMap.contains(var1);
   }

   public synchronized void resultMapPut(int var1, Object var2) {
      this.resultIntMap.put(var1, var2);
   }

   public void updatePreferences() {
      this.filterPornography = PreferenceLoader.loadBoolean("searchFilterPornography");
      this.filterProfanity = PreferenceLoader.loadBoolean("searchFilterProfanity");
      this.verboseNumbers = PreferenceLoader.loadBoolean("verboseNumbers");
   }

   public void searchWaiting(MessageBuffer var1) {
      SearchWaiting var2 = UtilityFactory.getSearchWaiting(this.core);
      var2.read(var1);
      if (this.containsKey(var2.getId())) {
         ((ObjectMap)this.get(var2.getId())).notifyObject(var2);
      } else {
         this.setChanged();
         this.notifyObservers(var2);
      }
   }

   public void setMinAvail(int var1, int var2) {
      synchronized (this.minAvailMap) {
         this.minAvailMap.put(var1, var2);
      }
   }

   public String getStats() {
      ResultCollection$GetCollectionStats var1 = new ResultCollection$GetCollectionStats();
      this.forEachEntry(var1);
      return var1.getStats();
   }
}
