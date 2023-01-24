package immersive_aircraft.config.configEntries;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FloatConfigEntry {
    float value() default 0;

    float min() default -Float.MAX_VALUE;

    float max() default Float.MAX_VALUE;
}
