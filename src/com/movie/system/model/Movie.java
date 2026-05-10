package com.movie.system.model;

public class Movie {
    private String title;

    // 이제 제목 하나만 받도록 아주 간단해졌습니다!
    public Movie(String title) {
        this.title = title;
    }

    // Getter & Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Override
    public String toString() {
        return "영화 제목: " + title;
    }
}
