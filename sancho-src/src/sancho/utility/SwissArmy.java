package sancho.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ServerSocket;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import sancho.core.ICore;
import sancho.core.Sancho;
import sancho.model.mldonkey.Network;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.view.preferences.PreferenceLoader;

public class SwissArmy {
   private static final float k = 1024.0F;
   private static final float m = 1048576.0F;
   private static final float g = 1.0737418E9F;
   private static final float t = 1.0995116E12F;
   private static String S_D = "d";
   private static String S_H = "h";
   private static String S_M = "m";
   private static String S_S = "s";
   private static String S_TB = "tb";
   private static String S_GB = "gb";
   private static String S_MB = "mb";
   private static String S_KB = "kb";
   private static String S_STB = " TB";
   private static String S_SGB = " GB";
   private static String S_SMB = " MB";
   private static String S_SKB = " KB";
   private static String S_NULL_MD4 = "00000000000000000000000000000000";
   private static final FieldPosition FP = new FieldPosition(1);
   private static DecimalFormat df00 = new DecimalFormat("0.0");
   private static DecimalFormat df000 = new DecimalFormat("0.00");
   private static DecimalFormat dfPercent = new DecimalFormat("0");
   private static DecimalFormat dfRate = new DecimalFormat("0.00");
   private static final DecimalFormat dfGrouped = new DecimalFormat();
   private static boolean humanReadable;
   private static boolean maxMegabytes;
   public static boolean disableUTF8;
   protected static Pattern fakeRE;
   private static long secondsInDay;
   private static long secondsInHour;
   static Pattern linksRE1;
   static Pattern linksRE2;
   static Pattern hrefRE;
   static Pattern envRE;

   public static boolean containsFake(String var0) {
      return fakeRE != null && var0 != null && fakeRE.matcher(var0).find();
   }

   public static String[] split(String var0, char var1) {
      Pattern var2 = null;
      String var3 = "([^" + var1 + "])*";

      try {
         var2 = Pattern.compile(var3);
      } catch (PatternSyntaxException var8) {
         var8.printStackTrace();
      }

      ArrayList var4 = new ArrayList();
      if (var0 != null) {
         Matcher var5 = var2.matcher(var0);

         while (var5.find()) {
            String var7 = var5.group();
            if (!var7.equals("")) {
               var4.add(var7);
            }
         }
      }

      String[] var9 = new String[var4.size()];
      var4.toArray(var9);
      return var9;
   }

   public static String replaceAll(String var0, String var1, String var2) {
      Pattern var3 = null;

      try {
         var3 = Pattern.compile(var1);
      } catch (PatternSyntaxException var5) {
         var5.printStackTrace();
      }

      return var3.matcher(var0).replaceAll(var2);
   }

   public static String calcStringSizeGrouped(long var0) {
      return dfGrouped.format(var0).intern();
   }

   public static String calcStringSize(long var0) {
      if (var0 == 0L) {
         return "0";
      } else {
         double var2 = (double)var0;
         if (!humanReadable) {
            return dfGrouped.format(var2).intern();
         } else {
            StringBuffer var4 = new StringBuffer(10);
            if (var2 >= 1.0995116E12F && !maxMegabytes) {
               df000.format(var2 / 1.0995116E12F, var4, FP);
               return var4.append(S_STB).toString().intern();
            } else if (var2 >= 1.0737418E9F && !maxMegabytes) {
               df000.format(var2 / 1.0737418E9F, var4, FP);
               return var4.append(S_SGB).toString().intern();
            } else if (var2 >= 1048576.0) {
               df00.format(var2 / 1048576.0, var4, FP);
               return var4.append(S_SMB).toString().intern();
            } else if (var2 >= 1024.0) {
               df00.format(var2 / 1024.0, var4, FP);
               return var4.append(S_SKB).toString().intern();
            } else {
               return String.valueOf(var0).intern();
            }
         }
      }
   }

