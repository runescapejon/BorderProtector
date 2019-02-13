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

}
