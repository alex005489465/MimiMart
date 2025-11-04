import express from 'express';
import cors from 'cors';
import { buildFrontend } from './services/builder.js';
import { deployFrontend } from './services/deployer.js';
import { buildBackend, checkBackendEnvironment } from './services/backend-builder.js';
import { deployGateway, checkGatewayEnvironment } from './services/gateway-deployer.js';

const app = express();

app.use(cors());
app.use(express.json());
app.use(express.static('/app/web'));

// 狀態檢查
app.get('/api/status', (req, res) => {
  const backendEnv = checkBackendEnvironment();
  const gatewayEnv = checkGatewayEnvironment();
  res.json({
    status: 'running',
    timestamp: new Date().toISOString(),
    projects: {
      frontend: ['admin-frontend', 'shop-frontend'],
      backend: ['backend'],
      gateway: ['nginx', 'cloudflared']
    },
    environment: {
      backend: backendEnv,
      gateway: gatewayEnv
    }
  });
});

// 後端構建（必須放在參數化路由之前）
app.post('/api/build/backend', async (req, res) => {
  try {
    console.log('開始構建後端...');
    const result = await buildBackend();

    res.json({
      success: true,
      project: 'backend',
      type: 'backend',
      action: 'build',
      result
    });
  } catch (error) {
    console.error('後端構建失敗:', error);
    res.status(500).json({
      success: false,
      error: error.message,
      stack: error.stack
    });
  }
});

// 網關部署（必須放在參數化路由之前）
app.post('/api/deploy/gateway', async (req, res) => {
  try {
    console.log('開始部署網關配置到 EC2...');
    const result = await deployGateway();

    res.json({
      success: true,
      project: 'gateway',
      type: 'gateway',
      action: 'deploy',
      result
    });
  } catch (error) {
    console.error('網關部署失敗:', error);
    res.status(500).json({
      success: false,
      error: error.message,
      stack: error.stack
    });
  }
});

// 前端構建（參數化路由，必須放在後面）
app.post('/api/build/:project', async (req, res) => {
  const { project } = req.params;

  try {
    console.log(`開始構建前端: ${project}`);
    const result = await buildFrontend(project);

    res.json({
      success: true,
      project,
      type: 'frontend',
      action: 'build',
      result
    });
  } catch (error) {
    console.error(`前端構建失敗: ${project}`, error);
    res.status(500).json({
      success: false,
      error: error.message,
      stack: error.stack
    });
  }
});

// 完整部署（構建 + 部署）
app.post('/api/deploy/:project', async (req, res) => {
  const { project } = req.params;
  const { skipBuild = false } = req.body;

  try {
    console.log(`開始部署: ${project} (skipBuild: ${skipBuild})`);

    let buildResult = null;
    if (!skipBuild) {
      buildResult = await buildFrontend(project);
    }

    const deployResult = await deployFrontend(project);

    res.json({
      success: true,
      project,
      action: 'deploy',
      buildResult,
      deployResult
    });
  } catch (error) {
    console.error(`部署失敗: ${project}`, error);
    res.status(500).json({
      success: false,
      error: error.message,
      stack: error.stack
    });
  }
});

const PORT = 3100;
app.listen(PORT, () => {
  console.log(`MimiMart CI/CD API 運行於 http://localhost:${PORT}`);
  console.log(`管理界面: http://localhost:${PORT}/`);
});
