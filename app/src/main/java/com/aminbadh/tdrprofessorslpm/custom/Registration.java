package com.aminbadh.tdrprofessorslpm.custom;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;

public class Registration implements Serializable {
    private String professorName, professorId, subject,
            fromTime, toTime, group, className, docRef;
    private ArrayList<String> absences;
    private long submitTime;

    public Registration() {
        // Used in Firebase Cloud Firestore.
    }

    public Registration(String professorName, String professorId, String subject,
                        String fromTime, String toTime, String group, String className,
                        ArrayList<String> absences, long submitTime) {
        this.professorName = professorName;
        this.professorId = professorId;
        this.subject = subject;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.group = group;
        this.className = className;
        this.absences = absences;
        this.submitTime = submitTime;
    }

    public String getProfessorName() {
        return professorName;
    }

    public String getProfessorId() {
        return professorId;
    }

    public String getSubject() {
        return subject;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public String getGroup() {
        return group;
    }

    @Exclude
    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String docRef) {
        this.docRef = docRef;
    }

    public ArrayList<String> getAbsences() {
        return absences;
    }

    public long getSubmitTime() {
        return submitTime;
    }

    public String getClassName() {
        return className;
    }
}
