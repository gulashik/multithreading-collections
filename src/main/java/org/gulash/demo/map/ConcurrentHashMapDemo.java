package org.gulash.demo.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Демонстрация работы с {@link ConcurrentHashMap}.
 *
 * <p><b>Суть:</b> Это потокобезопасная реализация Hash-таблицы, которая позволяет достичь высокой
 * производительности за счет разделения данных на сегменты (в ранних версиях) или использования CAS
 * и синхронизации на уровне отдельных корзин (в современных версиях Java).
 *
 * <p><b>Особенности:</b>
 * <ul>
 *   <li>Не блокирует всю карту при чтении.</li>
 *   <li>Запись может происходить параллельно в разные "корзины" (buckets).</li>
 *   <li>Не допускает {@code null} в качестве ключа или значения.</li>
 *   <li>Итераторы являются weak-consistent (слабо согласованными) и не выбрасывают
 *       {@code ConcurrentModificationException}.</li>
 * </ul>
 *
 * <p><b>Best Practices:</b>
 * <ul>
 *   <li>Используйте атомарные методы: {@code putIfAbsent}, {@code computeIfPresent}, {@code merge}
 *       вместо комбинации {@code contains} + {@code put}.</li>
 *   <li>Не полагайтесь на размер {@code size()} для точной логики управления потоками, так как
 *       он вычисляется приблизительно.</li>
 * </ul>
 *
 * <p><b>Подводные камни:</b>
 * <ul>
 *   <li>Методы вроде {@code size()} или {@code isEmpty()} имеют линейную сложность в некоторых
 *       случаях и могут быть неточными в момент выполнения.</li>
 *   <li>Агрегатные функции (например, {@code putAll}) не являются атомарными по отношению ко всей карте.</li>
 *   <li>Метод {@code compute} и его вариации блокируют конкретную "корзину" (bucket), поэтому
 *       вычисления внутри лямбды должны быть быстрыми и не блокирующими.</li>
 * </ul>
 */
public class ConcurrentHashMapDemo {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentHashMapDemo.class);
    private final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ConcurrentHashMapDemo demo = new ConcurrentHashMapDemo();
        demo.demonstrateAtomicUpdate();
    }

    /**
     * Демонстрирует атомарное обновление значения.
     * Мы используем {@code merge}, чтобы безопасно инкрементировать счетчик из разных потоков.
     * Атомарность гарантируется на уровне ключа.
     */
    public void demonstrateAtomicUpdate() {
        log.info("--- Atomic Update Demonstration ---");
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> {
                // merge(key, value, remappingFunction) - потокобезопасно
                map.merge("counter", 1, Integer::sum);
            });
        }

        executor.shutdown();
        try {
            if (executor.awaitTermination(1, TimeUnit.MINUTES)) {
                log.info("Final counter value: {}", map.get("counter"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
