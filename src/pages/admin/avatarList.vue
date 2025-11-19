<template>
    <view class="avatar-list-container">
        <!-- 顶部标题栏 -->
        <view class="header-section">
            <view class="header-left">
                <text class="header-title">头像框管理</text>
                <text class="avatar-count">共 {{ totalAvatars }} 个头像框</text>
            </view>
            <view class="header-right">
                <view class="search-box">
                    <image src="/static/icon/search.png" class="search-icon"></image>
                    <input 
                        type="text" 
                        v-model="searchKeyword" 
                        @confirm="handleSearch" 
                        placeholder="搜索头像框名称" 
                        class="search-input"
                    />
                    <view class="clear-icon" v-if="searchKeyword" @click="clearSearch">×</view>
                </view>
            </view>
        </view>
        
        <!-- 头像框列表 -->
        <scroll-view 
            scroll-y 
            class="avatar-scroll-view" 
            @scrolltolower="loadMoreAvatars"
            refresher-enabled
            :refresher-triggered="isRefreshing"
            @refresherrefresh="refreshAvatarList"
        >
            <!-- 头像框列表项 -->
            <view v-if="avatarList.length > 0" class="avatar-grid">
                <view 
                    class="avatar-item"
                    v-for="(avatar, index) in avatarList" 
                    :key="avatar.id"
                    @click="navigateToEdit(avatar)"
                >
                    <view class="avatar-preview">
                        <image 
                            :src="avatar.url" 
                            class="avatar-image" 
                            mode="aspectFit"
                        ></image>
                    </view>
                    <view class="avatar-info">
                        <text class="avatar-name">{{ avatar.name || '未命名头像框' }}</text>
                        <text class="avatar-introduce">{{ avatar.introduce }}</text>
                    </view>
                    
                </view>
            </view>
            
            <!-- 空状态 -->
            <view v-else-if="!isLoading" class="empty-state">
                <image src="/static/icon/empty.png" class="empty-icon"></image>
                <text class="empty-text">暂无头像框数据</text>
                <view class="add-avatar-btn" @click="navigateToAdd">添加头像框</view>
            </view>
            
            <!-- 加载状态 -->
            <view class="loading-state" v-if="isLoading && !isRefreshing">
                <view class="loading-spinner"></view>
                <text class="loading-text">加载中...</text>
            </view>
            
            <!-- 已加载全部 -->
            <view class="list-end" v-if="!hasMore && avatarList.length > 0">
                <text class="end-text">- 没有更多头像框了 -</text>
            </view>
        </scroll-view>
        
        <!-- 添加头像框浮动按钮 -->
        <view class="float-add-btn" @click="navigateToAdd">
            <text class="add-icon-text">+</text>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

// 状态变量
const avatarList = ref([]) // 头像框列表
const totalAvatars = ref(0) // 总数量
const isLoading = ref(false) // 加载状态
const isRefreshing = ref(false) // 刷新状态
const hasMore = ref(true) // 是否有更多数据
const currentPage = ref(1) // 当前页码
const searchKeyword = ref('') // 搜索关键词

// 页面加载时获取头像框列表
onMounted(() => {
    getAvatarList(true)
})

