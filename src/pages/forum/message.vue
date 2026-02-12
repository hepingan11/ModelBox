<template> 
	<view class="message-page" :class="themeClass">
		<!-- 顶部标签切换 -->
		<view class="tabs">
			<view 
				:class="['tab-item', { 'active': activeTab === 'likes' }]"
				@click="switchTab('likes')"
			>
				<text class="tab-text">点赞</text>
				<view v-if="activeTab === 'likes'" class="tab-line"></view>
			</view>
			<view 
				:class="['tab-item', { 'active': activeTab === 'comments' }]"
				@click="switchTab('comments')"
			>
				<text class="tab-text">评论</text>
				<view v-if="activeTab === 'comments'" class="tab-line"></view>
			</view>
		</view>

		<!-- 消息列表 -->
		<scroll-view 
			scroll-y 
			class="message-scroll"
			@scrolltolower="loadMore"
			refresher-enabled
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			:lower-threshold="100"
		>
			<!-- 加载状态 -->
			<view v-if="loading && messageList.length === 0" class="loading-container">
				<view class="loading-spinner"></view>
				<text class="loading-text">加载中...</text>
			</view>

			<!-- 消息列表 -->
			<view v-else-if="messageList.length > 0" class="message-list">
				<view 
					class="message-item" 
					v-for="message in messageList" 
					:key="message.forumId + '-' + message.userId"
					@click="goToForumDetail(message.forumId)"
				>
					<!-- 左侧用户头像 -->
					<image 
						:src="message.avatar || '/static/default-avatar.png'" 
						class="user-avatar"
						@click.stop="goToUserSpace(message.userId)"
					></image>

					<!-- 右侧内容 -->
					<view class="message-content">
						<!-- 用户名和时间 -->
						<view class="message-header">
							<text class="userName">{{ message.userName }}</text>
							<text class="time">{{ formatTime(message.createdTime) }}</text>
						</view>

						<!-- 点赞消息 -->
						<view v-if="activeTab === 'likes'" class="message-text">
							<text class="action-text">赞了你的帖子</text>
						</view>

						<!-- 评论消息 -->
						<view v-else class="message-text">
							<text class="comment-content">{{ message.content }}</text>
						</view>

						<!-- 帖子预览 -->
						<view class="forum-preview">
							<image 
								v-if="message.forumImg" 
								:src="message.forumImg" 
								class="forum-img"
								mode="aspectFill"
							></image>
							<text class="forum-title">{{ message.title }}</text>
						</view>
					</view>
				</view>

				<!-- 加载更多 -->
				<view v-if="hasMore" class="load-more">
					<view v-if="loadingMore" class="loading-more-spinner"></view>
					<text class="load-more-text">{{ loadingMore ? '加载中...' : '上拉加载更多' }}</text>
				</view>

				<!-- 没有更多 -->
				<view v-else class="no-more">
					<text class="no-more-text">没有更多了</text>
				</view>
			</view>

			<!-- 空状态 -->
			<view v-else class="empty-state">
				<image src="/static/empty.png" class="empty-icon"></image>
				<text class="empty-text">{{ activeTab === 'likes' ? '暂无点赞消息' : '暂无评论消息' }}</text>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useTheme } from '@/hooks/useTheme'

const { themeClass } = useTheme()

// 当前激活的标签
const activeTab = ref('likes')

// 消息列表
const messageList = ref([])

// 分页参数
const pageNum = ref(1)
const hasMore = ref(true)

// 加载状态
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)

// 切换标签
const switchTab = (tab) => {
	if (activeTab.value === tab) return
	activeTab.value = tab
	pageNum.value = 1
	hasMore.value = true
	messageList.value = []
	getMessageList()
}

