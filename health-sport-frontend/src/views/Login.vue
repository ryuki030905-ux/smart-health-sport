<template>
  <div class="login-root">
    <!-- 背景装饰 -->
    <div class="bg-layer">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="grid-lines"></div>
    </div>

<!-- 左边的介绍部分 -->
    <div class="left-panel">
      <div class="brand-block">
        <div class="logo-wrap">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <circle cx="24" cy="24" r="22" stroke="#4ade80" stroke-width="2" opacity="0.3"/>
            <path d="M12 26 L18 18 L24 24 L30 14 L36 22" stroke="#4ade80" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            <circle cx="24" cy="32" r="4" fill="#4ade80" opacity="0.8"/>
          </svg>
        </div>
        <h1 class="brand-name">HealthPulse</h1>
        <p class="brand-sub">个人健康与运动管理系统</p>
      </div>

      <div class="stats-row">
        <div class="stat-chip">
          <span class="stat-num">BMI</span>
          <span class="stat-label">健康指数</span>
        </div>
        <div class="stat-chip">
          <span class="stat-num">卡路里</span>
          <span class="stat-label">热量追踪</span>
        </div>
        <div class="stat-chip">
          <span class="stat-num">运动</span>
          <span class="stat-label">打卡计划</span>
        </div>
      </div>

      <p class="tagline">
        记录每一次运动<br/>
        追踪每一口饮食<br/>
        <em>掌握你的健康节律</em>
      </p>
    </div>

