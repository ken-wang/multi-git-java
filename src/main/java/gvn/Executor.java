package gvn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import gvn.command.GvnCommand;
import gvn.exception.GitCommandErrorException;

class Result {

  private String projectName;
  private Future<String> future;

  public Result(String projectName, Future<String> future) {
    this.projectName = projectName;
    this.future = future;
  }

  public String getProjectName() {
    return projectName;
  }

  public Future<String> getFuture() {
    return future;
  }
}


public class Executor {

  private final String PRE_PROJECT_NAME = "com.live365";
  private final String cmd;
  public Executor(String cmd) {
    this.cmd = cmd;
  }

  public void start() throws Exception {
    long before = System.currentTimeMillis();
    
    List<File> projects = getProjects();
    List<GvnCommand> commands = createGvnCommands(projects);
    List<Result> results = execute(commands);
    printResult(results);
    
    long after = System.currentTimeMillis();
    System.out.println("Time spent: " + (after - before) + " ms");
  }

  private List<File> getProjects() throws IOException {
    File dir = new File(".");
    List<File> projectFiles = new ArrayList<File>();
    for (File file : dir.listFiles()) {
      if (isGitProject(file)) {
        projectFiles.add(file);
      }
    }
    return projectFiles;
  }
  
  private boolean isGitProject(File file) {
    
    return file.isDirectory() && file.getName().startsWith(PRE_PROJECT_NAME);
  }
  
  private List<GvnCommand> createGvnCommands(List<File> projects) throws Exception {

    List<GvnCommand> commands = new ArrayList<GvnCommand>();
    for (File project : projects) {
      commands.add(new GvnCommand(project, cmd));
    }
    return commands;
  }

  private List<Result> execute(List<GvnCommand> commands) {

    ExecutorService exec = Executors.newCachedThreadPool();
    List<Result> results = new ArrayList<Result>();

    for (GvnCommand command : commands) {
      Future<String> future = exec.submit(command);
      results.add(new Result(command.getProjectName(), future));
    }
    exec.shutdown();
    return results;
  }

  private void printResult(List<Result> results) throws InterruptedException, ExecutionException {
  
    System.out.println("Executed command: " + cmd);
    for (Result executor : results) {
      System.out.println(executor.getProjectName());
      System.out.println(executor.getFuture().get());
    }
  }

  public static void main(String[] args) throws Exception {

    if (args.length == 0) {
      System.out.println("Usage: java -cp [jar file] [command: git svn rebase, git svn dcommit, git svn fetch]");
      System.exit(0);
    }
    
    String cmd = composeCommand(args);
    if(!cmd.startsWith("git")) {
      throw new GitCommandErrorException(cmd);
    }
    new Executor(cmd).start();
  }
  
  private static String composeCommand(String[] args){
    
    StringBuilder builder = new StringBuilder("");
    for(String arg : args) {
      builder.append(arg + " ");
    }
    return builder.toString();
  }

}
