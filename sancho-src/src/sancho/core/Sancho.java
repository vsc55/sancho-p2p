package sancho.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.MainWindow;
import sancho.view.console.ExecConsole;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;
import sancho.view.utility.Splash;
import sancho.view.utility.dialogs.BugDialog;

public class Sancho {
   private static CoreFactory coreFactory;
   public static boolean debug;
   public static boolean sDebug;
   private static Display display;
   private static ExecConsole execConsole;
   private static List linkList;
   public static boolean noBrowser;
   public static boolean noCore;
   private static File ourLockFile;
   public static long startTime;
   public static boolean spawnAborted;
   private static StringBuffer stringBuffer = new StringBuffer();
   public static boolean monitorMode;
   public static boolean bHasLoaded;
   public static boolean automated;
   public static boolean startMinimized;
   public static boolean startTray;
   private static boolean deleteLockFile;

   public static boolean hasLoaded() {
      return bHasLoaded;
   }

   public static void addLink(String var0) {
      if (linkList == null) {
         linkList = new ArrayList();
      }

      linkList.add(var0);
   }

   public static boolean argCheck(char var0, String var1) {
      return var1.length() == 2 && var1.indexOf(var0) == 1 && (var1.startsWith("-") || var1.startsWith("/"));
   }

   public static boolean argCheck(String var0, String var1) {
      return (var1.startsWith("-") || var1.startsWith("/")) && var1.substring(1).equals(var0);
   }

   private static void automatedLaunch() {
      coreFactory.setAutomated(true);
      coreFactory.setWantToConnect(true);
      if (PreferenceLoader.loadBoolean("useLastFile")) {
         coreFactory.readPreferences(SwissArmy.readLastFile());
      }

      if (coreFactory.successfulConnect() == 0) {
         sendDownloadLinks();
      }

      display.dispose();
   }

   private static void createLockFile() {
      try {
         FileOutputStream var0 = new FileOutputStream(ourLockFile);
         PrintStream var1 = new PrintStream(var0);
         var1.close();
         var0.close();
         ourLockFile.deleteOnExit();
      } catch (FileNotFoundException var3) {
      } catch (IOException var4) {
      }
   }

   public static void exit(int var0) {
      if (stringBuffer.length() > 0) {
         if (VersionInfo.getOSPlatform().equals("Windows") && stringBuffer.length() > 0) {
            MessageBox var1 = new MessageBox(new Shell(), 34);
            var1.setMessage(stringBuffer.toString());
            var1.open();
         } else {
            System.out.println(stringBuffer.toString());
         }
      }

      if (display != null && !display.isDisposed()) {
         display.dispose();
      }

      System.exit(var0);
   }

   public static ICore getCore() {
      return coreFactory.getCore();
   }

   public static ExecConsole getCoreConsole() {
      return execConsole;
   }

   public static void killCoreConsole() {
      if (execConsole != null) {
         execConsole.forceKill();
         display.syncExec(new Runnable() {
            public void run() {
               execConsole.dispose();
            }
         });
      }

      execConsole = null;
   }

   public static CoreFactory getCoreFactory() {
      return coreFactory;
   }

   public static boolean hasCollectionFactory() {
      return getCore() != null && getCore().getCollectionFactory() != null;
   }

   public static String getUptime() {
      return SwissArmy.calcUptime(startTime);
   }

   private static void initializeResources() {
      try {
         PreferenceLoader.initialize();
      } catch (IOException var2) {
         var2.printStackTrace();
         System.exit(2);
      }

      SResources.initialize();
      PreferenceLoader.initialize2();
      File var0 = new File(VersionInfo.getHomeDirectory() + "exit.log");
      if (var0.exists()) {
         var0.delete();
      }

      File var1 = new File(VersionInfo.getHomeDirectory() + "console.msg");
      if (var1.exists()) {
         var1.delete();
      }
   }

