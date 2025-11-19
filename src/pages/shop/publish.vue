<template>
    <view class="publish-container">
        <view class="form-header">
            <text class="header-title">{{ isEdit ? '编辑商品' : '发布商品' }}</text>
        </view>
        
        <!-- 表单区域 -->
        <scroll-view scroll-y class="form-scroll">
            <!-- 商品图片上传 -->
            <view class="form-item">
                <text class="item-label">商品图片</text>
                <text class="required-mark">*</text>
                <text class="item-tip">（最多上传9张图片）</text>
                
                <view class="image-upload-container">
                    <!-- 已上传的图片 -->
                    <view 
                        class="image-item" 
                        v-for="(url, index) in imageUrls" 
                        :key="index"
                    >
                        <image :src="url" mode="aspectFill" class="preview-image"></image>
                        <view class="delete-btn" @click="removeImage(index, url)">×</view>
                    </view>
                    
                    <!-- 上传按钮 -->
                    <view 
                        class="image-upload-btn" 
                        @click="chooseImage" 
                        v-if="imageUrls.length < 9"
                    >
                        <text class="upload-icon">+</text>
                    </view>
                </view>
            </view>
            
            <!-- 商品名称 -->
            <view class="form-item">
                <text class="item-label">商品名称</text>
                <text class="required-mark">*</text>
                <input 
                    type="text" 
                    v-model="formData.shopName" 
                    placeholder="请输入商品名称"
                    class="input-field"
                />
            </view>
            
            <!-- 商品类别 -->
            <view class="form-item">
                <text class="item-label">商品类别</text>
                <text class="required-mark">*</text>
                
                <view class="category-selection">
                    <view 
                        class="category-option" 
                        :class="{ active: formData.category === '学习用品' }"
                        @click="selectCategory('学习用品')"
                    >
                        学习用品
                    </view>
                    <view 
                        class="category-option" 
                        :class="{ active: formData.category === '电子产品' }"
                        @click="selectCategory('电子产品')"
                    >
                        电子产品
                    </view>
                    <view 
                        class="category-option" 
                        :class="{ active: formData.category === '衣物鞋包' }"
                        @click="selectCategory('衣物鞋包')"
                    >
                        衣物鞋包
                    </view>
                    <view 
                        class="category-option" 
                        :class="{ active: formData.category === '生活用品' }"
                        @click="selectCategory('生活用品')"
                    >
                        生活用品
                    </view>
                    <view 
                        class="category-option" 
                        :class="{ active: formData.category === '运动娱乐' }"
                        @click="selectCategory('运动娱乐')"
                    >
                        运动娱乐
                    </view>
                    <view 
                        class="category-option" 
                        :class="{ active: formData.category === '虚拟产品' }"
                        @click="selectCategory('虚拟产品')"
                    >
                        虚拟产品
                    </view>
                    <view 
                        class="category-option" 
                        :class="{ active: formData.category === '其它' }"
                        @click="selectCategory('其它')"
                    >
                        其它
                    </view>
                </view>
            </view>
            
            <!-- 商品价格 -->
            <view class="form-item">
                <text class="item-label">商品价格</text>
                <text class="required-mark">*</text>
                <input 
                    type="digit" 
                    v-model="formData.price" 
                    placeholder="请输入商品价格"
                    class="input-field"
                />
            </view>
            
            <!-- 商品介绍 -->
            <view class="form-item">
                <text class="item-label">商品介绍</text>
                <text class="required-mark">*</text>
                <textarea 
                    v-model="formData.introduce" 
                    placeholder="请详细描述您的商品，例如规格、成色等"
                    class="textarea-field"
                />
            </view>
        </scroll-view>
        
        <!-- 底部提交区域 -->
        <view class="footer-actions">
            <view class="cancel-btn" @click="navigateBack">取消</view>
            <view class="submit-btn" @click="submitForm">{{ isEdit ? '保存修改' : '发布商品' }}</view>
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
import { ref, reactive, onMounted } from 'vue';
import request from '@/utils/request.js';

// 表单数据
const formData = reactive({
    shopName: '',  // 商品名称
    price: '',     // 商品价格
    introduce: '', // 商品介绍
    category: '',  // 商品类别
});

// 图片URL列表
const imageUrls = ref([]);

// 提交状态
const isSubmitting = ref(false);
const loadingText = ref('上传中...');

