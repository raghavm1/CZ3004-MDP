package ntu.mdp.group.three.threadedTasks;

import main.Main;
import ntu.mdp.group.three.astarpathfinder.FastestPathThread;
import ntu.mdp.group.three.exploration.ExplorationThread;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class DisableButtonTask extends TimerTask {

    private boolean disable;

    // This task enables/disables all buttons/input fields after the commencing of movement of the robot.
    public DisableButtonTask(boolean disable) {
        this.disable = disable;
    }

    public void run() {
        while (ExplorationThread.isRunning() || FastestPathThread.isRunning()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
        Main.disableAllActionableInputs(this.disable);
    }
}

