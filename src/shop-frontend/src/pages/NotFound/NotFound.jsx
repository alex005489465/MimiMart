/**
 * 404 頁面
 */
import { Box, Typography, Button, Stack } from '@mui/material';
import { MdError } from 'react-icons/md';
import { useNavigate } from 'react-router-dom';
import styles from './NotFound.module.css';

const NotFound = () => {
  const navigate = useNavigate();

  return (
    <Box className={styles.notFoundPage}>
      <Stack spacing={3} alignItems="center" sx={{ py: 8 }}>
        <MdError size={120} color="#d32f2f" />
        <Typography variant="h1" sx={{ fontSize: '4rem', fontWeight: 700 }}>
          404
        </Typography>
        <Typography variant="h5" color="text.secondary">
          抱歉，您訪問的頁面不存在
        </Typography>
        <Button
          variant="contained"
          size="large"
          onClick={() => navigate('/')}
          sx={{ mt: 2 }}
        >
          返回首頁
        </Button>
      </Stack>
    </Box>
  );
};

export default NotFound;
