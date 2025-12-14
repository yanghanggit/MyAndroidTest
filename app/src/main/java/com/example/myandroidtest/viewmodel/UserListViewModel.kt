package com.example.myandroidtest.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myandroidtest.data.repository.UserRepository
import com.example.myandroidtest.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户列表 ViewModel
 * 
 * @HiltViewModel: 标记这是一个由 Hilt 管理的 ViewModel
 * @Inject constructor: 告诉 Hilt 使用构造函数注入依赖
 * 
 * Hilt 会自动：
 * 1. 创建 ViewModel 实例
 * 2. 注入 UserRepository 依赖
 * 3. 管理 ViewModel 生命周期
 * 
 * 职责：
 * 1. 从 Repository 获取数据
 * 2. 管理 UI 状态（加载中、成功、失败）
 * 3. 向 UI 层暴露 LiveData
 * 4. 处理业务逻辑
 * 
 * 生命周期：
 * - 比 Fragment 存活时间更长
 * - 屏幕旋转时数据不会丢失
 * - Activity/Fragment 销毁时才被清除
 */
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    // UI 状态封装
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * 加载用户列表
     * 
     * 使用协程处理异步操作：
     * - viewModelScope: ViewModel 作用域的协程
     * - 自动处理生命周期，ViewModel 销毁时自动取消
     */
    fun loadUsers() {
        Log.d("UserListViewModel", "开始加载用户数据...")
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // 从 Repository 获取数据
                val userList = repository.getUsers()
                
                _users.value = userList
                _isLoading.value = false
                
                Log.d("UserListViewModel", "用户数据加载成功：${userList.size} 条")
                
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "加载失败"
                
                Log.e("UserListViewModel", "用户数据加载失败", e)
            }
        }
    }

    /**
     * ViewModel 被清除时调用
     */
    override fun onCleared() {
        super.onCleared()
        Log.d("UserListViewModel", "UserListViewModel 被清除")
    }
}
