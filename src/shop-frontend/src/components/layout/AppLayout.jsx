/**
 * 應用主要佈局元件
 * 使用 Ant Design Layout 組件
 */
import { Layout } from 'antd';
import { Outlet } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';

const { Content } = Layout;

const AppLayout = () => {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header />
      <Content style={{ display: 'flex', flexDirection: 'column' }}>
        <Outlet />
      </Content>
      <Footer />
    </Layout>
  );
};

export default AppLayout;
