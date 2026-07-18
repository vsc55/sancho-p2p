package sancho.model.mldonkey;

import sancho.utility.regex.RE;
import sancho.utility.regex.REException;
import java.util.StringTokenizer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import sancho.core.CoreFactory;
import sancho.core.ICore;
import sancho.core.Sancho;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.utility.SwissArmy;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;

public class SharedFile extends AObject implements IObject_UID, IPreview {
   static RE pathRE;
   protected long bytesUploaded;
   protected int id;
   protected byte[] md4;
   protected String name;
   protected EnumNetwork networkEnum;
   protected int requests;
   protected long size;

   SharedFile(ICore var1) {
      super(var1);
   }

   protected void readSubFiles(MessageBuffer var1) {
   }

   public String[] getSubFileNames() {
      return null;
   }

   public long[] getSubFileSizes() {
      return null;
   }

   public String[] getSubFileMagics() {
      return null;
   }

   public String getMagic() {
      return "";
   }

   public String getContentRange(int var1) {
      long[] var2 = this.getSubFileSizes();
      if (var2 != null && var1 <= var2.length) {
         long var3 = 0L;

         for (int var5 = 0; var5 < var1; var5++) {
            var3 += var2[var5];
         }

         long var6 = var3 + var2[var1] - 1L;
         return "bytes=" + var3 + "-" + var6;
      } else {
         return "";
      }
   }

   public String getPreviewURL() {
      CoreFactory var1 = Sancho.getCoreFactory();
      String var2 = "";
      if (!var1.getPassword().equals("")) {
         var2 = var1.getUsername() + ":" + var1.getPassword() + "@";
      }

      return "http://" + var2 + var1.getHostname() + ":" + var1.getHTTPPort() + "/preview_upload?q=" + this.getId();
   }

   public synchronized Program getOSPreviewApp() {
      return null;
   }

   public String preview(Program var1, int var2) {
      return "Not supported";
   }

   public String preview(int var1) {
      return this.preview((String)null, var1);
   }

   public String preview(String var1, int var2) {
      String var3 = PreferenceLoader.loadString("previewExecutable");
      String var4 = PreferenceLoader.loadString("previewWorkingDirectory");
      String var5 = PreferenceLoader.loadString("previewExtensions");
      if (!var5.equals("")) {
         StringTokenizer var6 = new StringTokenizer(var5, ";");
         String var7 = "";
         String var8 = "";

         while (var6.hasMoreTokens()) {
            var7 = var6.nextToken();
            if (var6.hasMoreTokens()) {
               var8 = var6.nextToken();
               if (this.getName().toLowerCase().endsWith(var7.toLowerCase())) {
                  var3 = var8;
               }
            }
         }
      }

      if (var1 != null) {
         var3 = var1;
      }

      String var9 = this.getPreviewURL();
      String[] var11 = new String[]{var3, var9};
      SwissArmy.execInThread(var11, var4.equals("") ? null : var4);
      return var3;
   }

   public synchronized String getExtension() {
      int var1 = this.getName().lastIndexOf(".");
      return var1 != -1 ? this.getName().substring(var1 + 1).toLowerCase().intern() : "";
   }

   public synchronized ImageDescriptor getFileTypeImageDescriptor() {
      return SResources.getFileTypeImageDescriptor(this.getExtension());
   }

   public synchronized Image getFileTypeImage() {
      return SResources.getFileTypeImage(this.getExtension());
   }

   public synchronized long getBytesUploaded() {
      return this.bytesUploaded;
   }

   public String getED2K() {
      return "ed2k://|file|" + this.getName() + "|" + this.getSize() + "|" + this.getMD4() + "|/";
   }

   public synchronized int getId() {
      return this.id;
   }

   public synchronized String getMD4() {
      return SwissArmy.calcStringOfMD4(this.md4);
   }

   public synchronized String getName() {
      return this.name != null ? this.name : "";
   }

   public synchronized int getRequests() {
      return this.requests;
   }

   public synchronized String getRequestsString() {
      return SwissArmy.calcStringSizeGrouped((long)this.requests);
   }

   public synchronized String getNetworkName() {
      return this.networkEnum.getName();
   }

   public synchronized Image getNetworkImage() {
      return this.networkEnum.getImage();
   }

   public synchronized long getSize() {
      return this.size;
   }

   public synchronized String getSizeString() {
      return SwissArmy.calcStringSize(this.size);
   }

   public synchronized String getUploadedString() {
      return SwissArmy.calcStringSize(this.bytesUploaded);
   }

   public synchronized String[] getUIDs() {
      return null;
   }

   public void parseName() {
      if (this.name.indexOf("/") != -1) {
         this.name = pathRE.getMatch(this.name).toString();
      }
   }

   public void read(int var1, MessageBuffer var2) {
      synchronized (this) {
         this.id = var1;
         this.networkEnum = this.readNetworkEnum(var2);
         this.name = var2.getString();
         this.size = this.readSize(var2);
         this.bytesUploaded = var2.getUInt64();
         this.requests = var2.getInt32();
         this.readUIDs(var2);
         this.readSubFiles(var2);
         this.readMagic(var2);
         this.parseName();
      }
   }

   protected void readMagic(MessageBuffer var1) {
   }

   public void read(MessageBuffer var1) {
      this.read(var1.getInt32(), var1);
   }

   protected void readUIDs(MessageBuffer var1) {
      this.md4 = var1.getMd4();
   }

   protected long readSize(MessageBuffer var1) {
      return (long)var1.getInt32() & 4294967295L;
   }

   public boolean readUpdate(int var1, MessageBuffer var2) {
      long var3 = this.getBytesUploaded();
      int var5 = this.getRequests();
      this.read(var1, var2);
      return var3 != this.getBytesUploaded() || var5 != this.getRequests();
   }

   protected EnumNetwork readNetworkEnum(MessageBuffer var1) {
      return this.core.getNetworkCollection().getNetworkEnum(var1.getInt32());
   }

   public boolean upload(int var1, MessageBuffer var2) {
      long var3 = this.getBytesUploaded();
      int var5 = this.getRequests();
      synchronized (this) {
         this.bytesUploaded = var2.getUInt64();
         this.requests = var2.getInt32();
      }

      return var3 != this.getBytesUploaded() || var5 != this.getRequests();
   }

   public void computeTorrent(String var1, String var2) {
      Network var3 = this.core.getNetworkCollection().getByEnum(EnumNetwork.BT);
      if (var3 != null) {
         byte[] var4 = new byte[]{-1, -1};
         Object[] var5 = new Object[]{
            Integer.valueOf(var3.getId()),
            var4,
            Integer.valueOf(SwissArmy.UTF8Length(var2) + 2 + SwissArmy.UTF8Length(var1) + 2 + 6),
            Short.valueOf((short)1),
            Integer.valueOf(this.getId()),
            var1,
            var2
         };
         this.core.send((short)63, var5);
      }
   }

   static {
      try {
         pathRE = new RE("[^/]*$");
      } catch (REException var1) {
         Sancho.pDebug("SharedFile: " + var1);
      }
   }
}
