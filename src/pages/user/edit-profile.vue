<template>
	<view class="edit-profile">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<text class="nav-title">修改个人信息</text>
		</view>
		
		<!-- 表单区域 -->
		<view class="form-container">
			<!-- 头像修改 -->
			<view class="avatar-section">
				<view class="avatar-wrapper">
					<image :src="userInfo.avatar" mode="aspectFill" class="avatar"></image>
					<!-- 头像框 -->
					<image v-if="userInfo.avatarFrameUrl" :src="userInfo.avatarFrameUrl" class="avatar-frame" mode="aspectFit"></image>
				</view>
				<view class="avatar-options">
					
					<!-- 微信头像选择功能 -->
					<button v-if="isWechatMp" class="wx-avatar-button" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
						<text class="edit-text">修改头像</text>
					</button>
					<!-- 普通环境下的头像选择 -->
					<button v-else class="wx-avatar-button" @click="chooseAvatar">
						<text class="edit-text">修改头像</text>
					</button>
					<button class="wx-avatar-button" @click="chooseAvatarFrame">
						<text class="edit-text">修改头像框</text>
					</button>
				</view>
			</view>
			
			<!-- 背景图修改 -->
			<view class="bg-image-section">
				<view class="section-title">主页背景图</view>
				<view class="bg-image-preview">
					<image :src="userInfo.backgroundImage || defaultBackgroundImage" mode="aspectFill" class="bg-image"></image>
					<view class="bg-image-mask" @click="chooseBackgroundImage">
						<text class="bg-image-text">点击更换背景图</text>
					</view>
				</view>
			</view>
			
			<!-- 信息表单 -->
			<view class="form-group">
				<view class="form-item">
					<text class="label">用户名</text>
					<input type="text" 
						v-model="userInfo.username" 
						placeholder="请输入用户名"
						class="input"
					/>
				</view>
				
				<view class="form-item">
					<text class="label">手机号码</text>
					<input type="number" 
						v-model="userInfo.phone" 
						placeholder="请输入手机号码"
						maxlength="11"
						class="input"
					/>
				</view>
				
				
				<view class="form-item">
					<text class="label">用户ID</text>
					<text class="id-text">{{ userInfo.id }}</text>
				</view>
				
				<!-- 添加密码修改部分 -->
				<view class="form-item">
					<text class="label">原密码</text>
					<input type="password" 
						v-model="passwordForm.oldPassword" 
						placeholder="请输入原密码"
						class="input"
					/>
				</view>
				
				<view class="form-item">
					<text class="label">新密码</text>
					<input type="password" 
						v-model="passwordForm.newPassword" 
						placeholder="请输入新密码"
						class="input"
					/>
				</view>
				
				<view class="form-item">
					<text class="label">确认密码</text>
					<input type="password" 
						v-model="passwordForm.confirmPassword" 
						placeholder="请再次输入新密码"
						class="input"
					/>
				</view>
			</view>
			
			<!-- 修改提示信息 -->
			<view class="tips">
				<text class="tips-icon">ℹ️</text>
				<text class="tips-text">密码长度需要在6-20位之间，包含字母和数字</text>
			</view>
		</view>
		
		<!-- 保存按钮 -->
		<view class="button-container">
			<button class="save-btn" @click="saveChanges">保存修改</button>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 判断是否在微信小程序环境
const isWechatMp = ref(false)

// 用户信息
const userInfo = ref({
	id: '',
	username: '',
	role: '',
	avatar: '',
	phone: '',
	backgroundImage: '', // 添加背景图字段
	avatarFrameUrl: '' // 添加头像框字段
})

// 默认背景图
const defaultBackgroundImage = ''

// 密码表单
const passwordForm = ref({
	oldPassword: '',
	newPassword: '',
	confirmPassword: ''
})

// 检查环境
const checkEnvironment = () => {
	// #ifdef MP-WEIXIN
	isWechatMp.value = true
	// #endif
}

// 获取用户信息
const getUserInfo = async () => {
	uni.showLoading({
		title: '加载中...'
	})
	try {
		const res = await request(
			'/user/getUserInfo',
			{
				method: 'GET'
			}
		)
		if (res.code === 200) {
			userInfo.value = res.data
		} else {
			uni.showToast({
				title: res.msg || '获取用户信息失败',
				icon: 'none'
			})
		}
	} catch (error) {
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		uni.hideLoading()
	}
}

// 选择头像
const chooseAvatar = () => {
	uni.chooseImage({
		count: 1,
		sizeType: ['original'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			const tempFilePath = res.tempFilePaths[0]
			
			uni.showLoading({
				title: '上传中...'
			})
			
			// 使用uni.uploadFile上传文件
			uploadAvatarFile(tempFilePath)
		}
	})
}

