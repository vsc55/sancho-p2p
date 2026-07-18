package sancho.core;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import sancho.model.mldonkey.Option;
import sancho.utility.MyObservable;
import sancho.utility.MyObserver;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;

public class CoreFactory extends MyObservable implements MyObserver, Runnable {
   public static final int CLOSE = 1;
   public static final int OK = 0;
   public static final int RETRY = 2;
   private boolean automated;
   protected boolean autoReconnecting;
   private long connectedAt;
   protected String sResult;
   private boolean brc;
   private int irc;
   private int connectRC;
   private ICore core;
   protected Display display;
   private boolean hasConnected;
   private String hostname;
   private int numRetries;
   private String password;
   private boolean ask_pass;
   protected int port;
   private Socket socket;
   private String username;
   private boolean wantToConnect;
   private String description;

   public CoreFactory(Display var1) {
      this.display = var1;
      this.connectRC = -1;
      this.automated = false;
   }

   public int connect() {
      int var1;
      while ((var1 = this.startCore()) == 2) {
         this.numRetries++;
         this.updateSplash("splash.connecting");
         int var2 = PreferenceLoader.loadInt("autoReconnectDelay");

         while (this.autoReconnecting && var2 > 0) {
            SwissArmy.threadSleep(1000);
            this.notifyObject("[" + var2-- + "] " + SResources.getString("l.waitingToReconnect"));
         }

         if (!this.autoReconnecting) {
            boolean var3 = false;
            this.setChanged();
            this.notifyObservers("");
         }
      }

      return var1;
   }

   protected boolean createYesNoBox(String var1, String var2) {
      this.brc = false;
      this.display.syncExec(new CoreFactory$1(this, var1, var2));
      return this.brc;
   }

   private boolean createResYesNoBox(String var1, String var2) {
      return this.createYesNoBox(SResources.getString(var1), SResources.getString(var2));
   }

   public void disconnect() {
      if (this.core != null) {
         this.core.disconnect();
         if (this.core instanceof MLDonkeyCore) {
            ((MLDonkeyCore)this.core).deleteObservers();
         }

         this.setDisconnected();
      }
   }

   protected int errorHandling(String var1, String var2) {
      if (!this.automated && !this.createYesNoBox(var1, var2)) {
         return 1;
      } else {
         if (Sancho.getCoreConsole() != null) {
            this.display.syncExec(new CoreFactory$2(this));
         }

         Sancho.killCoreConsole();
         return !this.setupWizard() ? 1 : 2;
      }
   }

   protected synchronized int getConnectRC() {
      return this.connectRC;
   }

   public String getDescription() {
      if (this.core == null) {
         return "";
      } else if (this.description != null && !this.description.equals("")) {
         return this.description;
      } else {
         return this.hostname != null ? this.hostname : "";
      }
   }

   public ICore getCore() {
      return this.core;
   }

   public String getHostname() {
      return this.hostname;
   }

   public String getHTTPPort() {
      String var1 = "";
      Option var2 = (Option)this.core.getOptionCollection().get("http_port");
      if (var2 != null) {
         var1 = var2.getValue();
      }

      return var1;
   }

   public int getNumRetries() {
      return this.numRetries;
   }

   public String getPassword() {
      return this.password;
   }

   public Socket getSocket() {
      return this.socket;
   }

   public String getUptime() {
      return SwissArmy.calcUptime(this.connectedAt);
   }

   public String getUsername() {
      return this.username;
   }

   public void initialize() {
      this.readPreferences(0, false);
      Thread var1 = new Thread(this);
      var1.setDaemon(true);
      var1.start();
   }

