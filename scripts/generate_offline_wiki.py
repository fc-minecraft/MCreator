# -*- coding: utf-8 -*-
import os

OUTPUT_DIR = "plugins/mcreator-localization/help/ru_RU/wiki"
os.makedirs(OUTPUT_DIR, exist_ok=True)

STYLE = """
<style>
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
    .mc-param { font-weight: bold; color: #2c3e50; background-color: #eef2f5; padding: 2px 5px; border-radius: 3px; }
    table { width: 100%; border-collapse: collapse; margin: 25px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    th, td { border: 1px solid #ddd; padding: 15px; text-align: left; vertical-align: top; }
    th { background-color: #f8f9fa; color: #333; font-weight: bold; text-transform: uppercase; font-size: 0.9em; letter-spacing: 0.5px; }
    tr:nth-child(even) { background-color: #f9f9f9; }
    tr:hover { background-color: #f1f1f1; }
    .step-number { display: inline-block; width: 25px; height: 25px; background-color: #2980b9; color: white; border-radius: 50%; text-align: center; line-height: 25px; margin-right: 10px; font-weight: bold; }
    .nav-box { background-color: #f8f9fa; padding: 20px; border-radius: 10px; display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 15px; }
    .nav-item { background-color: white; padding: 15px; border-radius: 5px; border: 1px solid #eee; text-align: center; transition: transform 0.2s, box-shadow 0.2s; }
    .nav-item:hover { transform: translateY(-3px); box-shadow: 0 5px 15px rgba(0,0,0,0.1); border-color: #2980b9; }
</style>
"""

