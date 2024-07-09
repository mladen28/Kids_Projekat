package servent.handler;

import app.AppConfig;
import servent.message.ConfirmFriendMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AddFriendHandler implements MessageHandler{
    private Message clientMessage;

    public AddFriendHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ADD_FRIEND) {
            try {
                int friendPort = Integer.parseInt(clientMessage.getMessageText());

                AppConfig.chordState.addFriend(friendPort);

                if (clientMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
                    ConfirmFriendMessage confirmationMessage = new ConfirmFriendMessage(
                            AppConfig.myServentInfo.getIpAddress(),
                            AppConfig.myServentInfo.getListenerPort(),
                            "localhost",
                            friendPort,
                            String.valueOf(AppConfig.myServentInfo.getListenerPort())
                    );
                    MessageUtil.sendMessage(confirmationMessage);
                }
            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got add friend message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Add friend handler got a message that is not ADD_FRIEND");
        }
    }
}
