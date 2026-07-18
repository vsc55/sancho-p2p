package sancho.view.search.result;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ICustomViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import sancho.model.mldonkey.Result;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

class ResultTableMenuListener$ToolTipHandler implements IMenuListener {
   private Shell tipShell;
   private Label tipLabelImage;
   private Label tipLabelTitle;
   private Label tipLabelText;
   private Widget tipWidget;
   private Point tipPosition;
   private List namesList;
   private ResultTableMenuListener$CustomSeparator namesSeparator;
   private Point pt;
   private Font boldFont;
   private ToolBar toolBar;
   private Composite namesComposite;
   private long fileSize;
   private String md4;
   private String ed2k;
   private int charHeight;
   private Result result;
   private MenuManager popupMenu;
   private Display display;
   private boolean isVisible;
   int vscroll;
   private ResultTableMenuListener$ToolTipTimerTask timerTask;
   private int[] roundRect;
   private int[] roundRectR;
   private Region tipRegion;
   // $VF: synthetic field
   private final ResultTableMenuListener this$0;

   int[] roundRect(int var1, int var2, int var3, int var4) {
      int[] var5 = new int[]{0, 6, 1, 5, 1, 4, 4, 1, 5, 1, 6, 0};
      int[] var6 = new int[]{-6, 0, -5, 1, -4, 1, -1, 4, -1, 5, 0, 6};
      int[] var7 = new int[]{0, -6, -1, -5, -1, -4, -4, -1, -5, -1, -6, 0};
      int[] var8 = new int[]{6, 0, 5, -1, 4, -1, 1, -4, 1, -5, 0, -6};
      int[] var9 = new int[48];
      int var10 = 0;
      int[] var11 = var5;
      int[] var12 = var6;

      for (int var13 = 0; var13 < var11.length / 2; var13++) {
         var9[var10++] = var1 + var11[2 * var13];
         var9[var10++] = var2 + var11[2 * var13 + 1];
      }

      for (int var14 = 0; var14 < var12.length / 2; var14++) {
         var9[var10++] = var1 + var3 + var12[2 * var14];
         var9[var10++] = var2 + var12[2 * var14 + 1];
      }

      var11 = var7;
      var12 = var8;

      for (int var15 = 0; var15 < var11.length / 2; var15++) {
         var9[var10++] = var1 + var11[2 * var15] + var3;
         var9[var10++] = var2 + var11[2 * var15 + 1] + var4;
      }

      for (int var16 = 0; var16 < var12.length / 2; var16++) {
         var9[var10++] = var1 + var12[2 * var16];
         var9[var10++] = var2 + var12[2 * var16 + 1] + var4;
      }

      return var9;
   }

