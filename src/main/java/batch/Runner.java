package batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import exception.GitCommandErrorException;

public class Runner {

    private final String cmd;

    public Runner(String cmd) {
        this.cmd = cmd;
    }

    public void execute() throws Exception {

        long start = System.currentTimeMillis();
        Arrays.asList(new File(".").listFiles(file -> {
            if (file.isDirectory()) {
                File[] folders = file.listFiles(sub -> {
                    return sub.isDirectory() && sub.getName().equals(".git");
                });
                return folders.length > 0;
            } else {
                return false;
            }
        })).parallelStream().map(folder -> {
            StringBuilder builder = new StringBuilder();
            try {
                builder.append(folder.getName()).append("\n");
                Process process = Runtime.getRuntime().exec(cmd, null, folder.getAbsoluteFile());
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }
                    return builder.toString();
                }

            } catch (IOException e) {
                return builder.append("Error:").append(e.getMessage()).append("\n").toString();

            }
        }).forEach(response -> {
            System.out.println(response);
        });

        System.out.println("Time spent: " + (System.currentTimeMillis() - start) + " ms");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -cp [jar file] [command]");
            System.exit(0);
        }
        String cmd = composeCommand(args);
        System.out.println("Command: " + cmd);
        if (!cmd.startsWith("git")) {
            throw new GitCommandErrorException(cmd);
        }
        new Runner(cmd).execute();
    }

    private static String composeCommand(String[] args) {

        StringBuilder builder = new StringBuilder("");
        for (String arg : args) {
            builder.append(arg + " ");
        }
        return builder.toString();
    }

}
