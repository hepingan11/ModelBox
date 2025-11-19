<template> 
    <view class="avatar-edit-container">
        
        <!-- 表单区域 -->
        <scroll-view scroll-y class="form-scroll">
            <!-- 头像框预览 -->
            <view class="form-item">
                <text class="item-label">头像框图片</text>
                <text class="required-mark">*</text>
                
                <view class="avatar-preview-container">
                    <view class="avatar-preview" v-if="formData.url">
                        <image :src="formData.url" class="avatar-image" mode="aspectFit"></image>
                    </view>
                    <view class="avatar-upload-btn" @click="chooseImage" v-if="!formData.url">
                        <text class="upload-icon">+</text>
                        <text class="upload-text">上传头像框图片</text>
                    </view>
                    <view class="avatar-actions" v-if="formData.url">
                        <view class="action-btn change-btn" @click="chooseImage">更换图片</view>
                        <view class="action-btn remove-btn" @click="removeImage">删除图片</view>
                    </view>
                </view>
            </view>
            
            <!-- 头像框名称 -->
            <view class="form-item">
                <text class="item-label">头像框名称</text>
                <text class="required-mark">*</text>
                <input 
                    type="text" 
                    v-model="formData.name" 
                    placeholder="请输入头像框名称"
                    class="input-field"
                />
            </view>
            
            <!-- 头像框介绍 -->
            <view class="form-item">
                <text class="item-label">头像框介绍</text>
                <text class="required-mark">*</text>
                <textarea 
                    v-model="formData.introduce" 
                    placeholder="请输入头像框介绍"
                    class="textarea-field"
                />
            </view>
        </scroll-view>
        
        <!-- 底部提交区域 -->
        <view class="footer-actions">
            <view class="cancel-btn" @click="navigateBack">取消</view>
            <view class="submit-btn" @click="submitForm">{{ isEditMode ? '保存修改' : '添加头像框' }}</view>
        </view>
        
        <!-- 加载中遮罩 -->
        <view class="loading-mask" v-if="isSubmitting">
            <view class="loading-content">
                <view class="loading-spinner"></view>
                <text class="loading-text">{{ loadingText }}</text>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

// 表单数据
const formData = reactive({
    name: '',      // 头像框名称
    introduce: '', // 头像框介绍
    url: ''        // 头像框图片URL
})

// 状态变量
const isEditMode = ref(false)   // 是否为编辑模式
const avatarId = ref('')        // 头像框ID
const isSubmitting = ref(false) // 提交状态
const loadingText = ref('上传中...') // 加载文本

// 页面加载时检查是否为编辑模式
onMounted(() => {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    
    // 获取传递的参数
    if (currentPage && currentPage.options) {
        const { mode, id } = currentPage.options
        
        if (mode === 'edit' && id) {
            isEditMode.value = true
            avatarId.value = id
            fetchAvatarDetail(id)
        }
    }
})

// 获取头像框详情
const fetchAvatarDetail = async (id) => {
    isSubmitting.value = true
    loadingText.value = '加载数据中...'
    
    try {
        const res = await request('/admin/frame/detail', {
            method: 'GET',
            data: { id }
        })
        
        if (res.code === 200 && res.data) {
            const data = res.data
            formData.name = data.name || ''
            formData.introduce = data.introduce || ''
            formData.url = data.url || ''
        } else {
            uni.showToast({
                title: '获取头像框详情失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('获取头像框详情失败:', error)
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
    } finally {
        isSubmitting.value = false
    }
}

// 选择图片
const chooseImage = () => {
    uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
            if (res.tempFilePaths && res.tempFilePaths.length > 0) {
                uploadImage(res.tempFilePaths[0])
            }
        }
    })
}

