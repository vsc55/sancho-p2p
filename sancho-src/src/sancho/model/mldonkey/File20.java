package sancho.model.mldonkey;

import sancho.core.ICore;

public class File20 extends File18 {
   File20(ICore var1) {
      super(var1);
   }

   public void rename(String var1) {
      this.core.send((short)56, new Object[]{Integer.valueOf(this.getId()), var1});
   }
}
