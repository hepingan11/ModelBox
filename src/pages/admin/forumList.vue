<template>
	<view class="forum-list-container">
		<!-- 顶部搜索和筛选 -->
		<view class="search-filter">
			<view class="search-box">
				<image src="/static/icon/search.png" class="search-icon"></image>
				<input type="text" v-model="searchTitle" placeholder="搜索帖子标题" class="search-input" @input="handleSearch" />
				<text v-if="searchTitle" class="clear-icon" @click="clearSearch">✕</text>
			</view>
			<view class="filter-box">
				<picker mode="selector" :range="statusOptions" @change="handleStatusChange">
					<view class="filter-item">
						<text>状态: {{ statusOptions[currentStatus] }}</text>
						<image src="/static/icon/arrow-down.png" class="arrow-icon"></image>
					</view>
				</picker>
			</view>
		</view>

		<!-- 帖子列表 -->
		<scroll-view 
			scroll-y 
			class="forum-list" 
			@scrolltolower="loadMoreData"
			refresher-enabled
			:refresher-triggered="isRefreshing"
			@refresherrefresh="refreshForumList"
		>
			<!-- 帖子列表项 -->
			<view v-if="forumList.length > 0">
				<view 
					class="forum-item"
					v-for="(item, index) in forumList" 
					:key="item.id"
				>
					<view class="forum-content" @click="goToForumDetail(item.id)">
						<!-- 帖子封面图 -->
						<image 
							v-if="item.imageUrl && item.imageUrl.length > 0" 
							:src="item.imageUrl[0]" 
							class="forum-cover" 
							mode="aspectFill"
						></image>
						
						<!-- 帖子信息 -->
						<view class="forum-info">
							<view class="forum-title">{{ item.title }}</view>
							<view class="forum-summary">{{ item.content }}</view>
							
							<!-- 帖子统计 -->
							<view class="forum-stats">
								<view class="stat-item">
									<image src="/static/icon/look.png" class="stat-icon"></image>
									<text>{{ item.look || 0 }}</text>
								</view>
								<view class="stat-item">
									<image src="/static/icon/liked.png" class="stat-icon"></image>
									<text>{{ item.likes || 0 }}</text>
								</view>
								<text class="forum-time">{{ formatTime(item.createdTime) }}</text>
							</view>
						</view>
					</view>

					<!-- 作者信息 -->
					<view class="author-info">
						<image :src="item.avatar || defaultAvatar" class="author-avatar" mode="aspectFill"></image>
						<text class="author-name">{{ item.username || '用户' + item.userId }}</text>
						<text class="forum-status" :class="getStatusClass(item.status)">
							{{ getStatusText(item.status) }}
						</text>
					</view>

					<!-- 管理操作 -->
					<view class="action-buttons">
						<button class="action-btn btn-status" @click="toggleStatus(item)">
							{{ item.status === 0 ? '禁用' : '启用' }}
						</button>
						<button class="action-btn btn-delete" @click="deleteForum(item)">删除</button>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view v-else-if="!isLoading" class="empty-state">
				<image src="/static/icon/discussion.png" class="empty-icon"></image>
				<text class="empty-text">{{ searchTitle ? '未找到相关帖子' : '暂无帖子数据' }}</text>
			</view>
			
			<!-- 底部加载状态 -->
			<view class="loading-more" v-if="isLoading && !isRefreshing">
				<view class="loading-spinner"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 已加载全部 -->
			<view class="list-end" v-if="!hasMore && forumList.length > 0">
				<text class="end-text">- 没有更多帖子了 -</text>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 状态选项
const statusOptions = ['全部', '正常', '已禁用']
const currentStatus = ref(0)

// 列表数据
const forumList = ref([])
const searchTitle = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)
const isLoading = ref(false)
const isRefreshing = ref(false)
const defaultAvatar = '/static/user.png'

// 搜索防抖定时器
let searchTimer = null

