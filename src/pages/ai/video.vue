<template>
  <view class="page" :class="themeClass">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">AI 视频创作</text>
      <text class="subtitle">让创意动起来，生成精彩短视频</text>
    </view>

    <!-- 主 Tab 切换 -->
    <view class="tab-bar">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'create' }" 
        @tap="activeTab = 'create'"
      >
        <text>🎬 创作</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'gallery' }" 
        @tap="switchToGallery"
      >
        <text>📺 欣赏</text>
      </view>
    </view>

    <!-- ===== 创作 Tab ===== -->
    <scroll-view v-if="activeTab === 'create'" class="create-tab" scroll-y>
      <view class="section-card">
        <view class="section-header">
          <text class="section-icon">✨</text>
          <text class="section-title">视频描述</text>
        </view>
        
        <textarea
          class="prompt-input"
          v-model="videoForm.prompt"
          placeholder="描述您想生成的视频内容，例如：一只可爱的橘猫在阳光明媚的花园里追逐蝴蝶，画面温馨唯美..."
          :maxlength="500"
          auto-height
        />
        
        <view class="prompt-tips">
          <text>💡 提示：描述越详细，效果越好。支持中文输入。</text>
        </view>
      </view>

      <!-- 参考图片上传 -->
      <view class="section-card">
        <view class="section-header">
          <text class="section-icon">🖼️</text>
          <text class="section-title">参考图片 (可选)</text>
        </view>
        
        <view v-if="!refImagePath" class="upload-box" @tap="chooseRefImage">
          <text class="upload-plus">+</text>
          <text class="upload-hint">上传参考图片</text>
          <text class="upload-limit">支持 JPG/PNG，≤10MB</text>
        </view>
        <view v-else class="ref-preview">
          <image :src="refImagePath" class="ref-thumb" mode="aspectFill"></image>
          <view class="ref-remove" @tap="removeRefImage">×</view>
        </view>
      </view>

      <!-- 示例提示词 -->
      <view class="section-card">
        <view class="section-header">
          <text class="section-icon">💡</text>
          <text class="section-title">灵感示例</text>
        </view>
        <scroll-view scroll-x class="example-scroll">
          <view class="example-list">
            <view 
              v-for="(item, index) in promptExamples" 
              :key="index"
              class="example-item"
              @tap="useExample(item.prompt)"
            >
              <text class="example-title">{{ item.title }}</text>
              <text class="example-desc">{{ item.prompt }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 生成按钮 -->
      <view class="action-area">
        <view class="cost-tip">
          <text>⚡ 预计消耗50IT币 · 生成需3-10分钟</text>
        </view>
        <view class="generate-btn" :class="{ disabled: generating }" @tap="generateVideo">
          <text v-if="!generating">🚀 开始生成视频</text>
          <text v-else>⏳ 正在创建任务...</text>
        </view>
      </view>
      
      <view style="height: 40px;"></view>
    </scroll-view>

    <!-- ===== 欣赏 Tab ===== -->
    <view v-if="activeTab === 'gallery'" class="gallery-container">
      <!-- 子 Tab 切换 -->
      <view class="sub-tab-bar">
        <view 
          class="sub-tab-item" 
          :class="{ active: galleryType === 'public' }"
          @tap="switchGalleryType('public')"
        >
          <text>公共视频</text>
        </view>
        <view 
          class="sub-tab-item" 
          :class="{ active: galleryType === 'private' }"
          @tap="switchGalleryType('private')"
        >
          <text>我的视频</text>
        </view>
      </view>

      <scroll-view 
        class="gallery-scroll" 
        scroll-y 
        @scrolltolower="loadMoreVideos"
        :scroll-top="galleryScrollTop"
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view class="video-list">
          <view 
            v-for="item in videoList" 
            :key="item.videoId" 
            class="video-card"
            @tap="openVideoDetail(item)"
          >
            <!-- 视频封面/状态层 -->
            <view class="video-cover-box">
              <image 
                v-if="item.coverUrl || item.imgUrl"
                :src="getAssetUrl(item.coverUrl || item.imgUrl)" 
                class="video-cover" 
                mode="aspectFill"
              ></image>
              <view v-else class="video-placeholder">
                <text>🎬</text>
              </view>
              
              <!-- 状态标签 -->
              <view class="status-badge" :class="item.status">
                <text>{{ getStatusText(item.status) }}</text>
              </view>
              
              <!-- 生成中遮罩 -->
              <view v-if="item.status === 'running'" class="processing-mask">
                <view class="loading-spinner"></view>
                <text>生成中...</text>
              </view>
              
               <!-- 播放图标 -->
               <view v-if="item.status === 'succeeded'" class="play-icon-overlay">
                 <text class="play-icon">▶</text>
               </view>
            </view>
            
            <view class="video-info">
              <text class="video-prompt">{{ item.prompt || '无描述' }}</text>
              <view class="video-meta">
                <text class="time">{{ formatTime(item.createdTime) }}</text>
                <view v-if="galleryType === 'private'" class="status-icon">
                  <text v-if="item.isPublic">🔓</text>
                  <text v-else>🔒</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 空状态 -->
        <view v-if="!loading && videoList.length === 0" class="empty-state">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无视频数据</text>
        </view>
        
        <!-- 加载更多 -->
        <view v-if="loading" class="loading-more">
          <text>加载中...</text>
        </view>
        <view v-if="noMoreData && videoList.length > 0" class="no-more">
          <text>—— 没有更多了 ——</text>
        </view>
        
        <view style="height: 40px;"></view>
      </scroll-view>
    </view>

    <!-- 详情弹窗 -->
    <view v-if="showDetail" class="detail-modal" @tap="closeDetail">
      <view class="detail-box" @tap.stop>
        <view class="detail-header">
          <text class="detail-title">视频详情</text>
          <view class="close-btn" @tap="closeDetail">×</view>
        </view>
        
        <scroll-view class="detail-scroll" scroll-y>
          <!-- 视频播放器 -->
          <view v-if="detailData.status === 'succeeded' && detailData.videoUrl" class="player-wrapper">
             <video 
               :src="getAssetUrl(detailData.videoUrl)" 
               :poster="getAssetUrl(detailData.coverUrl)"
               class="video-player"
               controls
               object-fit="contain"
             ></video>
          </view>
          
          <view v-else class="status-display">
             <text class="status-text">{{ getStatusText(detailData.status) }}</text>
             <view v-if="detailData.status === 'running'" class="loading-spinner large"></view>
             <view v-if="detailData.status === 'failed'" class="retry-btn" @tap="retryGenerate(detailData)">
               <text>🔄 重新生成</text>
             </view>
          </view>
          
          <!-- 信息列表 -->
          <view class="info-group">
            <view class="info-row">
               <text class="label">ID</text>
               <text class="value">{{ detailData.videoId }}</text>
            </view>
            <view class="info-row">
               <text class="label">创建时间</text>
               <text class="value">{{ formatTime(detailData.createdTime) }}</text>
            </view>
            <view class="info-row" v-if="detailInfo">
               <text class="label">模型</text>
               <text class="value">{{ detailInfo.model || '未知' }}</text>
            </view>
             <view class="info-row" v-if="detailInfo">
               <text class="label">时长</text>
               <text class="value">{{ detailInfo.duration ? detailInfo.duration + '秒' : '-' }}</text>
            </view>
          </view>
          
          <view class="prompt-box">
             <text class="prompt-label">提示词</text>
             <text class="prompt-text">{{ detailData.prompt }}</text>
             <view class="copy-btn" @tap="copyText(detailData.prompt)">复制提示词</view>
          </view>

          <!-- 操作按钮 (仅我的视频) -->
          <view v-if="galleryType === 'private'" class="manage-actions">
            <view class="action-btn" :class="detailData.isPublic ? 'warning' : 'primary'" @tap="togglePublic(detailData)">
              <text>{{ detailData.isPublic ? '设为私有' : '设为公开' }}</text>
            </view>
            <view class="action-btn danger" @tap="deleteVideoItem(detailData)">
              <text>删除视频</text>
            </view>
          </view>
          
          <!-- 下载按钮 -->
          <view v-if="detailData.status === 'succeeded'" class="download-btn" @tap="downloadVideo(detailData)">
            <text>💾 保存视频</text>
          </view>

        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { useTheme } from '@/hooks/useTheme';
import { apiBaseUrl } from '@/store';

const { themeClass } = useTheme();

// 状态
const activeTab = ref('create');
const galleryType = ref('public'); // public | private
const imageBaseUrl = ref('');

// 创作状态
const videoForm = reactive({
  prompt: ''
});
const refImagePath = ref('');
const refImageFile = ref(null);
const generating = ref(false);

const promptExamples = [
  { title: '自然风景', prompt: '清晨的山谷里，薄雾缭绕，金色的阳光穿过云层洒向大地...' },
  { title: '城市夜景', prompt: '繁华都市的夜晚，霓虹灯闪烁，车流如织，从高楼俯视整个城市...' },
  { title: '动物世界', prompt: '非洲草原上，一群斑马在夕阳下奔跑，画面充满野性美...' }
];

// 列表状态
const videoList = ref([]);
const loading = ref(false);
const refreshing = ref(false);
const currentPage = ref(1);
const noMoreData = ref(false);
const galleryScrollTop = ref(0);

// 详情状态
const showDetail = ref(false);
const detailData = ref({});
const detailInfo = ref(null);

onMounted(() => {
  const envImageBase = import.meta.env.VUE_APP_IMAGE;
  imageBaseUrl.value = envImageBase || 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com';
});

// ===== 创作逻辑 =====
const chooseRefImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      const file = res.tempFiles[0];
      if (file.size > 10 * 1024 * 1024) {
        uni.showToast({ title: '图片不能超过10MB', icon: 'none' });
        return;
      }
      refImagePath.value = res.tempFilePaths[0];
      refImageFile.value = file; // 暂时只用路径，H5和App上传处理不同
    }
  });
};

