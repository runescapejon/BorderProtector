package me.runescapejon.borderprotector;

import java.io.File;
import org.slf4j.Logger;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;

@Plugin(id = "borderprotector", name = "BorderProtector", description = "Secure your border with this", version = "1.0")
public class BorderProtector {
	public static BorderProtector instance;
	private BorderProtector plugin;
	private Logger logger;
	private Language configmsg;
	GuiceObjectMapperFactory factory;
	private final File configDirectory;

	@Inject
	public BorderProtector(Logger logger, Game game, @ConfigDir(sharedRoot = false) File configDir,
			GuiceObjectMapperFactory factory) {
		this.logger = logger;
		this.configDirectory = configDir;
		this.factory = factory;
		instance = this;
	}

	@Listener
	public void block(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
		if (check(player)) {
			player.sendMessage(Text.builder().append(Language.getMessage()).build());
			// Just a side note that it seem if i use 1 tick i get a "switching phase error"
			// Also that i put 2 that it work out fine in addition i cannot use grab player
			// location and set it that i had to use Transform
			event.setToTransform(event.getFromTransform().setLocation(player.getWorld().getSpawnLocation()));
			Sponge.getScheduler().createTaskBuilder().delayTicks(2).execute(() -> event.setCancelled(true));
		}
	}

	private boolean check(Player player) {
		int diameter = (int) player.getWorld().getWorldBorder().getDiameter() / 2;
		int centerZ = (int) player.getWorld().getWorldBorder().getCenter().getZ();
		int centerX = (int) player.getWorld().getWorldBorder().getCenter().getX();
		int playerX = (int) player.getLocation().getPosition().getX();
		int playerZ = (int) player.getLocation().getPosition().getZ();
		double x = playerX - centerX;
		double z = playerZ - centerZ;
		return ((x > diameter || (-x) > diameter) || (z > diameter || (-z) > diameter));
	}

	public Logger getLogger() {
		return logger;
	}

	public GuiceObjectMapperFactory getFactory() {
		return factory;
	}

	@Listener
	public void onPreInit(GamePreInitializationEvent event) {
		plugin = this;
		loadConfig();
	}

	public Language getLangCfg() {
		return configmsg;
	}
	   @Listener
	    public void onReload(GameReloadEvent event) {
		   loadConfig();
	   }
	public boolean loadConfig() {
		if (!plugin.getConfigDirectory().exists()) {
			plugin.getConfigDirectory().mkdirs();
		}
		try {
			File configFile = new File(getConfigDirectory(), "messages.conf");
			if (!configFile.exists()) {
				configFile.createNewFile();
				logger.info("Creating Config for BorderProtector");
			}
			ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
					.setFile(configFile).build();
			CommentedConfigurationNode config = loader.load(ConfigurationOptions.defaults()
					.setObjectMapperFactory(plugin.getFactory()).setShouldCopyDefaults(true));
			configmsg = config.getValue(TypeToken.of(Language.class), new Language());
			loader.save(config);
			return true;
		} catch (Exception error) {
			getLogger().error("coudnt make the config", error);
			return false;
		}
	}

	public File getConfigDirectory() {
		return configDirectory;
	}
}