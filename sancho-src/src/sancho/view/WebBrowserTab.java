package sancho.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import sancho.core.Sancho;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.transfer.UniformResourceLocator;
import sancho.view.utility.AbstractTab;
import sancho.view.utility.LinkRipper;
import sancho.view.utility.NoDuplicatesCombo;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;
import sancho.view.viewFrame.ViewFrame;
import sancho.view.viewFrame.ViewListener;

public class WebBrowserTab extends AbstractTab {
   public CTabFolder cTabFolder;
   public NoDuplicatesCombo inputCombo;
   public Pattern regex;
   public Pattern bookmark_href;
   public Pattern bookmark_title;
   public Pattern bookmark_folder;
   public WebBrowserViewFrame viewFrame;
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

   private void activateDropTarget(Combo combo) {
      DropTarget dropTarget = new DropTarget(combo, 21);
      final UniformResourceLocator uRL = UniformResourceLocator.getInstance();
      TextTransfer textTransfer = TextTransfer.getInstance();
      dropTarget.setTransfer(new Transfer[]{uRL, textTransfer});
      final Combo linkEntryCombo = combo;
      dropTarget.addDropListener(new DropTargetAdapter() {
         public void dragEnter(DropTargetEvent event) {
            // Only request DROP_LINK when the source actually offers it; forcing detail=4
            // unconditionally made SWT reject a COPY-only drag (plain text/selection), so
            // the drop was silently never delivered. Fall back to COPY, else NONE.
            boolean supported = false;

            for (int i = 0; i < event.dataTypes.length; i++) {
               if (uRL.isSupportedType(event.dataTypes[i])) {
                  supported = true;
                  break;
               }
            }

            if (supported && (event.operations & 4) != 0) {
               event.detail = 4;
            } else if ((event.operations & 1) != 0) {
               event.detail = 1;
            } else {
               event.detail = 0;
            }
         }

         public void drop(DropTargetEvent event) {
            if (event.data != null) {
               linkEntryCombo.setText((String)event.data);
            }
         }
      });
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
      Browser browser;
      try {
         // SWT removed the old Mozilla/XULRunner backend (SWT.MOZILLA). On Windows
         // the plain default (SWT.NONE) is still the legacy Internet Explorer engine,
         // so prefer the modern Edge (Chromium/WebView2) backend there; if WebView2
         // is unavailable it throws, and we fall back to the platform default.
         if ("win32".equals(SWT.getPlatform())) {
            try {
               browser = new Browser(var1, SWT.EDGE);
            } catch (SWTError edgeUnavailable) {
               Sancho.pDebug("SWT.EDGE unavailable, falling back to default browser: " + edgeUnavailable.toString());
               browser = new Browser(var1, SWT.NONE);
            }
         } else {
            browser = new Browser(var1, SWT.NONE);
         }
      } catch (SWTError browserError) {
         Sancho.pDebug(browserError.toString());
         this.viewFrame.updateCLabelText("Browser failed (see FAQ): " + browserError.toString());
         this.viewFrame.updateCLabelToolTip(browserError.toString());
         return null;
      } catch (Exception otherError) {
         otherError.printStackTrace();
         return null;
      }

      this.loaded = true;
      browser.setLayoutData(new GridData(1808));
      browser.addStatusTextListener(new StatusTextListener() {
         public void changed(StatusTextEvent event) {
            Browser eventBrowser = (Browser)event.widget;
            if (eventBrowser != null && !eventBrowser.isDisposed()) {
               CTabItem tabItem = (CTabItem)eventBrowser.getData("cTabItem");
               if (tabItem == WebBrowserTab.this.cTabFolder.getSelection()) {
                  WebBrowserTab.this.getMainWindow().getStatusline().setText(event.text);
               }
            }
         }
      });
      browser.addTitleListener(new TitleListener() {
         public void changed(TitleEvent event) {
            Browser eventBrowser = (Browser)event.widget;
            if (eventBrowser != null && !eventBrowser.isDisposed()) {
               CTabItem tabItem = (CTabItem)eventBrowser.getData("cTabItem");
               // Null-check BEFORE setText: a title event can arrive before createBrowserTab
               // stores "cTabItem" (the Edge backend delivers titles from an async pump), so
               // dereferencing tabItem first threw an NPE and the title stopped updating.
               if (tabItem != null && !tabItem.isDisposed()) {
                  tabItem.setText(event.title);
                  if (tabItem == WebBrowserTab.this.cTabFolder.getSelection()) {
                     WebBrowserTab.this.viewFrame.updateCLabelText(event.title);
                  }
               }
            }
         }
      });
      browser.addCloseWindowListener(new CloseWindowListener() {
         public void close(WindowEvent event) {
            Browser eventBrowser = (Browser)event.widget;
            if (eventBrowser != null && !eventBrowser.isDisposed()) {
               CTabItem tabItem = (CTabItem)eventBrowser.getData("cTabItem");
               if (tabItem != null && !tabItem.isDisposed() && !WebBrowserTab.this.cTabFolder.isDisposed()) {
                  tabItem.dispose();
               }
            }
         }
      });
      browser.addOpenWindowListener(new OpenWindowListener() {
         public void open(WindowEvent event) {
            Browser newBrowser = WebBrowserTab.this.createBrowserTab();
            if (newBrowser != null) {
               event.browser = newBrowser;
            }
         }
      });
      browser.addLocationListener(new LocationListener() {
         public void changed(LocationEvent event) {
            WebBrowserTab.this.onChanged(event);
         }

         public void changing(LocationEvent event) {
            if (event.location != null) {
               String location = event.location;
               if (WebBrowserTab.this.regex.matcher(location).find()) {
                  WebBrowserTab.this.getMainWindow().getStatusline().setText(SResources.getString("l.sending") + location);
                  event.doit = false;
                  Sancho.send((short)8, location);
               } else if (event.top) {
                  WebBrowserTab.this.onChangingTop(event);
               }
            }
         }
      });
      return browser;
   }

