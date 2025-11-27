<template>
  <view class="admin-coupon-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-content">
        <text class="page-title">优惠券管理</text>
        <text class="page-subtitle">管理您的优惠券活动</text>
      </view>
      <view class="header-badge">
        <text class="badge-count">{{ total }}</text>
        <text class="badge-label">张</text>
      </view>
    </view>

    <!-- 顶部工具栏：搜索 -->
    <view class="toolbar">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          v-model="searchName" 
          placeholder="输入优惠券名称搜索" 
          confirm-type="search" 
          @confirm="handleSearch" 
        />
      </view>
    </view>
    
    <view class="action-bar">
      <button class="btn btn-search" @click="handleSearch">
        <text class="btn-icon">🔍</text>
        <text>搜索</text>
      </button>
      <button class="btn btn-add" @click="toAdd">
        <text class="btn-icon">➕</text>
        <text>新增优惠券</text>
      </button>
    </view>

    <!-- 列表 -->
    <scroll-view 
      class="list" 
      scroll-y
      @scrolltolower="loadMore"
      :lower-threshold="100"
    >
      <view v-if="loading && couponList.length === 0" class="loading">
        <text class="loading-icon">⏳</text>
        <text class="loading-text">加载中...</text>
      </view>
      <view v-else>
        <view v-if="couponList.length === 0" class="empty">
          <text class="empty-icon">🎫</text>
          <text class="empty-text">暂无优惠券数据</text>
          <text class="empty-hint">点击"新增优惠券"开始添加</text>
        </view>
        <view v-else class="list-content">
          <view class="card" v-for="item in couponList" :key="item.couponId">
            <view class="card-left">
              <view class="amount-box">
                <text class="currency">¥</text>
                <text class="amount">{{ Number(item.discount).toFixed(0) }}</text>
              </view>
              <text class="coupon-type">优惠券</text>
            </view>
            
            <view class="card-divider">
              <view class="divider-dot top"></view>
              <view class="divider-line"></view>
              <view class="divider-dot bottom"></view>
            </view>
            
            <view class="card-body">
              <view class="card-info">
                <text class="card-name">{{ item.name }}</text>
                <text class="card-desc" v-if="item.description">{{ item.description }}</text>
                <view class="card-meta">
                  <text class="meta-label">创建时间：</text>
                  <text class="meta-value">{{ formatDate(item.createdTime) }}</text>
                </view>
              </view>
              <view class="card-actions">
                <button class="action-btn btn-users" size="mini" @click="viewUsers(item)">
                  <text class="action-icon">👥</text>
                  <text>查看用户</text>
                </button>
                <button class="action-btn btn-edit" size="mini" @click="toEdit(item)">
                  <text class="action-icon">✏️</text>
                  <text>编辑</text>
                </button>
                <button class="action-btn btn-delete" size="mini" @click="remove(item)">
                  <text class="action-icon">🗑️</text>
                  <text>删除</text>
                </button>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 加载更多提示 -->
        <view v-if="couponList.length > 0" class="load-more">
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

const searchName = ref('')
const couponList = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

