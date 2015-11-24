package gvn.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class GvnCommand implements Callable<String> {

  private final File project;
  private final String command;

  public GvnCommand(File project, String command) {
    this.project = project;
    this.command = command;
  }

  @Override
  public final String call() throws Exception {
    StringBuilder builder = new StringBuilder();
    Process process = Runtime.getRuntime().exec(command, null, project.getAbsoluteFile());
    try (InputStreamReader in = new InputStreamReader(process.getInputStream()); 
        BufferedReader reader = new BufferedReader(in);) {
      String line = "";
      while ((line = reader.readLine()) != null) {
        builder.append(line).append("\n");
      }
      return builder.toString();
    }
  }

  public String getProjectName() {
    return project.getName();
  }

}
