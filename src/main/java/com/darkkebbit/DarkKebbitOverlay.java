package com.darkkebbit;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

@Slf4j
public class DarkKebbitOverlay extends Overlay
{
	private static final Color WARNING_BG = new Color(80, 0, 0, 180);
	private static final Color WARNING_BORDER = new Color(255, 60, 60);
	private static final Color WARNING_TEXT = Color.WHITE;
	private static final int WARNING_PADDING = 8;

	private final Client client;
	private final DarkKebbitPlugin plugin;
	private final DarkKebbitConfig config;

	@Inject
	public DarkKebbitOverlay(Client client, DarkKebbitPlugin plugin, DarkKebbitConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getBushPositions().isEmpty())
		{
			return null;
		}

		if (!plugin.isRingEquipped())
		{
			renderWarning(graphics);
		}

		if (plugin.isTrailActive() && plugin.getDestinationBush() != null)
		{
			renderBushHighlight(graphics, plugin.getDestinationBush());
		}

		return null;
	}

	private void renderBushHighlight(Graphics2D graphics, WorldPoint bushPoint)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, bushPoint);
		if (localPoint == null)
		{
			return;
		}

		Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);
		if (tilePoly == null)
		{
			return;
		}

		Color color = config.highlightColor();
		Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 50);

		OverlayUtil.renderPolygon(graphics, tilePoly, color, fillColor, new BasicStroke(2));

		if (config.showLabel())
		{
			Point textPoint = Perspective.getCanvasTextLocation(
				client, graphics, localPoint, "Dark kebbit", 0);
			if (textPoint != null)
			{
				OverlayUtil.renderTextLocation(graphics, textPoint, "Dark kebbit", Color.GREEN);
			}
		}
	}

	private void renderWarning(Graphics2D graphics)
	{
		String message = "Ring of Pursuit not equipped!";

		FontMetrics fm = graphics.getFontMetrics();
		int textWidth = fm.stringWidth(message);
		int textHeight = fm.getHeight();

		Rectangle canvasBounds = client.getCanvas().getBounds();
		int x = (canvasBounds.width - textWidth) / 2 - WARNING_PADDING;
		int y = 50;

		int boxWidth = textWidth + WARNING_PADDING * 2;
		int boxHeight = textHeight + WARNING_PADDING * 2;

		graphics.setColor(WARNING_BG);
		graphics.fillRect(x, y, boxWidth, boxHeight);
		graphics.setColor(WARNING_BORDER);
		graphics.setStroke(new BasicStroke(2));
		graphics.drawRect(x, y, boxWidth, boxHeight);

		graphics.setColor(WARNING_TEXT);
		graphics.drawString(message, x + WARNING_PADDING, y + WARNING_PADDING + fm.getAscent());
	}
}
