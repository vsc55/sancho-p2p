package sancho.view.transfer.downloads;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import sancho.core.Sancho;

class EditMP3TagsDialog$1 extends SelectionAdapter {
   // $VF: synthetic field
   private final EditMP3TagsDialog this$0;

   EditMP3TagsDialog$1(EditMP3TagsDialog var1) {
      this.this$0 = var1;
   }

   public void widgetSelected(SelectionEvent var1) {
      Object[] var2 = new Object[]{
         Integer.valueOf(EditMP3TagsDialog.access$000(this.this$0).getId()),
         EditMP3TagsDialog.access$100(this.this$0).getText(),
         EditMP3TagsDialog.access$200(this.this$0).getText(),
         EditMP3TagsDialog.access$300(this.this$0).getText(),
         EditMP3TagsDialog.access$400(this.this$0).getText(),
         EditMP3TagsDialog.access$500(this.this$0).getText(),
         Integer.valueOf(EditMP3TagsDialog.access$600(this.this$0).getSelection()),
         Integer.valueOf(EditMP3TagsDialog.access$700(this.this$0).getSelectionIndex())
      };
      Sancho.send((short)26, var2);
      this.this$0.close();
   }
}
