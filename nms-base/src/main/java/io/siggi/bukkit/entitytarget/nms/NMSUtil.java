package io.siggi.bukkit.entitytarget.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class NMSUtil<N extends NMSUtil> {

	private static NMSUtil util = null;

	public static NMSUtil get() {
		if (util == null) {
			try {
				Class clazz = Class.forName("io.siggi.bukkit.entitytarget.nms.NMSUtil_" + getVersion());
				util = (NMSUtil) clazz.newInstance();
			} catch (Exception e) {
			}
		}
		return util;
	}

	private static String getVersion() {
		String name = Bukkit.getServer().getClass().getName();
		String version = name.substring(name.indexOf(".v") + 1);
		version = version.substring(0, version.indexOf("."));
		return version;
	}
	public abstract void updateTargets(Player p, Entity entity, String targets);
	public abstract void setWalkDestination(LivingEntity entity, double x, double y, double z);
}
