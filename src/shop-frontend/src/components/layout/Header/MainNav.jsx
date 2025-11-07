/**
 * 主導航選單元件
 */
import { Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  HomeOutlined,
  AppstoreOutlined,
  TagsOutlined,
  GiftOutlined,
} from '@ant-design/icons';

const MainNav = ({ mode = 'horizontal' }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const items = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: '首頁',
    },
    {
      key: '/products',
      icon: <AppstoreOutlined />,
      label: '商品分類',
      children: [
        { key: '/products?category=electronics', label: '電子產品' },
        { key: '/products?category=fashion', label: '時尚服飾' },
        { key: '/products?category=home', label: '居家生活' },
        { key: '/products?category=sports', label: '運動休閒' },
      ],
    },
    {
      key: '/promotions',
      icon: <TagsOutlined />,
      label: '優惠活動',
    },
    {
      key: '/new-arrivals',
      icon: <GiftOutlined />,
      label: '新品上市',
    },
  ];

  const handleClick = (e) => {
    navigate(e.key);
  };

  return (
    <Menu
      mode={mode}
      selectedKeys={[location.pathname]}
      items={items}
      onClick={handleClick}
      style={{
        flex: 1,
        minWidth: 0,
        border: 'none',
        background: 'transparent',
      }}
    />
  );
};

export default MainNav;
