<template>
	<view class="publish-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<text class="cancel-btn" @click="goBack">取消</text>
			<text class="nav-title">发布帖子</text>
			<text class="publish-submit" :class="{'publish-active': canSubmit}" @click="submitPost">发布</text>
		</view>
		
		<!-- 表单内容 -->
		<view class="form-container">
			<!-- 标题输入 -->
			<input 
				type="text" 
				v-model="postTitle" 
				placeholder="请输入标题" 
				maxlength="50" 
				class="title-input"
			/>
			
			<!-- 内容输入 -->
			<textarea 
				v-model="postContent" 
				placeholder="分享你的想法..." 
				maxlength="2000" 
				class="content-input"
			></textarea>
			
			<!-- 图片上传区域 -->
			<view class="image-upload-section">
				<view class="image-list">
					<!-- 已上传的图片 -->
					<view 
						v-for="(item, index) in imageList" 
						:key="index" 
						class="image-item"
					>
						<image :src="item" mode="aspectFill" class="preview-image"></image>
						<text class="delete-icon" @click.stop="removeImage(index)">×</text>
					</view>
					
					<!-- 上传按钮 -->
					<view 
						class="upload-btn" 
						@click="chooseImage" 
						v-if="imageList.length < 9"
					>
						<text class="upload-icon">+</text>
						<text class="upload-text">{{imageList.length}}/9</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 上传进度弹窗 -->
		<view class="upload-progress-modal" v-if="uploading">
			<view class="progress-container">
				<text class="progress-title">正在上传...</text>
				<view class="progress-bar-wrapper">
					<view class="progress-bar" :style="{width: uploadProgress + '%'}"></view>
				</view>
				<text class="progress-text">{{uploadProgress}}%</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request.js'

// 帖子标题
const postTitle = ref('')
// 帖子内容
const postContent = ref('')
// 图片列表只存url
const imageList = ref([])
// 是否正在上传
const uploading = ref(false)
// 上传进度
const uploadProgress = ref(0)

// 判断是否可以提交
const canSubmit = computed(() => {
	return postTitle.value.trim() !== '' && (postContent.value.trim() !== '' || imageList.value.length > 0)
})

// 返回上一页
const goBack = () => {
	uni.navigateBack()
}

// 选择图片
const chooseImage = () => {
	const maxCount = 9 - imageList.value.length
	if (maxCount <= 0) {
		uni.showToast({ title: '最多选择9张图片', icon: 'none' })
		return
	}
	uni.chooseImage({
		count: maxCount,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: async (res) => {
			for (const filePath of res.tempFilePaths) {
				// 立即上传
				try {
					const url = await uploadFile(filePath)
					imageList.value.push(url)
				} catch (e) {
					uni.showToast({ title: '图片上传失败', icon: 'none' })
				}
			}
		}
	})
}

// 上传单张图片
const uploadFile = (filePath) => {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: '/system/uploadImg',
			filePath,
			name: 'file',
			header: {
				'Content-Type': 'multipart/form-data',
				"sa-token": uni.getStorageSync('sa-token')
			},
			success: (res) => {
				try {
					const result = JSON.parse(res.data)
					if (result.code === 200) {
						resolve(result.data)
					} else {
						reject(result.msg || '上传失败')
					}
				} catch (e) {
					reject('解析响应失败')
				}
			},
			fail: reject
		})
	})
}

// 删除图片
const removeImage = async (index) => {
	const url = imageList.value[index]
	try {
		await request('/system/deleteImg', {
			method: 'GET',
			data: { url: url }
		})
	} catch (e) {
		// 可以根据需要提示用户删除失败
		uni.showToast({
			title: '删除失败',
			icon: 'none'
		})
	}
	imageList.value.splice(index, 1)
}

// 提交帖子
const submitPost = async () => {
	if (!canSubmit.value) {
		uni.showToast({
			title: '请填写标题和内容',
			icon: 'none'
		})
		return
	}
	
	if (uploading.value) {
		return
	}
	
	// 开始上传
	uploading.value = true
	uploadProgress.value = 0
	
	try {
		// 准备上传的表单数据
		const formData = {
			title: postTitle.value,
			content: postContent.value,
			imageUrl: imageList.value
		}
		
		// 上传完成后，提交表单数据
		await submitFormData(formData)
	} catch (error) {
		
	} finally {
		uploading.value = false
	}
}

