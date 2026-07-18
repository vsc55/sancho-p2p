package sancho.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import sancho.model.mldonkey.ClientCollection;
import sancho.model.mldonkey.ClientStats;
import sancho.model.mldonkey.CollectionFactory;
import sancho.model.mldonkey.ConsoleMessage;
import sancho.model.mldonkey.DefineSearchesCollection;
import sancho.model.mldonkey.FileCollection;
import sancho.model.mldonkey.NetworkCollection;
import sancho.model.mldonkey.OptionCollection;
import sancho.model.mldonkey.ResultCollection;
import sancho.model.mldonkey.RoomCollection;
import sancho.model.mldonkey.ServerCollection;
import sancho.model.mldonkey.SharedFileCollection;
import sancho.model.mldonkey.UserCollection;
import sancho.model.mldonkey.utility.ClientMessage;
import sancho.model.mldonkey.utility.MessageBuffer;
import sancho.model.mldonkey.utility.MessageEncoder;
import sancho.model.mldonkey.utility.UtilityFactory;
import sancho.utility.MyObservable;
import sancho.utility.SwissArmy;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.AbstractTab;

public class MLDonkeyCore extends MyObservable implements ICore {
   protected static final int MAX_PROTOCOL = 41;
   protected int activeProtocol;
   protected AbstractTab activeTab;
   protected CollectionFactory collectionFactory;
   protected boolean connected;
   protected boolean connectionDenied;
   protected int coreProtocol;
   protected boolean initialized;
   protected boolean invalidPassword = true;
   protected int max_from_gui;
   protected int max_to_gui;
   protected MessageEncoder messageEncoder;
   protected MessageBuffer messageBuffer;
   protected String password;
   protected boolean pollMode;
   protected boolean pollPending;
   protected boolean pollUploaders;
   protected boolean pollUpStats;
   protected int pollDelay;
   protected int statsDelay;
   protected Socket socket;
   protected Timer timer;
   protected String username;
   protected boolean semaphore;
   protected long lastPollForStats;
   protected long lastStats;
   protected int requestFileInfoDelay;
   protected long lastRequestFileInfos;
   protected int timerCounter;
   protected String mldonkeyVersion = "";
   protected int opCode;
   // $VF: synthetic field
   static Class class$sancho$view$SharesTab;
   // $VF: synthetic field
   static Class class$sancho$view$TransferTab;

   public MLDonkeyCore(Socket var1, String var2, String var3, boolean var4) {
      this.socket = var1;
      this.username = var2;
      this.password = var3;
      this.pollMode = var4;
      this.semaphore = false;
      this.messageEncoder = new MessageEncoder(var1);
      this.updatePreferences();
   }

   public void checkIfDenied() {
      if (!this.initialized) {
         this.connected = false;
         this.connectionDenied = true;
         this.semaphore = true;
      }
   }

   public synchronized void connect() {
      this.connected = true;
   }

   public synchronized void disconnect() {
      this.connected = false;
   }

   protected void enablePollMode() {
      Object[] var1 = new Object[]{Short.valueOf((short)1), Integer.valueOf(1), Byte.valueOf((byte)1)};
      this.send((short)47, var1);
   }

   public synchronized ClientCollection getClientCollection() {
      return this.getCollectionFactory().getClientCollection();
   }

   public synchronized ClientStats getClientStats() {
      return this.getCollectionFactory().getClientStats();
   }

   public synchronized CollectionFactory getCollectionFactory() {
      return this.collectionFactory;
   }

   public synchronized ConsoleMessage getConsoleMessage() {
      return this.getCollectionFactory().getConsoleMessage();
   }

   public synchronized DefineSearchesCollection getDefineSearchesCollection() {
      return this.getCollectionFactory().getDefineSearchesCollection();
   }

   public synchronized FileCollection getFileCollection() {
      return this.getCollectionFactory().getFileCollection();
   }

   public synchronized NetworkCollection getNetworkCollection() {
      return this.getCollectionFactory().getNetworkCollection();
   }

