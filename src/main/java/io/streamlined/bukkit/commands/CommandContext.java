package io.streamlined.bukkit.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public class CommandContext {
    @Setter
    private CommandSender sender;
    @Setter
    private Command command;
    @Setter
    private String label;
    @Setter
    private ConcurrentSkipListSet<CommandArgument> args;

    public CommandContext(CommandSender sender, Command command, String label, String... args) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = getArgsFrom(args);
    }

    public CommandArgument getArg(int index) {
        return args.stream().filter(arg -> arg.getIndex() == index).findFirst().orElse(new CommandArgument());
    }

    public String getArgString(int index) {
        return args.stream().filter(arg -> arg.getIndex() == index).findFirst().orElse(new CommandArgument()).getContent();
    }

    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String... args) {
        ConcurrentSkipListSet<CommandArgument> arguments = new ConcurrentSkipListSet<>();
        for (int i = 0; i < args.length; i++) {
            arguments.add(new CommandArgument(i, args[i]));
        }

        return arguments;
    }

    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String string) {
        String[] args = string.split(" ");

        return getArgsFrom(args);
    }
}
