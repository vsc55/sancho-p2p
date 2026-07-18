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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.viewer.table.GTableContentProvider;
import sancho.view.viewer.table.GTableLabelProvider;

public class CustomTableViewer extends TableViewer implements ICustomViewer {
   private int[] columnIDs;
   private Hashtable parentToItemMap = new Hashtable();
   private boolean followSelection;

   protected void initializeVirtualManager(int style) {
   }

   protected Object[] getRawChildren(Object input) {
      Object[] elements = null;
      if (input != null) {
         IStructuredContentProvider contentProvider = (IStructuredContentProvider)this.getContentProvider();
         if (contentProvider != null) {
            elements = contentProvider.getElements(input);
         }
      }

      return elements != null ? elements : new Object[0];
   }

   public void updateSelection(ISelection selection) {
      super.updateSelection(selection);
   }

   public void updateDisplay() {
      this.followSelection = PreferenceLoader.loadBoolean("followSelection");
   }

   public void replace(Object element, TableItem item) {
      // If this element is still associated with a DIFFERENT TableItem (e.g. after a
      // re-sort/re-filter moved it to another row), disassociate that stale item and
      // invalidate its row so SWT re-renders it with its correct element. Without
      // this the element shows in two rows (duplicate) and the stale row "sticks"
      // and ignores sorting. The sibling CustomTreeViewer.replace has this guard;
      // the decompiled/ported table version had lost it.
      TableItem staleItem = (TableItem)this.parentToItemMap.get(element);
      if (staleItem != null && staleItem != item && !staleItem.isDisposed()) {
         staleItem.setData(null);
         Table table = this.getTable();
         int staleIndex = table.indexOf(staleItem);
         if (staleIndex != -1) {
            table.clear(staleIndex);
         }
      }

      this.parentToItemMap.put(element, item);
      item.setData(element);
      this.doUpdateItem(item, element);
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
      Table table = (Table)control;
      table.addMouseListener(new MouseAdapter() {
         public void mouseDown(MouseEvent event) {
            /* No-op: modern JFace TableViewer/TreeViewer create a default ColumnViewerEditor
            // whose activation strategy starts cell editing on click, so the 2008-era
            // tableViewerImpl.handleMouseDown() forwarding is no longer needed. */
         }
      });
      table.addListener(36, new Listener() {
         public void handleEvent(Event event) {
            GTableContentProvider contentProvider = (GTableContentProvider)getContentProvider();
            int index = event.index;
            TableItem item = (TableItem)event.item;
            contentProvider.updateElement(item, index);
         }
      });
   }

   public void clearAll() {
      this.preservingSelection(new Runnable() {
         public void run() {
            myInternalVirtualRefreshAll();
         }
      });
   }

   public void myClear(int newItemCount, int[] indices, Object[] elements) {
      Table table = this.getTable();
      int currentItemCount = table.getItemCount();
      if (newItemCount < 0) {
         newItemCount = currentItemCount;
      }

      TIntArrayList clearedIndices = new TIntArrayList();

      for (int i = 0; i < elements.length; i++) {
         Object element = elements[i];
         TableItem item = (TableItem)this.parentToItemMap.remove(element);
         if (item != null) {
            clearedIndices.add(indices[i]);
            item.setData(null);
         }
      }

      if (newItemCount >= 0 && currentItemCount != newItemCount) {
         table.setItemCount(newItemCount);
      }

      int[] clearedArray = clearedIndices.toNativeArray();

      for (int i = 0; i < clearedArray.length; i++) {
         int index = clearedArray[i];
         if (index >= newItemCount) {
            break;
         }

         table.clear(index);
      }

      // SWT's lazy SetData isn't effectively wired in this custom viewer, and a row
      // that never rendered has no item mapping, so the positional-diff clear above
      // can never re-render it (it stays stale — the "stuck" top rows). Explicitly
      // render the currently visible rows from the freshly-sorted content provider,
      // which is bounded by the screen height regardless of the table size.
      IContentProvider contentProvider = this.getContentProvider();
      if (contentProvider instanceof GTableContentProvider) {
         GTableContentProvider tableContentProvider = (GTableContentProvider)contentProvider;
         int itemHeight = table.getItemHeight();
         int topIndex = table.getTopIndex();
         int visibleCount = itemHeight > 0 ? table.getClientArea().height / itemHeight + 2 : newItemCount;
         for (int row = topIndex; row < newItemCount && row < topIndex + visibleCount; row++) {
            tableContentProvider.updateElement(table.getItem(row), row);
         }
      }
   }

