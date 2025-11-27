<template>
  <view class="coupon-edit-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">{{ isEdit ? '编辑优惠券' : '新增优惠券' }}</text>
      <text class="page-subtitle">{{ isEdit ? '修改优惠券信息' : '填写优惠券基本信息' }}</text>
    </view>
    
    <view class="form">
      <!-- 基本信息 -->
      <view class="section-title">
        <text class="title-icon">🎫</text>
        <text class="title-text">基本信息</text>
      </view>
      
      <view class="form-item">
        <text class="label">优惠券名称</text>
        <input 
          class="input" 
          v-model="form.name" 
          placeholder="请输入优惠券名称"
          maxlength="50"
        />
      </view>
      
      <view class="form-item">
        <text class="label">优惠金额(元)</text>
        <input 
          class="input" 
          type="digit" 
          v-model="form.discount" 
          placeholder="请输入优惠金额"
        />
      </view>
      
      <view class="form-item form-item--column">
        <text class="label">优惠券描述</text>
        <textarea 
          class="textarea" 
          v-model="form.description" 
          placeholder="选填，请输入优惠券使用说明或描述"
          maxlength="200"
        />
        <view class="char-count">{{ (form.description || '').length }}/200</view>
      </view>
      
      <!-- 预览卡片 -->
      <view class="section-title">
        <text class="title-icon">👁️</text>
        <text class="title-text">预览效果</text>
      </view>
      
      <view class="preview-card">
        <view class="preview-left">
          <view class="amount-box">
            <text class="currency">¥</text>
            <text class="amount">{{ form.discount ? Number(form.discount).toFixed(0) : '0' }}</text>
          </view>
          <text class="coupon-type">优惠券</text>
        </view>
        
        <view class="preview-divider">
          <view class="divider-dot top"></view>
          <view class="divider-line"></view>
          <view class="divider-dot bottom"></view>
        </view>
        
        <view class="preview-right">
          <text class="preview-name">{{ form.name || '优惠券名称' }}</text>
          <text class="preview-desc">{{ form.description || '优惠券描述信息' }}</text>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="actions">
        <button class="btn save" @click="submit">保存</button>
        <button class="btn cancel" @click="cancel">取消</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '../../../utils/request.js'

const API_BASE = '/admin/coupon'

const form = ref({
  couponId: undefined,
  name: '',
  discount: '',
  description: ''
})

const isEdit = computed(() => !!form.value.couponId)

// 表单验证
const validate = () => {
  if (!form.value.name || !form.value.name.trim()) {
    return '请输入优惠券名称'
  }
  if (!form.value.discount) {
    return '请输入优惠金额'
  }
  const amount = Number(form.value.discount)
  if (isNaN(amount) || amount <= 0) {
    return '请输入有效的优惠金额'
  }
  if (amount > 9999) {
    return '优惠金额不能超过9999元'
  }
  return ''
}

// 提交
const submit = async () => {
  const errorMsg = validate()
  if (errorMsg) {
    return uni.showToast({ title: errorMsg, icon: 'none' })
  }
  
  try {
    uni.showLoading({ title: '保存中...' })
    
    const data = {
      name: form.value.name.trim(),
      discount: Number(form.value.discount),
      description: form.value.description ? form.value.description.trim() : ''
    }
    
    // 如果是编辑模式，添加 couponId
    if (isEdit.value) {
      data.couponId = form.value.couponId
    }
    
    const url = isEdit.value ? `${API_BASE}/updateCoupon` : `${API_BASE}/addCoupon`
    
    await request(url, {
      method: 'POST',
      data: data,
      header: { 'Content-Type': 'application/json' }
    })
    
    uni.hideLoading()
    uni.showToast({ title: '保存成功', icon: 'success' })
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    console.error('保存失败：', e)
    uni.hideLoading()
    uni.showToast({ title: e?.errMsg || '保存失败', icon: 'none' })
  }
}

// 取消
const cancel = () => {
  uni.navigateBack()
}

// 加载详情（编辑模式）
const loadDetail = async (id) => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await request(`${API_BASE}/detail`, {
      method: 'GET',
      data: { couponId: Number(id) }
    })
    const data = res.data || res
    form.value = {
      couponId: data.couponId,
      name: data.name || '',
      discount: data.discount || '',
      description: data.description || ''
    }
    uni.hideLoading()
  } catch (e) {
    console.error('加载详情失败：', e)
    uni.hideLoading()
    uni.showToast({ title: '加载详情失败', icon: 'none' })
  }
}

