<template>
    <view class="report-container">
        <!-- 顶部导航栏 -->
        <view class="nav-bar">
            <view class="back-btn" @click="goBack">
                <text class="back-icon">←</text>
            </view>
            <text class="nav-title">举报{{ reportTypeName }}</text>
            <view class="placeholder"></view>
        </view>
        
        <!-- 举报内容 -->
        <view class="report-content">
            <view class="report-section">
                <text class="section-title">详细描述</text>
                <textarea 
                    class="report-textarea" 
                    v-model="description" 
                    placeholder="请详细描述您遇到的问题，以便我们更好地处理（选填）" 
                    maxlength="500"
                ></textarea>
                <text class="word-count">{{ description.length }}/500</text>
            </view>
            
            <view class="report-section">
                <text class="section-title">上传截图（选填）</text>
                <view class="upload-area">
                    <view 
                        class="upload-item" 
                        v-if="uploadedImages.length > 0"
                    >
                        <image :src="uploadedImages[0]" class="uploaded-img" mode="aspectFill"></image>
                        <view class="delete-btn" @click="deleteImage(0)">×</view>
                    </view>
                    
                    <view class="upload-btn" @click="chooseImage" v-if="uploadedImages.length === 0">
                        <text class="upload-icon">+</text>
                        <text class="upload-text">上传图片</text>
                    </view>
                </view>
            </view>
        </view>
        
        <!-- 提交按钮 -->
        <view class="submit-btn active" @click="submitReport">
            <text class="submit-text">提交举报</text>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request.js'

// 表单数据
const description = ref('')
const uploadedImages = ref([])

// 路由参数
const id = ref('')
const userId = ref('')
const type = ref('')

// 根据类型显示不同的举报名称
const reportTypeName = computed(() => {
    switch(type.value) {
        case 'comment':
            return '评论';
        case 'chat':
            return '消息';
        default:
            return '';
    }
})

// 返回上一页
const goBack = () => {
    uni.navigateBack()
}

// 选择图片
const chooseImage = () => {
    if (uploadedImages.value.length > 0) {
        uni.showToast({
            title: '最多上传1张图片',
            icon: 'none'
        })
        return
    }
    
    uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
            // 上传图片
            uploadImage(res.tempFilePaths[0])
        }
    })
}

// 上传图片
const uploadImage = async (filePath) => {
    uni.showLoading({
        title: '上传中...',
        mask: true
    })
    
    try {
        const imageUrl = await new Promise((resolve, reject) => {
            uni.uploadFile({
                url: '/system/uploadImg',
                filePath: filePath,
                name: 'file',
                header: {
                    'token': uni.getStorageSync('token') || ''
                },
                success: (uploadRes) => {
                    try {
                        const data = JSON.parse(uploadRes.data)
                        if (data.code === 200) {
                            resolve(data.data)
                        } else {
                            reject(data.msg || '上传失败')
                        }
                    } catch (e) {
                        reject('上传响应解析失败')
                    }
                },
                fail: (err) => {
                    reject(err)
                }
            })
        })
        
        // 添加到已上传图片列表
        uploadedImages.value.push(imageUrl)
        
    } catch (error) {
        uni.showToast({
            title: typeof error === 'string' ? error : '图片上传失败',
            icon: 'none'
        })
    } finally {
        uni.hideLoading()
    }
}

// 删除图片
const deleteImage = (index) => {
    uploadedImages.value.splice(index, 1)
}