const removeRefImage = () => {
  refImagePath.value = '';
  refImageFile.value = null;
};

const useExample = (text) => {
  videoForm.prompt = text;
};

const generateVideo = async () => {
  if (generating.value) return;
  if (!videoForm.prompt.trim()) {
    uni.showToast({ title: '请输入描述', icon: 'none' });
    return;
  }
  
  generating.value = true;
  const token = uni.getStorageSync('token');
  
  try {
    // 构造请求
    // 如果有图片，使用 uploadFile
    if (refImagePath.value) {
      uni.uploadFile({
        url: apiBaseUrl + '/video/createTask', // 假设后端接口路径
        filePath: refImagePath.value,
        name: 'file',
        formData: {
          prompt: videoForm.prompt
        },
        header: { 'Authorization': token },
        success: (res) => {
          generating.value = false;
          let data = res.data;
          if (typeof data === 'string') data = JSON.parse(data);
          
          if (data.code === 200) {
            uni.showToast({ title: '任务创建成功', icon: 'success' });
            videoForm.prompt = '';
            removeRefImage();
            // 切换到我的视频列表
            switchToGallery('private');
          } else {
             uni.showToast({ title: data.msg || '创建失败', icon: 'none' });
          }
        },
        fail: (err) => {
           generating.value = false;
           uni.showToast({ title: '上传失败', icon: 'none' });
        }
      });
    } else {
      // 纯文本生成
      const res = await uni.request({
        url: apiBaseUrl + '/video/createTask',  
        method: 'POST',
        header: {
          'Authorization': token,
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        data: {
          prompt: videoForm.prompt
        }
      });
      
      const resp = res.data;
      generating.value = false;
      
      if (resp.code === 200) {
         uni.showToast({ title: '任务创建成功', icon: 'success' });
         videoForm.prompt = '';
         // 切换到我的视频
         switchToGallery('private');
      } else {
         uni.showToast({ title: resp.msg || '创建失败', icon: 'none' });
      }
    }
  } catch (err) {
    generating.value = false;
    uni.showToast({ title: '请求出错', icon: 'none' });
  }
};

// ===== 列表逻辑 =====
const switchToGallery = (type = 'public') => {
  activeTab.value = 'gallery';
  if (type) galleryType.value = type;
  
  // 重置列表并加载
  videoList.value = [];
  currentPage.value = 1;
  noMoreData.value = false;
  loadVideoList();
};

const switchGalleryType = (type) => {
  if (type === galleryType.value) return;
  galleryType.value = type;
  refreshList();
};

const loadVideoList = async () => {
  if (loading.value || (noMoreData.value && !refreshing.value)) return;
  loading.value = true;
  const token = uni.getStorageSync('token');
  
  try {
    const url = galleryType.value === 'private' 
                ? '/video/list/private' 
                : '/video/list/public';
                
    const res = await uni.request({
      url: apiBaseUrl + url + '?page=' + currentPage.value,
      method: 'GET',
      header: { 'Authorization': token }
    });
    
    const resp = res.data;
    let list = [];
    if (resp.code === 200) {
       list = resp.data || resp; // 兼容不同返回格式
    } else if (Array.isArray(resp)) {
       list = resp;
    }
    
    // 如果是对象且有 data 字段
    if (!Array.isArray(list) && list.data) list = list.data;
    if (!Array.isArray(list)) list = [];

    if (refreshing.value) {
       videoList.value = list;
       refreshing.value = false;
    } else {
       videoList.value.push(...list);
    }
    
    if (list.length > 0) {
       currentPage.value++;
       if (list.length < 10) noMoreData.value = true; // 假设每页10条
    } else {
       noMoreData.value = true;
    }
    
  } catch (err) {
     console.error('加载视频列表失败', err);
     uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
     loading.value = false;
     refreshing.value = false;
  }
};

const loadMoreVideos = () => {
  loadVideoList();
};

const onRefresh = () => {
  refreshing.value = true;
  currentPage.value = 1;
  noMoreData.value = false;
  loadVideoList();
};

const refreshList = () => {
  videoList.value = [];
  currentPage.value = 1;
  noMoreData.value = false;
  loadVideoList();
};

const getAssetUrl = (url) => {
   if (!url) return '';
   if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('wxfile:') || url.startsWith('data:')) return url;
   return imageBaseUrl.value + url;
};

