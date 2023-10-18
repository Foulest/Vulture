package net.foulest.vulture.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for check information.
 *
 * @author Foulest
 * @project Vulture
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CheckInfo {

    String name();

    CheckType type();

    String description() default "No description provided.";

    boolean enabled() default true;

    boolean punishable() default true;

    String banCommand() default "ban %player% Unfair Advantage";

    int maxViolations() default 10;

    boolean experimental() default false;

    boolean setback() default true;

    boolean acceptsServerPackets() default false;
}
