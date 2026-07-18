package sancho.model.mldonkey.utility;

import java.util.ArrayList;
import java.util.List;
import sancho.core.ICore;
import sancho.model.mldonkey.enums.EnumQuery;
import sancho.utility.SwissArmy;

public class SearchQuery {
   public static final String S_OR = "OR";
   public static final String S_AND = "AND";
   public static final String S_ANDNOT = "ANDNOT";
   public static final String S_KEYWORD = "Keyword";
   public static final String S_AUDIO = "Audio";
   public static final String S_VIDEO = "Video";
   public static final String S_IMAGE = "Image";
   public static final String S_DOCUMENT = "Doc";
   public static final String S_PROGRAM = "Pro";
   public static final String S_ARCHIVE = "Arc";
   public static final String S_CDIMAGE = "Iso";
   public static final String S_EMULECOLLECTION = "EmuleCollection";
   private static final int OR = 1;
   private static final int AND = 2;
   private static final int ANDNOT = 3;
   private static int searchId;
   private List andNotList = new ArrayList();
   private Query andQuery;
   private Query firstQuery;
   private Query orQuery;
   private ICore core;
   private int currentType;
   private int keyWords;
   private int maxSearchResults;
   private int networkId;
   private byte searchType;

   public SearchQuery(ICore var1) {
      this.core = var1;
      this.andQuery = UtilityFactory.getQuery(var1);
      this.andQuery.setEnumQuery(EnumQuery.AND);
      this.orQuery = UtilityFactory.getQuery(var1);
      this.orQuery.setEnumQuery(EnumQuery.OR);
      this.searchType = 1;
      this.networkId = 0;
   }

   public void addKeyword(String var1) {
      if (var1 != null) {
         Query var2 = UtilityFactory.getQuery(this.core);
         var2.setEnumQuery(EnumQuery.KEYWORDS);
         var2.setString1("Keyword");
         var2.setString2(var1);
         this.keyWords++;
         if (this.keyWords == 1) {
            this.firstQuery = var2;
         } else {
            switch (this.currentType) {
               case 1:
                  if (this.firstQuery != null) {
                     this.orQuery.addQuery(this.firstQuery);
                  }

                  this.orQuery.addQuery(var2);
                  this.firstQuery = null;
                  break;
               case 3:
                  this.andNotList.add(var2);
                  break;
               default:
                  if (this.firstQuery != null) {
                     this.andQuery.addQuery(this.firstQuery);
                  }

                  this.andQuery.addQuery(var2);
                  this.firstQuery = null;
            }
         }
      }
   }

   public void addQueryToAnd(EnumQuery var1, String var2, String var3) {
      Query var4 = UtilityFactory.getQuery(this.core);
      var4.setEnumQuery(var1);
      var4.setString1(var2);
      var4.setString2(var3);
      this.andQuery.addQuery(var4);
   }

   public int getSearchId() {
      return searchId++;
   }

   public String parseWord(String var1) {
      if (var1.startsWith("-")) {
         this.currentType = 3;
         return var1.substring(1);
      } else if (var1.startsWith("+")) {
         this.currentType = 2;
         return var1.substring(1);
      } else if (var1.equalsIgnoreCase("|")) {
         this.currentType = 1;
         return null;
      } else if (var1.equalsIgnoreCase("AND")) {
         this.currentType = 2;
         return null;
      } else if (var1.equalsIgnoreCase("OR")) {
         this.currentType = 1;
         return null;
      } else if (var1.equalsIgnoreCase("ANDNOT")) {
         this.currentType = 3;
         return null;
      } else {
         return var1;
      }
   }

