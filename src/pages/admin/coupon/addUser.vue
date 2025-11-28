<template>
  <view class="add-user-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-content">
        <text class="page-title">添加用户优惠券</text>
        <text class="page-subtitle">选择用户并给予优惠券</text>
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
      <button class="btn btn-back" @click="goBack">
        <image src="/static/icon/edit.png" class="btn-icon" mode="aspectFit"></image>
        <text>返回</text>
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
          <text class="empty-hint">请搜索用户进行添加</text>
        </view>
        <view v-else class="list-content">
          <view class="user-card" v-for="item in userList" :key="item.id">
            <view class="user-avatar">
              <image 
                :src="item.avatar || '/static/user.png'" 
                class="avatar-img"
                mode="aspectFill"
              ></image>
            </view>
            
            <view class="user-info">
              <view class="user-header">
                <text class="user-name">{{ item.username }}</text>
                <text class="user-level" v-if="item.level">Lv.{{ item.level }}</text>
              </view>
              <view class="user-meta">
                <text class="meta-label">用户ID：</text>
                <text class="meta-value">{{ item.id }}</text>
              </view>
              <view class="user-meta">
                <text class="meta-label">手机号：</text>
                <text class="meta-value">{{ item.phone || '未绑定' }}</text>
              </view>
            </view>
            
            <view class="action-buttons">
              <button class="btn-add-coupon" @click="addCouponToUser(item)">
                <text class="btn-text">添加</text>
              </button>
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

const API_BASE = '/admin'

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
    const res = await request(`${API_BASE}/user/page`, {
      method: 'GET',
      data: {
        pageNum: pageNum.value,
        keyword: searchUsername.value || ''
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

// 给用户添加优惠券
const addCouponToUser = async (user) => {
  if (!couponId.value) {
    uni.showToast({ title: '优惠券ID不存在', icon: 'none' })
    return
  }
  
  // 弹出输入框让用户输入有效天数
  uni.showModal({
    title: '设置优惠券有效期',
    content: '',
    editable: true,
    placeholderText: '请输入天数（如：7）',
    success: async (res) => {
      if (res.confirm) {
        const day = parseInt(res.content)
        if (!day || day <= 0) {
          uni.showToast({ title: '请输入有效的天数', icon: 'none' })
          return
        }
        
        try {
          uni.showLoading({ title: '添加中...' })
          
          const res = await request(`/admin/coupon/userCoupon/add`, {
            method: 'GET',
            data: {
              userId: user.id,
              couponId: couponId.value,
              day: day
            }
          })
          
          uni.hideLoading()
          
          if (res.code === 200) {
            uni.showToast({ title: '添加成功', icon: 'success' })
            // 添加成功后刷新用户列表
            setTimeout(() => {
              fetchUserList()
            }, 1500)
          } else {
            uni.showToast({ title: res.msg || '添加失败', icon: 'none' })
          }
        } catch (e) {
          uni.hideLoading()
          console.error('添加优惠券失败', e)
          uni.showToast({ title: '添加失败', icon: 'none' })
        }
      }
    }
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
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
.add-user-page {
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

.btn-back {
  background: linear-gradient(135deg, #ff9800, #ffa726);
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

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.user-name {
  font-size: 32rpx;
  color: #2c3e50;
  font-weight: bold;
  margin-right: 16rpx;
}

.user-level {
  font-size: 22rpx;
  color: #ff9800;
  background: rgba(255, 152, 0, 0.1);
  padding: 2rpx 12rpx;
  border-radius: 20rpx;
  border: 1rpx solid rgba(255, 152, 0, 0.3);
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

/* 操作按钮 */
.action-buttons {
  display: flex;
  align-items: center;
  margin-left: 20rpx;
}

.btn-add-coupon {
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #4caf50, #66bb6a);
  color: #fff;
  border: none;
  border-radius: 20rpx;
  font-size: 26rpx;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
  transition: all 0.3s ease;
}

.btn-add-coupon:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(76, 175, 80, 0.3);
}

.btn-text {
  font-size: 26rpx;
  font-weight: 500;
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