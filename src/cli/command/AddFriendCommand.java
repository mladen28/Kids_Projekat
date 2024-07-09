package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import cli.command.CLICommand;
import servent.handler.AddFriendHandler;
import servent.message.AddFriendMessage;
import servent.message.util.MessageUtil;

public class AddFriendCommand implements CLICommand {

    @Override
    public String commandName() {
        return "add_friend";
    }

    @Override
    public void execute(String args) {
        String[] splitArgs = args.split(":");

        if (splitArgs.length == 2) {
            String adresa = splitArgs[0];
            int port = 0;
            try {
                port = Integer.parseInt(splitArgs[1]);

                if (port <= 0 || port > 2000) {
                    throw new NumberFormatException();
                }

                AppConfig.chordState.addFriend(port);

                AddFriendMessage afs = new AddFriendMessage(AppConfig.myServentInfo.getIpAddress(),AppConfig.myServentInfo.getListenerPort(), adresa, port, String.valueOf(AppConfig.myServentInfo.getListenerPort()));                MessageUtil.sendMessage(afs);

            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Nevažeći broj porta. Treba da bude ceo broj između 1000 i 2000.");
            }
        } else {
            AppConfig.timestampedErrorPrint("Nevažeći argumenti za add_friend. Koristi format [adresa:port]");
        }
    }
}
