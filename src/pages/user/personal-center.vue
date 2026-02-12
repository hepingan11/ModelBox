<template>
	<view class="personal-center" :class="themeClass">
		
		
		<!-- 顶部用户信息卡片 -->
		<scroll-view scroll-y="true" class="scroll-container" 
			:style="props.userId ? 'height: calc(100% - 90rpx);' : ''"
			@scrolltolower="onReachBottom" 
			refresher-enabled 
			:refresher-triggered="refreshing" 
			@refresherrefresh="onRefresh">
			
			<view class="user-card">
				<!-- 背景图 -->
				<view class="background-image">
					<image :src="userInfo.backgroundImage || defaultBackgroundImage" mode="aspectFill" class="bg-img"></image>
					<view class="bg-overlay"></view>
				</view>
				
				<view class="user-header">
					<view class="avatar-wrapper">
						<image :src="userInfo.avatar" mode="aspectFill" class="avatar-img"></image>
						<!-- 头像框 -->
						<image v-if="userInfo.avatarFrameUrl" :src="userInfo.avatarFrameUrl" class="avatar-frame" mode="aspectFit"></image>
					</view>
					<view class="user-details">
						<text class="user-name">{{ userInfo.userName || '未设置昵称' }}</text>
						<!-- 用户等级 -->
						<view class="user-level" :class="'level-' + (userInfo.level || 1)">
							<text class="level-text">Lv.{{ userInfo.level || 1 }}</text>
						</view>
					</view>
					<!-- 聊天按钮（只在查看他人主页时显示） -->
					<view v-if="props.userId && props.userId != currentUserId" class="chat-btn" @click.stop="goToChat">
						<text class="chat-icon">💬</text>
					</view>
				</view>
			</view>
			
			<!-- 内容标签页 -->
			<view class="content-tabs">
				<view 
					class="tab-item" 
					:class="{ active: activeTab === 'projects' }" 
					@click="activeTab = 'projects'"
				>
					<text>我的项目</text>
					<view class="tab-line" v-if="activeTab === 'projects'"></view>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: activeTab === 'posts' }" 
					@click="activeTab = 'posts'"
				>
					<text>我的帖子</text>
					<view class="tab-line" v-if="activeTab === 'posts'"></view>
				</view>
			</view>
			
			<!-- 项目列表 -->
			<view class="content-section" v-if="activeTab === 'projects'">
				<view class="empty-state" v-if="projectsList.length === 0 && !projectsLoadingMore">
					<image src="/static/empty.png" class="empty-icon"></image>
					<text class="empty-text">暂无发起项目</text>
					<view class="empty-buttons">
						<button class="empty-btn" @click="goToProjectList">去看看</button>
						<button class="empty-btn primary" @click="goToCreateProject">发起项目</button>
					</view>
				</view>
				
				<view class="projects-list" v-else>
					<view class="project-item" v-for="(item, index) in projectsList" :key="index" @click="goToProjectDetail(item.projectId)">
						<image :src="item.imageUrlList && item.imageUrlList.length > 0 ? item.imageUrlList[0] : '/static/default-project.png'" mode="aspectFill" class="project-image"></image>
						<view class="project-info">
							<text class="project-title">{{ item.projectName }}</text>
							<text class="project-intro" v-if="item.introduce">{{ item.introduce }}</text>
							<view class="project-tags">
								<text class="tag" v-if="item.projectFieldName">🏷️ {{ item.projectFieldName }}</text>
								<text class="tag" v-if="item.city">📍 {{ item.city }}</text>
							</view>
							<view class="project-status">
								<text class="project-date">{{ formatDate(item.createdTime) }}</text>
							</view>
						</view>
					</view>
					
					<!-- 加载更多 -->
					<view class="load-more" @click="loadMoreProjects" v-if="projectsList.length > 0 && projectsHasMore">
						<text class="load-text">{{ projectsLoadingMore ? '加载中...' : '加载更多' }}</text>
					</view>
					
					<view class="no-more" v-if="projectsList.length > 0 && !projectsHasMore">
						<text class="no-more-text">没有更多数据了</text>
					</view>
				</view>
			</view>
			
			<!-- 帖子列表 -->
			<view class="content-section" v-if="activeTab === 'posts'">
				<view class="empty-state" v-if="postsList.length === 0 && !postsLoadingMore">
					<image src="/static/empty.png" class="empty-icon"></image>
					<text class="empty-text">暂无发布帖子</text>
					<view class="empty-buttons">
						<button class="empty-btn" @click="goToForum">去看看</button>
						<button class="empty-btn primary" @click="goToPublishPost">发布帖子</button>
					</view>
				</view>
				
				<view class="posts-list" v-else>
					<view class="post-item" v-for="(item, index) in postsList" :key="index" @click="goToPostDetail(item.id)">
						<view class="post-content">
							<text class="post-title">{{ item.title }}</text>
							<text class="post-summary">{{ item.content }}</text>
						</view>
						<view class="post-images" v-if="item.imageUrl && item.imageUrl.length > 0">
							<image v-for="(img, imgIndex) in item.imageUrl.slice(0, 3)" :key="imgIndex" :src="img" mode="aspectFill" class="post-image"></image>
						</view>
						<view class="post-footer">
							<view class="post-stats">
								<view class="stat-item">
									<image src="/static/icon/look.png" class="stat-icon"></image>
									<text class="stat-count">{{ item.look || 0 }}</text>
								</view>
								<view class="stat-item">
									<image src="/static/icon/liked.png" class="stat-icon"></image>
									<text class="stat-count">{{ item.likes || 0 }}</text>
								</view>
							</view>
							<text class="post-date">{{ formatDate(item.createTime) }}</text>
						</view>
					</view>
					
					<!-- 加载更多 -->
					<view class="load-more" @click="loadMorePosts" v-if="postsList.length > 0 && postsHasMore">
						<text class="load-text">{{ postsLoadingMore ? '加载中...' : '加载更多' }}</text>
					</view>
					
					<view class="no-more" v-if="postsList.length > 0 && !postsHasMore">
						<text class="no-more-text">没有更多数据了</text>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import request from '@/utils/request.js'
