<template>
  <view class="container">
    <!-- 顶部筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item">
        <picker 
          @change="handleDormitoryChange" 
          :value="dormitoryIndex" 
          :range="dormitoryList" 
          range-key="dormitoryName"
        >
          <view class="picker-view">
            <text class="picker-text">{{ selectedDormitoryName }}</text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
      </view>
      <view class="refresh-btn" @click="refreshList">
        <text>刷新</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view 
      scroll-y 
      class="order-scroll" 
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onPullDownRefresh"
    >
      <view class="order-list" v-if="orderList.length > 0">
        <view class="order-card" v-for="(item, index) in orderList" :key="item.deliveryOrdersId">
          <view class="card-header">
            <view class="user-info">
              <text class="time">{{ formatTime(item.createdTime) }}</text>
            </view>
            <view class="price-tag">
              <text class="currency">¥</text>
              <text class="amount">{{ item.deliveryFee }}</text>
            </view>
          </view>
          
          <view class="card-body">
            <view class="route-info">
              <view class="route-item">
                <view class="dot start"></view>
                <text class="address">{{ item.startingAddress }}</text>
              </view>
              <view class="route-line"></view>
              <view class="route-item">
                <view class="dot end"></view>
                <text class="address">{{ item.addressContent }}</text>
                <text class="dorm-tag" v-if="item.dormitoryName">({{ item.dormitoryName }})</text>
              </view>
            </view>
            
            <view class="info-grid">
              <view class="info-item">
                <text class="label">商品价值</text>
                <text class="value">¥{{ item.deliveryShopValue || 0 }}</text>
              </view>
              <view class="info-item" v-if="item.deliveryTime">
                <text class="label">期望送达</text>
                <text class="value">{{ formatDeliveryTime(item.deliveryTime) }}</text>
              </view>
            </view>
            
            <view class="note-box" v-if="item.note">
              <text class="note-label">备注：</text>
              <text class="note-content">{{ item.note }}</text>
            </view>
          </view>
          
          <view class="card-footer">
            <button class="grab-btn" hover-class="btn-hover" @click="handleGrabOrder(item)">立即抢单</button>
          </view>
        </view>
      </view>
      
      <!-- 空状态/加载状态 -->
      <view class="status-container">
        <view v-if="loading && orderList.length === 0" class="loading-box">
          <view class="loading-spinner"></view>
          <text>正在加载订单...</text>
        </view>
        <view v-else-if="orderList.length === 0" class="empty-box">
          <image src="/static/empty.png" mode="aspectFit" class="empty-img"></image>
          <text>暂无待抢订单</text>
        </view>
        <view v-else-if="loading" class="loading-more">
          <text>加载更多...</text>
        </view>
        <view v-else-if="noMore" class="no-more">
          <text>没有更多订单了</text>
        </view>
      </view>
      
      <view class="safe-area-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 状态变量
const dormitoryList = ref([{ id: null, dormitoryName: '全部宿舍' }])
const dormitoryIndex = ref(0)
const selectedDormitoryName = ref('全部宿舍')
const selectedDormitoryId = ref(null)

const orderList = ref([])
const pageNum = ref(1)
const loading = ref(false)
const noMore = ref(false)
const isRefreshing = ref(false)
const schoolId = ref(1) // 默认学校ID

// 生命周期
onMounted(() => {
  getDormitoryList()
  getWaitOrdersList(true)
})

// 获取宿舍列表
const getDormitoryList = async () => {
  try {
    const res = await request('/delivery/getDormitoryList', {
      method: 'GET',
      data: { schoolId: schoolId.value }
    })
    if (res.code === 200 && res.data) {
      // 保留"全部宿舍"选项
      dormitoryList.value = [{ id: null, dormitoryName: '全部宿舍' }, ...res.data]
    }
  } catch (e) {
    console.error('获取宿舍列表失败', e)
  }
}

