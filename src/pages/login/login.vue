<template>
	<view class="login-container">
		<!-- 顶部装饰 -->
		<view class="decorations">
			<view class="circle circle-1"></view>
			<view class="circle circle-2"></view>
			<view class="circle circle-3"></view>
		</view>
		
		<!-- 管理员登录入口 -->
		<view class="admin-login-entry" @tap="goToAdminLogin">
			<text class="admin-login-text">管理员登录</text>
			<text class="admin-login-icon">👤</text>
		</view>
		
		<!-- 顶部logo -->
		<view class="logo-box">
			<image :src="wxUserInfo.avatarUrl || '/static/logo.jpg'" mode="aspectFit" class="logo-img"></image>
			<text class="logo-title">{{ wxUserInfo.nickName ? `Hi，${wxUserInfo.nickName}！` : 'Hi，欢迎回来！' }}</text>
			<text class="logo-subtitle">登录你的账户，开始新的旅程</text>
		</view>
		
		<!-- 登录表单 -->
		<view class="login-form">
			<!-- 账密登录表单 - 在微信环境下可折叠 -->
			<view v-if="showLoginForm" class="form-section">
				<view class="input-group">
					<text class="iconfont icon-user"></text>
					<input type="text" v-model="formData.username" placeholder="请输入用户名" class="input-field" />
				</view>
				
				<view class="input-group">
					<text class="iconfont icon-password"></text>
					<input type="password" v-model="formData.password" placeholder="请输入密码" class="input-field" />
				</view>
				
				<button class="login-btn" @tap="handleLogin" :disabled="isLoading || !isAgreed">
					<text v-if="!isLoading">登 录</text>
					<view class="loading-dots" v-else>
						<view class="dot"></view>
						<view class="dot"></view>
						<view class="dot"></view>
					</view>
				</button>
			</view>
			
			<!-- 在微信环境下显示展开按钮 -->
			<view v-if="isWechatMiniApp && !showLoginForm" class="toggle-form-btn" @tap="showLoginForm = true">
				<text>账号登录</text>
				<text class="toggle-icon">▼</text>
			</view>
			
			<!-- 社区合约勾选 -->
			<view class="agreement-box">
				<view class="checkbox-wrapper" @tap="toggleAgreement">
					<view class="checkbox" :class="{'checkbox-checked': isAgreed}">
						<text v-if="isAgreed" class="checkbox-icon">✓</text>
					</view>
				</view>
				<view class="agreement-text">
					我已阅读并同意
					<text class="agreement-link" @tap="showAgreement">《社区合约》</text>
				</view>
			</view>
			
			<!-- 微信快捷登录按钮 - 仅在微信小程序环境显示 -->
			<button v-if="isWechatMiniApp" class="wechat-login-btn"  @tap="handleWxLoginDirectly" :disabled="isLoading || !isAgreed">
				<view class="wechat-icon">
					<image src="/static/wechat-icon.svg" mode="aspectFit" class="wechat-icon-img"></image>
				</view>
				<text class="wechat-text">微信快捷登录</text>
			</button>
			
			<view class="options-box">
				<navigator url="/pages/login/register" class="register-link">注册新账号</navigator>
				<text class="forget-pwd">忘记密码？</text>
			</view>
		</view>
		
		<!-- 社区合约弹窗 -->
		<view v-if="showAgreementPopup" class="custom-popup-mask" @tap.self="closeAgreement">
			<view class="agreement-popup">
				<view class="agreement-popup-title">社区合约</view>
				<scroll-view scroll-y="true" class="agreement-popup-content">
					<view class="agreement-popup-text">
						<view class="agreement-section">
							<view class="agreement-section-title">一、用户行为规范</view>
							<view class="agreement-section-content">
								1.1 用户应遵守中华人民共和国相关法律法规。<br>
								1.2 用户不得发布违法、色情、暴力、政治敏感等不良信息。<br>
								1.3 用户应尊重他人，不得进行人身攻击、辱骂等不文明行为。<br>
								1.4 用户应尊重知识产权，不得未经授权转载、抄袭他人内容。
							</view>
						</view>
						<view class="agreement-section">
							<view class="agreement-section-title">二、隐私保护</view>
							<view class="agreement-section-content">
								2.1 我们将依法保护用户的个人隐私信息。<br>
								2.2 未经用户许可，我们不会向第三方披露用户个人信息。<br>
								2.3 我们可能收集用户的使用数据以改进服务质量。
							</view>
						</view>
						<view class="agreement-section">
							<view class="agreement-section-title">三、免责声明</view>
							<view class="agreement-section-content">
								3.1 用户发布的内容由用户自行负责。<br>
								3.2 平台不对用户间交易纠纷承担责任。<br>
								3.3 因不可抗力导致的服务中断，平台不承担责任。
							</view>
						</view>
					</view>
				</scroll-view>
				<view class="agreement-popup-btns">
					<button class="agreement-popup-btn" @tap="closeAgreement">关闭</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted } from 'vue'
