# Android 项目结构学习笔记

## � 核心术语

### Kotlin DSL

**DSL** = Domain-Specific Language（领域特定语言）

**Kotlin DSL** 是一种使用 Kotlin 语言编写的、专门用于 Gradle 构建配置的语法风格。

#### 什么是 Groovy？

**Groovy** 是一种运行在 JVM 上的动态编程语言，长期以来是 Gradle 构建脚本的默认语言。

- **历史地位**: 在 Kotlin DSL 出现之前，所有 Android 项目都使用 Groovy 编写构建脚本
- **动态类型**: 变量类型在运行时确定，灵活但容易出错
- **文件后缀**: `.gradle`（而非 `.gradle.kts`）
- **现状**: 仍被广泛使用，但新项目推荐使用 Kotlin DSL

**为什么从 Groovy 迁移到 Kotlin DSL？**

- Groovy 是动态语言，IDE 难以提供准确的代码补全和错误检查
- Kotlin 是静态类型语言，编译时就能发现错误
- 使用 Kotlin DSL 让构建脚本和应用代码使用同一种语言

#### 对比示例

**传统方式 (Groovy)**:

```groovy
// build.gradle (Groovy)
android {
    compileSdk 34
    defaultConfig {
        applicationId "com.example.app"
        minSdk 24
    }
}
```

**现代方式 (Kotlin DSL)**:

```kotlin
// build.gradle.kts (Kotlin DSL)
android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
    }
}
```

**优势**:

- ✅ 类型安全：编译时检查，减少错误
- ✅ IDE 支持更好：自动补全、跳转到定义
- ✅ 重构友好：重命名、查找引用等功能
- ✅ 使用 Kotlin 语法：与应用代码语言一致

**识别方法**:

- Groovy 方式：文件名是 `build.gradle`
- Kotlin DSL 方式：文件名是 `build.gradle.kts`（注意 `.kts` 后缀）

### Jetpack Compose

**Jetpack Compose** 是 Google 推出的现代化 Android UI 工具包，使用声明式编程构建界面。

**传统方式 (XML + View)**:

```xml
<!-- layout.xml -->
<LinearLayout>
    <TextView
        android:text="Hello World"
        android:textSize="24sp" />
    <Button
        android:text="Click Me"
        android:onClick="onButtonClick" />
</LinearLayout>
```

```kotlin
// Activity.kt
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.textView).text = "Hello"
    }
}
```

**现代方式 (Jetpack Compose)**:

```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text(text = "Hello World", fontSize = 24.sp)
            Button(onClick = { /* 处理点击 */ }) {
                Text("Click Me")
            }
        }
    }
}
```

**核心特点**:

- ✅ 声明式 UI：描述"想要什么"而非"如何做"
- ✅ 纯 Kotlin：不需要 XML 布局文件
- ✅ 实时预览：在 Android Studio 中即时查看 UI
- ✅ 更少代码：减少样板代码，提高开发效率
- ✅ 现代化：支持动画、主题、状态管理等

**对比总结**:

| 特性 | 传统方式 | Jetpack Compose |
|------|---------|-----------------|
| UI 定义 | XML 文件 | Kotlin 代码 |
| 编程范式 | 命令式 | 声明式 |
| 类型安全 | ❌ XML 无类型检查 | ✅ 编译时检查 |
| 预览 | 需要运行应用 | 实时预览 |
| 学习曲线 | 传统但繁琐 | 现代且简洁 |

**在本项目中的体现**:

你的项目使用了 Jetpack Compose，所以：

- MainActivity.kt 中使用 `setContent {}` 而非 `setContentView()`
- res/layout/ 文件夹可能为空（不需要 XML 布局）
- build.gradle.kts 中启用了 `compose = true`
- 依赖中包含 Compose 相关库（如 `androidx.compose.ui`）

## �📚 项目整体架构

### 核心概念层次

```text
项目根目录 (MyApplication)
├── 项目级配置文件 - 全局性、所有模块共享
├── 工具配置目录 - 为整个项目服务
└── 模块目录 (app, library...) - 独立功能单元
    └── 模块级配置文件 - 模块特定
```

## 🔑 关键配置文件的层级关系

### 三层配置体系

#### 第一层：项目定义层

##### settings.gradle.kts

- **位置**: 必须在项目根目录
- **作用**: Gradle 构建的入口文件，定义项目边界和模块组成
- **核心功能**:
  - 声明项目名称 (`rootProject.name`)
  - 定义包含哪些模块 (`include(":app")`)
  - 配置依赖仓库
- **地位**: 整个项目层次结构的最顶层

#### 第二层：全局配置层

##### 根目录/build.gradle.kts (项目级)

- **作用**: 配置所有模块共享的构建选项
- **特点**:
  - 声明插件但不立即应用 (`apply false`)
  - 相当于"插件仓库"
  - 修改频率很低

##### gradle.properties

- **作用**: 项目级别的 Gradle 全局属性配置
- **内容**: JVM内存、构建选项、AndroidX配置等
- **特点**:
  - 应该提交到版本控制 ✅
  - 团队所有成员共享
  - 配置构建工具行为

#### 第三层：模块配置层

##### app/build.gradle.kts (模块级)

- **作用**: 配置 app 模块特定的内容
- **内容**:
  - 直接应用插件（无 `apply false`)
  - Android 配置块 (`android {}`)
  - 依赖声明 (`dependencies {}`)
  - 版本号、SDK 版本等
- **特点**: 开发中最常修改的文件

##### app/src/main/AndroidManifest.xml

- **作用**: Android 应用的"身份证"，向系统声明应用结构
- **配置对象**: Android 系统（而非构建工具）
- **核心功能**:
  - 声明应用组件（Activity、Service等）
  - 权限声明
  - 应用图标、名称、主题
  - 启动入口定义

