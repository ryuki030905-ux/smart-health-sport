<template>
  <div class="charts-page">
    <div class="chart-controls">
      <div class="control-group">
        <label>体重趋势天数</label>
        <el-select v-model="weightDays" size="small" style="width:100px" @change="loadWeight">
          <el-option label="7天"  :value="7"/>
          <el-option label="30天" :value="30"/>
          <el-option label="90天" :value="90"/>
        </el-select>
      </div>
      <div class="control-group">
        <label>热量收支月份</label>
        <el-date-picker v-model="calMonth" type="month" value-format="YYYY-MM" size="small" style="width:130px" @change="loadCalorie"/>
      </div>
      <div class="control-group">
        <label>周运动起始日</label>
        <el-date-picker v-model="weekStart" type="date" value-format="YYYY-MM-DD" size="small" style="width:140px" @change="loadWeekly"/>
      </div>
    </div>

    <div class="charts-grid">
      <!-- 体重趋势 -->
      <div class="chart-panel wide">
        <div class="chart-header">
          <h3>体重变化趋势</h3>
          <span class="chart-sub">近 {{ weightDays }} 天</span>
        </div>
        <div ref="weightChartEl" class="chart-box"></div>
        <div class="empty-hint" v-if="!weightData.series.length">暂无数据，请先录入健康档案</div>
      </div>

      <!-- 周运动时长 -->
      <div class="chart-panel">
        <div class="chart-header">
          <h3>本周运动时长</h3>
          <span class="chart-sub">分钟</span>
        </div>
        <div ref="weeklyChartEl" class="chart-box"></div>
      </div>

      <!-- 热量收支 -->
      <div class="chart-panel wide">
        <div class="chart-header">
          <h3>月度热量收支</h3>
          <span class="chart-sub">{{ calMonth }}</span>
        </div>
        <div ref="calorieChartEl" class="chart-box tall"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 图表页这边的逻辑
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import * as echarts from 'echarts'
import { statsApi } from '@/api'

const weightChartEl  = ref(null)
const weeklyChartEl  = ref(null)
const calorieChartEl = ref(null)

let weightChart, weeklyChart, calorieChart

const weightDays = ref(30)
const calMonth   = ref(new Date().toISOString().slice(0,7))
const weekStart  = ref(getMonday())
const weightData = reactive({ series: [], xAxis: [] })

// 取一下本周一的日期
function getMonday() {
  const d = new Date()
  const day = d.getDay() || 7
  d.setDate(d.getDate() - day + 1)
  return d.toISOString().slice(0,10)
}

const baseOpts = {
  backgroundColor: 'transparent',
  grid: { left: 48, right: 20, top: 20, bottom: 40 },
  tooltip: { trigger: 'axis', backgroundColor: '#0d1829', borderColor: 'rgba(255,255,255,0.1)', textStyle: { color: '#f0fdf4', fontSize: 12 } },
  xAxis: { type: 'category', axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }, axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 11 }, splitLine: { show: false } },
  yAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 11 }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } } },
}

async function loadWeight() {
  try {
    const res = await statsApi.weightTrend(weightDays.value)
    const { xAxis, series } = res.data
    weightData.xAxis = xAxis; weightData.series = series
    weightChart?.setOption({
      ...baseOpts,
      xAxis: { ...baseOpts.xAxis, data: xAxis },
      series: [{
        type: 'line', data: series, smooth: true, symbol: 'circle', symbolSize: 6,
        lineStyle: { color: '#4ade80', width: 2.5 },
        itemStyle: { color: '#4ade80', borderColor: '#0d1829', borderWidth: 2 },
        areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1, [{ offset:0, color:'rgba(74,222,128,0.2)'}, { offset:1, color:'rgba(74,222,128,0)'}]) },
      }]
    })
  } catch {}
}

async function loadWeekly() {
  try {
    const res = await statsApi.weeklyExercise(weekStart.value)
    const { xAxis, series } = res.data
    weeklyChart?.setOption({
      ...baseOpts,
      xAxis: { ...baseOpts.xAxis, data: xAxis },
      series: [{
        type: 'bar', data: series, barMaxWidth: 40,
        itemStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1, [{ offset:0, color:'#38bdf8'}, { offset:1, color:'rgba(56,189,248,0.3)'}]), borderRadius: [4,4,0,0] },
      }]
    })
  } catch {}
}

async function loadCalorie() {
  try {
    const [y, m] = calMonth.value.split('-')
    const res = await statsApi.calorieBalance(Number(y), Number(m))
    const { xAxis, intake, burned, balance } = res.data
    calorieChart?.setOption({
      ...baseOpts,
      grid: { left: 56, right: 20, top: 20, bottom: 40 },
      legend: { data:['摄入','消耗','热量缺口'], textStyle:{ color:'rgba(255,255,255,0.5)', fontSize:11 }, right:0, top:0 },
      tooltip: { ...baseOpts.tooltip, trigger: 'axis' },
      xAxis: { ...baseOpts.xAxis, data: xAxis },
      series: [
        { name:'摄入',    type:'bar', data: intake,  stack:'cal', barMaxWidth:20, itemStyle:{ color:'rgba(56,189,248,0.7)', borderRadius:[2,2,0,0] } },
        { name:'消耗',    type:'bar', data: burned,  stack:'cal', barMaxWidth:20, itemStyle:{ color:'rgba(74,222,128,0.7)', borderRadius:[2,2,0,0] } },
        { name:'热量缺口',type:'line',data: balance, smooth:true, lineStyle:{ color:'#fbbf24', width:2 }, itemStyle:{ color:'#fbbf24' }, yAxisIndex:0 },
      ]
    })
  } catch {}
}

onMounted(() => {
  weightChart  = echarts.init(weightChartEl.value)
  weeklyChart  = echarts.init(weeklyChartEl.value)
  calorieChart = echarts.init(calorieChartEl.value)
  loadWeight(); loadWeekly(); loadCalorie()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  weightChart?.dispose(); weeklyChart?.dispose(); calorieChart?.dispose()
})
// 窗口大小变了以后，让图表也跟着重算一下
function handleResize() { weightChart?.resize(); weeklyChart?.resize(); calorieChart?.resize() }
</script>

<style scoped>
.charts-page { display: flex; flex-direction: column; gap: 24px; }
.chart-controls { display: flex; gap: 24px; align-items: center; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 16px 24px; }
.control-group { display: flex; align-items: center; gap: 10px; }
.control-group label { font-size: 13px; color: var(--text-secondary); white-space: nowrap; }
.charts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.chart-panel { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 24px; }
.chart-panel.wide { grid-column: 1 / -1; }
.chart-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.chart-header h3 { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.chart-sub { font-size: 12px; color: var(--text-muted); }
.chart-box { height: 240px; }
.chart-box.tall { height: 300px; }
.empty-hint { font-size: 13px; color: var(--text-muted); text-align: center; padding: 20px 0; }
</style>
