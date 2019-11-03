package io.github.xcube16.iseedragons;

import com.google.common.collect.BiMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mod(modid= ISeeDragons.MODID, version = ISeeDragons.VERSION, acceptableRemoteVersions = "*", name = ISeeDragons.NAME)
public class ISeeDragons {
    public static final String MODID = "iseedragons";
    public static final String NAME = "ISeeDragons";
    public static final String VERSION = "0.3";
    public static final Logger logger = LogManager.getLogger(NAME);


    @Nullable // lazy init
    private Method dragonSetSleeping;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        System.out.println(MODID + " dummy mod loaded");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        try {
            Field regField = EntityRegistry.instance().getClass().getDeclaredField("entityClassRegistrations");
            regField.setAccessible(true);
            BiMap<Class<? extends Entity>, EntityRegistry.EntityRegistration> reg =
                    (BiMap<Class<? extends Entity>, EntityRegistry.EntityRegistration>)regField.get(EntityRegistry.instance());
            for (EntityRegistry.EntityRegistration entity : reg.values()) {
                //logger.info(entity.getRegistryName().toString());
                if (this.isDragon(entity.getRegistryName())) {
                    logger.info("Fixed " + entity.getRegistryName() + " tracking distance");
                    Field rangeField = entity.getClass().getDeclaredField("trackingRange");
                    rangeField.setAccessible(true);
                    rangeField.set(entity, 256);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fix Ice and Fire entity tracking distance", e);
        }
    }

    @SubscribeEvent
    public void livingEntityAttacked(LivingAttackEvent e) {
        //String id = EntityRegistry.getEntry(e.getEntity().getClass()).getRegistryName();
        if (this.isDragon(EntityList.getKey(e.getEntity())) && e.getAmount() > 0.0F) {
            try {
                if (this.dragonSetSleeping == null) {
                    this.dragonSetSleeping = e.getEntity().getClass().getDeclaredMethod("setSleeping");
                    this.dragonSetSleeping.setAccessible(true); // it was public last I checked, but lets just do this anyway
                }
                this.dragonSetSleeping.invoke(e.getEntity(), false);
            } catch (Exception ex) {
                logger.error("Failed to wake up Ice and Fire dragon", ex);
            }
        }
    }

    private boolean isDragon(@Nullable ResourceLocation id) {
        if (id == null) {
            return false;
        }
        return id.getResourceDomain().equals("icdandfire") && (
                id.getResourcePath().equals("icedragon") ||
                id.getResourcePath().equals("firedragon"));
    }

}
