/**
 * 地址新增/編輯對話框元件
 */
import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  FormControlLabel,
  Checkbox,
  Stack,
  InputAdornment,
} from '@mui/material';
import { MdPerson, MdPhone, MdHome } from 'react-icons/md';

const AddressDialog = ({ open, onClose, onSave, editData }) => {
  const [formData, setFormData] = useState({
    recipientName: '',
    phone: '',
    address: '',
    isDefault: false,
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 編輯模式時載入資料
  useEffect(() => {
    if (editData) {
      setFormData({
        recipientName: editData.recipientName || '',
        phone: editData.phone || '',
        address: editData.address || '',
        isDefault: editData.isDefault || false,
      });
    } else {
      setFormData({
        recipientName: '',
        phone: '',
        address: '',
        isDefault: false,
      });
    }
    setErrors({});
  }, [editData, open]);

  // 表單驗證
  const validateForm = () => {
    const newErrors = {};

    if (!formData.recipientName || formData.recipientName.trim().length < 2) {
      newErrors.recipientName = '收件人姓名至少需要 2 個字元';
    }

    if (!formData.phone) {
      newErrors.phone = '請輸入收件人電話';
    } else if (!/^09\d{8}$/.test(formData.phone)) {
      newErrors.phone = '請輸入有效的手機號碼（例如：0912345678）';
    }

    if (!formData.address || formData.address.trim().length < 5) {
      newErrors.address = '收貨地址至少需要 5 個字元';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 處理儲存
  const handleSave = async () => {
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      await onSave(formData);
      handleClose();
    } catch (error) {
      // 錯誤由父元件處理
    } finally {
      setIsSubmitting(false);
    }
  };

  // 處理關閉
  const handleClose = () => {
    setFormData({
      recipientName: '',
      phone: '',
      address: '',
      isDefault: false,
    });
    setErrors({});
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>{editData ? '編輯收貨地址' : '新增收貨地址'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            fullWidth
            label="收件人姓名"
            value={formData.recipientName}
            onChange={(e) => setFormData({ ...formData, recipientName: e.target.value })}
            error={!!errors.recipientName}
            helperText={errors.recipientName}
            disabled={isSubmitting}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <MdPerson />
                </InputAdornment>
              ),
            }}
            required
          />

          <TextField
            fullWidth
            label="收件人電話"
            value={formData.phone}
            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            error={!!errors.phone}
            helperText={errors.phone || '請輸入手機號碼（例如：0912345678）'}
            disabled={isSubmitting}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <MdPhone />
                </InputAdornment>
              ),
            }}
            placeholder="0912345678"
            required
          />

          <TextField
            fullWidth
            label="收貨地址"
            value={formData.address}
            onChange={(e) => setFormData({ ...formData, address: e.target.value })}
            error={!!errors.address}
            helperText={errors.address}
            disabled={isSubmitting}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <MdHome />
                </InputAdornment>
              ),
            }}
            placeholder="請輸入完整地址"
            multiline
            rows={3}
            required
          />

          <FormControlLabel
            control={
              <Checkbox
                checked={formData.isDefault}
                onChange={(e) => setFormData({ ...formData, isDefault: e.target.checked })}
                disabled={isSubmitting}
              />
            }
            label="設為預設地址"
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={isSubmitting}>
          取消
        </Button>
        <Button onClick={handleSave} variant="contained" disabled={isSubmitting}>
          {isSubmitting ? '儲存中...' : '儲存'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddressDialog;
