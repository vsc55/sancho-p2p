package sancho.view.mainWindow;

import java.io.File;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import sancho.core.Sancho;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.MainWindow;
import sancho.view.downloadComplete.DownloadCompleteShell;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.AbstractTab;
import sancho.view.utility.CSpinner;
import sancho.view.utility.SResources;
import sancho.view.utility.VersionChecker;
import sancho.view.utility.WebLauncher;
import sancho.view.utility.WidgetFactory;
import sancho.view.utility.dialogs.AboutDialog;
import sancho.view.utility.dialogs.CoreVerbosityDialog;
import sancho.view.utility.dialogs.DebugDialog;
import sancho.view.utility.setupWizard.SetupWizard;
import sancho.view.utility.setupWizard.SetupWizardDialog;

public class MenuBar implements Runnable {
   private MainWindow mainWindow;
   private Shell shell;
   private Menu mainMenuBar;
   private Menu subMenu;
   private MenuItem menuItem;
   private boolean fullScreen;

   public MenuBar(MainWindow mainWindow) {
      this.mainWindow = mainWindow;
      this.shell = mainWindow.getShell();
      this.createContent();
   }

   // Runnable target for the screenshot menu: grabs the window (or the whole screen when
   // fullScreen is set) and saves it as a PNG. Deferred via Display.timerExec so the menu
   // is gone before the grab.
   public void run() {
      int width = 0;
      int height = 0;
      int x = 0;
      int y = 0;
      Display display = this.shell.getDisplay();
      if (this.fullScreen) {
         Rectangle bounds = display.getBounds();
         width = bounds.width;
         height = bounds.height;
      } else {
         Rectangle bounds = this.shell.getBounds();
         width = bounds.width;
         height = bounds.height;
         x = bounds.x;
         y = bounds.y;
      }

      Image image = new Image(display, width, height);

      try {
         GC gc = new GC(display);
         gc.copyArea(image, x, y);
         gc.dispose();
         FileDialog fileDialog = new FileDialog(this.shell, 8192);
         fileDialog.setFilterExtensions(new String[]{"*.png"});
         String path = fileDialog.open();
         if (path != null) {
            ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[]{image.getImageData()};
            loader.save(path, 5);
         }
      } finally {
         // The window-sized Image was never disposed (neither on save nor on
         // cancel), leaking a large native bitmap on every screenshot.
         image.dispose();
      }
   }

