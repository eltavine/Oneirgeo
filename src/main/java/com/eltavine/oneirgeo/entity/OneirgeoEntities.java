package com.eltavine.oneirgeo.entity;

import com.eltavine.oneirgeo.Oneirgeo;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** The dreams' own inhabitants; they are placed by {@link Apparitions} and world generation, never by natural spawning. */
public final class OneirgeoEntities {
    public static final EntityType<FacelessEntity> FACELESS = register("faceless",
            FabricEntityType.Builder.createMob(FacelessEntity::new, MobCategory.MONSTER, b -> b.defaultAttributes(FacelessEntity::createAttributes))
                    .sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(10));
    public static final EntityType<StalkerEntity> STALKER = register("stalker",
            FabricEntityType.Builder.createMob(StalkerEntity::new, MobCategory.MONSTER, b -> b.defaultAttributes(StalkerEntity::createAttributes))
                    .sized(0.6F, 2.6F).eyeHeight(2.4F).clientTrackingRange(12));
    public static final EntityType<MimicEntity> MIMIC = register("mimic",
            FabricEntityType.Builder.createMob(MimicEntity::new, MobCategory.MONSTER, b -> b.defaultAttributes(MimicEntity::createAttributes))
                    .sized(0.98F, 0.98F).eyeHeight(0.6F).clientTrackingRange(8));

    public static final EntityType<LifeguardEntity> LIFEGUARD = register("lifeguard",
            FabricEntityType.Builder.createMob(LifeguardEntity::new, MobCategory.MONSTER, b -> b.defaultAttributes(LifeguardEntity::createAttributes))
                    .sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(10));
    public static final EntityType<NurseEntity> NURSE = register("nurse",
            FabricEntityType.Builder.createMob(NurseEntity::new, MobCategory.MONSTER, b -> b.defaultAttributes(NurseEntity::createAttributes))
                    .sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(10));

    private OneirgeoEntities() {
    }

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Oneirgeo.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
    }
}