onLoad((options) => {
  if (options && options.id) {
    form.value.couponId = options.id
    loadDetail(options.id)
  }
})
</script>

<style scoped>
/* 主容器 - 茶叶主题背景 */
.coupon-edit-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #F3F8EA 0%, #E8F5E9 100%);
  padding: 20rpx;
  padding-bottom: 40rpx;
}

/* 页面标题 */
.page-header {
  padding: 32rpx 24rpx;
  margin-bottom: 20rpx;
}

.page-title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 8rpx;
  background: linear-gradient(135deg, #2c3e50, #ff9800);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  display: block;
  font-size: 26rpx;
  color: #7f8c8d;
  font-weight: 500;
}

/* 表单容器 - 圆角卡片设计 */
.form {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

/* 分区标题 */
.section-title {
  display: flex;
  align-items: center;
  padding: 24rpx 0 16rpx;
  margin-top: 20rpx;
  border-bottom: 3rpx solid #fff3e0;
}

.section-title:first-child {
  margin-top: 0;
}

.title-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #34495e;
  background: linear-gradient(135deg, #34495e, #ff9800);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 表单项 */
.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 2rpx solid #f0f0f0;
}

.form-item--column {
  flex-direction: column;
  align-items: flex-start;
  gap: 16rpx;
}

/* 标签 */
.label {
  width: 180rpx;
  color: #2c3e50;
  font-size: 28rpx;
  font-weight: 600;
}

.form-item--column .label {
  width: 100%;
  margin-bottom: 8rpx;
  color: #34495e;
}

/* 输入框 */
.input {
  flex: 1;
  height: 80rpx;
  background: #fef7ed;
  border: 2rpx solid #fff3e0;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333;
  transition: all 0.3s ease;
}

.input:focus {
  background: #fff8e1;
  border-color: #ffb74d;
}

/* 文本域 */
.textarea {
  width: 100%;
  min-height: 160rpx;
  background: #fef7ed;
  border: 2rpx solid #fff3e0;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  box-sizing: border-box;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  transition: all 0.3s ease;
}

.textarea:focus {
  background: #fff8e1;
  border-color: #ffb74d;
}

.char-count {
  align-self: flex-end;
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

/* 预览卡片 */
.preview-card {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  margin-top: 20rpx;
}

.preview-left {
  width: 180rpx;
  background: linear-gradient(135deg, #ff9800, #ffa726);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30rpx 20rpx;
}

.amount-box {
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
}

.currency {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
  margin-right: 4rpx;
}

.amount {
  font-size: 56rpx;
  color: #fff;
  font-weight: bold;
  line-height: 1;
}

.coupon-type {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.preview-divider {
  width: 2rpx;
  position: relative;
  background: transparent;
  margin: 0 12rpx;
}

.divider-line {
  position: absolute;
  top: 20rpx;
  bottom: 20rpx;
  left: 0;
  right: 0;
  border-left: 2rpx dashed #e0e0e0;
}

.divider-dot {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: linear-gradient(180deg, #F3F8EA 0%, #E8F5E9 100%);
}

.divider-dot.top {
  top: -10rpx;
}

.divider-dot.bottom {
  bottom: -10rpx;
}

.preview-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24rpx;
}

.preview-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
  line-height: 1.4;
}

.preview-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

/* 按钮组 */
.actions {
  display: flex;
  gap: 20rpx;
  padding-top: 40rpx;
  margin-top: 20rpx;
}

/* 按钮基础样式 */
.btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

/* 保存按钮 */
.btn.save {
  background: linear-gradient(135deg, #ff9800, #ffa726);
  color: #fff;
  box-shadow: 0 6rpx 20rpx rgba(255, 152, 0, 0.4);
}

.btn.save:active {
  box-shadow: 0 3rpx 12rpx rgba(255, 152, 0, 0.4);
}

/* 取消按钮 */
.btn.cancel {
  background: linear-gradient(135deg, #bdbdbd, #9e9e9e);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(158, 158, 158, 0.3);
}
</style>