   private void createContent() {
      this.mainMenuBar = new Menu(this.shell, 2);
      this.shell.setMenuBar(this.mainMenuBar);

      // --- File menu (rebuilt on show, so its items track the connection state) ---
      this.menuItem = new MenuItem(this.mainMenuBar, 64);
      this.menuItem.setText("&" + SResources.getString("menu.file"));
      final Menu fileMenu = new Menu(this.shell, 4);
      this.menuItem.setMenu(fileMenu);
      fileMenu.addMenuListener(new MenuAdapter() {
         public void menuShown(MenuEvent menuEvent) {
            MenuItem[] items = fileMenu.getItems();

            for (int i = 0; i < items.length; i++) {
               items[i].dispose();
            }

            if (Sancho.getCoreFactory().isAutoReconnecting()) {
               MenuBar.this.createMenuItem(fileMenu, "&" + SResources.getString("menu.file.stopAutoReconnect"), "nuke", new Listener() {
                  public void handleEvent(Event event) {
                     Sancho.getCoreFactory().setAutoReconnecting(false);
                  }
               });
            }

            if (Sancho.getCoreFactory().isConnected()) {
               MenuBar.this.createMenuItem(fileMenu, "&" + SResources.getString("menu.file.inputLink"), "tab.transfers.buttonSmall", new Listener() {
                  public void handleEvent(Event event) {
                     InputDialog dialog = new InputDialog(
                        MenuBar.this.mainWindow.getShell(),
                        SResources.getString("menu.file.inputLink"),
                        SResources.getString("menu.file.inputLink"),
                        "",
                        null
                     );
                     dialog.open();
                     String link = dialog.getValue();
                     if (link != null && !link.equals("") && Sancho.getCore() != null) {
                        SwissArmy.sendLink(Sancho.getCore(), link);
                     }
                  }
               });
               MenuBar.this.createMenuItem(
                  fileMenu, "&" + SResources.getString("menu.file.loadTorrents") + "\tCTRL+T", "tab.transfers.buttonSmall", new Listener() {
                     public void handleEvent(Event event) {
                        MenuBar.this.mainWindow.sendTorrentsFromHD();
                     }
                  }
               );
            }

            if (Sancho.getCoreFactory().isConnected() && Sancho.getCoreConsole() == null) {
               MenuBar.this.createMenuItem(fileMenu, "&" + SResources.getString("menu.file.killCore"), "nuke", new Listener() {
                  public void handleEvent(Event event) {
                     MessageBox box = new MessageBox(MenuBar.this.shell, 196);
                     box.setMessage(SResources.getString("mi.areYouSure"));
                     if (box.open() == 64 && MenuBar.this.mainWindow.getCore() != null) {
                        Sancho.send((short)3);
                     }
                  }
               });
            }

            if (Sancho.hasCollectionFactory()) {
               MenuBar.this.createMenuItem(fileMenu, "&" + SResources.getString("menu.file.disconnect"), "menu-disconnect", new Listener() {
                  public void handleEvent(Event event) {
                     if (Sancho.getCore() != null) {
                        Sancho.getCoreFactory().disconnect();
                     }
                  }
               });
            } else {
               MenuBar.this.menuItem = new MenuItem(fileMenu, 64);
               MenuBar.this.menuItem.setText(SResources.getString("menu.file.connect"));
               MenuBar.this.menuItem.setImage(SResources.getImage("menu-connect"));
               Menu connectMenu = new Menu(MenuBar.this.menuItem);
               MenuBar.this.menuItem.setMenu(connectMenu);
               MenuBar.this.createMenuItem(connectMenu, "&" + SResources.getString("menu.file.reconnect"), "menu-connect", new Listener() {
                  public void handleEvent(Event event) {
                     Sancho.getCoreFactory().reconnect();
                  }
               });
               MenuBar.this.menuItem = new MenuItem(connectMenu, 2);

               for (int hostIndex = 0; hostIndex <= 0 || PreferenceLoader.contains("hm_" + hostIndex + "_hostname"); hostIndex++) {
                  final int host = hostIndex;
                  String prefix = host == 0 ? SResources.getString("l.default") + ": " : "";
                  String description = PreferenceLoader.loadString("hm_" + host + "_description");
                  if (description.equals("")) {
                     description = PreferenceLoader.loadString("hm_" + host + "_hostname") + ":" + PreferenceLoader.loadString("hm_" + host + "_port");
                  }

                  MenuBar.this.createMenuItem(connectMenu, prefix + description, "menu-connect", new Listener() {
                     public void handleEvent(Event event) {
                        Sancho.getCoreFactory().reconnect(host);
                     }
                  });
               }
            }

            MenuBar.this.menuItem = new MenuItem(fileMenu, 2);
            String exitText = SResources.getString("menu.file.exit");
            if (PreferenceLoader.loadBoolean("killCoreOnExit") && Sancho.getCoreFactory().isConnected()) {
               exitText = SResources.getString("menu.file.exitAndKill");
            }

            MenuBar.this.createMenuItem(fileMenu, exitText, "x", new Listener() {
               public void handleEvent(Event event) {
                  MenuBar.this.mainWindow.getMinimizer().forceClose();
                  MenuBar.this.shell.close();
               }
            });
         }
      });

      // --- View menu (one entry per tab, rebuilt on show) ---
      this.menuItem = new MenuItem(this.mainMenuBar, 64);
      this.menuItem.setText("&" + SResources.getString("menu.view"));
      final Menu viewMenu = new Menu(this.shell, 4);
      this.menuItem.setMenu(viewMenu);
      viewMenu.addMenuListener(new MenuAdapter() {
         public void menuShown(MenuEvent menuEvent) {
            MenuItem[] items = viewMenu.getItems();

            for (int i = 0; i < items.length; i++) {
               items[i].dispose();
            }

            List tabs = MenuBar.this.mainWindow.getTabs();

            for (int i = 0; i < tabs.size(); i++) {
               final AbstractTab tab = (AbstractTab)tabs.get(i);
               MenuBar.this.menuItem = new MenuItem(viewMenu, 8);
               MenuBar.this.menuItem.setText(tab.getToolButton().getText());
               MenuBar.this.menuItem.setImage(tab.getToolButton().getSmallInActiveImage());
               MenuBar.this.menuItem.addListener(13, new Listener() {
                  public void handleEvent(Event event) {
                     tab.setActive();
                  }
               });
            }

            MenuBar.this.menuItem = new MenuItem(viewMenu, 2);
            MenuBar.this.createMenuItem(viewMenu, "&" + SResources.getString("menu.view.tabSelector"), "preferences", new Listener() {
               public void handleEvent(Event event) {
                  MenuBar.this.mainWindow.configureTabs();
               }
            });
         }
      });

      // --- Tools menu ---
      this.menuItem = new MenuItem(this.mainMenuBar, 64);
      this.menuItem.setText("&" + SResources.getString("menu.tools"));
      this.subMenu = new Menu(this.shell, 4);
      this.menuItem.setMenu(this.subMenu);
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.downloadHistory"), "tab.transfers.buttonSmall", new Listener() {
         public void handleEvent(Event event) {
            File logFile = new File(VersionInfo.getDownloadLogFile());
            if (!logFile.exists()) {
               MessageBox box = new MessageBox(MenuBar.this.shell, 34);
               box.setMessage(SResources.getString("l.noCompleteDownloads") + "\n" + VersionInfo.getDownloadLogFile());
               box.open();
            } else {
               new DownloadCompleteShell(MenuBar.this.mainWindow.getShell()).open();
            }
         }
      });
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.debug"), "info", new Listener() {
         public void handleEvent(Event event) {
            new DebugDialog(MenuBar.this.shell).open();
         }
      });
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.coreVerbosity"), "info", new Listener() {
         public void handleEvent(Event event) {
            if (Sancho.hasCollectionFactory()) {
               new CoreVerbosityDialog(MenuBar.this.shell).open();
            }
         }
      });
      this.menuItem = new MenuItem(this.subMenu, 64);
      this.menuItem.setText(SResources.getString("menu.tools.transparency"));
      this.menuItem.setImage(SResources.getImage("transparency"));
      Menu transparencyMenu = new Menu(this.menuItem);
      this.menuItem.setMenu(transparencyMenu);

      for (int step = 0; step < 10; step++) {
         final int transparencyStep = step;
         this.createMenuItem(transparencyMenu, step * 10 + "%", null, new Listener() {
            public void handleEvent(Event event) {
               int alpha = 255 - 255 * transparencyStep * 10 / 100;
               MenuBar.this.shell.setAlpha(alpha);
            }
         });
      }

      this.createMenuItem(transparencyMenu, "Custom", null, new Listener() {
         public void handleEvent(Event event) {
            new AlphaInputDialog(MenuBar.this.shell).open();
         }
      });
      this.menuItem = new MenuItem(this.subMenu, 64);
      this.menuItem.setText(SResources.getString("menu.tools.screenshot"));
      this.menuItem.setImage(SResources.getImage("camera"));
      Menu screenshotMenu = new Menu(this.menuItem);
      this.menuItem.setMenu(screenshotMenu);
      this.createMenuItem(screenshotMenu, "&" + SResources.getString("menu.tools.screenshot.window"), "camera", new Listener() {
         public void handleEvent(Event event) {
            MenuBar.this.fullScreen = false;
            MenuBar.this.shell.getDisplay().timerExec(250, MenuBar.this);
         }
      });
      this.createMenuItem(screenshotMenu, "&" + SResources.getString("menu.tools.screenshot.screen"), "camera", new Listener() {
         public void handleEvent(Event event) {
            MenuBar.this.fullScreen = true;
            MenuBar.this.shell.getDisplay().timerExec(250, MenuBar.this);
         }
      });
      this.menuItem = new MenuItem(this.subMenu, 2);
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.hostManager"), "cabinet", new Listener() {
         public void handleEvent(Event event) {
            SetupWizardDialog dialog = new SetupWizardDialog(MenuBar.this.mainWindow.getShell(), new SetupWizard());
            dialog.create();
            dialog.open();
         }
      });
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.preferences"), "preferences", new Listener() {
         public void handleEvent(Event event) {
            MenuBar.this.mainWindow.openPreferences();
         }
      });

      // --- Help menu ---
      this.menuItem = new MenuItem(this.mainMenuBar, 64);
      this.menuItem.setText("&" + SResources.getString("menu.help"));
      this.subMenu = new Menu(this.shell, 4);
      this.menuItem.setMenu(this.subMenu);
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.help.homepage"), "earth", new URLListener(VersionInfo.getHomePage2()));
      this.createMenuItem(this.subMenu, "&Donate", "earth", new URLListener(VersionInfo.getHomePage2()));
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.help.feedback"), "earth", new URLListener("mailto:" + VersionInfo.getEmail()));
      this.menuItem = new MenuItem(this.subMenu, 2);
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.help.checkVersion"), "ProgramIcon", new Listener() {
         public void handleEvent(Event event) {
            new VersionChecker(MenuBar.this.shell, MenuBar.this.mainWindow.getStatusline(), 0, true);
         }
      });
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.help.about"), "commit_question", new Listener() {
         public void handleEvent(Event event) {
            new AboutDialog(MenuBar.this.shell).open();
         }
      });
   }

   public MenuItem createMenuItem(Menu menu, String text, String imageName, Listener listener) {
      this.menuItem = new MenuItem(menu, 8);
      this.menuItem.setText(text);
      if (imageName != null) {
         this.menuItem.setImage(SResources.getImage(imageName));
      }

      this.menuItem.addListener(13, listener);
      return this.menuItem;
   }

   // The "Custom" transparency dialog: a 0..255 alpha slider that updates the main shell
   // live as it moves.
   private static class AlphaInputDialog extends Dialog {
      CSpinner spinner;
      Button okButton;
      Shell mainShell;

      public AlphaInputDialog(Shell shell) {
         super(shell);
         this.mainShell = shell;
      }

      protected void configureShell(Shell shell) {
         super.configureShell(shell);
         shell.setImage(VersionInfo.getProgramIcon());
         shell.setText(String.valueOf(this.mainShell.getAlpha() & 0xFF));
      }

      protected void createButtonsForButtonBar(Composite parent) {
         this.okButton = this.createButton(parent, 0, SResources.getString("b.ok"), true);
      }

      protected Button getOkButton() {
         return this.okButton;
      }

      protected Control createDialogArea(Composite parent) {
         Composite area = (Composite)super.createDialogArea(parent);
         area.setLayout(WidgetFactory.createGridLayout(2, 5, 5, 10, 5, false));
         final Scale scale = new Scale(area, 256);
         GridData gridData = new GridData(768);
         gridData.widthHint = 300;
         scale.setLayoutData(gridData);
         scale.setMinimum(0);
         scale.setMaximum(255);
         scale.setIncrement(1);
         scale.setPageIncrement(5);
         scale.setSelection(this.mainShell.getAlpha() & 0xFF);
         scale.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               int alpha = scale.getSelection();
               AlphaInputDialog.this.mainShell.setAlpha(alpha);
               AlphaInputDialog.this.getShell().setText(String.valueOf(alpha));
            }
         });
         return area;
      }

      protected void buttonPressed(int buttonId) {
         super.buttonPressed(buttonId);
      }
   }

   // A menu item that opens a URL (homepage / donate / feedback) in the web browser.
   private static class URLListener implements Listener {
      private String url;

      public URLListener(String url) {
         this.url = url;
      }

      public void handleEvent(Event event) {
         WebLauncher.openLink(this.url);
      }
   }
}
