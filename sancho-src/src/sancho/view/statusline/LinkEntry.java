package sancho.view.statusline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import sancho.core.Sancho;
import sancho.utility.SwissArmy;
import sancho.view.StatusLine;
import sancho.view.preferences.PreferenceLoader;
import sancho.view.transfer.UniformResourceLocator;
import sancho.view.utility.MyViewForm;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class LinkEntry {
   private StatusLine statusLine;

   public LinkEntry(StatusLine var1, Composite var2) {
      this.statusLine = var1;
      this.createContents(var2);
   }

   public void createContents(Composite var1) {
      MyViewForm var2 = WidgetFactory.createViewForm(var1, false);
      var2.setLayoutData(new GridData(1808));
      CLabel var3 = WidgetFactory.createCLabel(var2, "sl.linkEntryHeader", "up_arrow_green");
      var3.setFont(PreferenceLoader.loadFont("headerFontData"));
      Text var4 = new Text(var2, 578);
      var4.setLayoutData(new FillLayout());
      var4.setFont(PreferenceLoader.loadFont("ircConsoleFontData"));
      var4.setForeground(PreferenceLoader.loadColor("ircConsoleInputForeground"));
      var4.setBackground(PreferenceLoader.loadColor("ircConsoleInputBackground"));
      var4.addKeyListener(new LinkEntry$1(this, var4));
      Composite var5 = new Composite(var2, 0);
      var5.setLayout(WidgetFactory.createGridLayout(1, 1, 1, 0, 0, false));
      ToolBar var6 = new ToolBar(var5, 8519680);
      var6.setBackground(var6.getDisplay().getSystemColor(22));
      ToolItem var7 = new ToolItem(var6, 8);
      var7.setToolTipText(SResources.getString("sl.addLocalTorrents"));
      var7.setImage(SResources.getImage("folder-12"));
      var7.setText("");
      var7.addSelectionListener(new LinkEntry$2(this));
      ToolItem var8 = new ToolItem(var6, 8);
      var8.setText(SResources.getString("sl.clear"));
      var8.setImage(SResources.getImage("clear-12"));
      var8.addSelectionListener(new LinkEntry$3(this, var4));
      ToolItem var9 = new ToolItem(var6, 8);
      var9.setText(SResources.getString("sl.send"));
      var9.setImage(SResources.getImage("up_arrow_green"));
      var9.addSelectionListener(new LinkEntry$4(this, var4));
      var2.setTopLeft(var3);
      var2.setContent(var4);
      var2.setTopRight(var5);
      var6.pack();
      if (PreferenceLoader.loadBoolean("dragAndDrop")) {
         this.activateDropTarget(var4);
      }
   }

   public void enterLinks(Text var1) {
      String var2 = var1.getText();
      Pattern var3 = null;

      try {
         String var4 = "(ed2k://\\|file\\|[^\\|]+\\|(\\d+)\\|([\\dabcdef]+)\\|)|(sfdl://\\|.+?\\|[^\\|]+\\|(\\d+)\\|([\\dabcdef]+)\\|)";
         var4 = var4 + "|(sig2dat:///?\\|File:[^\\|]+\\|Length:.+?\\|UUHash:\\=.+?\\=)|(\\\"magnet:\\?xt=.+?\\\")";
         var4 = var4 + "|(magnet:\\?xt=.+?\n)|(\"http://.+\\.torrent\\?[^>]+\")|(http://.+\\.torrent)";
         if (var1.getLineCount() == 1) {
            var4 = var4 + "|(magnet:\\?xt=.+)|(http://.+?\\.torrent.+)|(.+?\\.torrent.*)|(.+?\\.torrent)";
         }

         var3 = Pattern.compile(var4, Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException var7) {
         var7.printStackTrace();
      }

      Matcher var10 = var3.matcher(var2);
      int var5 = 0;

      while (var10.find()) {
         String var6 = SwissArmy.replaceAll(var10.group(), "\"", "");
         var6 = SwissArmy.replaceAll(var6, "\n", "");
         if (Sancho.hasCollectionFactory()) {
            SwissArmy.sendLink(Sancho.getCore(), var6);
         }

         var5++;
      }

      this.statusLine.setText(SResources.getString("sl.linksSent") + var5);
      var1.setText("");
   }

   private void activateDropTarget(Text var1) {
      byte var2 = 23;
      DropTarget var3 = new DropTarget(var1, var2);
      UniformResourceLocator var4 = UniformResourceLocator.getInstance();
      TextTransfer var5 = TextTransfer.getInstance();
      FileTransfer var6 = FileTransfer.getInstance();
      var3.setTransfer(new Transfer[]{var4, var5, var6});
      var3.addDropListener(new LinkEntry$5(this, var5, var4, var1, var6));
   }

   // $VF: synthetic method
   static StatusLine access$000(LinkEntry var0) {
      return var0.statusLine;
   }
}
