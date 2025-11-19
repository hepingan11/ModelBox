<template>
    <view class="detail-container">
        
        <!-- 商品详情内容 -->
        <scroll-view class="detail-content" scroll-y>
            <!-- 加载中状态 -->
            <view class="loading-wrapper" v-if="isLoading">
                <view class="loading-animation"></view>
                <text class="loading-text">加载中...</text>
            </view>
            
            <!-- 商品内容 -->
            <view v-else-if="goodsDetail">
                <!-- 商品图片轮播 -->
                <swiper 
                    class="goods-swiper" 
                    :indicator-dots="true" 
                    :autoplay="true" 
                    :interval="3000" 
                    :duration="500"
                    circular
                >
                    <swiper-item v-for="(img, index) in goodsDetail.imageUrl" :key="index" @click="previewImage(index)">
                        <image :src="img" class="swiper-image" mode="aspectFill"></image>
                    </swiper-item>
                    <!-- 无图片时显示默认图片 -->
                    <swiper-item v-if="!goodsDetail.imageUrl || goodsDetail.imageUrl.length === 0">
                        <image src="/static/default-goods.png" class="swiper-image" mode="aspectFill"></image>
                    </swiper-item>
                </swiper>
                
                <!-- 商品价格信息 -->
                <view class="goods-price-section">
                    <view class="price-row">
                        <text class="goods-price">¥{{ goodsDetail.price }}</text>
                    </view>
                    <view class="goods-name">{{ goodsDetail.shopName }}</view>
                </view>
                
                <!-- 商品卖家信息 -->
                <view class="seller-section">
                    <view class="section-title">卖家信息</view>
                    <view class="seller-info">
                        <view class="avatar-wrapper" @click="goToUserProfile(goodsDetail.userId)">
                            <image :src="goodsDetail.avatar || defaultAvatar" class="seller-avatar" mode="aspectFill"></image>
                            <!-- 头像框 -->
                            <image v-if="goodsDetail.avatarFrameUrl" :src="goodsDetail.avatarFrameUrl" class="avatar-frame" mode="aspectFit"></image>
                        </view>
                        <view class="seller-detail" @click="goToUserProfile(goodsDetail.userId)">
                            <view class="seller-name-wrapper">
                                <text class="seller-name">{{ goodsDetail.username || '用户' + goodsDetail.userId }}</text>
                                <view class="user-level" :class="'level-' + (goodsDetail.level || 1)">
                                    <text class="level-text">Lv.{{ goodsDetail.level || 1 }}</text>
                                </view>
                            </view>
                            <text class="publish-time">发布于 {{ formatTime(goodsDetail.createdTime) }}</text>
                        </view>
                        <view class="contact-btn" @click="contactSeller">
                            <text class="contact-text">联系卖家</text>
                        </view>
                    </view>
                </view>
                
                <!-- 商品介绍 -->
                <view class="goods-intro-section">
                    <view class="section-title">商品介绍</view>
                    <text class="goods-intro">{{ goodsDetail.introduce || '暂无商品介绍' }}</text>
                </view>
            </view>
            
            <!-- 加载失败 -->
            <view v-else class="load-error">
                <image class="error-icon" src="/static/icon/error.png" mode="aspectFit"></image>
                <text class="error-text">加载失败，请重试</text>
                <button class="retry-btn" @click="getGoodsDetail">重新加载</button>
            </view>
        </scroll-view>
        
        <!-- 底部操作栏 -->
        <view class="bottom-action-bar">
            <view class="action-btn share-btn" @click="shareGoods">
                <text class="action-icon">🔗</text>
                <text class="action-text">分享</text>
            </view>
            <view class="action-btn favorite-btn" :class="{'favorite-animate': isAnimating}" @click="toggleFavorite">
                <text class="action-icon">{{ isFavorite ? '❤️' : '🤍' }}</text>
                <text class="action-text">{{ isFavorite ? '已收藏' : '收藏' }}</text>
            </view>
            <view class="purchase-btn" @click="purchaseGoods">立即购买</view>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 商品ID
