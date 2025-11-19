<template>
  <view class="shop-list-container">
    <!-- 顶部标题栏 -->
    <view class="header-section">
      <view class="header-left">
        <text class="header-title">商品管理</text>
        <text class="shop-count">共 {{ totalProducts }} 个商品</text>
      </view>
      <view class="header-right">
        <view class="search-box">
          <image src="/static/icon/search.png" class="search-icon"></image>
          <input 
            type="text" 
            v-model="searchKeyword" 
            @confirm="handleSearch" 
            placeholder="搜索商品名称" 
            class="search-input"
          />
          <view class="clear-icon" v-if="searchKeyword" @click="clearSearch">×</view>
        </view>
        <view class="filter-btn" @click="showFilterOptions = true">
          <image src="/static/icon/filter.png" class="filter-icon"></image>
        </view>
      </view>
    </view>
    
    <!-- 分类选择栏 -->
    <scroll-view class="category-scroll" scroll-x show-scrollbar="false">
      <view 
        class="category-item" 
        :class="{ active: currentCategory === '' }"
        @click="selectCategory('')"
      >
        全部
      </view>
      <view 
        class="category-item" 
        :class="{ active: currentCategory === '学习用品' }"
        @click="selectCategory('学习用品')"
      >
        学习用品
      </view>
      <view 
        class="category-item" 
        :class="{ active: currentCategory === '电子产品' }"
        @click="selectCategory('电子产品')"
      >
        电子产品
      </view>
      <view 
        class="category-item" 
        :class="{ active: currentCategory === '衣物鞋包' }"
        @click="selectCategory('衣物鞋包')"
      >
        衣物鞋包
      </view>
      <view 
        class="category-item" 
        :class="{ active: currentCategory === '生活用品' }"
        @click="selectCategory('生活用品')"
      >
        生活用品
      </view>
      <view 
        class="category-item" 
        :class="{ active: currentCategory === '运动娱乐' }"
        @click="selectCategory('运动娱乐')"
      >
        运动娱乐
      </view>
      <view 
        class="category-item" 
        :class="{ active: currentCategory === '虚拟产品' }"
        @click="selectCategory('虚拟产品')"
      >
        虚拟产品
      </view>
      <view 
        class="category-item" 
        :class="{ active: currentCategory === '其它' }"
        @click="selectCategory('其它')"
      >
        其它
      </view>
    </scroll-view>
    
    <!-- 商品列表 -->
    <scroll-view 
      scroll-y 
      class="shop-scroll-view" 
      @scrolltolower="loadMoreProducts"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="refreshProductList"
    >
      <!-- 商品列表项 -->
      <view v-if="productList.length > 0">
        <view 
          class="product-item"
          v-for="(product, index) in productList" 
          :key="product.id"
        >
          <view class="product-image-section" @click="showProductDetail(product)">
            <image 
              :src="product.imageUrl && product.imageUrl.length > 0 ? product.imageUrl[0] : '/static/default-goods.png'" 
              class="product-image" 
              mode="aspectFill"
            ></image>
            <view v-if="product.status === 0" class="product-status pending">待审核</view>
            <view v-else-if="product.status === 2" class="product-status sold-out">未通过</view>
          </view>
          
          <view class="product-info-section" @click="showProductDetail(product)">
            <text class="product-name">{{ product.shopName }}</text>
            <text class="product-intro" v-if="product.introduce">{{ product.introduce }}</text>
            
            <view class="product-details">
              <view class="price-section">
                <text class="product-price">¥{{ product.price }}</text>
                <text class="product-category">{{ product.category || '未分类' }}</text>
              </view>
              <view class="time-section">
                <text class="publish-time">发布: {{ formatDate(product.createdTime) }}</text>
                <text class="seller-info">卖家: {{ product.username || 'ID:' + product.userId }}</text>
              </view>
              
              <!-- 拒绝原因 -->
              <view v-if="product.status === 2 && product.reason" class="reject-reason">
                <text class="reason-label">拒绝原因:</text>
                <text class="reason-content">{{ product.reason }}</text>
              </view>
            </view>
          </view>
          
          <view class="product-action-section">
            <view class="action-buttons">
              <button 
                class="status-btn" 
                :class="{
                  'approve-btn': product.status === 0,
                  'shelf-btn': product.status === 1,
                  'unshelf-btn': product.status === 2
                }"
                @click="toggleProductStatus(product)"
              >
                {{ getStatusBtnText(product.status) }}
              </button>
              <button class="delete-btn" @click="confirmDeleteProduct(product)">删除</button>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-else-if="!isLoading" class="empty-state">
        <image src="/static/icon/empty.png" class="empty-icon"></image>
        <text class="empty-text">暂无商品数据</text>
      </view>
      
      <!-- 底部加载状态 -->
      <view class="loading-more" v-if="isLoading && !isRefreshing">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 已加载全部 -->
      <view class="list-end" v-if="!hasMore && productList.length > 0">
        <text class="end-text">- 没有更多商品了 -</text>
      </view>
    </scroll-view>
    
    <!-- 筛选弹窗 -->
    <view class="filter-popup" v-if="showFilterOptions">
      <view class="filter-content">
        <view class="filter-header">
          <text class="filter-title">筛选条件</text>
          <text class="close-icon" @click="showFilterOptions = false">×</text>
        </view>
        
        <view class="filter-options">
          <view class="filter-option">
            <text class="option-label">商品状态</text>
            <view class="option-values">
              <view 
                class="option-value" 
                :class="{'selected': filterStatus === ''}"
                @click="filterStatus = ''"
              >全部</view>
              <view 
                class="option-value" 
                :class="{'selected': filterStatus === '1'}"
                @click="filterStatus = '1'"
              >已上架</view>
              <view 
                class="option-value" 
                :class="{'selected': filterStatus === '0'}"
                @click="filterStatus = '0'"
              >已下架</view>
              <view 
                class="option-value" 
                :class="{'selected': filterStatus === '2'}"
                @click="filterStatus = '2'"
              >待审核</view>
            </view>
          </view>
          
          <view class="filter-option">
            <text class="option-label">价格区间</text>
            <view class="price-range">
              <input 
                type="digit" 
                v-model="minPrice" 
                placeholder="最低价" 
                class="price-input"
              />
              <text class="price-separator">-</text>
              <input 
                type="digit" 
                v-model="maxPrice" 
                placeholder="最高价" 
                class="price-input"
              />
            </view>
          </view>
        </view>
        
        <view class="filter-buttons">
          <button class="reset-btn" @click="resetFilters">重置</button>
          <button class="apply-btn" @click="applyFilters">应用</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request.js'

