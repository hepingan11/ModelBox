<template>
  <view class="container">
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text>加载中...</text>
    </view>
    
    <view v-else class="content">
      <!-- 订单状态卡片 -->
      <view class="status-card">
        <view class="status-header">
          <text class="status-text">待接单</text>
          <text class="order-id">订单号：{{ orderInfo.orderNo || orderInfo.deliveryOrdersId }}</text>
        </view>
        <view class="price-info">
          <text class="label">配送费</text>
          <view class="price-value">
            <text class="currency">¥</text>
            <text class="amount">{{ orderInfo.deliveryFee }}</text>
          </view>
        </view>
      </view>
      
      <!-- 配送路线 -->
      <view class="section-card">
        <view class="section-title">配送路线</view>
        <view class="route-info">
          <view class="route-item">
            <view class="dot start"></view>
            <view class="address-box">
              <text class="address-title">取货地点</text>
              <text class="address-detail">{{ orderInfo.startingAddress }}</text>
            </view>
          </view>
          <view class="route-line"></view>
          <view class="route-item">
            <view class="dot end"></view>
            <view class="address-box">
              <text class="address-title">送达地点</text>
              <text class="address-detail">{{ orderInfo.addressContent }}</text>
              <text class="dorm-tag" v-if="orderInfo.dormitoryName">({{ orderInfo.dormitoryName }})</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 订单详情 -->
      <view class="section-card">
        <view class="section-title">订单详情</view>
        <view class="info-row">
          <text class="label">物品价值</text>
          <text class="value">¥{{ orderInfo.deliveryShopValue || 0 }}</text>
        </view>
        <view class="info-row">
          <text class="label">期望送达</text>
          <text class="value">{{ formatTime(orderInfo.deliveryTime) || '尽快送达' }}</text>
        </view>
        <view class="info-row" v-if="orderInfo.takeoutNo">
          <text class="label">取货凭证</text>
          <text class="value">{{ orderInfo.takeoutNo }}</text>
        </view>
        <view class="info-row" v-if="orderInfo.note">
          <text class="label">备注信息</text>
          <text class="value note">{{ orderInfo.note }}</text>
        </view>
        <view class="info-row">
          <text class="label">发布时间</text>
          <text class="value">{{ formatTime(orderInfo.createdTime) }}</text>
        </view>
      </view>
      
      <!-- 下单用户信息 -->
      <view class="section-card" v-if="userInfo">
        <view class="section-title">下单用户</view>
        <view class="user-profile" @click="goToUserProfile">
          <image :src="userInfo.avatar || '/static/default-avatar.png'" class="avatar" mode="aspectFill"></image>
          <view class="user-details">
            <text class="username">{{ userInfo.username || '匿名用户' }}</text>
            <text class="user-level" v-if="userInfo.level">Lv.{{ userInfo.level }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="!loading">
      <button class="grab-btn" @click="handleGrabOrder">立即抢单</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const orderId = ref('')
const orderInfo = ref({})
const userInfo = ref(null)
const loading = ref(true)

onLoad((options) => {
  if (options.id) {
    orderId.value = options.id
    getOrderDetail()
  }
})

// 获取订单详情
const getOrderDetail = async () => {
  loading.value = true
  try {
    // 获取订单信息
    const res = await request('/delivery/getDeliveryById', {
      method: 'GET',
      data: { deliveryOrdersId: orderId.value }
    })
    
    if (res.code === 200 && res.data) {
      orderInfo.value = res.data
      
      // 检查是否匿名
      if (res.data.isNick) {
        // 匿名显示
        userInfo.value = {
          nickName: '匿名用户',
          avatar: '/static/default-avatar.png',
          level: ''
        }
      } else if (res.data.customerId) {
        // 非匿名，获取真实用户信息
        getUserInfo(res.data.customerId)
      }
    } else {
      uni.showToast({ title: '获取订单详情失败', icon: 'none' })
    }
  } catch (e) {
    console.error('获取详情失败', e)
    uni.showToast({ title: '网络错误', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 获取用户信息
const getUserInfo = async (userId) => {
  try {
    const res = await request('/user/getUserInfoById', {
      method: 'GET',
      data: { userId }
    })
    if (res.code === 200) {
      userInfo.value = res.data
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

// 跳转到个人主页
const goToUserProfile = () => {
  // 只有非匿名用户且有customerId时才跳转
  if (!orderInfo.value.isNick && orderInfo.value.customerId) {
    uni.navigateTo({
      url: `/pages/user/personal-center?userId=${orderInfo.value.customerId}`
    })
  }
}

// 抢单
const handleGrabOrder = () => {
  uni.showModal({
    title: '确认抢单',
    content: '确定要接下这个订单吗？接单后请尽快完成配送。',
    success: async (res) => {
      if (res.confirm) {
        // TODO: 调用抢单API
        uni.showToast({
          title: '抢单功能开发中',
          icon: 'none'
        })
      }
    }
  })
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const pad = (n) => n < 10 ? `0${n}` : n
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 120rpx;
}

.loading-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #999;
  font-size: 28rpx;
}

.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid #eee;
  border-top: 4rpx solid #1abc9c;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 20rpx;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.content {
  padding: 20rpx;
}

.status-card {
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  color: #fff;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(26, 188, 156, 0.3);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.status-text {
  font-size: 40rpx;
  font-weight: bold;
}

.order-id {
  font-size: 24rpx;
  opacity: 0.8;
}

.price-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.price-info .label {
  font-size: 24rpx;
  opacity: 0.9;
  margin-bottom: 4rpx;
}

.price-value {
  display: flex;
  align-items: baseline;
}

.price-value .currency {
  font-size: 28rpx;
  margin-right: 4rpx;
}

.price-value .amount {
  font-size: 60rpx;
  font-weight: bold;
}

.section-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.02);
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  border-left: 6rpx solid #1abc9c;
  padding-left: 16rpx;
}

.route-info {
  position: relative;
}

.route-item {
  display: flex;
  align-items: flex-start;
}

.dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  margin-top: 10rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.dot.start {
  background-color: #1abc9c;
  border: 4rpx solid #e0f2f1;
}

.dot.end {
  background-color: #ff9800;
  border: 4rpx solid #fff3e0;
}

.route-line {
  width: 2rpx;
  height: 60rpx;
  background-color: #eee;
  margin-left: 13rpx;
  margin-top: -10rpx;
  margin-bottom: -10rpx;
}

.address-box {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.address-title {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 6rpx;
}

.address-detail {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
  line-height: 1.4;
}

.dorm-tag {
  font-size: 24rpx;
  color: #1abc9c;
  margin-top: 4rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  font-size: 28rpx;
  color: #666;
}

.info-row .value {
  font-size: 28rpx;
  color: #333;
  text-align: right;
  flex: 1;
  margin-left: 20rpx;
}

.info-row .value.note {
  color: #666;
}

.user-profile {
  display: flex;
  align-items: center;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #f0f0f0;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 6rpx;
}

.user-level {
  font-size: 20rpx;
  color: #ff9800;
  background-color: #fff3e0;
  padding: 2rpx 12rpx;
  border-radius: 20rpx;
  align-self: flex-start;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background-color: #fff;
  padding: 20rpx 30rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
  z-index: 100;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.grab-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(90deg, #1abc9c, #16a085);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 44rpx;
  margin: 0;
}

.grab-btn:active {
  opacity: 0.9;
  transform: scale(0.98);
}
</style>
