/**
 * 格式化工具函數
 */

/**
 * 格式化金額
 * @param {number} amount - 金額
 * @param {string} currency - 貨幣符號（預設為 NT$）
 * @returns {string} 格式化後的金額字串
 */
export function formatCurrency(amount, currency = 'NT$') {
  if (typeof amount !== 'number' || isNaN(amount)) {
    return `${currency} 0`
  }
  return `${currency} ${amount.toLocaleString('zh-TW')}`
}

/**
 * 格式化日期時間
 * @param {string|Date} date - 日期
 * @param {boolean} includeTime - 是否包含時間（預設為 true）
 * @returns {string} 格式化後的日期字串
 */
export function formatDateTime(date, includeTime = true) {
  if (!date) return '-'

  const dateObj = typeof date === 'string' ? new Date(date) : date

  if (isNaN(dateObj.getTime())) {
    return '-'
  }

  const options = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    ...(includeTime && {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  }

  return dateObj.toLocaleString('zh-TW', options)
}

/**
 * 格式化數字（加上千分位）
 * @param {number} num - 數字
 * @returns {string} 格式化後的數字字串
 */
export function formatNumber(num) {
  if (typeof num !== 'number' || isNaN(num)) {
    return '0'
  }
  return num.toLocaleString('zh-TW')
}

/**
 * 截斷文字
 * @param {string} text - 文字
 * @param {number} maxLength - 最大長度
 * @param {string} suffix - 後綴（預設為 ...）
 * @returns {string} 截斷後的文字
 */
export function truncateText(text, maxLength, suffix = '...') {
  if (!text || text.length <= maxLength) {
    return text
  }
  return text.substring(0, maxLength) + suffix
}

/**
 * 格式化檔案大小
 * @param {number} bytes - 位元組數
 * @returns {string} 格式化後的檔案大小
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes'

  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}
