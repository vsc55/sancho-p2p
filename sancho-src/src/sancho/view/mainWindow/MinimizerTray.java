package sancho.view.mainWindow;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import sancho.core.Sancho;
import sancho.model.mldonkey.ClientStats;
import sancho.utility.MyObservable;
import sancho.utility.VersionInfo;
import sancho.view.MainWindow;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.statusline.actions.DNDBoxAction;
import sancho.view.statusline.actions.PreferencesAction;
import sancho.view.utility.SResources;
import sancho.view.utility.dialogs.BandwidthDialog;

public class MinimizerTray extends Minimizer implements DisposeListener, IMenuListener {
   protected static final String S_DL = "DL: ";
   protected static final String S_UL = ", UL: ";
   private TrayItem trayItem;
   private MenuManager popupMenu;
   private Menu trayMenu;
   private boolean closeMe = false;
   private MainWindow mainWindow;

   public MinimizerTray(MainWindow mainWindow, String titleBarText) {
      super(mainWindow, titleBarText);
      this.mainWindow = mainWindow;
      this.createTrayIcon();
      this.createMenu();
      this.shell.addDisposeListener(this);
      this.setConnected(true);
   }

   public void setConnected(boolean connected) {
      super.setConnected(connected);
      if (connected && Sancho.hasCollectionFactory()) {
         Sancho.getCore().getClientStats().addObserver(this);
      }
   }

