<template>
  <view class="page" :class="themeClass">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">AI 智能绘画</text>
      <text class="subtitle">将创意变为精美艺术作品</text>
    </view>

    <!-- Tab 切换 -->
    <view class="tab-bar">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'create' }" 
        @tap="activeTab = 'create'"
      >
        <text>🎨 创作</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'gallery' }" 
        @tap="switchToGallery"
      >
        <text>🖼️ 欣赏</text>
      </view>
    </view>

    <!-- ===== 创作 Tab ===== -->
    <scroll-view v-if="activeTab === 'create'" class="create-tab" scroll-y>
      <!-- 提示词输入 -->
      <view class="section-card">
        <view class="section-header">
          <text class="section-icon">✨</text>
          <text class="section-title">创作提示</text>
        </view>

        <!-- 参考图片上传 - 仅图生图模型 -->
        <view v-if="drawingForm.model === 'DOUBAO_SEEDEDIT'" class="ref-upload">
          <view class="upload-label">参考图片</view>
          <view v-if="!refImagePath" class="upload-box" @tap="chooseRefImage">
            <text class="upload-plus">+</text>
            <text class="upload-hint">点击上传参考图片</text>
            <text class="upload-limit">支持 JPG/PNG，≤5MB</text>
          </view>
          <view v-else class="ref-preview">
            <image :src="refImagePath" class="ref-thumb" mode="aspectFill"></image>
            <view class="ref-remove" @tap="removeRefImage">×</view>
          </view>
        </view>

        <textarea
          class="prompt-input"
          v-model="drawingForm.prompt"
          placeholder="输入详细的图像描述，如: 一只戴着太阳镜的柴犬，坐在海滩上，背景是金色的落日..."
          :maxlength="-1"
          auto-height
        />
        <view class="cost-tip">
          <text>⚡ 一次请求消耗10IT币 · 生成图片默认公开展示</text>
        </view>
      </view>

      <!-- 生成设置 -->
      <view class="section-card">
        <view class="section-header">
          <text class="section-icon">⚙️</text>
          <text class="section-title">生成设置</text>
        </view>

        <view class="setting-row">
          <text class="setting-label">AI模型</text>
          <picker 
            :range="modelOptions" 
            range-key="label" 
            :value="modelIndex" 
            @change="onModelChange"
          >
            <view class="picker-value">
              {{ modelOptions[modelIndex].label }}
              <text class="arrow">▸</text>
            </view>
          </picker>
        </view>

        <view class="size-row">
          <view class="size-item">
            <text class="setting-label">宽度(px)</text>
            <view class="stepper">
              <view class="stepper-btn" @tap="adjustSize('width', -64)">−</view>
              <text class="stepper-value">{{ drawingForm.width }}</text>
              <view class="stepper-btn" @tap="adjustSize('width', 64)">+</view>
            </view>
          </view>
          <view class="size-item">
            <text class="setting-label">高度(px)</text>
            <view class="stepper">
              <view class="stepper-btn" @tap="adjustSize('height', -64)">−</view>
              <text class="stepper-value">{{ drawingForm.height }}</text>
              <view class="stepper-btn" @tap="adjustSize('height', 64)">+</view>
            </view>
          </view>
        </view>

        <view v-if="drawingForm.model === 'COGVIEW_4'" class="setting-row">
          <text class="setting-label">质量</text>
          <picker 
            :range="qualityOptions" 
            range-key="label" 
            :value="qualityIndex" 
            @change="onQualityChange"
          >
            <view class="picker-value">
              {{ qualityOptions[qualityIndex].label }}
              <text class="arrow">▸</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <view class="reset-btn" @tap="resetForm">
          <text>🔄 重置</text>
        </view>
        <view class="generate-btn" :class="{ disabled: generating }" @tap="generateImage">
          <text v-if="!generating">🎨 开始生成</text>
          <text v-else>⏳ 生成中...</text>
        </view>
      </view>

      <!-- 生成结果 -->
      <view v-if="generating" class="result-area">
        <view class="generating-animation">
          <view class="loading-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
          <text class="generating-text">AI 正在绘制您的创意...</text>
        </view>
      </view>

      <view v-else-if="resultImages.length > 0" class="result-area">
        <view class="section-header">
          <text class="section-icon">🖼️</text>
          <text class="section-title">生成结果</text>
        </view>
        <view class="result-grid">
          <view 
            v-for="(img, idx) in resultImages" 
            :key="idx" 
            class="result-card"
            @tap="previewResultImage(img)"
          >
            <image :src="img" class="result-img" mode="widthFix"></image>
            <view class="result-actions">
              <view class="result-action-btn" @tap.stop="saveImage(img)">💾 保存</view>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-result">
        <text class="empty-icon">🎨</text>
        <text class="empty-text">填写创作提示并点击"开始生成"</text>
        <text class="empty-sub">创作您的AI艺术作品</text>
      </view>

      <!-- 底部占位 -->
      <view style="height: 60px;"></view>
    </scroll-view>

    <!-- ===== 欣赏 Tab ===== -->
    <scroll-view 
      v-if="activeTab === 'gallery'" 
      class="gallery-tab" 
      scroll-y 
      @scrolltolower="loadMoreImages"
      :scroll-top="galleryScrollTop"
    >
      <!-- 刷新按钮 -->
      <view class="gallery-toolbar">
        <text class="gallery-count">共 {{ imageList.length }} 幅作品</text>
        <view class="refresh-btn" @tap="refreshGallery">
          <text>🔄 刷新</text>
        </view>
      </view>

      <!-- 图片网格 -->
      <view class="gallery-grid">
        <view 
          v-for="(item, index) in imageList" 
          :key="item.drawingId || index" 
          class="gallery-card"
          @tap="openPreview(item)"
        >
          <image 
            :src="imageBaseUrl + item.generateUrl" 
            class="gallery-img" 
            mode="widthFix"
            lazy-load
          ></image>
          <view class="gallery-info">
            <text class="gallery-prompt">{{ item.prompt || '无描述' }}</text>
            <view class="gallery-meta">
              <text class="gallery-user">{{ item.userName || '匿名' }}</text>
              <text class="gallery-time">{{ formatTime(item.createdTime) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="galleryLoading" class="loading-bar">
        <view class="loading-dots">
          <view class="dot"></view>
          <view class="dot"></view>
          <view class="dot"></view>
        </view>
        <text>加载更多作品...</text>
      </view>

      <view v-if="noMoreData && imageList.length > 0" class="no-more">
        <text>—— 已展示所有作品 ——</text>
      </view>

      <view v-if="!galleryLoading && imageList.length === 0" class="empty-gallery">
        <text class="empty-icon">🖼️</text>
        <text class="empty-text">暂无公开作品</text>
        <text class="empty-sub">快去创作第一个AI绘画吧!</text>
      </view>

      <view style="height: 60px;"></view>
    </scroll-view>

    <!-- ===== 图片预览弹窗 ===== -->
    <view v-if="showPreview" class="preview-modal" @tap="showPreview = false">
      <view class="preview-box" @tap.stop>
        <view class="preview-header">
          <text class="preview-title">作品详情</text>
          <view class="preview-close" @tap="showPreview = false">×</view>
        </view>
        <scroll-view class="preview-scroll" scroll-y>
          <image 
            :src="previewData.fullUrl" 
            class="preview-img" 
            mode="widthFix"
            @tap="previewFullImage"
          ></image>

          <!-- 作品信息 -->
          <view class="detail-section">
            <view class="detail-row">
              <text class="detail-label">创作者</text>
              <text class="detail-value">{{ previewData.userName || '匿名' }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">创建时间</text>
              <text class="detail-value">{{ formatTime(previewData.createdTime) }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">模型</text>
              <text class="detail-value">{{ previewData.model || 'AI绘画模型' }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">尺寸</text>
              <text class="detail-value">{{ previewData.size || '1024x1024' }}</text>
            </view>
            <view v-if="previewData.refImageUrl" class="detail-row">
              <text class="detail-label">参考原图</text>
              <image 
                :src="previewData.refImageUrl" 
                class="ref-img-preview" 
                mode="aspectFill" 
                @tap="previewRefImage"
                style="width: 60px; height: 60px; border-radius: 6px;"
              ></image>
            </view>
          </view>

          <view class="prompt-section">
            <text class="prompt-label">创作提示词</text>
            <text class="prompt-text">{{ previewData.prompt || '暂无描述' }}</text>
            <view class="copy-prompt-btn" @tap="copyPrompt">
              <text>📋 复制提示词</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="preview-actions">
            <view class="preview-action-btn save" @tap="saveImage(previewData.fullUrl)">
              💾 保存图片
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useTheme } from '@/hooks/useTheme';
import { apiBaseUrl } from '@/store';

const { themeClass } = useTheme();

// ===== 通用状态 =====
const activeTab = ref('create');
const imageBaseUrl = ref('');

onMounted(() => {
  // 从环境变量获取图片 OSS 域名
  // 如果未配置，使用默认值
  const envImageBase = import.meta.env.VUE_APP_IMAGE;
  imageBaseUrl.value = envImageBase || 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com';
});

// ===== 创作 Tab 状态 =====
const drawingForm = reactive({
  prompt: '',
  width: 1024,
  height: 1024,
  quality: 'standard',
  model: 'COGVIEW_4'
});

const generating = ref(false);
const resultImages = ref([]);

// 参考图片
const refImagePath = ref('');
const refImageFile = ref(null);

const modelOptions = [
  { value: 'COGVIEW_4', label: 'CogView4文生图(推荐)' },
  { value: 'DOUBAO_SEEDREAM', label: '豆包文生图' },
  { value: 'DOUBAO_SEEDEDIT', label: '豆包图生图' }
];
const modelIndex = ref(0);

const qualityOptions = [
  { value: 'standard', label: '标准质量' },
  { value: 'hd', label: '高清质量' }
];
const qualityIndex = ref(0);

const onModelChange = (e) => {
  modelIndex.value = e.detail.value;
  drawingForm.model = modelOptions[modelIndex.value].value;
};

const onQualityChange = (e) => {
  qualityIndex.value = e.detail.value;
  drawingForm.quality = qualityOptions[qualityIndex.value].value;
};

const adjustSize = (key, delta) => {
  const val = drawingForm[key] + delta;
  if (val >= 512 && val <= 2048) {
    drawingForm[key] = val;
  }
};

// 参考图片选择
const chooseRefImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const file = res.tempFiles[0];
      if (file.size > 5 * 1024 * 1024) {
        uni.showToast({ title: '图片不能超过5MB', icon: 'none' });
        return;
      }
      refImagePath.value = res.tempFilePaths[0];
      refImageFile.value = file;
    }
  });
};

