package sancho.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import sancho.core.CoreFactory;
import sancho.core.ICore;
import sancho.core.Sancho;
import sancho.utility.MyObservable;
import sancho.utility.MyObserver;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.mainWindow.CToolBar;
import sancho.view.mainWindow.MenuBar;
import sancho.view.mainWindow.Minimizer;
import sancho.view.mainWindow.MinimizerTray;
import sancho.view.preferences.CPreferenceManager;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.AbstractTab;
import sancho.view.utility.DNDBox;
import sancho.view.utility.DownloadCompleteDialog;
import sancho.view.utility.IDSelector;
import sancho.view.utility.LinkRipper;
import sancho.view.utility.SResources;
import sancho.view.utility.Splash;
import sancho.view.utility.VersionChecker;
import sancho.view.utility.WidgetFactory;
import sancho.view.utility.dialogs.BugDialog;

public class MainWindow implements ShellListener, MyObserver, DisposeListener {
   private static final int TRANSFER = 0;
   private static final int SEARCH = 1;
   private static final int SERVER = 2;
   private static final int SHARES = 3;
   private static final int CONSOLE = 4;
   private static final int STATISTICS = 5;
   private static final int MESSAGES = 6;
   private static final int ROOMS = 7;
   private static final int WEBBROWSER = 8;
   private static final String[] ALL_TAB_IDS = new String[]{
      "tab.transfers", "tab.search", "tab.servers", "tab.shares", "tab.console", "tab.statistics", "tab.friends", "tab.rooms", "tab.webbrowser"
   };
   private static final String prefString = "mainWindow";
   private String titleBarText = VersionInfo.getName() + " " + VersionInfo.getVersion();
   private CToolBar coolBar;
   private StatusLine statusLine;
   private StackLayout stackLayout;
   private Minimizer minimizer;
   private Shell shell;
   private Composite mainComposite;
   private Composite pageContainer;
   private List registeredTabs;
   private AbstractTab activeTab;
   private LinkRipper linkRipper = null;
   private static Clipboard clipboard;
   private DownloadCompleteDialog downloadCompleteDialog;
   private DNDBox dndBox;
   private boolean closing;

