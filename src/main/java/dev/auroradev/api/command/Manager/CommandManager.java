package dev.auroradev.api.command.Manager;


import dev.auroradev.api.command.Command.AuroraCommand;
import dev.auroradev.api.command.Command.AuroraPluginCommand;
import dev.auroradev.api.command.Execption.ArgumentParseException;
import dev.auroradev.api.command.struct.ArgumentTypeRegistry;
import dev.auroradev.api.command.struct.Types.Boolean.BooleanArgumentType;
import dev.auroradev.api.command.struct.Types.Double.DoubleArgumentType;
import dev.auroradev.api.command.struct.Types.Entity.EntityArgumentType;
import dev.auroradev.api.command.struct.Types.Float.FloatArgumentType;
import dev.auroradev.api.command.struct.Types.Integers.IntegerArgumentType;
import dev.auroradev.api.command.struct.Types.Location.LocationArgumentType;
import dev.auroradev.api.command.struct.Types.Player.AdminPlayerArgumentType;
import dev.auroradev.api.command.struct.Types.Player.OnlinePlayerArgumentType;
import dev.auroradev.api.command.struct.Types.Strings.StringArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CommandManager implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Map<String, AuroraCommand> commands;
    private final ArgumentTypeRegistry argumentRegistry;
    private final Logger logger;
    private final SimpleCommandMap commandMap;

    public CommandManager(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.argumentRegistry = new ArgumentTypeRegistry();
        this.logger = plugin.getLogger();
        this.commandMap = getCommandMap();
        registerDefaultArgumentTypes();
        logger.info("CommandManager initialized for plugin: " + plugin.getName());
    }

    private void registerDefaultArgumentTypes() {
        argumentRegistry.registerType("player", new OnlinePlayerArgumentType());
        argumentRegistry.registerType("string", new StringArgumentType());
        argumentRegistry.registerType("integer", new IntegerArgumentType());
        argumentRegistry.registerType("location", new LocationArgumentType());
        argumentRegistry.registerType("float", new FloatArgumentType());
        argumentRegistry.registerType("boolean", new BooleanArgumentType());
        argumentRegistry.registerType("double", new DoubleArgumentType());
        argumentRegistry.registerType("admins", new AdminPlayerArgumentType());
        argumentRegistry.registerType("entityType", new EntityArgumentType());
    }

    public void registerCommand(AuroraCommand command) {
        if (command == null) {
            logger.severe("Cannot register null command for plugin: " + plugin.getName());
            return;
        }
        commands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
        try {
            AuroraPluginCommand pluginCommand = new AuroraPluginCommand(command.getName(), plugin, this, command);
            pluginCommand.setAliases(command.getAliases());
            pluginCommand.setDescription("Managed by aurora command api");
            pluginCommand.setUsage("/" + command.getName() + " " + command.getUsage());

            if (commandMap != null){
                commandMap.register(plugin.getName().toLowerCase(), pluginCommand);
            }
        }catch (Exception e){

        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        logger.info("Processing command: " + command.getName() + " with label: " + label + ", args: " + (args != null ? String.join(", ", args) : "null") + " for plugin: " + plugin.getName());
        AuroraCommand auroraCommand = commands.get(command.getName().toLowerCase());
        if (auroraCommand == null) {
            logger.warning("No AuroraCommand found for: " + command.getName() + " in plugin: " + plugin.getName());
            return false;
        }

        if (!auroraCommand.hasPermission(sender)) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (auroraCommand.isOnCooldown(sender)) {
            long remaining = auroraCommand.getCooldownRemaining(sender);
            sender.sendMessage("§cCommand on cooldown! Wait " + (remaining / 1000) + " seconds.");
            return true;
        }

        try {
            auroraCommand.execute(sender, args);
        } catch (ArgumentParseException e) {
            sender.sendMessage("§c" + e.getMessage());
            logger.warning("Argument parsing error for " + command.getName() + ": " + e.getMessage() + " in plugin: " + plugin.getName());
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred while executing the command!");
            logger.severe("Unexpected error executing " + command.getName() + ": " + e.getMessage() + " in plugin: " + plugin.getName());
            e.printStackTrace();
        }

        auroraCommand.applyCooldown(sender);
        return true;
    }

    public ArgumentTypeRegistry getArgumentRegistry() {
        return argumentRegistry;
    }

    public SimpleCommandMap getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (SimpleCommandMap) commandMapField.get(Bukkit.getServer());
        }catch (NoSuchFieldException | IllegalAccessException e){
            return null;
        }
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}