// 上传图片
const uploadImage = async (filePath) => {
    isSubmitting.value = true
    loadingText.value = '上传图片中...'
    
    try {
        const uploadResult = await new Promise((resolve, reject) => {
            uni.uploadFile({
                url: '/system/uploadImg',
                filePath: filePath,
                name: 'file',
                header: {
                    'Content-Type': 'multipart/form-data',
                    'sa-token': uni.getStorageSync('sa-token')
                },
                success: (res) => {
                    try {
                        const data = JSON.parse(res.data)
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
        
        formData.url = uploadResult
        
    } catch (error) {
        console.error('上传图片失败:', error)
        uni.showToast({
            title: typeof error === 'string' ? error : '上传图片失败',
            icon: 'none'
        })
    } finally {
        isSubmitting.value = false
    }
}

// 删除图片
const removeImage = () => {
    formData.url = ''
}

// 表单验证
const validateForm = () => {
    if (!formData.url) {
        uni.showToast({
            title: '请上传头像框图片',
            icon: 'none'
        })
        return false
    }
    
    if (!formData.name.trim()) {
        uni.showToast({
            title: '请输入头像框名称',
            icon: 'none'
        })
        return false
    }
    
    if (!formData.introduce.trim()) {
        uni.showToast({
            title: '请输入头像框介绍',
            icon: 'none'
        })
        return false
    }
    
    return true
}

// 提交表单
const submitForm = async () => {
    if (!validateForm()) return
    
    isSubmitting.value = true
    loadingText.value = isEditMode.value ? '保存中...' : '提交中...'
    
    try {
        const endpoint = isEditMode.value ? '/admin/frame/update' : '/admin/frame/add'
        const data = {
            name: formData.name,
            introduce: formData.introduce,
            url: formData.url
        }
        
        // 编辑模式需要添加ID
        if (isEditMode.value) {
            data.id = avatarId.value
        }
        
        const res = await request(endpoint, {
            method: 'POST',
            data
        })
        
        if (res.code === 200) {
            uni.showToast({
                title: isEditMode.value ? '修改成功' : '添加成功',
                icon: 'success'
            })
            
            // 延迟返回
            setTimeout(() => {
                navigateBack()
            }, 1500)
        } else {
            uni.showToast({
                title: res.msg || (isEditMode.value ? '修改失败' : '添加失败'),
                icon: 'none'
            })
        }
    } catch (error) {
        console.error(isEditMode.value ? '修改头像框失败:' : '添加头像框失败:', error)
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
    } finally {
        isSubmitting.value = false
    }
}

// 返回上一页
const navigateBack = () => {
    uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.avatar-edit-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #f5f7fa;
}

/* 顶部标题栏 */
.header-section {
    padding: 30rpx;
    background-color: #ffffff;
    display: flex;
    align-items: center;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
    position: relative;
}

.back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    position: absolute;
    left: 30rpx;
}

.back-icon {
    font-size: 40rpx;
    color: #333;
}

.header-title {
    flex: 1;
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
    text-align: center;
}

/* 表单区域 */
.form-scroll {
    flex: 1;
    padding: 30rpx;
}

.form-item {
    margin-bottom: 40rpx;
    background-color: #fff;
    border-radius: 12rpx;
    padding: 30rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.item-label {
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
}

.required-mark {
    color: #ff4d4f;
    margin-left: 8rpx;
}

.input-field {
    margin-top: 20rpx;
    height: 80rpx;
    border-bottom: 1rpx solid #eee;
    font-size: 28rpx;
}

.textarea-field {
    margin-top: 20rpx;
    width: 100%;
    height: 200rpx;
    border: 1rpx solid #eee;
    border-radius: 8rpx;
    padding: 20rpx;
    font-size: 28rpx;
    box-sizing: border-box;
}

/* 头像框预览 */
.avatar-preview-container {
    margin-top: 30rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.avatar-preview {
    width: 300rpx;
    height: 300rpx;
    background-color: #f8f8f8;
    border-radius: 8rpx;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
}

.avatar-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.avatar-upload-btn {
    width: 300rpx;
    height: 300rpx;
    border: 2rpx dashed #ccc;
    border-radius: 8rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #f8f8f8;
}

.upload-icon {
    font-size: 60rpx;
    color: #ccc;
    margin-bottom: 20rpx;
}

.upload-text {
    font-size: 26rpx;
    color: #999;
}

.avatar-actions {
    display: flex;
    margin-top: 20rpx;
}

.action-btn {
    padding: 12rpx 30rpx;
    border-radius: 30rpx;
    font-size: 26rpx;
    margin: 0 10rpx;
}

.change-btn {
    background-color: #e6f7ff;
    color: #1890ff;
    border: 1rpx solid #1890ff;
}

.remove-btn {
    background-color: #fff2f0;
    color: #ff4d4f;
    border: 1rpx solid #ff4d4f;
}

/* 底部操作区 */
.footer-actions {
    display: flex;
    padding: 30rpx;
    background-color: #fff;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.cancel-btn {
    flex: 1;
    height: 90rpx;
    line-height: 90rpx;
    text-align: center;
    border: 1rpx solid #ddd;
    border-radius: 45rpx;
    color: #666;
    font-size: 30rpx;
    margin-right: 20rpx;
}

.submit-btn {
    flex: 1;
    height: 90rpx;
    line-height: 90rpx;
    text-align: center;
    background: linear-gradient(135deg, #8655a2, #1abc9c);
    border-radius: 45rpx;
    color: #fff;
    font-size: 30rpx;
    font-weight: 500;
    box-shadow: 0 4rpx 16rpx rgba(26, 188, 156, 0.3);
}

/* 加载遮罩 */
.loading-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 999;
}

.loading-content {
    background-color: #fff;
    padding: 40rpx;
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.loading-spinner {
    width: 80rpx;
    height: 80rpx;
    border: 6rpx solid #f3f3f3;
    border-top: 6rpx solid #1abc9c;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 20rpx;
}

.loading-text {
    font-size: 28rpx;
    color: #333;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>