const goodsId = ref('')
// 商品详情
const goodsDetail = ref(null)
// 加载状态
const isLoading = ref(true)
// 默认头像
const defaultAvatar = '/static/user.png'
// 收藏状态
const isFavorite = ref(false)
// 收藏按钮动画状态
const isAnimating = ref(false)

// 获取商品详情
const getGoodsDetail = async () => {
    // 避免重复请求
    if (isLoading.value === true && goodsDetail.value !== null) return
    
    isLoading.value = true
    
    try {
        if (!goodsId.value) {
            uni.showToast({
                title: '商品ID不存在',
                icon: 'none'
            })
            uni.navigateBack()
            return
        }
        
        console.log('开始请求商品详情, ID:', goodsId.value)
        
        // 请求商品详情
        const res = await request('/shop/getById', {
            method: 'GET',
            data: { id: goodsId.value }
        })
        
        if (res.code === 200) {
            goodsDetail.value = res.data
            console.log('商品详情:', goodsDetail.value)
            
            // 直接使用商品详情中的收藏状态
            isFavorite.value = goodsDetail.value.isFavorite || false
        } else {
            uni.showToast({
                title: res.msg || '获取商品详情失败',
                icon: 'none'
            })
        }
    } catch (error) {
        
    } finally {
        isLoading.value = false
    }
}

