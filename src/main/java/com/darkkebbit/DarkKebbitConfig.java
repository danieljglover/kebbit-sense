package com.darkkebbit;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("darkkebbit")
public interface DarkKebbitConfig extends Config
{
	@Alpha
	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight Color",
		description = "Color of the destination bush highlight",
		position = 0
	)
	default Color highlightColor()
	{
		return new Color(0, 255, 0, 100);
	}

	@ConfigItem(
		keyName = "showLabel",
		name = "Show Label",
		description = "Show text label on the destination bush",
		position = 1
	)
	default boolean showLabel()
	{
		return true;
	}
}
