<template>
	<view class="frame-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<text class="nav-title">选择头像框</text>
		</view>
		
		<!-- 当前头像预览 -->
		<view class="preview-section">
			<view class="avatar-preview">
				<image :src="userInfo.avatar" mode="aspectFill" class="avatar-img"></image>
				<image v-if="currentFrame" :src="currentFrame.url" class="frame-img" mode="aspectFit"></image>
			</view>
			<text class="preview-text">当前预览效果</text>
		</view>
		
		<!-- 头像框列表 -->
		<view class="frames-section">
			<view class="section-title">选择头像框</view>
			
			<!-- 加载状态 -->
			<view class="loading-container" v-if="loading">
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 头像框网格 -->
			<view class="frames-grid" v-else>
				<!-- 无头像框选项 -->
				<view class="frame-item" :class="{ 'active': selectedFrameUrl === null }" @click="selectFrame(null)">
					<view class="frame-inner">
						<image :src="userInfo.avatar" mode="aspectFill" class="frame-avatar"></image>
						<text class="frame-name">无头像框</text>
					</view>
				</view>
				
				<!-- 头像框列表 -->
				<view 
					v-for="(frame, index) in frameList" 
					:key="index" 
					class="frame-item" 
					:class="{ 'active': selectedFrameUrl === frame.url, 'locked': !isFrameUnlocked(frame.id) }"
					@click="selectFrame(frame)"
				>
					<view class="frame-inner">
						<view class="frame-preview">
							<image :src="userInfo.avatar" mode="aspectFill" class="frame-avatar"></image>
							<image :src="frame.url" class="frame-overlay" mode="aspectFit"></image>
							<view class="lock-icon" v-if="!isFrameUnlocked(frame.id)">
								<text class="lock-text">🔒</text>
							</view>
						</view>
						<text class="frame-name">{{ frame.name }}</text>
						<text class="frame-desc">{{ frame.introduce }}</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 底部操作按钮 -->
		<view class="button-container">
			<button class="save-btn" @click="saveFrameSelection" :disabled="!hasChanges">应用选择</button>
			<button class="cancel-btn" @click="goBack">返回</button>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import request from '@/utils/request.js';

// 用户信息
const userInfo = ref({
	avatar: '',
	avatarFrameUrl: ''
});

// 头像框列表
const frameList = ref([]);
// 用户已解锁的头像框ID列表
const unlockedFrameIds = ref([]);
// 加载状态
const loading = ref(true);
// 选中的头像框URL
const selectedFrameUrl = ref(null);
// 初始头像框URL（用于检测是否有变更）
const initialFrameUrl = ref(null);

// 计算当前预览的头像框
const currentFrame = computed(() => {
	if (selectedFrameUrl.value === null) {
		return null;
	}
	
	return frameList.value.find(frame => frame.url === selectedFrameUrl.value);
});

// 计算是否有变更
const hasChanges = computed(() => {
	return selectedFrameUrl.value !== initialFrameUrl.value;
});

// 检查头像框是否已解锁
const isFrameUnlocked = (frameId) => {
	return unlockedFrameIds.value.includes(frameId);
};

// 选择头像框
const selectFrame = (frame) => {
	if (frame === null) {
		// 选择无头像框
		selectedFrameUrl.value = null;
		return;
	}
	
	// 检查头像框是否已解锁
	if (!isFrameUnlocked(frame.id)) {
		uni.showToast({
			title: '该头像框尚未解锁',
			icon: 'none'
		});
		return;
	}
	
	selectedFrameUrl.value = frame.url;
};

// 保存头像框选择
const saveFrameSelection = async () => {
	if (!hasChanges.value) {
		uni.showToast({
			title: '未做任何修改',
			icon: 'none'
		});
		return;
	}
	
	try {
		uni.showLoading({
			title: '保存中...'
		});
		
		const res = await request('/user/updateFrame', {
			method: 'POST',
			data: selectedFrameUrl.value
		});
		
		if (res.code === 200) {
			uni.showToast({
				title: '头像框更新成功',
				icon: 'success'
			});
			
			// 跳转回上一页
			setTimeout(() => {
				uni.navigateBack();
			}, 1500);
		} else {
			uni.showToast({
				title: res.msg || '更新失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('保存头像框失败:', error);
		uni.showToast({
			title: '更新失败，请重试',
			icon: 'none'
		});
	} finally {
		uni.hideLoading();
	}
};

// 返回上一页
const goBack = () => {
	uni.navigateBack();
};

// 获取用户信息
const getUserInfo = async () => {
	try {
		const res = await request('/user/getUserInfo', {
			method: 'GET'
		});
		
		if (res.code === 200) {
			userInfo.value = res.data;
			// 设置初始头像框URL
			initialFrameUrl.value = userInfo.value.avatarFrameUrl || null;
			selectedFrameUrl.value = initialFrameUrl.value;
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
		const res = await request('/user/frameList', {
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
		const res = await request('/user/userFrameList', {
			method: 'GET'
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

// 页面加载时初始化数据
onMounted(() => {
	initData();
});
</script>

<style>
.frame-container {
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

/* 预览部分 */
.preview-section {
	background-color: #fff;
	margin-top: 20rpx;
	padding: 40rpx 30rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.avatar-preview {
	position: relative;
	width: 200rpx;
	height: 200rpx;
	margin-bottom: 20rpx;
}

.avatar-img {
	width: 100%;
	height: 100%;
	border-radius: 100rpx;
	border: 4rpx solid #fff;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
	z-index: 1;
}

.frame-img {
	position: absolute;
	top: -30rpx;
	left: -30rpx;
	width: 260rpx;
	height: 260rpx;
	z-index: 2;
}

.preview-text {
	color: #666;
	font-size: 28rpx;
}

/* 头像框列表部分 */
.frames-section {
	background-color: #fff;
	margin-top: 20rpx;
	padding: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 30rpx;
}

.loading-container {
	display: flex;
	justify-content: center;
	padding: 60rpx 0;
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

.frame-item.active .frame-inner {
	background-color: rgba(24, 144, 255, 0.1);
	border: 1px solid #1890ff;
}

.frame-item.locked .frame-inner {
	opacity: 0.7;
	background-color: #f0f0f0;
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

.lock-icon {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	border-radius: 60rpx;
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 3;
}

.lock-text {
	font-size: 40rpx;
	color: #fff;
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
	justify-content: space-between;
	z-index: 10;
	box-sizing: border-box;
}

.save-btn, .cancel-btn {
	flex: 1;
	height: 80rpx;
	line-height: 80rpx;
	text-align: center;
	border-radius: 8rpx;
	font-size: 28rpx;
}

.save-btn {
	background-color: #1890ff;
	color: #fff;
	margin-right: 20rpx;
}

.save-btn[disabled] {
	background-color: #ccc;
	color: #fff;
}

.cancel-btn {
	background-color: #f5f5f5;
	color: #666;
}
</style>
