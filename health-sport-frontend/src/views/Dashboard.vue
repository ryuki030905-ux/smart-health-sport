<template>
  <div class="dashboard">
    <!-- 顶部数据卡片 -->
    <div class="cards-grid">
      <div class="stat-card" v-for="card in statCards" :key="card.label">
        <div class="card-icon" :style="{ background: card.bg }">
          <span v-html="card.icon"></span>
        </div>
        <div class="card-body">
          <p class="card-value">{{ card.value }}</p>
          <p class="card-label">{{ card.label }}</p>
        </div>
        <div class="card-badge" :class="card.status">{{ card.badge }}</div>
      </div>

      <!-- AI 助手快捷入口 -->
      <router-link to="/ai" class="ai-shortcut-card">
        <div class="ai-icon-wrap">
          <svg viewBox="0 0 24 24" fill="none" width="24" height="24">
            <circle cx="12" cy="12" r="10" stroke="#4ade80" stroke-width="1.5" opacity="0.3"/>
            <path d="M12 6a1 1 0 011 1v4l3 3a1 1 0 01-1.7.7L12 13V7a1 1 0 011-1z" fill="#4ade80" opacity="0.7"/>
            <path d="M8 14a4 4 0 008 0" stroke="#4ade80" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="ai-card-body">
          <p class="ai-card-title">AI 健康助手</p>
          <p class="ai-card-desc">个性化饮食 / 运动建议</p>
        </div>
        <div class="ai-card-arrow">→</div>
      </router-link>
    </div>

    <!-- 中间两块内容 -->
    <div class="mid-row">
      <!-- 最新健康数据 -->
      <div class="panel health-panel">
        <div class="panel-header">
          <h3>最新健康档案</h3>
          <router-link to="/health" class="panel-link">查看全部 →</router-link>
        </div>
        <div v-if="latest" class="health-grid">
          <div class="health-item" v-for="h in healthItems" :key="h.label">
            <span class="h-label">{{ h.label }}</span>
            <span class="h-value" :style="{ color: h.color }">{{ h.value }}</span>
          </div>
        </div>
        <div v-else class="empty-hint">暂无健康记录，<router-link to="/health">去录入</router-link></div>
      </div>

      <!-- 今日热量情况 -->
      <div class="panel calorie-panel">
        <div class="panel-header">
          <h3>今日热量</h3>
          <span class="today-date">{{ todayStr }}</span>
        </div>
        <div class="calorie-ring-wrap">
          <svg class="ring-svg" viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="50" fill="none" stroke="rgba(255,255,255,0.06)" stroke-width="10"/>
            <circle cx="60" cy="60" r="50" fill="none" :stroke="ringColor" stroke-width="10"
              stroke-linecap="round" stroke-dasharray="314"
              :stroke-dashoffset="ringOffset"
              transform="rotate(-90 60 60)" style="transition: stroke-dashoffset 1s ease"/>
            <text x="60" y="54" text-anchor="middle" fill="#f0fdf4" font-size="16" font-weight="700">{{ todayBalance }}</text>
            <text x="60" y="70" text-anchor="middle" fill="rgba(255,255,255,0.4)" font-size="9">kcal 缺口</text>
          </svg>
          <div class="calorie-legend">
            <div class="legend-item">
              <span class="dot" style="background:#38bdf8"></span>
              <span>摄入 {{ todayIntake }} kcal</span>
            </div>
            <div class="legend-item">
              <span class="dot" style="background:#4ade80"></span>
              <span>消耗 {{ todayBurned }} kcal</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 运动计划进度 -->
    <div class="panel">
      <div class="panel-header">
        <h3>运动计划进度</h3>
        <router-link to="/exercise" class="panel-link">管理计划 →</router-link>
      </div>
      <div v-if="plans.length" class="plan-list">
        <div class="plan-item" v-for="p in plans" :key="p.id">
          <div class="plan-top">
            <span class="plan-name">{{ p.planName }}</span>
            <span class="plan-pct">{{ Math.round(p.progressRate || 0) }}%</span>
          </div>
          <div class="plan-bar">
            <div class="plan-fill" :style="{ width: Math.min(p.progressRate||0, 100) + '%', background: barColor(p.progressRate) }"></div>
          </div>
          <div class="plan-sub">{{ p.actualDuration }}min / {{ p.targetDuration }}min · {{ p.actualTimes }}/{{ p.targetTimes }} 次</div>
        </div>
      </div>
      <div v-else class="empty-hint">暂无运动计划，<router-link to="/exercise">去创建</router-link></div>
    </div>
  </div>
