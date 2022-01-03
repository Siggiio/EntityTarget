package io.siggi.bukkit.entitytarget;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EntityTargetCommand implements CommandExecutor {

	private final EntityTarget plugin;

	public EntityTargetCommand(EntityTarget plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			sender.sendMessage("This command can only be run by in-game players.");
			return true;
		}
		if (player.hasPermission("entitytarget.target")) {
			if (split.length == 1) {
				if (split[0].equalsIgnoreCase("walkhere")) {
					plugin.doWalkHere(player);
				} else if (split[0].equalsIgnoreCase("tphere")) {
					plugin.doTpHere(player);
				} else {
					plugin.target(player, split[0]);
				}
			} else if (split.length == 2) {
				String pn = split[1];
				Player p = plugin.getServer().getPlayer(pn);
				if (p == null) {
					player.sendMessage("Unknown player " + pn);
				}
				if (split[0].equalsIgnoreCase("walkto")) {
					plugin.doWalkTo(player, p);
				} else if (split[0].equalsIgnoreCase("tpto")) {
					plugin.doTpTo(player, p);
				}
			} else {
				plugin.target(player);
			}
		} else {
			player.sendRawMessage(ChatColor.RED + "You don't have permission to do that.");
		}
		return true;
	}
}
