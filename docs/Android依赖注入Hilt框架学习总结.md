# Android 依赖注入 Hilt 框架学习总结

> 本文档总结了 Android 开发中的核心概念：Application、Activity、依赖注入（DI, Data injection）以及 Hilt 框架的工作原理。

---

## 1. Application vs AppCompatActivity

### Application - 应用程序级别

- **整个应用只有一个实例**，在应用进程启动时最先创建
- **生命周期**：从应用启动到应用进程结束
- **作用**：
  - 全局初始化
  - 全局状态管理
  - 依赖注入容器（如 Hilt）
- **必须在 AndroidManifest.xml 中注册**

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 整个应用启动时执行一次
    }
}
```

### AppCompatActivity - 界面/屏幕

- **一个应用可以有多个 Activity**，每个代表一个界面
- **生命周期**：用户进入界面时创建，离开时销毁（可能被重建）
- **作用**：
  - 展示 UI
  - 处理用户交互
  - 托管 Fragment
- `AppCompatActivity` 是 `Activity` 的扩展版，提供向下兼容的 Material Design 支持

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 每次打开这个界面时执行
    }
}
```

### 启动顺序

```text
应用启动 → Application.onCreate() → MainActivity.onCreate()
         ↑ 根本入口            ↑ 第一个界面
```

**Application 是真正的根入口**，但用户看到的第一个界面通常是 MainActivity。

---

## 2. di 文件夹 - Dependency Injection（依赖注入）

### 什么是 di？

`di` = **Dependency Injection（依赖注入）**

### 作用

专门存放 Hilt 的依赖注入配置模块，定义"如何创建和提供对象"的规则。

### 为什么需要依赖注入？

```kotlin
// ❌ 不用依赖注入 - 直接 new
class UserViewModel {
    private val repository = UserRepository(MockUserDataSource())
    // 问题：难以测试、难以替换实现、耦合度高
}

// ✅ 使用依赖注入 - 由 Hilt 提供
class UserViewModel @Inject constructor(
    private val repository: UserRepository  // 自动注入
) {
    // 好处：易于测试、松耦合、可灵活替换实现
}
```

---

## 3. Hilt 依赖注入的工作原理

### 3.1 核心注解

#### @HiltAndroidApp

```kotlin
@HiltAndroidApp
class MyApplication : Application()
```

- 标记应用入口
- 触发 Hilt 代码生成
- 初始化依赖注入系统

#### @Module + @InstallIn

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    // 定义依赖提供规则
}
```

- `@Module`: 标记这是依赖注入模块
- `@InstallIn`: 指定生命周期范围
  - `SingletonComponent`: 应用级单例
  - `ActivityComponent`: Activity 级别
  - `FragmentComponent`: Fragment 级别

#### @Provides + @Singleton

```kotlin
@Provides
@Singleton
fun provideUserDataSource(): UserDataSource {
    return MockUserDataSource()
}
```

- `@Provides`: 告诉 Hilt 如何创建实例
- `@Singleton`: 确保只有一个实例

#### @HiltViewModel + @Inject constructor

```kotlin
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel()
```

- `@HiltViewModel`: 标记 ViewModel 由 Hilt 管理
- `@Inject constructor`: 构造函数注入依赖

### 3.2 Kotlin 主构造函数语法解析

```kotlin
class UserListViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
```

**等价的 Java 写法**：

```java
public class UserListViewModel extends ViewModel {
    private final UserRepository repository;
    
    @Inject
    public UserListViewModel(UserRepository repository) {
        this.repository = repository;
    }
}
```

**Kotlin 简洁之处**：

- 一行搞定：参数声明 + 属性声明 + 赋值
- `private val repository`: 同时声明构造参数和类属性
- `constructor` 关键字在注解时必须写

---

## 4. 依赖注入的执行流程

### 4.1 依赖树结构

```text
MyApplication (@HiltAndroidApp)  ← 系统初始化
      ↓
