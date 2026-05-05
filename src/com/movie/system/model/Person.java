package com.movie.system.model;

public abstract class Person {
    protected String name;
    protected String id;
    protected String password;

    public Person(String id) {
        this.id = id;
        this.name = "이름없음";
        this.password = "";
    }

    public Person(String name, String id, String password) {
        this.name = name;
        this.id = id;
        this.password = password;
    }

    // 로그인 추상 메서드
    public abstract boolean login(String id, String password);

    // Getter & Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
