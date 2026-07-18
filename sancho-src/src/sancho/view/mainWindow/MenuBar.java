package sancho.view.mainWindow;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import sancho.utility.VersionInfo;
import sancho.view.MainWindow;
import sancho.view.utility.SResources;

public class MenuBar implements Runnable {
   private MainWindow mainWindow;
   private Shell shell;
   private Menu mainMenuBar;
   private Menu subMenu;
   private MenuItem menuItem;
   private boolean fullScreen;

   public MenuBar(MainWindow var1) {
      this.mainWindow = var1;
      this.shell = var1.getShell();
      this.createContent();
   }

   public void run() {
      int var1 = 0;
      int var2 = 0;
      int var3 = 0;
      int var4 = 0;
      Display var5 = this.shell.getDisplay();
      if (this.fullScreen) {
         Rectangle var6 = var5.getBounds();
         var1 = var6.width;
         var2 = var6.height;
      } else {
         Rectangle var13 = this.shell.getBounds();
         var1 = var13.width;
         var2 = var13.height;
         var3 = var13.x;
         var4 = var13.y;
      }

      Image var14 = new Image(var5, var1, var2);

      try {
         GC var7 = new GC(var5);
         var7.copyArea(var14, var3, var4);
         var7.dispose();
         FileDialog var8 = new FileDialog(this.shell, 8192);
         var8.setFilterExtensions(new String[]{"*.png"});
         String var9 = var8.open();
         if (var9 != null) {
            ImageLoader var10 = new ImageLoader();
            var10.data = new ImageData[]{var14.getImageData()};
            var10.save(var9, 5);
         }
      } finally {
         // The window-sized Image was never disposed (neither on save nor on
         // cancel), leaking a large native bitmap on every screenshot.
         var14.dispose();
      }
   }

   private void createContent() {
      this.mainMenuBar = new Menu(this.shell, 2);
      this.shell.setMenuBar(this.mainMenuBar);
      this.menuItem = new MenuItem(this.mainMenuBar, 64);
      this.menuItem.setText("&" + SResources.getString("menu.file"));
      Menu var1 = new Menu(this.shell, 4);
      this.menuItem.setMenu(var1);
      var1.addMenuListener(new MenuBar$1(this, var1));
      this.menuItem = new MenuItem(this.mainMenuBar, 64);
      this.menuItem.setText("&" + SResources.getString("menu.view"));
      Menu var2 = new Menu(this.shell, 4);
      this.menuItem.setMenu(var2);
      var2.addMenuListener(new MenuBar$10(this, var2));
      this.menuItem = new MenuItem(this.mainMenuBar, 64);
      this.menuItem.setText("&" + SResources.getString("menu.tools"));
      this.subMenu = new Menu(this.shell, 4);
      this.menuItem.setMenu(this.subMenu);
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.downloadHistory"), "tab.transfers.buttonSmall", new MenuBar$13(this));
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.debug"), "info", new MenuBar$15(this));
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.coreVerbosity"), "info", new MenuBar$16(this));
      this.menuItem = new MenuItem(this.subMenu, 64);
      this.menuItem.setText(SResources.getString("menu.tools.transparency"));
      this.menuItem.setImage(SResources.getImage("transparency"));
      Menu var3 = new Menu(this.menuItem);
      this.menuItem.setMenu(var3);

      for (int var4 = 0; var4 < 10; var4++) {
         this.createMenuItem(var3, var4 * 10 + "%", null, new MenuBar$17(this, var4));
      }

      this.createMenuItem(var3, "Custom", null, new MenuBar$18(this));
      this.menuItem = new MenuItem(this.subMenu, 64);
      this.menuItem.setText(SResources.getString("menu.tools.screenshot"));
      this.menuItem.setImage(SResources.getImage("camera"));
      Menu var5 = new Menu(this.menuItem);
      this.menuItem.setMenu(var5);
      this.createMenuItem(var5, "&" + SResources.getString("menu.tools.screenshot.window"), "camera", new MenuBar$19(this));
      this.createMenuItem(var5, "&" + SResources.getString("menu.tools.screenshot.screen"), "camera", new MenuBar$20(this));
      this.menuItem = new MenuItem(this.subMenu, 2);
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.hostManager"), "cabinet", new MenuBar$21(this));
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.tools.preferences"), "preferences", new MenuBar$22(this));
      this.menuItem = new MenuItem(this.mainMenuBar, 64);
      this.menuItem.setText("&" + SResources.getString("menu.help"));
      this.subMenu = new Menu(this.shell, 4);
      this.menuItem.setMenu(this.subMenu);
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.help.homepage"), "earth", new MenuBar$URLListener(VersionInfo.getHomePage2()));
      this.createMenuItem(this.subMenu, "&Donate", "earth", new MenuBar$URLListener(VersionInfo.getHomePage2()));
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.help.feedback"), "earth", new MenuBar$URLListener("mailto:" + VersionInfo.getEmail()));
      this.menuItem = new MenuItem(this.subMenu, 2);
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.help.checkVersion"), "ProgramIcon", new MenuBar$23(this));
      this.createMenuItem(this.subMenu, "&" + SResources.getString("menu.help.about"), "commit_question", new MenuBar$24(this));
   }

   public MenuItem createMenuItem(Menu var1, String var2, String var3, Listener var4) {
      this.menuItem = new MenuItem(var1, 8);
      this.menuItem.setText(var2);
      if (var3 != null) {
         this.menuItem.setImage(SResources.getImage(var3));
      }

      this.menuItem.addListener(13, var4);
      return this.menuItem;
   }

   // $VF: synthetic method
   static MainWindow access$100(MenuBar var0) {
      return var0.mainWindow;
   }

   // $VF: synthetic method
   static Shell access$200(MenuBar var0) {
      return var0.shell;
   }

   // $VF: synthetic method
   static MenuItem access$302(MenuBar var0, MenuItem var1) {
      return var0.menuItem = var1;
   }

   // $VF: synthetic method
   static MenuItem access$300(MenuBar var0) {
      return var0.menuItem;
   }

   // $VF: synthetic method
   static boolean access$502(MenuBar var0, boolean var1) {
      return var0.fullScreen = var1;
   }
}
