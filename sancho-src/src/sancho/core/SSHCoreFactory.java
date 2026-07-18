package sancho.core;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.eclipse.swt.widgets.Display;
import sancho.utility.SwissArmy;
import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.utility.SResources;
import sancho.view.utility.Splash;

public class SSHCoreFactory extends CoreFactory {
   private JSch jsch;
   private Session session;
   private String ssh_host;
   private int ssh_lport = -1;
   private String ssh_pass;
   private int ssh_port = -1;
   private String ssh_rhost;
   private int ssh_rport = -1;
   private String ssh_user;
   private String ssh_socks4_proxy;
   private String ssh_socks5_proxy;
   private String ssh_http_proxy;
   private String ssh_proxy_user;
   private String ssh_proxy_pass;
   private boolean use_ssh;
   private boolean ssh_fwd_p;
   private int ssh_prport = -1;
   private int ssh_plport = -1;

   public SSHCoreFactory(Display var1) {
      super(var1);
   }

   public String getConnectedString() {
      String var1 = "";
      if (this.session != null) {
         var1 = "| " + this.session.getServerVersion();
      }

      return super.getConnectedString() + " " + var1;
   }

   public void disconnectSSH() {
      if (this.jsch != null) {
         if (this.session != null) {
            try {
               this.session.delPortForwardingL(this.ssh_lport);
            } catch (JSchException var2) {
               System.err.println("delPortForwarding: " + var2);
            }

            this.session.disconnect();
            this.session = null;
         }

         this.jsch = null;
      }
   }

   public int initializeSSH() {
      if (!this.use_ssh) {
         return 0;
      } else if (SwissArmy.portInUse(this.ssh_lport)) {
         return 0;
      } else {
         try {
            this.jsch = new JSch();
            String var1 = System.getProperty("file.separator");
            String var4 = VersionInfo.getUserHomeDirectory() + var1 + ".ssh" + var1;
            this.jsch.setKnownHosts(var4 + "known_hosts");
            this.addIdentity(this.jsch, new File(var4 + "id_dsa"), this.ssh_pass);
            this.addIdentity(this.jsch, new File(var4 + "id_rsa"), this.ssh_pass);
            this.session = this.jsch.getSession(this.ssh_user, this.ssh_host, this.ssh_port);
            this.addProxy(this.session);
            this.session.setPassword(this.ssh_pass);
            this.session.setUserInfo(new SSHUserInfo());
            this.session.connect();
            this.session.setPortForwardingL(this.ssh_lport, this.ssh_rhost, this.ssh_rport);
            if (this.ssh_fwd_p) {
               this.session.setPortForwardingL(this.ssh_plport, this.ssh_rhost, this.ssh_prport);
            }

            return 0;
         } catch (Exception var3) {
            StringWriter var2 = new StringWriter();
            var3.printStackTrace(new PrintWriter(var2, true));
            return this.autoReconnecting
               ? 2
               : this.errorHandling(
                  SResources.getString("core.sshFailedTitle"), SResources.getString("core.sshFailedText") + "\n" + var3 + "\n---\n" + var2.toString()
               );
         }
      }
   }

   private void addProxy(Session var1) {
      if (this.ssh_http_proxy != null && !this.ssh_http_proxy.equals("") && this.ssh_http_proxy.indexOf(":") > 0) {
         String var9 = this.ssh_http_proxy.substring(0, this.ssh_http_proxy.indexOf(58));
         int var7 = Integer.parseInt(this.ssh_http_proxy.substring(this.ssh_http_proxy.indexOf(58) + 1));
         ProxyHTTP var11 = new ProxyHTTP(var9, var7);
         if (this.ssh_proxy_user != null && !this.ssh_proxy_user.equals("")) {
            String var13 = "";
            if (this.ssh_proxy_pass != null) {
               var13 = this.ssh_proxy_pass;
            }

            var11.setUserPasswd(this.ssh_proxy_user, var13);
         }

         var1.setProxy(var11);
      } else if (this.ssh_socks5_proxy != null && !this.ssh_socks5_proxy.equals("") && this.ssh_socks5_proxy.indexOf(":") > 0) {
         String var8 = this.ssh_socks5_proxy.substring(0, this.ssh_socks5_proxy.indexOf(58));
         int var6 = Integer.parseInt(this.ssh_socks5_proxy.substring(this.ssh_socks5_proxy.indexOf(58) + 1));
         ProxySOCKS5 var10 = new ProxySOCKS5(var8, var6);
         if (this.ssh_proxy_user != null && !this.ssh_proxy_user.equals("")) {
            String var12 = "";
            if (this.ssh_proxy_pass != null) {
               var12 = this.ssh_proxy_pass;
            }

            var10.setUserPasswd(this.ssh_proxy_user, var12);
         }

         var1.setProxy(var10);
      } else if (this.ssh_socks4_proxy != null && !this.ssh_socks4_proxy.equals("") && this.ssh_socks4_proxy.indexOf(":") > 0) {
         String var3 = this.ssh_socks4_proxy.substring(0, this.ssh_socks4_proxy.indexOf(58));
         int var2 = Integer.parseInt(this.ssh_socks4_proxy.substring(this.ssh_socks4_proxy.indexOf(58) + 1));
         ProxySOCKS4 var4 = new ProxySOCKS4(var3, var2);
         if (this.ssh_proxy_user != null && !this.ssh_proxy_user.equals("")) {
            String var5 = "";
            if (this.ssh_proxy_pass != null) {
               var5 = this.ssh_proxy_pass;
            }

            var4.setUserPasswd(this.ssh_proxy_user, var5);
         }

         var1.setProxy(var4);
      }
   }

