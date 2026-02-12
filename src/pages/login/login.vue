<template>
	<view class="login-container" :class="themeClass">
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
			<image :src="wxUserInfo.avatarUrl || '/static/logo.png'" mode="aspectFit" class="logo-img"></image>
			<text class="logo-title">{{ wxUserInfo.nickName ? `Hi，${wxUserInfo.nickName}！` : 'Hi，欢迎回来！' }}</text>
			<text class="logo-subtitle">登录你的账户，开始新的旅程</text>
		</view>
		
		<!-- 登录表单 -->
		<view class="login-form">
			<!-- Tab 切换栏 -->
			<view class="login-tabs">
				<view 
					class="tab-item" 
					:class="{ 'tab-active': activeTab === 'wechat' }"
					@tap="activeTab = 'wechat'"
				>
					<text>微信登录</text>
				</view>
				<view 
					class="tab-item" 
					:class="{ 'tab-active': activeTab === 'account' }"
					@tap="activeTab = 'account'"
				>
					<text>账号登录</text>
				</view>
				<!-- Tab 滑动指示器 -->
				<view class="tab-indicator" :class="{ 'tab-indicator-right': activeTab === 'account' }"></view>
			</view>
			
			<!-- Tab 内容区 -->
			<view class="tab-content">
				<!-- 微信登录面板 -->
				<view v-if="activeTab === 'wechat'" class="tab-panel">
					<view class="wechat-login-area">
						<!-- 移除了 wechat-big-icon -->
						<text class="wechat-login-tip">点击下方按钮，授权手机号快速登录</text>
						<button 
							class="wechat-login-btn" 
							open-type="getPhoneNumber" 
							@getphonenumber="onGetPhoneNumber" 
							:disabled="isLoading || !isAgreed"
						>
							<view class="wechat-icon">
								<image src="/static/wechat-icon.svg" mode="aspectFit" class="wechat-icon-img"></image>
							</view>
							<text v-if="!isLoading" class="wechat-text">微信快捷登录</text>
							<view class="loading-dots" v-else>
								<view class="dot"></view>
								<view class="dot"></view>
								<view class="dot"></view>
							</view>
						</button>
					</view>
				</view>
				
				<!-- 账号登录面板 -->
				<view v-if="activeTab === 'account'" class="tab-panel">
					<view class="input-group">
						<text class="iconfont icon-user"></text>
						<input type="text" v-model="formData.userName" placeholder="请输入用户名" class="input-field" />
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
			</view>
			
			<!-- 社区合约勾选（两种方式共用） -->
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
import { useTheme } from '@/hooks/useTheme'

const { themeClass } = useTheme()

const { proxy } = getCurrentInstance()
const baseUrl = apiBaseUrl

const formData = ref({
	userName: '',
	password: ''
})

const isLoading = ref(false)
const wxUserInfo = ref({})
const isAgreed = ref(false)
const showAgreementPopup = ref(false)
const activeTab = ref('wechat')  // 默认微信登录 Tab

// 调试相关
const showDebug = ref(false)
const debugLogs = ref([])

