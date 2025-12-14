package com.example.myandroidtest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myandroidtest.model.User

/**
 * 用户 ViewModel
 * 负责管理 User 数据并向 UI 层提供数据
 */
class UserViewModel : ViewModel() {

    // 私有的可变 LiveData，只能在 ViewModel 内部修改
    private val _user = MutableLiveData<User>()

    // 公开的只读 LiveData，供 UI 层观察
    val user: LiveData<User> = _user

    /**
     * 加载用户数据（模拟从数据源获取）
     */
    fun loadUser() {
        // 模拟数据
        val sampleUser = User(
            id = 1,
            name = "张三",
            email = "zhangsan@example.com",
            age = 25
        )
        _user.value = sampleUser
    }

    /**
     * 更新用户信息
     */
    fun updateUser(name: String, email: String, age: Int) {
        val currentUser = _user.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(
                name = name,
                email = email,
                age = age
            )
            _user.value = updatedUser
        }
    }
}

