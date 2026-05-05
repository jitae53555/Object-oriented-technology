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

    // 관리자 전용 기능 1: 영화 교체
    public String replaceMovie(String newTitle) {
        System.out.println("[관리자] 영화를 [" + newTitle + "]로 교체하였습니다.");
        return newTitle;
    }

    // 관리자 전용 기능 2: 경품 추첨
    public void drawLottery(java.util.List<String> reviews) {
        if (reviews.isEmpty()) {
            System.out.println("[알림] 아직 작성된 후기가 없어 추첨할 수 없습니다.");
        } else {
            java.util.Random rnd = new java.util.Random();
            String winner = reviews.get(rnd.nextInt(reviews.size()));
            System.out.println("\n=== 🎉 후기 경품 추첨 결과! ===");
            System.out.println("당첨된 후기: " + winner);
            System.out.println("===============================");
        }
    }
}