   public MainWindow(Display var1) {
      Sancho.bHasLoaded = true;
      if (Sancho.monitorMode) {
         Splash.dispose();
         this.shell = new Shell(var1, 262152);
         this.shell.addDisposeListener(this);
         this.registeredTabs = new ArrayList();
         Sancho.getCoreFactory().addObserver(this);
         this.dndBox = new DNDBox(this);
      } else {
         clipboard = new Clipboard(var1);
         this.shell = new Shell(var1);
         this.shell.setImage(VersionInfo.getProgramIcon());
         this.shell.setLayout(new FillLayout());
         this.shell.addShellListener(this);
         boolean var2 = VersionInfo.hasTray()
            && var1.getSystemTray() != null
            && PreferenceLoader.loadBoolean("systrayEnabled");
         this.minimizer = (Minimizer)(var2 ? new MinimizerTray(this, this.titleBarText) : new Minimizer(this, this.titleBarText));
         this.minimizer.setTitleBarText();
         Splash.updateText("splash.creatingGUI");
         this.registeredTabs = new ArrayList();
         this.createContents(this.shell);
         this.shell.addDisposeListener(this);
         Sancho.getCoreFactory().addObserver(this);
         Splash.dispose();
         this.restoreWindowBounds(this.shell);
         if (!VersionInfo.hasTray() || !PreferenceLoader.loadBoolean("windowStartTray") && !Sancho.startTray) {
            if (PreferenceLoader.loadBoolean("windowStartMinimized") || Sancho.startMinimized) {
               this.shell.setMinimized(true);
            }

            this.shell.open();
         } else if (!SWT.getPlatform().equals("gtk") && !SWT.getPlatform().equals("fox")) {
            this.minimizer.minimize(true);
         } else {
            this.shell.open();
            this.minimizer.minimize(true);
         }

         // Force a full recursive layout now that the shell has its restored bounds.
         // Otherwise mainComposite's GridLayout may not allocate the CToolBar row
         // until the first manual resize, leaving the navigation bar invisible at
         // startup (layoutCoolBar() alone only re-lays-out the inner composite).
         this.shell.layout(true, true);

         if (PreferenceLoader.loadBoolean("dndBox")) {
            this.toggleDNDBox();
         }

         var1.addFilter(1, new MainWindow$1(this));
      }

      if (PreferenceLoader.loadBoolean("versionCheck")) {
         new VersionChecker(this.shell, this.statusLine, 4444);
      }

      try {
         while (!this.shell.isDisposed()) {
            if (!var1.readAndDispatch()) {
               var1.sleep();
            }
         }
      } catch (SWTError var6) {
         if (Sancho.debug) {
            var6.printStackTrace();
            if (var6.throwable != null) {
               var6.throwable.printStackTrace();
            }
         } else {
            StringWriter var3 = new StringWriter();
            if (this.activeTab != null) {
               var3.write(this.activeTab.toString() + "\n");
            }

            var6.printStackTrace(new PrintWriter(var3, true));
            if (var6.throwable != null) {
               var6.throwable.printStackTrace(new PrintWriter(var3, true));
            }

            new BugDialog(new Shell(var1), var3.toString()).open();
         }
      } catch (SWTException var7) {
         if (Sancho.debug) {
            var7.printStackTrace();
            if (var7.throwable != null) {
               var7.throwable.printStackTrace();
            }
         } else {
            StringWriter var4 = new StringWriter();
            if (this.activeTab != null) {
               var4.write(this.activeTab.toString() + "\n");
            }

            var7.printStackTrace(new PrintWriter(var4, true));
            if (var7.throwable != null) {
               var7.throwable.printStackTrace(new PrintWriter(var4, true));
            }

            new BugDialog(new Shell(var1), var4.toString()).open();
         }
      } catch (Exception var8) {
         if (Sancho.debug) {
            var8.printStackTrace();
         } else {
            StringWriter var5 = new StringWriter();
            if (this.activeTab != null) {
               var5.write(this.activeTab.toString() + "\n");
            }

            var8.printStackTrace(new PrintWriter(var5, true));
            new BugDialog(new Shell(var1), var5.toString()).open();
         }
      }

      StringWriter var9 = new StringWriter();
      new Throwable().printStackTrace(new PrintWriter(var9, true));
   }

   private int getCurrentTabIndex() {
      return this.registeredTabs.indexOf(this.activeTab);
   }

   private int getTabCount() {
      return this.registeredTabs.size();
   }

   private void setCurrentTab(int var1) {
      AbstractTab var2 = (AbstractTab)this.registeredTabs.get(var1);
      var2.setActive();
   }

   private void createContents(Shell var1) {
      this.mainComposite = new Composite(var1, 0);
      this.mainComposite.setLayout(WidgetFactory.createGridLayout(1, 1, 1, 0, 1, false));
      new MenuBar(this);
      new Label(this.mainComposite, 258).setLayoutData(new GridData(768));
      this.coolBar = new CToolBar(this, PreferenceLoader.loadBoolean("toolbarSmallButtons"));
      String var2 = "mainWindowSash";
      SashForm var3 = WidgetFactory.createSashForm(this.mainComposite, var2);
      var3.setLayoutData(new GridData(1808));
      this.pageContainer = new Composite(var3, 0);
      this.stackLayout = new StackLayout();
      this.pageContainer.setLayout(this.stackLayout);
      Composite var4 = new Composite(var3, 0);
      var4.setLayout(new FillLayout());
      WidgetFactory.loadSashForm(var3, var2);
      this.addTabs();
      this.coolBar.layoutCoolBar();
      this.statusLine = new StatusLine(this, var3, this.pageContainer, var4);
   }

