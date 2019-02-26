package me.runescapejon.borderprotector;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.service.NucleusRTPService;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;

@Plugin(id = "borderprotector", name = "BorderProtector", description = "Secure your border with this", version = "1.2", dependencies = {
		@Dependency(id = "nucleus", optional = true) })
public class BorderProtector {

	public static BorderProtector instance;
	private BorderProtector plugin;
	private Logger logger;
	private Language configmsg;
	GuiceObjectMapperFactory factory;
	private final File configDirectory;

	@Inject
	public BorderProtector(Logger logger, @ConfigDir(sharedRoot = false) File configDir,
			GuiceObjectMapperFactory factory) {
		this.logger = logger;

		this.configDirectory = configDir;
		this.factory = factory;
		instance = this;
	}

	@Listener
	public void BlockBypass(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
		if (check(player)) {
			player.sendMessage(Text.builder().append(Language.getMessage()).build());
			if (Language.TeleportSpawn) {
				event.setToTransform(event.getFromTransform().setLocation(player.getWorld().getSpawnLocation()));
				Sponge.getScheduler().createTaskBuilder().delayTicks(1)
						.execute(() -> event.setCancelled(true)).submit(instance);
			}
			if (Language.UseNucleusRTP) {
				if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {	     
					NucleusRTPService.RTPOptions options = NucleusAPI.getRTPService().get().options();
					NucleusRTPService Service = NucleusAPI.getRTPService().get();
					for (Task task : Sponge.getScheduler().getScheduledTasks()) {
						Optional<Location<World>> rtp = Service.getLocation(player.getLocation(), player.getWorld(),
								options);
						if (rtp.isPresent()) {
							//System.out.println(rtp);
							event.setToTransform(event.getFromTransform().setLocation(rtp.get()));
							task.cancel();
						}
						if (!rtp.isPresent()) {
							//event.setCancelled(true); It seem i dont need it. It instantly teleport you random.
							//Also that there a weird system that setCancelled(true) effect nucleus
					
						}
					}
				      
			        }
				}
				if (!Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
					logger.info("[BorderProtector] Nucleus not installed");
				}
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