const removeRefImage = () => {
  refImagePath.value = '';
  refImageFile.value = null;
};

// 监听模型切换
watch(() => drawingForm.model, (val) => {
  if (val !== 'DOUBAO_SEEDEDIT') {
    removeRefImage();
  }
});

// 生成图片
const generateImage = async () => {
  if (generating.value) return;
  if (!drawingForm.prompt.trim()) {
    uni.showToast({ title: '请输入提示词', icon: 'none' });
    return;
  }
  if (drawingForm.model === 'DOUBAO_SEEDEDIT' && !refImageFile.value) {
    uni.showToast({ title: '请上传参考图片', icon: 'none' });
    return;
  }

  generating.value = true;
  const token = uni.getStorageSync('token');
  const size = `${drawingForm.width}x${drawingForm.height}`;

  try {
    if (drawingForm.model === 'DOUBAO_SEEDEDIT' && refImageFile.value) {
      // 图生图 - 使用 uploadFile
      const uploadRes = await new Promise((resolve, reject) => {
        uni.uploadFile({
          url: apiBaseUrl + '/draw/zhipu/image',
          filePath: refImagePath.value,
          name: 'file',
          formData: {
            prompt: drawingForm.prompt,
            size: size,
            quality: drawingForm.quality,
            model: drawingForm.model
          },
          header: { 'Authorization': token },
          success: resolve,
          fail: reject
        });
      });
      
      let data = uploadRes.data;
      if (typeof data === 'string') data = JSON.parse(data);
      
      if (data.code === 200 && data.data) {
        const imgUrl = typeof data.data === 'string' ? data.data : data.data.url || data.data;
        resultImages.value = [imgUrl];
        uni.showToast({ title: '生成成功!', icon: 'success' });
      } else {
        uni.showToast({ title: data.msg || '生成失败', icon: 'none' });
      }
    } else {
      // 文生图 - 使用 uni.request
      const res = await uni.request({
        url: apiBaseUrl + '/draw/zhipu/image',
        method: 'POST',
        header: {
          'Authorization': token,
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        data: {
          prompt: drawingForm.prompt,
          size: size,
          quality: drawingForm.quality,
          model: drawingForm.model
        }
      });

      const resp = res.data;
      if (resp.code === 200 && resp.data) {
        const imgData = resp.data;
        if (typeof imgData === 'string') {
          resultImages.value = [imgData];
        } else if (Array.isArray(imgData)) {
          resultImages.value = imgData;
        } else if (imgData.url) {
          resultImages.value = [imgData.url];
        }
        uni.showToast({ title: '生成成功!', icon: 'success' });
      } else {
        uni.showToast({ title: resp.msg || '生成失败', icon: 'none' });
      }
    }
  } catch (err) {
    console.error('生成错误:', err);
    uni.showToast({ title: '生成出错，请重试', icon: 'none' });
  } finally {
    generating.value = false;
  }
};

const resetForm = () => {
  drawingForm.prompt = '';
  drawingForm.width = 1024;
  drawingForm.height = 1024;
  drawingForm.quality = 'standard';
  drawingForm.model = 'COGVIEW_4';
  modelIndex.value = 0;
  qualityIndex.value = 0;
  removeRefImage();
  resultImages.value = [];
};

// 预览结果图
const previewResultImage = (url) => {
  uni.previewImage({
    urls: resultImages.value.length > 0 ? resultImages.value : [url],
    current: url
  });
};

// 保存图片
const saveImage = (url) => {
  // #ifdef H5
  uni.showToast({ title: '请长按图片保存', icon: 'none' });
  // #endif
  // #ifndef H5
  uni.downloadFile({
    url: url,
    success: (res) => {
      uni.saveImageToPhotosAlbum({
        filePath: res.tempFilePath,
        success: () => {
          uni.showToast({ title: '保存成功', icon: 'success' });
        },
        fail: () => {
          uni.showToast({ title: '保存失败', icon: 'none' });
        }
      });
    },
    fail: () => {
      uni.showToast({ title: '下载失败', icon: 'none' });
    }
  });
  // #endif
};

// ===== 欣赏 Tab 状态 =====
const imageList = ref([]);
const galleryLoading = ref(false);
const currentPage = ref(1);
const noMoreData = ref(false);
const galleryScrollTop = ref(0);
const showPreview = ref(false);
const previewData = ref({});

const switchToGallery = () => {
  activeTab.value = 'gallery';
  if (imageList.value.length === 0) {
    loadGalleryImages();
  }
};

const loadGalleryImages = async () => {
  if (galleryLoading.value || noMoreData.value) return;
  galleryLoading.value = true;
  const token = uni.getStorageSync('token');

  try {
    const res = await uni.request({
      url: apiBaseUrl + '/draw/list/public?page=' + currentPage.value,
      method: 'GET',
      header: { 'Authorization': token }
    });

    const resp = res.data;
    let list = [];
    if (resp.code === 200) {
      list = resp.data || [];
    } else if (Array.isArray(resp)) {
      list = resp;
    } else if (resp.data && Array.isArray(resp.data)) {
      list = resp.data;
    }

    if (list.length > 0) {
      imageList.value.push(...list);
      currentPage.value++;
      if (list.length < 20) {
        noMoreData.value = true;
      }
    } else {
      noMoreData.value = true;
    }
  } catch (err) {
    console.error('加载画廊失败:', err);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    galleryLoading.value = false;
  }
};

const loadMoreImages = () => {
  loadGalleryImages();
};

const refreshGallery = () => {
  imageList.value = [];
  currentPage.value = 1;
  noMoreData.value = false;
  galleryScrollTop.value = 0;
  loadGalleryImages();
  uni.showToast({ title: '刷新成功', icon: 'success' });
};

const openPreview = (item) => {
  const imgUrl = item.generateUrl.startsWith('http') ? item.generateUrl : imageBaseUrl.value + item.generateUrl;
  let refUrl = '';
  if (item.image) {
    refUrl = item.image.startsWith('http') ? item.image : imageBaseUrl.value + item.image;
  }

  previewData.value = {
    ...item,
    fullUrl: imgUrl,
    refImageUrl: refUrl
  };
  showPreview.value = true;
};

const previewFullImage = () => {
  uni.previewImage({
    urls: [previewData.value.fullUrl],
    current: previewData.value.fullUrl
  });
};

const previewRefImage = () => {
  if (previewData.value.refImageUrl) {
    uni.previewImage({
      urls: [previewData.value.refImageUrl],
      current: previewData.value.refImageUrl
    });
  }
};

const copyPrompt = () => {
  const text = previewData.value.prompt || '';
  if (!text) {
    uni.showToast({ title: '无提示词', icon: 'none' });
    return;
  }
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' });
    }
  });
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知';
  const time = new Date(timeStr);
  const now = new Date();
  const diff = now - time;
  const minute = 60000;
  const hour = 3600000;
  const day = 86400000;
  
  if (diff < minute) return '刚刚';
  if (diff < hour) return Math.floor(diff / minute) + '分钟前';
  if (diff < day) return Math.floor(diff / hour) + '小时前';
  if (diff < day * 30) return Math.floor(diff / day) + '天前';
  return time.toLocaleDateString();
};
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: var(--bgColor1);
  display: flex;
  flex-direction: column;
}

