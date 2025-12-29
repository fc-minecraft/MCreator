import requests
from bs4 import BeautifulSoup
from markdownify import markdownify as md
import json
import os
import re

BASE_URL = "https://mcreator.net/wiki/"
OUTPUT_DIR = "src/main/resources/help/ru_RU/wiki/"
TOC_FILE = "src/main/resources/help/toc.json"
SLUG_MAP_FILE = "src/main/resources/help/slug_map.json"

# Create output directory
os.makedirs(OUTPUT_DIR, exist_ok=True)

# List of specific pages to scrape to cover key areas
# (Since scraping the *entire* wiki might be too much/blocked, we focus on the structure requested)
PAGES_TO_SCRAPE = [
    # General
    {"url": "https://mcreator.net/wiki/how-make-block", "slug": "block", "title": "Блоки"},
    {"url": "https://mcreator.net/wiki/how-make-item", "slug": "item", "title": "Предметы"},
    {"url": "https://mcreator.net/wiki/how-make-tool", "slug": "tool", "title": "Инструменты"},
    {"url": "https://mcreator.net/wiki/how-make-armor", "slug": "armor", "title": "Броня"},
    {"url": "https://mcreator.net/wiki/how-make-animated-texture", "slug": "animation", "title": "Анимации"},
    {"url": "https://mcreator.net/wiki/how-make-procedure", "slug": "procedures", "title": "Процедуры"},
    {"url": "https://mcreator.net/wiki/procedure-blocks", "slug": "procedure-blocks", "title": "Блоки процедур"},
    {"url": "https://mcreator.net/wiki/global-triggers", "slug": "global_triggers", "title": "Глобальные триггеры"},
    {"url": "https://mcreator.net/wiki/variables", "slug": "variables", "title": "Переменные"},
    {"url": "https://mcreator.net/wiki/how-make-living-entity", "slug": "entity", "title": "Сущности"},
    {"url": "https://mcreator.net/wiki/how-make-biome", "slug": "biome", "title": "Биомы"},
    {"url": "https://mcreator.net/wiki/how-make-dimension", "slug": "dimension", "title": "Измерения"},
    {"url": "https://mcreator.net/wiki/how-make-gui", "slug": "gui", "title": "Интерфейсы (GUI)"},
    {"url": "https://mcreator.net/wiki/how-make-recipe", "slug": "recipe", "title": "Рецепты"},
    {"url": "https://mcreator.net/wiki/how-make-structure", "slug": "structure", "title": "Структуры"},
    {"url": "https://mcreator.net/wiki/how-make-fluid", "slug": "fluid", "title": "Жидкости"},
    {"url": "https://mcreator.net/wiki/how-make-plant", "slug": "plant", "title": "Растения"},
    {"url": "https://mcreator.net/wiki/how-make-particle", "slug": "particle", "title": "Частицы"},
    {"url": "https://mcreator.net/wiki/how-make-command", "slug": "command", "title": "Команды"},
    {"url": "https://mcreator.net/wiki/how-make-key-binding", "slug": "keybinding", "title": "Клавиши"},
    {"url": "https://mcreator.net/wiki/how-make-creative-tab", "slug": "tab", "title": "Творческие вкладки"},
    {"url": "https://mcreator.net/wiki/how-make-overlay", "slug": "overlay", "title": "Наложения"},
    {"url": "https://mcreator.net/wiki/how-make-achievement", "slug": "achievement", "title": "Достижения"},
    {"url": "https://mcreator.net/wiki/how-make-music-disc", "slug": "musicdisc", "title": "Музыкальные пластинки"},
    {"url": "https://mcreator.net/wiki/how-make-painting", "slug": "painting", "title": "Картины"},
    {"url": "https://mcreator.net/wiki/how-make-tag", "slug": "tag", "title": "Связки (Tags)"},
    {"url": "https://mcreator.net/wiki/how-make-function", "slug": "function", "title": "Функции"},
    {"url": "https://mcreator.net/wiki/how-make-loot-table", "slug": "loot_table", "title": "Таблицы лута"},
    {"url": "https://mcreator.net/wiki/workspace-tab", "slug": "workspace", "title": "Рабочее пространство"},
    {"url": "https://mcreator.net/wiki/interface", "slug": "interface", "title": "Интерфейс"},
]

slug_map = {
    "index": "index.md"
}

toc_entries = [
    {"title": "help_browser.toc.home", "file": "index.md", "children": []}
]

def scrape_page(url, slug, title):
    print(f"Scraping {url}...")
    try:
        response = requests.get(url, timeout=10)
        if response.status_code != 200:
            print(f"Failed to retrieve {url}")
            return None

        soup = BeautifulSoup(response.content, 'html.parser')

        # Extract main content
        content_div = soup.find(id="block-system-main")
        if not content_div:
            content_div = soup.find(class_="content")

        if not content_div:
            print(f"Could not find content for {url}")
            return None

        # Clean up
        for tag in content_div.find_all(['script', 'style', 'iframe']):
            tag.decompose()

        # Remove "Rate this" or social buttons
        for div in content_div.find_all(class_="rate-widget-2"):
            div.decompose()

        # Convert to Markdown
        html_content = str(content_div)
        markdown = md(html_content, heading_style="ATX")

        # Add Header
        markdown = f"# {title}\n\n*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*\n\n" + markdown

        filename = f"{slug}.md"
        with open(os.path.join(OUTPUT_DIR, filename), 'w', encoding='utf-8') as f:
            f.write(markdown)

        slug_map[slug] = filename
        slug_map[url.replace("https://mcreator.net/wiki/", "")] = filename # Map full wiki slug too

        return {"title": title, "file": filename}

    except Exception as e:
        print(f"Error scraping {url}: {e}")
        return None

# Create Index
with open(os.path.join(OUTPUT_DIR, "index.md"), 'w', encoding='utf-8') as f:
    f.write("# Справка MCreator\n\nДобро пожаловать во встроенную систему справки.\nВыберите интересующий вас раздел в меню слева.\n")

# Scrape loop
children_nodes = []
for page in PAGES_TO_SCRAPE:
    node = scrape_page(page["url"], page["slug"], page["title"])
    if node:
        children_nodes.append(node)

# Build TOC
toc_entries[0]["children"] = children_nodes

# Save JSONs
with open(TOC_FILE, 'w', encoding='utf-8') as f:
    json.dump(toc_entries, f, indent=2, ensure_ascii=False)

with open(SLUG_MAP_FILE, 'w', encoding='utf-8') as f:
    json.dump(slug_map, f, indent=2, ensure_ascii=False)

print("Scraping complete.")
