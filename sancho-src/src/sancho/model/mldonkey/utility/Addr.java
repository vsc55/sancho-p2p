package sancho.model.mldonkey.utility;

import org.eclipse.swt.graphics.Image;
import sancho.model.mldonkey.enums.EnumClientMode;
import sancho.view.utility.SResources;

public class Addr {
   private byte[] byteArray;
   private String ipString;

   public int compareTo(Object var1) {
      if (var1 instanceof Addr) {
         Addr var2 = (Addr)var1;
         if (this.hasHostName() && !var2.hasHostName()) {
            return 1;
         } else if (!this.hasHostName() && var2.hasHostName()) {
            return -1;
         } else if (this.hasHostName() && var2.hasHostName()) {
            return this.getIpString().compareToIgnoreCase(var2.getIpString());
         } else if (this.getByteAddress() == null && var2.getByteAddress() == null) {
            // Both firewalled/unknown -> equal. Returning 1 here (as before) for
            // both a.compareTo(b) and b.compareTo(a) broke antisymmetry and made
            // TimSort throw "Comparison method violates its general contract".
            return 0;
         } else if (var2.getByteAddress() == null) {
            return 1;
         } else {
            return this.getByteAddress() == null ? -1 : compare(this.getByteAddress(), var2.getByteAddress());
         }
      } else {
         return -1;
      }
   }

   public synchronized byte[] getByteAddress() {
      return this.byteArray;
   }

   private synchronized String getIpString() {
      if (this.ipString != null) {
         return this.ipString;
      } else if (this.byteArray != null) {
         StringBuffer var1 = new StringBuffer(16);
         var1.append(this.byteArray[0] & 255);
         var1.append(".");
         var1.append(this.byteArray[1] & 255);
         var1.append(".");
         var1.append(this.byteArray[2] & 255);
         var1.append(".");
         var1.append(this.byteArray[3] & 255);
         return var1.toString().intern();
      } else {
         return "";
      }
   }

   public synchronized boolean hasHostName() {
      return this.byteArray == null && this.ipString != null;
   }

   public synchronized void read(boolean var1, MessageBuffer var2) {
      if (var1) {
         this.ipString = var2.getString();
         this.byteArray = null;
      } else {
         if (this.byteArray == null) {
            this.byteArray = new byte[4];
         }

         var2.getIP(this.byteArray);
         this.ipString = null;
      }

      this.readCountryCode(var2);
   }

   public void readCountryCode(MessageBuffer var1) {
   }

   public void read(MessageBuffer var1) {
      this.read(var1.getBool(), var1);
   }

   public synchronized void setUnknown() {
      this.byteArray = null;
   }

   public synchronized boolean isBlocked() {
      return false;
   }

   public String toString() {
      return this.getByteAddress() == null && this.getIpString().equals("") ? EnumClientMode.FIREWALLED.getName() : this.getIpString();
   }

   public Image getImage() {
      return SResources.getImage("f_--");
   }

   public String getCountry() {
      return "N/A";
   }

   private static int compare(byte[] var0, byte[] var1) {
      if (var0 == var1) {
         return 0;
      } else if (var0 == null) {
         return -1;
      } else if (var1 == null) {
         return 1;
      } else {
         for (int var4 = 0; var4 < var0.length; var4++) {
            int var2 = var0[var4] & 255;
            int var3 = var1[var4] & 255;
            if (var2 != var3) {
               return var2 - var3;
            }
         }

         return 0;
      }
   }
}
