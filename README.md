# PDF to Podcast Script Generator

A local, browser-based app that turns PDF documents into editable podcast scripts. It generates **text only**—no audio, voice synthesis, or TTS.

## Features
- Upload PDF files and extract text with PyMuPDF
- Optional OCR fallback for scanned PDFs (requires Tesseract installed)
- Custom format, tone, duration, language, and content-depth instructions
- OpenRouter-compatible chat completions API
- Chunking and intermediate summaries for long documents
- Edit and regenerate the full script; export Markdown or TXT
- Local SQLite history

## Run locally
1. Install Python 3.10+ and (optionally) Tesseract OCR.
2. Create and activate a virtual environment.
3. Install dependencies: `pip install -r requirements.txt`
4. Copy `.env.example` to `.env` and add your OpenRouter API key.
5. Start: `uvicorn app.main:app --reload`
6. Open http://127.0.0.1:8000

The app runs on your computer; no VPS is required. Keep your API key private and never commit `.env`.

## Configuration
Set `OPENROUTER_API_KEY`, optionally `OPENROUTER_MODEL` (default: `openai/gpt-4o-mini`), and `MAX_UPLOAD_MB` in `.env`. Model availability and pricing are controlled by OpenRouter and the selected provider.

## Notes
OCR requires the Tesseract executable to be installed separately. PDF extraction and generation happen locally except for the document text sent to the configured AI provider for script generation. Do not upload confidential documents unless you are comfortable sending their extracted text to that provider.
