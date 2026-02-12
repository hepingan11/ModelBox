<template>
  <div class="drawing-gallery">
    <!-- 页面标题 -->
    <div class="gallery-header">
      <div class="header-content">
        <h1 class="title">
          <el-icon><Picture /></el-icon>
          AI绘画作品鉴赏
        </h1>
        <p class="subtitle">探索AI创造的精美艺术作品</p>
      </div>
      <el-button 
        class="refresh-btn" 
        circle 
        :icon="Refresh" 
        size="large"
        @click="refreshImages"
        :loading="loading"
        title="刷新列表"
      />
    </div>

    <!-- 图片网格容器 -->
    <div class="gallery-container" ref="galleryContainer">
      <div class="image-grid" ref="imageGrid">
        <div
          v-for="(item, index) in imageList"
          :key="item.drawingId"
          class="image-card"
          @click="openImagePreview(item, index)"
        >
          <!-- 图片容器 -->
          <div class="image-wrapper">
            <img
              :src="imageUrl + item.generateUrl"
              :alt="item.prompt"
              class="gallery-image"
              @load="onImageLoad"
              @error="onImageError"
            />
            <!-- 加载中遮罩 -->
            <div class="image-loading" v-if="!item.imageLoaded">
              <el-icon class="loading-icon"><Loading /></el-icon>
            </div>
          </div>

          <!-- 图片信息 -->
          <div class="image-info">
            <!-- 用户信息 -->
            <div class="user-info">
              <img :src="imageUrl + item.avatar || defaultAvatar" class="user-avatar" />
              <span class="user-name">{{ item.userName || '匿名用户' }}</span>
            </div>

            <!-- 提示词 -->
            <div class="prompt-text" :title="item.prompt">
              {{ item.prompt }}
            </div>

            <!-- 创建时间 -->
            <div class="create-time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(item.createdTime) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多指示器 -->
      <div class="loading-more" v-if="loading && imageList.length > 0">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载更多精彩作品...</span>
      </div>

      <!-- 没有更多数据提示 -->
      <div class="no-more-data" v-if="noMoreData && imageList.length > 0">
        <el-icon><CheckCircle /></el-icon>
        <span>已展示所有作品</span>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && imageList.length === 0">
        <el-icon class="empty-icon"><Picture /></el-icon>
        <h3>暂无作品</h3>
        <p>还没有公开的AI绘画作品，快去创作第一个吧！</p>
      </div>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      :title="null"
      width="85%"
      :show-close="false"
      center
      class="image-preview-dialog"
      :lock-scroll="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :top="'5vh'"
    >
      <template #header>
        <div class="custom-header">
          <div class="header-left">
            <el-icon class="dialog-icon"><Picture /></el-icon>
            <span class="dialog-title">作品详情</span>
          </div>
          <el-button 
            type="info" 
            :icon="Close" 
            circle 
            size="small"
            @click="previewVisible = false"
            class="close-btn"
          />
        </div>
      </template>
      
      <div class="preview-content">
        <!-- 左侧图片区域 -->
        <div class="preview-left">
          <div class="image-container">
            <el-image
              :src="imageUrl + previewImage.generateUrl"
              :alt="previewImage.prompt"
              fit="contain"
              class="preview-image"
              :preview-src-list="[imageUrl + previewImage.generateUrl]"
              :preview-teleported="true"
              :z-index="10000"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
          
          <!-- 图片操作按钮 -->
          <div class="image-actions">

            <el-button 
              type="success" 
              :icon="Download" 
              @click="downloadImage"
              size="small"
            >
              下载图片
            </el-button>
            
          </div>
        </div>
        
        <!-- 右侧信息区域 -->
        <div class="preview-right">
          <!-- 创作者信息 -->
          <div class="creator-section">
            <div class="creator-header">
              <img 
                :src="imageUrl + previewImage.avatar || defaultAvatar" 
                class="creator-avatar" 
              />
              <div class="creator-details">
                <h4 class="creator-name">{{ previewImage.userName || '匿名用户' }}</h4>
                <p class="creator-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(previewImage.createdTime) }}
                </p>
              </div>
            </div>
          </div>

          <!-- 作品信息 -->
          <div class="artwork-info">
            <div class="info-card">
              <div class="info-header">
                <el-icon><Edit /></el-icon>
                <span>创作提示词</span>
              </div>
              <div class="prompt-content">
                <p class="prompt-text">{{ previewImage.prompt || '暂无描述' }}</p>
                <el-button 
                  type="text" 
                  size="small"
                  @click="copyPrompt"
                  class="copy-btn"
                >
                  <el-icon><CopyDocument /></el-icon>
                  复制提示词
                </el-button>
              </div>
            </div>

            <!-- 技术参数 -->
            <div class="info-card">
              <div class="info-header">
                <el-icon><Setting /></el-icon>
                <span>技术参数</span>
              </div>
              <div class="params-grid">
                <div class="param-item">
                  <label>模型：</label>
                  <span>{{ previewImage.model || 'AI绘画模型' }}</span>
                </div>
                <div class="param-item">
                  <label>尺寸：</label>
                  <span>{{ previewImage.size || '512x512' }}</span>
                </div>
                <div class="param-item" v-if="previewImage.image">
                  <label>参考图片：</label>
                  <el-image
                    :src="imageUrl + previewImage.image "
                    class="preview-image"
                    fit="contain"
                    style="width: 100px; height: 100px;"
                  />
                </div>
              </div>
            </div>

          
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElNotification, ElMessage } from 'element-plus'
import { 
  Picture, Loading, Clock, CheckCircle, Close, ZoomIn, Download, Share,
  Edit, Setting, Star, StarFilled, ChatDotRound, CopyDocument, Refresh
} from '@element-plus/icons-vue'
import { getPublicDrawingList } from '../../../api/BSideApi'