// 分类状态文本
const getStatusText = (status) => {
   const map = {
     'running': '生成中',
     'succeeded': '已完成',
     'failed': '失败',
     'queueing': '排队中'
   };
   return map[status] || status;
};

// ===== 详情逻辑 =====
const openVideoDetail = (item) => {
   detailData.value = item;
   detailInfo.value = null; // 重置详细信息
   showDetail.value = true;
   
   // 异步获取详情
   fetchVideoDetail(item.videoId);
};

const fetchVideoDetail = async (videoId) => {
   const token = uni.getStorageSync('token');
   try {
     const res = await uni.request({
       url: apiBaseUrl + '/video/detail?videoId=' + videoId,
       method: 'GET',
       header: { 'Authorization': token }
     });
     if (res.data.code === 200) {
       detailInfo.value = res.data.data;
     }
   } catch (e) { console.error(e); }
};

const closeDetail = () => {
   showDetail.value = false;
};

const copyText = (text) => {
   uni.setClipboardData({ data: text });
};

const retryGenerate = async (item) => {
   const token = uni.getStorageSync('token');
   try {
     const res = await uni.request({
       url: apiBaseUrl + '/video/retry/' + item.videoId,
       method: 'POST',
       header: { 'Authorization': token }
     });
     if (res.data.code === 200) {
        uni.showToast({ title: '已重新提交' });
        item.status = 'running'; // 乐观更新
     } else {
        uni.showToast({ title: res.data.msg || '失败', icon: 'none' });
     }
   } catch(e) { uni.showToast({ title: '请求失败', icon: 'none' }); }
};

