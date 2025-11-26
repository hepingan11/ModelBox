<template>
	<view class="page-container">
		<scroll-view scroll-y class="page-scroll" :show-scrollbar="false">
			<!-- 顶部大轮播图 -->
			<view class="swiper-container">
				<swiper
					class="main-swiper"
					:indicator-dots="true"
					:autoplay="true"
					:interval="3000"
					:duration="500"
					circular
					indicator-color="rgba(255,255,255,0.5)"
					indicator-active-color="#ffffff"
				>
					<swiper-item v-for="(item, idx) in banners" :key="idx" @click="onBannerClick(item)">
						<image :src="item.url" class="swiper-img" mode="aspectFill" />
					</swiper-item>
				</swiper>
				<!-- 底部渐变遮罩 -->
				<view class="swiper-gradient-mask"></view>
			</view>

			<!-- 悬浮内容区域 -->
			<view class="overlap-content">
				<!-- 主要功能卡片 -->
				<view class="main-card">
					<!-- 学校定位 -->
					<view class="location-header" @click="openSchoolPopup">
						<view class="location-left">
							<text class="location-icon">📍</text>
							<text class="location-text">{{ currentSchool ? currentSchool.schoolName : '点击绑定学校' }}</text>
						</view>
						<text class="location-arrow">切换</text>
					</view>

					<!-- 核心业务入口 (接单/下单) -->
					<view class="core-actions">
						<view class="action-item" @click="navigateToDeliveryTake">
							<view class="icon-box take-bg">
								<image src="/static/shop.png" class="action-icon"></image>
							</view>
							<view class="action-info">
								<text class="action-title">我要接单</text>
								<text class="action-desc">赚取配送费</text>
							</view>
						</view>
						
						<view class="divider"></view>
						
						<view class="action-item" @click="navigateToDeliveryOrder">
							<view class="icon-box place-bg">
								<image src="/static/shop-selected.png" class="action-icon"></image>
							</view>
							<view class="action-info">
								<text class="action-title">我要下单</text>
								<text class="action-desc">帮我送东西</text>
							</view>
						</view>
					</view>

					<!-- 工具栏网格 -->
					<view class="tools-grid-section">
						<view class="tool-item" @click="navigateToTool('aiChat')">
							<image src="/static/icon/ai-chat.png" class="tool-img"></image>
							<text class="tool-text">AI问答</text>
						</view>
						<view class="tool-item" @click="navigateToTool('campusPickup')">
							<image src="/static/icon/campus-pickup.png" class="tool-img"></image>
							<text class="tool-text">校园代取</text>
						</view>
						<view class="tool-item" @click="navigateToTool('findPartner')">
							<image src="/static/icon/find-partner.png" class="tool-img"></image>
							<text class="tool-text">寻找搭子</text>
						</view>
						<view class="tool-item" @click="navigateToTool('classSub')">
							<image src="/static/icon/class-sub.png" class="tool-img"></image>
							<text class="tool-text">代课代会</text>
						</view>
					</view>
				</view>

				<!-- 最新活动广告区域 -->
				<view class="content-section latest-activities">
					<view class="section-header">
						<text class="section-title">最新活动</text>
						<view class="view-more" @click="viewMoreActivities">
							<text>查看更多</text>
							<text class="arrow-icon">→</text>
						</view>
					</view>
					
					<view class="activities-loading" v-if="isLoadingActivities">
						<view class="loading-animation"></view>
						<text class="loading-text">加载中...</text>
					</view>
					
					<view v-else>
						<scroll-view class="activities-scroll" scroll-x>
							<view 
								class="activity-item" 
								v-for="(item, index) in activityList" 
								:key="index"
								@click="goToActivityDetail(item)"
							>
								<image :src="item.image" class="activity-image" mode="aspectFill"></image>
								<view class="activity-content">
									<text class="activity-title">{{ item.title }}</text>
									<text class="activity-desc">{{ item.content }}</text>
								</view>
							</view>
							
							<!-- 无数据状态 -->
							<view class="empty-activity" v-if="activityList.length === 0">
								<text class="empty-text">暂无活动</text>
							</view>
						</scroll-view>
					</view>
				</view>

				<!-- 最新商品展示 -->
				<view class="content-section latest-goods">
					<view class="section-header">
						<text class="section-title">最新商品</text>
						<view class="view-more" @click="viewMoreGoods">
							<text>查看更多</text>
							<text class="arrow-icon">→</text>
						</view>
					</view>
					
					<view class="goods-loading" v-if="isLoadingGoods">
						<view class="loading-animation"></view>
						<text class="loading-text">加载中...</text>
					</view>
					
					<view v-else class="latest-goods-content">
						<!-- 商品列表 -->
						<view class="goods-list" v-if="latestGoodsList.length > 0">
							<view class="goods-row">
								<view 
									class="goods-item" 
									v-for="(item, index) in latestGoodsList.slice(0, 4)" 
									:key="item.id"
									@click="navigateToGoodsDetail(item.id)"
								>
									<image 
										:src="item.imageUrl && item.imageUrl.length > 0 ? item.imageUrl[0] : '/static/default-goods.png'" 
										class="goods-image" 
										mode="aspectFill"
									></image>
									<view class="goods-info">
										<text class="goods-name">{{ item.shopName }}</text>
										<text class="goods-intro" v-if="item.introduce">{{ item.introduce }}</text>
										<view class="goods-price-row">
											<text class="goods-price">¥{{ item.price }}</text>
										</view>
									</view>
								</view>
							</view>
						</view>
						
						<!-- 空数据状态 -->
						<view class="empty-state" v-if="latestGoodsList.length === 0">
							<image src="/static/empty.png" class="empty-icon"></image>
							<text class="empty-text">暂无商品</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 学校选择弹窗 -->
			<view class="popup-mask" v-if="showSchoolPopup" @click="closeSchoolPopup" :class="{ 'popup-show': showPopupAnimation }">
				<view class="popup-content" @click.stop>
					<view class="popup-header">
						<text class="popup-title">选择学校</text>
						<text class="close-icon" @click="closeSchoolPopup">×</text>
					</view>
					<view class="search-box">
						<input 
							type="text" 
							v-model="searchSchoolName" 
							placeholder="请输入学校名称搜索" 
							class="search-input"
							@input="onSearchInput"
							@confirm="searchSchools"
						/>
						<view class="search-btn" @click="searchSchools">搜索</view>
					</view>
					<scroll-view scroll-y class="school-list">
						<view 
							class="school-item" 
							v-for="school in schoolSearchResults" 
							:key="school.schoolId"
							@click="selectSchool(school)"
						>
							<text class="school-item-name">{{ school.schoolName }}</text>
						</view>
						<view v-if="hasSearched && schoolSearchResults.length === 0" class="empty-result">
							<text>未找到相关学校</text>
						</view>
					</scroll-view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
  import { ref, onMounted } from 'vue'
  import request from '@/utils/request.js'

  // 轮播图图片
  const banners = ref([])

  // 活动广告列表
  const activityList = ref([])
  // 活动页码
  const activityPageNum = ref(1)
  // 活动加载状态
  const isLoadingActivities = ref(true)

  // 最新商品列表
  const latestGoodsList = ref([])
  // 商品加载状态
  const isLoadingGoods = ref(true)
  
  // 活动通知控制
  const showActivity = ref(true)
  
  // 学校相关状态
  const currentSchool = ref(null)
  const showSchoolPopup = ref(false)
  const showPopupAnimation = ref(false)
  const searchSchoolName = ref('')
  const schoolSearchResults = ref([])
  const hasSearched = ref(false)
  
  // 首页海报
  const showHomePoster = ref(false)
  const homePosterData = ref(null)
  
  let searchTimer = null

  // 获取轮播图数据
  const getCarouselList = async () => {
  	try {
  		const res = await request('/system/carousel/list', {
  			method: 'GET'
  		})
  		
  		if (res.code === 200) {
  			banners.value = res.data || []
  		} else {
  			console.error('获取轮播图列表失败:', res.msg)
  		}
  	} catch (error) {
  		console.error('获取轮播图列表失败:', error)
  	}
  }
  
  // 页面加载时的动画效果和数据获取
  onMounted(() => {
  	// 获取轮播图数据
  	getCarouselList()
  	// 获取活动广告数据
  	getActivityList()
  	// 获取最新商品数据
  	getLatestGoods()
	// 获取已绑定的学校
	getBoundSchool()
	// 获取首页海报
	getHomePoster()
  })
  
  // 打开学校选择弹窗
  const openSchoolPopup = () => {
  	showSchoolPopup.value = true
  	setTimeout(() => {
  		showPopupAnimation.value = true
  	}, 10)
  	searchSchoolName.value = ''
  	schoolSearchResults.value = []
  	hasSearched.value = false
  }

  // 关闭学校选择弹窗
  const closeSchoolPopup = () => {
  	showPopupAnimation.value = false
  	setTimeout(() => {
  		showSchoolPopup.value = false
  	}, 300)
  }
  
  const onSearchInput = () => {
	if (searchTimer) clearTimeout(searchTimer)
	searchTimer = setTimeout(() => {
		if (!searchSchoolName.value.trim()) {
			schoolSearchResults.value = []
			hasSearched.value = false
			return
		}
		searchSchools(true)
	}, 500)
}

  // 搜索学校
  const searchSchools = async (isAuto = false) => {
  	if (!searchSchoolName.value.trim()) {
  		if (!isAuto) {
  			uni.showToast({
  				title: '请输入学校名称',
  				icon: 'none'
  			})
  		}
  		return
  	}
  	
  	try {
  		const res = await request('/delivery/school/list', {
  			method: 'GET',
  			data: {
  				schoolName: searchSchoolName.value
  			}
  		})
  		
  		if (res.code === 200) {
  			schoolSearchResults.value = res.data || []
  			hasSearched.value = true
  		} else {
  			uni.showToast({
  				title: res.msg || '搜索失败',
  				icon: 'none'
  			})
  		}
  	} catch (error) {
  		console.error('搜索学校失败:', error)
  		uni.showToast({
  			title: '搜索失败，请稍后重试',
  			icon: 'none'
  		})
  	}
  }
  
  // 选择学校
  const selectSchool = (school) => {
  	currentSchool.value = school
  	uni.setStorageSync('schoolId', school.schoolId)
  	closeSchoolPopup()
  	uni.showToast({
  		title: '绑定成功',
  		icon: 'success'
  	})
  }
  
  // 获取已绑定的学校
  const getBoundSchool = async () => {
  	const schoolId = uni.getStorageSync('schoolId')
  	if (!schoolId) return
  	
  	try {
  		const res = await request('/delivery/school/getName', {
  			method: 'GET',
  			data: {
  				schoolId: schoolId
  			}
  		})
  		
  		if (res.code === 200 && res.data) {
  			currentSchool.value = res.data
  		}
  	} catch (error) {
  		console.error('获取绑定学校失败:', error)
  	}
  }
  
  // 获取首页海报
  const getHomePoster = async () => {
  	try {
  		const res = await request('/activity/hotList', {
  			method: 'GET'
  		})
  		
  		if (res.code === 200 && res.data && res.data.length > 0) {
  			// 取第一个作为海报
  			homePosterData.value = res.data[0]
  			showHomePoster.value = true
  		}
  	} catch (error) {
  		console.error('获取首页海报失败:', error)
  	}
  }
  
  const closeHomePoster = () => {
  	showHomePoster.value = false
  }
  
  const onPosterClick = () => {
  	if (homePosterData.value) {
  		closeHomePoster()
  		goToActivityDetail(homePosterData.value)
  	}
  }
  
  // 轮播图点击跳转
  const onBannerClick = (item) => {
  	if (item && item.navtoUrl) {
  		goToActivityDetail({
  			url: item.navtoUrl
  		})
  	}
  }
  
  // 获取活动广告列表
  const getActivityList = async () => {
  	isLoadingActivities.value = true
  	
  	try {
  		const res = await request('/activity/list', {
  			method: 'GET',
  			data: {
  				pageNum: activityPageNum.value
  			}
  		})
  		
  		if (res.code === 200) {
  			let list = res.data || []
  			activityList.value = list
  		} else {
  			console.error('获取活动列表失败:', res.msg)
  		}
  	} catch (error) {
  		console.error('获取活动列表失败:', error)
  	} finally {
  		isLoadingActivities.value = false
  	}
  }
  
  // 获取最新商品列表
  const getLatestGoods = async () => {
  	isLoadingGoods.value = true
	
	try {
		const res = await request('/shop/list', {
			method: 'GET',
			data: {
				pageNum: 1,
				shopName: '',
				category: ''
			}
		})
		
		if (res.code === 200) {
			latestGoodsList.value = res.data || []
		} else {
			console.error('获取最新商品失败:', res.msg)
		}
	} catch (error) {
		console.error('获取最新商品失败:', error)
	} finally {
		isLoadingGoods.value = false
	}
}

