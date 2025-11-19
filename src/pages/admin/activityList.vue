<template>
  <view class="activity-container">
    <!-- 顶部标题栏 -->
    <view class="header-section">
      <view class="header-left">
        <text class="header-title">活动管理</text>
        <text class="activity-count">共 {{ totalActivities }} 个活动</text>
      </view>
      <view class="header-right">
        <view class="search-box">
          <image src="/static/icon/search.png" class="search-icon"></image>
          <input 
            type="text" 
            v-model="searchKeyword" 
            @confirm="handleSearch" 
            placeholder="搜索活动标题" 
            class="search-input"
          />
          <view class="clear-icon" @tap="clearSearch" v-if="searchKeyword">×</view>
        </view>
        <view class="add-btn" @tap="createActivity">
          <text class="add-icon">+</text>
        </view>
      </view>
    </view>
    
    <!-- 筛选选项栏 -->
    <view class="filter-tabs">
      <view 
        v-for="(filter, index) in filterOptions" 
        :key="index"
        class="filter-item" 
        :class="{ active: currentFilter === filter.value }"
        @tap="selectFilter(filter.value)"
      >
        {{ filter.label }}
      </view>
    </view>
    
    <!-- 活动列表 -->
    <scroll-view 
      scroll-y 
      class="activity-scroll-view" 
      @scrolltolower="loadMoreActivities"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="refreshActivityList"
    >

      <!-- 活动列表 -->
      <view v-if="activityList && activityList.length > 0" class="activity-full-list">
        <view 
          class="activity-item" 
          v-for="(item, index) in activityList" 
          :key="index"
          @tap="handleItemClick(item)"
        >
          <image :src="item.image || '/static/default-image.png'" class="activity-image" mode="aspectFill"></image>
          <view class="activity-content">
            <view class="activity-header">
              <text class="activity-title">{{item.title || '无标题'}}</text>
              <view class="status-tags">
                <view v-if="item.type && item.type.trim()" class="activity-tag">{{ item.type }}</view>
                <view class="status-tag" :class="item.status === '上线' ? 'status-online' : 'status-offline'">
                  {{ item.status }}
                </view>
              </view>
            </view>
            <text class="activity-desc">{{item.content || '暂无描述'}}</text>
            <view class="activity-footer">
              <text class="activity-time">{{formatTime(item.createdTime)}}</text>
              <view class="activity-actions">
                <button class="action-btn edit-btn" @tap.stop="editActivity(item)">编辑</button>
                <button class="action-btn delete-btn" @tap.stop="confirmDeleteActivity(item)">删除</button>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-else-if="!isLoading" class="empty-state">
        <image src="/static/empty.png" class="empty-icon"></image>
        <text class="empty-text">暂无{{ currentFilter }}数据</text>
        <view class="empty-btn" @tap="createActivity">添加活动</view>
      </view>
      
      <!-- 底部加载状态 -->
      <view class="loading-more" v-if="isLoading && !isRefreshing">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 已加载全部 -->
      <view class="list-end" v-if="!hasMore && activityList.length > 0">
        <text class="end-text">- 没有更多活动了 -</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request.js'

// 筛选选项
const filterOptions = [
  { label: '全部', value: '' },
  { label: '活动', value: '活动' },
  { label: '公告', value: '公告' },
  { label: '广告', value: '广告' }
]

// 活动列表数据
const activityList = ref([])
const totalActivities = ref(0)
const currentPage = ref(1)
const hasMore = ref(true)
const isLoading = ref(false)
const isRefreshing = ref(false)

// 搜索和筛选
const searchKeyword = ref('')
const currentFilter = ref('')

