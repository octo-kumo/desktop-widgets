package me.kumo.widgets;

import me.kumo.widgets.timetable.Timetable;
import me.kumo.widgets.timetable.TimetableCrawler;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.UnknownHostException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Widgets extends JFrame {
    public static Preferences prefs = Preferences.userNodeForPackage(Widgets.class);
    public static Preferences cache = Preferences.userNodeForPackage(Timetable.class);

    static {
        try {
            Widgets.prefs.sync();
            Widgets.cache.sync();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }
    public static final Minerva minerva = new Minerva();
    private static Timetable.GUI gui;
    public boolean theme = true;
    public boolean strong = true;
    public boolean alignRight = true;

    public Widgets() {
        setType(Type.UTILITY);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        System.out.println(prefs.absolutePath());
    }

    public void alignES() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
        setLocation(
                alignRight ? screenSize.width - insets.right - getWidth() : insets.left,
                screenSize.height - insets.bottom - getHeight());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Widgets widget = new Widgets();
            widget.getContentPane().add(gui = new Timetable.GUI(widget), BorderLayout.CENTER);
            widget.revalidate();
            widget.repaint();
            widget.alignES();
            widget.setVisible(true);
            new Timer(10000, e -> widget.repaint()).start();
            refresh();
            try {
                WidgetTray.setup(widget);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
            gui.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2)
                        WidgetTray.openCalendar(widget);
                }
            });
        });
    }

    public void setTheme(boolean theme) {
        this.theme = theme;
    }

    public void setStrong(boolean strong) {
        this.strong = strong;
    }

    public void setAlignRight(boolean alignRight) {
        this.alignRight = alignRight;
        this.alignES();
    }

    public static void refresh() {
        SwingUtilities.invokeLater(() -> {
            String usern = WidgetTray.autoLogin.getChecked() ? WidgetTray.id.getValue()
                    : JOptionPane.showInputDialog("Your minerva id?");
            if (usern == null)
                return;
            String pass = WidgetTray.autoLogin.getChecked() ? WidgetTray.pin.getValue()
                    : JOptionPane.showInputDialog("Your minerva pin?");
            if (pass == null)
                return;
            try {
                if (minerva.login(usern, pass)) {
                    gui.setSchedule(TimetableCrawler.getSchedule(minerva, 0, false));
                } else {
                    JOptionPane.showMessageDialog(null, "Unable to login\nPossibly due to incorrect login information",
                            "Timetable", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RuntimeException e) {
                if (e.getCause() instanceof UnknownHostException) {
                    Timetable schedule = TimetableCrawler.getSchedule(null);
                    if (schedule != null) {
                        gui.setSchedule(schedule);
                        JOptionPane.showMessageDialog(null, "Using cached data!", "Warn", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, e.getMessage() + "\nOffline!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Runtime Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
