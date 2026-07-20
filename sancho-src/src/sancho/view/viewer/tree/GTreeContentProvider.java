package sancho.view.viewer.tree;

import gnu.trove.TIntArrayList;
import java.util.ArrayList;
import org.eclipse.jface.viewers.CustomTreeViewer;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.TreeItem;
import sancho.utility.MyObservable;
import sancho.utility.MyObserver;
import sancho.view.viewer.GView;
import sancho.view.viewer.IGContentProvider;

public abstract class GTreeContentProvider implements IGContentProvider, ITreeContentProvider, ILazyTreeContentProvider, MyObserver {
   protected static final Object[] EMPTY_ARRAY = new Object[0];
   protected GView gView;
   protected boolean needsRefresh;
   protected CustomTreeViewer treeViewer;
   protected ArrayList sfList = new ArrayList();

   public void setNeedsRefresh(boolean needsRefresh) {
      this.needsRefresh = needsRefresh;
   }

   public synchronized Object getSFElement(int index) {
      // Same guard as GTableContentProvider: the widget selection can reference a
      // row the model already cleared, so bounds-check instead of letting
      // ArrayList.get throw IndexOutOfBoundsException. Callers tolerate null.
      return index >= 0 && index < this.sfList.size() ? this.sfList.get(index) : null;
   }

   public synchronized int getSFIndex(Object element) {
      return this.sfList.indexOf(element);
   }

   public ArrayList getOldExpanded() {
      ArrayList expanded = new ArrayList();
      int[] expandedInts = this.treeViewer.getExpandedInts();

      for (int i = 0; i < expandedInts.length; i++) {
         expanded.add(this.getSFElement(expandedInts[i]));
      }

      return expanded;
   }

   public void setOldExpanded(ArrayList expanded) {
      TIntArrayList indexes = new TIntArrayList();

      for (Object element : expanded) {
         int index = this.getSFIndex(element);
         if (index >= 0) {
            indexes.add(index);
         }
      }

      this.treeViewer.setExpandedInts(indexes.toNativeArray());
   }

   public synchronized int[] getAllNumChildren() {
      Object[] elements = this.sfList.toArray();
      int[] numChildren = new int[elements.length];

      for (int i = 0; i < elements.length; i++) {
         numChildren[i] = this.getNumChildren(elements[i]);
      }

      return numChildren;
   }

   public synchronized void updateSorted(boolean force) {
      ArrayList expanded = this.getOldExpanded();
      ArrayList oldList = this.sfList;
      Object[] sortedChildren = this.treeViewer.getSortedChildren(this.treeViewer.getInput());
      int count = sortedChildren.length;
      this.sfList = new ArrayList(count);

      for (int i = 0; i < count; i++) {
         this.sfList.add(sortedChildren[i]);
      }

      this.setOldExpanded(expanded);
      TIntArrayList removedIndexes = new TIntArrayList();
      ArrayList removedElements = new ArrayList();
      int index = 0;

      for (Object element : oldList) {
         if (index >= count || sortedChildren[index] != element || force) {
            removedElements.add(element);
            removedIndexes.add(index);
         }

         index++;
      }

      this.treeViewer.myClear(count, true, removedIndexes.toNativeArray(), removedElements.toArray());
   }

   public GTreeContentProvider(GView gView) {
      this.gView = gView;
   }

   public void dispose() {
   }

   public Object[] getChildren(Object element) {
      return EMPTY_ARRAY;
   }

   public Object getParent(Object element) {
      return null;
   }

   public void updateChildCount(Object element, int count) {
   }

   public void updateChild(Object element) {
      int index = this.getSFIndex(element);
      if (index >= 0) {
         if (this.treeViewer.isExpanded(index)) {
            this.treeViewer.myClear(-1, false, new int[]{index}, new Object[]{element});
         } else {
            Object[] sortedChildren = this.treeViewer.getSortedChildren(element);
            this.treeViewer.setChildCount(element, sortedChildren.length);
         }
      }
   }

   public synchronized int[] getIndexesWithChildren() {
      TIntArrayList indexes = new TIntArrayList();
      int index = 0;

      for (Object element : this.sfList) {
         if (this.hasChildren(element)) {
            indexes.add(index);
         }

         index++;
      }

      return indexes.toNativeArray();
   }

   public void updateElement(Object element, int index) {
   }

   public void updateElement(TreeItem item, Object element, int index) {
      if (element == this.treeViewer.getInput()) {
         Object sfElement = this.getSFElement(index);
         this.treeViewer.replace(sfElement, item);
         boolean expanded = this.treeViewer.isExpanded(index);
         if (this.hasChildren(sfElement)) {
            Object[] sortedChildren = this.treeViewer.getSortedChildren(sfElement);
            this.treeViewer.setChildCount(item, sortedChildren.length);
         } else {
            this.treeViewer.setChildCount(item, 0);
         }

         this.treeViewer.replace(sfElement, item);
         if (item.getExpanded() != expanded) {
            item.setExpanded(expanded);
         }
      } else if (this.hasChildren(element)) {
         Object[] sortedChildren = this.treeViewer.getSortedChildren(element);
         if (index < sortedChildren.length) {
            int sfIndex = this.getSFIndex(element);
            if (sfIndex >= 0) {
               Object child = sortedChildren[index];
               this.treeViewer.replaceChild(element, child, item);
            }
         }
      }
   }

   public boolean hasChildren(Object element) {
      return false;
   }

   public abstract int getNumChildren(Object element);

   public void initialize() {
      this.treeViewer = ((GTreeView)this.gView).getTreeViewer();
   }

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      if (viewer instanceof CustomTreeViewer) {
         this.treeViewer = (CustomTreeViewer)viewer;
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

   public abstract void onUpdate(MyObservable observable, Object data, int id);

   public void update(MyObservable observable, Object data, int id) {
      if (this.gView != null && !this.gView.isDisposed()) {
         if (this.gView.isVisible() && this.gView.isActive()) {
            // asyncExec (not syncExec) so this observer callback doesn't block the
            // core reader thread on the UI thread; matches the table content
            // providers and avoids a potential deadlock.
            this.gView.getComposite().getDisplay().asyncExec(new Runnable() {
               public void run() {
                  GTreeContentProvider.this.onUpdate(observable, data, id);
               }
            });
         } else {
            this.needsRefresh = true;
         }
      }
   }

   public void updateDisplay() {
   }
}
