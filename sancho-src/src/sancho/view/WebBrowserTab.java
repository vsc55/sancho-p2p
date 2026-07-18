package sancho.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import sancho.core.Sancho;
import sancho.utility.SwissArmy;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.transfer.UniformResourceLocator;
import sancho.view.utility.AbstractTab;
import sancho.view.utility.LinkRipper;
import sancho.view.utility.NoDuplicatesCombo;
import sancho.view.utility.WidgetFactory;

public class WebBrowserTab extends AbstractTab {
   public CTabFolder cTabFolder;
   public NoDuplicatesCombo inputCombo;
   public Pattern regex;
   public Pattern bookmark_href;
   public Pattern bookmark_title;
   public Pattern bookmark_folder;
   public WebBrowserTab$WebBrowserViewFrame viewFrame;
   protected boolean loaded;
   protected boolean loadedBookmarks;
   public int maxFLen = 10;

   public WebBrowserTab(MainWindow var1, String var2) {
      super(var1, var2);

      try {
         this.regex = Pattern.compile(
            "(ed2k://\\|file\\|[^\\|]+\\|(\\d+)\\|([\\dabcdef]+)\\|)|(sig2dat:///?\\|File:[^\\|]+\\|Length:.+?\\|UUHash:\\=.+?\\=)|(\\\"magnet:\\?xt=.+?\\\")|(magnet:\\?xt=.+?\n)|(magnet:\\?xt=.+)|(http://.+?/.+?\\.torrent.+)|(\"http://.+?/.+?\\.torrent\\?[^>]+\")|(http://.+?/.+?\\.torrent)",
            Pattern.CASE_INSENSITIVE
         );
         this.bookmark_href = Pattern.compile("HREF=\"(.+?)\"");
         this.bookmark_title = Pattern.compile("<A.+?>(.+?)</A>");
         this.bookmark_folder = Pattern.compile("<H3.+?>(.+?)</H3>");
      } catch (PatternSyntaxException var4) {
      }

      this.updateDisplay();
   }

   private void activateDropTarget(Combo var1) {
      DropTarget var2 = new DropTarget(var1, 21);
      UniformResourceLocator var3 = UniformResourceLocator.getInstance();
      TextTransfer var4 = TextTransfer.getInstance();
      var2.setTransfer(new Transfer[]{var3, var4});
      var2.addDropListener(new WebBrowserTab$1(this, var3, var1));
   }

   public void browserBack() {
      this.browserBack(this.getSelectedBrowser());
   }

   public void browserBack(Browser var1) {
      if (var1 != null && !var1.isDisposed()) {
         var1.back();
      }
   }

   public void browserForward() {
      this.browserForward(this.getSelectedBrowser());
   }

   public void browserForward(Browser var1) {
      if (var1 != null && !var1.isDisposed()) {
         var1.forward();
      }
   }

   public void browserRefresh() {
      this.browserRefresh(this.getSelectedBrowser());
   }

   public void browserRefresh(Browser var1) {
      if (var1 != null && !var1.isDisposed()) {
         var1.refresh();
      }
   }

   public void browserStop() {
      this.browserStop(this.getSelectedBrowser());
   }

   public void browserStop(Browser var1) {
      if (var1 != null && !var1.isDisposed()) {
         var1.stop();
      }
   }

   public Browser createBrowser(Composite var1) {
      Browser var2;
      try {
         // SWT removed the old Mozilla/XULRunner backend (SWT.MOZILLA). On Windows
         // the plain default (SWT.NONE) is still the legacy Internet Explorer engine,
         // so prefer the modern Edge (Chromium/WebView2) backend there; if WebView2
         // is unavailable it throws, and we fall back to the platform default.
         if ("win32".equals(SWT.getPlatform())) {
            try {
               var2 = new Browser(var1, SWT.EDGE);
            } catch (SWTError var4) {
               Sancho.pDebug("SWT.EDGE unavailable, falling back to default browser: " + var4.toString());
               var2 = new Browser(var1, SWT.NONE);
            }
         } else {
            var2 = new Browser(var1, SWT.NONE);
         }
      } catch (SWTError var5) {
         Sancho.pDebug(var5.toString());
         this.viewFrame.updateCLabelText("Browser failed (see FAQ): " + var5.toString());
         this.viewFrame.updateCLabelToolTip(var5.toString());
         return null;
      } catch (Exception var6) {
         var6.printStackTrace();
         return null;
      }

      this.loaded = true;
      var2.setLayoutData(new GridData(1808));
      WebBrowserTab$2 var3 = new WebBrowserTab$2(this);
      var2.addStatusTextListener(var3);
      var2.addTitleListener(new WebBrowserTab$3(this));
      var2.addCloseWindowListener(new WebBrowserTab$4(this));
      var2.addOpenWindowListener(new WebBrowserTab$5(this));
      var2.addLocationListener(new WebBrowserTab$6(this));
      return var2;
   }

