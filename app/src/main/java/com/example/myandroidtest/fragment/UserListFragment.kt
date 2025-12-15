package com.example.myandroidtest.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myandroidtest.R
import com.example.myandroidtest.adapter.UserAdapter
import com.example.myandroidtest.model.User
import com.example.myandroidtest.viewmodel.UserListViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * UserListFragment - 展示用户列表
 * 
 * @AndroidEntryPoint: 标记这个 Fragment 使用 Hilt 进行依赖注入
 * 
 * 架构升级：完整的 MVVM + Repository + Hilt 模式
 * 
 * 架构层次：
 * Fragment (UI) → ViewModel (业务逻辑) → Repository (数据层) → DataSource (数据源)
 *                     ↑
 *                 Hilt 自动注入
 * 
 * 这个例子展示了：
 * 1. RecyclerView + ViewHolder 的真正使用场景
 * 2. MVVM 架构的完整实现
 * 3. Hilt 依赖注入的使用
 * 4. 关注点分离：UI、业务逻辑、数据层各司其职
 * 
 * 关键观察点：
 * 1. 创建了 50 个用户数据，但只会创建约 10-15 个 ViewHolder
 * 2. 滑动时，ViewHolder 会被复用来显示新数据
 * 3. 查看 Logcat 日志，观察 onCreateViewHolder 和 onBindViewHolder 的调用
 * 
 * 性能对比：
 * - 没有 ViewHolder：50 个 item × 每次滑动都 findViewById = 极慢
 * - 有 ViewHolder：创建 15 个 ViewHolder × findViewById 一次 = 极快
 */
@AndroidEntryPoint
class UserListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvStats: TextView
    private lateinit var userAdapter: UserAdapter
    
    // Hilt 自动注入 ViewModel
    private val viewModel: UserListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("UserListFragment", "╔════════════════════════════════════════╗")
        Log.d("UserListFragment", "║  UserListFragment 启动                 ║")
        Log.d("UserListFragment", "║  MVVM + Repository 完整架构            ║")
        Log.d("UserListFragment", "╚════════════════════════════════════════╝")
        
        val rootView = inflater.inflate(R.layout.fragment_user_list, container, false)
        
        // 初始化视图
        setupViews(rootView)
        
        // 观察数据变化
        observeViewModel()
        
        // 加载数据
        viewModel.loadUsers()
        
        return rootView
    }

    /**
     * 初始化视图
     */
    private fun setupViews(rootView: View) {
        recyclerView = rootView.findViewById(R.id.recyclerView)
        tvStats = rootView.findViewById(R.id.tv_stats)
        
        // 设置 LayoutManager - 控制列表的布局方式
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        Log.d("UserListFragment", "RecyclerView 已初始化")
    }

    /**
     * 观察 ViewModel 的数据变化
     * 
     * LiveData 观察者模式：
     * - 自动感知生命周期
     * - 数据变化时自动更新 UI
     * - 避免内存泄漏
     */
    private fun observeViewModel() {
        // 观察用户列表数据
        viewModel.users.observe(viewLifecycleOwner) { userList ->
            Log.d("UserListFragment", "收到用户数据：${userList.size} 条")
            setupAdapter(userList)
        }
        
        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Log.d("UserListFragment", "⏳ 加载中...")
            } else {
                Log.d("UserListFragment", "✅ 加载完成")
            }
        }
        
        // 观察错误信息
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("UserListFragment", "❌ 加载失败：$it")
            }
        }
    }

    /**
     * 设置 Adapter
     */
    private fun setupAdapter(userList: List<User>) {
        userAdapter = UserAdapter(userList)
        
        // 设置统计回调 - 实时更新创建和绑定次数
        userAdapter.onStatsUpdate = { createCount, bindCount ->
            tvStats.text = getString(R.string.stats_format, createCount, bindCount)
            
            // 关键观察：
            // 创建次数 ≈ 屏幕可见数量 + 2-3 个缓存
            // 绑定次数 = 每次显示新数据时 +1
            Log.d("UserListFragment", "📊 统计：创建=$createCount, 绑定=$bindCount")
        }
        
        recyclerView.adapter = userAdapter
        
        Log.d("UserListFragment", "Adapter 已设置")
        Log.d("UserListFragment", "")
        Log.d("UserListFragment", "🔍 请滑动列表并观察日志！")
        Log.d("UserListFragment", "")
        Log.d("UserListFragment", "观察要点：")
        Log.d("UserListFragment", "1. onCreateViewHolder 只调用 10-15 次（创建 ViewHolder）")
        Log.d("UserListFragment", "2. onBindViewHolder 频繁调用（复用 ViewHolder）")
        Log.d("UserListFragment", "3. 同一个 ViewHolder (相同 ID) 显示不同的数据")
        Log.d("UserListFragment", "4. ViewModel 管理数据，Fragment 只负责展示")
        Log.d("UserListFragment", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("UserListFragment", "╔════════════════════════════════════════╗")
        Log.d("UserListFragment", "║  UserListFragment 销毁                 ║")
        Log.d("UserListFragment", "╚════════════════════════════════════════╝")
    }
}
