package exceptions;

// Modifica "Exception" in "RuntimeException"
public class DatabaseException extends Exception {

  public DatabaseException(String message) {
    super(message);
  }

  public DatabaseException(String sqlState, Throwable cause) {
    super(format(sqlState, cause), cause);
  }

  private static String format(String sqlState, Throwable cause) {
    String state = (sqlState == null || sqlState.isBlank()) ? "?????" : sqlState;

    String msg = (cause == null || cause.getMessage() == null || cause.getMessage().isBlank())
            ? "(nessun messaggio)"
            : cause.getMessage().replace("\n", " ").replace("\r", " ");

    return "SQLSTATE " + state + ": " + msg;
  }
}