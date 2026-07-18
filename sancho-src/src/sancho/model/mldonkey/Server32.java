package sancho.model.mldonkey;

import sancho.core.ICore;

public class Server32 extends Server29 {
   Server32(ICore var1) {
      super(var1);
   }

   public void togglePreferred() {
      Object[] var1 = new Object[]{Integer.valueOf(this.getId()), Byte.valueOf((byte)(this.isPreferred() ? 0 : 1))};
      this.core.send((short)67, var1);
   }

   public void rename(String var1) {
      Object[] var2 = new Object[]{Integer.valueOf(this.getId()), var1};
      this.core.send((short)66, var2);
   }
}
