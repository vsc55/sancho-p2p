package sancho.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import sancho.core.Sancho;
import sancho.model.mldonkey.Client;
import sancho.model.mldonkey.utility.ClientMessage;
import sancho.utility.MyObservable;
import sancho.utility.MyObserver;
import sancho.utility.SwissArmy;
import sancho.view.console.MessageConsole;
import sancho.view.friends.FriendsTableView;
import sancho.view.friends.FriendsViewFrame;
import sancho.view.friends.clientDirectories.ClientDirectoriesTableView;
import sancho.view.friends.clientDirectories.ClientDirectoriesViewFrame;
import sancho.view.friends.clientFiles.ClientFilesTableView;
import sancho.view.friends.clientFiles.ClientFilesViewFrame;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.AbstractTab;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class FriendsTab extends AbstractTab implements MyObserver {
   private CTabFolder cTabFolder;
   private Hashtable openTabs = new Hashtable();
   private FriendsTab$MessagesViewFrame messagesViewFrame;
   private FriendsViewFrame friendsViewFrame;

   public FriendsTab(MainWindow var1, String var2) {
      super(var1, var2);
   }

   protected void createContents(Composite var1) {
      String var2 = "messagesSash";
      SashForm var3 = WidgetFactory.createSashForm(var1, var2);
      this.createLeftSash(var3);
      this.createRightSash(var3);
      WidgetFactory.loadSashForm(var3, var2);
      this.onConnect();
   }

   public void onConnect() {
      super.onConnect();
      if (this.getCore() != null) {
         this.getCore().addObserver(this);
      }
   }

   public void dispose() {
      if (this.getCore() != null) {
         this.getCore().deleteObserver(this);
      }

      if (this.cTabFolder != null && !this.cTabFolder.isDisposed()) {
         CTabItem[] var1 = this.cTabFolder.getItems();

         for (int var2 = 0; var2 < var1.length; var2++) {
            var1[var2].dispose();
         }

         this.cTabFolder.dispose();
         this.cTabFolder = null;
      }

      super.dispose();
   }

   private void createLeftSash(SashForm var1) {
      this.friendsViewFrame = new FriendsViewFrame(var1, "l.friends", "tab.friends.buttonSmall", this);
      this.addViewFrame(this.friendsViewFrame);
      this.friendsViewFrame.getGView().getComposite().addMouseListener(new FriendsTab$1(this));
   }

   private void createFilesView(SashForm var1) {
      String var2 = "directoriesFilesSash";
      SashForm var3 = WidgetFactory.createSashForm(var1, var2);
      ClientDirectoriesViewFrame var4 = new ClientDirectoriesViewFrame(var3, "l.clientDirectories", "tab.friends.buttonSmall", this);
      ((FriendsTableView)this.friendsViewFrame.getGView()).setDirectoryView((ClientDirectoriesTableView)var4.getGView());
      ClientFilesViewFrame var5 = new ClientFilesViewFrame(var3, "l.clientFiles", "tab.friends.buttonSmall", this);
      ((ClientDirectoriesTableView)var4.getGView()).setFilesView((ClientFilesTableView)var5.getGView());
      WidgetFactory.loadSashForm(var3, var2);
      this.addViewFrame(var4);
      this.addViewFrame(var5);
   }

   private void createRightSash(SashForm var1) {
      String var2 = "filesMessagesSash";
      SashForm var3 = WidgetFactory.createSashForm(var1, var2);
      this.createFilesView(var3);
      this.createMessagesView(var3);
      WidgetFactory.loadSashForm(var3, var2);
   }

   private void createMessagesView(SashForm var1) {
      this.messagesViewFrame = new FriendsTab$MessagesViewFrame(this, var1, "l.messageTabs", "tab.friends.buttonSmall", this);
      this.addViewFrame(this.messagesViewFrame);
      int var2 = PreferenceLoader.loadBoolean("messagesCTabFolderTabsOnTop") ? 128 : 1024;
      this.cTabFolder = WidgetFactory.createCTabFolder(this.messagesViewFrame.getChildComposite(), 8388608 | var2);
      WidgetFactory.addCTabFolderMenu(this.cTabFolder, "messagesCTabFolder");
      this.cTabFolder.setBorderVisible(false);
      this.cTabFolder.addCTabFolder2Listener(new FriendsTab$2(this));
      this.cTabFolder.addSelectionListener(new FriendsTab$3(this));
   }

   public void closeAllTabs() {
      for (Object var2o : this.openTabs.keySet()) { Integer var2 = (Integer)var2o;
         CTabItem var3 = (CTabItem)this.openTabs.get(var2);
         if (var3 != null) {
            MessageConsole var4 = (MessageConsole)var3.getData("messageConsole");
            if (var4 != null) {
               var4.dispose();
            }

            var3.dispose();
         }
      }

      this.openTabs.clear();
      this.setTabsLabel();
   }

   public void update(MyObservable var1, Object var2, int var3) {
      if (var2 instanceof ClientMessage && !this.cTabFolder.isDisposed()) {
         this.cTabFolder.getDisplay().syncExec(new FriendsTab$4(this, var2));
      }
   }

   public void setTabsLabel() {
      String var1 = "";
      if (this.cTabFolder.getSelection() != null) {
         var1 = " -> " + this.cTabFolder.getSelection().getText();
      }

      this.messagesViewFrame.updateCLabelText(SResources.getString("l.messageTabs") + ": " + this.openTabs.size() + var1);
   }

   public void sendTabMessage(int var1, String var2) {
      CTabItem var3 = (CTabItem)this.openTabs.get(Integer.valueOf(var1));
      MessageConsole var4 = (MessageConsole)var3.getData("messageConsole");
      var4.append(var2 + var4.getLineDelimiter());
   }

   public void messageFromClient(ClientMessage var1) {
      if (this.cTabFolder != null && !this.cTabFolder.isDisposed() && Sancho.hasCollectionFactory()) {
         StatusLine var2 = this.getMainWindow().getStatusline();
         var2.setText(SResources.getString("l.newMessage"));
         var2.setImage(SResources.getImage("new-message"));
         if (this.openTabs.containsKey(Integer.valueOf(var1.getId()))) {
            Client var4 = (Client)this.getCore().getClientCollection().get(var1.getId());
            String var3;
            if (var4 == null) {
               var3 = this.getTimeStamp() + var1.getId() + ": <unknown>> " + var1.getText();
            } else {
               var3 = this.getTimeStamp() + var1.getId() + ": " + var4.getName() + "> " + var1.getText();
            }

            this.sendTabMessage(var1.getId(), var3);
         } else {
            Client var8 = null;

            for (int var9 = 0; var9 < 3 && var8 == null && Sancho.hasCollectionFactory(); var9++) {
               var8 = (Client)this.getCore().getClientCollection().get(var1.getId());
               if (var8 == null) {
                  SwissArmy.threadSleep(1000);
               }
            }

            String var5;
            if (var8 == null) {
               var5 = var1.getId() + ": <unknown>";
            } else {
               var5 = var8.getId() + ": " + var8.getName();
            }

            String var6 = this.getTimeStamp() + var5 + "> " + var1.getText();
            CTabItem var7 = this.addCTabItem(var1.getId(), "  " + var5);
            if (this.cTabFolder.getItemCount() == 1) {
               this.setItemFocus(var7);
            }

            this.sendTabMessage(var1.getId(), var6);
            this.setTabsLabel();
         }
      }
   }

   public CTabItem addCTabItem(int var1, String var2) {
      CTabItem var3 = new CTabItem(this.cTabFolder, 0);
      var3.setText(var2);
      MessageConsole var4 = new MessageConsole(this.cTabFolder, 64, var1);
      var3.setControl(var4.getComposite());
      var3.setData("id", Integer.valueOf(var1));
      var3.setData("messageConsole", var4);
      this.openTabs.put(Integer.valueOf(var1), var3);
      return var3;
   }

   public void openTab(Client var1) {
      if (!this.openTabs.containsKey(Integer.valueOf(var1.getId()))) {
         String var2 = "  " + var1.getId() + ": " + var1.getName();
         this.setItemFocus(this.addCTabItem(var1.getId(), var2));
      } else {
         this.cTabFolder.setSelection((CTabItem)this.openTabs.get(Integer.valueOf(var1.getId())));
      }

      this.setTabsLabel();
   }

   public String getTimeStamp() {
      SimpleDateFormat var1 = new SimpleDateFormat("[HH:mm:ss] ");
      return var1.format(new Date());
   }

   public void setItemFocus(CTabItem var1) {
      this.cTabFolder.setSelection(var1);
      MessageConsole var2 = (MessageConsole)var1.getData("messageConsole");
      var2.setFocus();
   }

   public void setActive() {
      super.setActive();
      if (this.getMainWindow().getStatusline() != null) {
         this.getMainWindow().getStatusline().clear();
      }

      if (this.cTabFolder.getSelection() != null) {
         this.setItemFocus(this.cTabFolder.getSelection());
      }
   }

   // $VF: synthetic method
   static Hashtable access$000(FriendsTab var0) {
      return var0.openTabs;
   }
}
