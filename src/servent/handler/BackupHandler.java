package servent.handler;

import app.AppConfig;
import servent.message.BackupMessage;
import servent.message.Message;
import servent.message.MessageType;

public class BackupHandler implements MessageHandler {

    private Message clientMessage;

    public BackupHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.BACKUP) {
            BackupMessage backupMessage = (BackupMessage) clientMessage;
            AppConfig.chordState.addToBackupMap(clientMessage.getSenderPort(), backupMessage.getMap());
            AppConfig.timestampedStandardPrint("Received backup data");
        } else {
            AppConfig.timestampedErrorPrint("Backup handler got something that is not backup message.");
        }
    }
}

