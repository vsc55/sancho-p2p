package sancho.view.viewer.filters;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import sancho.model.mldonkey.Client;
import sancho.model.mldonkey.File;
import sancho.model.mldonkey.Result;
import sancho.model.mldonkey.Server;
import sancho.model.mldonkey.SharedFile;
import sancho.model.mldonkey.utility.FileComment;
import sancho.utility.SwissArmy;
import sancho.view.downloadComplete.DownloadCompleteItem;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.transfer.fileDialog.SubfileItem;
import sancho.view.viewer.GView;

public class RefineFilter extends ViewerFilter {
   private static String SEPARATOR = " ";
   private GView gView;
   private Pattern regex;
   private boolean refine;
   private boolean returnValue;
   private boolean searchAlternates;

   public RefineFilter(GView var1) {
      this.gView = var1;
      this.update();
   }

   public void update() {
      if (this.refine = !this.gView.getRefineString().equals("") && !this.gView.getRefineString().equals("-")) {
         try {
            String var1 = this.gView.getRefineString();
            if (var1.length() > 1000) {
               var1 = var1.substring(0, 1000);
            }

            if (var1.startsWith("-")) {
               var1 = var1.substring(1);
            }

            this.regex = Pattern.compile(var1, Pattern.CASE_INSENSITIVE);
         } catch (PatternSyntaxException var2) {
            this.refine = false;
         }
      }

      this.returnValue = !PreferenceLoader.loadBoolean("refineFilterNegation");
      if (this.returnValue && this.gView.getRefineString().startsWith("-")) {
         this.returnValue = false;
      }

      this.searchAlternates = PreferenceLoader.loadBoolean("refineFilterAlternates");
   }

   public boolean select(Viewer var1, Object var2, Object var3) {
      if (!this.refine) {
         return true;
      } else {
         String var4;
         if (var3 instanceof Server) {
            Server var5 = (Server)var3;
            StringBuffer var6 = new StringBuffer(64);
            var6.append(var5.getName());
            var6.append(SEPARATOR);
            var6.append(var5.getDescription());
            var6.append(SEPARATOR);
            var6.append(var5.getAddr());
            var4 = var6.toString();
         } else if (var3 instanceof Result) {
            Result var7 = (Result)var3;
            StringBuffer var11 = new StringBuffer();
            var11.append(var7.getName());
            if (this.searchAlternates) {
               this.appendAlternates(var11, var7.getNames());
            }

            var4 = var11.toString();
         } else if (var3 instanceof SharedFile) {
            var4 = ((SharedFile)var3).getName();
         } else if (var3 instanceof Client) {
            Client var8 = (Client)var3;
            StringBuffer var12 = new StringBuffer();
            var12.append(var8.getName());
            var12.append(SEPARATOR);
            var12.append(var8.getUploadFilename());
            var4 = var12.toString();
         } else if (var3 instanceof File) {
            File var9 = (File)var3;
            StringBuffer var13 = new StringBuffer();
            var13.append(var9.getName());
            var13.append(SEPARATOR);
            var13.append(var9.getUser());
            var13.append(SEPARATOR);
            var13.append(var9.getGroup());
            if (this.searchAlternates) {
               this.appendAlternates(var13, var9.getNames());
            }

            var4 = var13.toString();
         } else if (var3 instanceof DownloadCompleteItem) {
            var4 = ((DownloadCompleteItem)var3).getName();
         } else if (var3 instanceof SubfileItem) {
            var4 = ((SubfileItem)var3).getName();
         } else {
            if (!(var3 instanceof FileComment)) {
               return true;
            }

            FileComment var10 = (FileComment)var3;
            StringBuffer var14 = new StringBuffer();
            var14.append(var10.getName());
            var14.append("");
            var14.append(var10.getComment());
            var4 = var14.toString();
         }

         return this.regex.matcher(var4).find() ? this.returnValue : !this.returnValue;
      }
   }

   public void appendAlternates(StringBuffer var1, String[] var2) {
      for (int var3 = 0; var3 < var2.length; var3++) {
         var1.append(SEPARATOR);
         var1.append(var2[var3]);
      }
   }

   public String toString() {
      return "5," + SwissArmy.replaceAll(this.gView.getRefineString(), ",", "") + ",";
   }
}
