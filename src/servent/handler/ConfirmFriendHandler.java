package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class ConfirmFriendHandler implements MessageHandler{
    private Message clientMessage;

    public ConfirmFriendHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.CONFIRM_FRIEND) {
            try {
                int friendPort = Integer.parseInt(clientMessage.getMessageText());
                AppConfig.timestampedStandardPrint("Successfully confirmed friend with port: " + friendPort);
            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got confirm friend message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Confirm friend handler got a message that is not CONFIRM_FRIEND");
        }
    }
}