/* ===== 顶部 ===== */
.header {
  padding: 20px 20px 10px;
  text-align: center;
  
  .title {
    display: block;
    font-size: 24px;
    font-weight: 800;
    background: linear-gradient(135deg, #7b61ff, #a78bfa);
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

/* ===== Tab 切换 ===== */
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
      background: linear-gradient(135deg, #7b61ff, #a78bfa);
      color: #fff;
      box-shadow: 0 4px 12px rgba(123, 97, 255, 0.3);
    }
  }
}

/* ===== 创作 Tab ===== */
.create-tab {
  flex: 1;
  padding: 16px 16px 0;
  box-sizing: border-box;
  width: 100%;
  overflow: hidden;
}

.section-card {
  background: var(--bgColor2);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 14px;
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 3px;
    background: linear-gradient(90deg, #7b61ff, #a78bfa);
    opacity: 0.7;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  
  .section-icon {
    font-size: 16px;
  }
  
  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--textColor1);
  }
}

.prompt-input {
  width: 100%;
  min-height: 100px;
  background: var(--bgColor1);
  border-radius: 10px;
  padding: 12px;
  font-size: 14px;
  color: var(--textColor1);
  line-height: 20px;
  box-sizing: border-box;
}

.cost-tip {
  margin-top: 8px;
  
  text {
    font-size: 11px;
    color: var(--textColor4);
  }
}

