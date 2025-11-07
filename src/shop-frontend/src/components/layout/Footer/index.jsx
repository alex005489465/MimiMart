/**
 * Footer 元件
 * 四欄式頁尾：關於我們、客服中心、購物指南、聯絡方式
 */
import { Layout, Row, Col, Space, Typography } from 'antd';
import {
  PhoneOutlined,
  MailOutlined,
  EnvironmentOutlined,
  FacebookOutlined,
  InstagramOutlined,
  TwitterOutlined,
  YoutubeOutlined,
} from '@ant-design/icons';
import { Link } from 'react-router-dom';
import styles from './Footer.module.css';

const { Footer: AntFooter } = Layout;
const { Title, Text, Link: AntLink } = Typography;

const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <AntFooter className={styles.footer}>
      <div className={styles.container}>
        <Row gutter={[32, 32]}>
          {/* 關於我們 */}
          <Col xs={24} sm={12} md={6}>
            <div className={styles.section}>
              <Title level={5} className={styles.title}>
                關於我們
              </Title>
              <Space direction="vertical" size="small">
                <Link to="/about" className={styles.link}>
                  公司簡介
                </Link>
                <Link to="/team" className={styles.link}>
                  團隊介紹
                </Link>
                <Link to="/careers" className={styles.link}>
                  加入我們
                </Link>
                <Link to="/news" className={styles.link}>
                  最新消息
                </Link>
              </Space>
            </div>
          </Col>

          {/* 客服中心 */}
          <Col xs={24} sm={12} md={6}>
            <div className={styles.section}>
              <Title level={5} className={styles.title}>
                客服中心
              </Title>
              <Space direction="vertical" size="small">
                <Link to="/help" className={styles.link}>
                  常見問題
                </Link>
                <Link to="/shipping" className={styles.link}>
                  配送說明
                </Link>
                <Link to="/returns" className={styles.link}>
                  退換貨政策
                </Link>
                <Link to="/payment" className={styles.link}>
                  付款方式
                </Link>
              </Space>
            </div>
          </Col>

          {/* 購物指南 */}
          <Col xs={24} sm={12} md={6}>
            <div className={styles.section}>
              <Title level={5} className={styles.title}>
                購物指南
              </Title>
              <Space direction="vertical" size="small">
                <Link to="/how-to-order" className={styles.link}>
                  如何購物
                </Link>
                <Link to="/member-benefits" className={styles.link}>
                  會員權益
                </Link>
                <Link to="/privacy" className={styles.link}>
                  隱私權政策
                </Link>
                <Link to="/terms" className={styles.link}>
                  服務條款
                </Link>
              </Space>
            </div>
          </Col>

          {/* 聯絡方式 */}
          <Col xs={24} sm={12} md={6}>
            <div className={styles.section}>
              <Title level={5} className={styles.title}>
                聯絡我們
              </Title>
              <Space direction="vertical" size="middle">
                <div className={styles.contact}>
                  <PhoneOutlined className={styles.icon} />
                  <Text>(02) 1234-5678</Text>
                </div>
                <div className={styles.contact}>
                  <MailOutlined className={styles.icon} />
                  <AntLink href="mailto:support@mimimart.com">
                    support@mimimart.com
                  </AntLink>
                </div>
                <div className={styles.contact}>
                  <EnvironmentOutlined className={styles.icon} />
                  <Text>台北市信義區信義路五段7號</Text>
                </div>

                {/* 社群媒體 */}
                <Space size="middle" className={styles.social}>
                  <AntLink
                    href="https://facebook.com"
                    target="_blank"
                    aria-label="Facebook"
                  >
                    <FacebookOutlined className={styles.socialIcon} />
                  </AntLink>
                  <AntLink
                    href="https://instagram.com"
                    target="_blank"
                    aria-label="Instagram"
                  >
                    <InstagramOutlined className={styles.socialIcon} />
                  </AntLink>
                  <AntLink
                    href="https://twitter.com"
                    target="_blank"
                    aria-label="Twitter"
                  >
                    <TwitterOutlined className={styles.socialIcon} />
                  </AntLink>
                  <AntLink
                    href="https://youtube.com"
                    target="_blank"
                    aria-label="YouTube"
                  >
                    <YoutubeOutlined className={styles.socialIcon} />
                  </AntLink>
                </Space>
              </Space>
            </div>
          </Col>
        </Row>

        {/* 版權聲明 */}
        <div className={styles.copyright}>
          <Text type="secondary">
            © {currentYear} MimiMart. All rights reserved.
          </Text>
        </div>
      </div>
    </AntFooter>
  );
};

export default Footer;
