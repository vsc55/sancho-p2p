package sancho.view.viewer;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ICustomViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import sancho.model.mldonkey.Client;
import sancho.model.mldonkey.enums.EnumHostState;
import sancho.model.mldonkey.utility.Addr;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.viewer.table.GTableLabelProvider;

public abstract class GSorter extends ViewerSorter implements DisposeListener {
   protected int columnIndex;
   protected ICustomViewer cViewer;
   protected GView gView;
   protected int lastColumnIndex;
   protected boolean direction;
   protected boolean prevDirection;
   protected int prevColumnIndex;
   protected PreferenceStore preferenceStore = PreferenceLoader.getPreferenceStore();
   protected boolean workingDirection;
   protected static final boolean UP = true;
   protected static final boolean DOWN = false;

   public GSorter(GView var1) {
      this.gView = var1;
   }

   public int compare(Viewer var1, Object var2, Object var3) {
      this.workingDirection = this.direction;
      int var4 = this._compare(var1, var2, var3, this.cViewer.getColumnIDs()[this.columnIndex]);
      if (var4 == 0) {
         this.workingDirection = this.prevDirection;
         var4 = this._compare(var1, var2, var3, this.cViewer.getColumnIDs()[this.prevColumnIndex]);
      }

      return var4;
   }

   protected int _compare(Viewer var1, Object var2, Object var3, int var4) {
      return 0;
   }

   protected int compareAddrs(Addr var1, Addr var2) {
      return this.workingDirection ? var1.compareTo(var2) : var2.compareTo(var1);
   }

   protected int compareBooleans(boolean var1, boolean var2) {
      return this.workingDirection ? (var1 ? (var2 ? 0 : 1) : (var2 ? -1 : 0)) : (var2 ? (var1 ? 0 : 1) : (var1 ? -1 : 0));
   }

   protected int compareClientStates(Client var1, Client var2) {
      boolean var5 = var1.getStateEnum() == EnumHostState.CONNECTED_DOWNLOADING;
      boolean var6 = var2.getStateEnum() == EnumHostState.CONNECTED_DOWNLOADING;
      // Only when exactly one is downloading does it sort first. When both are
      // downloading, returning -1 (as before) for both a,b and b,a broke
      // antisymmetry (TimSort "violates its general contract"); fall through to
      // the rank/activity tie-break instead.
      if (var5 != var6) {
         return var5 ? -1 : 1;
      } else {
         int var3 = var1.getState().getRank();
         int var4 = var2.getState().getRank();
         return var3 <= 0 && var4 <= 0 ? this.compareStrings(var1.getDetailedClientActivity(), var2.getDetailedClientActivity()) : this.compareInts(var3, var4);
      }
   }

   protected int compareDefault(TableViewer var1, int var2, Object var3, Object var4) {
      GTableLabelProvider var5 = (GTableLabelProvider)var1.getLabelProvider();
      String var6 = var5.getColumnText(var3, var2);
      String var7 = var5.getColumnText(var4, var2);
      return this.compareStrings(var6, var7);
   }

   protected int comparePercents(float var1, float var2) {
      if (var1 == var2) {
         return 0;
      } else {
         var1 *= 100000.0F;
         var2 *= 100000.0F;
         float var3 = this.workingDirection ? var1 - var2 : var2 - var1;
         int var4 = (int)var3;
         if (var3 >= 2.1474836E9F) {
            var4 = Integer.MAX_VALUE;
         }

         if (var3 <= -2.1474836E9F) {
            var4 = Integer.MIN_VALUE;
         }

         return var4;
      }
   }

   protected int compareInts(int var1, int var2) {
      return this.workingDirection ? var1 - var2 : var2 - var1;
   }

   protected int compareLongs(long var1, long var3) {
      if (var1 == var3) {
         return 0;
      } else {
         long var5 = this.workingDirection ? var1 - var3 : var3 - var1;
         int var7 = (int)var5;
         if (var5 >= 2147483647L) {
            var7 = Integer.MAX_VALUE;
         }

         if (var5 <= -2147483648L) {
            var7 = Integer.MIN_VALUE;
         }

         return var7;
      }
   }

   protected int compareStrings(String var1, String var2) {
      if (var1.equals(var2)) {
         return 0;
      } else if (var1.equals("")) {
         return 1;
      } else if (var2.equals("")) {
         return -1;
      } else {
         return this.direction ? var1.compareToIgnoreCase(var2) : var2.compareToIgnoreCase(var1);
      }
   }

   public int getLastColumnIndex() {
      return this.lastColumnIndex;
   }

   public boolean getDirection() {
      return this.direction;
   }

   public void initialize() {
      this.cViewer = (ICustomViewer)this.gView.getViewer();
      this.gView.getComposite().addDisposeListener(this);
      String var1 = PreferenceLoader.loadString(this.gView.getPreferenceString() + "LastSortColumn");
      String var2 = PreferenceLoader.loadString(this.gView.getPreferenceString() + "PrevSortColumn");
      if (!var1.equals("") && this.gView.getColumnIDs().indexOf(var1) != -1) {
         this.setColumnIndex(this.gView.getColumnIDs().indexOf(var1));
         this.setDirection(PreferenceLoader.loadBoolean(this.gView.getPreferenceString() + "LastSortOrder"));
      }

      if (!var2.equals("") && this.gView.getColumnIDs().indexOf(var2) != -1) {
         this.prevColumnIndex = this.gView.getColumnIDs().indexOf(var2);
         this.prevDirection = PreferenceLoader.loadBoolean(this.gView.getPreferenceString() + "PrevSortOrder");
      }
   }

   public boolean isSorterProperty(Object var1, String var2) {
      return true;
   }

   public void setColumnIndex(int var1) {
      if (var1 != this.lastColumnIndex) {
         this.prevDirection = this.direction;
         this.prevColumnIndex = this.columnIndex;
      } else {
         this.prevDirection = !this.prevDirection;
      }

      this.columnIndex = var1;
      this.direction = this.columnIndex == this.lastColumnIndex ? !this.direction : this.sortOrder(this.columnIndex);
      this.lastColumnIndex = this.columnIndex;
   }

   public void setDirection(boolean var1) {
      this.direction = var1;
   }

   public boolean sortOrder(int var1) {
      return true;
   }

   public void updateDisplay() {
   }

   public void widgetDisposed(DisposeEvent var1) {
      String var2 = this.gView.getPreferenceString();
      this.preferenceStore.setValue(var2 + "LastSortColumn", String.valueOf(this.gView.getColumnIDs().charAt(this.columnIndex)));
      this.preferenceStore.setDefault(var2 + "LastSortOrder", true);
      this.preferenceStore.setValue(var2 + "LastSortOrder", this.direction);
      this.preferenceStore.setValue(var2 + "PrevSortColumn", String.valueOf(this.gView.getColumnIDs().charAt(this.prevColumnIndex)));
      this.preferenceStore.setDefault(var2 + "PrevSortOrder", true);
      this.preferenceStore.setValue(var2 + "PrevSortOrder", this.prevDirection);
   }
}
