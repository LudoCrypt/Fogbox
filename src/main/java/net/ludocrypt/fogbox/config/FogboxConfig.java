package net.ludocrypt.fogbox.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "fogbox")
public class FogboxConfig implements ConfigData {

	@ConfigEntry.Gui.RequiresRestart
	public boolean enableClouds = false;

	public static void init() {
		AutoConfig.register(FogboxConfig.class, GsonConfigSerializer::new);
	}

	public static FogboxConfig getInstance() {
		return AutoConfig.getConfigHolder(FogboxConfig.class).getConfig();
	}

}