// 编辑模式标志
const isEdit = ref(false);
const goodsId = ref('');
const originalImages = ref([]);

// 页面加载时检查是否为编辑模式
onMounted(() => {
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    
    // 获取传递的参数
    if (currentPage && currentPage.options && currentPage.options.id) {
        goodsId.value = currentPage.options.id;
        isEdit.value = true;
        fetchGoodsDetail(goodsId.value);
    }
});

// 获取商品详情
const fetchGoodsDetail = async (id) => {
    isSubmitting.value = true;
    loadingText.value = '加载商品信息...';
    
    try {
        const res = await request('/shop/getById', {
            method: 'GET',
            data: { id }
        });
        
        if (res.code === 200 && res.data) {
            const goodsData = res.data;
            
            // 填充表单数据
            formData.shopName = goodsData.shopName || '';
            formData.price = goodsData.price ? goodsData.price.toString() : '';
            formData.introduce = goodsData.introduce || '';
            formData.category = goodsData.category || '';
            
            // 填充图片列表
            if (goodsData.imageUrl && Array.isArray(goodsData.imageUrl)) {
                imageUrls.value = [...goodsData.imageUrl];
                originalImages.value = [...goodsData.imageUrl]; // 保存原始图片列表，用于比较哪些被删除了
            }
        } else {
            uni.showToast({
                title: '获取商品信息失败',
                icon: 'none'
            });
            setTimeout(() => {
                navigateBack();
            }, 1500);
        }
    } catch (error) {
        console.error('获取商品信息失败:', error);
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        });
        setTimeout(() => {
            navigateBack();
        }, 1500);
    } finally {
        isSubmitting.value = false;
    }
};

// 选择图片
const chooseImage = () => {
    uni.chooseImage({
        count: 9 - imageUrls.value.length, // 最多还能选几张
        sizeType: ['compressed'], // 压缩图片
        sourceType: ['album', 'camera'], // 相册或相机
        success: (res) => {
            // 预处理图片
            handleImages(res.tempFilePaths);
        },
        fail: (err) => {
            console.error('选择图片失败:', err);
        }
    });
};

// 处理并上传图片
const handleImages = async (tempFilePaths) => {
    if (!tempFilePaths || tempFilePaths.length === 0) return;
    
    isSubmitting.value = true;
    loadingText.value = '上传图片中...';
    
    for (const filePath of tempFilePaths) {
        try {
            const result = await uploadImage(filePath);
            if (result) {
                imageUrls.value.push(result);
            }
        } catch (error) {
            console.error('上传图片失败:', error);
            uni.showToast({
                title: '图片上传失败，请重试',
                icon: 'none'
            });
        }
    }
    
    isSubmitting.value = false;
};

// 上传单张图片
const uploadImage = (filePath) => {
    return new Promise((resolve, reject) => {
        uni.uploadFile({
            url: '/system/uploadImg', // 使用请求工具中的baseUrl
            filePath: filePath,
            header: {
                'Content-Type': 'multipart/form-data',
                "sa-token": uni.getStorageSync('sa-token')
            },
            name: 'file',
            success: (uploadRes) => {
                try {
                    // 尝试解析响应数据
                    const data = JSON.parse(uploadRes.data);
                    if (data.code === 200) {
                        resolve(data.data); // 返回图片URL
                    } else {
                        reject(data.msg || '上传失败');
                    }
                } catch (e) {
                    reject('上传响应解析失败');
                }
            },
            fail: (err) => {
                reject(err);
            }
        });
    });
};

// 移除图片
const removeImage = async (index, url) => {
    // 如果是编辑模式且图片在原始列表中，需要调用删除接口
    if (isEdit.value && originalImages.value.includes(url)) {
        try {
            const res = await request('/shop/deleteImg', {
                method: 'POST',
                data: url
            });
            
            if (res.code !== 200) {
                uni.showToast({
                    title: '删除图片失败',
                    icon: 'none'
                });
                return;
            }
        } catch (error) {
            console.error('删除图片失败:', error);
            uni.showToast({
                title: '删除图片失败',
                icon: 'none'
            });
            return;
        }
    }
    
    // 从列表中移除图片
    imageUrls.value.splice(index, 1);
};

// 选择类别
const selectCategory = (category) => {
    formData.category = category;
};