   public synchronized OptionCollection getOptionCollection() {
      return this.getCollectionFactory().getOptionCollection();
   }

   public int getProtocol() {
      return this.activeProtocol;
   }

   public synchronized ResultCollection getResultCollection() {
      return this.getCollectionFactory().getResultCollection();
   }

   public synchronized RoomCollection getRoomCollection() {
      return this.getCollectionFactory().getRoomCollection();
   }

   public synchronized ServerCollection getServerCollection() {
      return this.getCollectionFactory().getServerCollection();
   }

   public synchronized SharedFileCollection getSharedFileCollection() {
      return this.getCollectionFactory().getSharedFileCollection();
   }

   public synchronized UserCollection getUserCollection() {
      return this.getCollectionFactory().getUserCollection();
   }

   public String getCoreVersion() {
      return this.mldonkeyVersion != null ? this.mldonkeyVersion : "";
   }

   public synchronized boolean isConnected() {
      return this.connected;
   }

   public boolean initialized() {
      return this.initialized;
   }

   public boolean semaphore() {
      return this.semaphore;
   }

   public boolean isConnectionDenied() {
      return this.connectionDenied;
   }

   public boolean isInvalidPassword() {
      return this.invalidPassword;
   }

   public void notifyInitialized() {
      if (!this.initialized) {
         this.invalidPassword = false;
         this.semaphore = true;
         this.initialized = true;
      }
   }

   public void notifyObject(Object var1) {
      this.setChanged();
      this.notifyObservers(var1);
   }

   private void onIOException(IOException var1) {
      this.disconnect();
      this.setChanged();
      this.notifyObservers(var1);
   }

   private void pollStats() {
      this.getNetworkCollection().getAllStats();
   }

   private void pollForStats() {
      if (this.pollUpStats
         && (class$sancho$view$SharesTab == null ? (class$sancho$view$SharesTab = class$("sancho.view.SharesTab")) : class$sancho$view$SharesTab)
            .isInstance(this.activeTab)) {
         this.send((short)49);
      }

      if ((class$sancho$view$TransferTab == null ? (class$sancho$view$TransferTab = class$("sancho.view.TransferTab")) : class$sancho$view$TransferTab)
         .isInstance(this.activeTab)) {
         if (this.getProtocol() >= 23) {
            if (this.pollUploaders) {
               this.send((short)57);
            }

            if (this.pollPending) {
               this.send((short)58);
            }
         } else if (this.pollUploaders && Sancho.hasCollectionFactory()) {
            this.getClientCollection().updateUploaders(this);
         }
      }
   }

   protected void readCoreProtocol(MessageBuffer var1) {
      this.coreProtocol = var1.getInt32();
      this.activeProtocol = Math.min(this.coreProtocol, 41);
      if (this.activeProtocol > 29) {
         this.max_to_gui = var1.getInt32();
         this.max_from_gui = var1.getInt32();
      }

      if (this.pollMode) {
         this.enablePollMode();
      }

      this.sendPassword();
      this.sendInterestedInSources();
      this.sendGetVersion();
      this.collectionFactory = CollectionFactory.getFactory(this.activeProtocol, this);
      this.startTimer();
   }

