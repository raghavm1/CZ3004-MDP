package ntu.mdp.group.three.exploration;

import ntu.mdp.group.three.config.RobotConfig;
import ntu.mdp.group.three.communication.CommunicationSocket;

import java.util.concurrent.TimeUnit;

public class ExplorationTimer {

    private long startTime = 0;
    private long stopTime = TimeUnit.SECONDS.toMillis(315) / 1000;
    private boolean isRunning = false;

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.isRunning = true;
    }

    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.isRunning = false;
        System.exit(0);
    }

    public long getElapsedTime() {
        long elapsed;
        if (isRunning) elapsed = ((System.currentTimeMillis() - startTime) / 1000);
        else elapsed = ((stopTime - startTime) / 1000);

        this.stopTime = TimeUnit.SECONDS.toMillis(315) / 1000;
        System.out.println("elapsed: " + elapsed);
        System.out.println("stopTime: " + this.stopTime);
        if (elapsed >= this.stopTime) {
            CommunicationSocket.getInstance().sendMessage(RobotConfig.END_TOUR);
            System.exit(0);
        }
        return elapsed;
    }
}
