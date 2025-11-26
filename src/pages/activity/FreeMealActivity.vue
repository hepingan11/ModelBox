<template>
	<view class="page-container">
		<!-- 顶部Banner区域 -->
		<view class="banner-section">
			<view class="banner-title">每日免单</view>
			<view class="banner-subtitle">幸运降临 • 惊喜不断</view>
			<view class="rule-tips">
				<text>每人每天可抽取一次</text>
				<text>免单券仅限分享给好友使用</text>
			</view>
		</view>

		<!-- 抽奖操作区域 -->
		<view class="action-section">
			<!-- 兑换码区域 -->
			<view class="code-exchange-card">
				<view class="exchange-header">
					<text class="exchange-title">兑换优惠券</text>
				</view>
				<view class="exchange-input-box">
					<input 
						type="text" 
						v-model="exchangeCode" 
						placeholder="请输入兑换码" 
						class="code-input"
						placeholder-class="input-placeholder"
					/>
					<button class="exchange-btn" @click="redeemCode" :disabled="!exchangeCode">兑换</button>
				</view>
			</view>

			<view class="draw-card">
				<image src="/static/icon/gift-box.png" class="gift-icon" mode="aspectFit"></image>
				<button class="draw-btn" @click="drawCoupon" :disabled="isDrawing">
					{{ isDrawing ? '抽取中...' : '立即抽取免单券' }}
				</button>
				<view class="record-link" @click="openMyRecords">
					<text>查看我的优惠券记录 ></text>
				</view>
			</view>
		</view>

		<!-- 今日幸运用户列表 -->
		<view class="list-section">
			<view class="section-header">
				<view class="header-line"></view>
				<text class="header-title">今日幸运榜</text>
				<view class="header-line"></view>
			</view>
			
			<view class="lucky-users-container" v-if="luckyUsers.length > 0">
				<view class="user-grid">
					<view class="user-item" v-for="(user, index) in luckyUsers" :key="user.userId || index">
						<image :src="user.avatar || '/static/default-avatar.png'" class="user-avatar" mode="aspectFill"></image>
						<text class="user-name">{{ formatName(user.userName || user.nickName) }}</text>
					</view>
				</view>
			</view>
			
			<view class="empty-state" v-else>
				<text class="empty-text">今天还没有幸运儿，快来当第一个吧！</text>
			</view>
		</view>

		<!-- 抽中弹窗 -->
		<view class="popup-mask" v-if="showResultPopup" @click="closePopup">
			<view class="popup-content" @click.stop>
				<view class="popup-header">
					<text class="popup-title">恭喜获得免单券！</text>
					<text class="close-icon" @click="closePopup">×</text>
				</view>
				
				<view class="ticket-box">
					<view class="ticket-code">{{ couponCode }}</view>
					<view class="ticket-label">免单券码</view>
				</view>
				
				<view class="tips-box">
					<text class="warning-icon">⚠️</text>
					<text class="warning-text">注意：该免单券仅限分享给好友使用，本人不可用。</text>
				</view>
				
				<button class="share-btn" open-type="share">
					<text class="share-icon">📤</text>
					<text>分享给好友</text>
				</button>
			</view>
		</view>

		<!-- 我的记录弹窗 -->
		<view class="popup-mask" v-if="showRecordPopup" @click="closeRecordPopup">
			<view class="popup-content record-popup" @click.stop>
				<view class="popup-header">
					<text class="popup-title">我的免单券</text>
					<text class="close-icon" @click="closeRecordPopup">×</text>
				</view>
				
				<scroll-view scroll-y class="record-list" v-if="myCoupons.length > 0">
					<view class="record-item" v-for="(item, index) in myCoupons" :key="index">
						<view class="record-info">
							<text class="record-code">{{ item.code }}</text>
							<text class="record-time">{{ item.createTime }}</text>
						</view>
						<view class="record-status" :class="{ used: item.status === 1 }">
							{{ item.status === 1 ? '已使用' : '未使用' }}
						</view>
					</view>
				</scroll-view>
				
				<view class="empty-record" v-else>
					<image src="/static/empty.png" class="empty-icon"></image>
					<text class="empty-text">暂无免单记录</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShareAppMessage } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const luckyUsers = ref([])
const couponCode = ref('')
const showResultPopup = ref(false)
const isDrawing = ref(false)
const showRecordPopup = ref(false)
const myCoupons = ref([])
const exchangeCode = ref('')

