package com.aminbadh.tdrprofessorslpm.custom;

public class Student {
    private String name, num, classN;

    public Student() {
    }

    public Student(String name, String num, String classN) {
        this.name = name;
        this.num = num;
        this.classN = classN;
    }

    public String getName() {
        return name;
    }

    public String getNum() {
        return num;
    }

    public String getClassN() {
        return classN;
    }
}