   public static String calcStringOfSeconds(long var0) {
      StringBuffer var2 = new StringBuffer(10);
      if (var0 < 60L) {
         return var2.append(var0).append(S_S).toString().intern();
      } else {
         long var3 = var0 / secondsInDay;
         if (var3 > 9999L) {
            return "";
         } else {
            long var5 = var0 - var3 * secondsInDay;
            long var7 = var5 / secondsInHour;
            var5 -= var7 * secondsInHour;
            long var9 = var5 / 60L;
            var5 -= var9 * 60L;
            if (var3 > 0L) {
               return var2.append(var3).append(S_D).toString().intern();
            } else if (var7 > 0L) {
               var2.append(var7).append(S_H);
               if (var9 > 0L) {
                  var2.append(" ").append(var9).append(S_M);
               }

               return var2.toString().intern();
            } else if (var9 > 0L) {
               var2.append(var9).append(S_M);
               if (var5 > 0L) {
                  var2.append(" ").append(var5).append(S_S);
               }

               return var2.toString().intern();
            } else {
               return var2.append(var9).append(S_M).toString().intern();
            }
         }
      }
   }

   public static String calcStringOfSecondsFull(long var0) {
      StringBuffer var2 = new StringBuffer(10);
      if (var0 < 60L) {
         return var2.append(var0).append(S_S).toString().intern();
      } else {
         long var3 = var0 / secondsInDay;
         long var5 = var0 - var3 * secondsInDay;
         long var7 = var5 / secondsInHour;
         var5 -= var7 * secondsInHour;
         long var9 = var5 / 60L;
         var5 -= var9 * 60L;
         boolean var11 = false;
         if (var3 > 0L) {
            var2.append(var3).append(S_D);
            var11 = true;
         }

         if (var7 > 0L) {
            if (var11) {
               var2.append(" ");
            }

            var2.append(var7).append(S_H);
            var11 = true;
         }

         if (var9 > 0L) {
            if (var11) {
               var2.append(" ");
            }

            var2.append(var9).append(S_M);
            var11 = true;
         }

         if (var5 > 0L) {
            if (var11) {
               var2.append(" ");
            }

            var2.append(var5).append(S_S);
         }

         return var2.toString().intern();
      }
   }

   public static String calcUptime(long var0) {
      long var2 = 0L;
      long var4 = System.currentTimeMillis();
      if (var4 > var0) {
         var2 = (var4 - var0) / 1000L;
      }

      return calcStringOfSeconds(var2);
   }

   public static String calcTimeOfSeconds(long var0) {
      StringBuffer var2 = new StringBuffer(10);
      long var3 = var0 / 60L / 60L;
      long var5 = var0 - var3 * 60L * 60L;
      long var7 = var5 / 60L;
      var5 -= var7 * 60L;
      if (var3 > 0L) {
         var2.append(var3);
         var2.append(S_H);
      }

      if (var7 > 0L) {
         var2.append(var7);
         var2.append(S_M);
      }

      if (var5 > 0L) {
         var2.append(var5);
         var2.append(S_S);
      }

      return var2.toString();
   }

   public static String calcStringOfMD4(byte[] var0) {
      if (var0 == null) {
         return S_NULL_MD4;
      } else {
         StringBuffer var1 = new StringBuffer(32);
         int var2 = var0.length;

         for (int var4 = 0; var4 < var2; var4++) {
            short var3 = (short)(var0[var4] & 255);
            if (var3 <= 15) {
               var1.append(0);
            }

            var1.append(Integer.toHexString(var3));
         }

         return var1.toString().intern();
      }
   }

   public static long stringSizeToLong(String var0, String var1) {
      // multiplier must be long: 2^40 (TB) overflows int, so the old code left it 0
      // and every "TB" size converted to 0 bytes. double keeps precision at GB/TB.
      long var2 = 1L;
      if (var1.equalsIgnoreCase(S_KB)) {
         var2 = 1024L;
      } else if (var1.equalsIgnoreCase(S_MB)) {
         var2 = 1048576L;
      } else if (var1.equalsIgnoreCase(S_GB)) {
         var2 = 1073741824L;
      } else if (var1.equalsIgnoreCase(S_TB)) {
         var2 = 1099511627776L;
      }

      double var3;
      try {
         var3 = Double.parseDouble(var0);
      } catch (NumberFormatException var5) {
         var3 = 1.0;
      }

      return (long)(var3 * (double)var2);
   }

