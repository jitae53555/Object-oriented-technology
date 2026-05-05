package com.movie.system.model;

public class Ticket {
    private String title;
    private String id;
    private String seat;

    public Ticket(String title, String id, String seat) {
        this.title = title;
        this.id = id;
        this.seat = seat;
    }

    // Getter & Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSeat() { return seat; }
    public void setSeat(String seat) { this.seat = seat; }

    @Override
    public String toString() {
        return String.format("예매 정보 [영화: %s | 사용자ID: %s | 좌석: %s]", title, id, seat);
    }
}
