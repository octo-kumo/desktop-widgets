package me.kumo;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WidgetTray {
    public static PopupMenu popupMenu;
    public static PrefStringItem id;
    public static PrefStringItem pin;
    public static PrefCheckboxMenuItem autoLogin;

    public static TrayIcon setup(Widgets widget) throws AWTException {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(WidgetTray.class.getResource("/timetable.png"));
            popupMenu = new PopupMenu();
            popupMenu.add(new PrefCheckboxMenuItem("Visibility", "themeVisible", true, widget::setVisible));
            popupMenu.add(new PrefCheckboxMenuItem("Dark Theme", "themeDark", widget.theme, widget::setTheme));
            popupMenu.add(new PrefCheckboxMenuItem("Strong", "themeStrong", widget.strong, widget::setStrong));
            popupMenu.add(new PrefCheckboxMenuItem("Align right", "themeAlignRight", widget.alignRight, widget::setAlignRight));
            popupMenu.addSeparator();
            popupMenu.add(id = new PrefStringItem("Student ID", "studentId", null));
            popupMenu.add(pin = new PrefStringItem("Minerva Pin", "minervaPin", null));
            autoLogin = new PrefCheckboxMenuItem("Auto Login", "autoLogin", false, null);
            popupMenu.add(autoLogin);
            popupMenu.addSeparator();
            MenuItem refresh = new MenuItem("Refresh");
            MenuItem exitItem = new MenuItem("Exit");
            refresh.addActionListener(e -> Widgets.refresh());
            exitItem.addActionListener(e -> System.exit(0));
            popupMenu.add(refresh);
            popupMenu.add(exitItem);
            // Create the tray icon
            TrayIcon trayIcon = getTrayIcon(widget, image);
            tray.add(trayIcon);
            return trayIcon;
        }
        return null;
    }

    private static TrayIcon getTrayIcon(Widgets widget, Image image) {
        TrayIcon trayIcon = new TrayIcon(image, "Desktop Widgets", popupMenu);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new PrefCheckboxMenuItem("Visibility", "themeVisible", true, widget::setVisible).setState(!new PrefCheckboxMenuItem("Visibility", "themeVisible", true, widget::setVisible).getState());
                widget.setVisible(new PrefCheckboxMenuItem("Visibility", "themeVisible", true, widget::setVisible).getState());
            }
        });
        return trayIcon;
    }
}
