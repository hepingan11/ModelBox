<template> 
    <view class="myshop-container">
        
        <!-- 商品列表区域 -->
        <scroll-view 
            class="goods-scroll-view" 
            scroll-y 
            :refresher-enabled="true"
            :refresher-triggered="isRefreshing"
            @refresherrefresh="onRefresh"
            @scrolltolower="onReachBottom"
        >
            <!-- 商品列表 -->
            <view class="goods-list-container">
                <view class="goods-list" v-if="myGoodsList.length > 0">
                    <view 
                        class="goods-item" 
                        v-for="(item, index) in myGoodsList" 
                        :key="item.id"
                    >
                        <view class="goods-content" @click="navigateToDetail(item.id)">
                            <image 
                                :src="item.imageUrl && item.imageUrl.length > 0 ? item.imageUrl[0] : '/static/default-goods.png'" 
                                class="goods-image" 
                                mode="aspectFill"
                            ></image>
                            <view class="goods-info">
                                <text class="goods-name">{{ item.shopName }}</text>
                                <text class="goods-price">¥{{ item.price }}</text>
                                <view class="goods-status" :class="getStatusClass(item)">
                                    {{ getStatusText(item) }}
                                </view>
                                <text class="goods-time">{{ formatTime(item.createdTime) }}</text>
                                
                                <!-- 审核拒绝原因 -->
                                <view v-if="item.status === 2 && item.reason" class="reject-reason">
                                    <text class="reason-label">拒绝原因:</text>
                                    <text class="reason-content">{{ item.reason }}</text>
                                </view>
                            </view>
                        </view>
                        <view class="goods-actions">
                            <view class="action-btn edit-btn" @click="editGoods(item)">
                                <image src="/static/icon/edit.png" class="action-icon-image"></image>
                                <text class="action-text">编辑</text>
                            </view>
                            <view class="action-btn status-btn" @click="toggleStatus(item)">
                                <image :src="item.isOnline ? '/static/icon/isOnline.png' : '/static/icon/notOnline.png'" class="action-icon-image"></image>
                                <text class="action-text">{{ item.isOnline ? '下架' : '上架' }}</text>
                            </view>
                        </view>
                    </view>
                </view>

                <!-- 空数据状态 -->
                <view class="empty-state" v-if="myGoodsList.length === 0 && !isLoading">
                    <image src="/static/empty.png" class="empty-icon"></image>
                    <text class="empty-text">您还没有发布商品</text>
                    <view class="empty-btn" @click="navigateToPublish">立即发布</view>
                </view>

                <!-- 加载状态 -->
                <view class="loading-state" v-if="isLoading && !isRefreshing">
                    <view class="loading-spinner"></view>
                    <text class="loading-text">加载中...</text>
                </view>

                <!-- 加载结束状态 -->
                <view class="list-end" v-if="hasMore === false && myGoodsList.length > 0">
                    <text class="end-text">- 没有更多商品了 -</text>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 状态变量
const myGoodsList = ref([]) // 我的商品列表
const isLoading = ref(false) // 加载状态
const isRefreshing = ref(false) // 刷新状态
const hasMore = ref(true) // 是否有更多数据
const pageNum = ref(1) // 当前页码

// 页面加载时获取我的商品列表
onMounted(() => {
    getMyGoodsList()
})

