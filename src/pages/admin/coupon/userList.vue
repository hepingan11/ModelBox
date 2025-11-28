<template>
  <view class="user-coupon-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-content">
        <text class="page-title">优惠券用户列表</text>
        <text class="page-subtitle">查看拥有该优惠券的用户</text>
      </view>
      <view class="header-badge">
        <text class="badge-count">{{ total }}</text>
        <text class="badge-label">人</text>
      </view>
    </view>

    <!-- 顶部工具栏：搜索 -->
    <view class="toolbar">
      <view class="search-box">
        <image src="/static/icon/search.png" class="search-icon" mode="aspectFit"></image>
        <input 
          class="search-input" 
          v-model="searchUsername" 
          placeholder="输入用户名搜索" 
          confirm-type="search" 
          @confirm="handleSearch" 
        />
      </view>
    </view>
    
    <view class="action-bar">
      <button class="btn btn-search" @click="handleSearch">
        <image src="/static/icon/search.png" class="btn-icon" mode="aspectFit"></image>
        <text>搜索</text>
      </button>
      <button class="btn btn-add" @click="goToAddUser">
        <image src="/static/icon/add.png" class="btn-icon" mode="aspectFit"></image>
        <text>添加</text>
      </button>
    </view>

    <!-- 用户列表 -->
    <scroll-view 
      class="list" 
      scroll-y
      @scrolltolower="loadMore"
      :lower-threshold="100"
    >
      <view v-if="loading && userList.length === 0" class="loading">
        <text class="loading-icon">⏳</text>
        <text class="loading-text">加载中...</text>
      </view>
      <view v-else>
        <view v-if="userList.length === 0" class="empty">
          <text class="empty-icon">👥</text>
          <text class="empty-text">暂无用户数据</text>
          <text class="empty-hint">该优惠券暂无用户领取</text>
        </view>
        <view v-else class="list-content">
          <view class="user-card" v-for="item in userList" :key="item.userId">
            <view class="user-avatar">
              <image 
                :src="item.avatar || '/static/user.png'" 
                class="avatar-img"
                mode="aspectFill"
              ></image>
            </view>
            
            <view class="user-info">
              <view class="name-row">
                <text class="user-name">{{ item.userName }}</text>
                <view class="coupon-tag" :class="item.type === 1 ? 'discount' : 'reduce'">
                  <text>{{ item.type === 0 ? '满减' : (item.discount === 0 ? '免单' : '折扣') }}</text>
                  <text v-if="item.discount > 0" style="margin-left: 4rpx;">
                    {{ item.type === 0 ? '¥' + Number(item.discount).toFixed(0) : formatDiscount(item.discount) + '折' }}
                  </text>
                </view>
              </view>
              
              <view class="user-meta">
                <text class="meta-label">领取时间：</text>
                <text class="meta-value">{{ formatDate(item.gettingTime) }}</text>
              </view>
              <view class="user-meta" v-if="item.usedTime">
                <text class="meta-label">使用时间：</text>
                <text class="meta-value">{{ formatDate(item.usedTime) }}</text>
              </view>
              <view class="user-meta">
                <text class="meta-label">失效时间：</text>
                <text class="meta-value">{{ formatDate(item.expireTime) }}</text>
              </view>
            </view>
            
            <view class="status-badge" :class="getStatusClass(item.status)">
              <text class="status-text">{{ item.status }}</text>
            </view>
          </view>
        </view>
        
        <!-- 加载更多提示 -->
        <view v-if="userList.length > 0" class="load-more">
          <text v-if="loadingMore" class="load-more-text">加载中...</text>
          <text v-else-if="hasMore" class="load-more-text">上拉加载更多</text>
          <text v-else class="load-more-text">没有更多数据了</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '../../../utils/request.js'

const API_BASE = '/admin/coupon'

const searchUsername = ref('')
const userList = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)
const couponId = ref('')

// 是否还有更多数据
const hasMore = computed(() => pageNum.value < totalPages.value)

// 检查是否过期
const isExpired = (expirationTime) => {
  if (!expirationTime) return false
  return new Date(expirationTime) < new Date()
}

// 格式化折扣
const formatDiscount = (val) => {
  const num = Number(val)
  if (isNaN(num)) return val
  // 如果是0-1之间的小数，乘以10
  if (num > 0 && num <= 1) {
    return parseFloat((num * 10).toFixed(1))
  }
  return num
}

