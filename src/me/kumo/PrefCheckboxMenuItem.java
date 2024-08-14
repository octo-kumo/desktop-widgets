package me.kumo;

import java.awt.*;
import java.util.function.Consumer;

public class PrefCheckboxMenuItem extends CheckboxMenuItem {
    public PrefCheckboxMenuItem(String name, String prefId, boolean def, Consumer<Boolean> stateListener) {
        super(name, Widgets.prefs.getBoolean(prefId, def));
        addItemListener(e -> {
            Widgets.prefs.putBoolean(prefId, getState());
            if (stateListener != null) stateListener.accept(getState());
        });
        if (stateListener != null) stateListener.accept(getState());
    }
}
