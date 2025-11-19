<template>
    <view class="shop-container">
        <!-- 背景装饰元素 -->
        <view class="bg-decoration">
            <view class="floating-circle circle-1"></view>
            <view class="floating-circle circle-2"></view>
            <view class="floating-circle circle-3"></view>
            <view class="floating-square square-1"></view>
            <view class="floating-square square-2"></view>
        </view>
        
        <!-- 顶部搜索栏 -->
        <view class="search-bar">
            <view class="search-input-wrapper">
                <image src="/static/icon/search.png" class="search-icon"></image>
                <input 
                    type="text" 
                    v-model="shopName" 
                    class="search-input" 
                    placeholder="搜索商品" 
                    confirm-type="search"
                    @confirm="handleSearch"
                />
                <view class="clear-icon" v-if="shopName" @click="clearSearch">×</view>
            </view>
            <view class="search-btn" @click="handleSearch">搜索</view>
        </view>

        <!-- 主内容滚动区域 -->
        <scroll-view 
            class="main-scroll-view" 
            scroll-y 
            :refresher-enabled="true"
            :refresher-triggered="isRefreshing"
            @refresherrefresh="onRefresh"
            @scrolltolower="onReachBottom"
        >
            <!-- 顶部导航选择模块 -->
            <view class="nav-tabs">
                <view class="tab-item" @click="navigateToMyGoods">
                    <image src="/static/icon/myshop.png" class="tab-icon-image"></image>
                    <text class="tab-text">我的商品</text>
                </view>
                <view class="tab-item" @click="navigateToMessages">
                    <image src="/static/icon/messageList.png" class="tab-icon-image"></image>
                    <text class="tab-text">消息列表</text>
                </view>
                <view class="tab-item" @click="navigateToFavorite">
                    <image src="/static/icon/starList.png" class="tab-icon-image"></image>
                    <text class="tab-text">收藏商品</text>
                </view>
            </view>

            <!-- 商品分类选择 -->
            <scroll-view class="category-scroll" scroll-x show-scrollbar="false">
                <view 
                    class="category-item" 
                    :class="{ active: currentCategory === '' }"
                    @click="selectCategory('')"
                >
                    全部
                </view>
                <view 
                    class="category-item" 
                    :class="{ active: currentCategory === '学习用品' }"
                    @click="selectCategory('学习用品')"
                >
                    学习用品
                </view>
                <view 
                    class="category-item" 
                    :class="{ active: currentCategory === '电子产品' }"
                    @click="selectCategory('电子产品')"
                >
                    电子产品
                </view>
                <view 
                    class="category-item" 
                    :class="{ active: currentCategory === '衣物鞋包' }"
                    @click="selectCategory('衣物鞋包')"
                >
                    衣物鞋包
                </view>
                <view 
                    class="category-item" 
                    :class="{ active: currentCategory === '生活用品' }"
                    @click="selectCategory('生活用品')"
                >
                    生活用品
                </view>
                <view 
                    class="category-item" 
                    :class="{ active: currentCategory === '运动娱乐' }"
                    @click="selectCategory('运动娱乐')"
                >
                    运动娱乐
                </view>
                <view 
                    class="category-item" 
                    :class="{ active: currentCategory === '虚拟产品' }"
                    @click="selectCategory('虚拟产品')"
                >
                    虚拟产品
                </view>
                <view 
                    class="category-item" 
                    :class="{ active: currentCategory === '其它' }"
                    @click="selectCategory('其它')"
                >
                    其它
                </view>
            </scroll-view>

            <!-- 商品列表区域 -->
            <view class="goods-list-container">
                <view class="goods-list" v-if="goodsList.length > 0">
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
                                <text class="goods-intro" v-if="item.introduce">{{ item.introduce }}</text>
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
                <view class="empty-state" v-if="goodsList.length === 0 && !isLoading">
                    <image src="/static/empty.png" class="empty-icon"></image>
                    <text class="empty-text">暂无商品</text>
                </view>

                <!-- 加载状态 -->
                <view class="loading-state" v-if="isLoading && !isRefreshing">
                    <view class="loading-spinner"></view>
                    <text class="loading-text">加载中...</text>
                </view>

                <!-- 加载结束状态 -->
                <view class="list-end" v-if="hasMore === false && goodsList.length > 0">
                    <text class="end-text">- 没有更多商品了 -</text>
                </view>
            </view>
        </scroll-view>
        
        <!-- 悬浮的发布按钮 -->
        <view class="floating-button" @click="navigateToPublish">
            <text class="plus-icon">+</text>
            <text class="button-text">发布</text>
        </view>
    </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request.js'

