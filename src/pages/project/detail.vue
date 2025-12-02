<template>
	<view class="project-detail-page">
		<!-- 加载状态 -->
		<view v-if="loading" class="loading-container">
			<view class="loading-spinner"></view>
			<text class="loading-text">加载中...</text>
		</view>
		
		<!-- 项目详情 -->
		<view v-else-if="project" class="detail-content">
			<!-- 项目图片轮播 -->
			<view class="image-section">
				<swiper 
					v-if="project.imageUrlList && project.imageUrlList.length > 0"
					class="image-swiper" 
					indicator-dots 
					autoplay 
					circular
					indicator-color="rgba(255, 255, 255, 0.5)"
					indicator-active-color="#00A872"
				>
					<swiper-item v-for="(img, index) in project.imageUrlList" :key="index">
						<image :src="img" class="swiper-image" mode="aspectFill"></image>
					</swiper-item>
				</swiper>
				<image 
					v-else
					src="/static/default-project.png" 
					class="default-image" 
					mode="aspectFill"
				></image>
			</view>
			
			<!-- 项目基本信息 -->
			<view class="info-card">
				<view class="project-header">
					<text class="project-name">{{ project.projectName }}</text>
					<view :class="['status-badge', 'status-' + project.status]">
						<text class="status-text">{{ getStatusText(project.status) }}</text>
					</view>
				</view>
				
				<!-- 项目标签 -->
				<view class="project-tags">
					<view class="tag" v-if="project.projectFieldName">
						<image src="/static/icon/Industry.png" class="tag-icon"></image>
						<text class="tag-text">{{ project.projectFieldName }}</text>
					</view>
					<view class="tag" v-if="project.city">
						<image src="/static/icon/local.png" class="tag-icon"></image>
						<text class="tag-text">{{ project.city }}</text>
					</view>
					<view class="tag" v-if="project.ageRequirement">
						<text class="tag-icon">👤</text>
						<text class="tag-text">{{ project.ageRequirement }}</text>
					</view>
				</view>
				
				<!-- 创建时间 -->
				<view class="create-time">
					<text class="time-icon">🕒</text>
					<text class="time-text">创建于 {{ formatTime(project.createdTime) }}</text>
				</view>
			</view>
			
			<!-- 项目介绍 -->
			<view class="info-card" v-if="project.introduce">
				<view class="card-title">
					<text class="title-text">项目介绍</text>
				</view>
				<view class="introduce-content">
					<text class="introduce-text">{{ project.introduce }}</text>
				</view>
			</view>
			
			<!-- 项目发起人 -->
			<view class="info-card" v-if="creatorInfo">
				<view class="card-title">
					<text class="title-text">项目发起人</text>
				</view>
				<view class="creator-card" @click.stop="goToUserSpace(creatorInfo.userId || creatorInfo.id)">
					<view class="avatar-wrapper">
						<image 
							:src="creatorInfo.avatar || '/static/default-avatar.png'" 
							class="creator-avatar"
						></image>
						<!-- 头像框 -->
						<image v-if="creatorInfo.avatarFrameUrl" :src="creatorInfo.avatarFrameUrl" class="avatar-frame-large" mode="aspectFit"></image>
					</view>
					<view class="creator-info">
						<view class="creator-name-row">
							<text class="creator-name">{{ creatorInfo.username || creatorInfo.nickname || '用户' }}</text>
							<view v-if="creatorInfo.level" class="creator-level" :class="'level-' + creatorInfo.level">
								<text class="level-text">Lv.{{ creatorInfo.level }}</text>
							</view>
						</view>
						<text v-if="creatorInfo.signature" class="creator-signature">{{ creatorInfo.signature }}</text>
					</view>
					<text class="view-arrow">→</text>
				</view>
			</view>
			
			<!-- 项目成员 -->
			<view class="info-card" v-if="project.memberList && project.memberList.length > 0">
				<view class="card-title">
					<text class="title-text">项目成员</text>
					<text class="member-count">{{ project.memberList.length }}人</text>
				</view>
				<view class="member-list">
					<view 
						v-for="(member, index) in project.memberList" 
						:key="index"
						class="member-item"
						:class="{ 'clickable': member.id }"
						@click="goToUserSpace(member.id)"
					>
						<view class="avatar-wrapper">
							<image 
								:src="member.avatar || '/static/default-avatar.png'" 
								class="member-avatar"
							></image>
							<!-- 头像框 -->
							<image v-if="member.avatarFrameUrl" :src="member.avatarFrameUrl" class="avatar-frame" mode="aspectFit"></image>
						</view>
						<view class="member-info">
							<view class="member-name-row">
								<text class="member-name">{{ member.username || member.nickname || '用户' }}</text>
								<view v-if="member.level" class="member-level" :class="'level-' + member.level">
									<text class="level-text">Lv.{{ member.level }}</text>
								</view>
							</view>
							<text class="member-time">申请时间：{{ formatTime(member.createdTime) }}</text>
						</view>
						<view :class="['member-status', getMemberStatusClass(member.status)]">
							{{ getMemberStatusText(member.status) }}
						</view>
					</view>
				</view>
			</view>
			
			<!-- 审核失败原因 -->
			<view class="info-card fail-reason-card" v-if="project.status === 'REJECTED' && project.failReason">
				<view class="card-title">
					<text class="title-text">审核未通过原因</text>
				</view>
				<view class="fail-reason-content">
					<text class="fail-reason-text">{{ project.failReason }}</text>
				</view>
			</view>
		</view>
		
		<!-- 空状态 -->
		<view v-else class="empty-state">
			<image src="/static/empty.png" class="empty-icon"></image>
			<text class="empty-text">项目不存在</text>
		</view>
		
		<!-- 底部操作栏 -->
		<view class="bottom-bar" v-if="project">
			<view class="action-btn chat-btn" @click="handleChat">
				<text class="btn-icon">💬</text>
				<text class="btn-text">聊天</text>
			</view>
			<!-- 创建者：显示解散项目 -->
			<view class="action-btn disband-btn" @click="handleDisband" v-if="isCreator">
				<text class="btn-text">解散项目</text>
			</view>
			<!-- 非创建者且未加入：显示申请加入 -->
			<view class="action-btn join-btn" @click="handleJoin" v-else-if="!isMember">
				<text class="btn-text">申请加入</text>
			</view>
			<!-- 非创建者但已加入：显示退出项目 -->
			<view class="action-btn leave-btn" @click="handleLeave" v-else>
				<text class="btn-text">退出项目</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request'

