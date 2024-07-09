package cli.command;

import app.AppConfig;
import file.FileUtils;


public class RemoveCommand implements CLICommand {

    @Override
    public String commandName() {
        return "remove";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for remove command. Should be remove path.");
            return;
        }

        String path = AppConfig.ROOT_DIR + FileUtils.FILE_SEPARATOR + args;

        AppConfig.chordState.removeFileFromStorage(path);
    }
}
