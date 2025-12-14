package com.example.myandroidtest.model

/**
 * 用户数据模型
 * 这是 MVVM 架构中的 Model 层最简单的示例
 */
data class User(
    val id: Int,           // 用户ID
    val name: String,      // 用户名
    val email: String,     // 邮箱
    val age: Int = 0       // 年龄，默认值为0
)
