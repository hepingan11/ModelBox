<template>
	<view class="register-container">
		<!-- 顶部装饰 -->
		<view class="decorations">
			<view class="circle circle-1"></view>
			<view class="circle circle-2"></view>
			<view class="circle circle-3"></view>
		</view>
		
		<!-- 顶部logo -->
		<view class="logo-box">
			<image src="/static/logo.jpg" mode="aspectFit" class="logo-img"></image>
			<text class="logo-title">欢迎加入我们！</text>
			<text class="logo-subtitle">创建你的账户，开启全新体验</text>
		</view>
		
		<!-- 注册表单 -->
		<view class="register-form">
			<view class="input-group">
				<text class="iconfont icon-user"></text>
				<input type="text" v-model="formData.username" placeholder="请设置用户名" class="input-field" />
			</view>
			
			<view class="input-group">
				<text class="iconfont icon-password"></text>
				<input type="password" v-model="formData.password" placeholder="请设置密码" class="input-field" />
			</view>
			
			<view class="input-group">
				<text class="iconfont icon-password"></text>
				<input type="password" v-model="formData.confirmPassword" placeholder="请确认密码" class="input-field" />
			</view>
			
			<button class="register-btn" @tap="handleRegister" :disabled="isLoading">
				<text v-if="!isLoading">注 册</text>
				<view class="loading-dots" v-else>
					<view class="dot"></view>
					<view class="dot"></view>
					<view class="dot"></view>
				</view>
			</button>
			
			<view class="options-box">
				<navigator url="/pages/login/login" open-type="redirect" class="login-link">已有账号？立即登录</navigator>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { apiBaseUrl } from '../../store/index'

const { proxy } = getCurrentInstance()
const baseUrl = proxy?.$apiBaseUrl || apiBaseUrl

const formData = ref({
	username: '',
	password: '',
	confirmPassword: '',
	role: 'family' // 保留默认角色值，但不再在界面上显示选择
})

const isLoading = ref(false)

const handleRegister = () => {
	// 表单验证
	if (!formData.value.username) {
		uni.showToast({
			title: '请输入用户名',
			icon: 'none'
		})
		return
	}
	
	if (!formData.value.password) {
		uni.showToast({
			title: '请设置密码',
			icon: 'none'
		})
		return
	}
	
	if (formData.value.password !== formData.value.confirmPassword) {
		uni.showToast({
			title: '两次密码输入不一致',
			icon: 'none'
		})
		return
	}
	
	if (!formData.value.phone || formData.value.phone.length !== 11) {
		uni.showToast({
			title: '请输入有效的手机号',
			icon: 'none'
		})
		return
	}
	
	isLoading.value = true
	
	// 使用全局API基础URL
	uni.request({
		url: `${baseUrl}/user/register`,
		method: 'POST',
		data: {
			username: formData.value.username,
			password: formData.value.password,
			phone: formData.value.phone,
			role: formData.value.role
		},
		success: (res) => {
			if (res.data.code === 200) {
				uni.showToast({
					title: '注册成功',
					icon: 'success'
				})
				// 注册成功后跳转到登录页
				setTimeout(() => {
					uni.redirectTo({
						url: '/pages/login/login'
					})
				}, 1500)
			} else {
				uni.showToast({
					title: res.data.msg || '注册失败',
					icon: 'none'
				})
			}
		},
		fail: () => {
			uni.showToast({
				title: '网络错误',
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
.register-container {
	padding: 60rpx 40rpx;
	min-height: 100vh;
	background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
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
	opacity: 0.2;
}

.circle-1 {
	width: 300rpx;
	height: 300rpx;
	background: linear-gradient(45deg, #FF85C2, #FFA1A1);
	top: -100rpx;
	right: -50rpx;
}

.circle-2 {
	width: 200rpx;
	height: 200rpx;
	background: linear-gradient(45deg, #5271FF, #5F04AF);
	top: 200rpx;
	left: -100rpx;
}

.circle-3 {
	width: 150rpx;
	height: 150rpx;
	background: linear-gradient(45deg, #6FDFDF, #0BB5FF);
	bottom: 100rpx;
	right: 100rpx;
}

.logo-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 60rpx;
	position: relative;
	z-index: 1;
}

.logo-img {
	width: 160rpx;
	height: 160rpx;
	margin-bottom: 20rpx;
	border-radius: 50%;
	box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
}

.logo-title {
	font-size: 42rpx;
	color: #333;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.logo-subtitle {
	font-size: 26rpx;
	color: #666;
}

.register-form {
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
	border-color: #FF85C2;
	box-shadow: 0 0 10rpx rgba(255, 133, 194, 0.2);
}

.input-group .iconfont {
	font-size: 40rpx;
	color: #FF85C2;
	margin-right: 20rpx;
}

.input-field {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.register-btn {
	width: 100%;
	height: 90rpx;
	background: linear-gradient(45deg, #FF85C2, #FFA1A1);
	color: #fff;
	font-size: 32rpx;
	border-radius: 45rpx;
	margin-top: 60rpx;
	box-shadow: 0 8rpx 20rpx rgba(255, 133, 194, 0.3);
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s;
}

.register-btn:active {
	transform: scale(0.98);
	box-shadow: 0 4rpx 10rpx rgba(255, 133, 194, 0.2);
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

.options-box {
	display: flex;
	justify-content: center;
	margin-top: 30rpx;
	padding: 0 20rpx;
}

.login-link {
	font-size: 26rpx;
	color: #FF85C2;
	transition: color 0.3s;
}

.login-link:active {
	color: #E5659E;
}
</style>
