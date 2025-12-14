package com.example.myandroidtest.data.source

import com.example.myandroidtest.model.User
import kotlinx.coroutines.delay

/**
 * 模拟数据源实现
 * 
 * 提供测试用的模拟用户数据
 * 在实际开发中，这可以帮助：
 * 1. 前端独立开发，不依赖后端 API
 * 2. 编写单元测试
 * 3. 演示和原型开发
 */
class MockUserDataSource : UserDataSource {
    
    /**
     * 获取模拟用户列表
     * 
     * 创建 50 个用户数据来演示 RecyclerView + ViewHolder：
     * - 虽然有 50 条数据
     * - 但只会创建约 10-15 个 ViewHolder
     * - 这就是 ViewHolder 的价值！
     */
    override suspend fun getUsers(): List<User> {
        // 模拟网络延迟
        delay(300)
        
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
}
