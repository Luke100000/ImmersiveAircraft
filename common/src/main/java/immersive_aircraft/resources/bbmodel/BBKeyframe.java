package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import org.joml.Vector3f;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.Locale;
import java.util.regex.Pattern;


public class BBKeyframe {
    private static final Pattern NUMERIC_LITERAL = Pattern.compile("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?");

    public final BBAnimator.Channel channel;
    public final float time;
    public final Expression[] expressions;
    private final float[] constants = {Float.NaN, Float.NaN, Float.NaN};

    public BBKeyframe(JsonObject element) {
        this.channel = BBAnimator.Channel.valueOf(element.getAsJsonPrimitive("channel").getAsString().toUpperCase(Locale.ROOT));
        this.time = element.getAsJsonPrimitive("time").getAsFloat();
        this.expressions = new Expression[3];
        JsonObject point = element.getAsJsonArray("data_points").get(0).getAsJsonObject();
        this.expressions[0] = getExpression(point, "x", 0);
        this.expressions[1] = getExpression(point, "y", 1);
        this.expressions[2] = getExpression(point, "z", 2);
    }

    private Expression getExpression(JsonObject point, String axis, int index) {
        String value = point.getAsJsonPrimitive(axis).getAsString();
        Expression expression = new Expression(
                value.replace("variable.", "variable_"),
                BBAnimationVariables.getArgumentArray()
        );
        if (expression.checkSyntax()) {
            float result = (float) expression.calculate();
            // Only numeric literals are cached; other expressions may depend on changing state.
            if (NUMERIC_LITERAL.matcher(value.trim()).matches()) {
                constants[index] = result;
            }
        }
        return expression;
    }

    public Vector3f evaluate() {
        return new Vector3f(
                calculate(0),
                calculate(1),
                calculate(2)
        );
    }

    private float calculate(int index) {
        if (!Float.isNaN(constants[index])) {
            return constants[index];
        }
        Expression expression = expressions[index];
        return expression.getSyntaxStatus() ? (float) expression.calculate() : Float.NaN;
    }
}
