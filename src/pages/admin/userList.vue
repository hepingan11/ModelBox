<template>
  <view class="user-list-container">
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
    
    <!-- 用户列表 -->
    <scroll-view 
      scroll-y 
      class="user-scroll-view" 
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="refreshUserList"
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
            <image :src="user.avatar || '/static/avatar/default.png'" mode="aspectFill" class="user-avatar"></image>
            <!-- 头像框 -->
            <image 
              v-if="user.avatarFrame" 
              :src="user.avatarFrame" 
              class="avatar-frame" 
              mode="aspectFit"
            ></image>
            <view v-if="user.isVip" class="vip-badge">VIP</view>
          </view>
          
          <view class="user-info-section">
            <view class="user-name-row">
              <text class="user-name">{{ user.username }}</text>
              <text v-if="user.role === 'admin'" class="admin-tag">管理员</text>
              <text v-if="user.role === 'user'" class="user-tag">普通用户</text>
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
      <view class="loading-more" v-if="isLoading">
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 分页控件 -->
      <view class="pagination-container" v-if="userList.length > 0">
        <view class="pagination">
          <view 
            class="page-btn prev-btn" 
            :class="{'disabled': currentPage <= 1}"
            @click="prevPage"
          >
            上一页
          </view>
          
          <view class="page-numbers">
            <view 
              v-for="page in displayedPages" 
              :key="page"
              class="page-number"
              :class="{'active': currentPage === page}"
              @click="goToPage(page)"
            >
              {{ page }}
            </view>
          </view>
          
          <view 
            class="page-btn next-btn" 
            :class="{'disabled': currentPage >= totalPages}"
            @click="nextPage"
          >
            下一页
          </view>
        </view>
        
        <view class="page-info">
          {{ currentPage }} / {{ totalPages }} 页，共 {{ totalUsers }} 条
        </view>
      </view>
    </scroll-view>
    
    <!-- 筛选弹窗 -->
    <view class="filter-popup" v-if="showFilterOptions">
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
                :class="{'selected': filterRole === 'admin'}"
                @click="filterRole = 'admin'"
              >管理员</view>
              <view 
                class="option-value" 
                :class="{'selected': filterRole === 'user'}"
                @click="filterRole = 'user'"
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
const isLoading = ref(false)
const isRefreshing = ref(false)

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(totalUsers.value / pageSize.value) || 1
})

// 计算显示哪些页码
const displayedPages = computed(() => {
  const pages = []
  const maxDisplayed = 5 // 最多显示5个页码
  
  if (totalPages.value <= maxDisplayed) {
    // 如果总页数小于等于最大显示数，则全部显示
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push(i)
    }
  } else {
    // 否则显示当前页附近的页码
    let start = Math.max(currentPage.value - 2, 1)
    let end = Math.min(start + maxDisplayed - 1, totalPages.value)
    
    // 如果end到达了最大值，重新计算start
    if (end === totalPages.value) {
      start = Math.max(end - maxDisplayed + 1, 1)
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
  }
  
  return pages
})

// 搜索和筛选
const searchKeyword = ref('')
const showFilterOptions = ref(false)
const filterStatus = ref('')
const filterRole = ref('')
const tempFilters = ref({
  status: '',
  role: ''
})

// 获取用户总数
const getUserCount = async () => {
  try {
    const params = {}
    
    // 添加筛选条件到查询参数
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterRole.value) params.role = filterRole.value
    
    const res = await request('/admin/user/count', {
      method: 'GET',
      data: params
    })
    
    if (res.code === 200) {
      totalUsers.value = res.data || 0
    } else {
      console.error('获取用户总数失败:', res.msg)
    }
  } catch (error) {
    console.error('获取用户总数失败:', error)
  }
}

