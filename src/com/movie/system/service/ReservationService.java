package com.movie.system.service;

import com.movie.system.model.Ticket;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    private List<Ticket> reservations = new ArrayList<>();

    // 예매하기
    public void reserveTicket(Ticket ticket) {
        reservations.add(ticket);
        System.out.println("[서비스] 예매가 완료되었습니다: " + ticket);
    }

    // 전체 예매 목록 확인
    public void showAllReservations() {
        System.out.println("----- 전체 예매 목록 -----");
        for (Ticket t : reservations) {
            System.out.println(t);
        }
    }
}
