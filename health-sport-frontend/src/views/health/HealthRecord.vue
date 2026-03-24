<template>
  <div class="health-page">
    <div class="top-row">
      <!-- BMI 信息卡片 -->
      <div class="bmi-card" v-if="latest">
        <div class="bmi-left">
          <p class="bmi-num" :style="{ color: statusColor[latest.healthStatus] }">{{ latest.bmi }}</p>
          <p class="bmi-label">BMI 指数</p>
          <div class="status-tag" :class="latest.healthStatus?.toLowerCase()">{{ statusMap[latest.healthStatus] }}</div>
        </div>
        <div class="bmi-right">
          <div class="metric-row" v-for="m in metricItems" :key="m.label">
            <span class="m-label">{{ m.label }}</span>
            <span class="m-val">{{ m.value }}</span>
          </div>
        </div>
      </div>
      <div class="bmi-card empty-bmi" v-else>
        <p>还没有健康记录</p>
        <p>录入第一条数据后这里会显示你的健康状态</p>
      </div>

      <!-- 录入表单 -->
      <div class="form-panel">
        <div class="panel-header">
          <h3>{{ editing ? '编辑记录' : '录入健康数据' }}</h3>
          <button v-if="editing" class="cancel-btn" @click="cancelEdit">取消</button>
        </div>
        <el-form :model="form" label-width="70px" size="small">
          <div class="form-grid">
            <el-form-item label="日期">
              <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%"/>
            </el-form-item>
            <el-form-item label="身高(cm)">
              <el-input v-model.number="form.height" type="number" placeholder="如：172"/>
            </el-form-item>
            <el-form-item label="体重(kg)">
              <el-input v-model.number="form.weight" type="number" placeholder="如：68"/>
            </el-form-item>
            <el-form-item label="收缩压">
              <el-input v-model.number="form.systolicBp" type="number" placeholder="mmHg"/>
            </el-form-item>
            <el-form-item label="舒张压">
              <el-input v-model.number="form.diastolicBp" type="number" placeholder="mmHg"/>
            </el-form-item>
            <el-form-item label="血糖">
              <el-input v-model.number="form.bloodSugar" type="number" placeholder="mmol/L"/>
            </el-form-item>
            <el-form-item label="心率">
              <el-input v-model.number="form.heartRate" type="number" placeholder="次/分"/>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="今日状态..."/>
            </el-form-item>
          </div>
          <el-button type="primary" :loading="saving" @click="handleSave" style="width:100%;margin-top:8px">
            {{ editing ? '保存修改' : '保存并计算 BMI' }}
          </el-button>
        </el-form>
      </div>
    </div>

    <!-- 历史记录表 -->
    <div class="panel">
      <div class="panel-header">
        <h3>历史记录</h3>
        <span class="count-badge">共 {{ records.length }} 条</span>
      </div>
      <el-table :data="records" style="width:100%" row-class-name="dark-row">
        <el-table-column prop="recordDate"   label="日期"   width="110"/>
        <el-table-column prop="weight"       label="体重"   width="80">
          <template #default="{ row }">{{ row.weight ?? '--' }} kg</template>
        </el-table-column>
        <el-table-column prop="height"       label="身高"   width="80">
          <template #default="{ row }">{{ row.height ?? '--' }} cm</template>
        </el-table-column>
        <el-table-column prop="bmi"          label="BMI"    width="80"/>
        <el-table-column prop="bodyFat"      label="体脂率" width="80">
          <template #default="{ row }">{{ row.bodyFat ?? '--' }}%</template>
        </el-table-column>
        <el-table-column prop="healthStatus" label="健康状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.healthStatus)" size="small">{{ statusMap[row.healthStatus] || '--' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="血压" width="100">
          <template #default="{ row }">{{ row.systolicBp ? `${row.systolicBp}/${row.diastolicBp}` : '--' }}</template>
        </el-table-column>
        <el-table-column prop="heartRate" label="心率" width="80">
          <template #default="{ row }">{{ row.heartRate ?? '--' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip/>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="startEdit(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
// 健康档案页这边的逻辑
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { healthApi } from '@/api'

const records = ref([])
const latest  = ref(null)
const saving  = ref(false)
const editing = ref(false)
const editId  = ref(null)

const statusMap   = { UNDERWEIGHT:'偏瘦', NORMAL:'正常', OVERWEIGHT:'超重', OBESE:'肥胖' }
const statusColor = { UNDERWEIGHT:'#38bdf8', NORMAL:'#4ade80', OVERWEIGHT:'#fbbf24', OBESE:'#f87171' }
const tagType     = s => ({ UNDERWEIGHT:'info', NORMAL:'success', OVERWEIGHT:'warning', OBESE:'danger' }[s] || '')

const form = reactive({ recordDate:'', height:null, weight:null, systolicBp:null, diastolicBp:null, bloodSugar:null, heartRate:null, remark:'' })

const metricItems = computed(() => {
  if (!latest.value) return []
  const l = latest.value
  return [
    { label: '体重',   value: l.weight   ? l.weight+'kg' : '--' },
    { label: '体脂率', value: l.bodyFat  ? l.bodyFat+'%' : '--' },
    { label: '血压',   value: l.systolicBp ? `${l.systolicBp}/${l.diastolicBp} mmHg` : '--' },
    { label: '血糖',   value: l.bloodSugar ? l.bloodSugar+' mmol/L' : '--' },
    { label: '心率',   value: l.heartRate ? l.heartRate+' 次/分' : '--' },
    { label: '记录日期', value: l.recordDate || '--' },
  ]
})

// 清空表单
function resetForm() { Object.assign(form, { recordDate:'', height:null, weight:null, systolicBp:null, diastolicBp:null, bloodSugar:null, heartRate:null, remark:'' }) }

// 编辑的时候把这一行的数据回填到表单里
function startEdit(row) {
  editing.value = true; editId.value = row.id
  Object.assign(form, { recordDate: row.recordDate, height: row.height, weight: row.weight, systolicBp: row.systolicBp, diastolicBp: row.diastolicBp, bloodSugar: row.bloodSugar, heartRate: row.heartRate, remark: row.remark })
}
// 取消编辑
function cancelEdit() { editing.value = false; editId.value = null; resetForm() }

async function handleSave() {
  if (!form.recordDate) { ElMessage.warning('请选择日期'); return }
  saving.value = true
  try {
    if (editing.value) {
      await healthApi.update(editId.value, form); ElMessage.success('修改成功'); cancelEdit()
    } else {
      await healthApi.add(form); ElMessage.success('保存成功，已自动计算 BMI'); resetForm()
    }
    await loadData()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除这条记录？', '提示', { type: 'warning' })
  await healthApi.remove(id); ElMessage.success('已删除'); loadData()
}

async function loadData() {
  try {
    const res = await healthApi.list(); records.value = res.data?.records || res.data || []
    latest.value = records.value[0] || null
  } catch {}
}
onMounted(loadData)
</script>

<style scoped>
.health-page { display: flex; flex-direction: column; gap: 24px; }
.top-row { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.bmi-card {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius-lg); padding: 28px;
  display: flex; gap: 32px; align-items: flex-start;
}
.empty-bmi { flex-direction: column; justify-content: center; color: var(--text-muted); font-size: 13px; gap: 8px; }
.bmi-left { display: flex; flex-direction: column; align-items: center; gap: 8px; min-width: 100px; }
.bmi-num { font-size: 56px; font-weight: 800; line-height: 1; }
.bmi-label { font-size: 12px; color: var(--text-muted); }
.status-tag { padding: 4px 14px; border-radius: 20px; font-size: 13px; font-weight: 600; }
.status-tag.normal   { background: rgba(74,222,128,0.15); color: #4ade80; }
.status-tag.overweight { background: rgba(251,191,36,0.15); color: #fbbf24; }
.status-tag.obese    { background: rgba(248,113,113,0.15); color: #f87171; }
.status-tag.underweight { background: rgba(56,189,248,0.15); color: #38bdf8; }
.bmi-right { flex: 1; display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.metric-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; background: rgba(255,255,255,0.03); border-radius: var(--radius-sm); border: 1px solid var(--border); }
.m-label { font-size: 12px; color: var(--text-muted); }
.m-val   { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.form-panel { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 24px; }
.panel { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 24px; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.panel-header h3 { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.count-badge { font-size: 12px; color: var(--text-muted); background: var(--bg-card); border: 1px solid var(--border); padding: 3px 10px; border-radius: 10px; }
.cancel-btn { font-size: 12px; color: var(--text-secondary); background: none; border: 1px solid var(--border); padding: 4px 10px; border-radius: 6px; cursor: pointer; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 4px; }
</style>
