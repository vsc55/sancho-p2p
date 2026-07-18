package sancho.model.mldonkey;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import sancho.core.CoreFactory;
import sancho.core.ICore;
import sancho.core.Sancho;
import sancho.model.mldonkey.enums.AbstractEnum;
import sancho.model.mldonkey.enums.EnumExtension;
import sancho.model.mldonkey.enums.EnumFileState;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.model.mldonkey.enums.EnumPriority;
import sancho.model.mldonkey.utility.FileComment;
import sancho.model.mldonkey.utility.FileState;
import sancho.model.mldonkey.utility.Format;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.UtilityFactory;
import sancho.utility.MyObservable;
import sancho.utility.MyObserver;
import sancho.utility.ObjectMap;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.transfer.FileClient;
import sancho.view.utility.SResources;

public class File extends AObjectO implements MyObserver, IObject_UID, IPreview {
   public static final int CHANGED_ACTIVE = 1;
   public static final int CHANGED_RAVAIL = 2;
   public static final int CHANGED_DOWNLOADED = 4;
   public static final int CHANGED_ETA = 8;
   public static final int CHANGED_LAST = 16;
   public static final int CHANGED_PERCENT = 32;
   public static final int CHANGED_RATE = 64;
   public static final int CHANGED_STATE = 128;
   public static final int CHANGED_NOT_INTERESTING = 256;
   public static final int CHANGED_SOURCES = 512;
   public static final int CHANGED_AVAIL = 1024;
   public static final int CHANGED_COMMENTS = 2048;
   private int changedBits;
   protected int activeSources;
   protected long age;
   protected long ageTS;
   protected String avail;
   protected int[] chunkAges;
   protected String chunks;
   protected ObjectMap clientWeakMap;
   protected long downloaded;
   protected String downloadedString;
   protected long etaSeconds;
   protected long eta2Seconds;
   protected String etaString;
   protected String eta2String;
   protected EnumExtension extensionEnum;
   protected Set fileClientSet;
   protected EnumFileState fileStateEnum;
   protected Format format;
   protected int id;
   protected int lastSeen;
   protected byte[] md4;
   protected String name;
   protected String[] names;
   protected EnumNetwork networkEnum;
   protected int numChunks;
   protected int numClients;
   protected int numConnectedClients;
   protected float percent;
   protected int priority;
   protected EnumPriority priorityEnum;
   protected float rate;
   protected int relativeAvail;
   protected long size;
   protected int sources;
   protected boolean containsFake;
   protected FileState state;

   File(ICore var1) {
      super(var1);
      this.state = UtilityFactory.getFileState(var1);
      this.format = UtilityFactory.getFormat(var1);
      this.clientWeakMap = new ObjectMap(true);
   }

   protected Set getFileClientSet() {
      if (this.fileClientSet == null) {
         this.fileClientSet = Collections.synchronizedSet(new HashSet());
      }

      return this.fileClientSet;
   }

   public synchronized void addChangedBits(int var1) {
      this.changedBits |= var1;
   }

   public synchronized void removeChangedBits(int var1) {
      this.changedBits &= ~var1;
   }

   public synchronized int getChangedBits() {
      return this.changedBits;
   }

   public synchronized void clearChangedBits() {
      this.changedBits = 0;
   }

   public synchronized boolean hasChangedBit(int var1) {
      return (this.changedBits & var1) != 0;
   }

   public String[] getSubFileNames() {
      return null;
   }

   public String[] getSubFileMagics() {
      return null;
   }

