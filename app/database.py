import sqlite3
from pathlib import Path
from datetime import datetime, timezone

DB = Path(__file__).resolve().parent.parent / "podcasts.db"

def connect():
    conn = sqlite3.connect(DB)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    with connect() as db:
        db.execute("""CREATE TABLE IF NOT EXISTS scripts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            filename TEXT NOT NULL,
            script TEXT NOT NULL,
            created_at TEXT NOT NULL
        )""")

def save_script(title, script, filename):
    with connect() as db:
        cur = db.execute("INSERT INTO scripts(title,filename,script,created_at) VALUES(?,?,?,?)",
                         (title, filename, script, datetime.now(timezone.utc).isoformat()))
        return cur.lastrowid

def list_scripts():
    with connect() as db:
        return [dict(r) for r in db.execute("SELECT id,title,filename,created_at FROM scripts ORDER BY id DESC LIMIT 100")]

def get_script(script_id):
    with connect() as db:
        row = db.execute("SELECT * FROM scripts WHERE id=?", (script_id,)).fetchone()
        return dict(row) if row else None

def delete_script(script_id):
    with connect() as db:
        db.execute("DELETE FROM scripts WHERE id=?", (script_id,))
