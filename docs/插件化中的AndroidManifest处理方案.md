# 插件化中的 AndroidManifest.xml 处理方案

## 一、核心问题

### 问题本质

在插件化场景中，插件 APK 面临的根本问题是：

```text
正常 APP：
安装时 → 系统读取 AndroidManifest.xml 
      → 注册所有组件（Activity、Service等）
      → 系统知道这些组件的存在
      → 可以正常启动和运行

插件 APK：
没有安装 → 系统不知道插件的存在 
        → Manifest 不会被系统读取
        → 插件中的组件无法直接启动 ❌
        → 权限声明不会生效 ❌
```

### 问题描述

```text
插件是运行时动态下载的
  ↓
没有通过 PackageManager 安装
  ↓
系统不会解析插件的 AndroidManifest.xml
  ↓
如何启动插件中的 Activity？
如何使用插件需要的权限？
```

---

## 二、Shadow 的解决方案：占坑 + 代理机制

### 核心思路

**"欺骗" Android 系统**：让系统以为启动的是宿主 APP 中已注册的组件，实际执行的是插件的代码。

### 实现机制

#### 1. 宿主 APP 预埋占坑组件

在宿主 APP 的 AndroidManifest.xml 中预先声明多个"占坑"组件（容器组件）：

```xml
<!-- 宿主 APP 的 AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.host">

    <application>
        <!-- 真实的宿主 Activity -->
        <activity android:name=".MainActivity" 
                  android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- ========== 预埋的占坑 Activity（代理/容器） ========== -->
        <!-- 标准启动模式 -->
        <activity android:name=".container.ContainerActivity1" />
        <activity android:name=".container.ContainerActivity2" />
        <activity android:name=".container.ContainerActivity3" />
        
        <!-- singleTask 启动模式 -->
        <activity 
            android:name=".container.ContainerActivitySingleTask"
            android:launchMode="singleTask" />
        
        <!-- singleTop 启动模式 -->
        <activity 
            android:name=".container.ContainerActivitySingleTop"
            android:launchMode="singleTop" />
        
        <!-- 竖屏 -->
        <activity 
            android:name=".container.ContainerActivityPortrait"
            android:screenOrientation="portrait" />
        
        <!-- 横屏 -->
        <activity 
            android:name=".container.ContainerActivityLandscape"
            android:screenOrientation="landscape" />
        
        <!-- Service 占坑 -->
        <service android:name=".container.ContainerService1" />
        <service android:name=".container.ContainerService2" />
        
        <!-- BroadcastReceiver 通常动态注册，不需要占坑 -->
        <!-- ContentProvider 可能需要特殊处理 -->
    </application>
</manifest>
```

#### 2. 运行时替换机制

当需要启动插件 Activity 时，Shadow 框架进行拦截和替换：

```kotlin
// ========== 用户代码 ==========
// 启动插件中的 LoginActivity
pluginManager.startPluginActivity("com.example.plugin.LoginActivity")

// ========== Shadow 内部处理（简化版） ==========
fun startPluginActivity(pluginActivityName: String) {
    // 1. 加载插件 Activity 类
    val pluginClass = pluginClassLoader.loadClass(pluginActivityName)
    
    // 2. 根据插件 Activity 的属性选择合适的占坑 Activity
    val containerActivityClass = selectContainerActivity(pluginClass)
    // 例如：如果插件 Activity 需要竖屏 → 选择 ContainerActivityPortrait
    
    // 3. 创建 Intent，指向占坑 Activity
    val intent = Intent(context, containerActivityClass)
    
    // 4. 将真实插件类名作为参数传递
    intent.putExtra("plugin_class_name", pluginActivityName)
    intent.putExtra("plugin_extras", originalExtras)  // 原始参数
    
    // 5. 启动占坑 Activity（系统允许，因为已在 Manifest 中声明）
    context.startActivity(intent)
}

// 根据需求选择合适的占坑 Activity
fun selectContainerActivity(pluginClass: Class<*>): Class<*> {
    return when {
        needsPortrait(pluginClass) -> ContainerActivityPortrait::class.java
        needsSingleTask(pluginClass) -> ContainerActivitySingleTask::class.java
        else -> ContainerActivity1::class.java
    }
}
```

#### 3. 占坑 Activity 运行插件代码

占坑 Activity 是一个"空壳"，它的职责是承载和代理插件 Activity：