DataModule (@Module)  ← 规则起点
      ↓
      ├─ provideUserDataSource()  ← 创建数据源（叶子节点）
      │        ↓
      │   MockUserDataSource()  ← 数据源头（无依赖）
      │
      └─ provideUserRepository(dataSource)  ← 依赖上面的结果
               ↓
          UserRepository(dataSource)
               ↓
          UserListViewModel(repository)
               ↓
          UserListFragment (viewModel)
```

### 4.2 完整的依赖注入链

```text
UserListFragment 启动
      ↓
private val viewModel: UserListViewModel by viewModels()  ← 触发点
      ↓
Hilt 检测到 @HiltViewModel 和 @Inject constructor
      ↓
Hilt 发现 UserListViewModel 需要 UserRepository
      ↓
Hilt 查找 DataModule.provideUserRepository(dataSource)
      ↓
provideUserRepository() 需要 UserDataSource 参数
      ↓
Hilt 查找 DataModule.provideUserDataSource()  ← 先调用这个
      ↓
返回 MockUserDataSource 实例
      ↓
将 MockUserDataSource 传给 provideUserRepository()
      ↓
返回 UserRepository 实例
      ↓
将 UserRepository 注入到 UserListViewModel 构造函数
      ↓
创建 UserListViewModel 实例
      ↓
viewModel 变量赋值完成
```

### 4.3 调用时机

#### 第一次访问（触发调用）

```kotlin
class FragmentA : Fragment() {
    private val viewModel: UserListViewModel by viewModels()
    //                                         ↑
    // 第一次访问时触发：
    // provideUserDataSource() 执行 → 创建 MockUserDataSource 单例 ✅
    // provideUserRepository(dataSource) 执行 → 创建 UserRepository 单例 ✅
}
```

#### 后续访问（复用单例）

```kotlin
class FragmentB : Fragment() {
    private val viewModel: SomeViewModel by viewModels()
    //                                    ↑
    // 需要 UserRepository
    // provideUserDataSource() 不执行 ❌（复用已有单例）
    // provideUserRepository() 不执行 ❌（复用已有单例）
    // 直接返回第一次创建的实例 ✅
}
```

#### 时序图

```text
时刻 T0: Fragment.onCreateView() 开始执行
      ↓
时刻 T1: 访问 viewModel 属性（lazy 延迟初始化）
      ↓
时刻 T2: Hilt 检测到需要 UserListViewModel
      ↓
时刻 T3: 发现需要 UserRepository 依赖
      ↓
时刻 T4: 【第一次调用】provideUserDataSource()
         执行：return MockUserDataSource()
         结果：创建 MockUserDataSource 单例实例
      ↓
时刻 T5: 【第二次调用】provideUserRepository(dataSource)
         接收：T4 创建的 MockUserDataSource 实例
         执行：return UserRepository(dataSource)
         结果：创建 UserRepository 单例实例
      ↓
时刻 T6: 创建 UserListViewModel(repository)
         接收：T5 创建的 UserRepository 实例
      ↓
时刻 T7: viewModel 初始化完成，可以使用
```

### 4.4 关键规则

✅ **自动触发**：任何需要 `UserRepository` 的地方都会自动解析依赖链  
✅ **只调用一次**：因为 `@Singleton`，`provide` 方法只在第一次执行  
✅ **参数自动注入**：`provideUserRepository(dataSource)` 的参数由 Hilt 自动提供  
✅ **全局复用**：整个应用共享同一个 `UserRepository` 和 `UserDataSource` 实例

---

## 5. Hilt 的编译时魔法

### 5.1 为什么看不到显式调用？

你写的代码：

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 没有显式调用 Hilt？
    }
}
```

### 5.2 Hilt 自动生成的代码

Hilt 的**注解处理器**在编译时自动生成：

