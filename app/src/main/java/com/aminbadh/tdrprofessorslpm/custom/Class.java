package com.aminbadh.tdrprofessorslpm.custom;

import java.io.Serializable;
import java.util.ArrayList;

public class Class implements Serializable {
    private String classNum, docId;
    private Level level;
    private ArrayList<String> students1, students2;

    public Class() {
        // Used in Firebase Cloud Firestore.
    }

    public String getClassName() {
        return level.getLevel() + classNum;
    }

    public String getClassNum() {
        return classNum;
    }

    public String getDocId() {
        return docId;
    }

    public Level getLevel() {
        return level;
    }

    public ArrayList<String> getStudents1() {
        return students1;
    }

    public ArrayList<String> getStudents2() {
        return students2;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
