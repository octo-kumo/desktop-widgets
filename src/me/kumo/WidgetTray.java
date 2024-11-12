package me.kumo;

import me.kumo.timetable.Timetable;
import me.kumo.timetable.TimetableCrawler;
import me.kumo.timetable.TimetablePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
                Widgets.prefs.remove("timetable");
                TimetableCrawler._instance = null;
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
        JFrame f = new JFrame("Weekly Calendar");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.add(new Component() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                TimetablePainter.paintWeek((Graphics2D) g, TimetableCrawler._instance, Timetable.THEMES[widget.theme ? 1 : 0], getWidth(), getHeight(), false);
            }
        });
        f.setSize(800, 600);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private static TrayIcon getTrayIcon(Widgets widget, Image image) {
        TrayIcon trayIcon = new TrayIcon(image, "Desktop Widgets", popupMenu);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                vis.setState(!vis.getState());
                widget.setVisible(vis.getState());
            }
        });
        return trayIcon;
    }
}
