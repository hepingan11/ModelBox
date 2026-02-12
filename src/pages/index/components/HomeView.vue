<template>
	<view class="page-container" :class="themeClass">
		<scroll-view 
			scroll-y 
			class="page-scroll" 
			:show-scrollbar="false"
			refresher-enabled
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
		>
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
				<!-- 最新活动广告区域 -->
				<view class="content-section latest-activities">
					<view class="section-header">
						<text class="section-title">最新活动</text>
						
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

				<!-- 最新项目展示 -->
				<view class="content-section latest-projects">
					<view class="section-header">
						<text class="section-title">最新项目</text>
						
					</view>
					
					<view class="projects-loading" v-if="isLoadingProjects">
						<view class="loading-animation"></view>
						<text class="loading-text">加载中...</text>
					</view>
					
					<view v-else class="latest-projects-content">
						<view class="projects-list" v-if="projectList.length > 0">
							<view 
								class="project-item" 
								v-for="project in projectList" 
								:key="project.projectId"
								@click="goToProjectDetail(project.projectId)"
							>
								<!-- 项目图片区域 -->
								<view class="project-images" v-if="project.imageUrlList && project.imageUrlList.length > 0">
									<image 
										v-for="(img, imgIdx) in project.imageUrlList.slice(0, 3)" 
										:key="imgIdx"
										:src="img" 
										class="project-image" 
										mode="aspectFill"
									></image>
								</view>
								<image 
									v-else
									src="/static/default-project.png" 
									class="project-image project-image-single" 
									mode="aspectFill"
								></image>
								
								<!-- 项目信息 -->
								<view class="project-info">
									<text class="project-name">{{ project.projectName }}</text>
									<text class="project-intro" v-if="project.introduce">{{ project.introduce }}</text>
									
									<!-- 项目元信息 -->
									<view class="project-meta">
										<view class="project-city" v-if="project.city">
											<image src="/static/icon/local.png" class="meta-icon"></image>
											<text>{{ project.city }}</text>
										</view>
										<text class="project-age" v-if="project.ageRequirement"> {{ project.ageRequirement }}</text>
									</view>
									
									<!-- 成员头像 -->
									<view v-if="project.memberList && project.memberList.length > 0" class="project-members">
										<image 
											v-for="(member, idx) in project.memberList.slice(0, 3)" 
											:key="idx"
											:src="member.avatar || '/static/default-avatar.png'" 
											class="member-avatar"
										></image>
										<text v-if="project.memberList.length > 3" class="member-count">
											+{{ project.memberList.length - 3 }}
										</text>
									</view>
								</view>
							</view>
						</view>
						
						<!-- 空数据状态 -->
						<view class="empty-projects" v-if="projectList.length === 0">
							<text class="empty-text">暂无项目</text>
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
			<!-- 底部占位，防止被悬浮 TabBar 遮挡 -->
			<view style="height: 180rpx;"></view>
		</scroll-view>
	</view>
</template>