<!-- 右边的登录注册部分 -->
    <div class="right-panel">
      <div class="form-card" :class="{ shake: shaking }">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录你的健康账户</p>
        </div>

        <div class="tab-row">
          <button class="tab-btn" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
          <button class="tab-btn" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
        </div>

        <!-- 登录表单 -->
        <transition name="slide-fade" mode="out-in">
          <div v-if="mode === 'login'" key="login" class="form-body">
            <div class="field-group" :class="{ error: errors.username }">
              <label>用户名</label>
              <div class="input-wrap">
                <svg class="input-icon" viewBox="0 0 20 20" fill="none">
                  <circle cx="10" cy="7" r="3.5" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M3 17c0-3.314 3.134-6 7-6s7 2.686 7 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <input
                  v-model="loginForm.username"
                  type="text"
                  placeholder="请输入用户名"
                  @keyup.enter="handleLogin"
                  @input="errors.username = ''"
                />
              </div>
              <span class="err-msg" v-if="errors.username">{{ errors.username }}</span>
            </div>

            <div class="field-group" :class="{ error: errors.password }">
              <label>密码</label>
              <div class="input-wrap">
                <svg class="input-icon" viewBox="0 0 20 20" fill="none">
                  <rect x="4" y="9" width="12" height="9" rx="2" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M7 9V6a3 3 0 016 0v3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <input
                  v-model="loginForm.password"
                  :type="showPwd ? 'text' : 'password'"
                  placeholder="请输入密码"
                  @keyup.enter="handleLogin"
                  @input="errors.password = ''"
                />
                <button class="eye-btn" @click="showPwd = !showPwd" type="button">
                  <svg v-if="!showPwd" viewBox="0 0 20 20" fill="none">
                    <path d="M2 10s3-6 8-6 8 6 8 6-3 6-8 6-8-6-8-6z" stroke="currentColor" stroke-width="1.5"/>
                    <circle cx="10" cy="10" r="2.5" stroke="currentColor" stroke-width="1.5"/>
                  </svg>
                  <svg v-else viewBox="0 0 20 20" fill="none">
                    <path d="M3 3l14 14M8.5 8.6A2.5 2.5 0 0012 12M4.5 5.6C3 7 2 10 2 10s3 6 8 6c1.5 0 2.9-.4 4-.9M7 4.2C7.9 4 9 4 10 4c5 0 8 6 8 6s-.7 1.5-2 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
              <span class="err-msg" v-if="errors.password">{{ errors.password }}</span>
            </div>

            <div class="alert-box" v-if="alertMsg">{{ alertMsg }}</div>

            <button class="submit-btn" @click="handleLogin" :disabled="loading">
              <span v-if="!loading">登 录</span>
              <span v-else class="loading-dots">
                <i></i><i></i><i></i>
              </span>
            </button>
          </div>

          <!-- 注册表单 -->
          <div v-else key="register" class="form-body">
            <div class="field-row">
              <div class="field-group" :class="{ error: errors.regUsername }">
                <label>用户名</label>
                <div class="input-wrap">
                  <svg class="input-icon" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="7" r="3.5" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M3 17c0-3.314 3.134-6 7-6s7 2.686 7 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <input v-model="regForm.username" type="text" placeholder="4-20个字符" @input="errors.regUsername = ''"/>
                </div>
                <span class="err-msg" v-if="errors.regUsername">{{ errors.regUsername }}</span>
              </div>

              <div class="field-group" :class="{ error: errors.regPassword }">
                <label>密码</label>
                <div class="input-wrap">
                  <svg class="input-icon" viewBox="0 0 20 20" fill="none">
                    <rect x="4" y="9" width="12" height="9" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M7 9V6a3 3 0 016 0v3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <input v-model="regForm.password" type="password" placeholder="8-20个字符" @input="errors.regPassword = ''"/>
                </div>
                <span class="err-msg" v-if="errors.regPassword">{{ errors.regPassword }}</span>
              </div>
            </div>

            <div class="field-row">
              <div class="field-group">
                <label>昵称（可选）</label>
                <div class="input-wrap">
                  <svg class="input-icon" viewBox="0 0 20 20" fill="none">
                    <path d="M4 15s1-3 6-3 6 3 6 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <circle cx="10" cy="8" r="3" stroke="currentColor" stroke-width="1.5"/>
                  </svg>
                  <input v-model="regForm.nickname" type="text" placeholder="你的昵称"/>
                </div>
              </div>

              <div class="field-group">
                <label>年龄（可选）</label>
                <div class="input-wrap">
                  <svg class="input-icon" viewBox="0 0 20 20" fill="none">
                    <rect x="3" y="4" width="14" height="13" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M7 2v4M13 2v4M3 9h14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <input v-model="regForm.age" type="number" placeholder="年龄" min="1" max="120"/>
                </div>
              </div>
            </div>

            <div class="field-group">
              <label>性别（可选）</label>
              <div class="gender-row">
                <button
                  v-for="g in genderOptions" :key="g.value"
                  class="gender-btn"
                  :class="{ active: regForm.gender === g.value }"
                  @click="regForm.gender = g.value"
                  type="button"
                >{{ g.label }}</button>
              </div>
            </div>

            <div class="alert-box success" v-if="regSuccess">注册成功！请登录</div>
            <div class="alert-box" v-if="alertMsg">{{ alertMsg }}</div>

            <button class="submit-btn" @click="handleRegister" :disabled="loading">
              <span v-if="!loading">注 册</span>
              <span v-else class="loading-dots"><i></i><i></i><i></i></span>
            </button>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
// 登录页这边的逻辑
import { ref, reactive } from 'vue'
import { authApi } from '@/api'

// 页面里会用到的几个状态
const mode      = ref('login')
const loading   = ref(false)
const showPwd   = ref(false)
const alertMsg  = ref('')
const regSuccess= ref(false)
const shaking   = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm   = reactive({ username: '', password: '', nickname: '', age: '', gender: 0 })
const errors    = reactive({})

const genderOptions = [
  { label: '保密', value: 0 },
  { label: '男',   value: 1 },
  { label: '女',   value: 2 },
]

// 输入有问题时让卡片抖一下
function triggerShake() {
  shaking.value = true
  setTimeout(() => shaking.value = false, 500)
}

