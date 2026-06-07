package com.movie.system.main;

import com.movie.system.config.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        System.out.println("MySQL 데이터베이스 연결 테스트를 시작합니다.");
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("MySQL 데이터베이스 연결 성공");
                System.out.println("연결된 DB 이름: " + conn.getCatalog());
            } else {
                System.err.println("데이터베이스 연결 객체가 비어있거나 닫혀 있습니다.");
            }
        } catch (SQLException e) {
            System.err.println("데이터베이스 연결 실패");
            System.err.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