/* 参考图片上传 */
.ref-upload {
  margin-bottom: 12px;
  
  .upload-label {
    font-size: 13px;
    color: var(--textColor2);
    margin-bottom: 8px;
  }
  
  .upload-box {
    border: 2px dashed var(--bgColor3);
    border-radius: 10px;
    padding: 20px;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    
    .upload-plus {
      font-size: 32px;
      color: var(--textColor3);
    }
    
    .upload-hint {
      font-size: 13px;
      color: var(--textColor3);
    }
    
    .upload-limit {
      font-size: 11px;
      color: var(--textColor4);
    }
  }
  
  .ref-preview {
    position: relative;
    width: 120px;
    height: 120px;
    
    .ref-thumb {
      width: 120px;
      height: 120px;
      border-radius: 10px;
    }
    
    .ref-remove {
      position: absolute;
      top: -6px;
      right: -6px;
      width: 22px;
      height: 22px;
      background: #ff4d4f;
      color: #fff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
    }
  }
}

/* 设置 */
.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
  
  .setting-label {
    font-size: 14px;
    color: var(--textColor2);
  }
  
  .picker-value {
    font-size: 14px;
    color: var(--themeColor1);
    display: flex;
    align-items: center;
    gap: 4px;
    
    .arrow {
      font-size: 12px;
      color: var(--textColor4);
    }
  }
}

