package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import com.mojang.math.Vector3f;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.Locale;


public class BBKeyframe {
    public BBAnimator.Channel channel;
    public float time;
    public Expression[] expressions;

    public BBKeyframe(JsonObject element) {
        this.channel = BBAnimator.Channel.valueOf(element.getAsJsonPrimitive("channel").getAsString().toUpperCase(Locale.ROOT));
        this.time = element.getAsJsonPrimitive("time").getAsFloat();
        this.expressions = new Expression[3];
        JsonObject point = element.getAsJsonArray("data_points").get(0).getAsJsonObject();
        this.expressions[0] = new Expression(point.getAsJsonPrimitive("x").getAsString(), BBAnimationVariables.getArgumentArray());
        this.expressions[1] = new Expression(point.getAsJsonPrimitive("y").getAsString(), BBAnimationVariables.getArgumentArray());
        this.expressions[2] = new Expression(point.getAsJsonPrimitive("z").getAsString(), BBAnimationVariables.getArgumentArray());
    }

    public Vector3f evaluate() {
        return new Vector3f(
                (float) this.expressions[0].calculate(),
                (float) this.expressions[1].calculate(),
                (float) this.expressions[2].calculate()
        );
    }
}
