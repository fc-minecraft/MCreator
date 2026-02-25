# -*- coding: utf-8 -*-
import os
import shutil

OUTPUT_DIR = "plugins/mcreator-localization/help/ru_RU/wiki"
LOC_FILE = "plugins/mcreator-localization/lang/texts_ru_RU.properties"

# Common CSS content
CSS_CONTENT = """
    body { font-family: 'Noto Sans', sans-serif; padding: 40px; line-height: 1.6; color: #333; background-color: #fff; max-width: 1000px; margin: 0 auto; font-size: 16px; }
    h1 { color: #2c3e50; border-bottom: 3px solid #eee; padding-bottom: 15px; margin-top: 0; font-size: 2.8em; }
    h2 { color: #2980b9; margin-top: 50px; border-bottom: 2px solid #eee; padding-bottom: 10px; font-size: 2.0em; }
    h3 { color: #34495e; margin-top: 35px; font-size: 1.5em; border-left: 5px solid #bdc3c7; padding-left: 10px; }
    h4 { color: #7f8c8d; margin-top: 25px; font-size: 1.2em; font-weight: bold; }
    p { margin-bottom: 15px; text-align: justify; }
    a { color: #2980b9; text-decoration: none; font-weight: bold; transition: color 0.3s; }
    a:hover { color: #c0392b; text-decoration: underline; }
    code { background-color: #f8f9fa; padding: 2px 6px; border: 1px solid #e1e4e8; border-radius: 4px; font-family: 'Consolas', monospace; color: #e83e8c; font-size: 0.95em; }
    pre { background-color: #f8f9fa; padding: 15px; border: 1px solid #e1e4e8; border-radius: 5px; overflow-x: auto; font-family: 'Consolas', monospace; }
    ul, ol { padding-left: 25px; margin-bottom: 20px; }
    li { margin-bottom: 8px; }
    .tip { background-color: #d1ecf1; color: #0c5460; padding: 20px; border-left: 5px solid #17a2b8; margin: 30px 0; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
    .warning { background-color: #fff3cd; color: #856404; padding: 20px; border-left: 5px solid #ffc107; margin: 30px 0; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
    .mc-param { font-weight: bold; color: #2c3e50; background-color: #eef2f5; padding: 2px 5px; border-radius: 3px; border: 1px solid #dee2e6; }
    table { width: 100%; border-collapse: collapse; margin: 25px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    th, td { border: 1px solid #ddd; padding: 15px; text-align: left; vertical-align: top; }
    th { background-color: #f8f9fa; color: #333; font-weight: bold; text-transform: uppercase; font-size: 0.9em; letter-spacing: 0.5px; }
    tr:nth-child(even) { background-color: #f9f9f9; }
    tr:hover { background-color: #f1f1f1; }
    .step-number { display: inline-block; width: 25px; height: 25px; background-color: #2980b9; color: white; border-radius: 50%; text-align: center; line-height: 25px; margin-right: 10px; font-weight: bold; }
    .nav-box { background-color: #f8f9fa; padding: 20px; border-radius: 10px; display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 15px; }
    .nav-item { background-color: white; padding: 15px; border-radius: 5px; border: 1px solid #eee; text-align: center; transition: transform 0.2s, box-shadow 0.2s; }
    .nav-item:hover { transform: translateY(-3px); box-shadow: 0 5px 15px rgba(0,0,0,0.1); border-color: #2980b9; }
    .proc-block { background-color: #f0f0f0; border-left: 5px solid #2ecc71; padding: 10px 15px; margin: 10px 0; font-family: 'Consolas', monospace; font-weight: bold; }
"""