// 页面参数
const projectId = ref(null)

// 项目详情
const project = ref(null)
const loading = ref(false)

// 项目发起人信息
const creatorInfo = ref(null)

// 计算是否为成员
const isMember = computed(() => {
	if (!project.value || !project.value.memberList) return false
	const currentUserId = uni.getStorageSync('userId')
	if (!currentUserId) return false
	return project.value.memberList.some(m => m.id == currentUserId || m.userId == currentUserId)
})

// 计算是否为项目创建者
const isCreator = computed(() => {
	if (!project.value) return false
	const currentUserId = uni.getStorageSync('userId')
	if (!currentUserId) return false
	return project.value.userId == currentUserId
})

// 获取项目详情
const getProjectDetail = async () => {
	if (!projectId.value) {
		uni.showToast({ title: '项目ID不存在', icon: 'none' })
		return
	}
	
	loading.value = true
	try {
		const res = await request('/project/detail', {
			method: 'GET',
			data: {
				projectId: projectId.value
			}
		})
		
		if (res.code === 200) {
			project.value = res.data
			// 获取项目发起人信息
			if (res.data.userId) {
				getCreatorInfo(res.data.userId)
			}
		} else {
			uni.showToast({ title: res.msg || '获取项目详情失败', icon: 'none' })
		}
	} catch (error) {
		console.error('获取项目详情失败:', error)
		uni.showToast({ title: '获取项目详情失败', icon: 'none' })
	} finally {
		loading.value = false
	}
}

// 获取项目发起人信息
const getCreatorInfo = async (userId) => {
	try {
		console.log('获取发起人信息，userId:', userId)
		
		const res = await request('/user/getUserInfoById', {
			method: 'GET',
			data: {
				userId: userId
			}
		})
		
		if (res.code === 200) {
			creatorInfo.value = res.data
			console.log('发起人信息获取成功:', res.data)
		} else {
			console.error('获取发起人信息失败:', res.msg)
		}
	} catch (error) {
		console.error('获取发起人信息失败:', error)
	}
}

