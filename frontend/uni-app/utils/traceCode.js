/**
 * 从扫码结果中解析批次溯源码
 * 支持:
 *   WM202610310001
 *   http(s)://host/#/pages/consumer/trace?code=WM202610310001
 *   pages/consumer/trace?code=WM202610310001
 *   https://host/t/WM202610310001
 */
const CODE_RE = /^[A-Za-z0-9]{4,32}$/

export function parseTraceCode(raw) {
  if (!raw) return ''
  const s = String(raw).trim()
  if (CODE_RE.test(s)) return s.toUpperCase()
  const q = s.match(/[?&]code=([A-Za-z0-9]{4,32})/)
  if (q) return q[1].toUpperCase()
  const t = s.match(/\/t\/([A-Za-z0-9]{4,32})(?:[/?#]|$)/)
  if (t) return t[1].toUpperCase()
  return ''
}
