<template>
  <view class="code-market">
    <!-- 头部搜索 -->
    <view class="header-section">
      <view class="search-box">
        <input 
          class="search-input" 
          v-model="searchKeyword" 
          placeholder="搜索项目名称" 
          confirm-type="search"
          @confirm="onSearch"
        />
        <view class="search-btn" @tap="onSearch">搜索</view>
      </view>
      
      <!-- 筛选 -->
      <scroll-view scroll-x class="filter-scroll">
        <view 
          class="filter-item" 
          :class="{ active: !selectedLanguage }" 
          @tap="onFilter('')"
        >全部</view>
        <view 
          v-for="lang in languages" 
          :key="lang" 
          class="filter-item" 
          :class="{ active: selectedLanguage === lang }"
          @tap="onFilter(lang)"
        >
          {{ lang }}
        </view>
      </scroll-view>
    </view>
    
    <!-- 列表 -->
    <scroll-view scroll-y class="code-list" @scrolltolower="loadMore">
      <view 
        v-for="item in codeList" 
        :key="item.codeId" 
        class="code-card"
        @tap="goToDetail(item)"
      >
        <image 
          :src="getAssetUrl(item.imageList && item.imageList[0])" 
          class="code-cover" 
          mode="aspectFill"
        ></image>
        <view class="code-info">
          <text class="code-title">{{ item.codeName }}</text>
          <view class="code-tags">
            <text class="lang-tag">{{ item.language }}</text>
            <text class="price-tag">{{ item.price > 0 ? '¥' + item.price : '免费' }}</text>
          </view>
          <text class="code-intro">{{ item.introduce }}</text>
          <view class="code-meta">
            <text class="views">{{ item.look || 0 }}次查看</text>
          </view>
        </view>
      </view>
      
      <!-- 加载状态 -->
      <view v-if="loading" class="loading-more">加载中...</view>
      <view v-if="!loading && codeList.length === 0" class="empty-state">
        <text>暂无数据</text>
      </view>
    </scroll-view>
    
    <!-- 悬浮按钮 -->
    <view class="fab-group">
      <view class="fab-btn my-btn" @tap="goToMy">
        <text>我的</text>
      </view>
      <view class="fab-btn publish-btn" @tap="goToPublish">
        <text>发布</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getCodeList } from '@/api/code.js';

const searchKeyword = ref('');
const selectedLanguage = ref('');
const codeList = ref([]);
const loading = ref(false);
const page = ref(1);
const total = ref(0);

const languages = ['JavaScript', 'Python', 'Java', 'C', 'Go', 'PHP', 'Vue', 'React', 'Other'];
const imageBaseUrl = import.meta.env.VUE_APP_IMAGE || '';

const getAssetUrl = (url) => {
  if (!url) return '/static/logo.png'; // Placeholder
  if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) return url;
  return imageBaseUrl + url;
};

const fetchData = async () => {
    loading.value = true;
    try {
        const res = await getCodeList(searchKeyword.value, selectedLanguage.value);
        // Assuming res is array or { list, total }
        if (Array.isArray(res)) {
            if (page.value === 1) codeList.value = res;
            else codeList.value = [...codeList.value, ...res];
        } else if (res && res.data) {
             if (page.value === 1) codeList.value = res.data;
             else codeList.value = [...codeList.value, ...res.data];
        }
    } catch (e) {
        console.error(e);
        uni.showToast({ title: '加载失败', icon: 'none' });
    } finally {
        loading.value = false;
    }
};

const onSearch = () => {
    page.value = 1;
    fetchData();
};

const onFilter = (lang) => {
    selectedLanguage.value = lang;
    page.value = 1;
    fetchData();
};

const loadMore = () => {
    // If pagination supported
    // page.value++;
    // fetchData();
};

const goToDetail = (item) => {
    uni.navigateTo({
        url: `/pages/code/detail?item=${encodeURIComponent(JSON.stringify(item))}`
    });
};

const goToMy = () => {
    uni.navigateTo({ url: '/pages/code/my' });
};

const goToPublish = () => {
    uni.navigateTo({ url: '/pages/code/publish' });
};

onMounted(() => {
    fetchData();
});
</script>

<style lang="scss">
.code-market {
  min-height: 100vh;
  background-color: #1a1a1a;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.header-section {
  padding: 10px;
  background-color: #252525;
  
  .search-box {
    display: flex;
    background: #333;
    border-radius: 20px;
    padding: 5px 15px;
    align-items: center;
    
    .search-input {
      flex: 1;
      height: 36px;
      color: #fff;
    }
    
    .search-btn {
      color: #aaa;
      margin-left: 10px;
    }
  }
  
  .filter-scroll {
    white-space: nowrap;
    margin-top: 10px;
    
    .filter-item {
      display: inline-block;
      padding: 5px 15px;
      background: #333;
      border-radius: 15px;
      margin-right: 10px;
      font-size: 14px;
      color: #ccc;
      
      &.active {
        background: #007aff;
        color: #fff;
      }
    }
  }
}

.code-list {
  flex: 1;
  padding: 10px;
  box-sizing: border-box;
}

.code-card {
  display: flex;
  background: #252525;
  border-radius: 10px;
  margin-bottom: 10px;
  padding: 10px;
  
  .code-cover {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    flex-shrink: 0;
    margin-right: 10px;
  }
  
  .code-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .code-title {
      font-size: 16px;
      font-weight: bold;
      color: #fff;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
    
    .code-tags {
      margin: 5px 0;
      .lang-tag {
        font-size: 12px;
        background: #333;
        padding: 2px 6px;
        border-radius: 4px;
        margin-right: 5px;
        color: #aaa;
      }
      .price-tag {
        font-size: 12px;
        color: #ff9800;
        float: right;
      }
    }
    
    .code-intro {
      font-size: 12px;
      color: #888;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
    }
    
    .code-meta {
      font-size: 10px;
      color: #666;
      text-align: right;
      margin-top: 5px;
    }
  }
}

.fab-group {
    position: fixed;
    right: 20px;
    bottom: 50px;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.fab-btn {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: #fff;
    box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.my-btn {
    background: #4caf50;
}

.publish-btn {
    background: #007aff;
}

.loading-more, .empty-state {
    text-align: center;
    padding: 20px;
    color: #888;
}
</style>
