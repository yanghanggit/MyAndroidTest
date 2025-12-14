package com.example.myandroidtest.di

import com.example.myandroidtest.data.repository.UserRepository
import com.example.myandroidtest.data.source.MockUserDataSource
import com.example.myandroidtest.data.source.UserDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 依赖注入模块
 * 
 * @Module: 标记这是一个 Hilt 模块
 * @InstallIn(SingletonComponent::class): 指定模块的生命周期范围
 * - SingletonComponent: 应用级单例，整个应用生命周期内只有一个实例
 * - ActivityComponent: Activity 级别
 * - FragmentComponent: Fragment 级别
 * 
 * 这个模块定义了如何创建和提供依赖对象
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * 提供 UserDataSource 实例
     * 
     * @Provides: 告诉 Hilt 如何创建这个类型的实例
     * @Singleton: 确保整个应用只有一个实例
     * 
     * 💡 切换数据源只需修改这里：
     * return MockUserDataSource()  ← 当前使用 Mock 数据
     * return RemoteUserDataSource() ← 切换到真实 API
     */
    @Provides
    @Singleton
    fun provideUserDataSource(): UserDataSource {
        return MockUserDataSource()
    }

    /**
     * 提供 UserRepository 实例
     * 
     * 参数 dataSource 会由 Hilt 自动注入
     * （从上面的 provideUserDataSource 获取）
     */
    @Provides
    @Singleton
    fun provideUserRepository(dataSource: UserDataSource): UserRepository {
        return UserRepository(dataSource)
    }
}
