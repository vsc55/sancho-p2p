package sancho.model.mldonkey;

import sancho.core.ICore;

public class Client23 extends Client21 {
   public Client23(ICore var1) {
      super(var1);
   }

   public void connect() {
      this.core.send((short)61, Integer.valueOf(this.getId()));
   }

   public void disconnect() {
      this.core.send((short)62, Integer.valueOf(this.getId()));
   }
}
