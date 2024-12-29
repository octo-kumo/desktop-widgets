package me.kumo.timetable;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static javax.swing.SwingConstants.*;
import static me.kumo.utils.GraphicsUtils.drawStringAJ;
import static me.kumo.utils.GraphicsUtils.drawStringClipped;

public class TimetablePainter {
    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("hh mm ");
    public static final int W = 400, H = 50;
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static void setup(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static int paint(Graphics2D g, Timetable schedule, Color[] theme, boolean strong, boolean align) {
        setup(g);
        Color bg = theme[3];
        g.setColor(theme[0]);

        if (schedule == null || schedule.classes == null) {
            g.setFont(g.getFont().deriveFont(20f).deriveFont(strong ? Font.BOLD : Font.PLAIN));
            drawStringAJ(g, "No schedule", align ? W - 10 : 10, H - 10, BOTTOM, align ? RIGHT : LEFT, strong ? bg : TRANSPARENT);
            return 20;
        }

        long now = LocalTime.now().toSecondOfDay();
//                LocalTime.ofSecondOfDay((LocalTime.now().toSecondOfDay() * 10000L) % (86400L * 1000_000_000L));
        int day = LocalDate.now().getDayOfWeek().getValue() - 1;
        int dday = day;
        final Timetable.Class[][] timetable = schedule.classes;
        while (timetable[dday].length == 0 || (dday == day && timetable[dday][timetable[dday].length - 1].end() < now))
            dday = (dday + 1) % 7;
        double process = 0;
        Timetable.Class[] classes = timetable[dday];

        g.setFont(g.getFont().deriveFont(20f).deriveFont(strong ? Font.BOLD : Font.PLAIN));
        drawStringAJ(g, DayOfWeek.of(dday + 1) + (dday == day ? " " + LocalTime.ofSecondOfDay(now).format(DISPLAY) :
                dday == (day + 1) % 7 ? " TMR" : "(IN " + (dday + 7 - day) % 7 + " DAYS)"), align ? W - 10 : 10, H - 10, BOTTOM, align ? RIGHT : LEFT, strong ? bg : TRANSPARENT);


        for (int i = 0; i < classes.length; i++) {
            Color c;
            if (dday == day && classes[i].end() < now) {
                c = theme[1];
                process++;
            } else if (dday == day && classes[i].start() < now) {
                c = theme[2];
                process += 1.0 * (now - classes[i].start()) / (classes[i].end() - classes[i].start());
            } else c = theme[0];

            g.setColor(c);
            int y = i * H + H;
//            g.drawRect(0, y, W, H);
            g.setFont(g.getFont().deriveFont(20f).deriveFont(strong ? Font.BOLD : Font.PLAIN));

            drawStringAJ(g, LocalTime.ofSecondOfDay(classes[i].start()).format(DISPLAY), align ? W - 10 : 10, y, TOP, align ? RIGHT : LEFT, strong ? bg : TRANSPARENT);
            drawStringAJ(g, classes[i].name(), align ? W - 75 : 75, y, TOP, align ? RIGHT : LEFT, strong ? bg : TRANSPARENT);

            g.setFont(g.getFont().deriveFont(20f).deriveFont(Font.PLAIN));
            drawStringAJ(g, LocalTime.ofSecondOfDay(classes[i].end()).format(DISPLAY), align ? W - 10 : 10, y + H, BOTTOM, align ? RIGHT : LEFT, strong ? bg : TRANSPARENT);

            g.setColor(new Color(128 << 24 | (c.getRGB() & 0x00FFFFFF), true));
            g.setFont(g.getFont().deriveFont(15f));
            drawStringAJ(g, classes[i].location(), align ? W - 75 : 75, y + H, BOTTOM, align ? RIGHT : LEFT, strong ? bg : TRANSPARENT);
        }
        g.setColor(theme[0]);
        g.fillRect(align ? W - 5 : 0, H, 5, (int) (process * H));
        return classes.length * H + H;
    }

    public static void paintWeek(Graphics2D g, Timetable schedule, Color[] theme, int w, int h, boolean weekends) {
        setup(g);
        g.setColor(theme[0]);
        g.fillRect(0, 0, w, h);
        int top = 50;
        int left = 50;
        if (schedule == null || schedule.classes == null) return;
        int colW = (w - left) / (weekends ? 7 : 5);

        int minH = schedule.minHour;
        int maxH = schedule.maxHour;
        double minuteH = (h - top) * 1d / ((maxH - minH) * 60);
        for (int m = 0; m < (maxH - minH) * 60; m += 30) {
            int y = (int) (m * minuteH) + top;
            g.setColor(transparent(theme[1], 0.5f));
            g.drawLine(left, y, w, y);
            g.setColor(theme[1]);
            g.setFont(g.getFont().deriveFont(15f));
            drawStringAJ(g, LocalTime.of(minH, 0).plusMinutes(m).format(DISPLAY), left - 5, y, CENTER, RIGHT, TRANSPARENT);
        }
        for (int day = 0; day < (weekends ? 7 : 5); day++) {
            g.setColor(theme[1]);
            g.setFont(g.getFont().deriveFont(20f));
            drawStringAJ(g, DayOfWeek.of(day + 1).toString(), day * colW + left + colW / 2f, top / 2f, CENTER, CENTER, TRANSPARENT);
            Timetable.Class[] classes = schedule.classes[day];
            int x = day * colW + left;
            for (Timetable.Class c : classes) {
                int y = (int) ((c.start() - LocalTime.of(minH, 0).toSecondOfDay()) / 60d * minuteH) + top;
                int height = (int) ((c.end() - c.start()) / 60d * minuteH);
                g.setColor(theme[0]);
                g.fillRect(x, y, colW, height);
                g.setColor(theme[1]);
                g.drawRect(x, y, colW, height);
                g.setFont(g.getFont().deriveFont(15f));

                int topM = drawStringAJ(g, LocalTime.ofSecondOfDay(c.start()).format(DISPLAY), x + colW - 5, y + 5, TOP, RIGHT, TRANSPARENT);
                int bottomM = drawStringAJ(g, LocalTime.ofSecondOfDay(c.end()).format(DISPLAY), x + colW - 5, y + height - 5, BOTTOM, RIGHT, TRANSPARENT);

                drawStringClipped(g, c.name(), x + 5, y + 5, TOP, LEFT, TRANSPARENT, colW - 10 - topM);
                drawStringClipped(g, c.location(), x + 5, y + height - 5, BOTTOM, LEFT, TRANSPARENT, colW - 10 - bottomM);

            }
        }
    }

    private static Color transparent(Color color, float v) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (v * 255));
    }
}