<script setup>
  import { ref, onMounted } from 'vue'
  import request from '@/utils/request.js'
  import { useTheme } from '@/hooks/useTheme'

  const { themeClass } = useTheme()

  // 轮播图图片
  const banners = ref([])

  // 活动广告列表
  const activityList = ref([])
  // 活动页码
  const activityPageNum = ref(1)
  // 活动加载状态
  const isLoadingActivities = ref(true)
  
  // 下拉刷新状态
  const refreshing = ref(false)
  
  // 活动通知控制
  const showActivity = ref(true)
  
  // 项目列表
  const projectList = ref([])
  // 项目加载状态
  const isLoadingProjects = ref(true)
  
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
  
  // 下拉刷新
  const onRefresh = async () => {
  	refreshing.value = true
  	
  	try {
  		// 重新获取所有数据
  		await Promise.all([
  			getCarouselList(),
  			getActivityList(),
  			getProjectList(),
  			getBoundSchool(),
  			getHomePoster()
  		])
  		
  	} catch (error) {
  		console.error('刷新失败:', error)
  	} finally {
  		refreshing.value = false
  	}
  }
  
  // 页面加载时的动画效果和数据获取
  onMounted(() => {
  	// 获取轮播图数据
  	getCarouselList()
  	// 获取活动广告数据
  	getActivityList()
  	// 获取项目列表数据
  	getProjectList()
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

// 获取项目列表
const getProjectList = async () => {
	isLoadingProjects.value = true
	
	try {
		const res = await request('/project/list', {
			method: 'GET',
			data: {
				pageNum: 1,
				projectName: '',
				city: '',
				projectFieldId: 0,
				ageRequirement: ''
			}
		})
		
		if (res.code === 200) {
			// 只显示前6个项目
			projectList.value = (res.data.records || []).slice(0, 6)
		} else {
			console.error('获取项目列表失败:', res.msg)
		}
	} catch (error) {
		console.error('获取项目列表失败:', error)
	} finally {
		isLoadingProjects.value = false
	}
}

// 跳转到项目详情
const goToProjectDetail = (projectId) => {
	uni.navigateTo({
		url: `/pages/project/detail?id=${projectId}`
	})
}

// 查看更多项目
const viewMoreProjects = () => {
	uni.navigateTo({
		url: '/pages/project/list'
	})
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
	uni.navigateTo({ url: '/pages/forum/forum' })
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
	background: linear-gradient(180deg, var(--dColor2) 0%, var(--bgColor1) 40%, var(--bgColor2) 100%);
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
	border-radius: 0 0 40rpx 40rpx;
	overflow: hidden;
}

.swiper-img {
	width: 100%;
	height: 100%;
	object-fit: cover;
	animation: zoomIn 0.6s ease-out;
}

.swiper-gradient-mask {
	position: absolute;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 150rpx;
	background: linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,0.3));
	pointer-events: none;
	border-radius: 0 0 40rpx 40rpx;
}

/* 内容悬浮区域 */
.overlap-content {
	position: relative;
	margin-top: -60rpx;
	z-index: 10;
	padding: 0 24rpx 40rpx;
}

/* 内容区块 */
.content-section {
	margin-bottom: 30rpx;
	background: var(--bgColor2);
	border-radius: 20rpx;
	padding: 30rpx 24rpx;
	box-shadow: 0 4rpx 12rpx var(--bgboxShadowColor1);
	position: relative;
	overflow: hidden;
}

.content-section::before {
	content: '';
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	height: 4rpx;
	background: linear-gradient(90deg, #00A872 0%, #4CAF50 50%, #8BC34A 100%);
}

/* 公共头部样式 */
.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
	padding-left: 10rpx;
	border-left: 8rpx solid var(--themeColor1);
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: var(--textColor1);
	position: relative;
	padding-left: 20rpx;
}

