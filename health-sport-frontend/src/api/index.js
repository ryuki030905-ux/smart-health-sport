// 接口都先放这里，后面找起来会方便一点
import request from './request'

// 登录注册相关
export const authApi = {
  login:    data => request.post('/auth/login', data),
  register: data => request.post('/auth/register', data),
  logout:   ()   => request.post('/auth/logout'),
}

// 健康档案相关
export const healthApi = {
  list:   params => request.get('/health/records', { params }),
  latest: ()     => request.get('/health/records/latest'),
  add:    data   => request.post('/health/records', data),
  update: (id, data) => request.put(`/health/records/${id}`, data),
  remove: id     => request.delete(`/health/records/${id}`),
}

// 运动相关
export const exerciseApi = {
  dictList:   ()     => request.get('/exercise/dict'),
  recordList: params => request.get('/exercise/records', { params }),
  addRecord:  data   => request.post('/exercise/records', data),
  planList:   ()     => request.get('/exercise/plans'),
  addPlan:    data   => request.post('/exercise/plans', data),
}

// 饮食相关
export const dietApi = {
  search:     keyword => request.get('/diet/food', { params: { keyword } }),
  recordList: params  => request.get('/diet/records', { params }),
  addRecord:  data    => request.post('/diet/records', data),
  remove:     id      => request.delete(`/diet/records/${id}`),
}

// 图表统计相关
export const statsApi = {
  weightTrend:     days         => request.get('/statistics/weight-trend', { params: { days } }),
  calorieBalance:  (year,month) => request.get('/statistics/calorie-balance', { params: { year, month } }),
  weeklyExercise:  startDate    => request.get('/statistics/weekly-exercise', { params: { startDate } }),
}

// 管理员相关
export const adminApi = {
  userList:   params       => request.get('/admin/users', { params }),
  setStatus:  (id, status) => request.put(`/admin/users/${id}/status`, { status }),
  setAiLimit: (id, aiDailyLimit) => request.put(`/admin/users/${id}/ai-limit`, { aiDailyLimit }),
  exDictList: params       => request.get('/admin/exercise-dict', { params }),
  addExDict:  data         => request.post('/admin/exercise-dict', data),
  updateExDict:(id, data)  => request.put(`/admin/exercise-dict/${id}`, data),
  delExDict:  id           => request.delete(`/admin/exercise-dict/${id}`),
  foodList:   params       => request.get('/admin/food-dict', { params }),
  addFood:    data         => request.post('/admin/food-dict', data),
  updateFood: (id, data)   => request.put(`/admin/food-dict/${id}`, data),
  delFood:    id           => request.delete(`/admin/food-dict/${id}`),
}

// AI 健康助手相关
export const aiApi = {
  // 获取 AI 个性化建议
  getAdvice: data => request.post('/ai/advice', data),
  // 查询今日剩余次数
  getRemain: ()   => request.get('/ai/remain'),
}
