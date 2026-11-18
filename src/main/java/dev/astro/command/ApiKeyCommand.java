package dev.astro.command;

import dev.astro.AstroClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

/** /firedkey <key> sets the Hypixel API key used by Level Head. */
public class ApiKeyCommand extends CommandBase {

    @Override
    public String getCommandName() { return "firedkey"; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/firedkey <your-hypixel-api-key>";
    }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("setapikey", "astrokey");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            if (AstroClient.INSTANCE.getHypixelAPI().hasKey()) {
                String key = AstroClient.INSTANCE.getHypixelAPI().getApiKey();
                sender.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.RED + "[FIRED Client] " + EnumChatFormatting.WHITE +
                        "API key is set (" + key.substring(0, 8) + "...). Use " +
                        EnumChatFormatting.YELLOW + "/firedkey <key>" + EnumChatFormatting.WHITE +
                        " to change it."));
            } else {
                sender.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.RED + "[FIRED Client] " + EnumChatFormatting.RED +
                        "No API key set. Use " + EnumChatFormatting.YELLOW + "/firedkey <key>" +
                        EnumChatFormatting.RED + " — get one with " + EnumChatFormatting.GREEN +
                        "/api new" + EnumChatFormatting.RED + " on Hypixel."));
            }
            return;
        }

        String key = args[0].trim();
        if (key.length() != 36 || key.split("-").length != 5) {
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[FIRED Client] " + EnumChatFormatting.RED +
                    "Invalid key format. Run " + EnumChatFormatting.GREEN + "/api new" +
                    EnumChatFormatting.RED + " on Hypixel to get a valid key."));
            return;
        }

        AstroClient.INSTANCE.getHypixelAPI().setApiKey(key);
        sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.RED + "[FIRED Client] " + EnumChatFormatting.GREEN +
                "API key saved! Level Head will now use the Hypixel API."));
    }
}
