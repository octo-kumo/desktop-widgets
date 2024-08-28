package me.kumo.timetable;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static javax.swing.SwingConstants.*;
import static me.kumo.utils.GraphicsUtils.drawStringAJ;

public class TimetablePainter {
    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("hh mm ");
    public static final int W = 400, H = 50;
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static int paint(Graphics2D g, Timetable schedule, Color[] theme, boolean strong, boolean align) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color bg = theme[3];
        g.setColor(theme[0]);

        if (schedule == null) {
            g.setFont(g.getFont().deriveFont(20f).deriveFont(strong ? Font.BOLD : Font.PLAIN));
            drawStringAJ(g, "No schedule", align ? W - 10 : 10, H - 10, BOTTOM, align ? RIGHT : LEFT, strong ? bg : TRANSPARENT);
            return 20;
        }

        long now = LocalTime.now().toSecondOfDay();
//                LocalTime.ofSecondOfDay((LocalTime.now().toSecondOfDay() * 10000L) % (86400L * 1000_000_000L));
        int day = LocalDate.now().getDayOfWeek().getValue() - 1;
        int dday = day;
        final var timetable = schedule.classes;
        while (timetable[dday].length == 0 || (dday == day && timetable[dday][timetable[dday].length - 1].end() < now))
            dday = (dday + 1) % 7;
        double process = 0;
        var classes = timetable[dday];

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
}
