package sancho.view.server;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.jface.dialogs.IInputValidator;
import sancho.view.utility.SResources;

class ServerViewFrame$HTTPValidator implements IInputValidator {
   static Pattern regex;

   public String isValid(String var1) {
      return regex.matcher(var1).find() ? null : SResources.getString("l.invalidInput");
   }

   static {
      try {
         regex = Pattern.compile("http(s)?://\\S*");
      } catch (PatternSyntaxException var1) {
      }
   }
}