// 状态变量
const goodsList = ref([]) // 商品列表
const isLoading = ref(false) // 加载状态
const isRefreshing = ref(false) // 刷新状态
const hasMore = ref(true) // 是否有更多数据
const pageNum = ref(1) // 当前页码
const shopName = ref('') // 搜索关键词
const currentCategory = ref('') // 当前选中的分类

// 将商品列表转换为每行两列的格式
const goodsRows = computed(() => {
    const rows = []
    for (let i = 0; i < goodsList.value.length; i += 2) {
        // 每两个商品为一行
        rows.push(goodsList.value.slice(i, i + 2))
    }
    return rows
})

// 页面加载时获取商品列表
onMounted(() => {
    getGoodsList()
})

// 获取商品列表
const getGoodsList = async () => {
    if (isLoading.value) return
    isLoading.value = true

    try {
        const res = await request('/shop/list', {
            method: 'GET',
            data: {
                pageNum: pageNum.value,
                shopName: shopName.value,
                category: currentCategory.value
            }
        })

        if (res.code === 200) {
            const list = res.data || []
            
            // 如果是第一页，直接替换列表
            if (pageNum.value === 1) {
                goodsList.value = list
            } else {
                // 否则追加到现有列表
                goodsList.value = [...goodsList.value, ...list]
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
    getGoodsList() // 重新获取商品列表
}

// 触底加载更多
const onReachBottom = () => {
    if (hasMore.value && !isLoading.value) {
        getGoodsList()
    }
}

// 搜索商品
const handleSearch = () => {
    pageNum.value = 1 // 重置页码
    hasMore.value = true // 重置加载更多状态
    goodsList.value = [] // 清空现有列表
    getGoodsList() // 获取搜索结果
}

// 清除搜索
const clearSearch = () => {
    shopName.value = ''
    handleSearch()
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

// 导航到我的商品
const navigateToMyGoods = () => {
    uni.navigateTo({
        url: '/pages/shop/myshop'
    })
}

// 导航到消息列表
const navigateToMessages = () => {
    uni.navigateTo({
        url: '/pages/shop/message'
    })
}

// 导航到收藏商品
const navigateToFavorite = () => {
    uni.navigateTo({
        url: '/pages/shop/favorite'
    })
}

// 选择分类
const selectCategory = (category) => {
    // 如果点击的是当前已选中的分类，不执行任何操作
    if (currentCategory.value === category) return;
    
    currentCategory.value = category
    pageNum.value = 1 // 重置页码
    hasMore.value = true // 重置加载更多状态
    goodsList.value = [] // 清空现有列表
    getGoodsList() // 获取分类商品
}
</script>

<style>
:root {
    --theme-color: #1abc9c;
}

.shop-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4f2fb 50%, #f5f7fa 100%);
    position: relative;
    overflow: hidden;
}

/* 背景装饰元素 */
.bg-decoration {
    position: absolute;
    width: 100%;
    height: 100%;
    z-index: 0;
    overflow: hidden;
}

.floating-circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.4;
}

.circle-1 {
    width: 300rpx;
    height: 300rpx;
    background: linear-gradient(45deg, #baeb34, rgba(186, 235, 52, 0.3));
    top: -100rpx;
    left: -100rpx;
    animation: float 15s infinite ease-in-out;
}

.circle-2 {
    width: 200rpx;
    height: 200rpx;
    background: linear-gradient(45deg, #1abc9c, rgba(26, 188, 156, 0.3));
    bottom: 20%;
    right: -50rpx;
    animation: float 12s infinite ease-in-out reverse;
}

.circle-3 {
    width: 150rpx;
    height: 150rpx;
    background: linear-gradient(45deg, #9b59b6, rgba(155, 89, 182, 0.3));
    bottom: 10%;
    left: 10%;
    animation: float 18s infinite ease-in-out;
}

.floating-square {
    position: absolute;
    border-radius: 15rpx;
    opacity: 0.3;
    transform: rotate(45deg);
}

.square-1 {
    width: 100rpx;
    height: 100rpx;
    background: linear-gradient(45deg, #3498db, rgba(52, 152, 219, 0.3));
    top: 30%;
    right: 10%;
    animation: rotate 20s infinite linear;
}

.square-2 {
    width: 80rpx;
    height: 80rpx;
    background: linear-gradient(45deg, #e74c3c, rgba(231, 76, 60, 0.3));
    top: 60%;
    left: 5%;
    animation: rotate 15s infinite linear reverse;
}

@keyframes float {
    0%, 100% {
        transform: translateY(0) scale(1);
    }
    50% {
        transform: translateY(-30rpx) scale(1.05);
    }
}

@keyframes rotate {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}

/* 搜索栏样式 */
.search-bar {
    display: flex;
    align-items: center;
    padding: 20rpx 30rpx;
    background-color: #fff;
    position: sticky;
    top: 0;
    z-index: 10;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.search-input-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    height: 70rpx;
    background-color: #f5f7fa;
    border-radius: 35rpx;
    padding: 0 20rpx;
    margin-right: 20rpx;
}

.search-icon {
    width: 36rpx;
    height: 36rpx;
    margin-right: 10rpx;
}

.search-input {
    flex: 1;
    height: 70rpx;
    font-size: 28rpx;
    color: #333;
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

.search-btn {
    width: 100rpx;
    height: 70rpx;
    line-height: 70rpx;
    text-align: center;
    font-size: 28rpx;
    color: var(--theme-color);
}

/* 主滚动区域 */
.main-scroll-view {
    flex: 1;
    height: calc(100vh - 110rpx); /* 减去搜索栏高度 */
}

/* 导航选择模块样式 */
.nav-tabs {
    display: flex;
    justify-content: space-around;
    padding: 20rpx 0;
    background-color: #fff;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.tab-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10rpx 0;
}

.tab-icon-image {
    width: 48rpx;
    height: 48rpx;
    margin-bottom: 10rpx;
}

.tab-text {
    font-size: 24rpx;
    color: #333;
}

/* 商品列表区域 */
.goods-list-container {
    width: 100%;
    max-width: 750rpx;
    margin: 0 auto;
    padding: 0 20rpx 20rpx;
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
    font-size: 30rpx;
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

.goods-intro {
    font-size: 24rpx;
    color: #666;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    height: 68rpx;
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
    color: #358e53;
    font-weight: bold;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.goods-sales {
    font-size: 24rpx;
    color: #999;
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

/* 悬浮按钮样式 */
.floating-button {
    position: fixed;
    right: 30rpx;
    bottom: 50rpx;
    width: 120rpx;
    height: 120rpx;
    background: linear-gradient(135deg, #1abc9c, #1abc9c);
    border-radius: 60rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 16rpx rgba(186, 235, 52, 0.4);
    z-index: 100;
}

.plus-icon {
    font-size: 48rpx;
    color: #fff;
    font-weight: bold;
    margin-bottom: -5rpx;
    line-height: 1;
}

.button-text {
    font-size: 24rpx;
    color: #fff;
    margin-top: 5rpx;
}

/* 商品分类选择 */
.category-scroll {
    white-space: nowrap;
    background-color: #fff;
    padding: 20rpx 0;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.category-item {
    display: inline-block;
    padding: 12rpx 30rpx;
    margin: 0 10rpx;
    font-size: 28rpx;
    color: #666;
    background-color: #f5f7fa;
    border-radius: 20rpx;
    transition: all 0.3s;
}

.category-item:first-child {
    margin-left: 20rpx;
}

.category-item:last-child {
    margin-right: 20rpx;
}

.category-item.active {
    background-color: #1abc9c;
    color: #fff;
    font-weight: 500;
}
</style>