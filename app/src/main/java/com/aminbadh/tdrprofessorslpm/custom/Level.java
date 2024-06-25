package com.aminbadh.tdrprofessorslpm.custom;

import java.io.Serializable;

public class Level implements Serializable {
    private String level, docId;

    public Level() {
        // Used in Firebase Cloud Firestore.
    }

    public Level(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
