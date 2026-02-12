<template>
  <view class="user-list-container" :class="themeClass">
    <!-- 顶部标题栏 -->
    <view class="header-section">
      <view class="header-left">
        <text class="header-title">用户管理</text>
        <text class="user-count">共 {{ totalUsers }} 个用户</text>
      </view>
      <view class="header-right">
        <view class="search-box">
          <image src="/static/icon/search.png" class="search-icon"></image>
          <input 
            type="text" 
            v-model="searchKeyword" 
            @confirm="handleSearch" 
            placeholder="搜索用户名/手机号" 
            class="search-input"
          />
        </view>
        <view class="filter-btn" @click="showFilterOptions = true">
          <image src="/static/icon/filter.png" class="filter-icon"></image>
        </view>
      </view>
    </view>
    
    <!-- 用户列表 - 无限滚动 -->
    <scroll-view 
      scroll-y 
      class="user-scroll-view"
      :style="{ height: scrollViewHeight + 'px' }"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="refreshUserList"
      @scrolltolower="loadMore"
      :lower-threshold="150"
      :scroll-with-animation="true"
      :enhanced="true"
      :show-scrollbar="false"
      :bounces="true"
    >
      <!-- 用户列表项 -->
      <view v-if="userList.length > 0">
        <view 
          class="user-item"
          v-for="(user, index) in userList" 
          :key="user.id"
          @click="showUserDetail(user)"
        >
          <view class="user-avatar-section">
            <image :src="user.avatar || '/static/avatar/default.png'" mode="aspectFill" class="user-avatar" lazy-load></image>
            <!-- 头像框 -->
            <image 
              v-if="user.avatarFrame" 
              :src="user.avatarFrame" 
              class="avatar-frame" 
              mode="aspectFit"
              lazy-load
            ></image>
            <view v-if="user.isVip" class="vip-badge">VIP</view>
          </view>
          
          <view class="user-info-section">
            <view class="user-name-row">
              <text class="user-name">{{ user.userName }}</text>
              <text v-if="user.type === 'ADMIN'" class="admin-tag">管理员</text>
              <text v-if="user.type === 'USER'" class="user-tag">普通用户</text>
              <!-- 用户等级 -->
              <view v-if="user.level" :class="['user-level', `level-${user.level}`]">
                <text class="level-text">Lv.{{ user.level }}</text>
              </view>
            </view>
            
            <view class="user-details">
              <text class="user-id">ID: {{ user.id }}</text>
              <text class="user-phone">{{ user.phone || '未绑定手机' }}</text>
            </view>
            
            <view class="user-stats">
              <text class="reg-time">注册: {{ formatDate(user.createdTime) }}</text>
              <text class="login-time">最近登录: {{ formatDate(user.lastLoginTime) }}</text>
            </view>
          </view>
          
          <!-- 添加操作按钮 -->
          <view class="user-action-section">
            <image src="/static/icon/frame.png" class="action-btn frame-btn" @click.stop="unlockFrame(user)"></image>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-else-if="!isLoading" class="empty-state">
        <image src="/static/icon/empty.png" class="empty-icon"></image>
        <text class="empty-text">暂无用户数据</text>
      </view>
      
      <!-- 底部加载状态 -->
      <view class="load-status" v-if="userList.length > 0">
        <view v-if="isLoadingMore" class="loading-more">
          <view class="loading-spinner"></view>
          <text class="loading-text">加载中...</text>
        </view>
        <view v-else-if="!hasMore" class="all-loaded">
          <text class="all-loaded-text">— 已加载全部 {{ totalUsers }} 条数据 —</text>
        </view>
      </view>
      
      <!-- 首次加载状态 -->
      <view class="loading-more" v-if="isLoading && userList.length === 0">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
    </scroll-view>
    
    <!-- 筛选弹窗 -->
    <view class="filter-popup" v-if="showFilterOptions" @click.self="showFilterOptions = false">
      <view class="filter-content">
        <view class="filter-header">
          <text class="filter-title">筛选条件</text>
          <text class="close-icon" @click="showFilterOptions = false">×</text>
        </view>
        
        <view class="filter-options">
          <view class="filter-option">
            <text class="option-label">用户状态</text>
            <view class="option-values">
              <view 
                class="option-value" 
                :class="{'selected': filterStatus === ''}"
                @click="filterStatus = ''"
              >全部</view>
              <view 
                class="option-value" 
                :class="{'selected': filterStatus === '1'}"
                @click="filterStatus = '1'"
              >正常</view>
              <view 
                class="option-value" 
                :class="{'selected': filterStatus === '0'}"
                @click="filterStatus = '0'"
              >禁用</view>
            </view>
          </view>
          
          <view class="filter-option">
            <text class="option-label">用户角色</text>
            <view class="option-values">
              <view 
                class="option-value" 
                :class="{'selected': filterRole === ''}"
                @click="filterRole = ''"
              >全部</view>
              <view 
                class="option-value" 
                :class="{'selected': filterRole === 'ADMIN'}"
                @click="filterRole = 'ADMIN'"
              >管理员</view>
              <view 
                class="option-value" 
                :class="{'selected': filterRole === 'USER'}"
                @click="filterRole = 'USER'"
              >普通用户</view>
            </view>
          </view>
        </view>
        
        <view class="filter-buttons">
          <button class="reset-btn" @click="resetFilters">重置</button>
          <button class="apply-btn" @click="applyFilters">应用</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request.js'

