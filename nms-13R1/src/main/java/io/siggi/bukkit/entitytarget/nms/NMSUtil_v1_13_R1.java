package io.siggi.bukkit.entitytarget.nms;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.server.v1_13_R1.EntityCreature;
import net.minecraft.server.v1_13_R1.EntityInsentient;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EntityTypes;
import net.minecraft.server.v1_13_R1.MinecraftKey;
import net.minecraft.server.v1_13_R1.PathfinderGoal;
import net.minecraft.server.v1_13_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_13_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_13_R1.RegistryMaterials;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NMSUtil_v1_13_R1 extends NMSUtil<NMSUtil_v1_13_R1> {

	@Override
	public void updateTargets(Player p, Entity entity, String targets) {
		try {
			String[] targetsSplit = targets.split(",");
			ArrayList<EntityTypes> entities = new ArrayList();
			RegistryMaterials<MinecraftKey, EntityTypes<?>> entityTypes = EntityTypes.REGISTRY;
			for (String target : targetsSplit) {
				if (!target.contains(":")) {
					target = "minecraft:" + target;
				}
				for (MinecraftKey key : entityTypes.keySet()) {
					if (key.toString().equalsIgnoreCase(target)) {
						entities.add(entityTypes.get(key));
					}
				}
			}
			Field field = CraftEntity.class.getDeclaredField("entity");
			field.setAccessible(true);
			EntityCreature ent = (EntityCreature) field.get(entity);

			Field goalSelectorField = EntityInsentient.class.getDeclaredField("targetSelector");
			goalSelectorField.setAccessible(true);
			PathfinderGoalSelector targetSelector = (PathfinderGoalSelector) goalSelectorField.get(ent);

			Field listField = PathfinderGoalSelector.class.getDeclaredField("b");
			listField.setAccessible(true);
			Set registeredPathfinders = (Set) listField.get(targetSelector);

			ArrayList<PathfinderGoal> toRemove = new ArrayList<>();
			int x = 1;
			for (Object goalItem : registeredPathfinders) {
				Class goalClass = goalItem.getClass();
				
				Field fieldA = goalClass.getDeclaredField("a");
				fieldA.setAccessible(true);
				PathfinderGoal objectA = (PathfinderGoal) fieldA.get(goalItem);
				
				Field fieldB = goalClass.getDeclaredField("b");
				fieldB.setAccessible(true);
				int objectB = fieldB.getInt(goalItem);

				if (objectA instanceof PathfinderGoalNearestAttackableTarget) {
					toRemove.add(objectA);
					x = objectB;
				}
			}
			for (PathfinderGoal remove : toRemove) {
				targetSelector.a(remove);
			}
			p.sendMessage("Removed " + toRemove.size() + " target" + (toRemove.size() == 1 ? "" : "s") + ".");
			for (int i = 0; i < entities.size(); i++) {
				targetSelector.a(x, new PathfinderGoalNearestAttackableTarget(ent, entities.get(i).c(), 0, true, false, null));
			}
			p.sendMessage("Added " + entities.size() + " target" + (entities.size() == 1 ? "" : "s") + ".");
			p.sendMessage("New goals have been set!");
		} catch (Exception e) {
			e.printStackTrace();
			p.sendMessage("Could not set new goals!");
		}
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