   public long[] getSubFileSizes() {
      return null;
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

   protected void addFileClient(FileClient var1) {
      this.getFileClientSet().add(var1);
      this.addChangedBits(512);
      this.notifyChangedProperties();
   }

   public void addSource(Client var1) {
      if (!this.clientWeakMap.contains(var1)) {
         this.clientWeakMap.add(var1);
         var1.addObserver(this);
         this.addChangedBits(512);
         if (var1.isTransferring()) {
            this.numConnectedClients++;
            this.setActiveSources(1);
            if (this.findFileClient(var1) == null) {
               this.addClientToFileClientSet(var1);
            }
         } else if (var1.isConnected()) {
            this.numConnectedClients++;
         }

         this.notifyChangedProperties();
      }
   }

   public int getNumComments() {
      return 0;
   }

   public synchronized String getNumCommentsString() {
      return String.valueOf(this.getNumComments());
   }

   public void addClientToFileClientSet(Client var1) {
      FileClient var2 = new FileClient(this, var1);
      this.addFileClient(var2);
      this.core.getFileCollection().sendUpdate(var2, 1);
   }

   protected void calcDownloadedString() {
      String var1 = this.getDownloadedString();
      this.downloadedString = SwissArmy.calcStringSize(this.getDownloaded());
      if (!var1.equals(this.downloadedString)) {
         this.addChangedBits(4);
      }

      String var2 = this.getPercentString();
      long var3 = this.getSize();
      this.percent = var3 > 0L ? (float)this.getDownloaded() / (float)var3 * 100.0F : 0.0F;
      if (!var2.equals(this.getPercentString())) {
         this.addChangedBits(32);
      }
   }

   protected void calcETA() {
      if (this.rate == 0.0F) {
         this.etaSeconds = Long.MAX_VALUE;
      } else {
         this.etaSeconds = (long)((float)(this.getSize() - this.getDownloaded()) / (this.getRate() + 1.0F));
      }

      String var1 = this.getEtaString();
      EnumFileState var2 = this.getFileStateEnum();
      boolean var3 = this.core.getFileCollection().eta2();
      if (var2 != EnumFileState.QUEUED && var2 != EnumFileState.DOWNLOADED && var2 != EnumFileState.PAUSED) {
         this.etaString = SwissArmy.calcStringOfSeconds(this.etaSeconds);
         if (var3) {
            long var4 = this.getDownloaded();
            long var6 = this.getSize() - var4;
            if (var4 != 0L && var6 != 0L && this.getAge() != 0L) {
               if (this.ageTS == 0L) {
                  this.ageTS = System.currentTimeMillis();
               }

               long var8 = this.getAge() + (System.currentTimeMillis() - this.ageTS) / 1000L;
               this.eta2Seconds = var6 * var8 / var4;
               this.eta2String = SwissArmy.calcStringOfSeconds(this.eta2Seconds);
            } else {
               this.eta2String = "-";
               this.eta2Seconds = Long.MAX_VALUE;
            }
         }
      } else {
         this.etaString = "-";
         if (var3) {
            this.eta2Seconds = Long.MAX_VALUE;
            this.eta2String = "-";
         }
      }

      if (this.etaString.equals("")) {
         this.etaString = "-";
      }

      if (!var1.equals(this.etaString)) {
         this.addChangedBits(8);
      }
   }

   public void connectAll() {
      this.core.send((short)20, Integer.valueOf(this.getId()));
   }

   public synchronized boolean containsFake() {
      return this.containsFake;
   }

   public void dispose() {
      this.clientWeakMap.deleteObservers();
      this.deleteObservers();
   }

   public boolean equals(Object var1) {
      return var1 instanceof File && this.getId() == ((File)var1).getId();
   }

   public synchronized FileClient findFileClient(Client var1) {
      for (Object var3o : this.getFileClientSet()) { FileClient var3 = (FileClient)var3o;
         if (var1 == var3.getClient()) {
            return var3;
         }
      }

      return null;
   }

   public synchronized int getActiveSources() {
      return this.activeSources;
   }

   public synchronized long getAge() {
      return this.age;
   }

   public synchronized String getAgeString() {
      return SwissArmy.calcStringOfSeconds(System.currentTimeMillis() / 1000L - this.getAge());
   }

   public synchronized Network[] getAllAvailNetworks() {
      return new Network[0];
   }

   public synchronized String getAvail() {
      return this.avail;
   }

   public String getAvails(Network var1) {
      return "";
   }

   public synchronized int[] getChunkAges() {
      if (this.chunkAges == null) {
         return null;
      } else {
         int[] var1 = new int[this.chunkAges.length];
         System.arraycopy(this.chunkAges, 0, var1, 0, this.chunkAges.length);
         return var1;
      }
   }

   public synchronized String getChunks() {
      return this.chunks != null ? this.chunks : "";
   }

   public ObjectMap getClientWeakMap() {
      return this.clientWeakMap;
   }

   public String getComment() {
      return "";
   }

   public synchronized int getConnected() {
      return this.numConnectedClients;
   }

   public synchronized long getDownloaded() {
      return this.downloaded;
   }

   public synchronized long getRemaining() {
      return this.getSize() - this.getDownloaded();
   }

   public synchronized String getDownloadedString() {
      return this.downloadedString != null ? this.downloadedString : "";
   }

   public synchronized String getRemainingString() {
      return SwissArmy.calcStringSize(this.getRemaining());
   }

   public synchronized EnumNetwork getEnumNetwork() {
      return this.networkEnum;
   }

   public String getUser() {
      return "";
   }

   public String getGroup() {
      return "";
   }

   public synchronized Program getOSPreviewApp() {
      return SResources.getOSPreviewApp(this.getExtension());
   }

   public synchronized String getExtension() {
      int var1 = this.getName().lastIndexOf(".");
      return var1 != -1 ? this.getName().substring(var1 + 1).toLowerCase().intern() : "";
   }

   public String getED2K() {
      return "ed2k://|file|" + this.getName() + "|" + this.getSize() + "|" + this.getMD4() + "|/";
   }

   public synchronized long getETA() {
      return this.etaSeconds;
   }

   public synchronized long getETA2() {
      return this.eta2Seconds;
   }

   public synchronized String getEtaString() {
      return this.etaString != null ? this.etaString : "";
   }

   public synchronized String getEta2String() {
      return this.eta2String != null ? this.eta2String : "";
   }

   public synchronized int getFileClientSetSize() {
      return this.fileClientSet == null ? 0 : this.getFileClientSet().size();
   }

   public synchronized Object[] getFileClientSetArray() {
      return SwissArmy.toArray(this.getFileClientSet());
   }

   public String getFileFormat() {
      return this.getExtension();
   }

   public void getFileLocations() {
      this.core.send((short)34, Integer.valueOf(this.getId()));
   }

   public synchronized EnumFileState getFileStateEnum() {
      return this.fileStateEnum;
   }

   public synchronized EnumExtension getFileType() {
      return this.extensionEnum != null ? this.extensionEnum : EnumExtension.UNKNOWN;
   }

   public Format getFormat() {
      return this.format;
   }

   public synchronized int getId() {
      return this.id;
   }

   public synchronized int getLastSeen() {
      return this.lastSeen;
   }

   public synchronized String getLastSeenString() {
      return this.lastSeen == 8640000 ? "-" : SwissArmy.calcStringOfSeconds((long)this.lastSeen);
   }

   public synchronized String getHash() {
      return this.getMD4();
   }

   public String getMagic() {
      return "";
   }

   public boolean hasFileComments() {
      return false;
   }

   public FileComment[] getFileComments() {
      return null;
   }

   public synchronized String getMD4() {
      return SwissArmy.calcStringOfMD4(this.md4);
   }

   public synchronized String getName() {
      if (this.name == null) {
         return "";
      } else {
         EnumNetwork var1 = this.getEnumNetwork();
         if (var1 != EnumNetwork.GNUT && var1 != EnumNetwork.GNUT2) {
            return this.name;
         } else {
            String var2 = String.valueOf('\u0000');
            return SwissArmy.replaceAll(this.name, var2, "");
         }
      }
   }

   public synchronized String[] getNames() {
      return this.names != null ? this.names : new String[0];
   }

   public synchronized int getNumChunks() {
      return this.numChunks;
   }

   public synchronized int getNumClients() {
      return this.numClients;
   }

   public synchronized int getNumSources() {
      return this.sources;
   }

   public synchronized float getPercent() {
      return this.percent;
   }

   public synchronized int getPriority() {
      return this.priority;
   }

   public synchronized AbstractEnum getPriorityEnum() {
      return this.priorityEnum;
   }

   public synchronized Image getFileTypeImage() {
      return SResources.getFileTypeImage(this.getFileFormat());
   }

   public synchronized Image getPriorityImage() {
      if (this.priority == 0) {
         return SResources.getImage("prioritynormal");
      } else {
         return this.priority > 0 ? SResources.getImage("priorityhigh") : SResources.getImage("prioritylow");
      }
   }

   public synchronized ImageDescriptor getFileTypeImageDescriptor() {
      return SResources.getFileTypeImageDescriptor(this.getFileFormat());
   }

   public synchronized float getRate() {
      return this.rate;
   }

   public synchronized String getRateString() {
      EnumFileState var1 = this.getFileStateEnum();
      if (var1 != EnumFileState.PAUSED && var1 != EnumFileState.QUEUED && var1 != EnumFileState.DOWNLOADED) {
         return this.getRate() == 0.0F ? "-" : SwissArmy.rateToString(this.getRate());
      } else {
         return var1.getName();
      }
   }

   public synchronized int getRelativeAvail() {
      return this.relativeAvail;
   }

   public int getAvgRating() {
      return -1;
   }

   public synchronized Image getCommentImage() {
      int var1 = this.getAvgRating();
      if (var1 < 0) {
         return SResources.getImage("blank-16");
      } else {
         switch (var1) {
            case 1:
               return SResources.getImage("filerating1");
            case 2:
               return SResources.getImage("filerating2");
            case 3:
               return SResources.getImage("filerating3");
            case 4:
               return SResources.getImage("filerating4");
            case 5:
               return SResources.getImage("filerating5");
            default:
               return SResources.getImage("filerating0");
         }
      }
   }

   public synchronized Image getAvgRatingImage() {
      int var1 = this.getAvgRating();
      if (var1 < 0) {
         return null;
      } else {
         switch (var1) {
            case 1:
               return SResources.getImage("dotrating1");
            case 2:
               return SResources.getImage("dotrating2");
            case 3:
               return SResources.getImage("dotrating3");
            case 4:
               return SResources.getImage("dotrating4");
            case 5:
               return SResources.getImage("dotrating5");
            default:
               return SResources.getImage("dotrating0");
         }
      }
   }

   public synchronized String getRelativeAvailString() {
      StringBuffer var1 = new StringBuffer();
      var1.append(this.relativeAvail);
      var1.append("%");
      return var1.toString().intern();
   }

   public synchronized long getSize() {
      return this.size;
   }

   public synchronized String getSizeString() {
      return SwissArmy.calcStringSize(this.getSize());
   }

   public int getSources() {
      return this.clientWeakMap.size();
   }

   public String getSourcesString() {
      int var1 = this.getSources();
      if (var1 == 0) {
         return "-";
      } else {
         int var2 = this.getActiveSources();
         return var2 > 0 ? var1 + "(" + var2 + ")" : String.valueOf(var1).intern();
      }
   }

   public synchronized String getPercentString() {
      return SwissArmy.percentToString(this.getPercent());
   }

   public synchronized String getPriorityString() {
      if (this.core.getFileCollection().verboseNumbers) {
         return this.priority == 0 ? this.priorityEnum.getName() : this.priorityEnum.getName() + "(" + this.priority + ")";
      } else {
         return String.valueOf(this.priority);
      }
   }

   public synchronized String[] getUIDs() {
      return null;
   }

   public boolean hasAvails() {
      return false;
   }

   public int hashCode() {
      return this.getId();
   }

   public boolean isActive() {
      EnumFileState var1 = this.getFileStateEnum();
      return var1 == EnumFileState.DOWNLOADING || var1 == EnumFileState.PAUSED || var1 == EnumFileState.QUEUED;
   }

   public boolean isInteresting() {
      EnumFileState var1 = this.getFileStateEnum();
      return var1 == EnumFileState.DOWNLOADING || var1 == EnumFileState.PAUSED || var1 == EnumFileState.DOWNLOADED || var1 == EnumFileState.QUEUED;
   }

   public void manualClean() {
      synchronized (this.clientWeakMap) {
         Object[] var2 = this.clientWeakMap.getKeyArray();

         for (int var3 = 0; var3 < var2.length; var3++) {
            Client var4 = (Client)var2[var3];
            if (!this.core.getClientCollection().containsKey(var4.getId())) {
               this.clientWeakMap.removeFromMain(var4);
            }
         }
      }
   }

   public void notifyChangedProperties() {
      this.setChanged();
      this.notifyObservers();
      this.removeChangedBits(1024);
   }

   public String preview(int var1) {
      return this.preview((String)null, var1);
   }

   protected String getTempFileName(int var1) {
      String var2 = PreferenceLoader.loadString("previewWorkingDirectory");
      if (var2 != null && !var2.equals("")) {
         if (!var2.endsWith(java.io.File.separator)) {
            var2 = var2 + java.io.File.separator;
         }

         String[] var3 = new String[]{"ed2k", "bitprint", "sha1", "ttr", "md5", "sig2dat", "bt"};

         for (int var4 = 0; var4 < var3.length; var4++) {
            String var5 = "urn_" + var3[var4] + "_" + this.getHash().toUpperCase();
            java.io.File var6 = new java.io.File(var2 + var5);
            if (var6.exists()) {
               return var5;
            }
         }
      }

      String var8 = this.getEnumNetwork().getTempFilePrefix() + this.getHash().toUpperCase();
      String[] var9 = this.getSubFileNames();
      if (var1 >= 0 && var9 != null && var9.length > var1) {
         String var10 = var8 + java.io.File.separator + var9[var1];
         String var11 = var2 + var10;
         java.io.File var7 = new java.io.File(var11);
         if (var7.exists()) {
            return var10;
         }
      }

      return var8;
   }

   public String preview(Program var1, int var2) {
      String var3 = PreferenceLoader.loadString("previewWorkingDirectory");
      String var4 = this.getTempFileName(var2);
      if (!var3.equals("") && !var3.endsWith(java.io.File.separator)) {
         var3 = var3 + java.io.File.separator;
      }

      var4 = this.repSep(var4);
      if (var4.startsWith(java.io.File.separator)) {
         var4 = var4.substring(1);
      }

      String var5 = var3 + var4;
      var1.execute(var5);
      return var5;
   }

   private String repSep(String var1) {
      String var2 = java.io.File.separator;
      if (var2.equals("/") && var1.indexOf("\\") != -1) {
         var1 = SwissArmy.replaceAll(var1, "\\", "/");
      }

      if (var2.equals("\\") && var1.indexOf("/") != -1) {
         var1 = SwissArmy.replaceAll(var1, "/", "\\");
      }

      return var1;
   }

   public String getPreviewURL() {
      CoreFactory var1 = Sancho.getCoreFactory();
      String var2 = "";
      if (!var1.getPassword().equals("")) {
         var2 = var1.getUsername() + ":" + var1.getPassword() + "@";
      }

      return "http://" + var2 + var1.getHostname() + ":" + var1.getHTTPPort() + "/preview_download?q=" + this.getId();
   }

   public String preview(String var1, int var2) {
      String var3 = PreferenceLoader.loadString("previewExecutable");
      String var4 = PreferenceLoader.loadString("previewWorkingDirectory");
      boolean var5 = PreferenceLoader.loadBoolean("previewUseHttp");
      String var6 = PreferenceLoader.loadString("previewExtensions");
      if (!var6.equals("")) {
         StringTokenizer var7 = new StringTokenizer(var6, ";");
         String var8 = "";
         String var9 = "";

         while (var7.hasMoreTokens()) {
            var8 = var7.nextToken();
            if (var7.hasMoreTokens()) {
               var9 = var7.nextToken();
               if (this.getName().toLowerCase().endsWith(var8.toLowerCase())) {
                  var3 = var9;
               }
            }
         }
      }

      if (var1 != null) {
         var3 = var1;
      }

      String var12 = this.getPreviewURL();
      String var14 = this.getTempFileName(var2);
      int var16 = 0;
      if (var14.indexOf("/") != -1) {
         var16 = var14.lastIndexOf("/");
      }

      if (var14.indexOf("\\") != -1) {
         int var10 = var14.lastIndexOf("\\");
         var16 = Math.max(var10, var16);
      }

      if (var16 > 0) {
         String var17 = var14.substring(0, var16);
         var17 = this.repSep(var17);
         String var11 = var14.substring(var16);
         if (var11.length() > 0) {
            var11 = var11.substring(1);
         }

         if (!var4.equals("") && !var4.endsWith(java.io.File.separator)) {
            var4 = var4 + java.io.File.separator;
         }

         var4 = var4 + var17;
         var14 = var11;
      }

      String var19 = "";
      if (var3.equals("")) {
         this.core.send((short)30, Integer.valueOf(this.getId()));
      } else {
         String[] var21 = new String[]{var3, var5 ? var12 : var14};
         if (!var5) {
            var19 = "";
            if (!var4.equals("")) {
               var19 = var19 + var4;
               if (!var4.endsWith(java.io.File.separator)) {
                  var19 = var19 + java.io.File.separator;
               }
            }

            var19 = var19 + var14;
         }

         SwissArmy.execInThread(var21, var4.equals("") ? null : var4);
      }

      return var19;
   }

   public void read(int var1, MessageBuffer var2) {
      synchronized (this) {
         this.id = var1;
         this.networkEnum = this.readNetwork(var2.getInt32());
         this.names = var2.getStringList();
         this.md4 = var2.getMd4();
         this.size = this.readSize(var2);
         this.downloaded = this.readDownloaded(var2);
         this.calcDownloadedString();
         this.sources = var2.getInt32();
         this.numClients = var2.getInt32();
         this.readState(var2);
         this.setChunks(var2.getString(false));
         this.readAvailability(var2);
         this.readRate(var2);
         this.chunkAges = this.readChunkAges(var2);
         this.age = this.readAge(var2);
         this.getFormat().read(var2);
         this.name = var2.getString();
         this.setLastSeen(var2.getInt32());
         this.setPriority(var2.getSignedInt32());
         this.readComment(var2);
         this.readUIDs(var2);
         this.readSubFiles(var2);
         this.readMagic(var2);
         this.readFileComments(var2);
         this.readUser(var2);
         this.readGroup(var2);
         this.calcETA();
         this.calcFileType();
         this.fakeCheck();
      }

      if ((this.getChangedBits() & 128) != 0 && this.state.getState() == EnumFileState.DOWNLOADED) {
         Sancho.getCoreFactory().notifyObject(this);
      }

      this.notifyChangedProperties();
   }

   protected void readUser(MessageBuffer var1) {
   }

   protected void readGroup(MessageBuffer var1) {
   }

   protected void readSubFiles(MessageBuffer var1) {
   }

   protected void readMagic(MessageBuffer var1) {
   }

   protected void readFileComments(MessageBuffer var1) {
   }

   protected void fakeCheck() {
      if (!this.containsFake) {
         this.containsFake = SwissArmy.containsFake(this.name);
      }

      if (!this.containsFake) {
         this.containsFake = SwissArmy.containsFake(this.getComment());
      }

      if (!this.containsFake) {
         for (int var1 = 0; var1 < this.names.length; var1++) {
            this.containsFake = SwissArmy.containsFake(this.names[var1]);
            if (this.containsFake) {
               break;
            }
         }
      }
   }

   protected void readUIDs(MessageBuffer var1) {
   }

   public void read(MessageBuffer var1) {
      this.read(var1.getInt32(), var1);
   }

   protected long readAge(MessageBuffer var1) {
      if (this.core.getFileCollection().eta2()) {
         this.ageTS = System.currentTimeMillis();
      }

      try {
         return Long.parseLong(var1.getString(false));
      } catch (NumberFormatException var3) {
         return 0L;
      }
   }

   protected int[] readChunkAges(MessageBuffer var1) {
      String[] var2 = var1.getStringList(false);
      int[] var3 = new int[var2.length];

      for (int var4 = 0; var4 < var2.length; var4++) {
         var3[var4] = (int)(System.currentTimeMillis() / 1000L) - Integer.parseInt(var2[var4]);
      }

      return var3;
   }

   protected void readComment(MessageBuffer var1) {
   }

   protected long readDownloaded(MessageBuffer var1) {
      return (long)var1.getInt32() & 4294967295L;
   }

   protected void readRate(MessageBuffer var1) {
      float var2 = var1.getFloat();
      if (this.rate != var2) {
         this.addChangedBits(64);
      }

      this.rate = var2;
   }

   protected long readSize(MessageBuffer var1) {
      return (long)var1.getInt32() & 4294967295L;
   }

   protected void readState(MessageBuffer var1) {
      boolean var2 = this.isInteresting();
      EnumFileState var3 = this.getFileStateEnum();
      this.state.read(var1);
      this.fileStateEnum = this.state.getState();
      if (var2 && !this.isInteresting()) {
         this.addChangedBits(256);
      }

      if (var3 != this.getFileStateEnum()) {
         this.addChangedBits(128);
         if (this.state.getState() == EnumFileState.DOWNLOADED
            && PreferenceLoader.loadBoolean("downloadCompleteLog")
            && this.getName() != null
            && !this.getName().equals("")) {
            try {
               PrintWriter var4 = new PrintWriter(new BufferedWriter(new FileWriter(new java.io.File(VersionInfo.getDownloadLogFile()), true)));
               var4.write(System.currentTimeMillis() + " " + this.getED2K() + "\n");
               var4.close();
            } catch (FileNotFoundException var6) {
               Sancho.pDebug(var6.toString());
            } catch (IOException var7) {
               Sancho.pDebug(var7.toString());
            }
         }
      }
   }

   public void readUpdate(MessageBuffer var1) {
      synchronized (this) {
         this.downloaded = this.readDownloaded(var1);
         this.calcDownloadedString();
         this.readRate(var1);
         this.setLastSeen(var1.getInt32());
         this.calcETA();
      }

      this.notifyChangedProperties();
   }

   public FileClient removeFileClient(Client var1) {
      FileClient var2 = this.findFileClient(var1);
      if (var2 != null) {
         this.getFileClientSet().remove(var2);
         this.addChangedBits(512);
         this.notifyChangedProperties();
      }

      return var2;
   }

   public void removeSource(MessageBuffer var1) {
      int var2 = var1.getInt32();
      Client var3 = (Client)this.core.getClientCollection().get(var2);
      if (var3 != null) {
         this.clientWeakMap.remove(var3);
         var3.deleteObserver(this);
         if (var3.countObservers() == 0) {
            if (var3.isConnected() && this.numConnectedClients > 0) {
               this.numConnectedClients--;
            }

            this.core.getClientCollection().removeSource(var2, var3);
         }

         this.addChangedBits(512);
         this.notifyChangedProperties();
      }
   }

   public void saveFileAs(String var1) {
      Object[] var2 = new Object[]{Integer.valueOf(this.getId()), var1};
      this.core.send((short)13, var2);
   }

   public void sendPriority(boolean var1, int var2) {
      Object[] var3 = new Object[]{Integer.valueOf(this.getId()), null};
      if (var1) {
         var2 += this.getPriority();
      }

      var3[1] = Integer.valueOf(var2);
      this.core.send((short)51, var3);
   }

   public void sendPriority(EnumPriority var1) {
      this.sendPriority(false, var1.getMaxValue());
   }

   public void setActiveSources(int var1) {
      int var2 = this.getActiveSources();
      synchronized (this) {
         if (var1 == 0) {
            this.activeSources = 0;
            Object[] var4 = this.clientWeakMap.getKeyArray();

            for (int var5 = 0; var5 < var4.length; var5++) {
               Client var6 = (Client)var4[var5];
               if (var6.isTransferring()) {
                  this.activeSources++;
               }
            }
         } else {
            this.activeSources += var1;
         }
      }

      if (var2 != this.getActiveSources()) {
         this.addChangedBits(1);
         this.notifyChangedProperties();
      }
   }

   protected void readAvailability(MessageBuffer var1) {
      this.avail = var1.getString(false);
      this.setRelativeAvail();
   }

   protected void setChunks(String var1) {
      this.chunks = var1;
      this.numChunks = 0;

      for (int var3 = 0; var3 < this.chunks.length(); var3++) {
         char var2 = this.chunks.charAt(var3);
         if (var2 == '2' || var2 == '3') {
            this.numChunks++;
         }
      }
   }

   public void setComment(String var1) {
      String var2 = "comment " + this.getMD4() + " \"" + var1 + "\"";
      this.core.send((short)29, var2);
      this.core.send((short)37, Integer.valueOf(this.getId()));
   }

   public void requestFileInfo() {
      this.core.send((short)37, Integer.valueOf(this.getId()));
   }

   public void chown(String var1) {
   }

   public void chgrp(String var1) {
   }

   protected void calcFileType() {
      int var1 = this.getName().lastIndexOf(".");
      String var2 = this.name.substring(var1 + 1, this.name.length());
      EnumExtension var3 = EnumExtension.GET_EXT(var2);
      this.extensionEnum = var3 != null ? var3 : EnumExtension.UNKNOWN;
   }

   protected void setLastSeen(int var1) {
      int var2 = this.getLastSeen();
      this.lastSeen = var1;
      if (var2 != this.lastSeen) {
         this.addChangedBits(16);
      }
   }

   public void rename(String var1) {
      var1 = "rename " + this.getId() + " \"" + var1 + "\"";
      this.core.send((short)29, var1);
      this.core.send((short)37, Integer.valueOf(this.id));
   }

   protected EnumNetwork readNetwork(int var1) {
      return this.core.getNetworkCollection().getNetworkEnum(var1);
   }

   protected void setPriority(int var1) {
      this.priority = var1;
      this.priorityEnum = EnumPriority.intToEnum(var1);
   }

   public void setRelativeAvail() {
      int var1 = this.relativeAvail;
      this.relativeAvail = 0;
      int var2 = 0;
      int var3 = 0;
      if (this.avail == null) {
         this.avail = "";
      }

      String var4 = this.getChunks();
      if (this.avail.length() > 0 && this.avail.length() == var4.length()) {
         for (int var5 = 0; var5 < this.avail.length(); var5++) {
            if (var4.charAt(var5) == '0' || var4.charAt(var5) == '1') {
               var2++;
               if (this.avail.charAt(var5) > 0) {
                  var3++;
               }
            }
         }

         if (var2 > 0) {
            this.relativeAvail = (int)((float)var3 / (float)var2 * 100.0F);
         }
      }

      if (var1 != this.relativeAvail) {
         this.addChangedBits(2);
      }
   }

   public void setState(EnumFileState var1) {
      EnumFileState var2 = this.getFileStateEnum();
      Object[] var3 = new Object[2];
      byte var4 = 23;
      var3[0] = Integer.valueOf(this.id);
      if (var2 == EnumFileState.PAUSED && var1 == EnumFileState.DOWNLOADING) {
         var3[1] = Byte.valueOf((byte)1);
      } else if ((var2 == EnumFileState.DOWNLOADING || var2 == EnumFileState.QUEUED) && var1 == EnumFileState.PAUSED) {
         var3[1] = Byte.valueOf((byte)0);
      } else {
         if (var1 != EnumFileState.CANCELLED) {
            return;
         }

         var4 = 11;
         var3 = new Object[]{Integer.valueOf(this.id)};
      }

      this.core.send(var4, var3);
   }

   protected boolean checkFileNum(Client var1) {
      return true;
   }

   public void update(MyObservable var1, Object var2, int var3) {
      if (var1 instanceof Client) {
         if (var2 == null) {
            Client var4 = (Client)var1;
            if ((var3 & 4) != 0) {
               if (this.checkFileNum(var4)) {
                  this.setActiveSources(1);
                  FileClient var5 = this.findFileClient(var4);
                  byte var6 = 0;
                  if (var5 == null) {
                     var5 = new FileClient(this, var4);
                     var6 = 1;
                     this.addFileClient(var5);
                  }

                  this.core.getFileCollection().sendUpdate(var5, var6);
               }
            } else {
               FileClient var7;
               if ((var3 & 8) != 0 && (var7 = this.removeFileClient(var4)) != null) {
                  this.setActiveSources(-1);
                  this.core.getFileCollection().sendUpdate(var7, 2);
               }
            }

            if ((var3 & 1) != 0) {
               this.numConnectedClients++;
            } else if ((var3 & 2) != 0 && this.numConnectedClients > 0) {
               this.numConnectedClients--;
            }
         }

         this.clientWeakMap.addOrUpdate(var1);
      }
   }

   public void verifyChunks() {
      this.core.send((short)24, Integer.valueOf(this.getId()));
   }
}