// 是否还有更多数据
const hasMore = computed(() => pageNum.value < totalPages.value)

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 获取优惠券列表
const fetchList = async (isLoadMore = false) => {
  if (loading.value || (loadingMore.value && isLoadMore)) return
  
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
    pageNum.value = 1
  }
  
  try {
    const res = await request(`${API_BASE}/list`, {
      method: 'GET',
      data: {
        pageNum: pageNum.value,
        name: searchName.value || ''
      }
    })
    
    const data = res.data || res
    const records = data.records || data.list || []
    
    if (isLoadMore) {
      couponList.value = [...couponList.value, ...records]
    } else {
      couponList.value = records
    }
    
    total.value = data.total || 0
    totalPages.value = data.pages || Math.ceil(total.value / pageSize.value)
  } catch (e) {
    console.error('获取优惠券列表失败', e)
    uni.showToast({ title: '获取列表失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

// 加载更多
const loadMore = () => {
  if (!hasMore.value || loadingMore.value) return
  pageNum.value++
  fetchList(true)
}

// 新增
const toAdd = () => {
  uni.navigateTo({ url: '/pages/admin/coupon/edit' })
}

// 编辑
const toEdit = (item) => {
  uni.navigateTo({ url: `/pages/admin/coupon/edit?id=${item.couponId}` })
}

// 查看用户
const viewUsers = (item) => {
  uni.navigateTo({ url: `/pages/admin/coupon/userList?couponId=${item.couponId}` })
}

// 删除
const remove = (item) => {
  uni.showModal({
    title: '提示',
    content: `确定删除"${item.name}"吗？`,
    success: async (r) => {
      if (r.confirm) {
        try {
          await request(`${API_BASE}/deleteCoupon`, { 
            method: 'GET',
            data: { couponId: item.couponId }
          })
          uni.showToast({ title: '删除成功', icon: 'success' })
          fetchList()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(fetchList)
onShow(() => {
  // 从编辑页返回时刷新
  fetchList()
})
</script>

<style scoped>
/* 主容器 - 茶叶主题背景 */
.admin-coupon-page {
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
  background: linear-gradient(135deg, #ff9800, #ffa726);
  border-radius: 50%;
  box-shadow: 0 8rpx 20rpx rgba(255, 152, 0, 0.3);
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
  font-size: 32rpx;
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
  font-size: 32rpx;
  margin-right: 8rpx;
}

.btn-search {
  background: linear-gradient(135deg, #42a5f5, #64b5f6);
  color: #fff;
}

.btn-add {
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

/* 优惠券卡片 */
.card {
  display: flex;
  padding: 24rpx;
  margin-bottom: 20rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  overflow: hidden;
}

.card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

/* 左侧金额区域 */
.card-left {
  width: 160rpx;
  background: linear-gradient(135deg, #ff9800, #ffa726);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  border-radius: 16rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.amount-box {
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
}

.currency {
  font-size: 28rpx;
  color: #fff;
  font-weight: bold;
  margin-right: 2rpx;
}

.amount {
  font-size: 48rpx;
  color: #fff;
  font-weight: bold;
  line-height: 1;
}

.coupon-type {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

/* 分隔线 */
.card-divider {
  width: 2rpx;
  position: relative;
  background: transparent;
  margin: 0 12rpx;
  flex-shrink: 0;
}

.divider-line {
  position: absolute;
  top: 10rpx;
  bottom: 10rpx;
  left: 0;
  right: 0;
  border-left: 2rpx dashed #e0e0e0;
}

.divider-dot {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #f8faf8;
}

.divider-dot.top {
  top: -8rpx;
}

.divider-dot.bottom {
  bottom: -8rpx;
}

/* 卡片主体 */
.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-info {
  flex: 1;
}

.card-name {
  display: block;
  font-size: 32rpx;
  color: #2c3e50;
  font-weight: bold;
  line-height: 1.4;
  margin-bottom: 8rpx;
}

.card-desc {
  display: block;
  font-size: 24rpx;
  color: #7f8c8d;
  line-height: 1.5;
  margin-bottom: 12rpx;
}

.card-meta {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.meta-label {
  font-size: 24rpx;
  color: #999;
}

.meta-value {
  font-size: 24rpx;
  color: #666;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12rpx 20rpx;
  height: 60rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  font-weight: 500;
  border: none;
  transition: all 0.3s ease;
}

.action-btn:active {
  transform: scale(0.95);
}

.action-icon {
  font-size: 28rpx;
  margin-right: 4rpx;
}

.btn-users {
  background: linear-gradient(135deg, #4caf50, #66bb6a);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
}

.btn-edit {
  background: linear-gradient(135deg, #42a5f5, #64b5f6);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(66, 165, 245, 0.3);
}

.btn-delete {
  background: linear-gradient(135deg, #ff6b6b, #ff8787);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.3);
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
