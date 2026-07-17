package sancho.view;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import sancho.core.Sancho;
import sancho.view.statusline.CoreConsoleItem;
import sancho.view.statusline.IStatusItem;
import sancho.view.statusline.LinkEntry;
import sancho.view.statusline.LinkEntryItem;
import sancho.view.statusline.NetworkItem;
import sancho.view.statusline.RateItem;
import sancho.view.statusline.StatusConsole;
import sancho.view.utility.WidgetFactory;

public class StatusLine {
   protected CLabel cLabel;
   protected Composite linkEntryComposite;
   protected Composite consoleComposite;
   protected MainWindow mainWindow;
   protected List statusItemList;
   protected Composite statusLineComposite;
   protected StatusConsole statusConsole;

   public StatusLine(MainWindow var1, SashForm var2, Composite var3, Composite var4) {
      this.mainWindow = var1;
      this.statusItemList = new ArrayList();
      this.createContents(var2, var3, var4);
   }

   private void addSeparator(Composite var1) {
      Label var2 = new Label(var1, 514);
      var2.setLayoutData(WidgetFactory.createGridData(1040, -1, 0));
   }

   public void clear() {
      if (!this.cLabel.isDisposed()) {
         this.cLabel.setText("");
         this.cLabel.setToolTipText("");
         this.cLabel.setImage(null);
      }
   }

   public void updateDisplay() {
      this.statusConsole.updateDisplay();
   }

   protected void createContents(SashForm var1, Composite var2, Composite var3) {
      boolean var4 = Sancho.getCoreConsole() != null;
      Composite var5 = new Composite(this.mainWindow.getMainComposite(), 2048);
      var5.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      var5.setLayoutData(new GridData(768));
      this.linkEntryComposite = this.createHiddenComposite(var5);
      new LinkEntry(this, this.linkEntryComposite);
      this.statusLineComposite = new Composite(var5, 0);
      this.statusLineComposite.setLayout(WidgetFactory.createGridLayout(var4 ? 9 : 7, 0, 0, 0, 0, false));
      this.statusLineComposite.setLayoutData(new GridData(768));
      this.statusItemList.add(new NetworkItem(this));
      this.addSeparator(this.statusLineComposite);
      if (var4) {
         new CoreConsoleItem(this);
         this.addSeparator(this.statusLineComposite);
      }

      Composite var6 = new Composite(this.statusLineComposite, 0);
      var6.setLayout(new FillLayout());
      var6.setLayoutData(new GridData(1808));
      this.cLabel = new CLabel(var6, 0);
      this.cLabel.addMouseListener(new StatusLine$1(this));
      this.cLabel.setText("");
      this.addSeparator(this.statusLineComposite);
      this.statusConsole = new StatusConsole(var1, var2, var3);
      this.statusItemList.add(this.statusConsole);
      // The win32-specific link entry paired with the removed WebBrowserTab_win32;
      // the standard WebBrowserTab is now used everywhere, so use LinkEntryItem.
      new LinkEntryItem(this, this.statusConsole);

      this.addSeparator(this.statusLineComposite);
      this.statusItemList.add(new RateItem(this));
      if (Sancho.hasCollectionFactory()) {
         this.setText(Sancho.getCoreFactory().getStatusString());
      }
   }

   private Composite createHiddenComposite(Composite var1) {
      Composite var2 = new Composite(var1, 0);
      var2.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      var2.setLayoutData(WidgetFactory.createGridData(768, -1, 0));
      return var2;
   }

   public Composite getLinkEntryComposite() {
      return this.linkEntryComposite;
   }

   public Composite getConsoleComposite() {
      return this.consoleComposite;
   }

   public MainWindow getMainWindow() {
      return this.mainWindow;
   }

   public Composite getStatusline() {
      return this.statusLineComposite;
   }

   public void setConnected(boolean var1) {
      for (int var2 = 0; var2 < this.statusItemList.size(); var2++) {
         ((IStatusItem)this.statusItemList.get(var2)).setConnected(var1);
      }
   }

   public void setImage(Image var1) {
      if (!this.cLabel.isDisposed()) {
         this.cLabel.setImage(var1);
      }
   }

   public void setText(String var1) {
      if (!this.cLabel.isDisposed()) {
         this.cLabel.setText(var1);
         this.cLabel.update();
      }
   }

   public void setToolTip(String var1) {
      if (!this.cLabel.isDisposed()) {
         this.cLabel.setToolTipText(var1);
      }
   }
}
