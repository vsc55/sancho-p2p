package sancho.view.statistics;

import gnu.trove.TIntArrayList;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;

public class GraphHistory implements PaintListener {
   private Shell shell;
   private GraphData graph;
   private TIntArrayList maxList;
   private TIntArrayList avgList;
   private Color gridColor = PreferenceLoader.loadColor("graphGridColor");
   private Color backgroundColor = PreferenceLoader.loadColor("graphBackgroundColor");
   private Color textColor = PreferenceLoader.loadColor("graphTextColor");
   private ScrollBar hScrollBar;
   private static String RS_HOUR = SResources.getString("graph.historyHour");
   private static String RS_AVG = SResources.getString("graph.historyAvg");
   private static String RS_MAX = SResources.getString("graph.historyMax");
   private static String RS_KBS = SResources.getString("graph.historyKBS");
   private static int BAR_WIDTH = 100;

   public GraphHistory(GraphData var1) {
      this.graph = var1;
      this.maxList = var1.getMaxList();
      this.avgList = var1.getAvgList();
      this.createContents();
   }

   public void createContents() {
      this.shell = new Shell(331120);
      this.hScrollBar = this.shell.getHorizontalBar();
      this.hScrollBar.setVisible(false);
      this.hScrollBar.addSelectionListener(new GraphHistory$1(this));
      this.shell.setImage(VersionInfo.getProgramIcon());
      this.shell.setText(this.graph.getTitle());
      this.shell.setLayout(new FillLayout());
      this.shell.addDisposeListener(new GraphHistory$2(this));
      this.shell.addControlListener(new GraphHistory$3(this));
      this.shell.addPaintListener(this);
      this.shell.setBounds(PreferenceLoader.loadRectangle("graphHistoryWindowBounds"));
      this.adjustScrollBar();
      this.shell.open();
   }

   public void adjustScrollBar() {
      int var1 = this.shell.getClientArea().width;
      int var2 = this.maxList.size();
      if (var1 >= BAR_WIDTH * var2) {
         this.hScrollBar.setVisible(false);
      } else {
         this.hScrollBar.setVisible(true);
         this.hScrollBar.setMinimum(0);
         this.hScrollBar.setMaximum(var2);
         this.hScrollBar.setIncrement(1);
         this.hScrollBar.setPageIncrement(1);
         this.hScrollBar.setSelection(0);
         this.hScrollBar.setThumb(var1 / BAR_WIDTH);
      }
   }

   public int getStart() {
      return !this.hScrollBar.isVisible() ? 0 : this.hScrollBar.getSelection();
   }

   public void paintControl(PaintEvent var1) {
      if (this.shell.getClientArea().width >= 5 && this.shell.getClientArea().height >= 5) {
         int var2 = this.shell.getClientArea().height;
         int var3 = this.shell.getClientArea().width;
         Image var4 = new Image(this.shell.getDisplay(), this.shell.getClientArea());
         GC var5 = new GC(var4);
         var5.setBackground(this.backgroundColor);
         var5.fillRectangle(0, 0, this.shell.getClientArea().width, this.shell.getClientArea().height);
         var5.setForeground(this.gridColor);

         for (int var6 = 0; var6 < var3; var6 += 20) {
            // var6 must be int: as a byte it overflowed past 127 (120 + 20 -> -116)
            // and the loop never terminated once the width was >= 128px, hanging the
            // UI when the graph-history window opened.
            var5.drawLine(var6, 0, var6, var2 + 1);
         }

         for (int var7 = var2 + 1; var7 > 0; var7 -= 20) {
            var5.drawLine(0, var7, var3 + 1, var7);
         }

         this.drawGraph(var5);
         var1.gc.drawImage(var4, 0, 0);
         var5.dispose();
         var4.dispose();
      }
   }

   private void drawGraph(GC var1) {
      if (this.maxList.size() == 0) {
         var1.setForeground(this.textColor);
         var1.drawText(SResources.getString("graph.noHistory"), 0, 0);
      } else {
         int var2 = var1.getFontMetrics().getHeight() + 2;
         float var3 = (float)this.shell.getClientArea().height - (float)(var2 * 3);
         float var8 = 2.0F;

         for (int var9 = 0; var9 < this.maxList.size(); var9++) {
            if ((float)(this.maxList.getQuick(var9) / 10) > var8) {
               var8 = (float)this.maxList.getQuick(var9) / 10.0F;
            }
         }

         float var10 = (var3 - 10.0F) / var8;
         int var11 = 0;
         var1.setForeground(this.textColor);
         Color var12 = PreferenceLoader.loadColor("graph" + this.graph.getGraphName() + "Color1");
         Color var13 = PreferenceLoader.loadColor("graph" + this.graph.getGraphName() + "Color2");
         int var14 = this.shell.getClientArea().height;
         int var15 = this.getStart();
         int var16 = 0;

         for (int var17 = var15; var17 < this.maxList.size(); var16++) {
            int var6 = this.maxList.getQuick(var17);
            int var7 = this.avgList.getQuick(var17);
            var11 = var16 * BAR_WIDTH;
            float var4 = (float)(var6 / 10);
            float var5 = (float)(var7 / 10);
            var4 *= var10;
            var5 *= var10;
            var1.setBackground(var13);
            var1.fillRectangle(var11, var14 + 1, BAR_WIDTH - 2, -((int)var4));
            var1.setBackground(var12);
            var1.fillRectangle(var11, var14 + 1, BAR_WIDTH - 2, -((int)var5));
            var1.drawText(RS_HOUR + (var17 + 1), var11, 0, true);
            var1.drawText(RS_AVG + (double)var7 / 100.0 + RS_KBS, var11, var2, true);
            var1.drawText(RS_MAX + (double)var6 / 100.0 + RS_KBS, var11, 2 * var2, true);
            var17++;
         }
      }
   }

   // $VF: synthetic method
   static Shell access$000(GraphHistory var0) {
      return var0.shell;
   }
}
