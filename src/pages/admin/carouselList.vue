<template>
	<view class="carousel-container">
		<!-- 添加轮播图按钮 -->
		<view class="add-section">
			<button class="add-btn" @click="openAddModal">添加轮播图</button>
		</view>
		
		<!-- 轮播图列表 -->
		<scroll-view 
			scroll-y 
			class="carousel-list"
			refresher-enabled
			:refresher-triggered="isRefreshing"
			@refresherrefresh="refreshList"
		>
			<view class="list-content" v-if="carouselList.length > 0">
				<view 
					class="carousel-item"
					v-for="(item, index) in carouselList" 
					:key="item.id"
				>
					<image 
						:src="item.url" 
						class="carousel-image" 
						mode="aspectFill"
						@click="previewImage(item.url)"
					></image>
					<view class="item-info" v-if="item.navtoUrl">
						<text class="link-label">跳转：</text>
						<text class="link-text">{{item.navtoUrl}}</text>
					</view>
					<view class="item-actions">
						<button class="edit-btn" @click="openEditModal(item)">编辑</button>
						<button class="delete-btn" @click="deleteCarousel(item)">删除</button>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view v-else class="empty-state">
				<image src="/static/empty.png" class="empty-icon"></image>
				<text class="empty-text">暂无轮播图</text>
			</view>
		</scroll-view>

		<!-- 添加/编辑弹窗 -->
		<view class="popup-mask" v-if="showAddPopup" @click="closeAddModal">
			<view class="popup-content" @click.stop>
				<view class="popup-title">{{ isEditMode ? '编辑轮播图' : '添加轮播图' }}</view>
				
				<view class="form-item">
					<view class="label">图片</view>
					<view class="upload-box" @click="chooseImage">
						<image v-if="formData.url" :src="formData.url" mode="aspectFill" class="preview-img"></image>
						<view v-else class="upload-placeholder">
							<text class="plus">+</text>
							<text class="text">点击上传</text>
						</view>
					</view>
				</view>
				
				<view class="form-item">
					<view class="label">跳转链接</view>
					<input class="input" v-model="formData.navtoUrl" placeholder="请输入跳转链接(可选)" />
				</view>
				
				<view class="popup-btns">
					<button class="btn cancel" @click="closeAddModal">取消</button>
					<button class="btn confirm" @click="submitForm">{{ isEditMode ? '保存' : '确定' }}</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 轮播图列表
const carouselList = ref([])
// 刷新状态
const isRefreshing = ref(false)

// 弹窗状态
const showAddPopup = ref(false)
// 编辑模式
const isEditMode = ref(false)
// 当前编辑的轮播图ID
const currentEditId = ref(null)
// 表单数据
const formData = ref({
	url: '',
	navtoUrl: ''
})

