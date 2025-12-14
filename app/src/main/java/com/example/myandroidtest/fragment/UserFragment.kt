package com.example.myandroidtest.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myandroidtest.model.User
import com.example.myandroidtest.ui.theme.MyAndroidTestTheme
import com.example.myandroidtest.viewmodel.UserViewModel

/**
 * 用户信息 Fragment
 * 使用 Fragment 将 UI 模块化，便于复用和管理
 */
class UserFragment : Fragment() {
    
    // 使用 viewModels() 委托创建 ViewModel
    private val userViewModel: UserViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("UserFragment", "onCreateView 被调用")
        
        // 在 Fragment 中使用 Compose
        return ComposeView(requireContext()).apply {
            setContent {
                MyAndroidTestTheme {
                    UserScreen(viewModel = userViewModel)
                }
            }
        }
    }
    
    /**
     * 用户信息展示界面
     */
    @Composable
    private fun UserScreen(viewModel: UserViewModel) {
        // 观察 ViewModel 中的 LiveData
        val user by viewModel.user.observeAsState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MVVM 架构示例",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "（使用 Fragment）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // 显示用户信息卡片
            user?.let {
                UserInfoCard(user = it)
            } ?: run {
                Text("暂无用户数据", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 加载用户数据按钮
            Button(onClick = {
                Log.d("UserFragment", "加载用户数据")
                viewModel.loadUser()
            }) {
                Text("加载用户数据")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 更新���户数据按钮
            Button(
                onClick = {
                    Log.d("UserFragment", "更新用户数据")
                    viewModel.updateUser(
                        name = "李四",
                        email = "lisi@example.com",
                        age = 30
                    )
                },
                enabled = user != null
            ) {
                Text("更新用户信息")
            }
        }
    }
    
    /**
     * 用户信息卡片
     */
    @Composable
    private fun UserInfoCard(user: User) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "ID: ${user.id}", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "姓名: ${user.name}", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "邮箱: ${user.email}", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "年龄: ${user.age}", fontSize = 16.sp)
            }
        }
    }
}
