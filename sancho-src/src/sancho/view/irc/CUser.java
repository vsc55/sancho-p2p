package sancho.view.irc;

import org.jibble.pircbot.User;

public class CUser {
   private String prefix;
   private String nick;
   private String lowerNick;

   public CUser(String var1, String var2) {
      this.prefix = var1;
      this.nick = var2;
      this.lowerNick = var2.toLowerCase();
   }

   public CUser(User var1) {
      this.prefix = var1.getPrefix();
      this.nick = var1.getNick();
      this.lowerNick = this.nick.toLowerCase();
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void nickChange(String var1) {
      this.nick = var1;
      this.lowerNick = var1.toLowerCase();
   }

   public boolean isOp() {
      return this.prefix.indexOf(64) >= 0;
   }

   public boolean hasVoice() {
      return this.prefix.indexOf(43) >= 0;
   }

   public void setPrefix(String var1) {
      this.prefix = var1;
   }

   public String getNick() {
      return this.nick;
   }

   public String toString() {
      return this.getPrefix() + this.getNick();
   }

   public void deOp() {
      if (this.prefix.indexOf(43) >= 0) {
         this.prefix = "+";
      } else {
         this.prefix = "";
      }
   }

   public void op() {
      if (this.prefix.indexOf(64) == -1) {
         this.prefix = "@" + this.prefix;
      }
   }

   public void voice() {
      // second test was a duplicated indexOf('@') (64); it must check '+' (43) so a
      // user isn't voiced twice ("++nick").
      if (this.prefix.indexOf(64) == -1 && this.prefix.indexOf(43) == -1) {
         this.prefix = "+" + this.prefix;
      }
   }

   public void deVoice() {
      // a voiced (non-op) user's prefix is exactly "+", so indexOf('+') is 0; the
      // old ">= 1" never matched and -v never cleared the voice. Use ">= 0".
      if (this.prefix.indexOf(43) >= 0 && this.prefix.indexOf(64) == -1) {
         this.prefix = "";
      }
   }

   public boolean equals(String var1) {
      return var1.toLowerCase().equals(this.lowerNick);
   }

   public int hashCode() {
      return this.lowerNick.hashCode();
   }
}
