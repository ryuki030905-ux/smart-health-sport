<template>
  <div class="layout">
    <!-- 左侧菜单 -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <svg width="28" height="28" viewBox="0 0 48 48" fill="none">
            <circle cx="24" cy="24" r="22" stroke="#4ade80" stroke-width="2" opacity="0.3"/>
            <path d="M12 26 L18 18 L24 24 L30 14 L36 22" stroke="#4ade80" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            <circle cx="24" cy="32" r="4" fill="#4ade80" opacity="0.8"/>
          </svg>
          <span class="logo-text" v-show="!collapsed">HealthPulse</span>
        </div>
        <button class="collapse-btn" @click="collapsed = !collapsed">
          <svg viewBox="0 0 20 20" fill="none" :style="{ transform: collapsed ? 'rotate(180deg)' : '' }">
            <path d="M13 5l-5 5 5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <nav class="nav">
        <div class="nav-group">
          <p class="nav-label" v-show="!collapsed">主功能</p>
          <router-link v-for="item in mainNav" :key="item.path" :to="item.path" class="nav-item">
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text" v-show="!collapsed">{{ item.label }}</span>
          </router-link>
        </div>
        <div class="nav-group" v-if="userStore.isAdmin">
          <p class="nav-label" v-show="!collapsed">管理员</p>
          <router-link v-for="item in adminNav" :key="item.path" :to="item.path" class="nav-item">
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text" v-show="!collapsed">{{ item.label }}</span>
          </router-link>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info" v-show="!collapsed">
          <div class="avatar">{{ avatarChar }}</div>
          <div class="user-detail">
            <p class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</p>
            <p class="user-role">{{ userStore.isAdmin ? '管理员' : '普通用户' }}</p>
          </div>
        </div>
        <div class="avatar-sm" v-show="collapsed">{{ avatarChar }}</div>
        <button class="logout-btn" @click="handleLogout" title="退出登录">
          <svg viewBox="0 0 20 20" fill="none">
            <path d="M13 10H3M13 10l-3-3M13 10l-3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M8 6V5a2 2 0 012-2h5a2 2 0 012 2v10a2 2 0 01-2 2h-5a2 2 0 01-2-2v-1" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <main class="main-content">
      <div class="page-header">
        <h1 class="page-title">{{ currentTitle }}</h1>
        <div class="header-right">
          <span class="date-badge">{{ todayStr }}</span>
        </div>
      </div>
      <div class="page-body">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
// 布局页的一些基础逻辑
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router     = useRouter()
const route      = useRoute()
const userStore  = useUserStore()
const collapsed  = ref(false)

const avatarChar = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || '?'
  return name.charAt(0).toUpperCase()
})
const currentTitle = computed(() => route.meta.title || '')
const todayStr = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日`
})

// 退出登录
function handleLogout() {
  userStore.logout()
  router.push('/login')
}

const mainNav = [
  { path: '/dashboard', label: '首页总览', icon: `<svg viewBox="0 0 20 20" fill="none"><rect x="2" y="2" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="11" y="2" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="2" y="11" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="11" y="11" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/></svg>` },
  { path: '/health',    label: '健康档案', icon: `<svg viewBox="0 0 20 20" fill="none"><path d="M10 3v14M3 10h14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><rect x="2" y="2" width="16" height="16" rx="3" stroke="currentColor" stroke-width="1.5"/></svg>` },
  { path: '/exercise',  label: '运动管理', icon: `<svg viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.5"/><path d="M6 10l2.5 2.5L14 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>` },
  { path: '/diet',      label: '饮食记录', icon: `<svg viewBox="0 0 20 20" fill="none"><path d="M6 2v6a4 4 0 008 0V2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><path d="M10 12v6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>` },
  { path: '/charts',    label: '数据图表', icon: `<svg viewBox="0 0 20 20" fill="none"><path d="M3 15l4-5 4 3 4-7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>` },
  { path: '/ai',        label: 'AI 健康助手', icon: `<svg viewBox="0 0 20 20" fill="none"><path d="M10 2a1 1 0 011 1v3.586l2.707 2.707A1 1 0 0015 9V8a1 1 0 10-2 0v1H8V8a1 1 0 10-2 0v1H3a1 1 0 00-.707 1.707L5 13.414V15a1 1 0 001 1h8a1 1 0 001-1v-1.586l2.707-2.707A1 1 0 0017 11V9a1 1 0 10-2 0v2H8V9a1 1 0 10-2 0v2H5a1 1 0 00-1 1v3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>` },
]
const adminNav = [
  { path: '/admin/users', label: '用户管理', icon: `<svg viewBox="0 0 20 20" fill="none"><circle cx="8" cy="7" r="3" stroke="currentColor" stroke-width="1.5"/><path d="M2 17c0-3 2.7-5 6-5s6 2 6 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><path d="M14 9l2 2 3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>` },
  { path: '/admin/dict',  label: '字典管理', icon: `<svg viewBox="0 0 20 20" fill="none"><path d="M4 4h12v12H4z" stroke="currentColor" stroke-width="1.5" rx="2"/><path d="M8 4v12M4 8h4M4 12h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>` },
]
</script>

