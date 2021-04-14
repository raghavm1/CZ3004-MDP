package ntu.mdp.group.three.userInterface;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.text.ParsePosition;
import java.util.function.UnaryOperator;

public class NumberConverterFieldTest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        TextField numberField = new TextField();
        NumberStringFilteredConverter converter = new NumberStringFilteredConverter();
        final TextFormatter<Number> formatter = new TextFormatter<>(
                converter,
                0,
                converter.getFilter()
        );

        numberField.setTextFormatter(formatter);

        formatter.valueProperty().addListener((observable, oldValue, newValue) ->
                System.out.println(newValue)
        );

        stage.setScene(new Scene(numberField));
        stage.show();
    }

    class NumberStringFilteredConverter extends NumberStringConverter {
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

                ParsePosition parsePosition = new ParsePosition( 0 );
                Object object = getNumberFormat().parse( newText, parsePosition );
                if ( object == null || parsePosition.getIndex() < newText.length()) {
                    return null;
                } else {
                    return change;
                }
            };
        }
    }
}