// 用户列表数据
const userList = ref([])
const totalUsers = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const isLoading = ref(false)       // 首次加载
const isLoadingMore = ref(false)   // 加载更多
const isRefreshing = ref(false)
const hasMore = ref(true)          // 是否还有更多数据

// scroll-view 高度（动态计算）
const scrollViewHeight = ref(0)

// 搜索和筛选
const searchKeyword = ref('')
const showFilterOptions = ref(false)
const filterStatus = ref('')
const filterRole = ref('')

// 构建查询参数（复用）
const buildParams = (page) => {
  const params = { pageNum: page, pageSize: pageSize.value, keyword: searchKeyword.value || '' }
  if (filterStatus.value) params.status = filterStatus.value
  if (filterRole.value) params.type = filterRole.value
  return params
}

// 计算 scroll-view 高度（header 高度约 120rpx ≈ 根据屏幕动态算）
const calcScrollViewHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  // header 实际占用大约 90px，留出顶部安全区
  const headerHeight = 90 + (systemInfo.statusBarHeight || 0)
  scrollViewHeight.value = systemInfo.windowHeight - headerHeight
}

// 获取用户总数（独立调用，不阻塞列表请求）
const getUserCount = async () => {
  try {
    const params = buildParams(1)
    delete params.pageNum
    
    const res = await request('/admin/user/count', {
      method: 'GET',
      data: params
    })
    
    if (res.code === 200) {
      totalUsers.value = res.data || 0
    }
  } catch (error) {
    console.error('获取用户总数失败:', error)
  }
}

// 获取用户列表（核心加载函数）
const fetchUserList = async (page, append = false) => {
  try {
    const params = buildParams(page)
    
    const res = await request('/admin/user/page', {
      method: 'GET',
      data: params
    })
    
    if (res.code === 200) {
      let records = []
      
      if (Array.isArray(res.data)) {
        records = res.data
      } else if (res.data && res.data.records) {
        records = res.data.records
      } else {
        records = res.data ? [res.data] : []
      }
      
      if (append) {
        // 追加模式：将新数据追加到已有列表
        userList.value = [...userList.value, ...records]
      } else {
        // 替换模式：替换整个列表
        userList.value = records
      }
      
      // 判断是否还有更多数据
      hasMore.value = records.length >= pageSize.value
      currentPage.value = page
      
      return records
    } else {
      uni.showToast({
        title: res.msg || '获取用户列表失败',
        icon: 'none'
      })
      return []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    })
    return []
  }
}

