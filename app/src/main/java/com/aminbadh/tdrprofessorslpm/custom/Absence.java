package com.aminbadh.tdrprofessorslpm.custom;

import java.util.ArrayList;

public class Absence {
    private ArrayList<Student> absences = new ArrayList<>();
    private long date;
    private ArrayList<String> f8t9 = new ArrayList<>();
    private ArrayList<String> f9t10 = new ArrayList<>();
    private ArrayList<String> f10t11 = new ArrayList<>();
    private ArrayList<String> f11t12 = new ArrayList<>();
    private ArrayList<String> f12t13 = new ArrayList<>();
    private ArrayList<String> f13t14 = new ArrayList<>();
    private ArrayList<String> f14t15 = new ArrayList<>();
    private ArrayList<String> f15t16 = new ArrayList<>();
    private ArrayList<String> f16t17 = new ArrayList<>();
    private ArrayList<String> f17t18 = new ArrayList<>();

    public Absence() {
    }

    public Absence(ArrayList<Student> absences, long date, ArrayList<String> f8t9,
                   ArrayList<String> f9t10, ArrayList<String> f10t11, ArrayList<String> f11t12,
                   ArrayList<String> f12t13, ArrayList<String> f13t14, ArrayList<String> f14t15,
                   ArrayList<String> f15t16, ArrayList<String> f16t17, ArrayList<String> f17t18) {
        this.absences = absences;
        this.date = date;
        this.f8t9 = f8t9;
        this.f9t10 = f9t10;
        this.f10t11 = f10t11;
        this.f11t12 = f11t12;
        this.f12t13 = f12t13;
        this.f13t14 = f13t14;
        this.f14t15 = f14t15;
        this.f15t16 = f15t16;
        this.f16t17 = f16t17;
        this.f17t18 = f17t18;
    }

    public ArrayList<Student> getAbsences() {
        return absences;
    }

    public long getDate() {
        return date;
    }

    public ArrayList<String> getF8t9() {
        return f8t9;
    }

    public ArrayList<String> getF9t10() {
        return f9t10;
    }

    public ArrayList<String> getF10t11() {
        return f10t11;
    }

    public ArrayList<String> getF11t12() {
        return f11t12;
    }

    public ArrayList<String> getF12t13() {
        return f12t13;
    }

    public ArrayList<String> getF13t14() {
        return f13t14;
    }

    public ArrayList<String> getF14t15() {
        return f14t15;
    }

    public ArrayList<String> getF15t16() {
        return f15t16;
    }

    public ArrayList<String> getF16t17() {
        return f16t17;
    }

    public ArrayList<String> getF17t18() {
        return f17t18;
    }

    public void setAbsences(ArrayList<Student> absences) {
        this.absences = absences;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setF8t9(ArrayList<String> f8t9) {
        this.f8t9 = f8t9;
    }

    public void setF9t10(ArrayList<String> f9t10) {
        this.f9t10 = f9t10;
    }

    public void setF10t11(ArrayList<String> f10t11) {
        this.f10t11 = f10t11;
    }

    public void setF11t12(ArrayList<String> f11t12) {
        this.f11t12 = f11t12;
    }

    public void setF12t13(ArrayList<String> f12t13) {
        this.f12t13 = f12t13;
    }

    public void setF13t14(ArrayList<String> f13t14) {
        this.f13t14 = f13t14;
    }

    public void setF14t15(ArrayList<String> f14t15) {
        this.f14t15 = f14t15;
    }

    public void setF15t16(ArrayList<String> f15t16) {
        this.f15t16 = f15t16;
    }

    public void setF16t17(ArrayList<String> f16t17) {
        this.f16t17 = f16t17;
    }

    public void setF17t18(ArrayList<String> f17t18) {
        this.f17t18 = f17t18;
    }
}