   private void addIdentity(JSch var1, File var2, String var3) throws JSchException {
      if (var2.exists()) {
         if (var3 != null && !var3.equals("")) {
            var1.addIdentity(var2.getAbsolutePath(), var3);
         } else {
            var1.addIdentity(var2.getAbsolutePath());
         }
      }
   }

   public String getHTTPPort() {
      return this.ssh_fwd_p ? String.valueOf(this.ssh_plport) : super.getHTTPPort();
   }

   public void readPreferences(int var1, boolean var2) {
      super.readPreferences(var1, var2);
      this.use_ssh = this.use_ssh && !var2 ? this.use_ssh : PreferenceLoader.loadBoolean("hm_" + var1 + "_use_ssh");
      this.ssh_user = this.ssh_user != null && !var2 ? this.ssh_user : PreferenceLoader.loadString("hm_" + var1 + "_ssh_user");
      this.ssh_host = this.ssh_host != null && !var2 ? this.ssh_host : PreferenceLoader.loadString("hm_" + var1 + "_ssh_host");
      this.ssh_pass = this.ssh_pass != null && !var2 ? this.ssh_pass : PreferenceLoader.loadString("hm_" + var1 + "_ssh_pass");
      this.ssh_port = this.ssh_port != -1 && !var2 ? this.ssh_port : PreferenceLoader.loadInt("hm_" + var1 + "_ssh_port");
      this.ssh_rhost = this.ssh_rhost != null && !var2 ? this.ssh_rhost : PreferenceLoader.loadString("hm_" + var1 + "_ssh_rhost");
      this.ssh_rport = this.ssh_rport != -1 && !var2 ? this.ssh_rport : PreferenceLoader.loadInt("hm_" + var1 + "_ssh_rport");
      this.ssh_lport = this.ssh_lport != -1 && !var2 ? this.ssh_lport : PreferenceLoader.loadInt("hm_" + var1 + "_ssh_lport");
      this.ssh_fwd_p = this.ssh_fwd_p && !var2 ? this.ssh_fwd_p : PreferenceLoader.loadBoolean("hm_" + var1 + "_ssh_fwd_p");
      this.ssh_prport = this.ssh_prport != -1 && !var2 ? this.ssh_prport : PreferenceLoader.loadInt("hm_" + var1 + "_ssh_prport");
      this.ssh_plport = this.ssh_plport != -1 && !var2 ? this.ssh_plport : PreferenceLoader.loadInt("hm_" + var1 + "_ssh_plport");
      this.ssh_socks5_proxy = this.ssh_socks5_proxy != null && !var2 ? this.ssh_socks5_proxy : PreferenceLoader.loadString("hm_" + var1 + "_ssh_socks5_proxy");
      this.ssh_socks4_proxy = this.ssh_socks4_proxy != null && !var2 ? this.ssh_socks4_proxy : PreferenceLoader.loadString("hm_" + var1 + "_ssh_socks4_proxy");
      this.ssh_http_proxy = this.ssh_http_proxy != null && !var2 ? this.ssh_http_proxy : PreferenceLoader.loadString("hm_" + var1 + "_ssh_http_proxy");
      this.ssh_proxy_user = this.ssh_proxy_user != null && !var2 ? this.ssh_proxy_user : PreferenceLoader.loadString("hm_" + var1 + "_ssh_proxy_user");
      this.ssh_proxy_pass = this.ssh_proxy_pass != null && !var2 ? this.ssh_proxy_pass : PreferenceLoader.loadString("hm_" + var1 + "_ssh_proxy_pass");
   }

   public void setDisconnected() {
      this.disconnectSSH();
      super.setDisconnected();
   }

   public int startCore() {
      int var1 = this.initializeSSH();
      return var1 != 2 && var1 != 1 ? super.startCore() : var1;
   }

   // JSch callback: routes SSH password / passphrase / yes-no / message prompts to the
   // CoreFactory dialogs (was SSHCoreFactory$SSHUserInfo, with its showMessage Runnable).
   private class SSHUserInfo implements UserInfo {
      String passwd;

      public String getPassphrase() {
         return this.passwd;
      }

      public String getPassword() {
         return this.passwd;
      }

      public boolean promptPassphrase(String message) {
         String entered = SSHCoreFactory.this.askForPassword("/SSH", message);
         SSHCoreFactory.this.sResult = null;
         if (entered == null) {
            return false;
         } else {
            this.passwd = entered;
            return true;
         }
      }

      public boolean promptPassword(String message) {
         String entered = SSHCoreFactory.this.askForPassword("/SSH", message);
         SSHCoreFactory.this.sResult = null;
         if (entered == null) {
            return false;
         } else {
            this.passwd = entered;
            return true;
         }
      }

      public boolean promptYesNo(String message) {
         return SSHCoreFactory.this.createYesNoBox(VersionInfo.getName() + "/SSH", message);
      }

      public void showMessage(final String message) {
         SSHCoreFactory.this.display.syncExec(new Runnable() {
            public void run() {
               Splash.setVisible(false);
               CoreFactory.openInformation(null, VersionInfo.getName() + "/SSH", message);
               Splash.setVisible(true);
            }
         });
      }
   }
}
