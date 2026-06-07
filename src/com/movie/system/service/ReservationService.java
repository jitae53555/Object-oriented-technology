package com.movie.system.service;

import com.movie.system.config.DBConnection;
import com.movie.system.model.Ticket;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ReservationService {
    private List<Ticket> reservations = new ArrayList<>();
    private List<String> reviewList = new ArrayList<>();

    public ReservationService() {
        System.out.println("[시스템] 데이터 저장 위치: MySQL 데이터베이스 및 reviews.txt");
        loadFromDatabase();
        loadReviewsFromFile();
    }

    // 예매하기 (MySQL DB 저장 기능으로 변경)
    public void reserveTicket(Ticket ticket) {
        String sql = "INSERT INTO reservations (movie_title, student_id, seat) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ticket.getTitle());
            pstmt.setString(2, ticket.getId());
            pstmt.setString(3, ticket.getSeat());
            pstmt.executeUpdate();

            reservations.add(ticket);
            System.out.println("[서비스] 예매 정보가 MySQL 데이터베이스에 성공적으로 저장되었습니다: " + ticket);
        } catch (SQLException e) {
            System.err.println("[오류] 예매 정보 DB 저장 실패: " + e.getMessage());
}
    }

    // 데이터베이스에서 예매 목록을 읽어오는 메서드
    private void loadFromDatabase() {
        reservations.clear();
        String sql = "SELECT movie_title, student_id, seat FROM reservations";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String movieTitle = rs.getString("movie_title");
                String studentId = rs.getString("student_id");
                String seat = rs.getString("seat");

                Ticket t = new Ticket(movieTitle, studentId, seat);
                reservations.add(t);
            }
            System.out.println("[알림] MySQL 데이터베이스로부터 예매 내역(" + reservations.size() + "건)을 성공적으로 불러왔습니다.");
        } catch (SQLException e) {
            System.err.println("[알림] 데이터베이스 예매 내역 불러오기 실패(연결 설정 대기 중): " + e.getMessage());
        }
    }

    // 후기 파일을 읽어서 목록에 담아주는 메서드 (기존 파일 방식 유지)
    private void loadReviewsFromFile() {
        try {
            File file = new File("reviews.txt");
            if (!file.exists())
                return;

            Scanner s = new Scanner(file, "UTF-8");
            while (s.hasNextLine()) {
                reviewList.add(s.nextLine());
            }
            s.close();
        } catch (Exception e) {
            System.out.println("[오류] 후기 불러오기 실패: " + e.getMessage());
        }
    }

    // 전체 예매 목록 가져오기
    public List<Ticket> getReservations() {
        loadFromDatabase();
        return reservations;
    }

    // 후기 추가 (학번과 함께 저장 + 파일에도 바로 저장)
    public void addReview(String studentId, String review) {

        for (String r : reviewList) {
            if (r.startsWith("[" + studentId + "]")) {
                System.out.println("[오류] 이미 후기를 작성하셨습니다. (한 사람당 하나만 가능)");
                return;
            }
        }

        String msg = "[" + studentId + "] " + review;
        reviewList.add(msg);

        try {
            PrintWriter pw = new PrintWriter("reviews.txt", "UTF-8");
            for (String s : reviewList) {
                pw.println(s);
            }
            pw.close();
            System.out.println("[알림] 후기가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            System.out.println("[오류] 후기 저장 실패: " + e.getMessage());
        }
    }

    // 후기 목록 가져오기
    public List<String> getReviewList() {
        return reviewList;
    }

    // 전체 데이터 초기화 (DB 및 파일 비우기)
    public void clearData() {
        // 1. 메모리 비우기
        reservations.clear();
        reviewList.clear();

        // 2. MySQL 데이터베이스 예매 및 당첨자 테이블 비우기
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("DELETE FROM reservations");
            stmt.executeUpdate("DELETE FROM winners");
            System.out.println("[알림] MySQL 예매 및 당첨자 테이블 데이터가 초기화되었습니다.");
        } catch (SQLException e) {
            System.err.println("[오류] DB 데이터 초기화 중 문제가 발생했습니다: " + e.getMessage());
        }

        // 3. 후기 파일 비우기 (빈 파일을 새로 만드는 방식)
        try {
            PrintWriter pw2 = new PrintWriter("reviews.txt", "UTF-8");
            pw2.close();
        } catch (Exception e) {
            System.out.println("[오류] 파일 초기화 중 문제가 발생했습니다: " + e.getMessage());
        }
    }

    // 예매한 좌석 변경 (MySQL DB 업데이트 기능)
    public boolean updateReservationSeat(String studentId, String newSeat) {
        String sql = "UPDATE reservations SET seat = ? WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newSeat);
            pstmt.setString(2, studentId);
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                loadFromDatabase();
                System.out.println("[서비스] 학번 [" + studentId + "]님의 예매 좌석이 [" + newSeat + "](으)로 변경되었습니다.");
                return true;
            } else {
                System.err.println("[오류] 예매 정보를 찾을 수 없어 좌석을 변경할 수 없습니다.");
            }
        } catch (SQLException e) {
            System.err.println("[오류] 예매 변경 중 DB 에러가 발생했습니다: " + e.getMessage());
        }
        return false;
    }

    // 후기를 작성했는지 여부 확인
    public boolean hasWrittenReview(String studentId) {
        for (String r : reviewList) {
            if (r.startsWith("[" + studentId + "]")) {
                return true;
            }
        }
        return false;
    }

    // 예매자 중에서 무작위 5명을 경품 추첨하고 DB winners 테이블에 기록하는 메소드
    public List<Ticket> drawGiveaway() {
        List<Ticket> winners = new ArrayList<>();
        
        // 1. 기존 당첨자 테이블 비우기
        String clearSql = "DELETE FROM winners";
        // 2. 현재 예매 목록에서 무작위 5명 선정
        String selectSql = "SELECT movie_title, student_id, seat FROM reservations ORDER BY RAND() LIMIT 5";
        // 3. 당첨자 정보 winners 테이블에 인서트
        String insertSql = "INSERT INTO winners (movie_title, student_id, seat) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // 기존 당첨자 삭제
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(clearSql);
            }
            
            // 신규 무작위 당첨자 선정 및 DB 저장
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql);
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                
                while (rs.next()) {
                    String movieTitle = rs.getString("movie_title");
                    String studentId = rs.getString("student_id");
                    String seat = rs.getString("seat");
                    
                    winners.add(new Ticket(movieTitle, studentId, seat));
                    
                    pstmt.setString(1, movieTitle);
                    pstmt.setString(2, studentId);
                    pstmt.setString(3, seat);
                    pstmt.addBatch();
                }
                
                pstmt.executeBatch();
            }
            
            conn.commit();
            System.out.println("[서비스] 5명 무작위 추첨 및 MySQL winners 테이블 저장이 성공적으로 완료되었습니다.");
        } catch (SQLException e) {
            System.err.println("[오류] 경품 추첨 데이터베이스 처리 중 장애 발생: " + e.getMessage());
        }
        
        return winners;
    }

    // 예매 취소 (MySQL DB 삭제 및 메모리 리스트 동기화)
    public boolean cancelReservation(String studentId) {
        String sql = "DELETE FROM reservations WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                loadFromDatabase(); // DB 업데이트 후 메모리 데이터 실시간 동기화
                System.out.println("[서비스] 학번 [" + studentId + "]님의 예매 내역이 정상적으로 취소되었습니다.");
                return true;
            } else {
                System.err.println("[오류] 예매 내역을 찾을 수 없어 취소할 수 없습니다.");
            }
        } catch (SQLException e) {
            System.err.println("[오류] 예매 취소 중 DB 장애 발생: " + e.getMessage());
        }
        return false;
    }
}
