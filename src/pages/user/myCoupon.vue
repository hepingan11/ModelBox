<template>
	<view class="page-container">
		<!-- 顶部标签栏 -->
		<view class="tabs">
			<view class="tab-item active">
				<text>我的优惠券</text>
				<view class="tab-line"></view>
			</view>
		</view>
		
		<scroll-view 
			scroll-y 
			class="coupon-list" 
			@scrolltolower="loadMore"
			refresher-enabled
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh"
		>
			<view class="list-content" v-if="couponList.length > 0">
				<view class="coupon-item" v-for="(item, index) in couponList" :key="item.couponId || index">
					<view class="coupon-left">
						<view class="coupon-amount">
							<text class="symbol" v-if="item.type === 1">¥</text>
							<text class="amount">{{ item.discount }}</text>
							<text class="unit" v-if="item.type === 2">折</text>
						</view>
						<view class="coupon-condition">满{{ item.goalPrice }}可用</view>
					</view>
					<view class="coupon-right">
						<view class="coupon-info">
							<text class="coupon-name">{{ item.couponName }}</text>
							<text class="coupon-type-tag">{{ item.type === 1 ? '满减券' : '折扣券' }}</text>
							<text class="coupon-time">领取时间：{{ formatDate(item.createdTime) }}</text>
						</view>
						<view class="coupon-btn">去使用</view>
					</view>
				</view>
				
				<view class="load-more" v-if="hasMore">
					<text>{{ isLoading ? '加载中...' : '上拉加载更多' }}</text>
				</view>
				<view class="no-more" v-else>
					<text>没有更多了</text>
				</view>
			</view>
			
			<view class="empty-state" v-else>
				<image src="/static/empty.png" class="empty-icon"></image>
				<text class="empty-text">暂无优惠券</text>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

const couponList = ref([])
const pageNum = ref(1)
const isLoading = ref(false)
const hasMore = ref(true)
const isRefreshing = ref(false)

const getCouponList = async (reset = false) => {
	if (isLoading.value) return
	isLoading.value = true
	
	if (reset) {
		pageNum.value = 1
		hasMore.value = true
	}
	
	try {
		const res = await request('/coupon/myList', {
			method: 'GET',
			data: {
				pageNum: pageNum.value
			}
		})
		
		if (res.code === 200) {
			const list = res.data || []
			if (reset) {
				couponList.value = list
			} else {
				couponList.value = [...couponList.value, ...list]
			}
			
			// 假设每页10条，如果返回少于10条说明没有更多了
			if (list.length < 10) {
				hasMore.value = false
			} else {
				pageNum.value++
			}
		}
	} catch (error) {
		console.error('获取优惠券列表失败:', error)
		uni.showToast({
			title: '获取失败，请重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
		isRefreshing.value = false
	}
}

const loadMore = () => {
	if (hasMore.value && !isLoading.value) {
		getCouponList()
	}
}

const onRefresh = () => {
	isRefreshing.value = true
	getCouponList(true)
}

const formatDate = (dateStr) => {
	if (!dateStr) return ''
	const date = new Date(dateStr)
	return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
	getCouponList(true)
})
</script>

<style lang="scss" scoped>
.page-container {
	min-height: 100vh;
	background-color: #f5f7fa;
	display: flex;
	flex-direction: column;
}

.tabs {
	background: #fff;
	display: flex;
	justify-content: center;
	padding: 0 30rpx;
	position: sticky;
	top: 0;
	z-index: 10;
}

.tab-item {
	padding: 24rpx 0;
	font-size: 30rpx;
	color: #333;
	position: relative;
	font-weight: bold;
}

.tab-line {
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 40rpx;
	height: 4rpx;
	background: #FF5252;
	border-radius: 2rpx;
}

.coupon-list {
	flex: 1;
	height: 0;
	padding: 20rpx;
}

.list-content {
	padding-bottom: 30rpx;
}

.coupon-item {
	display: flex;
	background: #fff;
	margin-bottom: 20rpx;
	border-radius: 16rpx;
	overflow: hidden;
	position: relative;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.coupon-left {
	width: 200rpx;
	background: linear-gradient(135deg, #FF8A80 0%, #FF5252 100%);
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	color: #fff;
	padding: 20rpx;
	position: relative;
	border-right: 2rpx dashed rgba(255, 255, 255, 0.5);
	
	&::after {
		content: '';
		position: absolute;
		right: -10rpx;
		top: 50%;
		transform: translateY(-50%);
		width: 20rpx;
		height: 20rpx;
		background: #f5f7fa;
		border-radius: 50%;
	}
}

.coupon-amount {
	display: flex;
	align-items: baseline;
}

.symbol {
	font-size: 28rpx;
	margin-right: 4rpx;
}

.amount {
	font-size: 56rpx;
	font-weight: bold;
}

.unit {
	font-size: 28rpx;
	margin-left: 4rpx;
}

.coupon-condition {
	font-size: 22rpx;
	margin-top: 8rpx;
	opacity: 0.9;
}

.coupon-right {
	flex: 1;
	padding: 24rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.coupon-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.coupon-name {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 12rpx;
}

.coupon-type-tag {
	font-size: 20rpx;
	color: #FF5252;
	border: 1rpx solid #FF5252;
	padding: 2rpx 10rpx;
	border-radius: 8rpx;
	align-self: flex-start;
	margin-bottom: 12rpx;
}

.coupon-time {
	font-size: 22rpx;
	color: #999;
}

.coupon-btn {
	padding: 10rpx 24rpx;
	background: #FF5252;
	color: #fff;
	font-size: 24rpx;
	border-radius: 30rpx;
	margin-left: 20rpx;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 100rpx;
}

.empty-icon {
	width: 200rpx;
	height: 200rpx;
	margin-bottom: 30rpx;
}

.empty-text {
	color: #999;
	font-size: 28rpx;
}

.load-more, .no-more {
	text-align: center;
	padding: 20rpx 0;
	color: #999;
	font-size: 24rpx;
}
</style>
