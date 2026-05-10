package com.movie.system.main;

import java.util.Random;

public class Reward {
    public static void main(String[] args) {

        // 1. 관객 이름 준비
        String[] players = { "김철수", "이영희", "박민수", "최지우", "정다정" };
        Random random = new Random();

        System.out.println("=== 🎁 경품 추첨 시작! ===\n");

        // 2. 한 명씩 확인 (반복문)
        for (int i = 0; i < players.length; i++) {
            String name = players[i];

            // 첫 번째 조건: 예매 여부 (50% 확률)
            if (random.nextBoolean()) {
                System.out.println("[확인] " + name + "님은 영화를 보셨네요!");

                // 두 번째 조건: 당첨 여부 (예매자 중 다시 50% 확률)
                if (random.nextBoolean()) {
                    System.out.println("   ㄴ 🎉 축하합니다! 경품에 당첨되셨습니다!");
                } else {
                    System.out.println("   ㄴ 아쉽지만 다음 기회에..");
                }
            } else {
                System.out.println("[확인] " + name + "님은 예매 기록이 없습니다.");
            }

            System.out.println("---------------------------");
        }
    }
}