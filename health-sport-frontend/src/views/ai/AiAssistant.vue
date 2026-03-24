<template>
  <div class="ai-page">

    <!-- 顶部提示条：每日剩余次数 -->
    <div class="quota-bar">
      <div class="quota-left">
        <svg viewBox="0 0 20 20" fill="none" width="16" height="16">
          <path d="M10 2a1 1 0 011 1v3.586l2.707 2.707A1 1 0 0015 9V8a1 1 0 10-2 0v1H8V8a1 1 0 10-2 0v1H3a1 1 0 00-.707 1.707L5 13.414V15a1 1 0 001 1h8a1 1 0 001-1v-1.586l2.707-2.707A1 1 0 0017 11V9a1 1 0 10-2 0v2H8V9a1 1 0 10-2 0v2H5a1 1 0 00-1 1v3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span v-if="remaining === -1">今日剩余 <strong>无限</strong> 次 AI 建议</span>
        <span v-else>今日剩余 <strong>{{ remaining }}</strong> 次 AI 建议</span>
      </div>
      <div class="quota-dots">
        <template v-if="remaining !== -1">
          <span v-for="i in 3" :key="i" class="dot" :class="{ used: i > remaining }"></span>
        </template>
      </div>
    </div>

    <!-- 主体：左侧提问区 + 右侧建议展示 -->
    <div class="main-row">

      <!-- 左侧：提问表单 -->
      <div class="ask-panel">
        <h3 class="panel-title">✦ 获取个性化建议</h3>

        <!-- 建议类型选择 -->
        <div class="form-group">
          <label class="form-label">建议类型</label>
          <div class="type-chips">
            <button
              v-for="t in typeOptions"
              :key="t.value"
              class="chip"
              :class="{ active: form.adviceType === t.value }"
              @click="form.adviceType = t.value"
            >
              <span v-html="t.icon"></span>
              {{ t.label }}
            </button>
          </div>
        </div>

        <!-- 今日数据预览（自动填入请求） -->
        <div class="today-stats" v-if="todayData">
          <p class="form-label">今日数据（将一并发给 AI）</p>
          <div class="stat-row">
            <div class="mini-stat">
              <span class="mini-val">{{ todayData.weight || '--' }}<em>kg</em></span>
              <span class="mini-label">体重</span>
            </div>
            <div class="mini-stat">
              <span class="mini-val">{{ todayData.exerciseMin || '--' }}<em>min</em></span>
              <span class="mini-label">运动时长</span>
            </div>
            <div class="mini-stat">
              <span class="mini-val">{{ todayData.calIntake || '--' }}<em>kcal</em></span>
              <span class="mini-label">摄入</span>
            </div>
            <div class="mini-stat">
              <span class="mini-val">{{ todayData.calBurned || '--' }}<em>kcal</em></span>
              <span class="mini-label">消耗</span>
            </div>
          </div>
        </div>

        <!-- 目标选择 -->
        <div class="form-group">
          <label class="form-label">我的目标</label>
          <div class="type-chips">
            <button
              v-for="g in goalOptions"
              :key="g"
              class="chip"
              :class="{ active: form.goal === g }"
              @click="form.goal = g"
            >{{ g }}</button>
          </div>
        </div>

        <!-- 补充提问 -->
        <div class="form-group">
          <label class="form-label">补充说明 <span class="opt">(可选)</span></label>
          <textarea
            class="textarea"
            v-model="form.extraQuestion"
            placeholder="比如：最近膝盖有点不舒服、我是素食主义者、最近加班熬夜比较多…"
            rows="3"
          ></textarea>
        </div>

        <!-- 提交按钮 -->
        <button class="submit-btn" :disabled="loading || (remaining !== -1 && remaining <= 0)" @click="submit">
          <span v-if="loading" class="spinner"></span>
          <svg v-else viewBox="0 0 20 20" fill="none" width="16" height="16">
            <path d="M3 10l5 5 9-9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ loading ? 'AI 分析中…' : (remaining !== -1 && remaining <= 0) ? '今日次数已用完' : '获取建议' }}
        </button>

        <p v-if="remaining !== -1 && remaining <= 0" class="quota-tip">明天还有 3 次免费机会哦 😊</p>
      </div>

      <!-- 右侧：AI 建议展示 -->
      <div class="result-panel">
        <!-- 空状态 -->
        <div v-if="!result && !loading" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 64 64" fill="none" width="64" height="64">
              <circle cx="32" cy="32" r="30" stroke="rgba(74,222,128,0.2)" stroke-width="2"/>
              <path d="M20 32 Q32 16 44 32 Q32 48 20 32Z" stroke="rgba(74,222,128,0.4)" stroke-width="1.5" fill="rgba(74,222,128,0.05)"/>
              <circle cx="32" cy="32" r="5" fill="rgba(74,222,128,0.6)"/>
              <circle cx="32" cy="14" r="2.5" fill="rgba(74,222,128,0.3)"/>
              <circle cx="32" cy="50" r="2.5" fill="rgba(74,222,128,0.3)"/>
              <circle cx="14" cy="32" r="2.5" fill="rgba(74,222,128,0.3)"/>
              <circle cx="50" cy="32" r="2.5" fill="rgba(74,222,128,0.3)"/>
            </svg>
          </div>
          <p class="empty-title">AI 健康助手</p>
          <p class="empty-desc">左边选择建议类型，AI 将根据你的今日数据给出个性化饮食/运动建议</p>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="thinking-dots">
            <span></span><span></span><span></span>
          </div>
          <p>AI 正在分析你的健康数据…</p>
          <p class="loading-sub">这通常需要 2~3 分钟</p>
        </div>

        <!-- AI 返回结果 -->
        <div v-if="result && !loading" class="result-content">
          <div class="result-header">
            <div class="result-type-badge" :class="result.adviceType.toLowerCase()">
              {{ typeLabel(result.adviceType) }}
            </div>
            <span class="result-time">{{ formatTime(result.generatedAt) }}</span>
          </div>

          <div class="result-summary" v-if="result.summary">
            <svg viewBox="0 0 16 16" fill="none" width="14" height="14">
              <path d="M2 8h12M8 2l6 6-6 6" stroke="#4ade80" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            {{ result.summary }}
          </div>

          <div class="result-body">
            <p v-for="(para, i) in paragraphs" :key="i" :class="{ highlight: isHighlight(para) }">
              {{ para }}
            </p>
          </div>

          <div class="result-footer">
            <span v-if="result.remainingDaily === -1" class="remain-info">今日剩余无限次</span>
            <span v-else class="remain-info">今日剩余 {{ result.remainingDaily }} 次</span>
            <button class="copy-btn" @click="copyResult">复制建议</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { aiApi, healthApi, exerciseApi, dietApi } from '@/api'
