package com.darkkebbit;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DarkKebbitPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DarkKebbitPlugin.class);
		RuneLite.main(args);
	}
}