// 提交表单数据
const submitFormData = async (formData) => {
	try {
		uploadProgress.value = 90
		
		// 发送请求提交帖子
		const res = await request('/forum/publish', {
			method: 'POST',
			data: formData
		})
		
		uploadProgress.value = 100
		
		if (res.code === 200) {
			uni.showToast({
				title: res.msg,
				icon: 'none'
			})
			
			// 延迟返回，让用户看到提示
			setTimeout(() => {
				uni.navigateBack({
					delta: 1,
					success: () => {
						// 发送一个事件通知列表页刷新
						uni.$emit('refreshForumList')
					}
				})
			}, 2000)
		} else {
			throw new Error(res.msg || '发布失败')
		}
	} catch (error) {
		console.error('网络错误，提交表单失败:', error)
		throw error
	}
}

// 监听页面返回
onMounted(() => {
	// 页面返回时提示用户未保存的内容将丢失
	uni.onBackPress((options) => {
		if (options.from === 'backbutton' && (postTitle.value || postContent.value || imageList.value.length > 0)) {
			uni.showModal({
				title: '提示',
				content: '未发布的内容将会丢失，确定要离开吗？',
				success: (res) => {
					if (res.confirm) {
						uni.navigateBack({
							delta: 1
						})
					}
				}
			})
			return true // 拦截默认返回行为
		}
		return false
	})
})
</script>

<style>
.publish-container {
	background-color: #fff;
	min-height: 100vh;
}

/* 导航栏样式 */
.nav-bar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.cancel-btn {
	font-size: 30rpx;
	color: #666;
}

.nav-title {
	font-size: 34rpx;
	font-weight: bold;
	color: #333;
}

.publish-submit {
	font-size: 30rpx;
	color: #ccc;
	font-weight: bold;
}

.publish-active {
	color: #1890ff;
}

/* 表单容器 */
.form-container {
	padding: 30rpx;
}

/* 标题输入 */
.title-input {
	font-size: 34rpx;
	font-weight: bold;
	color: #333;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
	margin-bottom: 30rpx;
}

/* 内容输入 */
.content-input {
	width: 100%;
	height: 300rpx;
	font-size: 30rpx;
	color: #333;
	line-height: 1.5;
	padding: 0;
	margin-bottom: 30rpx;
}

/* 图片上传区域 */
.image-upload-section {
	margin-top: 40rpx;
}

.image-list {
	display: flex;
	flex-wrap: wrap;
}

.image-item {
	width: 210rpx;
	height: 210rpx;
	margin-right: 20rpx;
	margin-bottom: 20rpx;
	position: relative;
}

.preview-image {
	width: 100%;
	height: 100%;
	border-radius: 8rpx;
}

.delete-icon {
	position: absolute;
	right: -15rpx;
	top: -15rpx;
	width: 40rpx;
	height: 40rpx;
	line-height: 36rpx;
	text-align: center;
	font-size: 30rpx;
	color: #fff;
	background-color: rgba(0, 0, 0, 0.5);
	border-radius: 20rpx;
}

.upload-btn {
	width: 210rpx;
	height: 210rpx;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	background-color: #f5f7fa;
	border-radius: 8rpx;
	margin-bottom: 20rpx;
}

.upload-icon {
	font-size: 60rpx;
	color: #ccc;
	margin-bottom: 10rpx;
}

.upload-text {
	font-size: 24rpx;
	color: #999;
}

/* 上传进度弹窗 */
.upload-progress-modal {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 999;
}

.progress-container {
	width: 500rpx;
	background-color: #fff;
	border-radius: 12rpx;
	padding: 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.progress-title {
	font-size: 32rpx;
	color: #333;
	margin-bottom: 30rpx;
}

.progress-bar-wrapper {
	width: 100%;
	height: 20rpx;
	background-color: #f0f0f0;
	border-radius: 10rpx;
	overflow: hidden;
	margin-bottom: 20rpx;
}

.progress-bar {
	height: 100%;
	background-color: #1890ff;
	transition: width 0.3s;
}

.progress-text {
	font-size: 28rpx;
	color: #666;
}
</style>
