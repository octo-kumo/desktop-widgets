package me.kumo.timetable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.kumo.Widgets;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Timetable {
    private static final Gson gson = new GsonBuilder().create();

    private static final Color[][] THEMES = new Color[][]{
            {Color.WHITE, Color.GRAY, Color.CYAN, new Color(0, 0, 0, 100)},
            {Color.BLACK, Color.DARK_GRAY, Color.GREEN, new Color(255, 255, 255, 170)},
    };

    public Class[][] classes;

    public Timetable(Class[][] classes) {
        this.classes = classes;
        System.out.println(this.classes.length);
        System.out.println(Arrays.deepToString(this.classes));
        if (this.classes.length != 7) throw new RuntimeException("Class schedule 2d array does not have 5 rows!");
    }

    public record Class(String name, String location, long start, long end) {
    }

    public static class GUI extends JComponent {
        private Timetable schedule;
        private final Widgets widget;
        private int height;

        public GUI(Widgets widget) {
            this.widget = widget;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int oldH = height;
            height = TimetablePainter.paint((Graphics2D) g, schedule, THEMES[widget.theme ? 0 : 1], widget.strong, widget.alignRight);

            if (height != oldH) {
                revalidate();
                widget.pack();
                widget.alignES();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            if (super.isPreferredSizeSet()) return super.getPreferredSize();
            return new Dimension(TimetablePainter.W, height == 0 ? 200 : height);
        }

        public void setSchedule(Timetable schedule) {
            this.schedule = schedule;
        }
    }

    @Override
    public String toString() {
        return gson.toJson(classes);
    }

    public static Timetable fromString(String json) {
        return new Timetable(gson.fromJson(json, Class[][].class));
    }
}
