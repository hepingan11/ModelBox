<template>
	<view class="admin-login-container">
		<!-- 顶部装饰 -->
		<view class="decorations">
			<view class="circle circle-1"></view>
			<view class="circle circle-2"></view>
			<view class="circle circle-3"></view>
		</view>
		
		<!-- 返回按钮 -->
		<view class="back-btn" @tap="goBack">
			<text class="back-icon">←</text>
			<text class="back-text">返回</text>
		</view>
		
		<!-- 顶部标题 -->
		<view class="header-box">
			<view class="admin-icon">👨‍💼</view>
			<text class="header-title">管理员登录</text>
			<text class="header-subtitle">请输入管理员账号和密钥</text>
		</view>
		
		<!-- 登录表单 -->
		<view class="login-form">
			<view class="input-group">
				<text class="iconfont icon-user">👤</text>
				<input type="text" v-model="formData.userName" placeholder="请输入管理员账号" class="input-field" />
			</view>
			
			<view class="input-group">
				<text class="iconfont icon-password">🔒</text>
				<input type="password" v-model="formData.password" placeholder="请输入管理员密码" class="input-field" />
			</view>
			
			<view class="input-group">
				<text class="iconfont icon-key">🔑</text>
				<input type="password" v-model="formData.adminKey" placeholder="请输入管理员密钥" class="input-field" />
			</view>
			
			<button class="login-btn" @tap="handleAdminLogin" :disabled="isLoading">
				<text v-if="!isLoading">管理员登录</text>
				<view class="loading-dots" v-else>
					<view class="dot"></view>
					<view class="dot"></view>
					<view class="dot"></view>
				</view>
			</button>
			
			<view class="tips-box">
				<text class="tips-text">* 管理员密钥由系统管理员提供，请妥善保管</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { apiBaseUrl } from '../../store/index'

const { proxy } = getCurrentInstance()
const baseUrl = apiBaseUrl

// 表单数据
const formData = ref({
	userName: '',
	password: '',
	adminKey: '' // 管理员密钥
})

// 加载状态
const isLoading = ref(false)

// 返回上一页
const goBack = () => {
	uni.navigateBack()
}

// 管理员登录
const handleAdminLogin = () => {
	// 表单验证
	if (!formData.value.userName || !formData.value.password || !formData.value.adminKey) {
		uni.showToast({
			title: '请填写完整登录信息',
			icon: 'none'
		})
		return
	}
	
	isLoading.value = true
	
	// 调用管理员登录接口
	uni.request({
		url: `${baseUrl}/user/login`,
		method: 'POST',
		data: formData.value,
		success: (res) => {
			if (res.data.code === 200) {
				// 存储管理员token
				uni.setStorageSync('sa-token', res.data.data.tokenValue)
				uni.setStorageSync('is-admin', true)
				
				uni.showToast({
					title: '登录成功',
					icon: 'success'
				})
				
				// 延迟跳转到管理后台
				setTimeout(() => {
					uni.switchTab({
						url: '/pages/user/user'
					})
				}, 1500)
			} else {
				uni.showToast({
					title: res.data.msg || '登录失败',
					icon: 'none'
				})
			}
		},
		fail: () => {
			uni.showToast({
				title: '网络错误，请稍后重试',
				icon: 'none'
			})
		},
		complete: () => {
			isLoading.value = false
		}
	})
}
</script>

<style>
.admin-login-container {
	padding: 60rpx 40rpx;
	min-height: 100vh;
	background: linear-gradient(135deg, #f0f4f9 0%, #b6c8e1 100%);
	position: relative;
	overflow: hidden;
}

/* 装饰性元素 */
.decorations {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	z-index: 0;
}

.circle {
	position: absolute;
	border-radius: 50%;
	opacity: 0.15;
}

.circle-1 {
	width: 300rpx;
	height: 300rpx;
	background: linear-gradient(45deg, #3A50C0, #5271FF);
	top: -100rpx;
	right: -50rpx;
}

.circle-2 {
	width: 200rpx;
	height: 200rpx;
	background: linear-gradient(45deg, #3A50C0, #5271FF);
	top: 200rpx;
	left: -100rpx;
}

.circle-3 {
	width: 150rpx;
	height: 150rpx;
	background: linear-gradient(45deg, #3A50C0, #5271FF);
	bottom: 100rpx;
	right: 100rpx;
}

/* 返回按钮 */
.back-btn {
	position: absolute;
	top: 40rpx;
	left: 40rpx;
	display: flex;
	align-items: center;
	z-index: 10;
	background: rgba(255, 255, 255, 0.8);
	padding: 15rpx 25rpx;
	border-radius: 30rpx;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
	backdrop-filter: blur(5px);
	transition: all 0.3s;
}

.back-btn:active {
	transform: scale(0.95);
}

.back-icon {
	font-size: 28rpx;
	margin-right: 8rpx;
}

.back-text {
	font-size: 24rpx;
	color: #3A50C0;
}

/* 顶部标题 */
.header-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-top: 100rpx;
	margin-bottom: 60rpx;
	position: relative;
	z-index: 1;
}

.admin-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.header-title {
	font-size: 42rpx;
	color: #333;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.header-subtitle {
	font-size: 26rpx;
	color: #666;
}

/* 登录表单 */
.login-form {
	padding: 40rpx;
	background: rgba(255, 255, 255, 0.85);
	border-radius: 20rpx;
	box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
	position: relative;
	z-index: 1;
	backdrop-filter: blur(10px);
}

.input-group {
	display: flex;
	align-items: center;
	padding: 25rpx 30rpx;
	background-color: #f8f8f8;
	border-radius: 40rpx;
	margin-bottom: 30rpx;
	transition: all 0.3s;
	border: 2rpx solid transparent;
}

.input-group:focus-within {
	border-color: #3A50C0;
	box-shadow: 0 0 10rpx rgba(58, 80, 192, 0.2);
}

.input-group .iconfont {
	font-size: 36rpx;
	color: #3A50C0;
	margin-right: 20rpx;
	width: 40rpx;
	text-align: center;
}

.input-field {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.login-btn {
	width: 100%;
	height: 90rpx;
	background: linear-gradient(45deg, #3A50C0, #5271FF);
	color: #fff;
	font-size: 32rpx;
	border-radius: 45rpx;
	margin-top: 60rpx;
	box-shadow: 0 8rpx 20rpx rgba(58, 80, 192, 0.3);
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s;
}

.login-btn:active {
	transform: scale(0.98);
	box-shadow: 0 4rpx 10rpx rgba(58, 80, 192, 0.2);
}

/* 加载动画 */
.loading-dots {
	display: flex;
	justify-content: center;
	align-items: center;
}

.dot {
	width: 12rpx;
	height: 12rpx;
	background-color: #fff;
	border-radius: 50%;
	margin: 0 6rpx;
	animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
	animation-delay: -0.32s;
}

.dot:nth-child(2) {
	animation-delay: -0.16s;
}

@keyframes bounce {
	0%, 80%, 100% {
		transform: scale(0);
	}
	40% {
		transform: scale(1);
	}
}

/* 提示信息 */
.tips-box {
	margin-top: 30rpx;
	padding: 0 20rpx;
}

.tips-text {
	font-size: 24rpx;
	color: #999;
	line-height: 1.5;
}
</style>
