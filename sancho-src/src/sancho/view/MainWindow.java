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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import sancho.core.CoreFactory;
import sancho.model.mldonkey.File;
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

   public MainWindow(Display display) {
      Sancho.bHasLoaded = true;
      if (Sancho.monitorMode) {
         Splash.dispose();
         this.shell = new Shell(display, 262152);
         this.shell.addDisposeListener(this);
         this.registeredTabs = new ArrayList();
         Sancho.getCoreFactory().addObserver(this);
         this.dndBox = new DNDBox(this);
      } else {
         clipboard = new Clipboard(display);
         this.shell = new Shell(display);
         this.shell.setImage(VersionInfo.getProgramIcon());
         this.shell.setLayout(new FillLayout());
         this.shell.addShellListener(this);
         boolean useTray = VersionInfo.hasTray()
            && display.getSystemTray() != null
            && PreferenceLoader.loadBoolean("systrayEnabled");
         this.minimizer = (Minimizer)(useTray ? new MinimizerTray(this, this.titleBarText) : new Minimizer(this, this.titleBarText));
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

         display.addFilter(1, new Listener() {
            public void handleEvent(Event event) {
               if ((event.stateMask & SWT.MOD1) != 0 || (event.stateMask & 65536) != 0) {
                  if ((event.stateMask & 65536) != 0) {
                     if (event.keyCode == 16777219) {
                        int prevIndex = MainWindow.this.getCurrentTabIndex() - 1;
                        if (prevIndex < 0) {
                           prevIndex = MainWindow.this.getTabCount() - 1;
                        }

                        MainWindow.this.setCurrentTab(prevIndex);
                     } else if (event.keyCode == 16777220) {
                        int nextIndex = MainWindow.this.getCurrentTabIndex() + 1;
                        if (nextIndex >= MainWindow.this.getTabCount()) {
                           nextIndex = 0;
                        }

                        MainWindow.this.setCurrentTab(nextIndex);
                     }
                  }

                  if (event.keyCode >= 49 && event.keyCode <= 57) {
                     int tabNumber = event.keyCode - 48;
                     if (tabNumber <= MainWindow.this.getTabCount()) {
                        MainWindow.this.setCurrentTab(tabNumber - 1);
                     }
                  }

                  if ((event.stateMask & SWT.MOD1) != 0 && event.keyCode == 116) {
                     MainWindow.this.sendTorrentsFromHD();
                  }
               }
            }
         });
      }

      if (PreferenceLoader.loadBoolean("versionCheck")) {
         new VersionChecker(this.shell, this.statusLine, 4444, false);
      }

      try {
         while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
               display.sleep();
            }
         }
      } catch (SWTError swtError) {
         if (Sancho.debug) {
            swtError.printStackTrace();
            if (swtError.throwable != null) {
               swtError.throwable.printStackTrace();
            }
         } else {
            StringWriter writer = new StringWriter();
            if (this.activeTab != null) {
               writer.write(this.activeTab.toString() + "\n");
            }

            swtError.printStackTrace(new PrintWriter(writer, true));
            if (swtError.throwable != null) {
               swtError.throwable.printStackTrace(new PrintWriter(writer, true));
            }

            new BugDialog(new Shell(display), writer.toString()).open();
         }
      } catch (SWTException swtException) {
         if (Sancho.debug) {
            swtException.printStackTrace();
            if (swtException.throwable != null) {
               swtException.throwable.printStackTrace();
            }
         } else {
            StringWriter writer = new StringWriter();
            if (this.activeTab != null) {
               writer.write(this.activeTab.toString() + "\n");
            }

            swtException.printStackTrace(new PrintWriter(writer, true));
            if (swtException.throwable != null) {
               swtException.throwable.printStackTrace(new PrintWriter(writer, true));
            }

            new BugDialog(new Shell(display), writer.toString()).open();
         }
      } catch (Exception exception) {
         if (Sancho.debug) {
            exception.printStackTrace();
         } else {
            StringWriter writer = new StringWriter();
            if (this.activeTab != null) {
               writer.write(this.activeTab.toString() + "\n");
            }

            exception.printStackTrace(new PrintWriter(writer, true));
            new BugDialog(new Shell(display), writer.toString()).open();
         }
      }

      StringWriter writer = new StringWriter();
      new Throwable().printStackTrace(new PrintWriter(writer, true));
   }

   private int getCurrentTabIndex() {
      return this.registeredTabs.indexOf(this.activeTab);
   }

   private int getTabCount() {
      return this.registeredTabs.size();
   }

   private void setCurrentTab(int index) {
      AbstractTab tab = (AbstractTab)this.registeredTabs.get(index);
      tab.setActive();
   }

   private void createContents(Shell shell) {
      this.mainComposite = new Composite(shell, 0);
      this.mainComposite.setLayout(WidgetFactory.createGridLayout(1, 1, 1, 0, 1, false));
      new MenuBar(this);
      new Label(this.mainComposite, 258).setLayoutData(new GridData(768));
      this.coolBar = new CToolBar(this, PreferenceLoader.loadBoolean("toolbarSmallButtons"));
      String sashPrefName = "mainWindowSash";
      SashForm sashForm = WidgetFactory.createSashForm(this.mainComposite, sashPrefName);
      sashForm.setLayoutData(new GridData(1808));
      this.pageContainer = new Composite(sashForm, 0);
      this.stackLayout = new StackLayout();
      this.pageContainer.setLayout(this.stackLayout);
      Composite statusComposite = new Composite(sashForm, 0);
      statusComposite.setLayout(new FillLayout());
      WidgetFactory.loadSashForm(sashForm, sashPrefName);
      this.addTabs();
      this.coolBar.layoutCoolBar();
      this.statusLine = new StatusLine(this, sashForm, this.pageContainer, statusComposite);
   }

   private void addTab(int tabIndex) {
      if (tabIndex != 8 || !Sancho.noBrowser) {
         Splash.updateText("splash.creatingTab", SResources.getString(ALL_TAB_IDS[tabIndex]), tabIndex + 1);
         switch (tabIndex) {
            case 0:
               this.registeredTabs.add(new TransferTab(this, ALL_TAB_IDS[tabIndex]));
               break;
            case 1:
               this.registeredTabs.add(new SearchTab(this, ALL_TAB_IDS[tabIndex]));
               break;
            case 2:
               this.registeredTabs.add(new ServerTab(this, ALL_TAB_IDS[tabIndex]));
               break;
            case 3:
               this.registeredTabs.add(new SharesTab(this, ALL_TAB_IDS[tabIndex]));
               break;
            case 4:
               this.registeredTabs.add(new ConsoleTab(this, ALL_TAB_IDS[tabIndex]));
               break;
            case 5:
               this.registeredTabs.add(new StatisticTab(this, ALL_TAB_IDS[tabIndex]));
               break;
            case 6:
               this.registeredTabs.add(new FriendsTab(this, ALL_TAB_IDS[tabIndex]));
               break;
            case 7:
               this.registeredTabs.add(new RoomsTab(this, ALL_TAB_IDS[tabIndex]));
               break;
            case 8:
               // WebBrowserTab_win32 reimplemented OLE/COM event dispatch for the
               // 2008-era 32-bit SWT. Its COMObject callbacks (int method(int[]))
               // no longer match modern 64-bit SWT (long method(long[])), so it is
               // broken. The standard Browser widget handles win32 natively now, so
               // always use the portable WebBrowserTab.
               Object tab = new WebBrowserTab(this, ALL_TAB_IDS[tabIndex]);
               this.registeredTabs.add(tab);
         }
      }
   }

   public void switchToFriends() {
      for (int i = 0; i < this.registeredTabs.size(); i++) {
         AbstractTab tab = (AbstractTab)this.registeredTabs.get(i);
         if (tab instanceof FriendsTab) {
            this.setCurrentTab(i);
            break;
         }
      }
   }

   public void autoSearch(String text) {
      for (int i = 0; i < this.registeredTabs.size(); i++) {
         AbstractTab tab = (AbstractTab)this.registeredTabs.get(i);
         if (tab instanceof SearchTab) {
            ((SearchTab)tab).autoSearch(text);
            this.setCurrentTab(i);
            break;
         }
      }
   }

   private void addTabs() {
      String ids = IDSelector.loadIDs("mainWindowTabs", this.getAllTabIDs());

      for (int i = 0; i < ids.length(); i++) {
         int tabIndex = ids.charAt(i) - 'A';
         this.addTab(tabIndex);
      }

      this.setVisible(true);
      AbstractTab tab = (AbstractTab)this.registeredTabs.get(0);
      tab.setActive();
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
      for (int i = 0; i < this.registeredTabs.size(); i++) {
         AbstractTab tab = (AbstractTab)this.registeredTabs.get(i);
         tab.setInActive();
         tab.dispose();
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

   public void setActive(AbstractTab tab) {
      if (this.activeTab != null) {
         this.activeTab.setInActive();
      }

      this.stackLayout.topControl = tab.getContent();
      this.pageContainer.layout();
      this.activeTab = tab;
   }

   public void openPreferences() {
      CPreferenceManager preferenceManager = new CPreferenceManager(PreferenceLoader.getPreferenceStore());
      if (preferenceManager.open(new Shell()) == 0) {
         Iterator iterator = this.registeredTabs.iterator();

         while (iterator.hasNext()) {
            ((AbstractTab)iterator.next()).updateDisplay();
         }

         if (this.getCore() != null) {
            this.getCore().updatePreferences();
         }

         this.statusLine.updateDisplay();
      }
   }

   public void restoreWindowBounds(Shell shell) {
      if (PreferenceLoader.loadBoolean("windowMaximized")) {
         shell.setMaximized(true);
      } else {
         shell.setBounds(PreferenceLoader.loadRectangle("windowBounds"));
      }

      int alpha = PreferenceLoader.loadInt("windowAlpha");
      if (alpha >= 10 && alpha <= 255) {
         shell.setAlpha(alpha);
      }
   }

   public void saveWindowBounds(Shell shell) {
      PreferenceStore preferenceStore = PreferenceLoader.getPreferenceStore();
      if (shell.getMaximized()) {
         preferenceStore.setValue("windowMaximized", shell.getMaximized());
      } else {
         PreferenceConverter.setValue(preferenceStore, "windowBounds", shell.getBounds());
         preferenceStore.setValue("windowMaximized", shell.getMaximized());
      }

      preferenceStore.setValue("windowAlpha", shell.getAlpha());
   }

   public void update(MyObservable observable, final Object arg, int eventType) {
      if (this.shell != null && !this.shell.isDisposed() && !this.closing) {
         if (observable instanceof CoreFactory) {
            // asyncExec so this core-thread observer callback doesn't block on the
            // UI thread; the update is fire-and-forget and asyncExec keeps order.
            this.shell.getDisplay().asyncExec(new Runnable() {
               public void run() {
                  if (MainWindow.this.shell != null && !MainWindow.this.shell.isDisposed()) {
                     if (arg instanceof File) {
                        if (PreferenceLoader.loadBoolean("downloadCompleteDialog")) {
                           if (MainWindow.this.downloadCompleteDialog == null) {
                              MainWindow.this.downloadCompleteDialog = new DownloadCompleteDialog(new Shell(), MainWindow.this);
                              MainWindow.this.downloadCompleteDialog.open();
                           }

                           MainWindow.this.downloadCompleteDialog.addFile((File)arg);
                        }
                     } else if (arg instanceof Boolean) {
                        boolean connected = arg == Boolean.TRUE;

                        for (Object tabObject : MainWindow.this.registeredTabs) { AbstractTab tab = (AbstractTab)tabObject;
                           if (connected) {
                              tab.onConnect();
                           } else {
                              tab.onDisconnect();
                           }
                        }

                        if (MainWindow.this.statusLine != null) {
                           MainWindow.this.statusLine.setConnected(connected);
                        }

                        if (MainWindow.this.minimizer != null) {
                           MainWindow.this.minimizer.setConnected(connected);
                        }

                        if (MainWindow.this.dndBox != null) {
                           MainWindow.this.dndBox.setConnected(connected);
                        }
                     } else if (arg instanceof String && MainWindow.this.statusLine != null) {
                        MainWindow.this.statusLine.setText((String)arg);
                     }
                  }
               }
            });
         }
      }
   }

   public void configureTabs() {
      IDSelector idSelector = new IDSelector(this.shell, this.getTabLegend(), this.getPreferenceString(), "Tabs", "l.tabs");
      if (idSelector.open() == 0) {
         idSelector.savePrefs();
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

   public void setVisible(boolean visible) {
      for (int i = 0; i < this.registeredTabs.size(); i++) {
         ((AbstractTab)this.registeredTabs.get(i)).setVisible(visible);
      }
   }

   public void shellActivated(ShellEvent event) {
   }

   public void shellClosed(ShellEvent event) {
      event.doit = this.minimizer.close();
      this.setVisible(false);
   }

   public void shellDeactivated(ShellEvent event) {
   }

   public void shellDeiconified(ShellEvent event) {
      if (!this.minimizer.isRestoring()) {
         this.minimizer.restore();
      }

      this.setVisible(true);
   }

   public void shellIconified(ShellEvent event) {
      event.doit = this.minimizer.minimize();
      this.setVisible(false);
   }

   public void sendTorrentsFromHD() {
      if (Sancho.hasCollectionFactory()) {
         FileDialog fileDialog = new FileDialog(this.getShell(), 2);
         fileDialog.setFilterExtensions(new String[]{"*.torrent"});
         if (fileDialog.open() != null && Sancho.getCore() != null) {
            String path = fileDialog.getFilterPath() + System.getProperty("file.separator");
            String[] fileNames = fileDialog.getFileNames();

            for (int i = 0; i < fileNames.length; i++) {
               SwissArmy.sendLink(Sancho.getCore(), path + fileNames[i]);
            }

            this.statusLine.setText(SResources.getString("sl.linksSent") + fileNames.length);
         }
      }
   }

   public static void copyToClipboard(String text) {
      if (text != null && !text.equals("") && clipboard != null && !clipboard.isDisposed()) {
         clipboard.setContents(new String[]{text}, new Transfer[]{TextTransfer.getInstance()});
         if (SWT.getPlatform().equals("fox")) {
            Shell shell = new Shell();
            Text textWidget = new Text(shell, 2);
            textWidget.setText(text);
            textWidget.selectAll();
            textWidget.copy();
            textWidget.dispose();
            shell.dispose();
         }
      }
   }

   public void widgetDisposed(DisposeEvent event) {
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

      Iterator iterator = this.registeredTabs.iterator();

      while (iterator.hasNext()) {
         ((AbstractTab)iterator.next()).dispose();
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

}
