<template>
	<view class="home-container">
		<!-- 背景装饰元素 -->
		<view class="bg-decoration">
			<view class="floating-circle circle-1"></view>
			<view class="floating-circle circle-2"></view>
			<view class="floating-circle circle-3"></view>
			<view class="floating-square square-1"></view>
			<view class="floating-square square-2"></view>
		</view>
		
		<!-- 顶部轮播图 -->
		<view class="swiper-section">
			<swiper
				class="home-swiper"
				:indicator-dots="true"
				:autoplay="true"
				:interval="3000"
				:duration="500"
				circular
			>
				<swiper-item v-for="(item, idx) in banners" :key="idx">
					<image :src="item" class="swiper-image" mode="aspectFill" />
				</swiper-item>
			</swiper>
		</view>
		
		<!-- 学校绑定区域 -->
		<view class="school-section" @click="openSchoolPopup">
			<view class="school-info">
				<text class="school-icon">📍</text>
				<text class="school-name">{{ currentSchool ? currentSchool.schoolName : '点击绑定学校' }}</text>
			</view>
			<image src="/static/icon/arrow-right.png" class="school-arrow-icon" mode="aspectFit"></image>
		</view>
		
		<!-- 外卖跑腿业务模块 -->
		<view class="delivery-section">
			<view class="delivery-card take-order" @click="navigateToDeliveryTake">
				<view class="delivery-content">
					<text class="delivery-title">我要接单</text>
					<text class="delivery-desc">赚取配送费</text>
				</view>
				<image src="/static/shop.png" class="delivery-icon" mode="aspectFit"></image>
			</view>
			<view class="delivery-card place-order" @click="navigateToDeliveryOrder">
				<view class="delivery-content">
					<text class="delivery-title">我要下单</text>
					<text class="delivery-desc">帮我送东西</text>
				</view>
				<image src="/static/shop-selected.png" class="delivery-icon" mode="aspectFit"></image>
			</view>
		</view>
		
		<!-- 功能工具栏 -->
		<!-- <view class="tools-section">
			<scroll-view class="tools-scroll" scroll-x>
				<view class="tools-grid">
					<view class="tool-item" @click="navigateToTool('aiChat')">
						<view class="tool-icon-container ai-chat">
							<image src="/static/icon/ai-chat.png" class="tool-icon"></image>
						</view>
						<text class="tool-name">AI问答</text>
					</view>
					
					<view class="tool-item" @click="navigateToTool('aiDraw')">
						<view class="tool-icon-container ai-draw">
							<image src="/static/icon/ai-draw.png" class="tool-icon"></image>
						</view>
						<text class="tool-name">AI绘画</text>
					</view>
					
					<view class="tool-item" @click="navigateToTool('campusPickup')">
						<view class="tool-icon-container campus-pickup">
							<image src="/static/icon/campus-pickup.png" class="tool-icon"></image>
						</view>
						<text class="tool-name">校园代取</text>
					</view>
					
					<view class="tool-item" @click="navigateToTool('findPartner')">
						<view class="tool-icon-container find-partner">
							<image src="/static/icon/find-partner.png" class="tool-icon"></image>
						</view>
						<text class="tool-name">寻找搭子</text>
					</view>
					
					<view class="tool-item" @click="navigateToTool('classSub')">
						<view class="tool-icon-container class-sub">
							<image src="/static/icon/class-sub.png" class="tool-icon"></image>
						</view>
						<text class="tool-name">代课代会</text>
					</view>
					
					<view class="tool-item" @click="navigateToTool('homeworkHelp')">
						<view class="tool-icon-container homework-help">
							<image src="/static/icon/homework-help.png" class="tool-icon"></image>
						</view>
						<text class="tool-name">作业帮帮</text>
					</view>
				</view>
			</scroll-view>
		</view> -->
		
		<!-- 最新商品展示 -->
		<view class="latest-goods">
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
		
		<!-- 最新活动广告区域 -->
		<view class="latest-activities">
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
							<view class="activity-header">
								<text class="activity-title">{{ item.title }}</text>
								<view class="activity-tag">{{ item.type }}</view>
							</view>
							<text class="activity-desc">{{ item.content }}</text>
							<view class="activity-footer">
								<text class="activity-time">{{ formatTime(item.createdTime) }}</text>
								<view class="activity-url-indicator" v-if="item.url">
									<text>查看详情</text>
									<text class="arrow-icon">→</text>
								</view>
							</view>
						</view>
					</view>
					
					<!-- 无数据状态 -->
					<view class="empty-activity" v-if="activityList.length === 0">
						<text class="empty-text">暂无活动</text>
					</view>
				</scroll-view>
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

		<!-- 首页海报弹窗 -->
		<view class="poster-mask" v-if="showHomePoster" @click.stop @touchmove.stop.prevent>
			<view class="poster-wrapper">
				<view class="poster-header">
					<view class="poster-info">
						<text class="poster-title">{{ homePosterData?.title || '通知' }}</text>
						<text class="poster-subtitle">{{ homePosterData?.content || '点击查看详情' }}</text>
					</view>
					<view class="poster-close" @click="closeHomePoster">×</view>
				</view>
				<image 
					:src="homePosterData?.image" 
					class="poster-image" 
					mode="widthFix" 
					:show-menu-by-longpress="true"
					@click="onPosterClick"
				></image>
				<view class="poster-footer">
					<text>手机微信页面可长按图片扫一扫</text>
				</view>
			</view>
		</view>
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
  			banners.value = (res.data || []).map(item => item.url)
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
				url: "/pages/forum/detail?id=1"
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
	
	// 工具导航逻辑，后续开发时取消注释
	/*
	switch(tool) {
		case 'aiChat':
			uni.navigateTo({ url: '/pages/tools/ai-chat' })
			break
		case 'aiDraw':
			uni.navigateTo({ url: '/pages/tools/ai-draw' })
			break
		case 'campusPickup':
			uni.navigateTo({ url: '/pages/tools/campus-pickup' })
			break
		case 'findPartner':
			uni.navigateTo({ url: '/pages/tools/find-partner' })
			break
		case 'classSub':
			uni.navigateTo({ url: '/pages/tools/class-sub' })
			break
		case 'homeworkHelp':
			uni.navigateTo({ url: '/pages/tools/homework-help' })
			break
	}
	*/
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
.home-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #f5f7fa 0%, #e4f2fb 50%, #f5f7fa 100%);
	display: flex;
	flex-direction: column;
	position: relative;
}

