package sancho.view.utility;

import sancho.utility.regex.RE;
import sancho.utility.regex.REMatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import sancho.core.Sancho;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.MainWindow;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.transfer.UniformResourceLocator;

public class LinkRipper extends Dialog implements Runnable, IMenuListener {
   static RE endSlashRE;
   static RE frameRE;
   static RE snRE;
   MainWindow mainWindow;
   MenuManager popupMenu;
   boolean ripping;
   Thread thread;
   Group urlGroup;
   List urlList;
   Text urlText;
   String URLtoRip;

   public LinkRipper(Shell var1, MainWindow var2) {
      super(var1);
      this.mainWindow = var2;
   }

   private void activateDropTarget(Text var1) {
      byte var2 = 23;
      DropTarget var3 = new DropTarget(var1, var2);
      UniformResourceLocator var4 = UniformResourceLocator.getInstance();
      TextTransfer var5 = TextTransfer.getInstance();
      var3.setTransfer(new Transfer[]{var4, var5});
      var3.addDropListener(new LinkRipper$1(this, var4, var1));
   }

   public void addMenuItem(Menu var1, String var2, String var3, SelectionAdapter var4) {
      MenuItem var5 = new MenuItem(var1, 8);
      var5.setText(SResources.getString(var2));
      var5.setImage(SResources.getImage(var3));
      var5.addSelectionListener(var4);
   }

   public void addToClipBoard(String var1) {
      MainWindow.copyToClipboard(var1);
   }

   public boolean close() {
      PreferenceStore var1 = PreferenceLoader.getPreferenceStore();
      PreferenceConverter.setValue(var1, "linkRipperWindowBounds", this.getShell().getBounds());
      this.mainWindow.closeLinkRipper();
      return super.close();
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(SResources.getImage("web-link"));
      var1.setText(VersionInfo.getName() + " " + SResources.getString("l.linkRipper"));
   }

   protected void constrainShellSize() {
      super.constrainShellSize();
      Shell var1 = this.getShell();
      if (PreferenceLoader.contains("linkRipperWindowBounds")) {
         var1.setBounds(PreferenceLoader.loadRectangle("linkRipperWindowBounds"));
      } else {
         var1.setSize(500, 300);
         Point var2 = var1.getLocation();
         this.getShell().setLocation(var2.x - 200, var2.y);
      }
   }

   protected Control createButtonBar(Composite var1) {
      Composite var2 = new Composite(var1, 0);
      var2.setLayout(WidgetFactory.createGridLayout(2, 5, 5, 5, 5, false));
      var2.setLayoutData(new GridData(768));
      Button var3 = new Button(var2, 0);
      var3.setLayoutData(new GridData(768));
      var3.setText(SResources.getString("b.downloadAll"));
      var3.addSelectionListener(new LinkRipper$2(this));
      Button var4 = new Button(var2, 0);
      var4.setLayoutData(new GridData(128));
      var4.setText(SResources.getString("b.close"));
      var4.addSelectionListener(new LinkRipper$3(this));
      return var2;
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      Composite var3 = new Composite(var2, 0);
      var3.setLayoutData(new GridData(768));
      var3.setLayout(WidgetFactory.createGridLayout(3, 0, 0, 5, 5, false));
      Label var4 = new Label(var3, 0);
      var4.setText(SResources.getString("rip.url"));
      var4.setLayoutData(new GridData(32));
      this.urlText = new Text(var3, 2052);
      if (PreferenceLoader.loadBoolean("dragAndDrop")) {
         this.activateDropTarget(this.urlText);
      }

      this.urlText.setLayoutData(new GridData(768));
      Button var5 = new Button(var3, 0);
      var5.setLayoutData(new GridData(128));
      var5.setText(SResources.getString("rip.rip"));
      this.urlGroup = new Group(var2, 0);
      this.urlGroup.setLayoutData(new GridData(1808));
      this.urlGroup.setLayout(WidgetFactory.createGridLayout(1, 5, 5, 5, 5, false));
      this.urlGroup.setText(SResources.getString("rip.waiting"));
      this.urlList = new List(this.urlGroup, 2818);
      this.createMenu();
      this.urlList.addListener(3, new LinkRipper$4(this));
      this.urlList.setLayoutData(new GridData(1808));
      this.urlList.addMouseListener(new LinkRipper$5(this));
      var5.addSelectionListener(new LinkRipper$6(this));
      this.urlText.addKeyListener(new LinkRipper$7(this));
      return var2;
   }

   private void createMenu() {
      this.popupMenu = new MenuManager("");
      this.popupMenu.setRemoveAllWhenShown(true);
      this.popupMenu.addMenuListener(this);
   }

   public void downloadAll() {
      for (int var1 = 0; var1 < this.urlList.getItems().length; var1++) {
         SwissArmy.sendLink(Sancho.getCore(), this.urlList.getItems()[var1]);
      }

      this.mainWindow.getStatusline().setText(SResources.getString("sl.linksSent") + this.urlList.getItemCount());
   }

   public void downloadSelected() {
      if (this.urlList.getSelectionCount() > 0) {
         String[] var1 = this.urlList.getSelection();

         for (int var2 = 0; var2 < var1.length; var2++) {
            SwissArmy.sendLink(Sancho.getCore(), var1[var2]);
         }

         this.mainWindow.getStatusline().setText(SResources.getString("sl.linksSent") + var1.length);
      }
   }

