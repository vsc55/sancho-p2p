package sancho.view.utility;

public class Base64 {
   public static String encode(byte[] var0) {
      StringBuffer var1 = new StringBuffer();

      for (int var2 = 0; var2 < var0.length; var2 += 3) {
         // int, not byte: as a byte the index overflowed past 127 and crashed
         // (or looped) for inputs longer than 127 bytes, e.g. long HTTP credentials.
         var1.append(encodeBlock(var0, var2));
      }

      return var1.toString();
   }

   protected static char[] encodeBlock(byte[] var0, int var1) {
      int var2 = 0;
      int var3 = var0.length - var1 - 1;
      int var4 = var3 >= 2 ? 2 : var3;

      for (int var5 = 0; var5 <= var4; var5++) {
         byte var6 = var0[var1 + var5];
         int var7 = var6 < 0 ? var6 + 256 : var6;
         var2 += var7 << 8 * (2 - var5);
      }

      char[] var9 = new char[4];

      for (int var10 = 0; var10 < 4; var10++) {
         int var8 = var2 >>> 6 * (3 - var10) & 63;
         var9[var10] = getChar(var8);
      }

      if (var3 < 1) {
         var9[2] = '=';
      }

      if (var3 < 2) {
         var9[3] = '=';
      }

      return var9;
   }

   protected static char getChar(int var0) {
      if (var0 >= 0 && var0 <= 25) {
         return (char)(65 + var0);
      } else if (var0 >= 26 && var0 <= 51) {
         return (char)(97 + (var0 - 26));
      } else if (var0 >= 52 && var0 <= 61) {
         return (char)(48 + (var0 - 52));
      } else if (var0 == 62) {
         return '+';
      } else {
         return (char)(var0 == 63 ? '/' : '?');
      }
   }

   public static byte[] decode(String var0) {
      int var1 = 0;

      for (int var2 = var0.length() - 1; var0.charAt(var2) == '='; var2--) {
         var1++;
      }

      int var3 = var0.length() * 6 / 8 - var1;
      byte[] var4 = new byte[var3];
      int var5 = 0;

      for (int var6 = 0; var6 < var0.length(); var6 += 4) {
         int var7 = (getValue(var0.charAt(var6)) << 18)
            + (getValue(var0.charAt(var6 + 1)) << 12)
            + (getValue(var0.charAt(var6 + 2)) << 6)
            + getValue(var0.charAt(var6 + 3));

         for (int var8 = 0; var8 < 3 && var5 + var8 < var4.length; var8++) {
            var4[var5 + var8] = (byte)(var7 >> 8 * (2 - var8) & 0xFF);
         }

         var5 += 3;
      }

      return var4;
   }

   protected static int getValue(char var0) {
      if (var0 >= 'A' && var0 <= 'Z') {
         return var0 - 65;
      } else if (var0 >= 'a' && var0 <= 'z') {
         return var0 - 97 + 26;
      } else if (var0 >= '0' && var0 <= '9') {
         return var0 - 48 + 52;
      } else if (var0 == '+') {
         return 62;
      } else if (var0 == '/') {
         return 63;
      } else {
         return var0 == 61 ? 0 : -1;
      }
   }
}
