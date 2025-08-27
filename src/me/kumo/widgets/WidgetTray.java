package me.kumo.widgets;

import me.kumo.widgets.timetable.Timetable;
import me.kumo.widgets.timetable.TimetableCrawler;
import me.kumo.widgets.timetable.TimetablePainter;

import javax.swing.*;
import java.awt.Component;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import dorkbox.systemTray.Menu;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.Separator;
import dorkbox.systemTray.SystemTray;

public class WidgetTray {
    public static PrefStringItem id;
    public static PrefStringItem pin;
    public static PrefCheckboxMenuItem autoLogin;

    public static SystemTray setup(Widgets widget) throws AWTException {
        SystemTray tray = SystemTray.get();
        if (tray == null) {
            throw new RuntimeException("Unable to load SystemTray!");
        }
        Image image = Toolkit.getDefaultToolkit().getImage(WidgetTray.class.getResource("/timetable.png"));
        Menu menu = tray.getMenu();
        menu.add(new MenuItem("Desktop Widgets"));
        menu.add(new MenuItem(Version.CURRENT.get()));
        menu.add(new JSeparator());
        menu.add(new PrefCheckboxMenuItem("Visibility", "themeVisible", true, widget::setVisible));
        menu.add(new PrefCheckboxMenuItem("Dark Theme", "themeDark", widget.theme, widget::setTheme));
        menu.add(new PrefCheckboxMenuItem("Strong", "themeStrong", widget.strong, widget::setStrong));
        menu.add(new PrefCheckboxMenuItem("Align right", "themeAlignRight", widget.alignRight,
                widget::setAlignRight));
        menu.add(new PrefCheckboxMenuItem("Always on top", "themeAlwaysTop", false, widget::setAlwaysOnTop));
        menu.add(new JSeparator());
        menu.add(id = new PrefStringItem("Student ID", "studentId", null));
        menu.add(pin = new PrefStringItem("Minerva Pin", "minervaPin", null));
        autoLogin = new PrefCheckboxMenuItem("Auto Login", "autoLogin", false, null);
        menu.add(autoLogin);
        menu.add(new Separator());
        MenuItem calendar = new MenuItem("Weekly Calendar");
        MenuItem refresh = new MenuItem("Refresh");
        MenuItem exitItem = new MenuItem("Exit");
        calendar.setCallback((e) -> openCalendar(widget));

        refresh.setCallback(e -> {
            TimetableCrawler.clearCache();
            Widgets.refresh();
        });
        exitItem.setCallback(e -> System.exit(0));
        menu.add(calendar);
        menu.add(refresh);
        menu.add(exitItem);
        tray.setImage(image);
        return tray;
    }

    public static void openCalendar(Widgets widget) {
        AtomicReference<Timetable> currentCalendar = new AtomicReference<>(
                TimetableCrawler.getSchedule(Widgets.minerva, 0, false));
        AtomicInteger offset = new AtomicInteger();
        JFrame f = new JFrame("Weekly Calendar");
        JLabel date = new JLabel(currentCalendar.get().date);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.add(new Component() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                TimetablePainter.paintWeek((Graphics2D) g, currentCalendar.get(),
                        Timetable.THEMES[widget.theme ? 1 : 0], getWidth(), getHeight(), false);
            }
        }, BorderLayout.CENTER);
        f.add(new JToolBar() {
            {
                add(new JButton("Refresh") {
                    {
                        addActionListener(e -> {
                            TimetableCrawler.clearCache();
                            Widgets.refresh();
                            f.repaint();
                        });
                    }
                });
                add(new JButton("Dark Theme") {
                    {
                        addActionListener(e -> {
                            widget.setTheme(!widget.theme);
                            f.repaint();
                        });
                    }
                });
                add(new JButton("Previous Week") {
                    {
                        addActionListener(e -> {
                            offset.getAndDecrement();
                            currentCalendar.set(TimetableCrawler.getSchedule(Widgets.minerva, offset.get(), false));
                            date.setText(currentCalendar.get().date);
                            f.repaint();
                        });
                    }
                });
                add(date);
                add(new JButton("Next Week") {
                    {
                        addActionListener(e -> {
                            offset.getAndIncrement();
                            currentCalendar.set(TimetableCrawler.getSchedule(Widgets.minerva, offset.get(), false));
                            date.setText(currentCalendar.get().date);
                            f.repaint();
                        });
                    }
                });
                add(new JButton("Close") {
                    {
                        addActionListener(e -> f.dispose());
                    }
                });
                setFloatable(false);
            }
        }, BorderLayout.NORTH);
        f.setSize(800, 600);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
