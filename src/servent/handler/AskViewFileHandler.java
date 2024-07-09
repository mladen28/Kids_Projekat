package servent.handler;

import app.AppConfig;
import file.MyFile;
import servent.message.AskViewFileMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellViewFileMessage;
import servent.message.util.MessageUtil;


public class AskViewFileHandler implements MessageHandler {

	private final Message clientMessage;
	
	public AskViewFileHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ASK_VIEW_FILE) {
			AskViewFileMessage askViewFileMessageMessage = (AskViewFileMessage) clientMessage;

			MyFile file = AppConfig.chordState.viewFile(askViewFileMessageMessage.getMessageText(), clientMessage.getSenderPort());

			if(file != null) {
				Message tellMessage = new TellViewFileMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
								AppConfig.chordState.getNextNodeIp(), clientMessage.getSenderPort(), file);
				MessageUtil.sendMessage(tellMessage);
			}

		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}
	}

}