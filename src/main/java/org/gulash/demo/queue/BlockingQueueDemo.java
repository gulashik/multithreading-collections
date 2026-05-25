package org.gulash.demo.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Демонстрация работы с реализациями {@link BlockingQueue}.
 *
 * <p>В этом классе рассматриваются:
 * <ul>
 *   <li>{@link ArrayBlockingQueue}: Очередь на базе массива фиксированного размера.</li>
 *   <li>{@link LinkedBlockingQueue}: Очередь на базе связанных узлов, может быть ограниченной или нет.</li>
 *   <li>{@link PriorityBlockingQueue}: Безграничная очередь с приоритетами.</li>
 * </ul>
 *
 * <h3>ArrayBlockingQueue</h3>
 * <p><b>Суть:</b> Классический кольцевой буфер. Предварительно выделяет память под массив.
 * <p><b>Особенности:</b> Фиксированный размер, опциональная честность (fairness).
 *
 * <h3>LinkedBlockingQueue</h3>
 * <p><b>Суть:</b> Использует раздельные замки (locks) для вставки и удаления, что улучшает
 * параллелизм по сравнению с {@code ArrayBlockingQueue}.
 * <p><b>Best Practice:</b> Всегда указывайте {@code capacity}, чтобы избежать переполнения памяти
 * (по умолчанию {@code Integer.MAX_VALUE}).
 *
 * <h3>PriorityBlockingQueue</h3>
 * <p><b>Суть:</b> Элементы извлекаются согласно их естественному порядку или компаратору.
 * <p><b>Подводный камень:</b> Если поток-производитель работает быстрее потребителя,
 * очередь может расти бесконечно, пока не закончится память (OOM).
 *
 */
public class BlockingQueueDemo {
    private static final Logger log = LoggerFactory.getLogger(BlockingQueueDemo.class);

    public static void main(String[] args) throws InterruptedException {
        BlockingQueueDemo demo = new BlockingQueueDemo();
        demo.demonstrateArrayBlockingQueue();
        demo.demonstratePriorityQueue();
    }

    /**
     * Демонстрирует поведение блокировки при заполнении очереди (ArrayBlockingQueue).
     */
    public void demonstrateArrayBlockingQueue() throws InterruptedException {
        log.info("Demonstrating ArrayBlockingQueue (capacity 2)...");
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2);

        queue.put(1);
        queue.put(2);

        log.info("Queue is full. Next put() will block.");

        Thread producer = new Thread(() -> {
            try {
                log.info("Attempting to put 3...");
                queue.put(3);
                log.info("Successfully put 3");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        Thread.sleep(1000);

        log.info("Taking element: {}", queue.take());
        producer.join();
    }

    /**
     * Демонстрирует работу PriorityBlockingQueue.
     */
    public void demonstratePriorityQueue() throws InterruptedException {
        log.info("Demonstrating PriorityBlockingQueue...");
        BlockingQueue<Integer> queue = new PriorityBlockingQueue<>();

        queue.put(50);
        queue.put(10);
        queue.put(30);

        log.info("Taking elements from PriorityQueue (expected order 10, 30, 50):");
        log.info("Take: {}", queue.take());
        log.info("Take: {}", queue.take());
        log.info("Take: {}", queue.take());
    }
}
