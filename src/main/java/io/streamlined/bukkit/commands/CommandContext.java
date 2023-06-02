package io.streamlined.bukkit.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.concurrent.ConcurrentSkipListSet;

public class CommandContext {
    @Getter @Setter
    private CommandSender sender;
    @Getter @Setter
    private Command command;
    @Getter @Setter
    private String label;
    @Getter @Setter
    private ConcurrentSkipListSet<CommandArgument> args;

    public CommandContext(CommandSender sender, Command command, String label, String... args) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = getArgsFrom(args);
    }

    public String getArg(int index) {
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
