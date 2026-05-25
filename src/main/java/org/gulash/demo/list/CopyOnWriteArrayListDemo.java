package org.gulash.demo.list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Демонстрация работы с {@link CopyOnWriteArrayList}.
 *
 * <p><b>Суть:</b> Потокобезопасная реализация {@link List}, в которой любая операция изменения
 * (add, set, remove) приводит к созданию новой копии базового массива.
 *
 * <p><b>Особенности:</b>
 * <ul>
 *   <li>Чтение очень быстрое и не требует блокировок.</li>
 *   <li>Изменение очень дорогое из-за копирования всего массива.</li>
 *   <li>Итераторы работают со снимком данных на момент создания и не отражают последующие изменения.</li>
 *   <li>Итераторы не поддерживают операцию {@code remove()}.</li>
 * </ul>
 *
 * <p><b>Best Practices:</b>
 * <ul>
 *   <li>Используйте, когда количество операций чтения значительно превышает количество операций записи.</li>
 *   <li>Идеально подходит для списков слушателей (listeners) или кэшей, которые редко меняются.</li>
 *   <li>Избегайте использования для больших списков с частыми обновлениями.</li>
 * </ul>
 *
 * <p><b>Подводные камни:</b>
 * <ul>
 *   <li>Потребление памяти: каждое изменение создает новый массив, что может привести к частым сборкам мусора.</li>
 * </ul>
 */
public class CopyOnWriteArrayListDemo {
    private static final Logger log = LoggerFactory.getLogger(CopyOnWriteArrayListDemo.class);
    private final List<Integer> list = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        CopyOnWriteArrayListDemo demo = new CopyOnWriteArrayListDemo();
        demo.demonstrateSafeIteration();
    }

    /**
     * Демонстрирует безопасность итерации при одновременном изменении.
     * В обычной {@link java.util.ArrayList} это вызвало бы {@code ConcurrentModificationException}.
     */
    public void demonstrateSafeIteration() {
        log.info("Starting CopyOnWriteArrayList safe iteration demonstration...");
        list.add(1);
        list.add(2);
        list.add(3);

        // Получим только текущие значения 1, 2, 3
        Iterator<Integer> it = list.iterator();

        // Поток на запись
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            list.add(4);
            log.info("Added 4 to list");
        });

        // Итерация - мы увидим только 1, 2, 3
        while (it.hasNext()) {
            Integer value = it.next();
            log.info("Iterator read value: {}", value);
            // Пытаемся имитировать задержку
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }

        executor.shutdown();
        log.info("Current list state: {}", list);
    }
}
