import os
import httpx
from dotenv import load_dotenv

load_dotenv()
API_URL = "https://openrouter.ai/api/v1/chat/completions"

def chunks(text, size=12000):
    paragraphs = text.splitlines()
    result, current = [], ""
    for paragraph in paragraphs:
        if len(current) + len(paragraph) + 1 > size and current:
            result.append(current)
            current = ""
        current += paragraph + "\n"
    if current.strip():
        result.append(current)
    return result

async def ask(client, api_key, model, messages, max_tokens=3000):
    response = await client.post(API_URL, headers={
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
        "HTTP-Referer": os.getenv("OPENROUTER_SITE_URL", "http://127.0.0.1:8000"),
        "X-Title": os.getenv("OPENROUTER_APP_NAME", "PDF Podcast Script Generator")
    }, json={"model": model, "messages": messages, "temperature": 0.7, "max_tokens": max_tokens})
    if response.status_code >= 400:
        raise ValueError(f"AI provider returned {response.status_code}: {response.text[:500]}")
    data = response.json()
    return data["choices"][0]["message"]["content"]

async def generate_script(source, podcast_format, tone, duration, language, depth, instructions=""):
    api_key = os.getenv("OPENROUTER_API_KEY", "")
    if not api_key or api_key == "your_openrouter_api_key_here":
        raise ValueError("Add your OpenRouter API key to .env before generating a script.")
    model = os.getenv("OPENROUTER_MODEL", "openai/gpt-4o-mini")
    parts = chunks(source)
    async with httpx.AsyncClient(timeout=180) as client:
        if len(parts) == 1:
            context = parts[0]
        else:
            summaries = []
            for i, part in enumerate(parts, 1):
                summaries.append(await ask(client, api_key, model, [
                    {"role": "system", "content": "Summarize this document segment faithfully. Preserve key facts, names, figures, arguments, and caveats. Do not invent details."},
                    {"role": "user", "content": f"Segment {i}/{len(parts)}:\n{part}"}
                ], 1200))
            context = "\n\n".join(summaries)
        prompt = f"""Create a polished podcast SCRIPT (text only) based on the source below.
Format: {podcast_format}
Tone: {tone}
Approximate duration: {duration}
Language: {language}
Content depth: {depth}
Additional instructions: {instructions or 'None'}
Use speaker labels and natural spoken transitions where appropriate. Structure the script with a title, opening, coherent segments, and closing. Respect the requested duration as closely as possible. Stay faithful to the source; do not fabricate facts. If the source does not support a claim, omit it or qualify it.
SOURCE:
{context}"""
        return await ask(client, api_key, model, [
            {"role": "system", "content": "You are an expert podcast scriptwriter and careful document analyst."},
            {"role": "user", "content": prompt}
        ], 7000)