   public int initializeSocket() {
      this.updateSplash("splash.initializeSocket", "", 0);

      try {
         if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
         }

         this.socket = new Socket(this.hostname, this.port);
         return 0;
      } catch (UnknownHostException var4) {
         return this.autoReconnecting
            ? 2
            : this.errorHandling(SResources.getString("core.invalidAddressTitle"), SResources.getString("core.invalidAddressText"));
      } catch (IOException var5) {
         if (Sancho.getCoreConsole() != null) {
            this.display.syncExec(new CoreFactory$3(this));
         }

         return this.autoReconnecting
            ? 2
            : this.errorHandling(SResources.getString("core.notFoundTitle"), this.hostname + ":" + this.port + " " + SResources.getString("core.notFoundText"));
      }
   }

   public int interactiveConnect() {
      if (!this.checkIfInitialized()) {
         return 1;
      } else if (PreferenceLoader.loadBoolean("hostManagerOnStart") && !this.setupWizard()) {
         return 1;
      } else {
         this.automated = false;
         this.wantToConnect = true;
         return this.successfulConnect();
      }
   }

   public boolean isAutoReconnecting() {
      return this.autoReconnecting;
   }

   public boolean isConnected() {
      return this.core != null ? this.core.isConnected() : false;
   }

   public void notifyObject(Object var1) {
      this.setChanged();
      this.notifyObservers(var1);
   }

   private int onConnectionDenied() {
      if (this.createResYesNoBox("core.connectionDeniedTitle", "core.connectionDeniedText")) {
         return !this.setupWizard() ? 1 : 2;
      } else {
         return 1;
      }
   }

   private int onInvalidPassword() {
      if (this.createResYesNoBox("core.invalidLoginTitle", "core.invalidLoginText")) {
         return !this.setupWizard() ? 1 : 2;
      } else {
         return 1;
      }
   }

   public void readPreferences(int var1) {
      this.readPreferences(var1, true);
   }

   public boolean checkIfInitialized() {
      if (!PreferenceLoader.loadBoolean("initialized")) {
         if (!this.setupWizard()) {
            return false;
         }

         PreferenceLoader.getPreferenceStore().setValue("initialized", true);
         PreferenceLoader.saveStore();
      }

      return true;
   }

   public void readPreferences(int var1, boolean var2) {
      if (var2 && !Sancho.automated && PreferenceLoader.loadBoolean("useLastFile")) {
         SwissArmy.writeLastFile(var1);
      }

      if (!PreferenceLoader.contains("hm_" + var1 + "_hostname")) {
         var1 = 0;
      }

      this.port = this.port != 0 && !var2 ? this.port : PreferenceLoader.loadInt("hm_" + var1 + "_port");
      this.hostname = this.hostname != null && !var2 ? this.hostname : PreferenceLoader.loadString("hm_" + var1 + "_hostname");
      this.username = this.username != null && !var2 ? this.username : PreferenceLoader.loadString("hm_" + var1 + "_username");
      this.password = this.password != null && !var2 ? this.password : PreferenceLoader.loadString("hm_" + var1 + "_password");
      this.description = this.description != null && !var2 ? this.description : PreferenceLoader.loadString("hm_" + var1 + "_description");
      this.ask_pass = this.ask_pass && !var2 ? this.ask_pass : PreferenceLoader.loadBoolean("hm_" + var1 + "_ask_pass");
   }

   public synchronized void reconnect() {
      this.wantToConnect = true;
   }

   public void reconnect(int var1) {
      this.readPreferences(var1);
      this.reconnect();
   }

   public void reconnectO() {
      this.connect();
   }

   public void run() {
      while (true) {
         if (this.core == null && this.wantToConnect) {
            this.connectRC = this.connect();
            if (this.connectRC == 1) {
               this.wantToConnect = false;
            }
         }

         SwissArmy.threadSleep(1000);
      }
   }

   public synchronized void setAutomated(boolean var1) {
      this.automated = var1;
   }

   public synchronized void setAutoReconnect() {
      this.wantToConnect = true;
      this.autoReconnecting = true;
   }

   public void setAutoReconnecting(boolean var1) {
      this.autoReconnecting = var1;
   }

   public void setDisconnected() {
      synchronized (this) {
         this.wantToConnect = false;
         this.core = null;
      }

      this.setChanged();
      this.notifyObservers(Boolean.FALSE);
      this.setChanged();
      this.notifyObservers("");
   }

   public void setHostPort(String var1, int var2) {
      this.hostname = var1;
      this.port = var2;
   }

   public void setPassword(String var1) {
      this.password = var1;
   }

   private boolean setupWizard() {
      this.brc = false;
      this.irc = 0;
      this.display.syncExec(new CoreFactory$4(this));
      this.readPreferences(this.irc);
      return this.brc;
   }

   public void setUsername(String var1) {
      this.username = var1;
   }

   public synchronized void setWantToConnect(boolean var1) {
      this.wantToConnect = var1;
   }

   public String getStatusString() {
      return SResources.getString("e.state.connected") + this.getConnectedString() + this.getCoreVersion();
   }

   public int startCore() {
      int var1 = this.initializeSocket();
      if (var1 != 2 && var1 != 1) {
         if (this.core != null) {
            this.core.deleteObservers();
         }

         if (this.ask_pass) {
            String var2 = this.askForPassword("/CORE", SResources.getString("hm.password"));
            this.sResult = null;
            if (var2 != null) {
               this.password = var2;
            }
         }

         this.core = (ICore)(Sancho.monitorMode
            ? new MLDonkeyCoreMonitor(this.socket, this.username, this.password, this.automated)
            : new MLDonkeyCore(this.socket, this.username, this.password, this.automated));
         this.core.addObserver(this);
         this.core.connect();
         Thread var5 = new Thread(this.core);
         var5.setDaemon(true);
         var5.start();
         int var3 = PreferenceLoader.loadInt("connectionTimeout");
         int var4 = var3 * 4;

         while (var4-- > 0 && this.core != null && !this.core.semaphore()) {
            SwissArmy.threadSleep(250);
         }

         if (this.core != null && !this.core.isConnectionDenied()) {
            if (this.core.isInvalidPassword()) {
               this.core.deleteObservers();
               this.core = null;
               return this.autoReconnecting ? 2 : this.onInvalidPassword();
            } else {
               this.connectedAt = System.currentTimeMillis();
               this.autoReconnecting = false;
               this.hasConnected = true;
               this.setChanged();
               this.notifyObservers(this.getStatusString());
               this.setChanged();
               this.notifyObservers(Boolean.TRUE);
               return 0;
            }
         } else {
            if (this.core != null) {
               this.core.deleteObservers();
            }

            this.core = null;
            return this.autoReconnecting ? 2 : this.onConnectionDenied();
         }
      } else {
         return var1;
      }
   }

   public String getCoreVersion() {
      if (this.core != null) {
         String var1 = this.core.getCoreVersion();
         if (var1.equals("")) {
            return var1;
         } else {
            var1 = var1 + " | " + this.core.getProtocol();
            return " | " + var1;
         }
      } else {
         return "";
      }
   }

   public String getConnectedString() {
      return "";
   }

   public int successfulConnect() {
      while (true) {
         int var1 = this.getConnectRC();
         if (!this.display.readAndDispatch()) {
            SwissArmy.threadSleep(100);
            if (var1 == 0 || var1 == 1) {
               return var1;
            }
         }
      }
   }

   public void update(MyObservable var1, Object var2, int var3) {
      if (var2 instanceof IOException && var1 == this.core) {
         this.setDisconnected();
         if (this.hasConnected && PreferenceLoader.loadBoolean("autoReconnect")) {
            this.setAutoReconnect();
         }
      }
   }

   public void updateSplash(String var1) {
      this.display.syncExec(new CoreFactory$5(this, var1));
   }

   public void updateSplash(String var1, String var2, int var3) {
      this.display.syncExec(new CoreFactory$6(this, var1, var2, var3));
   }

   public String askForPassword(String var1, String var2) {
      this.display.syncExec(new CoreFactory$7(this, var1, var2));
      return this.sResult;
   }

   public static boolean openQuestion(Shell var0, String var1, String var2) {
      String[] var3 = new String[]{SResources.getString("b.yes"), SResources.getString("b.no")};
      CoreFactory$9 var4 = new CoreFactory$9(var0, var1, VersionInfo.getProgramIcon(), var2, 3, var3, 0);
      return var4.open() == 0;
   }

   public static void openInformation(Shell var0, String var1, String var2) {
      MessageDialog var3 = new MessageDialog(var0, var1, VersionInfo.getProgramIcon(), var2, 2, new String[]{SResources.getString("b.ok")}, 0);
      var3.open();
   }

   // $VF: synthetic method
   static boolean access$002(CoreFactory var0, boolean var1) {
      return var0.brc = var1;
   }

   // $VF: synthetic method
   static int access$102(CoreFactory var0, int var1) {
      return var0.irc = var1;
   }
}
