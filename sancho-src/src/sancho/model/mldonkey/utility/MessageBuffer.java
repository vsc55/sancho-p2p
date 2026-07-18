package sancho.model.mldonkey.utility;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import sancho.core.ICore;

public class MessageBuffer {
   private static final String[] EMPTY_STRING_ARRAY = new String[0];
   private static final Tag[] EMPTY_TAG_ARRAY = new Tag[0];
   private static final int CAPACITY = 8192;
   private BufferedInputStream bInputStream;
   private byte[] byteArray;
   private ICore core;
   private byte[] intByteArray;
   private byte[] messageByteArray;
   private int lastLength;
   private int iterator;
   private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

   public MessageBuffer(ICore var1, BufferedInputStream var2) {
      this.core = var1;
      this.iterator = 0;
      this.bInputStream = var2;
      this.intByteArray = new byte[4];
      this.messageByteArray = new byte[8192];
   }

   public int getIterator() {
      return this.iterator;
   }

   public boolean getBool() {
      return this.getByte() == 1;
   }

   public byte getBoolOption() {
      return this.getByte();
   }

   public byte getByte() {
      return this.byteArray[this.iterator++];
   }

   public float getFloat() {
      float var1 = 0.0F;

      try {
         var1 = Float.parseFloat(this.getString());
      } catch (NumberFormatException var3) {
      }

      return var1;
   }

   public int getInt32() {
      return this.getByte() & 0xFF | (this.getByte() & 0xFF) << 8 | (this.getByte() & 0xFF) << 16 | this.getByte() << 24;
   }

   public int[] getInt32List() {
      int var1 = this.getUInt16();
      int[] var2 = new int[var1];

      for (int var3 = 0; var3 < var1; var3++) {
         var2[var3] = this.getInt32();
      }

      return var2;
   }

   public short getInt8() {
      return (short)(this.getByte() & 255);
   }

   public void getIP(byte[] var1) {
      for (int var2 = 0; var2 < 4; var2++) {
         var1[var2] = this.getByte();
      }
   }

   public byte[] getMd4() {
      byte[] var1 = new byte[16];
      boolean var2 = true;

      for (int var3 = 0; var3 < 16; var3++) {
         var1[var3] = this.getByte();
         if (var1[var3] != 0) {
            var2 = false;
         }
      }

      return var2 ? null : var1;
   }

   public int getSignedInt32() {
      int var1 = 0;

      for (int var2 = 0; var2 < 4; var2++) {
         if (this.core.getProtocol() > 16) {
            var1 |= (this.getByte() & 255) << var2 * 8;
         } else {
            var1 |= this.getByte() << var2 * 8;
         }
      }

      return var1;
   }

   public String getString() {
      return this.getString(true);
   }

   public String getString(boolean var1) {
      int var2 = this.getUInt16();
      if (var2 >= 65535) {
         var2 = this.getInt32();
      }

      if (var2 > 0) {
         // Decode with an explicit charset so the result never depends on the JVM
         // default (which flipped to UTF-8 in JDK 18, so the same bytes rendered
         // differently on JDK 17 vs 18+). Modern mldonkey sends UTF-8.
         String var3 = new String(this.byteArray, this.iterator, var2, StandardCharsets.UTF_8);
         this.iterator += var2;
         return var3.intern();
      } else {
         return "";
      }
   }

   public String[] getStringList() {
      return this.getStringList(true);
   }

   public String[] getStringList(boolean var1) {
      int var2 = this.getUInt16();
      if (var2 <= 0) {
         return EMPTY_STRING_ARRAY;
      } else {
         String[] var3 = new String[var2];

         for (int var4 = 0; var4 < var2; var4++) {
            var3[var4] = this.getString(var1);
         }

         return var3;
      }
   }

   public Tag[] getTagList() {
      int var1 = this.getUInt16();
      if (var1 <= 0) {
         return EMPTY_TAG_ARRAY;
      } else {
         Tag[] var2 = new Tag[var1];

         for (int var3 = 0; var3 < var1; var3++) {
            var2[var3] = UtilityFactory.getTag(this.core);
            var2[var3].read(this);
         }

         return var2;
      }
   }

   public int getUInt16() {
      int var1 = this.getByte() & 255;
      int var2 = this.getByte() & 255;
      return var1 + 256 * var2;
   }

   public long getUInt64() {
      long var1 = 0L;

      for (int var3 = 0; var3 < 8; var3++) {
         long var4 = (long)(this.getByte() & 255);
         var4 <<= var3 * 8;
         var1 += var4;
      }

      return var1;
   }

   public byte[] read(byte[] var1, int var2) throws IOException {
      this.lastLength = var2;
      if (var1 == null) {
         if (var2 > 8192) {
            var1 = new byte[var2];
         } else {
            var1 = this.messageByteArray;
         }
      }

      int var4 = 0;

      while (var4 < var2) {
         try {
            int var3 = this.bInputStream.read(var1, var4, var2 - var4);
            if (var3 <= 0) {
               throw new IOException();
            }

            var4 += var3;
         } catch (SocketException var6) {
            throw new IOException();
         }
      }

      return var1;
   }

   public int readMessageLength() throws IOException {
      byte[] var1 = this.read(this.intByteArray, 4);
      return var1[0] & 0xFF | (var1[1] & 0xFF) << 8 | (var1[2] & 0xFF) << 16 | (var1[3] & 0xFF) << 24;
   }

   public int readMessage() throws IOException {
      this.iterator = 0;
      this.byteArray = this.read(null, this.readMessageLength());
      return this.getUInt16();
   }

   public String getLastMessage() {
      int var1 = this.byteArray.length;
      byte[] var2 = new byte[var1];
      System.arraycopy(this.byteArray, 0, var2, 0, var1 - 1);
      return hexDump(var2, this.lastLength);
   }

   public int getLastLength() {
      return this.lastLength;
   }

   public static String hexDump(byte[] var0, int var1) {
      byte var2 = 0;
      StringBuffer var3 = new StringBuffer(var1);
      int var4 = 0;

      while (var4 < var1) {
         String var5 = "0000000" + Integer.toString(var2, 16);
         var3.append(var5.substring(var5.length() - 8)).append("  ");
         StringBuffer var6 = new StringBuffer(16);
         int var7 = 0;

         for (var7 = 0; var4 < var1 && var7 < 16; var4++) {
            var3.append(byteToHex(var0[var4])).append(' ');
            char var8 = (char)var0[var4];
            var6.append(var8 > ' ' && var8 < 127 ? var8 : '.');
            if (var7 == 7) {
               var3.append(" ");
            }

            var7++;
         }

         if (var7 < 8) {
            var3.append(" ");
         }

         for (int var9 = var7; var9 < 16; var9++) {
            var3.append("   ");
            var6.append(" ");
         }

         var3.append(" ").append("|").append(var6).append("|");
         if (var4 > 0 && var4 % 16 == 0) {
            var3.append("\n");
            var2 += 16;
         }
      }

      return var3.toString();
   }

   public static final String byteToHex(byte var0) {
      char[] var1 = new char[]{HEX_DIGITS[var0 >>> 4 & 15], HEX_DIGITS[var0 & 15]};
      return new String(var1);
   }
}
