package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.firestring.FireStringManager;
import host.plas.bou.utils.ColorUtils;
import host.plas.bou.utils.EntityUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class FireStringCMD extends SimplifiedCommand {
    public FireStringCMD() {
        super("runfirestring", BukkitOfUtils.getInstance());
    }

    @Override
    public boolean command(CommandContext ctx) {
        if (! ctx.isArgUsable(0)) {
            ctx.sendMessage("&cUsage: /firestring <help|(<fire-string-identifier>) <args...>>");
            return false;
        }

        String action = ctx.getStringArg(0).toLowerCase();
        if (action.equalsIgnoreCase("help")) {
            StringBuilder sb = new StringBuilder("&7&m  &r &c&lFire Strings &6&lHelp &7&m  &r\n");

            sb.append("&eThis command is used to run &cFire Strings&8.\n");
            sb.append("&eYou can find more information about &cFire Strings &eon the wiki&8.\n");
            sb.append("&cBukkitOfUtils &ewiki&8: &7https://wiki.plas.host/bukkitofutils/#fire-strings\n");
            sb.append("&b&oClick anywhere on this message to open the link&8.");

            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://wiki.plas.host/bukkitofutils/#fire-strings");
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, ColorUtils.color("&7Click to open the wiki"));

            ctx.sendMessage(sb.toString(), ColorUtils.colorWithEvents(sb.toString(), clickEvent, hoverEvent));

            return true;
        }

        if (! ctx.isArgUsable(1)) {
            ctx.sendMessage("&cUsage: /firestring <help|(<fire-string-identifier>) <args...>>");
            return false;
        }

        String content = ctx.getArgsAsString();
        FireStringManager.fire(content);

        ctx.sendMessage("&cFire String &efired&8!");
        ctx.sendMessage("&eContent: &b" + content);

        return true;
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        ConcurrentSkipListSet<String> completions = new ConcurrentSkipListSet<>();

        if (ctx.getArgs().size() <= 1) {
            completions.addAll(List.of(
                    "help", "(console)", "(player)", "(consolechat)", "(playerchat)", "(message)", "(title)",
                    "(broadcast)", "(broadcasttitle)"
            ));
        }

        if (ctx.getArgs().size() >= 2) {
            String arg1 = ctx.getStringArg(0).toLowerCase();
            if (! arg1.equalsIgnoreCase("help")) {
                completions.add("args...");
            }
        }

        if (ctx.getArgs().size() == 2) {
            String arg1 = ctx.getStringArg(0).toLowerCase();
            if (arg1.equalsIgnoreCase("(player)") || arg1.equalsIgnoreCase("(playerchat)") ||
                    arg1.equalsIgnoreCase("(message)") || arg1.equalsIgnoreCase("(title)")) {
                completions.addAll(EntityUtils.getOnlinePlayerNames());
            }
        }

        return completions;
    }
}