// 提交举报
const submitReport = async () => {
    uni.showLoading({
        title: '提交中...',
        mask: true
    })
    
    try {
        const res = await request('/report/submit', {
            method: 'POST',
            data: {
                type: type.value,
                content: description.value,
                image: uploadedImages.value.length > 0 ? uploadedImages.value[0] : '',
                reportUserId: userId.value,
                typeId: id.value
            }
        })
        
        if (res.code === 200) {
            uni.showToast({
                title: '举报成功',
                icon: 'success'
            })
            
            // 延迟返回
            setTimeout(() => {
                uni.navigateBack()
            }, 1500)
        } else {
            uni.showToast({
                title: res.msg || '举报失败',
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

// 获取路由参数
const getRouteParams = () => {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    
    // 兼容不同环境的参数获取方式
    if (currentPage.options) {
        id.value = currentPage.options.id || currentPage.options.chatId || ''
        userId.value = currentPage.options.userId || ''
        type.value = currentPage.options.type || (currentPage.options.chatId ? 'chat' : 'comment')
    } else if (currentPage.$page && currentPage.$page.options) {
        id.value = currentPage.$page.options.id || currentPage.$page.options.chatId || ''
        userId.value = currentPage.$page.options.userId || ''
        type.value = currentPage.$page.options.type || (currentPage.$page.options.chatId ? 'chat' : 'comment')
    } else if (currentPage.$route && currentPage.$route.query) {
        id.value = currentPage.$route.query.id || currentPage.$route.query.chatId || ''
        userId.value = currentPage.$route.query.userId || ''
        type.value = currentPage.$route.query.type || (currentPage.$route.query.chatId ? 'chat' : 'comment')
    }
    
    console.log('举报参数:', {
        id: id.value,
        userId: userId.value,
        type: type.value
    })
}

onMounted(() => {
    getRouteParams()
})

// 导出onLoad方法供页面实例调用
defineExpose({
    onLoad(options) {
        console.log('举报页面onLoad被调用，参数:', options)
        if (options) {
            id.value = options.id || options.chatId || ''
            userId.value = options.userId || ''
            type.value = options.type || (options.chatId ? 'chat' : 'comment')
        }
    }
})
</script>

<style lang="scss" scoped>
.report-container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: #f5f7fa;
}

/* 导航栏样式 */
.nav-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 30rpx;
    background-color: #fff;
    position: sticky;
    top: 0;
    z-index: 100;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.back-icon {
    font-size: 40rpx;
    color: #333;
}

.nav-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
}

.placeholder {
    width: 60rpx;
}

/* 举报内容 */
.report-content {
    flex: 1;
    padding: 30rpx;
}

.report-section {
    margin-bottom: 40rpx;
    background-color: #fff;
    border-radius: 10rpx;
    padding: 30rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
}

/* 举报类型 */
.report-types {
    display: flex;
    flex-wrap: wrap;
}

.type-item {
    width: 30%;
    height: 80rpx;
    background-color: #f5f7fa;
    border-radius: 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 3%;
    margin-bottom: 20rpx;
}

.type-item.selected {
    background: linear-gradient(135deg, #1abc9c, #61ca5b);
}

.type-text {
    font-size: 28rpx;
    color: #333;
}

.selected .type-text {
    color: #fff;
}

/* 详细描述 */
.report-textarea {
    width: 100%;
    height: 200rpx;
    background-color: #f5f7fa;
    border-radius: 10rpx;
    padding: 20rpx;
    font-size: 28rpx;
    box-sizing: border-box;
}

.word-count {
    font-size: 24rpx;
    color: #999;
    text-align: right;
    margin-top: 10rpx;
    display: block;
}

/* 上传区域 */
.upload-area {
    display: flex;
    flex-wrap: wrap;
}

.upload-item, .upload-btn {
    width: 200rpx;
    height: 200rpx;
    margin-right: 20rpx;
    margin-bottom: 20rpx;
    border-radius: 10rpx;
    overflow: hidden;
    position: relative;
}

.uploaded-img {
    width: 100%;
    height: 100%;
}

.delete-btn {
    position: absolute;
    top: 10rpx;
    right: 10rpx;
    width: 40rpx;
    height: 40rpx;
    background-color: rgba(0, 0, 0, 0.5);
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 24rpx;
}

.upload-btn {
    background-color: #f5f7fa;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 2rpx dashed #ddd;
}

.upload-icon {
    font-size: 60rpx;
    color: #999;
    margin-bottom: 10rpx;
}

.upload-text {
    font-size: 24rpx;
    color: #999;
}

/* 提交按钮 */
.submit-btn {
    margin: 40rpx 30rpx;
    height: 90rpx;
    background-color: #e0e0e0;
    border-radius: 45rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.submit-btn.active {
    background: linear-gradient(135deg, #1abc9c, #7ee153);
}

.submit-text {
    font-size: 32rpx;
    color: #666;
}

.active .submit-text {
    color: #fff;
}
</style>