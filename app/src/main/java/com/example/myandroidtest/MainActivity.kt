package com.example.myandroidtest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroidtest.fragment.UserListFragment

/**
 * MainActivity 现在作为 Fragment 的容器
 * 使用 Fragment 实现模块化的 UI 架构
 * 
 * 当前展示：RecyclerView + ViewHolder + MVVM 完整架构演示
 */
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