   private void createTrayIcon() {
      // getSystemTray() returns null on tray-less platforms (modern GNOME/Wayland,
      // some gtk) even though hasTray() green-lights them — and new TrayItem(null,0)
      // throws ERROR_NULL_ARGUMENT, which killed startup. The old "trayItem == null"
      // guard was dead (a constructor never returns null). Guard the tray itself; when
      // absent, leave trayItem null and let the app run with plain window minimize.
      Tray tray = this.mainWindow.getShell().getDisplay().getSystemTray();
      if (tray != null) {
         this.trayItem = new TrayItem(tray, 0);
         this.trayItem.setImage(VersionInfo.getTrayIcon());
         this.trayItem.setToolTipText(this.titleBarText);
         this.trayItem.addListener(35, new Listener() {
            public void handleEvent(Event event) {
               Menu menu = MinimizerTray.this.trayMenu;
               if (menu != null && !menu.isDisposed()) {
                  menu.setVisible(true);
               }
            }
         });
         this.trayItem.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event) {
               if (!PreferenceLoader.loadBoolean("systraySingleClick")) {
                  MinimizerTray.this.toggleFromTray();
               }
            }

            public void widgetSelected(SelectionEvent event) {
               if (PreferenceLoader.loadBoolean("systraySingleClick")) {
                  MinimizerTray.this.toggleFromTray();
               }
            }
         });
      }
   }

   private void createMenu() {
      this.popupMenu = new MenuManager("");
      this.popupMenu.setRemoveAllWhenShown(true);
      this.popupMenu.addMenuListener(this);
      // Build the SWT context menu once and reuse it; createContextMenu() mints a new
      // native Menu on every call, so calling it per right-click leaked one each time.
      // setRemoveAllWhenShown(true) + the menu listener still rebuild the items on show.
      this.trayMenu = this.popupMenu.createContextMenu(this.shell);
   }

   public boolean close() {
      if (!this.closeMe && PreferenceLoader.loadBoolean("minimizeOnClose")) {
         this.hide();
         return false;
      } else {
         return true;
      }
   }

   public void hide() {
      if (!this.shell.getMinimized()) {
         this.shell.setMinimized(true);
      }

      if (this.shell.isVisible()) {
         this.shell.setVisible(false);
      }

      if (Sancho.getCoreConsole() != null) {
         Sancho.getCoreConsole().getShell().setVisible(false);
      }
   }

   public void forceClose() {
      this.closeMe = true;
   }

   public void startMin() {
      this.hide();
   }

   public boolean minimize(boolean force) {
      if (!force && !PreferenceLoader.loadBoolean("systrayOnMinimize")) {
         return true;
      } else {
         this.hide();
         return false;
      }
   }

   public boolean minimize() {
      return this.minimize(false);
   }

   // Hide/restore toggle driven by a tray click. Ignored until the main window has
   // finished loading, so a click during the splash cannot restore a half-built window
   // (the tray icon already exists then, to support starting minimized to the tray).
   private void toggleFromTray() {
      if (!this.mainWindow.isGuiReady()) {
         return;
      }

      if (this.shell.isVisible()) {
         this.hide();
      } else {
         this.restore();
         if (this.shell.getMinimized()) {
            this.shell.setMinimized(false);
         }
      }
   }

   public void restore() {
      this.isRestoring = true;
      if (!this.shell.isVisible()) {
         this.shell.setVisible(true);
      }

      this.shell.forceActive();
      this.setTitleBarText();
      if (this.trayItem != null && !this.trayItem.isDisposed()) {
         this.trayItem.setToolTipText(this.titleBarText);
      }

      this.isRestoring = false;
   }

   public void menuAboutToShow(IMenuManager menuManager) {
      menuManager.add(new DNDBoxAction(this.mainWindow));
      menuManager.add(new Separator());
      menuManager.add(new PreferencesAction(this.mainWindow));
      new BandwidthDialog(this.shell, menuManager);
      menuManager.add(new Separator());
      menuManager.add(new HideRestoreAction());
      menuManager.add(new CloseAction());
   }

   public void update(MyObservable observable, Object arg, int id) {
      if (observable instanceof ClientStats) {
         final ClientStats clientStats = (ClientStats)observable;
         if (clientStats != null && this.shell != null && !this.shell.isDisposed()) {
            this.shell.getDisplay().asyncExec(new Runnable() {
               public void run() {
                  if (MinimizerTray.this.shell != null
                     && !MinimizerTray.this.shell.isDisposed()
                     && MinimizerTray.this.trayItem != null
                     && !MinimizerTray.this.trayItem.isDisposed()) {
                     if (MinimizerTray.this.shell.isVisible() && MinimizerTray.this.shell.getMinimized()) {
                        MinimizerTray.this.setTitleBar(clientStats);
                     }

                     MinimizerTray.this.setTrayToolTip(clientStats);
                  }
               }
            });
         }
      }
   }

   public void setTrayToolTip(ClientStats clientStats) {
      StringBuffer buffer = new StringBuffer(32);
      buffer.append(this.titleBarText);
      buffer.append("\n");
      buffer.append(Sancho.getCoreFactory().getDescription());
      buffer.append("\n");
      buffer.append("DL: ");
      buffer.append(clientStats.getTcpDownRateString());
      buffer.append(", UL: ");
      buffer.append(clientStats.getTcpUpRateString());
      if (this.trayItem != null && !this.trayItem.isDisposed()) {
         this.trayItem.setToolTipText(buffer.toString());
      }
   }

   public void widgetDisposed(DisposeEvent disposeEvent) {
      if (Sancho.hasCollectionFactory()) {
         Sancho.getCore().getClientStats().deleteObserver(this);
      }

      if (this.trayItem != null && !this.trayItem.isDisposed()) {
         this.trayItem.dispose();
      }
   }

   // Context-menu action: toggles between hiding to the tray and restoring the window.
   private class HideRestoreAction extends Action {
      public HideRestoreAction() {
         super(SResources.getString(MinimizerTray.this.shell.isVisible() ? "mi.hide" : "mi.restore"));
         this.setImageDescriptor(SResources.getImageDescriptor(MinimizerTray.this.shell.isVisible() ? "minus" : "plus"));
      }

      public void run() {
         if (MinimizerTray.this.shell.isVisible()) {
            MinimizerTray.this.hide();
         } else {
            MinimizerTray.this.restore();
            if (MinimizerTray.this.shell.getMinimized()) {
               MinimizerTray.this.shell.setMinimized(false);
            }
         }
      }
   }

   // Context-menu action: closes the application, bypassing minimize-on-close.
   private class CloseAction extends Action {
      public CloseAction() {
         super(SResources.getString("mi.close"));
         this.setImageDescriptor(SResources.getImageDescriptor("x"));
      }

      public void run() {
         MinimizerTray.this.closeMe = true;
         MinimizerTray.this.shell.close();
      }
   }
}
