"""
ai-service/rag.py
Chroma 向量库 + 免费 HuggingFace Embedding
支持 RAG 初始化失败自动降级，避免影响主链路 LLM 调用。
"""
import os
from typing import Optional

from langchain.embeddings import HuggingFaceBgeEmbeddings
from langchain.vectorstores import Chroma
from langchain.document_loaders import DirectoryLoader, TextLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter

KNOWLEDGE_DIR = os.path.join(os.path.dirname(__file__), "knowledge")
PERSIST_DIR = os.path.join(os.path.dirname(__file__), ".vectorstore")
EMBED_MODEL = os.getenv("EMBED_MODEL", "BAAI/bge-small-zh-v1.5").strip()
RAG_ENABLED = os.getenv("RAG_ENABLED", "true").strip().lower() == "true"
RAG_ALLOW_DEGRADED = os.getenv("RAG_ALLOW_DEGRADED", "true").strip().lower() == "true"

_vectorstore: Chroma | None = None
_rag_status: dict = {
    "enabled": RAG_ENABLED,
    "ready": False,
    "degraded": False,
    "embed_model": EMBED_MODEL,
    "message": "RAG 尚未初始化",
}


def _get_embed_model():
    return HuggingFaceBgeEmbeddings(
        model_name=EMBED_MODEL,
        model_kwargs={"device": "cpu"},
        encode_kwargs={"normalize_embeddings": True},
    )


def get_rag_status() -> dict:
    return dict(_rag_status)


def init_vectorstore() -> Chroma:
    """初始化（或加载）Chroma 向量库"""
    global _vectorstore

    if not RAG_ENABLED:
        _rag_status.update({
            "ready": False,
            "degraded": True,
            "message": "RAG 已禁用（RAG_ENABLED=false）",
        })
        raise RuntimeError("RAG 已禁用")

    if _vectorstore is not None:
        _rag_status.update({
            "ready": True,
            "degraded": False,
            "message": "RAG 已就绪（复用已加载向量库）",
        })
        return _vectorstore

    try:
        embed_model = _get_embed_model()

        if os.path.exists(PERSIST_DIR):
            _vectorstore = Chroma(
                persist_directory=PERSIST_DIR,
                embedding_function=embed_model,
                collection_metadata={"hnsw:space": "cosine"},
            )
            count = _vectorstore._collection.count()
            print(f"[RAG] 从 {PERSIST_DIR} 加载已有向量库，共 {count} 条记录")
            _rag_status.update({
                "ready": True,
                "degraded": False,
                "message": f"RAG 已加载，记录数 {count}",
            })
        else:
            if not os.path.exists(KNOWLEDGE_DIR):
                os.makedirs(KNOWLEDGE_DIR, exist_ok=True)
                _create_sample_knowledge()

            loader = DirectoryLoader(
                KNOWLEDGE_DIR,
                glob="**/*.md",
                loader_cls=TextLoader,
                loader_kwargs={"encoding": "utf-8"},
            )
            docs = loader.load()

            splitter = RecursiveCharacterTextSplitter(
                chunk_size=500,
                chunk_overlap=50,
                separators=["\n\n", "\n", "。", " "],
            )
            chunks = splitter.split_documents(docs)

            _vectorstore = Chroma.from_documents(
                documents=chunks,
                embedding=embed_model,
                persist_directory=PERSIST_DIR,
                collection_metadata={"hnsw:space": "cosine"},
            )
            _vectorstore.persist()
            print(f"[RAG] 向量库构建完毕，共 {len(chunks)} 个知识块，已保存到 {PERSIST_DIR}")
            _rag_status.update({
                "ready": True,
                "degraded": False,
                "message": f"RAG 已构建，知识块 {len(chunks)}",
            })

        return _vectorstore
    except Exception as e:
        _rag_status.update({
            "ready": False,
            "degraded": True,
            "message": f"RAG 初始化失败：{e}",
        })
        if RAG_ALLOW_DEGRADED:
            print(f"[RAG] 初始化失败，已降级为无知识库模式：{e}")
            raise RuntimeError(f"RAG 初始化失败（已降级）: {e}")
        raise


