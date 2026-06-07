-- =============================================================================
-- 영화 예매 시스템 및 관리자 모드를 위한 MySQL 스키마 및 더미 데이터 스크립트
-- 작성 목적: 시스템 작동 검증 및 쿼리 성능 테스트용 현실적 더미 데이터 구축
-- 기준 날짜: 2026-05-30 (오늘), 2026-05-31 (내일)
-- =============================================================================

-- 데이터베이스 생성 및 선택 (선택 사항, 필요 시 주석을 해제하고 사용하세요)
-- CREATE DATABASE IF NOT EXISTS movie_reservation_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE movie_reservation_db;

-- -----------------------------------------------------------------------------
-- [참고용] 스키마 생성 DDL (기존 테이블이 있다면 삭제 후 재생성 가능하도록 작성)
-- -----------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `reservation_seats`;
DROP TABLE IF EXISTS `reservations`;
DROP TABLE IF EXISTS `seats`;
DROP TABLE IF EXISTS `schedules`;
DROP TABLE IF EXISTS `theaters`;
DROP TABLE IF EXISTS `movies`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. 사용자 테이블 (users)
CREATE TABLE `users` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 ID',
    `login_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 ID',
    `password` VARCHAR(255) NOT NULL COMMENT '암호화된 해시 비밀번호',
    `name` VARCHAR(50) NOT NULL COMMENT '사용자 실명',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일 주소',
    `role` ENUM('ADMIN', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER' COMMENT '권한 (관리자/일반고객)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 영화 테이블 (movies)
CREATE TABLE `movies` (
    `movie_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '영화 고유 ID',
    `title` VARCHAR(100) NOT NULL COMMENT '영화 제목',
    `genre` VARCHAR(50) NOT NULL COMMENT '영화 장르',
    `duration` INT NOT NULL COMMENT '상영 시간 (분)',
    `release_date` DATE NOT NULL COMMENT '개봉일',
    `status` ENUM('ON_SCREEN', 'UPCOMING') NOT NULL DEFAULT 'ON_SCREEN' COMMENT '상영 상태 (상영중/개봉예정)',
    `description` TEXT COMMENT '영화 소개글'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 상영관 테이블 (theaters)