   public Browser createBrowserTab() {
      Composite var1 = new Composite(this.cTabFolder, 0);
      CTabItem var2 = new CTabItem(this.cTabFolder, 0);
      var2.setControl(var1);
      var2.setText("blank");
      var1.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      this.inputCombo = new NoDuplicatesCombo(var1, 0);
      this.inputCombo.setLayoutData(new GridData(768));

      try {
         if (SWT.getPlatform().equals("win32") && PreferenceLoader.loadBoolean("dragAndDrop")) {
            this.activateDropTarget(this.inputCombo);
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      WebBrowserTab$7 var3 = new WebBrowserTab$7(this);
      this.inputCombo.addKeyListener(var3);
      Browser var4 = this.createBrowser(var1);
      if (var4 != null) {
         var4.setData("cTabItem", var2);
         var2.setData("browser", var4);
      }

      return var4;
   }

   public void updateDisplay() {
      super.updateDisplay();
      this.maxFLen = Math.max(PreferenceLoader.loadInt("maxFavoriteLength"), 10);
   }

   protected void createContents(Composite var1) {
      this.viewFrame = new WebBrowserTab$WebBrowserViewFrame(this, var1, "tab.webbrowser", "tab.webbrowser.buttonSmall", this);
      this.addViewFrame(this.viewFrame);
      this.cTabFolder = WidgetFactory.createCTabFolder(
         this.viewFrame.getChildComposite(), 64 | (PreferenceLoader.loadBoolean("webBrowserCTabFolderTabsOnTop") ? 128 : 1024)
      );
      WidgetFactory.addCTabFolderMenu(this.cTabFolder, "webBrowserCTabFolder");
      if (this.createBrowserTab() != null) {
         this.cTabFolder.setSelection(0);
         this.cTabFolder.addCTabFolder2Listener(new WebBrowserTab$8(this));
         this.cTabFolder.addSelectionListener(new WebBrowserTab$9(this));
      }
   }

   public void createFavoritesMenu(IMenuManager var1) {
      if (this.loaded) {
         var1.add(new WebBrowserTab$NewBrowserTabAction(this));
         var1.add(new Separator());
         String var2 = PreferenceLoader.loadStringEnv("bookmarksFile");
         File var3 = new File(var2);
         if (var2 != null && !var2.equals("") && var3.exists()) {
            this.loadedBookmarks = true;

            try {
               BufferedReader var4 = new BufferedReader(new FileReader(var3));
               this.traverseBookmarks(var4, var1);
            } catch (Exception var6) {
               System.out.println(var6);
            }
         }

         var2 = PreferenceLoader.loadStringEnv("adrFile");
         var3 = new File(var2);
         if (var2 != null && !var2.equals("") && var3.exists()) {
            this.loadedBookmarks = true;

            try {
               BufferedReader var9 = new BufferedReader(new FileReader(var3));
               this.traverseADR(var9, var1);
            } catch (Exception var5) {
               System.out.println(var5);
            }
         }
      }
   }

   protected void addIfFull(IMenuManager var1, String var2, String var3) {
      if (var2 != null && var3 != null) {
         var1.add(new WebBrowserTab$ADRBookmark(this, var2, var3));
      }
   }

   public void traverseADR(BufferedReader var1, IMenuManager var2) {
      ArrayList var3 = new ArrayList();
      byte var5 = 0;
      Object var6 = var2;
      String var7 = null;
      String var8 = null;

      try {
         String var4;
         while ((var4 = var1.readLine()) != null) {
            var4 = var4.trim();
            if (var4.startsWith("#FOLDER")) {
               var5 = 1;
            } else if (var4.startsWith("-")) {
               int var9 = var3.size() - 1;
               if (var9 >= 0) {
                  IMenuManager var10 = (IMenuManager)var3.get(var9);
                  var3.remove(var9);
                  var10.add((IContributionItem)var6);
                  var6 = var10;
               }

               var5 = 0;
            } else if (var4.startsWith("#URL")) {
               var7 = null;
               var8 = null;
               var5 = 2;
            } else if (var4.startsWith("NAME=")) {
               if (var5 == 1) {
                  var3.add(var6);
                  var6 = new MenuManager(this.formatTitle(var4.substring(5)));
               } else if (var5 == 2) {
                  var7 = var4.substring(5);
                  this.addIfFull((IMenuManager)var6, var7, var8);
               }
            } else if (var4.startsWith("URL=")) {
               var8 = var4.substring(4);
               this.addIfFull((IMenuManager)var6, var7, var8);
            }
         }

         var1.close();
      } catch (IOException var11) {
         System.out.println(var11);
      }
   }

   public void traverseBookmarks(BufferedReader var1, IMenuManager var2) {
      ArrayList var3 = new ArrayList();
      boolean var5 = false;
      int var6 = 0;
      Object var7 = var2;

      try {
         String var4;
         while ((var4 = var1.readLine()) != null) {
            var4 = var4.trim();
            if (var4.startsWith("<DL>")) {
               if (!var5) {
                  var5 = true;
               } else {
                  var6++;
               }
            } else if (!var4.startsWith("</DL>")) {
               if (var4.startsWith("<DT>")) {
                  if (var4.indexOf("HREF=") != -1) {
                     ((IMenuManager)var7).add(new WebBrowserTab$NSBookmark(this, var4));
                  } else {
                     var3.add(var7);
                     Matcher var14 = this.bookmark_folder.matcher(var4);
                     String var15 = "Folder";
                     if (var14.find()) {
                        int var10 = var14.start(1);
                        int var11 = var14.end(1);
                        if (!var14.find()) {
                           var15 = var4.substring(var10, var11);
                        }
                     }

                     var7 = new MenuManager(this.formatTitle(var15));
                  }
               }
            } else {
               for (int var8 = var3.size(); var8 >= var6 && var8 > 0; var8--) {
                  IMenuManager var9 = (IMenuManager)var3.get(var8 - 1);
                  var3.remove(var8 - 1);
                  var9.add((IContributionItem)var7);
                  var7 = var9;
               }

               var6--;
            }
         }

         var1.close();
      } catch (IOException var12) {
         System.out.println(var12);
      }
   }

   protected boolean ctrlDown() {
      return false;
   }

   public String getInputText() {
      return this.inputCombo.getText();
   }

   protected void setupCtrlKey(Browser var1) {
   }

   public String[] getCurrentLinks() {
      return new String[0];
   }

   public Browser getSelectedBrowser() {
      if (this.cTabFolder == null) {
         return null;
      } else {
         CTabItem var1 = this.cTabFolder.getSelection();
         return var1 == null ? null : (Browser)var1.getData("browser");
      }
   }

   public void navigate(Browser var1, String var2) {
      if (var1 != null && !var1.isDisposed()) {
         if (var2.indexOf("//") == -1) {
            var2 = "http://" + var2;
         }

         var1.setUrl(var2);
      }
   }

   public void navigate(String var1) {
      this.navigate(this.getSelectedBrowser(), var1);
   }

   protected void onChanged(LocationEvent var1) {
      Browser var2 = (Browser)var1.widget;
      CTabItem var3 = (CTabItem)var2.getData("cTabItem");
      if (var3 == this.cTabFolder.getSelection()) {
         if (this.getMainWindow().getLinkRipper() != null) {
            LinkRipper var4 = this.getMainWindow().getLinkRipper();
            var4.setInputURL(var1.location);
         }

         this.inputCombo.setText(var1.location);
      }
   }

   protected String formatTitle(String var1) {
      int var2 = var1.length();
      if (var2 > this.maxFLen) {
         String var3 = var1.substring(0, this.maxFLen - 7);
         String var4 = var1.substring(var2 - 4, var2);
         var1 = var3 + "..." + var4;
      }

      if (var1.indexOf("&") != -1) {
         var1 = SwissArmy.replaceAll(var1, "&", "&&");
      }

      return var1;
   }
}
