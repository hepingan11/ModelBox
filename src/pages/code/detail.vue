<template>
  <view class="detail-page">
    <swiper class="swiper" indicator-dots autoplay>
      <swiper-item v-for="(img, idx) in imageList" :key="idx">
        <image :src="getAssetUrl(img)" mode="aspectFill" class="slide-image" @tap="previewImage(idx)"></image>
      </swiper-item>
    </swiper>
    
    <view class="info-section">
      <view class="header">
        <text class="title">{{ item.codeName }}</text>
        <text class="price">{{ item.price > 0 ? '¥' + item.price : '免费' }}</text>
      </view>
      
      <view class="meta">
        <text class="tag">{{ item.language }}</text>
        <text class="views">{{ item.look || 0 }}次查看</text>
      </view>
      
      <view class="intro-box">
        <text class="section-title">项目介绍</text>
        <text class="intro-text">{{ item.introduce }}</text>
      </view>
      
      <view class="download-box" v-if="downloadUrl">
        <text class="section-title">下载方式</text>
        <view class="link-row">
          <text class="link-text">{{ downloadUrl }}</text>
          <view class="copy-btn" @tap="copyLink">复制</view>
        </view>
        <view class="code-row" v-if="item.extractCode">
          <text>提取码: {{ item.extractCode }}</text>
          <view class="copy-btn small" @tap="copyCode">复制</view>
        </view>
      </view>
    </view>
    
    <view class="footer-bar">
      <view v-if="downloadUrl" class="action-btn download" @tap="copyLink">复制下载链接</view>
      <view v-else class="action-btn buy" @tap="handleBuy">
        {{ item.price > 0 ? '立即购买' : '免费获取' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app'; // UniApp hook
import { addLook, buyCode } from '@/api/code.js';

const item = ref({});
const imageList = computed(() => item.value.imageList || []);
const imageBaseUrl = import.meta.env.VUE_APP_IMAGE || '';

const getAssetUrl = (url) => {
  if (!url) return '/static/logo.png';
  if (url.startsWith('http')) return url;
  return imageBaseUrl + url;
};

const downloadUrl = computed(() => item.value.downloadUrl);

onLoad((options) => {
  if (options.item) {
    try {
      item.value = JSON.parse(decodeURIComponent(options.item));
      // Add look
      addLook(item.value.codeId);
    } catch (e) {
      console.error(e);
    }
  }
});

const previewImage = (current) => {
  const urls = imageList.value.map(url => getAssetUrl(url));
  uni.previewImage({
    urls,
    current: urls[current]
  });
};

const copyLink = () => {
  if (!downloadUrl.value) return;
  uni.setClipboardData({
    data: downloadUrl.value,
    success: () => uni.showToast({ title: '链接已复制' })
  });
};

const copyCode = () => {
  if (!item.value.extractCode) return;
  uni.setClipboardData({
    data: item.value.extractCode,
    success: () => uni.showToast({ title: '提取码已复制' })
  });
};

const handleBuy = async () => {
    if (item.value.price <= 0) {
        // Free item logic?
        // Web: if price 0, directly open downloadUrl logic?
        // But if we are in market list, we might not have downloadUrl yet unless we "buy" (process order)?
        // Or maybe free items expose URL directly?
        // Let's assume free items expose URL if not, we need to call an API to "get" it.
        // Web: if (price > 0) buy... else if (downloadUrl) open... else error.
        if (item.value.downloadUrl) {
            copyLink();
        } else {
             // Maybe free items need to be "bought" for 0 price to generate record?
             // Web doesn't seem to have "buy for 0" API explicitly, 
             // it checks if downloadUrl exists.
             // If market list doesn't return downloadUrl for free items, we have a problem.
             // Let's assume user needs to go to "My Code" if they bought it, OR free items just show url.
             uni.showToast({ title: '请尝试在"我的-已购买"中查看', icon: 'none' });
        }
        return;
    }
    
    uni.showLoading({ title: '创建订单...' });
    try {
        const res = await buyCode(item.value.codeId);
        // res should contain payUrl
        if (res && res.payUrl) {
             uni.hideLoading();
             // Open Alipay
             // #ifdef APP-PLUS
             plus.runtime.openURL(res.payUrl);
             // #endif
             // #ifdef H5
             window.location.href = res.payUrl;
             // #endif
             
             uni.showModal({
                 title: '支付提示',
                 content: '支付完成后，请在"我的-已购买"中查看源码',
                 showCancel: false,
                 success: () => {
                     // Maybe navigate to My Code
                     uni.navigateTo({ url: '/pages/code/my' });
                 }
             });
        } else {
            uni.showToast({ title: '获取支付链接失败', icon: 'none' });
        }
    } catch (e) {
        uni.hideLoading();
        uni.showToast({ title: '支付发起失败', icon: 'none' });
    }
};
</script>

<style lang="scss">
.detail-page {
  padding-bottom: 80px;
  background-color: #1a1a1a;
  min-height: 100vh;
  color: #fff;
}
.swiper {
  width: 100%;
  height: 250px;
  .slide-image { width: 100%; height: 100%; }
}
.info-section {
  padding: 15px;
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    .title { font-size: 20px; font-weight: bold; flex: 1; }
    .price { font-size: 18px; color: #ff9800; font-weight: bold; }
  }
  .meta {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    .tag { background: #333; padding: 2px 8px; border-radius: 4px; font-size: 12px; color: #ccc; }
    .views { font-size: 12px; color: #666; }
  }
  .intro-box {
    background: #252525;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
    .section-title { display: block; font-weight: bold; margin-bottom: 10px; border-left: 3px solid #007aff; padding-left: 8px; }
    .intro-text { font-size: 14px; line-height: 1.6; color: #ddd; }
  }
  .download-box {
    background: #252525;
    padding: 15px;
    border-radius: 8px;
    .section-title { display: block; font-weight: bold; margin-bottom: 10px; }
    .link-row, .code-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: #333;
      padding: 10px;
      border-radius: 4px;
      margin-bottom: 10px;
      .link-text { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-right: 10px; font-size: 14px; }
      .copy-btn { color: #007aff; font-size: 14px; }
    }
  }
}
.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #252525;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  border-top: 1px solid #333;
  .action-btn {
    width: 100%;
    height: 40px;
    line-height: 40px;
    text-align: center;
    border-radius: 20px;
    font-weight: bold;
    &.buy { background: #007aff; color: #fff; }
    &.download { background: #4caf50; color: #fff; }
  }
}
</style>
