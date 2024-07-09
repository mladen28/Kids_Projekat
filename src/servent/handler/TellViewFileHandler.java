package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellViewFileMessage;
import servent.message.util.MessageUtil;

public class TellViewFileHandler implements MessageHandler {

	private final Message clientMessage;
	
	public TellViewFileHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.TELL_VIEW_FILE) {
			TellViewFileMessage tellPullMessage = (TellViewFileMessage) clientMessage;
			AppConfig.chordState.printPulledFiles(tellPullMessage.getFile());
		} else {
			AppConfig.timestampedErrorPrint("Tell get handler got a message that is not TELL_GET");
		}
	}

}
