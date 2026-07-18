package sancho.view.search.result;

import java.util.Arrays;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import sancho.model.mldonkey.Result;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class ResultDetailDialog extends Dialog {
   Result result;

   public ResultDetailDialog(Shell var1, Result var2) {
      super(var1);
      this.result = var2;
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(this.result.getFileTypeImage());
      var1.setText(SResources.getString("s.r.resultDetails"));
   }

   protected void createButtonsForButtonBar(Composite var1) {
      this.createButton(var1, 0, SResources.getString("b.ok"), true);
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      var2.setLayout(WidgetFactory.createGridLayout(1, 5, 5, 0, 5, false));
      Text var3 = new Text(var2, 2058);
      var3.setLayoutData(new GridData(768));
      var3.setText(this.result.getToolTip());
      String[] var4 = this.result.getNames();
      if (var4 != null && var4.length > 1) {
         GC var5 = new GC(var1);
         int var6 = var5.getFontMetrics().getHeight();
         var5.dispose();
         int var7 = this.result.getNames().length;
         var7 = var7 > 6 ? 6 : var7;
         String[] var8 = new String[var4.length];
         System.arraycopy(var4, 0, var8, 0, var4.length);
         Arrays.sort(var8, String.CASE_INSENSITIVE_ORDER);
         List var9 = new List(var2, 2816);
         var9.setItems(var8);
         GridData var10 = new GridData(768);
         var10.heightHint = var7 * var6;
         var9.setLayoutData(var10);
      }

      return var2;
   }
}