```kotlin
// ========== 容器 Activity（宿主中的占坑组件） ==========
class ContainerActivity1 : Activity() {
    
    // 真实的插件 Activity 实例
    private lateinit var pluginActivity: Any
    
    // 插件 Activity 的生命周期代理接口
    private lateinit var pluginActivityDelegate: PluginActivityInterface
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. 获取真实插件类名
        val pluginClassName = intent.getStringExtra("plugin_class_name")
            ?: throw IllegalArgumentException("Plugin class name not found")
        
        // 2. 通过插件 ClassLoader 实例化插件 Activity
        pluginActivity = pluginClassLoader.loadClass(pluginClassName).newInstance()
        
        // 3. 注入容器的 Context（让插件 Activity 使用宿主的 Context）
        injectContext(pluginActivity, this)
        
        // 4. 调用插件 Activity 的 onCreate
        pluginActivityDelegate = pluginActivity as PluginActivityInterface
        pluginActivityDelegate.onCreate(savedInstanceState)
        
        // 5. 设置插件的 ContentView
        val pluginView = pluginActivityDelegate.getContentView()
        setContentView(pluginView)
    }
    
    override fun onStart() {
        super.onStart()
        pluginActivityDelegate.onStart()
    }
    
    override fun onResume() {
        super.onResume()
        pluginActivityDelegate.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        pluginActivityDelegate.onPause()
    }
    
    override fun onStop() {
        super.onStop()
        pluginActivityDelegate.onStop()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        pluginActivityDelegate.onDestroy()
    }
    
    // 代理其他方法
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pluginActivityDelegate.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onBackPressed() {
        if (!pluginActivityDelegate.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
```

### 架构示意图

```text
用户操作：
启动 "插件 LoginActivity" 
  ↓
Shadow 框架拦截：
选择合适的占坑 Activity（如 ContainerActivity1）
  ↓
系统实际运行：
启动 ContainerActivity1（宿主中已注册，合法）
  ↓
容器内部：
加载并实例化插件 LoginActivity
代理所有生命周期方法
  ↓
最终效果：
用户看到的是插件 LoginActivity 的界面
系统认为运行的是 ContainerActivity1
```

### 完整流程图

```text
┌─────────────────────────────────────────┐
│  用户调用：启动插件 Activity             │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│  Shadow 框架拦截 startActivity          │
│  - 解析插件 Activity 类名               │
│  - 选择合适的占坑 Activity              │
│  - 替换 Intent 的 target                │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│  Android 系统启动占坑 Activity          │
│  （系统认为这是合法的宿主组件）          │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│  占坑 Activity.onCreate()               │
│  - 获取真实插件类名                      │
│  - 加载插件 Activity 类                 │
│  - 实例化插件 Activity                  │
│  - 注入 Context 和资源                  │
│  - 调用插件 Activity.onCreate()         │
│  - 设置插件的 ContentView               │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│  插件 Activity 正常运行                 │
│  - 所有生命周期由容器代理               │
│  - 使用宿主的 Context                   │
│  - 访问插件的资源                       │
└─────────────────────────────────────────┘
```

---

## 三、权限问题

### 核心规则

**插件的权限必须在宿主 APP 的 Manifest 中声明！**

### 为什么？

```text
插件 APK 没有被系统安装
  ↓
插件的 AndroidManifest.xml 不会被系统读取
  ↓
插件中声明的 <uses-permission> 不会生效
  ↓
插件只能使用宿主 APP 已声明的权限
```

### 实际影响示例

```kotlin
// ========== 插件的 AndroidManifest.xml（不会生效） ==========
<manifest>
    <!-- ❌ 这个权限声明不会被系统识别 -->
    <uses-permission android:name="android.permission.CAMERA" />
</manifest>

// ========== 插件代码 ==========
class PluginPhotoActivity : Activity() {
    fun takePhoto() {
        // 如果宿主 Manifest 没有声明 CAMERA 权限
        // ❌ 这里会抛出 SecurityException
        camera.takePicture()
    }
}
```

### 解决方案

#### 方案 1：宿主预先声明所有权限（不推荐）

```xml
<!-- 宿主 APP 的 AndroidManifest.xml -->
<manifest>
    <!-- 声明所有可能的权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <!-- ... 更多权限 -->
</manifest>
```

**缺点**：

