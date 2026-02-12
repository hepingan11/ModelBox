<template>
	<view class="mylike-container" :class="themeClass">
		
		
		<!-- 帖子列表内容 -->
		<scroll-view 
			class="post-list-scroll" 
			scroll-y 
			@scrolltolower="loadMoreData"
			refresher-enabled
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh"
		>
			<!-- 加载中状态 -->
			<view class="loading-wrapper" v-if="isFirstLoading">
				<view class="loading-animation"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 帖子列表 -->
			<view v-else>
				<view class="post-list" v-if="likedPosts.length > 0">
					<view 
						class="post-item" 
						v-for="(item, index) in likedPosts" 
						:key="index"
						@click="goToDetail(item.id)"
					>
						<view class="post-header">
							<view class="author-info">
								<view class="avatar-wrapper">
									<image :src="item.avatar || defaultAvatar" class="author-avatar" mode="aspectFill"></image>
									<!-- 头像框 -->
									<image v-if="item.avatarFrameUrl" :src="item.avatarFrameUrl" class="avatar-frame" mode="aspectFit"></image>
								</view>
								<view class="author-detail">
									<view class="author-name-wrapper">
										<text class="author-name">{{ item.userName || '用户' + item.userId }}</text>
										<view class="user-level" :class="'level-' + (item.level || 1)">
											<text class="level-text">Lv.{{ item.level || 1 }}</text>
										</view>
									</view>
									<text class="publish-time">{{ formatTime(item.createdTime) }}</text>
								</view>
							</view>
						</view>
						
						<view class="post-content">
							<text class="post-title">{{ item.title }}</text>
							<text class="post-text">{{ item.content }}</text>
							
							<!-- 图片预览 -->
							<view class="post-images" v-if="item.imageUrl && item.imageUrl.length > 0">
								<view class="images-grid">
									<!-- 单张图片 -->
									<template v-if="item.imageUrl.length === 1">
										<view class="image-item single">
											<image :src="item.imageUrl[0]" mode="aspectFill" class="grid-image"></image>
										</view>
									</template>
									
									<!-- 两张图片 -->
									<template v-else-if="item.imageUrl.length === 2">
										<view class="image-item double" v-for="(img, imgIndex) in item.imageUrl" :key="imgIndex">
											<image :src="img" mode="aspectFill" class="grid-image"></image>
										</view>
									</template>
									
									<!-- 三张及以上图片 -->
									<template v-else>
										<view class="image-item triple" v-for="(img, imgIndex) in item.imageUrl.slice(0, 3)" :key="imgIndex">
											<image :src="img" mode="aspectFill" class="grid-image"></image>
											<!-- 显示剩余图片数量 -->
											<view v-if="imgIndex === 2 && item.imageUrl.length > 3" class="more-overlay">
												<text class="more-text">+{{ item.imageUrl.length - 3 }}</text>
											</view>
										</view>
									</template>
								</view>
							</view>
						</view>
						
						<view class="post-footer">
							<view class="post-stats">
								<view class="stat-item">
									<image src="/static/icon/look.png" class="stat-icon"></image>
									<text>{{ item.look || 0 }}</text>
								</view>
								<view class="stat-item">
									<image src="/static/icon/liked.png" class="stat-icon"></image>
									<text>{{ item.likes || 0 }}</text>
								</view>
								<view class="stat-item">
									<image src="/static/icon/reply.png" class="stat-icon"></image>
									<text>{{ item.commentCount || 0 }}</text>
								</view>
							</view>
							
							<view class="post-actions">
								<view class="action-btn unlike-btn" @click.stop="unlikePost(item)">
									<text>取消喜欢</text>
								</view>
							</view>
						</view>
					</view>
				</view>
				
				<!-- 上拉加载更多 -->
				<view class="load-more" v-if="likedPosts.length > 0">
					<text v-if="isLoading" class="loading-text">加载中...</text>
					<text v-else-if="hasMore" class="more-text">上拉加载更多</text>
					<text v-else class="no-more-text">没有更多数据了</text>
				</view>
				
				<!-- 无数据状态 -->
				<view class="empty-state" v-if="likedPosts.length === 0">
					<image class="empty-icon" src="/static/icon/liked.png" mode="aspectFit"></image>
					<text class="empty-text">你还没有喜欢过帖子</text>
					<view class="browse-btn" @click="browseForum">
						<text>去浏览论坛</text>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useTheme } from '@/hooks/useTheme'

