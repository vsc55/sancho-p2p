package sancho.model.mldonkey;

import org.eclipse.swt.graphics.Image;
import sancho.core.ICore;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.NetworkStatCollection;
import sancho.utility.SwissArmy;
import sancho.view.utility.SResources;

public class Network extends AObject {
   private static final String RS_CONNECTED_TO = SResources.getString("sl.n.connectedTo");
   private static final String RS_ENABLED = SResources.getString("sl.n.enabled");
   private static final String RS_DISABLED = SResources.getString("sl.n.disabled");
   private static final String S_CONNECTED = "connected";
   private static final String S_DISABLED = "disabled";
   private static final String S_DISCONNECTED = "disconnected";
   private static final String S_BAD_CONNECT = "badconnect";
   protected String configFile;
   protected long downloaded;
   private boolean enabled;
   private EnumNetwork enumNetwork;
   protected int id;
   protected String name;
   protected long uploaded;

   Network(ICore var1) {
      super(var1);
   }

   public void getStats() {
   }

   public boolean equals(EnumNetwork var1) {
      return this.getEnumNetwork() == var1;
   }

   public boolean equals(Object var1) {
      return var1 instanceof Network && this.getId() == ((Network)var1).getId();
   }

   public synchronized String getConfigFile() {
      return this.configFile != null ? this.configFile : "";
   }

   public synchronized long getDownloaded() {
      return this.downloaded;
   }

   public synchronized EnumNetwork getEnumNetwork() {
      return this.enumNetwork;
   }

   public synchronized int getId() {
      return this.id;
   }

   public synchronized Image getImage() {
      if (!this.isEnabled()) {
         return this.enumNetwork.getImage("disabled");
      } else if (this.core.getProtocol() >= 18 && !this.isVirtual() && (this.hasServers() || this.hasSupernodes())) {
         int var1 = this.core.getOptionCollection().getMaxConnected(this);
         int var2 = this.numConnectedServers();
         return (var2 < 1 || this.enumNetwork != EnumNetwork.DC) && var2 < var1
            ? this.enumNetwork.getImage(var2 == 0 ? "disconnected" : "badconnect")
            : this.enumNetwork.getImage("connected");
      } else {
         return this.enumNetwork.getImage("connected");
      }
   }

   public String getName() {
      return this.name != null ? this.name : "";
   }

   public String getToolTip() {
      StringBuffer var1 = new StringBuffer(256);
      if (!this.isEnabled() || !this.hasServers() && !this.hasSupernodes()) {
         if (this.isVirtual()) {
            return this.getName();
         } else {
            var1.append(this.getName());
            var1.append(" ");
            var1.append(this.isEnabled() ? RS_ENABLED : RS_DISABLED);
            return var1.toString();
         }
      } else {
         var1.append(this.getName());
         var1.append(" ");
         var1.append(RS_CONNECTED_TO);
         var1.append(this.numConnectedServers());
         return var1.toString();
      }
   }

   public synchronized long getUploaded() {
      return this.uploaded;
   }

   public synchronized String getUploadedString() {
      return SwissArmy.calcStringSize(this.uploaded);
   }

   public synchronized String getDownloadedString() {
      return SwissArmy.calcStringSize(this.downloaded);
   }

   public synchronized String toString() {
      StringBuffer var1 = new StringBuffer(50);
      var1.append(this.getName());
      var1.append(": U:");
      var1.append(this.getUploadedString());
      var1.append(" D:");
      var1.append(this.getDownloadedString());
      var1.append(" (");
      var1.append(SResources.getString(this.isEnabled() ? "stats.enabled" : "stats.disabled"));
      var1.append(")");
      return var1.toString();
   }

   public synchronized boolean hasChat() {
      return this.enumNetwork == EnumNetwork.DONKEY || this.enumNetwork == EnumNetwork.OV;
   }

   public int hashCode() {
      return this.getId();
   }

   public synchronized boolean hasRooms() {
      return this.enumNetwork == EnumNetwork.SOULSEEK || this.enumNetwork == EnumNetwork.DC;
   }

   public synchronized boolean hasServers() {
      return this.enumNetwork != EnumNetwork.BT && this.enumNetwork != EnumNetwork.FT && this.enumNetwork != EnumNetwork.GNUT;
   }

   public synchronized boolean hasSupernodes() {
      return this.enumNetwork == EnumNetwork.FT || this.enumNetwork == EnumNetwork.GNUT || this.enumNetwork == EnumNetwork.GNUT2;
   }

   public synchronized boolean hasUpload() {
      return this.enumNetwork == EnumNetwork.DONKEY
         || this.enumNetwork == EnumNetwork.OV
         || this.enumNetwork == EnumNetwork.BT
         || this.enumNetwork == EnumNetwork.GNUT
         || this.enumNetwork == EnumNetwork.DC;
   }

   public synchronized boolean isEnabled() {
      return this.enabled;
   }

   public boolean isMultinet() {
      return false;
   }

   public synchronized boolean isSearchable() {
      return this.enumNetwork != EnumNetwork.BT;
   }

   public boolean isVirtual() {
      return false;
   }

   public int numConnectedServers() {
      return this.core.getServerCollection().getConnected(this.getEnumNetwork());
   }

   public void read(int var1, MessageBuffer var2) {
      boolean var3 = this.isEnabled();
      synchronized (this) {
         this.id = var1;
         this.name = var2.getString();
         this.enabled = var2.getBool();
         this.configFile = var2.getString();
         this.uploaded = var2.getUInt64();
         this.downloaded = var2.getUInt64();
         this.enumNetwork = EnumNetwork.stringToEnum(this.name);
      }

      if (!this.isEnabled() && var3) {
         this.core.getServerCollection().removeAll(this.getEnumNetwork());
      }
   }

   public void read(MessageBuffer var1) {
      this.read(var1.getInt32(), var1);
   }

   protected void setConnectedServers(int var1) {
   }

   public NetworkStatCollection[] getNetworkStatCollection() {
      return null;
   }

   public void toggleEnabled() {
      Object[] var1 = new Object[]{Integer.valueOf(this.getId()), Byte.valueOf((byte)(this.isEnabled() ? 0 : 1))};
      this.core.send((short)40, var1);
   }

   public void readStats(MessageBuffer var1) {
   }
}