<style scoped>
.layout { display: flex; height: 100vh; overflow: hidden; background: var(--bg-base); }

/* ── sidebar ── */
.sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--bg-surface);
  border-right: 1px solid var(--border);
  transition: width 0.25s ease;
  overflow: hidden;
}
.sidebar.collapsed { width: 64px; }

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 16px;
  border-bottom: 1px solid var(--border);
}
.logo { display: flex; align-items: center; gap: 10px; overflow: hidden; }
.logo-text { font-size: 16px; font-weight: 700; color: var(--text-primary); white-space: nowrap; }
.collapse-btn {
  width: 28px; height: 28px; flex-shrink: 0;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: 6px; cursor: pointer; color: var(--text-secondary);
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.collapse-btn svg { width: 14px; height: 14px; transition: transform 0.25s; }
.collapse-btn:hover { border-color: var(--border-green); color: var(--green); }

.nav { flex: 1; padding: 16px 8px; overflow-y: auto; display: flex; flex-direction: column; gap: 20px; }
.nav-group { display: flex; flex-direction: column; gap: 2px; }
.nav-label { font-size: 10px; font-weight: 600; color: var(--text-muted); letter-spacing: 1.5px; text-transform: uppercase; padding: 0 8px; margin-bottom: 6px; white-space: nowrap; }
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 10px; border-radius: var(--radius-sm);
  color: var(--text-secondary); text-decoration: none;
  font-size: 14px; transition: all 0.18s; white-space: nowrap;
}
.nav-item:hover { background: var(--bg-card); color: var(--text-primary); }
.nav-item.router-link-active { background: var(--green-dim); color: var(--green); border: 1px solid var(--border-green); }
.nav-icon { width: 20px; height: 20px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.nav-icon :deep(svg) { width: 18px; height: 18px; }

.sidebar-footer {
  padding: 12px 8px;
  border-top: 1px solid var(--border);
  display: flex; align-items: center; gap: 8px;
}
.user-info { display: flex; align-items: center; gap: 8px; flex: 1; overflow: hidden; }
.avatar, .avatar-sm {
  width: 32px; height: 32px; border-radius: 8px;
  background: var(--green-dim); border: 1px solid var(--border-green);
  color: var(--green); font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.user-detail { overflow: hidden; }
.user-name { font-size: 13px; font-weight: 600; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.user-role { font-size: 11px; color: var(--text-muted); }
.logout-btn {
  width: 32px; height: 32px; flex-shrink: 0;
  background: none; border: 1px solid var(--border);
  border-radius: 8px; cursor: pointer; color: var(--text-secondary);
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
}
.logout-btn svg { width: 16px; height: 16px; }
.logout-btn:hover { border-color: rgba(248,113,113,0.3); color: var(--red); }

/* ── main ── */
.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 32px; border-bottom: 1px solid var(--border);
  background: var(--bg-surface);
}
.page-title { font-size: 20px; font-weight: 700; color: var(--text-primary); }
.date-badge {
  font-size: 12px; color: var(--text-secondary);
  background: var(--bg-card); border: 1px solid var(--border);
  padding: 5px 12px; border-radius: 20px;
}
.page-body { flex: 1; overflow-y: auto; padding: 28px 32px; }

/* ── transition ── */
.page-fade-enter-active, .page-fade-leave-active { transition: all 0.2s ease; }
.page-fade-enter-from { opacity: 0; transform: translateY(8px); }
.page-fade-leave-to   { opacity: 0; transform: translateY(-8px); }
</style>
