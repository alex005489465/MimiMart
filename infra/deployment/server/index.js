import express from 'express';
import cors from 'cors';
import { buildFrontend } from './services/builder.js';
import { deployFrontend } from './services/deployer.js';

const app = express();

app.use(cors());
app.use(express.json());
app.use(express.static('/app/web'));

// 狀態檢查
app.get('/api/status', (req, res) => {
  res.json({
    status: 'running',
    timestamp: new Date().toISOString(),
    projects: ['admin-frontend', 'shop-frontend']
  });
});

// 僅構建
app.post('/api/build/:project', async (req, res) => {
  const { project } = req.params;

  try {
    console.log(`開始構建: ${project}`);
    const result = await buildFrontend(project);

    res.json({
      success: true,
      project,
      action: 'build',
      result
    });
  } catch (error) {
    console.error(`構建失敗: ${project}`, error);
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