   public static int log2(int var0) {
      int var1;
      for (var1 = -1; var0 > 0; var1++) {
         var0 >>= 1;
      }

      return var1;
   }

   public static int countBits(int var0) {
      var0 = (var0 >>> 1 & 1431655765) + (var0 & 1431655765);
      var0 = (var0 >>> 2 & 858993459) + (var0 & 858993459);
      var0 = (var0 >>> 4 & 252645135) + (var0 & 252645135);
      var0 = (var0 >>> 8 & 16711935) + (var0 & 16711935);
      return (var0 >>> 16) + (var0 & 65535);
   }

   public static void threadSleep(int var0) {
      try {
         Thread.sleep((long)var0);
      } catch (InterruptedException var2) {
      }
   }

   public static String replaceEnvVars(String var0) {
      if (var0 == null) {
         return var0;
      }

      Matcher var1 = envRE.matcher(var0);
      if (!var1.find()) {
         return var0;
      } else {
         String var2 = "";
         int var3 = 0;

         do {
            int var5 = var1.start(1);
            var2 = var2 + var0.substring(var3, var5);
            var3 = var1.end(1);
            String var6 = var0.substring(var5, var3);
            String var7 = replaceAll(var6, "%", "");

            try {
               String var8 = System.getenv(var7);
               if (var8 != null) {
                  var6 = var8;
               }
            } catch (Exception var10) {
            } catch (Error var11) {
            }

            var2 = var2 + var6;
         } while (var1.find());

         return var2 + var0.substring(var3);
      }
   }

   public static String[] parseLinks(String[] var0) {
      Pattern var1 = PreferenceLoader.loadBoolean("linkRipperShowAll") ? linksRE2 : linksRE1;
      ArrayList var2 = new ArrayList();
      Object var3 = null;

      for (int var5 = 0; var5 < var0.length; var5++) {
         if (var0[var5] != null) {
            Matcher var4 = var1.matcher(var0[var5]);
            if (var4.find()) {
               var3 = var4.group();
               if (!var2.contains(var3)) {
                  var2.add(var3);
               }
            }
         }
      }

      String[] var6 = new String[var2.size()];
      var2.toArray(var6);
      Arrays.sort(var6, String.CASE_INSENSITIVE_ORDER);
      return var6;
   }

   public static String[] parseLinks(String var0) {
      ArrayList var1 = new ArrayList();
      if (var0 != null) {
         Matcher var6 = hrefRE.matcher(var0);

         while (var6.find()) {
            var1.add(var6.group());
         }
      }

      StringBuffer var3 = new StringBuffer();
      String[] var4 = new String[var1.size()];

      for (int var5 = 0; var5 < var4.length; var5++) {
         var3.setLength(0);
         var3.append((String)var1.get(var5));
         int var2 = var3.indexOf("=");
         if (var2 != -1 && var3.length() > var2 + 1) {
            var3.delete(0, var2 + 1);
         }

         var3.deleteCharAt(var3.length() - 1);
         lrTrim(var3);
         var4[var5] = replaceAll(var3.toString(), "\"", "");
      }

      return parseLinks(var4);
   }

   public static void lrTrim(StringBuffer var0) {
      while (var0.length() > 0 && (var0.charAt(0) == '"' || var0.charAt(0) == ' ')) {
         var0.deleteCharAt(0);
      }

      int var1 = 0;

      while ((var1 = var0.length() - 1) > 0 && (var0.charAt(var1) == '"' || var0.charAt(var1) == ' ')) {
         var0.deleteCharAt(var1);
      }
   }

   public static String getRandomString(int var0) {
      Random var1 = new Random();
      var1.setSeed(System.currentTimeMillis());
      StringBuffer var2 = new StringBuffer(var0);
      byte var4 = 26;

      for (int var5 = 0; var5 < var0; var5++) {
         int var3 = 97 + var1.nextInt(var4);
         var2.append((char)var3);
      }

      return var2.toString();
   }

   public static Object[] toArray(Collection var0) {
      ArrayList var1 = new ArrayList(var0.size());

      try {
         Iterator var2 = var0.iterator();

         while (var2.hasNext()) {
            var1.add(var2.next());
         }
      } catch (NoSuchElementException var3) {
      }

      return var1.toArray();
   }

