package sancho.view.server;

import sancho.utility.regex.RE;
import sancho.utility.regex.REException;
import org.eclipse.jface.dialogs.IInputValidator;
import sancho.view.utility.SResources;

class ServerViewFrame$HTTPValidator implements IInputValidator {
   static RE regex;

   public String isValid(String var1) {
      return regex.isMatch(var1) ? null : SResources.getString("l.invalidInput");
   }

   static {
      try {
         regex = new RE("http(s)?://\\S*");
      } catch (REException var1) {
      }
   }
}