   public void send() {
      Query var2;
      if (this.firstQuery != null) {
         if (this.andQuery.queryListSize() >= 1) {
            this.andQuery.addQuery(this.firstQuery);
            var2 = this.andQuery;
         } else {
            var2 = this.firstQuery;
         }
      } else if (this.orQuery.queryListSize() >= 1 && this.andQuery.queryListSize() >= 1) {
         this.andQuery.addQuery(this.orQuery);
         var2 = this.andQuery;
      } else if (this.orQuery.queryListSize() >= 1) {
         var2 = this.orQuery;
      } else {
         var2 = this.andQuery;
      }

      Query var1;
      if (this.andNotList.size() > 0) {
         Query var3;
         if (this.andNotList.size() > 1) {
            var3 = UtilityFactory.getQuery(this.core);
            var3.setEnumQuery(EnumQuery.OR);

            for (int var4 = 0; var4 < this.andNotList.size(); var4++) {
               var3.addQuery((Query)this.andNotList.get(var4));
            }
         } else {
            var3 = (Query)this.andNotList.get(0);
         }

         var1 = UtilityFactory.getQuery(this.core);
         var1.setEnumQuery(EnumQuery.ANDNOT);
         var1.setQuery1(var2);
         var1.setQuery2(var3);
      } else {
         var1 = var2;
      }

      ArrayList var6 = new ArrayList();
      var6.add(Integer.valueOf(searchId));
      Object[] var7 = var1.toObjectArray();

      for (int var5 = 0; var5 < var7.length; var5++) {
         var6.add(var7[var5]);
      }

      var6.add(Integer.valueOf(this.maxSearchResults));
      var6.add(Byte.valueOf(this.searchType));
      var6.add(Integer.valueOf(this.networkId));
      this.core.send((short)42, var6.toArray());
   }

   public void setFormat(String var1) {
      this.addQueryToAnd(EnumQuery.FORMAT, "", var1);
   }

   public void setLocalSearch() {
      this.searchType = 0;
   }

   public void setMaxSearchResults(int var1) {
      this.maxSearchResults = var1;
   }

   public void setMaxSize(long var1) {
      this.addQueryToAnd(EnumQuery.MAXSIZE, "", String.valueOf(var1));
   }

   public void setMedia(String var1) {
      this.addQueryToAnd(EnumQuery.MEDIA, "", var1);
   }

   public void setMinSize(long var1) {
      this.addQueryToAnd(EnumQuery.MINSIZE, "", String.valueOf(var1));
   }

   public void setMp3Album(String var1) {
      this.addQueryToAnd(EnumQuery.MP3_ALBUM, "", var1);
   }

   public void setMp3Artist(String var1) {
      this.addQueryToAnd(EnumQuery.MP3_ARTIST, "", var1);
   }

   public void setMp3Bitrate(String var1) {
      this.addQueryToAnd(EnumQuery.MP3_BITRATE, "", var1);
   }

   public void setMp3Title(String var1) {
      this.addQueryToAnd(EnumQuery.MP3_TITLE, "", var1);
   }

   public void setNetwork(int var1) {
      this.networkId = var1;
   }

   public void setSearchString(String var1) {
      String[] var2 = SwissArmy.split(var1, ' ');
      String var3 = "";
      boolean var4 = false;

      for (int var5 = 0; var5 < var2.length; var5++) {
         if (var2[var5].startsWith("\"") && !var4) {
            var4 = true;
            if (var2[var5].length() > 1) {
               var3 = var3 + (var3.equals("") ? "" : " ") + var2[var5].substring(1);
            }
         } else if (var2[var5].endsWith("\"") && var4) {
            if (var2[var5].length() > 2) {
               String var6 = var2[var5].substring(0, var2[var5].length() - 1);
               var3 = var3 + (var3.equals("") ? "" : " ") + var6;
               this.addKeyword(this.parseWord(var3));
               var3 = "";
               var4 = false;
            }
         } else if (var4) {
            var3 = var3 + (var3.equals("") ? "" : " ") + var2[var5];
         } else {
            this.addKeyword(this.parseWord(var2[var5]));
         }
      }

      if (!var3.equals("")) {
         this.addKeyword(this.parseWord(var3));
      }
   }

   public void setSubscribeSearch() {
      this.searchType = 2;
   }
}
