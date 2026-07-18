package sancho.view.console;

import org.eclipse.swt.widgets.Composite;
import sancho.core.Sancho;

public class RoomConsole extends Console {
   protected int roomNumber;

   public RoomConsole(Composite var1, int var2, int var3) {
      super(var1, var2);
      this.roomNumber = var3;
   }

   public void sendMessage() {
      if (Sancho.hasCollectionFactory()) {
         Object[] var1 = new Object[]{Integer.valueOf(this.roomNumber), Byte.valueOf((byte)1), Integer.valueOf(0), this.input.getText()};
         Sancho.send((short)39, var1);
      }
   }
}
