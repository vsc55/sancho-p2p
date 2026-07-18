package sancho.view.viewer.table;

import gnu.trove.TIntArrayList;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.CustomTableViewer;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.TableItem;
import sancho.utility.MyObservable;
import sancho.utility.MyObserver;
import sancho.view.viewer.GView;
import sancho.view.viewer.IGContentProvider;

public abstract class GTableContentProvider implements IGContentProvider, IStructuredContentProvider, ILazyContentProvider, MyObserver {
   protected static final Object[] EMPTY_ARRAY = new Object[0];
   protected GView gView;
   protected boolean needsRefresh;
   protected CustomTableViewer tableViewer;
   protected ArrayList sfList = new ArrayList();

   public synchronized Object getSFElement(int var1) {
      return this.sfList.get(var1);
   }

   public void setNeedsRefresh(boolean var1) {
      this.needsRefresh = var1;
   }

   public synchronized int getSFIndex(Object var1) {
      return this.sfList.indexOf(var1);
   }

   public synchronized int[] getIndicesOf(List var1) {
      int[] var2 = new int[var1.size()];
      int var3 = 0;

      for (Object var5 : var1) {
         int var6 = this.sfList.indexOf(var5);
         if (var6 != -1) {
            var2[var3++] = var6;
         }
      }

      if (var3 < var2.length) {
         System.arraycopy(var2, 0, var2 = new int[var3], 0, var3);
      }

      return var2;
   }

   public synchronized void updateElement(int var1) {
      // ILazyContentProvider hook: SWT calls this when it needs to render a virtual
      // row (scrolled into view, or after Table.clear()). It was EMPTY, so those
      // rows never got their element and showed stale/blank data (the top rows that
      // "stuck" and ignored sorting). Render the current sfList element for the row.
      if (var1 >= 0 && var1 < this.sfList.size() && this.tableViewer != null) {
         this.updateElement(this.tableViewer.getTable().getItem(var1), var1);
      }
   }

   public synchronized void updateElement(TableItem var1, int var2) {
      ((CustomTableViewer)this.gView.getViewer()).replace(this.sfList.get(var2), var1);
   }

   public synchronized void updateSorted(boolean var1) {
      ArrayList var2 = this.sfList;
      Object[] var3 = this.tableViewer.getSortedChildren(this.tableViewer.getInput());
      int var4 = var3.length;
      this.sfList = new ArrayList(var4);

      for (int var5 = 0; var5 < var4; var5++) {
         this.sfList.add(var3[var5]);
      }

      TIntArrayList var6 = new TIntArrayList();
      ArrayList var7 = new ArrayList();
      int var8 = 0;

      for (Object var10 : var2) {
         if (var8 >= var4 || var3[var8] != var10 || var1) {
            var7.add(var10);
            var6.add(var8);
         }

         var8++;
      }

      this.tableViewer.myClear(var4, var6.toNativeArray(), var7.toArray());
   }

   public GTableContentProvider(GView var1) {
      this.gView = var1;
   }

   public void dispose() {
   }

   public Object[] getElements(Object var1) {
      return EMPTY_ARRAY;
   }

   public void initialize() {
      this.tableViewer = ((GTableView)this.gView).getTableViewer();
   }

   public void inputChanged(Viewer var1, Object var2, Object var3) {
      if (var1 instanceof CustomTableViewer) {
         this.tableViewer = (CustomTableViewer)var1;
      }
   }

   public void setActive(boolean var1) {
      if (var1 && this.needsRefresh) {
         this.gView.clearAll();
         this.needsRefresh = false;
      }
   }

   public void setVisible(boolean var1) {
      if (var1 && this.needsRefresh) {
         this.gView.clearAll();
         this.needsRefresh = false;
      }
   }

   public void update(MyObservable var1, Object var2, int var3) {
   }

   public void updateDisplay() {
   }
}
