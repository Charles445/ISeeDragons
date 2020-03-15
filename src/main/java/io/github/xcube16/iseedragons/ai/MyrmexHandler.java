package io.github.xcube16.iseedragons.ai;

import java.lang.reflect.Field;

import io.github.xcube16.iseedragons.ISeeDragons;
import io.github.xcube16.iseedragons.StaticConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MyrmexHandler 
{
	//Watches for myrmex spawns and sets their task delay to the config value
	
	Class c_EntityAITasks;
	Field f_tickRate;
	boolean enabled;
	
	public MyrmexHandler()
	{
		enabled = true;
		
		try
		{
			c_EntityAITasks = Class.forName("net.minecraft.entity.ai.EntityAITasks");
			
			//Try both deobfuscated and obfuscated tickRate
			try
			{
				f_tickRate = c_EntityAITasks.getDeclaredField("tickRate");
			}
			catch(Exception e)
			{
				f_tickRate = c_EntityAITasks.getDeclaredField("field_75779_e");
			}
			
			f_tickRate.setAccessible(true);
		}
		catch (Exception e)
		{
			ISeeDragons.logger.error("Failed to setup Myrmex Handler! Disabling!", e);
			enabled = false;
		}
	}
	
	@SubscribeEvent
	public void onMyrmexJoinWorld(EntityJoinWorldEvent event)
	{
		if(enabled && event.getEntity() instanceof EntityLiving)
		{
			EntityLiving entity = (EntityLiving) event.getEntity();
			ResourceLocation id = EntityList.getKey(entity.getClass());
			if(this.isMyrmex(id))
			{
				//Get task AI
				
				EntityAITasks tasks = entity.tasks;
				if(tasks!=null)
				{
					try
					{
						f_tickRate.setInt(tasks, StaticConfig.myrmexalltaskdelay);
					}
					catch (Exception e)
					{
						ISeeDragons.logger.error("Myrmex spawned but tickRate was unable to be modified!", e);
					}
				}
			}
		}
	}
	
	private boolean isMyrmex(ResourceLocation id)
	{
		if (id == null){
            return false;
        }
		
		if(id.getResourceDomain().equals("iceandfire"))
		{
			String resourcePath = id.getResourcePath();
			return resourcePath.equals("myrmex_worker")||
					resourcePath.equals("myrmex_soldier")||
					resourcePath.equals("myrmex_sentinel")||
					resourcePath.equals("myrmex_royal")||
					resourcePath.equals("myrmex_queen");
		}
		else
		{
			return false;
		}
	}
}