```java
// 生成位置：app/build/generated/hilt/component_sources/debug/
public abstract class Hilt_MyApplication extends Application {
    
    // ⚡ Hilt 创建依赖注入的组件管理器
    private final ApplicationComponentManager componentManager = 
        new ApplicationComponentManager(new ComponentSupplier() {
            public Object get() {
                // 构建整个依赖图！
                return DaggerMyApplication_HiltComponents_SingletonC.builder()
                    .applicationContextModule(new ApplicationContextModule(Hilt_MyApplication.this))
                    .build();
            }
        });
    
    @Override
    public void onCreate() {
        hiltInternalInject();  // ⚡ 这里初始化依赖注入！
        super.onCreate();
    }
}
```

### 5.3 真实的继承关系

```text
Application (Android 框架)
    ↑
Hilt_MyApplication (Hilt 自动生成，包含依赖注入逻辑)
    ↑
MyApplication (你写的代码) ← 实际上继承的是 Hilt_MyApplication
```

### 5.4 执行流程

```text
应用启动
    ↓
MyApplication.onCreate() 被调用
    ↓
执行 super.onCreate()  ← 你写的这行
    ↓
调用到 Hilt_MyApplication.onCreate()  ← Hilt 生成的
    ↓
执行 hiltInternalInject()  ← 初始化依赖图
    ↓
创建 DaggerMyApplication_HiltComponents_SingletonC
    ↓
扫描所有 @Module，构建依赖图
    ↓
依赖注入系统就绪 ✅
```

### 5.5 编译时代码生成

1. **编译前**：你的代码只有 `@HiltAndroidApp` 注解
2. **编译时**：注解处理器生成 `Hilt_MyApplication` 基类
3. **编译后**：你的类实际继承的是生成的基类

这就是现代框架的"魔法"——用注解处理器在编译时生成样板代码，让开发代码更简洁！

---

## 6. 完整的架构层次

```text
Fragment (UI 层)
    ↓ 观察
ViewModel (业务逻辑层)
    ↓ 依赖注入
Repository (数据仓库层)
    ↓ 依赖注入
DataSource (数据源层)
    ↓ 实现
MockUserDataSource / RemoteUserDataSource (具体实现)
```

### 各层职责

| 层级 | 职责 | 依赖注入方式 |
|------|------|------------|
| Fragment | 展示 UI，观察数据 | `@AndroidEntryPoint` + `by viewModels()` |
| ViewModel | 管理 UI 状态，处理业务逻辑 | `@HiltViewModel` + `@Inject constructor` |
| Repository | 封装数据来源，提供统一接口 | `DataModule.provideUserRepository()` |
| DataSource | 定义数据获取契约（接口） | `DataModule.provideUserDataSource()` |
| MockUserDataSource | 具体实现（模拟数据） | 直接 `new` 创建 |

---

## 7. 关键结论

1. **Application 是应用的根入口**，Activity 是界面入口
2. **di 文件夹存放依赖注入配置**，定义对象创建规则
3. **Hilt 通过编译时代码生成实现依赖注入**，看不到显式调用是正常的
4. **依赖注入链是自动解析的**，从需求点逆向追溯到数据源
5. **@Singleton 确保只创建一次**，后续访问都是复用单例
6. **依赖注入在首次需要时触发**，不是应用启动时就全部创建
7. **接口 + 实现的模式**实现了松耦合，易于测试和替换

---

## 8. 实用技巧

### 切换数据源

只需修改 `DataModule`：

```kotlin
@Provides
@Singleton
fun provideUserDataSource(): UserDataSource {
    // return MockUserDataSource()  // 开发时用模拟数据
    return RemoteUserDataSource()   // 上线时用真实 API
}
```

### 查看生成的代码

```text
app/build/generated/hilt/component_sources/debug/
```

### 调试依赖注入

在 `provide` 方法中添加日志：

```kotlin
@Provides
@Singleton
fun provideUserDataSource(): UserDataSource {
    Log.d("Hilt", "创建 UserDataSource")
    return MockUserDataSource()
}
```

---

**学习日期**：2025年12月15日  
**关键字**：Android, Hilt, 依赖注入, DI, MVVM, Application, Activity, @HiltAndroidApp, @Module, @Provides, @Singleton, @Inject
