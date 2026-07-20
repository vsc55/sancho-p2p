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

   public synchronized Object getSFElement(int index) {
      // The table widget's selection can momentarily reference a row the model has
      // already cleared — e.g. a persisted/auto selection firing at startup before
      // the content provider is populated (sfList still empty) — so bounds-check
      // instead of letting ArrayList.get throw IndexOutOfBoundsException. Callers
      // (getSelectionFromWidget) already skip a null element.
      return index >= 0 && index < this.sfList.size() ? this.sfList.get(index) : null;
   }

   public void setNeedsRefresh(boolean needsRefresh) {
      this.needsRefresh = needsRefresh;
   }

   public synchronized int getSFIndex(Object element) {
      return this.sfList.indexOf(element);
   }

   public synchronized int[] getIndicesOf(List list) {
      int[] indices = new int[list.size()];
      int count = 0;

      for (Object element : list) {
         int index = this.sfList.indexOf(element);
         if (index != -1) {
            indices[count++] = index;
         }
      }

      if (count < indices.length) {
         System.arraycopy(indices, 0, indices = new int[count], 0, count);
      }

      return indices;
   }

   public synchronized void updateElement(int index) {
      // ILazyContentProvider hook: SWT calls this when it needs to render a virtual
      // row (scrolled into view, or after Table.clear()). It was EMPTY, so those
      // rows never got their element and showed stale/blank data (the top rows that
      // "stuck" and ignored sorting). Render the current sfList element for the row.
      if (index >= 0 && index < this.sfList.size() && this.tableViewer != null) {
         this.updateElement(this.tableViewer.getTable().getItem(index), index);
      }
   }

   public synchronized void updateElement(TableItem item, int index) {
      ((CustomTableViewer)this.gView.getViewer()).replace(this.sfList.get(index), item);
   }

   public synchronized void updateSorted(boolean force) {
      ArrayList oldList = this.sfList;
      Object[] sortedChildren = this.tableViewer.getSortedChildren(this.tableViewer.getInput());
      int count = sortedChildren.length;
      this.sfList = new ArrayList(count);

      for (int i = 0; i < count; i++) {
         this.sfList.add(sortedChildren[i]);
      }

      TIntArrayList removedIndices = new TIntArrayList();
      ArrayList removedElements = new ArrayList();
      int index = 0;

      for (Object element : oldList) {
         if (index >= count || sortedChildren[index] != element || force) {
            removedElements.add(element);
            removedIndices.add(index);
         }

         index++;
      }

      this.tableViewer.myClear(count, removedIndices.toNativeArray(), removedElements.toArray());
   }

   public GTableContentProvider(GView gView) {
      this.gView = gView;
   }

   public void dispose() {
   }

   public Object[] getElements(Object input) {
      return EMPTY_ARRAY;
   }

   public void initialize() {
      this.tableViewer = ((GTableView)this.gView).getTableViewer();
   }

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      if (viewer instanceof CustomTableViewer) {
         this.tableViewer = (CustomTableViewer)viewer;
      }
   }

   public void setActive(boolean active) {
      if (active && this.needsRefresh) {
         this.gView.clearAll();
         this.needsRefresh = false;
      }
   }

   public void setVisible(boolean visible) {
      if (visible && this.needsRefresh) {
         this.gView.clearAll();
         this.needsRefresh = false;
      }
   }

   public void update(MyObservable observable, Object arg, int type) {
   }

   public void updateDisplay() {
   }
}
