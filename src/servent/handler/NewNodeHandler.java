package servent.handler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import app.AppConfig;
import app.ServentInfo;
import file.MyFile;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.SorryMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

import static app.AppConfig.chordState;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;
	
	public NewNodeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
			int newNodePort = clientMessage.getSenderPort();
			ServentInfo newNodeInfo = new ServentInfo("localhost", newNodePort);
			
			//check if the new node collides with another existing node.
			if (chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						"localhost", clientMessage.getSenderPort());
				MessageUtil.sendMessage(sry);
				return;
			}

			//check if he is my predecessor
			boolean isMyPred = chordState.isKeyMine(newNodeInfo.getChordId());
			if (isMyPred) { //if yes, prepare and send welcome message


				ServentInfo hisPred = chordState.getPredecessor();
				if (hisPred == null) {
					hisPred = AppConfig.myServentInfo;
				}
				
				chordState.setPredecessor(newNodeInfo);
				
				Map<Integer, MyFile> myValues = chordState.getValueMap();
				Map<Integer, MyFile> hisValues = new ConcurrentHashMap<>();


				int myId = AppConfig.myServentInfo.getChordId();
				int hisPredId = hisPred.getChordId();
				int newNodeId = newNodeInfo.getChordId();
				
				for (Entry<Integer, MyFile> valueEntry : myValues.entrySet()) {
					int key = valueEntry.getKey();
					if (hisPredId == myId) { //i am first and he is second
						if (myId < newNodeId) {
							if (key <= newNodeId && key > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else {
							if (key <= newNodeId || key > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
					}
					if (hisPredId < myId) { //my old predecesor was before me
						if (key <= newNodeId) {
							hisValues.put(valueEntry.getKey(), valueEntry.getValue());
						}
					} else { //my old predecesor was after me
						if (hisPredId > newNodeId) { //new node overflow
							if (key <= newNodeId || key > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else { //no new node overflow
							if (key <= newNodeId && key > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
						
					}
					
				}
				for (Integer key : hisValues.keySet()) { //remove his values from my map
					myValues.remove(key);
				}
				chordState.setValueMap(myValues);
				

				WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						"localhost", newNodePort, hisValues);
				MessageUtil.sendMessage(wm);
			} else { //if he is not my predecessor, let someone else take care of it
				ServentInfo nextNode = chordState.getNextNodeForKey(newNodeInfo.getChordId());
				NewNodeMessage nnm = new NewNodeMessage("localhost", newNodePort, nextNode.getIpAddress(), nextNode.getListenerPort());
				MessageUtil.sendMessage(nnm);
			}
			
		} else {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
		}

	}

}
