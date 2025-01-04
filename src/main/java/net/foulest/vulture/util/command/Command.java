/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture.util.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define commands for the CommandFramework.
 * This annotation should be applied to methods that represent commands.
 * This class is part of the CommandFramework.
 *
 * @author minnymin3
 * @see <a href="https://github.com/mcardy/CommandFramework">CommandFramework GitHub</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * @return The name of the command. If it is a sub command then its values would be separated by periods.
     * i.e. a command that would be a sub command of test would be 'test.subcommandname'.
     */
    String name();

    /**
     * @return The required permission of the command.
     */
    String permission() default "";

    /**
     * @return The message sent to the player when they do not have permission to execute it.
     */
    String noPermission() default "&cNo permission.";

    /**
     * @return A list of alternate names that the command is executed under.
     * See name() for details on how names work.
     */
    String[] aliases() default {};

    /**
     * @return The description that will appear in the /help of the command.
     */
    String description();

    /**
     * @return The usage that will appear in the /help of the command.
     */
    String usage();

    /**
     * @return If the command is available to players only.
     */
    boolean inGameOnly() default false;
}
