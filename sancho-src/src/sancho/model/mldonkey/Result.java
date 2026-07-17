package sancho.model.mldonkey;

import sancho.utility.regex.RE;
import sancho.utility.regex.REException;
import java.util.StringTokenizer;
import org.eclipse.swt.graphics.Image;
import sancho.core.ICore;
import sancho.model.mldonkey.enums.EnumExtension;
import sancho.model.mldonkey.enums.EnumNetwork;
import sancho.model.mldonkey.enums.EnumRating;
import sancho.model.mldonkey.enums.EnumType;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.Tag;
import sancho.utility.SwissArmy;
import sancho.view.utility.SResources;

public class Result extends AObject implements IObject_UID {
   private static final String RS_MD4 = SResources.getString("r.tt.md4");
   private static final String RS_NETWORK = SResources.getString("r.tt.network");
   private static final String RS_FORMAT = SResources.getString("r.tt.format");
   private static final String RS_SIZE = SResources.getString("r.tt.size");
   private static final String RS_ADOWNLOADED = SResources.getString("r.tt.alreadyDownloaded");
   private static final String TAG_LENGTH = "length";
   private static final String TAG_TIME = "time";
   private static final String TAG_SECONDS = "seconds";
   private static final String TAG_CODEC = "codec";
   private static final String TAG_RESOLUTION = "resolution";
   private static final String TAG_SAMPLE_RATE = "sampleRate";
   private static final String TAG_BITRATE = "bitrate";
   private static final String TAG_QUALITY = "quality";
   private static final String TAG_AVAILABILITY = "availability";
   private static final String TAG_COMPLETESOURCES = "completesources";
   private static final String S_NEWLINE = "\n";
   protected static RE fakeRE;
   protected static RE pornographyFilterRE;
   protected static RE profanityFilterRE;
   protected boolean downloaded;
   protected boolean containsFake;
   protected boolean containsPornography;
   protected boolean containsProfanity;
   protected EnumExtension extensionEnum;
   protected String comment;
   protected String format;
   protected int id;
   protected byte[] md4;
   protected String[] names;
   protected EnumNetwork networkEnum;
   protected EnumRating rating;
   protected long size;
   protected int tag_availability;
   protected int tag_bitrate;
   protected int tag_completesources = -1;
   protected String tag_codec;
   protected String tag_length;
   protected Tag[] tagList;
   protected String type;

   Result(ICore var1) {
      super(var1);
   }

   public synchronized boolean downloaded() {
      return this.downloaded;
   }

   public synchronized boolean containsFake() {
      return this.containsFake;
   }

   public synchronized boolean containsPornography() {
      return this.containsPornography;
   }

   public synchronized boolean containsProfanity() {
      return this.containsProfanity;
   }

   public boolean equals(Object var1) {
      return var1 instanceof Result && this.getId() == ((Result)var1).getId();
   }

   public synchronized int getAvail() {
      return this.tag_availability;
   }

   public synchronized int getBitrateTag() {
      return this.tag_bitrate;
   }

   public synchronized EnumNetwork getEnumNetwork() {
      return this.networkEnum;
   }

   public synchronized String getNetworkName() {
      return this.networkEnum.getName();
   }

   public synchronized Image getNetworkImage() {
      return this.networkEnum.getImage();
   }

   public synchronized String getBitrateTagString() {
      return this.tag_bitrate > 0 ? String.valueOf(this.tag_bitrate).intern() : "";
   }

   public synchronized String getCodecTag() {
      return this.tag_codec != null ? this.tag_codec : "";
   }

   public synchronized String getComment() {
      return this.comment != null ? this.comment : "";
   }

   public synchronized int getCompleteSources() {
      return this.tag_completesources;
   }

   public String getCompleteSourcesString() {
      int var1 = this.getCompleteSources();
      return var1 != -1 ? String.valueOf(var1).intern() : "";
   }

   public String getED2K() {
      return this.getName() == null || this.getEnumNetwork() != EnumNetwork.DONKEY && this.getEnumNetwork() != EnumNetwork.UNKNOWN
         ? ""
         : "ed2k://|file|" + this.getName() + "|" + this.getSize() + "|" + this.getMD4() + "|/";
   }

   public synchronized String getFormat() {
      return this.format != null ? this.format : "";
   }

   protected void calcFormat() {
      if (this.format == null || this.format.equals("")) {
         int var1 = this.getName().lastIndexOf(".");
         if (var1 != -1) {
            this.format = this.getName().substring(var1 + 1).toLowerCase().intern();
         }
      }
   }

