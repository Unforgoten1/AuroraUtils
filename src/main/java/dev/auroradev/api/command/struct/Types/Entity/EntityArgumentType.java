package dev.auroradev.api.command.struct.Types.Entity;

import dev.auroradev.api.command.Execption.ArgumentParseException;

import dev.auroradev.api.command.struct.ArgumentType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EntityArgumentType implements ArgumentType<EntityType> {
    @Override
    public String getName() {
        return "entityType";
    }

    @Override
    public EntityType parse(CommandSender sender, String input) throws ArgumentParseException {
        if (input == null || input.trim().isEmpty()){
            throw new ArgumentParseException("entity cant be null");
        }
        for (EntityType entity : EntityType.values()){
            if (entity.getName().equalsIgnoreCase(input)){
                return entity;
            }
        }
        throw new ArgumentParseException("Not a valid EntityType");
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        List<String> entities = new ArrayList<>();
        for (EntityType type : EntityType.values()){
            entities.add(type.name());
        }
        return entities;
    }
}