import { useTheme } from '@/hooks/useTheme'

const { themeClass } = useTheme()

// 获取页面参数
const props = defineProps({
	userId: {
		type: [String, Number],
		default: ''
	}
})

// 默认背景图
const defaultBackgroundImage = ''

// 用户信息
const userInfo = ref({
	id: '',
	userName: '',
	type: '',
	avatar: '',
	phone: '',
	backgroundImage: '',
	avatarFrameUrl: '',
	level: 1
})

// 当前登录用户ID
const currentUserId = ref(uni.getStorageSync('userId'))

// 活动标签页
const activeTab = ref('projects')

// 项目列表和帖子列表
const projectsList = ref([])
const postsList = ref([])

// 分页参数
const projectsPage = ref(1)
const postsPage = ref(1)
const pageSize = 10
const projectsLoadingMore = ref(false)
const postsLoadingMore = ref(false)
const projectsHasMore = ref(true)
const postsHasMore = ref(true)
const refreshing = ref(false)

// 获取用户信息
const getUserInfo = () => {
	// 如果有userId参数，则获取指定用户的信息
	const url = props.userId ? '/user/getUserInfoById' : '/user/getUserInfo'
	const params = props.userId ? { userId: props.userId } : {}
	
	request(url, {
		method: 'GET',
		data: params
	}).then(res => {
		if (res.code === 200) {
			userInfo.value = res.data
			console.log('获取到用户信息:', userInfo.value)
			// 获取到用户信息后加载项目和帖子
			if (activeTab.value === 'projects') {
				getUserProjects()
			} else {
				getUserPosts()
			}
		} else {
			uni.showToast({
				title: res.msg || '获取用户信息失败',
				icon: 'none'
			})
		}
	}).catch(err => {
		console.error('获取用户信息失败:', err)
		uni.showToast({
			title: '获取用户信息失败',
			icon: 'none'
		})
	})
}