PAGES = {
    "index": """
    <h1>Энциклопедия MCreator 🚀</h1>
    <p>Привет, друг! Добро пожаловать в полное руководство по созданию модов для Minecraft. Здесь мы разберем всё: от создания простого блока земли до сложнейших скриптов с магией.</p>
    <p>MCreator — это конструктор, который пишет код за тебя. Тебе нужно лишь придумать идею и собрать её как LEGO.</p>

    <div class="tip">
        <strong>Совет для новичка:</strong> Не пытайся сразу создать "Майнкрафт 2.0". Начни с простого: сделай новый меч, новую еду или блок. Когда разберешься, переходи к мобам и процедурам.
    </div>

    <h2>📚 Разделы</h2>

    <div class="nav-box">
        <div class="nav-item">
            <a href="how-make-block.html">🧱 Блок (Block)</a><br>
            <small>Строительные материалы, руды, механизмы</small>
        </div>
        <div class="nav-item">
            <a href="how-make-item.html">💎 Предмет (Item)</a><br>
            <small>Вещи в инвентаре, еда, пластинки</small>
        </div>
        <div class="nav-item">
            <a href="how-make-tool.html">⛏️ Инструмент (Tool)</a><br>
            <small>Кирки, Мечи, Топоры, Мульти-инструменты</small>
        </div>
        <div class="nav-item">
            <a href="how-make-armor.html">🛡️ Броня (Armor)</a><br>
            <small>Шлемы, нагрудники, ботинки</small>
        </div>
        <div class="nav-item">
            <a href="how-make-entity.html">🧟 Сущность (Entity)</a><br>
            <small>Мобы, Животные, Монстры, NPC</small>
        </div>
        <div class="nav-item">
            <a href="how-make-procedure.html">⚡ Процедуры (Procedures)</a><br>
            <small>Скрипты, Логика, События, Магия</small>
        </div>
        <div class="nav-item">
            <a href="how-make-biome.html">🌵 Биом (Biome)</a><br>
            <small>Леса, Пустыни, Генерация мира</small>
        </div>
        <div class="nav-item">
            <a href="how-make-dimension.html">🌌 Измерение (Dimension)</a><br>
            <small>Порталы в новые миры</small>
        </div>
        <div class="nav-item">
            <a href="how-make-recipe.html">📜 Рецепты (Recipe)</a><br>
            <small>Крафты в верстаке и печке</small>
        </div>
        <div class="nav-item">
            <a href="how-make-plant.html">🌻 Растение (Plant)</a><br>
            <small>Цветы, Урожай, Деревья</small>
        </div>
        <div class="nav-item">
            <a href="how-make-fluid.html">💧 Жидкость (Fluid)</a><br>
            <small>Вода, Лава, Нефть, Кислота</small>
        </div>
        <div class="nav-item">
            <a href="how-make-creative-inventory-tab.html">📂 Вкладка Креатива</a><br>
            <small>Своя группа предметов</small>
        </div>
        <div class="nav-item">
            <a href="how-make-structure.html">🏰 Структура</a><br>
            <small>Данжи, Дома, Руины</small>
        </div>
        <div class="nav-item">
            <a href="burn-time-fuels.html">🔥 Топливо и Горение</a><br>
            <small>Сколько горят предметы</small>
        </div>
        <div class="nav-item">
            <a href="how-make-achievement.html">🏆 Достижения</a><br>
            <small>Ачивки и Прогресс</small>
        </div>
        <div class="nav-item">
            <a href="how-make-overlay.html">🖥️ Оверлей (Overlay)</a><br>
            <small>Интерфейс на экране</small>
        </div>
        <div class="nav-item">
            <a href="how-make-particle.html">✨ Частицы (Particle)</a><br>
            <small>Эффекты, дым, магия</small>
        </div>
        <div class="nav-item">
            <a href="how-make-potion.html">🧪 Зелья (Potion)</a><br>
            <small>Эффекты зелий</small>
        </div>
        <div class="nav-item">
            <a href="how-make-tag.html">🏷️ Теги (Tag)</a><br>
            <small>Группы предметов (шерсть, доски)</small>
        </div>
        <div class="nav-item">
            <a href="how-make-key-binding.html">⌨️ Кнопки (Key Binding)</a><br>
            <small>Действия на клавиши</small>
        </div>
        <div class="nav-item">
            <a href="how-make-command.html">💬 Команды (Command)</a><br>
            <small>/mycommand</small>
        </div>
        <div class="nav-item">
            <a href="how-make-villager-trades.html">💰 Торговля (Villager Trades)</a><br>
            <small>Новые сделки</small>
        </div>
        <div class="nav-item">
            <a href="how-make-enchantment.html">✨ Зачарования (Enchantment)</a><br>
            <small>Магия на предметы</small>
        </div>
        <div class="nav-item">
            <a href="how-make-painting.html">🖼️ Картины (Painting)</a><br>
            <small>Искусство</small>
        </div>
        <div class="nav-item">
            <a href="how-make-loot-table.html">📦 Таблицы добычи (Loot Table)</a><br>
            <small>Дроп из сундуков и мобов</small>
        </div>
        <div class="nav-item">
            <a href="gui-editor.html">🪟 Редактор GUI</a><br>
            <small>Меню и Инвентари</small>
        </div>
    </div>
    """,

    "burn-time-fuels": """
    <h1>Время горения (Burn Time) 🔥</h1>
    <p>Здесь указано, сколько тиков горит каждый предмет в печке. Помни: 20 тиков = 1 секунда.</p>
    <p>Чтобы переплавить 1 предмет, нужно 200 тиков (10 секунд).</p>

    <h2>Таблица горения</h2>
    <table>
        <tr>
            <th>Предмет</th>
            <th>Время (Тики)</th>
            <th>Операций (Сколько переплавит)</th>
        </tr>
        <tr><td>🪣 Ведро лавы (Lava Bucket)</td><td>20000</td><td>100</td></tr>
        <tr><td>⬛ Угольный блок (Coal Block)</td><td>16000</td><td>80</td></tr>
        <tr><td>🌿 Блок сушеной ламинарии (Dried Kelp Block)</td><td>4000</td><td>20</td></tr>
        <tr><td>🔥 Огненный стержень (Blaze Rod)</td><td>2400</td><td>12</td></tr>
        <tr><td>⚫ Уголь / Древесный уголь (Coal / Charcoal)</td><td>1600</td><td>8</td></tr>
        <tr><td>🚤 Лодка (Boat)</td><td>1200</td><td>6</td></tr>
        <tr><td>🪵 Бревно (Log)</td><td>300</td><td>1.5</td></tr>
        <tr><td>🟫 Доски (Planks)</td><td>300</td><td>1.5</td></tr>
        <tr><td>🥢 Палка (Stick)</td><td>100</td><td>0.5</td></tr>
        <tr><td>🌱 Саженец (Sapling)</td><td>100</td><td>0.5</td></tr>
        <tr><td>🧶 Шерсть (Wool)</td><td>100</td><td>0.5</td></tr>
    </table>
    """,

    "how-make-achievement": """
    <h1>Достижение (Achievement / Advancement) 🏆</h1>
    <p>Сделай игроку приятно, выдав награду за действие!</p>

    <h2>Настройки</h2>
    <ul>
        <li><span class="mc-param">Achievement icon</span>: Иконка (любой предмет).</li>
        <li><span class="mc-param">Achievement description</span>: Описание (что нужно сделать?).</li>
        <li><span class="mc-param">Type (Тип)</span>:
            <ul>
                <li><strong>Task:</strong> Обычное задание.</li>
                <li><strong>Goal:</strong> Цель (важнее).</li>
                <li><strong>Challenge:</strong> Испытание (самое крутое, фиолетовое, со звуком).</li>
            </ul>
        </li>
        <li><span class="mc-param">Show toast</span>: Показать всплывающее окно справа сверху?</li>
        <li><span class="mc-param">Announce to chat</span>: Написать в чат, что игрок получил ачивку?</li>
    </ul>

    <h2>Родитель (Parent)</h2>
    <p>Достижения идут ветками. Выбери, после какой ачивки открывается эта. Если хочешь начать новую ветку, выбери "No parent (Root)".</p>
    """,

    "how-make-block": """
    <h1>Как создать Блок (Block) 🧱</h1>
    <p>Блок — это основа Minecraft. Всё, что стоит на земле неподвижно — это блоки.</p>

    <h2>1. Визуализация (Visual) 🎨</h2>
    <p>Первым делом мы выбираем, как блок выглядит.</p>
    <ul>
        <li><span class="mc-param">Block texture (Текстура)</span>: Нажми на пустые квадраты.
            <ul>
                <li><strong>Main:</strong> Если нажать на центр, текстура заполнит все стороны.</li>
                <li><strong>Top/Bottom/Side:</strong> Можно сделать как у "Бревна" (сверху кольца, сбоку кора) или "Травы".</li>
            </ul>
        </li>
        <li><span class="mc-param">Transparency (Прозрачность)</span>:
            <ul>
                <li><strong>Solid:</strong> Обычный камень, земля. Свет не проходит.</li>
                <li><strong>Cutout:</strong> Стекло, решетка, листва. Есть прозрачные дырки.</li>
                <li><strong>Translucent:</strong> Вода, лед, цветное стекло. Полупрозрачность.</li>
            </ul>
        </li>
        <li><span class="mc-param">Block model (Модель)</span>:
            <ul>
                <li><strong>Normal:</strong> Куб.</li>
                <li><strong>Cross model:</strong> Как цветок или саженец (крестик).</li>
                <li><strong>Crop model:</strong> Как пшеница (решетка #).</li>
            </ul>
        </li>
    </ul>

    <h2>2. Свойства (Properties) ⚙️</h2>
    <p>Здесь мы настраиваем поведение блока.</p>
    <table>
        <tr><th>Параметр</th><th>Описание</th></tr>
        <tr><td><span class="mc-param">Material (Материал)</span></td><td>Из чего сделан? (Rock - камень, Wood - дерево, Iron - металл). Влияет на звук шагов и инструмент.</td></tr>
        <tr><td><span class="mc-param">Hardness (Твердость)</span></td><td>Как долго ломать рукой? Песок = 0.5, Камень = 1.5, Обсидиан = 50.</td></tr>
        <tr><td><span class="mc-param">Resistance (Взрывоустойчивость)</span></td><td>Защита от криперов. 6 = ломается, 6000 = бессмертный.</td></tr>
        <tr><td><span class="mc-param">Slipperiness (Скольжение)</span></td><td>0.6 = обычно. 0.98 = лед (очень скользко). 1.1 = "липкий" блок.</td></tr>
        <tr><td><span class="mc-param">Luminance (Свечение)</span></td><td>Светится ли в темноте? 0 = нет. 15 = как лампа.</td></tr>
        <tr><td><span class="mc-param">Has gravity (Гравитация)</span></td><td>Падает ли вниз как песок?</td></tr>
        <tr><td><span class="mc-param">Can walk through (Проходимость)</span></td><td>Можно ли пройти сквозь него? (Как через высокую траву).</td></tr>
        <tr><td><span class="mc-param">Custom Drop (Свой дроп)</span></td><td>Что выпадает? Если пусто — выпадает сам блок. Можно настроить выпадение алмазов из земли!</td></tr>
    </table>

    <h2>3. Дополнительно (Advanced) 🔧</h2>
    <ul>
        <li><span class="mc-param">Tick rate (Скорость тиков)</span>: Как часто блок "думает". 0 = никогда. 10 = 2 раза в секунду. Нужно для процедур "Update Tick".</li>
        <li><span class="mc-param">Flammability (Горение)</span>: Насколько быстро сгорит в огне.</li>
    </ul>

    <h2>4. Триггеры (Triggers) ⚡</h2>
    <p>Самое интересное! Привяжи процедуры к событиям:</p>
    <ul>
        <li><strong>On block right clicked (При клике ПКМ):</strong> Открыть сундук, поменять цвет, вывести сообщение.</li>
        <li><strong>On block added (При установке):</strong> Взорваться, выдать достижение.</li>
        <li><strong>On neighbour block changes (Сосед изменился):</strong> Если сломали блок снизу — упасть.</li>
        <li><strong>On entity walks on (Кто-то наступил):</strong> Нанести урон (как магма), дать эффект скорости.</li>
    </ul>
    """,

    "how-make-item": """
    <h1>Как создать Предмет (Item) 💎</h1>
    <p>Предметы — это вещи, которые лежат в инвентаре, но не ставятся как блоки (обычно). Еда, магия, материалы.</p>

    <h2>Свойства (Properties)</h2>
    <ul>
        <li><span class="mc-param">Name in GUI</span>: Название в игре.</li>
        <li><span class="mc-param">Rarity (Редкость)</span>: Цвет названия (Common - белый, Epic - фиолетовый).</li>
        <li><span class="mc-param">Creative inventory tab</span>: Где искать в креативе.</li>
        <li><span class="mc-param">Max stack size (Стак)</span>:
            <ul>
                <li>64: Обычно (земля, палки).</li>
                <li>16: Снежки, ведра.</li>
                <li>1: Инструменты, мечи (нельзя сложить).</li>
            </ul>
        </li>
        <li><span class="mc-param">Enchantability (Чаруемость)</span>: Шанс получить крутые чары. Золото = 22, Камень = 5.</li>
        <li><span class="mc-param">Item damage count (Прочность)</span>: Сколько раз можно использовать. 0 = бесконечно.</li>
    </ul>

    <h2>Еда (Food) 🍔</h2>
    <p>Если это съедобно:</p>
    <ul>
        <li><span class="mc-param">Nutritional value (Питательность)</span>: Сколько "окорочков" восстановит. 4 = 2 полных окорочка.</li>
        <li><span class="mc-param">Saturation (Насыщение)</span>: Скрытый параметр. Чем выше, тем дольше не хочется есть снова. (Стейк = высоко, Арбуз = низко).</li>
        <li><span class="mc-param">Eating item result</span>: Что останется в руке? (Например, миска после супа).</li>
    </ul>
    """,

    "how-make-entity": """
    <h1>Как создать Сущность (Entity / Mob) 🧟</h1>
    <p>Создай своего моба! Это может быть монстр, животное или NPC.</p>

    <h2>1. Визуал и Модель</h2>
    <p>Тебе нужна текстура и модель. Встроенные модели:</p>
    <ul>
        <li><strong>Biped:</strong> Человекоподобный (Зомби, Скелет).</li>
        <li><strong>Quadruped:</strong> Четвероногий (Корова, Свинья).</li>
    </ul>
    <div class="tip">Если хочешь сложную модель (дракон, машина), используй программу <strong>Blockbench</strong>, сохрани как Java Model и импортируй в MCreator (Resources -> Import Java Model).</div>

    <h2>2. Характеристики (Stats)</h2>
    <ul>
        <li><span class="mc-param">Health (Здоровье)</span>: 20 = 10 сердец (как у игрока).</li>
        <li><span class="mc-param">Experience (Опыт)</span>: Сколько сфер выпадет.</li>
        <li><span class="mc-param">Movement speed (Скорость)</span>: 0.3 = стандарт. 0.2 = медленно.</li>
        <li><span class="mc-param">Armor (Броня)</span>: Врожденная защита.</li>
        <li><span class="mc-param">Attack strength (Сила)</span>: Урон при ударе (для монстров).</li>
    </ul>

    <h2>3. Интеллект (AI) 🧠</h2>
    <p>Задачи выполняются по приоритету (сверху вниз). Чем выше, тем важнее.</p>
    <h3>Пример мирного животного:</h3>
    <ol>
        <li><strong>Swim:</strong> Плавать (чтобы не утонуть).</li>
        <li><strong>Panic when attacked:</strong> Убегать, если ударили.</li>
        <li><strong>Breed:</strong> Размножаться.</li>
        <li><strong>Tempt:</strong> Идти за едой (пшеницей).</li>
        <li><strong>Wander:</strong> Просто бродить.</li>
        <li><strong>Look around:</strong> Смотреть по сторонам.</li>
    </ol>

    <h3>Пример монстра:</h3>
    <ol>
        <li><strong>Swim:</strong> Плавать.</li>
        <li><strong>Melee attack:</strong> Атаковать вблизи.</li>
        <li><strong>Wander:</strong> Бродить.</li>
        <li><strong>Target: Hurt by target:</strong> Атаковать того, кто ударил.</li>
        <li><strong>Target: Nearest player:</strong> Атаковать игрока, если видит.</li>
    </ol>

    <h2>4. Спавн (Spawning) 🥚</h2>
    <ul>
        <li><span class="mc-param">Weight (Вес)</span>: Шанс появления. 100 = часто (Зомби). 5 = редко (Ведьма).</li>
        <li><span class="mc-param">Group size</span>: Мин/Макс количество в стае. Волки ходят по 4, Зомби по 1-4.</li>
        <li><span class="mc-param">Type (Тип)</span>:
            <ul>
                <li><strong>Monster:</strong> Спавнится только в темноте.</li>
                <li><strong>Creature:</strong> Спавнится на траве при свете (свиньи).</li>
                <li><strong>WaterCreature:</strong> В воде.</li>
            </ul>
        </li>
    </ul>
    """,

    "how-make-procedure": """
    <h1>Процедуры (Procedures) ⚡</h1>
    <p>Процедуры — это мозг твоего мода. Это визуальный код (как Scratch), который позволяет делать магию.</p>

    <h2>Как это работает?</h2>
    <p>Слева у тебя есть категории блоков (Logic, Math, Entity...). Ты перетаскиваешь их в рабочую область и соединяешь как пазл.</p>

    <div class="warning">
        <strong>Важно: Зависимости (Dependencies)</strong><br>
        Не все блоки работают везде! Например, блок "Дать предмет игроку" требует, чтобы процедура знала, <strong>кто такой игрок</strong>.
        Если ты вызовешь эту процедуру в событии "Update Tick" (обновление блока), игры вылетит или блок не сработает, потому что блок обновляется сам по себе, там нет игрока!
        <br><br>
        MCreator подсвечивает красным несовместимые блоки. Внимательно смотри на требования!
    </div>

    <hr>

    <h2>📚 Справочник блоков (Cookbook)</h2>

    <h3>1. Логика (Logic & Loops) 🧠</h3>
    <p>Управление потоком выполнения.</p>

    <div class="proc-block">Если (If) ... Выполнить (Do) ... Иначе (Else)</div>
    <p>Самый главный блок. "Если здоровье < 5, то вылечить, иначе убить". Нажми на синюю шестеренку на блоке, чтобы добавить ветку "Else If" или "Else".</p>

    <div class="proc-block">Повторить (Repeat) X раз</div>
    <p>Делает одно и то же несколько раз. Например, "Спавнить 10 зомби".</p>

    <div class="proc-block">Ждать (Wait) X тиков</div>
    <p>Пауза. 20 тиков = 1 секунда. "Ударить -> Ждать 20 тиков -> Ударить снова". Работает только в процедурах, запущенных сущностью!</p>

    <div class="proc-block">Вернуть (Return)</div>
    <p>Останавливает выполнение процедуры прямо сейчас. Полезно для условий: "Если у игрока нет меча -> Вернуть (стоп)".</p>

    <hr>

    <h3>2. Сущности (Entity Management) 🧟</h3>
    <p>Все, что касается мобов и игрока.</p>

    <div class="proc-block">Текущее здоровье (Current health)</div>
    <p>Возвращает число жизней. Используй в "If": <code>If (Current health < 5)</code>.</p>

    <div class="proc-block">Назначить здоровье (Set health)</div>
    <p>Лечит или калечит. <code>Set health to (Current health + 2)</code> — это лечение на 1 сердце.</p>

    <div class="proc-block">Добавить эффект зелья (Add potion effect)</div>
    <p>Накладывает эффекты. Speed, Strength, Poison. Level 0 = I уровень, Level 1 = II уровень.</p>

    <div class="proc-block">Телепортировать (Teleport)</div>
    <p>Перемещает сущность в координаты X, Y, Z. Можно телепортировать на 5 блоков вверх: <code>Set Y to (Y + 5)</code>.</p>

    <div class="proc-block">Проверить тип сущности (Is entity subtype of)</div>
    <p>Проверка: "Это зомби? Это игрок?". <code>If (Is entity subtype of Player) Do...</code></p>

    <hr>

    <h3>3. Мир и Блоки (World & Blocks) 🌍</h3>

    <div class="proc-block">Удалить блок (Remove block)</div>
    <p>Ломает блок в координатах X, Y, Z. Полезно для "исчезающих" мостов.</p>

    <div class="proc-block">Поставить блок (Place block)</div>
    <p>Ставит блок. Можно заменить воздух на алмазную руду.</p>

    <div class="proc-block">Спавн сущности (Spawn entity)</div>
    <p>Призывает моба. "Призвать Молнию" тоже здесь.</p>

    <div class="proc-block">Взрыв (Explode)</div>
    <p>Ба-бах! Power 4 = как ТНТ. Тип BREAK (ломает), NONE (только урон), DESTROY (в пыль).</p>

    <div class="proc-block">Время суток (Time of day)</div>
    <p>Можно проверить, день сейчас или ночь.</p>

    <hr>

    <h3>4. Предметы (Items) 🎒</h3>

    <div class="proc-block">Дать предмет (Add item to inventory)</div>
    <p>Кидает предмет в инвентарь. Если места нет, предмет выпадет под ноги.</p>

    <div class="proc-block">Удалить предмет (Remove item)</div>
    <p>Забирает предмет. Полезно для торговцев или квестов.</p>

    <div class="proc-block">Предмет в руке (Item in main/off hand)</div>
    <p>Что держит игрок? "Если в руке Палка, то пустить молнию".</p>

    <hr>

    <h3>5. Чат и Сообщения 💬</h3>

    <div class="proc-block">Отправить сообщение (Send chat message)</div>
    <p>Пишет в чат всем или конкретному игроку. Можно использовать цвета §c (красный), §a (зеленый).</p>

    <div class="proc-block">Вывести название (Action bar title)</div>
    <p>Надпись над хотбаром (как "Press Shift").</p>

    <hr>

    <h2>Примеры рецептов (Code snippets)</h2>

    <h3>🔥 Огненный меч</h3>
    <p><strong>Триггер:</strong> When entity hit with tool (Когда бьют инструментом)<br>
    <strong>Блоки:</strong><br>
    Set entity on fire (Target entity) for 5 seconds.</p>

    <h3>🚀 Прыжок в небо при клике</h3>
    <p><strong>Триггер:</strong> On right click with item (ПКМ предметом)<br>
    <strong>Блоки:</strong><br>
    Set velocity of (Source entity) to: X:0, Y:2, Z:0.<br>
    (Это подбросит игрока вверх).</p>

    <h3>💎 Блок удачи (Lucky Block)</h3>
    <p><strong>Триггер:</strong> On block destroyed (При разрушении)<br>
    <strong>Блоки:</strong><br>
    If (Random [0,1] < 0.5):<br>
    -- Spawn Gem<br>
    Else:<br>
    -- Explode at X, Y, Z Power 3</p>
    """,

    "how-make-tool": """
    <h1>Инструмент (Tool) ⛏️</h1>
    <p>Кирки, Топоры, Лопаты, Мотыги и Мечи.</p>

    <h2>Характеристики</h2>
    <ul>
        <li><span class="mc-param">Type (Тип)</span>: Pickaxe (Кирка), Axe (Топор), Sword (Меч), Shovel (Лопата).</li>
        <li><span class="mc-param">Harvest level (Уровень добычи)</span>:
            <ul>
                <li>0: Дерево (не добывает железо).</li>
                <li>1: Камень (добывает железо).</li>
                <li>2: Железо (добывает алмазы).</li>
                <li>3: Алмаз (добывает обсидиан).</li>
                <li>4: Незерит.</li>
            </ul>
        </li>
        <li><span class="mc-param">Efficiency (Эффективность)</span>: Скорость копания.
            <ul>
                <li>Wood: 2</li>
                <li>Stone: 4</li>
                <li>Iron: 6</li>
                <li>Diamond: 8</li>
                <li>Gold: 12 (самый быстрый, но хрупкий)</li>
            </ul>
        </li>
        <li><span class="mc-param">Attack Speed (Скорость атаки)</span>:
            <ul>
                <li>4.0: Как рукой (очень быстро, слабый урон).</li>
                <li>1.6: Меч (стандарт).</li>
                <li>1.0: Топор (медленно, сильный удар).</li>
            </ul>
        </li>
        <li><span class="mc-param">Damage vs entity (Урон)</span>: Сколько сердец снимает. (Значение в сердцах или единицах, помни: 2 единицы = 1 сердце).</li>
        <li><span class="mc-param">Repair item (Ремонт)</span>: Какой предмет нужен в наковальне для починки (например, твой слиток).</li>
    </ul>
    """,

    "how-make-armor": """
    <h1>Броня (Armor) 🛡️</h1>

    <h2>Текстуры</h2>
    <div class="warning">
        Для брони нужно 2 текстуры развертки (Layer 1 и Layer 2). Обычная иконка предмета здесь не подходит для отображения на игроке!
    </div>
    <p>Используй <strong>Tools -> Create armor texture</strong> в верхнем меню MCreator, чтобы нарисовать броню правильно.</p>
    <ul>
        <li><strong>Layer 1:</strong> Шлем, Нагрудник, Ботинки. (Оставляй место для штанов пустым).</li>
        <li><strong>Layer 2:</strong> Штаны (Leggings).</li>
    </ul>

    <h2>Защита</h2>
    <ul>
        <li><span class="mc-param">Defense Value (Защита)</span>: Количество "нагрудников" над здоровьем. Полный сет алмазной брони = 20.</li>
        <li><span class="mc-param">Toughness (Твердость)</span>: Скрытый параметр, защищает от сильных ударов. У алмаза 2.0.</li>
        <li><span class="mc-param">Knockback resistance (Стойкость)</span>: Шанс не отлететь при ударе (как Незерит).</li>
    </ul>
    """,

    "how-make-biome": """
    <h1>Биом (Biome) 🌵</h1>
    <p>Создай свой лес, пустыню или волшебную поляну.</p>

    <h2>Настройки</h2>
    <ul>
        <li><span class="mc-param">Ground block</span>: Блок поверхности (Трава).</li>
        <li><span class="mc-param">Underground block</span>: Блок под землей (Земля), идет слоем в 3-5 блоков.</li>
        <li><span class="mc-param">Trees per chunk</span>: Сколько деревьев?
            <ul>
                <li>0: Поле.</li>
                <li>1-5: Редкий лес.</li>
                <li>10: Обычный лес.</li>
                <li>50: Джунгли.</li>
            </ul>
        </li>
    </ul>

    <h2>Цвета (Atmosphere)</h2>
    <p>Ты можешь перекрасить небо, траву и воду!</p>
    <ul>
        <li><span class="mc-param">Sky color</span>: Цвет неба.</li>
        <li><span class="mc-param">Grass color</span>: Цвет травы.</li>
        <li><span class="mc-param">Water color</span>: Цвет воды.</li>
        <li><span class="mc-param">Fog color</span>: Цвет тумана вдалеке.</li>
    </ul>
    """,

    "how-make-recipe": """
    <h1>Рецепт (Recipe) 📜</h1>

    <h2>Типы крафта</h2>
    <ul>
        <li><strong>Crafting:</strong> Обычный верстак (3x3).</li>
        <li><strong>Smelting:</strong> Печка (Жарка).</li>
        <li><strong>Blasting:</strong> Плавильня (Только руды, x2 скорость).</li>
        <li><strong>Smoking:</strong> Коптильня (Только еда, x2 скорость).</li>
        <li><strong>Smithing:</strong> Стол кузнеца (Обновление алмазного до незеритового).</li>
    </ul>

    <h2>Как делать</h2>
    <p>Просто перетащи предметы из списка справа в сетку крафта. </p>
    <div class="tip">
        <strong>Использование Тегов (Tags):</strong><br>
        Если ты хочешь, чтобы рецепт принимал <strong>любую</strong> шерсть или <strong>любые</strong> доски, используй теги.
        Нажми "Use tags" под слотом и выбери <code>minecraft:planks</code> (доски) или <code>minecraft:wool</code> (шерсть).
    </div>
    """,

    "how-make-fluid": """
    <h1>Жидкость (Fluid) 💧</h1>
    <p>Вода, лава, нефть, яд, слайм.</p>

    <h2>Текстуры</h2>
    <p>Нужны две анимированные текстуры: <strong>Still</strong> (стоячая) и <strong>Flowing</strong> (текущая).</p>

    <h2>Физика</h2>
    <ul>
        <li><span class="mc-param">Density (Плотность)</span>:
            <ul>
                <li>1000: Вода.</li>
                <li>-1000: Газ (летит вверх, как дым).</li>
            </ul>
        </li>
        <li><span class="mc-param">Viscosity (Вязкость)</span>:
            <ul>
                <li>1000: Течет быстро как вода.</li>
                <li>6000: Медленно как лава.</li>
            </ul>
        </li>
    </ul>
    """,

    "how-make-structure": """
    <h1>Структура (Structure) 🏰</h1>
    <p>Как добавить свой домик или данж в генерацию мира.</p>

    <h2>Шаг 1: Сохранение в игре</h2>
    <ol>
        <li>Зайди в Minecraft.</li>
        <li>Построй свое здание.</li>
        <li>Выдай себе структурный блок: <code>/give @p structure_block</code>.</li>
        <li>Поставь его рядом со зданием. Режим <strong>SAVE</strong>.</li>
        <li>Выдели зону белыми линиями (изменяй X, Y, Z size).</li>
        <li>Назови структуру (например, <code>my_house</code>) и нажми <strong>SAVE</strong>.</li>
        <li>Файл .nbt появится в папке <code>.minecraft/saves/ТвойМир/generated/minecraft/structures/</code>.</li>
    </ol>

    <h2>Шаг 2: Импорт</h2>
    <ol>
        <li>В MCreator: <strong>Resources -> Structures -> Import structure from Minecraft</strong>.</li>
        <li>Выбери свой .nbt файл.</li>
        <li>Создай элемент "Structure" и выбери импортированный файл.</li>
    </ol>

    <h2>Шаг 3: Настройка спавна</h2>
    <ul>
        <li><span class="mc-param">Probability (Вероятность)</span>: 1,000,000 = почти никогда. 1000 = очень часто.</li>
        <li><span class="mc-param">Ground detection</span>: Включи, чтобы дом не спавнился в воздухе или под землей.</li>
    </ul>
    """,

    "how-make-plant": """
    <h1>Растение (Plant) 🌻</h1>

    <h2>Типы растений</h2>
    <ul>
        <li><span class="mc-param">Static plant</span>: Обычный цветок или трава.</li>
        <li><span class="mc-param">Growable plant</span>: Как пшеница (имеет стадии роста).</li>
        <li><span class="mc-param">Double plant</span>: Высокое растение (Подсолнух, Сирень).</li>
    </ul>

    <h2>Свойства</h2>
    <ul>
        <li><span class="mc-param">Plant type</span>:
            <ul>
                <li><strong>Desert:</strong> Можно сажать на песок (Кактус).</li>
                <li><strong>Plains:</strong> На траву и землю.</li>
                <li><strong>Water:</strong> Кувшинка (на воду).</li>
                <li><strong>Cave:</strong> Пещерное (без света).</li>
                <li><strong>Crop:</strong> Грядка (только на вспаханную землю).</li>
            </ul>
        </li>
    </ul>
    """,

    "how-make-dimension": """
    <h1>Измерение (Dimension) 🌌</h1>
    <p>Создай свой мир со своими законами!</p>

    <h2>Портал</h2>
    <p>Чтобы попасть в мир, нужен портал (как в Ад).</p>
    <ul>
        <li><span class="mc-param">Frame block</span>: Из чего рамка? (Обсидиан, Золото, Твой блок).</li>
        <li><span class="mc-param">Igniter item</span>: Чем поджигать? (Зажигалка или твой предмет).</li>
    </ul>

    <h2>Генерация</h2>
    <ul>
        <li><span class="mc-param">Main filler block</span>: Из чего состоит земля (Камень).</li>
        <li><span class="mc-param">Fluid block</span>: Океаны (Вода или Лава).</li>
        <li><span class="mc-param">Biomes</span>: Какие биомы там есть? Можно выбрать свои или ванильные.</li>
        <li><span class="mc-param">Skylight</span>: Есть ли солнце? (В Аду нет).</li>
        <li><span class="mc-param">Sleep attempt</span>: Что будет, если лечь спать? (Deny - нельзя, Explode - взрыв).</li>
    </ul>
    """,

    "how-make-creative-inventory-tab": """
    <h1>Вкладка Креатива (Creative Tab) 📂</h1>
    <p>Чтобы твои предметы не терялись в "Разном", создай для них свою вкладку.</p>
    <p>Просто создай элемент "Creative Tab", выбери иконку (любой предмет), и потом в каждом своем блоке/предмете выбирай эту вкладку в поле <strong>Creative inventory tab</strong>.</p>
    """,

    "how-make-overlay": """
    <h1>Оверлей (Overlay) 🖥️</h1>
    <p>Нарисуй что-нибудь на экране игрока! Жизни, ману, или прицел.</p>
    <h2>Настройки</h2>
    <ul>
        <li><span class="mc-param">Overlay event</span>: Когда показывать? (Always - всегда).</li>
        <li><span class="mc-param">Priority</span>: Поверх других или снизу? (Normal - обычно, High - поверх чата).</li>
    </ul>
    <p>В редакторе используй кнопки <strong>Image</strong> (картинка) и <strong>Text</strong> (надпись).</p>
    """,

    "how-make-particle": """
    <h1>Частицы (Particle) ✨</h1>
    <p>Магия, дым, искры. Частицы украшают игру.</p>
    <h2>Текстура</h2>
    <p>Выбери картинку. Важно: частицы всегда смотрят на игрока (как в Doom).</p>
    <h2>Физика</h2>
    <ul>
        <li><span class="mc-param">Velocity</span>: Скорость полета (X, Y, Z). Если Y=0.1, частица полетит вверх.</li>
        <li><span class="mc-param">Gravity</span>: Падает ли вниз?</li>
        <li><span class="mc-param">Life span</span>: Сколько живет? (в тиках).</li>
    </ul>
    """,

    "how-make-potion": """
    <h1>Зелье (Potion) 🧪</h1>
    <p>Свой эффект, как Сила или Скорость.</p>
    <h2>Свойства</h2>
    <ul>
        <li><span class="mc-param">Is instant</span>: Мгновенное (как Лечение) или длительное (как Отравление)?</li>
        <li><span class="mc-param">Is bad</span>: Это яд? (Красный шрифт).</li>
        <li><span class="mc-param">Color</span>: Цвет пузырьков.</li>
    </ul>
    <h2>Триггеры</h2>
    <p>Используй "On active tick", чтобы делать что-то каждую секунду (например, наносить урон).</p>
    """,

    "how-make-tag": """
    <h1>Теги (Tag) 🏷️</h1>
    <p>Группируй предметы вместе.</p>
    <p>Например, если ты создашь тег <code>mod:my_logs</code> и добавишь туда все свои виды бревен, ты сможешь использовать этот тег в рецептах. И рецепт будет работать для ЛЮБОГО бревна из этого списка.</p>
    <ul>
        <li><strong>Registry name:</strong> <code>minecraft:planks</code> (все доски), <code>minecraft:wool</code> (вся шерсть).</li>
    </ul>
    """,

    "how-make-key-binding": """
    <h1>Кнопки (Key Binding) ⌨️</h1>
    <p>Сделай так, чтобы при нажатии <strong>R</strong> происходила магия.</p>
    <h2>Настройка</h2>
    <ul>
        <li><span class="mc-param">Default key</span>: Какая кнопка по умолчанию? (R, G, H...).</li>
        <li><span class="mc-param">Category</span>: В каком разделе настроек искать (Gameplay, Misc).</li>
    </ul>
    <h2>Триггер</h2>
    <p>Привяжи процедуру к событию "On key pressed".</p>
    <div class="warning">
        Кнопки работают только когда игрок в игре. В меню они не работают.
    </div>
    """,

    "how-make-command": """
    <h1>Команды (Command) 💬</h1>
    <p>Своя команда, например <code>/home</code> или <code>/healme</code>.</p>
    <ul>
        <li><span class="mc-param">Command name</span>: Что писать в чат? (без слеша).</li>
        <li><span class="mc-param">Execution level</span>: Кто может использовать?
            <ul>
                <li>0: Все игроки.</li>
                <li>2: Операторы (OP) и командные блоки.</li>
                <li>4: Только серверная консоль.</li>
            </ul>
        </li>
    </ul>
    """,

    "how-make-villager-trades": """
    <h1>Торговля (Villager Trades) 💰</h1>
    <p>Добавь новые сделки жителям.</p>
    <ul>
        <li><span class="mc-param">Profession</span>: Кто торгует? (Farmer - фермер, Librarian - библиотекарь).</li>
        <li><span class="mc-param">Level</span>: На каком уровне прокачки появится сделка? (1 = новичок, 5 = мастер).</li>
    </ul>
    <h2>Сделка</h2>
    <ul>
        <li><strong>Buy item:</strong> Что житель забирает (Изумруд).</li>
        <li><strong>Sell item:</strong> Что житель дает (Алмазный меч).</li>
    </ul>
    """,

    "how-make-enchantment": """
    <h1>Зачарование (Enchantment) ✨</h1>
    <p>Магия для мечей и брони.</p>
    <ul>
        <li><span class="mc-param">Rarity</span>: Как часто попадается в столе зачарований.</li>
        <li><span class="mc-param">Min/Max level</span>: Уровни (I, II, III...).</li>
        <li><span class="mc-param">Target</span>: На что можно наложить? (Breakable - инструменты, Armor - броня, Weapon - оружие).</li>
    </ul>
    """,

    "how-make-painting": """
    <h1>Картина (Painting) 🖼️</h1>
    <p>Твое искусство на стене.</p>
    <p>Просто загрузи текстуру. MCreator сам разделит её на блоки (1x1, 2x1, 4x4).</p>
    <div class="tip">Текстура должна быть кратна 16 пикселям! (16x16, 32x16, 64x64).</div>
    """,

    "how-make-loot-table": """
    <h1>Таблица добычи (Loot Table) 📦</h1>
    <p>Настрой, что выпадает из сундуков в данжах или из мобов.</p>
    <ul>
        <li><span class="mc-param">Registry name</span>:
            <ul>
                <li><code>blocks/grass</code> - дроп из травы.</li>
                <li><code>entities/zombie</code> - дроп из зомби.</li>
                <li><code>chests/simple_dungeon</code> - сундук в спавнере.</li>
            </ul>
        </li>
    </ul>
    <p>Добавляй <strong>Pools</strong> (группы предметов) и <strong>Entries</strong> (сами предметы). Настрой <strong>Weight</strong> (шанс): чем больше число, тем чаще падает.</p>
    """,

    "gui-editor": """
    <h1>Редактор GUI (Меню) 🪟</h1>
    <p>Здесь ты рисуешь свои интерфейсы: сундуки, печки, верстаки.</p>
    <ul>
        <li><strong>Slot:</strong> Ячейка для предмета. (Input - можно класть, Output - только забирать).</li>
        <li><strong>Text:</strong> Надпись.</li>
        <li><strong>Button:</strong> Кнопка. Привяжи к ней процедуру!</li>
    </ul>
    <p>Не забудь включить <strong>Bind this GUI to a block</strong> в настройках блока, чтобы оно открывалось.</p>
    """,

    # Redirects for old/deleted pages to keep links working
    "entity-ids": """
    <h1>ID Сущностей</h1>
    <p>Этот раздел устарел. Смотри раздел <a href="how-make-entity.html">Сущности</a>.</p>
    """,
    "particles-ids": """
    <h1>ID Частиц</h1>
    <p>Этот раздел устарел. Смотри раздел <a href="how-make-particle.html">Частицы</a>.</p>
    """,
    "minecraft-block-and-item-list-registry-and-code-names": """
    <h1>Реестр Блоков</h1>
    <p>Смотри <a href="how-make-block.html">Блоки</a> или <a href="how-make-item.html">Предметы</a>.</p>
    """,
    "minecraft-vanilla-loot-tables-list": """
    <h1>Ванильный лут</h1>
    <p>Смотри <a href="how-make-loot-table.html">Таблицы добычи</a>.</p>
    """,
    "list-sound-effects-and-records": """
    <h1>Звуки</h1>
    <p>Звуки выбираются в настройках блоков и предметов.</p>
    """,
    "how-make-banner-pattern": """
    <h1>Узор флага</h1>
    <p>Создание узоров для флагов пока редко используется, но оно похоже на создание предметов.</p>
    """,
    "how-make-feature": """
    <h1>Особенность (Feature)</h1>
    <p>Смотри раздел <a href="how-make-structure.html">Структуры</a> или <a href="how-make-biome.html">Биомы</a>.</p>
    """,
    "how-make-food": """
    <h1>Еда</h1>
    <p>Еда теперь часть <a href="how-make-item.html">Предметов</a>. Зайди туда!</p>
    """,
    "how-make-item-extension": """
    <h1>Расширение предмета</h1>
    <p>Это сложная тема для программистов. Если ты новичок, тебе это не нужно.</p>
    """
}

