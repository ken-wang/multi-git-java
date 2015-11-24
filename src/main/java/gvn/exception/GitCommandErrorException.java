package gvn.exception;

public class GitCommandErrorException extends RuntimeException {

  private static final String errorMsg = "' %s' is not a correct git command.";

  public GitCommandErrorException(String cmd) {
    super(String.format(errorMsg, cmd));
  }

}
