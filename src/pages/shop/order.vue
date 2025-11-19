<template>
    <view class="order-container">
        <!-- 顶部导航栏 -->
        <view class="nav-bar">
            <view class="back-btn" @click="goBack">
                <text class="back-icon">←</text>
            </view>
            <text class="nav-title">确认订单</text>
            <view class="placeholder"></view>
        </view>

        <!-- 主内容区域 -->
        <scroll-view class="order-content" scroll-y>
            <!-- 商品信息 -->
            <view class="section goods-section">
                <view class="section-title">商品信息</view>
                <view class="goods-info" v-if="goodsInfo">
                    <image :src="goodsInfo.imageUrl && goodsInfo.imageUrl[0]" class="goods-image" mode="aspectFill"></image>
                    <view class="goods-details">
                        <text class="goods-name">{{ goodsInfo.shopName }}</text>
                        <text class="goods-price">¥{{ goodsInfo.price }}</text>
                        <text class="goods-quantity">数量: 1</text>
                    </view>
                </view>
            </view>

            <!-- 收货地址 -->
            <view class="section address-section">
                <view class="section-header">
                    <text class="section-title">收货地址</text>
                    <text class="add-btn" @click="showAddressForm = true">+ 新增</text>
                </view>

                <!-- 地址列表 -->
                <view class="address-list">
                    <view 
                        v-for="addr in addressList" 
                        :key="addr.id"
                        class="address-item"
                        :class="{ 'address-selected': selectedAddress && selectedAddress.id === addr.id }"
                        @click="selectAddress(addr)"
                    >
                        <view class="address-radio">
                            <view v-if="selectedAddress && selectedAddress.id === addr.id" class="radio-checked"></view>
                        </view>
                        <view class="address-content">
                            <view class="address-header">
                                <text class="address-name">{{ addr.username }}</text>
                                <text class="address-phone">{{ addr.phone }}</text>
                            </view>
                            <text class="address-text">{{ addr.addressContent }}</text>
                        </view>
                        <view class="address-actions">
                            <text class="action-icon" @click.stop="editAddress(addr)">✏️</text>
                            <text class="action-icon" @click.stop="deleteAddress(addr.id)">🗑️</text>
                        </view>
                    </view>
                </view>

                <!-- 空状态 -->
                <view v-if="addressList.length === 0" class="empty-state">
                    <text class="empty-text">还没有收货地址，请先添加</text>
                </view>
            </view>

            <!-- 订单备注 -->
            <view class="section remark-section">
                <view class="section-title">订单备注</view>
                <textarea 
                    v-model="orderRemark"
                    class="remark-input"
                    placeholder="请输入订单备注（可选）"
                    maxlength="200"
                ></textarea>
                <text class="char-count">{{ orderRemark.length }}/200</text>
            </view>

            <!-- 订单金额 -->
            <view class="section price-section">
                <view class="price-row">
                    <text class="price-label">商品价格</text>
                    <text class="price-value">¥{{ goodsInfo ? goodsInfo.price : 0 }}</text>
                </view>
                <view class="price-row">
                    <text class="price-label">运费</text>
                    <text class="price-value">¥0.00</text>
                </view>
                <view class="price-row total">
                    <text class="price-label">合计</text>
                    <text class="price-value total-price">¥{{ goodsInfo ? goodsInfo.price : 0 }}</text>
                </view>
            </view>

            <view style="height: 120rpx;"></view>
        </scroll-view>

        <!-- 底部操作栏 -->
        <view class="bottom-action-bar">
            <button class="cancel-btn" @click="goBack">取消</button>
            <button class="confirm-btn" @click="submitOrder" :disabled="!selectedAddress || isSubmitting">
                {{ isSubmitting ? '提交中...' : '提交订单' }}
            </button>
        </view>

        <!-- 地址编辑弹窗 -->
        <view v-if="showAddressForm" class="modal-overlay" @click="closeAddressForm">
            <view class="modal-content" @click.stop>
                <view class="modal-header">
                    <text class="modal-title">{{ editingAddress ? '编辑地址' : '新增地址' }}</text>
                    <text class="close-btn" @click="closeAddressForm">×</text>
                </view>

                <view class="form-group">
                    <label class="form-label">收货人</label>
                    <input 
                        v-model="formData.username"
                        type="text"
                        class="form-input"
                        placeholder="请输入收货人姓名"
                    />
                </view>

                <view class="form-group">
                    <label class="form-label">电话号码</label>
                    <input 
                        v-model="formData.phone"
                        type="tel"
                        class="form-input"
                        placeholder="请输入电话号码"
                    />
                </view>

                <view class="form-group">
                    <label class="form-label">收货地址</label>
                    <textarea 
                        v-model="formData.addressContent"
                        class="form-textarea"
                        placeholder="请输入详细收货地址"
                        maxlength="200"
                    ></textarea>
                </view>

                <view class="form-actions">
                    <button class="form-btn cancel" @click="closeAddressForm">取消</button>
                    <button class="form-btn submit" @click="saveAddress">保存</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 商品ID