// 获取消息列表
const getMessageList = async () => {
	if (loading.value || loadingMore.value) return

	if (pageNum.value === 1) {
		loading.value = true
	} else {
		loadingMore.value = true
	}

	try {
		const endpoint = activeTab.value === 'likes' 
			? '/forum/message/likes' 
			: '/forum/message/comments'

		const res = await request(endpoint, {
			method: 'GET',
			data: {
				pageNum: pageNum.value
			}
		})

		if (res.code === 200) {
			const newList = res.data.records || []
			
			if (pageNum.value === 1) {
				messageList.value = newList
			} else {
				messageList.value = [...messageList.value, ...newList]
			}

			// 判断是否还有更多数据
			hasMore.value = messageList.value.length < res.data.total
		} else {
			uni.showToast({
				title: res.message || '获取消息失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取消息失败:', error)
		uni.showToast({
			title: '获取消息失败',
			icon: 'none'
		})
	} finally {
		loading.value = false
		loadingMore.value = false
		refreshing.value = false
	}
}

// 下拉刷新
const onRefresh = () => {
	refreshing.value = true
	pageNum.value = 1
	hasMore.value = true
	getMessageList()
}

// 加载更多
const loadMore = () => {
	if (!hasMore.value || loadingMore.value || loading.value) return
	pageNum.value++
	getMessageList()
}

// 跳转到帖子详情
const goToForumDetail = (forumId) => {
	uni.navigateTo({
		url: `/pages/forum/detail?id=${forumId}`
	})
}

// 跳转到用户空间
const goToUserSpace = (userId) => {
	uni.navigateTo({
		url: `/pages/user/personal-center?userId=${userId}`
	})
}

// 格式化时间
const formatTime = (timeStr) => {
	if (!timeStr) return ''
	
	const time = new Date(timeStr)
	const now = new Date()
	const diff = now - time
	
	const minute = 60 * 1000
	const hour = 60 * minute
	const day = 24 * hour
	
	if (diff < minute) {
		return '刚刚'
	} else if (diff < hour) {
		return Math.floor(diff / minute) + '分钟前'
	} else if (diff < day) {
		return Math.floor(diff / hour) + '小时前'
	} else if (diff < 7 * day) {
		return Math.floor(diff / day) + '天前'
	} else {
		const year = time.getFullYear()
		const month = String(time.getMonth() + 1).padStart(2, '0')
		const date = String(time.getDate()).padStart(2, '0')
		return `${year}-${month}-${date}`
	}
}

onMounted(() => {
	getMessageList()
})
</script>

<style lang="scss" scoped>
.message-page {
	min-height: 100vh;
	background-color: var(--bgColor1);
	display: flex;
	flex-direction: column;
}

/* 标签切换 */
.tabs {
	display: flex;
	background-color: #fff;
	border-bottom: 1rpx solid var(--borderColor);
	position: sticky;
	top: 0;
	z-index: 10;
}

.darkMode .tabs {
	background-color: var(--bgColor2);
}

.tab-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 30rpx 0;
	position: relative;
}

.tab-text {
	font-size: 30rpx;
	color: var(--textColor3);
	transition: all 0.3s;
}

.tab-item.active .tab-text {
	color: var(--themeColor1);
	font-weight: bold;
	font-size: 32rpx;
}

.tab-line {
	position: absolute;
	bottom: 0;
	width: 60rpx;
	height: 4rpx;
	background-color: var(--themeColor1);
	border-radius: 2rpx;
}

/* 滚动容器 */
.message-scroll {
	flex: 1;
	height: calc(100vh - 100rpx);
}

/* 加载状态 */
.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #00A872;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text {
	margin-top: 20rpx;
	font-size: 28rpx;
	color: var(--textColor3);
}

/* 消息列表 */
.message-list {
	padding: 20rpx 0;
}

.message-item {
	display: flex;
	padding: 30rpx;
	background-color: #fff;
	margin-bottom: 20rpx;
	transition: background-color 0.3s;
}

.darkMode .message-item {
	background-color: var(--bgColor2);
}

.message-item:active {
	background-color: var(--bgColor1);
}

.user-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
	flex-shrink: 0;
}

.message-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.message-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.userName {
	font-size: 28rpx;
	color: var(--textColor1);
	font-weight: bold;
}

.time {
	font-size: 24rpx;
	color: var(--textColor3);
}

.message-text {
	font-size: 28rpx;
	line-height: 1.6;
}

.action-text {
	color: var(--textColor3);
}

.comment-content {
	color: var(--textColor1);
}

/* 帖子预览 */
.forum-preview {
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 20rpx;
	background-color: var(--bgColor1);
	border-radius: 12rpx;
}

.darkMode .forum-preview {
	background-color: var(--bgColor3);
}

.forum-img {
	width: 100rpx;
	height: 100rpx;
	border-radius: 8rpx;
	flex-shrink: 0;
}

.forum-title {
	flex: 1;
	font-size: 26rpx;
	color: var(--textColor3);
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	line-clamp: 2;
	-webkit-box-orient: vertical;
}

/* 加载更多 */
.load-more {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx 0;
	gap: 20rpx;
}

.loading-more-spinner {
	width: 40rpx;
	height: 40rpx;
	border: 3rpx solid #f3f3f3;
	border-top: 3rpx solid #00A872;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

.load-more-text {
	font-size: 26rpx;
	color: var(--textColor3);
}

/* 没有更多 */
.no-more {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx 0;
}

.no-more-text {
	font-size: 26rpx;
	color: var(--textColor3);
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 150rpx 0;
}

.empty-icon {
	width: 200rpx;
	height: 200rpx;
	margin-bottom: 30rpx;
}

.empty-text {
	font-size: 28rpx;
	color: var(--textColor3);
}
</style>