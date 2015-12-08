package gvn;

import java.io.File;
import java.io.FileFilter;

public class GitDirFilter implements FileFilter {

  @Override
  public boolean accept(File file) {
    if (file.isDirectory()) {
      File[] gitDir = file.listFiles(new FileFilter() {
        @Override
        public boolean accept(File subFile) {
          return subFile.isDirectory() 
//              && subFile.isHidden() 
              && subFile.getName().equals(".git");
        }
      });
      return gitDir.length > 0;
    } else {
      return false;
    }
  }

}