// 获取轮播图列表
const getCarouselList = async () => {
	try {
		const res = await request('/system/carousel/list', {
			method: 'GET'
		})
		
		if (res.code === 200) {
			carouselList.value = res.data || []
		} else {
			uni.showToast({
				title: res.msg || '获取轮播图列表失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取轮播图列表失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		isRefreshing.value = false
	}
}

// 刷新列表
const refreshList = () => {
	isRefreshing.value = true
	getCarouselList()
}

// 打开添加弹窗
const openAddModal = () => {
	isEditMode.value = false
	currentEditId.value = null
	formData.value = { url: '', navtoUrl: '' }
	showAddPopup.value = true
}

// 关闭添加弹窗
const closeAddModal = () => {
	showAddPopup.value = false
	isEditMode.value = false
	currentEditId.value = null
}

// 打开编辑弹窗
const openEditModal = (item) => {
	isEditMode.value = true
	currentEditId.value = item.id
	formData.value = {
		url: item.url,
		navtoUrl: item.navtoUrl || ''
	}
	showAddPopup.value = true
}

// 提交表单（添加或编辑）
const submitForm = async () => {
	if (!formData.value.url) {
		uni.showToast({ title: '请先上传图片', icon: 'none' })
		return
	}
	
	try {
		const url = isEditMode.value ? '/admin/carousel/update' : '/admin/carousel/add'
		const data = isEditMode.value 
			? { ...formData.value, id: currentEditId.value }
			: formData.value
		
		const res = await request(url, {
			method: 'POST',
			data: data
		})
		
		if (res.code === 200) {
			uni.showToast({ 
				title: isEditMode.value ? '修改成功' : '添加成功', 
				icon: 'success' 
			})
			closeAddModal()
			getCarouselList()
		} else {
			uni.showToast({ 
				title: res.msg || (isEditMode.value ? '修改失败' : '添加失败'), 
				icon: 'none' 
			})
		}
	} catch (error) {
		console.error(isEditMode.value ? '修改失败:' : '添加失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	}
}

// 选择并上传图片
const chooseImage = () => {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: async (res) => {
			const tempFilePath = res.tempFilePaths[0]
			
			// 显示上传中
			uni.showLoading({
				title: '上传中...'
			})
			
			try {
				// 上传图片
				const uploadRes = await uploadFile(tempFilePath)
				if (uploadRes.code === 200) {
					formData.value.url = uploadRes.data
				} else {
					uni.showToast({
						title: uploadRes.msg || '图片上传失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('操作失败:', error)
				uni.showToast({
					title: '网络错误，请稍后重试',
					icon: 'none'
				})
			} finally {
				uni.hideLoading()
			}
		}
	})
}

// 上传文件
const uploadFile = (filePath) => {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: '/system/uploadImg',
			filePath: filePath,
			header: {
				'sa-token': uni.getStorageSync('sa-token')
			},
			name: 'file',
			success: (uploadFileRes) => {
				try {
					const res = JSON.parse(uploadFileRes.data)
					resolve(res)
				} catch (e) {
					reject(e)
				}
			},
			fail: (error) => {
				reject(error)
			}
		})
	})
}

// 删除轮播图
const deleteCarousel = (item) => {
	uni.showModal({
		title: '确认删除',
		content: '确定要删除这张轮播图吗？',
		success: async (res) => {
			if (res.confirm) {
				try {
					const res = await request('/admin/carousel/delete', {
						method: 'GET',
						data: { id: item.id }
					})
					
					if (res.code === 200) {
						uni.showToast({
							title: '删除成功',
							icon: 'success'
						})
						getCarouselList()
					} else {
						uni.showToast({
							title: res.msg || '删除失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('删除轮播图失败:', error)
					uni.showToast({
						title: '网络错误，请稍后重试',
						icon: 'none'
					})
				}
			}
		}
	})
}

// 预览图片
const previewImage = (url) => {
	uni.previewImage({
		urls: [url],
		current: url
	})
}

// 页面加载时获取列表
onMounted(() => {
	getCarouselList()
})
</script>

<style lang="scss" scoped>
.carousel-container {
	min-height: 100vh;
	background-color: #f5f7fa;
	padding: 20rpx;
}

.add-section {
	margin-bottom: 20rpx;
}

.add-btn {
	background-color: #1abc9c;
	color: #fff;
	font-size: 28rpx;
	padding: 16rpx 32rpx;
	border-radius: 8rpx;
	border: none;
}

.carousel-list {
	height: calc(100vh - 100rpx);
}

.list-content {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.carousel-item {
	background-color: #fff;
	border-radius: 12rpx;
	overflow: hidden;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.carousel-image {
	width: 100%;
	height: 300rpx;
	display: block;
}

.item-info {
	padding: 10rpx 20rpx 0;
	display: flex;
	align-items: center;
}

.link-label {
	font-size: 24rpx;
	color: #999;
	margin-right: 10rpx;
}

.link-text {
	font-size: 24rpx;
	color: #666;
	flex: 1;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.item-actions {
	padding: 20rpx;
	display: flex;
	justify-content: flex-end;
	gap: 16rpx;
}

.edit-btn {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
	font-size: 26rpx;
	padding: 10rpx 20rpx;
	border-radius: 6rpx;
	border: none;
}

.delete-btn {
	background-color: #e74c3c;
	color: #fff;
	font-size: 26rpx;
	padding: 10rpx 20rpx;
	border-radius: 6rpx;
	border: none;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
}

.empty-icon {
	width: 200rpx;
	height: 200rpx;
	margin-bottom: 30rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 弹窗样式 */
.popup-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0,0,0,0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
}

.popup-content {
	width: 600rpx;
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx;
}

.popup-title {
	font-size: 32rpx;
	font-weight: bold;
	text-align: center;
	margin-bottom: 40rpx;
}

.form-item {
	margin-bottom: 30rpx;
}

.label {
	font-size: 28rpx;
	color: #333;
	margin-bottom: 16rpx;
}

.upload-box {
	width: 200rpx;
	height: 200rpx;
	background: #f5f7fa;
	border-radius: 8rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
	position: relative;
}

.upload-placeholder {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.plus {
	font-size: 60rpx;
	color: #999;
	line-height: 1;
}

.text {
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
}

.preview-img {
	width: 100%;
	height: 100%;
}

.input {
	height: 80rpx;
	background: #f5f7fa;
	border-radius: 8rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
	width: 100%;
	box-sizing: border-box;
}

.popup-btns {
	display: flex;
	gap: 20rpx;
	margin-top: 40rpx;
}

.btn {
	flex: 1;
	height: 80rpx;
	line-height: 80rpx;
	text-align: center;
	border-radius: 8rpx;
	font-size: 28rpx;
	border: none;
}

.btn.cancel {
	background: #f5f7fa;
	color: #666;
}

.btn.confirm {
	background: #1abc9c;
	color: #fff;
}
</style>