- ❌ 用户体验差（权限列表太长，用户可能拒绝安装）
- ❌ 安全风险（授予了不必要的权限）
- ❌ 应用商店可能拒绝上架
- ❌ 违反最小权限原则

#### 方案 2：按需声明（推荐）

```xml
<!-- 宿主 APP 的 AndroidManifest.xml -->
<manifest>
    <!-- 只声明确定需要的权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</manifest>
```

**优点**：

- ✅ 安全可控
- ✅ 用户体验好
- ✅ 符合最小权限原则

**注意事项**：

- ⚠️ 需要提前规划好所有插件的权限需求
- ⚠️ 插件功能受限于宿主声明的权限
- ⚠️ 新插件需要新权限时，需要更新宿主 APP

#### 方案 3：动态权限管理和检查

```kotlin
// ========== 宿主提供权限管理接口 ==========
interface HostPermissionManager {
    /**
     * 检查权限是否已授予
     */
    fun checkPermission(permission: String): Boolean
    
    /**
     * 请求权限（如果宿主 Manifest 中已声明）
     */
    fun requestPermission(
        permission: String, 
        callback: (granted: Boolean) -> Unit
    )
    
    /**
     * 检查宿主是否支持某个权限
     */
    fun isPermissionSupported(permission: String): Boolean
}

// ========== 插件调用 ==========
class PluginPhotoActivity : Activity() {
    
    private lateinit var hostPermissionManager: HostPermissionManager
    
    fun takePhoto() {
        // 1. 检查宿主是否支持 CAMERA 权限
        if (!hostPermissionManager.isPermissionSupported(Manifest.permission.CAMERA)) {
            Toast.makeText(this, "宿主不支持相机功能", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 2. 检查权限是否已授予
        if (!hostPermissionManager.checkPermission(Manifest.permission.CAMERA)) {
            // 3. 请求权限
            hostPermissionManager.requestPermission(Manifest.permission.CAMERA) { granted ->
                if (granted) {
                    performTakePhoto()
                } else {
                    Toast.makeText(this, "需要相机权限", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            performTakePhoto()
        }
    }
    
    private fun performTakePhoto() {
        // 执行拍照逻辑
        camera.takePicture()
    }
}
```

#### 方案 4：插件声明所需权限（元数据）

```kotlin
// ========== 插件在代码中声明所需权限 ==========
class PluginManifest {
    companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}

// ========== 宿主在加载插件前检查 ==========
fun loadPlugin(pluginPath: String): Boolean {
    // 1. 加载插件
    val classLoader = DexClassLoader(pluginPath, ...)
    
    // 2. 读取插件所需权限
    val manifestClass = classLoader.loadClass("com.example.plugin.PluginManifest")
    val requiredPermissions = manifestClass.getField("REQUIRED_PERMISSIONS").get(null) as Array<String>
    
    // 3. 检查宿主是否支持这些权限
    val unsupportedPermissions = requiredPermissions.filter { permission ->
        !isPermissionDeclaredInHost(permission)
    }
    
    // 4. 如果有不支持的权限，提示用户或拒绝加载
    if (unsupportedPermissions.isNotEmpty()) {
        Log.e("Plugin", "宿主不支持以下权限: $unsupportedPermissions")
        return false
    }
    
    return true
}
```

---

## 四、其他 Manifest 配置处理

### 1. 四大组件的占坑策略

#### Activity 占坑

```xml
<manifest>
    <application>
        <!-- 不同启动模式的占坑 -->
        <activity android:name=".container.StandardActivity1" />
        <activity android:name=".container.StandardActivity2" />
        
        <activity 
            android:name=".container.SingleTopActivity"
            android:launchMode="singleTop" />
        
        <activity 
            android:name=".container.SingleTaskActivity"
            android:launchMode="singleTask" />
        
        <activity 
            android:name=".container.SingleInstanceActivity"
            android:launchMode="singleInstance" />
        
        <!-- 不同屏幕方向的占坑 -->
        <activity 
            android:name=".container.PortraitActivity"
            android:screenOrientation="portrait" />
        
        <activity 
            android:name=".container.LandscapeActivity"
            android:screenOrientation="landscape" />
        
        <!-- 透明主题的占坑 -->
        <activity 
            android:name=".container.TranslucentActivity"
            android:theme="@android:style/Theme.Translucent.NoTitleBar" />
    </application>
</manifest>
```

#### Service 占坑

