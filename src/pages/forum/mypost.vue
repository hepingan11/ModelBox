<template>
	<view class="mypost-container">
		
		<!-- 选项卡 -->
		<view class="tab-bar">
			<view 
				class="tab-item" 
				v-for="(tab, index) in tabs" 
				:key="index" 
				:class="{ active: currentTab === index }"
				@click="switchTab(index)"
			>
				<text class="tab-text">{{ tab.name }}</text>
			</view>
		</view>
		
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
				<!-- 我的帖子列表 -->
				<view v-if="currentTab === 0">
					<view class="post-list" v-if="postList.length > 0">
						<view 
							class="post-item" 
							v-for="(item, index) in postList" 
							:key="index"
							@click="goToDetail(item.id)"
						>
							<view class="post-header">
								<view class="post-title-row">
									<text class="post-title">{{ item.title }}</text>
									<!-- <view class="post-status" :class="{'status-offline': !item.isOnline}">
										<text>{{ item.isOnline ? '已发布' : '已下架' }}</text>
									</view> -->
								</view>
								<text class="post-time">{{ formatTime(item.createdTime) }}</text>
							</view>
							
							<view class="post-content">
								<view class="post-text">{{ item.content }}</view>
								
								<!-- 图片预览 -->
								<view class="post-images" v-if="item.imageUrl && item.imageUrl.length > 0">
									<image 
										v-for="(img, imgIndex) in item.imageUrl.slice(0, 3)" 
										:key="imgIndex" 
										:src="img" 
										class="post-image"
										mode="aspectFill"
									></image>
									<view class="image-count" v-if="item.imageUrl.length > 3">
										<text>+{{ item.imageUrl.length - 3 }}</text>
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
									<view class="action-btn delete-btn" @click.stop="confirmDelete(item)">
										<text>删除</text>
									</view>
								</view>
							</view>
						</view>
					</view>
					
					<!-- 上拉加载更多 -->
					<view class="load-more" v-if="postList.length > 0">
						<text v-if="isLoading" class="loading-text">加载中...</text>
						<text v-else-if="hasMore" class="more-text">上拉加载更多</text>
						<text v-else class="no-more-text">没有更多数据了</text>
					</view>
					
					<!-- 无数据状态 -->
					<view class="empty-state" v-if="postList.length === 0">
						<image class="empty-icon" src="/static/icon/post.png" mode="aspectFit"></image>
						<text class="empty-text">你还没有发布过帖子</text>
						<view class="create-post-btn" @click="createNewPost">
							<text>去发布</text>
						</view>
					</view>
				</view>
				
				
				<!-- 活动广告列表 -->
				<view v-else-if="currentTab === 3">
					<view class="activity-list" v-if="activityList.length > 0">
						<view 
							class="activity-item" 
							v-for="(item, index) in activityList" 
							:key="index"
							@click="goToActivity(item)"
						>
							<image :src="item.image" class="activity-image" mode="aspectFill"></image>
							<view class="activity-content">
								<view class="activity-header">
									<text class="activity-title">{{ item.title }}</text>
									<view class="activity-tag">{{ item.type }}</view>
								</view>
								<text class="activity-text">{{ item.content }}</text>
								<view class="activity-footer">
									<text class="activity-time">{{ formatTime(item.createdTime) }}</text>
									<view class="activity-btn">
										<text>查看详情</text>
										<text class="arrow-icon">→</text>
									</view>
								</view>
							</view>
						</view>
					</view>
					
					<!-- 无数据状态 -->
					<view class="empty-state" v-if="activityList.length === 0 && !isLoading">
						<image class="empty-icon" src="/static/icon/activity.png" mode="aspectFit"></image>
						<text class="empty-text">暂无活动广告</text>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

// 选项卡定义
const tabs = [
	{ name: '我的帖子' },
	{ name: '活动广告' }
]
const currentTab = ref(0)

// 帖子列表数据
const postList = ref([])
// 本周热门帖子
const weeklyHotList = ref([])
// 本月热门帖子
const monthlyHotList = ref([])
// 活动广告列表
const activityList = ref([])
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

// 切换选项卡
const switchTab = (index) => {
	if (currentTab.value === index) return
	
	currentTab.value = index
	isFirstLoading.value = true
	
	// 根据选中的选项卡加载相应的数据
	switch(index) {
		case 0:
			if (postList.value.length === 0) {
				getMyPosts(true)
			} else {
				isFirstLoading.value = false
			}
			break
		case 1:
			if (weeklyHotList.value.length === 0) {
				getWeeklyHotPosts()
			} else {
				isFirstLoading.value = false
			}
			break
		case 2:
			if (monthlyHotList.value.length === 0) {
				getMonthlyHotPosts()
			} else {
				isFirstLoading.value = false
			}
			break
		case 3:
			if (activityList.value.length === 0) {
				getActivityList()
			} else {
				isFirstLoading.value = false
			}
			break
	}
}

