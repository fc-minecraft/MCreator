# План — Advanced Transport Element Features (FabricMC)

> Все задачи, идеи и технические детали по разработке элемента Transport для MCreator Fabric 1.21.8.

---

## ✅ Уже реализовано (Фаза 1)

- [x] Новые поля данных в `Transport.java` (maxHealth, planeMechanics, helicopterMechanics, maxAltitude, enableCrash, crashSpeed, enableFuel, fuelCapacity, fuelConsumption, spinParts, spinSpeed)
- [x] Список допустимых предметов топлива `fuelItems` (`List<FuelEntry>`) с настраиваемым количеством единиц
- [x] GUI страница "Flight & Fuel" в `TransportGUI.java` — двухколоночная раскладка
- [x] Редактор списка топлива `FuelItemsEditorView` с MCItemHolder и JSpinner на каждую строку
- [x] Комбобокс "Flight Mode" (None, Plane, Helicopter) вместо чекбоксов — только для типа AIR
- [x] Отключение управления полётом если тип не AIR
- [x] Русские переводы в `texts_ru_RU.properties`, английские в `texts.properties`
- [x] Совместимость с предыдущими сохранениями через `IWorkspaceDependent.setWorkspace()`
- [x] Генератор кода `transport.java.ftl`:
  - Синхронизированные данные DATA_FUEL, DATA_ENGINE, DATA_THROTTLE
  - Самолётная механика (разгон/торможение тягой, планирование, нос вниз)
  - Вертолётная механика (зависание, подъём/спуск Space/Shift)
  - Топливная система: дозаправка ПКМ с предметом, вкл/выкл двигателя Shift+ПКМ с пустой рукой
  - Защита от случайного спешивания: Shift+Space 30 тиков
  - HUD: состояние двигателя, тяга, топливо
  - Звуки двигателя (цикличные), переключения, дозаправки
  - Crash physics: взрыв при столкновении выше порога скорости
- [x] `transport_renderer.java.ftl`: вращение spinParts костей по Y (ротор) и Z (пропеллер)
- [x] `finalizeModElementGeneration()` — генерация текстуры яйца спавна
- [x] Успешная компиляция проекта через Gradle

---

## ✅ Фаза 2 — РЕАЛИЗОВАНА

### Задача 1: Клавиши управления ✅
- [x] Поля `engineToggleKey` (default "F") и `dismountKey` (default "Q") в `Transport.java`
- [x] `JComboBox` выбора клавиш в `TransportGUI.java` (новая вкладка "Клавиши и физика")
- [x] `transport_keys.java.ftl` — client-side класс с `KeyBindingHelper.registerKeyBinding()`
- [x] `transport_packet.java.ftl` — C2S пакет (ACTION_ENGINE_TOGGLE, ACTION_DISMOUNT_HOLD, ACTION_DISMOUNT_RELEASE)
- [x] Регистрация клавиш через `entityrenderers.java.ftl` (вызов `${name}TransportKeys.register()`)
- [x] Регистрация пакетов через `entities.java.ftl` (PayloadTypeRegistry + ServerPlayNetworking)
- [x] `transport.definition.yaml` — добавлены два новых шаблона
- [x] Клавиши появляются в настройках Minecraft → Управление в категории по названию мода

### Задача 2: Русификация HUD ✅
- [x] Все сообщения HUD переведены на русский язык через `Component.literal()`
  - "Двигатель: ВКЛ / ВЫКЛ"
  - "Тяга: X%"
  - "Топливо: X% (N/M)"
  - "Заправлено! Топливо: N / M"
  - "Нет топлива!"
  - "Топливо кончилось! Двигатель заглох."
  - "Бак полон!"
  - "Выход: ████░░░░░░" (прогресс-бар)
  - Подсказки: [F - завести], [Q - выйти (держать)]

### Задача 3: Правильный выход из транспорта ✅
- [x] Выход только по клавише `dismountKey` (удержание 20 тиков = 1 секунда)
- [x] Пакет DISMOUNT_HOLD отправляется каждый тик клавиши
- [x] Сервер считает тики через `dismountHoldTicks`, watchdog-таймер сбрасывает если пакеты прекратились
- [x] Флаг `canDismount` управляет доступом в `removePassenger()`
- [x] `resetDismountCounter()` вызывается при DISMOUNT_RELEASE или после выхода
- [x] Прогресс-бар 10 символов █/░ показывается в actionbar

### Задача 4: Реалистичные механики ✅
**Самолёт:**
- [x] Скорость сваливания (`stallSpeed`) — ниже порога нос падает вниз (-0.15 Y/тик)
- [x] Настраиваемый разгон (`accelerationRate`) и торможение (`brakeFactor`)
- [x] Планирование без двигателя

