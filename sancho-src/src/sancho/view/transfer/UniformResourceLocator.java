package sancho.view.transfer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

public class UniformResourceLocator extends ByteArrayTransfer {
   private static final String TYPENAME1 = "UniformResourceLocator";
   private static final String TYPENAME2 = "text/x-moz-url-data";
   private static final int TYPEID1 = Transfer.registerType("UniformResourceLocator");
   private static final int TYPEID2 = Transfer.registerType("text/x-moz-url-data");
   private static UniformResourceLocator _instance = new UniformResourceLocator();

   public static UniformResourceLocator getInstance() {
      return _instance;
   }

   public Object nativeToJava(TransferData var1) {
      if (!this.isSupportedType(var1)) {
         return null;
      } else {
         byte[] var2 = (byte[])super.nativeToJava(var1);
         if (var2 == null) {
            return null;
         } else if (var1.type == TYPEID2) {
            // Gecko "text/x-moz-url-data": the URL as UTF-16LE. Decoding with the JVM
            // default charset (UTF-8 since JDK 18) turned every char into mojibake; and
            // the old scan-to-first-NUL truncated it to one character (UTF-16 has a NUL
            // high byte on ASCII). Decode as UTF-16LE and cut at the first NUL.
            String var5 = new String(var2, StandardCharsets.UTF_16LE);
            int var6 = var5.indexOf(0);
            return var6 == -1 ? var5 : var5.substring(0, var6);
         } else {
            // Windows shell "UniformResourceLocator": a NUL-terminated single-byte
            // (ANSI/windows-1252) string. Decode it explicitly rather than with the JVM
            // default charset, which is UTF-8 on JDK 18+ and mangles bytes >= 0x80.
            int var3 = 0;

            for (int var4 = 0; var4 < var2.length && var2[var4] != 0; var4++) {
               var3++;
            }

            return new String(var2, 0, var3, Charset.forName("windows-1252"));
         }
      }
   }

   protected String[] getTypeNames() {
      return new String[]{"UniformResourceLocator", "text/x-moz-url-data"};
   }

   protected int[] getTypeIds() {
      return new int[]{TYPEID1, TYPEID2};
   }
}
