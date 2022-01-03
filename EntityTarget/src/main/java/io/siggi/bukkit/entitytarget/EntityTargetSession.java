package io.siggi.bukkit.entitytarget;

import org.bukkit.entity.Player;

public class EntityTargetSession {

	public EntityTargetSession(Player p) {
		this.p = p;
	}

	private final Player p;

	public Player getPlayer() {
		return p;
	}

	public boolean choosingEntityToControl = false;
	public String targetGoals = null;
	public long lastClick = 0L;
}