async function handleLogin() {
  alertMsg.value = ''
  errors.username = loginForm.username ? '' : '请输入用户名'
  errors.password = loginForm.password ? '' : '请输入密码'
  if (errors.username || errors.password) { triggerShake(); return }

  loading.value = true
  try {
    const res = await authApi.login({
      username: loginForm.username,
      password: loginForm.password,
    })
    if (res.code === 200) {
      const { token, userInfo } = res.data
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
// 登录成功后直接跳首页
      window.location.href = '/dashboard'
    } else {
      alertMsg.value = res.message || '登录失败'
      triggerShake()
    }
  } catch (e) {
    alertMsg.value = e.response?.data?.message || '网络错误，请重试'
    triggerShake()
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  alertMsg.value = ''
  regSuccess.value = false
  errors.regUsername = regForm.username.length >= 4 ? '' : '用户名至少4个字符'
  errors.regPassword = regForm.password.length >= 8 ? '' : '密码至少8个字符'
  if (errors.regUsername || errors.regPassword) { triggerShake(); return }

  loading.value = true
  try {
    const payload = {
      username: regForm.username,
      password: regForm.password,
      ...(regForm.nickname && { nickname: regForm.nickname }),
      ...(regForm.age      && { age: Number(regForm.age) }),
      gender: regForm.gender,
    }
    const res = await authApi.register(payload)
    if (res.code === 200) {
      regSuccess.value = true
      setTimeout(() => { mode.value = 'login'; regSuccess.value = false }, 1500)
    } else {
      alertMsg.value = res.message || '注册失败'
      triggerShake()
    }
  } catch (e) {
    alertMsg.value = e.response?.data?.message || '网络错误，请重试'
    triggerShake()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ── reset & root ── */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.login-root {
  display: flex;
  min-height: 100vh;
  background: #080f1a;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  position: relative;
  overflow: hidden;
}

/* ── animated background ── */
.bg-layer {
  position: absolute; inset: 0; pointer-events: none; z-index: 0;
}
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: drift 12s ease-in-out infinite alternate;
}
.orb-1 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(74,222,128,0.15) 0%, transparent 70%);
  top: -100px; left: -100px;
  animation-duration: 14s;
}
.orb-2 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(56,189,248,0.12) 0%, transparent 70%);
  bottom: -80px; right: 30%;
  animation-duration: 10s;
  animation-delay: -5s;
}
.orb-3 {
  width: 300px; height: 300px;
  background: radial-gradient(circle, rgba(167,139,250,0.1) 0%, transparent 70%);
  top: 40%; right: -50px;
  animation-duration: 16s;
  animation-delay: -8s;
}
@keyframes drift {
  from { transform: translate(0, 0) scale(1); }
  to   { transform: translate(30px, 40px) scale(1.1); }
}
.grid-lines {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(74,222,128,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(74,222,128,0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

/* ── left panel ── */
.left-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 64px;
  position: relative;
  z-index: 1;
}
.brand-block { margin-bottom: 48px; }
.logo-wrap {
  width: 64px; height: 64px;
  background: rgba(74,222,128,0.08);
  border: 1px solid rgba(74,222,128,0.2);
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 20px;
}
.brand-name {
  font-size: 36px;
  font-weight: 700;
  color: #f0fdf4;
  letter-spacing: -0.5px;
  margin-bottom: 6px;
}
.brand-sub {
  font-size: 14px;
  color: rgba(255,255,255,0.4);
  letter-spacing: 2px;
}
.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 40px;
}
.stat-chip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 20px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}
.stat-num {
  font-size: 15px;
  font-weight: 700;
  color: #4ade80;
}
.stat-label {
  font-size: 11px;
  color: rgba(255,255,255,0.4);
}
.tagline {
  font-size: 22px;
  line-height: 1.7;
  color: rgba(255,255,255,0.5);
  font-weight: 300;
}
.tagline em {
  font-style: normal;
  color: #4ade80;
  font-weight: 500;
}

/* ── right panel ── */
.right-panel {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 48px;
  position: relative;
  z-index: 1;
  background: rgba(255,255,255,0.02);
  border-left: 1px solid rgba(255,255,255,0.06);
  backdrop-filter: blur(20px);
}

/* ── form card ── */
.form-card {
  width: 100%;
  max-width: 360px;
}
.form-card.shake {
  animation: shake 0.4s ease;
}
@keyframes shake {
  0%,100% { transform: translateX(0); }
  20%,60%  { transform: translateX(-6px); }
  40%,80%  { transform: translateX(6px); }
}
.form-header {
  margin-bottom: 32px;
}
.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #f0fdf4;
  margin-bottom: 6px;
  letter-spacing: -0.3px;
}
.form-header p {
  font-size: 14px;
  color: rgba(255,255,255,0.4);
}

