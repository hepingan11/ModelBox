<template>
  <view class="my-code-page">
    <view class="tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'buy' }"
        @tap="switchTab('buy')"
      >已购买</view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'publish' }"
        @tap="switchTab('publish')"
      >我发布的</view>
    </view>
    
    <scroll-view scroll-y class="list-container">
      <view v-if="loading" class="loading">加载中...</view>
      <view v-else-if="list.length === 0" class="empty">暂无数据</view>
      
      <view 
        v-for="item in list" 
        :key="item.codeId" 
        class="code-item"
        @tap="goToDetail(item)"
      >
        <image 
          :src="getAssetUrl(item.imageList && item.imageList[0])" 
          class="thumb" 
          mode="aspectFill"
        ></image>
        <view class="content">
          <text class="title">{{ item.codeName }}</text>
          <text class="desc">{{ item.introduce }}</text>
          <view class="status-bar">
            <text class="price">{{ item.price > 0 ? '¥'+item.price : '免费' }}</text>
            <view v-if="activeTab === 'publish'" class="actions">
                <text class="btn delete" @tap.stop="onDelete(item)">删除</text>
                <text class="btn edit" @tap.stop="onEdit(item)">编辑</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getMyCodeList, getMyPublishList, deleteCode } from '@/api/code.js';

const activeTab = ref('buy');
const list = ref([]);
const loading = ref(false);

const imageBaseUrl = import.meta.env.VUE_APP_IMAGE || '';

const getAssetUrl = (url) => {
  if (!url) return '/static/logo.png';
  if (url.startsWith('http')) return url;
  return imageBaseUrl + url;
};

const loadData = async () => {
  loading.value = true;
  list.value = [];
  try {
    let res;
    if (activeTab.value === 'buy') {
      res = await getMyCodeList();
    } else {
      res = await getMyPublishList();
    }
    if (Array.isArray(res)) list.value = res;
    else if (res && res.data) list.value = res.data;
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const switchTab = (tab) => {
  activeTab.value = tab;
  loadData();
};

const goToDetail = (item) => {
  // If editing, handle differently? No, detail is for viewing.
  // Editing is handled by edit button.
  uni.navigateTo({
    url: `/pages/code/detail?item=${encodeURIComponent(JSON.stringify(item))}`
  });
};

const onDelete = (item) => {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，且已被购买的项目无法删除',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteCode(item.codeId);
          uni.showToast({ title: '已删除' });
          loadData();
        } catch (e) {
          uni.showToast({ title: e.msg || '删除失败', icon: 'none' });
        }
      }
    }
  });
};

const onEdit = (item) => {
    // Navigate to publish page with edit mode
    // Not implemented in publish page yet, but let's reserve
    uni.showToast({ title: '暂不支持编辑', icon: 'none' });
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss">
.my-code-page {
  height: 100vh;
  background-color: #1a1a1a;
  color: #fff;
  display: flex;
  flex-direction: column;
}
.tabs {
  display: flex;
  background: #333;
  padding: 10px;
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 10px;
    color: #888;
    &.active {
      color: #fff;
      font-weight: bold;
      border-bottom: 2px solid #007aff;
    }
  }
}
.list-container {
  flex: 1;
  padding: 10px;
  box-sizing: border-box;
}
.code-item {
  display: flex;
  background: #252525;
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 8px;
  
  .thumb {
    width: 80px;
    height: 80px;
    border-radius: 4px;
    margin-right: 10px;
    background: #444;
  }
  .content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    .title { font-size: 16px; font-weight: bold; }
    .desc { font-size: 12px; color: #888; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .status-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      .price { color: #ff9800; }
      .actions {
        .btn {
          margin-left: 10px;
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 4px;
          &.delete { background: #ff3b30; }
          &.edit { background: #007aff; }
        }
      }
    }
  }
}
.loading, .empty { text-align: center; padding: 20px; color: #666; }
</style>
