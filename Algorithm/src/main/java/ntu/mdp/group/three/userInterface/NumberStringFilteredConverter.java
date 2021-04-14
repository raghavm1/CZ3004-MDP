package ntu.mdp.group.three.userInterface;

import javafx.scene.control.TextFormatter;
import javafx.util.converter.NumberStringConverter;

import java.text.ParsePosition;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class NumberStringFilteredConverter extends NumberStringConverter implements UnaryOperator {
    // Note, if needed you can add in appropriate constructors
    // here to set locale, pattern matching or an explicit
    // type of NumberFormat.
    //
    // For more information on format control, see
    //    the NumberStringConverter constructors
    //    DecimalFormat class
    //    NumberFormat static methods for examples.
    // This solution can instead extend other NumberStringConverters if needed
    //    e.g. CurrencyStringConverter or PercentageStringConverter.

    public UnaryOperator<TextFormatter.Change> getFilter() {
        return change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }

            ParsePosition parsePosition = new ParsePosition(0);
            Object object = getNumberFormat().parse( newText, parsePosition );
            if (object == null || parsePosition.getIndex() < newText.length()) {
                return null;
            } else {
                return change;
            }
        };
    }

    private Pattern pattern;

    public final static Pattern HEIGHT_FILTER = Pattern.compile("^[1-9]$|^0[1-9]$|^1[0-9]$|^20$");
    public final static Pattern WIDTH_FILTER = Pattern.compile("^[1-9]$|^0[1-9]$|^1[0-5]$");
    public final static Pattern STEP_FILTER = Pattern.compile("^[1-5]$");
    public final static Pattern NUMBER_ONLY_FILTER = Pattern.compile("[0-9]+");
    public final static Pattern HUNDRED_PERCENT_FILTER = Pattern.compile("^[1-9][0-9]?$|^100$");

    public NumberStringFilteredConverter(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public Object apply(Object o) {
        TextFormatter.Change c = (TextFormatter.Change) o;
        return this.pattern.matcher(c.getText()).matches() ? o : null;
    }
}
