package sancho.view.utility;

class VersionChecker$3 implements Runnable {
   // $VF: synthetic field
   private final VersionChecker$1 this$1;

   VersionChecker$3(VersionChecker$1 var1) {
      this.this$1 = var1;
   }

   public void run() {
      VersionChecker$1.access$000(this.this$1).statusLine.setText(SResources.getString("l.versionCheckUnavailable"));
   }
}
