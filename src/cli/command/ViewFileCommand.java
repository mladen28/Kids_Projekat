package cli.command;

import app.AppConfig;
import file.FileUtils;
import file.MyFile;

public class ViewFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "view_file";
    }

    @Override
    public void execute(String args) {
        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid arguments for pull command. Should be view_file file name [file.txt]");
            return;
        }

        String[] splitArgs = args.split(" ");

        String path = AppConfig.ROOT_DIR + FileUtils.FILE_SEPARATOR + splitArgs[0];

        MyFile file = AppConfig.chordState.viewFile(path, AppConfig.myServentInfo.getListenerPort());

        if(file != null) {
            AppConfig.chordState.printPulledFiles(file);
        }
    }
}