```xml
<manifest>
    <application>
        <!-- Service 占坑 -->
        <service android:name=".container.ContainerService1" />
        <service android:name=".container.ContainerService2" />
        <service android:name=".container.ContainerService3" />
    </application>
</manifest>
```

```kotlin
// Service 代理实现
class ContainerService1 : Service() {
    private lateinit var pluginService: Any
    
    override fun onCreate() {
        super.onCreate()
        // 加载并实例化插件 Service
        val pluginClassName = /* 从启动参数获取 */
        pluginService = pluginClassLoader.loadClass(pluginClassName).newInstance()
        pluginService.onCreate()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return pluginService.onStartCommand(intent, flags, startId)
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return pluginService.onBind(intent)
    }
}
```

#### BroadcastReceiver 处理

```kotlin
// BroadcastReceiver 通常采用动态注册，不需要占坑
// 宿主提供注册接口
interface HostReceiverManager {
    fun registerReceiver(
        receiver: BroadcastReceiver,
        filter: IntentFilter
    )
    
    fun unregisterReceiver(receiver: BroadcastReceiver)
}

// 插件使用
val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 处理广播
    }
}
val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
hostReceiverManager.registerReceiver(receiver, filter)
```

#### ContentProvider 处理

```xml
<!-- ContentProvider 相对复杂，通常需要占坑 -->
<manifest>
    <application>
        <provider
            android:name=".container.ContainerProvider"
            android:authorities="com.example.host.provider"
            android:exported="false" />
    </application>
</manifest>
```

### 2. 主题和样式

```kotlin
// 插件可以有自己的主题资源
// 但 Activity 的主题需要通过占坑 Activity 预设或动态设置

// 动态设置主题
class ContainerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 在 super.onCreate 之前设置主题
        val themeResId = getPluginThemeResId()
        setTheme(themeResId)
        
        super.onCreate(savedInstanceState)
    }
}
```

### 3. Intent Filter

```xml
<!-- 插件的 Intent Filter 无法直接生效 -->
<!-- 需要通过宿主的占坑 Activity 中转 -->

<manifest>
    <application>
        <activity android:name=".container.ContainerActivity">
            <!-- 宿主可以声明一个通用的 Intent Filter -->
            <intent-filter>
                <action android:name="com.example.PLUGIN_ACTION" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

```kotlin
// 容器 Activity 根据参数分发到具体插件
class ContainerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        when (intent.action) {
            "com.example.PLUGIN_ACTION" -> {
                val pluginId = intent.getStringExtra("plugin_id")
                loadAndStartPlugin(pluginId)
            }
        }
    }
}
```

---

## 五、实际开发建议

### 1. 宿主 APP 设计阶段

#### 规划占坑组件

```kotlin
// 计算需要的占坑数量
// 考虑因素：
// - 最多同时运行的插件 Activity 数量
// - 不同启动模式的需求
// - 不同屏幕方向的需求
// - 不同主题的需求

// 示例：预埋 10 个标准 Activity
for (i in 1..10) {
    <activity android:name=".container.StandardActivity$i" />
}
```

#### 规划权限声明

```kotlin
// 创建权限清单文档
// 列出所有插件可能需要的权限
// 评估每个权限的必要性和风险

/**
 * 宿主 APP 权限规划
 * 
 * 基础权限（必须）：
 * - INTERNET: 所有插件都需要网络访问
 * 
 * 常用权限（推荐）：
 * - READ_EXTERNAL_STORAGE: 文件访问
 * - WRITE_EXTERNAL_STORAGE: 文件写入
 * - CAMERA: 拍照功能
 * 
 * 敏感权限（谨慎）：
 * - ACCESS_FINE_LOCATION: 精确定位
 * - READ_CONTACTS: 读取联系人
 * - RECORD_AUDIO: 录音
 */
```

### 2. 插件开发阶段

#### 遵守权限约束

```kotlin
// 插件开发时明确依赖的宿主权限
class PluginActivity : Activity() {
    
    companion object {
        // 声明插件需要的权限
        val REQUIRED_PERMISSIONS = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检查权限是否可用
        checkRequiredPermissions()
    }
    
    private fun checkRequiredPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter { permission ->
            !isPermissionGranted(permission)
        }
        
