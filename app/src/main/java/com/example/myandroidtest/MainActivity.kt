package com.example.myandroidtest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroidtest.fragment.UserListFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity 现在作为 Fragment 的容器
 * 使用 Fragment 实现模块化的 UI 架构
 * 
 * @AndroidEntryPoint: 标记这个 Activity 使用 Hilt
 * 这是必须的，因为内部的 Fragment 使用了 Hilt
 * 
 * 当前展示：RecyclerView + ViewHolder + MVVM + Hilt 完整架构演示
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate 被调用 - RecyclerView 列表演示")
        
        // 设置布局
        setContentView(R.layout.activity_main)
        
        // 只在第一次创建时添加 Fragment（避免配置更改时重复添加）
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                // 使用 UserListFragment 展示 RecyclerView + ViewHolder
                .replace(R.id.fragment_container, UserListFragment())
                .commit()
        }
    }
}