## 🔄 特殊配置文件对比

### local.properties vs gradle.properties

| 维度 | local.properties | gradle.properties |
|------|------------------|-------------------|
| **作用域** | 本地机器特定配置 | 整个项目的全局配置 |
| **提交Git** | ❌ 不提交（在 .gitignore 中） | ✅ 应该提交 |
| **内容** | SDK路径等环境信息 | 构建选项、编译设置 |
| **团队共享** | ❌ 每人不同 | ✅ 所有人相同 |
| **自动生成** | ✅ Android Studio 自动创建 | ❌ 需要手动维护 |
| **典型配置** | `sdk.dir`, `ndk.dir` | `org.gradle.jvmargs`, `android.useAndroidX` |

**关键理解**:

- local.properties 解决"我的环境"问题
- gradle.properties 解决"我们的构建规则"问题

## 🏗️ Gradle 构建流程

### 执行顺序

```text
1. Gradle 找到 settings.gradle.kts (根目录)
   └─> 识别项目结构和包含的模块

2. 读取根目录的 build.gradle.kts (项目级)
   └─> 应用全局配置和插件声明

3. 读取每个模块的 build.gradle.kts (模块级)
   └─> 应用模块特定配置

4. 构建过程中读取 gradle.properties 和 local.properties
   └─> 获取构建参数和环境路径
```

### 配置优先级（从高到低）

1. 命令行参数
2. IDE 设置（Android Studio）
3. gradle.properties
4. 系统环境变量

## 📦 模块化理解

### 什么是模块？

- **定义**: 可以独立编译的功能单元
- **声明方式**: 在 settings.gradle.kts 中通过 `include()` 声明
- **特征**:
  - 拥有自己的 build.gradle.kts
  - 可以被其他模块依赖
  - app 模块可以编译成 APK

### 模块间关系

```kotlin
include(":app")         // 主应用模块
include(":library")     // 库模块
include(":feature")     // 功能模块

// app 模块依赖 library
dependencies {
    implementation(project(":library"))
}
```

## 🛠️ gradle 文件夹的角色

### 核心理解

- **不是模块**: gradle 文件夹不是一个独立模块
- **是工具配置**: 为整个项目提供构建工具支持
- **服务所有模块**: 所有模块共享其配置

### 主要组成

```text
gradle/
├── wrapper/           # Gradle Wrapper（包装器）
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
└── libs.versions.toml # 版本目录（集中管理依赖版本）
```

### Gradle Wrapper 的意义

- **统一构建环境**: 所有开发者使用相同版本的 Gradle
- **自动下载**: 首次运行自动下载指定版本
- **跨平台**: gradlew (Mac/Linux) 和 gradlew.bat (Windows)

## 📱 AndroidManifest.xml 的特殊地位

### 与其他配置文件的根本区别

| 维度 | build.gradle.kts | gradle.properties | AndroidManifest.xml |
|------|------------------|-------------------|---------------------|
| **配置对象** | Gradle 构建工具 | Gradle 构建工具 | Android 系统 |
| **使用时机** | 编译构建时 | 编译构建时 | 应用安装和运行时 |
| **语言** | Kotlin DSL | Properties | XML |
| **核心作用** | 如何构建 | 构建参数 | 应用是什么 |

### 关键职责

1. **组件声明**: 告诉系统应用有哪些 Activity、Service
2. **权限请求**: 声明需要的系统权限
3. **应用身份**: 图标、名称、主题
4. **启动配置**: 定义应用入口（MAIN + LAUNCHER）

### Intent-Filter 的关键作用

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
```

**含义**: 这是主入口 Activity，在桌面启动器显示应用图标

## 💡 核心概念总结

### 1. 层级关系

```text
settings.gradle.kts (定义项目)
    ↓
根build.gradle.kts (全局配置) + gradle.properties (构建参数)
    ↓
模块build.gradle.kts (模块配置) + AndroidManifest.xml (系统声明)
    ↓
源代码和资源文件
```

### 2. 配置文件的"受众"

- **Gradle 系列** (`*.gradle.kts`, `gradle.properties`) → 给构建工具看
- **AndroidManifest.xml** → 给 Android 系统看
- **local.properties** → 给本地环境看

### 3. 版本控制策略

- ✅ **应该提交**: settings.gradle.kts, build.gradle.kts, gradle.properties, AndroidManifest.xml
- ❌ **不应提交**: local.properties, .gradle/, build/, .idea/ (部分)

### 4. 开发中的修改频率

- **经常修改**: app/build.gradle.kts (添加依赖), AndroidManifest.xml (添加组件/权限)
- **偶尔修改**: gradle.properties (性能调优)
- **很少修改**: settings.gradle.kts, 根build.gradle.kts
- **几乎不动**: local.properties (自动生成)

## 🎯 初学者关注重点

### 最需要掌握的文件（按重要性）

1. **app/build.gradle.kts** - 添加依赖、配置版本号
2. **AndroidManifest.xml** - 声明组件和权限
3. **gradle.properties** - 性能调优（遇到问题时）
4. **settings.gradle.kts** - 添加新模块时
5. **根build.gradle.kts** - 一般不需要改动

### 常见操作映射

- **添加第三方库** → app/build.gradle.kts 的 dependencies
- **添加新页面** → 创建 Activity 类 + 在 AndroidManifest.xml 声明
- **需要网络访问** → AndroidManifest.xml 添加 INTERNET 权限
- **构建速度慢** → gradle.properties 调整内存配置
- **添加新功能模块** → settings.gradle.kts 添加 include

---

> 本笔记基于 Android 标准项目结构，使用 Kotlin DSL 和 Jetpack Compose
