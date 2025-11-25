<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-content" :style="navContentStyle">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">←</text>
        </view>
        <text class="nav-title">发布跑腿订单</text>
        <view class="placeholder"></view>
      </view>
    </view>
    
    <!-- 占位符，防止内容被导航栏遮挡 -->
    <view :style="{ height: totalNavHeight + 'px' }"></view>

    <scroll-view class="content" scroll-y>
      <!-- 当前学校显示 -->
      <view class="current-school" v-if="currentSchoolName">
        <text class="school-label">当前学校：</text>
        <text class="school-val">{{ currentSchoolName }}</text>
      </view>

      <!-- 取货信息 -->
      <view class="section">
        <view class="section-title">取货信息</view>
        <view class="form-item">
          <text class="label">取货地址</text>
          <input class="input" v-model="form.startingAddress" placeholder="请输入取货地点（如：学校门口外卖柜）" />
        </view>
        <view class="form-item">
          <text class="label">是否已送达</text>
          <view style="flex: 1; display: flex; align-items: center;">
             <switch :checked="form.isDelivered" @change="handleIsDeliveredChange" color="#1abc9c" style="transform: scale(0.8);" />
             <text style="font-size: 24rpx; color: #666; margin-left: 20rpx;">{{ form.isDelivered ? '已送到取货点' : '未送达' }}</text>
          </view>
        </view>
        <view class="form-item" v-if="!form.isDelivered">
          <text class="label">预计到达</text>
          <picker mode="time" :value="form.takeoutDeliveryTime" @change="handleTimeChange">
            <view class="picker-view">
              <text class="picker-text" :class="{ 'placeholder-text': !form.takeoutDeliveryTime }">
                {{ form.takeoutDeliveryTime || '请选择预计到达时间' }}
              </text>
              <text class="picker-arrow">＞</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- 送达信息 -->
      <view class="section">
        <view class="section-header">
          <view class="section-title">送达信息</view>
          <view class="select-address-btn" @click="showAddressList = true">
            <text>选择收货地址</text>
            <text class="arrow">＞</text>
          </view>
        </view>
        
        <!-- 选中的地址展示 -->
        <view class="selected-address" v-if="selectedAddress" @click="showAddressList = true">
          <view class="user-row">
            <text class="name">{{ selectedAddress.username }}</text>
            <text class="phone">{{ selectedAddress.phone }}</text>
          </view>
          <view class="address-row">
            <text class="address-text">{{ getFullAddress(selectedAddress) }}</text>
          </view>
        </view>
        <view class="no-address" v-else @click="showAddressList = true">
          <text>请选择送达地址</text>
        </view>

        <view class="warning-tip">
          <text>温馨提示：请注意您所选择的地址的手机号与外卖信息一致，否则可能无法取件。</text>
        </view>
      </view>

      <!-- 物品及费用 -->
      <view class="section">
        <view class="section-title">物品及费用</view>
        
        <!-- 必填/核心信息 -->
        <view class="form-item">
          <text class="label">是否送上楼</text>
          <view style="flex: 1; display: flex; align-items: center;">
             <switch :checked="form.isFloor" @change="handleIsFloorChange" color="#1abc9c" style="transform: scale(0.8);" />
             <text style="font-size: 24rpx; color: #ff5722; margin-left: 20rpx;">{{ form.isFloor ? '+0.5元' : '' }}</text>
          </view>
        </view>
        <view class="form-item">
          <text class="label">配送费(元)</text>
          <input class="input price-input" type="digit" v-model="form.deliveryFee" placeholder="选择宿舍后自动显示" disabled />
        </view>
        <view class="form-item">
          <text class="label">最长等待(分)</text>
          <input class="input" type="number" v-model="form.waitTime" placeholder="默认为15分钟，超时无人接单自动取消" />
        </view>

        <!-- 更多选项开关 -->
        <view class="more-options-toggle" @click="showMoreInfo = !showMoreInfo">
          <text>{{ showMoreInfo ? '收起选填信息' : '展开更多选填信息' }}</text>
          <text class="arrow" :class="{ up: showMoreInfo }">∨</text>
        </view>

        <!-- 选填信息折叠区域 -->
        <view v-if="showMoreInfo" class="more-info-area">
          <view class="form-item">
            <text class="label">外卖单号</text>
            <input class="input" v-model="form.takeoutNo" placeholder="请输入外卖单号/取货码（方便核对）" />
          </view>
          <view class="form-item">
            <text class="label">物品价值(元)</text>
            <input class="input" type="digit" v-model="form.deliveryShopValue" placeholder="预估物品价值（可选）" />
          </view>
          <view class="form-item vertical">
            <text class="label">备注</text>
            <textarea class="textarea" v-model="form.note" placeholder="如有其他要求请填写,如备注下单平台，店铺商品名称等" maxlength="100"></textarea>
          </view>
          <view class="form-item">
            <text class="label">匿名下单</text>
            <switch :checked="form.isNick" @change="form.isNick = $event.detail.value" color="#1abc9c" style="transform: scale(0.8);" />
          </view>
        </view>
      </view>

      <view class="spacer"></view>
    </scroll-view>

    <!-- 底部提交栏 -->
    <view class="bottom-bar">
      <view class="total-price">
        <text>待支付：</text>
        <text class="price">¥{{ form.deliveryFee || '0' }}</text>
      </view>
      <button class="submit-btn" @click="submitOrder" :disabled="isSubmitting">立即下单</button>
    </view>

    <!-- 地址选择弹窗 -->
    <view class="popup-mask" v-if="showAddressList" @click="showAddressList = false">
      <view class="popup-content" @click.stop>
        <view class="popup-header">
          <text class="popup-title">选择收货地址</text>
          <view class="popup-actions">
            <text class="manage-btn" @click="openAddAddress">新增地址</text>
            <text class="close-icon" @click="showAddressList = false">×</text>
          </view>
        </view>
        <scroll-view scroll-y class="address-scroll">
          <view 
            class="address-item" 
            v-for="addr in addressList" 
            :key="addr.id"
            @click="selectAddress(addr)"
          >
            <view class="addr-info">
              <view class="addr-user">
                <text class="addr-name">{{ addr.username }}</text>
                <text class="addr-phone">{{ addr.phone }}</text>
              </view>
              <text class="addr-detail">{{ getFullAddress(addr) }}</text>
            </view>
            <view class="addr-actions">
              <view class="action-btn" @click.stop="editAddress(addr)">
                <image src="/static/icon/edit.png" class="action-icon"></image>
              </view>
              <view class="action-btn" @click.stop="deleteAddress(addr.id)">
                <image src="/static/icon/delete.png" class="action-icon"></image>
              </view>
            </view>
            <view class="addr-check" v-if="selectedAddress && selectedAddress.id === addr.id">✓</view>
          </view>
          <!-- 空状态 -->
          <view v-if="addressList.length === 0" class="empty-address">
            <text>暂无地址，请点击右上角新增</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 地址编辑/新增弹窗 -->
    <view v-if="showAddressForm" class="popup-mask" @click="closeAddressForm">
      <view class="popup-content" @click.stop>
        <view class="popup-header">
          <text class="popup-title">{{ editingAddress ? '编辑地址' : '新增地址' }}</text>
          <text class="close-icon" @click="closeAddressForm">×</text>
        </view>
        <view class="form-container">
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
            <label class="form-label">宿舍楼</label>
            <picker 
                @change="handleEditDormitoryChange" 
                :value="editingDormitoryIndex" 
                :range="dormitoryList" 
                range-key="dormitoryName"
            >
                <view class="picker-view input-picker" style="border: 1rpx solid #e8e8e8; border-radius: 8rpx; height: 80rpx; padding: 0 20rpx;">
                    <text class="picker-text" :class="{ 'placeholder-text': !formData.dormitoryId }">
                    {{ formData.dormitoryId ? (dormitoryList[editingDormitoryIndex]?.dormitoryName) : '请选择宿舍楼' }}
                    </text>
                    <text class="picker-arrow">＞</text>
                </view>
            </picker>
          </view>
          <view class="form-group">
            <label class="form-label">收货地址</label>
            <textarea 
              v-model="formData.addressContent"
              class="form-textarea"
              placeholder="请输入详细门牌号/房间号"
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

  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request.js'

