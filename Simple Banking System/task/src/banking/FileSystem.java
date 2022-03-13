package banking;

import java.io.File;

public class FileSystem {
    public static Boolean checkForFile(String path) {
        File tempFile = new File(path);
        if (tempFile.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
