import test from 'node:test'
import assert from 'node:assert/strict'

function resolveErrorMessage(err) {
  if (err?.response?.status === 401) return '登录已过期，请重新登录'
  if (err?.code === 'ECONNABORTED' || err?.message?.toLowerCase().includes('timeout')) {
    return 'AI 响应超时，请稍后重试'
  }
  return err?.response?.data?.message || '网络错误'
}

test('shows timeout message for axios timeout errors', () => {
  assert.equal(
    resolveErrorMessage({ code: 'ECONNABORTED', message: 'timeout of 50000ms exceeded' }),
    'AI 响应超时，请稍后重试'
  )
})

test('falls back to backend message for non-timeout errors', () => {
  assert.equal(
    resolveErrorMessage({ response: { data: { message: 'AI 服务不可用' } } }),
    'AI 服务不可用'
  )
})
