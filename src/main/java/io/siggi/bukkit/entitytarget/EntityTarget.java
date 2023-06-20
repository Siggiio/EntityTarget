package io.siggi.bukkit.entitytarget;

import io.siggi.cubecore.nms.NMSUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityTarget extends JavaPlugin implements Listener {

	private final WeakHashMap<Player, EntityTargetSession> sessions = new WeakHashMap<>();

	private EntityTargetSession getSession(Player p, boolean create) {
		EntityTargetSession session = sessions.get(p);
		if (session == null && create) {
			sessions.put(p, session = new EntityTargetSession(p));
		}
		return session;
	}

	@Override
	public void onEnable() {
		if (NMSUtil.get() == null) {
			System.err.println("EntityTarget requires a CubeCore version that is fully compatible with the current server version.");
			setEnabled(false);
			return;
		}
		sessions.clear();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		getCommand("entitytarget").setExecutor(new EntityTargetCommand(this));
	}

	@Override
	public void onDisable() {
		sessions.clear();
	}

	public Entity getControlledEntity(Player p) {
		if (!p.hasPermission("entitytarget.target")) {
			return null;
		}
		PlayerInventory inventory = p.getInventory();
		Entity offhand = getControlledEntity(inventory.getItemInOffHand());
		if (offhand != null) {
			return offhand;
		}
		return getControlledEntity(inventory.getItemInMainHand());
	}

	public Entity getControlledEntity(ItemStack stack) {
		if (stack == null) {
			return null;
		}
		Material type = stack.getType();
		if (type != Material.CHARCOAL) {
			return null;
		}
		ItemMeta itemMeta = stack.getItemMeta();
		if (!itemMeta.hasLore()) {
			return null;
		}
		List<String> lore = itemMeta.getLore();
		for (String l : lore) {
			if (l.startsWith("EntityTarget: ")) {
				try {
					UUID uuid = UUID.fromString(l.substring(l.indexOf(": ") + 2));
					return (LivingEntity) Bukkit.getEntity(uuid);
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	public ItemStack createTargetItem(Entity entity) {
		if (entity == null) {
			return null;
		}
		ItemStack stack = new ItemStack(Material.CHARCOAL, 1);
		ItemMeta meta = stack.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		lore.add("EntityTarget: " + entity.getUniqueId().toString());
		meta.setLore(lore);
		EntityType type = entity.getType();
		meta.setDisplayName("Control " + type.getName());
		stack.setItemMeta(meta);
		return stack;
	}

	public void target(Player player) {
		EntityTargetSession session = getSession(player, true);
		if (session.choosingEntityToControl) {
			session.choosingEntityToControl = false;
			player.sendMessage("EntityTarget Cancelled.");
		} else {
			session.choosingEntityToControl = true;
			player.sendMessage("Right click on the entity you wish to give instruction to.");
		}
	}

	public void doWalkHere(Player player) {
		Entity controlledEntity = getControlledEntity(player);
		if (controlledEntity instanceof LivingEntity) {
			LivingEntity entity = (LivingEntity) controlledEntity;
			Location loc = player.getLocation();
			NMSUtil.get().setWalkDestination(entity, loc.getX(), loc.getY(), loc.getZ());
		}
	}

	public void doTpHere(Player player) {
		Entity controlledEntity = getControlledEntity(player);
		if (controlledEntity instanceof LivingEntity) {
			LivingEntity entity = (LivingEntity) controlledEntity;
			entity.teleport(player.getLocation());
		}
	}

	public void doWalkTo(Player player, Player target) {
		Entity controlledEntity = getControlledEntity(player);
		if (controlledEntity instanceof LivingEntity) {
			LivingEntity entity = (LivingEntity) controlledEntity;
			Location loc = target.getLocation();
			NMSUtil.get().setWalkDestination(entity, loc.getX(), loc.getY(), loc.getZ());
		}
	}

	public void doTpTo(Player player, Player target) {
		Entity controlledEntity = getControlledEntity(player);
		if (controlledEntity instanceof LivingEntity) {
			LivingEntity entity = (LivingEntity) controlledEntity;
			entity.teleport(target.getLocation());
		}
	}

	public void walk(LivingEntity entity, Location loc) {
		NMSUtil.get().setWalkDestination(entity, loc.getX(), loc.getY(), loc.getZ());
	}

	public void teleport(LivingEntity entity, Location loc) {
		entity.teleport(loc);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void rightClickBlock(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		Entity controlledEntity = getControlledEntity(event.getPlayer());
		if (!(controlledEntity instanceof LivingEntity)) {
			return;
		}
		LivingEntity le = (LivingEntity) controlledEntity;
		Block relative = event.getClickedBlock().getRelative(event.getBlockFace());
		Location loc = new Location(relative.getWorld(), ((double) relative.getX()) + 0.5, ((double) relative.getY()), ((double) relative.getZ()) + 0.5);
		if (event.getPlayer().isSneaking()) {
			teleport(le, loc);
		} else {
			walk(le, loc);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void rightClickEntity(PlayerInteractEntityEvent event) {
		Player p = event.getPlayer();
		EntityTargetSession session = getSession(p, false);
		if (session == null) {
			return;
		}
		long now = System.currentTimeMillis();
		long timeSinceLastClick = now - session.lastClick;
		if (timeSinceLastClick < 100L) {
			event.setCancelled(true);
			return;
		}

		Entity controlledEntity = EntityTarget.this.getControlledEntity(p);

		Entity targetedEntity = event.getRightClicked();
		if (session.choosingEntityToControl) {
			session.lastClick = now;
			session.choosingEntityToControl = false;
			if (!(targetedEntity instanceof Creature)) {
				p.sendMessage("You can't set a target for that entity!");
				event.setCancelled(true);
			} else {
				p.sendMessage("Right click on the entity that you want this entity to attack, or to make the entity walk somewhere, go somewhere and type /entitytarget walkhere.");
				PlayerInventory inventory = p.getInventory();
				ItemStack targetItem = createTargetItem(targetedEntity);
				ItemStack oldItem;
				if (getControlledEntity(inventory.getItemInOffHand()) != null) {
					oldItem = inventory.getItemInOffHand();
					inventory.setItemInOffHand(targetItem);
				} else {
					oldItem = inventory.getItemInMainHand();
					if (oldItem == null || oldItem.getType() == Material.AIR) {
						oldItem = null;
					}
					inventory.setItemInMainHand(targetItem);
				}
				if (oldItem != null) {
					inventory.addItem(oldItem);
				}
				event.setCancelled(true);
			}
		} else if (controlledEntity instanceof Creature) {
			Creature attacker = (Creature) controlledEntity;
			if (!(targetedEntity instanceof LivingEntity)) {
				p.sendMessage("You can't target a non-living entity!");
				event.setCancelled(true);
			} else {
				p.sendMessage("Target set!");
				LivingEntity defendant = (LivingEntity) targetedEntity;
				event.setCancelled(true);
				attacker.setTarget(defendant);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		sessions.remove(p);
	}
}
