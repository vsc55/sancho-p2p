package org.eclipse.jface.viewers;

import gnu.trove.TIntArrayList;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.viewer.table.GTableContentProvider;
import sancho.view.viewer.table.GTableLabelProvider;

public class CustomTableViewer extends TableViewer implements ICustomViewer {
   private int[] columnIDs;
   private Hashtable parentToItemMap = new Hashtable();
   private boolean followSelection;

   protected void initializeVirtualManager(int var1) {
   }

   protected Object[] getRawChildren(Object var1) {
      Object[] var2 = null;
      if (var1 != null) {
         IStructuredContentProvider var3 = (IStructuredContentProvider)this.getContentProvider();
         if (var3 != null) {
            var2 = var3.getElements(var1);
         }
      }

      return var2 != null ? var2 : new Object[0];
   }

   public void updateSelection(ISelection var1) {
      super.updateSelection(var1);
   }

   public void updateDisplay() {
      this.followSelection = PreferenceLoader.loadBoolean("followSelection");
   }

   public void replace(Object var1, TableItem var2) {
      // If this element is still associated with a DIFFERENT TableItem (e.g. after a
      // re-sort/re-filter moved it to another row), disassociate that stale item and
      // invalidate its row so SWT re-renders it with its correct element. Without
      // this the element shows in two rows (duplicate) and the stale row "sticks"
      // and ignores sorting. The sibling CustomTreeViewer.replace has this guard;
      // the decompiled/ported table version had lost it.
      TableItem var3 = (TableItem)this.parentToItemMap.get(var1);
      if (var3 != null && var3 != var2 && !var3.isDisposed()) {
         var3.setData(null);
         Table var4 = this.getTable();
         int var5 = var4.indexOf(var3);
         if (var5 != -1) {
            var4.clear(var5);
         }
      }

      this.parentToItemMap.put(var1, var2);
      var2.setData(var1);
      this.doUpdateItem(var2, var1);
   }

   protected void myHandleOpen(SelectionEvent var1) {
      super.handleOpen(var1);
   }

   protected void hookControl(Control var1) {
      var1.addDisposeListener(new CustomTableViewer$1(this));
      OpenStrategy var2 = new OpenStrategy(var1);
      var2.addSelectionListener(new CustomTableViewer$2(this));
      var2.addPostSelectionListener(new CustomTableViewer$3(this));
      var2.addOpenListener(new CustomTableViewer$4(this));
      Table var3 = (Table)var1;
      var3.addMouseListener(new CustomTableViewer$5(this));
      var3.addListener(36, new CustomTableViewer$6(this));
   }

   public void clearAll() {
      this.preservingSelection(new CustomTableViewer$7(this));
   }

   public void myClear(int var1, int[] var2, Object[] var3) {
      Table var4 = this.getTable();
      int var5 = var4.getItemCount();
      if (var1 < 0) {
         var1 = var5;
      }

      TIntArrayList var6 = new TIntArrayList();

      for (int var7 = 0; var7 < var3.length; var7++) {
         Object var8 = var3[var7];
         TableItem var9 = (TableItem)this.parentToItemMap.remove(var8);
         if (var9 != null) {
            var6.add(var2[var7]);
            var9.setData(null);
         }
      }

      if (var1 >= 0 && var5 != var1) {
         var4.setItemCount(var1);
      }

      int[] var11 = var6.toNativeArray();

      for (int var12 = 0; var12 < var11.length; var12++) {
         int var10 = var11[var12];
         if (var10 >= var1) {
            break;
         }

         var4.clear(var10);
      }

      // SWT's lazy SetData isn't effectively wired in this custom viewer, and a row
      // that never rendered has no item mapping, so the positional-diff clear above
      // can never re-render it (it stays stale — the "stuck" top rows). Explicitly
      // render the currently visible rows from the freshly-sorted content provider,
      // which is bounded by the screen height regardless of the table size.
      IContentProvider var14 = this.getContentProvider();
      if (var14 instanceof GTableContentProvider) {
         GTableContentProvider var15 = (GTableContentProvider)var14;
         int var16 = var4.getItemHeight();
         int var17 = var4.getTopIndex();
         int var18 = var16 > 0 ? var4.getClientArea().height / var16 + 2 : var1;
         for (int var19 = var17; var19 < var1 && var19 < var17 + var18; var19++) {
            var15.updateElement(var4.getItem(var19), var19);
         }
      }
   }

   protected void setSelectionToWidget(ISelection var1, boolean var2) {
      this.virtualSetSelectionToWidget(((IStructuredSelection)var1).toList(), var2);
   }

   public void add(Object[] var1) {
      this.preservingSelection(new CustomTableViewer$8(this));
   }

   public void remove(Object[] var1) {
      this.preservingSelection(new CustomTableViewer$9(this));
   }

   public void refresh(Object var1) {
      this.preservingSelection(new CustomTableViewer$10(this));
   }

   protected void myInternalVirtualRefreshSome() {
      ((GTableContentProvider)this.getContentProvider()).updateSorted(false);
   }

   protected void myInternalVirtualRefreshAll() {
      ((GTableContentProvider)this.getContentProvider()).updateSorted(true);
   }

   public boolean updateOrRefresh(Object[] var1, String[] var2) {
      boolean var3 = false;

      for (int var4 = 0; var4 < var1.length; var4++) {
         if (this.myUpdate(var1[var4], var2)) {
            var3 = true;
         }
      }

      if (var3) {
         this.preservingSelection(new CustomTableViewer$11(this));
      }

      return var3;
   }

