package sancho.view.utility;

class LinkRipper$11 implements Runnable {
   // $VF: synthetic field
   private final String[] val$urlArray;
   // $VF: synthetic field
   private final LinkRipper this$0;

   LinkRipper$11(LinkRipper var1, String[] var2) {
      this.this$0 = var1;
      this.val$urlArray = var2;
   }

   public void run() {
      this.this$0.urlGroup.setText(SResources.getString("l.foundLinks") + "(" + this.val$urlArray.length + "):");

      for (int var1 = 0; var1 < this.val$urlArray.length; var1++) {
         this.this$0.urlList.add(this.val$urlArray[var1]);
      }
   }
}