</template>

<script setup>
// 首页的数据展示逻辑
import { ref, computed, onMounted } from 'vue'
import { healthApi, exerciseApi, statsApi } from '@/api'

const latest   = ref(null)
const plans    = ref([])
const todayIntake  = ref(0)
const todayBurned  = ref(0)

const todayStr = computed(() => {
  const d = new Date()
  return `${d.getMonth()+1}月${d.getDate()}日`
})
const todayBalance = computed(() => todayIntake.value - todayBurned.value)
const ringOffset   = computed(() => {
  const max = 3000
  const pct = Math.min(Math.abs(todayBalance.value) / max, 1)
  return 314 - pct * 314
})
const ringColor = computed(() => todayBalance.value > 0 ? '#f87171' : '#4ade80')

const statusMap = { UNDERWEIGHT:'偏瘦', NORMAL:'正常', OVERWEIGHT:'超重', OBESE:'肥胖' }
const statusColor = { UNDERWEIGHT:'#38bdf8', NORMAL:'#4ade80', OVERWEIGHT:'#fbbf24', OBESE:'#f87171' }

const healthItems = computed(() => {
  if (!latest.value) return []
  const l = latest.value
  return [
    { label: 'BMI',  value: l.bmi ?? '--',    color: statusColor[l.healthStatus] || '#4ade80' },
    { label: '体重', value: l.weight ? l.weight+'kg' : '--', color: '#f0fdf4' },
    { label: '体脂率',value: l.bodyFat ? l.bodyFat+'%' : '--', color: '#a78bfa' },
    { label: '健康状态', value: statusMap[l.healthStatus] || '--', color: statusColor[l.healthStatus] || '#4ade80' },
    { label: '血压', value: l.systolicBp ? `${l.systolicBp}/${l.diastolicBp}` : '--', color: '#38bdf8' },
    { label: '心率', value: l.heartRate ? l.heartRate+'次/分' : '--', color: '#fbbf24' },
  ]
})

const statCards = computed(() => [
  { label: '当前体重', value: latest.value?.weight ? latest.value.weight+'kg' : '--', badge: latest.value?.healthStatus ? statusMap[latest.value.healthStatus] : '--', status: latest.value?.healthStatus?.toLowerCase() || '', bg: 'rgba(74,222,128,0.1)', icon: `<svg viewBox="0 0 20 20" fill="none" width="22" height="22"><circle cx="10" cy="10" r="8" stroke="#4ade80" stroke-width="1.5"/><path d="M10 6v4l3 2" stroke="#4ade80" stroke-width="1.5" stroke-linecap="round"/></svg>` },
  { label: '今日摄入', value: todayIntake.value+'kcal', badge: '饮食', status: '', bg: 'rgba(56,189,248,0.1)', icon: `<svg viewBox="0 0 20 20" fill="none" width="22" height="22"><path d="M6 2v6a4 4 0 008 0V2" stroke="#38bdf8" stroke-width="1.5" stroke-linecap="round"/><path d="M10 12v6" stroke="#38bdf8" stroke-width="1.5" stroke-linecap="round"/></svg>` },
  { label: '今日消耗', value: todayBurned.value+'kcal', badge: '运动', status: '', bg: 'rgba(167,139,250,0.1)', icon: `<svg viewBox="0 0 20 20" fill="none" width="22" height="22"><circle cx="10" cy="10" r="8" stroke="#a78bfa" stroke-width="1.5"/><path d="M6 10l2.5 2.5L14 7" stroke="#a78bfa" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>` },
  { label: '进行中计划', value: plans.value.filter(p=>p.status==='ONGOING').length+'个', badge: '计划', status: '', bg: 'rgba(251,191,36,0.1)', icon: `<svg viewBox="0 0 20 20" fill="none" width="22" height="22"><rect x="3" y="4" width="14" height="13" rx="2" stroke="#fbbf24" stroke-width="1.5"/><path d="M7 2v4M13 2v4M3 9h14" stroke="#fbbf24" stroke-width="1.5" stroke-linecap="round"/></svg>` },
])

// 根据进度换个颜色看着更直观点
function barColor(pct) {
  if (!pct) return '#4ade80'
  if (pct >= 100) return '#4ade80'
  if (pct >= 60)  return '#fbbf24'
  return '#f87171'
}

