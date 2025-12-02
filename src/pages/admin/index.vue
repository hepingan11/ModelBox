<template>
  <view class="admin-container">
    <!-- 顶部欢迎信息 -->
    <view class="welcome-section">
      <view class="welcome-header">
        <text class="welcome-title">管理员控制台</text>
        <text class="welcome-subtitle">欢迎回来，{{ adminInfo.username }}</text>
      </view>
      <view class="stats-row">
        <view class="stat-card" v-for="(stat, index) in statistics" :key="index">
          <text class="stat-value">{{ stat.value }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
      </view>
    </view>

    <!-- 管理模块网格 -->
    <view class="management-grid">
      <!-- 用户管理 -->
      <view class="grid-card" @click="navigateToSection('user')">
        <image src="/static/admin/user.png" class="card-icon"></image>
        <view class="card-content">
          <text class="card-title">用户管理</text>
          <text class="card-desc">管理用户账号、权限和状态</text>
        </view>
      </view>

      <!-- 项目管理 -->
      <view class="grid-card" @click="navigateToSection('project')">
        <image src="/static/icon/project.png" class="card-icon"></image>
        <view class="card-content">
          <text class="card-title">项目管理</text>
          <text class="card-desc">管理项目信息</text>
        </view>
      </view>

      <!-- 帖子管理 -->
      <view class="grid-card" @click="navigateToSection('post')">
        <image src="/static/admin/post.png" class="card-icon"></image>
        <view class="card-content">
          <text class="card-title">帖子管理</text>
          <text class="card-desc">管理论坛帖子和评论</text>
        </view>
      </view>

      <!-- 活动广告管理 -->
      <view class="grid-card" @click="navigateToSection('activity')">
        <image src="/static/admin/activity.png" class="card-icon"></image>
        <view class="card-content">
          <text class="card-title">活动广告</text>
          <text class="card-desc">管理活动和广告投放</text>
        </view>
      </view>

      <!-- 会话管理 -->
      <view class="grid-card" @click="navigateToSection('chat')">
        <image src="/static/admin/message.png" class="card-icon"></image>
        <view class="card-content">
          <text class="card-title">会话管理</text>
          <text class="card-desc">查看和管理用户会话</text>
        </view>
      </view>

      <!-- 轮播图管理 -->
      <view class="grid-card" @click="navigateToSection('carousel')">
        <image src="/static/icon/image.png" class="card-icon"></image>
        <view class="card-content">
          <text class="card-title">轮播图</text>
          <text class="card-desc">管理首页轮播图展示</text>
        </view>
      </view>


      <view class="grid-card" @click="navigateToSection('report')">
        <image src="/static/icon/report.png" class="card-icon"></image>
        <view class="card-content">
          <text class="card-title">举报管理</text>
          <text class="card-desc">管理举报信息</text>
        </view>
      </view>

      <view class="grid-card" @click="navigateToSection('avatar')">
        <image src="/static/icon/frame.png" class="card-icon"></image>
        <view class="card-content">
          <text class="card-title">头像框管理</text>
          <text class="card-desc">管理头像框信息</text>
        </view>
      </view>

      <!-- 优惠券管理 -->
      <view class="grid-card" @click="navigateToSection('coupon')">
        <image src="/static/icon/coupon.png" class="card-icon"></image>
        <view class="card-content">
          <text class="card-title">优惠券管理</text>
          <text class="card-desc">管理平台优惠券</text>
        </view>
      </view>
  
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 管理员信息
const adminInfo = ref({
  username: '管理员',
  role: 'admin'
})

// 统计数据
const statistics = ref([
  { label: '总用户数', value: '0' },
  { label: '商品数量', value: '0' },
  { label: '论坛帖子', value: '0' },
  { label: '今日对话数', value: '0' }
])



// 获取管理员信息
const getAdminInfo = async () => {
  try {
    const res = await request('/user/getUserInfo', {
      method: 'GET'
    })
    if (res.code === 200) {
      adminInfo.value = res.data
    }
  } catch (error) {
    console.error('获取管理员信息失败:', error)
  }
}

// 获取统计数据
const getStatistics = async () => {
  try {
    const res = await request('/admin/statistics', {
      method: 'GET'
    })
    if (res.code === 200) {      
      // 后端直接返回包含各项统计数的对象
      if (res.data) {
        // 更新统计数据数组
        statistics.value = [
          { label: '总用户数', value: res.data.userCount || '0' },
          { label: '商品数量', value: res.data.shopCount || '0' },
          { label: '论坛帖子', value: res.data.forumCount || '0' },
          { label: '今日对话数', value: res.data.chatCount || '0' }
        ]
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}


// 导航到各个管理页面
const navigateToSection = (section) => {
  const routes = {
    user: '/pages/admin/userList',
    post: '/pages/admin/forumList',
    activity: '/pages/admin/activityList',
    chat: '/pages/admin/messageList',
    carousel: '/pages/admin/carouselList',
    report: '/pages/admin/reportList',
    avatar: '/pages/admin/avatarList',
    project: '/pages/admin/project/projectList',
    coupon: '/pages/admin/coupon/coupon',
  }
  
  if (routes[section]) {
    uni.navigateTo({
      url: routes[section]
    })
  }
}


onMounted(() => {
  getAdminInfo()
  getStatistics()
  getPendingCounts()
})
</script>

<style>
.admin-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 30rpx;
}

/* 欢迎区域样式 */
.welcome-section {
  background: linear-gradient(135deg, #1abc9c, #75ff52);
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  color: #fff;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(58, 80, 192, 0.2);
}

.welcome-header {
  margin-bottom: 30rpx;
}

.welcome-title {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
  display: block;
}

.welcome-subtitle {
  font-size: 28rpx;
  opacity: 0.9;
}

.stats-row {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  margin: 0 -10rpx;
}

.stat-card {
  flex: 1;
  min-width: 150rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12rpx;
  padding: 20rpx;
  margin: 10rpx;
  backdrop-filter: blur(5px);
}

.stat-value {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
  display: block;
}

.stat-label {
  font-size: 24rpx;
  opacity: 0.8;
}

/* 管理模块网格样式 */
.management-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.grid-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.grid-card:active {
  transform: scale(0.98);
}

.card-icon {
  margin-right: 20rpx;
  width: 55rpx;
  height: 55rpx;
  border-radius: 1rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 6rpx;
  display: block;
}

.card-desc {
  font-size: 24rpx;
  color: #999;
}

/* 快捷操作区域样式 */
.quick-actions {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.action-list {
  display: flex;
  justify-content: space-between;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  position: relative;
}

.action-icon {
  font-size: 36rpx;
  margin-bottom: 10rpx;
}

.action-text {
  font-size: 26rpx;
  color: #666;
}

.action-count {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  background-color: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  min-width: 28rpx;
  text-align: center;
}
</style>
