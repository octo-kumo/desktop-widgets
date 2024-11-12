package me.kumo.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class GraphicsUtils {

    public static void drawStringAJ(Graphics2D g2d, String text, float x, float y, int valign, int justify) {
        drawStringAJ(g2d, text, x, y, valign, justify, new Color(0, 0, 0, 0));
    }

    public static int drawStringAJ(Graphics2D g2d, String text, float x, float y, int valign, int justify, Color bg) {
        FontMetrics fontMetrics = g2d.getFontMetrics();
        Rectangle2D textBounds = fontMetrics.getStringBounds(text, g2d);
        switch (valign) {
            case SwingConstants.TOP:
                y += (float) (fontMetrics.getAscent());
                break;
            case SwingConstants.CENTER:
                y += (float) (fontMetrics.getAscent() / 2 - fontMetrics.getMaxDescent() / 2);
                break;
            case SwingConstants.BOTTOM:
                y -= (float) (fontMetrics.getMaxDescent());
                break;
        }
        switch (justify) {
            case SwingConstants.CENTER:
                x -= (float) (textBounds.getWidth() / 2);
                break;
            case SwingConstants.RIGHT:
                x -= (float) textBounds.getWidth();
                break;
        }
        Color c = g2d.getColor();
        g2d.setColor(bg);
        g2d.fill(new Rectangle2D.Double(x + textBounds.getX(),
                y + textBounds.getY(),
                textBounds.getWidth(),
                textBounds.getHeight()));
        g2d.setColor(c);
        g2d.drawString(text, x, y);
        return (int) textBounds.getWidth();
    }

    public static int drawStringClipped(Graphics2D g2d, String text, float x, float y, int valign,
                                        int justify, Color bg, int maxWidth) {
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // Check if text needs clipping
        String displayText = text;
        Rectangle2D originalBounds = fontMetrics.getStringBounds(text, g2d);
        if (originalBounds.getWidth() > maxWidth) {
            String ellipsis = "...";
            int low = 0;
            int high = text.length();
            int best = 0;

            while (low <= high) {
                int mid = (low + high) / 2;
                String substring = text.substring(0, mid);
                int width = (int) fontMetrics.getStringBounds(substring + ellipsis, g2d).getWidth();

                if (width <= maxWidth) {
                    best = mid;
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }

            displayText = text.substring(0, best) + ellipsis;
        }
        return drawStringAJ(g2d, displayText, x, y, valign, justify, bg);
    }
}
