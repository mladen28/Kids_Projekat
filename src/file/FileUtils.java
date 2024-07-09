package file;

import app.AppConfig;
import app.ChordState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtils {
    public static final String FILE_SEPARATOR = File.separator;

    public static boolean isPathFile(String rootDirectory, String path) {
        File f = new File(constructFullPath(rootDirectory, path));
        System.out.println("# file exists");
        return f.isFile();
    }

    public static MyFile getFileInfoFromPath(String rootDirectory, String path, String sharingType) {
        path = constructFullPath(rootDirectory, path);
        File f = new File(path);

        if (!f.exists()) {
            AppConfig.timestampedErrorPrint("File " + path + " doesn't exist.");
            return null;
        }

        try {
            int hashFile = ChordState.fileHash(path);
            String fileContent = readFileContent(f);
            return new MyFile(path, fileContent, AppConfig.myServentInfo.getChordId(), sharingType, hashFile);
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't read " + path + ".");
        }

        return null;
    }
    private static String constructFullPath(String rootDirectory, String path) {
        return rootDirectory + FILE_SEPARATOR + path;
    }

    private static String readFileContent(File file) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line).append("\n");
        }

        reader.close();
        return fileContent.toString().trim();
    }
}