   void processMessage(int var1, MessageBuffer var2) throws Exception {
      switch (var1) {
         case 0:
            this.readCoreProtocol(var2);
            break;
         case 1:
            if (!this.initialized) {
               this.notifyInitialized();
            }

            this.getOptionCollection().read(var2);
         case 2:
         case 7:
         case 8:
         case 11:
         case 14:
         case 17:
         case 22:
         case 25:
         case 28:
         case 29:
         case 30:
         case 33:
         case 37:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 54:
         case 57:
         default:
            break;
         case 3:
            this.getDefineSearchesCollection().read(var2);
            break;
         case 4:
            this.getResultCollection().resultInfo(var2);
            break;
         case 5:
            this.getResultCollection().read(var2);
            break;
         case 6:
            this.getResultCollection().searchWaiting(var2);
            break;
         case 9:
            this.getClientCollection().updateAvailability(var2);
            break;
         case 10:
            this.getFileCollection().addSource(var2);
            break;
         case 12:
            this.getServerCollection().serverUser(var2);
            break;
         case 13:
            this.getServerCollection().readUpdate(var2);
            break;
         case 15:
            this.getClientCollection().read(var2);
            break;
         case 16:
            this.getClientCollection().readUpdate(var2);
            break;
         case 18:
            this.getClientCollection().clientFile(var2);
            break;
         case 19:
            this.getConsoleMessage().read(var2);
            break;
         case 20:
            this.getNetworkCollection().read(var2);
            break;
         case 21:
            this.getUserCollection().read(var2);
            break;
         case 23:
            this.getRoomCollection().roomMessage(var2);
            break;
         case 24:
            this.getRoomCollection().addUser(var2);
            break;
         case 26:
            this.getServerCollection().read(var2);
            break;
         case 27:
            ClientMessage var3 = UtilityFactory.getClientMessage(this);
            var3.read(var2);
            this.setChanged();
            this.notifyObservers(var3);
            break;
         case 31:
            this.getRoomCollection().read(var2);
            break;
         case 32:
            this.getRoomCollection().removeUser(var2);
            break;
         case 34:
            this.getSharedFileCollection().upload(var2);
            break;
         case 35:
            this.getSharedFileCollection().unshared(var2);
            break;
         case 36:
            this.getOptionCollection().addSectionOption(var2);
            break;
         case 38:
            this.getOptionCollection().addPluginOption(var2);
            break;
         case 46:
            this.getFileCollection().update(var2);
            break;
         case 47:
            this.disconnect();
            this.semaphore = true;
            break;
         case 48:
            this.getSharedFileCollection().read(var2);
            break;
         case 49:
            if (!this.initialized) {
               this.notifyInitialized();
            }

            this.getClientStats().read(var2);
            break;
         case 50:
            this.getFileCollection().removeSource(var2);
            break;
         case 51:
            this.getClientCollection().clean(var2);
            this.getServerCollection().clean(var2);
            this.getFileCollection().clean();
            break;
         case 52:
            this.getFileCollection().add(var2);
            break;
         case 53:
            this.getFileCollection().read(var2);
            break;
         case 55:
            this.getClientCollection().uploaders(var2);
            break;
         case 56:
            this.getClientCollection().pending(var2);
            break;
         case 58:
            this.mldonkeyVersion = var2.getString();
            break;
         case 59:
            this.getNetworkCollection().readStats(var2);
      }
   }

   public String getLastMessage() {
      return this.messageBuffer == null
         ? "NMB"
         : this.opCode + "/" + this.coreProtocol + "/" + this.messageBuffer.getLastLength() + ": \n" + this.messageBuffer.getLastMessage();
   }

   public void run() throws RuntimeException {
      this.opCode = -1;

      try {
         this.messageBuffer = new MessageBuffer(this, new BufferedInputStream(this.socket.getInputStream()));
         this.sendProtocolVersion();

         while (this.connected) {
            this.opCode = this.messageBuffer.readMessage();
            this.processMessage(this.opCode, this.messageBuffer);
         }
      } catch (SocketException var6) {
         var6.printStackTrace();
         this.checkIfDenied();
      } catch (IOException var7) {
         this.checkIfDenied();
         this.onIOException(var7);
      } catch (Exception var8) {
         String var4 = "";
         int var5 = -1;
         if (this.messageBuffer != null) {
            var4 = this.messageBuffer.getLastMessage();
            var5 = this.messageBuffer.getLastLength();
         }

         Sancho.threadException(
            "Core o(" + this.opCode + ") l(" + var5 + ") p(" + this.coreProtocol + ") m(" + this.getCoreVersion() + ") t(" + this.activeTab + ")\n\n" + var4,
            var8
         );
         this.onIOException(new IOException());
      }

      this.stopTimer();
      if (this.collectionFactory != null) {
         CollectionFactory.dispose();
      }
   }

