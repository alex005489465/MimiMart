/**
 * 404 頁面
 */
import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import styles from './NotFound.module.css';

const NotFound = () => {
  const navigate = useNavigate();

  return (
    <div className={styles.notFoundPage}>
      <Result
        status="404"
        title="404"
        subTitle="抱歉，您訪問的頁面不存在"
        extra={
          <Button type="primary" size="large" onClick={() => navigate('/')}>
            返回首頁
          </Button>
        }
      />
    </div>
  );
};

export default NotFound;
