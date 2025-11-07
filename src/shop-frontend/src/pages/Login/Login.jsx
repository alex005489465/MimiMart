/**
 * 會員登入/註冊頁面
 * 使用 Ant Design Form 和 Tabs，整合 Zustand authStore
 */
import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Form, Input, Button, Checkbox, Tabs, Card, Typography, Alert, message } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import useAuthStore from '../../stores/authStore';
import styles from './Login.module.css';

const { Title, Text, Link } = Typography;

const Login = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login, register, isAuthenticated } = useAuthStore();

  const [loginForm] = Form.useForm();
  const [registerForm] = Form.useForm();
  const [activeTab, setActiveTab] = useState('login');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // 從 URL 參數讀取 tab（例如：/login?tab=register）
  useEffect(() => {
    const tab = searchParams.get('tab');
    if (tab === 'register') {
      setActiveTab('register');
    }
  }, [searchParams]);

  // 如果已登入，導向首頁
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  // 處理登入提交
  const handleLogin = async (values) => {
    setIsSubmitting(true);
    setErrorMessage('');

    const result = await login({
      email: values.email,
      password: values.password,
    });

    setIsSubmitting(false);

    if (result.success) {
      message.success('登入成功！');
      navigate('/');
    } else {
      setErrorMessage(result.error || '登入失敗，請檢查帳號密碼');
    }
  };

  // 處理註冊提交
  const handleRegister = async (values) => {
    setIsSubmitting(true);
    setErrorMessage('');

    const result = await register({
      username: values.username,
      email: values.email,
      password: values.password,
    });

    setIsSubmitting(false);

    if (result.success) {
      message.success('註冊成功！');
      navigate('/');
    } else {
      setErrorMessage(result.error || '註冊失敗，請稍後再試');
    }
  };

  // 登入表單
  const LoginForm = (
    <Form
      form={loginForm}
      name="login"
      onFinish={handleLogin}
      autoComplete="off"
      size="large"
      layout="vertical"
    >
      {errorMessage && (
        <Alert
          message={errorMessage}
          type="error"
          showIcon
          closable
          onClose={() => setErrorMessage('')}
          style={{ marginBottom: 24 }}
        />
      )}

      <Form.Item
        name="email"
        label="Email"
        rules={[
          { required: true, message: '請輸入 Email' },
          { type: 'email', message: '請輸入有效的 Email' },
        ]}
      >
        <Input
          prefix={<MailOutlined />}
          placeholder="請輸入您的 Email"
          disabled={isSubmitting}
        />
      </Form.Item>

      <Form.Item
        name="password"
        label="密碼"
        rules={[
          { required: true, message: '請輸入密碼' },
          { min: 6, message: '密碼至少需要 6 個字元' },
        ]}
      >
        <Input.Password
          prefix={<LockOutlined />}
          placeholder="請輸入您的密碼"
          disabled={isSubmitting}
        />
      </Form.Item>

      <Form.Item>
        <div className={styles.formOptions}>
          <Form.Item name="remember" valuePropName="checked" noStyle>
            <Checkbox disabled={isSubmitting}>記住我</Checkbox>
          </Form.Item>
          <Link onClick={() => message.info('忘記密碼功能開發中')}>
            忘記密碼？
          </Link>
        </div>
      </Form.Item>

      <Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          loading={isSubmitting}
          block
          size="large"
        >
          登入
        </Button>
      </Form.Item>
    </Form>
  );

  // 註冊表單
  const RegisterForm = (
    <Form
      form={registerForm}
      name="register"
      onFinish={handleRegister}
      autoComplete="off"
      size="large"
      layout="vertical"
    >
      {errorMessage && (
        <Alert
          message={errorMessage}
          type="error"
          showIcon
          closable
          onClose={() => setErrorMessage('')}
          style={{ marginBottom: 24 }}
        />
      )}

      <Form.Item
        name="username"
        label="使用者名稱"
        rules={[
          { required: true, message: '請輸入使用者名稱' },
          { min: 2, message: '使用者名稱至少需要 2 個字元' },
          { max: 20, message: '使用者名稱不能超過 20 個字元' },
        ]}
      >
        <Input
          prefix={<UserOutlined />}
          placeholder="請輸入使用者名稱"
          disabled={isSubmitting}
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
        <Input
          prefix={<MailOutlined />}
          placeholder="請輸入您的 Email"
          disabled={isSubmitting}
        />
      </Form.Item>

      <Form.Item
        name="password"
        label="密碼"
        rules={[
          { required: true, message: '請輸入密碼' },
          { min: 6, message: '密碼至少需要 6 個字元' },
          {
            pattern: /^(?=.*[a-zA-Z])(?=.*\d)/,
            message: '密碼必須包含英文字母和數字',
          },
        ]}
      >
        <Input.Password
          prefix={<LockOutlined />}
          placeholder="請輸入密碼（至少 6 位，需包含英文和數字）"
          disabled={isSubmitting}
        />
      </Form.Item>

      <Form.Item
        name="confirmPassword"
        label="確認密碼"
        dependencies={['password']}
        rules={[
          { required: true, message: '請再次輸入密碼' },
          ({ getFieldValue }) => ({
            validator(_, value) {
              if (!value || getFieldValue('password') === value) {
                return Promise.resolve();
              }
              return Promise.reject(new Error('兩次輸入的密碼不一致'));
            },
          }),
        ]}
      >
        <Input.Password
          prefix={<LockOutlined />}
          placeholder="請再次輸入密碼"
          disabled={isSubmitting}
        />
      </Form.Item>

      <Form.Item
        name="agreement"
        valuePropName="checked"
        rules={[
          {
            validator: (_, value) =>
              value
                ? Promise.resolve()
                : Promise.reject(new Error('請閱讀並同意服務條款')),
          },
        ]}
      >
        <Checkbox disabled={isSubmitting}>
          我已閱讀並同意{' '}
          <Link onClick={() => message.info('服務條款頁面開發中')}>
            服務條款
          </Link>{' '}
          和{' '}
          <Link onClick={() => message.info('隱私權政策頁面開發中')}>
            隱私權政策
          </Link>
        </Checkbox>
      </Form.Item>

      <Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          loading={isSubmitting}
          block
          size="large"
        >
          註冊
        </Button>
      </Form.Item>
    </Form>
  );

  const tabItems = [
    {
      key: 'login',
      label: '登入',
      children: LoginForm,
    },
    {
      key: 'register',
      label: '註冊',
      children: RegisterForm,
    },
  ];

  return (
    <div className={styles.loginPage}>
      <div className={styles.container}>
        <Card className={styles.loginCard}>
          <div className={styles.header}>
            <Title level={2} style={{ marginBottom: 8 }}>
              歡迎來到 MimiMart
            </Title>
            <Text type="secondary">請登入您的帳號或註冊新帳號</Text>
          </div>

          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            items={tabItems}
            centered
            size="large"
          />
        </Card>
      </div>
    </div>
  );
};

export default Login;