import { ElMessage } from 'element-plus'
import { normalizeAiAdviceResponse } from './ai-response.mjs'

// ── 剩余次数 ──
const remaining = ref(3)

// ── 今日上下文数据 ──
const todayData = ref(null)

// ── 建议结果 ──
const result = ref(null)
const loading = ref(false)

// ── 表单 ──
const form = reactive({
  adviceType: 'GENERAL',
  goal: '保持',
  extraQuestion: '',
})

const typeOptions = [
  {
    value: 'DIET',
    label: '饮食建议',
    icon: `<svg viewBox="0 0 16 16" fill="none" width="14" height="14"><path d="M5 2v4a3 3 0 006 0V2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><path d="M8 9v5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>`,
  },
  {
    value: 'EXERCISE',
    label: '运动建议',
    icon: `<svg viewBox="0 0 16 16" fill="none" width="14" height="14"><circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/><path d="M5 8l2 2 4-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  },
  {
    value: 'GENERAL',
    label: '综合建议',
    icon: `<svg viewBox="0 0 16 16" fill="none" width="14" height="14"><path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>`,
  },
]

const goalOptions = ['减脂', '增肌', '保持', '增重']

// ── 挂载时拉取次数和今日数据 ──
onMounted(async () => {
  await fetchRemain()
  await fetchTodayData()
})

async function fetchRemain() {
  try {
    const res = await aiApi.getRemain()
    remaining.value = res.data?.remaining ?? 3
  } catch {
    remaining.value = 3
  }
}

async function fetchTodayData() {
  try {
    // 并行拉取：最新体重 + 今日运动 + 今日饮食
    const [healthRes, exerciseRes, dietRes] = await Promise.allSettled([
      healthApi.latest(),
      exerciseApi.recordList({ date: todayStr() }),
      dietApi.recordList({ date: todayStr() }),
    ])

    const weight = healthRes.status === 'fulfilled' ? healthRes.value?.data?.weight ?? null : null

    let exerciseMin = 0, calBurned = 0
    if (exerciseRes.status === 'fulfilled') {
      const records = exerciseRes.value?.data || []
      exerciseMin = records.reduce((s, r) => s + (r.durationMin || 0), 0)
      calBurned = records.reduce((s, r) => s + parseFloat(r.caloriesBurned || 0), 0)
    }

    let calIntake = 0
    if (dietRes.status === 'fulfilled') {
      const records = dietRes.value?.data || []
      calIntake = records.reduce((s, r) => s + parseFloat(r.calories || 0), 0)
    }

    todayData.value = { weight, exerciseMin, calIntake: Math.round(calIntake), calBurned: Math.round(calBurned) }
  } catch (e) {
    todayData.value = null
  }
}

function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

// ── 提交 ──
async function submit() {
  if ((remaining.value !== -1 && remaining.value <= 0) || loading.value) return

  loading.value = true
  result.value = null

  try {
    const data = {
      adviceType: form.adviceType,
      goal: form.goal,
      extraQuestion: form.extraQuestion || null,
      weight: todayData.value?.weight || null,
      exerciseMinutesToday: todayData.value?.exerciseMin || null,
      caloriesIntakeToday: todayData.value?.calIntake || null,
      caloriesBurnedToday: todayData.value?.calBurned || null,
    }

    const res = await aiApi.getAdvice(data)
    const normalized = normalizeAiAdviceResponse(res)

    if (!normalized) {
      throw new Error('AI 响应结构无法识别')
    }

    result.value = normalized
    remaining.value = normalized.remainingDaily ?? (remaining.value - 1)
    if (normalized.debug?.error) {
      console.warn('AI debug info:', normalized.debug)
    }
  } catch (e) {
    if (e?.message === 'AI 响应结构无法识别') {
      ElMessage.error('AI 响应格式异常，请检查浏览器 Network 返回结构')
    }
  } finally {
    loading.value = false
  }
}

// ── 显示辅助 ──
function typeLabel(t) {
  return { DIET: '🍽 饮食', EXERCISE: '🏃 运动', GENERAL: '💡 综合' }[t] || '💡 综合'
}

function formatTime(isoStr) {
  if (!isoStr) return ''
  const d = new Date(isoStr)
  return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
}

const paragraphs = computed(() => {
  if (!result.value?.content) return []
  return result.value.content.split('\n').filter(l => l.trim())
})

function isHighlight(para) {
  return para.includes('建议') || para.includes('推荐') || para.includes('★')
}

function copyResult() {
  if (!result.value?.content) return
  navigator.clipboard.writeText(result.value.content).then(() => {
    ElMessage.success('建议内容已复制到剪贴板')
  })
}
</script>

<style scoped>
.ai-page { display: flex; flex-direction: column; gap: 20px; }

/* ── 配额条 ── */
.quota-bar {
  display: flex; align-items: center; justify-content: space-between;
  background: rgba(74,222,128,0.06); border: 1px solid rgba(74,222,128,0.15);
  border-radius: 12px; padding: 12px 20px;
}
.quota-left {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: rgba(74,222,128,0.7);
}
.quota-left strong { color: #4ade80; font-weight: 700; }
.quota-dots { display: flex; gap: 6px; }
.dot { width: 10px; height: 10px; border-radius: 50%; background: rgba(74,222,128,0.3); border: 1px solid rgba(74,222,128,0.5); }
.dot.used { background: rgba(255,255,255,0.06); border-color: var(--border); }

/* ── 主行 ── */
.main-row { display: grid; grid-template-columns: 380px 1fr; gap: 20px; align-items: start; }

/* ── 提问面板 ── */
.ask-panel {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius-lg); padding: 24px;
  display: flex; flex-direction: column; gap: 20px;
}
.panel-title { font-size: 15px; font-weight: 600; color: var(--text-primary); }

.form-group { display: flex; flex-direction: column; gap: 10px; }
.form-label { font-size: 12px; color: var(--text-secondary); font-weight: 500; }
.opt { color: var(--text-muted); font-weight: 400; }

.type-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  display: flex; align-items: center; gap: 5px;
  padding: 7px 14px; border-radius: 20px;
  border: 1px solid var(--border);
  background: none; color: var(--text-secondary);
  font-size: 13px; cursor: pointer; transition: all 0.18s;
}
.chip:hover { border-color: rgba(74,222,128,0.3); color: var(--green); }
.chip.active { background: rgba(74,222,128,0.1); border-color: rgba(74,222,128,0.4); color: var(--green); }

/* 今日数据 */
.today-stats { display: flex; flex-direction: column; gap: 10px; }
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.mini-stat {
  background: rgba(255,255,255,0.03); border: 1px solid var(--border);
  border-radius: 8px; padding: 8px 6px;
  display: flex; flex-direction: column; align-items: center; gap: 2px;
}
.mini-val { font-size: 14px; font-weight: 700; color: var(--text-primary); }
.mini-val em { font-size: 10px; font-weight: 400; color: var(--text-muted); }
.mini-label { font-size: 10px; color: var(--text-muted); }

.textarea {
  background: rgba(255,255,255,0.03); border: 1px solid var(--border);
  border-radius: var(--radius-sm); padding: 10px 12px;
  color: var(--text-primary); font-size: 13px; resize: vertical;
  font-family: inherit; outline: none; transition: border-color 0.2s;
}
.textarea:focus { border-color: rgba(74,222,128,0.3); }
.textarea::placeholder { color: var(--text-muted); }

.submit-btn {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  width: 100%; padding: 12px;
  background: var(--green); color: #0a1a0f;
  border: none; border-radius: var(--radius-sm);
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all 0.2s;
}
.submit-btn:hover:not(:disabled) { background: #22c55e; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.spinner {
  width: 14px; height: 14px; border-radius: 50%;
  border: 2px solid rgba(0,0,0,0.2); border-top-color: #0a1a0f;
  animation: spin 0.7s linear infinite; display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }

.quota-tip { font-size: 12px; color: var(--text-muted); text-align: center; }

/* ── 结果面板 ── */
.result-panel {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius-lg); min-height: 400px;
  overflow: hidden;
}

.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 400px; gap: 16px; text-align: center; padding: 40px;
}
.empty-icon { opacity: 0.6; }
.empty-title { font-size: 18px; font-weight: 700; color: var(--text-primary); }
.empty-desc { font-size: 13px; color: var(--text-muted); max-width: 300px; line-height: 1.6; }

.loading-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 400px; gap: 16px;
}
.thinking-dots { display: flex; gap: 8px; }
.thinking-dots span {
  width: 10px; height: 10px; border-radius: 50%; background: rgba(74,222,128,0.5);
  animation: bounce 1.2s ease-in-out infinite;
}
.thinking-dots span:nth-child(2) { animation-delay: 0.2s; }
.thinking-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}
.loading-state p { font-size: 14px; color: var(--text-secondary); }
.loading-sub { font-size: 12px; color: var(--text-muted); }

.result-content { padding: 24px; display: flex; flex-direction: column; gap: 16px; }
.result-header { display: flex; align-items: center; justify-content: space-between; }
.result-type-badge {
  font-size: 12px; padding: 4px 12px; border-radius: 20px;
  font-weight: 600;
}
.result-type-badge.diet { background: rgba(56,189,248,0.1); border: 1px solid rgba(56,189,248,0.3); color: #38bdf8; }
.result-type-badge.exercise { background: rgba(167,139,250,0.1); border: 1px solid rgba(167,139,250,0.3); color: #a78bfa; }
.result-type-badge.general { background: rgba(74,222,128,0.1); border: 1px solid rgba(74,222,128,0.3); color: #4ade80; }

.result-time { font-size: 11px; color: var(--text-muted); }
.result-summary {
  display: flex; align-items: flex-start; gap: 8px;
  padding: 12px 16px; background: rgba(74,222,128,0.05);
  border: 1px solid rgba(74,222,128,0.15); border-radius: 8px;
  font-size: 13px; color: rgba(74,222,128,0.85); line-height: 1.5;
}
.result-summary svg { flex-shrink: 0; margin-top: 2px; }

.result-body { display: flex; flex-direction: column; gap: 10px; }
.result-body p { font-size: 13.5px; color: var(--text-secondary); line-height: 1.7; }
.result-body p.highlight { color: var(--text-primary); }

.result-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 12px; border-top: 1px solid var(--border);
}
.remain-info { font-size: 12px; color: var(--text-muted); }
.copy-btn {
  font-size: 12px; padding: 5px 14px;
  background: none; border: 1px solid var(--border);
  border-radius: 6px; color: var(--text-secondary); cursor: pointer; transition: all 0.2s;
}
.copy-btn:hover { border-color: rgba(74,222,128,0.3); color: var(--green); }
</style>
