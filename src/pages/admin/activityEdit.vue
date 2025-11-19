<template>
	<view class="edit-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-left" @tap="goBack">
				<text class="back-icon">←</text>
				<text>返回</text>
			</view>
			<text class="nav-title">{{ isEdit ? '编辑活动' : '创建活动' }}</text>
			<view class="nav-right"></view>
		</view>

		<!-- 表单内容 -->
		<scroll-view scroll-y class="form-scroll">
			<view class="form-content">
				<!-- 封面图片 -->
				<view class="form-item">
					<text class="form-label">封面图片</text>
					<view class="image-upload" @tap="chooseImage">
						<image 
							v-if="formData.image" 
							:src="formData.image" 
							mode="aspectFill" 
							class="preview-image"
						></image>
						<view v-else class="upload-placeholder">
							<text class="upload-icon">+</text>
							<text class="upload-text">上传图片</text>
						</view>
					</view>
				</view>

				<!-- 活动标题 -->
				<view class="form-item">
					<text class="form-label">活动标题</text>
					<input 
						type="text" 
						v-model="formData.title"
						placeholder="请输入活动标题"
						class="form-input"
					/>
				</view>

				<!-- 活动类型 -->
				<view class="form-item">
					<text class="form-label">活动类型</text>
					<picker 
						:value="typeIndex" 
						:range="typeOptions" 
						@change="handleTypeChange"
						class="form-picker"
					>
						<view class="picker-value">
							{{ formData.type || '请选择活动类型' }}
						</view>
					</picker>
				</view>

				<!-- 活动 -->
				<view class="form-item">
					<text class="form-label">活动链接</text>
					<input 
						type="text" 
						v-model="formData.url"
						placeholder="请输入活动链接"
						class="form-input"
					/>
				</view>

				<!-- 活动状态 -->
				<view class="form-item">
					<text class="form-label">活动状态</text>
					<picker 
						:value="formData.status" 
						:range="statusOptions" 
						@change="handleStatusChange"
						class="form-picker"
					>
						<view class="picker-value">
							{{ formData.status || '请选择活动状态' }}
						</view>
					</picker>
				</view>

				<!-- 活动内容 -->
				<view class="form-item">
					<text class="form-label">活动内容</text>
					<textarea 
						v-model="formData.content"
						placeholder="请输入活动内容"
						class="form-textarea"
					/>
				</view>
			</view>
		</scroll-view>

		<!-- 底部按钮 -->
		<view class="bottom-bar">
			<button 
				class="submit-btn" 
				:disabled="isSubmitting"
				@tap="handleSubmit"
			>
				{{ isSubmitting ? '提交中...' : '保存' }}
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 获取页面参数
const query = defineProps({
	id: {
		type: [String, Number],
		default: ''
	},
	mode: {
		type: String,
		default: 'create'
	}
})

// 表单数据
const formData = ref({
	id: '',
	title: '',
	type: '',
	status: '下线',
	content: '',
	image: '',
	url: ''
})

// 选项数据
const typeOptions = ['活动', '公告', '广告']
const statusOptions = ['上线', '下线']
const typeIndex = ref(0)

// 状态标记
const isEdit = ref(query.mode === 'edit')
const isSubmitting = ref(false)