onMounted(async () => {
  try { latest.value = (await healthApi.latest()).data } catch {}
  try { plans.value  = (await exerciseApi.planList()).data || [] } catch {}
  try {
    const d = new Date()
    const res = await statsApi.calorieBalance(d.getFullYear(), d.getMonth()+1)
    const dayStr = `${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    const idx = res.data?.xAxis?.indexOf(dayStr) ?? -1
    if (idx >= 0) {
      todayIntake.value = res.data.intake[idx] || 0
      todayBurned.value = res.data.burned[idx] || 0
    }
  } catch {}
})
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 24px; }

.cards-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
.stat-card {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius-lg); padding: 20px;
  display: flex; align-items: center; gap: 16px;
  transition: border-color 0.2s;
  position: relative; overflow: hidden;
}
.stat-card:hover { border-color: var(--border-green); }
.card-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.card-body { flex: 1; }
.card-value { font-size: 22px; font-weight: 700; color: var(--text-primary); }
.card-label { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
.card-badge { position: absolute; top: 12px; right: 12px; font-size: 10px; padding: 2px 8px; border-radius: 10px; background: var(--bg-card); border: 1px solid var(--border); color: var(--text-muted); }
.card-badge.normal { background: rgba(74,222,128,0.1); border-color: var(--border-green); color: var(--green); }
.card-badge.overweight { background: rgba(251,191,36,0.1); border-color: rgba(251,191,36,0.3); color: var(--yellow); }
.card-badge.obese { background: rgba(248,113,113,0.1); border-color: rgba(248,113,113,0.3); color: var(--red); }

.mid-row { display: grid; grid-template-columns: 1fr 280px; gap: 24px; }
.panel { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 24px; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.panel-header h3 { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.panel-link { font-size: 12px; color: var(--green); text-decoration: none; }
.panel-link:hover { opacity: 0.8; }
.today-date { font-size: 12px; color: var(--text-muted); }

.health-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.health-item { display: flex; flex-direction: column; gap: 4px; padding: 12px; background: rgba(255,255,255,0.03); border-radius: var(--radius-sm); border: 1px solid var(--border); }
.h-label { font-size: 11px; color: var(--text-muted); }
.h-value { font-size: 18px; font-weight: 700; }

.calorie-ring-wrap { display: flex; flex-direction: column; align-items: center; gap: 16px; }
.ring-svg { width: 140px; height: 140px; }
.calorie-legend { display: flex; flex-direction: column; gap: 8px; width: 100%; }
.legend-item { display: flex; align-items: center; gap: 8px; font-size: 12px; color: var(--text-secondary); }
.dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }

.plan-list { display: flex; flex-direction: column; gap: 16px; }
.plan-item { display: flex; flex-direction: column; gap: 8px; }
.plan-top { display: flex; justify-content: space-between; align-items: center; }
.plan-name { font-size: 14px; color: var(--text-primary); font-weight: 500; }
.plan-pct { font-size: 13px; font-weight: 700; color: var(--green); }
.plan-bar { height: 6px; background: rgba(255,255,255,0.06); border-radius: 3px; overflow: hidden; }
.plan-fill { height: 100%; border-radius: 3px; transition: width 1s ease; }
.plan-sub { font-size: 11px; color: var(--text-muted); }

.empty-hint { font-size: 13px; color: var(--text-muted); padding: 20px 0; }
.empty-hint a { color: var(--green); text-decoration: none; }

/* AI 快捷入口卡片 */
.ai-shortcut-card {
  background: rgba(74,222,128,0.06);
  border: 1px solid rgba(74,222,128,0.2);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
}
.ai-shortcut-card:hover {
  border-color: rgba(74,222,128,0.5);
  background: rgba(74,222,128,0.1);
  transform: translateY(-1px);
}
.ai-icon-wrap {
  width: 48px; height: 48px;
  border-radius: 12px;
  background: rgba(74,222,128,0.1);
  border: 1px solid rgba(74,222,128,0.2);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.ai-card-body { flex: 1; }
.ai-card-title { font-size: 14px; font-weight: 700; color: #4ade80; }
.ai-card-desc { font-size: 11px; color: rgba(74,222,128,0.6); margin-top: 3px; }
.ai-card-arrow { font-size: 18px; color: rgba(74,222,128,0.4); }
</style>
