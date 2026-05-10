package com.movie.system.service;

import com.movie.system.model.Ticket;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ReservationService {
    private List<Ticket> reservations = new ArrayList<>();
    private List<String> reviewList = new ArrayList<>(); // 후기 저장소
    private final String FILE_PATH = "reservations.txt"; // 저장할 파일 이름

    // 생성자: 프로그램이 시작될 때 파일을 읽어옵니다.
    public ReservationService() {
        // [디버그] 파일이 실제로 어디에 저장되는지 확인용
        File f = new File(FILE_PATH);
        System.out.println("[시스템] 데이터 저장 위치: " + f.getAbsolutePath());
        
        loadFromFile();
        loadReviewsFromFile(); 
    }

    // 예매하기 (파일 저장 기능 추가)
    public void reserveTicket(Ticket ticket) {
        reservations.add(ticket);
        saveToFile(); // 리스트 전체를 저장합니다.
        System.out.println("[서비스] 예매가 완료되었습니다: " + ticket);
    }

    // 파일을 한 줄씩 읽어서 목록에 담아주는 메서드
    private void loadFromFile() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return;

            Scanner s = new Scanner(file, "UTF-8");
            while (s.hasNextLine()) {
                String line = s.nextLine();
                String[] data = line.split(",");
                if (data.length == 3) {
                    Ticket t = new Ticket(data[0], data[1], data[2]);
                    reservations.add(t);
                }
            }
            s.close();
        } catch (Exception e) {
            System.out.println("[오류] 예매 내역 불러오기 실패: " + e.getMessage());
        }
    }

    // 후기 파일을 읽어서 목록에 담아주는 메서드
    private void loadReviewsFromFile() {
        try {
            File file = new File("reviews.txt");
            if (!file.exists()) return;

            Scanner s = new Scanner(file, "UTF-8");
            while (s.hasNextLine()) {
                reviewList.add(s.nextLine());
            }
            s.close();
        } catch (Exception e) {
            System.out.println("[오류] 후기 불러오기 실패: " + e.getMessage());
        }
    }

    // 예매 정보를 파일에 저장하는 메서드
    private void saveToFile() {
        try {
            PrintWriter pw = new PrintWriter(FILE_PATH, "UTF-8");
            for (Ticket t : reservations) {
                pw.println(t.getTitle() + "," + t.getId() + "," + t.getSeat());
            }
            pw.close();
        } catch (Exception e) {
            System.out.println("[오류] 예매 내역 저장 실패: " + e.getMessage());
        }
    }

    // 전체 예매 목록 가져오기
    public List<Ticket> getReservations() {
        return reservations;
    }

    // 전체 예매 목록 확인 (참고용)
    public void showAllReservations() {
        System.out.println("----- 전체 예매 목록 -----");
        for (Ticket t : reservations) {
            System.out.println(t);
        }
    }

    // 후기 추가 (학번과 함께 저장 + 파일에도 바로 저장)
    public void addReview(String studentId, String review) {
        String msg = "[" + studentId + "] " + review;
        reviewList.add(msg);
        
        try {
            PrintWriter pw = new PrintWriter("reviews.txt", "UTF-8");
            for (String s : reviewList) {
                pw.println(s);
            }
            pw.close();
        } catch (Exception e) {
            System.out.println("[오류] 후기 저장 실패: " + e.getMessage());
        }
    }

    // 후기 목록 가져오기
    public List<String> getReviewList() {
        return reviewList;
    }

    // [추가] 전체 데이터 초기화 (초보자 스타일)
    public void clearData() {
        // 1. 메모리 비우기
        reservations.clear();
        reviewList.clear();

        // 2. 파일 비우기 (빈 파일을 새로 만드는 방식)
        try {
            PrintWriter pw1 = new PrintWriter(FILE_PATH, "UTF-8");
            pw1.close(); // 아무것도 안 쓰고 닫으면 비워짐
            
            PrintWriter pw2 = new PrintWriter("reviews.txt", "UTF-8");
            pw2.close();
            
            System.out.println("[알림] 모든 데이터가 성공적으로 초기화되었습니다.");
        } catch (Exception e) {
            System.out.println("[오류] 초기화 중 문제가 발생했습니다: " + e.getMessage());
        }
    }
}