// 添加调试日志
const addDebugLog = (message) => {
	const timestamp = new Date().toLocaleTimeString()
	debugLogs.value.push(`[${timestamp}] ${message}`)
	console.log(`[DEBUG] ${message}`)
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

// 页面加载时检查是否有已存储的微信用户信息
onMounted(() => {
	const storedUserInfo = uni.getStorageSync('wx-user-info')
	if (storedUserInfo) {
		wxUserInfo.value = storedUserInfo
		addDebugLog(`已获取到存储的微信用户信息: ${JSON.stringify(storedUserInfo)}`)
	}
})

const handleLogin = () => {
	if (!formData.value.userName || !formData.value.password) {
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
				uni.setStorageSync('token', res.data.data.tokenValue)
				uni.setStorageSync('userId', res.data.data.loginId)
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

// 获取微信用户昵称（尝试获取，失败不影响登录流程）
const getWxNickName = () => {
	return new Promise((resolve) => {
		// 微信小程序2022年后 getUserProfile 不再返回真实昵称
		// 但仍然尝试获取，如果获取不到则返回空字符串
		try {
			uni.getUserProfile({
				desc: '用于完善用户资料',
				success: (res) => {
					addDebugLog(`获取用户资料成功: ${JSON.stringify(res.userInfo)}`)
					const nickName = res.userInfo?.nickName || ''
					// 保存用户信息到本地
					wxUserInfo.value = res.userInfo || {}
					uni.setStorageSync('wx-user-info', res.userInfo)
					resolve(nickName)
				},
				fail: (err) => {
					addDebugLog(`获取用户资料失败（不影响登录）: ${JSON.stringify(err)}`)
					resolve('')
				}
			})
		} catch (e) {
			addDebugLog(`getUserProfile 不支持: ${e.message}`)
			resolve('')
		}
	})
}

// 处理获取手机号回调（微信登录核心流程）
const onGetPhoneNumber = async (e) => {
	addDebugLog(`getPhoneNumber 回调: ${JSON.stringify(e.detail)}`)
	
	if (!isAgreed.value) {
		uni.showToast({
			title: '请先同意社区合约',
			icon: 'none'
		})
		return
	}
	
	// 用户拒绝授权手机号
	if (e.detail.errMsg && e.detail.errMsg !== 'getPhoneNumber:ok') {
		addDebugLog(`用户拒绝手机号授权: ${e.detail.errMsg}`)
		uni.showToast({
			title: '需要授权手机号才能登录',
			icon: 'none'
		})
		return
	}
	
	// 获取手机号授权码（code）- 新版微信API返回的是 code，不是 encryptedData
	const phoneCode = e.detail.code
	if (!phoneCode) {
		addDebugLog('未获取到手机号授权码')
		uni.showToast({
			title: '获取手机号失败，请重试',
			icon: 'none'
		})
		return
	}
	
	addDebugLog(`手机号授权码: ${phoneCode}`)
	isLoading.value = true
	
	try {
		// 步骤1：调用 uni.login 获取登录 code
		const loginCode = await new Promise((resolve, reject) => {
			uni.login({
				provider: 'weixin',
				success: (res) => {
					addDebugLog(`uni.login 成功, code: ${res.code}`)
					resolve(res.code)
				},
				fail: (err) => {
					addDebugLog(`uni.login 失败: ${JSON.stringify(err)}`)
					reject(err)
				}
			})
		})
		
		// 步骤2：尝试获取微信昵称
		const nickName = await getWxNickName()
		addDebugLog(`昵称: ${nickName || '(未获取到)'}`)
		
		// 步骤3：调用后端登录接口，传递 code、phone（手机号授权码）、nickName
		const loginData = {
			code: loginCode,
			phone: phoneCode,
			nickName: nickName || ''
		}
		addDebugLog(`发送登录请求: ${JSON.stringify(loginData)}`)
		
		uni.request({
			url: `${baseUrl}/auth/wechat/login`,
			method: 'POST',
			header: {
				'Content-Type': 'application/json'
			},
			data: loginData,
			success: (res) => {
				addDebugLog(`登录响应: ${JSON.stringify(res.data)}`)
				if (res.data.code === 200) {
					uni.setStorageSync('token', res.data.data.tokenValue)
					uni.setStorageSync('userId', res.data.data.loginId)
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
	} catch (err) {
		addDebugLog(`登录流程出错: ${JSON.stringify(err)}`)
		isLoading.value = false
		uni.showToast({
			title: '登录失败，请重试',
			icon: 'none'
		})
	}
};

</script>

<style>
.login-container {
	padding: 60rpx 40rpx;
	min-height: 100vh;
	background: linear-gradient(135deg, var(--bgColor1) 0%, var(--bgColor2) 100%);
	position: relative;
	overflow: hidden;
	color: var(--textColor1);
}

/* 管理员登录入口 */
.admin-login-entry {
	position: absolute;
	top: 40rpx;
	right: 40rpx;
	background: var(--bgColor2);
	padding: 15rpx 25rpx;
	border-radius: 30rpx;
	display: flex;
	align-items: center;
	box-shadow: 0 4rpx 10rpx var(--bgboxShadowColor1);
	z-index: 10;
	backdrop-filter: blur(5px);
	transition: all 0.3s;
}

.admin-login-entry:active {
	transform: scale(0.95);
}

.admin-login-text {
	font-size: 24rpx;
	color: var(--themeColor1);
	font-weight: 500;
	margin-right: 8rpx;
}

.admin-login-icon {
	font-size: 28rpx;
	color: var(--textColor1);
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
	background: linear-gradient(45deg, var(--themeColor1), #5F04AF);
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
	box-shadow: 0 8rpx 20rpx var(--bgboxShadowColor1);
}

.logo-title {
	font-size: 42rpx;
	color: var(--textColor1);
	font-weight: bold;
	margin-bottom: 10rpx;
}

.logo-subtitle {
	font-size: 26rpx;
	color: var(--textColor3);
}

.login-form {
	padding: 0;
	background: var(--bgColor3);
	border-radius: 20rpx;
	box-shadow: 0 10rpx 30rpx var(--bgboxShadowColor1);
	position: relative;
	z-index: 1;
	backdrop-filter: blur(10px);
	overflow: hidden;
}

/* Tab 切换栏 */
.login-tabs {
	display: flex;
	position: relative;
	background: var(--bgColor1);
	border-bottom: 1rpx solid var(--borderColor);
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 28rpx 0;
	font-size: 30rpx;
	color: var(--textColor3);
	font-weight: 500;
	transition: color 0.3s;
	position: relative;
	z-index: 1;
}

.tab-item.tab-active {
	color: var(--themeColor1);
	font-weight: 600;
}

.tab-indicator {
	position: absolute;
	bottom: 0;
	left: 0;
	width: 50%;
	height: 4rpx;
	background: var(--themeColor1);
	border-radius: 4rpx;
	transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-indicator-right {
	transform: translateX(100%);
}

/* Tab 内容区 */
.tab-content {
	padding: 40rpx;
	/* 移除最小高度限制，根据内容自适应 */
}

.tab-panel {
	animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
	from {
		opacity: 0;
		transform: translateY(10rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

/* 微信登录面板 */
.wechat-login-area {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 10rpx 0 20rpx; /* 减少内边距 */
}

/* 移除了 wechat-big-icon 样式 */

.wechat-login-tip {
	font-size: 26rpx;
	color: var(--textColor3); /* 加深一点颜色确保可见 */
	margin-bottom: 40rpx;
	margin-top: 10rpx; /* 稍微给点顶部间距 */
}

.input-group {
	display: flex;
	align-items: center;
	padding: 25rpx 30rpx;
	background-color: var(--bgColor1);
	border-radius: 40rpx;
	margin-bottom: 30rpx;
	transition: all 0.3s;
	border: 2rpx solid transparent;
}

.input-group:focus-within {
	border-color: var(--themeColor1);
	box-shadow: 0 0 10rpx var(--themeColor1);
	opacity: 0.2;
}

.input-group .iconfont {
	font-size: 40rpx;
	color: var(--themeColor1);
	margin-right: 20rpx;
}

.input-field {
	flex: 1;
	font-size: 28rpx;
	color: var(--textColor1);
}

/* 社区合约勾选样式 */
.agreement-box {
	display: flex;
	align-items: center;
	padding: 20rpx 40rpx 30rpx;
	border-top: 1rpx solid rgba(0, 0, 0, 0.04);
}

.checkbox-wrapper {
	padding: 10rpx;
}

.checkbox {
	width: 36rpx;
	height: 36rpx;
	border: 2rpx solid var(--textColor4);
	border-radius: 6rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s;
}

.checkbox-checked {
	background-color: var(--themeColor1);
	border-color: var(--themeColor1);
}

.checkbox-icon {
	color: #fff;
	font-size: 24rpx;
	font-weight: bold;
}

.agreement-text {
	font-size: 26rpx;
	color: var(--textColor3);
	margin-left: 10rpx;
}

.agreement-link {
	color: var(--themeColor1);
	text-decoration: underline;
}

.login-btn {
	width: 100%;
	height: 90rpx;
	background: var(--themeColor1);
	color: var(--themeTextColor);
	font-size: 32rpx;
	font-weight: 600;
	border-radius: 12rpx;
	margin-top: 40rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 8rpx 24rpx var(--bgboxShadowColor1);
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
	box-shadow: 0 4rpx 12rpx var(--bgboxShadowColor1);
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
	background-color: var(--bgColor2);
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 10rpx 30rpx var(--bgboxShadowColor2);
}

.agreement-popup-title {
	font-size: 32rpx;
	font-weight: bold;
	text-align: center;
	padding: 30rpx 0;
	border-bottom: 1rpx solid var(--borderColor);
	color: var(--textColor1);
}

.agreement-popup-content {
	padding: 30rpx;
	max-height: 700rpx;
}

.agreement-popup-text {
	font-size: 26rpx;
	color: var(--textColor1);
	line-height: 1.6;
}

.agreement-section {
	margin-bottom: 30rpx;
}

.agreement-section-title {
	font-weight: bold;
	margin-bottom: 10rpx;
	color: var(--textColor1);
}

.agreement-section-content {
	color: var(--textColor3);
	padding-left: 20rpx;
}

.agreement-popup-btns {
	display: flex;
	border-top: 1rpx solid var(--borderColor);
}

.agreement-popup-btn {
	flex: 1;
	height: 90rpx;
	line-height: 90rpx;
	text-align: center;
	font-size: 30rpx;
	color: var(--themeColor1);
	background-color: var(--bgColor2);
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
