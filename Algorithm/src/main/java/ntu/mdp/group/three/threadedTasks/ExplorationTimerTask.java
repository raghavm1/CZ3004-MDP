package ntu.mdp.group.three.threadedTasks;

import ntu.mdp.group.three.communication.CommunicationSocket;

import java.util.concurrent.TimeUnit;

public class ExplorationTimerTask extends Thread {

    private final static int TIME_LIMIT_IN_SECONDS = 330;

    private static long runningTime;
    private static long endTime;

    private static ExplorationTimerTask thread = null;

    private ExplorationTimerTask() {
        super("ExplorationTimerTask");
        long startTime = System.currentTimeMillis();
        runningTime = startTime;
        endTime = startTime + TimeUnit.SECONDS.toMillis(TIME_LIMIT_IN_SECONDS);

        // multithreading here:
        try {
            join();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static ExplorationTimerTask getInstance() {
        if (thread == null) {
            thread = new ExplorationTimerTask();
        }
        return thread;
    }

    @Override
    public void run() {
        runningTime = System.currentTimeMillis();
        while (runningTime < endTime) runningTime = System.currentTimeMillis();

        // this code is now reached.
        CommunicationSocket.getInstance().sendMessage("D|");
        System.exit(0);
    }
}
