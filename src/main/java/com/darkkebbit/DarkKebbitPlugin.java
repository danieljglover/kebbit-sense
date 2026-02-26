package com.darkkebbit;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "KebbitSense",
	description = "Sense exactly where the dark kebbit is hiding — skip the trail, go straight to the bush",
	tags = {"hunter", "kebbit", "dark", "tracking", "piscatoris"}
)
public class DarkKebbitPlugin extends Plugin
{
	static final int BUSH_OBJECT_ID = 19428;
	static final int VARBIT_TRAILS_USED = 2984;
	static final int VARBIT_DESTINATION = 2994;
	static final int DARK_KEBBIT_TRAIL_TYPE = 2;
	static final int RING_OF_PURSUIT_ID = 21126;

	@Inject
	private Client client;

	@Inject
	private DarkKebbitConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DarkKebbitOverlay overlay;

	@Getter
	private final List<WorldPoint> bushPositions = new ArrayList<>();

	@Getter
	private WorldPoint destinationBush;

	@Getter
	private boolean trailActive;

	@Getter
	private boolean ringEquipped;

	@Provides
	DarkKebbitConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DarkKebbitConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		bushPositions.clear();
		destinationBush = null;
		trailActive = false;
		ringEquipped = false;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			bushPositions.clear();
			destinationBush = null;
			trailActive = false;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (bushPositions.isEmpty())
		{
			scanBushes();
		}

		checkRingEquipped();

		int trailsUsed = client.getVarbitValue(VARBIT_TRAILS_USED);
		int destination = client.getVarbitValue(VARBIT_DESTINATION);

		if (trailsUsed == DARK_KEBBIT_TRAIL_TYPE && destination >= 1 && destination <= bushPositions.size())
		{
			trailActive = true;
			destinationBush = bushPositions.get(destination - 1);
		}
		else
		{
			trailActive = false;
			destinationBush = null;
		}
	}

	private void scanBushes()
	{
		Tile[][][] tiles = client.getScene().getTiles();
		int plane = client.getPlane();

		if (tiles[plane] == null)
		{
			return;
		}

		List<WorldPoint> found = new ArrayList<>();

		for (Tile[] row : tiles[plane])
		{
			if (row == null)
			{
				continue;
			}

			for (Tile tile : row)
			{
				if (tile == null)
				{
					continue;
				}

				GroundObject groundObj = tile.getGroundObject();
				if (groundObj != null && groundObj.getId() == BUSH_OBJECT_ID)
				{
					found.add(groundObj.getWorldLocation());
				}

				for (GameObject obj : tile.getGameObjects())
				{
					if (obj != null && obj.getId() == BUSH_OBJECT_ID)
					{
						found.add(obj.getWorldLocation());
					}
				}
			}
		}

		found.sort(Comparator.comparingInt(WorldPoint::getY).reversed().thenComparingInt(WorldPoint::getX));
		bushPositions.addAll(found);

		if (!bushPositions.isEmpty())
		{
			log.info("Found {} dark kebbit bushes", bushPositions.size());
		}
	}

	private void checkRingEquipped()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			ringEquipped = false;
			return;
		}

		for (Item item : equipment.getItems())
		{
			if (item.getId() == RING_OF_PURSUIT_ID)
			{
				ringEquipped = true;
				return;
			}
		}

		ringEquipped = false;
	}
}
