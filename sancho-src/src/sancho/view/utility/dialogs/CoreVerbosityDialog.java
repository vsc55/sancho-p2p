package sancho.view.utility.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import sancho.core.Sancho;
import sancho.model.mldonkey.Option;
import sancho.utility.VersionInfo;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class CoreVerbosityDialog extends Dialog {
   String[] rawKeywords = new String[]{
      "mc",
      "mr",
      "mct",
      "ms",
      "verb",
      "sm",
      "net",
      "do",
      "up",
      "unk",
      "ov",
      "loc",
      "share",
      "md4",
      "connect",
      "udp",
      "super",
      "swarming",
      "hc",
      "hs",
      "act",
      "bw",
      "tor",
      "file",
      "redir",
      "unexp",
      "hid"
   };
   String[] keywords = new String[this.rawKeywords.length];
   Button[] buttonArray = new Button[this.rawKeywords.length + 1];
   Hashtable desc = new Hashtable();

   public CoreVerbosityDialog(Shell var1) {
      super(var1);
      Arrays.sort((Object[])this.rawKeywords);
      int var2 = 0;
      int var3 = 0;
      int var4 = 0;

      for (int var5 = 0; var5 < this.rawKeywords.length; var5++) {
         this.keywords[var5] = this.rawKeywords[var2];
         var2 += 9;
         if (var4++ == 2) {
            var4 = 0;
            var2 = ++var3;
         }
      }

      this.desc.put("all", "all");
      this.desc.put("mc", "client messages");
      this.desc.put("mr", "raw messages");
      this.desc.put("mct", "emule client tags");
      this.desc.put("ms", "server messages");
      this.desc.put("verb", "other");
      this.desc.put("sm", "source management");
      this.desc.put("net", "net");
      this.desc.put("do", "download warnings");
      this.desc.put("up", "upload warnings");
      this.desc.put("unk", "unknown messages");
      this.desc.put("ov", "overnet");
      this.desc.put("loc", "source research");
      this.desc.put("share", "sharing");
      this.desc.put("md4", "md4 computation");
      this.desc.put("connect", "connections");
      this.desc.put("udp", "udp messages");
      this.desc.put("super", "supernodes");
      this.desc.put("swarming", "swarming");
      this.desc.put("hc", "http_client messages");
      this.desc.put("hs", "http_server messages");
      this.desc.put("act", "activity");
      this.desc.put("bw", "bandwidth");
      this.desc.put("tor", "torrent loading");
      this.desc.put("file", "file handling");
      this.desc.put("redir", "redirector");
      this.desc.put("unexp", "unexpected messages");
      this.desc.put("hid", "hidden errors");
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      String var2 = "";
      if (Sancho.hasCollectionFactory()) {
         String var3 = Sancho.getCore().getCoreVersion();
         if (var3.length() > 0) {
            var2 = " " + var3;
         }
      }

      var1.setText(SResources.getString("menu.tools.coreVerbosity") + var2);
      var1.setImage(VersionInfo.getProgramIcon());
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      var2.setLayout(WidgetFactory.createGridLayout(3, 5, 5, 10, 3, false));
      if (!Sancho.hasCollectionFactory()) {
         return var2;
      } else {
         Option var3 = (Option)Sancho.getCore().getCollectionFactory().getOptionCollection().get("verbosity");
         if (var3 == null) {
            return var2;
         } else {
            ArrayList var4 = new ArrayList();
            StringTokenizer var5 = new StringTokenizer(var3.getValue(), " ");

            while (var5.hasMoreTokens()) {
               String var6 = var5.nextToken().toLowerCase();
               if (var6.equals("raw")) {
                  var6 = "mr";
               }

               if (var6.equals("ultra")) {
                  var6 = "super";
               }

               var4.add(var6);
            }

            this.buttonArray[0] = new Button(var2, 32);
            this.buttonArray[0].setText("all: All");
            this.buttonArray[0].setData("keyword", "all");
            if (var4.contains("all")) {
               this.buttonArray[0].setSelection(true);
            }

            GridData var11 = new GridData(768);
            var11.horizontalSpan = 3;
            this.buttonArray[0].setLayoutData(var11);
            Label var7 = new Label(var2, 258);
            var11 = new GridData(768);
            var11.horizontalSpan = 3;
            var7.setLayoutData(var11);

            for (int var8 = 0; var8 < this.keywords.length; var8++) {
               Button var9 = new Button(var2, 32);
               var9.setLayoutData(new GridData(768));
               var9.setText(this.keywords[var8] + ": " + this.desc.get(this.keywords[var8]));
               var9.setData("keyword", this.keywords[var8]);
               if (var4.contains(this.keywords[var8])) {
                  var9.setSelection(true);
               }

               this.buttonArray[var8 + 1] = var9;
            }

            var7 = new Label(var2, 258);
            var11 = new GridData(768);
            var11.horizontalSpan = 3;
            var7.setLayoutData(var11);
            return var2;
         }
      }
   }

   protected void createButtonsForButtonBar(Composite var1) {
      Button var2 = this.createButton(var1, 666, SResources.getString("b.deselectAll"), false);
      var2.addSelectionListener(new CoreVerbosityDialog$1(this));
      // OK is the default button so Enter accepts instead of cancelling (which
      // discarded the verbosity changes) — same fix as IRCConnectDialog.
      this.createButton(var1, 0, SResources.getString("b.ok"), true);
      this.createButton(var1, 1, SResources.getString("b.cancel"), false);
   }

   protected void buttonPressed(int var1) {
      if (var1 == 0 && Sancho.hasCollectionFactory()) {
         Option var2 = (Option)Sancho.getCore().getCollectionFactory().getOptionCollection().get("verbosity");
         if (var2 != null) {
            StringBuffer var3 = new StringBuffer();

            for (int var4 = 0; var4 < this.buttonArray.length; var4++) {
               if (this.buttonArray[var4].getSelection()) {
                  if (var3.length() > 0) {
                     var3.append(" ");
                  }

                  var3.append(this.buttonArray[var4].getData("keyword"));
               }
            }

            var2.setValue(var3.toString());
         }
      }

      super.buttonPressed(var1);
   }
}
