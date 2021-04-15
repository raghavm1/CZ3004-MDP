package ntu.mdp.group.three.threadedTasks;

import main.Main;

import java.util.TimerTask;

public class MoveRobotTask extends TimerTask {

    private String instruction;
    private int pixels;

    public MoveRobotTask(String instruction, int pixels) {
        this.instruction = instruction;
        this.pixels = pixels;
    }

    @Override
    public void run() { Main.ROBOT.moveTo(pixels, instruction); }
}