// 导航栏相关
const statusBarHeight = ref(0)
const navBarHeight = ref(44) // 默认导航栏高度
const totalNavHeight = computed(() => statusBarHeight.value + navBarHeight.value)

const navBarStyle = computed(() => ({
  paddingTop: `${statusBarHeight.value}px`,
  height: `${navBarHeight.value}px`,
  backgroundColor: '#fff',
  position: 'fixed',
  top: 0,
  left: 0,
  width: '100%',
  zIndex: 100
}))

const navContentStyle = computed(() => ({
  height: `${navBarHeight.value}px`,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0 30rpx'
}))

const form = ref({
  startingAddress: '',
  takeoutNo: '',
  takeoutDeliveryTime: (() => {
    const now = new Date()
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    return `${hours}:${minutes}`
  })(),
  deliveryFee: '',
  isFloor: false,
  isDelivered: true, // 是否已送达，默认已送达
  deliveryShopValue: '',
  waitTime: '',
  note: '',
  isNick: true,
  dormitoryId: null,
  schoolId: null // 默认学校ID
})

const currentSchoolName = ref('')
const selectedAddress = ref(null)
const addressList = ref([])
const showAddressList = ref(false)
const isSubmitting = ref(false)
const showMoreInfo = ref(false) // 是否展开更多信息
const baseDeliveryFee = ref(0) // 基础配送费


