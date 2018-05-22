package org.alexdev.kepler.game.commands;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.util.config.Configuration;
import org.alexdev.kepler.util.config.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CommandManager {

    private Map<String[], Command> commands;

    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);
    private static CommandManager instance;

    public CommandManager() {
        commands = new HashMap<>();
        //commands.put(new String[] { "about", "info" }, new AboutCommand());

        log.info("Loaded {} commands", commands.size());
    }

    /**
     * Gets the command.
     *
     * @param commandName the command name
     * @return the command
     */
    private Command getCommand(String commandName) {
        for (Entry<String[], Command> entrySet : commands.entrySet()) {
            for (String name : entrySet.getKey()) {

                if (commandName.equalsIgnoreCase(name)) {
                    return entrySet.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Checks for command.
     *
     * @param entity the player
     * @param message the message
     * @return true, if successful
     */
    public boolean hasCommand(Entity entity, String message) {
        if (message.startsWith(":") && message.length() > 1) {

            String commandName = message.split(":")[1].split(" ")[0];
            Command cmd = getCommand(commandName);

            if (cmd != null) {
                return this.hasCommandPermission(entity, cmd);
            }
        }

        return false;
    }

    /**
     * Checks for command permission.
     *
     * @param entity the player
     * @param cmd the command
     * @return true, if successful
     */
    public boolean hasCommandPermission(Entity entity, Command cmd) {
        if (cmd.getPermissions().length > 0) {

            for (String permission : cmd.getPermissions()) {
                if (entity.hasFuse(permission)) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

    /**
     * Invoke command.
     *
     * @param entity the player
     * @param message the message
     */
    public void invokeCommand(Entity entity, String message) {
        String commandName = message.split(":")[1].split(" ")[0];
        Command cmd = getCommand(commandName);

        String[] args = new String[0];

        if (message.length() > (commandName.length() + 2)) {
            args = message.replace(":" + commandName + " ", "").split(" ");
        }

        if (cmd != null) {
            
            if (args.length < cmd.getArguments().length) {
                if (entity instanceof Player) {
                    Player player = (Player)entity;
                    player.sendMessage(Locale.getInstance().getEntry("player.commands.no.args"));
                } else {
                    System.out.println(Locale.getInstance().getEntry("player.commands.no.args"));
                }
                return;
            }
            
            cmd.handleCommand(entity, message, args);
        }
    }

    /**
     * Gets the commands.
     *
     * @return the commands
     */
    public Map<String[], Command> getCommands() {
        return commands;
    }

    /**
     * Gets the instance
     *
     * @return the instance
     */
    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }

        return instance;
    }
}