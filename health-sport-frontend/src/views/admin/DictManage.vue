<template>
  <div class="dict-page">
    <el-tabs v-model="activeTab" class="dark-tabs">
      <!-- ── 运动字典 ── -->
      <el-tab-pane label="运动类型库" name="exercise">
        <div class="tab-layout">
          <div class="form-panel">
            <h3 class="form-title">{{ exEditing ? '编辑运动' : '新增运动' }}</h3>
            <el-form :model="exForm" label-width="80px" size="small">
              <el-form-item label="名称"><el-input v-model="exForm.name" placeholder="如：跑步"/></el-form-item>
              <el-form-item label="分类"><el-input v-model="exForm.category" placeholder="有氧/无氧/球类"/></el-form-item>
              <el-form-item label="MET值"><el-input v-model.number="exForm.metValue" type="number" placeholder="如：8.0"/></el-form-item>
              <el-form-item label="描述"><el-input v-model="exForm.description" type="textarea" :rows="2"/></el-form-item>
              <div class="btn-row">
                <el-button type="primary" :loading="exSaving" @click="saveExercise" style="flex:1">{{ exEditing ? '保存' : '新增' }}</el-button>
                <el-button v-if="exEditing" @click="cancelExEdit">取消</el-button>
              </div>
            </el-form>
          </div>
          <div class="table-panel">
            <el-table :data="exList" style="width:100%" size="small">
              <el-table-column prop="id"          label="ID"   width="60"/>
              <el-table-column prop="name"        label="名称" width="100"/>
              <el-table-column prop="category"    label="分类" width="80"/>
              <el-table-column prop="metValue"    label="MET"  width="70"/>
              <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip/>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" text @click="editExercise(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="delExercise(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- ── 食物字典 ── -->
      <el-tab-pane label="食物热量库" name="food">
        <div class="tab-layout">
          <div class="form-panel">
            <h3 class="form-title">{{ foodEditing ? '编辑食物' : '新增食物' }}</h3>
            <el-form :model="foodForm" label-width="90px" size="small">
              <el-form-item label="名称"><el-input v-model="foodForm.name" placeholder="如：白米饭"/></el-form-item>
              <el-form-item label="分类"><el-input v-model="foodForm.category" placeholder="主食/蔬菜/肉类"/></el-form-item>
              <el-form-item label="热量/100g"><el-input v-model.number="foodForm.caloriesPer100g" type="number" placeholder="kcal"/></el-form-item>
              <el-form-item label="蛋白质(g)"><el-input v-model.number="foodForm.protein" type="number"/></el-form-item>
              <el-form-item label="脂肪(g)"><el-input v-model.number="foodForm.fat" type="number"/></el-form-item>
              <el-form-item label="碳水(g)"><el-input v-model.number="foodForm.carbohydrate" type="number"/></el-form-item>
              <div class="btn-row">
                <el-button type="primary" :loading="foodSaving" @click="saveFood" style="flex:1">{{ foodEditing ? '保存' : '新增' }}</el-button>
                <el-button v-if="foodEditing" @click="cancelFoodEdit">取消</el-button>
              </div>
            </el-form>
          </div>
          <div class="table-panel">
            <el-table :data="foodList" style="width:100%" size="small">
              <el-table-column prop="id"              label="ID"    width="60"/>
              <el-table-column prop="name"            label="名称"  width="110"/>
              <el-table-column prop="category"        label="分类"  width="80"/>
              <el-table-column prop="caloriesPer100g" label="热量"  width="90">
                <template #default="{ row }">{{ row.caloriesPer100g }} kcal</template>
              </el-table-column>
              <el-table-column prop="protein"     label="蛋白质" width="75"/>
              <el-table-column prop="fat"         label="脂肪"   width="65"/>
              <el-table-column prop="carbohydrate"label="碳水"   width="65"/>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" text @click="editFood(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="delFood(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
// 这个页面脚本主要处理交互和数据请求
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const activeTab  = ref('exercise')
const exList     = ref([])
const foodList   = ref([])
const exEditing  = ref(false); const exEditId  = ref(null); const exSaving  = ref(false)
const foodEditing= ref(false); const foodEditId= ref(null); const foodSaving= ref(false)

const exForm   = reactive({ name:'', category:'', metValue:null, description:'' })
const foodForm = reactive({ name:'', category:'', caloriesPer100g:null, protein:null, fat:null, carbohydrate:null })

// ── exercise ──
async function loadExercise() { try { exList.value = (await adminApi.exDictList()).data || [] } catch {} }
// 这个函数处理一下editExercise逻辑
function editExercise(row) { exEditing.value=true; exEditId.value=row.id; Object.assign(exForm, row) }
// 这个函数处理一下cancelExEdit逻辑
function cancelExEdit() { exEditing.value=false; exEditId.value=null; Object.assign(exForm, { name:'', category:'', metValue:null, description:'' }) }
async function saveExercise() {
  if (!exForm.name || !exForm.metValue) { ElMessage.warning('名称和MET值必填'); return }
  exSaving.value = true
  try {
    exEditing.value ? await adminApi.updateExDict(exEditId.value, exForm) : await adminApi.addExDict(exForm)
    ElMessage.success('保存成功'); cancelExEdit(); loadExercise()
  } finally { exSaving.value = false }
}
async function delExercise(id) {
  await ElMessageBox.confirm('确定删除？', '提示', { type:'warning' })
  await adminApi.delExDict(id); ElMessage.success('已删除'); loadExercise()
}

// ── food ──
async function loadFood() { try { foodList.value = (await adminApi.foodList()).data || [] } catch {} }
// 这个函数处理一下editFood逻辑
function editFood(row) { foodEditing.value=true; foodEditId.value=row.id; Object.assign(foodForm, row) }
// 这个函数处理一下cancelFoodEdit逻辑
function cancelFoodEdit() { foodEditing.value=false; foodEditId.value=null; Object.assign(foodForm, { name:'', category:'', caloriesPer100g:null, protein:null, fat:null, carbohydrate:null }) }
async function saveFood() {
  if (!foodForm.name || !foodForm.caloriesPer100g) { ElMessage.warning('名称和热量必填'); return }
  foodSaving.value = true
  try {
    foodEditing.value ? await adminApi.updateFood(foodEditId.value, foodForm) : await adminApi.addFood(foodForm)
    ElMessage.success('保存成功'); cancelFoodEdit(); loadFood()
  } finally { foodSaving.value = false }
}
async function delFood(id) {
  await ElMessageBox.confirm('确定删除？', '提示', { type:'warning' })
  await adminApi.delFood(id); ElMessage.success('已删除'); loadFood()
}

onMounted(() => { loadExercise(); loadFood() })
</script>

<style scoped>
.dict-page :deep(.el-tabs__header) { border-bottom: 1px solid var(--border); margin-bottom: 20px; }
.dict-page :deep(.el-tabs__item) { color: var(--text-secondary); }
.dict-page :deep(.el-tabs__item.is-active) { color: var(--green); }
.dict-page :deep(.el-tabs__active-bar) { background: var(--green); }
.tab-layout { display: grid; grid-template-columns: 320px 1fr; gap: 24px; align-items: start; }
.form-panel { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 24px; }
.form-title { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 20px; }
.table-panel { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 24px; }
.btn-row { display: flex; gap: 8px; margin-top: 8px; }
</style>
