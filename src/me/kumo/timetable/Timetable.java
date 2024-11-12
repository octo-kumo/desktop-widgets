package me.kumo.timetable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.kumo.Widgets;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class Timetable {
    private static final Gson gson = new GsonBuilder().create();

    public static final Color[][] THEMES = new Color[][]{
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

    public static final class Class {
        private final String name;
        private final String location;
        private final long start;
        private final long end;

        public Class(String name, String location, long start, long end) {
            this.name = name;
            this.location = location;
            this.start = start;
            this.end = end;
        }

        public String name() {
            return name;
        }

        public String location() {
            return location;
        }

        public long start() {
            return start;
        }

        public long end() {
            return end;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            Class that = (Class) obj;
            return Objects.equals(this.name, that.name) &&
                    Objects.equals(this.location, that.location) &&
                    this.start == that.start &&
                    this.end == that.end;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, location, start, end);
        }

        @Override
        public String toString() {
            return "Class[" +
                    "name=" + name + ", " +
                    "location=" + location + ", " +
                    "start=" + start + ", " +
                    "end=" + end + ']';
        }

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