const { themeClass } = useTheme()

// 喜欢的帖子列表数据
const likedPosts = ref([])
// 页码
const pageNum = ref(1)
// 是否有更多数据
const hasMore = ref(true)
// 是否正在加载
const isLoading = ref(false)
// 是否为首次加载
const isFirstLoading = ref(true)
// 是否正在刷新
const isRefreshing = ref(false)
// 默认头像
const defaultAvatar = '/static/user.png'

// 获取我喜欢的帖子列表
const getLikedPosts = async (isRefresh = false) => {
	if (isLoading.value && !isRefreshing.value) return
	
	isLoading.value = true
	if (isRefresh) {
		pageNum.value = 1
		hasMore.value = true
	}
	
	try {
		const res = await request('/forum/mylike', {
			method: 'GET',
			data: { pageNum: pageNum.value }
		})
		
		if (res.code === 200) {
			let list = res.data || []
			
			// 如果是刷新，替换列表，否则追加
			if (isRefresh) {
				likedPosts.value = list
			} else {
				likedPosts.value = [...likedPosts.value, ...list]
			}
			
			// 判断是否还有更多数据
			if (list.length < 10) {
				hasMore.value = false
			} else {
				pageNum.value++
			}
		} else {
			uni.showToast({
				title: res.msg || '获取喜欢的帖子失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取喜欢的帖子失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
		isFirstLoading.value = false
		
		if (isRefreshing.value) {
			isRefreshing.value = false
			uni.stopPullDownRefresh()
		}
	}
}

// 下拉刷新
const onRefresh = () => {
	isRefreshing.value = true
	getLikedPosts(true)
}

// 加载更多数据
const loadMoreData = () => {
	if (hasMore.value && !isLoading.value) {
		getLikedPosts()
	}
}

// 跳转到帖子详情页
const goToDetail = (id) => {
	uni.navigateTo({
		url: `/pages/forum/detail?id=${id}`
	})
}

// 取消喜欢帖子
const unlikePost = async (post) => {
	try {
		const res = await request('/forum/like', {
			method: 'POST',
			data: post.id
		})
		
		if (res.code === 200) {
			uni.showToast({
				title: '已取消喜欢',
				icon: 'success'
			})
			
			// 从列表中移除帖子
			likedPosts.value = likedPosts.value.filter(item => item.id !== post.id)
			
			// 如果删除后列表为空，显示空状态
			if (likedPosts.value.length === 0) {
				hasMore.value = false
			}
		} else {
			uni.showToast({
				title: res.msg || '操作失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('取消喜欢失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	}
}

// 浏览论坛
const browseForum = () => {
	uni.switchTab({
		url: '/pages/forum/forum'
	})
}

// 返回上一页
const goBack = () => {
	uni.navigateBack()
}

// 格式化时间
const formatTime = (timestamp) => {
	if (!timestamp) return ''
	
	// 处理后端返回的LocalDateTime格式 (ISO-8601)
	let date
	if (typeof timestamp === 'string') {
		date = new Date(timestamp)
	} else {
		date = new Date(timestamp)
	}
	
	const now = new Date().getTime()
	const diff = now - date.getTime()
	
	// 一分钟内
	if (diff < 60 * 1000) {
		return '刚刚'
	}
	// 一小时内
	else if (diff < 60 * 60 * 1000) {
		return Math.floor(diff / (60 * 1000)) + '分钟前'
	}
	// 一天内
	else if (diff < 24 * 60 * 60 * 1000) {
		return Math.floor(diff / (60 * 60 * 1000)) + '小时前'
	}
	// 一周内
	else if (diff < 7 * 24 * 60 * 60 * 1000) {
		return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前'
	}
	// 超过一周
	else {
		return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
	}
}

// 页面加载时获取我喜欢的帖子列表
onMounted(() => {
	getLikedPosts()
})
</script>

<style>
.mylike-container {
	background-color: var(--bgColor1);
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

/* 导航栏样式 */
.nav-bar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 30rpx;
	background-color: #fff;
	position: sticky;
	top: 0;
	z-index: 100;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.back-btn {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.back-icon {
	font-size: 40rpx;
	color: #333;
}

.nav-title {
	font-size: 34rpx;
	font-weight: bold;
	color: #333;
}

.placeholder {
	width: 60rpx;
}

/* 帖子列表滚动区域 */
.post-list-scroll {
	flex: 1;
	height: calc(100vh - 100rpx);
}

/* 帖子列表 */
.post-list {
	padding: 20rpx;
}

/* 帖子项 */
.post-item {
	background-color: #fff;
	border-radius: 12rpx;
	padding: 24rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.darkMode .post-item {
	background-color: var(--bgColor2);
	box-shadow: none;
}

/* 帖子头部 */
.post-header {
	margin-bottom: 16rpx;
}

.author-info {
	display: flex;
	align-items: center;
}

.avatar-wrapper {
	position: relative;
	margin-right: 16rpx;
}

.author-avatar {
	width: 70rpx;
	height: 70rpx;
	border-radius: 35rpx;
	z-index: 1;
}

.avatar-frame {
	position: absolute;
	top: -15rpx;
	left: -15rpx;
	width: 100rpx;
	height: 100rpx;
	z-index: 2;
	pointer-events: none;
}

.author-detail {
	flex: 1;
}

.author-name-wrapper {
	display: flex;
	align-items: center;
}

.author-name {
	font-size: 28rpx;
	font-weight: bold;
	color: var(--textColor1);
	margin-right: 10rpx;
}

.user-level {
	display: inline-block;
	font-size: 20rpx;
	padding: 0 8rpx;
	height: 28rpx;
	line-height: 28rpx;
	color: #fff;
	background-color: #1abc9c;
	border-radius: 14rpx;
}

.level-text {
	font-size: 20rpx;
}

.publish-time {
	font-size: 24rpx;
	color: var(--textColor3);
	margin-top: 4rpx;
}

/* 帖子内容 */
.post-content {
	margin-bottom: 16rpx;
}

.post-title {
	font-size: 32rpx;
	font-weight: bold;
	color: var(--textColor1);
	margin-bottom: 10rpx;
	display: block;
}

.post-text {
	font-size: 28rpx;
	color: var(--textColor2);
	line-height: 1.5;
	margin-bottom: 16rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	line-clamp: 2;
	-webkit-box-orient: vertical;
}

/* 图片网格 */
.post-images {
	margin-top: 10rpx;
}

.images-grid {
	display: flex;
	flex-wrap: wrap;
	margin: -4rpx;
}

.image-item {
	margin: 4rpx;
	position: relative;
	overflow: hidden;
	border-radius: 8rpx;
}

.grid-image {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.single {
	width: calc(100% - 8rpx);
	height: 300rpx;
}

.double {
	width: calc(50% - 8rpx);
	height: 220rpx;
}

.triple {
	width: calc(33.33% - 8rpx);
	height: 180rpx;
}

.more-overlay {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
}

.more-text {
	color: #fff;
	font-size: 32rpx;
	font-weight: bold;
}

/* 帖子底部 */
.post-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 16rpx;
	border-top: 1rpx solid var(--borderColor);
}

.post-stats {
	display: flex;
}

.stat-item {
	display: flex;
	align-items: center;
	margin-right: 20rpx;
	font-size: 24rpx;
	color: var(--textColor3);
}

.stat-icon {
	width: 28rpx;
	height: 28rpx;
	margin-right: 6rpx;
}

.post-actions {
	display: flex;
}

.action-btn {
	padding: 6rpx 20rpx;
	border-radius: 30rpx;
	font-size: 24rpx;
}

.unlike-btn {
	background-color: #fff2f0;
	color: #ff4d4f;
	border: 1rpx solid #ffccc7;
}

.darkMode .unlike-btn {
	background-color: rgba(255, 77, 79, 0.1);
	border: 1rpx solid rgba(255, 77, 79, 0.3);
}

/* 加载中状态 */
.loading-wrapper {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 60rpx 0;
}

.loading-animation {
	width: 40rpx;
	height: 40rpx;
	border: 4rpx solid var(--borderColor);
	border-top: 4rpx solid var(--themeColor1);
	border-radius: 50%;
	animation: spin 1s linear infinite;
	margin-bottom: 20rpx;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text, .more-text, .no-more-text {
	font-size: 24rpx;
	color: var(--textColor3);
	text-align: center;
	padding: 20rpx 0;
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
	color: var(--textColor3);
	margin-bottom: 30rpx;
}

.browse-btn {
	background-color: var(--themeColor1);
	color: #fff;
	font-size: 28rpx;
	padding: 15rpx 40rpx;
	border-radius: 40rpx;
}

/* 加载更多 */
.load-more {
	text-align: center;
	padding: 20rpx 0 40rpx;
}
</style>