def generate():
    # 1. CLEANUP: Delete all existing HTML files in the directory to ensure no English leftovers
    if os.path.exists(OUTPUT_DIR):
        for filename in os.listdir(OUTPUT_DIR):
            if filename.endswith(".html") or filename.endswith(".css"):
                file_path = os.path.join(OUTPUT_DIR, filename)
                try:
                    os.unlink(file_path)
                    # print(f"Deleted old file: {filename}")
                except Exception as e:
                    print(f"Failed to delete {filename}: {e}")
    else:
        os.makedirs(OUTPUT_DIR, exist_ok=True)

    # 2. GENERATE: Write the shared CSS file
    css_path = os.path.join(OUTPUT_DIR, "style.css")
    with open(css_path, 'w', encoding='utf-8') as f:
        f.write(CSS_CONTENT)
    print(f"Generated style.css")

    # 3. GENERATE: Write the HTML files
    for slug, content in PAGES.items():
        filename = f"{slug}.html"
        filepath = os.path.join(OUTPUT_DIR, filename)

        full_html = f"""<html>
<head>
    <meta charset="utf-8">
    <title>{slug} - MCreator Wiki</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    {content}
    <br><hr>
    <p style="text-align: center;"><small><a href="index.html">🏠 Вернуться на главную</a></small></p>
</body>
</html>"""

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(full_html)
        print(f"Generated {filename} ({len(content)} chars)")

if __name__ == "__main__":
    generate()