const goodsId = ref('')
// 商品信息
const goodsInfo = ref(null)
// 地址列表
const addressList = ref([])
// 选中的地址
const selectedAddress = ref(null)
// 订单备注
const orderRemark = ref('')
// 是否显示地址表单
const showAddressForm = ref(false)
// 编辑中的地址
const editingAddress = ref(null)
// 表单数据
const formData = ref({
    username: '',
    phone: '',
    addressContent: ''
})
// 是否提交中
const isSubmitting = ref(false)

// 获取商品信息
const getGoodsInfo = async () => {
    if (!goodsId.value) {
        uni.showToast({
            title: '商品ID不存在',
            icon: 'none'
        })
        return
    }

    try {
        const res = await request('/shop/getById', {
            method: 'GET',
            data: { id: goodsId.value }
        })

        if (res.code === 200) {
            goodsInfo.value = res.data
        } else {
            uni.showToast({
                title: res.msg || '获取商品信息失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('获取商品信息失败:', error)
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
    }
}

// 获取地址列表
const getAddressList = async () => {
    try {
        const res = await request('/orders/address/list', {
            method: 'GET'
        })

        if (res.code === 200) {
            addressList.value = res.data || []
            // 默认选中第一个地址
            if (addressList.value.length > 0) {
                selectedAddress.value = addressList.value[0]
            }
        } else {
            console.error('获取地址列表失败:', res.msg)
        }
    } catch (error) {
        console.error('获取地址列表失败:', error)
    }
}

// 选择地址
const selectAddress = (address) => {
    selectedAddress.value = address
}

// 打开地址编辑表单
const editAddress = (address) => {
    editingAddress.value = address
    formData.value = {
        username: address.username,
        phone: address.phone,
        addressContent: address.addressContent
    }
    showAddressForm.value = true
}

// 关闭地址表单
const closeAddressForm = () => {
    showAddressForm.value = false
    editingAddress.value = null
    formData.value = {
        username: '',
        phone: '',
        addressContent: ''
    }
}

// 保存地址
const saveAddress = async () => {
    // 验证表单
    if (!formData.value.username || !formData.value.phone || !formData.value.addressContent) {
        uni.showToast({
            title: '请填写完整的地址信息',
            icon: 'none'
        })
        return
    }

    // 验证电话号码格式
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(formData.value.phone)) {
        uni.showToast({
            title: '请输入有效的电话号码',
            icon: 'none'
        })
        return
    }

    try {
        uni.showLoading({
            title: '保存中...',
            mask: true
        })

        const payload = {
            username: formData.value.username,
            phone: formData.value.phone,
            addressContent: formData.value.addressContent
        }

        // 如果是编辑，添加ID
        if (editingAddress.value) {
            payload.id = editingAddress.value.id
        }

        const res = await request('/orders/address/update', {
            method: 'POST',
            data: payload
        })

        uni.hideLoading()

        if (res.code === 200) {
            uni.showToast({
                title: editingAddress.value ? '地址更新成功' : '地址添加成功',
                icon: 'success'
            })
            closeAddressForm()
            await getAddressList()
        } else {
            uni.showToast({
                title: res.msg || '保存地址失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('保存地址失败:', error)
        uni.hideLoading()
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
    }
}

// 删除地址
const deleteAddress = async (addressId) => {
    uni.showModal({
        title: '确认删除',
        content: '确定要删除这个地址吗？',
        success: async (res) => {
            if (res.confirm) {
                try {
                    uni.showLoading({
                        title: '删除中...',
                        mask: true
                    })

                    const deleteRes = await request('/orders/address/delete', {
                        method: 'POST',
                        data: { addressId }
                    })

                    uni.hideLoading()

                    if (deleteRes.code === 200) {
                        uni.showToast({
                            title: '地址已删除',
                            icon: 'success'
                        })
                        await getAddressList()
                    } else {
                        uni.showToast({
                            title: deleteRes.msg || '删除地址失败',
                            icon: 'none'
                        })
                    }
                } catch (error) {
                    console.error('删除地址失败:', error)
                    uni.hideLoading()
                    uni.showToast({
                        title: '网络错误，请稍后重试',
                        icon: 'none'
                    })
                }
            }
        }
    })
}

// 创建订单并获取支付参数
const createOrder = async () => {
    try {
        const orderData = {
            shopId: goodsInfo.value.id,
            price: goodsInfo.value.price,
            deliveryFee: 0,
            totalPrice: goodsInfo.value.price,
            note: orderRemark.value
        }

        console.log('创建订单，数据:', orderData)
        const res = await request('/pay/jsapi/create', {
            method: 'POST',
            data: orderData
        })

        console.log('创建订单响应:', res)

        if (res.code === 200) {
            const payParams = res.data
            console.log('获取支付参数:', {
                appId: payParams.appId,
                timeStamp: payParams.timeStamp || payParams.timestamp,
                nonceStr: payParams.nonceStr,
                packageVal: payParams.packageVal || payParams.package,
                signType: payParams.signType,
                paySign: payParams.paySign
            })
            return payParams // 返回支付参数
        } else {
            uni.showToast({
                title: res.msg || '创建订单失败',
                icon: 'none'
            })
            return null
        }
    } catch (error) {
        console.error('创建订单失败:', error)
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
        return null
    }
}


// 调起微信支付
const requestWechatPay = (payParams) => {
    console.log('调起微信支付参数:', payParams)
    
    // 处理字段名兼容性（后端使用@SerializedName注解）
    const timeStamp = payParams.timeStamp || payParams.timestamp
    const packageVal = payParams.packageVal || payParams.package
    
    // 验证必要参数
    if (!timeStamp || !payParams.nonceStr || !packageVal || !payParams.signType || !payParams.paySign) {
        console.error('支付参数不完整:', {
            timeStamp: timeStamp,
            nonceStr: payParams.nonceStr,
            packageVal: packageVal,
            signType: payParams.signType,
            paySign: payParams.paySign
        })
        return Promise.reject(new Error('支付参数不完整'))
    }
    
    return new Promise((resolve, reject) => {
        uni.requestPayment({
            provider: 'wxpay',
            timeStamp: String(timeStamp),
            nonceStr: String(payParams.nonceStr),
            package: String(packageVal),
            signType: String(payParams.signType),
            paySign: String(payParams.paySign),
            success: (res) => {
                console.log('支付成功:', res)
                resolve(true)
            },
            fail: (err) => {
                console.error('支付失败:', err)
                reject(err)
            },
            complete: (res) => {
                console.log('支付完成:', res)
            }
        })
    })
}

// 提交订单
const submitOrder = async () => {
    if (!selectedAddress.value) {
        uni.showToast({
            title: '请选择收货地址',
            icon: 'none'
        })
        return
    }

    if (!goodsInfo.value) {
        uni.showToast({
            title: '商品信息加载失败',
            icon: 'none'
        })
        return
    }

    isSubmitting.value = true

    try {
        uni.showLoading({
            title: '创建订单中...',
            mask: true
        })

        // 第一步：创建订单（同时获取支付参数）
        const payParams = await createOrder()
        if (!payParams) {
            isSubmitting.value = false
            uni.hideLoading()
            return
        }

        uni.hideLoading()

        // 第二步：调起微信支付
        try {
            await requestWechatPay(payParams)
            
            uni.showToast({
                title: '支付成功',
                icon: 'success'
            })
            
            // 延迟后跳转到订单列表
            setTimeout(() => {
                uni.navigateTo({
                    url: `/pages/shop/orderList`
                })
            }, 1500)
        } catch (payError) {
            uni.showToast({
                title: '支付已取消或失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('提交订单失败:', error)
        uni.hideLoading()
        uni.showToast({
            title: '网络错误，请稍后重试',
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

// 页面加载时获取参数
defineExpose({
    onLoad(options) {
        if (options && options.id) {
            goodsId.value = options.id
            getGoodsInfo()
            getAddressList()
        }
    }
})

// 页面挂载
onMounted(() => {
    if (!goodsId.value) {
        try {
            const pages = getCurrentPages()
            if (pages && pages.length > 0) {
                const currentPage = pages[pages.length - 1]
                
                if (currentPage.options && currentPage.options.id) {
                    goodsId.value = currentPage.options.id
                } else if (currentPage.$page && currentPage.$page.options && currentPage.$page.options.id) {
                    goodsId.value = currentPage.$page.options.id
                } else if (currentPage.$route && currentPage.$route.query && currentPage.$route.query.id) {
                    goodsId.value = currentPage.$route.query.id
                }
                
                if (goodsId.value) {
                    getGoodsInfo()
                    getAddressList()
                }
            }
        } catch (error) {
            console.error('获取页面参数失败:', error)
        }
    }
})
</script>

<style scoped>
.order-container {
    background-color: #f5f7fa;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

/* 导航栏 */
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
    cursor: pointer;
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

/* 内容区域 */
.order-content {
    flex: 1;
    padding-bottom: 120rpx;
}

/* 区域样式 */
.section {
    background-color: #fff;
    margin: 20rpx;
    padding: 30rpx;
    border-radius: 12rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
    position: relative;
    padding-left: 20rpx;
}

.section-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 6rpx;
    width: 8rpx;
    height: 30rpx;
    background-color: #1abc9c;
    border-radius: 4rpx;
}

/* 商品信息 */
.goods-section {
    margin-top: 20rpx;
}

.goods-info {
    display: flex;
    gap: 20rpx;
}

.goods-image {
    width: 150rpx;
    height: 150rpx;
    border-radius: 8rpx;
    object-fit: cover;
}

.goods-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.goods-name {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
    line-height: 1.4;
}

.goods-price {
    font-size: 32rpx;
    color: #ff4d4f;
    font-weight: bold;
}

.goods-quantity {
    font-size: 24rpx;
    color: #999;
}

/* 地址区域 */
.address-section {
    margin-top: 20rpx;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
}

.add-btn {
    font-size: 24rpx;
    color: #1abc9c;
    cursor: pointer;
    padding: 8rpx 16rpx;
    background-color: #f0fffe;
    border-radius: 20rpx;
}

.address-list {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
}

.address-item {
    display: flex;
    gap: 16rpx;
    padding: 16rpx;
    border: 2rpx solid #e8e8e8;
    border-radius: 8rpx;
    cursor: pointer;
    transition: all 0.3s ease;
}

.address-item:hover {
    border-color: #1abc9c;
    background-color: #f9fffe;
}

.address-selected {
    border-color: #1abc9c;
    background-color: #f9fffe;
}

.address-radio {
    width: 32rpx;
    height: 32rpx;
    border: 2rpx solid #e8e8e8;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-top: 4rpx;
}

.address-selected .address-radio {
    border-color: #1abc9c;
}

.radio-checked {
    width: 16rpx;
    height: 16rpx;
    background-color: #1abc9c;
    border-radius: 50%;
}

.address-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
}

.address-header {
    display: flex;
    gap: 16rpx;
    align-items: center;
}

.address-name {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
}

.address-phone {
    font-size: 24rpx;
    color: #999;
}

.address-text {
    font-size: 24rpx;
    color: #666;
    line-height: 1.4;
}

.address-actions {
    display: flex;
    gap: 12rpx;
    align-items: center;
}

.action-icon {
    font-size: 28rpx;
    cursor: pointer;
    padding: 8rpx;
}

.empty-state {
    text-align: center;
    padding: 40rpx 20rpx;
}

.empty-text {
    font-size: 26rpx;
    color: #999;
}

/* 备注区域 */
.remark-section {
    margin-top: 20rpx;
}

.remark-input {
    width: 100%;
    min-height: 120rpx;
    padding: 16rpx;
    border: 1rpx solid #e8e8e8;
    border-radius: 8rpx;
    font-size: 26rpx;
    color: #333;
    box-sizing: border-box;
}

.char-count {
    display: block;
    text-align: right;
    font-size: 20rpx;
    color: #999;
    margin-top: 8rpx;
}

/* 价格区域 */
.price-section {
    margin-top: 20rpx;
}

.price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
}

.price-row.total {
    border-bottom: none;
    padding-top: 20rpx;
    padding-bottom: 0;
}

.price-label {
    font-size: 26rpx;
    color: #666;
}

.price-value {
    font-size: 26rpx;
    color: #333;
    font-weight: 500;
}

.total-price {
    font-size: 36rpx;
    color: #ff4d4f;
    font-weight: bold;
}

/* 底部操作栏 */
.bottom-action-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 100rpx;
    background-color: #fff;
    display: flex;
    gap: 16rpx;
    align-items: center;
    padding: 0 20rpx;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
    z-index: 99;
    box-sizing: border-box;
}

.cancel-btn,
.confirm-btn {
    flex: 1;
    height: 80rpx;
    border: none;
    border-radius: 40rpx;
    font-size: 28rpx;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
}

.cancel-btn {
    background-color: #f0f0f0;
    color: #666;
}

.cancel-btn:active {
    background-color: #e0e0e0;
}

.confirm-btn {
    background: linear-gradient(135deg, #1abc9c, #b54aff);
    color: #fff;
}

.confirm-btn:active:not(:disabled) {
    opacity: 0.9;
}

.confirm-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* 模态框 */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: flex-end;
    z-index: 1000;
}

.modal-content {
    width: 100%;
    background-color: #fff;
    border-radius: 20rpx 20rpx 0 0;
    padding: 30rpx;
    max-height: 80vh;
    overflow-y: auto;
    animation: slideUp 0.3s ease;
}

@keyframes slideUp {
    from {
        transform: translateY(100%);
    }
    to {
        transform: translateY(0);
    }
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
}

.modal-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
}

.close-btn {
    font-size: 40rpx;
    color: #999;
    cursor: pointer;
}

/* 表单样式 */
.form-group {
    margin-bottom: 24rpx;
}

.form-label {
    display: block;
    font-size: 26rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 12rpx;
}

.form-input,
.form-textarea {
    width: 100%;
    padding: 16rpx;
    border: 1rpx solid #e8e8e8;
    border-radius: 8rpx;
    font-size: 26rpx;
    color: #333;
    box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
    border-color: #1abc9c;
    outline: none;
}

.form-textarea {
    min-height: 120rpx;
    resize: vertical;
}

.form-actions {
    display: flex;
    gap: 16rpx;
    margin-top: 30rpx;
}

.form-btn {
    flex: 1;
    height: 80rpx;
    border: none;
    border-radius: 40rpx;
    font-size: 28rpx;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
}

.form-btn.cancel {
    background-color: #f0f0f0;
    color: #666;
}

.form-btn.cancel:active {
    background-color: #e0e0e0;
}

.form-btn.submit {
    background: linear-gradient(135deg, #1abc9c, #b54aff);
    color: #fff;
}

.form-btn.submit:active {
    opacity: 0.9;
}
</style>
