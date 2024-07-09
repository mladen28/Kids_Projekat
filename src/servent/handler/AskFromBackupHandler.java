package servent.handler;

import app.AppConfig;
import servent.message.AskFromBackupMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellBackupMessage;
import servent.message.util.MessageUtil;

public class AskFromBackupHandler implements MessageHandler{
    public Message clientMessage;
    public AskFromBackupHandler(Message clientMessage){
        this.clientMessage = clientMessage;
    }
    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ASK_FROM_BACKUP) {
            AskFromBackupMessage askFromBackupMessage = (AskFromBackupMessage) clientMessage;
            if(AppConfig.chordState.getBackupMap().containsKey(askFromBackupMessage.getPort())) {
                TellBackupMessage tellBackupMessage = new TellBackupMessage(
                        AppConfig.myServentInfo.getIpAddress(),
                        AppConfig.myServentInfo.getListenerPort(),
                        clientMessage.getReceiverIpAddress(),
                        clientMessage.getReceiverPort()
                );
                MessageUtil.sendMessage(tellBackupMessage);
            } else {
                AskFromBackupMessage askFromBackupMessage1 = new AskFromBackupMessage(
                        ((AskFromBackupMessage) clientMessage).getSenderIpAddress(),
                        clientMessage.getSenderPort(),
                        AppConfig.chordState.getNextNodeIp(),
                        AppConfig.chordState.getNextNodePort(),
                        askFromBackupMessage.getPort()
                );
                MessageUtil.sendMessage(askFromBackupMessage1);
            }
        }
    }
}