/* ── tabs ── */
.tab-row {
  display: flex;
  background: rgba(255,255,255,0.04);
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 28px;
  border: 1px solid rgba(255,255,255,0.06);
}
.tab-btn {
  flex: 1;
  padding: 9px;
  border: none;
  background: transparent;
  color: rgba(255,255,255,0.4);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 7px;
  transition: all 0.2s;
  font-family: inherit;
}
.tab-btn.active {
  background: rgba(74,222,128,0.15);
  color: #4ade80;
  border: 1px solid rgba(74,222,128,0.25);
}

/* ── form body ── */
.form-body { display: flex; flex-direction: column; gap: 16px; }
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.field-group { display: flex; flex-direction: column; gap: 6px; }
.field-group label {
  font-size: 12px;
  font-weight: 500;
  color: rgba(255,255,255,0.5);
  letter-spacing: 0.5px;
}
.field-group.error .input-wrap { border-color: rgba(248,113,113,0.5); }

.input-wrap {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px;
  padding: 0 12px;
  transition: border-color 0.2s, background 0.2s;
}
.input-wrap:focus-within {
  border-color: rgba(74,222,128,0.5);
  background: rgba(74,222,128,0.04);
}
.input-icon {
  width: 16px; height: 16px;
  color: rgba(255,255,255,0.3);
  flex-shrink: 0;
  margin-right: 8px;
}
.input-wrap input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #f0fdf4;
  font-size: 14px;
  padding: 11px 0;
  font-family: inherit;
}
.input-wrap input::placeholder { color: rgba(255,255,255,0.2); }
.eye-btn {
  background: none; border: none; cursor: pointer; padding: 4px;
  color: rgba(255,255,255,0.3); display: flex; align-items: center;
  transition: color 0.2s;
}
.eye-btn:hover { color: rgba(255,255,255,0.6); }
.eye-btn svg { width: 16px; height: 16px; }

.err-msg { font-size: 11px; color: #f87171; }

/* ── gender ── */
.gender-row { display: flex; gap: 8px; }
.gender-btn {
  flex: 1; padding: 9px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  color: rgba(255,255,255,0.4);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.gender-btn.active {
  background: rgba(74,222,128,0.12);
  border-color: rgba(74,222,128,0.3);
  color: #4ade80;
}

/* ── alert ── */
.alert-box {
  padding: 10px 14px;
  background: rgba(248,113,113,0.1);
  border: 1px solid rgba(248,113,113,0.2);
  border-radius: 8px;
  font-size: 13px;
  color: #f87171;
}
.alert-box.success {
  background: rgba(74,222,128,0.1);
  border-color: rgba(74,222,128,0.2);
  color: #4ade80;
}

/* ── submit button ── */
.submit-btn {
  width: 100%;
  padding: 13px;
  background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
  border: none;
  border-radius: 10px;
  color: #052e16;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  letter-spacing: 2px;
  transition: opacity 0.2s, transform 0.15s;
  font-family: inherit;
  margin-top: 4px;
}
.submit-btn:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
.submit-btn:active:not(:disabled) { transform: translateY(0); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ── loading dots ── */
.loading-dots { display: flex; align-items: center; justify-content: center; gap: 5px; }
.loading-dots i {
  display: block; width: 6px; height: 6px;
  background: #052e16; border-radius: 50%;
  animation: bounce 0.6s ease-in-out infinite alternate;
}
.loading-dots i:nth-child(2) { animation-delay: 0.15s; }
.loading-dots i:nth-child(3) { animation-delay: 0.3s; }
@keyframes bounce {
  from { transform: translateY(0); opacity: 0.6; }
  to   { transform: translateY(-5px); opacity: 1; }
}

/* ── transitions ── */
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.25s ease; }
.slide-fade-enter-from { opacity: 0; transform: translateX(16px); }
.slide-fade-leave-to   { opacity: 0; transform: translateX(-16px); }

/* ── responsive ── */
@media (max-width: 768px) {
  .left-panel { display: none; }
  .right-panel { width: 100%; border-left: none; padding: 40px 24px; }
}
</style>
