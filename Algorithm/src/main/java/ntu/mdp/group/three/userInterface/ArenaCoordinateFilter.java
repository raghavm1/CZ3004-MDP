package ntu.mdp.group.three.userInterface;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class ArenaCoordinateFilter implements UnaryOperator<TextFormatter.Change> {

    private boolean isWidth;
    private final static Pattern HEIGHT_FILTER = Pattern.compile("^[1-9]$|^0[1-9]$|^1[0-9]$|^20$");
    private final static Pattern WIDTH_FILTER = Pattern.compile("^[1-9]$|^0[1-9]$|^1[0-5]$");

    public ArenaCoordinateFilter(boolean isWidth) {
        this.isWidth = isWidth;
    }

    @Override
    public TextFormatter.Change apply(TextFormatter.Change aT) {
        return (isWidth ? WIDTH_FILTER : HEIGHT_FILTER)
                .matcher(aT.getText()).matches()
                ? aT : null;
    }
}
