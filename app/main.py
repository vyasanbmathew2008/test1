from pathlib import Path
import os
import uuid

import fitz
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

from app.database import init_db, save_script, list_scripts, get_script, delete_script
from app.services.ai_engine import generate_script

ROOT = Path(__file__).resolve().parent.parent
UPLOADS = ROOT / "uploads"
FRONTEND = ROOT / "frontend"
UPLOADS.mkdir(exist_ok=True)
MAX_BYTES = int(os.getenv("MAX_UPLOAD_MB", "30")) * 1024 * 1024

app = FastAPI(title="PDF to Podcast Script Generator")
app.mount("/static", StaticFiles(directory=FRONTEND), name="static")

@app.on_event("startup")
def startup():
    init_db()

@app.get("/")
def home():
    return FileResponse(FRONTEND / "index.html")

@app.post("/api/generate")
async def create_script(
    file: UploadFile = File(...),
    podcast_format: str = Form(...),
    tone: str = Form(...),
    duration: str = Form(...),
    language: str = Form(...),
    depth: str = Form(...),
    instructions: str = Form("")
):
    if not file.filename or not file.filename.lower().endswith(".pdf"):
        raise HTTPException(400, "Please upload a PDF file.")
    raw = await file.read()
    if len(raw) > MAX_BYTES:
        raise HTTPException(413, f"PDF exceeds the {MAX_BYTES // (1024*1024)} MB upload limit.")
    if not raw.startswith(b"%PDF"):
        raise HTTPException(400, "The uploaded file is not a valid PDF.")
    temp_path = UPLOADS / f"{uuid.uuid4().hex}.pdf"
    temp_path.write_bytes(raw)
    try:
        with fitz.open(temp_path) as doc:
            text = "\n\n".join(page.get_text("text") for page in doc)
        if not text.strip():
            try:
                import pytesseract
                from PIL import Image
                import io
                with fitz.open(temp_path) as doc:
                    text = "\n\n".join(pytesseract.image_to_string(Image.open(io.BytesIO(page.get_pixmap(dpi=180).tobytes("png")))) for page in doc)
            except ImportError:
                raise HTTPException(422, "No selectable text found. Install pytesseract and Pillow, plus the Tesseract OCR application, to process scanned PDFs.")
        if not text.strip():
            raise HTTPException(422, "No readable text found in this PDF.")
        script = await generate_script(text, podcast_format, tone, duration, language, depth, instructions)
        title = Path(file.filename).stem
        script_id = save_script(title, script, file.filename)
        return {"id": script_id, "title": title, "script": script}
    finally:
        temp_path.unlink(missing_ok=True)

@app.get("/api/history")
def history():
    return {"items": list_scripts()}

@app.get("/api/history/{script_id}")
def history_item(script_id: int):
    item = get_script(script_id)
    if not item:
        raise HTTPException(404, "Script not found.")
    return item

@app.delete("/api/history/{script_id}")
def remove_history(script_id: int):
    delete_script(script_id)
    return {"ok": True}
