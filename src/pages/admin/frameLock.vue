<template>
	<view class="frame-lock-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<text class="nav-title">解锁用户头像框</text>
		</view>
		
		<!-- 用户信息 -->
		<view class="user-info-section" v-if="userInfo">
			<view class="user-avatar-section">
				<image :src="userInfo.avatar || '/static/avatar/default.png'" class="user-avatar" mode="aspectFill"></image>
				<image v-if="userInfo.avatarFrameUrl" :src="userInfo.avatarFrameUrl" class="avatar-frame" mode="aspectFit"></image>
			</view>
			<view class="user-details">
				<text class="user-name">{{ userInfo.userName }}</text>
				<text class="user-id">ID: {{ userInfo.id }}</text>
			</view>
		</view>
		
		<!-- 头像框列表 -->
		<view class="frames-section">
			<view class="section-title">
				<text class="title-text">头像框列表</text>
				<text class="subtitle-text">点击解锁或锁定用户头像框</text>
			</view>
			
			<!-- 加载状态 -->
			<view class="loading-container" v-if="loading">
				<view class="loading-spinner"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 头像框网格 -->
			<view class="frames-grid" v-else>
				<view 
					v-for="(frame, index) in frameList" 
					:key="index" 
					class="frame-item" 
					:class="{ 'unlocked': isFrameUnlocked(frame.id) }"
					@click="toggleFrameLock(frame)"
				>
					<view class="frame-inner">
						<view class="frame-preview">
							<image :src="userInfo.avatar || '/static/avatar/default.png'" mode="aspectFill" class="frame-avatar"></image>
							<image :src="frame.url" class="frame-overlay" mode="aspectFit"></image>
							<view class="lock-status">
								<text class="lock-icon" v-if="!isFrameUnlocked(frame.id)">🔒</text>
								<text class="unlock-icon" v-else>🔓</text>
							</view>
						</view>
						<text class="frame-name">{{ frame.name }}</text>
						<text class="frame-desc">{{ frame.introduce }}</text>
						<view class="frame-status" :class="{'status-unlocked': isFrameUnlocked(frame.id), 'status-locked': !isFrameUnlocked(frame.id)}">
							{{ isFrameUnlocked(frame.id) ? '已解锁' : '未解锁' }}
						</view>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-state" v-if="!loading && frameList.length === 0">
				<text class="empty-text">暂无可用头像框</text>
			</view>
		</view>
		
		<!-- 底部按钮 -->
		<view class="button-container">
			<button class="back-btn" @click="goBack">返回</button>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

// 状态变量
const userId = ref('');
const userInfo = ref(null);
const frameList = ref([]);
const unlockedFrameIds = ref([]);
const loading = ref(true);

// 页面加载时获取参数
onMounted(() => {
	const pages = getCurrentPages();
	const currentPage = pages[pages.length - 1];
	
	if (currentPage && currentPage.options && currentPage.options.userId) {
		userId.value = currentPage.options.userId;
		initData();
	} else {
		uni.showToast({
			title: '缺少用户ID参数',
			icon: 'none'
		});
		setTimeout(() => {
			goBack();
		}, 1500);
	}
});

// 初始化数据
const initData = async () => {
	loading.value = true;
	
	try {
		// 并行请求数据
		await Promise.all([
			getUserInfo(),
			getFrameList(),
			getUserFrameList()
		]);
	} catch (error) {
		console.error('初始化数据失败:', error);
	} finally {
		loading.value = false;
	}
};

