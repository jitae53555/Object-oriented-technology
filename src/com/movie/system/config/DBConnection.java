package com.movie.system.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DBConnection {
    private static final Map<String, String> env = new HashMap<>();
    private static boolean isEnvLoaded = false;
    private static boolean isTableInitialized = false;

    // .env 파일을 파싱하는 커스텀 메서드
    private static synchronized void loadEnv() {
        if (isEnvLoaded) return;

        File envFile = new File(".env");
        if (!envFile.exists()) {
            System.err.println("[오류] .env 파일을 찾을 수 없습니다. 기본값을 사용하거나 프로젝트 루트에 생성해 주세요.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 주석(#)이나 빈 줄은 무시
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();
                    env.put(key, value);
                }
            }
            isEnvLoaded = true;
        } catch (IOException e) {
            System.err.println("[오류] .env 파일을 읽는 도중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 환경 변수 값을 가져오는 메서드
    public static String getEnv(String key, String defaultValue) {
        loadEnv();
        return env.getOrDefault(key, defaultValue);
    }

    // MySQL 데이터베이스 Connection 객체를 반환하는 메서드
    public static Connection getConnection() throws SQLException {
        loadEnv();

        String url = env.get("DB_URL");
        String user = env.get("DB_USER");
        String password = env.get("DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new SQLException("[오류] .env 파일에 DB 접속 정보(DB_URL, DB_USER, DB_PASSWORD)가 누락되었습니다.");
        }

        try {
            // MySQL JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("[오류] MySQL JDBC 드라이버를 찾을 수 없습니다. 클래스패스를 확인하세요.", e);
        }

        Connection conn = DriverManager.getConnection(url, user, password);
        
        // 테이블 자동 생성 및 초기화 수행 (최초 1회)
        if (!isTableInitialized) {
            initializeTables(conn);
            isTableInitialized = true;
        }

        return conn;
    }

    // 영화 시스템에 필요한 테이블들을 자동으로 생성하는 메서드
    private static void initializeTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // movies 테이블 생성 (현재 상영 영화 제목 저장용)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS movies (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  title VARCHAR(100) NOT NULL" +
                ")"
            );

            // reservations 테이블 생성 (영화 예매 목록 저장용)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS reservations (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  movie_title VARCHAR(100) NOT NULL," +
                "  student_id VARCHAR(50) NOT NULL," +
                "  seat VARCHAR(10) NOT NULL" +
                ")"
            );

            // winners 테이블 생성 (경품 추첨 당첨자 저장용)
            stmt.executeUpdate("DROP TABLE IF EXISTS winners");
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS winners (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  movie_title VARCHAR(100) NOT NULL," +
                "  student_id VARCHAR(50) NOT NULL," +
                "  seat VARCHAR(10) NOT NULL" +
                ")"
            );

            // 만약 movies 테이블이 비어있다면 초기 영화 데이터("어벤져스") 삽입
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM movies")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO movies (title) VALUES ('어벤져스')");
                }
            }
        } catch (SQLException e) {
            System.err.println("[오류] 데이터베이스 테이블 자동 초기화 실패: " + e.getMessage());
        }
    }
}
