/**
 * 驗證工具函數
 */

/**
 * 驗證電子郵件格式
 * @param {string} email - 電子郵件
 * @returns {boolean}
 */
export function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 驗證手機號碼（台灣）
 * @param {string} phone - 手機號碼
 * @returns {boolean}
 */
export function isValidPhone(phone) {
  const phoneRegex = /^09\d{8}$/
  return phoneRegex.test(phone)
}

/**
 * 驗證必填欄位
 * @param {any} value - 值
 * @returns {boolean}
 */
export function isRequired(value) {
  if (value === null || value === undefined) return false
  if (typeof value === 'string') return value.trim().length > 0
  if (Array.isArray(value)) return value.length > 0
  return true
}

/**
 * 驗證字串長度範圍
 * @param {string} str - 字串
 * @param {number} min - 最小長度
 * @param {number} max - 最大長度
 * @returns {boolean}
 */
export function isValidLength(str, min, max) {
  if (typeof str !== 'string') return false
  const length = str.trim().length
  return length >= min && length <= max
}

/**
 * 驗證數字範圍
 * @param {number} num - 數字
 * @param {number} min - 最小值
 * @param {number} max - 最大值
 * @returns {boolean}
 */
export function isValidRange(num, min, max) {
  if (typeof num !== 'number' || isNaN(num)) return false
  return num >= min && num <= max
}

/**
 * 驗證 URL 格式
 * @param {string} url - URL
 * @returns {boolean}
 */
export function isValidUrl(url) {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

/**
 * 驗證密碼強度（至少包含英文和數字，長度 8-20）
 * @param {string} password - 密碼
 * @returns {boolean}
 */
export function isValidPassword(password) {
  if (typeof password !== 'string') return false
  const hasLetter = /[a-zA-Z]/.test(password)
  const hasNumber = /\d/.test(password)
  const validLength = password.length >= 8 && password.length <= 20
  return hasLetter && hasNumber && validLength
}

/**
 * 驗證正整數
 * @param {any} value - 值
 * @returns {boolean}
 */
export function isPositiveInteger(value) {
  const num = Number(value)
  return Number.isInteger(num) && num > 0
}

/**
 * 驗證正數（包含小數）
 * @param {any} value - 值
 * @returns {boolean}
 */
export function isPositiveNumber(value) {
  const num = Number(value)
  return !isNaN(num) && num > 0
}
