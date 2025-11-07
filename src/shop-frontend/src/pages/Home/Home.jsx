/**
 * 首頁元件
 * 使用 Ant Design 元件重構
 */
import { Typography, Row, Col, Card, Button, Space } from 'antd';
import { ShoppingOutlined, SafetyOutlined, CustomerServiceOutlined, RocketOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import HeroBanner from '../../components/HeroBanner/HeroBanner';
import styles from './Home.module.css';

const { Title, Paragraph } = Typography;

export default function Home() {
  const navigate = useNavigate();

  // 服務特色
  const features = [
    {
      icon: <ShoppingOutlined style={{ fontSize: '48px', color: '#1890ff' }} />,
      title: '商品多樣',
      description: '提供豐富的商品選擇，滿足您的各種需求',
    },
    {
      icon: <SafetyOutlined style={{ fontSize: '48px', color: '#52c41a' }} />,
      title: '安全保障',
      description: '嚴格的品質把關，讓您購物更安心',
    },
    {
      icon: <CustomerServiceOutlined style={{ fontSize: '48px', color: '#faad14' }} />,
      title: '優質客服',
      description: '專業的客服團隊，隨時為您服務',
    },
    {
      icon: <RocketOutlined style={{ fontSize: '48px', color: '#722ed1' }} />,
      title: '快速配送',
      description: '高效的物流系統，確保商品準時送達',
    },
  ];

  return (
    <>
      {/* 輪播 Banner */}
      <HeroBanner />

      {/* 歡迎區塊 */}
      <section className={styles.welcomeSection}>
        <div className={styles.container}>
          <Title level={2} style={{ textAlign: 'center', marginBottom: 16 }}>
            歡迎來到 MimiMart
          </Title>
          <Paragraph
            style={{
              textAlign: 'center',
              fontSize: '16px',
              maxWidth: '600px',
              margin: '0 auto 48px',
            }}
          >
            您的線上購物好夥伴，提供多樣化的商品選擇與優質的購物體驗
          </Paragraph>

          {/* 服務特色 */}
          <Row gutter={[24, 24]}>
            {features.map((feature, index) => (
              <Col xs={24} sm={12} md={6} key={index}>
                <Card
                  hoverable
                  style={{ textAlign: 'center', height: '100%' }}
                  bodyStyle={{ padding: '32px 24px' }}
                >
                  <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    {feature.icon}
                    <Title level={4} style={{ margin: 0 }}>
                      {feature.title}
                    </Title>
                    <Paragraph
                      type="secondary"
                      style={{ margin: 0, fontSize: '14px' }}
                    >
                      {feature.description}
                    </Paragraph>
                  </Space>
                </Card>
              </Col>
            ))}
          </Row>

          {/* CTA 按鈕 */}
          <div style={{ textAlign: 'center', marginTop: 48 }}>
            <Space size="large">
              <Button
                type="primary"
                size="large"
                onClick={() => navigate('/products')}
              >
                開始購物
              </Button>
              <Button size="large" onClick={() => navigate('/promotions')}>
                查看優惠
              </Button>
            </Space>
          </div>
        </div>
      </section>

      {/* 未來可以在這裡加入更多區塊 */}
      {/* - 熱門商品推薦 */}
      {/* - 優惠活動 */}
      {/* - 新品上市 */}
    </>
  );
}
