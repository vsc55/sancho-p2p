package sancho.view.console;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import sancho.core.Sancho;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class Console {
   static Pattern regex;
   protected List commandHistory = new ArrayList();
   protected Composite composite;
   protected Color highlightColor;
   protected int historyMark = 0;
   protected StyledText infoDisplay;
   protected Text input;
   protected final int MAX_LINES = PreferenceLoader.loadInt("consoleMaxLines");
   protected Cursor handCursor;
   protected boolean usingHand;
   protected boolean underlineURLs;

   public Console(Composite var1, int var2) {
      this.createContents(var1, var2);
      this.handCursor = new Cursor(this.infoDisplay.getDisplay(), 21);
      this.infoDisplay.addMouseMoveListener(new Console$1(this));
      this.infoDisplay.addMouseTrackListener(new Console$2(this));
      this.infoDisplay.addMouseListener(new Console$3(this));
   }

   protected void disableHand() {
      if (this.usingHand) {
         this.infoDisplay.setCursor(null);
         this.usingHand = false;
      }
   }

   public void addMenuItem(Menu var1, String var2, String var3, SelectionAdapter var4) {
      MenuItem var5 = new MenuItem(var1, 8);
      var5.setText(SResources.getString(var2));
      var5.setImage(SResources.getImage(var3));
      var5.addSelectionListener(var4);
   }

   public void append(String var1) {
      int var2;
      if ((var2 = this.infoDisplay.getLineCount()) > this.MAX_LINES) {
         this.infoDisplay.replaceTextRange(0, this.infoDisplay.getOffsetAtLine(var2 - this.MAX_LINES + 5), "");
      }

      this.infoDisplay.setCaretOffset(this.infoDisplay.getText().length());
      this.infoDisplay.append(var1);
      if (this.underlineURLs) {
         int var3 = this.infoDisplay.getCharCount();
         var3 -= var1.length();
         Matcher var4 = regex.matcher(var1);
         while (var4.find()) {
            String var6 = var4.group();
            int var7 = var4.start();
            int var8 = var4.end();
            if (var6.startsWith("\"")) {
                  var7++;
               }

               if (var6.endsWith("\"")) {
                  var8--;
               } else if (var6.endsWith(".")) {
                  var8--;
               } else if (var6.endsWith(",")) {
                  var8--;
               } else if (var6.endsWith("?")) {
                  var8--;
               }

               int var9 = var8 - var7;
               if (var9 > 1) {
                  StyleRange var10 = new StyleRange();
                  var10.start = var3 + var7;
                  var10.length = var9;
                  var10.underline = true;
                  this.infoDisplay.setStyleRange(var10);
               }
            }
      }

      this.infoDisplay.setCaretOffset(this.infoDisplay.getCaretOffset() + var1.length() + 1);
      this.infoDisplay.showSelection();
   }

   protected void updateForeground(int var1, int var2, Color var3) {
      StyleRange[] var4 = this.infoDisplay.getStyleRanges(var1, var2);
      if (var4 == null) {
         StyleRange var5 = new StyleRange();
         var5.start = var1;
         var5.length = var2;
         var5.foreground = var3;
         this.infoDisplay.setStyleRange(var5);
      } else {
         int var9 = var1;

         for (int var6 = 0; var6 < var4.length; var6++) {
            int var7 = var4[var6].start;
            if (var9 < var7) {
               StyleRange var8 = new StyleRange();
               var8.start = var9;
               var8.length = var7 - var9;
               var8.foreground = var3;
               this.infoDisplay.setStyleRange(var8);
            }

            var9 = var7 + var4[var6].length;
            var4[var6].foreground = var3;
            this.infoDisplay.setStyleRange(var4[var6]);
         }

         if (var9 < var1 + var2) {
            StyleRange var10 = new StyleRange();
            var10.start = var9;
            var10.length = var1 + var2 - var9;
            var10.foreground = var3;
            this.infoDisplay.setStyleRange(var10);
         }
      }
   }

   public void appendInput() {
      this.prefixAppend();
      String var1 = this.input.getText();
      this.appendNewLine(var1);
      int var2 = this.infoDisplay.getCharCount() - var1.length() - this.getLineDelimiter().length();
      this.updateForeground(var2, var1.length(), this.highlightColor);
   }

   public void appendNewLine(String var1) {
      if (!this.infoDisplay.isDisposed()) {
         this.append(var1 + this.infoDisplay.getLineDelimiter());
      }
   }

   protected void createContents(Composite var1, int var2) {
      this.composite = new Composite(var1, 0);
      this.composite.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      this.infoDisplay = new StyledText(this.composite, var2 | 2 | 256 | 512 | 8);
      this.infoDisplay.setLayoutData(new GridData(1808));
      this.infoDisplay.setIndent(2);
      this.infoDisplay.addKeyListener(new Console$4(this));
      Menu var3 = new Menu(this.infoDisplay);
      this.addMenuItem(var3, "mi.copy", "copy", new Console$5(this));
      this.addMenuItem(var3, "mi.selectAll", "plus", new Console$6(this));
      this.addMenuItem(var3, "mi.clear", "clear", new Console$7(this));
      this.addMenuItem(var3, "l.find", "refine", new Console$8(this));
      this.infoDisplay.setMenu(var3);
      this.input = new Text(this.composite, 2052);
      this.input.setLayoutData(new GridData(768));
      this.input.addKeyListener(new Console$9(this));
      this.updateDisplay();
   }

   public void dispose() {
      this.input.dispose();
      this.infoDisplay.dispose();
      this.composite.dispose();
      // handCursor was created in the constructor but never disposed, leaking one
      // OS cursor handle per Console instance (status/message/IRC/exec consoles).
      if (this.handCursor != null && !this.handCursor.isDisposed()) {
         this.handCursor.dispose();
      }
   }

   public boolean isDisposed() {
      return this.infoDisplay == null || this.infoDisplay.isDisposed();
   }

   public Composite getComposite() {
      return this.composite;
   }

   public String getLineDelimiter() {
      return this.infoDisplay.getLineDelimiter();
   }

   public void prefixAppend() {
   }

   public void sendMessage() {
      Sancho.send((short)29, this.input.getText());
   }

   public void setFocus() {
      this.input.setFocus();
   }

   public void setActive() {
      this.input.setEnabled(true);
      this.infoDisplay.setEnabled(true);
   }

   public void setInactive() {
      this.input.setEnabled(false);
      this.infoDisplay.setEnabled(false);
   }

   public void updateDisplay() {
      this.infoDisplay.setFont(PreferenceLoader.loadFont("consoleFontData"));
      this.input.setFont(PreferenceLoader.loadFont("consoleFontData"));
      this.highlightColor = PreferenceLoader.loadColor("consoleHighlight");
      this.infoDisplay.setBackground(PreferenceLoader.loadColor("consoleBackground"));
      this.infoDisplay.setForeground(PreferenceLoader.loadColor("consoleForeground"));
      this.input.setBackground(PreferenceLoader.loadColor("consoleInputBackground"));
      this.input.setForeground(PreferenceLoader.loadColor("consoleInputForeground"));
      this.underlineURLs = PreferenceLoader.loadBoolean("consoleUnderlineURLs");
   }

   static {
      try {
         regex = Pattern.compile(
            "(\"http://.+?\")|(http://[^\\s]+)|(www.[^\\s]+)|([\\d]{1,3}[.][\\d]{1,3}[.][\\d]{1,3}[.][\\d]{1,3}[:][\\d]{1,4})|([\\d]{1,3}[.][\\d]{1,3}[.][\\d]{1,3}[.][\\d]{1,3})",
            Pattern.CASE_INSENSITIVE
         );
      } catch (PatternSyntaxException var1) {
      }
   }
}