import { apiBaseUrl } from '../../store/index'

const { proxy } = getCurrentInstance()
const baseUrl = apiBaseUrl

const formData = ref({
	username: '',
	password: ''
})

const isLoading = ref(false)
const wxUserInfo = ref({})
const isAgreed = ref(false)
const showAgreementPopup = ref(false)
const isWechatMiniApp = ref(false)
const showLoginForm = ref(true)

// 调试相关
const showDebug = ref(false)
const debugLogs = ref([])

// 添加调试日志
const addDebugLog = (message) => {
	const timestamp = new Date().toLocaleTimeString()
	debugLogs.value.push(`[${timestamp}] ${message}`)
	console.log(`[DEBUG] ${message}`)
}

// 清除调试日志
const clearDebugLogs = () => {
	debugLogs.value = []
}

// 跳转到管理员登录页面
const goToAdminLogin = () => {
	uni.navigateTo({
		url: '/pages/login/admin-login'
	})
}

// 切换协议同意状态
const toggleAgreement = () => {
	isAgreed.value = !isAgreed.value
}

// 显示协议弹窗
const showAgreement = () => {
	showAgreementPopup.value = true
}

// 关闭协议弹窗
const closeAgreement = () => {
	showAgreementPopup.value = false
}

// 检测是否为微信小程序环境
const detectWechatMiniApp = () => {
	// 获取系统信息
	const systemInfo = uni.getSystemInfoSync()
	console.log('系统信息:', systemInfo)
	// 在微信小程序中，platform 为 'devtools' 或其他值，但可以通过 uniPlatform 判断
	// 或者通过检查是否支持微信登录 API
	const isWeixin = systemInfo.platform === 'devtools' || 
	                 systemInfo.platform === 'ios' || 
	                 systemInfo.platform === 'android' ||
	                 (typeof wx !== 'undefined') // 微信小程序全局对象
	
	// 更可靠的方式：检查 __wxConfig 或其他微信特有对象
	const isWechatEnv = typeof wx !== 'undefined' && typeof wx.login === 'function'
	
	isWechatMiniApp.value = isWechatEnv
	// 在微信环境下，默认折叠账密登录表单
	if (isWechatEnv) {
		showLoginForm.value = false
	}
	addDebugLog(`环境检测: 微信小程序=${isWechatMiniApp.value}, 平台=${systemInfo.platform}`)
}

// 页面加载时检查是否有已存储的微信用户信息
onMounted(() => {
	// 检测环境
	detectWechatMiniApp()
	
	const storedUserInfo = uni.getStorageSync('wx-user-info')
	if (storedUserInfo) {
		wxUserInfo.value = storedUserInfo
		addDebugLog(`已获取到存储的微信用户信息: ${JSON.stringify(storedUserInfo)}`)
	}
})

