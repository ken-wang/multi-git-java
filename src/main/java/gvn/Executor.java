package gvn;

import java.io.File;
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

  private static final GitDirFilter filter = new GitDirFilter();
  private final String cmd;

  public Executor(String cmd) {
    this.cmd = cmd;
  }

  public void start() throws Exception {
    long start = System.currentTimeMillis();

    File[] gitProjects = listGitProjects();
    List<Result> results = execute(gitProjects);
    printResult(results);

    System.out.println("Time spent: " + (System.currentTimeMillis() - start) + " ms");
  }

  private File[] listGitProjects() {
    return new File(".").listFiles(filter);
  }

  private List<Result> execute(File[] gitProjects) {
    
    ExecutorService exec = Executors.newCachedThreadPool();
    List<Result> results = new ArrayList<Result>();
    for (File project : gitProjects) { 
      Future<String> future = exec.submit(new GvnCommand(project, this.cmd));
      results.add(new Result(project.getName(), future));
    }
    exec.shutdown();
    return results;
  }
  
  private void printResult(List<Result> results) throws InterruptedException, ExecutionException {

    System.out.println("Executed command: " + this.cmd);
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
    if (!cmd.startsWith("git")) {
      throw new GitCommandErrorException(cmd);
    }
    new Executor(cmd).start();
  }

  private static String composeCommand(String[] args) {

    StringBuilder builder = new StringBuilder("");
    for (String arg : args) {
      builder.append(arg + " ");
    }
    return builder.toString();
  }

}
