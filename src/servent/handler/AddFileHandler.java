package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import file.MyFile;
import servent.message.AddFileMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AddFileHandler implements MessageHandler {

    private final Message clientMessage;

    public AddFileHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ADD_FILE) {
            AddFileMessage additionFileMsg = (AddFileMessage) clientMessage;

            int fileKey = additionFileMsg.getFile().getFileKey();

            if (AppConfig.chordState.isKeyMine(fileKey)) {
                MyFile file = additionFileMsg.getFile();
                String sharingType = additionFileMsg.getSharingType();
                AppConfig.chordState.addToStorage(file, sharingType);
            }
            else {

                Message nextSuccessMessage = new AddFileMessage(
                        AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                        AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                        additionFileMsg.getFile(), additionFileMsg.getSharingType());
                MessageUtil.sendMessage(nextSuccessMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Add success handler got message that's not of type ADD_SUCCESS.");
        }

    }

}