// 跳转到活动详情
const goToActivityDetail = (activity) => {
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
				url: activity.url || "/pages/forum/detail?id=1"
			})
		}
	} else {
		// 没有URL，跳转到活动详情页
		uni.navigateTo({
				url: "/pages/forum/detail?id=1"
			})
	}
}

// 跳转到活动页面
const goActivity = () => {
	uni.navigateTo({ url: '/pages/activity/index' })
}

// 查看更多活动
const viewMoreActivities = () => {
	uni.switchTab({ url: '/pages/forum/forum' })
}

// 关闭活动通知
const closeActivity = () => {
	showActivity.value = false
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

// 导航到工具页面
const navigateToTool = (tool) => {
	// 显示开发中提示
	uni.showToast({
		title: '功能开发中，敬请期待',
		icon: 'none',
		duration: 2000
	})
}

// 跳转到商品详情
const navigateToGoodsDetail = (id) => {
	uni.navigateTo({
		url: `/pages/shop/detail?id=${id}`
	})
}

// 查看更多商品
const viewMoreGoods = () => {
	uni.switchTab({ url: '/pages/shop/index' })
}

// 跳转到接单页面
const navigateToDeliveryTake = () => {
	uni.navigateTo({
		url: '/pages/delivery/delivery-orders'
	})
}

// 跳转到下单页面
const navigateToDeliveryOrder = () => {
	uni.navigateTo({
		url: '/pages/delivery/delivery-create'
	})
}

</script>

<style>
.page-container {
	width: 100%;
	min-height: 100vh;
	background-color: #f5f7fa;
}

.page-scroll {
	height: 100vh;
}

/* 轮播图区域 */
.swiper-container {
	width: 100%;
	height: 560rpx;
	position: relative;
}

.main-swiper {
	width: 100%;
	height: 100%;
}

.swiper-img {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.swiper-gradient-mask {
	position: absolute;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 120rpx;
	background: linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,0.1));
	pointer-events: none;
}

