package com.example.myandroidtest.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myandroidtest.R
import com.example.myandroidtest.adapter.UserAdapter
import com.example.myandroidtest.model.User

/**
 * UserListFragment - 展示用户列表
 * 
 * 这个例子展示了 RecyclerView + ViewHolder 的真正使用场景！
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
class UserListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvStats: TextView
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("UserListFragment", "╔════════════════════════════════════════╗")
        Log.d("UserListFragment", "║  UserListFragment 启动                 ║")
        Log.d("UserListFragment", "║  准备展示 RecyclerView + ViewHolder    ║")
        Log.d("UserListFragment", "╚════════════════════════════════════════╝")
        
        val rootView = inflater.inflate(R.layout.fragment_user_list, container, false)
        
        // 初始化视图
        setupViews(rootView)
        
        // 准备数据
        val userList = generateUserList()
        Log.d("UserListFragment", "生成了 ${userList.size} 个用户数据")
        
        // 设置 Adapter
        setupAdapter(userList)
        
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
     * 设置 Adapter
     */
    private fun setupAdapter(userList: List<User>) {
        userAdapter = UserAdapter(userList)
        
        // 设置统计回调 - 实时更新创建和绑定次数
        userAdapter.onStatsUpdate = { createCount, bindCount ->
            tvStats.text = "创建次数: $createCount | 绑定次数: $bindCount"
            
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
        Log.d("UserListFragment", "")
    }

    /**
     * 生成测试用户数据
     * 
     * 创建 50 个用户数据来演示：
     * - 虽然有 50 条数据
     * - 但只会创建约 10-15 个 ViewHolder
     * - 这就是 ViewHolder 的价值！
     */
    private fun generateUserList(): List<User> {
        val names = listOf(
            "张三", "李四", "王五", "赵六", "孙七",
            "周八", "吴九", "郑十", "冯一", "陈二",
            "褚三", "卫四", "蒋五", "沈六", "韩七",
            "杨八", "朱九", "秦十", "尤一", "许二",
            "何三", "吕四", "施五", "张六", "孔七",
            "曹八", "严九", "华十", "金一", "魏二",
            "陶三", "姜四", "戚五", "谢六", "邹七",
            "喻八", "柏九", "水十", "窦一", "章二",
            "云三", "苏四", "潘五", "葛六", "奚七",
            "范八", "彭九", "郎十", "鲁一", "韦二"
        )

        return names.mapIndexed { index, name ->
            User(
                id = index + 1,
                name = name,
                email = "${name.lowercase()}${index + 1}@example.com",
                age = 20 + (index % 30)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("UserListFragment", "╔════════════════════════════════════════╗")
        Log.d("UserListFragment", "║  UserListFragment 销毁                 ║")
        Log.d("UserListFragment", "╚════════════════════════════════════════╝")
    }
}