   private static void interactiveLaunch() {
      startTime = System.currentTimeMillis();
      new Splash(display);
      if (!PreferenceLoader.loadString("coreExecutable").equals("")) {
         spawnCore();
      }

      if (noCore || coreFactory.interactiveConnect() == 0) {
         new MainWindow(display);
         PreferenceLoader.saveStore();
         PreferenceLoader.cleanUp();
         if (coreFactory.isConnected()) {
            coreFactory.disconnect();
         }
      }

      if (deleteLockFile && ourLockFile != null) {
         ourLockFile.delete();
      }

      Splash.dispose();
   }

   public static void main(String[] var0) {
      // Note: tolerance for live model data mutating mid-sort now lives in
      // GSorter.sort() (a local stable merge sort), so the global
      // -Djava.util.Arrays.useLegacyMergeSort flag is no longer needed.
      Display.setAppName("sancho");
      display = new Display();
      display.addListener(12, new Listener() {
         public void handleEvent(Event event) {
            if (deleteLockFile && ourLockFile != null) {
               ourLockFile.delete();
            }
         }
      });
      if (VersionInfo.isGNU() && Prov.x == 2) {
         System.out.println("");
      }

      coreFactory = new SSHCoreFactory(display);
      parseArgs(var0);
      initializeResources();
      coreFactory.initialize();
      if (linkList != null) {
         automated = true;
         deleteLockFile = false;
         automatedLaunch();
         exit(0);
      }

      if (!debug && !PreferenceLoader.loadBoolean("allowMultipleInstances")) {
         boolean var1 = false;
         ourLockFile = new File(VersionInfo.getHomeDirectory() + ".lock");
         var1 = ourLockFile.exists();
         if (!var1) {
            deleteLockFile = true;
            createLockFile();
         } else {
            MessageBox var2 = new MessageBox(new Shell(display), 193);
            var2.setText(SResources.getString("core.multipleCoresTitle"));
            var2.setMessage(SResources.getString("core.multipleCoresText"));
            if (var2.open() == 128) {
               deleteLockFile = false;
               exit(0);
            } else {
               deleteLockFile = true;
            }
         }
      }

      interactiveLaunch();
      exit(0);
   }

   public static void parseArgs(String[] var0) {
      for (int var1 = 0; var1 < var0.length; var1++) {
         if (!parseSingle(var0[var1])) {
            if (var0.length > var1 + 1 && parseDouble(var0[var1], var0[var1 + 1])) {
               var1++;
            } else {
               addLink(var0[var1]);
            }
         }
      }
   }

   public static void pDebug(String var0) {
      if (debug || sDebug) {
         System.err.println("[" + System.currentTimeMillis() + "] " + var0);
      }
   }

