package servent.message;



import file.MyFile;

import java.io.Serial;

public class TellViewFileMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -6213394344524749872L;


	private final MyFile file;

	public TellViewFileMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, MyFile file) {
		super(MessageType.TELL_VIEW_FILE, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
		this.file = file;
	}


	public MyFile getFile() {
		return file;
	}

}
