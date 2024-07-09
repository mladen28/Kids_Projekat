package servent.message;

import file.MyFile;

import java.util.Map;

public class BackupMessage extends BasicMessage {

    private static final long serialVersionUID = 435L;

    private final Map<Integer, MyFile> map;

    public BackupMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, Map<Integer, MyFile> mapa) {
        super(MessageType.BACKUP, senderIp, senderPort, receiverIp, receiverPort);
        this.map = mapa;
    }

    public Map<Integer, MyFile> getMap() {
        return map;
    }
}
