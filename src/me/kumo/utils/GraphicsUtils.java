package me.kumo.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class GraphicsUtils {

    public static void drawStringAJ(Graphics2D g2d, String text, float x, float y, int valign, int justify) {
        drawStringAJ(g2d, text, x, y, valign, justify, new Color(0, 0, 0, 0));
    }

    public static void drawStringAJ(Graphics2D g2d, String text, float x, float y, int valign, int justify, Color bg) {
        FontMetrics fontMetrics = g2d.getFontMetrics();
        Rectangle2D textBounds = fontMetrics.getStringBounds(text, g2d);
        switch (valign) {
            case SwingConstants.TOP -> y += (float) (fontMetrics.getAscent());
            case SwingConstants.CENTER -> y += (float) (fontMetrics.getAscent() / 2 - fontMetrics.getMaxDescent() / 2);
            case SwingConstants.BOTTOM -> y -= (float) (fontMetrics.getMaxDescent());
        }
        switch (justify) {
            case SwingConstants.CENTER -> x -= (float) (textBounds.getWidth() / 2);
            case SwingConstants.RIGHT -> x -= (float) textBounds.getWidth();
        }
        Color c = g2d.getColor();
        g2d.setColor(bg);
        g2d.fill(new Rectangle2D.Double(x + textBounds.getX(),
                y + textBounds.getY(),
                textBounds.getWidth(),
                textBounds.getHeight()));
        g2d.setColor(c);
        g2d.drawString(text, x, y);
    }
}