// 获取我的商品列表
const getMyGoodsList = async () => {
    if (isLoading.value) return
    isLoading.value = true

    try {
        const res = await request('/shop/myShop', {
            method: 'GET',
            data: {
                pageNum: pageNum.value
            }
        })

        if (res.code === 200) {
            const list = res.data || []
            console.log('获取到的商品数据:', list)
            
            // 如果是第一页，直接替换列表
            if (pageNum.value === 1) {
                myGoodsList.value = list
            } else {
                // 否则追加到现有列表
                myGoodsList.value = [...myGoodsList.value, ...list]
            }
            
            // 判断是否有更多数据
            hasMore.value = list.length > 0
            
            // 如果有数据，页码加1，为下次加载做准备
            if (list.length > 0) {
                pageNum.value++
            }
        } else {
            uni.showToast({
                title: res.msg || '获取商品列表失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('获取商品列表失败:', error)
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
    } finally {
        isLoading.value = false
        isRefreshing.value = false // 结束下拉刷新状态
    }
}

// 下拉刷新
const onRefresh = () => {
    isRefreshing.value = true
    pageNum.value = 1 // 重置页码
    hasMore.value = true // 重置加载更多状态
    getMyGoodsList() // 重新获取商品列表
}

// 触底加载更多
const onReachBottom = () => {
    if (hasMore.value && !isLoading.value) {
        getMyGoodsList()
    }
}

// 格式化时间
const formatTime = (timestamp) => {
    if (!timestamp) return ''
    
    const date = new Date(timestamp)
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    
    return `${year}-${month}-${day}`
}

// 获取商品状态文本
const getStatusText = (item) => {
    if (!item) return '未知状态'
    
    if (item.status === 2) return '审核拒绝'
    if (item.status === 0) return '待审核'
    if (item.status === 1) {
        return item.isOnline ? '已上架' : '已下架'
    }
    return '未知状态'
}

// 获取商品状态样式类
const getStatusClass = (item) => {
    if (!item) return ''
    
    if (item.status === 2) return 'status-rejected'
    if (item.status === 0) return 'status-pending'
    if (item.status === 1) {
        return item.isOnline ? 'status-online' : 'status-offline'
    }
    return ''
}

// 跳转到商品详情页
const navigateToDetail = (id) => {
    uni.navigateTo({
        url: `/pages/shop/detail?id=${id}`
    })
}

// 跳转到发布商品页面
const navigateToPublish = () => {
    uni.navigateTo({
        url: '/pages/shop/publish'
    })
}

// 编辑商品
const editGoods = (item) => {
    uni.navigateTo({
        url: `/pages/shop/publish?id=${item.id}`
    })
}

// 切换商品上下架状态
const toggleStatus = async (item) => {
    const actionText = item.isOnline ? '下架' : '上架'
    
    uni.showModal({
        title: `确认${actionText}`,
        content: `确定要${actionText}该商品吗？`,
        success: async (res) => {
            if (res.confirm) {
                try {
                    const result = await request('/shop/setOnline', {
                        method: 'GET',
                        data: {
                            id: item.id
                        }
                    })
                    
                    if (result.code === 200) {
                        uni.showToast({
                            title: `${actionText}成功`,
                            icon: 'success'
                        })
                        
                        // 更新本地数据
                        const index = myGoodsList.value.findIndex(goods => goods.id === item.id)
                        if (index !== -1) {
                            // 切换isOnline状态
                            myGoodsList.value[index].isOnline = !item.isOnline
                        }
                    } else {
                        uni.showToast({
                            title: result.msg || `${actionText}失败`,
                            icon: 'none'
                        })
                    }
                } catch (error) {
                    console.error(`${actionText}商品失败:`, error)
                    uni.showToast({
                        title: '网络错误，请稍后重试',
                        icon: 'none'
                    })
                }
            }
        }
    })
}

// 删除商品
const deleteGoods = (id) => {
    uni.showModal({
        title: '确认删除',
        content: '确定要删除该商品吗？删除后无法恢复',
        success: async (res) => {
            if (res.confirm) {
                try {
                    const result = await request('/shop/delete', {
                        method: 'POST',
                        data: {
                            id: id
                        }
                    })
                    
                    if (result.code === 200) {
                        uni.showToast({
                            title: '删除成功',
                            icon: 'success'
                        })
                        
                        // 从列表中移除被删除的商品
                        myGoodsList.value = myGoodsList.value.filter(item => item.id !== id)
                    } else {
                        uni.showToast({
                            title: result.msg || '删除失败',
                            icon: 'none'
                        })
                    }
                } catch (error) {
                    console.error('删除商品失败:', error)
                    uni.showToast({
                        title: '网络错误，请稍后重试',
                        icon: 'none'
                    })
                }
            }
        }
    })
}

// 返回上一页
const goBack = () => {
    uni.navigateBack()
}
</script>

<style>
.myshop-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #f5f7fa;
}

/* 导航栏样式 */
.nav-bar {
    display: flex;
    align-items: center;
    height: 90rpx;
    background-color: #fff;
    padding: 0 30rpx;
    position: relative;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.back-icon {
    font-size: 40rpx;
    color: #333;
}

.page-title {
    position: absolute;
    left: 0;
    right: 0;
    text-align: center;
    font-size: 32rpx;
    font-weight: 500;
    color: #333;
}

.add-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: auto;
}

.add-icon {
    font-size: 40rpx;
    color: #333;
}

/* 商品列表区域 */
.goods-scroll-view {
    flex: 1;
    padding: 20rpx;
    box-sizing: border-box;
}

.goods-list-container {
    width: 100%;
    max-width: 750rpx;
    margin: 0 auto;
    box-sizing: border-box;
}

.goods-list {
    width: 100%;
}

.goods-item {
    background-color: #fff;
    border-radius: 12rpx;
    margin-bottom: 20rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.goods-content {
    display: flex;
    padding: 20rpx;
}

.goods-image {
    width: 180rpx;
    height: 180rpx;
    border-radius: 8rpx;
    margin-right: 20rpx;
}

.goods-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.goods-name {
    font-size: 28rpx;
    color: #333;
    line-height: 1.4;
    margin-bottom: 10rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.goods-price {
    font-size: 32rpx;
    color: #ff4d4f;
    font-weight: bold;
    margin-bottom: 10rpx;
}

.goods-status {
    font-size: 24rpx;
    padding: 4rpx 12rpx;
    border-radius: 20rpx;
    display: inline-block;
    margin-bottom: 10rpx;
}

.status-pending {
    background-color: #e6f7ff;
    color: #1890ff;
}

.status-online {
    background-color: #f6ffed;
    color: #52c41a;
}

.status-offline {
    background-color: #f5f5f5;
    color: #999;
}

.status-rejected {
    background-color: #fff2f0;
    color: #ff4d4f;
}

.goods-time {
    font-size: 24rpx;
    color: #999;
}

.goods-actions {
    display: flex;
    border-top: 1rpx solid #f0f0f0;
}

.action-btn {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 16rpx 0;
}

.action-icon-image {
    width: 40rpx;
    height: 40rpx;
    margin-bottom: 6rpx;
}

.action-text {
    font-size: 24rpx;
    color: #666;
}

.edit-btn .action-text {
    color: #1890ff;
}

.status-btn .action-text {
    color: #52c41a;
}

.delete-btn .action-text {
    color: #ff4d4f;
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

.empty-btn {
    padding: 16rpx 40rpx;
    background-color: #1890ff;
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

/* 拒绝原因样式 */
.reject-reason {
    margin-top: 10rpx;
    padding: 10rpx;
    background-color: #fff2f0;
    border-radius: 8rpx;
    font-size: 24rpx;
    display: flex;
    flex-direction: column;
}

.reason-label {
    color: #ff4d4f;
    font-weight: 500;
    margin-bottom: 4rpx;
}

.reason-content {
    color: #666;
}
</style>
