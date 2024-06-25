package com.aminbadh.tdrprofessorslpm.custom;

import java.io.Serializable;
import java.util.ArrayList;

public class Professor implements Serializable {
    private String firstName, lastName, subject, role;
    private ArrayList<String> levels, level1S, level2Sc, level3M, level3Sc, level4L,
            level4M, level4Sc, level4T;

    public Professor() {
        // Used in Firebase Cloud Firestore.
    }

    public Professor(String firstName, String lastName, String subject, String role,
                     ArrayList<String> levels, ArrayList<String> level1S,
                     ArrayList<String> level2Sc, ArrayList<String> level3M,
                     ArrayList<String> level3Sc, ArrayList<String> level4L,
                     ArrayList<String> level4M, ArrayList<String> level4Sc,
                     ArrayList<String> level4T) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.subject = subject;
        this.role = role;
        this.levels = levels;
        this.level1S = level1S;
        this.level2Sc = level2Sc;
        this.level3M = level3M;
        this.level3Sc = level3Sc;
        this.level4L = level4L;
        this.level4M = level4M;
        this.level4Sc = level4Sc;
        this.level4T = level4T;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSubject() {
        return subject;
    }

    public String getRole() {
        return role;
    }

    public ArrayList<String> getLevels() {
        return levels;
    }

    public ArrayList<String> getLevel1S() {
        return level1S;
    }

    public ArrayList<String> getLevel2Sc() {
        return level2Sc;
    }

    public ArrayList<String> getLevel3M() {
        return level3M;
    }

    public ArrayList<String> getLevel3Sc() {
        return level3Sc;
    }

    public ArrayList<String> getLevel4L() {
        return level4L;
    }

    public ArrayList<String> getLevel4M() {
        return level4M;
    }

    public ArrayList<String> getLevel4Sc() {
        return level4Sc;
    }

    public ArrayList<String> getLevel4T() {
        return level4T;
    }

    public String getDisplayName() {
        return firstName + " " + lastName;
    }
}
