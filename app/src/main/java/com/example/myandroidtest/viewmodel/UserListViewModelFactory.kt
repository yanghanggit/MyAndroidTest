package com.example.myandroidtest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myandroidtest.data.repository.UserRepository

/**
 * UserListViewModel 的工厂类
 * 
 * 为什么需要 Factory？
 * - ViewModel 默认构造函数不能有参数
 * - 需要通过 Factory 来注入依赖（如 Repository）
 * - 这是依赖注入的一种实现方式
 * 
 * 未来可以用 Hilt 或 Koin 等 DI 框架替代手动创建 Factory
 */
class UserListViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserListViewModel::class.java)) {
            return UserListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