   public ResultTableMenuListener$ToolTipHandler(ResultTableMenuListener var1, Composite var2) {
      this.this$0 = var1;
      this.pt = new Point(0, 0);
      GC var3 = new GC(var2);
      this.charHeight = var3.getFontMetrics().getHeight();
      var3.dispose();
      this.display = var2.getDisplay();
      this.tipShell = new Shell(this.display, 278540);
      this.tipShell.setBackground(this.display.getSystemColor(29));
      this.tipShell.addPaintListener(new ResultTableMenuListener$3(this));
      this.tipShell.addDisposeListener(new ResultTableMenuListener$4(this));
      this.tipShell.addListener(21, new ResultTableMenuListener$5(this));
      this.tipShell.setLayout(WidgetFactory.createGridLayout(1, 10, 5, 0, 5, false));
      this.setColors(this.tipShell);
      Composite var4 = new Composite(this.tipShell, 0);
      var4.setLayoutData(new GridData(768));
      var4.setLayout(WidgetFactory.createGridLayout(2, 0, 0, 3, 0, false));
      this.setColors(var4);
      this.tipLabelImage = new Label(var4, 0);
      this.tipLabelTitle = new Label(var4, 0);
      this.tipLabelTitle.setLayoutData(new GridData(768));
      FontData[] var5 = this.tipLabelTitle.getFont().getFontData();

      for (int var6 = 0; var6 < var5.length; var6++) {
         var5[var6].setStyle(1);
      }

      this.boldFont = new Font(null, var5);
      this.tipLabelTitle.setFont(this.boldFont);
      this.setColors(this.tipLabelImage);
      this.setColors(this.tipLabelTitle);
      this.tipLabelImage.setLayoutData(new GridData(1));
      this.createSeparator(this.tipShell);
      this.tipLabelText = new Label(this.tipShell, 0);
      this.setColors(this.tipLabelText);
      this.tipLabelText.setLayoutData(new GridData(772));
      this.namesComposite = new Composite(this.tipShell, 0);
      this.namesComposite.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 5, false));
      this.namesComposite.setLayoutData(new GridData(768));
      this.setColors(this.namesComposite);
      this.namesSeparator = this.createSeparator(this.namesComposite);
      this.namesList = new List(this.namesComposite, 768);
      this.setColors(this.namesList);
      this.createSeparator(this.tipShell);
      if (!PreferenceLoader.loadBoolean("searchTooltipsOffset")) {
         Composite var7 = new Composite(this.tipShell, 0);
         var7.setLayoutData(new GridData(768));
         var7.setLayout(WidgetFactory.createGridLayout(3, 0, 0, 0, 0, false));
         var7.setBackground(this.namesList.getDisplay().getSystemColor(29));
         ToolBar var8 = new ToolBar(var7, 8388608);
         this.setColors(var8);
         var8.setLayoutData(new GridData(1));
         Composite var9 = new Composite(var7, 0);
         this.setColors(var9);
         GridData var10 = new GridData(1808);
         var10.heightHint = 1;
         var9.setLayoutData(var10);
         ToolItem var11 = new ToolItem(var8, 0);
         var11.setImage(SResources.getImage("down_arrow_green"));
         var11.setToolTipText(SResources.getString("s.download"));
         var11.addSelectionListener(new ResultTableMenuListener$6(this));
         this.toolBar = new ToolBar(var7, 8388608);
         this.setColors(this.toolBar);
         this.toolBar.setLayoutData(new GridData(128));
         if (VersionInfo.useWebServices()) {
            this.addToolItem("bitzi", "mi.web.bitzi", 1);
            this.addToolItem("edonkey", "mi.web.filedonkey", 2);
            this.addToolItem("razorback2", "mi.web.razorback2", 5);
         }
      }

      this.popupMenu = new MenuManager("");
      this.popupMenu.setRemoveAllWhenShown(true);
      this.popupMenu.addMenuListener(this);
      this.tipLabelText.setMenu(this.popupMenu.createContextMenu(this.tipShell));
   }

   public ResultTableMenuListener$CustomSeparator createSeparator(Composite var1) {
      ResultTableMenuListener$CustomSeparator var2 = new ResultTableMenuListener$CustomSeparator(this.this$0, var1);
      var2.setLayoutData(new GridData(768));
      return var2;
   }

   public void setColors(Control var1) {
      var1.setBackground(var1.getDisplay().getSystemColor(29));
      var1.setForeground(var1.getDisplay().getSystemColor(28));
   }

   public void addToolItem(String var1, String var2, int var3) {
      ToolItem var4 = new ToolItem(this.toolBar, 0);
      var4.setImage(SResources.getImage(var1));
      var4.setToolTipText(SResources.getString(var2));
      var4.addSelectionListener(new ResultTableMenuListener$7(this, var3));
   }

   public void startTimer() {
      this.timerTask = new ResultTableMenuListener$ToolTipTimerTask(this.this$0, this);
      this.display.timerExec(1000, this.timerTask);
   }

   public void stopTimer() {
      if (this.timerTask != null) {
         this.timerTask.stop = true;
         this.timerTask = null;
      }
   }

   public void dispose() {
      if (this.tipShell != null) {
         this.tipShell.dispose();
         this.tipShell = null;
      }

      if (this.tipRegion != null && !this.tipRegion.isDisposed()) {
         this.tipRegion.dispose();
         this.tipRegion = null;
      }

      this.stopTimer();
   }

   public void activateHoverHelp(Control var1, ICustomViewer var2) {
      var1.addMouseListener(new ResultTableMenuListener$8(this));
      var1.addMouseTrackListener(new ResultTableMenuListener$9(this, var1, var2));
   }

   private void setVisible(boolean var1) {
      this.tipShell.setVisible(var1);
      this.isVisible = var1;
      this.stopTimer();
      if (var1) {
         this.startTimer();
      } else {
         this.result = null;
      }
   }

   private void setHoverLocation(Shell var1, Point var2) {
      this.vscroll = ResultTableMenuListener.access$2900(this.this$0).getComposite().getVerticalBar().getSelection();
      Rectangle var3 = var1.getDisplay().getBounds();
      var1.pack();
      Rectangle var4 = var1.getBounds();
      int var5 = PreferenceLoader.loadBoolean("searchTooltipsOffset") ? 10 : -5;
      var4.x = Math.max(Math.min(var2.x, var3.width - var4.width), 0) + var5;
      var4.y = Math.max(Math.min(var2.y, var3.height - var4.height), 0) + var5;
      var1.setLocation(var4.x, var4.y);
      Point var6 = var1.computeSize(-1, -1);
      this.roundRect = this.roundRect(0, 0, var6.x - 1, var6.y - 1);
      this.roundRectR = this.roundRect(0, 0, var6.x, var6.y);
      // setRegion() does not dispose the previously installed region, and this runs
      // on every hover/reposition, so dispose the old one to avoid leaking GDI
      // region handles over a long browsing session.
      if (this.tipRegion != null && !this.tipRegion.isDisposed()) {
         this.tipRegion.dispose();
      }

      Region var7 = new Region();
      var7.add(this.roundRectR);
      var1.setRegion(var7);
      this.tipRegion = var7;
   }

   public void menuAboutToShow(IMenuManager var1) {
      var1.add(new ResultTableMenuListener$ToolTipHandler$CopyToolTipToClipboardAction(this));
   }

   // $VF: synthetic method
   static int[] access$800(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.roundRect;
   }

   // $VF: synthetic method
   static Shell access$900(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.tipShell;
   }

   // $VF: synthetic method
   static Font access$1000(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.boldFont;
   }

   // $VF: synthetic method
   static MenuManager access$1100(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.popupMenu;
   }

   // $VF: synthetic method
   static Result access$1200(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.result;
   }

   // $VF: synthetic method
   static ResultTableMenuListener access$1300(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.this$0;
   }

   // $VF: synthetic method
   static String access$1400(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.md4;
   }

   // $VF: synthetic method
   static String access$1500(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.ed2k;
   }

   // $VF: synthetic method
   static long access$1600(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.fileSize;
   }

   // $VF: synthetic method
   static void access$1700(ResultTableMenuListener$ToolTipHandler var0, boolean var1) {
      var0.setVisible(var1);
   }

   // $VF: synthetic method
   static Point access$1800(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.pt;
   }

   // $VF: synthetic method
   static Widget access$1902(ResultTableMenuListener$ToolTipHandler var0, Widget var1) {
      return var0.tipWidget = var1;
   }

   // $VF: synthetic method
   static Point access$2002(ResultTableMenuListener$ToolTipHandler var0, Point var1) {
      return var0.tipPosition = var1;
   }

   // $VF: synthetic method
   static Point access$2000(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.tipPosition;
   }

   // $VF: synthetic method
   static Widget access$1900(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.tipWidget;
   }

   // $VF: synthetic method
   static Result access$1202(ResultTableMenuListener$ToolTipHandler var0, Result var1) {
      return var0.result = var1;
   }

   // $VF: synthetic method
   static Label access$2100(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.tipLabelImage;
   }

   // $VF: synthetic method
   static Label access$2200(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.tipLabelTitle;
   }

   // $VF: synthetic method
   static String access$1402(ResultTableMenuListener$ToolTipHandler var0, String var1) {
      return var0.md4 = var1;
   }

   // $VF: synthetic method
   static String access$1502(ResultTableMenuListener$ToolTipHandler var0, String var1) {
      return var0.ed2k = var1;
   }

   // $VF: synthetic method
   static long access$1602(ResultTableMenuListener$ToolTipHandler var0, long var1) {
      return var0.fileSize = var1;
   }

   // $VF: synthetic method
   static Label access$2300(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.tipLabelText;
   }

   // $VF: synthetic method
   static List access$2400(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.namesList;
   }

   // $VF: synthetic method
   static ResultTableMenuListener$CustomSeparator access$2500(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.namesSeparator;
   }

   // $VF: synthetic method
   static Composite access$2600(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.namesComposite;
   }

   // $VF: synthetic method
   static int access$2700(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.charHeight;
   }

   // $VF: synthetic method
   static void access$2800(ResultTableMenuListener$ToolTipHandler var0, Shell var1, Point var2) {
      var0.setHoverLocation(var1, var2);
   }

   // $VF: synthetic method
   static Display access$3000(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.display;
   }

   // $VF: synthetic method
   static boolean access$3100(ResultTableMenuListener$ToolTipHandler var0) {
      return var0.isVisible;
   }
}
