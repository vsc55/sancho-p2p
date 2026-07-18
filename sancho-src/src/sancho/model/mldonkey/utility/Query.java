package sancho.model.mldonkey.utility;

import java.util.ArrayList;
import java.util.List;
import sancho.core.ICore;
import sancho.model.mldonkey.enums.EnumQuery;

public class Query {
   private ICore core;
   private EnumQuery enumQuery;
   private Query query1;
   private Query query2;
   private List queryList = new ArrayList();
   private String string1;
   private String string2;

   public Query(ICore var1) {
      this.core = var1;
   }

   public void addQuery(Query var1) {
      this.queryList.add(var1);
   }

   public int queryListSize() {
      return this.queryList.size();
   }

   public void addQueryToList(List var1, Query var2) {
      Object[] var3 = var2.toObjectArray();

      for (int var4 = 0; var4 < var3.length; var4++) {
         var1.add(var3[var4]);
      }
   }

   public Query createQuery() {
      return UtilityFactory.getQuery(this.core);
   }

   public EnumQuery getEnumQuery() {
      return this.enumQuery;
   }

   public Query[] getQueryList() {
      Query[] var1 = new Query[this.queryList.size()];
      this.queryList.toArray(var1);
      return var1;
   }

   public void read(MessageBuffer var1) {
      this.enumQuery = EnumQuery.byteToEnum(var1.getByte());
      if (this.enumQuery != EnumQuery.AND && this.enumQuery != EnumQuery.OR && this.enumQuery != EnumQuery.HIDDEN) {
         if (this.enumQuery == EnumQuery.ANDNOT) {
            this.query1 = this.createQuery();
            this.query1.read(var1);
            this.query2 = this.createQuery();
            this.query2.read(var1);
         } else if (this.enumQuery == EnumQuery.MODULE) {
            this.string1 = var1.getString();
            this.query1 = this.createQuery();
            this.query1.read(var1);
         } else {
            this.string1 = var1.getString();
            this.string2 = var1.getString();
         }
      } else {
         int var2 = var1.getUInt16();

         for (int var4 = 0; var4 < var2; var4++) {
            Query var3 = this.createQuery();
            var3.read(var1);
            this.queryList.add(var3);
         }
      }
   }

   public void setEnumQuery(EnumQuery var1) {
      this.enumQuery = var1;
   }

   public void setQuery1(Query var1) {
      this.query1 = var1;
   }

   public void setQuery2(Query var1) {
      this.query2 = var1;
   }

   public void setString1(String var1) {
      this.string1 = var1;
   }

   public void setString2(String var1) {
      this.string2 = var1;
   }

   public Object[] toObjectArray() {
      ArrayList var1 = new ArrayList();
      var1.add(Byte.valueOf(this.enumQuery.getByteValue()));
      if (this.enumQuery != EnumQuery.AND && this.enumQuery != EnumQuery.OR && this.enumQuery != EnumQuery.HIDDEN) {
         if (this.enumQuery == EnumQuery.ANDNOT) {
            this.addQueryToList(var1, this.query1);
            this.addQueryToList(var1, this.query2);
         } else if (this.enumQuery == EnumQuery.MODULE) {
            var1.add(this.string1);
            this.addQueryToList(var1, this.query1);
         } else {
            var1.add(this.string1);
            var1.add(this.string2);
         }
      } else {
         var1.add(Short.valueOf((short)this.queryList.size()));

         for (int var2 = 0; var2 < this.queryList.size(); var2++) {
            this.addQueryToList(var1, (Query)this.queryList.get(var2));
         }
      }

      return var1.toArray();
   }
}
