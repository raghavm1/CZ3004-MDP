package ntu.mdp.group.three.robot;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ntu.mdp.group.three.config.ArenaConfig;
import ntu.mdp.group.three.config.RobotConfig;
import ntu.mdp.group.three.config.Directions;
import ntu.mdp.group.three.config.SimulatorConfig;

import java.io.Serializable;

public class RobotImage extends Label implements Serializable {

    private int height, width;

	public RobotImage(String path, int width, int height) {
		this.height = height;
		this.width = width;
		this.setImage(path);
	}

	public void setImage(String path) {
		this.setVisible(false);
        Image imageIcon = new Image(path, width, height, true, true);
		Platform.runLater(() -> this.setGraphic(new ImageView(imageIcon)));
		this.setVisible(true);
	}

	public void moveTo(int pixels, String instruction) {
		switch (instruction.toUpperCase()) {
			case Directions.RIGHT_DIRECTION:
				this.moveRight(pixels);
				break;
			case Directions.LEFT_DIRECTION:
				this.moveLeft(pixels);
				break;
			case Directions.UP_DIRECTION:
				this.moveUp(pixels);
				break;
			case Directions.DOWN_DIRECTION:
				this.moveDown(pixels);
				break;
		}
	}

	public void moveRight(int pixels) {
		int x = (int) this.getTranslateX();
		if (x + pixels < ArenaConfig.WIDTH - (ArenaConfig.GRID_WIDTH * 3 - SimulatorConfig.ROBOT_WIDTH) / 2) {
			this.setTranslateX(x + pixels);
		}
	}

	public void moveLeft(int pixels) {
		int x = (int) this.getTranslateX();
		if (x - pixels >= ArenaConfig.MARGIN_LEFT + (ArenaConfig.GRID_WIDTH * 3 - SimulatorConfig.ROBOT_WIDTH) / 2) {
			this.setTranslateX(x - pixels);
		}
	}

	public void moveUp(int pixels) {
		int y = (int) this.getTranslateY();
		if (y - pixels >= ArenaConfig.MARGIN_TOP + (ArenaConfig.GRID_HEIGHT * 3 - SimulatorConfig.ROBOT_HEIGHT) / 2) {
			this.setTranslateY(y - pixels);
		}
	}

	public void moveDown(int pixels) {
		int y = (int) this.getTranslateY();
		if (y  + pixels < ArenaConfig.HEIGHT - (ArenaConfig.GRID_HEIGHT * 3 - SimulatorConfig.ROBOT_HEIGHT) / 2) {
			this.setTranslateY(y + pixels);
		}
	}

}