.section-title::before {
	content: '';
	position: absolute;
	left: 0;
	top: 50%;
	transform: translateY(-50%);
	width: 8rpx;
	height: 32rpx;
	background: linear-gradient(180deg, #00A872 0%, #4CAF50 100%);
	border-radius: 4rpx;
}

/* 项目样式 */
.latest-projects-content {
	margin-top: 20rpx;
}

.projects-list {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.project-item {
	display: flex;
	flex-direction: column;
	width: 100%;
	background: linear-gradient(135deg, #ffffff 0%, #fafffe 100%);
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 8rpx 24rpx rgba(76, 175, 80, 0.12);
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	border: 1rpx solid rgba(76, 175, 80, 0.1);
}

.project-item:active {
	transform: translateY(-4rpx);
	box-shadow: 0 12rpx 32rpx rgba(76, 175, 80, 0.18);
}

.project-images {
	display: flex;
	gap: 4rpx;
	width: 100%;
	height: 200rpx;
	position: relative;
}

.project-image {
	flex: 1;
	height: 200rpx;
	object-fit: cover;
}

.project-image-single {
	width: 100%;
	height: 200rpx;
}

.project-info {
	padding: 24rpx;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, #ffffff 100%);
}

.project-name {
	font-size: 30rpx;
	font-weight: bold;
	color: var(--textColor1);
	margin-bottom: 8rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	line-clamp: 1;
	overflow: hidden;
}

.project-intro {
	font-size: 24rpx;
	color: var(--textColor3);
	margin-bottom: 16rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	line-clamp: 2;
	overflow: hidden;
	line-height: 1.4;
}

.project-meta {
	display: flex;
	align-items: center;
	font-size: 22rpx;
	color: var(--textColor3);
	margin-bottom: 16rpx;
}

.project-city,
.project-age {
	display: flex;
	align-items: center;
	gap: 6rpx;
}

.meta-icon {
	width: 24rpx;
	height: 24rpx;
}

.project-members {
	display: flex;
	align-items: center;
	gap: -12rpx;
	margin-top: 8rpx;
}

.member-avatar {
	width: 48rpx;
	height: 48rpx;
	border-radius: 50%;
	border: 3rpx solid #fff;
}

.member-count {
	margin-left: 20rpx;
	font-size: 24rpx;
	color: #999;
}

/* 活动样式 */
.activity-item {
	display: inline-block;
	width: 480rpx;
	margin-right: 24rpx;
	vertical-align: top;
	background: linear-gradient(135deg, #ffffff 0%, #f8fffe 100%);
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 8rpx 24rpx rgba(0, 168, 114, 0.12);
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	border: 1rpx solid rgba(0, 168, 114, 0.08);
}

.activity-item:active {
	transform: translateY(-4rpx);
	box-shadow: 0 12rpx 32rpx rgba(0, 168, 114, 0.18);
}

.activity-image {
	width: 100%;
	height: 280rpx;
	object-fit: cover;
}

.activity-content {
	padding: 24rpx;
	white-space: normal;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, #ffffff 100%);
}

.activity-title {
	font-size: 28rpx;
	font-weight: bold;
	color: var(--textColor1);
	margin-bottom: 10rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	line-clamp: 1;
	overflow: hidden;
}

.activity-desc {
	font-size: 24rpx;
	color: var(--textColor3);
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	line-clamp: 2;
	overflow: hidden;
	line-height: 1.4;
}

.activities-scroll {
	white-space: nowrap;
	padding: 10rpx 0;
}

/* 加载/空状态 */
.loading-animation {
	width: 50rpx;
	height: 50rpx;
	border: 5rpx solid rgba(0, 168, 114, 0.1);
	border-top: 5rpx solid #00A872;
	border-radius: 50%;
	animation: spin 1s linear infinite;
	margin-bottom: 20rpx;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text, .empty-text {
	font-size: 26rpx;
	color: var(--textColor3);
	margin-top: 10rpx;
}

.empty-state, .empty-activity, .empty-projects, .activities-loading, .projects-loading {
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
	width: 100%;
	height: 80vh;
	background: var(--bgColor2);
	border-radius: 40rpx 40rpx 0 0;
	display: flex;
	flex-direction: column;
	position: absolute;
	bottom: 0;
	left: 0;
	/* transform: translateY(100%); */
	transition: transform 0.3s cubic-bezier(0.19, 1, 0.22, 1);
}

.popup-mask.popup-show .popup-content {
	transform: scale(1);
}

.popup-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
	padding: 30rpx;
}

.popup-title {
	font-size: 34rpx;
	font-weight: bold;
	color: var(--textColor1);
}

.close-icon {
	font-size: 40rpx;
	color: var(--textColor3);
	padding: 10rpx;
}

.search-box {
	display: flex;
	align-items: center;
	padding: 0 30rpx;
	margin-bottom: 20rpx;
}

.search-input {
	flex: 1;
	height: 72rpx;
	background: var(--bgColor1);
	border-radius: 36rpx;
	padding: 0 30rpx;
	font-size: 28rpx;
	margin-right: 20rpx;
	color: var(--textColor1);
}

.search-btn {
	width: 120rpx;
	height: 72rpx;
	background: var(--themeColor1);
	color: var(--themeTextColor);
	font-size: 28rpx;
	border-radius: 36rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.school-list {
	height: 700rpx;
	padding-left: 30rpx;
	padding-right: 30rpx;
}

.school-item {
	padding: 30rpx 0;
	border-bottom: 1rpx solid var(--borderColor);
}

.school-item:last-child {
	border-bottom: none;
}

.school-item-name {
	font-size: 30rpx;
	color: var(--textColor1);
}

.empty-result {
	text-align: center;
	padding: 40rpx 0;
	color: #999;
	font-size: 26rpx;
}
</style>