const togglePublic = async (item) => {
   const token = uni.getStorageSync('token');
   try {
     const res = await uni.request({
        url: apiBaseUrl + '/video/toggle?videoId=' + item.videoId,
        method: 'GET',
        header: { 'Authorization': token }
     });
     if (res.data.code === 200) {
        item.isPublic = !item.isPublic;
        uni.showToast({ title: item.isPublic ? '已公开' : '已私有' });
     }
   } catch(e) { uni.showToast({ title: '操作失败', icon: 'none' }); }
};

const deleteVideoItem = async (item) => {
   uni.showModal({
      title: '删除确认',
      content: '确定要删除这个视频吗？',
      success: async (res) => {
         if (res.confirm) {
             const token = uni.getStorageSync('token');
             await uni.request({
                 url: apiBaseUrl + '/video/delete/' + item.videoId,
                 method: 'DELETE', 
                 header: { 'Authorization': token }
             });
             closeDetail();
             refreshList();
             uni.showToast({ title: '已删除' });
         }
      }
   });
};

const downloadVideo = (item) => {
   const url = getAssetUrl(item.videoUrl);
   // #ifdef H5
   uni.showToast({ title: '请使用浏览器下载', icon: 'none' });
   // #endif
   // #ifndef H5
   uni.downloadFile({
      url: url,
      success: (res) => {
         uni.saveVideoToPhotosAlbum({
            filePath: res.tempFilePath,
            success: () => uni.showToast({ title: '保存成功' }),
            fail: () => uni.showToast({ title: '保存失败', icon: 'none' })
         });
      }
   });
   // #endif
};

