package com.movie.system.model;

public class Admin extends Person {
    public Admin(String id) {
        super(id);
        this.name = "관리자";
    }

    public Admin(String name, String id, String password) {
        super(name, id, password);
    }

    @Override
    public boolean login(String id, String password) {
        if (this.id.equals(id) && this.password.equals(password)) {
            System.out.println("[알림] 관리자 '" + name + "'님이 로그인되었습니다.");
            return true;
        }
        System.out.println("[오류] 학번이 일치하지 않습니다.");
        return false;
    }

    // 관리자 전용 기능 (예: 영화 추가)
    public void addMovie(Movie movie) {
        System.out.println("[관리자] 새로운 영화를 추가했습니다: " + movie.getTitle());
    }
}
