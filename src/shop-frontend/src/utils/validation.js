/**
 * 表單驗證工具函式
 */

export const validation = {
  // Email 格式驗證
  isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  },

  // 密碼強度驗證 (至少 6 個字元)
  isValidPassword(password) {
    return password && password.length >= 6 && password.length <= 50;
  },

  // 必填欄位驗證
  isRequired(value) {
    return value && value.trim().length > 0;
  },

  // 姓名驗證 (最長 100 字元)
  isValidName(name) {
    return name && name.trim().length > 0 && name.length <= 100;
  },

  // 手機號碼驗證 (台灣手機格式: 09 開頭 10 碼)
  isValidPhone(phone) {
    if (!phone) return true; // 手機號碼為選填
    const phoneRegex = /^09\d{8}$/;
    return phoneRegex.test(phone);
  },

  // 確認密碼驗證
  isPasswordMatch(password, confirmPassword) {
    return password === confirmPassword;
  },

  // Email 錯誤訊息
  getEmailError(email) {
    if (!this.isRequired(email)) {
      return 'Email 為必填欄位';
    }
    if (!this.isValidEmail(email)) {
      return 'Email 格式不正確';
    }
    return '';
  },

  // 密碼錯誤訊息
  getPasswordError(password) {
    if (!this.isRequired(password)) {
      return '密碼為必填欄位';
    }
    if (!this.isValidPassword(password)) {
      return '密碼長度必須為 6-50 個字元';
    }
    return '';
  },

  // 確認密碼錯誤訊息
  getConfirmPasswordError(password, confirmPassword) {
    if (!this.isRequired(confirmPassword)) {
      return '請再次輸入密碼';
    }
    if (!this.isPasswordMatch(password, confirmPassword)) {
      return '兩次輸入的密碼不一致';
    }
    return '';
  },

  // 姓名錯誤訊息
  getNameError(name) {
    if (!this.isRequired(name)) {
      return '姓名為必填欄位';
    }
    if (name.length > 100) {
      return '姓名長度不可超過 100 個字元';
    }
    return '';
  },

  // 手機號碼錯誤訊息
  getPhoneError(phone) {
    if (phone && !this.isValidPhone(phone)) {
      return '手機號碼格式不正確 (請輸入 09 開頭的 10 碼數字)';
    }
    return '';
  },
};