   private void addTab(int var1) {
      if (var1 != 8 || !Sancho.noBrowser) {
         Splash.updateText("splash.creatingTab", SResources.getString(ALL_TAB_IDS[var1]), var1 + 1);
         switch (var1) {
            case 0:
               this.registeredTabs.add(new TransferTab(this, ALL_TAB_IDS[var1]));
               break;
            case 1:
               this.registeredTabs.add(new SearchTab(this, ALL_TAB_IDS[var1]));
               break;
            case 2:
               this.registeredTabs.add(new ServerTab(this, ALL_TAB_IDS[var1]));
               break;
            case 3:
               this.registeredTabs.add(new SharesTab(this, ALL_TAB_IDS[var1]));
               break;
            case 4:
               this.registeredTabs.add(new ConsoleTab(this, ALL_TAB_IDS[var1]));
               break;
            case 5:
               this.registeredTabs.add(new StatisticTab(this, ALL_TAB_IDS[var1]));
               break;
            case 6:
               this.registeredTabs.add(new FriendsTab(this, ALL_TAB_IDS[var1]));
               break;
            case 7:
               this.registeredTabs.add(new RoomsTab(this, ALL_TAB_IDS[var1]));
               break;
            case 8:
               // WebBrowserTab_win32 reimplemented OLE/COM event dispatch for the
               // 2008-era 32-bit SWT. Its COMObject callbacks (int method(int[]))
               // no longer match modern 64-bit SWT (long method(long[])), so it is
               // broken. The standard Browser widget handles win32 natively now, so
               // always use the portable WebBrowserTab.
               Object var2 = new WebBrowserTab(this, ALL_TAB_IDS[var1]);
               this.registeredTabs.add(var2);
         }
      }
   }

   public void switchToFriends() {
      for (int var1 = 0; var1 < this.registeredTabs.size(); var1++) {
         AbstractTab var2 = (AbstractTab)this.registeredTabs.get(var1);
         if (var2 instanceof FriendsTab) {
            this.setCurrentTab(var1);
            break;
         }
      }
   }

   public void autoSearch(String var1) {
      for (int var2 = 0; var2 < this.registeredTabs.size(); var2++) {
         AbstractTab var3 = (AbstractTab)this.registeredTabs.get(var2);
         if (var3 instanceof SearchTab) {
            ((SearchTab)var3).autoSearch(var1);
            this.setCurrentTab(var2);
            break;
         }
      }
   }

   private void addTabs() {
      String var1 = IDSelector.loadIDs("mainWindowTabs", this.getAllTabIDs());

      for (int var3 = 0; var3 < var1.length(); var3++) {
         int var2 = var1.charAt(var3) - 'A';
         this.addTab(var2);
      }

      this.setVisible(true);
      AbstractTab var4 = (AbstractTab)this.registeredTabs.get(0);
      var4.setActive();
   }

   public AbstractTab getActiveTab() {
      return this.activeTab;
   }

   public LinkRipper openLinkRipper() {
      this.linkRipper = new LinkRipper(new Shell(), this);
      this.linkRipper.create();
      return this.linkRipper;
   }

   public LinkRipper getLinkRipper() {
      return this.linkRipper;
   }

   public void closeLinkRipper() {
      this.linkRipper = null;
   }

   private void removeAllTabs() {
      for (int var1 = 0; var1 < this.registeredTabs.size(); var1++) {
         AbstractTab var2 = (AbstractTab)this.registeredTabs.get(var1);
         var2.setInActive();
         var2.dispose();
      }

      this.registeredTabs.clear();
      ((StackLayout)this.pageContainer.getLayout()).topControl = null;
      this.pageContainer.layout();
      this.activeTab = null;
   }

   public void resetTabs() {
      this.removeAllTabs();
      this.getCoolBar().reset();
      this.addTabs();
      this.getCoolBar().layoutCoolBar();
      System.gc();
   }

   public void setActive(AbstractTab var1) {
      if (this.activeTab != null) {
         this.activeTab.setInActive();
      }

      this.stackLayout.topControl = var1.getContent();
      this.pageContainer.layout();
      this.activeTab = var1;
   }

