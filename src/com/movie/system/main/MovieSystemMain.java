package com.movie.system.main;

import com.movie.system.model.*;
import com.movie.system.service.ReservationService;
import java.util.List;
import java.util.Scanner;

public class MovieSystemMain {

    public static void main(String[] args) {
        Scanner sn = new Scanner(System.in);
        ReservationService service = new ReservationService();

        // 상영 중인 영화 정보를 객체 하나로 관리합니다.
        Movie currentMovie = new Movie("어벤져스");

        System.out.println("===============================");
        System.out.println("영화 예매 시스템 (Movie System)");
        System.out.println("===============================");

        // 1. 학번 입력받기 (로그인)
        String studentId;
        while (true) {
            System.out.print("학번을 입력하세요: ");
            studentId = sn.nextLine();

            // 학번 길이 체크 (5자리 ~ 10자리)
            if (studentId.length() >= 5 && studentId.length() <= 10) {
                break; // 올바른 길이면 반복 종료
            } else {
                System.out.println("[오류] 학번은 최소 5자리부터 10자리까지 입력 가능합니다.");
            }
        }

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

        // 기존에 저장된 예약 내역이 있다면 배치도에 표시.
        for (Ticket ticket : service.getReservations()) {
            String seatInfo = ticket.getSeat(); // 예: "A1"
            if (seatInfo.length() == 2) {
                int rIdx = seatInfo.charAt(0) - 'A';
                int cIdx = seatInfo.charAt(1) - '1';
                if (rIdx >= 0 && rIdx < 5 && cIdx >= 0 && cIdx < 5) {
                    seats[rIdx][cIdx] = 1;
                }
            }
        }

        // 2. 메뉴 루프
        boolean running = true;
        while (running) {
            System.out.println("\n----- 메뉴 -----");
            if (currentUser instanceof Admin) {
                // 관리자용 메뉴 (1~5번)
                System.out.println("1. 영화 제목 변경\n2. 예매 내역 확인\n3. 영화 후기 전체 보기\n4. 종료\n5. 전체 데이터 초기화");
            } else {
                // 일반 사용자용 메뉴 (1~4번)
                System.out.println("1. 영화 예매\n2. 예매 내역 확인\n3. 영화 후기 작성\n4. 종료");
            }
            System.out.print("선택: ");

            String choice = sn.nextLine();

            // 사용자가 선택한 번호에 따라 기능을 실행합니다.
            switch (choice) {
                case "1":
                    if (currentUser instanceof Admin) {
                        // [관리자] 영화 제목 변경
                        System.out.println("\n[관리자] 영화 제목 변경");
                        System.out.println("현재 영화 제목: " + currentMovie.getTitle());
                        System.out.print("교체할 새 영화 제목 입력: ");
                        String newTitle = sn.nextLine();
                        currentMovie.setTitle(newTitle);
                        System.out.println("[알림] 상영 영화가 [" + currentMovie.getTitle() + "]로 교체되었습니다.");
                    } else {
                        // [사용자] 영화 예매 진입 전 체크
                        boolean alreadyReserved = false;
                        for (Ticket t : service.getReservations()) {
                            if (t.getId().equals(currentUser.getId())) {
                                alreadyReserved = true;
                                break;
                            }
                        }

                        if (alreadyReserved) {
                            System.out.println("\n[오류] 이미 예매한 내역이 있습니다. 추가 예매는 불가능합니다.");
                            break;
                        }

                        // 예매 내역이 없는 경우에만 아래 로직 실행
                        System.out.println("\n현재 상영 중인 영화: " + currentMovie.getTitle());

                        // 좌석 배치도 출력
                        System.out.println("\n===== 좌석 배치도 (□: 빈 좌석, ■: 예약됨) =====");
                        System.out.print("   ");
                        for (int c = 0; c < 5; c++) {
                            System.out.print((c + 1) + "  ");
                        }
                        System.out.println();
                        for (int r = 0; r < 5; r++) {
                            System.out.print((char) ('A' + r) + "  ");
                            for (int c = 0; c < 5; c++) {
                                if (seats[r][c] == 0) {
                                    System.out.print("□  "); 
                                }else {
                                    System.out.print("■  ");
                                }
                            }
                            System.out.println();
                        }
                        System.out.println("==============================================");

                        while (true) {
                            System.out.print("\n예매할 좌석을 입력하세요 (예: A1 ~ E5): ");
                            String seatInput = sn.nextLine().toUpperCase().trim();

                            if (seatInput.length() == 2) {
                                char rowChar = seatInput.charAt(0);
                                char colChar = seatInput.charAt(1);

                                if (rowChar >= 'A' && rowChar <= 'E' && colChar >= '1' && colChar <= '5') {
                                    int rIdx = rowChar - 'A';
                                    int cIdx = colChar - '1';

                                    if (seats[rIdx][cIdx] == 1) {
                                        System.out.println("[오류] 이미 예약된 좌석입니다. 다른 좌석을 선택해 주세요.");
                                    } else {
                                        seats[rIdx][cIdx] = 1;
                                        Ticket ticket = new Ticket(currentMovie.getTitle(), currentUser.getId(), seatInput);
                                        service.reserveTicket(ticket);
                                        System.out.println("[완료] " + seatInput + " 좌석 예약이 완료되었습니다.");
                                        break; // 예약 성공 시 루프 탈출
                                    }
                                } else {
                                    System.out.println("[오류] 올바른 좌석 범위(A1 ~ E5)를 입력해 주세요.");
                                }
                            } else {
                                System.out.println("[오류] 좌석은 A1 형태로 두 글자만 입력해 주세요.");
                            }
                        }
                    }
                    break;

                case "2":
                    // [공통] 예매 내역 확인
                    System.out.println("\n--- 예매 내역 확인 ---");
                    System.out.print("확인할 학번을 입력하세요: ");
                    String searchId = sn.nextLine();

                    boolean found = false;
                    System.out.println("\n[" + searchId + "]님의 예매 내역:");
                    for (Ticket t : service.getReservations()) {
                        if (t.getId().equals(searchId)) {
                            System.out.println("- " + t);
                            found = true;
                        }
                    }
                    if (!found) {
                        System.out.println("예매 내역이 없습니다.");
                    }
                    break;

                case "3":
                    if (currentUser instanceof Admin) {
                        // [관리자] 영화 후기 목록 보기
                        System.out.println("\n--- [관리자] 영화 후기 목록 ---");
                        List<String> reviews = service.getReviewList();
                        if (reviews.isEmpty()) {
                            System.out.println("[알림] 현재 등록된 후기가 하나도 없습니다.");
                        } else {
                            int num = 1;
                            for (String r : reviews) {
                                System.out.println(num++ + ". " + r);
                            }
                        }
                    } else {
                        // [사용자] 영화 후기 작성
                        System.out.println("\n--- 영화 후기 작성 ---");
                        System.out.print("후기 내용을 입력해 주세요: ");
                        String text = sn.nextLine();
                        service.addReview(currentUser.getId(), text);
                    }
                    break;

                case "4":
                    running = false;
                    if (currentUser instanceof Admin) {
                        System.out.println("\n관리자 모드를 종료합니다.");
                    } else {
                        System.out.println("\n사용자 모드를 종료합니다. 즐거운 하루 되세요!");
                    }
                    break;

                case "5":
                    if (currentUser instanceof Admin) {
                        // [관리자] 전체 데이터 초기화
                        System.out.print("\n[주의] 정말로 모든 데이터를 초기화하시겠습니까? (y/n): ");
                        String confirm = sn.nextLine();
                        if (confirm.equalsIgnoreCase("y")) {
                            service.clearData();
                            // 좌석 정보도 초기화
                            for (int i = 0; i < 5; i++) {
                                for (int j = 0; j < 5; j++) {
                                    seats[i][j] = 0;
                                }
                            }
                        } else {
                            System.out.println("[알림] 초기화가 취소되었습니다.");
                        }
                    } else {
                        System.out.println("[알림] 메뉴에 있는 번호를 입력해 주세요.");
                    }
                    break;

                default:
                    System.out.println("[알림] 메뉴에 있는 번호를 입력해 주세요.");
            }
        }
        sn.close();
    }
}