// 地址表单相关
const showAddressForm = ref(false)
const editingAddress = ref(null)
const formData = ref({
  username: '',
  phone: '',
  addressContent: '',
  dormitoryId: null,
  schoolId: null
})

const dormitoryList = ref([])
const dormitoryIndex = ref(0)
const editingDormitoryIndex = ref(0) // 编辑地址时的宿舍索引
const selectedDormitoryName = ref('')

onMounted(() => {
  // 获取状态栏高度
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20
  
  // #ifdef MP-WEIXIN
  // 获取胶囊按钮位置，计算导航栏高度
  const menuButtonInfo = uni.getMenuButtonBoundingClientRect()
  if (menuButtonInfo) {
    // 导航栏高度 = (胶囊顶部 - 状态栏底部) * 2 + 胶囊高度
    // 这里简化处理，通常胶囊垂直居中
    const gap = menuButtonInfo.top - statusBarHeight.value
    navBarHeight.value = menuButtonInfo.height + (gap * 2)
  }
  // #endif

  // 获取绑定的学校信息
  const schoolId = uni.getStorageSync('schoolId')
  if (schoolId) {
    form.value.schoolId = schoolId
    getSchoolInfo(schoolId)
    // 先获取宿舍列表，再获取地址列表，确保自动选中地址时能匹配到宿舍信息计算费用
    getDormitoryList().then(() => {
        getAddressList()
    })
  } else {
    uni.showToast({
      title: '请先在首页绑定学校',
      icon: 'none',
      duration: 2000
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }
})

// 获取学校信息
const getSchoolInfo = async (schoolId) => {
  try {
    const res = await request('/delivery/school/getName', {
      method: 'GET',
      data: { schoolId }
    })
    if (res.code === 200 && res.data) {
      currentSchoolName.value = res.data.schoolName
    }
  } catch (e) {
    console.error('获取学校信息失败', e)
  }
}

const goBack = () => {
  uni.navigateBack()
}

// 获取地址列表
const getAddressList = async () => {
  try {
    const res = await request('/address/list', { method: 'GET' })
    if (res.code === 200) {
      const list = res.data || []
      addressList.value = list
      
      // 尝试获取上次使用的地址ID
      const lastUsedId = uni.getStorageSync('lastSelectedAddressId')
      let targetAddr = null
      
      if (lastUsedId) {
          targetAddr = list.find(item => item.id === lastUsedId)
      }
      
      // 如果找到了上次使用的地址，选中它
      if (targetAddr) {
          selectAddress(targetAddr)
      }
      // 如果没有上次使用的地址（或地址已被删除），但列表不为空，默认选中第一个
      else if (addressList.value.length > 0) {
        selectAddress(addressList.value[0])
      }
    }
  } catch (e) {
    console.error('获取地址失败', e)
  }
}

// 获取宿舍列表
const getDormitoryList = async () => {
  try {
    const res = await request('/delivery/getDormitoryList', {
      method: 'GET',
      data: { schoolId: form.value.schoolId }
    })
    if (res.code === 200 && res.data) {
      dormitoryList.value = res.data
    }
  } catch (e) {
    console.error('获取宿舍列表失败', e)
  }
}

// 选择宿舍
const handleDormitoryChange = (e) => {
  const index = e.detail.value
  dormitoryIndex.value = index
  const item = dormitoryList.value[index]
  if (item) {
    // 优先使用 dormitoryId，如果不存在则使用 id
    form.value.dormitoryId = item.dormitoryId || item.id
    selectedDormitoryName.value = item.dormitoryName
    
    // 配送费由宿舍楼决定
    if (item.deliveryPrice !== undefined && item.deliveryPrice !== null) {
      baseDeliveryFee.value = item.deliveryPrice
      calculateFee()
    }
  }
}

// 计算配送费
const calculateFee = () => {
  let fee = baseDeliveryFee.value
  if (form.value.isFloor) {
    fee += 0.5
  }
  // 保留两位小数，防止精度问题
  form.value.deliveryFee = Math.round(fee * 100) / 100
}

// 是否送上楼改变
const handleIsFloorChange = (e) => {
  form.value.isFloor = e.detail.value
  calculateFee()
}

// 是否送达状态改变
const handleIsDeliveredChange = (e) => {
  form.value.isDelivered = e.detail.value
  if (form.value.isDelivered) {
    // 如果已送达，自动设置时间为当前时间
    const now = new Date()
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    form.value.takeoutDeliveryTime = `${hours}:${minutes}`
  } else {
    // 如果未送达，默认选中当前时间，方便用户调整
    const now = new Date()
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    form.value.takeoutDeliveryTime = `${hours}:${minutes}`
  }
}

// 选择时间
const handleTimeChange = (e) => {
  form.value.takeoutDeliveryTime = e.detail.value
}

// 选择地址
const selectAddress = (addr) => {
  selectedAddress.value = addr
  showAddressList.value = false
  
  // 保存选中的地址ID到本地存储
  uni.setStorageSync('lastSelectedAddressId', addr.id)
  
  // 如果地址关联了宿舍，自动设置
  if (addr.dormitoryId) {
    const item = dormitoryList.value.find(d => d.dormitoryId === addr.dormitoryId || d.id === addr.dormitoryId)
    if (item) {
        form.value.dormitoryId = item.dormitoryId || item.id
        
        // 配送费由宿舍楼决定
        if (item.deliveryPrice !== undefined && item.deliveryPrice !== null) {
            baseDeliveryFee.value = item.deliveryPrice
            calculateFee()
        }
    }
  } else {
    // 如果地址没有宿舍信息，可能需要提示或者允许手动选择（这里简化处理，假设地址必须有宿舍）
     form.value.dormitoryId = null
     baseDeliveryFee.value = 0
     calculateFee()
  }
}

// 获取完整地址显示
const getFullAddress = (addr) => {
    let full = addr.addressContent || ''
    
    // 优先使用后端返回的宿舍名
    if (addr.dormitoryName) {
        full = addr.dormitoryName + ' ' + full
        return full
    }

    // 尝试在本地宿舍列表中查找
    if (addr.dormitoryId && dormitoryList.value.length > 0) {
        const item = dormitoryList.value.find(d => d.dormitoryId === addr.dormitoryId || d.id === addr.dormitoryId)
        if (item) {
            full = item.dormitoryName + ' ' + full
        }
    }
    return full
}

// 打开新增地址表单
const openAddAddress = () => {
  editingAddress.value = null
  formData.value = {
    username: '',
    phone: '',
    addressContent: '',
    dormitoryId: null,
    schoolId: form.value.schoolId
  }
  editingDormitoryIndex.value = 0
  showAddressForm.value = true
}

// 打开编辑地址表单
const editAddress = (addr) => {
  editingAddress.value = addr
  
  let dIndex = 0
  if (addr.dormitoryId && dormitoryList.value.length > 0) {
      dIndex = dormitoryList.value.findIndex(d => d.dormitoryId === addr.dormitoryId || d.id === addr.dormitoryId)
      if (dIndex < 0) dIndex = 0
  }
  editingDormitoryIndex.value = dIndex
  
  formData.value = {
    username: addr.username,
    phone: addr.phone,
    addressContent: addr.addressContent,
    dormitoryId: addr.dormitoryId,
    schoolId: addr.schoolId || form.value.schoolId
  }
  showAddressForm.value = true
}

// 处理编辑地址时的宿舍选择
const handleEditDormitoryChange = (e) => {
    const index = e.detail.value
    editingDormitoryIndex.value = index
    const item = dormitoryList.value[index]
    if (item) {
        formData.value.dormitoryId = item.dormitoryId || item.id
    }
}

// 关闭地址表单
const closeAddressForm = () => {
  showAddressForm.value = false
}

// 保存地址
const saveAddress = async () => {
  // 验证表单
  if (!formData.value.username || !formData.value.phone || !formData.value.addressContent) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (!formData.value.dormitoryId) {
    uni.showToast({ title: '请选择宿舍楼', icon: 'none' })
    return
  }
  // 验证手机号
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(formData.value.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '保存中' })
    
    const payload = { ...formData.value }
    if (editingAddress.value) {
      payload.id = editingAddress.value.id
    }

    const res = await request('/address/update', {
      method: 'POST',
      data: payload
    })

    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({ title: editingAddress.value ? '修改成功' : '添加成功', icon: 'success' })
      closeAddressForm()
      getAddressList()
    } else {
      uni.showToast({ title: res.msg || '保存失败', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
    console.error(e)
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

// 删除地址
const deleteAddress = (id) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除该地址吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中' })
          const res = await request('/address/delete', {
            method: 'POST',
            data: id 
          })
          uni.hideLoading()
          if (res.code === 200) {
            uni.showToast({ title: '删除成功', icon: 'success' })
            // 如果删除的是当前选中的地址，清空选中
            if (selectedAddress.value && selectedAddress.value.id === id) {
              selectedAddress.value = null
            }
            getAddressList()
          } else {
            uni.showToast({ title: res.msg || '删除失败', icon: 'none' })
          }
        } catch (e) {
          uni.hideLoading()
          console.error(e)
          uni.showToast({ title: '网络错误', icon: 'none' })
        }
      }
    }
  })
}

