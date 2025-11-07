/**
 * 會員中心主頁面
 * 使用 Ant Design Layout 和 Menu，整合 Zustand authStore
 */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Layout,
  Menu,
  Card,
  Avatar,
  Typography,
  Space,
  Descriptions,
  Button,
  Form,
  Input,
  message,
  Modal,
} from 'antd';
import {
  UserOutlined,
  EditOutlined,
  LockOutlined,
  ShoppingOutlined,
  HeartOutlined,
  LogoutOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import useAuthStore from '../../stores/authStore';
import styles from './Member.module.css';

const { Sider, Content } = Layout;
const { Title, Text } = Typography;

const Member = () => {
  const navigate = useNavigate();
  const { user, logout, updateUser } = useAuthStore();
  const [activeTab, setActiveTab] = useState('profile');
  const [editForm] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [isEditing, setIsEditing] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  // 處理登出
  const handleLogout = () => {
    Modal.confirm({
      title: '確認登出',
      icon: <ExclamationCircleOutlined />,
      content: '您確定要登出嗎？',
      okText: '確定',
      cancelText: '取消',
      onOk: () => {
        logout();
        message.success('已成功登出');
        navigate('/');
      },
    });
  };

  // 處理資料更新
  const handleUpdateProfile = async (values) => {
    setIsEditing(true);
    // 這裡應該呼叫 API 更新使用者資料
    // 暫時只更新本地狀態
    updateUser(values);
    message.success('資料更新成功！');
    setIsEditing(false);
    setActiveTab('profile');
  };

  // 處理密碼更新
  const handleChangePassword = async (values) => {
    setIsChangingPassword(true);
    // 這裡應該呼叫 API 更新密碼
    // 暫時只顯示成功訊息
    message.success('密碼更新成功！');
    setIsChangingPassword(false);
    passwordForm.resetFields();
    setActiveTab('profile');
  };

  // 選單項目
  const menuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '個人資料',
    },
    {
      key: 'edit',
      icon: <EditOutlined />,
      label: '編輯資料',
    },
    {
      key: 'password',
      icon: <LockOutlined />,
      label: '修改密碼',
    },
    {
      key: 'orders',
      icon: <ShoppingOutlined />,
      label: '訂單紀錄',
      disabled: true, // 待實作
    },
    {
      key: 'favorites',
      icon: <HeartOutlined />,
      label: '我的收藏',
      disabled: true, // 待實作
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '登出',
      danger: true,
    },
  ];

  // 處理選單點擊
  const handleMenuClick = ({ key }) => {
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
          <Card title="個人資料" bordered={false}>
            <Descriptions column={1} labelStyle={{ fontWeight: 600 }}>
              <Descriptions.Item label="使用者名稱">
                {user?.username || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Email">
                {user?.email || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="註冊時間">
                {user?.createdAt
                  ? new Date(user.createdAt).toLocaleDateString('zh-TW')
                  : '-'}
              </Descriptions.Item>
            </Descriptions>
          </Card>
        );

      case 'edit':
        return (
          <Card title="編輯資料" bordered={false}>
            <Form
              form={editForm}
              layout="vertical"
              initialValues={{
                username: user?.username,
                email: user?.email,
              }}
              onFinish={handleUpdateProfile}
            >
              <Form.Item
                name="username"
                label="使用者名稱"
                rules={[
                  { required: true, message: '請輸入使用者名稱' },
                  { min: 2, message: '至少需要 2 個字元' },
                ]}
              >
                <Input
                  prefix={<UserOutlined />}
                  placeholder="請輸入使用者名稱"
                />
              </Form.Item>

              <Form.Item
                name="email"
                label="Email"
                rules={[
                  { required: true, message: '請輸入 Email' },
                  { type: 'email', message: '請輸入有效的 Email' },
                ]}
              >
                <Input prefix={<UserOutlined />} placeholder="請輸入 Email" disabled />
              </Form.Item>

              <Form.Item>
                <Space>
                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={isEditing}
                  >
                    儲存
                  </Button>
                  <Button onClick={() => setActiveTab('profile')}>取消</Button>
                </Space>
              </Form.Item>
            </Form>
          </Card>
        );

      case 'password':
        return (
          <Card title="修改密碼" bordered={false}>
            <Form
              form={passwordForm}
              layout="vertical"
              onFinish={handleChangePassword}
            >
              <Form.Item
                name="currentPassword"
                label="目前密碼"
                rules={[{ required: true, message: '請輸入目前密碼' }]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="請輸入目前密碼"
                />
              </Form.Item>

              <Form.Item
                name="newPassword"
                label="新密碼"
                rules={[
                  { required: true, message: '請輸入新密碼' },
                  { min: 6, message: '密碼至少需要 6 個字元' },
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="請輸入新密碼"
                />
              </Form.Item>

              <Form.Item
                name="confirmPassword"
                label="確認新密碼"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: '請再次輸入新密碼' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('newPassword') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(
                        new Error('兩次輸入的密碼不一致')
                      );
                    },
                  }),
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="請再次輸入新密碼"
                />
              </Form.Item>

              <Form.Item>
                <Space>
                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={isChangingPassword}
                  >
                    更新密碼
                  </Button>
                  <Button
                    onClick={() => {
                      passwordForm.resetFields();
                      setActiveTab('profile');
                    }}
                  >
                    取消
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Card>
        );

      case 'orders':
        return (
          <Card title="訂單紀錄" bordered={false}>
            <Text type="secondary">訂單功能開發中...</Text>
          </Card>
        );

      case 'favorites':
        return (
          <Card title="我的收藏" bordered={false}>
            <Text type="secondary">收藏功能開發中...</Text>
          </Card>
        );

      default:
        return null;
    }
  };

  return (
    <div className={styles.memberPage}>
      <div className={styles.container}>
        <Layout className={styles.layout}>
          {/* 左側選單 */}
          <Sider
            width={250}
            className={styles.sider}
            breakpoint="md"
            collapsedWidth="0"
          >
            <div className={styles.userInfo}>
              <Avatar size={80} icon={<UserOutlined />} />
              <Title level={4} style={{ marginTop: 16, marginBottom: 4 }}>
                {user?.username || '會員'}
              </Title>
              <Text type="secondary">{user?.email}</Text>
            </div>

            <Menu
              mode="inline"
              selectedKeys={[activeTab]}
              items={menuItems}
              onClick={handleMenuClick}
              className={styles.menu}
            />
          </Sider>

          {/* 右側內容 */}
          <Content className={styles.content}>{renderContent()}</Content>
        </Layout>
      </div>
    </div>
  );
};

export default Member;