// 切换收藏状态
const toggleFavorite = async () => {
    // 如果正在动画中，不处理点击
    if (isAnimating.value) return;
    
    // 设置动画状态
    isAnimating.value = true;
    
    // 临时更新UI状态，让用户立即看到反馈
    const originalState = isFavorite.value;
    isFavorite.value = !originalState;
    
    try {
        uni.showLoading({
            title: originalState ? '取消收藏中...' : '收藏中...',
            mask: true
        });
        
        const res = await request('/shop/toggleFavorite', {
            method: 'GET',
            data: {id: goodsId.value}
        })
        
        uni.hideLoading();
        
        if (res.code === 200) {
            // 操作成功
            uni.showToast({
                title: isFavorite.value ? '收藏成功' : '已取消收藏',
                icon: 'success'
            })
            
            // 更新商品详情中的收藏状态
            if (goodsDetail.value) {
                goodsDetail.value.isFavorite = isFavorite.value;
            }
        } else {
            // 操作失败，恢复原状态
            isFavorite.value = originalState;
            uni.showToast({
                title: res.msg || '操作失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('收藏操作失败:', error);
        // 发生错误，恢复原状态
        isFavorite.value = originalState;
        uni.hideLoading();
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
    } finally {
        // 动画结束后重置状态
        setTimeout(() => {
            isAnimating.value = false;
        }, 500);
    }
}

// 联系卖家
const contactSeller = async () => {
    if (!goodsDetail.value || !goodsDetail.value.userId) {
        uni.showToast({
            title: '无法获取卖家信息',
            icon: 'none'
        })
        return
    }
    
    try {
        // 显示加载提示
        uni.showLoading({
            title: '正在连接...'
        })
        
        // 发起请求获取对话ID
        const res = await request('/chat/initiate', {
            method: 'GET',
            data: { userId: goodsDetail.value.userId }
        })
        
        uni.hideLoading()
        
        if (res.code === 200 && res.data && res.data.id) {
            // 获取到对话ID后跳转到聊天页面
            uni.navigateTo({
                url: `/pages/chat/chat?id=${res.data.id}&userId=${goodsDetail.value.userId}&username=${goodsDetail.value.username || '用户' + goodsDetail.value.userId}`
            })
        } else {
            uni.showToast({
                title: res.msg || '连接聊天失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('初始化聊天失败:', error)
        uni.hideLoading()
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
    }
}

// 分享商品
const shareGoods = () => {
    uni.showToast({
        title: '分享功能开发中',
        icon: 'none',
        image: '/static/creating.png'
    })
}

// 购买商品
const purchaseGoods = () => {
    if (!goodsDetail.value) return
    
    uni.navigateTo({
        url: `/pages/shop/order?id=${goodsId.value}`
    })
}

// 预览图片
const previewImage = (index) => {
    if (!goodsDetail.value || !goodsDetail.value.imageUrl || goodsDetail.value.imageUrl.length === 0) return
    
    uni.previewImage({
        current: index,
        urls: goodsDetail.value.imageUrl
    })
}

// 跳转到用户个人中心页面
const goToUserProfile = (userId) => {
    if (!userId) return
    
    uni.navigateTo({
        url: `/pages/user/personal-center?userId=${userId}`
    })
}

// 格式化时间
const formatTime = (timestamp) => {
    if (!timestamp) return ''
    
    // 处理后端返回的LocalDateTime格式 (ISO-8601)
    let date
    if (typeof timestamp === 'string') {
        date = new Date(timestamp)
    } else {
        date = new Date(timestamp)
    }
    
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 使用defineExpose导出方法供页面实例调用
defineExpose({
    onLoad(options) {
        console.log('页面onLoad被调用，参数:', options)
        if (options && options.id) {
            goodsId.value = options.id
            console.log('onLoad设置goodsId:', goodsId.value)
            getGoodsDetail()
        }
    }
})

// 页面加载时获取商品详情
onMounted(() => {
    // 如果之前没有设置goodsId，尝试从URL获取
    if (!goodsId.value) {
        try {
            const pages = getCurrentPages()
            if (pages && pages.length > 0) {
                const currentPage = pages[pages.length - 1]
                console.log('当前页面对象:', currentPage)
                
                // 小程序环境
                if (currentPage.options && currentPage.options.id) {
                    goodsId.value = currentPage.options.id
                    console.log('从options获取id:', goodsId.value)
                } 
                // App环境
                else if (currentPage.$page && currentPage.$page.options && currentPage.$page.options.id) {
                    goodsId.value = currentPage.$page.options.id
                    console.log('从$page.options获取id:', goodsId.value)
                }
                // 尝试从route中获取
                else if (currentPage.$route && currentPage.$route.query && currentPage.$route.query.id) {
                    goodsId.value = currentPage.$route.query.id
                    console.log('从$route.query获取id:', goodsId.value)
                }
                
                if (goodsId.value) {
                    getGoodsDetail()
                } else {
                    console.log('未能从页面对象获取ID参数')
                }
            } else {
                console.log('getCurrentPages返回空数组或null')
            }
        } catch (error) {
            console.error('获取页面参数失败:', error)
        }
    } else {
        console.log('使用已设置的goodsId:', goodsId.value)
        getGoodsDetail()
    }
})
</script>

<style>
.detail-container {
    background-color: #f5f7fa;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

/* 导航栏样式 */
.nav-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 30rpx;
    background-color: #fff;
    position: sticky;
    top: 0;
    z-index: 100;
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

.nav-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
}

.placeholder {
    width: 60rpx;
}

/* 内容区域 */
.detail-content {
    flex: 1;
    padding-bottom: 120rpx; /* 为底部操作栏留出空间 */
}

/* 加载中样式 */
.loading-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;
}

.loading-animation {
    width: 40rpx;
    height: 40rpx;
    border: 4rpx solid #f3f3f3;
    border-top: 4rpx solid #1abc9c;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 20rpx;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.loading-text {
    font-size: 24rpx;
    color: #999;
}

/* 商品轮播图 */
.goods-swiper {
    width: 100%;
    height: 750rpx;
}

.swiper-image {
    width: 100%;
    height: 100%;
}

/* 商品价格信息 */
.goods-price-section {
    background-color: #fff;
    padding: 30rpx;
}

.price-row {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
}

.goods-price {
    font-size: 48rpx;
    color: #ff4d4f;
    font-weight: bold;
}

.goods-name {
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
    line-height: 1.4;
}

/* 卖家信息 */
.seller-section {
    margin-top: 20rpx;
    background-color: #fff;
    padding: 30rpx;
}

.section-title {
    font-size: 30rpx;
    color: #333;
    font-weight: bold;
    margin-bottom: 20rpx;
    position: relative;
    padding-left: 20rpx;
}

.section-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 6rpx;
    width: 8rpx;
    height: 30rpx;
    background-color: #1abc9c;
    border-radius: 4rpx;
}

.seller-info {
    display: flex;
    align-items: center;
}

.avatar-wrapper {
    position: relative;
    margin-right: 20rpx;
    cursor: pointer;
}

.seller-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 40rpx;
    z-index: 1;
}

.avatar-frame {
    position: absolute;
    top: -20rpx;
    left: -20rpx;
    width: 120rpx;
    height: 120rpx;
    z-index: 2;
    pointer-events: none;
}

.seller-detail {
    flex: 1;
    cursor: pointer;
}

.seller-name-wrapper {
    display: flex;
    align-items: center;
}

.seller-name {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-right: 10rpx;
}

.user-level {
    display: inline-block;
    font-size: 20rpx;
    padding: 2rpx 10rpx;
    border-radius: 20rpx;
    background-color: #e6f7ff;
    color: #1890ff;
}

.level-1 {
    background-color: #e6f7ff;
    color: #1890ff;
}

.level-2 {
    background-color: #f6ffed;
    color: #52c41a;
}

.level-3 {
    background-color: #fff7e6;
    color: #fa8c16;
}

.level-4 {
    background-color: #fff1f0;
    color: #f5222d;
}

.level-5 {
    background-color: #f5f0ff;
    color: #722ed1;
}

.publish-time {
    font-size: 24rpx;
    color: #999;
    margin-top: 4rpx;
}

.contact-btn {
    padding: 10rpx 30rpx;
    background-color: #f0f7ff;
    border-radius: 30rpx;
}

.contact-text {
    font-size: 26rpx;
    color: #1890ff;
}

/* 商品介绍 */
.goods-intro-section {
    margin-top: 20rpx;
    background-color: #fff;
    padding: 30rpx;
    margin-bottom: 30rpx;
}

.goods-intro {
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
}

/* 底部操作栏 */
.bottom-action-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 100rpx;
    background-color: #fff;
    display: flex;
    align-items: center;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
    z-index: 99;
}

.action-btn {
    flex: 1;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
}

.favorite-btn {
    position: relative;
}

.favorite-animate {
    animation: favorite-pulse 0.5s ease;
}

@keyframes favorite-pulse {
    0% { transform: scale(1); }
    50% { transform: scale(1.2); }
    100% { transform: scale(1); }
}

.action-icon {
    font-size: 32rpx;
    margin-bottom: 4rpx;
    transition: all 0.3s ease;
}

.favorite-btn .action-icon {
    transform-origin: center;
}

.action-text {
    font-size: 22rpx;
    color: #666;
}

.purchase-btn {
    width: 300rpx;
    height: 80rpx;
    background: linear-gradient(135deg, #1abc9c, #b54aff);
    color: #fff;
    font-size: 30rpx;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 40rpx;
    margin: 0 20rpx;
}

/* 加载失败 */
.load-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;
}

.error-icon {
    width: 120rpx;
    height: 120rpx;
    margin-bottom: 30rpx;
}

.error-text {
    font-size: 28rpx;
    color: #999;
    margin-bottom: 30rpx;
}

.retry-btn {
    background-color: #1abc9c;
    color: #fff;
    font-size: 28rpx;
    border-radius: 40rpx;
    padding: 10rpx 40rpx;
}
</style>