.school-section {
	margin: 0 24rpx 20rpx;
	padding: 20rpx 30rpx;
	background: rgba(255, 255, 255, 0.9);
	border-radius: 16rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	position: relative;
	z-index: 1;
}

.school-info {
	display: flex;
	align-items: center;
}

.school-icon {
	margin-right: 16rpx;
	font-size: 32rpx;
}

.school-name {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.school-arrow-icon {
	width: 32rpx;
	height: 32rpx;
	opacity: 0.5;
}

/* 弹窗动画 */
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
	line-height: 1;
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

.school-item:last-child {
	border-bottom: none;
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

.bg-decoration {
	position: absolute;
	width: 100%;
	height: 100%;
	z-index: 0;
	overflow: hidden;
}

.floating-circle {
	position: absolute;
	border-radius: 50%;
	opacity: 0.4;
}

.circle-1 {
	width: 300rpx;
	height: 300rpx;
	background: linear-gradient(45deg, #baeb34, rgba(186, 235, 52, 0.3));
	top: -100rpx;
	left: -100rpx;
	animation: float 15s infinite ease-in-out;
}

.circle-2 {
	width: 200rpx;
	height: 200rpx;
	background: linear-gradient(45deg, #1abc9c, rgba(26, 188, 156, 0.3));
	bottom: 20%;
	right: -50rpx;
	animation: float 12s infinite ease-in-out reverse;
}

.circle-3 {
	width: 150rpx;
	height: 150rpx;
	background: linear-gradient(45deg, #9b59b6, rgba(155, 89, 182, 0.3));
	bottom: 10%;
	left: 10%;
	animation: float 18s infinite ease-in-out;
}

.floating-square {
	position: absolute;
	border-radius: 15rpx;
	opacity: 0.3;
	transform: rotate(45deg);
}

.square-1 {
	width: 100rpx;
	height: 100rpx;
	background: linear-gradient(45deg, #3498db, rgba(52, 152, 219, 0.3));
	top: 30%;
	right: 10%;
	animation: rotate 20s infinite linear;
}

.square-2 {
	width: 80rpx;
	height: 80rpx;
	background: linear-gradient(45deg, #e74c3c, rgba(231, 76, 60, 0.3));
	top: 60%;
	left: 5%;
	animation: rotate 15s infinite linear reverse;
}

@keyframes float {
	0%, 100% {
		transform: translateY(0) scale(1);
	}
	50% {
		transform: translateY(-30rpx) scale(1.05);
	}
}

@keyframes rotate {
	0% {
		transform: rotate(0deg);
	}
	100% {
		transform: rotate(360deg);
	}
}

.swiper-section {
	width: 100vw;
	height: 340rpx;
	margin-top: 10rpx;
	margin-bottom: 30rpx;
	position: relative;
	z-index: 1;
}

.delivery-section {
	margin: 0 24rpx 30rpx;
	display: flex;
	justify-content: space-between;
	position: relative;
	z-index: 1;
}

.delivery-card {
	width: 48%;
	height: 160rpx;
	border-radius: 20rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 0 30rpx;
	box-sizing: border-box;
	box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.05);
	transition: transform 0.2s ease;
}

.delivery-card:active {
	transform: scale(0.98);
}

.take-order {
	background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%);
}

.place-order {
	background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
}

.delivery-content {
	display: flex;
	flex-direction: column;
}

.delivery-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 8rpx;
}

.delivery-desc {
	font-size: 24rpx;
	color: #666;
}

.delivery-icon {
	width: 80rpx !important;
	height: 80rpx !important;
	opacity: 0.8;
	flex-shrink: 0;
}

.home-swiper {
	width: 100vw;
	height: 340rpx;
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 10rpx 20rpx rgba(0, 0, 0, 0.1);
}

.swiper-image {
	width: 100vw;
	height: 340rpx;
	object-fit: cover;
}

.feature-section {
	display: flex;
	justify-content: space-around;
	align-items: center;
	padding: 30rpx 0 20rpx 0;
	background: rgba(255, 255, 255, 0.8);
	backdrop-filter: blur(10rpx);
	border-radius: 24rpx;
	margin: 0 24rpx 30rpx 24rpx;
	box-shadow: 0 10rpx 20rpx rgba(0, 0, 0, 0.05);
	position: relative;
	z-index: 1;
	transform: translateY(0);
	transition: transform 0.3s ease;
}

.feature-section:active {
	transform: translateY(2rpx);
}

.feature-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	transition: transform 0.2s ease;
}

.feature-item:active {
	transform: scale(0.95);
}

.feature-icon {
	width: 80rpx;
	height: 80rpx;
	margin-bottom: 10rpx;
	transition: transform 0.3s ease;
}

.feature-item:hover .feature-icon {
	transform: translateY(-5rpx);
}

.feature-title {
	font-size: 28rpx;
	color: #1abc9c;
	font-weight: 500;
}

.recommend-section {
	margin: 0 24rpx;
	position: relative;
	z-index: 1;
}

.recommend-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.recommend-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	position: relative;
}

.recommend-title::after {
	content: '';
	position: absolute;
	bottom: -8rpx;
	left: 0;
	width: 60rpx;
	height: 6rpx;
	background: linear-gradient(90deg, #baeb34, #1abc9c);
	border-radius: 3rpx;
}

.recommend-more {
	font-size: 26rpx;
	color: #1abc9c;
	padding: 6rpx 12rpx;
	border-radius: 20rpx;
	background-color: rgba(26, 188, 156, 0.1);
}

.recommend-scroll {
	display: flex;
	flex-direction: row;
	overflow-x: auto;
	white-space: nowrap;
	padding: 10rpx 0;
}

.recommend-card {
	display: inline-block;
	width: 260rpx;
	margin-right: 24rpx;
	background: rgba(255, 255, 255, 0.9);
	border-radius: 18rpx;
	box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.08);
	overflow: hidden;
	transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.recommend-card:active {
	transform: scale(0.97);
	box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.08);
}

.recommend-img {
	width: 260rpx;
	height: 160rpx;
	object-fit: cover;
}

.recommend-info {
	padding: 18rpx 16rpx 12rpx 16rpx;
}

.recommend-name {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.recommend-desc {
	font-size: 24rpx;
	color: #999;
	margin-top: 6rpx;
}

/* 最新商品展示 */
.latest-goods {
	margin: 30rpx 24rpx;
	position: relative;
	z-index: 1;
}

.latest-goods-content {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.goods-loading {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40rpx 0;
}

.goods-list {
	width: 100%;
}

.goods-row {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	margin-bottom: 20rpx;
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

/* 最新活动广告区域 */
.latest-activities {
	margin: 30rpx 24rpx;
	position: relative;
	z-index: 1;
}

.activities-loading {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40rpx 0;
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

.loading-text {
	font-size: 24rpx;
	color: #999;
}

.activities-scroll {
	white-space: nowrap;
	padding: 10rpx 0;
}

.activity-item {
	display: inline-block;
	width: 300rpx;
	margin-right: 20rpx;
	background: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
	transition: transform 0.3s ease;
	position: relative;
}

.activity-item:active {
	transform: scale(0.97);
}

.activity-image {
	width: 300rpx;
	height: 180rpx;
	object-fit: cover;
}

.activity-item .activity-content {
	padding: 16rpx;
	display: block;
	white-space: normal;
}

.activity-item .activity-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10rpx;
}

.activity-item .activity-title {
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
	margin: 0;
	flex: 1;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.activity-item .activity-tag {
	font-size: 20rpx;
	color: #1abc9c;
	background-color: rgba(26, 188, 156, 0.1);
	padding: 2rpx 10rpx;
	border-radius: 10rpx;
	margin: 0 0 0 10rpx;
}

.activity-item .activity-desc {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
	height: 68rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	margin-bottom: 10rpx;
}

.activity-item .activity-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	border-top: 1rpx solid #f0f0f0;
	padding-top: 10rpx;
	margin-top: 6rpx;
}

.activity-item .activity-time {
	font-size: 22rpx;
	color: #999;
}

.activity-item .activity-url-indicator {
	font-size: 22rpx;
	color: #1abc9c;
	display: flex;
	align-items: center;
}

.empty-activity {
	padding: 40rpx 0;
	text-align: center;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 功能工具栏 */
.tools-section {
	margin: 20rpx 24rpx 30rpx;
	position: relative;
	z-index: 2;
}

.tools-scroll {
	width: 100%;
}

.tools-grid {
	display: flex;
	flex-wrap: nowrap;
	padding: 10rpx 0;
}

.tool-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	min-width: 160rpx;
	margin-right: 20rpx;
	transition: transform 0.2s ease;
}

.tool-item:active {
	transform: scale(0.95);
}

.tool-icon-container {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 12rpx;
	box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.08);
}

.tool-icon {
	width: 60rpx;
	height: 60rpx;
}

.tool-name {
	font-size: 24rpx;
	color: #333;
	font-weight: 500;
}

/* 工具图标背景颜色 */
.ai-chat {
	background: linear-gradient(135deg, #58d3ac, #8fe3c3);
}

.ai-draw {
	background: linear-gradient(135deg, #dafcd4, #ffffff);
	border: 1px solid #f0f0f0;
}

.campus-pickup {
	background: linear-gradient(135deg, #58d3ac, #8fe3c3);
}

.find-partner {
	background: linear-gradient(135deg, #dafcd4, #ffffff);
	border: 1px solid #f0f0f0;
}

.class-sub {
	background: linear-gradient(135deg, #58d3ac, #8fe3c3);
}

.homework-help {
	background: linear-gradient(135deg, #dafcd4, #ffffff);
	border: 1px solid #f0f0f0;
}

/* 公共样式 */
.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
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
	font-size: 26rpx;
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

.loading-text {
	font-size: 24rpx;
	color: #999;
}

.empty-state {
	padding: 40rpx 0;
	text-align: center;
}

.empty-icon {
	width: 100rpx;
	height: 100rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 最新商品展示 */
.latest-goods {
	margin: 30rpx 24rpx;
	position: relative;
	z-index: 1;
}

.latest-goods-content {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.goods-loading {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40rpx 0;
}

.goods-list {
	width: 100%;
}

.goods-row {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	margin-bottom: 20rpx;
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

/* 最新活动广告区域 */
.latest-activities {
	margin: 30rpx 24rpx;
	position: relative;
	z-index: 1;
}

.activities-loading {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40rpx 0;
}
</style>