const formatTime = (timeStr) => {
   if (!timeStr) return '';
   const d = new Date(timeStr);
   return d.toLocaleDateString() + ' ' + d.toLocaleTimeString().slice(0, 5);
}

</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: var(--bgColor1);
  display: flex;
  flex-direction: column;
}

.header {
  padding: 20px 20px 10px;
  text-align: center;
  
  .title {
    display: block;
    font-size: 24px;
    font-weight: 800;
    background: linear-gradient(135deg, #667eea, #764ba2);
    background-clip: text;
    -webkit-background-clip: text;
    color: transparent;
  }
  
  .subtitle {
    display: block;
    font-size: 13px;
    color: var(--textColor3);
    margin-top: 4px;
  }
}

.tab-bar {
  display: flex;
  margin: 12px 20px 0;
  background: var(--bgColor2);
  border-radius: 12px;
  padding: 4px;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 10px 0;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 500;
    color: var(--textColor3);
    transition: all 0.3s;
    
    &.active {
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: #fff;
    }
  }
}

/* 创作 Tab */
.create-tab {
  flex: 1;
  padding: 16px;
  box-sizing: border-box;
  width: 100%;
}

.section-card {
  background: var(--bgColor2);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 14px;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  
  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--textColor1);
  }
}

.prompt-input {
  width: 100%;
  min-height: 120px;
  background: var(--bgColor1);
  border-radius: 10px;
  padding: 12px;
  font-size: 14px;
  color: var(--textColor1);
  line-height: 1.5;
  box-sizing: border-box;
}

.prompt-tips {
  margin-top: 8px;
  font-size: 12px;
  color: var(--textColor4);
}

.upload-box {
  border: 2px dashed var(--bgColor3);
  border-radius: 10px;
  padding: 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .upload-plus {
    font-size: 30px;
    color: var(--textColor3);
  }
  .upload-hint {
    font-size: 13px;
    color: var(--textColor2);
    margin-top: 4px;
  }
  .upload-limit {
    font-size: 11px;
    color: var(--textColor4);
  }
}

.ref-preview {
  position: relative;
  width: 100%; 
  height: 160px;
  
  .ref-thumb {
    width: 100%;
    height: 100%;
    border-radius: 10px;
  }
  
  .ref-remove {
    position: absolute;
    top: 8px;
    right: 8px;
    background: rgba(0,0,0,0.6);
    color: #fff;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    text-align: center;
    line-height: 24px;
  }
}

.example-scroll {
  white-space: nowrap;
  width: 100%;
}

.example-list {
  display: flex;
  gap: 10px;
}

