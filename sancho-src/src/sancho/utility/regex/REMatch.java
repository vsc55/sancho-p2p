/*
 * Added during the modernization: thin java.util.regex adapter replacing the
 * abandoned gnu-regexp dependency.
 */
package sancho.utility.regex;

import java.util.regex.Matcher;

/**
 * Immutable snapshot of a single {@link Matcher} match, exposing the pieces of the
 * old {@code gnu.regexp.REMatch} that Sancho used: the matched text, its absolute
 * start/end, and per-group start/end.
 */
public class REMatch {
   private final String text;
   private final int start;
   private final int end;
   private final int[] groupStart;
   private final int[] groupEnd;
   private final String[] groups;

   REMatch(Matcher m) {
      this.text = m.group();
      this.start = m.start();
      this.end = m.end();
      int n = m.groupCount();
      this.groupStart = new int[n + 1];
      this.groupEnd = new int[n + 1];
      this.groups = new String[n + 1];
      for (int i = 0; i <= n; i++) {
         this.groupStart[i] = m.start(i);
         this.groupEnd[i] = m.end(i);
         this.groups[i] = m.group(i);
      }
   }

   /** The whole matched text (group 0). */
   public String toString() {
      return this.text;
   }

   /** The text of capture group {@code sub} (0 = whole match). */
   public String toString(int sub) {
      return sub >= 0 && sub < this.groups.length ? this.groups[sub] : null;
   }

   public int getStartIndex() {
      return this.start;
   }

   public int getEndIndex() {
      return this.end;
   }

   public int getStartIndex(int sub) {
      return sub >= 0 && sub < this.groupStart.length ? this.groupStart[sub] : -1;
   }

   public int getEndIndex(int sub) {
      return sub >= 0 && sub < this.groupEnd.length ? this.groupEnd[sub] : -1;
   }
}
