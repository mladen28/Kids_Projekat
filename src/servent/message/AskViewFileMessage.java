package servent.message;



import file.MyFile;

import java.io.Serial;

public class AskViewFileMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -8558031124520315033L;
	private final int requesterId;


	public AskViewFileMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, int requesterId, String path) {
		super(MessageType.ASK_VIEW_FILE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, path);
		this.requesterId = requesterId;
	}

	public int getRequesterId() {
		return requesterId;
	}


}
