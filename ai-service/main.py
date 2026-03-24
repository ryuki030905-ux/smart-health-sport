"""
ai-service/main.py
FastAPI 入口文件
"""
import os
from typing import Any, Dict, Optional

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv

from agent import run_health_agent, get_debug_snapshot
from rag import get_rag_status

load_dotenv()

DEBUG_VERBOSE = os.getenv("DEBUG_VERBOSE", "false").strip().lower() == "true"
ALLOW_CREDENTIALS = os.getenv("ALLOW_CREDENTIALS", "false").strip().lower() == "true"
ALLOWED_ORIGINS = [origin.strip() for origin in os.getenv("ALLOWED_ORIGINS", "http://localhost:5173,http://127.0.0.1:5173").split(",") if origin.strip()]

app = FastAPI(
    title="健康运动 AI 服务",
    description="基于 LangChain Agent + RAG 的个性化健康建议服务",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=ALLOW_CREDENTIALS,
    allow_methods=["*"],
    allow_headers=["*"],
)


class AdviceRequest(BaseModel):
    user_id: int
    advice_type: str          # DIET / EXERCISE / GENERAL
    weight: Optional[float] = None
    exercise_minutes_today: Optional[int] = None
    calories_intake_today: Optional[float] = None
    calories_burned_today: Optional[float] = None
    goal: Optional[str] = None
    extra_question: Optional[str] = None
    record_date: Optional[str] = None   # YYYY-MM-DD


class AdviceResponse(BaseModel):
    advice_type: str
    content: str
    summary: Optional[str] = None
    debug: Optional[Dict[str, Any]] = None


class DebugLlmRequest(BaseModel):
    prompt: str = "你好，请用一句话确认 AI 服务联通正常。"
    advice_type: str = "GENERAL"
    extra_question: Optional[str] = None


@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "service": "ai-service",
        "rag": get_rag_status(),
    }


if DEBUG_VERBOSE:
    @app.get("/debug/status")
    def debug_status():
        return get_debug_snapshot()


    @app.post("/debug/llm")
    def debug_llm(req: DebugLlmRequest):
        class _Req:
            user_id = 0
            advice_type = req.advice_type
            weight = None
            exercise_minutes_today = None
            calories_intake_today = None
            calories_burned_today = None
            goal = "调试"
            extra_question = req.extra_question or req.prompt
            record_date = None

        result = run_health_agent(_Req())
        return {
            "success": True,
            "result": result,
        }


@app.post("/agent/advice", response_model=AdviceResponse)
def get_advice(req: AdviceRequest) -> AdviceResponse:
    result = run_health_agent(req)

    summary = result["content"][:80].strip() + "…" if len(result["content"]) > 80 else result["content"]

    return AdviceResponse(
        advice_type=result["advice_type"],
        content=result["content"],
        summary=summary,
        debug=result.get("debug"),
    )


if __name__ == "__main__":
    import uvicorn

    port = int(os.getenv("PORT", "8000"))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