        if (missingPermissions.isNotEmpty()) {
            // 提示缺少权限或降级功能
            showPermissionWarning(missingPermissions)
        }
    }
}
```

#### 通过接口请求宿主能力

```kotlin
// 定义插件与宿主的通信接口
interface HostInterface {
    fun getPermissionManager(): HostPermissionManager
    fun getResourceManager(): HostResourceManager
    fun showToast(message: String)
}

// 插件使用宿主接口
class PluginActivity : Activity() {
    private lateinit var hostInterface: HostInterface
    
    fun requestHostCapability() {
        if (hostInterface.getPermissionManager().checkPermission(CAMERA)) {
            // 使用相机
        } else {
            hostInterface.showToast("需要相机权限")
        }
    }
}
```

### 3. 测试阶段

#### 权限测试清单

```kotlin
/**
 * 插件权限测试清单
 * 
 * □ 插件所需权限是否在宿主 Manifest 中声明
 * □ 运行时权限是否正确请求
 * □ 权限被拒绝时的降级处理
 * □ 缺少权限时的友好提示
 * □ 不同 Android 版本的权限兼容性
 */
```

#### 组件测试清单

```kotlin
/**
 * 插件组件测试清单
 * 
 * Activity 测试：
 * □ 不同启动模式是否正常
 * □ 生命周期是否正确代理
 * □ 界面是否正常显示
 * □ 返回栈是否正确
 * 
 * Service 测试：
 * □ 启动和绑定是否正常
 * □ 后台运行是否稳定
 * 
 * BroadcastReceiver 测试：
 * □ 动态注册是否生效
 * □ 接收广播是否及时
 * 
 * ContentProvider 测试：
 * □ 数据查询是否正常
 * □ 跨进程访问是否可用
 */
```

---

## 六、局限性和注意事项

### 插件化的固有局限

#### 1. 权限限制

```text
❌ 插件无法动态申请宿主未声明的权限
❌ 新功能需要新权限时，必须更新宿主 APP
✅ 可以通过服务端控制插件下发，但权限受限于宿主
```

#### 2. 组件数量限制

```text
❌ 占坑组件数量有限
❌ 同时运行的插件组件受限于占坑数量
✅ 可以动态复用占坑组件，但需要管理复杂度
```

#### 3. 配置受限

```text
❌ 某些 Manifest 配置必须在宿主中预设
❌ 动态修改系统级配置困难
✅ 可以通过代理机制在运行时动态调整部分配置
```

#### 4. 兼容性问题

```text
⚠️ 不同 Android 版本对组件管理的机制不同
⚠️ 系统升级可能影响插件化方案
⚠️ 需要持续维护和适配
```

### Shadow 框架的优势

```text
✅ 编译期处理：避免运行时 Hook，更加稳定
✅ 零反射：减少兼容性问题和性能开销
✅ 完整的生命周期管理：正确代理所有组件生命周期
✅ 资源隔离：插件资源独立，不会冲突
✅ 成熟的工业级方案：经过大规模商业应用验证
```

### 最佳实践总结

```text
1. 提前规划
   - 明确插件的功能边界
   - 设计合理的权限策略
   - 预留足够的占坑组件

2. 接口设计
   - 定义清晰的宿主-插件通信接口
   - 提供完善的能力查询机制
   - 实现优雅的降级策略

3. 测试覆盖
   - 多设备、多版本测试
   - 权限场景全覆盖
   - 异常情况处理测试

4. 文档完善
   - 维护权限清单文档
   - 记录占坑组件使用情况
   - 提供插件开发指南
```

---

## 七、总结

### 核心原理

插件化框架通过 **"占坑 + 代理"** 机制，让 Android 系统误以为在运行宿主 APP 中已注册的组件，实际执行的是动态加载的插件代码。

### 关键技术点

1. **组件占坑**：在宿主 Manifest 中预埋容器组件
2. **Intent 替换**：拦截并替换启动目标为占坑组件
3. **生命周期代理**：容器组件代理插件组件的所有生命周期
4. **权限共享**：插件使用宿主已声明的权限
5. **资源隔离**：通过独立的 ClassLoader 和 Resources 管理插件资源

### 适用场景

```text
✅ 业务模块频繁更新
✅ 按需加载功能模块
✅ 减少主包体积
✅ 实现模块化开发和独立发布
```

### 不适用场景

```text
❌ 需要频繁申请新权限的应用
❌ 插件功能过于复杂（如完整的独立 APP）
❌ 对性能要求极高的场景
❌ 团队技术储备不足
```

---