// 初始加载 / 刷新（重置列表，并行请求 count 和 list）
const loadInitial = async () => {
  if (isLoading.value) return
  isLoading.value = true
  hasMore.value = true
  
  try {
    // 并行请求 count 和第一页数据
    await Promise.all([
      getUserCount(),
      fetchUserList(1, false)
    ])
  } finally {
    isLoading.value = false
    if (isRefreshing.value) {
      isRefreshing.value = false
    }
  }
}

// 滚动到底部加载更多
const loadMore = async () => {
  if (isLoadingMore.value || isLoading.value || !hasMore.value) return
  isLoadingMore.value = true
  
  try {
    const nextPage = currentPage.value + 1
    await fetchUserList(nextPage, true)
  } finally {
    isLoadingMore.value = false
  }
}

// 下拉刷新
const refreshUserList = () => {
  isRefreshing.value = true
  loadInitial()
}

// 搜索用户
const handleSearch = () => {
  loadInitial()
}

// 重置筛选条件
const resetFilters = () => {
  filterStatus.value = ''
  filterRole.value = ''
}

// 应用筛选条件
const applyFilters = () => {
  showFilterOptions.value = false
  loadInitial()
}

// 编辑用户
const editUser = (user) => {
  uni.navigateTo({
    url: `/pages/admin/userEdit?id=${user.id}`
  })
}

