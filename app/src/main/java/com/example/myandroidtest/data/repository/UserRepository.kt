package com.example.myandroidtest.data.repository

import com.example.myandroidtest.data.source.UserDataSource
import com.example.myandroidtest.model.User

/**
 * 用户数据仓库
 *
 * Repository 模式的核心思想：
 * - 作为数据层和业务层之间的中介
 * - 封装数据来源的细节（网络、数据库、缓存等）
 * - 提供统一的数据访问接口
 *
 * 优势：
 * 1. 单一数据源：ViewModel 只需要知道 Repository，不需要关心数据从哪来
 * 2. 易于测试：可以注入 Mock DataSource 进行测试
 * 3. 灵活切换：更换数据源不影响上层业务逻辑
 */
class UserRepository(
    private val dataSource: UserDataSource
) {
    /**
     * 获取用户列表
     *
     * 未来可以在这里添加：
     * - 缓存策略
     * - 数据转换
     * - 错误处理
     * - 多数据源合并
     */
    suspend fun getUsers(): List<User> {
        return dataSource.getUsers()
    }
}