// 格式化时间
const formatTime = (time) => {
	if (!time) return ''
	const date = new Date(time)
	const year = date.getFullYear()
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	return `${year}-${month}-${day}`
}

// 获取状态文本
const getStatusText = (status) => {
	const statusMap = {
		'online': '寻求中',
		'not-online': '已结束',
		'pending': '审核中',
		'rejected': '未通过',
		'offline': '下线'
	}
	return statusMap[status] || status
}

// 获取成员状态文本
const getMemberStatusText = (status) => {
	const statusMap = {
		'pending': '待审核',
		'review': '已通过',
		'reject': '已拒绝'
	}
	return statusMap[status] || status
}

// 获取成员状态样式类
const getMemberStatusClass = (status) => {
	const classMap = {
		'pending': 'status-pending',
		'review': 'status-approved',
		'reject': 'status-rejected'
	}
	return classMap[status] || ''
}

// 聊天功能 - 跳转到与项目发起人的聊天页面
const handleChat = () => {
	if (!project.value) {
		uni.showToast({ title: '项目信息加载中', icon: 'none' })
		return
	}
	
	if (!project.value.userId) {
		uni.showToast({ title: '项目发起人信息不存在', icon: 'none' })
		return
	}
	
	// 检查是否为项目发起人自己
	const currentUserId = uni.getStorageSync('userId')
	if (project.value.userId == currentUserId) {
		uni.showToast({ title: '不能和自己聊天', icon: 'none' })
		return
	}
	
	// 跳转到聊天页面
	uni.navigateTo({
		url: `/pages/message/chat?userId=${project.value.userId}`
	})
}

// 申请加入
const handleJoin = () => {
	if (!project.value) {
		uni.showToast({ title: '项目信息加载中', icon: 'none' })
		return
	}
	
	// 显示选择弹窗：是否匿名加入
	uni.showModal({
		title: '选择加入方式',
		content: '是否以匿名方式加入项目？',
		confirmText: '匿名加入',
		cancelText: '实名加入',
		success: async (res) => {
			const isNick = res.confirm // true=匿名，false=实名
			
			uni.showLoading({ title: '申请中...' })
			
			try {
				const result = await request(`/project/join?projectId=${projectId.value}&isNick=${isNick}`, {
					method: 'GET'
				})
				
				uni.hideLoading()
				
				if (result.code === 200) {
					uni.showToast({ title: '申请成功', icon: 'success' })
					// 刷新项目详情
					setTimeout(() => {
						getProjectDetail()
					}, 1500)
				} else {
					uni.showToast({ title: result.msg || '申请失败', icon: 'none' })
				}
			} catch (error) {
				uni.hideLoading()
				console.error('申请加入失败:', error)
				uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
			}
		}
	})
}

// 退出项目
const handleLeave = () => {
	uni.showModal({
		title: '确认退出',
		content: '确定要退出该项目吗？',
		confirmText: '确定',
		cancelText: '取消',
		success: async (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '退出中...' })
				
				try {
					const result = await request('/project/leave', {
						method: 'GET',
						data: {
							projectId: projectId.value
						}
					})
					
					uni.hideLoading()
					
					if (result.code === 200) {
						uni.showToast({
							title: '退出成功',
							icon: 'success'
						})
						
						// 延迟返回上一页或刷新页面
						setTimeout(() => {
							uni.navigateBack({
								fail: () => {
									// 如果无法返回，则刷新当前页面
									getProjectDetail()
								}
							})
						}, 1500)
					} else {
						uni.showToast({
							title: result.msg || '退出失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.hideLoading()
					console.error('退出项目失败:', error)
					uni.showToast({
						title: '网络异常，请稍后重试',
						icon: 'none'
					})
				}
			}
		}
	})
}

