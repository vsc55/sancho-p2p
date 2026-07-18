package sancho.view.irc;

import java.text.Normalizer;
import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;

public class IRCClient extends PircBot {
   protected String server;
   protected String channel;
   protected String nickname;
   protected int nickCounter;
   protected boolean connected;
   protected IIRCListener ircListener;

   // Reduce a nickname to characters an IRC server accepts (RFC 2812): decompose
   // accents first (ñ->n, á->a), then keep only letters / digits / -[]\`_^{|} with a
   // letter-or-special first char. Falls back to a random nick if nothing is left.
   private static String sanitizeNick(String var0) {
      if (var0 == null) {
         var0 = "";
      }

      var0 = Normalizer.normalize(var0, Normalizer.Form.NFD);
      StringBuilder var1 = new StringBuilder(var0.length());

      for (int var2 = 0; var2 < var0.length(); var2++) {
         char var3 = var0.charAt(var2);
         boolean var4 = var3 >= 'A' && var3 <= 'Z' || var3 >= 'a' && var3 <= 'z' || "[]\\`_^{|}".indexOf(var3) >= 0;
         boolean var5 = var4 || var3 >= '0' && var3 <= '9' || var3 == '-';
         if (var1.length() == 0 ? var4 : var5) {
            var1.append(var3);
         }
      }

      return var1.length() == 0 ? SwissArmy.getRandomString(7) : var1.toString();
   }

   public IRCClient(IIRCListener var1) {
      this.ircListener = var1;
      this.server = PreferenceLoader.loadString("ircServer");
      this.channel = PreferenceLoader.loadString("ircChannel");
      this.nickname = PreferenceLoader.loadString("ircNickname");
      if (this.nickname.equals(VersionInfo.getName())) {
         this.nickname = SwissArmy.getRandomString(7);
      }

      // IRC servers only accept ASCII nicknames; a configured nick with accents/ñ
      // is rejected with "432 Erroneous Nickname". Strip it to valid characters.
      this.nickname = sanitizeNick(this.nickname);
      this.setName(this.nickname);
      this.setLogin(this.nickname);
      this.setVersion(VersionInfo.getName() + " " + VersionInfo.getVersion() + VersionInfo.getBrand() + ": " + VersionInfo.getShortHomePage());
      this.setFinger(VersionInfo.getName() + " " + VersionInfo.getVersion() + VersionInfo.getBrand() + ": " + VersionInfo.getShortHomePage());
      IRCClient$1 var2 = new IRCClient$1(this);
      var2.setDaemon(true);
      var2.start();
   }

   protected void onMessage(String var1, String var2, String var3, String var4, String var5) {
      super.onMessage(var1, var2, var3, var4, var5);
      this.ircListener.onMessage(var1, var2, var3, var4, Colors.removeFormattingAndColors(var5));
   }

   protected void onPrivateMessage(String var1, String var2, String var3, String var4) {
      super.onPrivateMessage(var1, var2, var3, var4);
      this.ircListener.onPrivateMessage(var1, var2, var3, var4);
   }

   protected void onNotice(String var1, String var2, String var3, String var4, String var5) {
      super.onNotice(var1, var2, var3, var4, var5);
      this.ircListener.onNotice(var1, var2, var3, var4, Colors.removeFormattingAndColors(var5));
   }

   protected void onJoin(String var1, String var2, String var3, String var4) {
      super.onJoin(var1, var2, var3, var4);
      this.ircListener.onJoin(var1, var2, var3, var4);
   }

   protected void onConnect() {
      super.onConnect();
      this.ircListener.onConnect();
   }

   protected void onDisconnect() {
      super.onDisconnect();
      this.ircListener.onDisconnect();
   }

   protected void onPart(String var1, String var2, String var3, String var4) {
      super.onPart(var1, var2, var3, var4);
      this.ircListener.onPart(var1, var2, var3, var4);
   }

   protected void onOp(String var1, String var2, String var3, String var4, String var5) {
      super.onOp(var1, var2, var3, var4, var5);
      this.ircListener.onOp(var1, var2, var3, var4, var5);
   }

   protected void onQuit(String var1, String var2, String var3, String var4) {
      super.onQuit(var1, var2, var3, var4);
      this.ircListener.onQuit(var1, var2, var3, Colors.removeFormattingAndColors(var4));
   }

   protected void onAction(String var1, String var2, String var3, String var4, String var5) {
      super.onAction(var1, var2, var3, var4, var5);
      this.ircListener.onAction(var1, var2, var3, var4, var5);
   }

   protected void onDeop(String var1, String var2, String var3, String var4, String var5) {
      super.onDeop(var1, var2, var3, var4, var5);
      this.ircListener.onDeop(var1, var2, var3, var4, var5);
   }

   protected void onVoice(String var1, String var2, String var3, String var4, String var5) {
      super.onVoice(var1, var2, var3, var4, var5);
      this.ircListener.onVoice(var1, var2, var3, var4, var5);
   }

   protected void onDeVoice(String var1, String var2, String var3, String var4, String var5) {
      super.onDeVoice(var1, var2, var3, var4, var5);
      this.ircListener.onDeVoice(var1, var2, var3, var4, var5);
   }

   protected void onUserList(String var1, User[] var2) {
      super.onUserList(var1, var2);
      this.ircListener.onUserList(var1, var2);
   }

   protected void onTopic(String var1, String var2, String var3, long var4, boolean var6) {
      super.onTopic(var1, var2, var3, var4, var6);
      this.ircListener.onTopic(var1, Colors.removeFormattingAndColors(var2), var3, var4, var6);
   }

   protected void onKick(String var1, String var2, String var3, String var4, String var5, String var6) {
      super.onKick(var1, var2, var3, var4, var5, var6);
      this.ircListener.onKick(var1, var2, var3, var4, var5, var6);
   }

   protected void onNickChange(String var1, String var2, String var3, String var4) {
      super.onNickChange(var1, var2, var3, var4);
      this.ircListener.onNickChange(var1, var2, var3, var4);
   }

   protected void onServerResponse(int var1, String var2) {
      switch (var1) {
         case 311:
            String[] var3 = SwissArmy.split(var2, ' ');
            if (var3.length >= 6) {
               this.ircListener.onWhoisUser(var3[0], var3[1], var3[2], var3[3], var3[4], var3[5]);
            }
      }
   }

   public void whois(String var1) {
      this.sendRawLine("WHOIS " + var1);
   }

   public void cDisconnect() {
      super.quitServer(VersionInfo.getName() + " " + VersionInfo.getVersion() + VersionInfo.getBrand() + ": " + VersionInfo.getShortHomePage());
   }

   public void close() {
      try {
         super.quitServer(VersionInfo.getName() + " " + VersionInfo.getVersion() + ": " + VersionInfo.getShortHomePage());
      } catch (Exception var3) {
      }

      try {
         super.dispose();
      } catch (Exception var2) {
      }
   }

   // $VF: synthetic method
   static void access$000(IRCClient var0, String var1) {
      var0.setName(var1);
   }
}
