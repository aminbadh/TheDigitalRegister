package com.aminbadh.tdrprofessorslpm.custom;

import java.io.Serializable;
import java.util.ArrayList;

public class MAObject implements Serializable {
    private final boolean register;
    private final ArrayList<Level> levels;

    public MAObject(boolean register, ArrayList<Level> levels) {
        this.register = register;
        this.levels = levels;
    }

    public boolean isRegister() {
        return register;
    }

    public ArrayList<Level> getLevels() {
        return levels;
    }
}
