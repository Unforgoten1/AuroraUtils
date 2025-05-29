package dev.auroradev.api.command.struct.Types.Double;

import dev.auroradev.api.command.Execption.ArgumentParseException;

import dev.auroradev.api.command.struct.ArgumentType;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class DoubleArgumentType implements ArgumentType<Double> {
    @Override
    public String getName() {
        return "double";
    }

    @Override
    public Double parse(CommandSender sender, String input) throws ArgumentParseException {
        try {
            return Double.parseDouble(input);
        }catch (NumberFormatException e){
            throw new ArgumentParseException(input);
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        return Collections.emptyList();
    }
}
