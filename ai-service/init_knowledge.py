#!/usr/bin/env python3
"""
init_knowledge.py
一次性脚本：初始化知识库向量索引（首次运行前执行一次即可）

使用方法：
    python init_knowledge.py
"""
import os
import sys

# 把当前目录加入 path，方便导入 rag 模块
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from rag import init_vectorstore, KNOWLEDGE_DIR

if __name__ == "__main__":
    print("=" * 50)
    print("开始初始化健康知识向量库 …")
    print(f"知识目录: {KNOWLEDGE_DIR}")
    print("=" * 50)

    vs = init_vectorstore()
    count = vs._collection.count()
    print()
    print(f"✅ 初始化完成！向量库共 {count} 条记录")
    print(f"向量库路径: {os.path.join(os.path.dirname(__file__), '.vectorstore')}")
    print()
    print("启动 AI 服务：")
    print("    cd ai-service && python -m uvicorn main:app --reload --port 8000")
