/**
 * 會員中心主頁面
 * 使用 MUI v6 元件，整合 Zustand authStore
 */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Card,
  CardContent,
  Avatar,
  Typography,
  Stack,
  Table,
  TableBody,
  TableRow,
  TableCell,
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Snackbar,
  Alert,
  Divider,
  InputAdornment,
  IconButton,
} from '@mui/material';
import {
  MdPerson,
  MdEdit,
  MdLock,
  MdShoppingBag,
  MdFavorite,
  MdLogout,
  MdWarning,
  MdVisibility,
  MdVisibilityOff,
  MdEmail,
  MdPhone,
  MdHome,
} from 'react-icons/md';
import useAuthStore from '../../stores/authStore';
import { memberService } from '../../services/memberService';
import styles from './Member.module.css';

const Member = () => {
  const navigate = useNavigate();
  const { user, logout, updateUser, refreshUser } = useAuthStore();
  const [activeTab, setActiveTab] = useState('profile');
  const [isEditing, setIsEditing] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  // Snackbar state
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  // Logout confirmation dialog
  const [logoutDialogOpen, setLogoutDialogOpen] = useState(false);

  // Avatar upload state
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);

  // Edit form state
  const [editData, setEditData] = useState({
    name: user?.name || '',
    email: user?.email || '',
    phone: user?.phone || '',
    homeAddress: user?.homeAddress || '',
  });
  const [editErrors, setEditErrors] = useState({});

  // Password form state
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [passwordErrors, setPasswordErrors] = useState({});
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // 處理登出
  const handleLogout = () => {
    setLogoutDialogOpen(true);
  };

  const confirmLogout = () => {
    logout();
    setSnackbar({ open: true, message: '已成功登出', severity: 'success' });
    navigate('/');
  };

  // 處理頭像上傳
  const handleAvatarUpload = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // 驗證檔案類型
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
    if (!allowedTypes.includes(file.type)) {
      setSnackbar({
        open: true,
        message: '僅支援 JPG、PNG 或 GIF 格式的圖片',
        severity: 'error',
      });
      return;
    }

    // 驗證檔案大小（5MB）
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
      setSnackbar({
        open: true,
        message: '圖片大小不能超過 5MB',
        severity: 'error',
      });
      return;
    }

    setIsUploadingAvatar(true);
    try {
      const response = await memberService.uploadAvatar(file);

      if (response.success) {
        // 更新使用者資料
        await refreshUser();
        setSnackbar({
          open: true,
          message: '頭像上傳成功！',
          severity: 'success',
        });
      } else {
        setSnackbar({
          open: true,
          message: response.message || '頭像上傳失敗',
          severity: 'error',
        });
      }
    } catch (error) {
      setSnackbar({
        open: true,
        message: error.response?.data?.message || '頭像上傳失敗，請稍後再試',
        severity: 'error',
      });
    } finally {
      setIsUploadingAvatar(false);
      // 清空 input 值，允許重複上傳相同檔案
      event.target.value = '';
    }
  };

  // 處理資料更新
  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    const errors = {};

    if (!editData.name || editData.name.length < 2) {
      errors.name = '姓名至少需要 2 個字元';
    }

    if (editData.phone && !/^09\d{8}$/.test(editData.phone)) {
      errors.phone = '請輸入有效的手機號碼（例如：0912345678）';
    }

    if (Object.keys(errors).length > 0) {
      setEditErrors(errors);
      return;
    }

    setIsEditing(true);
    try {
      // 呼叫後端 API 更新資料
      const response = await memberService.updateProfile({
        name: editData.name,
        phone: editData.phone,
        homeAddress: editData.homeAddress,
      });

      if (response.success) {
        // 更新 store 中的使用者資料
        updateUser({
          name: editData.name,
          phone: editData.phone,
          homeAddress: editData.homeAddress,
        });
        setSnackbar({ open: true, message: '資料更新成功！', severity: 'success' });
        setEditErrors({});
        setActiveTab('profile');
      } else {
        setSnackbar({ open: true, message: response.message || '更新失敗', severity: 'error' });
      }
    } catch (error) {
      setSnackbar({
        open: true,
        message: error.response?.data?.message || '更新失敗，請稍後再試',
        severity: 'error',
      });
    } finally {
      setIsEditing(false);
    }
  };

  // 處理密碼更新
  const handleChangePassword = async (e) => {
    e.preventDefault();
    const errors = {};

    if (!passwordData.currentPassword) {
      errors.currentPassword = '請輸入目前密碼';
    }
    if (!passwordData.newPassword || passwordData.newPassword.length < 6) {
      errors.newPassword = '密碼至少需要 6 個字元';
    }
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      errors.confirmPassword = '兩次輸入的密碼不一致';
    }

    if (Object.keys(errors).length > 0) {
      setPasswordErrors(errors);
      return;
    }

    setIsChangingPassword(true);
    try {
      // 呼叫後端 API 修改密碼
      const response = await memberService.changePassword({
        oldPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });

      if (response.success) {
        setSnackbar({ open: true, message: '密碼更新成功！', severity: 'success' });
        setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
        setPasswordErrors({});
        setActiveTab('profile');
      } else {
        setSnackbar({ open: true, message: response.message || '密碼更新失敗', severity: 'error' });
      }
    } catch (error) {
      setSnackbar({
        open: true,
        message: error.response?.data?.message || '密碼更新失敗，請稍後再試',
        severity: 'error',
      });
    } finally {
      setIsChangingPassword(false);
    }
  };

  // 選單項目
  const menuItems = [
    { key: 'profile', icon: <MdPerson />, label: '個人資料', disabled: false },
    { key: 'edit', icon: <MdEdit />, label: '編輯資料', disabled: false },
    { key: 'password', icon: <MdLock />, label: '修改密碼', disabled: false },
    { key: 'orders', icon: <MdShoppingBag />, label: '訂單紀錄', disabled: true },
    { key: 'favorites', icon: <MdFavorite />, label: '我的收藏', disabled: true },
  ];

  // 處理選單點擊
  const handleMenuClick = (key) => {
    if (key === 'logout') {
      handleLogout();
    } else {
      setActiveTab(key);
    }
  };

  // 渲染內容
  const renderContent = () => {
    switch (activeTab) {
      case 'profile':
        return (
          <Card>
            <CardContent>
              <Typography variant="h5" gutterBottom>個人資料</Typography>
              <Table>
                <TableBody>
                  <TableRow>
                    <TableCell component="th" scope="row" sx={{ fontWeight: 600, width: '30%' }}>
                      姓名
                    </TableCell>
                    <TableCell>{user?.name || '-'}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" scope="row" sx={{ fontWeight: 600 }}>
                      Email
                    </TableCell>
                    <TableCell>{user?.email || '-'}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" scope="row" sx={{ fontWeight: 600 }}>
                      電話
                    </TableCell>
                    <TableCell>{user?.phone || '-'}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" scope="row" sx={{ fontWeight: 600 }}>
                      住家地址
                    </TableCell>
                    <TableCell>{user?.homeAddress || '-'}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" scope="row" sx={{ fontWeight: 600 }}>
                      Email 驗證狀態
                    </TableCell>
                    <TableCell>
                      {user?.emailVerified ? (
                        <Typography color="success.main">已驗證</Typography>
                      ) : (
                        <Typography color="warning.main">未驗證</Typography>
                      )}
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        );

      case 'edit':
        return (
          <Card>
            <CardContent>
              <Typography variant="h5" gutterBottom>編輯資料</Typography>
              <Box component="form" onSubmit={handleUpdateProfile} sx={{ mt: 2 }}>
                <Stack spacing={3}>
                  <TextField
                    label="姓名"
                    value={editData.name}
                    onChange={(e) => setEditData({ ...editData, name: e.target.value })}
                    error={Boolean(editErrors.name)}
                    helperText={editErrors.name}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <MdPerson />
                        </InputAdornment>
                      ),
                    }}
                    fullWidth
                    required
                  />

                  <TextField
                    label="Email"
                    value={editData.email}
                    disabled
                    helperText="Email 無法修改"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <MdEmail />
                        </InputAdornment>
                      ),
                    }}
                    fullWidth
                  />

                  <TextField
                    label="電話"
                    value={editData.phone}
                    onChange={(e) => setEditData({ ...editData, phone: e.target.value })}
                    error={Boolean(editErrors.phone)}
                    helperText={editErrors.phone || '請輸入手機號碼（例如：0912345678）'}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <MdPhone />
                        </InputAdornment>
                      ),
                    }}
                    placeholder="0912345678"
                    fullWidth
                  />

                  <TextField
                    label="住家地址"
                    value={editData.homeAddress}
                    onChange={(e) => setEditData({ ...editData, homeAddress: e.target.value })}
                    error={Boolean(editErrors.homeAddress)}
                    helperText={editErrors.homeAddress}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <MdHome />
                        </InputAdornment>
                      ),
                    }}
                    placeholder="請輸入完整地址"
                    fullWidth
                    multiline
                    rows={2}
                  />

                  <Stack direction="row" spacing={2}>
                    <Button
                      type="submit"
                      variant="contained"
                      disabled={isEditing}
                    >
                      {isEditing ? '儲存中...' : '儲存'}
                    </Button>
                    <Button variant="outlined" onClick={() => setActiveTab('profile')}>
                      取消
                    </Button>
                  </Stack>
                </Stack>
              </Box>
            </CardContent>
          </Card>
        );

      case 'password':
        return (
          <Card>
            <CardContent>
              <Typography variant="h5" gutterBottom>修改密碼</Typography>
              <Box component="form" onSubmit={handleChangePassword} sx={{ mt: 2 }}>
                <Stack spacing={3}>
                  <TextField
                    label="目前密碼"
                    type={showCurrentPassword ? 'text' : 'password'}
                    value={passwordData.currentPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
                    error={Boolean(passwordErrors.currentPassword)}
                    helperText={passwordErrors.currentPassword}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <MdLock />
                        </InputAdornment>
                      ),
                      endAdornment: (
                        <InputAdornment position="end">
                          <IconButton onClick={() => setShowCurrentPassword(!showCurrentPassword)}>
                            {showCurrentPassword ? <MdVisibilityOff /> : <MdVisibility />}
                          </IconButton>
                        </InputAdornment>
                      ),
                    }}
                    fullWidth
                  />

                  <TextField
                    label="新密碼"
                    type={showNewPassword ? 'text' : 'password'}
                    value={passwordData.newPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                    error={Boolean(passwordErrors.newPassword)}
                    helperText={passwordErrors.newPassword}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <MdLock />
                        </InputAdornment>
                      ),
                      endAdornment: (
                        <InputAdornment position="end">
                          <IconButton onClick={() => setShowNewPassword(!showNewPassword)}>
                            {showNewPassword ? <MdVisibilityOff /> : <MdVisibility />}
                          </IconButton>
                        </InputAdornment>
                      ),
                    }}
                    fullWidth
                  />

                  <TextField
                    label="確認新密碼"
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={passwordData.confirmPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
                    error={Boolean(passwordErrors.confirmPassword)}
                    helperText={passwordErrors.confirmPassword}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <MdLock />
                        </InputAdornment>
                      ),
                      endAdornment: (
                        <InputAdornment position="end">
                          <IconButton onClick={() => setShowConfirmPassword(!showConfirmPassword)}>
                            {showConfirmPassword ? <MdVisibilityOff /> : <MdVisibility />}
                          </IconButton>
                        </InputAdornment>
                      ),
                    }}
                    fullWidth
                  />

                  <Stack direction="row" spacing={2}>
                    <Button
                      type="submit"
                      variant="contained"
                      disabled={isChangingPassword}
                    >
                      更新密碼
                    </Button>
                    <Button
                      variant="outlined"
                      onClick={() => {
                        setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
                        setPasswordErrors({});
                        setActiveTab('profile');
                      }}
                    >
                      取消
                    </Button>
                  </Stack>
                </Stack>
              </Box>
            </CardContent>
          </Card>
        );

      case 'orders':
        return (
          <Card>
            <CardContent>
              <Typography variant="h5" gutterBottom>訂單紀錄</Typography>
              <Typography color="text.secondary">訂單功能開發中...</Typography>
            </CardContent>
          </Card>
        );

      case 'favorites':
        return (
          <Card>
            <CardContent>
              <Typography variant="h5" gutterBottom>我的收藏</Typography>
              <Typography color="text.secondary">收藏功能開發中...</Typography>
            </CardContent>
          </Card>
        );

      default:
        return null;
    }
  };

  return (
    <Box className={styles.memberPage}>
      <Box className={styles.container}>
        <Box sx={{ display: 'flex', gap: 3, py: 3 }}>
          {/* 左側選單 */}
          <Box
            className={styles.sider}
            sx={{
              width: 250,
              flexShrink: 0,
              display: { xs: 'none', md: 'block' },
            }}
          >
            <Card>
              <CardContent>
                <Stack spacing={2} alignItems="center" sx={{ mb: 3 }}>
                  <Box sx={{ position: 'relative' }}>
                    <Avatar
                      src={user?.id ? memberService.getAvatarUrl(user.id) : undefined}
                      sx={{ width: 80, height: 80 }}
                    >
                      <MdPerson size={48} />
                    </Avatar>
                    <input
                      accept="image/jpeg,image/png,image/gif"
                      style={{ display: 'none' }}
                      id="avatar-upload"
                      type="file"
                      onChange={handleAvatarUpload}
                      disabled={isUploadingAvatar}
                    />
                    <label htmlFor="avatar-upload">
                      <IconButton
                        component="span"
                        size="small"
                        disabled={isUploadingAvatar}
                        sx={{
                          position: 'absolute',
                          bottom: 0,
                          right: 0,
                          backgroundColor: 'primary.main',
                          color: 'white',
                          '&:hover': {
                            backgroundColor: 'primary.dark',
                          },
                        }}
                      >
                        <MdEdit size={16} />
                      </IconButton>
                    </label>
                  </Box>
                  <Typography variant="h6">
                    {user?.name || '會員'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {user?.email}
                  </Typography>
                </Stack>

                <List className={styles.menu}>
                  {menuItems.map((item) => (
                    <ListItemButton
                      key={item.key}
                      selected={activeTab === item.key}
                      onClick={() => handleMenuClick(item.key)}
                      disabled={item.disabled}
                    >
                      <ListItemIcon sx={{ minWidth: 40 }}>
                        {item.icon}
                      </ListItemIcon>
                      <ListItemText primary={item.label} />
                    </ListItemButton>
                  ))}
                  <Divider sx={{ my: 1 }} />
                  <ListItemButton onClick={handleLogout} sx={{ color: 'error.main' }}>
                    <ListItemIcon sx={{ minWidth: 40, color: 'error.main' }}>
                      <MdLogout />
                    </ListItemIcon>
                    <ListItemText primary="登出" />
                  </ListItemButton>
                </List>
              </CardContent>
            </Card>
          </Box>

          {/* 右側內容 */}
          <Box className={styles.content} sx={{ flex: 1, minWidth: 0 }}>
            {renderContent()}
          </Box>
        </Box>
      </Box>

      {/* 登出確認對話框 */}
      <Dialog open={logoutDialogOpen} onClose={() => setLogoutDialogOpen(false)}>
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <MdWarning color="#ed6c02" />
            確認登出
          </Box>
        </DialogTitle>
        <DialogContent>
          <Typography>您確定要登出嗎？</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setLogoutDialogOpen(false)}>取消</Button>
          <Button onClick={confirmLogout} variant="contained" color="error">
            確定
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default Member;