// 解散项目
const handleDisband = () => {
	uni.showModal({
		title: '确认解散',
		content: '解散项目后将无法恢复，所有成员将被移除，确定要解散该项目吗？',
		confirmText: '确定解散',
		confirmColor: '#f44336',
		cancelText: '取消',
		success: async (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '解散中...' })
				
				try {
					const result = await request('/project/disband', {
						method: 'GET',
						data: {
							projectId: projectId.value
						}
					})
					
					uni.hideLoading()
					
					if (result.code === 200) {
						uni.showToast({
							title: '解散成功',
							icon: 'success'
						})
						
						// 延迟返回上一页
						setTimeout(() => {
							uni.navigateBack({
								delta: 1,
								fail: () => {
									// 如果无法返回，跳转到项目列表
									uni.redirectTo({
										url: '/pages/project/projectList'
									})
								}
							})
						}, 1500)
					} else {
						uni.showToast({
							title: result.msg || '解散失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.hideLoading()
					console.error('解散项目失败:', error)
					uni.showToast({
						title: '网络异常，请稍后重试',
						icon: 'none'
					})
				}
			}
		}
	})
}

// 跳转到用户空间
const goToUserSpace = (userId) => {
	if (!userId) {
		console.log('用户ID为空，无法跳转')
		return
	}
	
	console.log('跳转到用户空间，userId:', userId)
	
	uni.navigateTo({
		url: `/pages/user/personal-center?userId=${userId}`,
		fail: (err) => {
			console.error('跳转失败:', err)
			uni.showToast({
				title: '跳转失败',
				icon: 'none'
			})
		}
	})
}

// 页面加载
onLoad((options) => {
	if (options.id) {
		projectId.value = options.id
		getProjectDetail()
	}
})
</script>

<style scoped>
.project-detail-page {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 120rpx;
}

/* 加载状态 */
.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 200rpx 0;
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #f0f0f0;
	border-top-color: #00A872;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

@keyframes spin {
	to { transform: rotate(360deg); }
}

.loading-text {
	margin-top: 20rpx;
	font-size: 28rpx;
	color: #999;
}

/* 详情内容 */
.detail-content {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

/* 图片区域 */
.image-section {
	width: 100%;
	height: 500rpx;
	background-color: #fff;
}

.image-swiper {
	width: 100%;
	height: 100%;
}

.swiper-image {
	width: 100%;
	height: 100%;
}

.default-image {
	width: 100%;
	height: 100%;
}

/* 信息卡片 */
.info-card {
	background-color: #fff;
	padding: 32rpx;
	margin: 0 24rpx;
	border-radius: 16rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.info-card:first-of-type {
	margin-top: 20rpx;
}

/* 项目头部 */
.project-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 24rpx;
}

.project-name {
	flex: 1;
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.4;
}

.status-badge {
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	margin-left: 16rpx;
}

.status-PENDING {
	background-color: #fff3e0;
}

.status-APPROVED,
.status-ACTIVE {
	background-color: #e8f5e9;
}

.status-REJECTED {
	background-color: #ffebee;
}

.status-COMPLETED {
	background-color: #e3f2fd;
}

.status-text {
	font-size: 24rpx;
	color: #666;
}

.status-PENDING .status-text {
	color: #f57c00;
}

.status-APPROVED .status-text,
.status-ACTIVE .status-text {
	color: #00A872;
}

.status-REJECTED .status-text {
	color: #f44336;
}

.status-COMPLETED .status-text {
	color: #2196f3;
}

/* 项目标签 */
.project-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
	margin-bottom: 24rpx;
}

.tag {
	display: flex;
	align-items: center;
	padding: 8rpx 16rpx;
	background-color: #f5f5f5;
	border-radius: 20rpx;
}

.tag-icon {
	width: 24rpx;
	height: 24rpx;
	font-size: 24rpx;
	margin-right: 8rpx;
}

.tag-text {
	font-size: 24rpx;
	color: #666;
}

/* 创建时间 */
.create-time {
	display: flex;
	align-items: center;
	padding-top: 16rpx;
	border-top: 1rpx solid #f0f0f0;
}

.time-icon {
	font-size: 24rpx;
	margin-right: 8rpx;
}

.time-text {
	font-size: 24rpx;
	color: #999;
}