const handleLogin = () => {
	if (!formData.value.username || !formData.value.password) {
		uni.showToast({
			title: '请输入用户名和密码',
			icon: 'none'
		})
		return
	}
	
	if (!isAgreed.value) {
		uni.showToast({
			title: '请先同意社区合约',
			icon: 'none'
		})
		return
	}
	
	isLoading.value = true
	
	// 使用全局API基础URL
	uni.request({
		url: `${baseUrl}/user/login`,
		method: 'POST',
		data: formData.value,
		success: async (res) => {
			if (res.data.code === 200) {
				// 存储token
				console.log(res.data)
				uni.setStorageSync('sa-token', res.data.data.tokenValue)
				uni.showToast({
					title: '登录成功',
					icon: 'success'
				})
				
				setTimeout(() => {
					uni.switchTab({
						url: '/pages/index/index'
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
				title: '网络错误',
				icon: 'none'
			})
		},
		complete: () => {
			isLoading.value = false
		}
	})
}

// 直接处理微信登录
const handleWxLoginDirectly= () => {
	if (!isAgreed.value) {
		uni.showToast({
			title: '请先同意社区合约',
			icon: 'none'
		})
		return
	}
	
	uni.login({
		provider: 'weixin',
		success: (res) => {
			console.log(res.code)
			uni.request({
				url: `${baseUrl}/user/wx-login`,
				method: 'POST',
				data: res.code,
				success: (res) => {
					console.log(res)
					if(res.data.code === 200){
						uni.setStorageSync('sa-token', res.data.data.tokenValue)
						uni.showToast({
							title: '登录成功',
							icon: 'success'
						})
						setTimeout(() => {
							uni.switchTab({
								url: '/pages/index/index'
							})
						}, 1500)
					}else{
						uni.showToast({
							title: res.data.msg || '登录失败',
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
	});
};

</script>

<style>
.login-container {
	padding: 60rpx 40rpx;
	min-height: 100vh;
	background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
	position: relative;
	overflow: hidden;
}

/* 管理员登录入口 */
.admin-login-entry {
	position: absolute;
	top: 40rpx;
	right: 40rpx;
	background: rgba(255, 255, 255, 0.8);
	padding: 15rpx 25rpx;
	border-radius: 30rpx;
	display: flex;
	align-items: center;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
	z-index: 10;
	backdrop-filter: blur(5px);
	transition: all 0.3s;
}

.admin-login-entry:active {
	transform: scale(0.95);
}

.admin-login-text {
	font-size: 24rpx;
	color: #5271FF;
	font-weight: 500;
	margin-right: 8rpx;
}

.admin-login-icon {
	font-size: 28rpx;
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
	background: linear-gradient(45deg, #5271FF, #5F04AF);
	top: -100rpx;
	right: -50rpx;
}

.circle-2 {
	width: 200rpx;
	height: 200rpx;
	background: linear-gradient(45deg, #FF85C2, #FFA1A1);
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

.login-form {
	padding: 40rpx;
	background: rgba(255, 255, 255, 0.85);
	border-radius: 20rpx;
	box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
	position: relative;
	z-index: 1;
	backdrop-filter: blur(10px);
}

.form-section {
	animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
	from {
		opacity: 0;
		transform: translateY(-10rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

/* 展开/折叠按钮 */
.toggle-form-btn {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 24rpx 20rpx;
	background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
	border-radius: 12rpx;
	margin-bottom: 20rpx;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	border: 1rpx solid rgba(0, 0, 0, 0.05);
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.toggle-form-btn:active {
	background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
	transform: translateY(1rpx);
	box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.08);
}

.toggle-form-btn text {
	font-size: 28rpx;
	color: #495057;
	font-weight: 600;
	letter-spacing: 1rpx;
}

.toggle-icon {
	margin-left: 12rpx;
	font-size: 18rpx;
	transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	color: #868e96;
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
	border-color: #5271FF;
	box-shadow: 0 0 10rpx rgba(82, 113, 255, 0.2);
}

.input-group .iconfont {
	font-size: 40rpx;
	color: #5271FF;
	margin-right: 20rpx;
}

.input-field {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

/* 社区合约勾选样式 */
.agreement-box {
	display: flex;
	align-items: center;
	margin-top: 20rpx;
	padding: 0 10rpx;
}

.checkbox-wrapper {
	padding: 10rpx;
}

.checkbox {
	width: 36rpx;
	height: 36rpx;
	border: 2rpx solid #ccc;
	border-radius: 6rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s;
}

.checkbox-checked {
	background-color: #5271FF;
	border-color: #5271FF;
}

.checkbox-icon {
	color: #fff;
	font-size: 24rpx;
	font-weight: bold;
}

.agreement-text {
	font-size: 26rpx;
	color: #666;
	margin-left: 10rpx;
}

.agreement-link {
	color: #5271FF;
	text-decoration: underline;
}

.login-btn {
	width: 100%;
	height: 90rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	font-size: 32rpx;
	font-weight: 600;
	border-radius: 12rpx;
	margin-top: 40rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	border: none;
	letter-spacing: 2rpx;
	position: relative;
	overflow: hidden;
}

.login-btn::before {
	content: '';
	position: absolute;
	top: 0;
	left: -100%;
	width: 100%;
	height: 100%;
	background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
	transition: left 0.5s;
}

.login-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.login-btn:active::before {
	left: 100%;
}

.login-btn[disabled] {
	opacity: 0.5;
	background: linear-gradient(135deg, #a0aec0 0%, #cbd5e0 100%);
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
	cursor: not-allowed;
}

/* 微信登录按钮样式 */
.wechat-login-btn {
	width: 100%;
	height: 90rpx;
	background: linear-gradient(135deg, #07C160 0%, #05ad52 100%);
	color: #fff;
	font-size: 32rpx;
	font-weight: 600;
	border-radius: 12rpx;
	box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.35);
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	position: relative;
	overflow: hidden;
	border: none;
	letter-spacing: 2rpx;
}

.wechat-login-btn::before {
	content: '';
	position: absolute;
	top: 0;
	left: -100%;
	width: 100%;
	height: 100%;
	background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
	transition: left 0.5s;
}

.wechat-login-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(7, 193, 96, 0.25);
}

.wechat-login-btn:active::before {
	left: 100%;
}

.wechat-login-btn[disabled] {
	opacity: 0.5;
	background: linear-gradient(135deg, #a0aec0 0%, #cbd5e0 100%);
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
	cursor: not-allowed;
}

.wechat-icon {
	margin-right: 12rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.wechat-icon-img {
	width: 40rpx;
	height: 40rpx;
}

.wechat-text {
	font-size: 32rpx;
	font-weight: 600;
}

/* 社区合约弹窗样式 */
.agreement-popup {
	width: 600rpx;
	background-color: #fff;
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.2);
}

.agreement-popup-title {
	font-size: 32rpx;
	font-weight: bold;
	text-align: center;
	padding: 30rpx 0;
	border-bottom: 1rpx solid #eee;
}

.agreement-popup-content {
	padding: 30rpx;
	max-height: 700rpx;
}

.agreement-popup-text {
	font-size: 26rpx;
	color: #333;
	line-height: 1.6;
}

.agreement-section {
	margin-bottom: 30rpx;
}

.agreement-section-title {
	font-weight: bold;
	margin-bottom: 10rpx;
}

.agreement-section-content {
	color: #666;
	padding-left: 20rpx;
}

.agreement-popup-btns {
	display: flex;
	border-top: 1rpx solid #eee;
}

.agreement-popup-btn {
	flex: 1;
	height: 90rpx;
	line-height: 90rpx;
	text-align: center;
	font-size: 30rpx;
	color: #5271FF;
	background-color: #fff;
	border-radius: 0;
}

.agreement-popup-btn:after {
	border: none;
}

/* 调试区域 */
.debug-area {
	margin-top: 30rpx;
	background: #f0f0f0;
	border-radius: 10rpx;
	border: 1px solid #ddd;
	overflow: hidden;
}

.debug-title {
	background: #eee;
	padding: 10rpx 20rpx;
	font-size: 24rpx;
	color: #333;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.debug-close {
	font-size: 32rpx;
	padding: 0 10rpx;
}

.debug-content {
	padding: 10rpx;
	max-height: 400rpx;
}

.debug-log {
	font-size: 22rpx;
	color: #555;
	padding: 6rpx 0;
	border-bottom: 1px solid #eee;
	word-break: break-all;
}

.debug-clear {
	font-size: 24rpx;
	margin: 10rpx;
	height: 60rpx;
	line-height: 60rpx;
	background: #e0e0e0;
}

.debug-btn {
	margin-top: 20rpx;
	font-size: 26rpx;
	background: #f0f0f0;
	color: #666;
	height: 70rpx;
	line-height: 70rpx;
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
	justify-content: space-between;
	align-items: center;
	margin-top: 30rpx;
	padding: 20rpx;
	background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
	border-radius: 12rpx;
	border: 1rpx solid rgba(102, 126, 234, 0.1);
}

.register-link, .forget-pwd {
	font-size: 26rpx;
	color: #667eea;
	font-weight: 500;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	padding: 8rpx 12rpx;
	border-radius: 6rpx;
	position: relative;
}

.register-link::before, .forget-pwd::before {
	content: '';
	position: absolute;
	bottom: 0;
	left: 0;
	width: 0;
	height: 2rpx;
	background: linear-gradient(90deg, #667eea, #764ba2);
	transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.register-link:active, .forget-pwd:active {
	color: #764ba2;
	background: rgba(102, 126, 234, 0.1);
}

.register-link:active::before, .forget-pwd:active::before {
	width: 100%;
}

.custom-popup-mask {
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	background: rgba(0,0,0,0.35);
	z-index: 999;
	display: flex;
	justify-content: center;
	align-items: center;
}
</style>
