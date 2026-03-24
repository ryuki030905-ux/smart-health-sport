<template>
  <div class="exercise-page">
    <el-tabs v-model="activeTab" class="dark-tabs">
      <!-- 运动打卡 -->
      <el-tab-pane label="运动打卡" name="record">
        <div class="tab-content">
          <div class="form-panel">
            <h3 class="form-title">记录运动</h3>
            <el-form :model="recForm" label-width="80px" size="small">
              <div class="form-grid">
                <el-form-item label="运动类型">
                  <el-select v-model="recForm.exerciseDictId" placeholder="选择运动" style="width:100%">
                    <el-option v-for="d in dictList" :key="d.id" :label="`${d.name}（MET=${d.metValue}）`" :value="d.id"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="日期">
                  <el-date-picker v-model="recForm.exerciseDate" type="date" value-format="YYYY-MM-DD" style="width:100%"/>
                </el-form-item>
                <el-form-item label="时长(分钟)">
                  <el-input v-model.number="recForm.durationMin" type="number" placeholder="如：30"/>
                </el-form-item>
                <el-form-item label="强度">
                  <el-select v-model="recForm.intensity" style="width:100%">
                    <el-option label="低强度" value="LOW"/>
                    <el-option label="中等强度" value="MEDIUM"/>
                    <el-option label="高强度" value="HIGH"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="recForm.remark" placeholder="备注一下..."/>
                </el-form-item>
              </div>
              <el-button type="primary" :loading="recSaving" @click="submitRecord" style="width:100%;margin-top:8px">打卡 🏃</el-button>
            </el-form>
          </div>
          <div class="panel">
            <div class="panel-header">
              <h3>打卡记录</h3>
              <el-date-picker v-model="filterDate" type="date" value-format="YYYY-MM-DD" placeholder="按日期筛选" size="small" clearable @change="loadRecords"/>
            </div>
            <el-table :data="records" style="width:100%">
              <el-table-column prop="exerciseDate" label="日期" width="110"/>
              <el-table-column prop="exerciseName" label="运动" width="100"/>
              <el-table-column prop="durationMin"  label="时长" width="80">
                <template #default="{ row }">{{ row.durationMin }} 分钟</template>
              </el-table-column>
              <el-table-column prop="intensity" label="强度" width="90">
                <template #default="{ row }">
                  <el-tag :type="intensityType(row.intensity)" size="small">{{ intensityLabel(row.intensity) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="caloriesBurned" label="消耗" width="100">
                <template #default="{ row }">
                  <span style="color:#4ade80;font-weight:600">{{ row.caloriesBurned }} kcal</span>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip/>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- 运动计划 -->
      <el-tab-pane label="运动计划" name="plan">
        <div class="tab-content">
          <div class="form-panel">
            <h3 class="form-title">创建计划</h3>
            <el-form :model="planForm" label-width="90px" size="small">
              <div class="form-grid">
                <el-form-item label="计划名称">
                  <el-input v-model="planForm.planName" placeholder="如：三月减脂计划"/>
                </el-form-item>
                <el-form-item label="周期类型">
                  <el-select v-model="planForm.planType" style="width:100%">
                    <el-option label="周计划" value="WEEKLY"/>
                    <el-option label="月计划" value="MONTHLY"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="开始日期">
                  <el-date-picker v-model="planForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%"/>
                </el-form-item>
                <el-form-item label="结束日期">
                  <el-date-picker v-model="planForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%"/>
                </el-form-item>
                <el-form-item label="目标时长">
                  <el-input v-model.number="planForm.targetDuration" type="number" placeholder="分钟"/>
                </el-form-item>
                <el-form-item label="目标次数">
                  <el-input v-model.number="planForm.targetTimes" type="number" placeholder="次"/>
                </el-form-item>
              </div>
              <el-button type="primary" :loading="planSaving" @click="submitPlan" style="width:100%;margin-top:8px">创建计划</el-button>
            </el-form>
          </div>
          <div class="plans-grid">
            <div class="plan-card" v-for="p in plans" :key="p.id">
              <div class="plan-top">
                <span class="plan-name">{{ p.planName }}</span>
                <el-tag :type="planTagType(p.status)" size="small">{{ planStatusLabel(p.status) }}</el-tag>
              </div>
              <div class="plan-dates">{{ p.startDate }} ~ {{ p.endDate }} · {{ p.planType === 'WEEKLY' ? '周计划' : '月计划' }}</div>
              <div class="plan-bar-wrap">
                <div class="plan-bar-bg">
                  <div class="plan-bar-fill" :style="{ width: Math.min(p.progressRate||0,100)+'%', background: barColor(p.progressRate) }"></div>
                </div>
                <span class="plan-pct">{{ Math.round(p.progressRate||0) }}%</span>
              </div>
              <div class="plan-stats">
                <span>⏱ {{ p.actualDuration }}/{{ p.targetDuration }} 分钟</span>
                <span>🏃 {{ p.actualTimes }}/{{ p.targetTimes }} 次</span>
              </div>
            </div>
            <div class="empty-hint" v-if="!plans.length">还没有计划，创建一个吧！</div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
// 运动页这边的逻辑
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { exerciseApi } from '@/api'

const activeTab = ref('record')
const dictList  = ref([])
const records   = ref([])
const plans     = ref([])
const filterDate= ref('')
const recSaving = ref(false)
const planSaving= ref(false)

const recForm  = reactive({ exerciseDictId:null, exerciseDate:'', durationMin:null, intensity:'MEDIUM', remark:'' })
const planForm = reactive({ planName:'', planType:'WEEKLY', startDate:'', endDate:'', targetDuration:null, targetTimes:null })

const intensityLabel = i => ({ LOW:'低强度', MEDIUM:'中强度', HIGH:'高强度' }[i] || i)
const intensityType  = i => ({ LOW:'info', MEDIUM:'', HIGH:'danger' }[i] || '')
const planTagType    = s => ({ ONGOING:'success', COMPLETED:'info', ABANDONED:'danger' }[s] || '')
const planStatusLabel= s => ({ ONGOING:'进行中', COMPLETED:'已完成', ABANDONED:'已放弃' }[s] || s)
const barColor = pct => pct >= 100 ? '#4ade80' : pct >= 60 ? '#fbbf24' : '#f87171'

async function loadRecords() {
  try { records.value = (await exerciseApi.recordList(filterDate.value ? { date: filterDate.value } : {})).data || [] } catch {}
}
async function loadPlans() {
  try { plans.value = (await exerciseApi.planList()).data || [] } catch {}
}

async function submitRecord() {
  if (!recForm.exerciseDictId || !recForm.exerciseDate || !recForm.durationMin) { ElMessage.warning('请填写完整信息'); return }
  recSaving.value = true
  try {
    const res = await exerciseApi.addRecord(recForm)
    ElMessage.success(`打卡成功！消耗 ${res.data?.caloriesBurned ?? ''} kcal 🎉`)
    Object.assign(recForm, { exerciseDictId:null, exerciseDate:'', durationMin:null, intensity:'MEDIUM', remark:'' })
    loadRecords()
  } finally { recSaving.value = false }
}

async function submitPlan() {
  if (!planForm.planName || !planForm.startDate || !planForm.endDate) { ElMessage.warning('请填写完整信息'); return }
  planSaving.value = true
  try {
    await exerciseApi.addPlan(planForm); ElMessage.success('计划创建成功！')
    Object.assign(planForm, { planName:'', planType:'WEEKLY', startDate:'', endDate:'', targetDuration:null, targetTimes:null })
    loadPlans()
  } finally { planSaving.value = false }
}

onMounted(() => { exerciseApi.dictList().then(r => dictList.value = r.data || []); loadRecords(); loadPlans() })
</script>

<style scoped>
.exercise-page :deep(.el-tabs__header) { border-bottom: 1px solid var(--border); margin-bottom: 20px; }
.exercise-page :deep(.el-tabs__item) { color: var(--text-secondary); }
.exercise-page :deep(.el-tabs__item.is-active) { color: var(--green); }
.exercise-page :deep(.el-tabs__active-bar) { background: var(--green); }
.tab-content { display: grid; grid-template-columns: 340px 1fr; gap: 24px; align-items: start; }
.form-panel { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 24px; }
.form-title { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 20px; }
.form-grid { display: flex; flex-direction: column; gap: 4px; }
.panel { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 24px; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.panel-header h3 { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.plans-grid { display: flex; flex-direction: column; gap: 16px; }
.plan-card { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 20px; display: flex; flex-direction: column; gap: 12px; transition: border-color 0.2s; }
.plan-card:hover { border-color: var(--border-green); }
.plan-top { display: flex; align-items: center; justify-content: space-between; }
.plan-name { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.plan-dates { font-size: 12px; color: var(--text-muted); }
.plan-bar-wrap { display: flex; align-items: center; gap: 10px; }
.plan-bar-bg { flex: 1; height: 8px; background: rgba(255,255,255,0.06); border-radius: 4px; overflow: hidden; }
.plan-bar-fill { height: 100%; border-radius: 4px; transition: width 1s ease; }
.plan-pct { font-size: 13px; font-weight: 700; color: var(--green); min-width: 36px; text-align: right; }
.plan-stats { display: flex; gap: 16px; font-size: 12px; color: var(--text-secondary); }
.empty-hint { font-size: 13px; color: var(--text-muted); padding: 20px 0; }
</style>