.example-item {
  display: inline-flex;
  flex-direction: column;
  background: var(--bgColor1);
  padding: 10px;
  border-radius: 10px;
  width: 140px;
  white-space: normal;
  
  .example-title {
    font-size: 13px;
    font-weight: 600;
    color: var(--textColor1);
    margin-bottom: 4px;
  }
  
  .example-desc {
    font-size: 11px;
    color: var(--textColor3);
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.action-area {
  margin-top: 20px;
  text-align: center;
  
  .cost-tip {
    font-size: 11px;
    color: var(--textColor4);
    margin-bottom: 8px;
  }
  
  .generate-btn {
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: #fff;
    padding: 14px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
    
    &.disabled {
      opacity: 0.7;
    }
  }
}

/* 欣赏 Tab */
.gallery-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.sub-tab-bar {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 10px;
  
  .sub-tab-item {
    font-size: 14px;
    color: var(--textColor3);
    padding-bottom: 4px;
    border-bottom: 2px solid transparent;
    
    &.active {
      color: var(--textColor1);
      border-bottom-color: #667eea;
      font-weight: 600;
    }
  }
}

.gallery-scroll {
  flex: 1;
  padding: 0 16px;
  box-sizing: border-box;
  width: 100%;
}

.video-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding-bottom: 20px;
}

.video-card {
  background: var(--bgColor2);
  border-radius: 12px;
  overflow: hidden;
  box-sizing: border-box;
  
  .video-cover-box {
    position: relative;
    width: 100%;
    aspect-ratio: 16/9;
    background: #000;
    
    .video-cover {
       width: 100%;
       height: 100%;
    }
    
    .video-placeholder {
       width: 100%;
       height: 100%;
       display: flex;
       align-items: center;
       justify-content: center;
       font-size: 24px;
    }
    
    .status-badge {
       position: absolute;
       top: 6px;
       left: 6px;
       padding: 2px 6px;
       border-radius: 4px;
       font-size: 10px;
       color: #fff;
       
       &.running { background: #e6a23c; }
       &.succeeded { background: #67c23a; }
       &.failed { background: #f56c6c; }
    }
    
    .processing-mask {
       position: absolute;
       top: 0; left: 0; right: 0; bottom: 0;
       background: rgba(0,0,0,0.6);
       display: flex;
       flex-direction: column;
       align-items: center;
       justify-content: center;
       color: #fff;
       font-size: 12px;
       gap: 4px;
    }
    
    .play-icon-overlay {
       position: absolute;
       top: 50%; left: 50%;
       transform: translate(-50%, -50%);
       width: 32px; height: 32px;
       background: rgba(0,0,0,0.5);
       border-radius: 50%;
       display: flex; 
       align-items: center; 
       justify-content: center;
       
       .play-icon { color: #fff; font-size: 14px; margin-left: 2px; }
    }
  }
  
  .video-info {
     padding: 8px;
     
     .video-prompt {
        font-size: 12px;
        color: var(--textColor1);
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        margin-bottom: 4px;
     }
     
     .video-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .time { font-size: 10px; color: var(--textColor4); }
        .status-icon { font-size: 12px; color: var(--textColor3); }
     }
  }
}

.loading-spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  
  &.large { width: 24px; height: 24px; margin: 10px auto; border-color: var(--textColor3); border-top-color: var(--themeColor1); }
}

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.empty-state, .loading-more, .no-more {
  text-align: center;
  padding: 20px;
  color: var(--textColor3);
  font-size: 13px;
  
  .empty-icon { font-size: 40px; display: block; margin-bottom: 8px; }
}

/* 详情弹窗 */
.detail-modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.8);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

.detail-box {
  width: 100%;
  max-height: 85vh;
  background: var(--bgColor2);
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
  
  .detail-title { font-weight: 600; color: var(--textColor1); }
  .close-btn { font-size: 18px; color: var(--textColor3); padding: 0 4px; }
}

.detail-scroll {
  flex: 1;
  padding: 16px;
  box-sizing: border-box;
  width: 100%;
}

.player-wrapper {
  width: 100%;
  aspect-ratio: 16/9;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
  
  .video-player { width: 100%; height: 100%; }
}

.status-display {
  text-align: center;
  padding: 20px;
  background: var(--bgColor1);
  border-radius: 8px;
  margin-bottom: 16px;
  
  .status-text { color: var(--textColor2); display: block; margin-bottom: 8px; }
  .retry-btn { 
     display: inline-block; padding: 6px 16px; 
     background: var(--bgColor3); border-radius: 20px; 
     font-size: 13px; margin-top: 8px; color: var(--textColor1);
  }
}

.info-group {
  margin-bottom: 16px;
  
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid rgba(255,255,255,0.03);
    
    .label { color: var(--textColor3); font-size: 13px; }
    .value { color: var(--textColor1); font-size: 13px; }
  }
}

.prompt-box {
  background: var(--bgColor1);
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  .prompt-label { display: block; color: var(--textColor3); font-size: 12px; margin-bottom: 6px; }
  .prompt-text { display: block; color: var(--textColor1); font-size: 13px; line-height: 1.5; margin-bottom: 8px; }
  .copy-btn { 
    display: inline-block; padding: 4px 10px; background: rgba(102, 126, 234, 0.1); 
    color: #667eea; font-size: 11px; border-radius: 4px; 
  }
}

.manage-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .action-btn {
     flex: 1; padding: 10px; text-align: center; border-radius: 8px; font-size: 13px;
     
     &.primary { background: var(--bgColor3); color: var(--textColor1); }
     &.warning { background: rgba(230, 162, 60, 0.2); color: #e6a23c; }
     &.danger { background: rgba(245, 108, 108, 0.2); color: #f56c6c; }
  }
}

.download-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  padding: 12px;
  text-align: center;
  border-radius: 10px;
  font-weight: 500;
  font-size: 14px;
}
</style>