// 选择头像框
const chooseAvatarFrame = () => {
	uni.navigateTo({
		url: '/pages/user/frame'
	})
}

// 处理微信头像选择
const onChooseAvatar = (e) => {
	const tempFilePath = e.detail.avatarUrl
	
	if (tempFilePath) {
		uni.showLoading({
			title: '上传中...'
		})
		
		// 使用相同的上传功能上传微信头像
		uploadAvatarFile(tempFilePath)
	}
}

// 封装头像上传功能
const uploadAvatarFile = (filePath) => {
	// 使用uni.uploadFile上传文件
	uni.uploadFile({
		url: '/user/uploadAvatar', // 替换为实际的上传接口地址
		filePath: filePath,
		name: 'file',
		header: {
			'sa-token': uni.getStorageSync('sa-token')
		},
		success: (uploadRes) => {
			try {
				const result = JSON.parse(uploadRes.data)
				if (result.code === 200) {
					// 更新头像URL
					userInfo.value.avatar = result.data.url
					
					// 更新用户信息
					request('/user/updateUser', {
						method: 'POST',
						data: {
							...userInfo.value
						}
					}).then(updateRes => {
						if (updateRes.code === 200) {
							uni.showToast({
								title: '头像更新成功',
								icon: 'success'
							})
							// 重新获取用户信息以确保数据同步
							getUserInfo()
						} else {
							uni.showToast({
								title: updateRes.msg || '头像更新失败',
								icon: 'none'
							})
						}
					}).catch(() => {
						uni.showToast({
							title: '头像更新失败',
							icon: 'none'
						})
					})
				} else {
					uni.showToast({
						title: result.msg || '上传失败',
						icon: 'none'
					})
				}
			} catch (e) {
				uni.showToast({
					title: '上传失败，请重试',
					icon: 'none'
				})
			}
		},
		fail: (error) => {
			uni.showToast({
				title: '上传失败，请重试',
				icon: 'none'
			})
		},
		complete: () => {
			uni.hideLoading()
		}
	})
}

// 选择背景图片
const chooseBackgroundImage = () => {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			const tempFilePath = res.tempFilePaths[0]
			
			uni.showLoading({
				title: '上传中...'
			})
			
			// 上传背景图
			uploadBackgroundImage(tempFilePath)
		}
	})
}

// 上传背景图
const uploadBackgroundImage = (filePath) => {
	// 使用uni.uploadFile上传文件
	uni.uploadFile({
		url: '/user/updateBackgroundImage', // 替换为实际的上传接口地址
		filePath: filePath,
		name: 'file',
		header: {
			'sa-token': uni.getStorageSync('sa-token')
		},
		success: (uploadRes) => {
			try {
				const result = JSON.parse(uploadRes.data)
				if (result.code === 200) {
					// 更新背景图URL
					userInfo.value.backgroundImage = result.data.backgroundImage || result.data.url
					
					uni.showToast({
						title: '背景图更新成功',
						icon: 'success'
					})
					
					// 重新获取用户信息以确保数据同步
					setTimeout(() => {
						getUserInfo()
					}, 500)
				} else {
					uni.showToast({
						title: result.msg || '更新失败',
						icon: 'none'
					})
				}
			} catch (e) {
				uni.showToast({
					title: '更新失败，请重试',
					icon: 'none'
				})
			}
		},
		fail: (error) => {
			uni.showToast({
				title: '上传失败，请重试',
				icon: 'none'
			})
		},
		complete: () => {
			uni.hideLoading()
		}
	})
}