.size-row {
  display: flex;
  gap: 16px;
  padding: 10px 0;
  
  .size-item {
    flex: 1;
    
    .setting-label {
      display: block;
      font-size: 13px;
      color: var(--textColor3);
      margin-bottom: 8px;
    }
  }
}

.stepper {
  display: flex;
  align-items: center;
  background: var(--bgColor1);
  border-radius: 8px;
  overflow: hidden;
  
  .stepper-btn {
    width: 36px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    color: var(--textColor2);
    
    &:active {
      background: var(--bgColor3);
    }
  }
  
  .stepper-value {
    flex: 1;
    text-align: center;
    font-size: 14px;
    color: var(--textColor1);
    font-weight: 500;
  }
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .reset-btn {
    padding: 12px 20px;
    border-radius: 12px;
    border: 1.5px solid rgba(123, 97, 255, 0.3);
    text-align: center;
    font-size: 14px;
    color: var(--textColor2);
  }
  
  .generate-btn {
    flex: 1;
    padding: 12px 20px;
    border-radius: 12px;
    background: linear-gradient(135deg, #7b61ff, #a78bfa);
    text-align: center;
    font-size: 15px;
    font-weight: 600;
    color: #fff;
    box-shadow: 0 4px 15px rgba(123, 97, 255, 0.3);
    
    &.disabled {
      opacity: 0.6;
    }
  }
}

/* 生成动画 */
.generating-animation {
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--bgColor2);
  border-radius: 14px;
  
  .generating-text {
    color: var(--textColor2);
    font-size: 14px;
    margin-top: 16px;
  }
}