// 响应式数据
const imageList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const noMoreData = ref(false)
const galleryContainer = ref(null)
const imageGrid = ref(null)

// 图片预览相关
const previewVisible = ref(false)
const previewImage = ref({})
const imageQuality = ref(4) // 图片质量评分
const isLiked = ref(false) // 是否收藏
const commentCount = ref(0) // 评论数量

// 默认头像
const defaultAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const imageUrl = ref("");
// 初始化加载
onMounted(() => {
  imageUrl.value = process.env.VUE_APP_IMAGE;
  loadImages()
  setupInfiniteScroll()
})
// 清理事件监听器
onUnmounted(() => {
  
  if (galleryContainer.value) {
    galleryContainer.value.removeEventListener('scroll', handleScroll)
  }
})

/**
 * 加载图片数据
 */
async function loadImages() {
  if (loading.value || noMoreData.value) return

  loading.value = true
  try {
    const response = await getPublicDrawingList(currentPage.value)
    
    if (response && response.length > 0) {
      // 为每个图片添加加载状态
      const newImages = response.map(item => ({
        ...item,
        imageLoaded: false
      }))
      
      imageList.value.push(...newImages)
      currentPage.value++
      
      // 如果返回的数据少于预期，说明没有更多数据了
      if (response.length < 20) {
        noMoreData.value = true
      }
    } else {
      noMoreData.value = true
    }
  } catch (error) {
    console.error('加载图片失败:', error)
    ElNotification({
      title: '加载失败',
      message: '获取图片列表失败，请稍后重试',
      type: 'error'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 刷新图片列表
 */
function refreshImages() {
  imageList.value = []
  currentPage.value = 1
  noMoreData.value = false
  loadImages()
  ElMessage.success('刷新成功')
}

/**
 * 设置无限滚动
 */
function setupInfiniteScroll() {
  if (!galleryContainer.value) return

  galleryContainer.value.addEventListener('scroll', handleScroll)
}

/**
 * 处理滚动事件
 */
function handleScroll() {
  if (!galleryContainer.value) return

  const { scrollTop, scrollHeight, clientHeight } = galleryContainer.value
  
  // 当滚动到底部附近时加载更多
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    loadImages()
  }
}

/**
 * 图片加载完成事件
 */
function onImageLoad(event) {
  const img = event.target
  const card = img.closest('.image-card')
  if (card) {
    const index = Array.from(card.parentNode.children).indexOf(card)
    if (imageList.value[index]) {
      imageList.value[index].imageLoaded = true
    }
  }
}

/**
 * 图片加载错误事件
 */
function onImageError(event) {
  const img = event.target
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik0xMDAgMTQwSDYwTDgwIDEwMEwxMDAgMTIwTDEyMCA4MEwxNDAgMTQwSDEwMFoiIGZpbGw9IiNEREREREQiLz4KPGNpcmNsZSBjeD0iODAiIGN5PSI4MCIgcj0iMTAiIGZpbGw9IiNEREREREQiLz4KPC9zdmc+'
  
  const card = img.closest('.image-card')
  if (card) {
    const index = Array.from(card.parentNode.children).indexOf(card)
    if (imageList.value[index]) {
      imageList.value[index].imageLoaded = true
    }
  }
}

/**
 * 打开图片预览
 */
function openImagePreview(item, index) {
  previewImage.value = { ...item }
  previewVisible.value = true
}

/**
 * 格式化时间
 */
function formatTime(timeStr) {
  if (!timeStr) return '未知时间'
  
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time
  
  const minute = 60 * 1000
  const hour = minute * 60
  const day = hour * 24
  const month = day * 30
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < month) {
    return Math.floor(diff / day) + '天前'
  } else {
    return time.toLocaleDateString()
  }
}

/**
 * 打开全屏预览
 */
function openFullPreview() {
  // 触发 el-image 的预览功能
  const imageEl = document.querySelector('.preview-image')
  if (imageEl) {
    imageEl.click()
  }
}

/**
 * 下载图片
 */
async function downloadImage() {
  try {
    const imageUrl = process.env.VUE_APP_IMAGE + previewImage.value.generateUrl
    const response = await fetch(imageUrl)
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `ai-artwork-${previewImage.value.drawingId || Date.now()}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElNotification({
      title: '下载成功',
      message: '图片已保存到本地',
      type: 'success'
    })
  } catch (error) {
    console.error('下载失败:', error)
    ElNotification({
      title: '下载失败',
      message: '图片下载失败，请稍后重试',
      type: 'error'
    })
  }
}

/**
 * 分享图片
 */
function shareImage() {
  const shareUrl = window.location.href
  const shareText = `看看这个精美的AI绘画作品：${previewImage.value.prompt}`
  
  if (navigator.share) {
    navigator.share({
      title: 'AI绘画作品分享',
      text: shareText,
      url: shareUrl
    }).catch(error => {
      console.log('分享失败:', error)
      copyToClipboard(shareUrl)
    })
  } else {
    copyToClipboard(shareUrl)
  }
}

/**
 * 复制提示词
 */
function copyPrompt() {
  copyToClipboard(previewImage.value.prompt)
}

/**
 * 复制到剪贴板
 */
function copyToClipboard(text) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(() => {
      ElNotification({
        title: '复制成功',
        message: '内容已复制到剪贴板',
        type: 'success'
      })
    }).catch(() => {
      fallbackCopyTextToClipboard(text)
    })
  } else {
    fallbackCopyTextToClipboard(text)
  }
}

/**
 * 兼容性复制方法
 */
function fallbackCopyTextToClipboard(text) {
  const textArea = document.createElement('textarea')
  textArea.value = text
  document.body.appendChild(textArea)
  textArea.select()
  try {
    document.execCommand('copy')
    ElNotification({
      title: '复制成功',
      message: '内容已复制到剪贴板',
      type: 'success'
    })
  } catch (err) {
    ElNotification({
      title: '复制失败',
      message: '请手动复制内容',
      type: 'error'
    })
  }
  document.body.removeChild(textArea)
}

/**
 * 切换收藏状态
 */
function toggleLike() {
  isLiked.value = !isLiked.value
  ElNotification({
    title: isLiked.value ? '收藏成功' : '取消收藏',
    message: isLiked.value ? '已添加到我的收藏' : '已从收藏中移除',
    type: 'success'
  })
}

/**
 * 打开评论
 */
function openComments() {
  ElNotification({
    title: '功能开发中',
    message: '评论功能正在开发中，敬请期待！',
    type: 'info'
  })
}
</script>

<style lang="scss" scoped>
.drawing-gallery {
  min-height: 100vh;
  background-color: var(--bgColor1);
  padding: 20px;

  .gallery-header {
    text-align: center;
    color: var(--textColor1);
    margin-bottom: 40px;
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;

    .header-content {
      flex: 1;
    }

    .refresh-btn {
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      background: var(--bgColor2);
      border: 1px solid var(--borderColor);
      color: var(--textColor1);
      font-size: 20px;
      width: 50px;
      height: 50px;
      transition: all 0.3s ease;

      &:hover {
        background: var(--bgColor3);
        transform: translateY(-50%) rotate(180deg);
      }
    }

    .title {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      font-size: 2.5rem;
      font-weight: bold;
      margin: 0 0 10px 0;
      // text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    }

    .subtitle {
      font-size: 1.1rem;
      opacity: 0.9;
      margin: 0;
    }
  }

  .gallery-container {
    max-width: 1400px;
    margin: 0 auto;
    height: calc(100vh - 180px);
    overflow-y: auto;
    scrollbar-width: thin;
    scrollbar-color: rgba(255, 255, 255, 0.3) transparent;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--borderColor);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb:hover {
      background: var(--textColor3);
    }
  }

  .image-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
    padding: 0 10px;
  }

  .image-card {
    background: var(--bgColor2);
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    cursor: pointer;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    }

    .image-wrapper {
      position: relative;
      width: 100%;
      height: 200px;
      overflow: hidden;

      .gallery-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;
      }

      &:hover .gallery-image {
        transform: scale(1.05);
      }

      .image-loading {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--bgColor3);

        .loading-icon {
          font-size: 24px;
          color: #999;
          animation: spin 1s linear infinite;
        }
      }
    }

    .image-info {
      padding: 16px;

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;

        .user-avatar {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          object-fit: cover;
        }

        .user-name {
          font-weight: 500;
          color: var(--textColor1);
          font-size: 14px;
        }
      }

      .prompt-text {
        color: var(--textColor2);
        font-size: 13px;
        line-height: 1.4;
        margin-bottom: 10px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .create-time {
        display: flex;
        align-items: center;
        gap: 4px;
        color: var(--textColor3);
        font-size: 12px;

        .el-icon {
          font-size: 14px;
        }
      }
    }
  }

  .loading-more,
  .no-more-data {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 40px 20px;
    color: var(--textColor2);
    font-size: 14px;

    .loading-icon {
      animation: spin 1s linear infinite;
    }
  }

  .empty-state {
    text-align: center;
    color: var(--textColor2);
    padding: 80px 20px;

    .empty-icon {
      font-size: 64px;
      margin-bottom: 20px;
      opacity: 0.7;
    }

    h3 {
      font-size: 24px;
      margin: 0 0 10px 0;
    }

    p {
      font-size: 16px;
      opacity: 0.8;
      margin: 0;
    }
  }
}

// 图片预览对话框样式
:deep(.image-preview-dialog) {
  .el-dialog {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    max-height: 90vh; // 限制对话框最大高度
    display: flex;
    flex-direction: column;
  }

  .el-dialog__header {
    padding: 0;
    margin: 0;
    border-bottom: 1px solid var(--borderColor);
  }

  .el-dialog__body {
    padding: 0;
    max-height: 85vh;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  .custom-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: var(--themeColor1);
    color: var(--themeTextColor);
    flex-shrink: 0; // 防止头部被压缩

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .dialog-icon {
        font-size: 24px;
      }

      .dialog-title {
        font-size: 18px;
        font-weight: 600;
      }
    }

    .close-btn {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      color: var(--themeTextColor);

      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }

  .preview-content {
    display: flex;
    flex: 1;
    min-height: 0; // 防止 flex 子项溢出
    max-height: calc(85vh - 60px); // 减去头部高度
  }

  .preview-left {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: var(--bgColor3);
    min-width: 0; // 防止 flex 子项溢出

    .image-container {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
      min-height: 0; // 防止 flex 子项溢出
      overflow: hidden; // 确保内容不会溢出

      .preview-image {
        // max-height: 100%;
        width: auto;
        height: 100%;
        object-fit: contain; // 确保图片完整显示
        border-radius: 12px;
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
        cursor: pointer;
        transition: transform 0.3s ease;

        &:hover {
          transform: scale(1.02);
        }
      }

      .image-error {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        color: var(--textColor3);
        font-size: 16px;

        .el-icon {
          font-size: 48px;
        }
      }
    }

    .image-actions {
      padding: 20px;
      display: flex;
      gap: 12px;
      justify-content: center;
      background: var(--bgColor2);
      border-top: 1px solid var(--borderColor);

      .el-button {
        flex: 1;
        max-width: 120px;
      }
    }
  }

  .preview-right {
    flex: 0 0 380px;
    background: var(--bgColor2);
    overflow-y: auto;
    border-left: 1px solid var(--borderColor);
    max-height: 100%; // 确保不超出父容器

    .creator-section {
      padding: 24px;
      border-bottom: 1px solid var(--borderColor);

      .creator-header {
        display: flex;
        align-items: center;
        gap: 16px;

        .creator-avatar {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          object-fit: cover;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .creator-details {
          flex: 1;

          .creator-name {
            margin: 0 0 4px 0;
            font-size: 16px;
            font-weight: 600;
            color: var(--textColor1);
          }

          .creator-time {
            margin: 0;
            display: flex;
            align-items: center;
            gap: 4px;
            color: var(--textColor2);
            font-size: 13px;

            .el-icon {
              font-size: 14px;
            }
          }
        }
      }
    }

    .artwork-info {
      .info-card {
        padding: 20px 24px;
        border-bottom: 1px solid var(--borderColor);

        &:last-child {
          border-bottom: none;
        }

        .info-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 16px;
          font-weight: 600;
          color: var(--textColor1);
          font-size: 15px;

          .el-icon {
            font-size: 18px;
            color: var(--themeColor1);
          }
        }

        .prompt-content {
          .prompt-text {
            color: var(--textColor2);
            line-height: 1.6;
            margin: 0 0 12px 0;
            padding: 16px;
            background: var(--bgColor3);
            border-radius: 8px;
            border-left: 4px solid var(--themeColor1);
          }

          .copy-btn {
            padding: 4px 8px;
            color: var(--themeColor1);

            &:hover {
              background: var(--bgColor3);
            }
          }
        }

        .params-grid {
          display: grid;
          gap: 12px;

          .param-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;

            label {
              font-weight: 500;
              color: var(--textColor2);
            }

            span {
              color: var(--textColor1);
            }

            .el-rate {
              --el-rate-height: 16px;
            }
          }
        }

        .interaction-buttons {
          display: flex;
          gap: 12px;

          .interaction-btn {
            flex: 1;
            height: 40px;
          }
        }
      }

      .interaction-card {
        background: linear-gradient(135deg, var(--bgColor1) 0%, var(--bgColor3) 100%);
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .drawing-gallery {
    padding: 10px;

    .gallery-header {
      margin-bottom: 20px;
      
      .title {
        font-size: 1.8rem;
      }

      .refresh-btn {
        width: 40px;
        height: 40px;
        font-size: 16px;
        top: 0;
        transform: none;
      }
    }

    .image-grid {
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 15px;
    }

    .gallery-container {
      height: calc(100vh - 160px);
    }
  }

  :deep(.image-preview-dialog) {
    .el-dialog {
      width: 95% !important;
      margin: 2vh auto;
      max-height: 96vh; // 移动端限制高度
    }

    .preview-content {
      flex-direction: column;
      max-height: calc(96vh - 60px); // 减去头部高度
    }

    .preview-left {
      .image-container {
        padding: 15px;
        max-height: 40vh; // 移动端限制图片容器高度
      }

      .image-actions {
        padding: 15px;
        gap: 8px;
        flex-shrink: 0; // 防止按钮区域被压缩

        .el-button {
          max-width: none;
          font-size: 12px;
        }
      }
    }

    .preview-right {
      flex: none;
      border-left: none;
      border-top: 1px solid var(--borderColor);
      max-height: 50vh;

      .creator-section,
      .info-card {
        padding: 16px 20px;
      }

      .artwork-info .info-card .params-grid {
        grid-template-columns: 1fr 1fr;
        gap: 8px;

        .param-item {
          flex-direction: column;
          align-items: flex-start;
          padding: 4px 0;
          gap: 4px;
        }
      }

      .interaction-buttons {
        flex-direction: column;
        gap: 8px;
      }
    }
  }
}

@media (max-width: 480px) {
  .drawing-gallery {
    .image-grid {
      grid-template-columns: 1fr;
    }
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
