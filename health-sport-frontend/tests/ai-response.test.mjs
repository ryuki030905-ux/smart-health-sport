import test from 'node:test'
import assert from 'node:assert/strict'

import { normalizeAiAdviceResponse } from '../src/views/ai/ai-response.mjs'

test('extracts advice payload from wrapped backend result', () => {
  const normalized = normalizeAiAdviceResponse({
    code: 200,
    message: '操作成功',
    data: {
      adviceType: 'GENERAL',
      content: '保持规律作息',
      summary: '今天状态稳定',
      remainingDaily: 2,
    },
  })

  assert.equal(normalized.content, '保持规律作息')
  assert.equal(normalized.summary, '今天状态稳定')
  assert.equal(normalized.remainingDaily, 2)
})

test('extracts advice payload from doubly nested response shape', () => {
  const normalized = normalizeAiAdviceResponse({
    data: {
      code: 200,
      message: '操作成功',
      data: {
        adviceType: 'DIET',
        content: '早餐增加蛋白质',
        summary: '饮食结构可优化',
        remainingDaily: 1,
      },
    },
  })

  assert.equal(normalized.adviceType, 'DIET')
  assert.equal(normalized.content, '早餐增加蛋白质')
  assert.equal(normalized.remainingDaily, 1)
})

test('maps python snake_case fields to frontend camelCase fields', () => {
  const normalized = normalizeAiAdviceResponse({
    advice_type: 'EXERCISE',
    content: '今天适合低强度训练',
    summary: '控制强度',
    remaining_daily: 0,
    generated_at: '2026-03-24T10:00:00',
  })

  assert.equal(normalized.adviceType, 'EXERCISE')
  assert.equal(normalized.remainingDaily, 0)
  assert.equal(normalized.generatedAt, '2026-03-24T10:00:00')
})

test('returns null for empty payloads', () => {
  assert.equal(normalizeAiAdviceResponse(null), null)
  assert.equal(normalizeAiAdviceResponse({ data: null }), null)
})
