package sancho.view.irc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.jibble.pircbot.User;
import sancho.utility.VersionInfo;
import sancho.view.console.IRCConsole;
import sancho.view.irc.channelUsers.ChannelUsersTableView;
import sancho.view.irc.channelUsers.ChannelUsersViewFrame;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class IRCShell implements IIRCListener {
   Shell shell;
   IRCClient ircClient;
   CTabFolder cTabFolder;
   String selectedChannel;
   public static final String statusChannel = "status";
   Map tabMap = Collections.synchronizedMap(new HashMap());

   public IRCShell() {
      this.shell = new Shell();
      this.shell.setLayout(new FillLayout());
      this.shell.setImage(VersionInfo.getProgramIcon());
      this.setTitle(SResources.getString("l.irc.connecting"));
      this.shell.setLayout(new FillLayout());
      this.shell.addDisposeListener(new IRCShell$1(this));
      this.shell.addListener(21, new IRCShell$2(this));
      this.ircClient = new IRCClient(this);
      this.cTabFolder = WidgetFactory.createCTabFolder(this.shell, 1088);
      this.cTabFolder.addCTabFolder2Listener(new IRCShell$3(this));
      this.cTabFolder.addSelectionListener(new IRCShell$4(this));
      this.createTab("status");
      this.shell.setBounds(PreferenceLoader.loadRectangle("ircWindowBounds"));
      this.shell.open();
   }

   public IRCConsole getActiveConsole() {
      return ((IRCShell$IRCTab)this.tabMap.get(this.selectedChannel)).getConsole();
   }

   public IRCConsole getConsole(String var1) {
      return ((IRCShell$IRCTab)this.tabMap.get(var1.toLowerCase())).getConsole();
   }

   public ChannelUsersTableView getView(String var1) {
      return ((IRCShell$IRCTab)this.tabMap.get(var1.toLowerCase())).getView();
   }

   public IRCConsoleViewFrame getConsoleFrame(String var1) {
      return ((IRCShell$IRCTab)this.tabMap.get(var1.toLowerCase())).getConsoleFrame();
   }

   public List getCUsers(String var1) {
      return ((IRCShell$IRCTab)this.tabMap.get(var1.toLowerCase())).getCUsers();
   }

   public void createTab(String var1) {
      this.cTabFolder.getDisplay().syncExec(new IRCShell$5(this, var1));
   }

   public void setTitle(String var1) {
      this.shell.getDisplay().syncExec(new IRCShell$6(this, var1));
   }

   public IRCConsoleViewFrame createConsole(SashForm var1, String var2) {
      IRCConsoleViewFrame var3 = new IRCConsoleViewFrame(var1, var2.equals("status") ? "Status window" : "No topic", "irc", null, var2);
      var3.getConsole().setIRCClient(this.ircClient);
      var3.getConsole().setFocus();
      var3.getConsole().setShell(this.shell);
      return var3;
   }

   public ChannelUsersTableView createUserTable(SashForm var1) {
      ChannelUsersViewFrame var2 = new ChannelUsersViewFrame(var1, SResources.getString("l.users"), "tab.friends.buttonSmall", null);
      ChannelUsersTableView var3 = (ChannelUsersTableView)var2.getGView();
      var3.setIRCClient(this.ircClient);
      return var3;
   }

   public void onConnect() {
      this.setConnectedTitle();
      int[] var1 = new int[]{0};
      String[] var2 = new String[]{"*** Connected: " + this.ircClient.getServer() + " as " + this.ircClient.getNick()};
      this.getConsole("status").appendRawNL(var1, var2);
      var2 = new String[]{"*** Connected: use \"/nick <nick>\" to change your nickname"};
      this.getConsole("status").appendRawNL(var1, var2);
   }

   public void setConnectedTitle() {
      this.setTitle(this.ircClient.getServer() + "(" + this.ircClient.getNick() + ")");
   }

   public void onDisconnect() {
      this.setTitle(SResources.getString("l.irc.disconnected"));
      int[] var1 = new int[]{0};
      String[] var2 = new String[]{"*** Disconnected"};
      this.getConsole("status").appendRawNL(var1, var2);
   }

   public void onMessage(String var1, String var2, String var3, String var4, String var5) {
      int[] var6 = new int[]{0, 1};
      String[] var7 = new String[]{"<", var2, "> " + var5};
      this.getConsole(var1).appendRawNL(var6, var7);
   }

   public void onPrivateMessage(String var1, String var2, String var3, String var4) {
      int[] var5 = new int[]{0, 4, 0};
      String[] var6 = new String[]{"*[", var1, "]* " + var4};
      this.getActiveConsole().appendRawNL(var5, var6);
   }

   public void onNotice(String var1, String var2, String var3, String var4, String var5) {
      int[] var6 = new int[]{0, 4, 0};
      String var7 = var4.toLowerCase().equals(this.ircClient.getNick().toLowerCase()) ? "" : "/" + var4;
      String[] var8 = new String[]{"-[", var1, var7 + "]- " + var5};
      if (this.tabMap.containsKey(var4.toLowerCase())) {
         this.getConsole(var4).appendRawNL(var6, var8);
      } else {
         this.getActiveConsole().appendRawNL(var6, var8);
      }
   }

   public void onAction(String var1, String var2, String var3, String var4, String var5) {
      int[] var6 = new int[]{0, 1, 0};
      String[] var7 = new String[]{"* ", var1, " " + var5};
      this.getConsole(var4).appendRawNL(var6, var7);
   }

   public synchronized String[] processQuit(String var1) {
      ArrayList var2 = new ArrayList();

      for (Object var4o : this.tabMap.keySet()) { String var4 = (String)var4o;
         IRCShell$IRCTab var5 = (IRCShell$IRCTab)this.tabMap.get(var4);
         List var6 = var5.getCUsers();
         CUser var7 = this.getCUser(var6, var1);
         if (var7 != null) {
            var6.remove(var7);
            var2.add(var4);
            var5.getView().remove(var7);
         }
      }

      String[] var8 = new String[var2.size()];
      var2.toArray(var8);
      return var8;
   }

   public void onQuit(String var1, String var2, String var3, String var4) {
      String[] var5 = this.processQuit(var1);
      int[] var6 = new int[]{4, 1, 4};
      String[] var7 = new String[]{"*** Quit: ", var1, " (" + var4 + ")"};

      for (int var8 = 0; var8 < var5.length; var8++) {
         this.getConsole(var5[var8]).appendRawNL(var6, var7);
      }
   }

   public void onKick(String var1, String var2, String var3, String var4, String var5, String var6) {
      int[] var7 = new int[]{4, 1, 4, 1};
      String[] var8 = new String[]{"*** Kick: ", var5, " by ", var2};
      this.getConsole(var1).appendRawNL(var7, var8);
      this.getView(var1).remove(this.removeCUser(var1, var5));
   }

   public void onPart(String var1, String var2, String var3, String var4) {
      int[] var5 = new int[]{4, 1, 4};
      String[] var6 = new String[]{"*** Part: ", var2, " (" + var3 + "@" + var4 + ")"};
      this.getConsole(var1).appendRawNL(var5, var6);
      this.getView(var1).remove(this.removeCUser(var1, var2));
   }

   public void onTopic(String var1, String var2, String var3, long var4, boolean var6) {
      int[] var7 = new int[]{5, 0, 5, 1};
      String[] var8 = new String[]{"*** Topic: ", var2, " set by ", var3};
      this.getConsole(var1).appendRawNL(var7, var8);
      this.getConsoleFrame(var1).updateCLabelTextInGuiThread(var2);
      this.getConsoleFrame(var1).updateCLabelToolTipInGuiThread(var2);
   }

   public void onWhoisUser(String var1, String var2, String var3, String var4, String var5, String var6) {
      this.getActiveConsole().appendNL("*** " + var2 + "(" + var3 + "@" + var4 + ")" + var6);
   }

   public void onJoin(String var1, String var2, String var3, String var4) {
      if (this.tabMap.get(var1.toLowerCase()) == null) {
         this.createTab(var1.toLowerCase());
      }

      int[] var5 = new int[]{3, 1, 3};
      String[] var6 = new String[]{"*** Join: ", var2, " (" + var3 + "@" + var4 + ")"};
      this.getConsole(var1).appendRawNL(var5, var6);
      List var7 = this.getCUsers(var1);
      if (var7.size() > 0) {
         CUser var8 = new CUser("", var2);
         this.addCUser(var1, var8);
         this.getView(var1).add(var8);
      }
   }

   public synchronized void addCUser(String var1, CUser var2) {
      this.getCUsers(var1).add(var2);
   }

   public synchronized Object removeCUser(String var1, String var2) {
      CUser var3 = null;
      List var4 = this.getCUsers(var1);

      for (int var5 = 0; var5 < var4.size(); var5++) {
         if (((CUser)var4.get(var5)).equals(var2)) {
            var3 = (CUser)var4.get(var5);
            break;
         }
      }

      return var4.remove(var3) ? var3 : null;
   }

   public synchronized String[] processNickChange(String var1, String var2) {
      ArrayList var3 = new ArrayList();

      for (Object var5o : this.tabMap.keySet()) { String var5 = (String)var5o;
         IRCShell$IRCTab var6 = (IRCShell$IRCTab)this.tabMap.get(var5);
         List var7 = var6.getCUsers();
         CUser var8 = this.getCUser(var7, var1);
         if (var8 != null) {
            var8.nickChange(var2);
            var3.add(var5);
            var6.getView().update(var8);
         }
      }

      String[] var9 = new String[var3.size()];
      var3.toArray(var9);
      return var9;
   }

   public void onNickChange(String var1, String var2, String var3, String var4) {
      String[] var5 = this.processNickChange(var1, var4);
      int[] var6 = new int[]{5, 1, 5, 1};

      for (int var7 = 0; var7 < var5.length; var7++) {
         this.getConsole(var5[var7]).appendRawNL(var6, new String[]{"*** NickChange: ", var1, " is now known as ", var4});
      }

      if (var4.equals(this.ircClient.getNick())) {
         this.setConnectedTitle();
      }
   }

   public void onOp(String var1, String var2, String var3, String var4, String var5) {
      this.modeChange(var1, "+o", var5, var2);
      CUser var6 = this.getCUser(var1, var5);
      if (var6 != null) {
         var6.op();
         this.getView(var1).update(var6);
      }
   }

   public void modeChange(String var1, String var2, String var3, String var4) {
      int[] var5 = new int[]{5, 1, 5, 1};
      String[] var6 = new String[]{"*** Mode " + var2 + " ", var3, " by ", var4};
      this.getConsole(var1).appendRawNL(var5, var6);
   }

   public void onDeop(String var1, String var2, String var3, String var4, String var5) {
      this.modeChange(var1, "-o", var5, var2);
      CUser var6 = this.getCUser(var1, var5);
      if (var6 != null) {
         var6.deOp();
         this.getView(var1).update(var6);
      }
   }

   public void onVoice(String var1, String var2, String var3, String var4, String var5) {
      this.modeChange(var1, "+v", var5, var2);
      CUser var6 = this.getCUser(var1, var5);
      if (var6 != null) {
         var6.voice();
         this.getView(var1).update(var6);
      }
   }

   public void onDeVoice(String var1, String var2, String var3, String var4, String var5) {
      this.modeChange(var1, "-v", var5, var2);
      CUser var6 = this.getCUser(var1, var5);
      if (var6 != null) {
         var6.deVoice();
         this.getView(var1).update(var6);
      }
   }

   protected synchronized CUser getCUser(String var1, String var2) {
      return this.getCUser(((IRCShell$IRCTab)this.tabMap.get(var1)).getCUsers(), var2);
   }

   protected synchronized CUser getCUser(List var1, String var2) {
      for (int var3 = 0; var3 < var1.size(); var3++) {
         if (((CUser)var1.get(var3)).equals(var2)) {
            return (CUser)var1.get(var3);
         }
      }

      return null;
   }

   public void onUserList(String var1, User[] var2) {
      List var3 = this.getCUsers(var1);
      var3.clear();

      for (int var4 = 0; var4 < var2.length; var4++) {
         var3.add(new CUser(var2[var4]));
      }

      this.getView(var1).setInput(var3);
   }

   public void close() {
      this.ircClient.close();
   }
}
