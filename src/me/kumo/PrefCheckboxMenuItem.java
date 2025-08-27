package me.kumo;

import java.util.function.Consumer;

import dorkbox.systemTray.Checkbox;

public class PrefCheckboxMenuItem extends Checkbox {
    public PrefCheckboxMenuItem(String name, String prefId, boolean def, Consumer<Boolean> stateListener) {
        super(name);
        setCallback(e -> {
            System.out.println("Setting " + prefId + " to " + getChecked());
            Widgets.prefs.putBoolean(prefId, getChecked());
            if (stateListener != null)
                stateListener.accept(getChecked());
        });
        if (stateListener != null)
            stateListener.accept(getChecked());
    }
}
