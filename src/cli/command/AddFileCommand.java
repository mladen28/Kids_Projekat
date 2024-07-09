package cli.command;

import app.AppConfig;
import app.ChordState;
import file.FileUtils;
import file.MyFile;

public class AddFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "add_file";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid arguments for add file command. Usage: add_file path [private/public]");
            return;
        }

        String[] splitArgs = args.split(" ");
        if (splitArgs.length != 2) {
            AppConfig.timestampedStandardPrint("Invalid number of arguments. Usage: add_file path [private/public]");
            return;
        }

        String path = splitArgs[0];
        String sharingType = splitArgs[1];

        if (!sharingType.equalsIgnoreCase("private") && !sharingType.equalsIgnoreCase("public")) {
            AppConfig.timestampedStandardPrint("Invalid sharing type. Should be either 'private' or 'public'.");
            return;
        }

        processFilePath(path, sharingType);

    }

    private void processFilePath(String path, String sharingType) {

        if (FileUtils.isPathFile(AppConfig.ROOT_DIR, path)) {
            MyFile file = FileUtils.getFileInfoFromPath(AppConfig.ROOT_DIR, path, sharingType);
            if (file != null) {
                AppConfig.chordState.addToStorage(file, sharingType);
            }
        } else {
            AppConfig.timestampedErrorPrint("File " + path + " doesn't exist or it's a directory.");
        }
    }

}