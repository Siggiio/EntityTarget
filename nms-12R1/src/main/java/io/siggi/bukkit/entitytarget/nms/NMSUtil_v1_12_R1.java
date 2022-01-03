package io.siggi.bukkit.entitytarget.nms;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.PathfinderGoal;
import net.minecraft.server.v1_12_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_12_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_12_R1.RegistryMaterials;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NMSUtil_v1_12_R1 extends NMSUtil<NMSUtil_v1_12_R1> {

	@Override
	public void updateTargets(Player p, Entity entity, String targets) {
		try {
			String[] targetsSplit = targets.split(",");
			ArrayList<Class<EntityInsentient>> entities = new ArrayList();
			Field entityTypesField = EntityTypes.class.getDeclaredField("b");
			entityTypesField.setAccessible(true);
			RegistryMaterials<MinecraftKey, Class<? extends net.minecraft.server.v1_12_R1.Entity>> entityTypes = (RegistryMaterials<MinecraftKey, Class<? extends net.minecraft.server.v1_12_R1.Entity>>) entityTypesField.get(null);
			entityTypesField.setAccessible(false);
			for (String target : targetsSplit) {
				if (!target.contains(":")) {
					target = "minecraft:" + target;
				}
				for (MinecraftKey key : entityTypes.keySet()) {
					if (key.toString().equalsIgnoreCase(target)) {
						entities.add((Class<EntityInsentient>) entityTypes.get(key));
					}
				}
			}
			Field field = CraftEntity.class.getDeclaredField("entity");
			field.setAccessible(true);
			EntityCreature ent = (EntityCreature) field.get(entity);
			field.setAccessible(false);
			Field goalSelectorField = EntityInsentient.class.getDeclaredField("targetSelector");
			goalSelectorField.setAccessible(true);
			PathfinderGoalSelector targetSelector = (PathfinderGoalSelector) goalSelectorField.get(ent);
			goalSelectorField.setAccessible(false);
			Field listField = PathfinderGoalSelector.class.getDeclaredField("b");
			listField.setAccessible(true);
			Set registeredPathfinders = (Set) listField.get(targetSelector);
			listField.setAccessible(false);
			ArrayList toRemove = new ArrayList();
			int x = 1;
			for (Object goalItem : registeredPathfinders) {
				Class goalClass = goalItem.getClass();
				Field fieldA = goalClass.getDeclaredField("a");
				Field fieldB = goalClass.getDeclaredField("b");
				fieldA.setAccessible(true);
				fieldB.setAccessible(true);
				Object objectA = fieldA.get(goalItem);
				int objectB = fieldB.getInt(goalItem);
				fieldA.setAccessible(false);
				fieldB.setAccessible(false);
				if (objectA instanceof PathfinderGoalNearestAttackableTarget) {
					toRemove.add(objectA);
					x = objectB;
				}
			}
			for (int i = 0; i < toRemove.size(); i++) {
				targetSelector.a((PathfinderGoal) toRemove.get(i));
			}
			p.sendMessage("Removed " + toRemove.size() + " target" + (toRemove.size() == 1 ? "" : "s") + ".");
			for (int i = 0; i < entities.size(); i++) {
				//targetSelector.a(x, new PathfinderGoalNearestAttackableTarget(ent, (Class) entities.get(i), 0, true)); in 1.7 code
				targetSelector.a(x, new PathfinderGoalNearestAttackableTarget(ent, entities.get(i), 0, true, false, null));
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
