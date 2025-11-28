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
          v-model="form.couponName" 
          placeholder="请输入优惠券名称"
          maxlength="50"
        />
      </view>
      
      <view class="form-item">
        <text class="label">优惠券类型</text>
        <radio-group @change="handleTypeChange" style="display: flex; gap: 40rpx;">
           <label style="display: flex; align-items: center;">
             <radio value="0" :checked="form.type === 0" color="#ff9800" style="transform: scale(0.8);"/>
             <text>满减券</text>
           </label>
           <label style="display: flex; align-items: center;">
             <radio value="1" :checked="form.type === 1" color="#ff9800" style="transform: scale(0.8);"/>
             <text>折扣券</text>
           </label>
        </radio-group>
      </view>

      <view class="form-item">
        <text class="label">{{ form.type === 1 ? '折扣(折)' : '优惠金额(元)' }}</text>
        <input 
          class="input" 
          type="digit" 
          v-model="form.discount" 
          :placeholder="form.type === 1 ? '请输入折扣，如8.8折' : '请输入优惠金额'"
        />
        <text v-if="form.type === 1" style="font-size: 24rpx; color: #999; margin-left: 20rpx;">填0为免单券</text>
      </view>

      <view class="form-item">
         <text class="label">使用门槛(元)</text>
         <input 
           class="input" 
           type="digit" 
           v-model="form.goalPrice" 
           placeholder="满多少元可用"
         />
      </view>
      
      <!-- 预览卡片 -->
      <view class="section-title">
        <text class="title-icon">👁️</text>
        <text class="title-text">预览效果</text>
      </view>
      
      <view class="preview-card">
        <view class="preview-left">
          <view class="amount-box">
            <!-- 满减券 -->
            <block v-if="form.type === 0">
              <text class="currency">¥</text>
              <text class="amount">{{ form.discount ? Number(form.discount).toFixed(0) : '0' }}</text>
            </block>
            <!-- 折扣券 -->
            <block v-else>
              <block v-if="Number(form.discount) === 0 && form.discount !== ''">
                 <text class="amount" style="font-size: 40rpx;">免单</text>
              </block>
              <block v-else>
                 <text class="amount">{{ formatDiscount(form.discount) || '0' }}</text>
                 <text class="currency" style="margin-left: 2rpx;">折</text>
              </block>
            </block>
          </view>
          <text class="coupon-type">{{ form.type === 0 ? '满减券' : (Number(form.discount) === 0 && form.discount !== '' ? '免单券' : '折扣券') }}</text>
        </view>
        
        <view class="preview-divider">
          <view class="divider-dot top"></view>
          <view class="divider-line"></view>
          <view class="divider-dot bottom"></view>
        </view>
        
        <view class="preview-right">
          <text class="preview-name">{{ form.couponName || '优惠券名称' }}</text>
          <text class="preview-desc">满{{ form.goalPrice || 0 }}元可用</text>
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
  couponName: '', // 之前是 name
  type: 0, // 0:满减券 1:折扣券
  discount: '',
  goalPrice: ''
  // description 已移除
})

const isEdit = computed(() => !!form.value.couponId)

// 格式化折扣
const formatDiscount = (val) => {
  const num = Number(val)
  if (isNaN(num)) return val
  // 如果是0-1之间的小数，乘以10
  if (num > 0 && num <= 1) {
    return parseFloat((num * 10).toFixed(1))
  }
  return num
}

const handleTypeChange = (e) => {
    form.value.type = parseInt(e.detail.value)
    form.value.discount = '' // 切换类型清空金额
}

// 表单验证
const validate = () => {
  if (!form.value.couponName || !form.value.couponName.trim()) {
    return '请输入优惠券名称'
  }
  if (form.value.discount === '' || form.value.discount === null) {
     return form.value.type === 1 ? '请输入折扣' : '请输入优惠金额'
  }
  
  const amount = Number(form.value.discount)
  if (isNaN(amount) || amount < 0) {
    return '请输入有效的数值'
  }

  // 如果是满减券，金额必须大于0
  if (form.value.type === 0 && amount <= 0) {
      return '满减金额必须大于0'
  }
  
  // 如果是折扣券，折扣不能超过10（10折即原价，无意义，但可以允许）
  if (form.value.type === 1 && amount > 10) {
      return '折扣不能超过10折'
  }
  
  if (!form.value.goalPrice) {
      return '请输入使用门槛'
  }

  if (amount > 9999) {
    return '数值过大'
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
      couponName: form.value.couponName.trim(),
      type: form.value.type,
      goalPrice: Number(form.value.goalPrice),
      discount: Number(form.value.discount)
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
      couponName: data.couponName || data.name || '',
      type: data.type !== undefined ? data.type : 0,
      goalPrice: data.goalPrice || '',
      discount: data.discount !== undefined ? data.discount : ''
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