// 获取头像框列表
const getAvatarList = async (isRefresh = false) => {
    if (isLoading.value) return
    isLoading.value = true
    
    try {
        const params = {
            pageNum: currentPage.value,
            name: searchKeyword.value
        }
        
        const res = await request('/admin/frame/list', {
            method: 'GET',
            data: params
        })
        
        if (res.code === 200) {
            const list = res.data || []
            
            if (isRefresh) {
                avatarList.value = list
            } else {
                avatarList.value = [...avatarList.value, ...list]
            }
            
            totalAvatars.value = res.total || avatarList.value.length
            hasMore.value = list.length === 10 // 如果返回的数据量等于pageSize，则认为还有更多数据
            
            // 如果有数据，页码加1，为下次加载做准备
            if (list.length > 0 && !isRefresh) {
                currentPage.value++
            }
        } else {
            uni.showToast({
                title: res.msg || '获取头像框列表失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('获取头像框列表失败:', error)
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
    } finally {
        isLoading.value = false
        if (isRefreshing.value) {
            isRefreshing.value = false
        }
    }
}

// 下拉刷新
const refreshAvatarList = () => {
    isRefreshing.value = true
    currentPage.value = 1
    getAvatarList(true)
}

// 加载更多
const loadMoreAvatars = () => {
    if (hasMore.value && !isLoading.value) {
        getAvatarList()
    }
}

// 搜索头像框
const handleSearch = () => {
    currentPage.value = 1
    getAvatarList(true)
}

// 清除搜索
const clearSearch = () => {
    searchKeyword.value = ''
    handleSearch()
}

// 跳转到添加头像框页面
const navigateToAdd = () => {
    uni.navigateTo({
        url: '/pages/admin/avatarEdit?mode=add'
    })
}

// 跳转到编辑头像框页面
const navigateToEdit = (avatar) => {
    uni.navigateTo({
        url: `/pages/admin/avatarEdit?mode=edit&id=${avatar.id}`
    })
}

// 切换头像框状态
const toggleStatus = async (avatar) => {
    const newStatus = avatar.status === 1 ? 0 : 1
    const actionText = newStatus === 1 ? '启用' : '禁用'
    
    uni.showModal({
        title: `确认${actionText}`,
        content: `确定要${actionText}该头像框吗？`,
        success: async (res) => {
            if (res.confirm) {
                try {
                    const result = await request('/admin/avatar/updateStatus', {
                        method: 'POST',
                        data: {
                            id: avatar.id,
                            status: newStatus
                        }
                    })
                    
                    if (result.code === 200) {
                        uni.showToast({
                            title: `${actionText}成功`,
                            icon: 'success'
                        })
                        
                        // 更新本地数据
                        const index = avatarList.value.findIndex(item => item.id === avatar.id)
                        if (index !== -1) {
                            avatarList.value[index].status = newStatus
                        }
                    } else {
                        uni.showToast({
                            title: result.msg || `${actionText}失败`,
                            icon: 'none'
                        })
                    }
                } catch (error) {
                    console.error(`${actionText}头像框失败:`, error)
                    uni.showToast({
                        title: '网络错误，请稍后重试',
                        icon: 'none'
                    })
                }
            }
        }
    })
}

// 确认删除头像框
const confirmDelete = (avatar) => {
    uni.showModal({
        title: '确认删除',
        content: '确定要删除该头像框吗？删除后无法恢复',
        success: async (res) => {
            if (res.confirm) {
                try {
                    const result = await request('/admin/avatar/delete', {
                        method: 'POST',
                        data: {
                            id: avatar.id
                        }
                    })
                    
                    if (result.code === 200) {
                        uni.showToast({
                            title: '删除成功',
                            icon: 'success'
                        })
                        
                        // 从列表中移除被删除的头像框
                        avatarList.value = avatarList.value.filter(item => item.id !== avatar.id)
                        totalAvatars.value--
                    } else {
                        uni.showToast({
                            title: result.msg || '删除失败',
                            icon: 'none'
                        })
                    }
                } catch (error) {
                    console.error('删除头像框失败:', error)
                    uni.showToast({
                        title: '网络错误，请稍后重试',
                        icon: 'none'
                    })
                }
            }
        }
    })
}
</script>

<style lang="scss" scoped>
.avatar-list-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #f5f7fa;
    position: relative;
}

/* 顶部标题栏 */
.header-section {
    padding: 30rpx;
    background-color: #ffffff;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.header-left {
    display: flex;
    flex-direction: column;
}

.header-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 8rpx;
}

.avatar-count {
    font-size: 24rpx;
    color: #999;
}

.header-right {
    display: flex;
    align-items: center;
}

.search-box {
    display: flex;
    align-items: center;
    background-color: #f0f2f5;
    border-radius: 30rpx;
    padding: 10rpx 20rpx;
    margin-right: 20rpx;
}

.search-icon {
    width: 36rpx;
    height: 36rpx;
    margin-right: 10rpx;
}

.search-input {
    width: 240rpx;
    font-size: 26rpx;
}

.clear-icon {
    width: 40rpx;
    height: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36rpx;
    color: #999;
}

.add-btn {
    width: 70rpx;
    height: 70rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #1abc9c;
    border-radius: 50%;
}

.add-icon {
    width: 40rpx;
    height: 40rpx;
}

/* 头像框列表 */
.avatar-scroll-view {
    flex: 1;
    padding: 20rpx;
}

.avatar-grid {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
}

.avatar-item {
    width: 48%;
    background-color: #fff;
    border-radius: 12rpx;
    margin-bottom: 20rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
}

.avatar-preview {
    width: 100%;
    height: 200rpx;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f8f8f8;
}

.avatar-image {
    width: 160rpx;
    height: 160rpx;
}

.avatar-info {
    display: flex;
    flex-direction: column;
    padding: 15rpx;
    justify-content: space-between;
    align-items: center;
}

.avatar-name {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
}

.avatar-introduce {
    font-size: 22rpx;
    color: #999;
}

.status-active {
    background-color: #f6ffed;
    color: #52c41a;
}

.status-inactive {
    background-color: #f5f5f5;
    color: #999;
}

.avatar-actions {
    display: flex;
    padding: 15rpx;
}

.edit-btn, .status-btn, .delete-btn {
    flex: 1;
    font-size: 24rpx;
    padding: 8rpx 0;
    margin: 0 5rpx;
    border-radius: 20rpx;
    background-color: #fff;
}

.edit-btn {
    color: #1890ff;
    border: 1rpx solid #1890ff;
}

.enable-btn {
    color: #52c41a;
    border: 1rpx solid #52c41a;
}

.disable-btn {
    color: #faad14;
    border: 1rpx solid #faad14;
}

.delete-btn {
    color: #ff4d4f;
    border: 1rpx solid #ff4d4f;
}

/* 空状态 */
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;
}

.empty-icon {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 30rpx;
}

.empty-text {
    font-size: 28rpx;
    color: #999;
    margin-bottom: 30rpx;
}

.add-avatar-btn {
    padding: 16rpx 40rpx;
    background-color: #1abc9c;
    color: #fff;
    font-size: 28rpx;
    border-radius: 40rpx;
}

/* 加载状态 */
.loading-state {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 30rpx 0;
}

.loading-spinner {
    width: 40rpx;
    height: 40rpx;
    border: 4rpx solid #f3f3f3;
    border-top: 4rpx solid #1890ff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-right: 10rpx;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.loading-text {
    font-size: 24rpx;
    color: #999;
}

/* 列表结束状态 */
.list-end {
    text-align: center;
    padding: 30rpx 0;
}

.end-text {
    font-size: 24rpx;
    color: #999;
}

/* 浮动添加按钮 */
.float-add-btn {
    position: fixed;
    right: 40rpx;
    bottom: 40rpx;
    width: 100rpx;
    height: 100rpx;
    background-color: #1abc9c;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 16rpx rgba(26, 188, 156, 0.4);
    z-index: 99;
}

.add-icon-text {
    font-size: 60rpx;
    color: #fff;
    font-weight: 300;
    line-height: 1;
}
</style>