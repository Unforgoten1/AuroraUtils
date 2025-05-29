package dev.auroradev.api.command.struct.Types.Boolean;

import dev.auroradev.api.command.Execption.ArgumentParseException;
import dev.auroradev.api.command.struct.ArgumentType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class BooleanArgumentType implements ArgumentType<Boolean> {
    @Override
    public String getName() {
        return "boolean";
    }

    @Override
    public Boolean parse(CommandSender sender, String input) throws ArgumentParseException {
        try{
            return Boolean.parseBoolean(input);
        }catch (Exception e){
            throw new ArgumentParseException(input);
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        List<String> completions = new ArrayList<>();
        completions.add("true");
        completions.add("false");
        return completions;
    }
}
