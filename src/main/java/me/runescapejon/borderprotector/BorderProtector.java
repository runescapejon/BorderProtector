package me.runescapejon.borderprotector;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;


@Plugin(id = "borderprotector", name = "BorderProtector", description = "Secure your border with this", version = "1.0")
public class BorderProtector {
	@Listener
	public void block(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
		if (check(player)) {
			player.sendMessage(Text.of(TextColors.RED, "You cannot ender-pearl outside the border!"));
			//Just a side note that it seem if i use 1 tick i get a "switching phase error"
			//Also that i put 2 that it work out fine in addition i cannot use grab player location and set it that i had to use Transform
			event.setToTransform(event.getFromTransform().setLocation(player.getWorld().getSpawnLocation()));
			Sponge.getScheduler().createTaskBuilder().delayTicks(2)
				.execute(() -> event.setCancelled(true));
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
}
