package sancho.view.statistics;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.viewFrame.ViewFrame;

public class GraphCanvas extends Composite implements PaintListener, Runnable, DisposeListener {
   public static final short MAX_GRAPH_TYPES = 2;
   private Composite composite;
   private GraphData graphData;
   private String graphName;
   private GraphPainter graphPainter;
   private boolean graphReverse;
   private int graphType;
   private Image imageBuffer;
   private boolean needNewBuffer;
   private ViewFrame viewFrame;

   public GraphCanvas(Composite var1, String var2, String var3, ViewFrame var4) {
      super(var1, 262144);
      this.viewFrame = var4;
      this.composite = var1;
      this.graphName = var3;
      this.graphData = new GraphData(var1.getDisplay(), var2, var3);
      this.graphPainter = new GraphPainter(var1, this.graphData);
      this.addPaintListener(this);
      this.addDisposeListener(this);
      this.addControlListener(new GraphCanvas$1(this));
      this.addMouseListener(new GraphCanvas$2(this));
      this.updateDisplay();
   }

   public void addPoint(float var1) {
      this.graphData.addPoint((int)(var1 * 100.0F));
   }

   public GraphData getGraphData() {
      return this.graphData;
   }

   public void paintControl(PaintEvent var1) {
      // Also build the buffer when it's still null: the code assumed controlResized
      // (the only place setting needNewBuffer) always fires before the first paint,
      // which modern SWT doesn't guarantee -> paint() would get a null Image and
      // new GC(null) would throw.
      if (this.needNewBuffer || this.imageBuffer == null) {
         Rectangle var2 = this.composite.getBounds();
         if (var2.height <= 0 || var2.width <= 0) {
            return;
         }

         if (this.imageBuffer != null) {
            this.imageBuffer.dispose();
         }

         this.imageBuffer = new Image(null, this.composite.getBounds());
      }

      this.graphPainter.paint(var1.gc, this.imageBuffer);
      this.needNewBuffer = false;
   }

   public void redrawInThread() {
      if (!this.isDisposed() && this.viewFrame.isVisible()) {
         this.getDisplay().asyncExec(this);
      }
   }

   public void reverse() {
      this.graphReverse = !this.graphReverse;
      this.graphPainter.setReverse(this.graphReverse);
   }

   public void run() {
      if (!this.isDisposed() && this.isVisible()) {
         this.redraw();
      }
   }

   public void toggleDisplay() {
      this.graphType++;
      if (this.graphType > 2) {
         this.graphType = 0;
      }

      this.graphPainter.setGraphType(this.graphType);
      PreferenceStore var1 = PreferenceLoader.getPreferenceStore();
      var1.setValue("graph" + this.graphName + "Type", this.graphType);
   }

   public void updateDisplay() {
      this.graphType = PreferenceLoader.loadInt("graph" + this.graphName + "Type");
      this.graphReverse = PreferenceLoader.loadBoolean("graph" + this.graphName + "Reverse");
      this.graphPainter.updateDisplay();
      this.graphPainter.setGraphType(this.graphType);
      this.graphPainter.setReverse(this.graphReverse);
   }

   public void widgetDisposed(DisposeEvent var1) {
      if (this.imageBuffer != null && !this.imageBuffer.isDisposed()) {
         this.imageBuffer.dispose();
      }

      PreferenceStore var2 = PreferenceLoader.getPreferenceStore();
      var2.setValue("graph" + this.graphName + "Type", this.graphType);
      var2.setValue("graph" + this.graphName + "Reverse", this.graphReverse);
   }

   public void dispose() {
      this.graphData.dispose();
   }

   // $VF: synthetic method
   static boolean access$002(GraphCanvas var0, boolean var1) {
      return var0.needNewBuffer = var1;
   }
}
