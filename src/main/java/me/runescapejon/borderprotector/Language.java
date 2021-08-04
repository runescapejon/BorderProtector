package me.runescapejon.borderprotector;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Language {
	private static final BorderProtector plugin = BorderProtector.instance;
	@Setting(value = "Message", comment = "Here a option to change the display message to someone bypass the border.")
	public String Message = "&cYou cannot bypass outside the border!";

	public static Text getMessage() {
		return TextSerializers.FORMATTING_CODE.deserialize(plugin.getLangCfg().Message);
	}
	@Setting(value = "TeleportSpawn", comment = "If true have them teleport back to that world spawn point")
	public static boolean TeleportSpawn = true;

	@Setting(value = "Use Nucleus RTP", comment = "If true get nucleus to type rtp for players instead of spawn point")
	public static boolean UseNucleusRTP = false;

}