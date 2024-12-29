package me.kumo;

import me.kumo.timetable.Timetable;
import me.kumo.timetable.TimetableCrawler;
import me.kumo.timetable.TimetablePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WidgetTray {
    public static PopupMenu popupMenu;
    public static PrefStringItem id;
    public static PrefStringItem pin;
    public static PrefCheckboxMenuItem autoLogin;
    private static PrefCheckboxMenuItem vis;


    public static TrayIcon setup(Widgets widget) throws AWTException {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(WidgetTray.class.getResource("/timetable.png"));
            popupMenu = new PopupMenu();
            popupMenu.add("Desktop Widgets");
            popupMenu.add(Version.CURRENT.get());
            popupMenu.addSeparator();
            popupMenu.add(vis = new PrefCheckboxMenuItem("Visibility", "themeVisible", true, widget::setVisible));
            popupMenu.add(new PrefCheckboxMenuItem("Dark Theme", "themeDark", widget.theme, widget::setTheme));
            popupMenu.add(new PrefCheckboxMenuItem("Strong", "themeStrong", widget.strong, widget::setStrong));
            popupMenu.add(new PrefCheckboxMenuItem("Align right", "themeAlignRight", widget.alignRight, widget::setAlignRight));
            popupMenu.add(new PrefCheckboxMenuItem("Always on top", "themeAlwaysTop", false, widget::setAlwaysOnTop));
            popupMenu.addSeparator();
            popupMenu.add(id = new PrefStringItem("Student ID", "studentId", null));
            popupMenu.add(pin = new PrefStringItem("Minerva Pin", "minervaPin", null));
            autoLogin = new PrefCheckboxMenuItem("Auto Login", "autoLogin", false, null);
            popupMenu.add(autoLogin);
            popupMenu.addSeparator();
            MenuItem calendar = new MenuItem("Weekly Calendar");
            MenuItem refresh = new MenuItem("Refresh");
            MenuItem exitItem = new MenuItem("Exit");
            calendar.addActionListener(e -> openCalendar(widget));
            refresh.addActionListener(e -> {
                TimetableCrawler.clearCache();
                Widgets.refresh();
            });
            exitItem.addActionListener(e -> System.exit(0));
            popupMenu.add(calendar);
            popupMenu.add(refresh);
            popupMenu.add(exitItem);
            // Create the tray icon
            TrayIcon trayIcon = getTrayIcon(widget, image);
            tray.add(trayIcon);
            return trayIcon;
        }
        return null;
    }

    public static void openCalendar(Widgets widget) {
        AtomicReference<Timetable> currentCalendar = new AtomicReference<>(TimetableCrawler.getSchedule(Widgets.minerva, 0, false));
        AtomicInteger offset = new AtomicInteger();
        JFrame f = new JFrame("Weekly Calendar");
        JLabel date = new JLabel(currentCalendar.get().date);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.add(new Component() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                TimetablePainter.paintWeek((Graphics2D) g, currentCalendar.get(), Timetable.THEMES[widget.theme ? 1 : 0], getWidth(), getHeight(), false);
            }
        }, BorderLayout.CENTER);
        f.add(new JToolBar() {{
            add(new JButton("Refresh") {{
                addActionListener(e -> {
                    TimetableCrawler.clearCache();
                    Widgets.refresh();
                    f.repaint();
                });
            }});
            add(new JButton("Dark Theme") {{
                addActionListener(e -> {
                    widget.setTheme(!widget.theme);
                    f.repaint();
                });
            }});
            add(new JButton("Previous Week") {{
                addActionListener(e -> {
                    offset.getAndDecrement();
                    currentCalendar.set(TimetableCrawler.getSchedule(Widgets.minerva, offset.get(), false));
                    date.setText(currentCalendar.get().date);
                    f.repaint();
                });
            }});
            add(date);
            add(new JButton("Next Week") {{
                addActionListener(e -> {
                    offset.getAndIncrement();
                    currentCalendar.set(TimetableCrawler.getSchedule(Widgets.minerva, offset.get(), false));
                    date.setText(currentCalendar.get().date);
                    f.repaint();
                });
            }});
            add(new JButton("Close") {{
                addActionListener(e -> f.dispose());
            }});
            setFloatable(false);
        }}, BorderLayout.NORTH);
        f.setSize(800, 600);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private static TrayIcon getTrayIcon(Widgets widget, Image image) {
        TrayIcon trayIcon = new TrayIcon(image, "Desktop Widgets", popupMenu);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    vis.setState(!vis.getState());
                    widget.setVisible(vis.getState());
                }
            }
        });
        return trayIcon;
    }
}