   public static synchronized void fDebug(String var0, String var1) {
      try {
         FileOutputStream var2 = new FileOutputStream(VersionInfo.getHomeDirectory() + var1, true);
         PrintStream var3 = new PrintStream(var2);
         var3.print(var0);
         if (!var0.endsWith("\n")) {
            var3.print("\n");
         }

         var3.close();
         var2.close();
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public static boolean parseDouble(String var0, String var1) {
      if (argCheck('c', var0)) {
         PreferenceLoader.setPrefFile(var1);
         return true;
      } else if (argCheck('j', var0)) {
         PreferenceLoader.setHomeDirectory(var1);
         return true;
      } else if (argCheck('l', var0)) {
         addLink(var1);
         return true;
      } else if (argCheck('r', var0) || argCheck('R', var0)) {
         PreferenceLoader.jvm = var1;
         return true;
      } else if (argCheck('f', var0)) {
         PreferenceLoader.setLocaleString(var1);
         return true;
      } else if (argCheck('H', var0) || argCheck('h', var0)) {
         String[] var2 = SwissArmy.split(var1, ':');
         if (var2.length == 2) {
            try {
               int var3 = Integer.parseInt(var2[1]);
               coreFactory.setHostPort(var2[0], var3);
            } catch (NumberFormatException var5) {
            }
         }

         return true;
      } else if (argCheck('U', var0) || argCheck('u', var0)) {
         coreFactory.setUsername(var1);
         return true;
      } else if (!argCheck('P', var0) && !argCheck('p', var0)) {
         return false;
      } else {
         coreFactory.setPassword(var1);
         return true;
      }
   }

   public static boolean parseSingle(String var0) {
      if (argCheck('?', var0)) {
         printHelp();
         exit(1);
         return false;
      } else if (argCheck("min", var0)) {
         startMinimized = true;
         return true;
      } else if (argCheck("tray", var0)) {
         startTray = true;
         return true;
      } else if (argCheck('v', var0) || argCheck('V', var0)) {
         printVersion();
         exit(1);
         return false;
      } else if (argCheck('d', var0)) {
         debug = true;
         return true;
      } else if (argCheck('b', var0)) {
         noBrowser = true;
         return true;
      } else if (argCheck('m', var0)) {
         monitorMode = true;
         return true;
      } else if (argCheck('n', var0)) {
         noCore = true;
         return true;
      } else {
         return false;
      }
   }

   public static void printHelp() {
      initializeResources();
      printVersion();
      stringBuffer.append("Usage: " + VersionInfo.getName() + " [OPTION]...\n\n");
      printOption('c', "<filename>", "cmdline.c");
      printOption('j', "<directory>", "cmdline.j");
      printOption('l', "<linkname>", "cmdline.l");
      printOption('h', "<host:port>", "cmdline.h");
      printOption('u', "<username>", "cmdline.u");
      printOption('p', "<password>", "cmdline.p");
      printOption('f', "<xx_XX>", "cmdline.f");
      stringBuffer.append("\n");
      printOption('b', "", "cmdline.b");
      printOption('n', "", "cmdline.n");
      printOption('d', "", "cmdline.d");
      printOption('m', "", "cmdline.m");
      stringBuffer.append("\n");
      printOption("min", "", "cmdline.min");
      printOption("tray", "", "cmdline.tray");
   }

   public static void printOption(char var0, String var1, String var2) {
      stringBuffer.append("  -" + var0 + " " + var1 + " \t" + SResources.getString(var2) + "\n");
   }

   public static void printOption(String var0, String var1, String var2) {
      stringBuffer.append("  -" + var0 + " " + var1 + " \t" + SResources.getString(var2) + "\n");
   }

   public static void printVersion() {
      stringBuffer.append(VersionInfo.getName() + " " + VersionInfo.getVersion() + "\n");
   }

   private static void sendDownloadLinks() {
      if (getCore() != null) {
         for (int var0 = 0; var0 < linkList.size(); var0++) {
            SwissArmy.sendLink(getCore(), (String)linkList.get(var0));
         }
      }
   }

   private static void spawnCore() {
      Splash.updateText("splash.spawningCore");
      File var0 = new File(PreferenceLoader.loadString("coreExecutable"));
      int var1 = PreferenceLoader.loadInt("hm_0_port");
      spawnAborted = SwissArmy.portInUse(var1);
      if (spawnAborted) {
         Splash.updateText("splash.spawningAborted");
      } else {
         if (execConsole == null && var0.exists() && var0.isFile()) {
            execConsole = new ExecConsole(display);
            int var2 = 0;

            while (var2++ < 235 && !execConsole.coreStarted()) {
               SwissArmy.threadSleep(250);
            }
         }
      }
   }

   public static void send(short var0, Object[] var1) {
      if (getCore() != null) {
         getCore().send(var0, var1);
      }
   }

   public static void send(short var0, Object var1) {
      send(var0, new Object[]{var1});
   }

   public static void send(short var0) {
      send(var0, null);
   }

   public static void threadException(final String message, final Exception exception) {
      if (debug) {
         exception.printStackTrace();
      } else if (display != null && !display.isDisposed()) {
         display.asyncExec(new Runnable() {
            public void run() {
               StringWriter writer = new StringWriter();
               writer.write("From Thread: " + message + "\n\n");
               if (exception != null) {
                  exception.printStackTrace(new PrintWriter(writer, true));
               } else {
                  writer.write("NULL EXCEPTION");
               }

               new BugDialog(new Shell(display), writer.toString()).open();
            }
         });
      }
   }
}
