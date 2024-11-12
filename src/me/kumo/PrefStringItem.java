package me.kumo;

import javax.swing.*;
import java.awt.*;

public class PrefStringItem extends MenuItem {
    private String value;

    public PrefStringItem(String name, String prefId, String def) {
        super(name);
        value = Widgets.prefs.get(prefId, def);
        addActionListener(e -> {
            String res = JOptionPane.showInputDialog(null, "Enter a new value for '" + name + "':", value);
            if (res != null) {
                value = res;
                Widgets.prefs.put(prefId, value);
            }
        });
    }

    public String getValue() {
        return value;
    }
}
