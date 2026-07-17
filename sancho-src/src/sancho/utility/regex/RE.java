/*
 * Added during the modernization: thin java.util.regex adapter replacing the
 * abandoned gnu-regexp dependency (gnu-regexp:1.1.4, ~2001). Backed entirely by the
 * JDK's regex engine; only the small gnu.regexp API surface Sancho used is kept, so
 * call sites just changed their imports. All of Sancho's existing patterns were
 * verified to compile unchanged under java.util.regex.
 */
package sancho.utility.regex;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Drop-in replacement for the parts of {@code gnu.regexp.RE} that Sancho used. */
public class RE {

   /** Case-insensitive flag — same value as {@code gnu.regexp.RE.REG_ICASE}. */
   public static final int REG_ICASE = 0x02;

   private final Pattern pattern;

   public RE(String expr) throws REException {
      this(expr, 0);
   }

   /**
    * @param cflags gnu.regexp compile flags; only REG_ICASE (0x02) is meaningful
    *               here (other bits, e.g. REG_NOTBOL, are ignored as none of
    *               Sancho's patterns rely on them).
    */
   public RE(String expr, int cflags) throws REException {
      int flags = 0;
      if ((cflags & REG_ICASE) != 0) {
         flags |= Pattern.CASE_INSENSITIVE;
      }
      try {
         this.pattern = Pattern.compile(expr, flags);
      } catch (PatternSyntaxException e) {
         throw new REException(e.getMessage(), e);
      }
   }

   /** First match in {@code input}, or {@code null} if there is none. */
   public REMatch getMatch(String input) {
      if (input == null) {
         return null;
      }
      Matcher m = this.pattern.matcher(input);
      return m.find() ? new REMatch(m) : null;
   }

   /** All non-overlapping matches in {@code input} (never {@code null}). */
   public REMatch[] getAllMatches(String input) {
      ArrayList<REMatch> matches = new ArrayList<REMatch>();
      if (input != null) {
         Matcher m = this.pattern.matcher(input);
         while (m.find()) {
            matches.add(new REMatch(m));
         }
      }
      return matches.toArray(new REMatch[0]);
   }

   /** Whether {@code input} contains at least one match. */
   public boolean isMatch(String input) {
      return input != null && this.pattern.matcher(input).find();
   }

   /** Replace every match with {@code replacement} ({@code $1} backrefs supported). */
   public String substituteAll(String input, String replacement) {
      if (input == null) {
         return null;
      }
      return this.pattern.matcher(input).replaceAll(replacement);
   }
}
