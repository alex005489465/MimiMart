/**
 * 主導航選單元件
 */
import { useState } from 'react';
import { Button, Menu, MenuItem, Box, List, ListItemButton, ListItemIcon, ListItemText, Collapse } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  MdHome,
  MdApps,
  MdLocalOffer,
  MdCardGiftcard,
  MdExpandMore,
  MdExpandLess
} from 'react-icons/md';

const MainNav = ({ mode = 'horizontal' }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = useState(null);
  const [expandedMobile, setExpandedMobile] = useState(false);

  const menuItems = [
    {
      key: '/',
      icon: <MdHome size={20} />,
      label: '首頁',
    },
    {
      key: '/products',
      icon: <MdApps size={20} />,
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
      icon: <MdLocalOffer size={20} />,
      label: '優惠活動',
    },
    {
      key: '/new-arrivals',
      icon: <MdCardGiftcard size={20} />,
      label: '新品上市',
    },
  ];

  const handleClick = (path) => {
    navigate(path);
    setAnchorEl(null);
  };

  const handleProductsClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  // 桌面版導航
  if (mode === 'horizontal') {
    return (
      <Box sx={{ display: 'flex', gap: 1 }}>
        {menuItems.map((item) => {
          if (item.children) {
            return (
              <Box key={item.key}>
                <Button
                  startIcon={item.icon}
                  onClick={handleProductsClick}
                  sx={{
                    color: 'text.primary',
                    textTransform: 'none',
                    fontWeight: location.pathname === item.key ? 600 : 400,
                  }}
                >
                  {item.label}
                </Button>
                <Menu
                  anchorEl={anchorEl}
                  open={Boolean(anchorEl)}
                  onClose={handleClose}
                >
                  {item.children.map((child) => (
                    <MenuItem
                      key={child.key}
                      onClick={() => handleClick(child.key)}
                    >
                      {child.label}
                    </MenuItem>
                  ))}
                </Menu>
              </Box>
            );
          }
          return (
            <Button
              key={item.key}
              startIcon={item.icon}
              onClick={() => handleClick(item.key)}
              sx={{
                color: 'text.primary',
                textTransform: 'none',
                fontWeight: location.pathname === item.key ? 600 : 400,
              }}
            >
              {item.label}
            </Button>
          );
        })}
      </Box>
    );
  }

  // 行動版導航
  return (
    <List>
      {menuItems.map((item) => {
        if (item.children) {
          return (
            <Box key={item.key}>
              <ListItemButton onClick={() => setExpandedMobile(!expandedMobile)}>
                <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
                <ListItemText primary={item.label} />
                {expandedMobile ? <MdExpandLess /> : <MdExpandMore />}
              </ListItemButton>
              <Collapse in={expandedMobile} timeout="auto" unmountOnExit>
                <List component="div" disablePadding>
                  {item.children.map((child) => (
                    <ListItemButton
                      key={child.key}
                      sx={{ pl: 4 }}
                      onClick={() => handleClick(child.key)}
                    >
                      <ListItemText primary={child.label} />
                    </ListItemButton>
                  ))}
                </List>
              </Collapse>
            </Box>
          );
        }
        return (
          <ListItemButton
            key={item.key}
            onClick={() => handleClick(item.key)}
            selected={location.pathname === item.key}
          >
            <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} />
          </ListItemButton>
        );
      })}
    </List>
  );
};

export default MainNav;
