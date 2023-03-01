package io.siggi.bukkit.entitytarget.nms;

import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class NMSUtil_v1_19_R2 extends NMSUtil<NMSUtil_v1_19_R2> {

	@Override
	public void setWalkDestination(LivingEntity entity, double x, double y, double z) {
		CraftLivingEntity ce = (CraftLivingEntity) entity;
		EntityLiving ent = ce.getHandle();
		if (ent instanceof EntityInsentient) {
			EntityInsentient ei = (EntityInsentient) ent;
			ei.E().a(x, y, z, 1.0D);
		}
	}
}
