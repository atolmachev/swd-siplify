package simplifyme;

public class GrepException extends RuntimeException {
  public GrepException(String message) {
    super(message);
  }

  public GrepException(String message, Throwable cause) {
    super(message, cause);
  }
}
