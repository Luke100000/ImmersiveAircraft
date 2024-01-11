package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.LinkedList;
import java.util.List;

public class BBBone extends BBObject {
    public final List<BBObject> children = new LinkedList<>();

    public BBBone(JsonObject element, BBModel model) {
        super(element);

        element.getAsJsonObject().get("children").getAsJsonArray().forEach(child -> {
            if (child.isJsonObject()) {
                this.children.add(new BBBone(child.getAsJsonObject(), model));
            } else {
                BBObject object = model.objects.get(child.getAsString());
                if (object != null) {
                    this.children.add(object);
                }
            }
        });

        Argument x = new Argument("x", 0);
        Expression e = new Expression("2+x", x);
        x.setArgumentValue(7);
        double calculate = e.calculate();
        System.out.printf("ca");
    }
}