   protected String getRawPage(String var1) {
      URL var2;
      try {
         var2 = new URL(var1);
      } catch (MalformedURLException var6) {
         Sancho.pDebug("LinkRipper: " + var6);
         return null;
      }

      try {
         BufferedReader var3 = new BufferedReader(new InputStreamReader(var2.openStream()));
         StringBuffer var4 = new StringBuffer();

         String var5;
         while ((var5 = var3.readLine()) != null) {
            var4.append(var5);
         }

         var3.close();
         return var4.toString();
      } catch (IOException var7) {
         Sancho.pDebug("LinkRipper: " + var7);
         return null;
      }
   }

   protected int getShellStyle() {
      return 2160 | (SWT.getPlatform().equals("fox") ? 0 : 0);
   }

   public void menuAboutToShow(IMenuManager var1) {
      var1.add(new LinkRipper$DownloadSelectedAction(this));
      var1.add(new LinkRipper$DownloadAllAction(this));
      var1.add(new Separator());
      var1.add(new LinkRipper$CopyAction(this));
      var1.add(new LinkRipper$CopyAllAction(this));
      var1.add(new Separator());
      var1.add(new LinkRipper$ToggleShowAllAction());
   }

   public void ripLinks() {
      if (!this.ripping) {
         this.ripping = true;
         this.urlList.removeAll();
         this.URLtoRip = this.urlText.getText();
         if (this.URLtoRip.equals("")) {
            this.ripping = false;
         } else {
            this.urlGroup.setText(SResources.getString("rip.ripping"));
            this.thread = new Thread(this);
            this.thread.start();
         }
      }
   }

   public void run() {
      if (!this.URLtoRip.toLowerCase().startsWith("ftp")) {
         if (!this.URLtoRip.toLowerCase().startsWith("http")) {
            this.URLtoRip = "http://" + this.URLtoRip;
         }

         if (endSlashRE.getMatch(this.URLtoRip) == null) {
            this.URLtoRip = this.URLtoRip + "/";
         }

         String var11 = this.getRawPage(this.URLtoRip);
         if (snRE.getMatch(this.URLtoRip) != null) {
            REMatch var12 = frameRE.getMatch(var11);
            if (var12 != null) {
               String var14 = var11.substring(var12.getStartIndex(1), var12.getEndIndex(1));
               var14 = var14 + "list_news.html";
               var11 = this.getRawPage(var14);
            }
         }

         if (var11 == null) {
            if (this.urlGroup != null && !this.urlGroup.isDisposed()) {
               this.urlGroup.getDisplay().syncExec(new LinkRipper$10(this));
               this.ripping = false;
            }
         } else {
            String[] var13 = SwissArmy.parseLinks(var11);
            if (this.urlGroup != null && !this.urlGroup.isDisposed()) {
               this.urlGroup.getDisplay().syncExec(new LinkRipper$11(this, var13));
               this.ripping = false;
            }
         }
      } else {
         if (!this.URLtoRip.endsWith("/")) {
            this.URLtoRip = this.URLtoRip + "/";
         }

         try {
            InputStream var1 = new URL(this.URLtoRip).openStream();
            BufferedReader var2 = new BufferedReader(new InputStreamReader(var1));
            ArrayList var3 = new ArrayList();
            String var4 = "";

            while ((var4 = var2.readLine()) != null) {
               StringTokenizer var5 = new StringTokenizer(var4);
               int var6 = var5.countTokens();
               if (var6 > 8) {
                  StringBuffer var7 = new StringBuffer(this.URLtoRip);

                  for (int var8 = 0; var8 < 8; var8++) {
                     var5.nextToken();
                  }

                  for (int var9 = 8; var9 < var6; var9++) {
                     var7.append(var5.nextToken());
                     if (var9 != var6 - 1) {
                        var7.append(" ");
                     }
                  }

                  if (var4.startsWith("d")) {
                     var7.append("/");
                  }

                  var3.add(var7.toString());
               }
            }

            String[] var17 = new String[var3.size()];
            var3.toArray(var17);
            if (this.urlGroup == null || this.urlGroup.isDisposed()) {
               return;
            }

            this.urlGroup.getDisplay().syncExec(new LinkRipper$8(this, var17));
         } catch (Exception var10) {
            this.urlGroup.getDisplay().syncExec(new LinkRipper$9(this));
         }

         this.ripping = false;
      }
   }

   public void setCurrentLinks(String[] var1) {
      this.urlList.removeAll();
      if (var1 != null) {
         String[] var2 = SwissArmy.parseLinks(var1);
         this.urlGroup.setText(SResources.getString("rip.found") + "(" + var2.length + "):");

         for (int var3 = 0; var3 < var2.length; var3++) {
            this.urlList.add(var2[var3]);
         }
      }
   }

   public void setFocus() {
      this.getShell().setFocus();
   }

   public void setInputURL(String var1) {
      if (var1 != null) {
         this.urlText.setText(var1);
      }
   }

   static {
      try {
         snRE = new RE("http://.+?suprnova.org");
         frameRE = new RE("src='(.+?)'");
         endSlashRE = new RE("http://.+/");
      } catch (Exception var1) {
      }
   }
}
