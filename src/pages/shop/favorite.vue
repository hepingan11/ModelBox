<template>
    <view class="favorite-container">
        
        
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
                <view class="goods-list" v-if="favoriteList.length > 0">
                    <view 
                        class="goods-row" 
                        v-for="(row, rowIndex) in goodsRows" 
                        :key="rowIndex"
                    >
                        <view 
                            class="goods-item" 
                            v-for="(item, colIndex) in row" 
                            :key="item.id"
                            @click="navigateToDetail(item.id)"
                        >
                            <image 
                                :src="item.imageUrl && item.imageUrl.length > 0 ? item.imageUrl[0] : '/static/default-goods.png'" 
                                class="goods-image" 
                                mode="aspectFill"
                            ></image>
                            <view class="goods-info">
                                <text class="goods-name">{{ item.shopName }}</text>
                                <view class="goods-price-row">
                                    <text class="goods-price">¥{{ item.price }}</text>
                                </view>
                            </view>
                        </view>
                        <!-- 占位元素，保证每行的最后一个不足时也能保持布局 -->
                        <view class="goods-item placeholder" v-if="row.length === 1"></view>
                    </view>
                </view>

                <!-- 空数据状态 -->
                <view class="empty-state" v-if="favoriteList.length === 0 && !isLoading">
                    <image src="/static/empty.png" class="empty-icon"></image>
                    <text class="empty-text">暂无收藏商品</text>
                </view>

                <!-- 加载状态 -->
                <view class="loading-state" v-if="isLoading && !isRefreshing">
                    <view class="loading-spinner"></view>
                    <text class="loading-text">加载中...</text>
                </view>

                <!-- 加载结束状态 -->
                <view class="list-end" v-if="hasMore === false && favoriteList.length > 0">
                    <text class="end-text">- 没有更多收藏商品了 -</text>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request.js'

// 状态变量
const favoriteList = ref([]) // 收藏商品列表
const isLoading = ref(false) // 加载状态
const isRefreshing = ref(false) // 刷新状态
const hasMore = ref(true) // 是否有更多数据
const pageNum = ref(1) // 当前页码

// 将商品列表转换为每行两列的格式
const goodsRows = computed(() => {
    const rows = []
    for (let i = 0; i < favoriteList.value.length; i += 2) {
        // 每两个商品为一行
        rows.push(favoriteList.value.slice(i, i + 2))
    }
    return rows
})

// 页面加载时获取收藏商品列表
onMounted(() => {
    getFavoriteList()
})

// 获取收藏商品列表
const getFavoriteList = async () => {
    if (isLoading.value) return
    isLoading.value = true

    try {
        const res = await request('/shop/myFavorite', {
            method: 'GET',
            data: {
                pageNum: pageNum.value
            }
        })

        if (res.code === 200) {
            const list = res.data || []
            
            // 如果是第一页，直接替换列表
            if (pageNum.value === 1) {
                favoriteList.value = list
            } else {
                // 否则追加到现有列表
                favoriteList.value = [...favoriteList.value, ...list]
            }
            
            // 判断是否有更多数据
            hasMore.value = list.length > 0
            
            // 如果有数据，页码加1，为下次加载做准备
            if (list.length > 0) {
                pageNum.value++
            }
        } else {
            uni.showToast({
                title: res.msg || '获取收藏列表失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('获取收藏列表失败:', error)
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
    getFavoriteList() // 重新获取收藏商品列表
}

// 触底加载更多
const onReachBottom = () => {
    if (hasMore.value && !isLoading.value) {
        getFavoriteList()
    }
}

// 跳转到商品详情页
const navigateToDetail = (id) => {
    uni.navigateTo({
        url: `/pages/shop/detail?id=${id}`
    })
}

// 返回上一页
const goBack = () => {
    uni.navigateBack()
}
</script>

<style>
.favorite-container {
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
    padding: 0 10rpx;
    box-sizing: border-box;
}

.goods-list {
    width: 100%;
    box-sizing: border-box;
}

.goods-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;
    width: 100%;
}

.goods-item {
    width: 48%;
    background-color: #fff;
    border-radius: 12rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
    box-sizing: border-box;
}

.goods-item.placeholder {
    visibility: hidden;
}

.goods-image {
    width: 100%;
    height: 340rpx;
}

.goods-info {
    padding: 16rpx;
    box-sizing: border-box;
}

.goods-name {
    font-size: 28rpx;
    color: #333;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    height: 78rpx;
    word-break: break-all;
    width: 100%;
}

.goods-price-row {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-top: 10rpx;
    padding: 0 2rpx;
    width: 100%;
}

.goods-price {
    font-size: 32rpx;
    color: #ff4d4f;
    font-weight: bold;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
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
</style>
