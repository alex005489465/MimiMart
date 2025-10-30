import Header from '../../components/Header/Header'
import HeroBanner from '../../components/HeroBanner/HeroBanner'
import styles from './Home.module.css'

export default function Home() {
  return (
    <div className={styles.homePage}>
      <Header />
      <main className={styles.main}>
        <HeroBanner />

        {/* 未來可以在這裡加入更多區塊 */}
        <section className={styles.welcomeSection}>
          <div className={styles.container}>
            <h2 className={styles.sectionTitle}>歡迎來到 MimiMart</h2>
            <p className={styles.sectionDescription}>
              您的線上購物好夥伴，提供多樣化的商品選擇與優質的購物體驗
            </p>
          </div>
        </section>
      </main>

      <footer className={styles.footer}>
        <div className={styles.container}>
          <p>&copy; 2025 MimiMart. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}