   public Browser createBrowserTab() {
      Composite var1 = new Composite(this.cTabFolder, 0);
      CTabItem var2 = new CTabItem(this.cTabFolder, 0);
      var2.setControl(var1);
      var2.setText("blank");
      var1.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      this.inputCombo = new NoDuplicatesCombo(var1, 0);
      this.inputCombo.setLayoutData(new GridData(768));
      // Bind this combo to its tab so every read/write targets the right one; the
      // shared this.inputCombo field only ever points at the last-created tab, so with
      // multiple tabs the URL used to land in the wrong (last) tab's address bar.
      var2.setData("inputCombo", this.inputCombo);

      try {
         if (SWT.getPlatform().equals("win32") && PreferenceLoader.loadBoolean("dragAndDrop")) {
            this.activateDropTarget(this.inputCombo);
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      this.inputCombo.addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent event) {
            NoDuplicatesCombo combo = (NoDuplicatesCombo)event.widget;
            if (event.character == '\r' || event.keyCode == 16777296) {
               WebBrowserTab.this.navigate(combo.getText());
               combo.add(combo.getText(), 0);
               combo.setText("");
            }
         }
      });
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
      this.viewFrame = new WebBrowserViewFrame(var1, "tab.webbrowser", "tab.webbrowser.buttonSmall", this);
      this.addViewFrame(this.viewFrame);
      this.cTabFolder = WidgetFactory.createCTabFolder(
         this.viewFrame.getChildComposite(), 64 | (PreferenceLoader.loadBoolean("webBrowserCTabFolderTabsOnTop") ? 128 : 1024)
      );
      WidgetFactory.addCTabFolderMenu(this.cTabFolder, "webBrowserCTabFolder");
      if (this.createBrowserTab() != null) {
         this.cTabFolder.setSelection(0);
         this.cTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            public void close(CTabFolderEvent event) {
               CTabItem tabItem = (CTabItem)event.item;
               Browser browser = (Browser)tabItem.getData("browser");
               if (WebBrowserTab.this.cTabFolder.getItemCount() == 1) {
                  if (browser != null && !browser.isDisposed()) {
                     browser.setUrl("about:blank");
                  }

                  NoDuplicatesCombo combo = WebBrowserTab.this.getInputCombo(tabItem);
                  if (combo != null) {
                     combo.setText("");
                  }

                  WebBrowserTab.this.viewFrame.updateCLabelText(SResources.getString("tab.webbrowser"));
                  tabItem.setText("blank");
                  event.doit = false;
               } else if (browser != null && !browser.isDisposed()) {
                  browser.dispose();
               }
            }
         });
         this.cTabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               CTabItem tabItem = (CTabItem)event.item;
               Browser browser = (Browser)tabItem.getData("browser");
               NoDuplicatesCombo combo = WebBrowserTab.this.getInputCombo(tabItem);
               if (combo != null) {
                  if (browser != null && !browser.isDisposed()) {
                     combo.setText(browser.getUrl());
                  }

                  combo.setFocus();
               }

               WebBrowserTab.this.viewFrame.updateCLabelText(tabItem.getText());
            }
         });
      }
   }

   public void createFavoritesMenu(IMenuManager var1) {
      if (this.loaded) {
         var1.add(new NewBrowserTabAction());
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
         var1.add(new ADRBookmark(var2, var3));
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
                     ((IMenuManager)var7).add(new NSBookmark(var4));
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

   public String getInputText() {
      NoDuplicatesCombo var1 = this.getSelectedInputCombo();
      return var1 == null ? "" : var1.getText();
   }

   public NoDuplicatesCombo getInputCombo(CTabItem var1) {
      return var1 == null ? null : (NoDuplicatesCombo)var1.getData("inputCombo");
   }

   public NoDuplicatesCombo getSelectedInputCombo() {
      return this.cTabFolder == null ? null : this.getInputCombo(this.cTabFolder.getSelection());
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

         NoDuplicatesCombo var5 = this.getInputCombo(var3);
         if (var5 != null) {
            var5.setText(var1.location);
         }
      }
   }

   protected void onChangingTop(LocationEvent var1) {
      Browser var2 = (Browser)var1.widget;
      CTabItem var3 = (CTabItem)var2.getData("cTabItem");
      NoDuplicatesCombo var4 = this.getInputCombo(var3);
      if (var4 != null) {
         var4.setText(var1.location);
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

   // The tab's view frame: a browser toolbar (configurable link buttons + stop/refresh/
   // back/forward) plus the favorites menu wiring. Non-static inner class so its tool
   // buttons can reach the enclosing WebBrowserTab directly.
   public class WebBrowserViewFrame extends ViewFrame {
      private int numToolItems;

      public WebBrowserViewFrame(Composite parent, String label, String icon, AbstractTab tab) {
         super(parent, label, icon, tab);
         this.createViewToolBar();
         this.createViewListener(new WebBrowserViewListener(this));
      }

      public void createViewToolBar() {
         super.createViewToolBar();
         this.numToolItems = PreferenceLoader.loadInt("webBrowserToolItems");

         for (int i = 1; i < this.numToolItems + 1; i++) {
            final int toolIndex = i;
            this.addToolItem(PreferenceLoader.loadString("webBrowserToolItem" + i), String.valueOf(i), new SelectionAdapter() {
               public void widgetSelected(SelectionEvent event) {
                  WebBrowserTab.this.navigate(PreferenceLoader.loadString("webBrowserToolItem" + toolIndex));
               }
            });
         }

         this.addToolItem("ti.web.sancho", "ProgramIcon", new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               WebBrowserTab.this.navigate(VersionInfo.getHomePage2());
            }
         });
         this.addToolSeparator();
         this.addToolItem("ti.web.stop", "page-stop", new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               WebBrowserTab.this.browserStop();
            }
         });
         this.addToolItem("ti.web.refresh", "page-refresh", new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               WebBrowserTab.this.browserRefresh();
            }
         });
         this.addToolSeparator();
         this.addToolItem("ti.web.back", "page-back", new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               WebBrowserTab.this.browserBack();
            }
         });
         this.addToolItem("ti.web.forward", "page-forward", new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               WebBrowserTab.this.browserForward();
            }
         });
      }

      public void updateDisplay() {
         super.updateDisplay();
         if (this.numToolItems != PreferenceLoader.loadInt("webBrowserToolItems") && this.toolBar != null) {
            for (int i = this.toolBar.getItemCount() - 1; i >= 0; i--) {
               this.toolBar.getItems()[i].dispose();
            }

            this.toolBar.dispose();
            this.createViewToolBar();
            this.toolBar.getParent().layout();
         } else if (this.toolBar != null) {
            for (int i = 1; i <= this.numToolItems; i++) {
               this.toolBar.getItems()[i - 1].setToolTipText(PreferenceLoader.loadString("webBrowserToolItem" + i));
            }
         }
      }
   }

   // Routes the view frame's context-menu build to the tab's favorites menu.
   public class WebBrowserViewListener extends ViewListener {
      public WebBrowserViewListener(ViewFrame frame) {
         super(frame);
      }

      public void menuAboutToShow(IMenuManager menu) {
         WebBrowserTab.this.createFavoritesMenu(menu);
      }
   }

   // Favorites-menu action: open a fresh browser tab and select it.
   public class NewBrowserTabAction extends Action {
      public NewBrowserTabAction() {
         super(SResources.getString("l.newBrowserTab"));
      }

      public void run() {
         Browser browser = WebBrowserTab.this.createBrowserTab();
         if (browser != null) {
            CTabItem tabItem = (CTabItem)browser.getData("cTabItem");
            WebBrowserTab.this.cTabFolder.setSelection(tabItem);
         }
      }
   }

   // A Netscape-style (bookmarks.html) favorite parsed from a <DT><A HREF=...> line.
   public class NSBookmark extends Action {
      String href;

      public NSBookmark(String line) {
         Matcher titleMatcher = WebBrowserTab.this.bookmark_title.matcher(line);
         Matcher hrefMatcher = WebBrowserTab.this.bookmark_href.matcher(line);
         String title = "Unknown";
         if (titleMatcher.find()) {
            int start = titleMatcher.start(1);
            int end = titleMatcher.end(1);
            if (!titleMatcher.find()) {
               title = line.substring(start, end);
            }
         }

         if (hrefMatcher.find()) {
            int start = hrefMatcher.start(1);
            int end = hrefMatcher.end(1);
            if (!hrefMatcher.find()) {
               this.href = line.substring(start, end);
            }
         }

         this.setText(WebBrowserTab.this.formatTitle(title));
         this.setImageDescriptor(SResources.getImageDescriptor("web-link-m"));
      }

      public void run() {
         if (this.href != null && !this.href.equals("")) {
            WebBrowserTab.this.navigate(WebBrowserTab.this.getSelectedBrowser(), this.href);
         }
      }
   }

   // An Opera-style (.adr) favorite: an explicit name + URL pair.
   public class ADRBookmark extends Action {
      String URL;

      public ADRBookmark(String name, String url) {
         this.setText(WebBrowserTab.this.formatTitle(name));
         this.setImageDescriptor(SResources.getImageDescriptor("web-link-o"));
         this.URL = url;
      }

      public void run() {
         if (this.URL != null && !this.URL.equals("")) {
            WebBrowserTab.this.navigate(WebBrowserTab.this.getSelectedBrowser(), this.URL);
         }
      }
   }
}