/* 卡片标题 */
.card-title {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 24rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.member-count {
	font-size: 28rpx;
	color: #00A872;
	font-weight: 600;
}

/* 项目介绍 */
.introduce-content {
	line-height: 1.8;
}

.introduce-text {
	font-size: 28rpx;
	color: #666;
	white-space: pre-wrap;
}

/* 项目发起人 */
.creator-card {
	display: flex;
	align-items: center;
	padding: 20rpx;
	background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
	border-radius: 16rpx;
	border: 2rpx solid #e8f5e9;
	cursor: pointer;
	transition: all 0.3s ease;
}

.creator-card:hover {
	background: linear-gradient(135deg, #e8f5e9 0%, #f8f9fa 100%);
	transform: translateY(-2rpx);
	box-shadow: 0 4rpx 12rpx rgba(0, 168, 114, 0.1);
}

.creator-card:active {
	transform: scale(0.98);
}

.creator-avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
}

.avatar-frame-large {
	position: absolute;
	top: -18rpx;
	left: -18rpx;
	width: 136rpx;
	height: 136rpx;
	pointer-events: none;
}

.creator-info {
	flex: 1;
	margin-left: 24rpx;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.creator-name-row {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.creator-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.creator-level {
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
	font-size: 22rpx;
}

.creator-signature {
	font-size: 24rpx;
	color: #999;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.view-arrow {
	font-size: 36rpx;
	color: #00A872;
	margin-left: 16rpx;
}

/* 成员列表 */
.member-list {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.member-item {
	display: flex;
	align-items: center;
	padding: 16rpx;
	background-color: #f9f9f9;
	border-radius: 12rpx;
	transition: all 0.3s ease;
}

.member-item.clickable {
	cursor: pointer;
}

.member-item.clickable:hover {
	background-color: #f0f0f0;
	transform: translateX(4rpx);
}

.member-item.clickable:active {
	background-color: #e8e8e8;
	transform: scale(0.98);
}

.avatar-wrapper {
	position: relative;
	margin-right: 20rpx;
}

.member-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
}

.avatar-frame {
	position: absolute;
	top: -15rpx;
	left: -15rpx;
	width: 110rpx;
	height: 110rpx;
	pointer-events: none;
}

.member-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.member-name-row {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.member-name {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}

.member-level {
	padding: 2rpx 8rpx;
	border-radius: 8rpx;
	font-size: 20rpx;
}

.level-text {
	font-weight: bold;
}

.member-time {
	font-size: 22rpx;
	color: #999;
}

.member-status {
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
	font-size: 22rpx;
	white-space: nowrap;
}

.member-status.status-pending {
	background-color: #fff3e0;
	color: #f57c00;
}

.member-status.status-approved {
	background-color: #e0f7f0;
	color: #00A872;
}

.member-status.status-rejected {
	background-color: #ffebee;
	color: #c62828;
}

/* 审核失败原因 */
.fail-reason-card {
	border-left: 4rpx solid #f44336;
}

.fail-reason-content {
	padding: 16rpx;
	background-color: #ffebee;
	border-radius: 8rpx;
}

.fail-reason-text {
	font-size: 28rpx;
	color: #d32f2f;
	line-height: 1.6;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 200rpx 0;
}

.empty-icon {
	width: 200rpx;
	height: 200rpx;
	margin-bottom: 32rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 底部操作栏 */
.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 20rpx 24rpx;
	background-color: #fff;
	box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
	z-index: 100;
}

.action-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 24rpx;
	border-radius: 48rpx;
	transition: all 0.2s;
}

.action-btn:active {
	transform: scale(0.95);
}

.chat-btn {
	background-color: #e8f5e9;
	gap: 8rpx;
}

.btn-icon {
	font-size: 32rpx;
}

.btn-text {
	font-size: 28rpx;
	font-weight: 600;
}

.chat-btn .btn-text {
	color: #00A872;
	font-weight: 600;
}

.join-btn {
	flex: 1;
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	box-shadow: 0 4rpx 12rpx rgba(0, 168, 114, 0.3);
}

.join-btn .btn-text {
	color: #fff;
}

.leave-btn {
	flex: 1;
	background-color: #fff3e0;
	border: 2rpx solid #ff9800;
	transition: all 0.3s ease;
}

.leave-btn:active {
	background-color: #ffe0b2;
}

.leave-btn .btn-text {
	color: #ff9800;
}

.disband-btn {
	flex: 1;
	background-color: #ffebee;
	border: 2rpx solid #f44336;
	transition: all 0.3s ease;
}

.disband-btn:active {
	background-color: #ffcdd2;
}

.disband-btn .btn-text {
	color: #f44336;
	font-weight: bold;
}
</style>
