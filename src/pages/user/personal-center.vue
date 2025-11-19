<template>
	<view class="personal-center">
		
		
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
						<text class="user-name">{{ userInfo.username || '未设置昵称' }}</text>
						<!-- 用户等级 -->
						<view class="user-level" :class="'level-' + (userInfo.level || 1)">
							<text class="level-text">Lv.{{ userInfo.level || 1 }}</text>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 内容标签页 -->
			<view class="content-tabs">
				<view 
					class="tab-item" 
					:class="{ active: activeTab === 'goods' }" 
					@click="activeTab = 'goods'"
				>
					<text>我的商品</text>
					<view class="tab-line" v-if="activeTab === 'goods'"></view>
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
			
			<!-- 商品列表 -->
			<view class="content-section" v-if="activeTab === 'goods'">
				<view class="empty-state" v-if="goodsList.length === 0 && !goodsLoadingMore">
					<image src="/static/empty.png" class="empty-icon"></image>
					<text class="empty-text">暂无发布商品</text>
					<view class="empty-buttons">
						<button class="empty-btn" @click="goToShop">去逛逛</button>
						<button class="empty-btn primary" @click="goToPublish">发布商品</button>
					</view>
				</view>
				
				<view class="goods-list" v-else>
					<view class="goods-item" v-for="(item, index) in goodsList" :key="index" @click="goToGoodsDetail(item.id)">
						<image :src="item.imageUrl && item.imageUrl.length > 0 ? item.imageUrl[0] : '/static/default-goods.png'" mode="aspectFill" class="goods-image"></image>
						<view class="goods-info">
							<text class="goods-title">{{ item.shopName }}</text>
							<text class="goods-intro" v-if="item.introduce">{{ item.introduce }}</text>
							<text class="goods-price">¥{{ item.price }}</text>
							<view class="goods-status">
								<text :class="['status-tag', item.isOnline ? 'online' : 'offline']">{{ item.isOnline ? '出售中' : '已下架' }}</text>
								<text class="goods-date">{{ formatDate(item.createTime) }}</text>
							</view>
						</view>
					</view>
					
					<!-- 加载更多 -->
					<view class="load-more" @click="loadMoreGoods" v-if="goodsList.length > 0 && goodsHasMore">
						<text class="load-text">{{ goodsLoadingMore ? '加载中...' : '加载更多' }}</text>
					</view>
					
					<view class="no-more" v-if="goodsList.length > 0 && !goodsHasMore">
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
	username: '',
	role: '',
	avatar: '',
	phone: '',
	backgroundImage: '',
	avatarFrameUrl: '',
	level: 1
})

// 活动标签页
const activeTab = ref('goods')

// 商品列表和帖子列表
const goodsList = ref([])
const postsList = ref([])

// 分页参数
const goodsPage = ref(1)
const postsPage = ref(1)
const pageSize = 10
const goodsLoadingMore = ref(false)
const postsLoadingMore = ref(false)
const goodsHasMore = ref(true)
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
			// 获取到用户信息后加载商品和帖子
			if (activeTab.value === 'goods') {
				getUserGoods()
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

// 获取用户发布的商品
const getUserGoods = () => {
	if (!userInfo.value.id || !goodsHasMore.value) return
	
	goodsLoadingMore.value = true
	uni.showLoading({
		title: '加载中...'
	})
	
	request('/shop/getShopByUserId', {
		method: 'GET',
		data: {
			pageNum: goodsPage.value,
			userId: userInfo.value.id
		}
	}).then(res => {
		uni.hideLoading()
		goodsLoadingMore.value = false
		
		if (res.code === 200) {
			const records = res.data || []
			
			if (goodsPage.value === 1) {
				goodsList.value = records
			} else {
				goodsList.value = [...goodsList.value, ...records]
			}
			
			// 判断是否还有更多数据
			if (records.length < pageSize) {
				goodsHasMore.value = false
			}
			
			console.log('获取商品成功:', goodsList.value)
		} else {
			uni.showToast({
				title: res.msg || '获取商品失败',
				icon: 'none'
			})
		}
	}).catch((err) => {
		console.error('获取商品失败:', err)
		uni.hideLoading()
		goodsLoadingMore.value = false
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

// 加载更多商品
const loadMoreGoods = () => {
	if (goodsLoadingMore.value || !goodsHasMore.value) return
	goodsPage.value++
	getUserGoods()
}

// 加载更多帖子
const loadMorePosts = () => {
	if (postsLoadingMore.value || !postsHasMore.value) return
	postsPage.value++
	getUserPosts()
}

// 跳转到商品详情
const goToGoodsDetail = (id) => {
	uni.navigateTo({
		url: `/pages/shop/detail?id=${id}`
	})
}

// 跳转到商城首页
const goToShop = () => {
	uni.switchTab({
		url: '/pages/shop/index'
	})
}

// 跳转到发布商品页面
const goToPublish = () => {
	uni.navigateTo({
		url: '/pages/shop/publish'
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
	goodsPage.value = 1
	postsPage.value = 1
	goodsHasMore.value = true
	postsHasMore.value = true
	
	// 重新获取数据
	getUserInfo()
	
	setTimeout(() => {
		refreshing.value = false
	}, 1000)
}

// 触底加载更多
const onReachBottom = () => {
	if (activeTab.value === 'goods') {
		loadMoreGoods()
	} else {
		loadMorePosts()
	}
}

// 监听标签页切换
watch(activeTab, (newVal) => {
	// 每次切换标签都重置一下数据，避免看到旧数据
	if (newVal === 'goods') {
		if (goodsList.value.length === 0) {
			goodsPage.value = 1
			goodsHasMore.value = true
			getUserGoods()
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
	background-color: #f5f7fa;
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
	background-color: #fff;
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
}

.nav-right {
	margin-left: auto;
}

/* 用户信息卡片 */
.user-card {
	position: relative;
	background-color: #fff;
	border-radius: 0 0 12rpx 12rpx;
	padding: 140rpx 30rpx 40rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
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

/* 内容标签页 */
.content-tabs {
	display: flex;
	background-color: #fff;
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
	color: #666;
	font-weight: 500;
}

.tab-item.active text {
	color: #333;
	font-weight: bold;
}

.tab-line {
	position: absolute;
	bottom: 0;
	width: 60rpx;
	height: 6rpx;
	background-color: #58d3ac;
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
	color: #999;
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
	background-color: #f5f5f5;
	color: #666;
	border: none;
	line-height: 1.5;
}

.empty-btn.primary {
	background-color: #58d3ac;
	color: #fff;
}

/* 商品列表 */
.goods-list {
	padding: 10rpx 0;
}

.goods-item {
	display: flex;
	padding: 20rpx;
	background-color: #fff;
	border-radius: 12rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.04);
}

.goods-image {
	width: 160rpx;
	height: 160rpx;
	border-radius: 8rpx;
	margin-right: 20rpx;
	object-fit: cover;
}

.goods-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.goods-title {
	font-size: 30rpx;
	font-weight: 500;
	color: #333;
	margin-bottom: 6rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
}

.goods-intro {
	font-size: 26rpx;
	color: #666;
	margin-bottom: 6rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
}

.goods-price {
	font-size: 32rpx;
	font-weight: bold;
	color: #58d3ac;
	margin-bottom: 6rpx;
}

.goods-status {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.status-tag {
	font-size: 24rpx;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
}

.status-tag.online {
	background-color: rgba(88, 211, 172, 0.1);
	color: #58d3ac;
}

.status-tag.offline {
	background-color: rgba(0, 0, 0, 0.05);
	color: #999;
}

.goods-date {
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