// 获取论坛列表数据
const getForumList = async (isRefresh = false) => {
	if (isLoading.value) return
	
	isLoading.value = true
	if (isRefresh) {
		pageNum.value = 1
		hasMore.value = true
	}
	
	try {
		const params = {
			pageNum: pageNum.value,
			title: searchTitle.value || ''
		}
		
		const res = await request('/admin/forum/list', {
			method: 'GET',
			data: params
		})
		
		if (res.code === 200) {
			let list = res.data || []
			
			if (isRefresh) {
				forumList.value = list
			} else {
				forumList.value = [...forumList.value, ...list]
			}
			
			hasMore.value = list.length >= pageSize.value
			
			// 如果有数据，页码加1，为下次加载做准备
			if (list.length > 0 && !isRefresh) {
				pageNum.value++
			}
		} else {
			uni.showToast({
				title: res.msg || '获取帖子列表失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取帖子列表失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
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
const refreshForumList = () => {
	isRefreshing.value = true
	getForumList(true)
}

// 加载更多
const loadMoreData = () => {
	if (!hasMore.value || isLoading.value) return
	getForumList()
}

// 处理搜索输入
const handleSearch = () => {
	if (searchTimer) clearTimeout(searchTimer)
	
	searchTimer = setTimeout(() => {
		getForumList(true)
	}, 500)
}

// 清除搜索
const clearSearch = () => {
	searchTitle.value = ''
	getForumList(true)
}

// 处理状态筛选变化
const handleStatusChange = (e) => {
	currentStatus.value = parseInt(e.detail.value)
	getForumList(true)
}

// 删除帖子
const deleteForum = (item) => {
	uni.showModal({
		title: '确认删除',
		content: '确定要删除这个帖子吗？此操作不可恢复。',
		success: async (res) => {
			if (res.confirm) {
				try {
					const res = await request('/admin/forum/delete', {
						method: 'GET',
						data: { id: item.id }
					})
					
					if (res.code === 200) {
						uni.showToast({
							title: '删除成功',
							icon: 'success'
						})
						// 从列表中移除
						const index = forumList.value.findIndex(f => f.id === item.id)
						if (index > -1) {
							forumList.value.splice(index, 1)
						}
					} else {
						uni.showToast({
							title: res.msg || '删除失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('删除帖子失败:', error)
					uni.showToast({
						title: '网络错误，请稍后重试',
						icon: 'none'
					})
				}
			}
		}
	})
}

// 切换帖子状态
const toggleStatus = (item) => {
	const actionText = item.status === 0 ? '禁用' : '启用'
	uni.showModal({
		title: '确认操作',
		content: `确定要${actionText}这个帖子吗？`,
		success: async (res) => {
			if (res.confirm) {
				try {
					const res = await request('/admin/forum/status', {
						method: 'GET',
						data: { id: item.id }
					})
					
					if (res.code === 200) {
						uni.showToast({
							title: `${actionText}成功`,
							icon: 'success'
						})
						// 更新列表中的状态
						const index = forumList.value.findIndex(f => f.id === item.id)
						if (index > -1) {
							forumList.value[index].status = item.status === 0 ? 1 : 0
						}
					} else {
						uni.showToast({
							title: res.msg || `${actionText}失败`,
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('更新帖子状态失败:', error)
					uni.showToast({
						title: '网络错误，请稍后重试',
						icon: 'none'
					})
				}
			}
		}
	})
}

// 跳转到帖子详情
const goToForumDetail = (id) => {
	uni.navigateTo({
		url: `/pages/forum/detail?id=${id}`
	})
}

// 获取状态文本
const getStatusText = (status) => {
	return status === 0 ? '正常' : '已禁用'
}

// 获取状态样式类
const getStatusClass = (status) => {
	return status === 0 ? 'status-normal' : 'status-disabled'
}

// 格式化时间
const formatTime = (timestamp) => {
	if (!timestamp) return ''
	
	let date
	if (typeof timestamp === 'string') {
		date = new Date(timestamp)
	} else {
		date = new Date(timestamp)
	}
	
	return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 页面加载时获取论坛列表
onMounted(() => {
	getForumList(true)
})
</script>

<style lang="scss" scoped>
.forum-list-container {
	background-color: #f5f7fa;
	min-height: 100vh;
	padding: 20rpx;
}

/* 顶部搜索和筛选栏 */
.search-filter {
	background-color: #fff;
	padding: 20rpx 30rpx;
	position: sticky;
	top: 0;
	z-index: 10;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.search-box {
	flex: 1;
	display: flex;
	align-items: center;
	background-color: #f5f7fa;
	border-radius: 50rpx;
	padding: 12rpx 24rpx;
	margin-right: 20rpx;
}

.search-icon {
	width: 36rpx;
	height: 36rpx;
	margin-right: 10rpx;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.clear-icon {
	font-size: 28rpx;
	color: #999;
	padding: 0 10rpx;
}

.filter-box {
	min-width: 160rpx;
}

.filter-item {
	display: flex;
	align-items: center;
	font-size: 28rpx;
	color: #333;
}

.arrow-icon {
	width: 24rpx;
	height: 24rpx;
	margin-left: 8rpx;
}

/* 论坛列表 */
.forum-list {
	height: calc(100vh - 40rpx);
}

.forum-item {
	background-color: #fff;
	border-radius: 12rpx;
	margin-bottom: 20rpx;
	padding: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.forum-content {
	display: flex;
	margin-bottom: 20rpx;
}

.forum-cover {
	width: 200rpx;
	height: 150rpx;
	border-radius: 8rpx;
	margin-right: 20rpx;
}

.forum-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.forum-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 10rpx;
}

.forum-summary {
	font-size: 26rpx;
	color: #666;
	margin-bottom: 20rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
}

.forum-stats {
	display: flex;
	align-items: center;
}

.stat-item {
	display: flex;
	align-items: center;
	margin-right: 20rpx;
	font-size: 24rpx;
	color: #999;
}

.stat-icon {
	width: 28rpx;
	height: 28rpx;
	margin-right: 6rpx;
}

.forum-time {
	font-size: 24rpx;
	color: #999;
	margin-left: auto;
}

/* 作者信息 */
.author-info {
	display: flex;
	align-items: center;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f7fa;
}

.author-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	margin-right: 12rpx;
}

.author-name {
	font-size: 26rpx;
	color: #333;
	margin-right: auto;
}

.forum-status {
	font-size: 24rpx;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
}

.status-normal {
	background-color: #e8f5e9;
	color: #4caf50;
}

.status-disabled {
	background-color: #ffebee;
	color: #f44336;
}

/* 操作按钮 */
.action-buttons {
	display: flex;
	justify-content: flex-end;
	margin-top: 20rpx;
}

.action-btn {
	font-size: 26rpx;
	padding: 10rpx 20rpx;
	border-radius: 6rpx;
	margin-left: 20rpx;
	border: none;
}

.btn-status {
	background-color: #1abc9c;
	color: #fff;
}

.btn-delete {
	background-color: #e74c3c;
	color: #fff;
}

/* 加载状态 */
.loading-more {
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
	font-size: 30rpx;
	color: #333;
}

/* 列表底部 */
.list-end {
	text-align: center;
	padding: 20rpx 0;
	color: #999;
	font-size: 24rpx;
}
</style>