**Вертолёт:**
- [x] Авторотация при выключении двигателя (медленное падение -0.08)
- [x] Демпфирование вертикального дрейфа при hover

**Земля/Вода:**
- [x] Настраиваемая инерция (`inertiaFactor`) — плавное торможение
- [x] При воде на суше — снижение скорости x5
- [x] Coast-to-stop с настраиваемым коэффициентом торможения

### Задача 5: Расширенные настройки ✅
- [x] Настройки HUD: `showEngineHUD`, `showFuelHUD`, `showThrottleHUD`, `showHints`
- [x] Расширенные настройки краша: `explosionRadius`, `crashDamageToPlayer`, `crashDropItems`
- [x] Все новые поля в UI (вкладка "Клавиши и физика")
- [x] Save/Load всех новых полей в `openInEditingMode` и `getElementFromGUI`

---

## 📁 Файлы изменённые в Фазе 2

| Файл | Статус |
|------|--------|
| `Transport.java` | ✅ +14 полей |
| `TransportGUI.java` | ✅ Новая вкладка "Клавиши и физика", save/load |
| `transport.java.ftl` | ✅ Полная перезапись: RU HUD, packet handler, физика |
| `transport_packet.java.ftl` | ✅ НОВЫЙ — C2S пакет управления |
| `transport_keys.java.ftl` | ✅ НОВЫЙ — Client-side клавиши |
| `transport.definition.yaml` | ✅ +2 шаблона |
| `entityrenderers.java.ftl` | ✅ Вызов `${name}TransportKeys.register()` |
| `entities.java.ftl` | ✅ Регистрация C2S пакетов |
| `texts_ru_RU.properties` | ✅ +18 новых ключей |
| `texts.properties` | ✅ +18 новых ключей |

---

## ⚠️ Известные ограничения / TODO

- **Category name**: Клавиши транспорта появятся в категории `key.categories.modid.transport` — нужно добавить этот ключ в lang-файл мода через Fabric (не критично — работает с fallback)
- **crashDropItems**: `spawnAtLocation` в текущей реализации требует `ServerLevel` — это обернуто корректно, но не тестировалось
- **Keybind conflict**: Если у двух транспортов одинаковый ключ — оба будут регистрировать свои keybinds в разных категориях, конфликта не будет
- **Клавиши из кода vs vanilla**: Vanilla Shift/Space не конфликтуют с новыми клавишами — они разные InputActions

## ✅ Фаза 2.5 — Исправления и полировка ✅

- [x] **Устранение мерцания HUD и Q**: Отрисовка HUD и шкалы выхода перенесена полностью на клиентскую часть (`transport_keys.java.ftl`). Теперь они обновляются синхронно, предотвращая наложение сообщений и мерцание.
- [x] **Умный показ подсказок**: Подсказка клавиши `Q` (выйти) теперь автоматически скрывается, когда двигатель запущен.
- [x] **Удобное управление самолетом**: Реализована динамическая подъемная сила (lift), компенсирующая гравитацию пропорционально скорости. При полете вперед (смотря прямо) самолет летит ровно и не падает камнем. При отпускании клавиш самолет переходит в плавное планирование вместо резкого падения.
- [x] **Исправление 3D предпросмотра моделей**: Переписан парсер Java-моделей в `TransportGUI.java`. Теперь он делит файл по точкам с запятой (statements), а не по строкам, что позволило успешно парсить даже те `addBox` вызовы, которые MCreator или Blockbench переносят на несколько строк.
- [x] **Исправление порядка вращения**: В 3D-вьюере изменен порядок вращения костей на Z -> Y -> X, что совпадает со стандартами Minecraft (ModelPart) и исправило искажения формы моделей (например, плавников акулы).
- [x] **Справка/Help система**: Сгенерированы 68 файлов справки (34 на русском в `help/ru_RU/transport/` и 34 на английском в `help/default/transport/`), что полностью убрало ошибки "Запись справки пока не определена".
- [x] **Локализация яйца призыва**: Ключ локализации яйца призыва `item.modid.registryname_spawn_egg` генерируется автоматически при пересохранении элемента, давая корректное название в игре.

---

## 🔮 Фаза 3 — Идеи на будущее (не срочно)

- [ ] Визуальный редактор посадочных мест прямо в превью 3D модели
- [ ] Анимации при посадке (приседание модели)
- [ ] Дым из выхлопа при работе двигателя (партиклы)
- [ ] Разрушаемость: несколько уровней повреждения (дым, огонь, взрыв)
- [ ] Настраиваемые звуки (engineStartSound, engineLoopSound, engineStopSound)
- [ ] Кастомный HUD через существующий Overlay элемент MCreator
- [ ] Proper lang file integration (Component.translatable вместо literal)