// 获取今日幸运用户
const getTodayLuckyUsers = async () => {
	try {
		const res = await request('/activity/freeMeal/todayList', {
			method: 'GET'
		})
		if (res.code === 200) {
			luckyUsers.value = res.data || []
		}
	} catch (error) {
		console.error('获取幸运用户列表失败:', error)
	}
}

// 兑换优惠券
const redeemCode = async () => {
	if (!exchangeCode.value) return
	
	try {
		const res = await request('/activity/freeMeal/userCode', {
			method: 'GET',
			data: {
				code: exchangeCode.value
			}
		})
		
		if (res.code === 200) {
			uni.showToast({
				title: '兑换成功',
				icon: 'success'
			})
			// 跳转到我的优惠券页面
			setTimeout(() => {
				uni.navigateTo({
					url: '/pages/user/myCoupon'
				})
			}, 1500)
		} else {
			uni.showToast({
				title: res.msg || '兑换失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('兑换失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	}
}

// 抽取免单券
const drawCoupon = async () => {
	if (isDrawing.value) return
	isDrawing.value = true
	
	try {
		const res = await request('/activity/freeMeal/getFreeMeal', {
			method: 'GET'
		})
		
		if (res.code === 200 && res.data) {
			couponCode.value = res.data
			showResultPopup.value = true
		} else {
			uni.showToast({
				title: res.msg || '未满足获取条件或今日已领完',
				icon: 'none',
				duration: 2000
			})
		}
	} catch (error) {
		console.error('抽取失败:', error)
		uni.showToast({
			title: '网络繁忙，请稍后重试',
			icon: 'none'
		})
	} finally {
		isDrawing.value = false
	}
}

const closePopup = () => {
	showResultPopup.value = false
}

const openMyRecords = () => {
	showRecordPopup.value = true
	getMyCoupons()
}

const closeRecordPopup = () => {
	showRecordPopup.value = false
}

const getMyCoupons = async () => {
	try {
		const res = await request('/activity/freeMeal/myList', {
			method: 'GET'
		})
		if (res.code === 200) {
			myCoupons.value = res.data || []
		}
	} catch (error) {
		console.error('获取记录失败:', error)
	}
}

const formatName = (name) => {
	if (!name) return '神秘用户'
	if (name.length > 4) {
		return name.substring(0, 4) + '...'
	}
	return name
}

// 分享配置
onShareAppMessage(() => {
	return {
		title: '送你一张免单券！快来领取吧~',
		path: '/pages/index/index', // 或者跳转到具体的领券页面
		imageUrl: '/static/share-cover.png' // 建议配置一个分享图
	}
})

onMounted(() => {
	getTodayLuckyUsers()
})
</script>

<style lang="scss" scoped>
.page-container {
	min-height: 100vh;
	background: linear-gradient(180deg, #FF5252 0%, #FF8A80 100%);
	padding-bottom: 40rpx;
}

.banner-section {
	padding: 80rpx 40rpx 40rpx;
	text-align: center;
	color: #fff;
}

.banner-title {
	font-size: 60rpx;
	font-weight: bold;
	margin-bottom: 16rpx;
	text-shadow: 0 4rpx 8rpx rgba(163, 0, 0, 0.3);
}

.banner-subtitle {
	font-size: 32rpx;
	opacity: 0.9;
	margin-bottom: 30rpx;
}

.rule-tips {
	display: flex;
	flex-direction: column;
	font-size: 24rpx;
	opacity: 0.8;
	gap: 8rpx;
	background: rgba(255, 255, 255, 0.15);
	padding: 16rpx;
	border-radius: 12rpx;
	display: inline-flex;
}

.code-exchange-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.exchange-header {
	margin-bottom: 20rpx;
}

.exchange-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	position: relative;
	padding-left: 20rpx;
	
	&::before {
		content: '';
		position: absolute;
		left: 0;
		top: 50%;
		transform: translateY(-50%);
		width: 8rpx;
		height: 32rpx;
		background: #FF5252;
		border-radius: 4rpx;
	}
}

.exchange-input-box {
	display: flex;
	gap: 20rpx;
}

.code-input {
	flex: 1;
	height: 80rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
	padding: 0 24rpx;
	font-size: 28rpx;
}

.exchange-btn {
	width: 160rpx;
	height: 80rpx;
	line-height: 80rpx;
	background: #FF5252;
	color: #fff;
	font-size: 28rpx;
	border-radius: 12rpx;
	margin: 0;
	
	&[disabled] {
		background: #ccc;
		color: #fff;
	}
}

.action-section {
	padding: 0 40rpx;
	margin-bottom: 60rpx;
}

.draw-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.gift-icon {
	width: 160rpx;
	height: 160rpx;
	margin-bottom: 30rpx;
}

.draw-btn {
	width: 100%;
	background: linear-gradient(90deg, #FF5252, #FF1744);
	color: #fff;
	font-size: 36rpx;
	font-weight: bold;
	border-radius: 50rpx;
	padding: 20rpx 0;
	box-shadow: 0 6rpx 16rpx rgba(255, 82, 82, 0.4);
	
	&:active {
		transform: scale(0.98);
		opacity: 0.9;
	}
	
	&[disabled] {
		opacity: 0.6;
		background: #ccc;
	}
}

.record-link {
	margin-top: 30rpx;
	font-size: 26rpx;
	color: #666;
	text-decoration: underline;
}

.list-section {
	margin: 0 30rpx;
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 30rpx;
}

.section-header {
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 30rpx;
}

.header-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin: 0 20rpx;
}

.header-line {
	width: 60rpx;
	height: 2rpx;
	background: #ddd;
}

.user-grid {
	display: flex;
	flex-wrap: wrap;
	margin: 0 -10rpx;
}

.user-item {
	width: 25%;
	padding: 0 10rpx;
	margin-bottom: 30rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	box-sizing: border-box;
}

.user-avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	border: 4rpx solid #FFE0B2;
	margin-bottom: 10rpx;
}

.user-name {
	font-size: 24rpx;
	color: #333;
	margin-bottom: 6rpx;
	text-align: center;
	width: 100%;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.lucky-tag {
	font-size: 20rpx;
	color: #fff;
	background: #FF9800;
	padding: 2rpx 12rpx;
	border-radius: 20rpx;
}

.empty-state {
	text-align: center;
	padding: 60rpx 0;
}

.empty-text {
	color: #999;
	font-size: 28rpx;
}

/* 弹窗样式 */
.popup-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.7);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
	animation: fadeIn 0.2s ease;
}

.popup-content {
	width: 600rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 40rpx;
	position: relative;
	animation: popUp 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.popup-header {
	text-align: center;
	margin-bottom: 40rpx;
	position: relative;
}

.popup-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #FF5252;
}

.close-icon {
	position: absolute;
	right: -20rpx;
	top: -20rpx;
	font-size: 40rpx;
	color: #999;
	padding: 10rpx;
}

.ticket-box {
	background: #FFF8E1;
	border: 2rpx dashed #FFB74D;
	border-radius: 12rpx;
	padding: 30rpx;
	text-align: center;
	margin-bottom: 30rpx;
}

.ticket-code {
	font-size: 48rpx;
	font-weight: bold;
	color: #333;
	letter-spacing: 4rpx;
	margin-bottom: 10rpx;
}

.ticket-label {
	font-size: 24rpx;
	color: #FF9800;
}

.tips-box {
	display: flex;
	background: #F5F5F5;
	padding: 20rpx;
	border-radius: 8rpx;
	margin-bottom: 40rpx;
}

.warning-icon {
	font-size: 28rpx;
	margin-right: 10rpx;
}

.warning-text {
	font-size: 26rpx;
	color: #666;
	line-height: 1.4;
	flex: 1;
}

.share-btn {
	background: #07C160;
	color: #fff;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	padding: 20rpx 0;
	border-radius: 50rpx;
	border: none;
	
	&::after {
		border: none;
	}
}

.share-icon {
	font-size: 36rpx;
	margin-right: 10rpx;
}

.record-popup {
	height: 700rpx;
	display: flex;
	flex-direction: column;
}

.record-list {
	flex: 1;
	height: 0;
}

.record-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 24rpx 0;
	border-bottom: 1rpx solid #eee;
}

.record-info {
	display: flex;
	flex-direction: column;
}

.record-code {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 6rpx;
}

.record-time {
	font-size: 22rpx;
	color: #999;
}

.record-status {
	font-size: 24rpx;
	color: #FF5252;
	background: rgba(255, 82, 82, 0.1);
	padding: 4rpx 16rpx;
	border-radius: 20rpx;
}

.record-status.used {
	color: #999;
	background: #f5f5f5;
}

.empty-record {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 100%;
}

@keyframes fadeIn {
	from { opacity: 0; }
	to { opacity: 1; }
}

@keyframes popUp {
	from { transform: scale(0.8); opacity: 0; }
	to { transform: scale(1); opacity: 1; }
}
</style>