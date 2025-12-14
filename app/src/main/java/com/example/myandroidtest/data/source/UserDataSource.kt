package com.example.myandroidtest.data.source

import com.example.myandroidtest.model.User

/**
 * 用户数据源接口
 * 
 * 定义获取用户数据的契约，具体实现可以是：
 * - MockUserDataSource: 本地模拟数据
 * - RemoteUserDataSource: 远程 API 数据
 * - LocalUserDataSource: 本地数据库数据
 * 
 * 这种抽象使得数据来源可以随时切换，而不影响上层业务逻辑
 */
interface UserDataSource {
    /**
     * 获取用户列表
     * 使用 suspend 关键字支持协程，便于处理异步操作
     */
    suspend fun getUsers(): List<User>
}
