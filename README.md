# Java Concurrent Collections Demo Project

Проект охватывает следующие коллекции:
- `ConcurrentHashMap`
- `CopyOnWriteArrayList`
- `ArrayBlockingQueue`
- `LinkedBlockingQueue`
- `PriorityBlockingQueue`
- `ConcurrentLinkedQueue`
- `LinkedBlockingDeque`

## Основные принципы (Best Practices)
- **Атомарность:** Используйте атомарные операции (`compute`, `merge`, `putIfAbsent`) вместо последовательного вызова `contains` и `put`.
- **Производительность:** Выбирайте коллекцию исходя из профиля нагрузки (чтение vs запись). Например, `CopyOnWriteArrayList` идеален для частого чтения и редкой записи.
- **Ограничения:** Всегда ограничивайте размер блокирующих очередей (`capacity`), чтобы избежать `OutOfMemoryError`.
- **Масштабируемость:** `ConcurrentHashMap` использует разделение на корзины, что позволяет множеству потоков писать одновременно.
- **Точность:** Помните, что `size()` у конкурентных коллекций часто имеет сложность O(n) или является приблизительным.
- **Параллелизм:** Используйте методы параллельной агрегации в `ConcurrentHashMap` (например, `reduce`, `search`) для эффективной обработки больших объемов данных.
