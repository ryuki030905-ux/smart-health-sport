<template>
  <div class="diet-page">
    <div class="top-row">
      <!-- 录入饮食 -->
      <div class="form-panel">
        <h3 class="form-title">记录饮食</h3>
        <el-form :model="form" label-width="80px" size="small">
          <el-form-item label="日期">
            <el-date-picker v-model="form.dietDate" type="date" value-format="YYYY-MM-DD" style="width:100%" @change="loadRecords"/>
          </el-form-item>
          <el-form-item label="餐次">
            <el-select v-model="form.mealType" style="width:100%">
              <el-option label="🌅 早餐" value="BREAKFAST"/>
              <el-option label="☀️ 午餐" value="LUNCH"/>
              <el-option label="🌙 晚餐" value="DINNER"/>
              <el-option label="🍎 加餐" value="SNACK"/>
            </el-select>
          </el-form-item>
          <el-form-item label="搜索食物">
            <el-input v-model="keyword" placeholder="输入食物名称..." @input="searchFood" clearable>
              <template #prefix><span style="color:var(--text-muted)">🔍</span></template>
            </el-input>
          </el-form-item>
          <div class="food-results" v-if="searchResults.length">
            <div class="food-item" v-for="f in searchResults" :key="f.id" :class="{ selected: form.foodDictId === f.id }" @click="selectFood(f)">
              <span class="food-name">{{ f.name }}</span>
              <span class="food-cal">{{ f.caloriesPer100g }} kcal/100g</span>
            </div>
          </div>
          <div class="selected-food" v-if="selectedFood">
            <span>已选：{{ selectedFood.name }}</span>
            <span class="food-cal">{{ selectedFood.caloriesPer100g }} kcal/100g</span>
          </div>
          <el-form-item label="摄入量(g)" style="margin-top:12px">
            <el-input v-model.number="form.quantityG" type="number" placeholder="如：200">
              <template #suffix>
                <span v-if="estimatedCal" style="color:#4ade80;font-size:12px">≈ {{ estimatedCal }} kcal</span>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" placeholder="备注..."/>
          </el-form-item>
          <el-button type="primary" :loading="saving" @click="handleAdd" style="width:100%;margin-top:4px">添加饮食记录</el-button>
        </el-form>
      </div>

      <!-- 当天汇总 -->
      <div class="summary-panel">
        <div class="panel-header">
          <h3>今日热量汇总</h3>
          <span class="date-tag">{{ form.dietDate }}</span>
        </div>
        <div class="total-cal">
          <span class="cal-num">{{ totalCalories }}</span>
          <span class="cal-unit">kcal</span>
        </div>
        <div class="meal-summary" v-for="meal in mealSummary" :key="meal.type">
          <div class="meal-info">
            <span class="meal-icon">{{ meal.icon }}</span>
            <span class="meal-name">{{ meal.label }}</span>
            <span class="meal-count">{{ meal.count }} 条</span>
          </div>
          <span class="meal-cal">{{ meal.calories }} kcal</span>
        </div>
        <div class="gap-info" v-if="burnedToday">
          <div class="gap-row">
            <span>今日消耗</span><span style="color:#4ade80">{{ burnedToday }} kcal</span>
          </div>
          <div class="gap-row">
            <span>热量缺口</span>
            <span :style="{ color: (totalCalories - burnedToday) > 0 ? '#f87171' : '#4ade80' }">
              {{ totalCalories - burnedToday > 0 ? '+' : '' }}{{ totalCalories - burnedToday }} kcal
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 饮食记录 -->
    <div class="panel">
      <div class="panel-header">
        <h3>饮食记录</h3>
      </div>
      <div class="meal-group" v-for="meal in mealGroups" :key="meal.type">
        <div class="meal-group-header">
          <span>{{ meal.icon }} {{ meal.label }}</span>
          <span class="group-cal">{{ meal.totalCal }} kcal</span>
        </div>
        <el-table :data="meal.items" style="width:100%" size="small">
          <el-table-column prop="foodName"   label="食物"   min-width="120"/>
          <el-table-column prop="quantityG"  label="摄入量" width="90">
            <template #default="{ row }">{{ row.quantityG }} g</template>
          </el-table-column>
          <el-table-column prop="calories"   label="热量"   width="110">
            <template #default="{ row }"><span style="color:#4ade80;font-weight:600">{{ row.calories }} kcal</span></template>
          </el-table-column>
          <el-table-column prop="remark"     label="备注"   min-width="100" show-overflow-tooltip/>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button size="small" text type="danger" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="empty-hint" v-if="!records.length">今日暂无饮食记录</div>
    </div>
  </div>
</template>

<script setup>
// 饮食页这边的逻辑
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dietApi, statsApi } from '@/api'

const records      = ref([])
const searchResults= ref([])
const selectedFood = ref(null)
const keyword      = ref('')
const saving       = ref(false)
const burnedToday  = ref(0)

const form = reactive({ dietDate: new Date().toISOString().slice(0,10), mealType:'LUNCH', foodDictId:null, quantityG:null, remark:'' })

