package ntu.mdp.group.three.threadedTasks;

import ntu.mdp.group.three.communication.CommunicationSocket;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SenseTask extends Thread {

    public final static int POLL_TIME = 15;
    public static int currentTime = 0;

    private static long runningTime;
    private static SenseTask thread = null;

    private final static AtomicLong endTime = new AtomicLong(15);

    private SenseTask() {
        super("SenseTask");
        // Initialize SenseTask variables.
        SenseTask.resetTimer();

        // multithreading here:
        try {
            join();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        runningTime = System.currentTimeMillis();
        while (runningTime < endTime.get()) runningTime = System.currentTimeMillis();

        // Send S| command.
        if (CommunicationSocket.checkConnection())  CommunicationSocket.getInstance().sendMessage("S|");

        // Reset polling timer.
        SenseTask.resetTimer();
        run();
    }

    public static SenseTask getInstance() {
        if (thread == null) thread = new SenseTask();
        return thread;
    }

    public static void resetTimer() {
        long startTime = System.currentTimeMillis();
        runningTime = startTime;
        endTime.set(startTime + TimeUnit.SECONDS.toMillis(POLL_TIME));
        SenseTask.currentTime = 0;
    }
}
