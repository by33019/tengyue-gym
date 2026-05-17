# 全局布局重设计 — 侧边栏 + 活力运动风

## 目标

将当前窄列居中布局（`max-w-lg mx-auto`）改为桌面端侧边栏 + 宽内容区的专业布局，配合活力运动风格配色，提升视觉档次和空间利用率。

## 布局架构

```
桌面端 (≥1024px):                    移动端 (<1024px):
┌────────┬──────────────────┐        ┌──────────────────┐
│ Sidebar│  Content Area    │        │   Content Area   │
│ 240px  │  flex-1 + padding│        │   max-w-full     │
│ fixed  │  scrollable      │        │   + BottomNav    │
└────────┴──────────────────┘        └──────────────────┘
```

- 侧边栏：深色渐变背景，图标+文字导航，底部用户信息
- 移动端：隐藏侧边栏，保留底部导航（已有）

## 色彩方案（活力运动风）

| 角色 | Tailwind 类/值 | 用途 |
|:---|:---|:---|
| 主色 primary | `#FF3B5C` coral-red | 按钮、链接、强调 |
| 辅助 secondary | `#00F5A0` neon-green | 成功状态、数据指示 |
| 强调 accent | `#00D2FF` cyan | 信息提示、AI 模块 |
| 背景 bg | `#0B0B14` deep-purple-black | 全局背景 |
| 卡片 card | `rgba(255,255,255,0.03)` + blur | 卡片容器 |
| 文字 primary | `#FFFFFF` | 主文字 |
| 文字 secondary | `rgba(255,255,255,0.5)` | 次要文字 |
| 文字 muted | `rgba(255,255,255,0.25)` | 辅助文字 |

## 文件改造清单

### 核心布局（改造 1 个，新建 1 个）

| 文件 | 操作 | 说明 |
|:---|:---|:---|
| `layouts/DefaultLayout.vue` | 重写 | 加入响应式侧边栏，桌面端侧栏+内容区，移动端底部导航 |
| `components/Sidebar.vue` | 新建 | 侧边栏组件：Logo、导航、用户信息、登出 |

### 页面改造（13 个页面）

| 文件 | 主要改造 |
|:---|:---|
| `HomePage.vue` | 顶部运动风横幅（渐变+大标题），2列卡片网格，右侧统计面板 |
| `CheckinPage.vue` | 左右分栏（表单 + 日历预览），运动类型大卡片 |
| `CalendarPage.vue` | 全宽日历，大号热力图 |
| `PlanPage.vue` | 2-3列计划卡片网格，模板市场横幅 |
| `PlanCreatePage.vue` | 表单项水平排列，充分利用宽度 |
| `PlanDetailPage.vue` | 全宽详情布局 |
| `AiPage.vue` | 对话区扩宽至900px，功能卡片横排胶囊按钮 |
| `ProfilePage.vue` | 全宽：头像横幅 → 身体数据 → 成就墙 → 设置 |
| `AdminPage.vue` | 全宽数据表 + 顶部统计卡行 |
| `StatsPage.vue` | 全宽 ECharts 图表 + 指标卡行 |
| `RankingPage.vue` | 全宽排名表 + 筛选项横排 |
| `CommunityPage.vue` | 宽内容流 + 右侧热门话题 |
| `LoginPage.vue` | 保持居中（登录页无需侧栏），强化渐变背景 |

### 响应式断点

| 断点 | 宽度 | 行为 |
|:---|:---|:---|
| Mobile | <768px | 全宽内容 + 底部导航，单列 |
| Tablet | 768-1023px | 全宽内容 + 底部导航，2列 |
| Desktop | ≥1024px | 侧边栏(240px) + 内容区，2-3列 |

## 关键设计决策

1. **侧边栏不占内容空间** — 使用 `ml-[240px]` 而非 flex 布局，避免内容区计算复杂
2. **每个页面独立去掉 `max-w-lg`** — 改用 `w-full` + 合理的内边距 `px-6 lg:px-10`
3. **动画过渡** — 侧边栏切换、hover 效果平滑过渡
4. **卡片圆角统一** — `rounded-2xl`（16px）
5. **字体** — 标题使用 `Bebas Neue`（运动感），正文 `Noto Sans SC`