.loading-dots {
  display: flex;
  gap: 8px;
  
  .dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #7b61ff;
    animation: dotPulse 1.4s ease-in-out infinite;
    
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 结果展示 */
.result-area {
  margin-bottom: 16px;
}

.result-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.result-card {
  flex: 1;
  min-width: 45%;
  background: var(--bgColor2);
  border-radius: 12px;
  overflow: hidden;
  
  .result-img {
    width: 100%;
  }
  
  .result-actions {
    padding: 8px 12px;
    display: flex;
    justify-content: center;
    
    .result-action-btn {
      font-size: 12px;
      color: var(--themeColor1);
      padding: 4px 12px;
      background: rgba(123, 97, 255, 0.1);
      border-radius: 16px;
    }
  }
}

/* 空状态 */
.empty-result, .empty-gallery {
  padding: 60px 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }
  
  .empty-text {
    font-size: 16px;
    color: var(--textColor2);
    font-weight: 500;
  }
  
  .empty-sub {
    font-size: 13px;
    color: var(--textColor4);
    margin-top: 4px;
  }
}

/* ===== 欣赏 Tab ===== */
.gallery-tab {
  flex: 1;
  padding: 12px 16px 0;
  box-sizing: border-box;
  width: 100%;
}

.gallery-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  
  .gallery-count {
    font-size: 13px;
    color: var(--textColor3);
  }
  
  .refresh-btn {
    padding: 6px 14px;
    border-radius: 16px;
    background: var(--bgColor2);
    font-size: 12px;
    color: var(--textColor2);
  }
}

