package com.movie.system.main;

import com.movie.system.model.*;
import com.movie.system.service.ReservationService;
import java.util.List;
import java.util.Scanner;

public class MovieSystemMain {

    public static void main(String[] args) {
        Scanner sn = new Scanner(System.in);
        ReservationService service = new ReservationService();

        // [MySQL 연동] 상영 중인 영화 정보를 데이터베이스에서 불러옵니다.
        String movieTitle = "어벤져스";
        try (java.sql.Connection conn = com.movie.system.config.DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT title FROM movies LIMIT 1")) {
            if (rs.next()) {
                movieTitle = rs.getString("title");
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[알림] DB에서 영화 제목을 불러올 수 없어 기본값을 설정합니다: " + e.getMessage());
        }

        Movie currentMovie = new Movie(movieTitle);

        System.out.println("===============================");
        System.out.println("영화 예매 시스템 (Movie System)");
        System.out.println("===============================");

        // 학번 입력받기 (로그인)
        String studentId;
        while (true) {
            System.out.print("학번을 입력하세요: ");
            studentId = sn.nextLine();

            // 학번 길이 체크 (5자리 ~ 10자리)
            if (studentId.length() >= 5 && studentId.length() <= 10) {
                break;
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


        // 메뉴
        boolean running = true;
        while (running) {
            System.out.println("\n----- 메뉴 -----");
            if (currentUser instanceof Admin) {
                System.out.println("1. 영화 제목 변경\n2. 예매 내역 확인\n3. 영화 후기 전체 보기\n4. 경품 추첨\n5. 종료\n6. 전체 데이터 초기화");
            } else {
                System.out.println("1. 영화 예매\n2. 예매 내역 확인\n3. 영화 후기 작성\n4. 영화 예매 변경\n5. 종료");
            }
            System.out.print("선택: ");

            String choice = sn.nextLine();

            // 사용자가 선택한 번호에 따라 기능을 실행
            switch (choice) {
                case "1":
                    if (currentUser instanceof Admin) {
                        System.out.println("\n[관리자] 영화 제목 변경");
                        System.out.println("현재 영화 제목: " + currentMovie.getTitle());
                        System.out.print("교체할 새 영화 제목 입력: ");
                        String newTitle = sn.nextLine();
                        currentMovie.setTitle(newTitle);

                        // [MySQL 연동] 변경된 영화 제목을 DB에 업데이트
                        try (java.sql.Connection conn = com.movie.system.config.DBConnection.getConnection();
                             java.sql.PreparedStatement pstmt = conn.prepareStatement("UPDATE movies SET title = ?")) {
                            pstmt.setString(1, newTitle);
                            int affected = pstmt.executeUpdate();
                            if (affected == 0) {
                                try (java.sql.Statement stmt = conn.createStatement()) {
                                    stmt.executeUpdate("INSERT INTO movies (title) VALUES ('" + newTitle + "')");
                                }
                            }
                            System.out.println("[알림] 변경된 영화 제목이 MySQL 데이터베이스에 영구 반영되었습니다.");
                        } catch (java.sql.SQLException e) {
                            System.err.println("[오류] DB 영화 제목 업데이트 실패: " + e.getMessage());
                        }

                        System.out.println("[알림] 상영 영화가 [" + currentMovie.getTitle() + "]로 교체되었습니다.");
                    } else {
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
                        System.out.println("\n현재 상영 중인 영화: " + currentMovie.getTitle());
                        List<Ticket> currentReservations = service.getReservations();

                        System.out.println("\n===== 좌석 배치도 (□: 빈 좌석, ■: 예약됨) =====");
                        System.out.print("   ");
                        for (int c = 0; c < 5; c++) {
                            System.out.print((c + 1) + "  ");
                        }
                        System.out.println();
                        for (int r = 0; r < 5; r++) {
                            char rowChar = (char) ('A' + r);
                            System.out.print(rowChar + "  ");
                            for (int c = 0; c < 5; c++) {
                                String seatName = "" + rowChar + (c + 1);
                                boolean isReserved = false;
                                for (Ticket t : currentReservations) {
                                    if (t.getSeat().equalsIgnoreCase(seatName)) {
                                        isReserved = true;
                                        break;
                                    }
                                }
                                if (isReserved) {
                                    System.out.print("■  ");
                                } else {
                                    System.out.print("□  ");
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
                                    boolean isReserved = false;
                                    for (Ticket t : service.getReservations()) {
                                        if (t.getSeat().equalsIgnoreCase(seatInput)) {
                                            isReserved = true;
                                            break;
                                        }
                                    }

                                    if (isReserved) {
                                        System.out.println("[오류] 이미 예약된 좌석입니다. 다른 좌석을 선택해 주세요.");
                                    } else {
                                        Ticket ticket = new Ticket(currentMovie.getTitle(), currentUser.getId(), seatInput);
                                        service.reserveTicket(ticket);
                                        System.out.println("[완료] " + seatInput + " 좌석 예약이 완료되었습니다.");
                                        break;
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
                        System.out.println("\n--- 영화 후기 작성 ---");
                        System.out.print("후기 내용을 입력해 주세요: ");
                        String text = sn.nextLine();
                        service.addReview(currentUser.getId(), text);
                    }
                    break;

                case "4":
                    if (currentUser instanceof Admin) {
                        drawGiveaway(service);
                    } else {
                        changeReservation(currentUser, service, currentMovie, sn);
                    }
                    break;

                case "5":
                    running = false;
                    if (currentUser instanceof Admin) {
                        System.out.println("\n관리자 모드를 종료합니다.");
                    } else {
                        System.out.println("\n사용자 모드를 종료합니다. 즐거운 하루 되세요!");
                    }
                    break;

                case "6":
                    if (currentUser instanceof Admin) {
                        System.out.print("\n[주의] 정말로 모든 데이터를 초기화하시겠습니까? (y/n): ");
                        String confirm = sn.nextLine();
                        if (confirm.equalsIgnoreCase("y")) {
                            service.clearData();
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

    // [사용자] 영화 예매 변경 및 취소 로직
    private static void changeReservation(Person currentUser, ReservationService service, Movie currentMovie, Scanner sn) {
        System.out.println("\n--- 영화 예매 변경 및 취소 ---");
        
        if (service.hasWrittenReview(currentUser.getId())) {
            System.out.println("[오류] 이미 영화 후기를 작성하셨기 때문에 예매 변경 및 취소가 불가능합니다.");
            return;
        }
        Ticket userTicket = null;
        for (Ticket t : service.getReservations()) {
            if (t.getId().equals(currentUser.getId())) {
                userTicket = t;
                break;
            }
        }
        
        if (userTicket == null) {
            System.out.println("[오류] 예매한 내역이 없습니다. 먼저 영화를 예매해 주세요.");
            return;
        }

        System.out.println("현재 예매하신 영화: " + userTicket.getTitle());
        System.out.println("현재 예매하신 좌석: " + userTicket.getSeat());

        System.out.println("\n원하시는 작업을 선택하세요:");
        System.out.println("1. 좌석 변경");
        System.out.println("2. 예매 취소");
        System.out.print("선택: ");
        String subChoice = sn.nextLine().trim();

        if (subChoice.equals("2")) {
            boolean success = service.cancelReservation(currentUser.getId());
            if (success) {
                System.out.println("[완료] 예매가 취소되었습니다.");
            } else {
                System.out.println("[오류] 예매 취소에 실패했습니다.");
            }
            return;
        }

        List<Ticket> currentReservations = service.getReservations();

        System.out.println("\n===== 좌석 배치도 (□: 빈 좌석, ■: 예약됨) =====");
        System.out.print("   ");
        for (int c = 0; c < 5; c++) {
            System.out.print((c + 1) + "  ");
        }
        System.out.println();
        for (int r = 0; r < 5; r++) {
            char rowChar = (char) ('A' + r);
            System.out.print(rowChar + "  ");
            for (int c = 0; c < 5; c++) {
                String seatName = "" + rowChar + (c + 1);
                
                boolean isReserved = false;
                for (Ticket t : currentReservations) {
                    if (t.getSeat().equalsIgnoreCase(seatName)) {
                        isReserved = true;
                        break;
                    }
                }
                
                if (isReserved) {
                    System.out.print("■  ");
                } else {
                    System.out.print("□  ");
                }
            }
            System.out.println();
        }
        System.out.println("==============================================");
        
        while (true) {
            System.out.print("\n변경할 새 좌석을 입력하세요 (예: A1 ~ E5, 취소하려면 '취소' 입력): ");
            String seatInput = sn.nextLine().toUpperCase().trim();
            
            if (seatInput.equals("취소")) {
                System.out.println("[알림] 예매 변경이 취소되었습니다.");
                break;
            }
            
            if (seatInput.length() == 2) {
                char rowChar = seatInput.charAt(0);
                char colChar = seatInput.charAt(1);
                
                if (rowChar >= 'A' && rowChar <= 'E' && colChar >= '1' && colChar <= '5') {
                    if (seatInput.equalsIgnoreCase(userTicket.getSeat())) {
                        System.out.println("[오류] 현재 이미 선택하신 좌석입니다. 다른 좌석을 선택해 주세요.");
                        continue;
                    }
                    boolean isReserved = false;
                    for (Ticket t : service.getReservations()) {
                        if (t.getSeat().equalsIgnoreCase(seatInput) && !t.getId().equals(currentUser.getId())) {
                            isReserved = true;
                            break;
                        }
                    }
                    
                    if (isReserved) {
                        System.out.println("[오류] 이미 다른 사람이 예약한 좌석입니다. 다른 좌석을 선택해 주세요.");
                    } else {
                        String oldSeat = userTicket.getSeat();
                        boolean success = service.updateReservationSeat(currentUser.getId(), seatInput);
                        if (success) {
                            System.out.println("[완료] 좌석이 [" + oldSeat + "] -> [" + seatInput + "](으)로 성공적으로 변경되었습니다.");
                        } else {
                            System.out.println("[오류] 예매 변경 처리에 실패했습니다.");
                        }
                        break;
                    }
                } else {
                    System.out.println("[오류] 올바른 좌석 범위(A1 ~ E5)를 입력해 주세요.");
                }
            } else {
                System.out.println("[오류] 좌석은 A1 형태로 두 글자만 입력해 주세요.");
            }
        }
    }

    // [관리자] 경품 추첨 로직
    private static void drawGiveaway(ReservationService service) {
        System.out.println("\n--- [관리자] 경품 추첨 (5명 무작위 추첨) ---");

        List<Ticket> currentReservations = service.getReservations();
        if (currentReservations.isEmpty()) {
            System.out.println("[오류] 현재 예매자가 한 명도 없어 경품 추첨을 진행할 수 없습니다.");
            return;
        }
        System.out.println("현재 총 예매자 수: " + currentReservations.size() + "명");
        if (currentReservations.size() < 5) {
            System.out.println("[알림] 예매자가 5명 미만입니다. 현재 예매자 전원을 당첨자로 추첨합니다.");
        } else {
            System.out.println("예매자 중 무작위 5명을 추첨하여 데이터베이스에 저장합니다...");
        }
        List<Ticket> winners = service.drawGiveaway();

        System.out.println("\n====================================");
        System.out.println("        경품 추첨 당첨자 명단        ");
        System.out.println("====================================");
        if (winners.isEmpty()) {
            System.out.println("추첨 도중 에러가 발생했거나 당첨자가 없습니다.");
        } else {
            int rank = 1;
            for (Ticket winner : winners) {
                System.out.printf("   [%d등] 학번: %-12s | 좌석: %-5s (영화: %s)%n",
                                  rank++, winner.getId(), winner.getSeat(), winner.getTitle());
            }
        }
        System.out.println("=========================================");
        System.out.println("[안내] 당첨자 내역은 MySQL 데이터베이스의 `winners` 테이블에 영구 보존됩니다.");
    }
}
