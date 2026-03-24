<template>
<!-- 没权限时显示这个页面 -->
  <div class="page-403">
    <div class="content">
      <div class="code">403</div>
      <p class="msg">你没有权限访问此页面</p>
      <router-link to="/dashboard" class="back-btn">返回首页</router-link>
    </div>
  </div>
</template>

<style scoped>
.page-403 { height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--bg-base); }
.content { text-align: center; }
.code { font-size: 120px; font-weight: 800; color: rgba(255,255,255,0.06); line-height: 1; }
.msg  { font-size: 16px; color: var(--text-secondary); margin: 16px 0 32px; }
.back-btn { padding: 10px 28px; background: var(--green-dim); border: 1px solid var(--border-green); color: var(--green); border-radius: var(--radius-md); text-decoration: none; font-size: 14px; font-weight: 600; }
</style>