   public void openPreferences() {
      CPreferenceManager var1 = new CPreferenceManager(PreferenceLoader.getPreferenceStore());
      if (var1.open(new Shell()) == 0) {
         Iterator var2 = this.registeredTabs.iterator();

         while (var2.hasNext()) {
            ((AbstractTab)var2.next()).updateDisplay();
         }

         if (this.getCore() != null) {
            this.getCore().updatePreferences();
         }

         this.statusLine.updateDisplay();
      }
   }

   public void restoreWindowBounds(Shell var1) {
      if (PreferenceLoader.loadBoolean("windowMaximized")) {
         var1.setMaximized(true);
      } else {
         var1.setBounds(PreferenceLoader.loadRectangle("windowBounds"));
      }

      int var2 = PreferenceLoader.loadInt("windowAlpha");
      if (var2 >= 10 && var2 <= 255) {
         var1.setAlpha(var2);
      }
   }

   public void saveWindowBounds(Shell var1) {
      PreferenceStore var2 = PreferenceLoader.getPreferenceStore();
      if (var1.getMaximized()) {
         var2.setValue("windowMaximized", var1.getMaximized());
      } else {
         PreferenceConverter.setValue(var2, "windowBounds", var1.getBounds());
         var2.setValue("windowMaximized", var1.getMaximized());
      }

      var2.setValue("windowAlpha", var1.getAlpha());
   }

   public void update(MyObservable var1, Object var2, int var3) {
      if (this.shell != null && !this.shell.isDisposed() && !this.closing) {
         if (var1 instanceof CoreFactory) {
            // asyncExec so this core-thread observer callback doesn't block on the
            // UI thread; the update is fire-and-forget and asyncExec keeps order.
            this.shell.getDisplay().asyncExec(new MainWindow$2(this, var2));
         }
      }
   }

   public void configureTabs() {
      IDSelector var1 = new IDSelector(this.shell, this.getTabLegend(), this.getPreferenceString(), "Tabs", "l.tabs");
      if (var1.open() == 0) {
         var1.savePrefs();
         this.resetTabs();
      }
   }

   public void toggleDNDBox() {
      if (this.dndBox == null) {
         this.dndBox = new DNDBox(this);
         PreferenceLoader.getPreferenceStore().setValue("dndBox", true);
      } else {
         this.dndBox.close();
         this.dndBox = null;
         PreferenceLoader.getPreferenceStore().setValue("dndBox", false);
      }
   }

   public void closeDownloadCompleteDialog() {
      this.downloadCompleteDialog = null;
   }

   public Composite getMainComposite() {
      return this.mainComposite;
   }

   public StatusLine getStatusline() {
      return this.statusLine;
   }

   public List getTabs() {
      return this.registeredTabs;
   }

   public String getPreferenceString() {
      return "mainWindow";
   }

   public String getAllTabIDs() {
      return IDSelector.createIDString(ALL_TAB_IDS);
   }

   public String[] getTabLegend() {
      return ALL_TAB_IDS;
   }

   public Composite getPageContainer() {
      return this.pageContainer;
   }

   public ICore getCore() {
      return Sancho.getCoreFactory().getCore();
   }

   public Shell getShell() {
      return this.shell;
   }

   public CToolBar getCoolBar() {
      return this.coolBar;
   }

   public Minimizer getMinimizer() {
      return this.minimizer;
   }

   public void setVisible(boolean var1) {
      for (int var2 = 0; var2 < this.registeredTabs.size(); var2++) {
         ((AbstractTab)this.registeredTabs.get(var2)).setVisible(var1);
      }
   }

   public void shellActivated(ShellEvent var1) {
   }

   public void shellClosed(ShellEvent var1) {
      var1.doit = this.minimizer.close();
      this.setVisible(false);
   }

   public void shellDeactivated(ShellEvent var1) {
   }

   public void shellDeiconified(ShellEvent var1) {
      if (!this.minimizer.isRestoring()) {
         this.minimizer.restore();
      }

      this.setVisible(true);
   }