PAGES = {
    "index": """
    <h1>Энциклопедия MCreator</h1>
    <p>Добро пожаловать в полное, пошаговое руководство по созданию модов для Minecraft! 🚀</p>
    <p>Здесь ты найдешь подробнейшие инструкции о каждом элементе интерфейса MCreator. Мы разберем каждую кнопку, каждое поле ввода и каждый секрет, чтобы ты мог создать мод своей мечты.</p>

    <div class="tip">
        <strong>Совет:</strong> Если ты не знаешь, что делает какая-то кнопка в программе, просто нажми на маленький знак вопроса рядом с ней, и MCreator (с нашей помощью) откроет нужную страницу!
    </div>

    <h2>📚 Содержание</h2>

    <div class="nav-box">
        <div class="nav-item">
            <a href="how-make-block.html">🧱 Блок (Block)</a><br>
            <small>Основа мира</small>
        </div>
        <div class="nav-item">
            <a href="how-make-item.html">💎 Предмет (Item)</a><br>
            <small>Вещи в инвентаре</small>
        </div>
        <div class="nav-item">
            <a href="how-make-tool.html">⛏️ Инструмент (Tool)</a><br>
            <small>Кирки, Мечи, Топоры</small>
        </div>
        <div class="nav-item">
            <a href="how-make-armor.html">🛡️ Броня (Armor)</a><br>
            <small>Защита и стиль</small>
        </div>
        <div class="nav-item">
            <a href="how-make-entity.html">🧟 Сущность (Entity)</a><br>
            <small>Мобы и Животные</small>
        </div>
        <div class="nav-item">
            <a href="how-make-biome.html">🌵 Биом (Biome)</a><br>
            <small>Природные зоны</small>
        </div>
        <div class="nav-item">
            <a href="how-make-dimension.html">🌌 Измерение (Dimension)</a><br>
            <small>Новые миры</small>
        </div>
        <div class="nav-item">
            <a href="how-make-procedure.html">⚡ Процедуры</a><br>
            <small>Логика и Скрипты</small>
        </div>
        <div class="nav-item">
            <a href="how-make-recipe.html">📜 Рецепты (Recipe)</a><br>
            <small>Крафты</small>
        </div>
        <div class="nav-item">
            <a href="how-make-food.html">🍎 Еда (Food)</a><br>
            <small>Вкусняшки</small>
        </div>
        <div class="nav-item">
            <a href="how-make-plant.html">🌻 Растение (Plant)</a><br>
            <small>Флора</small>
        </div>
        <div class="nav-item">
            <a href="how-make-fluid.html">💧 Жидкость (Fluid)</a><br>
            <small>Вода, Лава, Нефть</small>
        </div>
        <div class="nav-item">
            <a href="how-make-creative-inventory-tab.html">📂 Вкладка</a><br>
            <small>Группа в креативе</small>
        </div>
        <div class="nav-item">
            <a href="how-make-structure.html">🏰 Структура</a><br>
            <small>Данжи и Руины</small>
        </div>
    </div>
    """,

    "how-make-block": """
    <h1>Блок (Block)</h1>
    <p>Блок — это фундаментальная единица мира Minecraft. Все, что ты видишь в мире и что стоит на месте — это блоки. Земля, Камень, Верстак, Сундук, Печка, Листва — всё это блоки.</p>

    <h2>🚀 Как создать блок: Пошагово</h2>
    <ol>
        <li>Нажми на большую зеленую кнопку <strong>(+)</strong> в левом верхнем углу интерфейса (или нажми <code>B</code>).</li>
        <li>В появившемся списке выбери <strong>Block (Блок)</strong>.</li>
        <li>Введи <strong>название</strong> (на английском, без пробелов). Например: <code>SuperStone</code>.</li>
        <li>Нажми <strong>Create new block</strong>.</li>
    </ol>

    <hr>

    <h2>📋 Вкладка 1: Visual (Визуализация)</h2>
    <p>Здесь настраивается внешний вид блока.</p>

    <table>
        <tr>
            <th>Параметр</th>
            <th>Описание</th>
        </tr>
        <tr>
            <td><span class="mc-param">Block texture</span><br>(Текстура)</td>
            <td>
                Нажми на квадраты, чтобы выбрать картинку.<br>
                <ul>
                    <li><strong>Main/Bottom:</strong> Если нажать на центральный квадрат, эта текстура применится ко всем сторонам (как у Земли).</li>
                    <li><strong>Top/Bottom/Side:</strong> Если хочешь разные текстуры (как у Бревна или Травы), нажимай на каждый квадрат отдельно (Left, Right, Front, Back, Top, Bottom).</li>
                </ul>
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Block model</span><br>(Модель)</td>
            <td>
                Форма блока:<br>
                <ul>
                    <li><strong>Normal:</strong> Обычный куб (Камень).</li>
                    <li><strong>Cross model:</strong> Крест-накрест (Цветок, Саженец).</li>
                    <li><strong>Crop model:</strong> Решетка (Пшеница).</li>
                    <li><strong>Single texture:</strong> Куб с одинаковой текстурой со всех сторон.</li>
                </ul>
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Transparency</span><br>(Прозрачность)</td>
            <td>
                Как блок пропускает свет:<br>
                <ul>
                    <li><strong>Solid:</strong> Непрозрачный (Камень). Самый быстрый для игры.</li>
                    <li><strong>Cutout:</strong> Есть дырки, но нет полупрозрачности (Стекло, Листва).</li>
                    <li><strong>Translucent:</strong> Полупрозрачный (Лед, Вода). Самый требовательный к ресурсам.</li>
                </ul>
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Check this if the block has a tint</span></td>
            <td>Включи, если хочешь, чтобы блок менял цвет в зависимости от биома (как Трава или Листва).</td>
        </tr>
        <tr>
            <td><span class="mc-param">Block rotation mode</span></td>
            <td>
                Будет ли блок вращаться при установке?<br>
                <ul>
                    <li><strong>None:</strong> Не вращается (Земля).</li>
                    <li><strong>Y axis:</strong> Вращается влево-вправо (Печка, Сундук).</li>
                    <li><strong>Log rotation:</strong> Вращается как бревно (вверх, на бок).</li>
                </ul>
            </td>
        </tr>
    </table>

    <h2>📦 Вкладка 2: Bounding Box (Границы)</h2>
    <p>Здесь можно настроить физические границы блока (хитбокс). По умолчанию это полный куб (от 0 до 16).</p>
    <div class="tip">
        Координаты считаются в пикселях (от 0 до 16).<br>
        Например, полублок (Slab) имеет высоту от 0 до 8 по оси Y.
    </div>

    <h2>⚙️ Вкладка 3: Properties (Свойства)</h2>
    <p>Самая важная вкладка! Здесь задается характер блока.</p>

    <table>
        <tr>
            <th>Параметр</th>
            <th>Значение и примеры</th>
        </tr>
        <tr>
            <td><span class="mc-param">Name in GUI</span></td>
            <td>Имя, которое видит игрок в инвентаре (можно на русском!).</td>
        </tr>
        <tr>
            <td><span class="mc-param">Material</span></td>
            <td>
                Из чего сделан блок (влияет на звук и инструмент):<br>
                Rock (Камень), Wood (Дерево), Earth (Земля), Iron (Металл), Glass (Стекло).
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Creative inventory tab</span></td>
            <td>В какой вкладке креатива искать блок.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Hardness</span><br>(Твердость)</td>
            <td>
                Сколько времени ломать блок рукой:<br>
                0.5 = Песок<br>
                1.5 = Камень<br>
                5.0 = Железный блок<br>
                50.0 = Обсидиан
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Resistance</span><br>(Взрывоустойчивость)</td>
            <td>
                Защита от взрывов:<br>
                6 = Камень (ломается от ТНТ)<br>
                1200 = Обсидиан (не ломается)
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Slipperiness</span><br>(Скольжение)</td>
            <td>
                0.6 = Обычно (Земля)<br>
                0.8 = Слизь<br>
                0.98 = Лед (очень скользко)
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Luminance</span><br>(Свечение)</td>
            <td>0 = Не светится. 15 = Максимум (как Лампа).</td>
        </tr>
        <tr>
            <td><span class="mc-param">Has gravity</span></td>
            <td>Если галочка стоит, блок падает вниз, если под ним пусто (как Песок).</td>
        </tr>
        <tr>
            <td><span class="mc-param">Can walk through</span></td>
            <td>Если галочка стоит, сквозь блок можно пройти (как сквозь высокую траву или открытую дверь).</td>
        </tr>
        <tr>
            <td><span class="mc-param">Custom Drop</span></td>
            <td>
                Что выпадает при разрушении?<br>
                Если пусто — выпадает сам блок.<br>
                Если выбрать предмет (например, Рубин) — выпадет он (для руд).
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Tool to destroy</span></td>
            <td>Чем ломать? (Pickaxe/Axe/Shovel).</td>
        </tr>
        <tr>
            <td><span class="mc-param">Harvest level</span></td>
            <td>
                Уровень инструмента:<br>
                0 = Дерево/Золото<br>
                1 = Камень<br>
                2 = Железо<br>
                3 = Алмаз
            </td>
        </tr>
    </table>

    <h2>🔧 Вкладка 4: Advanced Properties (Дополнительно)</h2>
    <ul>
        <li><span class="mc-param">Tick rate:</span> Как часто обновляется блок. 10 = каждые полсекунды. Нужно для процедур "Update Tick".</li>
        <li><span class="mc-param">Flammability:</span> Насколько легко загорается (0 = не горит).</li>
        <li><span class="mc-param">Enchantment power bonus:</span> Если поставить вокруг стола зачарований, сколько "книжных полок" он заменит?</li>
        <li><span class="mc-param">Color on map:</span> Каким цветом точка на карте.</li>
    </ul>

    <h2>⚡ Вкладка 5: Triggers (Триггеры)</h2>
    <p>Здесь ты можешь привязать <strong>Процедуры</strong> к событиям.</p>
    <ul>
        <li><strong>On block right clicked:</strong> Когда игрок кликает ПКМ (например, открыть GUI или поменять текстуру).</li>
        <li><strong>On block added:</strong> Когда блок поставили.</li>
        <li><strong>On neighbour block changes:</strong> Когда соседний блок изменился (нужно для механизмов).</li>
        <li><strong>On tick update:</strong> Срабатывает регулярно (например, для роста растений или спавна частиц).</li>
        <li><strong>On entity walks on the block:</strong> Когда кто-то наступил (как Магма блок наносит урон).</li>
    </ul>

    <h2>🌍 Вкладка 6: Generation (Генерация)</h2>
    <p>Настрой, если хочешь, чтобы блок сам появлялся в мире (как руда).</p>
    <ul>
        <li><span class="mc-param">Dimensions to generate:</span> В каких мирах? (Surface = Обычный, Nether = Ад).</li>
        <li><span class="mc-param">Block to replace:</span> Вместо чего появляться? Обычно Stone (Камень).</li>
        <li><span class="mc-param">Frequency on chunks:</span> Сколько раз пытаться заспавнить в одном чанке.</li>
        <li><span class="mc-param">Average amount of ore groups:</span> Размер одной жилы.</li>
    </ul>
    """,

    "how-make-item": """
    <h1>Предмет (Item)</h1>
    <p>Предметы лежат в инвентаре. Они не ставятся на землю как блоки (если только это не особый случай). Мечи, Еда, Материалы, Пластинки — это предметы.</p>

    <h2>🚀 Создание</h2>
    <ol>
        <li>Нажми <strong>(+)</strong> -> <strong>Item</strong> (Предмет).</li>
        <li>Введи имя (например, <code>MagicDust</code>).</li>
    </ol>

    <h2>📋 Вкладка 1: Visual</h2>
    <ul>
        <li><span class="mc-param">Item texture:</span> Выбери иконку (обычно 16x16 пикселей).</li>
        <li><span class="mc-param">Item model:</span> Normal (плоская картинка) или Block (3D модель).</li>
    </ul>

    <h2>⚙️ Вкладка 2: Properties</h2>
    <table>
        <tr>
            <th>Параметр</th>
            <th>Описание</th>
        </tr>
        <tr>
            <td><span class="mc-param">Name in GUI</span></td>
            <td>Название предмета в игре.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Rarity</span> (Редкость)</td>
            <td>
                Цвет названия:<br>
                Common (Белый), Uncommon (Желтый), Rare (Голубой), Epic (Фиолетовый).
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Creative tab</span></td>
            <td>В какой вкладке искать.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Max stack size</span></td>
            <td>
                64 — стандарт (блоки, уголь).<br>
                16 — снежки, яйца.<br>
                1 — инструменты, зелья (нельзя сложить вместе).
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Enchantability</span></td>
            <td>Шанс хороших чар. Золото = 22, Камень = 5.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Item damage count</span></td>
            <td>Прочность. Если 0 — предмет вечный. Если 100 — сломается после 100 использований.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Damage vs entity</span></td>
            <td>Урон, если ударить моба этим предметом.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Is immune to fire</span></td>
            <td>Если галочка стоит, предмет не сгорит, если упадет в лаву (как Незерит).</td>
        </tr>
    </table>

    <h2>🍔 Вкладка 3: Food Properties (Еда)</h2>
    <p>Заполни это, если предмет можно съесть.</p>
    <ul>
        <li><span class="mc-param">Nutritional value:</span> Сколько голода восстановит (4 = 2 окорочка).</li>
        <li><span class="mc-param">Saturation:</span> Насыщение (скрытый сытость). Чем выше, тем дольше не хочется есть.</li>
        <li><span class="mc-param">Eating speed:</span> Как долго жевать (32 = стандарт).</li>
    </ul>
    """,

    "how-make-entity": """
    <h1>Сущность (Entity / Mob)</h1>
    <p>Самый сложный и интересный элемент! Создай своего монстра, друга или животное.</p>

    <h2>🚀 Создание</h2>
    <ol>
        <li>Нажми <strong>(+)</strong> -> <strong>Living Entity</strong>.</li>
        <li>Введи имя (например, <code>FireZombie</code>).</li>
    </ol>

    <h2>📋 Вкладка 1: Visual</h2>
    <ul>
        <li><span class="mc-param">Entity model:</span>
            <ul>
                <li><strong>Biped:</strong> Как человек/зомби.</li>
                <li><strong>Quadruped:</strong> На четырех ногах (корова).</li>
                <li><strong>Chicken/Spider/etc:</strong> Стандартные модели.</li>
                <li><strong>Java Model:</strong> Своя 3D модель из Blockbench.</li>
            </ul>
        </li>
        <li><span class="mc-param">Texture:</span> Картинка (скин) моба.</li>
        <li><span class="mc-param">Glow texture:</span> То, что светится в темноте (глаза).</li>
        <li><span class="mc-param">Entity shadow size:</span> Размер тени под ногами.</li>
    </ul>

    <h2>⚙️ Вкладка 2: Properties</h2>
    <table>
        <tr>
            <th>Параметр</th>
            <th>Описание</th>
        </tr>
        <tr>
            <td><span class="mc-param">Mob label</span></td>
            <td>Имя над головой (если пусто, то нет).</td>
        </tr>
        <tr>
            <td><span class="mc-param">Creature attribute</span></td>
            <td>
                Тип:<br>
                Undefined (Обычный)<br>
                Undead (Нежить: горит на солнце, лечится уроном)<br>
                Arthropod (Пауки)<br>
                Water (Водные)
            </td>
        </tr>
        <tr>
            <td><span class="mc-param">Health</span></td>
            <td>Здоровье. 20 = 10 сердец (как у игрока). Зомби = 20.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Experience amount</span></td>
            <td>Сколько опыта выпадет при смерти.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Movement speed</span></td>
            <td>Скорость бега. 0.3 — нормально. 0.2 — медленно.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Attack strength</span></td>
            <td>Урон в ближнем бою.</td>
        </tr>
        <tr>
            <td><span class="mc-param">Armor protection</span></td>
            <td>Встроенная защита (как будто он в броне).</td>
        </tr>
        <tr>
            <td><span class="mc-param">Equipment</span></td>
            <td>Что он держит в руках и носит при спавне?</td>
        </tr>
    </table>

    <h2>🧠 Вкладка 3: AI and Goals (Интеллект)</h2>
    <p>Здесь ты программируешь мозг моба. Список задач выполняется сверху вниз. Чем выше задача, тем она важнее.</p>

    <div class="tip">
        <strong>Пример для агрессивного монстра:</strong><br>
        1. Swim (Плавать, чтобы не утонуть).<br>
        2. Melee attack (Бить врага).<br>
        3. Wander (Бродить).<br>
        4. Look around (Смотреть по сторонам).<br>
        <br>
        <strong>Target Tasks (Кого бить):</strong><br>
        1. Hurt by target (Того, кто ударил меня).<br>
        2. Attack Player (Игрока).
    </div>

    <ul>
        <li><span class="mc-param">Wander:</span> Просто ходить.</li>
        <li><span class="mc-param">Look at player:</span> Поворачивать голову на игрока.</li>
        <li><span class="mc-param">Leap at target:</span> Прыгать на врага (как паук).</li>
        <li><span class="mc-param">Panic:</span> Убегать при уроне.</li>
        <li><span class="mc-param">Avoid entity:</span> Убегать от кого-то (например, от кошек).</li>
        <li><span class="mc-param">Breed:</span> Размножаться.</li>
    </ul>

    <h2>🥚 Вкладка 4: Spawning (Появление)</h2>
    <ul>
        <li><span class="mc-param">Spawn probability (Weight):</span> Шанс спавна. 100 = часто (Зомби). 10 = редко (Эндермен).</li>
        <li><span class="mc-param">Min/Max group size:</span> По одному или стаями? (Волки ходят по 4).</li>
        <li><span class="mc-param">Creature type:</span>
            <ul>
                <li>Monster: Спавнится в темноте.</li>
                <li>Creature: Спавнится на траве при свете (животные).</li>
                <li>Ambient: Летучие мыши.</li>
                <li>WaterCreature: Рыбы.</li>
            </ul>
        </li>
    </ul>
    """,

    "how-make-tool": """
    <h1>Инструмент (Tool)</h1>
    <p>Кирки, Топоры, Лопаты, Мотыги и Мечи (технически Меч — тоже инструмент).</p>

    <h2>⚙️ Свойства</h2>
    <ul>
        <li><span class="mc-param">Type:</span> Тип инструмента (Pickaxe, Axe, Sword, Shovel, Hoe, Multi-tool).</li>
        <li><span class="mc-param">Harvest Level:</span> Уровень добычи.
            <ul>
                <li>0: Дерево (не добывает железо).</li>
                <li>1: Камень (добывает железо).</li>
                <li>2: Железо (добывает алмазы).</li>
                <li>3: Алмаз (добывает обсидиан).</li>
                <li>4: Незерит.</li>
            </ul>
        </li>
        <li><span class="mc-param">Efficiency:</span> Скорость работы.
            <ul>
                <li>Wood: 2</li>
                <li>Stone: 4</li>
                <li>Iron: 6</li>
                <li>Diamond: 8</li>
                <li>Gold: 12 (самый быстрый, но хрупкий)</li>
            </ul>
        </li>
        <li><span class="mc-param">Enchantability:</span> Шанс хороших чар.</li>
        <li><span class="mc-param">Attack Speed:</span> Скорость замаха. Топоры медленные (1.0), мечи быстрые (1.6).</li>
        <li><span class="mc-param">Damage vs Entity:</span> Урон по врагам.</li>
        <li><span class="mc-param">Repair item:</span> Чем чинить в наковальне? Выбери свой слиток или материал.</li>
    </ul>
    """,

    "how-make-armor": """
    <h1>Броня (Armor)</h1>

    <h2>👔 Текстуры</h2>
    <p>Для брони нужно две специальные текстуры развертки:</p>
    <ol>
        <li><strong>Layer 1:</strong> Рисуются Шлем, Нагрудник и Ботинки.</li>
        <li><strong>Layer 2:</strong> Рисуются Штаны (Leggings).</li>
    </ol>
    <div class="tip">Используй встроенный редактор текстур MCreator (Tools -> Create armor texture), чтобы нарисовать их правильно.</div>

    <h2>🛡️ Параметры защиты</h2>
    <p>Настраиваются отдельно для каждого элемента (Helmet, Body, Leggings, Boots).</p>
    <ul>
        <li><span class="mc-param">Defense Value:</span> Количество "щитков" брони.
            <ul>
                <li>Кожа: 1-2</li>
                <li>Железо: 2-6</li>
                <li>Алмаз: 3-8</li>
            </ul>
        </li>
        <li><span class="mc-param">Toughness:</span> Твердость брони. Защищает от сильных ударов. У алмаза 2.0.</li>
        <li><span class="mc-param">Knockback resistance:</span> Сопротивление отбрасыванию (как у Незерита).</li>
    </ul>
    """,

    "how-make-biome": """
    <h1>Биом (Biome)</h1>
    <p>Создай свою природную зону.</p>

    <h2>📋 Настройки</h2>
    <ul>
        <li><span class="mc-param">Ground block:</span> Верхний блок (обычно Трава).</li>
        <li><span class="mc-param">Underground block:</span> Блок под верхом (обычно Земля, 3-5 слоев).</li>
        <li><span class="mc-param">Biome category:</span> Тип (Forest, Desert, Icy, Ocean). Влияет на поведение игры (например, в Icy замерзает вода).</li>
    </ul>

    <h2>🎨 Атмосфера</h2>
    <p>Перекрась мир!</p>
    <ul>
        <li><span class="mc-param">Sky color:</span> Цвет неба.</li>
        <li><span class="mc-param">Grass color:</span> Цвет травы в этом биоме.</li>
        <li><span class="mc-param">Water color:</span> Цвет воды (можно сделать зеленую кислоту).</li>
        <li><span class="mc-param">Fog color:</span> Цвет тумана на горизонте.</li>
    </ul>

    <h2>🌳 Генерация</h2>
    <ul>
        <li><span class="mc-param">Temperature:</span> Температура. > 1.0 = сухо (пустыня). < 0.15 = снег.</li>
        <li><span class="mc-param">Rainfall:</span> Влажность.</li>
        <li><span class="mc-param">Trees per chunk:</span> Плотность леса. 0 = поле, 10 = лес, 50 = джунгли.</li>
    </ul>
    """,

    "how-make-procedure": """
    <h1>Процедуры (Procedures)</h1>
    <p>Это сердце твоего мода. Скрипты, которые управляют логикой.</p>

    <h2>🧩 Как собрать процедуру</h2>
    <p>Интерфейс похож на Scratch. Ты перетаскиваешь блоки из списка справа в рабочую область.</p>

    <h3>Основные категории блоков:</h3>
    <ul>
        <li><strong>Event Management:</strong> Работа с отменой событий (Cancel Event).</li>
        <li><strong>Flow Control:</strong> Условия (If/Else), Циклы (Repeat), Ожидание (Wait).</li>
        <li><strong>Entity Procedures:</strong> Убить моба, вылечить, дать эффект, телепортировать.</li>
        <li><strong>Block Procedures:</strong> Сломать блок, поставить блок, проверить блок.</li>
        <li><strong>Item Procedures:</strong> Дать предмет, удалить предмет, зачаровать.</li>
        <li><strong>World Procedures:</strong> Время, погода, взрывы, молнии.</li>
    </ul>

    <h2>🎯 Глобальные Триггеры (Global Triggers)</h2>
    <p>Можно создать процедуру, которая срабатывает не от блока, а от глобального события игры. Для этого создай элемент "Procedure" и выбери триггер:</p>
    <ul>
        <li><span class="mc-param">Player joins the world:</span> Игрок зашел.</li>
        <li><span class="mc-param">Player wakes up:</span> Игрок поспал.</li>
        <li><span class="mc-param">Entity dies:</span> Кто-то умер.</li>
        <li><span class="mc-param">Command executed:</span> Введена команда.</li>
    </ul>

    <div class="warning">
        <strong>Зависимости (Dependencies):</strong><br>
        Не все блоки кода доступны во всех триггерах. Например, блок "Source Entity" (кто ударил) доступен только в триггере "Entity attacked", но недоступен в "Update Tick". MCreator подсветит ошибки красным.
    </div>
    """,

    "how-make-recipe": """
    <h1>Рецепт (Recipe)</h1>

    <h2>Типы рецептов</h2>
    <ul>
        <li><strong>Crafting:</strong> Верстак (3x3).</li>
        <li><strong>Smelting:</strong> Печка (Руда -> Слиток).</li>
        <li><strong>Blasting:</strong> Плавильня (Быстрая плавка руд).</li>
        <li><strong>Smoking:</strong> Коптильня (Быстрая жарка еды).</li>
        <li><strong>Stonecutting:</strong> Камнерез.</li>
        <li><strong>Campfire cooking:</strong> Костер.</li>
        <li><strong>Smithing:</strong> Стол кузнеца (Незеритовое улучшение).</li>
    </ul>

    <h2>Как настроить крафт</h2>
    <p>Просто перетащи предметы из правой панели в сетку крафта. Если хочешь использовать <strong>Теги</strong> (например, "любая шерсть"), нажми кнопку "Use tags" под слотом.</p>
    """,

    "how-make-fluid": """
    <h1>Жидкость (Fluid)</h1>
    <p>Создай новую воду, лаву или кислоту.</p>
    <h2>Текстуры</h2>
    <p>Нужно две текстуры (Still - стоячая, Flowing - текущая). Они должны быть анимированными (файл .png.mcmeta).</p>
    <h2>Физика</h2>
    <ul>
        <li><span class="mc-param">Density (Плотность):</span> Вода = 1000. Если меньше 0, жидкость (газ) полетит вверх.</li>
        <li><span class="mc-param">Viscosity (Вязкость):</span> Как медленно она течет. Лава = 6000 (очень вязкая).</li>
        <li><span class="mc-param">Luminance:</span> Светится ли она?</li>
    </ul>
    """,

    "how-make-structure": """
    <h1>Структура (Structure)</h1>
    <p>Добавь свои постройки в генерацию мира.</p>

    <h2>1. Подготовка</h2>
    <ol>
        <li>Построй здание в игре.</li>
        <li>Используй <strong>Structure Block</strong> (выдай командой <code>/give @p structure_block</code>).</li>
        <li>Выдели зону (режим Save), нажми SAVE.</li>
        <li>Файл .nbt сохранится в папке мира.</li>
    </ol>

    <h2>2. Импорт в MCreator</h2>
    <ol>
        <li>Resources -> Structures -> Import structure from Minecraft.</li>
        <li>Выбери файл .nbt.</li>
    </ol>

    <h2>3. Настройка спавна</h2>
    <ul>
        <li><span class="mc-param">Probability:</span> Шанс. 1,000,000 = очень редко. 1000 = очень часто.</li>
        <li><span class="mc-param">World types:</span> Surface (поверхность), Nether (Ад).</li>
    </ul>
    """
}

def generate():
    for slug, content in PAGES.items():
        filename = f"{slug}.html"
        filepath = os.path.join(OUTPUT_DIR, filename)

        full_html = f"""<html>
<head>
    <meta charset="utf-8">
    <title>{slug} - MCreator Wiki</title>
    {STYLE}
</head>
<body>
    {content}
    <br><hr>
    <p><small><a href="index.html">⬅️ Вернуться к оглавлению</a></small></p>
</body>
</html>"""

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(full_html)
        print(f"Generated {filename} ({len(content)} chars)")

if __name__ == "__main__":
    generate()