// 获取用户发起的项目
const getUserProjects = () => {
	if (!userInfo.value.id || !projectsHasMore.value) return
	
	projectsLoadingMore.value = true
	uni.showLoading({
		title: '加载中...'
	})
	
	request('/project/projectListByUserId', {
		method: 'GET',
		data: {
			pageNum: projectsPage.value,
			userId: userInfo.value.id
		}
	}).then(res => {
		uni.hideLoading()
		projectsLoadingMore.value = false
		
		if (res.code === 200) {
			const records = res.data.records || []
			
			if (projectsPage.value === 1) {
				projectsList.value = records
			} else {
				projectsList.value = [...projectsList.value, ...records]
			}
			
			// 判断是否还有更多数据
			if (records.length < pageSize) {
				projectsHasMore.value = false
			}
			
			console.log('获取项目成功:', projectsList.value)
		} else {
			uni.showToast({
				title: res.msg || '获取项目失败',
				icon: 'none'
			})
		}
	}).catch((err) => {
		console.error('获取项目失败:', err)
		uni.hideLoading()
		projectsLoadingMore.value = false
	})
}

// 获取用户发布的帖子
const getUserPosts = () => {
	if (!userInfo.value.id || !postsHasMore.value) return
	postsLoadingMore.value = true
	uni.showLoading({
		title: '加载中...'
	})
	
	request('/forum/listByUserId', {
		method: 'GET',
		data: {
			pageNum: postsPage.value,
			userId: userInfo.value.id
		}
	}).then(res => {
		uni.hideLoading()
		postsLoadingMore.value = false
		
		if (res.code === 200) {
			const records = res.data || []
			
			if (postsPage.value === 1) {
				postsList.value = records
			} else {
				postsList.value = [...postsList.value, ...records]
			}
			
			// 判断是否还有更多数据
			if (records.length < pageSize) {
				postsHasMore.value = false
			}
			
			console.log('获取帖子成功:', postsList.value)
		} else {
			uni.showToast({
				title: res.msg || '获取帖子失败',
				icon: 'none'
			})
		}
	}).catch((err) => {
		console.error('获取帖子失败:', err)
		uni.hideLoading()
		postsLoadingMore.value = false
	})
}

// 加载更多项目
const loadMoreProjects = () => {
	if (projectsLoadingMore.value || !projectsHasMore.value) return
	projectsPage.value++
	getUserProjects()
}

// 加载更多帖子
const loadMorePosts = () => {
	if (postsLoadingMore.value || !postsHasMore.value) return
	postsPage.value++
	getUserPosts()
}

// 跳转到项目详情
const goToProjectDetail = (id) => {
	uni.navigateTo({
		url: `/pages/project/detail?id=${id}`
	})
}

// 跳转到聊天页面
const goToChat = () => {
	if (!props.userId) {
		uni.showToast({ title: '用户ID为空', icon: 'none' })
		return
	}
	
	uni.navigateTo({
		url: `/pages/message/chat?userId=${props.userId}`
	})
}

// 跳转到项目列表
const goToProjectList = () => {
	uni.switchTab({
		url: '/pages/project/projectList'
	})
}

// 跳转到发起项目页面
const goToCreateProject = () => {
	uni.navigateTo({
		url: '/pages/project/createProject'
	})
}


// 跳转到帖子详情
const goToPostDetail = (id) => {
	uni.navigateTo({
		url: `/pages/forum/detail?id=${id}`
	})
}

// 跳转到论坛首页
const goToForum = () => {
	uni.switchTab({
		url: '/pages/forum/forum'
	})
}

// 跳转到发布帖子页面
const goToPublishPost = () => {
	uni.navigateTo({
		url: '/pages/forum/publish'
	})
}

// 返回上一页
const goBack = () => {
	uni.navigateBack()
}

// 日期格式化
const formatDate = (dateString) => {
	if (!dateString) return ''
	
	const date = new Date(dateString)
	const now = new Date()
	
	// 计算时间差（毫秒）
	const diff = now - date
	
	// 如果小于24小时，显示"x小时前"
	if (diff < 24 * 60 * 60 * 1000) {
		const hours = Math.floor(diff / (60 * 60 * 1000))
		if (hours < 1) {
			return '刚刚'
		}
		return `${hours}小时前`
	}
	
	// 如果小于30天，显示"x天前"
	if (diff < 30 * 24 * 60 * 60 * 1000) {
		const days = Math.floor(diff / (24 * 60 * 60 * 1000))
		return `${days}天前`
	}
	
	// 否则显示年月日
	const year = date.getFullYear()
	const month = (date.getMonth() + 1).toString().padStart(2, '0')
	const day = date.getDate().toString().padStart(2, '0')
	
	return `${year}-${month}-${day}`
}

