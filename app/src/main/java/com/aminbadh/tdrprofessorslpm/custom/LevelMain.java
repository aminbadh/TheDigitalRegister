package com.aminbadh.tdrprofessorslpm.custom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class LevelMain implements Serializable {
    private final String resource;
    private final ArrayList<Level> levels;
    private final Professor professor;

    public LevelMain(String resource, ArrayList<Level> levels, Professor professor) {
        // Sort the levels ArrayList alphabetically by it's level String object.
        Collections.sort(levels, (level, t1) -> level.getLevel().compareTo(t1.getLevel()));
        this.resource = resource;
        this.levels = levels;
        this.professor = professor;
    }

    public String getResource() {
        return resource;
    }

    public ArrayList<Level> getLevels() {
        return levels;
    }

    public Professor getProfessor() {
        return professor;
    }
}
