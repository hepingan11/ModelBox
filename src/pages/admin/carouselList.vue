<template>
	<view class="carousel-container">
		<!-- 添加轮播图按钮 -->
		<view class="add-section">
			<button class="add-btn" @click="chooseImage">添加轮播图</button>
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
					<view class="item-actions">
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
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 轮播图列表
const carouselList = ref([])
// 刷新状态
const isRefreshing = ref(false)

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
					// 添加轮播图
					const addRes = await request('/admin/carousel/add', {
						method: 'POST',
						data: {
							url: uploadRes.data
						}
					})
					
					if (addRes.code === 200) {
						uni.showToast({
							title: '添加成功',
							icon: 'success'
						})
						getCarouselList()
					} else {
						uni.showToast({
							title: addRes.msg || '添加失败',
							icon: 'none'
						})
					}
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

.item-actions {
	padding: 20rpx;
	display: flex;
	justify-content: flex-end;
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
</style>