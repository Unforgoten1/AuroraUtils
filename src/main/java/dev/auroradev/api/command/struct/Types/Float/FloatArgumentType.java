package dev.auroradev.api.command.struct.Types.Float;

import dev.auroradev.api.command.Execption.ArgumentParseException;

import dev.auroradev.api.command.struct.ArgumentType;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class FloatArgumentType implements ArgumentType<Float> {
    @Override
    public String getName() {
        return "float";
    }

    @Override
    public Float parse(CommandSender sender, String input) throws ArgumentParseException {
        try {
            return Float.parseFloat(input);
        }catch (NumberFormatException e){
            throw new ArgumentParseException(input);
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        return Collections.emptyList();
    }
}
