/**
 * 搜尋欄元件
 */
import { useState } from 'react';
import { Input } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import styles from './Header.module.css';

const { Search } = Input;

const SearchBar = () => {
  const [searchValue, setSearchValue] = useState('');
  const navigate = useNavigate();

  const handleSearch = (value) => {
    if (value.trim()) {
      navigate(`/products?search=${encodeURIComponent(value.trim())}`);
      setSearchValue('');
    }
  };

  return (
    <div className={styles.searchBar}>
      <Search
        placeholder="搜尋商品..."
        value={searchValue}
        onChange={(e) => setSearchValue(e.target.value)}
        onSearch={handleSearch}
        enterButton={<SearchOutlined />}
        size="large"
        allowClear
      />
    </div>
  );
};

export default SearchBar;