   public void update(Object[] var1, String[] var2) {
      boolean var3 = false;

      for (int var4 = 0; var4 < var1.length; var4++) {
         if (this.myUpdate(var1[var4], var2)) {
            var3 = true;
         }
      }

      if (var3) {
         this.preservingSelection(new CustomTableViewer$12(this));
      }
   }

   public void update(Object var1, String[] var2) {
   }

   public boolean myUpdate(Object var1, String[] var2) {
      TableItem var3 = (TableItem)this.parentToItemMap.get(var1);
      if (var3 == null) {
         return this.passesFilters(var1);
      } else {
         return this.failsFilters(var1) ? true : this.internalUpdate(var3, var1, var2);
      }
   }

   protected boolean internalUpdate(Item var1, Object var2, String[] var3) {
      boolean var4 = false;
      if (var3 != null) {
         for (int var5 = 0; var5 < var3.length; var5++) {
            var4 = this.needsRefilter(var2, var3[var5]);
            if (var4) {
               break;
            }
         }
      }

      boolean var8;
      if (var3 == null) {
         var8 = true;
      } else {
         var8 = false;
         IBaseLabelProvider var6 = this.getLabelProvider();

         for (int var7 = 0; var7 < var3.length; var7++) {
            var8 = var6.isLabelProperty(var2, var3[var7]);
            if (var8) {
               break;
            }
         }
      }

      if (var8) {
         this.doUpdateItem(var1, var2);
      }

      return var4;
   }

   protected boolean passesFilters(Object var1) {
      ViewerFilter[] var2 = this.getFilters();
      if (var2.length == 0) {
         return true;
      } else {
         Object var3 = this.getRoot();

         for (int var4 = 0; var4 < var2.length; var4++) {
            if (var2[var4].select(this, var3, var1)) {
               return true;
            }
         }

         return false;
      }
   }

   protected boolean failsFilters(Object var1) {
      ViewerFilter[] var2 = this.getFilters();

      for (int var3 = 0; var3 < var2.length; var3++) {
         if (!var2[var3].select(this, this.getRoot(), var1)) {
            return true;
         }
      }

      return false;
   }

   protected void preservingSelection(Runnable var1) {
      // Original re-implemented the base logic via the now-private inChange /
      // restoreSelection fields; modern JFace preservingSelection() is equivalent.
      super.preservingSelection(var1);
   }

   protected List getSelectionFromWidget() {
      int[] var1 = this.getTable().getSelectionIndices();
      GTableContentProvider var2 = (GTableContentProvider)this.getContentProvider();
      ArrayList var3 = new ArrayList(var1.length);

      for (int var4 = 0; var4 < var1.length; var4++) {
         Object var5 = var2.getSFElement(var1[var4]);
         if (var5 != null) {
            var3.add(var5);
         }
      }

      return var3;
   }

   protected void virtualSetSelectionToWidget(List var1, boolean var2) {
      int var3 = var1.size();
      if (var3 != 0) {
         Table var4 = this.getTable();
         TableItem var5 = null;
         GTableContentProvider var6 = (GTableContentProvider)this.getContentProvider();
         TIntArrayList var7 = new TIntArrayList(var3);

         for (Object var9 : var1) {
            TableItem var10 = (TableItem)this.parentToItemMap.get(var9);
            int var11 = var6.getSFIndex(var9);
            if (var11 >= 0) {
               var7.add(var11);
            }

            if (var10 == null && var11 >= 0) {
               var10 = var4.getItem(var11);
               if (var10.getData() == null) {
                  var6.updateElement(var10, var11);
               }
            }

            if (var5 == null && var10 != null) {
               var5 = var10;
            }
         }

         if (this.followSelection) {
            var4.setSelection(var7.toNativeArray());
         } else {
            var4.deselectAll();
            var4.select(var7.toNativeArray());
         }

         if (var2 && var5 != null) {
            var4.showItem(var5);
         }
      }
   }

   public Object[] getSortedChildren(Object var1) {
      return super.getSortedChildren(var1);
   }

   public CustomTableViewer(Composite var1, int var2) {
      super(var1, var2);
   }

   public void closeAllTTE() {
   }

   public void setEditors(boolean var1) {
   }

   public boolean getEditors() {
      return false;
   }

   public void setColumnIDs(String var1) {
      this.columnIDs = new int[var1.length()];

      for (int var2 = 0; var2 < var1.length(); var2++) {
         this.columnIDs[var2] = var1.charAt(var2) - 'A';
      }
   }

   public int[] getColumnIDs() {
      return this.columnIDs;
   }

   protected void doUpdateItem(Item var1, Object var2) {
      TableItem var3 = (TableItem)var1;
      GTableLabelProvider var4 = (GTableLabelProvider)this.getLabelProvider();
      int var5 = this.getTable().getColumnCount();
      boolean var6 = var4 instanceof ITableFontProvider;

      for (int var7 = 0; var7 < var5; var7++) {
         var3.setBackground(var7, var4.getBackground(var2, var7));
         var3.setForeground(var7, var4.getForeground(var2, var7));
         if (var6) {
            var3.setFont(var7, ((ITableFontProvider)var4).getFont(var2, var7));
         }

         var3.setText(var7, var4.getColumnText(var2, var7));
         var3.setImage(var7, var4.getColumnImage(var2, var7));
      }
   }
}
