package sancho.model.mldonkey;

import sancho.core.ICore;
import sancho.model.mldonkey.utility.FileComment;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.UtilityFactory;
import sancho.utility.SwissArmy;

public class File41 extends File36 {
   String[] subFileMagics;
   String magic;
   String user;
   String group;
   FileComment[] fileComments;
   int avgRating = -1;

   File41(ICore var1) {
      super(var1);
   }

   public void chown(String var1) {
      String var2 = "chown \"" + var1 + "\" " + this.getId();
      this.core.send((short)29, var2);
      this.core.send((short)37, Integer.valueOf(this.getId()));
   }

   public void chgrp(String var1) {
      String var2 = "chgrp \"" + var1 + "\" " + this.getId();
      this.core.send((short)29, var2);
      this.core.send((short)37, Integer.valueOf(this.getId()));
   }

   public synchronized String getUser() {
      return this.user;
   }

   public synchronized String getGroup() {
      return this.group;
   }

   public synchronized int getAvgRating() {
      return this.avgRating;
   }

   protected void readSubFiles(MessageBuffer var1) {
      int var2 = var1.getUInt16();
      if (var2 > 0) {
         this.subFileNames = new String[var2];
         this.subFileSizes = new long[var2];
         this.subFileMagics = new String[var2];

         for (int var3 = 0; var3 < var2; var3++) {
            this.subFileNames[var3] = var1.getString();
            this.subFileSizes[var3] = var1.getUInt64();
            this.subFileMagics[var3] = var1.getString();
         }
      }
   }

   protected void readUser(MessageBuffer var1) {
      this.user = var1.getString();
   }

   protected void readGroup(MessageBuffer var1) {
      this.group = var1.getString();
   }

   protected void readMagic(MessageBuffer var1) {
      this.magic = var1.getString();
   }

   protected void readFileComments(MessageBuffer var1) {
      int var2 = var1.getUInt16();
      if (var2 > 0) {
         this.avgRating = 0;
         int var3 = 0;
         if (this.fileComments != null) {
            var3 = this.fileComments.length;
         }

         if (var3 != var2) {
            this.addChangedBits(2048);
         }

         this.fileComments = new FileComment[var2];
         int var4 = 0;
         int var5 = 0;

         for (int var6 = 0; var6 < var2; var6++) {
            this.fileComments[var6] = UtilityFactory.getFileComment(this.core);
            this.fileComments[var6].read(var1);
            int var7 = this.fileComments[var6].getRating();
            if (var7 < 0) {
               var7 = 0;
            } else if (var7 > 5) {
               var7 = 5;
            }

            if (var7 > 0) {
               var4++;
               var5 += var7;
            }
         }

         if (var4 > 0) {
            this.avgRating = (int)Math.round((double)var5 / (double)var4);
         }
      }
   }

   public synchronized int getNumComments() {
      return this.fileComments == null ? 0 : this.fileComments.length;
   }

   public String getNumCommentsString() {
      return String.valueOf(this.getNumComments());
   }

   protected void fakeCheck() {
      super.fakeCheck();
      if (!this.containsFake && this.fileComments != null) {
         for (int var1 = 0; var1 < this.fileComments.length; var1++) {
            this.containsFake = SwissArmy.containsFake(this.fileComments[var1].getComment());
            if (this.containsFake) {
               break;
            }
         }
      }
   }

   public synchronized String getMagic() {
      return this.magic != null ? this.magic : "";
   }

   public synchronized String[] getSubFileMagics() {
      if (this.subFileNames == null) {
         return null;
      } else {
         String[] var1 = new String[this.subFileMagics.length];
         System.arraycopy(this.subFileMagics, 0, var1, 0, this.subFileMagics.length);
         return var1;
      }
   }

   public synchronized boolean hasFileComments() {
      return this.fileComments != null;
   }

   public synchronized FileComment[] getFileComments() {
      return this.fileComments;
   }
}
