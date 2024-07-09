package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class PongHandler implements MessageHandler{

    private Message clientMessage;

    public PongHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.PONG) {

            if(clientMessage.getMessageText().equals("Pred")){
                AppConfig.chordState.setPingPred(false);
            }
            else{
                AppConfig.chordState.setPingSuc(false);
            }

        }else {
            AppConfig.timestampedErrorPrint("PONG handler got something that is not pong message.");
        }

    }
}