// 表单验证
const validateForm = () => {
    if (imageUrls.value.length === 0) {
        uni.showToast({
            title: '请至少上传一张商品图片',
            icon: 'none'
        });
        return false;
    }
    
    if (!formData.shopName.trim()) {
        uni.showToast({
            title: '请输入商品名称',
            icon: 'none'
        });
        return false;
    }
    
    if (!formData.category) {
        uni.showToast({
            title: '请选择商品类别',
            icon: 'none'
        });
        return false;
    }
    
    if (!formData.price) {
        uni.showToast({
            title: '请输入商品价格',
            icon: 'none'
        });
        return false;
    }
    
    // 验证价格格式
    const priceNum = parseFloat(formData.price);
    if (isNaN(priceNum) || priceNum <= 0) {
        uni.showToast({
            title: '请输入有效的商品价格',
            icon: 'none'
        });
        return false;
    }
    
    if (!formData.introduce.trim()) {
        uni.showToast({
            title: '请输入商品介绍',
            icon: 'none'
        });
        return false;
    }
    
    return true;
};

// 提交表单
const submitForm = async () => {
    if (!validateForm()) return;
    
    isSubmitting.value = true;
    loadingText.value = isEdit.value ? '保存中...' : '提交中...';
    
    try {
        const endpoint = isEdit.value ? '/shop/update' : '/shop/publish';
        const data = {
            shopName: formData.shopName,
            price: parseFloat(formData.price),
            introduce: formData.introduce,
            imageUrl: imageUrls.value,
            category: formData.category
        };
        
        // 如果是编辑模式，添加商品ID
        if (isEdit.value) {
            data.id = goodsId.value;
        }
        
        const res = await request(endpoint, {
            method: 'POST',
            data: data
        });
        
        if (res.code === 200) {
            uni.showToast({
                title: isEdit.value ? '修改成功' : '发布成功-请等待审核',
                icon: 'success'
            });
            
            // 延迟返回，让用户看到成功提示
            setTimeout(() => {
                navigateBack();
            }, 1500);
        } else {
            uni.showToast({
                title: res.msg || (isEdit.value ? '修改失败' : '发布失败'),
                icon: 'none'
            });
        }
    } catch (error) {
        console.error(isEdit.value ? '修改商品失败:' : '发布商品失败:', error);
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        });
    } finally {
        isSubmitting.value = false;
    }
};

// 返回上一页
const navigateBack = () => {
    uni.navigateBack();
};
</script>

<style>
.publish-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #f5f7fa;
    position: relative;
}

.form-header {
    padding: 30rpx;
    background-color: #fff;
    border-bottom: 1rpx solid #eee;
}

.header-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
}

.form-scroll {
    flex: 1;
    padding: 0 30rpx;
}

.form-item {
    margin: 30rpx 0;
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

.item-tip {
    font-size: 24rpx;
    color: #999;
    margin-left: 10rpx;
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

/* 图片上传区域 */
.image-upload-container {
    display: flex;
    flex-wrap: wrap;
    margin-top: 20rpx;
}

.image-item {
    width: 180rpx;
    height: 180rpx;
    margin-right: 20rpx;
    margin-bottom: 20rpx;
    position: relative;
    border-radius: 8rpx;
    overflow: hidden;
}

.preview-image {
    width: 100%;
    height: 100%;
}

.delete-btn {
    position: absolute;
    top: 0;
    right: 0;
    width: 40rpx;
    height: 40rpx;
    background-color: rgba(0, 0, 0, 0.6);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    border-bottom-left-radius: 8rpx;
}

.image-upload-btn {
    width: 180rpx;
    height: 180rpx;
    border: 2rpx dashed #ccc;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    margin-bottom: 20rpx;
    background-color: #fafafa;
}

.upload-icon {
    font-size: 60rpx;
    color: #ccc;
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
    box-shadow: 0 4rpx 16rpx var(--theme-color-shadow);
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
    border-top: 6rpx solid var(--theme-color);
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

/* 类别选择样式 */
.category-selection {
    display: flex;
    flex-wrap: wrap;
    margin-top: 20rpx;
}

.category-option {
    padding: 15rpx 25rpx;
    background-color: #f5f7fa;
    border-radius: 30rpx;
    margin-right: 20rpx;
    margin-bottom: 20rpx;
    font-size: 26rpx;
    color: #666;
    transition: all 0.3s;
}

.category-option.active {
    background-color: #1abc9c;
    color: #fff;
}
</style>