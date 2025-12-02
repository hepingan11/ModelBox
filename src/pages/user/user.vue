<template>
	<view class="user-container">
		<!-- 背景装饰元素 -->
		<view class="bg-decoration">
			<view class="floating-circle circle-1"></view>
			<view class="floating-circle circle-2"></view>
			<view class="floating-circle circle-3"></view>
			<view class="floating-square square-1"></view>
			<view class="floating-square square-2"></view>
		</view>
		
		<!-- 顶部用户信息卡片 -->
		<view class="user-card" @click="goToPersonalCenter">
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
					<view class="user-level-row">
						<!-- 每日签到按钮 -->
						<view 
							class="sign-in-btn" 
							:class="{'signed': isSigned}"
							@click.stop="handleSignIn"
						>
							<image :src="isSigned ? '/static/icon/signed.png' : '/static/icon/sign.png'" class="sign-icon"></image>
							<text class="sign-text">{{ isSigned ? '已签到' : '每日签到' }}</text>
						</view>
						
						<!-- 用户等级 -->
						<view class="user-level" :class="'level-' + (userInfo.level || 1)">
							<text class="level-text">Lv.{{ userInfo.level || 1 }}</text>
						</view>
					</view>
				</view>
				<!-- 添加退出登录图标 -->
				<view class="logout-icon" @click.stop="confirmLogout">
					<image src="/static/icon/logout.png" class="menu-icon-image"></image>
				</view>
			</view>
		</view>
		
		
		
		<!-- 账号管理 - 网格布局 -->
		<view class="section-card">
			<view class="menu-grid">
				
				<view class="grid-item" @click="editUserInfo">
					<view class="grid-icon-wrapper">
						<image src="/static/icon/info.png" class="grid-icon"></image>
					</view>
					<text class="grid-text">修改信息</text>
				</view>

				<view class="grid-item" @click="goToMyProject">
					<view class="grid-icon-wrapper">
						<image src="/static/icon/project.png" class="grid-icon"></image>
					</view>
					<text class="grid-text">我的项目</text>
				</view>

				<view class="grid-item" @click="goToMessage">
					<view class="grid-icon-wrapper">
						<image src="/static/admin/message.png" class="grid-icon"></image>
					</view>
					<text class="grid-text">我的消息</text>
				</view>

				<view class="grid-item" @click="goToMyCoupon">
					<view class="grid-icon-wrapper" >
						<image src="/static/icon/coupon.png" class="grid-icon"></image>
					</view>
					<text class="grid-text">优惠券</text>
				</view>

				<view class="grid-item" @click="goToCustomerService">
					<view class="grid-icon-wrapper">
						<image src="/static/admin/customer-service.png" class="grid-icon"></image>
					</view>
					<text class="grid-text">联系客服</text>
				</view>

				<view class="grid-item" @click="goToReport">
					<view class="grid-icon-wrapper">
						<image src="/static/icon/report.png" class="grid-icon"></image>
					</view>
					<text class="grid-text">我的举报</text>
				</view>
			
				<view class="grid-item" @click="aboutApp">
					<view class="grid-icon-wrapper">
						<image src="/static/logo.jpg" class="grid-icon"></image>
					</view>
					<text class="grid-text">关于应用</text>
				</view>

				<!-- 管理员界面入口 -->
				<view class="grid-item admin-grid-entry" v-if="isAdmin" @click="goToAdminPanel">
					<view class="grid-icon-wrapper">
						<text class="grid-icon-emoji">🔐</text>
					</view>
					<text class="grid-text">管理员界面</text>
				</view>
			</view>
		</view>
		
		<!-- 活动通知区域 -->
		<view class="activity-section">
			<view class="activity-card" @click="goActivity('community')">
				<view class="activity-content">
					<view class="activity-image-container">
						<image src="https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/%E5%96%87%E5%8F%AD%20(1).png" class="activity-icon" mode="aspectFit"></image>
					</view>
					<view class="activity-text">
						<view class="activity-tag">活动</view>
						<text class="activity-title">社区开荒激励</text>
						<text class="activity-desc">发布帖子 点赞数每月前三赢现金激励</text>
					</view>
				</view>
			</view>
		</view>

		<view class="activity-section">
			<view class="activity-card" @click="goActivity('join')">
				<view class="activity-content">
					<view class="activity-image-container">
						<image src="https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/%E5%8A%A0%E7%9B%9F.png" class="activity-icon" mode="aspectFit"></image>
					</view>
					<view class="activity-text">
						<view class="activity-tag">合作</view>
						<text class="activity-title">加入我们</text>
						<text class="activity-desc">加入我们，一起打造一个更好的社区</text>
					</view>
				</view>
			</view>
		</view>
		
	</view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request.js'

