# LuminCore 女性健康助手 - 项目结构

![版本](https://img.shields.io/badge/版本-1.0.0-brightgreen)
![平台](https://img.shields.io/badge/平台-Android-blue)
![状态](https://img.shields.io/badge/状态-活跃开发中-success)

## 📂 项目整体结构

```
app/src/main/
├── java/top/mizhoubaobei/womenhealth/
│   ├── data/                           # 数据层
│   │   ├── db/                         # 数据库相关
│   │   │   ├── AppDatabase.kt          # Room 数据库配置
│   │   │   ├── DateConverter.kt        # 日期类型转换器
│   │   │   ├── MenstrualDao.kt         # 数据访问对象
│   │   │   ├── MenstrualRecord.kt      # 月经记录实体
│   │   │   └── SQLiteMenstrualStorage.kt # SQLite存储实现
│   │   └── repository/                 # 数据仓库
│   │       └── (待开发)                 # 待实现的数据仓库类
│   ├── ui/                             # 用户界面层
│   │   ├── calendar/                   # 日历功能
│   │   │   ├── CalendarAdapter.kt      # 日历适配器
│   │   │   ├── CalendarDay.kt          # 日历日期模型
│   │   │   ├── CalendarFragment.kt     # 日历页面
│   │   │   ├── CalendarViewModel.kt    # 日历视图模型
│   │   │   └── DateDetailDialog.kt     # 日期详情对话框
│   │   ├── list/                       # 记录列表功能
│   │   │   ├── AddRecordDialog.kt      # 添加记录对话框
│   │   │   ├── ListFragment.kt         # 列表页面
│   │   │   ├── ListViewModel.kt        # 列表视图模型
│   │   │   └── RecordsAdapter.kt       # 记录适配器
│   │   ├── quickadd/                   # 快速添加功能
│   │   │   ├── QuickAddFragment.kt     # 快速添加页面
│   │   │   ├── QuickAddViewModel.kt    # 快速添加视图模型
│   │   │   └── SymptomsDialog.kt       # 症状选择对话框
│   │   ├── statistics/                 # 统计分析功能
│   │   │   ├── StatisticsFragment.kt   # 统计页面
│   │   │   ├── StatisticsViewModel.kt  # 统计视图模型
│   │   │   └── SymptomStatAdapter.kt   # 症状统计适配器
│   │   ├── MainActivity.kt             # 主活动
│   │   └── WomenHealthApplication.kt   # 应用程序类
│   └── utils/                          # 工具类(待开发)
├── res/                                # 资源文件
│   ├── drawable/                       # 图像资源
│   │   ├── bg_*.xml                    # 各种背景资源
│   │   └── ic_*.xml                    # 各种图标资源
│   ├── layout/                         # 布局文件
│   │   ├── activity_main.xml           # 主活动布局
│   │   ├── fragment_*.xml              # 各功能片段布局
│   │   ├── dialog_*.xml                # 对话框布局
│   │   └── item_*.xml                  # 列表项布局
│   ├── menu/                           # 菜单配置
│   │   └── bottom_nav_menu.xml         # 底部导航菜单
│   ├── navigation/                     # 导航图
│   │   └── mobile_navigation.xml       # 主导航图
│   └── values/                         # 字符串、颜色等资源
│       ├── colors.xml                  # 颜色定义
│       ├── strings.xml                 # 字符串资源
│       ├── styles.xml                  # 样式定义
│       └── themes.xml                  # 主题定义
└── AndroidManifest.xml                 # 应用清单文件
```

## 🛠️ 技术架构

### 核心架构
- **MVVM架构**：采用Model-View-ViewModel设计模式
- **数据层**：使用Room数据库进行本地数据存储
- **UI层**：基于Jetpack组件构建，包括Navigation、ViewModel等
- **异步处理**：使用Kotlin协程进行异步操作

### 主要技术栈
- **开发语言**：Kotlin
- **数据库**：Room
- **UI组件**：Jetpack Compose(待迁移)/XML布局
- **依赖注入**：Hilt(待实现)
- **图表库**：MPAndroidChart(待集成)
- **日期处理**：ThreeTenABP(待集成)

## 📝 待开发模块

根据DEVELOPMENT_PLAN.md中的规划，以下模块待开发：

1. **数据层增强**
   - 完整的Repository模式实现
   - 数据缓存策略
   - 高级查询功能

2. **工具类**
   - DateUtils.kt - 日期处理工具
   - PredictionUtils.kt - 周期预测工具
   - NotificationUtils.kt - 通知工具
   - BackupUtils.kt - 备份工具

3. **依赖注入**
   - AppModule.kt - 应用模块
   - DatabaseModule.kt - 数据库模块

4. **高级功能**
   - 云端同步功能
   - 多主题支持
   - 数据分析模块

## 🔄 近期开发重点

1. 完善数据层Repository实现
2. 添加依赖注入支持
3. 实现周期预测算法
4. 开发数据备份功能
5. 优化UI性能

---