// 提交订单
const submitOrder = async () => {
  if (!form.value.startingAddress) return uni.showToast({ title: '请填写取货地址', icon: 'none' })
  if (!selectedAddress.value) return uni.showToast({ title: '请选择送达地址', icon: 'none' })
  if (!form.value.dormitoryId) return uni.showToast({ title: '请选择送达宿舍楼', icon: 'none' })
  if (!form.value.deliveryFee) return uni.showToast({ title: '请填写配送费', icon: 'none' })
  if (!form.value.waitTime) return uni.showToast({ title: '请填写等待时间', icon: 'none' })

  isSubmitting.value = true
  uni.showLoading({ title: '创建订单中' })

  try {
    const payload = {
      startingAddress: form.value.startingAddress,
      addressContent: selectedAddress.value.addressContent, // 从选中地址获取
      dormitoryId: form.value.dormitoryId,
      schoolId: form.value.schoolId,
      deliveryFee: parseFloat(form.value.deliveryFee),
      deliveryShopValue: form.value.deliveryShopValue ? parseFloat(form.value.deliveryShopValue) : 0,
      waitTime: parseInt(form.value.waitTime),
      note: form.value.note,
      takeoutNo: form.value.takeoutNo,
      isNick: form.value.isNick ? 1 : 0,
      isFloor: form.value.isFloor
    }

    // 处理预计到达时间
    if (form.value.takeoutDeliveryTime) {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      // 组合成 yyyy-MM-dd HH:mm:ss 格式
      payload.takeoutDeliveryTime = `${year}-${month}-${day} ${form.value.takeoutDeliveryTime}:00`
    } else if (form.value.isDelivered) {
        // 兜底：如果是已送达但没有时间，使用当前时间
        const now = new Date()
        const year = now.getFullYear()
        const month = String(now.getMonth() + 1).padStart(2, '0')
        const day = String(now.getDate()).padStart(2, '0')
        const hours = String(now.getHours()).padStart(2, '0')
        const minutes = String(now.getMinutes()).padStart(2, '0')
        const seconds = String(now.getSeconds()).padStart(2, '0')
        payload.takeoutDeliveryTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }

    const res = await request('/delivery/orders/create', {
      method: 'POST',
      data: payload
    })

    uni.hideLoading()

    if (res.code === 200) {
      // 获取支付参数并支付
      requestWechatPay(res.data)
    } else {
      uni.showToast({ title: res.msg || '下单失败', icon: 'none' })
    }

  } catch (e) {
    uni.hideLoading()
    console.error(e)
    uni.showToast({ title: '网络错误', icon: 'none' })
  } finally {
    isSubmitting.value = false
  }
}

