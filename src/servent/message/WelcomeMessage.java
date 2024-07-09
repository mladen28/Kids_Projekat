package servent.message;

import file.MyFile;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, MyFile> values;



	public WelcomeMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,Map<Integer, MyFile> values) {
		super(MessageType.WELCOME, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

		this.values = values;
	}
	public Map<Integer, MyFile> getValues() {
		return values;
	}
}
