package org.eclipse.jface.viewers;

import gnu.trove.TIntArrayList;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.eclipse.jface.util.IOpenEventListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import sancho.utility.SwissArmy;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.transfer.downloads.DownloadTreeLabelProvider;
import sancho.view.viewer.table.GTableLabelProvider;
import sancho.view.viewer.tree.GTreeContentProvider;

public class CustomTreeViewer extends TreeViewer implements ICustomViewer {
   private Hashtable chunkImageDataCache = new Hashtable();
   private Hashtable parentToItemMap = new Hashtable();
   private Hashtable parentToChildrenMap = new Hashtable();
   private TIntArrayList expandedIndex = new TIntArrayList();
   private boolean activeEditors;
   private int chunksColumn = -1;
   private int[] columnIDs;
   private boolean followSelection;

   protected Object[] getRawChildren(Object input) {
      GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
      if (this.equals(input, this.getRoot())) {
         Object[] elements = null;
         if (input != null && contentProvider != null) {
            elements = contentProvider.getElements(input);
         }

         return elements != null ? elements : new Object[0];
      } else {
         Object[] children = contentProvider.getChildren(input);
         return children != null ? children : new Object[0];
      }
   }

   public CustomTreeViewer(Composite parent, int style) {
      super(parent, style);
      Tree tree = this.getTree();
      tree.addTreeListener(new TreeListener() {
         public void treeCollapsed(TreeEvent event) {
            TreeItem item = (TreeItem)event.item;
            int index = getTree().indexOf(item);
            if (index >= 0) {
               removeExpanded(index);
            }
         }

         public void treeExpanded(TreeEvent event) {
            TreeItem item = (TreeItem)event.item;
            int index = getTree().indexOf(item);
            if (index >= 0) {
               addExpanded(index);
            }
         }
      });
   }

   protected void myHandleOpen(SelectionEvent event) {
      super.handleOpen(event);
   }

   protected void hookControl(Control control) {
      control.addDisposeListener(new DisposeListener() {
         public void widgetDisposed(DisposeEvent event) {
            handleDispose(event);
         }
      });
      OpenStrategy openStrategy = new OpenStrategy(control);
      openStrategy.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent event) {
            handleSelect(event);
         }