// 调起微信支付
const requestWechatPay = (payParams) => {
    // 处理字段名兼容性
    const timeStamp = payParams.timeStamp || payParams.timestamp
    const packageVal = payParams.packageVal || payParams.package
    
    // 验证必要参数
    if (!timeStamp || !payParams.nonceStr || !packageVal || !payParams.signType || !payParams.paySign) {
        uni.showToast({ title: '支付参数异常', icon: 'none' })
        return
    }
    
    uni.requestPayment({
        provider: 'wxpay',
        timeStamp: String(timeStamp),
        nonceStr: String(payParams.nonceStr),
        package: String(packageVal),
        signType: String(payParams.signType),
        paySign: String(payParams.paySign),
        success: (res) => {
            uni.showToast({ title: '支付成功', icon: 'success' })
            setTimeout(() => {
                uni.navigateBack() // 返回上一页，或者跳转到订单列表
            }, 1500)
        },
        fail: (err) => {
            console.error('支付失败:', err)
            uni.showToast({ title: '支付已取消或失败', icon: 'none' })
        }
    })
}

</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* .nav-bar 样式已移至动态样式 */
.back-btn { width: 60rpx; height: 100%; display: flex; align-items: center; }
.nav-title { font-weight: bold; font-size: 32rpx; }
.placeholder { width: 60rpx; }