/* 内容悬浮区域 */
.overlap-content {
	position: relative;
	margin-top: -80rpx;
	z-index: 10;
	padding: 0 24rpx 40rpx;
}

/* 主要功能卡片 */
.main-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 30rpx 24rpx;
	box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
	margin-bottom: 24rpx;
}

/* 定位头 */
.location-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-bottom: 24rpx;
	border-bottom: 1rpx solid #f5f5f5;
	margin-bottom: 30rpx;
}

.location-left {
	display: flex;
	align-items: center;
	max-width: 80%;
}

.location-icon {
	font-size: 32rpx;
	margin-right: 10rpx;
}

.location-text {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.location-arrow {
	font-size: 24rpx;
	color: #999;
}

/* 核心动作 */
.core-actions {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.action-item {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 10rpx 0;
}

.action-item:active {
	opacity: 0.8;
}

.icon-box {
	width: 90rpx;
	height: 90rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 20rpx;
}

.take-bg {
	background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%);
}

.place-bg {
	background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
}

.action-icon {
	width: 50rpx;
	height: 50rpx;
}

.action-info {
	display: flex;
	flex-direction: column;
}

.action-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 6rpx;
}

.action-desc {
	font-size: 22rpx;
	color: #999;
}

.divider {
	width: 1rpx;
	height: 60rpx;
	background-color: #eee;
	margin: 0 30rpx;
}