// 获取活动列表
const getActivityList = async (isRefresh = false) => {
  if (isLoading.value) return
  isLoading.value = true
  console.log('开始加载数据，参数：', {
    pageNum: currentPage.value,
    title: searchKeyword.value,
    type: currentFilter.value
  })
  
  try {
    const params = {
      pageNum: currentPage.value,
      title: searchKeyword.value,
      type: currentFilter.value
    }
    
    const res = await request('/admin/activity/page', {
      method: 'GET',
      data: params
    })
    
    console.log('API返回数据：', res)
    
    if (res.code === 200) {
      const records = Array.isArray(res.data) ? res.data : []
      const total = records.length
      
      console.log('解析后的数据：', {
        total,
        recordsLength: records.length,
        firstRecord: records[0]
      })
      
      // 确保在更新列表之前数据是数组
      if (isRefresh) {
        activityList.value = [...records]
      } else {
        activityList.value = [...activityList.value, ...records]
      }
      
      totalActivities.value = total
      hasMore.value = activityList.value.length < total
      
      // 打印最终的列表状态
      console.log('最终列表状态：', {
        listLength: activityList.value.length,
        hasMore: hasMore.value,
        total: totalActivities.value,
        firstItem: activityList.value
      })
    } else {
      uni.showToast({
        title: res.msg || '获取活动列表失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
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
const refreshActivityList = () => {
  isRefreshing.value = true
  currentPage.value = 1
  getActivityList(true)
}

// 加载更多
const loadMoreActivities = () => {
  if (!hasMore.value || isLoading.value) return
  getActivityList()
}

// 搜索活动
const handleSearch = () => {
  currentPage.value = 1
  getActivityList(true)
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  handleSearch()
}

// 选择筛选条件
const selectFilter = (filter) => {
  console.log('筛选被点击：', filter)
  if (currentFilter.value === filter) {
    console.log('当前已是该筛选项：', filter)
    return
  }
  
  currentFilter.value = filter
  console.log('设置新的筛选值：', currentFilter.value)
  currentPage.value = 1
  getActivityList(true)
}

// 创建新活动
const createActivity = () => {
  console.log('点击创建活动')
  uni.navigateTo({
    url: '/pages/admin/activityEdit?mode=create'
  })
}

// 编辑活动
const editActivity = (item) => {
  console.log('点击编辑活动：', item)
  uni.navigateTo({
    url: `/pages/admin/activityEdit?id=${item.id}&mode=edit`
  })
}

// 确认删除活动
const confirmDeleteActivity = (item) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除活动"${item.title}"吗？删除后无法恢复`,
    success: async (res) => {
      if (res.confirm) {
        await deleteActivity(item)
      }
    }
  })
}

// 删除活动
const deleteActivity = async (item) => {
  try {
    const result = await request('/admin/activity/delete', {
      method: 'POST',
      data: {
        id: item.id
      }
    })
    
    if (result.code === 200) {
      uni.showToast({
        title: '删除成功',
        icon: 'success'
      })
      
      // 从列表中移除
      const index = activityList.value.findIndex(i => i.id === item.id)
      if (index !== -1) {
        activityList.value.splice(index, 1)
        totalActivities.value--
      }
    } else {
      uni.showToast({
        title: result.msg || '删除失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('删除活动失败:', error)
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    })
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '未知'
  
  // 处理后端返回的LocalDateTime格式 (ISO-8601)
  let date
  if (typeof timestamp === 'string') {
    date = new Date(timestamp)
  } else {
    date = new Date(timestamp)
  }
  
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 处理列表项点击
const handleItemClick = (item) => {
  console.log('点击列表项：', item)
  // 这里可以添加查看详情等功能
}

// 确保组件挂载时加载数据
onMounted(() => {
  console.log('组件挂载，开始加载数据')
  getActivityList(true)
  
  // 监听活动编辑成功事件，刷新列表
  uni.$on('refreshActivityList', () => {
    console.log('收到刷新列表事件')
    getActivityList(true)
  })
})

// 页面卸载时移除事件监听
onUnmounted(() => {
  uni.$off('refreshActivityList')
})
</script>

<style lang="scss" scoped>
.activity-container {
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

.activity-count {
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

.clear-icon {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  color: #999;
}

.add-btn {
  width: 70rpx;
  height: 70rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #1abc9c;
  border-radius: 50%;
  box-shadow: 0 2rpx 10rpx rgba(26, 188, 156, 0.3);
}

.add-icon {
  font-size: 40rpx;
  color: #fff;
  font-weight: bold;
}

/* 筛选选项栏 */
.filter-tabs {
  display: flex;
  background-color: #fff;
  padding: 20rpx 30rpx;
  overflow-x: auto;
  white-space: nowrap;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.filter-item {
  padding: 12rpx 30rpx;
  margin-right: 20rpx;
  font-size: 28rpx;
  color: #666;
  background-color: #f5f7fa;
  border-radius: 30rpx;
  transition: all 0.3s;
}

.filter-item.active {
  background-color: #1abc9c;
  color: #fff;
  font-weight: 500;
}

/* 活动列表 */
.activity-scroll-view {
  flex: 1;
  padding: 20rpx;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.activity-full-list {
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.activity-item {
  background-color: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  overflow: hidden;
  width: 100%;
  max-width: 100%;
}

.activity-image {
  width: 100%;
  height: 320rpx;
  object-fit: cover;
}

.activity-content {
  padding: 20rpx;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.activity-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tags {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.activity-tag {
  background-color: #1abc9c;
  color: white;
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 6rpx rgba(26, 188, 156, 0.3);
  white-space: nowrap;
  flex-shrink: 0;
}

.status-tag {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-weight: 500;
}

.status-online {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.status-offline {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffa39e;
}

.activity-desc {
  font-size: 28rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  margin-bottom: 15rpx;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15rpx;
  border-top: 1rpx solid #f0f0f0;
  margin-top: 15rpx;
}

.activity-time {
  font-size: 24rpx;
  color: #999;
}

.activity-actions {
  display: flex;
}

.action-btn {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  margin-left: 15rpx;
  background-color: #fff;
}

.edit-btn {
  color: #1890ff;
  border: 1px solid #1890ff;
}

.delete-btn {
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
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
  margin-bottom: 30rpx;
}

.empty-btn {
  padding: 16rpx 40rpx;
  background-color: #1abc9c;
  color: #fff;
  font-size: 28rpx;
  border-radius: 40rpx;
}

/* 加载状态 */
.loading-more, .list-end {
  text-align: center;
  padding: 20rpx 0;
}

.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10rpx;
  display: inline-block;
  vertical-align: middle;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text, .end-text {
  font-size: 24rpx;
  color: #999;
  display: inline-block;
  vertical-align: middle;
}
</style>