// 获取待接单列表
const getWaitOrdersList = async (refresh = false) => {
  if (loading.value) return
  
  if (refresh) {
    pageNum.value = 1
    noMore.value = false
  } else if (noMore.value) {
    return
  }
  
  loading.value = true
  
  try {
    const params = {
      pageNum: pageNum.value,
      schoolId: schoolId.value
    }
    
    if (selectedDormitoryId.value) {
      params.dormitoryId = selectedDormitoryId.value
    }
    
    const res = await request('/delivery/waitOrders/list', {
      method: 'GET',
      data: params
    })
    
    if (res.code === 200) {
      const list = res.data.records || []
      const total = res.data.total || 0
      const size = res.data.size || 10
      
      if (refresh) {
        orderList.value = list
      } else {
        orderList.value = [...orderList.value, ...list]
      }
      
      // 判断是否还有更多数据
      if (orderList.value.length >= total || list.length < size) {
        noMore.value = true
      } else {
        pageNum.value++
      }
    } else {
      uni.showToast({ title: res.msg || '获取订单失败', icon: 'none' })
    }
  } catch (e) {
    console.error('获取订单列表失败', e)
    uni.showToast({ title: '网络错误', icon: 'none' })
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

// 筛选宿舍
const handleDormitoryChange = (e) => {
  const index = e.detail.value
  dormitoryIndex.value = index
  const selected = dormitoryList.value[index]
  selectedDormitoryName.value = selected.dormitoryName
  selectedDormitoryId.value = selected.id
  
  // 重新加载列表
  getWaitOrdersList(true)
}

// 下拉刷新
const onPullDownRefresh = () => {
  isRefreshing.value = true
  getWaitOrdersList(true)
}

// 手动刷新按钮
const refreshList = () => {
  uni.showLoading({ title: '刷新中' })
  getWaitOrdersList(true).then(() => {
    uni.hideLoading()
  })
}

// 加载更多
const loadMore = () => {
  getWaitOrdersList(false)
}

// 抢单操作
const handleGrabOrder = (item) => {
  uni.showModal({
    title: '确认抢单',
    content: `确定要接下这个送到 ${item.dormitoryName || ''} ${item.addressContent} 的订单吗？`,
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
  const now = new Date()
  
  const pad = (n) => n < 10 ? `0${n}` : n
  
  if (date.getDate() === now.getDate() && date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()) {
    return `今天 ${pad(date.getHours())}:${pad(date.getMinutes())}`
  }
  
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const formatDeliveryTime = (timeStr) => {
  if (!timeStr) return '尽快送达'
  return formatTime(timeStr)
}
</script>

<style>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.filter-bar {
  height: 88rpx;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.filter-item {
  flex: 1;
}

.picker-view {
  display: flex;
  align-items: center;
}

.picker-text {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.picker-arrow {
  font-size: 24rpx;
  color: #999;
  margin-left: 8rpx;
}

.refresh-btn {
  font-size: 26rpx;
  color: #1abc9c;
  padding: 10rpx 20rpx;
  background-color: rgba(26, 188, 156, 0.1);
  border-radius: 30rpx;
}

.order-scroll {
  flex: 1;
  height: 0;
}

.order-list {
  padding: 20rpx;
}

.order-card {
  background-color: #fff;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.time {
  font-size: 24rpx;
  color: #999;
}

.price-tag {
  color: #ff5722;
  font-weight: bold;
}

.currency {
  font-size: 24rpx;
}

.amount {
  font-size: 36rpx;
}

.card-body {
  margin-bottom: 20rpx;
}

.route-info {
  margin-bottom: 20rpx;
}

.route-item {
  display: flex;
  align-items: center;
  margin: 10rpx 0;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.dot.start {
  background-color: #1abc9c;
}

.dot.end {
  background-color: #ff9800;
}

.route-line {
  width: 2rpx;
  height: 20rpx;
  background-color: #eee;
  margin-left: 7rpx;
  margin-top: -4rpx;
  margin-bottom: -4rpx;
}

.address {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.dorm-tag {
  font-size: 24rpx;
  color: #666;
  margin-left: 10rpx;
}

.info-grid {
  display: flex;
  background-color: #f9f9f9;
  padding: 16rpx;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.info-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 4rpx;
}

.value {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.note-box {
  background-color: #fff9e6;
  padding: 12rpx 16rpx;
  border-radius: 8rpx;
  display: flex;
  align-items: flex-start;
}

.note-label {
  font-size: 24rpx;
  color: #ff9800;
  flex-shrink: 0;
}

.note-content {
  font-size: 24rpx;
  color: #666;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.grab-btn {
  margin: 0;
  background: linear-gradient(90deg, #1abc9c, #16a085);
  color: #fff;
  font-size: 28rpx;
  padding: 0 40rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(26, 188, 156, 0.3);
}

.btn-hover {
  opacity: 0.9;
  transform: scale(0.98);
}

.status-container {
  padding: 40rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-box, .empty-box, .loading-more, .no-more {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-img {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 20rpx;
  opacity: 0.5;
}

.loading-spinner {
  width: 36rpx;
  height: 36rpx;
  border: 3rpx solid #eee;
  border-top: 3rpx solid #1abc9c;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16rpx;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.status-container text {
  font-size: 24rpx;
  color: #999;
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
}
</style>