.content {
  flex: 1;
  padding: 20rpx;
}

.current-school {
  background-color: #e8f5e9;
  padding: 20rpx 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
}

.school-label {
  font-size: 28rpx;
  color: #666;
}

.school-val {
  font-size: 30rpx;
  font-weight: bold;
  color: #1abc9c;
  margin-left: 10rpx;
}

.section {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  border-left: 8rpx solid #1abc9c;
  padding-left: 16rpx;
}
.section-header .section-title { margin-bottom: 0; }

.form-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.form-item:last-child { border-bottom: none; }
.form-item.vertical { flex-direction: column; align-items: flex-start; }

.label {
  width: 180rpx;
  font-size: 28rpx;
  color: #333;
}

.input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}
.price-input { color: #ff5722; font-weight: bold; }

.textarea {
  width: 100%;
  height: 120rpx;
  margin-top: 16rpx;
  background-color: #f9f9f9;
  padding: 16rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
  box-sizing: border-box;
}

.select-address-btn {
  font-size: 24rpx;
  color: #1abc9c;
  display: flex;
  align-items: center;
}

.selected-address {
  background-color: #f9fff9;
  padding: 20rpx;
  border-radius: 8rpx;
  border: 1rpx solid #e0f2f1;
}

.no-address {
  background-color: #f5f5f5;
  padding: 30rpx;
  text-align: center;
  border-radius: 8rpx;
  color: #999;
  font-size: 28rpx;
}

.user-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}
.name { font-weight: bold; font-size: 28rpx; }
.phone { color: #666; font-size: 26rpx; }
.address-text { color: #333; font-size: 28rpx; line-height: 1.4; }
.address-row { margin-top: 10rpx; }

.picker-view {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.picker-text { font-size: 28rpx; color: #333; }
.placeholder-text { color: #888; }
.picker-arrow { color: #999; }
.picker-item { width: 100%; }
.picker-item .label-box {
  width: 180rpx;
  display: flex;
  align-items: center;
}
.field-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 8rpx;
}
.label-text {
  font-size: 28rpx;
  color: #333;
}
.required-mark {
  color: #ff5722;
  margin-left: 4rpx;
  font-weight: bold;
}
picker { flex: 1; }

.spacer { height: 120rpx; }

.bottom-bar {
  background-color: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  box-sizing: border-box;
  z-index: 99;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.total-price {
  font-size: 28rpx;
  color: #333;
}
.price {
  font-size: 36rpx;
  color: #ff5722;
  font-weight: bold;
}

.submit-btn {
  margin: 0;
  background: linear-gradient(90deg, #1abc9c, #16a085);
  color: #fff;
  border-radius: 40rpx;
  padding: 0 60rpx;
  font-size: 30rpx;
  height: 80rpx;
  line-height: 80rpx;
}
.submit-btn:disabled { opacity: 0.6; }

.warning-tip {
  margin-top: 20rpx;
  padding: 16rpx;
  background-color: #fffbe6;
  border: 1rpx solid #ffe58f;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #fa8c16;
  line-height: 1.4;
}

/* 更多选项开关 */
.more-options-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
  font-size: 26rpx;
  color: #1abc9c;
}
.more-options-toggle .arrow {
  margin-left: 8rpx;
  font-size: 24rpx;
  transition: transform 0.3s;
}
.more-options-toggle .arrow.up {
  transform: rotate(180deg);
}
.more-info-area {
  border-top: 1rpx solid #f5f5f5;
}

/* 弹窗样式 */
.popup-mask {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0,0,0,0.5); z-index: 100;
  display: flex; justify-content: center; align-items: center;
}
.popup-content {
  width: 650rpx; max-height: 70%;
  background-color: #fff;
  border-radius: 20rpx;
  display: flex; flex-direction: column;
  overflow: hidden;
}
.popup-header {
  padding: 30rpx; border-bottom: 1rpx solid #eee;
  display: flex; justify-content: space-between; align-items: center;
}
.popup-title { font-weight: bold; font-size: 30rpx; }
.close-icon { font-size: 40rpx; color: #999; padding: 10rpx; }
.address-scroll { max-height: 600rpx; }
.address-item {
  padding: 30rpx; border-bottom: 1rpx solid #f5f5f5;
  display: flex; justify-content: space-between; align-items: center;
}
.address-item:active { background-color: #f9f9f9; }
.addr-info { flex: 1; }
.addr-user { margin-bottom: 8rpx; }
.addr-name { font-weight: bold; margin-right: 20rpx; }
.addr-phone { color: #666; font-size: 24rpx; }
.addr-detail { color: #333; font-size: 26rpx; line-height: 1.4; }
.addr-check { color: #1abc9c; font-weight: bold; font-size: 32rpx; margin-left: 20rpx; }
.empty-address { padding: 40rpx; text-align: center; color: #999; font-size: 26rpx; }

/* 地址管理按钮样式 */
.popup-actions {
  display: flex;
  align-items: center;
}
.manage-btn {
  font-size: 26rpx;
  color: #1abc9c;
  margin-right: 20rpx;
  padding: 6rpx 16rpx;
  background-color: rgba(26, 188, 156, 0.1);
  border-radius: 20rpx;
}
.addr-actions {
  display: flex;
  margin-right: 10rpx;
}
.action-btn {
  padding: 10rpx;
  font-size: 28rpx;
  margin-left: 10rpx;
  display: flex;
  align-items: center;
}
.action-icon {
  width: 32rpx;
  height: 32rpx;
}

/* 表单样式 */
.form-container {
  padding: 30rpx;
}
.form-group {
  margin-bottom: 24rpx;
}
.form-label {
  display: block;
  font-size: 26rpx;
  color: #333;
  margin-bottom: 12rpx;
}
.form-input,
.form-textarea {
  width: 100%;
  padding: 0 20rpx;
  height: 80rpx;
  line-height: 80rpx;
  border: 1rpx solid #e8e8e8;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}
.form-textarea {
  height: 160rpx;
  line-height: 1.4;
  padding: 20rpx;
}
.form-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30rpx;
}
.form-btn {
  width: 45%;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  margin: 0;
}
.form-btn.cancel {
  background-color: #f5f5f5;
  color: #666;
}
.form-btn.submit {
  background: linear-gradient(90deg, #1abc9c, #16a085);
  color: #fff;
}
</style>