// 获取活动详情
const getActivityDetail = async () => {
	try {
		const res = await request('/activity/getById', {
			method: 'GET',
			data: {
				id: query.id
			}
		})

		if (res.code === 200 && res.data) {
			formData.value = {
				...formData.value,
				...res.data
			}
			
			// 只需要设置类型索引
			typeIndex.value = typeOptions.indexOf(res.data.type)
		} else {
			uni.showToast({
				title: res.msg || '获取活动详情失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取活动详情失败:', error)
		uni.showToast({
			title: '网络异常，请稍后重试',
			icon: 'none'
		})
	}
}

// 选择图片
const chooseImage = () => {
	uni.chooseImage({
		count: 1,
		success: (res) => {
			const tempFilePath = res.tempFilePaths[0]
			// 上传图片到服务器
			uni.showLoading({
				title: '上传中...'
			})
			uni.uploadFile({
				url: '/system/uploadImg',
				filePath: tempFilePath,
				header: {
					'sa-token': uni.getStorageSync('sa-token')
				},
				name: 'file',
				success: (uploadRes) => {
					const data = JSON.parse(uploadRes.data)
					if (data.code === 200 && data.data) {
						formData.value.image = data.data
						uni.showToast({
							title: '上传成功',
							icon: 'success'
						})
					} else {
						uni.showToast({
							title: data.msg || '上传失败',
							icon: 'none'
						})
					}
				},
				fail: (err) => {
					console.error('图片上传失败:', err)
					uni.showToast({
						title: '图片上传失败',
						icon: 'none'
					})
				},
				complete: () => {
					uni.hideLoading()
				}
			})
		}
	})
}

// 处理类型选择
const handleTypeChange = (e) => {
	const index = e.detail.value
	typeIndex.value = index
	formData.value.type = typeOptions[index]
}

// 处理状态选择
const handleStatusChange = (e) => {
	const index = e.detail.value
	formData.value.status = statusOptions[index]
}

// 表单提交
const handleSubmit = async () => {
	// 表单验证
	if (!formData.value.title?.trim()) {
		return uni.showToast({
			title: '请输入活动标题',
			icon: 'none'
		})
	}
	if (!formData.value.type) {
		return uni.showToast({
			title: '请选择活动类型',
			icon: 'none'
		})
	}
	if (!formData.value.status) {
		return uni.showToast({
			title: '请选择活动状态',
			icon: 'none'
		})
	}
	if (!formData.value.content?.trim()) {
		return uni.showToast({
			title: '请输入活动内容',
			icon: 'none'
		})
	}

	isSubmitting.value = true
	try {
		// 根据mode判断是新建还是编辑
		const url = isEdit.value ? '/admin/activity/update' : '/admin/activity/add'
		const res = await request(url, {
			method: 'POST',
			data: formData.value
		})

		if (res.code === 200) {
			uni.showToast({
				title: isEdit.value ? '更新成功' : '创建成功',
				icon: 'success'
			})
			
			// 通知列表页刷新
			uni.$emit('refreshActivityList')
			
			// 返回上一页
			setTimeout(() => {
				uni.navigateBack()
			}, 1500)
		} else {
			uni.showToast({
				title: res.msg || (isEdit.value ? '更新失败' : '创建失败'),
				icon: 'none'
			})
		}
	} catch (error) {
		console.error(isEdit.value ? '更新失败:' : '创建失败:', error)
		uni.showToast({
			title: '网络异常，请稍后重试',
			icon: 'none'
		})
	} finally {
		isSubmitting.value = false
	}
}

// 返回上一页
const goBack = () => {
	uni.navigateBack()
}

// 页面加载时获取详情
onMounted(() => {
	if (isEdit.value && query.id) {
		getActivityDetail()
	}
})
</script>

<style lang="scss" scoped>
.edit-container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #f5f7fa;
}

/* 导航栏 */
.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
	background-color: #fff;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.nav-left {
	display: flex;
	align-items: center;
	font-size: 28rpx;
	color: #666;
}

.back-icon {
	font-size: 36rpx;
	margin-right: 10rpx;
}

.nav-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.nav-right {
	width: 100rpx;
}

/* 表单区域 */
.form-scroll {
	flex: 1;
	overflow: hidden;
}

.form-content {
	padding: 30rpx;
}

.form-item {
	margin-bottom: 30rpx;
	background-color: #fff;
	border-radius: 12rpx;
	padding: 20rpx;
}

.form-label {
	font-size: 28rpx;
	color: #333;
	margin-bottom: 20rpx;
	display: block;
}

.form-input {
	width: 100%;
	height: 80rpx;
	font-size: 28rpx;
	color: #333;
	padding: 0 20rpx;
	background-color: #f5f7fa;
	border-radius: 8rpx;
}

.form-textarea {
	width: 100%;
	height: 300rpx;
	font-size: 28rpx;
	color: #333;
	padding: 20rpx;
	background-color: #f5f7fa;
	border-radius: 8rpx;
}

.form-picker {
	width: 100%;
}

.picker-value {
	height: 80rpx;
	line-height: 80rpx;
	font-size: 28rpx;
	color: #333;
	padding: 0 20rpx;
	background-color: #f5f7fa;
	border-radius: 8rpx;
}

/* 图片上传 */
.image-upload {
	width: 100%;
	height: 400rpx;
	background-color: #f5f7fa;
	border-radius: 8rpx;
	overflow: hidden;
}

.preview-image {
	width: 100%;
	height: 100%;
}

.upload-placeholder {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.upload-icon {
	font-size: 60rpx;
	color: #999;
	margin-bottom: 20rpx;
}

.upload-text {
	font-size: 28rpx;
	color: #999;
}

/* 底部按钮 */
.bottom-bar {
	padding: 20rpx 30rpx;
	background-color: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.submit-btn {
	width: 100%;
	height: 80rpx;
	line-height: 80rpx;
	text-align: center;
	background: linear-gradient(90deg, #1abc9c, #16a085);
	color: #fff;
	font-size: 28rpx;
	border-radius: 40rpx;
	box-shadow: 0 2rpx 10rpx rgba(26, 188, 156, 0.3);

	&:active {
		opacity: 0.9;
	}

	&[disabled] {
		opacity: 0.6;
		background: #ccc;
	}
}
</style>
