package com.aminbadh.tdrprofessorslpm.custom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Class implements Serializable {
    private String docRef;
    private Map<String, String> data;
    private ArrayList<String> students1, students2, absences;

    public Class() {
        // Used in Firebase Cloud Firestore.
    }

    public String getClassName() {
        return data.get("full");
    }

    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String docRef) {
        this.docRef = docRef;
    }

    public ArrayList<String> getAbsences() {
        return absences;
    }

    public ArrayList<String> getStudents1() {
        return students1;
    }

    public ArrayList<String> getStudents2() {
        return students2;
    }

    public Map<String, String> getData() {
        return data;
    }
}