.gallery-grid {
  column-count: 2;
  column-gap: 10px;
}

.gallery-card {
  break-inside: avoid;
  background: var(--bgColor2);
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 10px;
  box-sizing: border-box;
  
  .gallery-img {
    width: 100%;
    display: block;
  }
  
  .gallery-info {
    padding: 10px;
    
    .gallery-prompt {
      font-size: 12px;
      color: var(--textColor2);
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .gallery-meta {
      display: flex;
      justify-content: space-between;
      margin-top: 6px;
      
      .gallery-user, .gallery-time {
        font-size: 10px;
        color: var(--textColor4);
      }
    }
  }
}

.loading-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: var(--textColor3);
  font-size: 13px;
}

.no-more {
  text-align: center;
  padding: 16px;
  color: var(--textColor4);
  font-size: 12px;
}

/* ===== 预览弹窗 ===== */
.preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

.preview-box {
  width: 100%;
  max-height: 85vh;
  background: var(--bgColor2);
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  
  .preview-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--textColor1);
  }
  
  .preview-close {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: var(--bgColor3);
    color: var(--textColor2);
    font-size: 16px;
  }
}

.preview-scroll {
  flex: 1;
  padding: 0 16px 16px;
  max-height: calc(85vh - 50px);
  box-sizing: border-box;
  width: 100%;
}

.preview-img {
  width: 100%;
  border-radius: 10px;
  margin-top: 12px;
}

.detail-section {
  margin-top: 16px;
  background: var(--bgColor1);
  border-radius: 10px;
  padding: 12px;
  box-sizing: border-box;
  width: 100%;
  
  .detail-row {
    display: flex;
    justify-content: space-between;
    padding: 6px 0;
    
    &:not(:last-child) {
      border-bottom: 1px solid rgba(255,255,255,0.04);
    }
    
    align-items: flex-start;

    
    .detail-label {
      font-size: 13px;
      color: var(--textColor3);
    }
    
    .detail-value {
      font-size: 13px;
      color: var(--textColor1);
      font-weight: 500;
      flex: 1;
      text-align: right;
      word-break: break-all;
      white-space: normal;
      margin-left: 12px;
    }
  }
}

.prompt-section {
  margin-top: 12px;
  background: var(--bgColor1);
  border-radius: 10px;
  padding: 12px;
  width: 100%;
  box-sizing: border-box;
  
  .prompt-label {
    display: block;
    font-size: 13px;
    color: var(--textColor3);
    margin-bottom: 6px;
  }
  
  .prompt-text {
    display: block;
    font-size: 14px;
    color: var(--textColor1);
    line-height: 1.5;
  }
  
  .copy-prompt-btn {
    margin-top: 8px;
    display: inline-flex;
    padding: 4px 12px;
    border-radius: 16px;
    background: rgba(123, 97, 255, 0.1);
    font-size: 12px;
    color: #7b61ff;
  }
}

.preview-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  
  .preview-action-btn {
    flex: 1;
    text-align: center;
    padding: 10px;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 500;
    
    &.save {
      background: linear-gradient(135deg, #7b61ff, #a78bfa);
      color: #fff;
    }
  }
}
</style>
