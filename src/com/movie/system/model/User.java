package com.movie.system.model;

public class User extends Person {
    public User(String id) {
        super(id);
    }

    public User(String name, String id, String password) {
        super(name, id, password);
    }

    @Override
    public boolean login(String id, String password) {
        if (this.id.equals(id) && this.password.equals(password)) {
            System.out.println("[알림] 사용자 '" + name + "'님이 로그인되었습니다.");
            return true;
        }
        System.out.println("[오류] 학번이 일치하지 않습니다.");
        return false;
    }
}
