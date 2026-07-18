package sancho.view.utility;

import sancho.utility.VersionInfo;
import sancho.view.preferences.PreferenceLoader;

class VersionChecker$2 implements Runnable {
   // $VF: synthetic field
   private final VersionChecker$1 this$1;

   VersionChecker$2(VersionChecker$1 var1) {
      this.this$1 = var1;
   }

   public void run() {
      VersionChecker var2 = VersionChecker$1.access$000(this.this$1);
      String var1 = var2.newVersion;
      // A dead/parked update host can answer with HTML (or an empty body -> null)
      // instead of a version string. Only act when it actually looks like a version,
      // so we don't show a bogus "latest version" text or a false new-version popup
      // (and don't NPE on a null line).
      boolean var3 = var1 != null && var1.matches("\\d+(\\.\\d+)*(-\\d+)?");
      if (var2.shell != null && !var2.shell.isDisposed()) {
         if (var2.statusLine != null && var3) {
            var2.statusLine
               .setText(
                  "["
                     + VersionInfo.getName()
                     + "] "
                     + SResources.getString("l.current")
                     + VersionInfo.getVersion()
                     + " / "
                     + SResources.getString("l.latest")
                     + var1
               );
         }

         if (var3 && !var1.equals(VersionInfo.getVersion()) && PreferenceLoader.loadBoolean("versionCheckPopup")) {
            new VersionChecker$VersionDialog(var2.shell, var1).open();
         }
      }
   }
}
