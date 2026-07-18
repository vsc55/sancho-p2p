package org.eclipse.jface.viewers;

public interface ICustomViewer extends IInputProvider {
   int[] getColumnIDs();

   void setColumnIDs(String columns);

   void setContentProvider(IContentProvider contentProvider);

   void setLabelProvider(IBaseLabelProvider labelProvider);

   void setSorter(ViewerSorter sorter);

   void setInput(Object input);

   void setEditors(boolean enabled);

   boolean getEditors();

   void updateDisplay();

   void updateSelection(ISelection selection);

   void clearAll();
}