   public synchronized int getId() {
      return this.id;
   }

   public synchronized String[] getUIDs() {
      return null;
   }

   public synchronized String getLengthTag() {
      return this.tag_length != null ? this.tag_length : "";
   }

   public synchronized String getMD4() {
      return SwissArmy.calcStringOfMD4(this.md4);
   }

   public Image getFileTypeImage() {
      return SResources.getFileTypeImage(this.getFormat());
   }

   public String getName() {
      String[] var1 = this.getNames();
      if (var1.length < 1) {
         return "";
      } else {
         String var2 = var1[0];
         EnumNetwork var3 = this.getEnumNetwork();
         if (var3 == EnumNetwork.GNUT || var3 == EnumNetwork.GNUT2) {
            String var4 = String.valueOf('\u0000');
            var2 = SwissArmy.replaceAll(var2, var4, "");
         }

         return var2;
      }
   }

   public synchronized String[] getNames() {
      return this.names != null ? this.names : new String[0];
   }

   public synchronized EnumRating getRating() {
      return this.rating;
   }

   public synchronized String getRatingString() {
      if (this.core.getResultCollection().verboseNumbers) {
         StringBuffer var1 = new StringBuffer();
         var1.append(this.rating.getName());
         var1.append("(");
         var1.append(this.getAvail());
         var1.append(")");
         return var1.toString().intern();
      } else {
         return String.valueOf(this.getAvail());
      }
   }

   public synchronized long getSize() {
      return this.size;
   }

   public synchronized String getSizeString() {
      return SwissArmy.calcStringSize(this.getSize()).intern();
   }

   public synchronized Tag[] getTagList() {
      if (this.tagList != null && this.tagList.length != 0) {
         Tag[] var1 = new Tag[this.tagList.length];
         System.arraycopy(this.tagList, 0, var1, 0, this.tagList.length);
         return var1;
      } else {
         return new Tag[0];
      }
   }

   public String getToolTip() {
      return this.getName() + "\n" + this.getToolTipContent();
   }

   public String getToolTipContent() {
      StringBuffer var1 = new StringBuffer();
      String[] var2 = this.getUIDs();
      if (var2 != null) {
         for (int var3 = 0; var3 < var2.length; var3++) {
            var1.append(var2[var3]);
            var1.append("\n");
         }
      } else if (this.getEnumNetwork() == EnumNetwork.DONKEY) {
         var1.append(RS_MD4);
         var1.append(this.getMD4().toUpperCase());
         var1.append("\n");
      }

      var1.append(RS_NETWORK);
      var1.append(this.getEnumNetwork().getName());
      var1.append("\n");
      if (!this.getFormat().equals("")) {
         var1.append(RS_FORMAT);
         var1.append(this.getFormat());
         var1.append("\n");
      }

      var1.append(RS_SIZE);
      var1.append(this.getSizeString());
      Tag[] var6 = this.getTagList();

      for (int var5 = 0; var5 < var6.length; var5++) {
         Tag var4 = var6[var5];
         var1.append("\n");
         var1.append(this.localizeTagName(var4.getName()));
         var1.append(": ");
         if (var4.getType() == EnumType.STRING) {
            var1.append(var4.getStringValue());
         } else {
            var1.append(var4.getValue());
         }
      }

      if (this.downloaded()) {
         var1.append("\n");
         var1.append(RS_ADOWNLOADED);
      }

      return var1.toString();
   }

   protected String localizeTagName(String var1) {
      String var2 = this.lookupTagName(var1);
      return var2 == null ? var1 : SResources.getString(var2);
   }

   protected String lookupTagName(String var1) {
      if (var1.equalsIgnoreCase("length")) {
         return "s.r.tag.length";
      } else if (var1.equalsIgnoreCase("time")) {
         return "s.r.tag.time";
      } else if (var1.equalsIgnoreCase("seconds")) {
         return "s.r.tag.seconds";
      } else if (var1.equalsIgnoreCase("codec")) {
         return "s.r.tag.codec";
      } else if (var1.equalsIgnoreCase("resolution")) {
         return "s.r.tag.resolution";
      } else if (var1.equalsIgnoreCase("sampleRate")) {
         return "s.r.tag.samplerate";
      } else if (var1.equalsIgnoreCase("bitrate")) {
         return "s.r.tag.bitrate";
      } else if (var1.equalsIgnoreCase("quality")) {
         return "s.r.tag.quality";
      } else if (var1.equalsIgnoreCase("availability")) {
         return "s.r.tag.availability";
      } else {
         return var1.equalsIgnoreCase("completesources") ? "s.r.tag.completesources" : null;
      }
   }

