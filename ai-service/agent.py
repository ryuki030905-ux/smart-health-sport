"""
ai-service/agent.py
健康建议主逻辑：
- 拉取 Spring Boot 用户当天数据
- 拼接 RAG 上下文
- 调用大模型生成建议
- 提供调试信息与更清晰的错误提示
"""
from typing import Dict, Any
import os
import json
import httpx
import asyncio
from dataclasses import dataclass

try:
    from langchain_openai import ChatOpenAI
except ImportError:
    from langchain.chat_models import ChatOpenAI

from rag import get_rag_context, get_rag_status


SPRING_BOOT_BASE_URL = os.getenv("SPRING_BOOT_BASE_URL", "http://localhost:8080").rstrip("/")
DEBUG_VERBOSE = os.getenv("DEBUG_VERBOSE", "true").strip().lower() == "true"
LLM_AUTO_DETECT_MODELS = os.getenv("LLM_AUTO_DETECT_MODELS", "true").strip().lower() == "true"
LLM_ENABLE_MODEL_FALLBACK = os.getenv("LLM_ENABLE_MODEL_FALLBACK", "true").strip().lower() == "true"


@dataclass
class LlmConfig:
    provider: str
    api_key: str
    model_name: str
    base_url: str | None


def _spring_url(path: str) -> str:
    return f"{SPRING_BOOT_BASE_URL}{path}"


def _normalize_model_candidates(model_name: str, available_models: list[str] | None = None) -> list[str]:
    candidates: list[str] = []

    def push(value: str | None):
        if value and value not in candidates:
            candidates.append(value)

    push(model_name)
    stripped = model_name.strip()
    push(stripped)
    if stripped.startswith("/"):
        push(stripped[1:])
    else:
        push(f"/{stripped}")

    if available_models:
        simple_name = stripped.lstrip("/")
        for item in available_models:
            if item == stripped or item == simple_name or item.endswith(f"/{simple_name}"):
                push(item)

    return candidates


def _fetch_available_models(cfg: LlmConfig) -> list[str]:
    if not cfg.base_url or not LLM_AUTO_DETECT_MODELS:
        return []

    headers = {}
    if cfg.api_key:
        headers["Authorization"] = f"Bearer {cfg.api_key}"

    try:
        with httpx.Client(timeout=20) as client:
            resp = client.get(f"{cfg.base_url.rstrip('/')}/models", headers=headers)
            resp.raise_for_status()
            payload = resp.json()
            items = payload.get("data", []) if isinstance(payload, dict) else []
            return [x.get("id") for x in items if isinstance(x, dict) and x.get("id")]
    except Exception as e:
        print(f"[Agent] 获取模型列表失败（忽略，不影响主流程）: {e}")
        return []


def query_health_record(user_id: int, date: str = None) -> str:
    date_param = f"&date={date}" if date else ""
    url = _spring_url(f"/internal/health?userId={user_id}{date_param}")
    try:
        r = httpx.get(url, timeout=10)
        r.raise_for_status()
        data = r.json().get("data", {})
        record = data.get("record")
        if not record:
            return f"用户 {user_id} 在 {date or '今日'} 没有健康记录"
        return json.dumps(record, ensure_ascii=False, default=str)
    except Exception as e:
        return f"[健康记录查询失败] {e}"


def query_exercise(user_id: int, date: str = None) -> str:
    url = _spring_url(f"/internal/exercise?userId={user_id}") + (f"&date={date}" if date else "")
    try:
        r = httpx.get(url, timeout=10)
        r.raise_for_status()
        return json.dumps(r.json().get("data", {}), ensure_ascii=False)
    except Exception as e:
        return f"[运动数据查询失败] {e}"


def query_diet(user_id: int, date: str = None) -> str:
    url = _spring_url(f"/internal/diet?userId={user_id}") + (f"&date={date}" if date else "")
    try:
        r = httpx.get(url, timeout=10)
        r.raise_for_status()
        return json.dumps(r.json().get("data", {}), ensure_ascii=False)
    except Exception as e:
        return f"[饮食数据查询失败] {e}"


def _build_rag_prompt(advice_type: str, user_context: str, extra_question: str) -> str:
    rag_knowledge = get_rag_context(advice_type)

    base = f"""你是一位专业、温暖、有循证依据的健康管理助手（健身教练+营养师）。
请根据以下【用户今日数据】和【健康知识库】，为用户提供个性化建议。

【用户数据】
{user_context}

【相关健康知识】
{rag_knowledge}

【用户补充提问】
{extra_question or "无"}

请用中文回复，语气亲切自然，分点说明，每条建议给出具体可执行的行动。
"""

    if advice_type == "DIET":
        return base + "\n重点：分析今日热量摄入是否达标，给出饮食调整建议（加餐/减餐/替换食物）。"
    elif advice_type == "EXERCISE":
        return base + "\n重点：分析今日运动强度是否足够，给出运动计划建议（时长/类型/强度调整）。"
    else:
        return base + "\n重点：给出综合健康建议，涵盖饮食、运动、作息三方面。"


