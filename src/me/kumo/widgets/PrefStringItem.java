package me.kumo.widgets;

import java.util.prefs.BackingStoreException;

import javax.swing.*;

import dorkbox.systemTray.MenuItem;

public class PrefStringItem extends MenuItem {
    private String value;

    public PrefStringItem(String name, String prefId, String def) {
        super(name);
        value = Widgets.prefs.get(prefId, def);
        setCallback(e -> {
            System.out.println("Opening dialog for " + prefId);
            String res = JOptionPane.showInputDialog(null, "Enter a new value for '" + name + "':", value);
            if (res != null) {
                value = res;
                System.out.println("Setting " + prefId);
                Widgets.prefs.put(prefId, value);
                try {
                    Widgets.prefs.flush();
                } catch (BackingStoreException e1) {
                    throw new RuntimeException(e1);
                }
            }
        });
    }

    public String getValue() {
        return value;
    }
}