// 保存修改
const saveChanges = async () => {
	// 表单验证
	if (!userInfo.value.username.trim()) {
		uni.showToast({
			title: '请输入用户名',
			icon: 'none'
		})
		return
	}
	
	if (!userInfo.value.phone || !/^1[3-9]\d{9}$/.test(userInfo.value.phone)) {
		uni.showToast({
			title: '请输入正确的手机号码',
			icon: 'none'
		})
		return
	}
	
	// 密码验证
	if (passwordForm.value.oldPassword || passwordForm.value.newPassword || passwordForm.value.confirmPassword) {
		// 如果有任何一个密码字段被填写，则所有密码字段都必须填写
		if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
			uni.showToast({
				title: '请完整填写密码信息',
				icon: 'none'
			})
			return
		}
		
		
		// 验证两次密码是否一致
		if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
			uni.showToast({
				title: '两次输入的密码不一致',
				icon: 'none'
			})
			return
		}
	}
	
	uni.showLoading({
		title: '保存中...'
	})
	
	try {
		// 构建更新数据
		const updateData = {
			...userInfo.value,
			// 确保背景图字段也被包含
			backgroundImage: userInfo.value.backgroundImage || ''
		}
		
		// 如果有修改密码，添加密码相关数据
		if (passwordForm.value.oldPassword) {
			if(passwordForm.value.oldPassword !== userInfo.value.password){
				uni.showToast({
					title: '原密码错误',
					icon: 'none'
				})
				return
			}else{
				updateData.password = passwordForm.value.newPassword
			}
		}
		
		// 调用接口更新用户信息
		const res = await request(
			'/user/updateUser',
			{
				method: 'POST',
				data: updateData
			}
		)
		
		if (res.code === 200) {
			uni.showToast({
				title: '保存成功',
				icon: 'success'
			})
			// 返回上一页
			setTimeout(() => {
				uni.navigateBack()
			}, 1500)
		} else {
			uni.showToast({
				title: res.msg || '保存失败',
				icon: 'none'
			})
		}
	} catch (error) {
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		uni.hideLoading()
	}
}

// 页面加载时获取用户信息
onMounted(() => {
	checkEnvironment()
	getUserInfo()
})
</script>

<style>
.edit-profile {
	min-height: 100vh;
	background-color: #f5f7fa;
	padding-bottom: 40rpx;
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

/* 表单容器 */
.form-container {
	background-color: #fff;
	margin-top: 20rpx;
	padding: 30rpx;
}

/* 头像部分 */
.avatar-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 30rpx 0;
	border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.avatar-wrapper {
	position: relative;
	margin-bottom: 20rpx;
}

.avatar {
	width: 160rpx;
	height: 160rpx;
	border-radius: 80rpx;
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

.avatar-options {
	display: flex;
	flex-direction: row;
	justify-content: center;
	gap: 20rpx;
}

.avatar-edit, .wx-avatar-button {
	background-color: rgba(24, 144, 255, 0.1);
	padding: 10rpx 30rpx;
	border-radius: 30rpx;
}

.wx-avatar-button {
	margin: 0;
	font-size: inherit;
	line-height: normal;
	background-color: rgba(7, 193, 96, 0.1);
	color: #016fff;
}

.wx-avatar-button::after {
	border: none;
}

.edit-text {
	color: #1890ff;
	font-size: 28rpx;
}

.wx-avatar-button .edit-text {
	color: #07C160;
}

/* 表单样式 */
.form-group {
	padding: 20rpx 0;
}

.form-item {
	display: flex;
	align-items: center;
	padding: 30rpx 0;
	border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.form-item:last-child {
	border-bottom: none;
}

.label {
	width: 160rpx;
	font-size: 30rpx;
	color: #666;
}

.input {
	flex: 1;
	font-size: 30rpx;
	color: #333;
}

.role-tag {
	font-size: 28rpx;
	color: #1890ff;
	background-color: rgba(24, 144, 255, 0.1);
	padding: 4rpx 20rpx;
	border-radius: 20rpx;
}

.id-text {
	font-size: 30rpx;
	color: #999;
}

/* 提示信息 */
.tips {
	display: flex;
	align-items: center;
	padding: 20rpx;
	background-color: #f6f6f6;
	border-radius: 8rpx;
	margin-top: 30rpx;
}

.tips-icon {
	font-size: 32rpx;
	margin-right: 10rpx;
}

.tips-text {
	font-size: 26rpx;
	color: #999;
}

/* 按钮样式 */
.button-container {
	padding: 40rpx 30rpx;
}

.save-btn {
	background-color: #00A872;
	color: #fff;
	font-size: 32rpx;
	padding: 10rpx 0;
	border-radius: 12rpx;
	text-align: center;
}

.save-btn:active {
	opacity: 0.8;
}

/* 背景图部分 */
.bg-image-section {
	padding: 30rpx 0;
	border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.section-title {
	font-size: 30rpx;
	color: #666;
	margin-bottom: 20rpx;
}

.bg-image-preview {
	position: relative;
	width: 100%;
	height: 200rpx;
	border-radius: 12rpx;
	overflow: hidden;
	margin-bottom: 10rpx;
}

.bg-image {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.bg-image-mask {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.3);
	display: flex;
	justify-content: center;
	align-items: center;
	opacity: 0;
	transition: opacity 0.3s;
}

.bg-image-preview:hover .bg-image-mask,
.bg-image-preview:active .bg-image-mask {
	opacity: 1;
}

.bg-image-text {
	color: #fff;
	font-size: 28rpx;
	text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}
</style>
