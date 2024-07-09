package servent.handler;

import app.AppConfig;
import file.MyFile;
import servent.message.Message;
import servent.message.MessageType;

import java.util.Map;

public class TellBackupHandler implements MessageHandler{
    private final Message clientMessage;

    public TellBackupHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TELL_BACKUP) {
            AppConfig.chordState.addToValueMap(AppConfig.chordState.getBackupMap().get(AppConfig.chordState.getPredecessor().getListenerPort()));
        }
    }
}