def get_rag_context(advice_type: str, top_k: int = 4) -> str:
    """
    根据建议类型检索相关知识片段
    advice_type: DIET | EXERCISE | GENERAL
    """
    if not RAG_ENABLED:
        return "（RAG 已禁用，以下回答将基于模型通用能力生成）"

    try:
        vs = init_vectorstore()
    except Exception as e:
        if RAG_ALLOW_DEGRADED:
            return f"（知识库暂不可用，已自动降级为通用回答模式：{e}）"
        raise

    query_map = {
        "DIET": "减脂饮食营养热量摄入食物推荐三餐搭配",
        "EXERCISE": "运动计划有氧无氧燃脂心率训练强度",
        "GENERAL": "健康养生体重管理生活习惯健康指标",
    }
    query = query_map.get(advice_type.upper(), "健康建议")

    results = vs.similarity_search(query, k=top_k)

    if not results:
        return "（暂无相关知识库内容，请基于通用健康常识给出建议）"

    ctx = "\n".join(f"- {r.page_content.strip()}" for r in results)
    return ctx


def _create_sample_knowledge():
    """首次运行时写入示例健康知识文档"""
    samples = {
        "diet_guide.md": """
# 减脂饮食指南

## 核心原则
- 热量赤字：每日摄入 < 消耗，才能减少体脂
- 优质蛋白：每公斤体重摄入 1.2~1.6g 蛋白质（鸡胸、鱼、豆类）
- 复合碳水：优先选燕麦、红薯、糙米、全麦面包
- 蔬菜不限量：西兰花、菠菜、生菜等低卡高纤
- 健康脂肪：坚果、牛油果、橄榄油适量

## 三餐分配建议（减脂版）
- 早餐（30%）：蛋白质 + 复合碳水（例：2个鸡蛋 + 燕麦）
- 午餐（40%）：主食 + 蛋白 + 大量蔬菜（例：糙米饭 + 鸡胸 + 西兰花）
- 晚餐（30%）：轻量碳水 + 蛋白（例：红薯 + 三文鱼）
- 加餐：坚果或酸奶（控制在 150kcal 以内）

## 常见误区
1. 不吃晚饭 → 基础代谢下降，易反弹
2. 只吃水果 → 果糖摄入过多，热量超标
3. 完全断碳水 → 情绪低落，注意力下降
""",
        "exercise_guide.md": """
# 运动计划指南

## 有氧运动建议
- 频率：每周 3~5 次，每次 30~60 分钟
- 强度：保持在最大心率的 60%~75%（220-年龄）
- 推荐：跑步、骑行、游泳、跳绳、椭圆机
- 燃脂心率区间计算：例：30岁 → (220-30)×0.6=114 ~ (220-30)×0.75=143 bpm

## 无氧/力量训练
- 频率：每周 2~3 次，每次 45~60 分钟
- 重点肌群：深蹲（腿）、硬拉（背腿）、卧推（胸）、引体（背）
- 每次训练覆盖 2~3 个大肌群
- 组间休息：30~90 秒（减脂期偏短）

## 运动顺序
热身（5min）→ 无氧力量（45min）→ 有氧（20~30min）→ 拉伸（5min）

## 注意事项
- 运动后补充蛋白质：30g 左右（乳清蛋白或鸡蛋）
- 避免过度训练：同一肌群至少休息 48 小时
- 体重基数大者：优先骑车/游泳，减少膝盖压力
""",
        "general_health.md": """
# 综合健康知识

## BMI 参考
- 计算公式：体重(kg) ÷ 身高(m)²
- 过轻：< 18.5 | 正常：18.5~24 | 超重：24~28 | 肥胖：> 28

## 每日建议热量（成人轻体力）
- 男性：2000~2400 kcal
- 女性：1600~2000 kcal
- 减脂期：在此基础上减少 300~500 kcal

## 睡眠建议
- 成年人每日 7~9 小时
- 睡前 1 小时避免使用手机
- 固定起床时间（包括周末）

## 饮水建议
- 每日 1500~2000 ml（体重×30~35ml）
- 运动后额外补充：每丢失 1kg 体重补 1.5L 水
- 餐前喝一杯水有助于控制食欲

## 常见健康指标
- 空腹血糖：正常 < 100 mg/dL
- 血压：正常 < 120/80 mmHg
- 总胆固醇：正常 < 200 mg/dL
""",
    }

    os.makedirs(KNOWLEDGE_DIR, exist_ok=True)
    for filename, content in samples.items():
        path = os.path.join(KNOWLEDGE_DIR, filename)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content.strip())
        print(f"[RAG] 已写入示例知识文件: {path}")