/* 工具栏 */
.tools-grid-section {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	margin-top: 10rpx;
}

.tool-item {
	width: 20%;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-top: 20rpx;
}

.tool-item:active {
	opacity: 0.8;
}

.tool-img {
	width: 60rpx;
	height: 60rpx;
	margin-bottom: 12rpx;
}

.tool-text {
	font-size: 24rpx;
	color: #666;
}

/* 内容区块 */
.content-section {
	margin-bottom: 24rpx;
	background-color: #fff;
	border-radius: 16rpx;
}

/* 公共头部样式 */
.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	padding: 0 10rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	position: relative;
}

.section-title::after {
	content: '';
	position: absolute;
	bottom: -8rpx;
	left: 0;
	width: 60rpx;
	height: 6rpx;
	background: linear-gradient(90deg, #baeb34, #1abc9c);
	border-radius: 3rpx;
}

.view-more {
	font-size: 26rpx;
	color: #1abc9c;
	display: flex;
	align-items: center;
}

.arrow-icon {
	margin-left: 6rpx;
}

/* 商品列表样式 */
.latest-goods-content {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.goods-list {
	width: 100%;
}

.goods-row {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
}

.goods-item {
	width: 48%;
	background-color: #fff;
	border-radius: 12rpx;
	overflow: hidden;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
	margin-bottom: 20rpx;
}

.goods-image {
	width: 100%;
	height: 200rpx;
	object-fit: cover;
}

.goods-info {
	padding: 16rpx;
}

.goods-name {
	font-size: 26rpx;
	color: #333;
	line-height: 1.4;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 1;
	-webkit-box-orient: vertical;
	margin-bottom: 8rpx;
}

.goods-intro {
	font-size: 22rpx;
	color: #666;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 1;
	-webkit-box-orient: vertical;
	margin-bottom: 8rpx;
}

.goods-price-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.goods-price {
	font-size: 28rpx;
	color: #58d3ac;
	font-weight: bold;
}

/* 活动样式 */
.activity-item {
	display: inline-block;
	width: 480rpx;
	margin-right: 24rpx;
	vertical-align: top;
}

.activity-image {
	width: 100%;
	height: 280rpx;
	border-radius: 20rpx;
	object-fit: cover;
	box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.08);
}

.activity-content {
	padding: 16rpx 4rpx;
	white-space: normal;
}

.activity-title {
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
	display: block;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	margin-bottom: 8rpx;
}

.activity-desc {
	font-size: 24rpx;
	color: #999;
	line-height: 1.4;
	display: block;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.activities-scroll {
	white-space: nowrap;
	padding: 10rpx 0;
}

/* 加载/空状态 */
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

.loading-text, .empty-text {
	font-size: 24rpx;
	color: #999;
}

.empty-state, .empty-activity, .goods-loading, .activities-loading {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40rpx 0;
}

.empty-icon {
	width: 100rpx;
	height: 100rpx;
	margin-bottom: 20rpx;
}

/* 弹窗样式 */
.popup-mask {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	z-index: 999;
	display: flex;
	align-items: center;
	justify-content: center;
	opacity: 0;
	transition: opacity 0.3s ease;
	visibility: hidden;
}

.popup-mask.popup-show {
	opacity: 1;
	visibility: visible;
}

.popup-content {
	width: 600rpx;
	background-color: #fff;
	border-radius: 24rpx;
	padding: 30rpx;
	max-height: 80vh;
	display: flex;
	flex-direction: column;
	transform: scale(0.95);
	transition: transform 0.3s ease;
}

.popup-mask.popup-show .popup-content {
	transform: scale(1);
}

.popup-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.popup-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.close-icon {
	font-size: 40rpx;
	color: #999;
	padding: 10rpx;
}

.search-box {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.search-input {
	flex: 1;
	height: 72rpx;
	background-color: #f5f7fa;
	border-radius: 36rpx;
	padding: 0 30rpx;
	font-size: 28rpx;
	margin-right: 20rpx;
}

.search-btn {
	padding: 16rpx 30rpx;
	background: linear-gradient(135deg, #1abc9c, #16a085);
	color: #fff;
	border-radius: 36rpx;
	font-size: 28rpx;
}

.school-list {
	height: 700rpx;
}

.school-item {
	padding: 24rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.school-item-name {
	font-size: 28rpx;
	color: #333;
}

.empty-result {
	text-align: center;
	padding: 40rpx 0;
	color: #999;
	font-size: 26rpx;
}
</style>