   protected void setSelectionToWidget(ISelection selection, boolean reveal) {
      this.virtualSetSelectionToWidget(((IStructuredSelection)selection).toList(), reveal);
   }

   public void add(Object[] elements) {
      this.preservingSelection(new Runnable() {
         public void run() {
            myInternalVirtualRefreshSome();
         }
      });
   }

   public void remove(Object[] elements) {
      this.preservingSelection(new Runnable() {
         public void run() {
            myInternalVirtualRefreshSome();
         }
      });
   }

   public void refresh(Object element) {
      this.preservingSelection(new Runnable() {
         public void run() {
            myInternalVirtualRefreshSome();
         }
      });
   }

   protected void myInternalVirtualRefreshSome() {
      ((GTableContentProvider)this.getContentProvider()).updateSorted(false);
   }

   protected void myInternalVirtualRefreshAll() {
      ((GTableContentProvider)this.getContentProvider()).updateSorted(true);
   }

   public boolean updateOrRefresh(Object[] elements, String[] properties) {
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

      return changed;
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
      TableItem item = (TableItem)this.parentToItemMap.get(element);
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

      for (int i = 0; i < filters.length; i++) {
         if (!filters[i].select(this, this.getRoot(), element)) {
            return true;
         }
      }

      return false;
   }

   protected void preservingSelection(Runnable updateCode) {
      // Original re-implemented the base logic via the now-private inChange /
      // restoreSelection fields; modern JFace preservingSelection() is equivalent.
      super.preservingSelection(updateCode);
   }

   protected List getSelectionFromWidget() {
      int[] selectionIndices = this.getTable().getSelectionIndices();
      GTableContentProvider contentProvider = (GTableContentProvider)this.getContentProvider();
      ArrayList selection = new ArrayList(selectionIndices.length);

      for (int i = 0; i < selectionIndices.length; i++) {
         Object element = contentProvider.getSFElement(selectionIndices[i]);
         if (element != null) {
            selection.add(element);
         }
      }

      return selection;
   }

   protected void virtualSetSelectionToWidget(List selection, boolean reveal) {
      int size = selection.size();
      if (size != 0) {
         Table table = this.getTable();
         TableItem itemToShow = null;
         GTableContentProvider contentProvider = (GTableContentProvider)this.getContentProvider();
         TIntArrayList indices = new TIntArrayList(size);

         for (Object element : selection) {
            TableItem item = (TableItem)this.parentToItemMap.get(element);
            int index = contentProvider.getSFIndex(element);
            if (index >= 0) {
               indices.add(index);
            }

            if (item == null && index >= 0) {
               item = table.getItem(index);
               if (item.getData() == null) {
                  contentProvider.updateElement(item, index);
               }
            }

            if (itemToShow == null && item != null) {
               itemToShow = item;
            }
         }

         if (this.followSelection) {
            table.setSelection(indices.toNativeArray());
         } else {
            table.deselectAll();
            table.select(indices.toNativeArray());
         }

         if (reveal && itemToShow != null) {
            table.showItem(itemToShow);
         }
      }
   }

   public Object[] getSortedChildren(Object parentElement) {
      return super.getSortedChildren(parentElement);
   }

   public CustomTableViewer(Composite parent, int style) {
      super(parent, style);
   }

   public void closeAllTTE() {
   }

   public void setEditors(boolean enabled) {
   }

   public boolean getEditors() {
      return false;
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
      TableItem tableItem = (TableItem)item;
      GTableLabelProvider labelProvider = (GTableLabelProvider)this.getLabelProvider();
      int columnCount = this.getTable().getColumnCount();
      boolean hasFontProvider = labelProvider instanceof ITableFontProvider;

      for (int column = 0; column < columnCount; column++) {
         tableItem.setBackground(column, labelProvider.getBackground(element, column));
         tableItem.setForeground(column, labelProvider.getForeground(element, column));
         if (hasFontProvider) {
            tableItem.setFont(column, ((ITableFontProvider)labelProvider).getFont(element, column));
         }

         tableItem.setText(column, labelProvider.getColumnText(element, column));
         tableItem.setImage(column, labelProvider.getColumnImage(element, column));
      }
   }
}
