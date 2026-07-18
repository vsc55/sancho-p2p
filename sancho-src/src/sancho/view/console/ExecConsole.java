package sancho.view.console;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.io.File;
import java.io.IOException;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import sancho.core.Sancho;
import sancho.utility.MyObservable;
import sancho.utility.MyObserver;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class ExecConsole implements MyObserver {
   private static final int STDOUT = 1;
   private static final int STDERR = 2;
   private boolean coreStarted;
   private Pattern errorRE;
   private Process execProcess;
   private Color highlightColor;
   private final int MAX_LINES = PreferenceLoader.loadInt("consoleMaxLines");
   private StyledText outputConsole;
   private Shell shell;
   private ExecConsole$StreamMonitor stderrMonitor;
   private ExecConsole$StreamMonitor stdoutMonitor;
   private Display display;
   private boolean hasExited;

   public ExecConsole(Display var1) {
      this.display = var1;
      this.createContents();
      this.runExec();
   }

   public void appendLine(ExecConsole$StreamMonitor var1, String var2) {
      int var3;
      if ((var3 = this.outputConsole.getLineCount()) > this.MAX_LINES) {
         this.outputConsole.replaceTextRange(0, this.outputConsole.getOffsetAtLine(var3 - this.MAX_LINES + 5), "");
      }

      this.outputConsole.setCaretOffset(this.outputConsole.getText().length());
      int var4 = this.outputConsole.getCharCount();
      this.outputConsole.append(var2 + this.outputConsole.getLineDelimiter());
      if (var1.getType() == 2) {
         this.outputConsole
            .setStyleRange(new StyleRange(var4, var2.length(), this.outputConsole.getDisplay().getSystemColor(3), this.outputConsole.getBackground()));
      } else if (this.errorRE.matcher(var2).find()) {
         this.outputConsole.setStyleRange(new StyleRange(var4, var2.length(), this.highlightColor, this.outputConsole.getBackground()));
      }

      this.outputConsole.setCaretOffset(this.outputConsole.getCaretOffset() + var2.length());
      this.outputConsole.showSelection();
   }

   public boolean coreStarted() {
      if (this.exited()) {
         this.coreStarted = true;
      }

      return this.coreStarted;
   }

   public boolean exited() {
      if (this.hasExited) {
         return true;
      } else {
         try {
            if (this.execProcess != null) {
               int var1 = this.execProcess.exitValue();
               if (var1 > 0 || var1 <= 0) {
                  this.hasExited = true;
                  return true;
               }
            }
         } catch (IllegalThreadStateException var2) {
         }

         return false;
      }
   }

   public void createContents() {
      this.shell = new Shell(this.display, 2160);
      this.shell.setImage(VersionInfo.getProgramIcon());
      this.shell.setText("Core");
      this.shell.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      this.shell.addDisposeListener(new ExecConsole$1(this));
      this.shell.addListener(21, new ExecConsole$2(this));
      if (PreferenceLoader.contains("coreExecutableWindowBounds")) {
         this.shell.setBounds(PreferenceLoader.loadRectangle("coreExecutableWindowBounds"));
      }

      this.outputConsole = new StyledText(this.shell, 2826);
      this.outputConsole.setLayoutData(new GridData(1808));
      this.outputConsole.setFont(PreferenceLoader.loadFont("consoleFontData"));
      this.outputConsole.setBackground(PreferenceLoader.loadColor("consoleBackground"));
      this.outputConsole.setForeground(PreferenceLoader.loadColor("consoleForeground"));
      this.highlightColor = PreferenceLoader.loadColor("consoleHighlight");
      this.outputConsole.append("availableProcessors: " + Runtime.getRuntime().availableProcessors() + this.outputConsole.getLineDelimiter());
      this.outputConsole.append("maximumMem: " + Runtime.getRuntime().maxMemory() + this.outputConsole.getLineDelimiter());
      this.outputConsole.append("totalMem: " + Runtime.getRuntime().totalMemory() + this.outputConsole.getLineDelimiter());
      this.outputConsole
         .append("freeMem: " + Runtime.getRuntime().freeMemory() + this.outputConsole.getLineDelimiter() + this.outputConsole.getLineDelimiter());
      Menu var1 = new Menu(this.outputConsole);
      MenuItem var2 = new MenuItem(var1, 8);
      var2.setText(SResources.getString("mi.copy"));
      var2.addListener(13, new ExecConsole$3(this));
      this.outputConsole.setMenu(var1);

      try {
         this.errorRE = Pattern.compile("error", Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException var4) {
         this.errorRE = null;
      }
   }

   public void dispose() {
      if (this.stdoutMonitor != null) {
         this.stdoutMonitor.stop();
      }

      if (this.stderrMonitor != null) {
         this.stderrMonitor.stop();
      }

      if (this.outputConsole != null) {
         this.outputConsole.dispose();
      }

      if (this.shell != null && !this.shell.isDisposed()) {
         this.shell.dispose();
      }
   }

   public Shell getShell() {
      return this.shell;
   }

   public void forceKill() {
      if (PreferenceLoader.loadBoolean("killSpawnedCoreOnExit")) {
         Sancho.send((short)3);
         short var1 = 250;
         int var2 = PreferenceLoader.loadInt("killSpawnedCoreDelay") * (1000 / var1);

         for (int var3 = 0; var3 < var2; var3++) {
            if (this.execProcess == null) {
               return;
            }

            if (this.exited()) {
               return;
            }

            SwissArmy.threadSleep(var1);
         }

         if (this.execProcess != null) {
            Sancho.pDebug("Hard kill (check for left over .pid/.tmp): " + this.execProcess);
            this.execProcess.destroy();
            Sancho.pDebug("Destroyed: " + this.execProcess);
         }
      }
   }

   public void runExec() {
      try {
         File var1 = new File(PreferenceLoader.loadStringEnv("coreExecutable"));
         String var2 = var1.getParent();
         // Use the String[] overload: the single-String exec() splits the command
         // on whitespace, so a core path like C:\Program Files\...\mlnet.exe would
         // try to run "C:\Program". var2 (parent dir) can be null for a bare name.
         this.execProcess = Runtime.getRuntime().exec(new String[]{var1.toString()}, null, var2 == null ? null : new File(var2));
         Runtime.getRuntime().addShutdownHook(new ExecConsole$4(this));
         this.stdoutMonitor = new ExecConsole$StreamMonitor(this, this.execProcess.getInputStream(), 1);
         this.stdoutMonitor.addObserver(this);
         Thread var3 = new Thread(this.stdoutMonitor);
         var3.setDaemon(true);
         var3.start();
         this.stderrMonitor = new ExecConsole$StreamMonitor(this, this.execProcess.getErrorStream(), 2);
         this.stderrMonitor.addObserver(this);
         Thread var4 = new Thread(this.stderrMonitor);
         var4.setDaemon(true);
         var4.start();
      } catch (IOException var5) {
         Sancho.pDebug("exec:" + var5);
      }
   }

   public void update(MyObservable var1, Object var2, int var3) {
      if (var2 instanceof String) {
         String var4 = (String)var2;
         if (!this.outputConsole.isDisposed()) {
            this.outputConsole.getDisplay().asyncExec(new ExecConsole$5(this, var1, var4));
         }
      }
   }

   // $VF: synthetic method
   static Shell access$000(ExecConsole var0) {
      return var0.shell;
   }

   // $VF: synthetic method
   static StyledText access$100(ExecConsole var0) {
      return var0.outputConsole;
   }

   // $VF: synthetic method
   static boolean access$200(ExecConsole var0) {
      return var0.coreStarted;
   }

   // $VF: synthetic method
   static boolean access$202(ExecConsole var0, boolean var1) {
      return var0.coreStarted = var1;
   }
}
