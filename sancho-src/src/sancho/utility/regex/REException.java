/*
 * Added during the modernization: thin java.util.regex adapter replacing the
 * abandoned gnu-regexp dependency. Mirrors the small gnu.regexp API surface Sancho
 * used, so call sites only needed their imports changed.
 */
package sancho.utility.regex;

/** Checked exception thrown when a pattern fails to compile (like gnu.regexp). */
public class REException extends Exception {
   private static final long serialVersionUID = 1L;

   public REException(String message) {
      super(message);
   }

   public REException(String message, Throwable cause) {
      super(message, cause);
   }
}
