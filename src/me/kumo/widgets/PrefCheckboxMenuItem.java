package me.kumo.widgets;

import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;

import dorkbox.systemTray.Checkbox;

public class PrefCheckboxMenuItem extends Checkbox {
    public PrefCheckboxMenuItem(String name, String prefId, boolean def, Consumer<Boolean> stateListener) {
        super(name);
        setChecked(Widgets.prefs.getBoolean(prefId, def));
        setCallback(e -> {
            System.out.println("Setting " + prefId + " to " + getChecked());
            Widgets.prefs.putBoolean(prefId, getChecked());
            try {
                Widgets.prefs.flush();
            } catch (BackingStoreException e1) {
                throw new RuntimeException(e1);
            }
            if (stateListener != null)
                stateListener.accept(getChecked());
        });
        if (stateListener != null)
            stateListener.accept(getChecked());
    }
}