   public static void clear(Map var0) {
      int var1 = var0.size();

      try {
         Iterator var2 = var0.entrySet().iterator();

         while (--var1 >= 0) {
            try {
               var2.next();
               var2.remove();
            } catch (NoSuchElementException var4) {
            }
         }
      } catch (NoSuchElementException var5) {
      }
   }

   public static void execInThread(String[] var0, String var1) {
      SwissArmy$1 var2 = new SwissArmy$1(var1, var0);
      var2.setDaemon(true);
      var2.start();
   }

   public static byte[] fileToByteArray(String var0) {
      File var1 = new File(var0);
      if (var1.exists()) {
         ByteArrayOutputStream var2 = new ByteArrayOutputStream();
         short var3 = 8192;
         byte[] var4 = new byte[var3];

         try {
            BufferedInputStream var5 = new BufferedInputStream(new FileInputStream(var1));

            int var6;
            while ((var6 = var5.read(var4, 0, var3)) != -1) {
               var2.write(var4, 0, var6);
            }

            var5.close();
            var2.close();
            return var2.toByteArray();
         } catch (IOException var7) {
            Sancho.pDebug(var7.toString());
            var7.printStackTrace();
            return null;
         }
      } else {
         Sancho.pDebug("File not found (permissions?): " + var1.getAbsolutePath());
         return null;
      }
   }

   public static void sendLocalTorrent(String var0, ICore var1) {
      if (Sancho.hasCollectionFactory()) {
         Network var2 = var1.getNetworkCollection().getByEnum(EnumNetwork.BT);
         if (var2 != null) {
            byte[] var3 = fileToByteArray(replaceAll(var0, "\"", ""));
            if (var3 != null) {
               byte[] var4 = new byte[]{-1, -1};
               Object[] var5 = new Object[]{Integer.valueOf(var2.getId()), var4, Integer.valueOf(var3.length + 2), Short.valueOf((short)0), var3};
               Sancho.send((short)63, var5);
               Sancho.pDebug("Sent Size: " + var3.length);
               threadSleep(1000);
            }
         }
      }
   }

   public static boolean isSupportedProtocol(String var0) {
      String[] var1 = new String[]{"http:", "ftp:", "ssh:", "ed2k:", "magnet:", "sig2dat:"};
      String var2 = var0.toLowerCase();

      for (int var3 = 0; var3 < var1.length; var3++) {
         if (var2.startsWith(var1[var3])) {
            return true;
         }
      }

      return false;
   }

   public static boolean portInUse(int var0) {
      try {
         ServerSocket var1 = new ServerSocket(var0);
         var1.close();
         return false;
      } catch (BindException var3) {
         return true;
      } catch (IOException var4) {
         return true;
      }
   }

   public static String sfdl2ed2k(String var0) {
      int var1 = var0.indexOf("|");
      if (var1 < 0) {
         return var0;
      } else {
         int var2 = var0.indexOf("|", var1 + 1);
         if (var2 < 0) {
            return var0;
         } else {
            var0 = "ed2k://|file" + var0.substring(var2);
            return replaceAll(var0, "\"", "");
         }
      }
   }

   public static void sendLink(ICore var0, String var1) {
      String var2 = var1.toLowerCase();
      if (var2.startsWith("sfdl://")) {
         var1 = sfdl2ed2k(var1);
         var2 = sfdl2ed2k(var2);
      }

      if (var0 != null && Sancho.hasCollectionFactory()) {
         Network var3 = var0.getNetworkCollection().getByEnum(EnumNetwork.BT);
         Sancho.pDebug("Network: " + (var3 != null ? "BT" : "null"));
         if (var0.getProtocol() > 25 && var3 != null && !isSupportedProtocol(var2)) {
            sendLocalTorrent(var1, var0);
         } else if (!var2.startsWith("ftp:") && !var2.startsWith("ssh:") && (!var2.startsWith("http:") || var2.endsWith("torrent") || var2.endsWith("tor"))) {
            Sancho.send((short)8, var1);
         } else {
            Sancho.send((short)29, "http " + var1);
         }
      }
   }

