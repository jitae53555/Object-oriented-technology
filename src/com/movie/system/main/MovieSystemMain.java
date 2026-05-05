package com.movie.system.main;

import com.movie.system.model.*;
import com.movie.system.service.ReservationService;
import java.util.Scanner;

public class MovieSystemMain {
    public static void main(String[] args) {
        Scanner sn = new Scanner(System.in);
        ReservationService service = new ReservationService();

        System.out.println("===============================");
        System.out.println("   영화 예매 시스템 (Movie System)");
        System.out.println("===============================");

        // 1. 학번 입력받기 (로그인)
        System.out.print("학번을 입력하세요: ");
        String studentId = sn.nextLine();

        Person currentUser;
        if (studentId.equals("20269999")) {
            currentUser = new Admin(studentId);
            System.out.println("\n[관리자 모드로 접속했습니다]");
        } else {
            currentUser = new User(studentId);
            System.out.println("\n[사용자 모드로 접속했습니다]");
        }

        // 좌석 초기화 (5x5 = 25명)
        int[][] seats = new int[5][5];

        // 2. 메뉴 루프
        boolean running = true;
        while (running) {
            System.out.println("\n----- 메뉴 -----");
            if (currentUser instanceof Admin) {
                System.out.println("0. 영화 정보 변경 (관리자)");
            }
            System.out.println("1. 영화 예매");
            System.out.println("2. 예매 내역 확인");
            System.out.println("3. 종료");
            System.out.print("선택: ");
            
            String choice = sn.nextLine();

            switch (choice) {
                case "0":
                    if (currentUser instanceof Admin) {
                        System.out.print("변경할 영화 제목: ");
                        String title = sn.nextLine();
                        ((Admin) currentUser).addMovie(new Movie(title, "장르 미정", 120));
                    } else {
                        System.out.println("관리자만 접근 가능한 메뉴입니다.");
                    }
                    break;
                case "1":
                    System.out.println("현재 상영 중인 영화: [어벤져스]");
                    System.out.print("예매할 영화 제목: ");
                    String movieTitle = sn.nextLine();

                    // 1. 네모 모양 좌석 배치도 출력
                    System.out.println("\n===== 좌석 배치도 (□: 빈 좌석, ■: 예약됨) =====");
                    System.out.print("   "); 
                    for (int c = 0; c < seats[0].length; c++) {
                        System.out.print((c + 1) + "  "); 
                    }
                    System.out.println();

                    for (int r = 0; r < seats.length; r++) {
                        System.out.print((char)('A' + r) + "  "); 
                        for (int c = 0; c < seats[r].length; c++) {
                            if (seats[r][c] == 0) {
                                System.out.print("□  "); 
                            } else {
                                System.out.print("■  "); 
                            }
                        }
                        System.out.println(); 
                    }
                    System.out.println("==============================================");

                    // 2. 사용자 입력 받기
                    try {
                        System.out.print("\n예약할 좌석의 행을 입력하세요 (A~E): ");
                        String rowInput = sn.nextLine().toUpperCase();
                        if (rowInput.isEmpty()) throw new Exception("행을 입력해야 합니다.");
                        char rowChar = rowInput.charAt(0);
                        int rIdx = rowChar - 'A';

                        System.out.print("예약할 좌석의 열을 입력하세요 (1~5): ");
                        int colInput = Integer.parseInt(sn.nextLine());
                        int cIdx = colInput - 1;

                        // 3. 입력값 검증 및 예약 처리
                        if (rIdx < 0 || rIdx >= seats.length || cIdx < 0 || cIdx >= seats[0].length) {
                            System.out.println("❌ 잘못된 좌석 번호입니다. 다시 시도해주세요.");
                        } 
                        else if (seats[rIdx][cIdx] == 1) {
                            System.out.println("❌ 이미 예약된 좌석입니다. 다른 좌석을 선택해주세요.");
                        } 
                        else {
                            seats[rIdx][cIdx] = 1; // 상태 업데이트
                            String seatInfo = rowChar + String.valueOf(colInput);
                            Ticket ticket = new Ticket(movieTitle, currentUser.getId(), seatInfo);
                            service.reserveTicket(ticket);
                            System.out.println("✅ 학번 [" + currentUser.getId() + "]님, " + seatInfo + " 좌석 예약이 완료되었습니다.");
                        }
                    } catch (Exception e) {
                        System.out.println("❌ 입력 형식이 잘못되었습니다: " + e.getMessage());
                    }
                    break;
                case "2":
                    service.showAllReservations();
                    break;
                case "3":
                    running = false;
                    System.out.println("프로그램을 종료합니다.");
                    break;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
        sn.close();
    }
}
