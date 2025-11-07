/**
 * 搜尋欄元件
 */
import { useState } from 'react';
import { TextField, InputAdornment, IconButton } from '@mui/material';
import { MdSearch, MdClear } from 'react-icons/md';
import { useNavigate } from 'react-router-dom';
import styles from './Header.module.css';

const SearchBar = () => {
  const [searchValue, setSearchValue] = useState('');
  const navigate = useNavigate();

  const handleSearch = () => {
    if (searchValue.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchValue.trim())}`);
      setSearchValue('');
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const handleClear = () => {
    setSearchValue('');
  };

  return (
    <div className={styles.searchBar}>
      <TextField
        placeholder="搜尋商品..."
        value={searchValue}
        onChange={(e) => setSearchValue(e.target.value)}
        onKeyPress={handleKeyPress}
        size="small"
        fullWidth
        InputProps={{
          endAdornment: (
            <InputAdornment position="end">
              {searchValue && (
                <IconButton size="small" onClick={handleClear} edge="end">
                  <MdClear />
                </IconButton>
              )}
              <IconButton size="small" onClick={handleSearch} edge="end">
                <MdSearch />
              </IconButton>
            </InputAdornment>
          ),
        }}
      />
    </div>
  );
};

export default SearchBar;