   public void shellIconified(ShellEvent var1) {
      var1.doit = this.minimizer.minimize();
      this.setVisible(false);
   }

   public void sendTorrentsFromHD() {
      if (Sancho.hasCollectionFactory()) {
         FileDialog var1 = new FileDialog(this.getShell(), 2);
         var1.setFilterExtensions(new String[]{"*.torrent"});
         if (var1.open() != null && Sancho.getCore() != null) {
            String var2 = var1.getFilterPath() + System.getProperty("file.separator");
            String[] var3 = var1.getFileNames();

            for (int var4 = 0; var4 < var3.length; var4++) {
               SwissArmy.sendLink(Sancho.getCore(), var2 + var3[var4]);
            }

            this.statusLine.setText(SResources.getString("sl.linksSent") + var3.length);
         }
      }
   }

   public static void copyToClipboard(String var0) {
      if (var0 != null && !var0.equals("") && clipboard != null && !clipboard.isDisposed()) {
         clipboard.setContents(new String[]{var0}, new Transfer[]{TextTransfer.getInstance()});
         if (SWT.getPlatform().equals("fox")) {
            Shell var1 = new Shell();
            Text var2 = new Text(var1, 2);
            var2.setText(var0);
            var2.selectAll();
            var2.copy();
            var2.dispose();
            var1.dispose();
         }
      }
   }

   public void widgetDisposed(DisposeEvent var1) {
      this.saveWindowBounds(this.shell);
      if (this.getLinkRipper() != null) {
         this.getLinkRipper().close();
      }

      if (this.downloadCompleteDialog != null) {
         this.downloadCompleteDialog.close();
      }

      if (this.dndBox != null) {
         this.dndBox.close();
      }

      Iterator var4 = this.registeredTabs.iterator();

      while (var4.hasNext()) {
         ((AbstractTab)var4.next()).dispose();
      }

      if (PreferenceLoader.loadBoolean("killSpawnedCoreOnExit") && (Sancho.getCoreConsole() != null || Sancho.spawnAborted)) {
         Sancho.send((short)3);
         if (Sancho.getCoreConsole() != null) {
            this.getStatusline().setText(SResources.getString("menu.file.killCore"));
            Sancho.killCoreConsole();
         }
      } else if (PreferenceLoader.loadBoolean("killCoreOnExit") && Sancho.getCoreFactory().isConnected()) {
         Sancho.send((short)3);
      }

      if (Sancho.getCore() != null) {
         this.closing = true;
         Sancho.getCoreFactory().deleteObservers();
         Sancho.getCoreFactory().disconnect();
      }

      if (clipboard != null && !clipboard.isDisposed()) {
         clipboard.dispose();
      }
   }

   // $VF: synthetic method
   static int access$000(MainWindow var0) {
      return var0.getCurrentTabIndex();
   }

   // $VF: synthetic method
   static int access$100(MainWindow var0) {
      return var0.getTabCount();
   }

   // $VF: synthetic method
   static void access$200(MainWindow var0, int var1) {
      var0.setCurrentTab(var1);
   }

   // $VF: synthetic method
   static Shell access$300(MainWindow var0) {
      return var0.shell;
   }

   // $VF: synthetic method
   static DownloadCompleteDialog access$400(MainWindow var0) {
      return var0.downloadCompleteDialog;
   }

   // $VF: synthetic method
   static DownloadCompleteDialog access$402(MainWindow var0, DownloadCompleteDialog var1) {
      return var0.downloadCompleteDialog = var1;
   }

   // $VF: synthetic method
   static List access$500(MainWindow var0) {
      return var0.registeredTabs;
   }

   // $VF: synthetic method
   static StatusLine access$600(MainWindow var0) {
      return var0.statusLine;
   }

   // $VF: synthetic method
   static Minimizer access$700(MainWindow var0) {
      return var0.minimizer;
   }

   // $VF: synthetic method
   static DNDBox access$800(MainWindow var0) {
      return var0.dndBox;
   }
}