         public void widgetDefaultSelected(SelectionEvent event) {
            handleDoubleSelect(event);
         }
      });
      openStrategy.addPostSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent event) {
            handlePostSelect(event);
         }
      });
      openStrategy.addOpenListener(new IOpenEventListener() {
         public void handleOpen(SelectionEvent event) {
            myHandleOpen(event);
         }
      });
      this.addTreeListener(control, new TreeListener() {
         public void treeExpanded(TreeEvent event) {
            handleTreeExpand(event);
         }

         public void treeCollapsed(TreeEvent event) {
            handleTreeCollapse(event);
         }
      });
      Tree tree = (Tree)control;
      tree.addMouseListener(new MouseAdapter() {
         public void mouseDown(MouseEvent event) {
            /* No-op: modern JFace TableViewer/TreeViewer create a default ColumnViewerEditor
            // whose activation strategy starts cell editing on click, so the 2008-era
            // tableViewerImpl.handleMouseDown() forwarding is no longer needed. */
         }
      });
      if ((tree.getStyle() & 268435456) != 0) {
         tree.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
               unmapAllElements();
            }
         });
         tree.addListener(36, new Listener() {
            public void handleEvent(Event event) {
               GTreeContentProvider contentProvider = (GTreeContentProvider)getContentProvider();
               Tree tree = (Tree)event.widget;
               int index = event.index;
               TreeItem item = (TreeItem)event.item;
               TreeItem parentItem = item.getParentItem();
               Object parentElement;
               if (parentItem != null) {
                  parentElement = parentItem.getData();
                  if (parentElement == null) {
                     int parentIndex = tree.indexOf(parentItem);
                     parentElement = contentProvider.getSFElement(parentIndex);
                  }
               } else {
                  parentElement = getInput();
               }

               contentProvider.updateElement(item, parentElement, index);
            }
         });
      }
   }

   public boolean isExpanded(int index) {
      return this.expandedIndex.contains(index);
   }

   public void addExpanded(int index) {
      if (!this.expandedIndex.contains(index)) {
         this.expandedIndex.add(index);
      }
   }

   public void removeExpanded(int index) {
      int position = this.expandedIndex.indexOf(index);
      if (position >= 0) {
         this.expandedIndex.remove(position);
      }
   }

   public int[] getExpandedInts() {
      return this.expandedIndex.toNativeArray();
   }

   public void setExpandedInts(int[] indices) {
      this.expandedIndex.clear();
      this.expandedIndex.add(indices);
   }

   public void expandAll() {
      this.getTree().setRedraw(false);
      GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
      int[] indexesWithChildren = contentProvider.getIndexesWithChildren();

      for (int i = 0; i < indexesWithChildren.length; i++) {
         this.expand(indexesWithChildren[i]);
      }

      this.getTree().setRedraw(true);
   }

   public void collapseAll() {
      this.getTree().setRedraw(false);
      Tree tree = this.getTree();
      int count = tree.getItemCount();

      for (int i = 0; i < count; i++) {
         this.collapse(i);
      }

      this.getTree().setRedraw(true);
   }

   public boolean getExpandedState(Object element) {
      GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
      int index = contentProvider.getSFIndex(element);
      return this.isExpanded(index);
   }

   public void collapseToLevel(Object element, int level) {
      GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
      int index = contentProvider.getSFIndex(element);
      if (index >= 0) {
         Tree tree = this.getTree();
         int count = tree.getItemCount();
         if (index < count) {
            TreeItem item = tree.getItem(index);
            item.setExpanded(false);
         }

         this.removeExpanded(index);
      }
   }

   public void expand(int index) {
      Tree tree = this.getTree();
      int count = tree.getItemCount();
      if (index < count) {
         TreeItem item = tree.getItem(index);
         item.setExpanded(true);
         this.addExpanded(index);
      }
   }

   public void collapse(int index) {
      if (this.isExpanded(index)) {
         Tree tree = this.getTree();
         int count = tree.getItemCount();
         if (index < count) {
            TreeItem item = tree.getItem(index);
            item.setExpanded(false);
            this.removeExpanded(index);
         }
      }
   }

   public void expandToLevel(Object element, int level) {
      GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
      int index = contentProvider.getSFIndex(element);
      if (index >= 0 && contentProvider.hasChildren(element)) {
         this.expand(index);
      }
   }

   public void refresh(Object element) {
      this.preservingSelection(new Runnable() {
         public void run() {
            myInternalVirtualRefreshSome();
         }
      });
   }

   public void add(Object parentElement, Object[] childElements) {
      this.preservingSelection(new Runnable() {
         public void run() {
            myInternalVirtualRefreshSome();
         }
      });
   }

   public void remove(Object[] elements) {
      this.removeFromCache(elements);
      this.preservingSelection(new Runnable() {
         public void run() {
            myInternalVirtualRefreshSome();
         }
      });
   }

   public void update(Object[] elements, String[] properties) {
      boolean changed = false;

      for (int i = 0; i < elements.length; i++) {
         if (this.myUpdate(elements[i], properties)) {
            changed = true;
         }
      }

      if (changed) {
         this.preservingSelection(new Runnable() {
            public void run() {
               myInternalVirtualRefreshSome();
            }
         });
      }
   }

   public void update(Object element, String[] properties) {
   }

   public boolean myUpdate(Object element, String[] properties) {
      TreeItem item = (TreeItem)this.parentToItemMap.get(element);
      if (item == null) {
         return this.passesFilters(element);
      } else {
         return this.failsFilters(element) ? true : this.internalUpdate(item, element, properties);
      }
   }

   protected boolean internalUpdate(Item item, Object element, String[] properties) {
      boolean refilter = false;
      if (properties != null) {
         for (int i = 0; i < properties.length; i++) {
            refilter = this.needsRefilter(element, properties[i]);
            if (refilter) {
               break;
            }
         }
      }

      boolean isLabelProperty;
      if (properties == null) {
         isLabelProperty = true;
      } else {
         isLabelProperty = false;
         IBaseLabelProvider labelProvider = this.getLabelProvider();

         for (int i = 0; i < properties.length; i++) {
            isLabelProperty = labelProvider.isLabelProperty(element, properties[i]);
            if (isLabelProperty) {
               break;
            }
         }
      }

      if (isLabelProperty) {
         this.doUpdateItem(item, element);
      }

      return refilter;
   }

   public ISelection getSelection() {
      Control control = this.getControl();
      if (control != null && !control.isDisposed()) {
         Item[] selectedItems = this.getSelection(control);
         ArrayList selection = new ArrayList(selectedItems.length);
         TreeItem[] allItems = this.getTree().getItems();
         GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
         int searchStart = 0;

         for (int i = 0; i < selectedItems.length; i++) {
            TreeItem item = (TreeItem)selectedItems[i];
            if (item.getParentItem() == null) {
               Object data = item.getData();
               if (data != null) {
                  selection.add(data);
               } else {
                  for (int j = searchStart; j < allItems.length; j++) {
                     if (item == allItems[j]) {
                        selection.add(contentProvider.getSFElement(j));
                        searchStart = j;
                        break;
                     }
                  }
               }
            } else {
               Object childData = item.getData();
               if (childData != null) {
                  selection.add(childData);
               }
            }
         }

         return new StructuredSelection((List)selection);
      } else {
         return TreeSelection.EMPTY;
      }
   }

   protected void setSelectionToWidget(List selection, boolean reveal) {
      if (selection == null) {
         this.getTree().deselectAll();
      } else {
         this.virtualSetSelectionToWidget(selection, reveal);
      }
   }

   protected void virtualSetSelectionToWidget(List selection, boolean reveal) {
      int size = selection.size();
      if (size != 0) {
         Tree tree = this.getTree();
         Object root = this.getRoot();
         ArrayList items = new ArrayList(size);
         GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
         TreeItem itemToShow = null;

         for (Object element : selection) {
            TreeItem item = (TreeItem)this.parentToItemMap.get(element);
            if (item != null) {
               items.add(item);
            } else {
               int index = contentProvider.getSFIndex(element);
               if (index >= 0) {
                  item = tree.getItem(index);
                  items.add(item);
                  if (item.getData() == null) {
                     contentProvider.updateElement(item, root, index);
                  }
               }
            }

            if (itemToShow == null && item != null) {
               itemToShow = item;
            }
         }

         if (items.size() > 0) {
            TreeItem[] itemArray = new TreeItem[items.size()];
            items.toArray(itemArray);
            if (this.followSelection) {
               tree.setSelection(itemArray);
            } else {
               tree.deselectAll();

               for (int i = 0; i < itemArray.length; i++) {
                  tree.select(itemArray[i]);
               }
            }
         } else {
            tree.deselectAll();
         }

         if (reveal && itemToShow != null) {
            tree.showItem(itemToShow);
         }
      }
   }

   protected void preservingSelection(Runnable updateCode) {
      // See CustomTableViewer: delegate to the equivalent modern JFace method.
      super.preservingSelection(updateCode);
   }

   public synchronized void removeFromCache(Object[] elements) {
      for (int i = 0; i < elements.length; i++) {
         this.removeFromCache(elements[i]);
      }
   }

   public synchronized void removeFromCache(Object element) {
      this.chunkImageDataCache.remove(element);
      // The download tree caches its chunk images in the label provider, not here;
      // prune that (the real) map too so it doesn't grow without bound.
      IBaseLabelProvider labelProvider = this.getLabelProvider();
      if (labelProvider instanceof DownloadTreeLabelProvider) {
         ((DownloadTreeLabelProvider)labelProvider).removeFromCache(element);
      }
   }

   public void replace(Object element, TreeItem item) {
      TreeItem staleItem = (TreeItem)this.parentToItemMap.get(element);
      if (staleItem != null && staleItem != item) {
         this.removeFromCache(element);
         if (!staleItem.isDisposed()) {
            staleItem.setData(null);
            staleItem.setItemCount(0);
            Tree tree = this.getTree();
            int staleIndex = tree.indexOf(staleItem);
            if (staleIndex != -1) {
               tree.clear(staleIndex, true);
            }
         }
      }

      this.parentToItemMap.put(element, item);
      item.setData(element);
      this.doUpdateItem(item, element);
   }

   public void replaceChild(Object parent, Object childElement, TreeItem item) {
      Hashtable childMap = (Hashtable)this.parentToChildrenMap.get(parent);
      if (childMap == null) {
         childMap = new Hashtable();
         this.parentToChildrenMap.put(parent, childMap);
      }

      TreeItem staleItem = (TreeItem)childMap.get(childElement);
      if (staleItem != null) {
         TreeItem parentItem = item.getParentItem();
         int childIndex = parentItem.indexOf(item);
         if (!staleItem.isDisposed()) {
            parentItem = staleItem.getParentItem();
            childIndex = parentItem.indexOf(staleItem);
         }

         if (staleItem != item && !staleItem.isDisposed()) {
            staleItem.setData(null);
            parentItem = staleItem.getParentItem();
            childIndex = parentItem.indexOf(staleItem);
            if (childIndex != -1) {
               parentItem.clear(childIndex, true);
            }
         }
      }

      childMap.put(childElement, item);
      item.setData(childElement);
      this.doUpdateItem(item, childElement);
   }

   public void addChild(Object parent, Object childElement) {
      GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
      TreeItem parentItem = (TreeItem)this.parentToItemMap.get(parent);
      if (parentItem != null) {
         Hashtable childMap = (Hashtable)this.parentToChildrenMap.get(parent);
         if (childMap == null) {
            Object[] sortedChildren = this.getSortedChildren(parent);
            parentItem.setItemCount(sortedChildren.length);
         } else {
            int currentCount = parentItem.getItemCount();
            Object[] sortedChildren = this.getSortedChildren(parent);
            int childCount = sortedChildren.length;
            if (childCount > currentCount) {
               int insertIndex = -1;

               for (int i = 0; i < childCount; i++) {
                  if (sortedChildren[i] == childElement) {
                     insertIndex = i;
                     break;
                  }
               }

               if (insertIndex > -1 && insertIndex <= currentCount) {
                  new TreeItem(parentItem, parentItem.getStyle(), insertIndex);
               } else {
                  contentProvider = (GTreeContentProvider)this.getContentProvider();
                  int parentIndex = contentProvider.getSFIndex(parent);
                  if (parentIndex >= 0) {
                     this.myClear(-1, false, new int[]{parentIndex}, new Object[]{parent});
                  }
               }
            }
         }
      }
   }

   public void removeChild(Object parent, Object childElement) {
      Hashtable childMap = (Hashtable)this.parentToChildrenMap.get(parent);
      if (childMap != null) {
         TreeItem item = (TreeItem)childMap.remove(childElement);
         if (item != null) {
            if (childMap.size() == 0) {
               this.parentToChildrenMap.remove(parent);
            }

            this.removeFromCache(childElement);
            if (!item.isDisposed()) {
               item.setData(null);
               item.dispose();
            }
         } else {
            GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
            int parentIndex = contentProvider.getSFIndex(parent);
            if (parentIndex >= 0) {
               this.myClear(-1, false, new int[]{parentIndex}, new Object[]{parent});
            }
         }
      } else {
         GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
         int parentIndex = contentProvider.getSFIndex(parent);
         if (parentIndex >= 0) {
            this.myClear(-1, false, new int[]{parentIndex}, new Object[]{parent});
         }
      }
   }

   public void setChildCount(TreeItem item, int count) {
      item.setItemCount(count);
   }

   public void setChildCount(Object element, int count) {
      TreeItem item = (TreeItem)this.parentToItemMap.get(element);
      if (item != null) {
         item.setItemCount(count);
      }
   }

   protected boolean passesFilters(Object element) {
      ViewerFilter[] filters = this.getFilters();
      if (filters.length == 0) {
         return true;
      } else {
         Object root = this.getRoot();

         for (int i = 0; i < filters.length; i++) {
            if (filters[i].select(this, root, element)) {
               return true;
            }
         }

         return false;
      }
   }

   protected boolean failsFilters(Object element) {
      ViewerFilter[] filters = this.getFilters();
      Object root = this.getRoot();

      for (int i = 0; i < filters.length; i++) {
         if (!filters[i].select(this, root, element)) {
            return true;
         }
      }

      return false;
   }

   protected void inputChanged(Object input, Object oldInput) {
      this.refresh();
   }

   public void clearAll() {
      this.preservingSelection(new Runnable() {
         public void run() {
            myInternalVirtualRefreshAll();
         }
      });
   }

   public void myClear(int newItemCount, boolean collapse, int[] indices, Object[] elements) {
      Tree tree = this.getTree();
      int currentItemCount = tree.getItemCount();
      if (newItemCount < 0) {
         newItemCount = currentItemCount;
      }

      ArrayList clearedItems = new ArrayList();
      TIntArrayList clearedIndices = new TIntArrayList();

      for (int i = 0; i < elements.length; i++) {
         Object element = elements[i];
         TreeItem item = (TreeItem)this.parentToItemMap.remove(element);
         if (item != null) {
            this.removeFromCache(element);
            clearedIndices.add(indices[i]);
            clearedItems.add(item);
            item.setData(null);
         } else if (!collapse) {
            clearedIndices.add(indices[i]);
         }

         Hashtable childMap = (Hashtable)this.parentToChildrenMap.remove(elements[i]);
         if (childMap != null) {
            Object[] childKeys = SwissArmy.toArray(childMap.keySet());

            for (int k = 0; k < childKeys.length; k++) {
               this.removeFromCache(childKeys[k]);
               TreeItem childItem = (TreeItem)childMap.get(childKeys[k]);
               childItem.setData(null);
            }
         }
      }

      if (newItemCount >= 0 && currentItemCount != newItemCount) {
         tree.setItemCount(newItemCount);
      }

      int[] clearedArray = clearedIndices.toNativeArray();

      for (int i = 0; i < clearedArray.length; i++) {
         int index = clearedArray[i];
         if (index >= newItemCount) {
            break;
         }

         if (collapse) {
            TreeItem item = (TreeItem)clearedItems.get(i);
            item.setExpanded(false);
            item.setItemCount(0);
         }

         if (index < newItemCount) {
            tree.clear(index, true);
         }
      }

      // Explicitly render the visible top-level rows from the freshly-sorted content
      // provider, mirroring CustomTableViewer.myClear. The positional-diff clear above
      // relies on SWT lazy SetData re-firing, which the modernized SWT/JFace does not
      // do reliably, so top rows could otherwise render stale ("stuck") after a column
      // sort or filter switch. Bounded by the screen height (child rows keep using the
      // expand SetData path).
      GTreeContentProvider contentProvider = (GTreeContentProvider)this.getContentProvider();
      Object input = this.getInput();
      if (input != null) {
         int itemHeight = tree.getItemHeight();
         TreeItem topItem = tree.getTopItem();
         int topIndex = topItem != null ? tree.indexOf(topItem) : 0;
         if (topIndex < 0) {
            topIndex = 0;
         }

         int visibleCount = itemHeight > 0 ? tree.getClientArea().height / itemHeight + 2 : newItemCount;

         for (int row = topIndex; row < newItemCount && row < topIndex + visibleCount; row++) {
            contentProvider.updateElement(tree.getItem(row), input, row);
         }
      }
   }

   protected void myInternalVirtualRefreshAll() {
      ((GTreeContentProvider)this.getContentProvider()).updateSorted(true);
   }

   protected void myInternalVirtualRefreshSome() {
      ((GTreeContentProvider)this.getContentProvider()).updateSorted(false);
   }

   public void updateSelection(ISelection selection) {
      super.updateSelection(selection);
   }

   public void updateDisplay() {
      this.followSelection = PreferenceLoader.loadBoolean("followSelection");
   }

   public Object[] getSortedChildren(Object parentElement) {
      Object[] children = this.getFilteredChildren(parentElement);
      ViewerSorter sorter = this.getSorter();
      if (sorter != null) {
         children = (Object[])children.clone();
         sorter.sort(this, children);
      }

      return children;
   }

   public void setChunksColumn(int column) {
      if (this.chunksColumn != column) {
         this.chunksColumn = column;
      }
   }

   public int getChunksColumn() {
      return this.chunksColumn;
   }

   public void setEditors(boolean enabled) {
      this.activeEditors = enabled;
   }

   public boolean getEditors() {
      return this.activeEditors;
   }

   public void setColumnIDs(String columns) {
      this.columnIDs = new int[columns.length()];

      for (int i = 0; i < columns.length(); i++) {
         this.columnIDs[i] = columns.charAt(i) - 'A';
      }
   }

   public int[] getColumnIDs() {
      return this.columnIDs;
   }

   protected void doUpdateItem(Item item, Object element) {
      TreeItem treeItem = (TreeItem)item;
      GTableLabelProvider labelProvider = (GTableLabelProvider)this.getLabelProvider();
      int columnCount = this.getTree().getColumnCount();
      boolean hasFontProvider = labelProvider instanceof ITableFontProvider;

      for (int column = 0; column < columnCount; column++) {
         treeItem.setBackground(column, labelProvider.getBackground(element, column));
         treeItem.setForeground(column, labelProvider.getForeground(element, column));
         if (!this.activeEditors || column != this.chunksColumn) {
            if (hasFontProvider) {
               treeItem.setFont(column, ((ITableFontProvider)labelProvider).getFont(element, column));
            }

            treeItem.setText(column, labelProvider.getColumnText(element, column));
            treeItem.setImage(column, labelProvider.getColumnImage(element, column));
         }
      }
   }
}
