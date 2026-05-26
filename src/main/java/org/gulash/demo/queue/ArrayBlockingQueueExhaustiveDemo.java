package org.gulash.demo.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Исчерпывающее руководство по {@link ArrayBlockingQueue}.
 *
 * <h3>Суть и предназначение</h3>
 * <p>{@code ArrayBlockingQueue} — это классическая реализация ограниченной (bounded) очереди
 * на базе кольцевого массива. Она работает по принципу FIFO (First-In-First-Out).
 * При создании очереди обязательно указывается её ёмкость (capacity), которая не может быть изменена в дальнейшем.
 *
 * <h3>Особенности и настройки</h3>
 * <ul>
 *   <li><b>Fairness (Честность):</b> Можно передать параметр {@code fair} в конструктор.
 *   Если {@code true}, то потоки, ожидающие доступа, будут обслуживаться в порядке очереди (FIFO).
 *   Это уменьшает пропускную способность, но предотвращает "голодание" (starvation) потоков.</li>
 *   <li><b>Блокирующие операции:</b> {@code put()} и {@code take()} блокируют поток, если очередь полна или пуста соответственно.</li>
 * </ul>
 *
 * <h3>Best Practices</h3>
 * <ul>
 *   <li>Всегда используйте {@code offer()} с таймаутом, если не хотите вечной блокировки.</li>
 *   <li>Для массовой выгрузки элементов используйте {@code drainTo()}, это значительно быстрее, чем поочередный {@code take()}.</li>
 *   <li>Выбирайте {@code capacity} исходя из ожидаемой нагрузки и доступной памяти.</li>
 * </ul>
 *
 * <h3>Подводные камни и частые проблемы</h3>
 * <ul>
 *   <li><b>Фиксированный размер:</b> Если вы ошиблись с размером, очередь станет узким местом системы.</li>
 *   <li><b>Производительность Fairness:</b> Включение честности сильно бьет по скорости (иногда в 10 раз и более).</li>
 *   <li><b>Итераторы:</b> Итераторы слабо-согласованные (weakly consistent), они не бросают {@code ConcurrentModificationException}.</li>
 * </ul>
 *
 */
public class ArrayBlockingQueueExhaustiveDemo {
    private static final Logger log = LoggerFactory.getLogger(ArrayBlockingQueueExhaustiveDemo.class);

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting ArrayBlockingQueue Exhaustive Demo...");
        
        ArrayBlockingQueueExhaustiveDemo demo = new ArrayBlockingQueueExhaustiveDemo();
        
        demo.demonstrateBasicOperations();
        demo.demonstrateBlockingMethods();
        demo.demonstrateTimeoutMethods();
        demo.demonstrateBulkOperations();
        demo.demonstrateFairness();

        log.info("Demo finished.");
    }

    /**
     * Группа 1: Базовые методы (не блокирующие или выбрасывающие исключения).
     * 
     * <p><b>add(e):</b> Добавляет элемент. Бросает IllegalStateException, если очередь полна.
     * <p><b>offer(e):</b> Добавляет элемент. Возвращает false, если очередь полна.
     * <p><b>remove():</b> Извлекает голову. Бросает NoSuchElementException, если пуста.
     * <p><b>poll():</b> Извлекает голову. Возвращает null, если пуста.
     * <p><b>element():</b> Смотрит голову. Бросает NoSuchElementException, если пуста.
     * <p><b>peek():</b> Смотрит голову. Возвращает null, если пуста.
     */
    public void demonstrateBasicOperations() {
        log.info("--- 1. Basic Operations ---");
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);

        log.info("Adding 'First': {}", queue.offer("First")); // true
        log.info("Offering 'Second' (queue full): {}", queue.offer("Second")); // false

        try {
            queue.add("Third");
        } catch (IllegalStateException e) {
            log.warn("add() failed as expected: Queue full");
        }

        log.info("Peek head: {}", queue.peek()); // First
        log.info("Poll head: {}", queue.poll()); // First
        log.info("Poll from empty: {}", queue.poll()); // null
    }

    /**
     * Группа 2: Блокирующие методы (самые важные для многопоточности).
     * 
     * <p><b>put(e):</b> Ждет освобождения места.
     * <p><b>take():</b> Ждет появления элемента.
     * 
     * <p>Эти методы — основа паттерна Producer-Consumer.
     */
    public void demonstrateBlockingMethods() throws InterruptedException {
        log.info("--- 2. Blocking Methods (put/take) ---");
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2);

        Thread consumer = new Thread(() -> {
            try {
                log.info("[Consumer] Waiting for element...");
                Integer item = queue.take(); // Заблокируется, пока не появится элемент
                log.info("[Consumer] Received: {}", item);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer.start();
        Thread.sleep(1000); // Даем потребителю "заснуть" на пустой очереди
        
        log.info("[Producer] Putting 100...");
        queue.put(100); // Заблокируется, пока очередь заполнена
        consumer.join();
    }

    /**
     * Группа 3: Методы с таймаутом.
     * 
     * <p>Позволяют избежать вечной блокировки, что критично для систем.
     */
    public void demonstrateTimeoutMethods() throws InterruptedException {
        log.info("--- 3. Timeout Operations ---");
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        queue.put("Occupied");

        log.info("Attempting offer with timeout (wait 2s)...");
        boolean success = queue.offer("New Element", 2, TimeUnit.SECONDS);
        log.info("Offer success: {}", success); // false, так как очередь занята

        log.info("Attempting poll with timeout (wait 1s)...");
        queue.poll(); // освобождаем
        String item = queue.poll(1, TimeUnit.SECONDS);
        log.info("Poll result: {}", item); // null, так как уже пусто
    }

    /**
     * Группа 4: Массовые операции.
     * 
     * <p><b>drainTo(collection):</b> Перемещает все доступные элементы в другую коллекцию.
     * Это гораздо эффективнее в плане производительности (уменьшает количество захватов блокировок).
     */
    public void demonstrateBulkOperations() throws InterruptedException {
        log.info("--- 4. Bulk Operations ---");
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
        for (int i = 0; i < 5; i++) queue.put(i);

        List<Integer> batch = new ArrayList<>();
        int drained = queue.drainTo(batch);
        log.info("Drained {} elements: {}", drained, batch);
        log.info("Queue size after drain: {}", queue.size());
    }

    /**
     * Группа 5: Параметр Fair (Честность).
     * 
     * <p>По умолчанию fair = false.
     * Если fair = true, гарантируется порядок FIFO для ожидающих потоков.
     */
    public void demonstrateFairness() {
        log.info("--- 5. Fairness ---");
        // Создание очереди с включенным параметром честности
        BlockingQueue<Integer> fairQueue = new ArrayBlockingQueue<>(5, true);
        log.info("Fair queue created. Is fair: {}, size: {}", true, fairQueue.size());
        
        // В реальном приложении это повлияет на то, какой из многих ждущих 
        // потоков-писателей первым запишет данные, когда место освободится.
    }
}
