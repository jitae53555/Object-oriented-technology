package com.movie.system.model;

public class Movie {
    private String title;
    private String genre;
    private int time; // 분 단위

    public Movie(String title, String genre, int time) {
        this.title = title;
        this.genre = genre;
        this.time = time;
    }

    // Getter & Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }

    @Override
    public String toString() {
        return String.format("영화 제목: %s | 장르: %s | 상영시간: %d분", title, genre, time);
    }
}
