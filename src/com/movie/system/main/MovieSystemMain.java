package com.movie.system.main;

import com.movie.system.model.*;
import com.movie.system.service.ReservationService;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
public class MovieSystemMain {
    public static void main(String[] args) {
        Scanner sn = new Scanner(System.in, "UTF-8"); // 시스템 기본 인코딩
        ReservationService service = new ReservationService();
        String currentMovie = "어벤져스"; // 초기값

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
            System.out.println("3. 영화 후기 작성");
            System.out.println("4. 종료");
            System.out.print("선택: ");
            
            String choice = sn.nextLine();

            switch (choice) {
                case "0":
                    if (currentUser instanceof Admin) {
                        System.out.println("\n[관리자 전용 메뉴]");
                        System.out.println("1. 영화 제목 변경");
                        System.out.println("2. 후기 경품 추첨");
                        System.out.print("선택: ");
                        String adminChoice = sn.nextLine();

                        if (adminChoice.equals("1")) {
                            System.out.print("교체할 새 영화 제목 입력: ");
                            String newTitle = sn.nextLine(); // 관리자가 입력한 값
                            currentMovie = newTitle;        // 시스템 상태 업데이트
                            System.out.println("[알림] 상영 영화가 [" + currentMovie + "]로 교체되었습니다.");
                        } else if (adminChoice.equals("2")) {
                            List<String> reviews = service.getReviewList();
                            if (reviews.isEmpty()) {
                                System.out.println("[알림] 아직 작성된 후기가 없어 추첨할 수 없습니다.");
                            } else {
                                Random rnd = new Random();
                                String winnerReview = reviews.get(rnd.nextInt(reviews.size()));
                                System.out.println("\n=== 🎉 후기 경품 추첨 결과! ===");
                                System.out.println("당첨된 후기: " + winnerReview);
                                System.out.println("축하드립니다! 담당자에게 연락해 주세요.");
                                System.out.println("===============================");
                            }
                        }
                    } else {
                        System.out.println("관리자만 접근 가능한 메뉴입니다.");
                    }
                    break;
                case "1":
                    System.out.println("현재 상영 중인 영화: [" + currentMovie + "]");
                    String movieTitle = currentMovie; 
                    System.out.println("[알림] [" + movieTitle + "] 영화 예매를 진행합니다.");

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
                            System.out.println("[오류] 잘못된 좌석 번호입니다. 다시 시도해주세요.");
                        } 
                        else if (seats[rIdx][cIdx] == 1) {
                            System.out.println("[오류] 이미 예약된 좌석입니다. 다른 좌석을 선택해주세요.");
                        } 
                        else {
                            seats[rIdx][cIdx] = 1; // 상태 업데이트
                            String seatInfo = rowChar + String.valueOf(colInput);
                            Ticket ticket = new Ticket(movieTitle, currentUser.getId(), seatInfo);
                            service.reserveTicket(ticket);
                            System.out.println("[완료] 학번 [" + currentUser.getId() + "]님, " + seatInfo + " 좌석 예약이 완료되었습니다.");
                        }
                    } catch (Exception e) {
                        System.out.println("[오류] 입력 형식이 잘못되었습니다: " + e.getMessage());
                    }
                    break;
                case "2":
                    service.showAllReservations();
                    break;
                case "3":
                    System.out.println("\n--- 영화 후기 작성 ---");
                    System.out.print("후기 내용을 입력해 주세요: ");
                    String reviewText = sn.nextLine();
                    service.addReview(currentUser.getId(), reviewText);
                    System.out.println("[알림] 후기가 성공적으로 등록되었습니다. 참여해 주셔서 감사합니다!");
                    break;

                case "4":
                    running = false;
                    System.out.println("\n**********************************");
                    System.out.println("       프로그램을 종료합니다.");
                    System.out.println("**********************************");
                    System.out.println("\n[안내] 영화를 다 보고 나서 후기를 작성해 주세요!");
                    System.out.println("-> 후기 작성 선착순 10명에게 커피쿠폰 증정!");
                    System.out.println("\n감사합니다. 좋은 하루 되세요!");
                    break;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
        sn.close();
    }
}