// 获取我的帖子列表
const getMyPosts = async (isRefresh = false) => {
	if (isLoading.value && !isRefreshing.value) return
	
	isLoading.value = true
	if (isRefresh) {
		pageNum.value = 1
		hasMore.value = true
	}
	
	try {
		const res = await request('/forum/listByMy', {
			method: 'GET',
			data: { pageNum: pageNum.value }
		})
		
		if (res.code === 200) {
			let list = res.data || []
			
			// 如果是刷新，替换列表，否则追加
			if (isRefresh) {
				postList.value = list
			} else {
				postList.value = [...postList.value, ...list]
			}
			
			// 判断是否还有更多数据
			if (list.length < 10) {
				hasMore.value = false
			} else {
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
		isFirstLoading.value = false
		
		if (isRefreshing.value) {
			isRefreshing.value = false
			uni.stopPullDownRefresh()
		}
	}
}

// 获取本周热门帖子
const getWeeklyHotPosts = async () => {
	isLoading.value = true
	
	try {
		const res = await request('/forum/weeklyHot', {
			method: 'GET'
		})
		
		if (res.code === 200) {
			weeklyHotList.value = res.data || []
		} else {
			uni.showToast({
				title: res.msg || '获取本周热门失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取本周热门失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
		isFirstLoading.value = false
	}
}

// 获取本月热门帖子
const getMonthlyHotPosts = async () => {
	isLoading.value = true
	
	try {
		const res = await request('/forum/monthlyHot', {
			method: 'GET'
		})
		
		if (res.code === 200) {
			monthlyHotList.value = res.data || []
		} else {
			uni.showToast({
				title: res.msg || '获取本月热门失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取本月热门失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
		isFirstLoading.value = false
	}
}

// 获取活动广告列表
const getActivityList = async () => {
	isLoading.value = true
	
	try {
		const res = await request('/activity/list', {
			method: 'GET',
			data: { pageNum: 1 }
		})
		
		if (res.code === 200) {
			activityList.value = res.data || []
		} else {
			uni.showToast({
				title: res.msg || '获取活动列表失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取活动列表失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
		isFirstLoading.value = false
	}
}

// 跳转到活动详情页
const goToActivity = (activity) => {
	// 如果有URL，则跳转到URL
	if (activity.url) {
		// 检查URL是否为外部链接(http或https开头)
		if (activity.url.startsWith('http://') || activity.url.startsWith('https://')) {
			// 外部链接，使用系统浏览器打开
			// #ifdef H5
			window.open(activity.url, '_blank')
			// #endif
			
			// #ifdef APP-PLUS
			plus.runtime.openURL(activity.url)
			// #endif
			
			// #ifdef MP
			uni.setClipboardData({
				data: activity.url,
				success: () => {
					uni.showToast({
						title: '链接已复制，请在浏览器中打开',
						icon: 'none'
					})
				}
			})
			// #endif
		} else {
			// 内部页面，直接导航
			uni.navigateTo({
				url: activity.url
			})
		}
	} else {
		// 没有URL，跳转到活动详情页
		uni.navigateTo({
			url: `/pages/activity/detail?id=${activity.id}`
		})
	}
}

// 下拉刷新
const onRefresh = () => {
	isRefreshing.value = true
	
	// 根据当前选中的选项卡刷新数据
	switch(currentTab.value) {
		case 0:
			getMyPosts(true)
			break
		case 1:
			getWeeklyHotPosts()
			break
		case 2:
			getMonthlyHotPosts()
			break
		case 3:
			getActivityList()
			break
	}
}

// 加载更多数据
const loadMoreData = () => {
	// 只有我的帖子选项卡支持分页加载更多
	if (currentTab.value === 0 && hasMore.value && !isLoading.value) {
		getMyPosts()
	}
}

// 跳转到帖子详情页
const goToDetail = (id) => {
	uni.navigateTo({
		url: `/pages/forum/detail?id=${id}`
	})
}

// 编辑帖子
const editPost = (post) => {
	uni.navigateTo({
		url: `/pages/forum/publish?id=${post.id}`
	})
}

// 确认删除帖子
const confirmDelete = (post) => {
	uni.showModal({
		title: '确认删除',
		content: '确定要删除这条帖子吗？删除后无法恢复',
		confirmColor: '#ff4d4f',
		success: (res) => {
			if (res.confirm) {
				deletePost(post.id)
			}
		}
	})
}

// 删除帖子
const deletePost = async (id) => {
	try {
		const res = await request('/forum/deleteById', {
			method: 'GET',
			data: { id: id }
		})
		
		if (res.code === 200) {
			uni.showToast({
				title: '删除成功',
				icon: 'success'
			})
			
			// 从列表中移除已删除的帖子
			postList.value = postList.value.filter(item => item.id !== id)
			
			// 如果删除后列表为空，显示空状态
			if (postList.value.length === 0) {
				hasMore.value = false
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

// 创建新帖子
const createNewPost = () => {
	uni.navigateTo({
		url: '/pages/forum/publish'
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

// 页面加载时获取我的帖子列表
onMounted(() => {
	getMyPosts()
})
</script>

<style>
.mypost-container {
	background-color: #f5f7fa;
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

/* 选项卡样式 */
.tab-bar {
	display: flex;
	justify-content: space-around;
	background-color: #fff;
	padding: 15rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
	position: sticky;
	top: 100rpx;
	z-index: 99;
}

.tab-item {
	padding: 10rpx 20rpx;
	position: relative;
}

.tab-text {
	font-size: 28rpx;
	color: #666;
	transition: all 0.3s;
}

.tab-item.active .tab-text {
	color: #1abc9c;
	font-weight: bold;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: -10rpx;
	left: 50%;
	transform: translateX(-50%);
	width: 40rpx;
	height: 4rpx;
	background-color: #1abc9c;
	border-radius: 2rpx;
}

/* 修改帖子列表滚动区域高度 */
.post-list-scroll {
	flex: 1;
	height: calc(100vh - 180rpx);
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
	position: relative;
}

.post-header {
	margin-bottom: 16rpx;
}

.post-title-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10rpx;
}

.post-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	flex: 1;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.post-status {
	font-size: 24rpx;
	color: #1abc9c;
	background-color: rgba(26, 188, 156, 0.1);
	padding: 4rpx 12rpx;
	border-radius: 20rpx;
	margin-left: 10rpx;
}

.status-offline {
	color: #999;
	background-color: #f5f5f5;
}

.post-time {
	font-size: 24rpx;
	color: #999;
}

.post-content {
	margin-bottom: 16rpx;
}

.post-text {
	font-size: 28rpx;
	color: #666;
	line-height: 1.5;
	margin-bottom: 16rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
}

.post-images {
	display: flex;
	margin-top: 10rpx;
}

.post-image {
	width: 150rpx;
	height: 150rpx;
	margin-right: 10rpx;
	border-radius: 8rpx;
	object-fit: cover;
}

.image-count {
	width: 150rpx;
	height: 150rpx;
	background-color: rgba(0, 0, 0, 0.03);
	border-radius: 8rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #999;
	font-size: 30rpx;
}

.post-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 16rpx;
	border-top: 1rpx solid #f0f0f0;
}

.post-stats {
	display: flex;
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

.post-actions {
	display: flex;
}

.action-btn {
	padding: 6rpx 20rpx;
	border-radius: 30rpx;
	font-size: 24rpx;
	margin-left: 15rpx;
}

.edit-btn {
	background-color: #e6f7ff;
	color: #1890ff;
	border: 1rpx solid #91d5ff;
}

.delete-btn {
	background-color: #fff2f0;
	color: #ff4d4f;
	border: 1rpx solid #ffccc7;
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
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #1abc9c;
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
	color: #999;
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
	color: #999;
	margin-bottom: 30rpx;
}

.create-post-btn {
	background-color: #1abc9c;
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

/* 热门排名样式 */
.hot-ranking {
	position: absolute;
	top: 15rpx;
	left: 15rpx;
	width: 40rpx;
	height: 40rpx;
	background-color: rgba(26, 188, 156, 0.8);
	color: #fff;
	font-size: 28rpx;
	font-weight: bold;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 2;
}

/* 前三名特殊样式 */
.post-item:nth-child(1) .hot-ranking {
	background-color: #ff6b6b;
}

.post-item:nth-child(2) .hot-ranking {
	background-color: #ff922b;
}

.post-item:nth-child(3) .hot-ranking {
	background-color: #ffd43b;
}

/* 作者信息样式 */
.author-info {
	display: flex;
	align-items: center;
	margin-bottom: 15rpx;
}

.author-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 30rpx;
	margin-right: 10rpx;
}

.author-name {
	font-size: 26rpx;
	color: #666;
}

/* 活动广告样式 */
.activity-list {
	padding: 20rpx;
}

.activity-item {
	background-color: #fff;
	border-radius: 12rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	overflow: hidden;
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
}

.activity-tag {
	padding: 4rpx 15rpx;
	background-color: rgba(26, 188, 156, 0.1);
	color: #1abc9c;
	font-size: 24rpx;
	border-radius: 20rpx;
}

.activity-text {
	font-size: 28rpx;
	color: #666;
	line-height: 1.5;
	margin-bottom: 20rpx;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
	text-overflow: ellipsis;
}

.activity-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 15rpx;
	border-top: 1rpx solid #f0f0f0;
}

.activity-time {
	font-size: 24rpx;
	color: #999;
}

.activity-btn {
	display: flex;
	align-items: center;
	color: #1abc9c;
	font-size: 26rpx;
}

.arrow-icon {
	margin-left: 6rpx;
}
</style>