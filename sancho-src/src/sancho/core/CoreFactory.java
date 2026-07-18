package sancho.core;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import sancho.model.mldonkey.Option;
import sancho.utility.MyObservable;
import sancho.utility.MyObserver;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.Splash;
import sancho.view.utility.SResources;
import sancho.view.utility.setupWizard.SetupWizard;
import sancho.view.utility.setupWizard.SetupWizardDialog;

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

   public CoreFactory(Display display) {
      this.display = display;
      this.connectRC = -1;
      this.automated = false;
   }

   public int connect() {
      int result;
      while ((result = this.startCore()) == 2) {
         this.numRetries++;
         this.updateSplash("splash.connecting");
         int delay = PreferenceLoader.loadInt("autoReconnectDelay");

         while (this.autoReconnecting && delay > 0) {
            SwissArmy.threadSleep(1000);
            this.notifyObject("[" + delay-- + "] " + SResources.getString("l.waitingToReconnect"));
         }

         if (!this.autoReconnecting) {
            this.setChanged();
            this.notifyObservers("");
         }
      }

      return result;
   }

   protected boolean createYesNoBox(final String title, final String message) {
      this.brc = false;
      this.display.syncExec(new Runnable() {
         public void run() {
            Splash.setVisible(false);
            CoreFactory.this.brc = CoreFactory.openQuestion(null, title, message);
         }
      });
      return this.brc;
   }

   private boolean createResYesNoBox(String titleKey, String messageKey) {
      return this.createYesNoBox(SResources.getString(titleKey), SResources.getString(messageKey));
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

   protected int errorHandling(String title, String message) {
      if (!this.automated && !this.createYesNoBox(title, message)) {
         return 1;
      } else {
         if (Sancho.getCoreConsole() != null) {
            this.display.syncExec(new Runnable() {
               public void run() {
                  Sancho.getCoreConsole().getShell().close();
               }
            });
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
      String value = "";
      Option httpPort = (Option)this.core.getOptionCollection().get("http_port");
      if (httpPort != null) {
         value = httpPort.getValue();
      }

      return value;
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
      Thread thread = new Thread(this);
      thread.setDaemon(true);
      thread.start();
   }

   public int initializeSocket() {
      this.updateSplash("splash.initializeSocket", "", 0);

      try {
         if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
         }

         this.socket = new Socket(this.hostname, this.port);
         return 0;
      } catch (UnknownHostException unknownHost) {
         return this.autoReconnecting
            ? 2
            : this.errorHandling(SResources.getString("core.invalidAddressTitle"), SResources.getString("core.invalidAddressText"));
      } catch (IOException ioError) {
         if (Sancho.getCoreConsole() != null) {
            this.display.syncExec(new Runnable() {
               public void run() {
                  Sancho.getCoreConsole().getShell().open();
               }
            });
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

   public void notifyObject(Object message) {
      this.setChanged();
      this.notifyObservers(message);
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

   public void readPreferences(int hostIndex) {
      this.readPreferences(hostIndex, true);
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

   public void readPreferences(int hostIndex, boolean reload) {
      if (reload && !Sancho.automated && PreferenceLoader.loadBoolean("useLastFile")) {
         SwissArmy.writeLastFile(hostIndex);
      }

      if (!PreferenceLoader.contains("hm_" + hostIndex + "_hostname")) {
         hostIndex = 0;
      }

      this.port = this.port != 0 && !reload ? this.port : PreferenceLoader.loadInt("hm_" + hostIndex + "_port");
      this.hostname = this.hostname != null && !reload ? this.hostname : PreferenceLoader.loadString("hm_" + hostIndex + "_hostname");
      this.username = this.username != null && !reload ? this.username : PreferenceLoader.loadString("hm_" + hostIndex + "_username");
      this.password = this.password != null && !reload ? this.password : PreferenceLoader.loadString("hm_" + hostIndex + "_password");
      this.description = this.description != null && !reload ? this.description : PreferenceLoader.loadString("hm_" + hostIndex + "_description");
      this.ask_pass = this.ask_pass && !reload ? this.ask_pass : PreferenceLoader.loadBoolean("hm_" + hostIndex + "_ask_pass");
   }

   public synchronized void reconnect() {
      this.wantToConnect = true;
   }

   public void reconnect(int hostIndex) {
      this.readPreferences(hostIndex);
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

   public synchronized void setAutomated(boolean automated) {
      this.automated = automated;
   }

   public synchronized void setAutoReconnect() {
      this.wantToConnect = true;
      this.autoReconnecting = true;
   }

   public void setAutoReconnecting(boolean autoReconnecting) {
      this.autoReconnecting = autoReconnecting;
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

   public void setHostPort(String hostname, int port) {
      this.hostname = hostname;
      this.port = port;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   private boolean setupWizard() {
      this.brc = false;
      this.irc = 0;
      this.display.syncExec(new Runnable() {
         public void run() {
            Splash.setVisible(false);
            SetupWizardDialog dialog = new SetupWizardDialog(null, new SetupWizard());
            dialog.create();
            CoreFactory.this.brc = 0 == dialog.open();
            CoreFactory.this.irc = dialog.getNum();
            Splash.setVisible(true);
         }
      });
      this.readPreferences(this.irc);
      return this.brc;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public synchronized void setWantToConnect(boolean wantToConnect) {
      this.wantToConnect = wantToConnect;
   }

   public String getStatusString() {
      return SResources.getString("e.state.connected") + this.getConnectedString() + this.getCoreVersion();
   }

   public int startCore() {
      int socketResult = this.initializeSocket();
      if (socketResult != 2 && socketResult != 1) {
         if (this.core != null) {
            this.core.deleteObservers();
         }

         if (this.ask_pass) {
            String entered = this.askForPassword("/CORE", SResources.getString("hm.password"));
            this.sResult = null;
            if (entered != null) {
               this.password = entered;
            }
         }

         this.core = (ICore)(Sancho.monitorMode
            ? new MLDonkeyCoreMonitor(this.socket, this.username, this.password, this.automated)
            : new MLDonkeyCore(this.socket, this.username, this.password, this.automated));
         this.core.addObserver(this);
         this.core.connect();
         Thread coreThread = new Thread(this.core);
         coreThread.setDaemon(true);
         coreThread.start();
         int timeout = PreferenceLoader.loadInt("connectionTimeout");
         int attempts = timeout * 4;

         while (attempts-- > 0 && this.core != null && !this.core.semaphore()) {
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
         return socketResult;
      }
   }

   public String getCoreVersion() {
      if (this.core != null) {
         String version = this.core.getCoreVersion();
         if (version.equals("")) {
            return version;
         } else {
            version = version + " | " + this.core.getProtocol();
            return " | " + version;
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
         int rc = this.getConnectRC();
         if (!this.display.readAndDispatch()) {
            SwissArmy.threadSleep(100);
            if (rc == 0 || rc == 1) {
               return rc;
            }
         }
      }
   }

   public void update(MyObservable observable, Object arg, int flag) {
      if (arg instanceof IOException && observable == this.core) {
         this.setDisconnected();
         if (this.hasConnected && PreferenceLoader.loadBoolean("autoReconnect")) {
            this.setAutoReconnect();
         }
      }
   }

   public void updateSplash(final String text) {
      this.display.syncExec(new Runnable() {
         public void run() {
            Splash.updateText(text);
         }
      });
   }

   public void updateSplash(final String text, final String percent, final int index) {
      this.display.syncExec(new Runnable() {
         public void run() {
            Splash.updateText(text, percent, index);
         }
      });
   }

   public String askForPassword(final String title, final String message) {
      this.display.syncExec(new Runnable() {
         public void run() {
            Splash.setVisible(false);
            InputDialog dialog = new InputDialog(null, VersionInfo.getName() + title, message, "", (IInputValidator)null) {
               protected Control createDialogArea(Composite parent) {
                  Control area = super.createDialogArea(parent);
                  this.getText().setEchoChar('*');
                  return area;
               }

               protected void configureShell(Shell shell) {
                  super.configureShell(shell);
                  shell.setImage(VersionInfo.getProgramIcon());
               }
            };
            if (dialog.open() == 0) {
               CoreFactory.this.sResult = dialog.getValue();
            } else {
               CoreFactory.this.sResult = null;
            }

            Splash.setVisible(true);
         }
      });
      return this.sResult;
   }

   public static boolean openQuestion(Shell parent, String title, String message) {
      String[] buttons = new String[]{SResources.getString("b.yes"), SResources.getString("b.no")};
      MessageDialog dialog = new MessageDialog(parent, title, VersionInfo.getProgramIcon(), message, 3, buttons, 0) {
         protected void setShellStyle(int style) {
            super.setShellStyle(style & -65537);
         }
      };
      return dialog.open() == 0;
   }

   public static void openInformation(Shell parent, String title, String message) {
      MessageDialog dialog = new MessageDialog(parent, title, VersionInfo.getProgramIcon(), message, 2, new String[]{SResources.getString("b.ok")}, 0);
      dialog.open();
   }
}
