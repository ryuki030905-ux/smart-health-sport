<template>
  <div class="admin-page">
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索用户名..." clearable size="small" style="width:260px" @keyup.enter="loadUsers" @clear="loadUsers">
        <template #prefix><span style="color:var(--text-muted)">🔍</span></template>
      </el-input>
      <el-button type="primary" size="small" @click="loadUsers">搜索</el-button>
      <span class="total-badge">共 {{ total }} 名用户</span>
    </div>

    <el-table :data="users" style="width:100%" v-loading="loading">
      <el-table-column prop="id"         label="ID"    width="70"/>
      <el-table-column prop="username"   label="用户名" width="130"/>
      <el-table-column prop="nickname"   label="昵称"   width="120">
        <template #default="{ row }">{{ row.nickname || '--' }}</template>
      </el-table-column>
      <el-table-column prop="gender"    label="性别"   width="70">
        <template #default="{ row }">{{ ['保密','男','女'][row.gender] || '--' }}</template>
      </el-table-column>
      <el-table-column prop="age"       label="年龄"   width="70">
        <template #default="{ row }">{{ row.age ?? '--' }}</template>
      </el-table-column>
      <el-table-column prop="role"      label="角色"   width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'info'" size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status"    label="状态"   width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '已封禁' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="aiDailyLimit" label="AI次数" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.role === 'ADMIN'" type="warning" size="small">无限制</el-tag>
          <span v-else>{{ row.aiDailyLimit ?? 3 }} 次/天</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" min-width="160" show-overflow-tooltip/>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.role !== 'ADMIN'"
            size="small" text
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >{{ row.status === 1 ? '封禁' : '解封' }}</el-button>
          <el-button
            v-if="row.role !== 'ADMIN'"
            size="small" text type="primary"
            @click="editAiLimit(row)"
          >设置AI次数</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadUsers"
      />
    </div>
  </div>
</template>

<script setup>
// 这个页面脚本主要处理交互和数据请求
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const users   = ref([])
const keyword = ref('')
const page    = ref(1)
const size    = ref(10)
const total   = ref(0)
const loading = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminApi.userList({ keyword: keyword.value, page: page.value, size: size.value })
    users.value = res.data?.records || res.data || []
    total.value = res.data?.total || users.value.length
  } finally { loading.value = false }
}

async function toggleStatus(row) {
  const action = row.status === 1 ? '封禁' : '解封'
  await ElMessageBox.confirm(`确定要${action}用户 "${row.username}"？`, '操作确认', { type: 'warning' })
  await adminApi.setStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success(`${action}成功`)
  loadUsers()
}

async function editAiLimit(row) {
  const { value } = await ElMessageBox.prompt(
    `请输入用户「${row.username}」的每日 AI 次数（可设为 0）`,
    '设置 AI 次数',
    {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValue: row.aiDailyLimit ?? 3,
      inputPattern: /^\d+$/,
      inputErrorMessage: '请输入大于等于0的整数'
    }
  )

  const aiDailyLimit = Number(value)
  await adminApi.setAiLimit(row.id, aiDailyLimit)
  ElMessage.success('AI 次数设置成功')
  loadUsers()
}

onMounted(loadUsers)
</script>

<style scoped>
.admin-page { display: flex; flex-direction: column; gap: 20px; }
.search-bar { display: flex; align-items: center; gap: 12px; }
.total-badge { font-size: 13px; color: var(--text-muted); margin-left: 8px; }
.pagination { display: flex; justify-content: flex-end; }
</style>