const mealMeta = { BREAKFAST:{ icon:'🌅', label:'早餐' }, LUNCH:{ icon:'☀️', label:'午餐' }, DINNER:{ icon:'🌙', label:'晚餐' }, SNACK:{ icon:'🍎', label:'加餐' } }

const estimatedCal = computed(() => {
  if (!selectedFood.value || !form.quantityG) return 0
  return Math.round((form.quantityG / 100) * selectedFood.value.caloriesPer100g)
})

const totalCalories = computed(() => records.value.reduce((s, r) => s + (r.calories || 0), 0).toFixed(0))

const mealSummary = computed(() =>
  Object.entries(mealMeta).map(([type, meta]) => {
    const items = records.value.filter(r => r.mealType === type)
    return { type, ...meta, count: items.length, calories: items.reduce((s,r) => s+(r.calories||0), 0).toFixed(0) }
  })
)
const mealGroups = computed(() =>
  Object.entries(mealMeta).map(([type, meta]) => {
    const items = records.value.filter(r => r.mealType === type)
    return { type, ...meta, items, totalCal: items.reduce((s,r)=>s+(r.calories||0),0).toFixed(0) }
  }).filter(g => g.items.length > 0)
)

let searchTimer = null
// 搜食物的时候做个简单防抖，不然请求太频繁
function searchFood() {
  clearTimeout(searchTimer)
  if (!keyword.value.trim()) { searchResults.value = []; return }
  searchTimer = setTimeout(async () => {
    try { searchResults.value = (await dietApi.search(keyword.value)).data || [] } catch {}
  }, 300)
}
// 选中食物后顺手把表单里的食物 id 也带上
function selectFood(f) { selectedFood.value = f; form.foodDictId = f.id; keyword.value = ''; searchResults.value = [] }

async function handleAdd() {
  if (!form.foodDictId || !form.dietDate || !form.quantityG) { ElMessage.warning('请完整填写信息'); return }
  saving.value = true
  try {
    await dietApi.addRecord(form); ElMessage.success('记录成功！')
    form.foodDictId = null; form.quantityG = null; form.remark = ''; selectedFood.value = null
    loadRecords()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await dietApi.remove(id); loadRecords()
}

async function loadRecords() {
  try { records.value = (await dietApi.recordList({ date: form.dietDate })).data || [] } catch {}
}

async function loadBurned() {
  try {
    const d = new Date(form.dietDate)
    const res = await statsApi.calorieBalance(d.getFullYear(), d.getMonth()+1)
    const dayStr = `${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    const idx = res.data?.xAxis?.indexOf(dayStr) ?? -1
    burnedToday.value = idx >= 0 ? (res.data.burned[idx] || 0) : 0
  } catch {}
}

onMounted(() => { loadRecords(); loadBurned() })
</script>

<style scoped>
.diet-page { display: flex; flex-direction: column; gap: 24px; }
.top-row { display: grid; grid-template-columns: 360px 1fr; gap: 24px; align-items: start; }
.form-panel, .summary-panel, .panel { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 24px; }
.form-title { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 20px; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.panel-header h3 { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.date-tag { font-size: 12px; color: var(--text-muted); }
.food-results { max-height: 160px; overflow-y: auto; border: 1px solid var(--border); border-radius: var(--radius-sm); margin-bottom: 8px; }
.food-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; cursor: pointer; transition: background 0.15s; }
.food-item:hover, .food-item.selected { background: var(--green-dim); }
.food-name { font-size: 13px; color: var(--text-primary); }
.food-cal  { font-size: 12px; color: var(--text-muted); }
.selected-food { display: flex; justify-content: space-between; padding: 8px 12px; background: var(--green-dim); border: 1px solid var(--border-green); border-radius: var(--radius-sm); font-size: 13px; color: var(--green); margin-bottom: 8px; }
.total-cal { text-align: center; padding: 20px 0; border-bottom: 1px solid var(--border); margin-bottom: 16px; }
.cal-num { font-size: 48px; font-weight: 800; color: var(--green); }
.cal-unit { font-size: 16px; color: var(--text-muted); margin-left: 4px; }
.meal-summary { display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--border); }
.meal-info { display: flex; align-items: center; gap: 8px; }
.meal-icon { font-size: 16px; }
.meal-name { font-size: 13px; color: var(--text-primary); }
.meal-count { font-size: 11px; color: var(--text-muted); }
.meal-cal { font-size: 13px; font-weight: 600; color: var(--text-secondary); }
.gap-info { margin-top: 16px; display: flex; flex-direction: column; gap: 8px; }
.gap-row { display: flex; justify-content: space-between; font-size: 13px; color: var(--text-secondary); }
.meal-group { margin-bottom: 20px; }
.meal-group-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; font-size: 14px; font-weight: 600; color: var(--text-primary); border-bottom: 1px solid var(--border); margin-bottom: 8px; }
.group-cal { font-size: 13px; color: var(--green); }
.empty-hint { font-size: 13px; color: var(--text-muted); padding: 20px 0; }
</style>
