package sancho.model.mldonkey;

import sancho.core.ICore;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.NetworkStatCollection;
import sancho.model.mldonkey.utility.UtilityFactory;

public class Network41 extends Network18 {
   NetworkStatCollection[] networkStatCollectionArray;

   Network41(ICore var1) {
      super(var1);
   }

   public void getStats() {
      Object[] var1 = new Object[]{Integer.valueOf(this.getId())};
      this.core.send((short)68, var1);
   }

   public synchronized NetworkStatCollection[] getNetworkStatCollection() {
      if (this.networkStatCollectionArray == null) {
         return null;
      } else {
         NetworkStatCollection[] var1 = new NetworkStatCollection[this.networkStatCollectionArray.length];
         System.arraycopy(this.networkStatCollectionArray, 0, var1, 0, this.networkStatCollectionArray.length);
         return var1;
      }
   }

   public void readStats(MessageBuffer var1) {
      synchronized (this) {
         int var3 = var1.getUInt16();
         if (var3 > 0 && (this.networkStatCollectionArray == null || var3 != this.networkStatCollectionArray.length)) {
            this.networkStatCollectionArray = new NetworkStatCollection[var3];
         }

         if (var3 > 0) {
            for (int var4 = 0; var4 < var3; var4++) {
               NetworkStatCollection var5 = this.networkStatCollectionArray[var4];
               if (var5 == null) {
                  var5 = UtilityFactory.getNetworkStatCollection();
               }

               var5.read(var1);
               this.networkStatCollectionArray[var4] = var5;
            }
         }
      }
   }
}
