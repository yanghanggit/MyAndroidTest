package com.example.myandroidtest.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myandroidtest.R
import com.example.myandroidtest.model.User

/**
 * UserAdapter - RecyclerView 的适配器
 * 
 * 这是 ViewHolder 真正发挥作用的地方！
 * 
 * 核心概念：
 * 1. RecyclerView 只创建少量的 ViewHolder（大约屏幕可见数量 + 2）
 * 2. 当滑动时，滑出屏幕的 ViewHolder 会被复用来显示新数据
 * 3. ViewHolder 保证每个 View 对象的控件引用始终正确
 */
class UserAdapter(private val userList: List<User>) : 
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // 统计数据 - 用于演示
    private var createCount = 0  // 创建了多少个 ViewHolder
    private var bindCount = 0    // 绑定了多少次数据
    
    // 回调接口 - 向 Fragment 报告统计数据
    var onStatsUpdate: ((createCount: Int, bindCount: Int) -> Unit)? = null

    /**
     * 步骤 1: 创建 ViewHolder
     * 
     * 重要：这个方法只在需要新的 ViewHolder 时才调用！
     * - 首次显示时：创建屏幕可见的数量 + 缓存的数量（大约 10-15 个）
     * - 滑动时：如果有可复用的 ViewHolder，就不会调用这个方法
     * 
     * 这就是性能提升的关键！
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        createCount++
        Log.d("UserAdapter", "═══════════════════════════════════════════")
        Log.d("UserAdapter", "⭐ onCreateViewHolder 被调用！")
        Log.d("UserAdapter", "   创建第 $createCount 个 ViewHolder")
        Log.d("UserAdapter", "   这是一次完整的创建：inflate + findViewById")
        Log.d("UserAdapter", "═══════════════════════════════════════════")
        
        // 1. 创建 View
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_card, parent, false)
        
        // 2. 创建 ViewHolder（内部会执行 findViewById 缓存控件）
        val holder = UserViewHolder(view)
        
        // 更新统计
        onStatsUpdate?.invoke(createCount, bindCount)
        
        return holder
    }

    /**
     * 步骤 2: 绑定数据到 ViewHolder
     * 
     * 重要：这个方法会频繁调用！
     * - 首次显示：每个可见的 item 调用一次
     * - 滑动时：每次复用 ViewHolder 时都会调用
     * 
     * 由于 ViewHolder 已经缓存了控件引用，
     * 这里不需要 findViewById，直接赋值即可，非常快！
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        bindCount++
        val user = userList[position]
        
        Log.d("UserAdapter", "─────────────────────────────────────────")
        Log.d("UserAdapter", "🔄 onBindViewHolder 被调用")
        Log.d("UserAdapter", "   位置: $position")
        Log.d("UserAdapter", "   数据: ${user.name}")
        Log.d("UserAdapter", "   ViewHolder ID: ${holder.hashCode()}")
        Log.d("UserAdapter", "   绑定次数: $bindCount")
        Log.d("UserAdapter", "   💡 复用 ViewHolder，无需 findViewById！")
        Log.d("UserAdapter", "─────────────────────────────────────────")
        
        // 使用 ViewHolder 绑定数据
        holder.bind(user)
        
        // 更新统计
        onStatsUpdate?.invoke(createCount, bindCount)
    }

    /**
     * 返回数据总数
     */
    override fun getItemCount(): Int = userList.size

    /**
     * UserViewHolder - ViewHolder 类
     * 
     * 这就是 ViewHolder 的核心！
     * 
     * 职责：
     * 1. 在构造时查找并缓存控件引用（findViewById 只执行一次）
     * 2. 提供 bind() 方法更新数据（直接使用缓存的引用）
     * 
     * 生命周期：
     * - 创建：onCreateViewHolder 时创建
     * - 复用：从回收池取出，重新绑定数据
     * - 销毁：RecyclerView 销毁时
     * 
     * 关键：一个 ViewHolder 对象会被多次复用来显示不同的数据！
     */
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        // ⭐ 缓存的控件引用 - 在构造时查找并保存，只执行一次！
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvEmail: TextView = itemView.findViewById(R.id.tv_email)
        private val tvAge: TextView = itemView.findViewById(R.id.tv_age)
        
        init {
            Log.d("UserViewHolder", "   📦 ViewHolder 创建 (ID: ${this.hashCode()})")
            Log.d("UserViewHolder", "   📦 findViewById 执行了 3 次（tvName, tvEmail, tvAge）")
            Log.d("UserViewHolder", "   📦 这些引用会被缓存，下次复用时直接用！")
        }

        /**
         * 绑定数据到 View
         * 
         * 关键：这里直接使用缓存的控件引用，不需要再次 findViewById！
         * 
         * 这个方法会被频繁调用：
         * - 第一次显示数据时调用
         * - 滑动时 ViewHolder 被复用时调用
         * 
         * 但无论调用多少次，都不需要再次查找控件，性能极高！
         */
        fun bind(user: User) {
            // 直接使用缓存的引用，快速赋值
            tvName.text = "姓名: ${user.name}"
            tvEmail.text = "邮箱: ${user.email}"
            tvAge.text = "年龄: ${user.age} 岁"
            
            Log.d("UserViewHolder", "      ✅ 数据已绑定（使用缓存的控件引用）")
        }
    }
}