// 刷新页面数据
const onRefresh = () => {
	refreshing.value = true
	
	// 重置分页参数
	projectsPage.value = 1
	postsPage.value = 1
	projectsHasMore.value = true
	postsHasMore.value = true
	
	// 重新获取数据
	getUserInfo()
	
	setTimeout(() => {
		refreshing.value = false
	}, 1000)
}

// 触底加载更多
const onReachBottom = () => {
	if (activeTab.value === 'projects') {
		loadMoreProjects()
	} else {
		loadMorePosts()
	}
}

// 监听标签页切换
watch(activeTab, (newVal) => {
	// 每次切换标签都重置一下数据，避免看到旧数据
	if (newVal === 'projects') {
		if (projectsList.value.length === 0) {
			projectsPage.value = 1
			projectsHasMore.value = true
			getUserProjects()
		}
	} else if (newVal === 'posts') {
		if (postsList.value.length === 0) {
			postsPage.value = 1
			postsHasMore.value = true
			getUserPosts()
		}
	}
})

onMounted(() => {
	// 页面加载时获取用户信息
	getUserInfo()
})
</script>

<style lang="scss" scoped>
.personal-center {
	background-color: var(--bgColor1);
	min-height: 100vh;
	height: 100vh;
	position: relative;
}

.scroll-container {
	height: 100%;
	box-sizing: border-box;
}

/* 顶部标题栏 */
.nav-bar {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 90rpx;
	background-color: var(--bgColor2);
	display: flex;
	align-items: center;
	padding: 0 30rpx;
	z-index: 10;
}

.nav-left {
	display: flex;
	align-items: center;
}

.back-icon {
	font-size: 32rpx;
	margin-right: 10rpx;
}

.nav-title {
	font-size: 32rpx;
	font-weight: bold;
	color: var(--textColor1);
}

.nav-right {
	margin-left: auto;
}

/* 用户信息卡片 */
.user-card {
	position: relative;
	background-color: var(--bgColor2);
	border-radius: 0 0 12rpx 12rpx;
	padding: 140rpx 30rpx 40rpx;
	box-shadow: 0 2rpx 10rpx var(--bgboxShadowColor1);
	margin-bottom: 20rpx;
	overflow: hidden;
}

/* 背景图样式 */
.background-image {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 240rpx;
	overflow: hidden;
}

.bg-img {
	width: 100%;
	height: 100%;
	object-fit: cover;
	transform: scale(1.05);
}

.bg-overlay {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(0,0,0,0.5));
}

.user-header {
	position: relative;
	z-index: 2;
	display: flex;
	align-items: center;
}

.avatar-wrapper {
	position: relative;
	margin-left: 8rpx;
	margin-right: 30rpx;
}

.avatar-img {
	width: 150rpx;
	height: 150rpx;
	border-radius: 75rpx;
	border: 4rpx solid #fff;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
	z-index: 1;
}

.avatar-frame {
	position: absolute;
	top: -30rpx;
	left: -30rpx;
	width: 220rpx;
	height: 220rpx;
	z-index: 2;
	pointer-events: none;
}

.user-details {
	margin-left: 20rpx;
	flex: 1;
}

