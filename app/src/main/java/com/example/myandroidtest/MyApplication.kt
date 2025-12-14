package com.example.myandroidtest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application 类
 * 
 * @HiltAndroidApp 注解触发 Hilt 的代码生成
 * 包括用于依赖注入的基类和组件
 * 
 * 这是使用 Hilt 的第一步，必须在 AndroidManifest.xml 中注册
 */
@HiltAndroidApp
class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Hilt 会在这里初始化依赖图
    }
}
