package com.movie.system.model;

public abstract class Person {
    protected String id;

    public Person(String id) {
        this.id = id;
    }

    // Getter & Setter
    public String getId() { return id; }
}
