package io.siggi.bukkit.entitytarget.nms;

import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NMSUtil_v1_17_R1 extends NMSUtil<NMSUtil_v1_17_R1> {

	@Override
	public void updateTargets(Player p, Entity entity, String targets) {
		p.sendMessage("Setting targets not supported on this version of Minecraft.");
	}

	@Override
	public void setWalkDestination(LivingEntity entity, double x, double y, double z) {
		CraftLivingEntity ce = (CraftLivingEntity) entity;
		EntityLiving ent = ce.getHandle();
		if (ent instanceof EntityInsentient) {
			EntityInsentient ei = (EntityInsentient) ent;
			ei.getNavigation().a(x, y, z, 1.0D);
		}
	}
}