.user-name {
	font-size: 38rpx;
	font-weight: bold;
	color: #fff;
	margin-top: 12rpx;
	margin-bottom: 25rpx;
	display: block;
	text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* 聊天按钮 */
.chat-btn {
	width: 88rpx;
	height: 88rpx;
	background: var(--themeColor1);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 12rpx var(--bgboxShadowColor1);
	transition: all 0.3s ease;
}

.chat-btn:active {
	transform: scale(0.95);
	opacity: 0.8;
}

.chat-icon {
	font-size: 40rpx;
	line-height: 1;
}

/* 内容标签页 */
.content-tabs {
	display: flex;
	background-color: var(--bgColor2);
	padding: 0 30rpx;
	position: sticky;
	top: 0;
	z-index: 10;
}

.tab-item {
	padding: 30rpx 0;
	margin-right: 60rpx;
	position: relative;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.tab-item text {
	font-size: 32rpx;
	color: var(--textColor3);
	font-weight: 500;
}

.tab-item.active text {
	color: var(--themeColor1);
	font-weight: bold;
}

.tab-line {
	position: absolute;
	bottom: 0;
	width: 60rpx;
	height: 6rpx;
	background-color: var(--themeColor1);
	border-radius: 3rpx;
}

/* 内容区域 */
.content-section {
	padding: 20rpx 30rpx;
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
	font-size: 30rpx;
	color: var(--textColor3);
	margin-bottom: 30rpx;
}

.empty-buttons {
	display: flex;
	justify-content: center;
	margin-top: 20rpx;
}

.empty-btn {
	margin: 0 15rpx;
	padding: 12rpx 40rpx;
	font-size: 28rpx;
	border-radius: 40rpx;
	background-color: var(--bgColor1);
	color: var(--textColor3);
	border: none;
	line-height: 1.5;
}

.empty-btn.primary {
	background-color: var(--themeColor1);
	color: var(--themeTextColor);
}

/* 项目列表 */
.projects-list {
	padding: 10rpx 0;
}

.project-item {
	display: flex;
	padding: 20rpx;
	background-color: var(--bgColor2);
	border-radius: 12rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 6rpx var(--bgboxShadowColor1);
}

.project-image {
	width: 160rpx;
	height: 160rpx;
	border-radius: 8rpx;
	margin-right: 20rpx;
	object-fit: cover;
}

.project-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.project-title {
	font-size: 30rpx;
	font-weight: 500;
	color: var(--textColor1);
	margin-bottom: 6rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
}

.project-intro {
	font-size: 26rpx;
	color: var(--textColor3);
	margin-bottom: 8rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
}

.project-tags {
	display: flex;
	gap: 8rpx;
	margin-bottom: 8rpx;
	flex-wrap: wrap;
}

.project-tags .tag {
	font-size: 22rpx;
	color: var(--textColor3);
	background-color: var(--bgColor1);
	padding: 4rpx 8rpx;
	border-radius: 4rpx;
}

.project-status {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.status-tag {
	font-size: 24rpx;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
}

.status-PENDING {
	background-color: #fff3e0;
	color: #f57c00;
}

.status-APPROVED,
.status-ACTIVE {
	background-color: rgba(0, 168, 114, 0.1);
	color: #00A872;
}

.status-REJECTED {
	background-color: #ffebee;
	color: #f44336;
}

.status-COMPLETED {
	background-color: #e3f2fd;
	color: #2196f3;
}

.project-date {
	font-size: 24rpx;
	color: #999;
}

/* 帖子列表 */
.posts-list {
	padding: 10rpx 0;
}

.post-item {
	padding: 24rpx;
	background-color: #fff;
	border-radius: 12rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.04);
}

.post-title {
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
	margin-bottom: 12rpx;
	display: block;
}

.post-summary {
	font-size: 28rpx;
	color: #666;
	margin-bottom: 16rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.post-images {
	display: flex;
	margin-bottom: 16rpx;
	flex-wrap: wrap;
}

.post-image {
	width: 200rpx;
	height: 200rpx;
	border-radius: 8rpx;
	margin-right: 10rpx;
	margin-bottom: 10rpx;
	object-fit: cover;
}

.post-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.post-stats {
	display: flex;
}

.stat-item {
	display: flex;
	align-items: center;
	margin-right: 20rpx;
}

.stat-icon {
	width: 32rpx;
	height: 32rpx;
	margin-right: 6rpx;
}

.stat-count {
	font-size: 26rpx;
	color: #999;
}

.post-date {
	font-size: 24rpx;
	color: #999;
}

/* 加载更多 */
.load-more {
	text-align: center;
	padding: 20rpx 0;
	background-color: #fff;
	border-radius: 12rpx;
	margin-top: 10rpx;
}

.load-text {
	font-size: 28rpx;
	color: #999;
}

.no-more {
	text-align: center;
	padding: 20rpx 0;
	margin-top: 10rpx;
}

.no-more-text {
	font-size: 28rpx;
	color: #999;
}
</style>