function isObject(value) {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
}

function mapAdviceFields(payload) {
  if (!isObject(payload)) return null

  const adviceType = payload.adviceType ?? payload.advice_type ?? 'GENERAL'
  const content = payload.content ?? ''
  const summary = payload.summary ?? null
  const remainingDaily = payload.remainingDaily ?? payload.remaining_daily ?? null
  const generatedAt = payload.generatedAt ?? payload.generated_at ?? null
  const debug = payload.debug ?? null

  if (!content && !summary && !debug) return null

  return {
    ...payload,
    adviceType,
    content,
    summary,
    remainingDaily,
    generatedAt,
    debug,
  }
}

export function normalizeAiAdviceResponse(response) {
  if (!isObject(response)) return null

  const direct = mapAdviceFields(response)
  if (direct) return direct

  const firstLevel = response.data
  const fromFirstLevel = mapAdviceFields(firstLevel)
  if (fromFirstLevel) return fromFirstLevel

  if (isObject(firstLevel) && isObject(firstLevel.data)) {
    return mapAdviceFields(firstLevel.data)
  }

  return null
}
