<template>
  <!-- 简单无权限提示页：有些路由会直接跳到这里 -->
  <div class="forbidden">403 - 无权限访问</div>
</template>

<style scoped>
.forbidden {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  font-weight: 600;
}
</style>