   public void send(short var1) {
      this.send(var1, null);
   }

   public void send(short var1, Object var2) {
      this.send(var1, new Object[]{var2});
   }

   public void send(short var1, Object[] var2) {
      try {
         this.messageEncoder.send(var1, var2);
      } catch (IOException var4) {
         this.onIOException(var4);
      }
   }

   protected void sendInterestedInSources() {
      this.sendInterestedInSources(PreferenceLoader.loadBoolean("mldonkey.InterestedInSources"));
   }

   protected void sendInterestedInSources(boolean var1) {
      if (this.getProtocol() >= 27) {
         this.send((short)64, Byte.valueOf((byte)(var1 ? 1 : 0)));
      }
   }

   protected void sendPassword() {
      this.send((short)52, new String[]{this.password, this.username});
   }

   protected void sendGetVersion() {
      if (this.getProtocol() > 29) {
         this.send((short)65);
      }
   }

   protected void sendProtocolVersion() {
      this.send((short)0, Integer.valueOf(41));
   }

   public void setActiveTab(AbstractTab var1) {
      this.activeTab = var1;
      this.pollForStats();
   }

   private void startTimer() {
      this.lastRequestFileInfos = System.currentTimeMillis();
      if (this.timer != null) {
         this.timer.cancel();
      }

      this.timer = new Timer();
      this.timer.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            if (!MLDonkeyCore.this.isConnected()) {
               this.cancel();
            } else {
               long now = System.currentTimeMillis();
               if (now > MLDonkeyCore.this.lastPollForStats + (long)(MLDonkeyCore.this.pollDelay * 1000)) {
                  MLDonkeyCore.this.pollForStats();
                  MLDonkeyCore.this.lastPollForStats = now;
               }

               if (now > MLDonkeyCore.this.lastStats + (long)(MLDonkeyCore.this.statsDelay * 1000)) {
                  MLDonkeyCore.this.pollStats();
                  MLDonkeyCore.this.lastStats = now;
               }

               if (MLDonkeyCore.this.requestFileInfoDelay > 0
                  && now > MLDonkeyCore.this.lastRequestFileInfos + (long)(MLDonkeyCore.this.requestFileInfoDelay * 1000)) {
                  MLDonkeyCore.this.getFileCollection().requestAllFileInfos();
                  MLDonkeyCore.this.lastRequestFileInfos = now;
               }

               if (MLDonkeyCore.this.timerCounter++ == 5) {
                  MLDonkeyCore.this.getClientCollection().cleanDeadClients();
               }

               MLDonkeyCore.this.getFileCollection().sendUpdate();
            }
         }
      }, 0L, 777L);
   }

   private void stopTimer() {
      if (this.timer != null) {
         this.timer.cancel();
      }
   }

   public void updatePreferences() {
      SwissArmy.updatePreferences();
      this.pollUpStats = PreferenceLoader.loadBoolean("pollUpStats");
      this.pollUploaders = PreferenceLoader.loadBoolean("pollUploaders");
      this.pollPending = PreferenceLoader.loadBoolean("pollPending");
      this.pollDelay = PreferenceLoader.loadInt("pollDelay");
      this.requestFileInfoDelay = PreferenceLoader.loadInt("requestFileInfoDelay");
      this.statsDelay = PreferenceLoader.loadInt("statsDelay");
      this.sendInterestedInSources();
      if (this.getCollectionFactory() != null) {
         this.getServerCollection().updatePreferences();
         this.getFileCollection().updatePreferences();
         this.getResultCollection().updatePreferences();
      }
   }

   // $VF: synthetic method
   static Class class$(String var0) {
      try {
         return Class.forName(var0);
      } catch (ClassNotFoundException var2) {
         throw new NoClassDefFoundError(var2.getMessage());
      }
   }

}