// 默认背景图
const defaultBackgroundImage = ''

// 用户信息
const userInfo = ref({
	id: '',
	username: '',
	role: '',
	avatar: '',
	phone: '',
	backgroundImage: '', // 背景图字段
	avatarFrameUrl: '', // 添加头像框字段
	level: 1 // 添加用户等级字段
})

// 签到状态
const isSigned = ref(false)
const signDisabled = ref(false)

// 判断是否为管理员
const isAdmin = computed(() => {
	return userInfo.value.role === 'admin'
	
})


// 处理每日签到
const handleSignIn = async () => {
	if (isSigned.value || signDisabled.value) {
		uni.showToast({
			title: '今日已签到',
			icon: 'none'
		})
		return
	}
	
	// 防止重复点击
	signDisabled.value = true
	
	try {
		const res = await request('/user/sign', {
			method: 'GET'
		})
		
		if (res.code === 200) {
			isSigned.value = true
			uni.showToast({
				title: '签到成功',
				icon: 'success'
			})
			
			// 存储今日已签到状态
			const today = new Date().toDateString()
			uni.setStorageSync('lastSignDate', today)
			
			// 如果签到有奖励，可以在这里处理
			if (res.data && res.data.reward) {
				setTimeout(() => {
					uni.showToast({
						title: `获得奖励: ${res.data.reward}`,
						icon: 'none'
					})
				}, 1500)
			}
		} else {
			uni.showToast({
				title: res.msg || '签到失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('签到失败:', error)
		uni.showToast({
			title: '网络异常，请稍后重试',
			icon: 'none'
		})
	} finally {
		// 一段时间后重置按钮状态，防止频繁点击
		setTimeout(() => {
			signDisabled.value = false
		}, 2000)
	}
}

// 检查是否已经签到
const checkSignStatus = () => {
	const lastSignDate = uni.getStorageSync('lastSignDate')
	const today = new Date().toDateString()
	
	if (lastSignDate === today) {
		isSigned.value = true
	} else {
		isSigned.value = false
	}
}

// 跳转到我的项目
const goToMyProject = () => {
	uni.navigateTo({
		url: '/pages/project/myProject'
	})
}

const goToMessage = () => {
	uni.navigateTo({
		url: '/pages/message/conversationList'
	})
}

// 跳转到优惠券列表
const goToMyCoupon = () => {
	uni.navigateTo({
		url: '/pages/user/myCoupon'
	})
}

// 修改用户信息
const editUserInfo = () => {
	uni.navigateTo({
		url: '/pages/user/edit-profile'
	})
}

// 跳转到举报页面
const goToReport = () => {
	uni.navigateTo({
		url: '/pages/user/myReport'
	})
}

// 跳转到管理员界面
const goToAdminPanel = () => {
	uni.navigateTo({
		url: '/pages/admin/index'
	})
}

// 关于应用
const aboutApp = () => {
	uni.navigateTo({
		url: '/pages/user/about'
	})
}

// 联系客服
const goToCustomerService = () => {
	uni.navigateTo({
		url: '/pages/user/customer-service'
	})
}

// 退出登录确认
const confirmLogout = () => {
	uni.showModal({
		title: '退出登录',
		content: '确定要退出当前账号吗？',
		success: (res) => {
			if (res.confirm) {
				loginout()
			}
		}
	})
}

// 退出登录
const loginout = () => {
	uni.removeStorageSync("sa-token")
	uni.removeStorageSync("is-admin") // 清除管理员状态
	uni.removeStorageSync("userId")
	uni.showToast({
		title: '退出登录',
		icon: 'success'
	})
	setTimeout(() => {
		uni.reLaunch({
			url: "/pages/login/login"
		})
	}, 1500)
}

// 跳转个人中心
const goToPersonalCenter = () => {
	uni.navigateTo({
		url: '/pages/user/personal-center'
	})
}


// 跳转到活动页面
const goActivity = (type) => {
	if (type === 'community') {
		uni.navigateTo({
			url: '/pages/forum/detail?id=3'
		})
	} else if (type === 'join') {
		uni.navigateTo({
			url: '/pages/project/detail?id=1'
		})
	}
}

onMounted(() => {
	// 页面加载时获取用户信息
	request('/user/getUserInfo',{
		method:'GET'
	}).then(res=>{
		if(res.code === 200){
			userInfo.value = res.data
			console.log('获取到用户信息:', userInfo.value)
		}else{
			uni.showToast({
				title: res.msg || '获取用户信息失败',
				icon: 'none'
			})
		}
	})
	
	// 检查签到状态
	checkSignStatus()
})
</script>

<style>
.user-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #f5f7fa 0%, #e4f2fb 50%, #f5f7fa 100%);
	padding-bottom: 30rpx;
	position: relative;
	overflow: hidden;
}

/* 背景装饰元素 */
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

/* 用户信息卡片 */
.user-card {
	position: relative;
	background-color: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(10px);
	border-radius: 0 0 12rpx 12rpx;
	padding: 140rpx 30rpx 40rpx;
	box-shadow: 0 2rpx 20rpx rgba(0, 0, 0, 0.1);
	margin-bottom: 30rpx;
	overflow: hidden;
	z-index: 1;
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
	/* filter: blur(2px) brightness(0.9); */
	transform: scale(1.05); /* 避免虚化后边缘出现空白 */
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

.logout-icon {
	padding: 20rpx;
	margin-top: 90rpx;
	margin-right: 10rpx;
}

.logout-icon .menu-icon-image {
	width: 44rpx;
	height: 44rpx;
	opacity: 0.9;
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

.user-level-row {
	display: flex;
	align-items: center;
	gap: 15rpx;
}

.user-info-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

/* 注意：用户等级相关样式已经定义在App.vue全局样式中，此处移除 */

.user-id {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.9);
	text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* 通用卡片样式 */
.section-card {
	background-color: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(10px);
	border-radius: 12rpx;
	margin: 0 30rpx 30rpx;
	padding: 30rpx;
	box-shadow: 0 2rpx 20rpx rgba(0, 0, 0, 0.1);
	position: relative;
	z-index: 1;
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-bottom: 24rpx;
	border-bottom: 1px solid rgba(0, 0, 0, 0.05);
	margin-bottom: 24rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.add-btn {
	font-size: 28rpx;
	color: #1890ff;
}

/* 患者卡片样式 */
.patient-card {
	background-color: #f9f9f9;
	border-radius: 8rpx;
	padding: 24rpx;
}

.patient-info {
	display: flex;
	align-items: center;
}

.patient-avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50rpx;
	margin-right: 20rpx;
}

.patient-details {
	flex: 1;
}

.patient-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 6rpx;
	display: block;
}

.patient-status {
	font-size: 24rpx;
	display: inline-block;
	padding: 2rpx 12rpx;
	border-radius: 20rpx;
}

.online {
	color: #52c41a;
	background-color: rgba(82, 196, 26, 0.1);
}

.offline {
	color: #999;
	background-color: rgba(0, 0, 0, 0.05);
}

.action-buttons {
	display: flex;
	flex-direction: column;
}

.small-btn {
	font-size: 24rpx;
	padding: 10rpx 20rpx;
	border-radius: 30rpx;
	margin-bottom: 10rpx;
	text-align: center;
	min-width: 140rpx;
}

.monitor-btn {
	background-color: #1890ff;
	color: #fff;
}

.unbind-btn {
	background-color: #f5f5f5;
	color: #999;
}

/* 设备列表样式 */
.device-list {
	margin-top: 20rpx;
}

.device-item {
	display: flex;
	align-items: center;
	padding: 24rpx 0;
	border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.device-item:last-child {
	border-bottom: none;
}

.device-icon {
	width: 80rpx;
	height: 80rpx;
	border-radius: 40rpx;
	background-color: #e6f7ff;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 20rpx;
}

.device-icon.watch {
	background-color: #e6f7ff;
}

.device-icon.other {
	background-color: #fff2e8;
}

.icon-text {
	font-size: 40rpx;
}

.device-details {
	flex: 1;
}

.device-name {
	font-size: 30rpx;
	color: #333;
	margin-bottom: 6rpx;
	display: block;
}

.device-status {
	font-size: 24rpx;
	color: #999;
}

.device-actions {
	padding: 0 10rpx;
}

.action-icon {
	font-size: 36rpx;
}

/* 空状态样式 */
.empty-info {
	padding: 40rpx 0;
	text-align: center;
}

.empty-icon {
	width: 200rpx;
	height: 200rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 30rpx;
	color: #333;
	margin-bottom: 10rpx;
	display: block;
}

.empty-desc {
	font-size: 26rpx;
	color: #999;
}

/* 菜单网格样式 */
.menu-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 20rpx;
	margin-top: 10rpx;
}

.grid-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 20rpx 10rpx;
	background: transparent;
	border-radius: 0;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	border: none;
}

.grid-item:active {
	background: transparent;
	transform: scale(0.95);
	box-shadow: none;
}

.grid-icon-wrapper {
	width: 70rpx;
	height: 70rpx;
	border-radius: 80%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 16rpx;
	box-shadow: 0 4rpx 8rpx rgba(26, 188, 156, 0.3);
}

.grid-icon {
	width: 50rpx;
	height: 50rpx;
}

.grid-icon-emoji {
	font-size: 36rpx;
}

.grid-text {
	font-size: 24rpx;
	color: #333;
	text-align: center;
	font-weight: 500;
	line-height: 1.4;
}

/* 管理员入口样式 */
.admin-grid-entry .grid-icon-wrapper {
	background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
	box-shadow: 0 4rpx 12rpx rgba(255, 152, 0, 0.3);
}

.admin-grid-entry .grid-text {
	color: #ff9800;
}

/* 退出登录按钮 */
.logout-btn {
	margin: 0 30rpx 40rpx;
	background-color: #fff;
	color: #ff4d4f;
	text-align: center;
	padding: 30rpx 0;
	border-radius: 12rpx;
	font-size: 32rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

/* 添加图标样式 */
.menu-icon-image {
	width: 40rpx;
	height: 40rpx;
	margin-right: 20rpx;
}

/* 活动通知区域 */
.activity-section {
	margin: 30rpx 24rpx;
	position: relative;
	z-index: 1;
}

.activity-card {
	background: linear-gradient(135deg, #baeb34, #1abc9c);
	border-radius: 20rpx;
	padding: 24rpx;
	color: #fff;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 8rpx 16rpx rgba(186, 235, 52, 0.2);
	position: relative;
	overflow: hidden;
}

.activity-card::before {
	content: '';
	position: absolute;
	top: -100rpx;
	right: -100rpx;
	width: 200rpx;
	height: 200rpx;
	background: rgba(255, 255, 255, 0.1);
	border-radius: 50%;
}

.activity-content {
	display: flex;
	align-items: center;
	flex: 1;
    position: relative;
}

.activity-image-container {
    position: absolute;
    left: 400rpx;
    top: 50%;
    transform: translateY(-50%);
    width: 300rpx;
    height: 300rpx;
    opacity: 0.2;
    display: flex;
    align-items: center;
    justify-content: center;
}

.activity-icon {
	width: 300rpx;
	height: 300rpx;
}

.activity-text {
	flex: 1;
    padding-left: 20rpx;
    position: relative;
    z-index: 2;
}

.activity-tag {
	display: inline-block;
	background: rgba(255, 255, 255, 0.3);
	color: #fff;
	font-size: 22rpx;
	padding: 4rpx 12rpx;
	border-radius: 10rpx;
	margin-bottom: 10rpx;
}

.activity-title {
	font-size: 30rpx;
	font-weight: bold;
	margin-bottom: 6rpx;
	display: block;
}

.activity-desc {
	font-size: 24rpx;
	opacity: 0.9;
}


/* 签到按钮和等级样式 */

.sign-in-btn {
	background: linear-gradient(135deg, #1abc9c, #2ecc71);
	border-radius: 30rpx;
	padding: 8rpx 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 10rpx rgba(26, 188, 156, 0.3);
	transition: all 0.3s ease;
}

.sign-icon {
	width: 28rpx;
	height: 28rpx;
	margin-right: 8rpx;
}

.sign-text {
	font-size: 24rpx;
	font-weight: bold;
	color: #fff;
}

.signed {
	background: linear-gradient(135deg, #bdc3c7, #95a5a6);
	box-shadow: 0 4rpx 10rpx rgba(149, 165, 166, 0.3);
}
</style>