   public static int readLastFile() {
      File var0 = new File(VersionInfo.getHomeDirectory() + ".last");
      if (!var0.exists()) {
         return 0;
      } else {
         BufferedReader var1 = null;

         byte var4;
         try {
            var1 = new BufferedReader(new FileReader(var0));
            String var2 = var1.readLine();

            try {
               return Integer.parseInt(var2);
            } catch (NumberFormatException var18) {
               return 0;
            }
         } catch (FileNotFoundException var19) {
            return 0;
         } catch (IOException var20) {
            var4 = 0;
         } finally {
            try {
               if (var1 != null) {
                  var1.close();
               }
            } catch (IOException var17) {
               return 0;
            }
         }

         return var4;
      }
   }

   public static void writeLastFile(int var0) {
      File var1 = new File(VersionInfo.getHomeDirectory() + ".last");

      try {
         FileOutputStream var2 = new FileOutputStream(var1);
         PrintStream var3 = new PrintStream(var2);
         var3.println(var0);
         var3.close();
         var2.close();
      } catch (FileNotFoundException var5) {
      } catch (IOException var6) {
      }
   }

   public static String stringNoAccel(String var0) {
      return replaceAll(var0, "&", "&&");
   }

   public static synchronized void updatePreferences() {
      dfRate = updateDF(PreferenceLoader.loadInt("dlRateDecimals"));
      dfPercent = updateDF(PreferenceLoader.loadInt("dlPercentDecimals"));
      humanReadable = PreferenceLoader.loadBoolean("humanReadable");
      maxMegabytes = PreferenceLoader.loadBoolean("maxMegabytes");
      int var0 = PreferenceLoader.loadInt("humanReadableDecimals");
      disableUTF8 = PreferenceLoader.loadBoolean("disableUTF8");
      if (var0 < 0) {
         df00 = new DecimalFormat("0.0");
         df000 = new DecimalFormat("0.00");
      } else {
         df00 = updateDF(var0);
         df000 = updateDF(var0);
      }
   }

   public static synchronized String calcRate(float var0) {
      StringBuffer var1 = new StringBuffer(8);
      df000.format((double)var0, var1, FP);
      return var1.toString().intern();
   }

   public static synchronized String percentToString(float var0) {
      StringBuffer var1 = new StringBuffer(10);
      dfPercent.format((double)var0, var1, FP);
      var1.append("%");
      return var1.toString().intern();
   }

   public static synchronized String rateToString(float var0) {
      return dfRate.format((double)(var0 / 1000.0F)).intern();
   }

   public static int UTF8Length(String var0) {
      byte[] var1;
      try {
         var1 = var0.getBytes("UTF8");
      } catch (UnsupportedEncodingException var3) {
         var1 = var0.getBytes();
      }

      return var1.length;
   }

   public static DecimalFormat updateDF(int var0) {
      String var1 = "0";
      if (var0 > 0) {
         var1 = var1 + ".";

         for (int var2 = 0; var2 < var0; var2++) {
            var1 = var1 + "0";
         }
      }

      return new DecimalFormat(var1);
   }

   static {
      dfGrouped.setGroupingSize(3);

      try {
         fakeRE = Pattern.compile("fake", Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException var6) {
         fakeRE = null;
      }

      secondsInDay = 86400L;
      secondsInHour = 3600L;
      String var0 = "(ed2k://\\|file\\|[^\\|]+\\|(\\d+)\\|([\\dabcdef]+)\\|)|(sig2dat:///?\\|File:[^\\|]+\\|Length:.+?\\|UUHash:.+?\\=.+?\\=)|(magnet:\\?xt=.+)|(\"http://.+\\.torrent\\?[^>]+\")|(http://.+\\.torrent)";
      String var1 = var0 + "|(http://.+)" + "|(ftp://.+)";
      String var2 = "href.*?=.+?>";
      String var3 = "(%.+?%)";

      try {
         linksRE1 = Pattern.compile(var0, Pattern.CASE_INSENSITIVE);
         linksRE2 = Pattern.compile(var0 + var1, Pattern.CASE_INSENSITIVE);
         hrefRE = Pattern.compile(var2, Pattern.CASE_INSENSITIVE);
         envRE = Pattern.compile(var3, Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException var5) {
      }
   }
}
