package net.foulest.vulture.util.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying a tab completer method.
 * This class is part of the CommandFramework.
 *
 * @author minnymin3
 * <a href="https://github.com/mcardy/CommandFramework">...</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Completer {

    /**
     * The command that this completer completes.
     * If it is a sub command, then its values would be separated by periods.
     * i.e. a command that would be a sub command of test would be 'test.subcommandname'
     */
    String name();

    /**
     * A list of alternate names that the completer is executed under.
     * See name() for details on how names work.
     */
    String[] aliases() default {};
}
