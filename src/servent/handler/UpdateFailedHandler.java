package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AskFromBackupMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateFailedMessage;
import servent.message.util.MessageUtil;

public class UpdateFailedHandler implements MessageHandler{

    private Message clientMessage;

    public UpdateFailedHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }


    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.UPDATE_FAILED) {
            UpdateFailedMessage updateFailedMessage = (UpdateFailedMessage) clientMessage;

            System.out.println(AppConfig.chordState.getPredecessor().getListenerPort());
            System.out.println(updateFailedMessage.getFailedServent().getListenerPort());

            for (ServentInfo servent : AppConfig.chordState.getAllNodeInfo()) {
                if (servent.getListenerPort() == updateFailedMessage.getFailedServent().getListenerPort()) {
                    if (AppConfig.chordState.getPredecessor() == updateFailedMessage.getFailedServent()) {
                        AskFromBackupMessage askFromBackupMessage = new AskFromBackupMessage(
                                AppConfig.myServentInfo.getIpAddress(),
                                AppConfig.myServentInfo.getListenerPort(),
                                AppConfig.chordState.getNextNodeIp(),
                                AppConfig.chordState.getNextNodePort(),
                                AppConfig.chordState.getPredecessor().getListenerPort()
                        );
                        MessageUtil.sendMessage(askFromBackupMessage);
                    } else {
                        System.out.println(AppConfig.chordState.getSuccessorTable()[0].getListenerPort());
                        System.out.println(updateFailedMessage.getFailedServent().getListenerPort());

                        if (AppConfig.chordState.getSuccessorTable()[0].getListenerPort() == updateFailedMessage.getFailedServent().getListenerPort()) {
                            AppConfig.chordState.handleNodeFailed(updateFailedMessage.getFailedServent());
                            AppConfig.timestampedStandardPrint("My successor with port " + AppConfig.chordState.getSuccessorTable()[0].getListenerPort() + " is shutdown");

                        } else {
                            AppConfig.chordState.handleNodeFailed(updateFailedMessage.getFailedServent());
                            UpdateFailedMessage updateMessage = new UpdateFailedMessage(
                                    AppConfig.myServentInfo.getIpAddress(),
                                    AppConfig.myServentInfo.getListenerPort(),
                                    AppConfig.chordState.getSuccessorTable()[0].getIpAddress(),
                                    AppConfig.chordState.getSuccessorTable()[0].getListenerPort(),
                                    updateFailedMessage.getFailedServent()
                            );
                            MessageUtil.sendMessage(updateMessage);
                        }
                    }
                }
            }
            AppConfig.timestampedStandardPrint("The system has already been updated");
        }
    }
}