// 获取状态样式类
const getStatusClass = (status) => {
  if (!status) return ''
  if (status === '已过期' || status === '已失效') return 'expired'
  if (status === '已使用') return 'used'
  if (status === '未使用') return 'active'
  return ''
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '--'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 获取用户列表
const fetchUserList = async (isLoadMore = false) => {
  if (loading.value || (loadingMore.value && isLoadMore)) return
  
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
    pageNum.value = 1
  }
  
  try {
    const res = await request(`${API_BASE}/userCoupon/list`, {
      method: 'GET',
      data: {
        pageNum: pageNum.value,
        username: searchUsername.value || '',
        couponId: couponId.value
      }
    })
    
    const data = res.data || res
    const records = data.records || data.list || []
    
    if (isLoadMore) {
      userList.value = [...userList.value, ...records]
    } else {
      userList.value = records
    }
    
    total.value = data.total || 0
    totalPages.value = data.pages || Math.ceil(total.value / pageSize.value)
  } catch (e) {
    console.error('获取用户列表失败', e)
    uni.showToast({ title: '获取用户列表失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchUserList()
}

// 加载更多
const loadMore = () => {
  if (!hasMore.value || loadingMore.value) return
  pageNum.value++
  fetchUserList(true)
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 跳转到添加用户页面
const goToAddUser = () => {
  if (!couponId.value) {
    uni.showToast({ title: '优惠券ID不存在', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/admin/coupon/addUser?couponId=${couponId.value}`
  })
}

onMounted(() => {
  // 获取路由参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  
  if (options.couponId) {
    couponId.value = options.couponId
    fetchUserList()
  } else {
    uni.showToast({ title: '优惠券ID不存在', icon: 'none' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }
})

onShow(() => {
  // 页面显示时刷新数据
  if (couponId.value) {
    fetchUserList()
  }
})
</script>

<style scoped>
/* 主容器 - 茶叶主题背景 */
.user-coupon-page {
  display: flex;
  height: 100vh;
  flex-direction: column;
  background: linear-gradient(180deg, #F3F8EA 0%, #E8F5E9 100%);
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 24rpx 24rpx;
}

.header-content {
  flex: 1;
}

.page-title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 8rpx;
  background: linear-gradient(135deg, #2c3e50, #4caf50);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  display: block;
  font-size: 26rpx;
  color: #7f8c8d;
  font-weight: 500;
}

.header-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #42a5f5, #64b5f6);
  border-radius: 50%;
  box-shadow: 0 8rpx 20rpx rgba(66, 165, 245, 0.3);
}

.badge-count {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  line-height: 1;
}

.badge-label {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 4rpx;
}

/* 工具栏 */
.toolbar {
  display: flex;
  gap: 16rpx;
  padding: 0 20rpx 16rpx;
  align-items: center;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  height: 80rpx;
  background: #fff;
  border: 2rpx solid #e8f5e9;
  border-radius: 40rpx;
  padding: 0 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.search-box:focus-within {
  border-color: #81c784;
  box-shadow: 0 4rpx 16rpx rgba(129, 199, 132, 0.2);
}

.search-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #333;
}

.search-input::placeholder {
  color: #999;
}

/* 操作按钮栏 */
.action-bar {
  display: flex;
  gap: 16rpx;
  padding: 0 20rpx 20rpx;
}

.btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 3rpx 10rpx rgba(0, 0, 0, 0.1);
}

.btn-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 15rpx;
}

.btn-search {
  background: linear-gradient(135deg, #42a5f5, #64b5f6);
  color: #fff;
}

.btn-add {
  background: linear-gradient(135deg, #4caf50, #66bb6a);
  color: #fff;
}

/* 列表容器 */
.list {
  flex: 1;
  padding: 0 20rpx;
}

.list-content {
  padding-bottom: 20rpx;
}

/* 加载和空状态 */
.loading, .empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  text-align: center;
}

.loading-icon, .empty-icon {
  font-size: 96rpx;
  margin-bottom: 20rpx;
}

.loading-text, .empty-text {
  font-size: 32rpx;
  color: #666;
  font-weight: 500;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #999;
}

/* 用户卡片 */
.user-card {
  display: flex;
  align-items: center;
  padding: 24rpx;
  margin-bottom: 20rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
}

.user-card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

/* 用户头像 */
.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

/* 用户信息 */
.user-info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.user-name {
  font-size: 32rpx;
  color: #2c3e50;
  font-weight: bold;
  margin-bottom: 0;
  margin-right: 16rpx;
}

.coupon-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  display: flex;
  align-items: center;
}

.coupon-tag.reduce {
  background: rgba(255, 152, 0, 0.1);
  color: #ff9800;
  border: 1rpx solid rgba(255, 152, 0, 0.3);
}

.coupon-tag.discount {
  background: rgba(26, 188, 156, 0.1);
  color: #1abc9c;
  border: 1rpx solid rgba(26, 188, 156, 0.3);
}

.user-meta {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.meta-label {
  font-size: 24rpx;
  color: #999;
  margin-right: 8rpx;
}

.meta-value {
  font-size: 24rpx;
  color: #666;
}

/* 状态徽章 */
.status-badge {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
  background: #ccc; /* 默认 */
  color: #fff;
}

.status-badge.active {
  background: linear-gradient(135deg, #4caf50, #66bb6a);
}

.status-badge.used {
  background: linear-gradient(135deg, #42a5f5, #64b5f6);
}

.status-badge.expired {
  background: linear-gradient(135deg, #f44336, #ef5350);
}

.status-text {
  font-size: 22rpx;
}

/* 加载更多 */
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx 0;
}

.load-more-text {
  font-size: 26rpx;
  color: #999;
}
</style>