// 获取用户信息
const getUserInfo = async () => {
	try {
		const res = await request('/user/getUserInfoById', {
			method: 'GET',
			data: { userId: userId.value }
		});
		
		if (res.code === 200) {
			userInfo.value = res.data;
		} else {
			uni.showToast({
				title: res.msg || '获取用户信息失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('获取用户信息失败:', error);
		uni.showToast({
			title: '获取用户信息失败',
			icon: 'none'
		});
	}
};

// 获取所有头像框列表
const getFrameList = async () => {
	try {
		const res = await request('/admin/frame/listAll', {
			method: 'GET'
		});
		
		if (res.code === 200) {
			frameList.value = res.data || [];
		} else {
			uni.showToast({
				title: res.msg || '获取头像框列表失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('获取头像框列表失败:', error);
		uni.showToast({
			title: '获取头像框列表失败',
			icon: 'none'
		});
	}
};

// 获取用户已解锁的头像框列表
const getUserFrameList = async () => {
	try {
		const res = await request('/admin/frame/lockList', {
			method: 'GET',
			data: { userId: userId.value }
		});
		
		if (res.code === 200) {
			unlockedFrameIds.value = res.data || [];
		} else {
			uni.showToast({
				title: res.msg || '获取已解锁头像框失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('获取已解锁头像框失败:', error);
		uni.showToast({
			title: '获取已解锁头像框失败',
			icon: 'none'
		});
	}
};

// 检查头像框是否已解锁
const isFrameUnlocked = (frameId) => {
	return unlockedFrameIds.value.includes(frameId);
};

// 切换头像框解锁状态
const toggleFrameLock = async (frame) => {
	const isUnlocked = isFrameUnlocked(frame.id);
	const action = isUnlocked ? '锁定' : '解锁';
	
	uni.showModal({
		title: `确认${action}头像框`,
		content: `确定要${action}用户 "${userInfo.value.userName}" 的 "${frame.name}" 头像框吗？`,
		success: async (res) => {
			if (res.confirm) {
				try {
					uni.showLoading({
						title: `${action}中...`
					});
					
					const endpoint = isUnlocked ? '/admin/frame/lock' : '/admin/frame/unlock';
					
					const result = await request(endpoint, {
						method: 'POST',
						data: {
							userId: userId.value,
							id: frame.id
						}
					});
					
					if (result.code === 200) {
						uni.showToast({
							title: `${action}成功`,
							icon: 'success'
						});
						
						// 更新本地解锁状态
						if (isUnlocked) {
							unlockedFrameIds.value = unlockedFrameIds.value.filter(id => id !== frame.id);
						} else {
							unlockedFrameIds.value.push(frame.id);
						}
					} else {
						uni.showToast({
							title: result.msg || `${action}失败`,
							icon: 'none'
						});
					}
				} catch (error) {
					console.error(`${action}头像框失败:`, error);
					uni.showToast({
						title: `${action}失败，请重试`,
						icon: 'none'
					});
				} finally {
					uni.hideLoading();
				}
			}
		}
	});
};

// 返回上一页
const goBack = () => {
	uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.frame-lock-container {
	min-height: 100vh;
	background-color: #f5f7fa;
	padding-bottom: 140rpx; /* 为底部按钮留出空间 */
}

/* 导航栏样式 */
.nav-bar {
	background-color: #fff;
	padding: 20rpx 30rpx;
	border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.nav-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

/* 用户信息部分 */
.user-info-section {
	background-color: #fff;
	margin-top: 20rpx;
	padding: 30rpx;
	display: flex;
	align-items: center;
}

.user-avatar-section {
	position: relative;
	width: 120rpx;
	height: 120rpx;
	margin-right: 30rpx;
}

.user-avatar {
	width: 100%;
	height: 100%;
	border-radius: 60rpx;
	border: 4rpx solid #fff;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
	z-index: 1;
}

.avatar-frame {
	position: absolute;
	top: -20rpx;
	left: -20rpx;
	width: 160rpx;
	height: 160rpx;
	z-index: 2;
}

.user-details {
	flex: 1;
}

.user-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 10rpx;
	display: block;
}

.user-id {
	font-size: 24rpx;
	color: #999;
}

/* 头像框列表部分 */
.frames-section {
	background-color: #fff;
	margin-top: 20rpx;
	padding: 30rpx;
}

.section-title {
	margin-bottom: 30rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	display: block;
}

.subtitle-text {
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
	display: block;
}

.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 60rpx 0;
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #1890ff;
	border-radius: 50%;
	animation: spin 1s linear infinite;
	margin-bottom: 20rpx;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text {
	color: #999;
	font-size: 28rpx;
}

.frames-grid {
	display: flex;
	flex-wrap: wrap;
	margin: 0 -10rpx;
}

.frame-item {
	width: 33.33%;
	padding: 10rpx;
	box-sizing: border-box;
}

.frame-inner {
	background-color: #f9f9f9;
	border-radius: 12rpx;
	padding: 20rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	transition: all 0.3s;
}

.frame-item.unlocked .frame-inner {
	background-color: rgba(82, 196, 26, 0.05);
	border: 1px solid #52c41a;
}

.frame-preview {
	position: relative;
	width: 120rpx;
	height: 120rpx;
	margin-bottom: 16rpx;
}

.frame-avatar {
	width: 100%;
	height: 100%;
	border-radius: 60rpx;
	z-index: 1;
}

.frame-overlay {
	position: absolute;
	top: -20rpx;
	left: -20rpx;
	width: 160rpx;
	height: 160rpx;
	z-index: 2;
}

.lock-status {
	position: absolute;
	bottom: -10rpx;
	right: -10rpx;
	width: 40rpx;
	height: 40rpx;
	background-color: #fff;
	border-radius: 50%;
	display: flex;
	justify-content: center;
	align-items: center;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
	z-index: 3;
}

.lock-icon, .unlock-icon {
	font-size: 24rpx;
}

.frame-name {
	font-size: 28rpx;
	color: #333;
	margin-bottom: 8rpx;
	text-align: center;
}

.frame-desc {
	font-size: 24rpx;
	color: #999;
	text-align: center;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	margin-bottom: 10rpx;
}

.frame-status {
	font-size: 22rpx;
	padding: 4rpx 12rpx;
	border-radius: 20rpx;
	margin-top: 10rpx;
}

.status-unlocked {
	background-color: rgba(82, 196, 26, 0.1);
	color: #52c41a;
}

.status-locked {
	background-color: rgba(0, 0, 0, 0.05);
	color: #999;
}

/* 空状态 */
.empty-state {
	padding: 60rpx 0;
	text-align: center;
}

.empty-text {
	color: #999;
	font-size: 28rpx;
}

/* 底部按钮 */
.button-container {
	position: fixed;
	bottom: 0;
	left: 0;
	width: 100%;
	padding: 30rpx;
	background-color: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
	display: flex;
	justify-content: center;
	z-index: 10;
	box-sizing: border-box;
}

.back-btn {
	width: 80%;
	height: 80rpx;
	line-height: 80rpx;
	text-align: center;
	border-radius: 40rpx;
	font-size: 28rpx;
	background-color: #1890ff;
	color: #fff;
}
</style>