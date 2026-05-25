package org.gulash.demo.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.Queue;

/**
 * Демонстрация работы с {@link ConcurrentLinkedQueue} и {@link LinkedBlockingDeque}.
 *
 * <h3>ConcurrentLinkedQueue</h3>
 * <p><b>Суть:</b> Эффективная неблокирующая (wait-free/lock-free) очередь на базе связанных узлов,
 * использующая CAS (Compare-And-Swap) операции.
 * <p><b>Особенности:</b> Безграничная, очень высокая производительность при малом количестве коллизий.
 * <p><b>Подводный камень:</b> {@code size()} имеет сложность O(n). Никогда не используйте
 * {@code queue.size() > 0}, используйте {@code !queue.isEmpty()}.
 *
 * <h3>LinkedBlockingDeque</h3>
 * <p><b>Суть:</b> Двусторонняя очередь (Double-Ended Queue), которая может блокировать потоки.
 * <p><b>Особенности:</b> Позволяет вставлять и извлекать элементы с обоих концов (LIFO/FIFO).
 * Опционально ограниченная.
 */
public class NonBlockingAndDequeDemo {
    private static final Logger log = LoggerFactory.getLogger(NonBlockingAndDequeDemo.class);

    public static void main(String[] args) throws InterruptedException {
        NonBlockingAndDequeDemo demo = new NonBlockingAndDequeDemo();
        demo.demonstrateNonBlockingQueue();
        demo.demonstrateDeque();
    }

    /**
     * Демонстрирует работу ConcurrentLinkedQueue.
     */
    public void demonstrateNonBlockingQueue() {
        log.info("Demonstrating ConcurrentLinkedQueue...");
        Queue<String> queue = new ConcurrentLinkedQueue<>();

        queue.offer("task1");
        queue.offer("task2");

        log.info("Polled: {}", queue.poll());
        log.info("Polled: {}", queue.poll());

        log.info("Is empty: {}", queue.isEmpty());
    }

    /**
     * Демонстрирует работу LinkedBlockingDeque как стека и как очереди.
     */
    public void demonstrateDeque() throws InterruptedException {
        log.info("Demonstrating LinkedBlockingDeque... Simple");
        BlockingDeque<String> deque = new LinkedBlockingDeque<>(5);

        // Работаем как со стеком (LIFO)
        log.info("Using Deque as Stack (LIFO):");
        deque.push("1");
        deque.push("2");
        log.info("Popped: {}", deque.pop()); // 2
        log.info("Popped: {}", deque.pop()); // 1

        // Работаем как с очередью (FIFO)
        log.info("Using Deque as Queue (FIFO):");
        deque.offerLast("10");
        deque.offerLast("20");
        log.info("Took: {}", deque.takeFirst()); // 10
        log.info("Took: {}", deque.takeFirst()); // 20


        log.info("Demonstrating LinkedBlockingDeque... Producer-Consumer");
        // Producer: put() заблокируется если в очереди 5 элементов
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    String task = "task-" + i;
                    deque.put(task);             // блокирует если полна
                    log.info("Produced: {}", task);
                }
                deque.put("STOP");              // сигнал завершения
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer: take() заблокируется если очередь пуста
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String task = deque.take();   // блокирует если пуста
                    if ("STOP".equals(task)) break;
                    log.info("Consumed: {}", task);
                    Thread.sleep(100);           // имитируем обработку
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
    }
}
