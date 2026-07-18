package sancho.model.mldonkey.utility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class MessageEncoder {
   private ByteArrayOutputStream baOutputStream;
   private BufferedOutputStream bOutputStream;
   private byte[] numberBuffer;
   private byte[] header;
   private int length;
   private short opCode;
   private Socket socket;

   public MessageEncoder(Socket var1) {
      this.socket = var1;
      this.header = new byte[6];
      this.numberBuffer = new byte[8];
      this.baOutputStream = new ByteArrayOutputStream();
   }

   private void appendNumber(Number var1) {
      byte var2;
      if (var1 instanceof Short) {
         var2 = 2;
         this.numberBuffer = this.toBytes((Short)var1, this.numberBuffer, 0);
      } else if (var1 instanceof Integer) {
         var2 = 4;
         this.numberBuffer = this.toBytes((Integer)var1, this.numberBuffer, 0);
      } else {
         var2 = 8;
         this.numberBuffer = this.toBytes((Long)var1, this.numberBuffer, 0);
      }

      this.baOutputStream.write(this.numberBuffer, 0, var2);
   }

   private void appendObject(Object var1) {
      if (var1 instanceof Byte) {
         this.baOutputStream.write((Byte)var1);
      } else if (var1 instanceof Number) {
         this.appendNumber((Number)var1);
      } else if (var1 instanceof String) {
         String var2 = (String)var1;

         byte[] var3;
         try {
            var3 = var2.getBytes("UTF8");
         } catch (UnsupportedEncodingException var5) {
            var3 = var2.getBytes();
         }

         this.appendNumber(Short.valueOf((short)var3.length));
         this.baOutputStream.write(var3, 0, var3.length);
      }
   }

   private int createContent(Object[] var1) {
      for (int var2 = 0; var2 < var1.length; var2++) {
         if (var1[var2] instanceof byte[]) {
            byte[] var5 = (byte[])var1[var2];
            this.baOutputStream.write(var5, 0, var5.length);
         } else if (var1[var2].getClass().isArray()) {
            Object[] var3 = (Object[])var1[var2];
            this.appendNumber(Short.valueOf((short)var3.length));

            for (int var4 = 0; var4 < var3.length; var4++) {
               this.appendObject(var3[var4]);
            }
         } else {
            this.appendObject(var1[var2]);
         }
      }

      return this.baOutputStream.size();
   }

   private void createHeader() {
      this.header = this.toBytes(Integer.valueOf(this.length), this.header, 0);
      this.header = this.toBytes(Short.valueOf(this.opCode), this.header, 4);
   }

   public void send(short var1, Object[] var2) throws IOException {
      this.opCode = var1;
      this.length = 2;
      this.baOutputStream.reset();
      if (var2 != null) {
         this.length = this.length + this.createContent(var2);
      }

      this.createHeader();
      this.write();
   }

   private byte[] toBytes(Integer var1, byte[] var2, int var3) {
      int var4 = var1;
      var2[0 + var3] = (byte)(var4 & 0xFF);
      var2[1 + var3] = (byte)((var4 & 65535) >> 8);
      var2[2 + var3] = (byte)((var4 & 16777215) >> 16);
      var2[3 + var3] = (byte)((var4 & 2147483647) >> 24);
      return var2;
   }

   private byte[] toBytes(Long var1, byte[] var2, int var3) {
      long var4 = var1;

      for (int var6 = 0; var6 < 8; var6++) {
         var2[var6 + var3] = (byte)((int)(var4 % 256L));
         var4 /= 256L;
      }

      return var2;
   }

   private byte[] toBytes(Short var1, byte[] var2, int var3) {
      short var4 = var1;

      for (int var5 = 0; var5 < 2; var5++) {
         var2[var5 + var3] = (byte)(var4 % 256);
         var4 = (short)(var4 / 256);
      }

      return var2;
   }

   public void write() throws IOException {
      if (this.bOutputStream == null) {
         this.bOutputStream = new BufferedOutputStream(this.socket.getOutputStream());
      }

      this.bOutputStream.write(this.header);
      if (this.length > 2) {
         this.bOutputStream.write(this.baOutputStream.toByteArray());
      }

      this.bOutputStream.flush();
   }
}
