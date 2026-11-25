# PDF to Podcast Script Studio

Turn a PDF into a polished, editable podcast script with AI. Choose the episode format, tone, target length, language, and level of detail—then generate, edit, copy, or export the result.

> **Text only:** this project creates podcast scripts. It does not generate audio, voiceovers, or MP3 files.

## What it does

- **Reads PDFs** using [PyMuPDF](https://pymupdf.readthedocs.io/).
- **Handles long documents** by splitting extracted text into sections and summarizing them before writing the final script.
- **Lets you shape the episode** with free-form settings for podcast format, tone, duration, language, and content depth.
- **Supports extra instructions** such as target audience, topics to emphasize, or things to avoid.
- **Uses OpenRouter** for AI script generation, with a configurable model.
- **Gives you an editable result** with copy, Markdown export, and plain-text export.
- **Keeps a local history** of generated scripts in SQLite, so you can reopen or delete saved scripts.

## How it works

1. Select or drop a PDF into the app.
2. The backend extracts selectable text from the document.
3. For longer PDFs, the text is divided into chunks and summarized to create a manageable source.
4. The source and your episode instructions are sent to the selected OpenRouter model.
5. The generated script appears in the editor. Make changes, copy it, or download it as `.md` or `.txt`.

## Requirements

- Python **3.10 or newer**
- An [OpenRouter API key](https://openrouter.ai/keys)
- Internet access for AI generation
- Tesseract OCR is optional and only needed for scanned/image-only PDFs

## Run locally

### 1. Get the project

```bash
git clone https://github.com/vyasanbmathew2008/test1.git
cd test1
```

### 2. Create a virtual environment

**macOS / Linux**
```bash
python3 -m venv .venv
source .venv/bin/activate
```

**Windows PowerShell**
```powershell
py -m venv .venv
.venv\\Scripts\\Activate.ps1
```

### 3. Install dependencies

```bash
python -m pip install --upgrade pip
pip install -r requirements.txt
```

### 4. Configure OpenRouter

Copy the example environment file:

**macOS / Linux**
```bash
cp .env.example .env
```

**Windows PowerShell**
```powershell
Copy-Item .env.example .env
```

Open `.env` and replace the placeholder with your OpenRouter key:

```dotenv
OPENROUTER_API_KEY=your_openrouter_api_key_here
OPENROUTER_MODEL=openai/gpt-4o-mini
OPENROUTER_SITE_URL=http://127.0.0.1:8000
OPENROUTER_APP_NAME=PDF Podcast Script Generator
MAX_UPLOAD_MB=30
```

### 5. Start the app

```bash
uvicorn app.main:app --reload
```

Open **http://127.0.0.1:8000** in your browser.

Stop the development server with `Ctrl+C`.

## Configuration

| Variable | Required | Description |
|---|---:|---|
| `OPENROUTER_API_KEY` | Yes | Your OpenRouter API key. Keep it private. |
| `OPENROUTER_MODEL` | No | Model identifier sent to OpenRouter. Defaults to `openai/gpt-4o-mini`. |
| `OPENROUTER_SITE_URL` | No | Site URL included in OpenRouter request headers. |
| `OPENROUTER_APP_NAME` | No | App name included in OpenRouter request headers. |
| `MAX_UPLOAD_MB` | No | Maximum PDF upload size in MB. Defaults to `30`. |

Model availability, rate limits, and pricing depend on OpenRouter and the model/provider you select. Check your OpenRouter account for current details.

## Scanned PDFs (optional OCR)

The normal PDF text-extraction path does not need OCR. If a PDF contains scanned pages with no selectable text, install the additional Python packages:

```bash
pip install pytesseract Pillow
```

You must also install the **Tesseract OCR application** separately and make sure its executable is available on your system PATH. Then restart the app and try the scanned PDF again.

## Project structure

```text
.
├── app/
│   ├── main.py                 # FastAPI routes and PDF processing
│   ├── database.py             # Local SQLite script history
│   └── services/
│       └── ai_engine.py        # OpenRouter requests and long-document chunking
├── frontend/
│   ├── index.html              # Browser interface
│   ├── style.css               # App styling
│   └── app.js                  # Upload, editor, history, and export behavior
├── .env.example                # Environment variable template
├── .gitignore
├── LICENSE                     # MIT License
└── requirements.txt
```

## API routes

The FastAPI backend exposes these routes:

| Method | Route | Purpose |
|---|---|---|
| `GET` | `/` | Serve the web app |
| `POST` | `/api/generate` | Extract a PDF and generate a script |
| `GET` | `/api/history` | List saved scripts |
| `GET` | `/api/history/{script_id}` | Retrieve one saved script |
| `DELETE` | `/api/history/{script_id}` | Delete a saved script |

Interactive API documentation is available at **http://127.0.0.1:8000/docs** while the app is running.

## Data, privacy, and security

- The PDF is processed by the local backend. Its extracted text is sent to OpenRouter to generate the script; review the provider's terms and privacy policy before using sensitive documents.
- Generated script history is stored in a local SQLite database named `podcasts.db` in the project directory.
- Uploaded temporary PDF files are removed after processing.
- Your API key belongs in `.env`. The repository ignores this file—**never commit your real key or paste it into frontend code**.
- Do not expose this development server publicly without adding appropriate authentication, rate limiting, and production security controls.

## Current scope and limitations

- Output is an editable text script, not audio.
- OCR is optional and requires a separate Tesseract installation.
- Script history is local to the machine running the backend.
- AI generation requires an internet connection and may incur charges depending on your OpenRouter model/provider.
- This repository is a local-first starter project; production hosting and persistent shared storage require additional setup.

## Contributing

Issues and pull requests are welcome. When reporting a problem, include your operating system, Python version, the steps to reproduce it, and the relevant error message. **Do not include API keys or confidential document content.**

## License

This project is licensed under the **MIT License**. You are free to use, copy, modify, distribute, sublicense, and sell copies of the software, subject to the conditions in the license.

See the [LICENSE](LICENSE) file for the full license text.