// 获取用户列表
const getUserList = async (isRefresh = false) => {
  if (isLoading.value) return
  isLoading.value = true
  
  try {
    // 如果是刷新，重置页码
    if (isRefresh) {
      currentPage.value = 1
    }
    
    // 先获取总用户数
    await getUserCount()
    
    const params = {
      pageNum: currentPage.value,
      keyword: searchKeyword.value
    }
    
    // 添加筛选条件到查询参数
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterRole.value) params.role = filterRole.value
    
    const res = await request('/admin/user/page', {
      method: 'GET',
      data: params
    })
    
    if (res.code === 200) {
      // 处理直接返回List<User>的情况
      let records = [];
      
      if (Array.isArray(res.data)) {
        // 如果直接返回了数组
        records = res.data;
      } else if (res.data && res.data.records) {
        // 如果返回了标准分页对象
        records = res.data.records;
      } else {
        // 其他情况，可能直接就是数据
        records = res.data ? [res.data] : [];
      }
      
      console.log('获取到的用户数据:', records);
      
      userList.value = records;
      
      // 如果当前页大于总页数，跳转到最后一页
      if (currentPage.value > totalPages.value && totalPages.value > 0) {
        currentPage.value = totalPages.value;
        getUserList();
      }
    } else {
      uni.showToast({
        title: res.msg || '获取用户列表失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
    if (isRefreshing.value) {
      isRefreshing.value = false
    }
  }
}

// 下拉刷新
const refreshUserList = () => {
  isRefreshing.value = true
  getUserList(true)
}

// 前往上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    getUserList()
  }
}

// 前往下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    getUserList()
  }
}

// 跳转到指定页
const goToPage = (page) => {
  if (page !== currentPage.value) {
    currentPage.value = page
    getUserList()
  }
}

// 搜索用户
const handleSearch = () => {
  getUserList(true)
}

// 重置筛选条件
const resetFilters = () => {
  filterStatus.value = ''
  filterRole.value = ''
}

// 应用筛选条件
const applyFilters = () => {
  showFilterOptions.value = false
  getUserList(true)
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
      ? `确定要禁用用户"${user.username}"吗？` 
      : `确定要启用用户"${user.username}"吗？`,
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
  getUserCount()
  getUserList(true)
})
</script>

<style lang="scss" scoped>
.user-list-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
}

/* 顶部标题栏 */
.header-section {
  padding: 30rpx;
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.user-count {
  font-size: 24rpx;
  color: #999;
}

.header-right {
  display: flex;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: #f0f2f5;
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
}

.filter-btn {
  width: 70rpx;
  height: 70rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
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
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
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
  background-color: #f0f2f5;
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
  background-color: #ff9800;
  color: white;
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
  color: #333;
  margin-right: 10rpx;
}

.admin-tag, .user-tag {
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 10rpx;
  margin-left: 10rpx;
}

.admin-tag {
  background-color: #ff4d4f;
  color: white;
}

.user-tag {
  background-color: #52c41a;
  color: white;
}

/* 用户等级样式，使用全局样式 */
.user-level {
  margin-left: 10rpx;
}

.user-details {
  display: flex;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.user-id {
  margin-right: 20rpx;
}

.user-stats {
  display: flex;
  font-size: 22rpx;
  color: #999;
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
  color: #999;
}

/* 加载状态 */
.loading-more, .all-loaded {
  text-align: center;
  padding: 20rpx 0;
}

.loading-text, .all-loaded-text {
  font-size: 24rpx;
  color: #999;
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
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f0f0f0;
}

.filter-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.close-icon {
  font-size: 40rpx;
  color: #999;
}

.filter-options {
  padding: 20rpx 30rpx;
}

.filter-option {
  margin-bottom: 30rpx;
}

.option-label {
  font-size: 28rpx;
  color: #333;
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
  color: #666;
  background-color: #f5f5f5;
  margin-right: 20rpx;
  margin-bottom: 20rpx;
}

.option-value.selected {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.filter-buttons {
  display: flex;
  padding: 20rpx 30rpx;
  border-top: 1px solid #f0f0f0;
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
  background-color: #f5f5f5;
  color: #666;
  margin-right: 20rpx;
}

.apply-btn {
  background-color: #1890ff;
  color: #fff;
}

/* 分页控件 */
.pagination-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  margin-top: 20rpx;
}

.pagination {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.page-btn {
  padding: 10rpx 20rpx;
  border: 1px solid #1890ff;
  border-radius: 8rpx;
  color: #1890ff;
  font-size: 24rpx;
  margin: 0 10rpx;
}

.page-btn.disabled {
  border-color: #d9d9d9;
  color: #d9d9d9;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  align-items: center;
}

.page-number {
  min-width: 60rpx;
  height: 60rpx;
  line-height: 60rpx;
  text-align: center;
  border: 1px solid #d9d9d9;
  border-radius: 8rpx;
  margin: 0 10rpx;
  font-size: 24rpx;
  color: #666;
}

.page-number.active {
  background-color: #1890ff;
  color: #fff;
  border-color: #1890ff;
}

.page-info {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}
</style>