   public synchronized String getType() {
      if (this.type != null && !this.type.equals("")) {
         return this.type;
      } else {
         return this.extensionEnum != null ? this.extensionEnum.getName() : EnumExtension.UNKNOWN.getName();
      }
   }

   public int hashCode() {
      return this.getId();
   }

   public boolean isDownloading() {
      return this.core.getFileCollection().containsHash(this.getMD4());
   }

   protected void parseTags() {
      this.tag_codec = "";
      this.tag_length = "";

      for (int var2 = 0; var2 < this.tagList.length; var2++) {
         String var1 = this.tagList[var2].getName();
         if (var1.equals("length") || var1.equals("time") || var1.equals("seconds")) {
            this.tag_length = this.tagList[var2].getStringValue();
         } else if (var1.equals("codec") || var1.equals("resolution") || var1.equals("sampleRate")) {
            this.tag_codec = this.tagList[var2].getStringValue();
         } else if (var1.equals("bitrate")) {
            this.tag_bitrate = this.tagList[var2].getValue();
         } else if (var1.equals("completesources")) {
            this.tag_completesources = this.tagList[var2].getValue();
         } else if (var1.equals("quality")) {
            StringTokenizer var3 = new StringTokenizer(this.tagList[var2].getStringValue());
            if (var3.hasMoreTokens()) {
               try {
                  this.tag_bitrate = Integer.parseInt(var3.nextToken());
               } catch (Exception var5) {
               }
            }
         } else if (var1.equals("availability")) {
            this.tag_availability = this.tagList[var2].getValue();
         }

         if (this.tag_availability == 0) {
            this.tag_availability = 1;
         }
      }
   }

   protected void readUIDs(MessageBuffer var1) {
      this.md4 = var1.getMd4();
   }

   public void read(int var1, MessageBuffer var2) {
      synchronized (this) {
         this.id = var1;
         this.networkEnum = this.readNetworkEnum(var2);
         this.names = var2.getStringList();
         this.readUIDs(var2);
         this.size = this.readSize(var2);
         this.format = var2.getString();
         this.calcFormat();
         this.type = var2.getString();
         this.tagList = var2.getTagList();
         this.comment = var2.getString();
         this.downloaded = var2.getBool();
         this.regexFilters();
         this.parseTags();
         this.setRating();
         this.calcFileType();
      }
   }

   public void read(MessageBuffer var1) {
      this.read(var1.getInt32(), var1);
   }

   protected long readSize(MessageBuffer var1) {
      return (long)var1.getInt32() & 4294967295L;
   }

   protected void regexFilters() {
      if (this.core.getResultCollection().filterPornography || this.core.getResultCollection().filterProfanity) {
         for (int var1 = 0; var1 < this.names.length; var1++) {
            if (profanityFilterRE != null && profanityFilterRE.getMatch(this.names[var1]) != null) {
               this.containsProfanity = true;
               if (this.containsPornography) {
                  break;
               }
            }

            if (pornographyFilterRE != null && pornographyFilterRE.getMatch(this.names[var1]) != null) {
               this.containsPornography = true;
               if (this.containsProfanity) {
                  break;
               }
            }

            if (!this.containsFake) {
               this.containsFake = SwissArmy.containsFake(this.names[var1]);
            }
         }
      }

      if (!this.containsFake) {
         this.containsFake = SwissArmy.containsFake(this.comment);
      }
   }

   protected void calcFileType() {
      EnumExtension var1 = EnumExtension.GET_EXT(this.getFormat());
      this.extensionEnum = var1 != null ? var1 : EnumExtension.UNKNOWN;
   }

   protected EnumNetwork readNetworkEnum(MessageBuffer var1) {
      return this.core.getNetworkCollection().getNetworkEnum(var1.getInt32());
   }

   protected void setRating() {
      this.rating = this.containsFake ? EnumRating.FAKE : EnumRating.intToEnum(this.getAvail());
   }

   static {
      try {
         profanityFilterRE = new RE("fuck|shit", 2);
      } catch (REException var3) {
         profanityFilterRE = null;
      }

      try {
         pornographyFilterRE = new RE(
            "fuck|shit|porn|pr0n|pussy|xxx|sex|erotic|anal|lolita|sluts|fetish|naked|incest|bondage|masturbat|blow.*job|barely.*legal", 2
         );
      } catch (REException var2) {
         pornographyFilterRE = null;
      }

      try {
         fakeRE = new RE("fake", 2);
      } catch (REException var1) {
         fakeRE = null;
      }
   }
}