// 切换用户状态
const toggleUserStatus = (user) => {
  uni.showModal({
    title: user.status === 1 ? '禁用用户' : '启用用户',
    content: user.status === 1 
      ? `确定要禁用用户"${user.userName}"吗？` 
      : `确定要启用用户"${user.userName}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const newStatus = user.status === 1 ? 0 : 1
          const res = await request('/admin/user/updateStatus', {
            method: 'POST',
            data: {
              userId: user.id,
              status: newStatus
            }
          })
          
          if (res.code === 200) {
            uni.showToast({
              title: '操作成功',
              icon: 'success'
            })
            
            // 更新本地状态
            const index = userList.value.findIndex(u => u.id === user.id)
            if (index !== -1) {
              userList.value[index].status = newStatus
            }
          } else {
            uni.showToast({
              title: res.msg || '操作失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('操作失败:', error)
          uni.showToast({
            title: '网络异常，请稍后重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 查看用户详情
const showUserDetail = (user) => {
  uni.navigateTo({
    url: `/pages/user/personal-center?userId=${user.id}`
  })
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 解锁头像框
const unlockFrame = (user) => {
  uni.navigateTo({
    url: `/pages/admin/frameLock?userId=${user.id}`
  })
}

onMounted(() => {
  calcScrollViewHeight()
  loadInitial()
})
</script>

<style lang="scss" scoped>
.user-list-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--bgColor1);
  color: var(--textColor1);
}

/* 顶部标题栏 */
.header-section {
  padding: 30rpx;
  background-color: var(--bgColor2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 10rpx var(--bgboxShadowColor1);
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 34rpx;
  font-weight: bold;
  color: var(--textColor1);
  margin-bottom: 8rpx;
}

.user-count {
  font-size: 24rpx;
  color: var(--textColor3);
}

.header-right {
  display: flex;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: var(--bgColor1);
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
  color: var(--textColor1);
}

.filter-btn {
  width: 70rpx;
  height: 70rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bgColor1);
  border-radius: 50%;
}

.filter-icon {
  width: 40rpx;
  height: 40rpx;
}

/* 用户列表 */
.user-scroll-view {
  flex: 1;
  padding: 20rpx;
}

.user-item {
  display: flex;
  background-color: var(--bgColor2);
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx var(--bgboxShadowColor1);
}

.user-avatar-section {
  position: relative;
  margin-right: 20rpx;
  width: 100rpx;
  height: 100rpx;
}

.user-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: var(--bgColor1);
  z-index: 1;
}

.avatar-frame {
  position: absolute;
  top: -10rpx;
  left: -10rpx;
  width: 120rpx;
  height: 120rpx;
  z-index: 2;
  pointer-events: none;
}

.vip-badge {
  position: absolute;
  bottom: -5rpx;
  right: -5rpx;
  background-color: var(--vipColor);
  color: var(--tagTextColor);
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 20rpx;
  font-weight: bold;
  z-index: 3;
}

.user-info-section {
  flex: 1;
  margin-right: 20rpx;
}

.user-name-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.user-name {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--textColor1);
  margin-right: 10rpx;
}

.admin-tag, .user-tag {
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 10rpx;
  margin-left: 10rpx;
}

.admin-tag {
  background-color: var(--adminColor);
  color: var(--tagTextColor);
}

.user-tag {
  background-color: var(--userColor);
  color: var(--tagTextColor);
}

/* 用户等级样式，使用全局样式 */
.user-level {
  margin-left: 10rpx;
}

.user-details {
  display: flex;
  font-size: 24rpx;
  color: var(--textColor3);
  margin-bottom: 10rpx;
}

.user-id {
  margin-right: 20rpx;
}

.user-stats {
  display: flex;
  flex-direction: column; /* Added from instruction */
  font-size: 22rpx;
  color: var(--textColor3); /* Replaced #999 */
}

.reg-time {
  margin-right: 20rpx;
}

.user-action-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-left: auto;
  padding-left: 20rpx;
}

.action-btn {
  width: 40rpx;
  height: 40rpx;
}

.frame-btn {
  color: #1890ff;
}

.status-indicator {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
}

.status-active {
  background-color: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.status-inactive {
  background-color: rgba(255, 77, 79, 0.1);
  color: #ff4d4f;
}

.action-buttons {
  display: flex;
}

.edit-btn, .block-btn, .unblock-btn {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  margin-left: 10rpx;
  background-color: #fff;
}

.edit-btn {
  color: #1890ff;
  border: 1px solid #1890ff;
}

.block-btn {
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
}

.unblock-btn {
  color: #52c41a;
  border: 1px solid #52c41a;
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
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: var(--textColor3);
}

/* 加载状态 */
.load-status {
  padding: 20rpx 0 40rpx;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx 0;
}

.loading-spinner {
  width: 36rpx;
  height: 36rpx;
  border: 3rpx solid #e0e0e0;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 16rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 24rpx;
  color: var(--textColor3);
}

.all-loaded {
  text-align: center;
  padding: 30rpx 0 40rpx;
}

.all-loaded-text {
  font-size: 24rpx;
  color: var(--textColor3);
}

/* 筛选弹窗 */
.filter-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.filter-content {
  width: 600rpx;
  background-color: var(--bgColor2);
  border-radius: 20rpx;
  overflow: hidden;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid var(--borderColor);
}

.filter-title {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--textColor1);
}

.close-icon {
  font-size: 40rpx;
  color: var(--textColor3);
}

.filter-options {
  padding: 20rpx 30rpx;
}

.filter-option {
  margin-bottom: 30rpx;
}

.option-label {
  font-size: 28rpx;
  color: var(--textColor1);
  margin-bottom: 20rpx;
  display: block;
}

.option-values {
  display: flex;
  flex-wrap: wrap;
}

.option-value {
  padding: 12rpx 30rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: var(--textColor3);
  background-color: var(--bgColor1);
  margin-right: 20rpx;
  margin-bottom: 20rpx;
}

.option-value.selected {
  background-color: var(--dColor2);
  color: var(--themeColor1);
  border: 1px solid var(--themeColor1);
}

.filter-buttons {
  display: flex;
  padding: 20rpx 30rpx;
  border-top: 1px solid var(--borderColor);
}

.reset-btn, .apply-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 28rpx;
  border-radius: 40rpx;
}

.reset-btn {
  background-color: var(--bgColor1);
  color: var(--textColor3);
  margin-right: 20rpx;
}

.apply-btn {
  background-color: var(--themeColor1);
  color: var(--themeTextColor);
}
</style>