CREATE TABLE `theaters` (
    `theater_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '상영관 고유 ID',
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '상영관 명칭 (예: 1관, IMAX관)',
    `total_seats` INT NOT NULL COMMENT '총 좌석 수'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 상영 스케줄 테이블 (schedules)
CREATE TABLE `schedules` (
    `schedule_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '스케줄 고유 ID',
    `movie_id` INT NOT NULL COMMENT '영화 ID (FK)',
    `theater_id` INT NOT NULL COMMENT '상영관 ID (FK)',
    `start_time` DATETIME NOT NULL COMMENT '상영 시작 시간',
    `end_time` DATETIME NOT NULL COMMENT '상영 종료 시간',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '기본 관람료',
    FOREIGN KEY (`movie_id`) REFERENCES `movies` (`movie_id`) ON DELETE CASCADE,
    FOREIGN KEY (`theater_id`) REFERENCES `theaters` (`theater_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 좌석 테이블 (seats)
CREATE TABLE `seats` (
    `seat_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '좌석 고유 ID',
    `theater_id` INT NOT NULL COMMENT '상영관 ID (FK)',
    `seat_row` CHAR(1) NOT NULL COMMENT '좌석 열 (예: A, B)',
    `seat_number` INT NOT NULL COMMENT '좌석 번호 (예: 1, 2)',
    UNIQUE KEY `unique_theater_seat` (`theater_id`, `seat_row`, `seat_number`),
    FOREIGN KEY (`theater_id`) REFERENCES `theaters` (`theater_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 예매 테이블 (reservations)
CREATE TABLE `reservations` (
    `reservation_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '예매 고유 ID',
    `user_id` INT NOT NULL COMMENT '예매한 사용자 ID (FK)',
    `schedule_id` INT NOT NULL COMMENT '상영 스케줄 ID (FK)',
    `total_price` DECIMAL(10, 2) NOT NULL COMMENT '총 결제 금액',
    `status` ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED' COMMENT '예매 상태 (확정/취소)',
    `reserved_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '예매 일시',
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`schedule_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 예매-좌석 매핑 테이블 (reservation_seats)
CREATE TABLE `reservation_seats` (
    `reservation_seat_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '예매 좌석 고유 ID',
    `reservation_id` INT NOT NULL COMMENT '예매 ID (FK)',
    `seat_id` INT NOT NULL COMMENT '좌석 ID (FK)',
    UNIQUE KEY `unique_reservation_seat` (`reservation_id`, `seat_id`),
    FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`) ON DELETE CASCADE,
    FOREIGN KEY (`seat_id`) REFERENCES `seats` (`seat_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- [DML] 요구사항 충족 현실적 더미 데이터 삽입 (DML)
-- =============================================================================

-- 외래키 체크 임시 해제하지 않고, 완벽한 순서로 삽입하여 무결성 유지!
START TRANSACTION;

-- -----------------------------------------------------------------------------
-- 요구사항 1: users 테이블 데이터 삽입
-- 관리자 계정 2개(role='ADMIN'), 일반 고객 계정 5개(role='CUSTOMER')
-- 암호화된 해시 형태의 임의의 비밀번호 문자열 부여 (BCrypt 형식 예시)
-- -----------------------------------------------------------------------------
INSERT INTO `users` (`user_id`, `login_id`, `password`, `name`, `email`, `role`, `created_at`) VALUES
-- 관리자 계정 (2개)
(1, 'admin1', '$2a$12$Lvy6Zc4lM87yF8a7c2m8eO2pW/5.L4a6Q4rW5C7b4yV4fB1C2d3e.', '김관리', 'admin1@cinebox.com', 'ADMIN', '2026-05-01 09:00:00'),
(2, 'admin2', '$2a$12$Kvy7Zc4lM87yF8a7c2m8eO2pW/5.L4a6Q4rW5C7b4yV4fB1C2d3f.', '이운영', 'admin2@cinebox.com', 'ADMIN', '2026-05-02 10:30:00'),
-- 일반 고객 계정 (5개)
(3, 'customer1', '$2a$12$Hvz8Zc4lM87yF8a7c2m8eO2pW/5.L4a6Q4rW5C7b4yV4fB1C2d3g.', '홍길동', 'gildong.hong@gmail.com', 'CUSTOMER', '2026-05-10 14:22:15'),
(4, 'customer2', '$2a$12$Jvz9Zc4lM87yF8a7c2m8eO2pW/5.L4a6Q4rW5C7b4yV4fB1C2d3h.', '김철수', 'chulsoo.kim@naver.com', 'CUSTOMER', '2026-05-12 17:45:30'),
(5, 'customer3', '$2a$12$Avz0Zc4lM87yF8a7c2m8eO2pW/5.L4a6Q4rW5C7b4yV4fB1C2d3i.', '이영희', 'younghee.lee@daum.net', 'CUSTOMER', '2026-05-15 11:05:42'),
(6, 'customer4', '$2a$12$Bvz1Zc4lM87yF8a7c2m8eO2pW/5.L4a6Q4rW5C7b4yV4fB1C2d3j.', '박민수', 'minsu.park@gmail.com', 'CUSTOMER', '2026-05-20 20:18:03'),
(7, 'customer5', '$2a$12$Cvz2Zc4lM87yF8a7c2m8eO2pW/5.L4a6Q4rW5C7b4yV4fB1C2d3k.', '최수진', 'sujin.choi@kakao.com', 'CUSTOMER', '2026-05-25 15:33:55');


-- -----------------------------------------------------------------------------
-- 요구사항 2: movies 테이블 데이터 삽입
-- 현재 상영 중(ON_SCREEN)이거나 개봉 예정(UPCOMING)인 한국/외국 영화 데이터 5개
-- -----------------------------------------------------------------------------
INSERT INTO `movies` (`movie_id`, `title`, `genre`, `duration`, `release_date`, `status`, `description`) VALUES
(1, '범죄도시5', '액션/범죄', 120, '2026-05-15', 'ON_SCREEN', '괴물형사 마석도와 서울 광수대가 한층 더 강력해진 신종 마약 범죄 조직을 소탕하기 위해 펼치는 통쾌한 범죄 소탕 작전!'),
(2, '기생충 2', '스릴러/드라마', 135, '2026-06-25', 'UPCOMING', '아카데미 수상작 기생충, 그 후 7년 뒤의 이야기. 예측 불가능한 새로운 서스펜스가 시작된다.'),
(3, '인터스텔라: 리본', 'SF/어드벤처', 165, '2026-05-20', 'ON_SCREEN', '지구의 기후 종말 이후 새로운 은하계를 개척하기 위해 블랙홀 속으로 뛰어든 탐사대의 경이로운 우주 대여정.'),
(4, '아바타: 불과 재', 'SF/액션', 180, '2026-12-18', 'UPCOMING', '판도라 행성의 재의 부족과 설리 가족이 충돌하며 전개되는 판도라 행성 사상 가장 치열하고 뜨거운 전쟁.'),
(5, '러브레터: 재개봉', '멜로/로맨스', 117, '2026-05-29', 'ON_SCREEN', '\"오겡끼데스까?\" 눈 덮인 홋카이도를 배경으로 가슴 아픈 첫사랑의 기억을 더듬어가는 클래식 로맨스 걸작.');


-- -----------------------------------------------------------------------------
-- 요구사항 3: theaters 테이블 데이터 삽입
-- 1관(100석), 2관(120석), IMAX관(150석) 정보 생성
-- -----------------------------------------------------------------------------
INSERT INTO `theaters` (`theater_id`, `name`, `total_seats`) VALUES
(1, '1관', 100),
(2, '2관', 120),
(3, 'IMAX관', 150);


-- -----------------------------------------------------------------------------
-- 요구사항 4: schedules 테이블 데이터 삽입
-- 영화와 상영관을 매핑하여 오늘(2026-05-30)과 내일(2026-05-31) 날짜에 상영하는 스케줄 총 10개 생성
-- 영화 러닝타임 및 상영관이 겹치지 않도록 현실적인 시간대로 배치
-- -----------------------------------------------------------------------------
INSERT INTO `schedules` (`schedule_id`, `movie_id`, `theater_id`, `start_time`, `end_time`, `price`) VALUES
-- 2026-05-30 (오늘) 상영 일정 (5개)
(1, 1, 1, '2026-05-30 10:00:00', '2026-05-30 12:00:00', 14000.00), -- 범죄도시5 (1관) - 조조/오전
(2, 3, 1, '2026-05-30 13:00:00', '2026-05-30 15:45:00', 15000.00), -- 인터스텔라 (1관) - 오후
(3, 5, 1, '2026-05-30 17:00:00', '2026-05-30 18:57:00', 13000.00), -- 러브레터 (1관) - 저녁
(4, 1, 2, '2026-05-30 14:00:00', '2026-05-30 16:00:00', 14000.00), -- 범죄도시5 (2관) - 오후
(5, 3, 3, '2026-05-30 19:00:00', '2026-05-30 21:45:00', 22000.00), -- 인터스텔라 (IMAX관) - 심야/황금시간대

-- 2026-05-31 (내일) 상영 일정 (5개)
(6, 1, 1, '2026-05-31 10:00:00', '2026-05-31 12:00:00', 14000.00), -- 범죄도시5 (1관)
(7, 3, 1, '2026-05-31 13:00:00', '2026-05-31 15:45:00', 15000.00), -- 인터스텔라 (1관)
(8, 5, 2, '2026-05-31 15:00:00', '2026-05-31 16:57:00', 13000.00), -- 러브레터 (2관)
(9, 1, 3, '2026-05-31 12:00:00', '2026-05-31 14:00:00', 20000.00), -- 범죄도시5 (IMAX관)
(10, 3, 3, '2026-05-31 18:00:00', '2026-05-31 20:45:00', 22000.00); -- 인터스텔라 (IMAX관)


-- -----------------------------------------------------------------------------
-- 요구사항 5: seats 테이블 데이터 삽입
-- 1관(theater_id=1)의 A열 1~10번, B열 1~10번 좌석 등록 (총 20개 좌석)
-- -----------------------------------------------------------------------------
INSERT INTO `seats` (`seat_id`, `theater_id`, `seat_row`, `seat_number`) VALUES
-- A열 1~10번 좌석
(1, 1, 'A', 1),
(2, 1, 'A', 2),
(3, 1, 'A', 3),
(4, 1, 'A', 4),
(5, 1, 'A', 5),
(6, 1, 'A', 6),
(7, 1, 'A', 7),
(8, 1, 'A', 8),
(9, 1, 'A', 9),
(10, 1, 'A', 10),
-- B열 1~10번 좌석
(11, 1, 'B', 1),
(12, 1, 'B', 2),
(13, 1, 'B', 3),
(14, 1, 'B', 4),
(15, 1, 'B', 5),
(16, 1, 'B', 6),
(17, 1, 'B', 7),
(18, 1, 'B', 8),
(19, 1, 'B', 9),
(20, 1, 'B', 10);


-- -----------------------------------------------------------------------------
-- 요구사항 6: reservations 및 reservation_seats 테이블 데이터 삽입
-- 일반 고객이 특정 스케줄의 좌석을 성공적으로 예매한 시나리오 데이터 3건 생성
-- 
-- 시나리오 1: customer1 (user_id=3)이 오늘 10시 범죄도시5 (schedule_id=1) 상영 일정을
--            1관의 명당 자리 A열 1번, 2번 (seat_id 1, 2)으로 예매.
--            티켓 단가 14,000원 x 2명 = 총 28,000원 결제 완료.
-- 
-- 시나리오 2: customer2 (user_id=4)가 오늘 13시 인터스텔라: 리본 (schedule_id=2) 상영 일정을
--            1관의 B열 5번 (seat_id 15) 좌석으로 예매.
--            티켓 단가 15,000원 x 1명 = 총 15,000원 결제 완료.
-- 
-- 시나리오 3: customer3 (user_id=5)이 오늘 17시 러브레터: 재개봉 (schedule_id=3) 상영 일정을
--            1관의 A열 9번, 10번 (seat_id 9, 10) 커플석 느낌으로 예매.
--            티켓 단가 13,000원 x 2명 = 총 26,000원 결제 완료.
-- -----------------------------------------------------------------------------

-- 6-1. reservations 테이블 삽입 (총 3건의 성공적인 예매 생성)
INSERT INTO `reservations` (`reservation_id`, `user_id`, `schedule_id`, `total_price`, `status`, `reserved_at`) VALUES
(1, 3, 1, 28000.00, 'CONFIRMED', '2026-05-30 08:15:30'), -- 시나리오 1
(2, 4, 2, 15000.00, 'CONFIRMED', '2026-05-30 11:20:10'), -- 시나리오 2
(3, 5, 3, 26000.00, 'CONFIRMED', '2026-05-30 15:40:22'); -- 시나리오 3

-- 6-2. reservation_seats 테이블 삽입 (예매 건별 선택한 좌석 매핑)
INSERT INTO `reservation_seats` (`reservation_seat_id`, `reservation_id`, `seat_id`) VALUES
-- 시나리오 1 (A1, A2 좌석 매핑)
(1, 1, 1), -- reservation_id=1, seat_id=1 (A열 1번)
(2, 1, 2), -- reservation_id=1, seat_id=2 (A열 2번)

-- 시나리오 2 (B5 좌석 매핑)
(3, 2, 15), -- reservation_id=2, seat_id=15 (B열 5번)

-- 시나리오 3 (A9, A10 좌석 매핑)
(4, 3, 9),  -- reservation_id=3, seat_id=9 (A열 9번)
(5, 3, 10); -- reservation_id=3, seat_id=10 (A열 10번)

-- 트랜잭션 정상 종료 및 반영
COMMIT;

-- -----------------------------------------------------------------------------
-- [테스트 & 검증용 쿼리 모음] - 쿼리가 잘 동작하는지 확인할 수 있는 샘플 셀렉트문들
-- -----------------------------------------------------------------------------
/*
-- 1. 전체 영화 목록과 현재 상영 상태 조회
SELECT title, genre, duration, release_date, status FROM movies;

-- 2. 상영 스케줄별 예매 현황 (영화 정보, 관 명칭, 시작 시간, 예매 완료 수량)
SELECT 
    m.title AS '영화명',
    t.name AS '상영관',
    s.start_time AS '시작시간',
    COUNT(rs.reservation_seat_id) AS '예매된좌석수'
FROM schedules s
JOIN movies m ON s.movie_id = m.movie_id
JOIN theaters t ON s.theater_id = t.theater_id
LEFT JOIN reservations r ON s.schedule_id = r.schedule_id AND r.status = 'CONFIRMED'
LEFT JOIN reservation_seats rs ON r.reservation_id = rs.reservation_id
GROUP BY s.schedule_id;

-- 3. 특정 고객(홍길동)의 상세 예매 내역 및 좌석 정보 조회
SELECT 
    u.name AS '고객명',
    m.title AS '영화명',
    t.name AS '상영관',
    s.start_time AS '상영시간',
    GROUP_CONCAT(CONCAT(se.seat_row, '열 ', se.seat_number, '번') ORDER BY se.seat_id) AS '선택좌석',
    r.total_price AS '결제금액',
    r.reserved_at AS '예매시간'
FROM reservations r
JOIN users u ON r.user_id = u.user_id
JOIN schedules s ON r.schedule_id = s.schedule_id
JOIN movies m ON s.movie_id = m.movie_id
JOIN theaters t ON s.theater_id = t.theater_id
JOIN reservation_seats rs ON r.reservation_id = rs.reservation_id
JOIN seats se ON rs.seat_id = se.seat_id
WHERE u.login_id = 'customer1'
GROUP BY r.reservation_id;
*/
