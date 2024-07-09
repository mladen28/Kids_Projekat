package servent.message;


import file.MyFile;

import java.io.Serial;

public class AddFileMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 7277911492888707017L;
    private final MyFile file;
    private final String sharingType;

    public AddFileMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, MyFile file, String sharingType) {
        super(MessageType.ADD_FILE, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.file = file;
        this.sharingType = sharingType;
    }

    public MyFile getFile() { return file; }

    public String getSharingType() {
        return sharingType;
    }
}
