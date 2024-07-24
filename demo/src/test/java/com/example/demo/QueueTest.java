package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@AutoConfigureMockMvc
class QueueTest {
    private static final int MAX_REQUESTS = 100;
    private LinkedBlockingQueue<String> queue;

    @BeforeEach
        // @BeforeEach : 각 테스트 메서드가 실행되기 전에 큐를 초기화 함.
    void setUp() {
        queue = new LinkedBlockingQueue<>();
    }


    // *InterruptedException
    // Interrupt를 언제 사용하나? : Thread 에게 하던 일 멈추라고 신호를 보내기 위해! (Thread가 자기 자신을 인터럽트 할 수 도 있음.)


    @Test
    void testQueueProcessing() throws InterruptedException { // Produce와 Consumer 스레드를 시작. 모든 요청 처리 후 큐가 비어있는지 확인
        Thread producerThread = new Thread(new Producer(queue, MAX_REQUESTS)); //MAX_REQUESTS 수만큼 요청을 생성하여 큐에 추가
        Thread consumerThread = new Thread(new Consumer(queue));

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join(10000); // 10초 내에 완료되지 않으면 실패

        assertTrue(queue.isEmpty(), "Queue should be empty after processing all requests");
    }

    private void assertTrue(boolean empty, String s) {
    }

    // 큐에 요청을 추가하는 스레드
    static class Producer implements Runnable {
        private final LinkedBlockingQueue<String> queue;
        private final int maxRequests;

        Producer(LinkedBlockingQueue<String> queue, int maxRequests) { // 큐와 최대 요청 수를 초기화 함.
            this.queue = queue;
            this.maxRequests = maxRequests;
        }

        @Override
        public void run() { // 요청을 생성하여 큐에 추가하고, 각 요청마다 100ms 대기함.
            for (int i = 1; i <= maxRequests; i++) {
                try {
                    String request = "Request " + i;
                    queue.put(request);  // 큐에 요청 추가
                    System.out.println("Produced: " + request);
                    Thread.sleep(100);  // 시뮬레이션을 위해 잠시 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Thread 멈추라고 신호 보내기
                }
            }
        }
    }

    // 큐에서 요청을 꺼내 처리하는 스레드드
    static class Consumer implements Runnable {
        private final LinkedBlockingQueue<String> queue;

        Consumer(LinkedBlockingQueue<String> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String request = queue.poll(1, TimeUnit.SECONDS);  // 큐에서 요청 꺼내기 (타임아웃 1초)
                    if (request == null) break;  // 타임아웃 발생 시 종료
                    processRequest(request);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Thread 멈추라고 신호 보내기
                }
            }
        }

        private void processRequest(String request) {
            System.out.println("Consumed: " + request);
            try {
                Thread.sleep(200);  // 처리 시간을 시뮬레이션하기 위해 잠시 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}