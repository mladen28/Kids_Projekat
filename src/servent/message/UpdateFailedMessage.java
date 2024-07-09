package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class UpdateFailedMessage extends BasicMessage{
    @Serial
    private static final long serialVersionUID = 2261615800740634457L;
    private final ServentInfo failedServent;
    public UpdateFailedMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, ServentInfo failedServent) {
        super(MessageType.UPDATE_FAILED, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.failedServent = failedServent;
    }

    public ServentInfo getFailedServent() {
        return failedServent;
    }
}