// 商品列表数据
const productList = ref([])
const totalProducts = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)
const isLoading = ref(false)
const isRefreshing = ref(false)

// 搜索和筛选
const searchKeyword = ref('')
const showFilterOptions = ref(false)
const currentCategory = ref('')
const filterStatus = ref('')
const minPrice = ref('')
const maxPrice = ref('')

// 获取商品列表
const getProductList = async (isRefresh = false) => {
  if (isLoading.value) return
  isLoading.value = true
  
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      shopName: searchKeyword.value,
      category: currentCategory.value
    }
    
    // 添加筛选条件
    if (filterStatus.value) params.status = filterStatus.value
    if (minPrice.value) params.minPrice = minPrice.value
    if (maxPrice.value) params.maxPrice = maxPrice.value
    
    const res = await request('/admin/shop/page', {
      method: 'GET',
      data: params
    })
    
    if (res.code === 200) {
      const total = res.data.length;
      const records = res.data;
      
      // 商品列表中已包含拒绝原因，不需要单独获取
      
      if (isRefresh) {
        productList.value = records || []
      } else {
        productList.value = [...productList.value, ...(records || [])]
      }
      
      totalProducts.value = total
      hasMore.value = productList.value.length < total
      
      // 如果有数据，页码加1，为下次加载做准备
      if ((records || []).length > 0 && !isRefresh) {
        currentPage.value++
      }
    } else {
      uni.showToast({
        title: res.msg || '获取商品列表失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
    if (isRefreshing.value) {
      isRefreshing.value = false
    }
  }
}

// 商品列表中已包含拒绝原因，不需要单独获取

// 下拉刷新
const refreshProductList = () => {
  isRefreshing.value = true
  currentPage.value = 1
  getProductList(true)
}

// 加载更多
const loadMoreProducts = () => {
  if (!hasMore.value || isLoading.value) return
  getProductList()
}

// 搜索商品
const handleSearch = () => {
  currentPage.value = 1
  getProductList(true)
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  handleSearch()
}

// 重置筛选条件
const resetFilters = () => {
  filterStatus.value = ''
  minPrice.value = ''
  maxPrice.value = ''
}

// 应用筛选条件
const applyFilters = () => {
  showFilterOptions.value = false
  currentPage.value = 1
  getProductList(true)
}

// 选择分类
const selectCategory = (category) => {
  if (currentCategory.value === category) return;
  
  currentCategory.value = category
  currentPage.value = 1
  getProductList(true)
}

// 切换商品状态
const toggleProductStatus = (product) => {
  // 根据当前状态显示不同的操作选项
  if (product.status === 0) {
    // 待审核商品，显示通过/拒绝选项
    uni.showModal({
      title: '商品审核',
      content: `请选择对商品"${product.shopName}"的审核结果`,
      showCancel: true,
      cancelText: '拒绝',
      confirmText: '通过',
      success: async (res) => {
        if (res.confirm) {
          // 审核通过
          await updateProductStatus(product, 1, '审核通过');
        } else if (res.cancel) {
          // 审核拒绝，弹出输入拒绝原因的弹窗
          showRejectReasonInput(product);
        }
      }
    });
  } else if (product.status === 1) {
    // 已通过商品，可以设置为拒绝
    uni.showModal({
      title: '修改状态',
      content: `确定要将商品"${product.shopName}"设为拒绝状态吗？`,
      success: async (res) => {
        if (res.confirm) {
          showRejectReasonInput(product);
        }
      }
    });
  } else if (product.status === 2) {
    // 已拒绝商品，可以设置为通过
    uni.showModal({
      title: '修改状态',
      content: `确定要将商品"${product.shopName}"设为通过状态吗？`,
      success: async (res) => {
        if (res.confirm) {
          await updateProductStatus(product, 1, '状态修改');
  } 
      }
    });
  }
}

// 显示拒绝原因输入弹窗
const showRejectReasonInput = (product) => {
  uni.showModal({
    title: '拒绝原因',
    content: '',
    editable: true,
    placeholderText: '请输入拒绝原因',
    success: async (res) => {
      if (res.confirm) {
        const reason = res.content.trim();
        
        if (!reason) {
          uni.showToast({
            title: '请输入拒绝原因',
            icon: 'none'
          });
          return;
        }
        
        await rejectProduct(product, reason);
      }
    }
  });
}

// 拒绝商品并提交原因
const rejectProduct = async (product, reason) => {
  try {
    // 先更新商品状态为拒绝
    const statusResult = await request('/admin/shop/status', {
      method: 'GET',
            data: {
              id: product.id,
        status: 2 // 拒绝状态
            }
    });
          
    if (statusResult.code !== 200) {
            uni.showToast({
        title: statusResult.msg || '设置拒绝状态失败',
        icon: 'none'
      });
      return;
    }
    
    // 提交拒绝原因
    const reasonResult = await request('/admin/shop/reason', {
      method: 'GET',
      data: {
        id: product.id,
        reason: reason
      }
    });
    
    if (reasonResult.code === 200) {
      uni.showToast({
        title: product.status === 0 ? '审核拒绝成功' : '状态已修改为拒绝',
              icon: 'success'
      });
            
            // 更新本地状态
      const index = productList.value.findIndex(p => p.id === product.id);
            if (index !== -1) {
        productList.value[index].status = 2;
        productList.value[index].reason = reason;
            }
          } else {
            uni.showToast({
        title: reasonResult.msg || '提交拒绝原因失败',
              icon: 'none'
      });
          }
        } catch (error) {
    console.error('设置商品拒绝状态失败:', error);
          uni.showToast({
            title: '网络异常，请稍后重试',
            icon: 'none'
    });
        }
}

// 确认删除商品
const confirmDeleteProduct = (product) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除商品"${product.shopName}"吗？删除后无法恢复`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await request('/admin/shop/delete', {
            method: 'POST',
            data: {
              id: product.id
            }
          })
          
          if (result.code === 200) {
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })
            
            // 从列表中移除
            const index = productList.value.findIndex(p => p.id === product.id)
            if (index !== -1) {
              productList.value.splice(index, 1)
              totalProducts.value--
            }
          } else {
            uni.showToast({
              title: result.msg || '删除失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('删除商品失败:', error)
          uni.showToast({
            title: '网络异常，请稍后重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 查看商品详情
const showProductDetail = (product) => {
  uni.navigateTo({
    url: `/pages/shop/detail?id=${product.id}`
  })
}

// 更新商品状态
const updateProductStatus = async (product, newStatus, actionText) => {
  try {
    const result = await request('/admin/shop/status', {
      method: 'GET',
      data: {
        id: product.id,
        status: newStatus
      }
    });
    
    if (result.code === 200) {
      uni.showToast({
        title: `${actionText}成功`,
        icon: 'success'
      });
      
      // 更新本地状态
      const index = productList.value.findIndex(p => p.id === product.id);
      if (index !== -1) {
        productList.value[index].status = newStatus;
        // 如果是设为通过状态，清除拒绝原因
        if (newStatus === 1) {
          productList.value[index].reason = '';
        }
      }
    } else {
      uni.showToast({
        title: result.msg || `${actionText}失败`,
        icon: 'none'
      });
    }
  } catch (error) {
    console.error(`${actionText}商品失败:`, error);
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    });
  }
}

// 获取状态按钮文本
const getStatusBtnText = (status) => {
  switch (status) {
    case 0: return '审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '操作'
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  getProductList(true)
})
</script>

<style lang="scss" scoped>
.shop-list-container {
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
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.shop-count {
  font-size: 24rpx;
  color: #999;
}

.header-right {
  display: flex;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: #f0f2f5;
  border-radius: 30rpx;
  padding: 10rpx 20rpx;
  margin-right: 20rpx;
}

.search-icon {
  width: 36rpx;
  height: 36rpx;
  margin-right: 10rpx;
}

.search-input {
  width: 240rpx;
  font-size: 26rpx;
}

.clear-icon {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  color: #999;
}

.filter-btn {
  width: 70rpx;
  height: 70rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
  border-radius: 50%;
}

.filter-icon {
  width: 40rpx;
  height: 40rpx;
}

/* 分类选择栏 */
.category-scroll {
  white-space: nowrap;
  background-color: #fff;
  padding: 20rpx 0;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.category-item {
  display: inline-block;
  padding: 12rpx 30rpx;
  margin: 0 10rpx;
  font-size: 28rpx;
  color: #666;
  background-color: #f5f7fa;
  border-radius: 20rpx;
  transition: all 0.3s;
}

.category-item:first-child {
  margin-left: 20rpx;
}

.category-item:last-child {
  margin-right: 20rpx;
}

.category-item.active {
  background-color: #1abc9c;
  color: #fff;
  font-weight: 500;
}

/* 商品列表 */
.shop-scroll-view {
  flex: 1;
  padding: 20rpx;
}

.product-item {
  display: flex;
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.product-image-section {
  position: relative;
  width: 200rpx;
  height: 200rpx;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-status {
  position: absolute;
  top: 0;
  left: 0;
  padding: 6rpx 12rpx;
  font-size: 22rpx;
  color: white;
}

.sold-out {
  background-color: rgba(153, 153, 153, 0.8);
}

.pending {
  background-color: rgba(255, 152, 0, 0.8);
}

.product-info-section {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.product-intro {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-details {
  display: flex;
  flex-direction: column;
}

.price-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.product-price {
  font-size: 32rpx;
  color: #358e53;
  font-weight: bold;
}

.product-category {
  font-size: 24rpx;
  color: #999;
  background-color: #f5f7fa;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.time-section {
  display: flex;
  justify-content: space-between;
  font-size: 22rpx;
  color: #999;
}

.product-action-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 20rpx;
  min-width: 130rpx;
}

.action-buttons {
  display: flex;
  flex-direction: column;
}

.status-btn, .delete-btn {
  font-size: 24rpx;
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
  margin-bottom: 15rpx;
  text-align: center;
  background-color: #fff;
}

.approve-btn {
  color: #1890ff;
  border: 1px solid #1890ff;
}

.shelf-btn {
  color: #52c41a;
  border: 1px solid #52c41a;
}

.unshelf-btn {
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
}

.delete-btn {
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
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
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 加载状态 */
.loading-more, .list-end {
  text-align: center;
  padding: 20rpx 0;
}

.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10rpx;
  display: inline-block;
  vertical-align: middle;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text, .end-text {
  font-size: 24rpx;
  color: #999;
  display: inline-block;
  vertical-align: middle;
}

/* 筛选弹窗 */
.filter-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.filter-content {
  width: 600rpx;
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f0f0f0;
}

.filter-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.close-icon {
  font-size: 40rpx;
  color: #999;
}

.filter-options {
  padding: 20rpx 30rpx;
}

.filter-option {
  margin-bottom: 30rpx;
}

.option-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.option-values {
  display: flex;
  flex-wrap: wrap;
}

.option-value {
  padding: 12rpx 30rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #666;
  background-color: #f5f5f5;
  margin-right: 20rpx;
  margin-bottom: 20rpx;
}

.option-value.selected {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.price-range {
  display: flex;
  align-items: center;
}

.price-input {
  flex: 1;
  height: 70rpx;
  background-color: #f5f5f5;
  border-radius: 10rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
}

.price-separator {
  margin: 0 20rpx;
  color: #999;
}

.filter-buttons {
  display: flex;
  padding: 20rpx 30rpx;
  border-top: 1px solid #f0f0f0;
}

.reset-btn, .apply-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 28rpx;
  border-radius: 40rpx;
}

.reset-btn {
  background-color: #f5f5f5;
  color: #666;
  margin-right: 20rpx;
}

.apply-btn {
  background-color: #1abc9c;
  color: #fff;
}

/* 状态标签样式 */
.status-tag {
  padding: 6rpx 20rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
  text-align: center;
  margin-bottom: 15rpx;
}

.status-approved {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-rejected {
  background-color: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

/* 拒绝原因样式 */
.reject-reason {
  margin-top: 10rpx;
  padding: 10rpx;
  background-color: #fff2f0;
  border-radius: 8rpx;
  font-size: 24rpx;
  display: flex;
}

.reason-label {
  color: #ff4d4f;
  font-weight: 500;
  margin-right: 10rpx;
}

.reason-content {
  color: #666;
  flex: 1;
}
</style>