def _load_llm_config() -> LlmConfig:
    provider   = os.getenv("LLM_PROVIDER", "custom").strip().lower()
    api_key    = os.getenv("LLM_API_KEY", "").strip()
    model_name = os.getenv("LLM_MODEL", "").strip()

    if not model_name:
        raise ValueError("LLM_MODEL 环境变量未设置，请在 .env 中配置模型名称")

    if provider == "siliconflow":
        return LlmConfig(provider, api_key, model_name, "https://api.siliconflow.cn/v1")
    if provider == "zhipu":
        return LlmConfig(provider, api_key, model_name, "https://open.bigmodel.cn/api/paas/v4")
    if provider == "ollama":
        return LlmConfig(provider, "ollama", model_name, "http://localhost:11434/v1")
    if provider == "openai":
        return LlmConfig(provider, api_key, model_name, None)

    base_url = os.getenv("LLM_BASE_URL", "").strip()
    if not base_url:
        raise ValueError("LLM_PROVIDER=custom 时必须设置 LLM_BASE_URL")
    return LlmConfig(provider, api_key or "dummy-key", model_name, base_url.rstrip("/"))


def _build_chat_openai(model_name: str, cfg: LlmConfig):
    common_kwargs = {
        "model": model_name,
        "temperature": 0.7,
        "max_tokens": 1024,
        "request_timeout": 120,
    }

    if cfg.base_url:
        return ChatOpenAI(
            **common_kwargs,
            api_key=cfg.api_key,
            base_url=cfg.base_url,
        )

    return ChatOpenAI(
        **common_kwargs,
        api_key=cfg.api_key,
    )


def _invoke_llm(prompt_text: str) -> tuple[str, dict]:
    cfg = _load_llm_config()
    available_models = _fetch_available_models(cfg)
    candidates = [cfg.model_name]
    if LLM_ENABLE_MODEL_FALLBACK:
        candidates = _normalize_model_candidates(cfg.model_name, available_models)

    last_error = None
    for candidate in candidates:
        try:
            llm = _build_chat_openai(candidate, cfg)
            response = llm.invoke(prompt_text).content
            debug = {
                "provider": cfg.provider,
                "model_requested": cfg.model_name,
                "model_used": candidate,
                "base_url": cfg.base_url,
                "available_models_sample": available_models[:20],
            }
            return response, debug
        except Exception as e:
            last_error = e
            print(f"[Agent] 模型尝试失败 model={candidate}: {e}")

    raise RuntimeError(str(last_error) if last_error else "LLM 调用失败")


def get_debug_snapshot() -> dict:
    cfg = _load_llm_config()
    available_models = _fetch_available_models(cfg)
    return {
        "spring_boot_base_url": SPRING_BOOT_BASE_URL,
        "llm": {
            "provider": cfg.provider,
            "model": cfg.model_name,
            "base_url": cfg.base_url,
            "available_models_count": len(available_models),
            "available_models_sample": available_models[:30],
        },
        "rag": get_rag_status(),
    }


def run_health_agent(req) -> Dict[str, Any]:
    def sync_fetch():
        loop = asyncio.new_event_loop()
        try:
            health_url = _spring_url(f"/internal/health?userId={req.user_id}&date={req.record_date or ''}")
            exercise_url = _spring_url(f"/internal/exercise?userId={req.user_id}&date={req.record_date or ''}")
            diet_url = _spring_url(f"/internal/diet?userId={req.user_id}&date={req.record_date or ''}")

            async def fetch_all():
                async with httpx.AsyncClient(timeout=10) as client:
                    return await asyncio.gather(
                        client.get(health_url),
                        client.get(exercise_url),
                        client.get(diet_url),
                    )

            a, b, c = loop.run_until_complete(fetch_all())
            health_data = a.json().get("data", {})
            exercise_data = b.json().get("data", {})
            diet_data = c.json().get("data", {})
            ctx = f"体重: {health_data.get('record', {}).get('weight', '未知')} kg\n"
            ctx += f"今日运动: {exercise_data.get('totalMinutes', 0)} 分钟, 消耗 {exercise_data.get('totalCalories', 0)} kcal\n"
            ctx += f"今日饮食: 摄入 {diet_data.get('totalCalories', 0)} kcal"
            return ctx
        except Exception as e:
            parts = []
            if req.weight:
                parts.append(f"体重: {req.weight} kg")
            if req.exercise_minutes_today:
                parts.append(f"今日运动: {req.exercise_minutes_today} 分钟, 消耗 {req.calories_burned_today or 0} kcal")
            if req.calories_intake_today:
                parts.append(f"今日饮食: 摄入 {req.calories_intake_today} kcal")
            return "\n".join(parts) if parts else "暂无用户数据，请根据知识库给出通用健康建议。"
        finally:
            loop.close()

    user_context = sync_fetch()
    prompt_text = _build_rag_prompt(req.advice_type, user_context, req.extra_question)

    try:
        response, debug_info = _invoke_llm(prompt_text)
    except Exception as e:
        error_msg = str(e)
        print(f"[Agent] LLM 调用失败: {error_msg}")
        response = (
            f"抱歉，AI 服务暂时无法响应（{error_msg[:100]}）。"
            f"可能原因：模型配置不兼容、代理服务鉴权异常、网络连接异常或 API 额度不足。"
            f"请检查 .env 中的 LLM_MODEL、LLM_BASE_URL、LLM_API_KEY 与调试接口输出。"
        )
        debug_info = {
            "error": error_msg,
            "rag": get_rag_status(),
        }

    result = {
        "advice_type": req.advice_type,
        "content": response.strip(),
    }

    if DEBUG_VERBOSE:
        result["debug"] = debug_info

    return result
