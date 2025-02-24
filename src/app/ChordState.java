package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import file.MyFile;
import servent.message.*;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;

	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}
	public static int fileHash(String str) {
		int hash = 7;
		for (int i = 0; i < str.length(); i++) {
			hash = (hash*31 + str.charAt(i)) % CHORD_SIZE;
		}
		return hash;
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	
	private Map<Integer, MyFile> valueMap;

	private Map<Integer, Map<Integer, MyFile>> backupMap;

	private List<Integer> prijatelji;

	private static volatile boolean pingPred = false;
	private static volatile boolean pingSuc = false;

	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new ConcurrentHashMap<>();
		allNodeInfo = new ArrayList<>();
		backupMap = new ConcurrentHashMap<>();
		prijatelji = new ArrayList<>();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
		this.valueMap = welcomeMsg.getValues();
		//proveri da li treba nova da se kreira ili saamo pozove get iz welcomeMs

		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}
	public String getNextNodeIp() {
		return successorTable[0].getIpAddress();
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public void setSuccessorTable(ServentInfo[] newSuccessorTable) {
		this.successorTable = newSuccessorTable;
	}

	public Map<Integer, MyFile> getValueMap() {
		return valueMap;
	}

	public void setValueMap(Map<Integer, MyFile> valueMap) {
		this.valueMap = valueMap;
	}

	public List<ServentInfo> getAllNodeInfo() {
		return allNodeInfo;
	}

	public Map<Integer, Map<Integer, MyFile>> getBackupMap() {
		return backupMap;
	}

	public void addToBackupMap(Integer port, Map<Integer, MyFile> backupMap) {
		this.backupMap.put(port, backupMap);
	}
	public void addToValueMap(Map<Integer, MyFile> valueMap) {
		this.valueMap.putAll(valueMap);
	}

	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		
		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size()-1);
		} else {
			predecessorInfo = newList.get(newList.size()-1);
		}
		
		updateSuccessorTable();
	}

	public void handleNodeFailed(ServentInfo newNode) {
		if (newNode == null) {
			AppConfig.timestampedErrorPrint("Couldn't find node with port " + newNode.getListenerPort());
			return;
		}
		allNodeInfo.remove(newNode);

		allNodeInfo.sort(new Comparator<ServentInfo>() {

			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}

		});

		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();

		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}

		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size()-1);
		} else {
			predecessorInfo = newList.get(newList.size()-1);
		}

		updateSuccessorTable();
	}

	public void addFriend(int port) {
		if (!prijatelji.contains(port)) {
			prijatelji.add(port);
			AppConfig.timestampedStandardPrint("Node with port " + port + " added as a friend.");
		} else {
			AppConfig.timestampedStandardPrint("Node with port " + port + " is already a friend.");
		}
	}
	public void addToStorage(MyFile file, String sharingType) {
		String filePath = file.getPath();
		int hashFile = file.getFileKey();

		if (valueMap.containsKey(hashFile)) {
			AppConfig.timestampedStandardPrint("We already have " + filePath);
			return;
		}

		System.out.println(hashFile);

		if (isKeyMine(hashFile)) {
			valueMap.put(hashFile, new MyFile(file, sharingType));
			AppConfig.timestampedStandardPrint("File " + filePath + " stored successfully " + "as " + sharingType);
			System.out.println(valueMap);
			System.out.println("# After addition to storage");
			for (Map.Entry<Integer, MyFile> entry : valueMap.entrySet()) {
				System.out.println("storage = " + entry.getKey() + " -- " + entry.getValue() + " -- " + entry.getValue().getOriginalNode());
			}
		} else {
			String nextNodeIp = AppConfig.chordState.getNextNodeIp();
			int nextNodePort = AppConfig.chordState.getNextNodePort();

			Message addFileMsg = new AddFileMessage(
					AppConfig.myServentInfo.getIpAddress(),
					AppConfig.myServentInfo.getListenerPort(),
					nextNodeIp, nextNodePort,
					file,
					sharingType
			);
			MessageUtil.sendMessage(addFileMsg);
		}
	}

	public void removeFileFromStorage(String path) {
		int hash = ChordState.fileHash(path);
		if(valueMap.containsKey(hash)) {
			MyFile fileToRemove = valueMap.get(hash);

			if (fileToRemove == null) {
				AppConfig.timestampedErrorPrint("File does not exist - " + path);
				return;
			}
			valueMap.remove(hash);
			AppConfig.timestampedStandardPrint("Removing file " + path + " from virtual memory. My value map has " + getValueMap().size() + " elements");
		} else {
			Message removeMessage = new RemoveMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
					getNextNodeIp(), getNextNodePort(), path);
			MessageUtil.sendMessage(removeMessage);
		}
	}

	public MyFile viewFile(String path, int port) {
		int hash = ChordState.fileHash(path);

		if(isKeyMine(hash)) {
			MyFile file = valueMap.get(hash);

			if (file == null) {
				AppConfig.timestampedErrorPrint("No files found to pull at path - " + path);
			}
			if(getPrijatelji().contains(port) || file.getSharingType().equalsIgnoreCase("public")){
					return file;
			} else {
					AppConfig.timestampedStandardPrint("Access denied for node " + "localhost:" + port + " because we are not friends and file is private");
			}

		} else {
			String nextNodeIp = getNextNodeIp();
			int nextNodePort = getNextNodePort();

			Message askMessage = new AskViewFileMessage(
					AppConfig.myServentInfo.getIpAddress(),
					port,
					nextNodeIp, nextNodePort,
					AppConfig.myServentInfo.getChordId(), path
			);
			MessageUtil.sendMessage(askMessage);
		}
		return null;
	}
	public void printPulledFiles(MyFile file) {
		AppConfig.timestampedStandardPrint("Printing pulled files");
			System.out.printf("\n----- File Path: %s -----\n", file.getPath());
			System.out.printf("Content: \n%s\n", file.getContent());
	}

	public List<Integer> getPrijatelji() {
		return prijatelji;
	}

	public static boolean isPingPred() {
		return pingPred;
	}

	public static void setPingPred(boolean pingPred) {
		ChordState.pingPred = pingPred;
	}

	public static boolean isPingSuc() {
		return pingSuc;
	}

	public static void setPingSuc(boolean pingSuc) {
		ChordState.pingSuc = pingSuc;
	}


}
