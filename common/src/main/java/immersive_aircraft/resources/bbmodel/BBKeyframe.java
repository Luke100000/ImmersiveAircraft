package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import org.joml.Vector3f;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.Locale;


public class BBKeyframe {
    public final BBAnimator.Channel channel;
    public final float time;
    public final Expression[] expressions;

    public BBKeyframe(JsonObject element) {
        this.channel = BBAnimator.Channel.valueOf(element.getAsJsonPrimitive("channel").getAsString().toUpperCase(Locale.ROOT));
        this.time = element.getAsJsonPrimitive("time").getAsFloat();
        this.expressions = new Expression[3];
        JsonObject point = element.getAsJsonArray("data_points").get(0).getAsJsonObject();
        this.expressions[0] = getExpression(point, "x");
        this.expressions[1] = getExpression(point, "y");
        this.expressions[2] = getExpression(point, "z");
    }

    private static Expression getExpression(JsonObject point, String x) {
        return new Expression(point.getAsJsonPrimitive(x).getAsString().replace("variable.", "variable_"), BBAnimationVariables.getArgumentArray());
    }

    public Vector3f evaluate() {
        return new Vector3f(
                (float) this.expressions[0].calculate(),
                (float) this.expressions[1].calculate(),
                (float) this.expressions[2].calculate()
        );
    }
}
