package app;

import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.Arrays;
import java.util.List;

public class BuddyService implements Runnable, Cancellable {

    private volatile boolean working = true;

    private final Thread backupThread;

    public BuddyService() {
        backupThread = new Thread(this::periodicBackup);
        backupThread.start();
    }

    @Override
    public void run()  {
        while (working) {

            try{
                Thread.sleep(2000);

                while (AppConfig.chordState.getAllNodeInfo().size() > 1) {
                    Thread.sleep(2000);
                    //Saljemo dve PING poruke za predecessora i successora
                    if (AppConfig.chordState.getPredecessor() != null && AppConfig.chordState.getSuccessorTable()[0] != null) {
                        Message pingMsgPred = new PingMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                                AppConfig.chordState.getPredecessor().getIpAddress(),
                                AppConfig.chordState.getPredecessor().getListenerPort(), "Pred");
                        AppConfig.timestampedStandardPrint("Sending PING message to Predecessor" + pingMsgPred);
                        MessageUtil.sendMessage(pingMsgPred);

                        AppConfig.chordState.setPingPred(true);

                        Message pingMsgSuc = new PingMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                                AppConfig.chordState.getSuccessorTable()[0].getIpAddress(),
                                AppConfig.chordState.getSuccessorTable()[0].getListenerPort(), "Suc");
                        AppConfig.timestampedStandardPrint("Sending PING message to Successor" + pingMsgSuc);
                        MessageUtil.sendMessage(pingMsgSuc);

                        AppConfig.chordState.setPingSuc(true);

                        Thread.sleep(AppConfig.WEAK_TIMEOUT);

                        if (AppConfig.chordState.isPingPred()) {
                            AppConfig.timestampedStandardPrint("Sending PING message to Predecessor after WEAK_TIMEOUT" + pingMsgPred);
                            MessageUtil.sendMessage(pingMsgPred);
                            AppConfig.chordState.setPingPred(true);

                            Thread.sleep(AppConfig.STRONG_TIMEOUT);

                            if (AppConfig.chordState.isPingPred()) {
                                ServentInfo failedServent = AppConfig.chordState.getPredecessor();
                                if(AppConfig.chordState.getBackupMap().containsKey(AppConfig.chordState.getPredecessor().getListenerPort())) {
                                   AppConfig.chordState.addToValueMap(AppConfig.chordState.getBackupMap().get(AppConfig.chordState.getPredecessor().getListenerPort()));
                                   AppConfig.timestampedStandardPrint("My value map is updated. I have information of failed node with port " + AppConfig.chordState.getPredecessor().getListenerPort());

                                }
                                AppConfig.chordState.handleNodeFailed(AppConfig.chordState.getPredecessor());

                                UpdateFailedMessage updateMessage = new UpdateFailedMessage(
                                        AppConfig.myServentInfo.getIpAddress(),
                                        AppConfig.myServentInfo.getListenerPort(),
                                        AppConfig.chordState.getSuccessorTable()[0].getIpAddress(),
                                        AppConfig.chordState.getSuccessorTable()[0].getListenerPort(),
                                        failedServent
                                );
                                MessageUtil.sendMessage(updateMessage);

                                AppConfig.timestampedStandardPrint("STRONG TIME OUT PREDECESSOR");
                            }

                        }
                        if (AppConfig.chordState.isPingSuc()) {
                            AppConfig.timestampedStandardPrint("Sending PING message to Successor after WEAK_TIMEOUT" + pingMsgSuc);
                            MessageUtil.sendMessage(pingMsgSuc);
                            AppConfig.chordState.setPingSuc(true);

                            Thread.sleep(AppConfig.STRONG_TIMEOUT);

                            if (AppConfig.chordState.isPingSuc()) {
                                ServentInfo failedServent = AppConfig.chordState.getSuccessorTable()[0];
                                AppConfig.chordState.handleNodeFailed(failedServent);

                                UpdateFailedMessage updateMessage = new UpdateFailedMessage(
                                        AppConfig.myServentInfo.getIpAddress(),
                                        AppConfig.myServentInfo.getListenerPort(),
                                        AppConfig.chordState.getSuccessorTable()[0].getIpAddress(),
                                        AppConfig.chordState.getSuccessorTable()[0].getListenerPort(),
                                        failedServent
                                );
                                MessageUtil.sendMessage(updateMessage);
                                AppConfig.timestampedStandardPrint("STRONG TIME OUT SUCCESSOR");
                            }
                        }
                    }
                }
            }catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void periodicBackup() {
        while (working) {
            try {
                performBackup();
                Thread.sleep(5000); // 5 sekundi
            } catch (InterruptedException e) {
                if (!working) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    private void performBackup() {
        ServentInfo[] successors = AppConfig.chordState.getSuccessorTable();
        List<ServentInfo> uniqueSuccessors = Arrays.stream(successors).distinct().toList();
        if (uniqueSuccessors != null) {
            for (ServentInfo successor : uniqueSuccessors) {
                if (successor != null) {
                        BackupMessage backupMsg = new BackupMessage(
                                AppConfig.myServentInfo.getIpAddress(),
                                AppConfig.myServentInfo.getListenerPort(),
                                successor.getIpAddress(),
                                successor.getListenerPort(),
                                AppConfig.chordState.getValueMap()
                        );
                        MessageUtil.sendMessage(backupMsg);
                    }
            }
        }
    }
    @Override
    public void stop() {
        this.working = false;
        backupThread.interrupt();
    }

}
