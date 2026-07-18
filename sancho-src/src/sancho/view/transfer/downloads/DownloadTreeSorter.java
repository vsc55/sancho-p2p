package sancho.view.transfer.downloads;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import sancho.model.mldonkey.Client;
import sancho.model.mldonkey.File;
import sancho.model.mldonkey.enums.EnumFileState;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.transfer.FileClient;
import sancho.view.viewer.GSorter;
import sancho.view.viewer.GView;

public class DownloadTreeSorter extends GSorter {
   private ITableLabelProvider labelProvider;
   private boolean maintainSortOrder = false;

   public DownloadTreeSorter(GView var1) {
      super(var1);
   }

   public int category(Object var1) {
      if (var1 instanceof File) {
         return 1;
      } else {
         return var1 instanceof FileClient ? 2 : 3;
      }
   }

   private static int fileStateRank(EnumFileState var0) {
      if (var0 == EnumFileState.DOWNLOADED) {
         return 0;
      } else if (var0 == EnumFileState.QUEUED) {
         return 1;
      } else {
         return var0 == EnumFileState.PAUSED ? 2 : 3;
      }
   }

   protected int _compare(Viewer var1, Object var2, Object var3, int var4) {
      int var5 = this.category(var2);
      int var6 = this.category(var3);
      if (var5 != var6) {
         return var5 - var6;
      } else if (var2 instanceof File) {
         File var11 = (File)var2;
         File var12 = (File)var3;
         switch (var4) {
            case 0:
               return this.compareInts(var11.getId(), var12.getId());
            case 1:
               return this.compareStrings(var11.getEnumNetwork().getName(), var12.getEnumNetwork().getName());
            case 2:
               return this.compareStrings(var11.getName(), var12.getName());
            case 3:
               return this.compareLongs(var11.getSize(), var12.getSize());
            case 4:
               return this.compareLongs(var11.getDownloaded(), var12.getDownloaded());
            case 5:
               return this.comparePercents(var11.getPercent(), var12.getPercent());
            case 6:
               return this.compareInts(var11.getSources(), var12.getSources());
            case 7:
               return this.compareInts(var11.getRelativeAvail(), var12.getRelativeAvail());
            case 8: {
               // Rank by state (Downloaded < Queued < Paused < other), then break
               // ties by rate. The old constant returns (-1/2/3) gave the same sign
               // for both a,b and b,a on same-state pairs, violating the comparator
               // contract (TimSort abort with 2+ files sharing a state).
               int var13 = fileStateRank(var11.getFileStateEnum());
               int var14 = fileStateRank(var12.getFileStateEnum());
               return var13 != var14 ? var13 - var14 : this.comparePercents(var11.getRate(), var12.getRate());
            }
            case 9:
               return this.compareInts(var11.getNumChunks(), var12.getNumChunks());
            case 10:
               this.labelProvider = (ITableLabelProvider)((TreeViewer)var1).getLabelProvider();
               if (this.labelProvider.getColumnText(var2, this.columnIndex).equals("")) {
                  return 1;
               } else {
                  if (this.labelProvider.getColumnText(var3, this.columnIndex).equals("")) {
                     return -1;
                  }

                  return this.compareLongs(var11.getETA(), var12.getETA());
               }
            case 11:
               return this.compareInts(var11.getPriority(), var12.getPriority());
            case 12:
               return this.compareInts(var11.getLastSeen(), var12.getLastSeen());
            case 13:
               return this.compareLongs(var11.getAge(), var12.getAge());
            case 14:
               return this.compareLongs(var11.getETA2(), var12.getETA2());
            case 15:
               return this.compareInts(var11.getNumClients(), var12.getNumClients());
            case 16:
               return this.compareInts(var11.getNumSources(), var12.getNumSources());
            case 17:
               return this.compareLongs(var11.getRemaining(), var12.getRemaining());
            case 18:
               return this.compareInts(var11.getNumComments(), var12.getNumComments());
            case 19:
               return this.compareStrings(var11.getUser(), var12.getUser());
            case 20:
               return this.compareStrings(var11.getGroup(), var12.getGroup());
            default:
               return 0;
         }
      } else {
         Client var7 = ((FileClient)var2).getClient();
         Client var8 = ((FileClient)var3).getClient();
         switch (var4) {
            case 0:
               return this.compareInts(var7.getId(), var8.getId());
            case 1:
               return this.compareStrings(var7.getEnumNetwork().getName(), var8.getEnumNetwork().getName());
            case 2:
               return this.compareStrings(var7.getName(), var8.getName());
            case 3:
            case 11:
               this.labelProvider = (ITableLabelProvider)((TreeViewer)var1).getLabelProvider();
               return this.compareStrings(this.labelProvider.getColumnText(var2, this.columnIndex), this.labelProvider.getColumnText(var3, this.columnIndex));
            case 4:
               return this.compareLongs(var7.getDownloaded(), var8.getDownloaded());
            case 5:
               return this.compareInts(var7.getPort(), var8.getPort());
            case 6:
               return this.compareAddrs(var7.getAddr(), var8.getAddr());
            case 7:
            case 8:
            case 10:
            case 12:
            default:
               return 0;
            case 9:
               FileClient var9 = (FileClient)var2;
               FileClient var10 = (FileClient)var3;
               return this.compareInts(var7.getNumChunks(var9.getFile().getId()), var8.getNumChunks(var10.getFile().getId()));
            case 13:
               return this.compareInts(var7.getConnectedTime(), var8.getConnectedTime());
         }
      }
   }

   public boolean isSorterProperty(Object var1, String var2) {
      if (this.maintainSortOrder && var1 instanceof File) {
         File var3 = (File)var1;
         switch (this.cViewer.getColumnIDs()[this.columnIndex]) {
            case 4:
            case 17:
               return var3.hasChangedBit(4);
            case 5:
               return var3.hasChangedBit(32);
            case 6:
               return var3.hasChangedBit(512);
            case 7:
               return var3.hasChangedBit(2);
            case 8:
               return var3.hasChangedBit(64) || var3.hasChangedBit(128);
            case 9:
            case 11:
            case 13:
            case 14:
            case 15:
            case 16:
            default:
               return false;
            case 10:
               return var3.hasChangedBit(8);
            case 12:
               return var3.hasChangedBit(16);
            case 18:
               return var3.hasChangedBit(2048);
         }
      } else {
         return false;
      }
   }

   public boolean sortOrder(int var1) {
      switch (this.cViewer.getColumnIDs()[var1]) {
         case 0:
         case 1:
         case 2:
         case 10:
         case 12:
         case 14:
         case 17:
         case 19:
         case 20:
            return true;
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 11:
         case 13:
         case 15:
         case 16:
         case 18:
         default:
            return false;
      }
   }

   public void updateDisplay() {
      this.maintainSortOrder = PreferenceLoader.loadBoolean("maintainSortOrder");
   }
}
