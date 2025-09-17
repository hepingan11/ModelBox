<template>
  <div class="video-page-wrapper">
    <div class="video-container">
      <!-- 页面标题部分 -->
      <div class="header-section">
        <div class="title-content">
          <h1 data-text="AI视频创作">AI视频创作</h1>
          <p class="subtitle">
            {{ activeTab === 'private' ? '查看您的AI视频生成历史，管理您的创意作品' : '发现社区的精彩AI视频作品' }}
          </p>
        </div>
        
        <div class="header-actions">
          <el-button
            type="primary"
            size="large"
            @click="refreshList"
            :loading="refreshing"
            class="refresh-btn"
          >
            <el-icon><Refresh /></el-icon>
            刷新列表
          </el-button>
          
          <el-button
            type="success"
            size="large"
            @click="goToVideoGenerate"
            class="create-btn"
          >
            <el-icon><VideoPlay /></el-icon>
            创建新视频
          </el-button>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <div class="stat-card">
          <div class="stat-value">{{ totalVideos }}</div>
          <div class="stat-label">总视频数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ runningCount }}</div>
          <div class="stat-label">生成中</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ succeededCount }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ failedCount }}</div>
          <div class="stat-label">生成失败</div>
        </div>
      </div>

      <!-- 选项卡切换 -->
      <div class="tabs-section">
        <div class="custom-tabs">
          <button 
            class="tab-btn" 
            :class="{ active: activeTab === 'private' }"
            @click="switchTab('private')"
          >
            <el-icon><Lock /></el-icon>
            我的视频
          </button>
          <button 
            class="tab-btn" 
            :class="{ active: activeTab === 'public' }"
            @click="switchTab('public')"
          >
            <el-icon><View /></el-icon>
            公共视频
          </button>
        </div>
      </div>

      <!-- 视频列表 -->
      <div class="video-list-section">
        <div v-if="loading && videoList.length === 0" class="loading-state">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>加载视频列表中...</span>
        </div>

        <div v-else-if="videoList.length === 0" class="empty-state">
          <el-empty 
            :description="activeTab === 'private' ? '暂无视频作品' : '暂无公共视频'" 
            :image-size="200"
          >
            <template #description>
              <p v-if="activeTab === 'private'">还没有创建任何AI视频，快去创建第一个吧！</p>
              <p v-else>目前还没有公开的视频作品</p>
            </template>
            <el-button 
              v-if="activeTab === 'private'" 
              type="primary" 
              @click="goToVideoGenerate"
            >
              <el-icon><VideoPlay /></el-icon>
              立即创建
            </el-button>
          </el-empty>
        </div>

        <div v-else class="video-grid">
          <div 
            v-for="video in videoList" 
            :key="video.videoId"
            class="video-card"
            :class="{ 'processing': video.status === 'running' }"
          >
            <!-- 视频封面/预览区域 -->
            <div class="video-preview">
              <div v-if="video.status === 'running'" class="processing-overlay">
                <div class="processing-content">
                  <el-icon class="processing-icon"><Loading /></el-icon>
                  <span class="processing-text">AI正在生成中...</span>
                  <div class="processing-dots">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>

              <div v-else-if="video.status === 'failed'" class="failed-overlay">
                <div class="failed-content">
                  <el-icon class="failed-icon"><Warning /></el-icon>
                  <span class="failed-text">生成失败</span>
                  <el-button size="small" type="primary" @click="retryGenerate(video)">
                    重新生成
                  </el-button>
                </div>
              </div>

              <div v-else-if="video.status === 'succeeded'" class="video-content">
                <video 
                  v-if="video.videoUrl"
                  :poster="video.coverUrl"
                  controls
                  preload="metadata"
                  class="video-player"
                  @click.stop
                >
                  <source :src="imageUrl+video.videoUrl" type="video/mp4">
                  您的浏览器不支持视频播放
                </video>
                
                <img 
                  v-else-if="video.coverUrl"
                  :src="video.coverUrl" 
                  alt="视频封面"
                  class="video-cover"
                />
                
                <div v-else class="placeholder-cover">
                  <el-icon><VideoPlay /></el-icon>
                </div>

                <!-- 详情按钮覆盖层 -->
                <div class="detail-overlay" @click="showVideoDetail(video)">
                  <el-icon class="detail-icon"><InfoFilled /></el-icon>
                  <span class="detail-text">查看详情</span>
                </div>
              </div>

              <!-- 提示图片 -->
              <img 
                v-if="video.imgUrl" 
                :src="imageUrl + video.imgUrl" 
                alt="提示图片"
                class="prompt-image"
              />
            </div>

            <!-- 视频信息 -->
            <div class="video-info">
              <div class="video-prompt">
                <h3 class="prompt-title">创作提示</h3>
                <p class="prompt-text">{{ video.prompt || '无提示词' }}</p>
              </div>

              <div class="video-meta">
                <div class="meta-item">
                  <el-tag 
                    :type="getStatusType(video.status)" 
                    size="small"
                    class="status-tag"
                  >
                    {{ getStatusText(video.status) }}
                  </el-tag>
                </div>

                <!-- 显示公开状态标签 -->
                <div v-if="activeTab === 'private' && video.status === 'succeeded'" class="meta-item">
                  <el-tag 
                    :type="video.isPublic ? 'success' : 'info'" 
                    size="small"
                    class="public-tag"
                  >
                    <el-icon><Share v-if="video.isPublic" /><Lock v-else /></el-icon>
                    {{ video.isPublic ? '公开' : '私有' }}
                  </el-tag>
                </div>
                
                <div class="meta-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(video.createdTime) }}</span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="video-actions">
                <el-button 
                  v-if="video.status === 'succeeded'"
                  size="small" 
                  type="info"
                  @click="showVideoDetail(video)"
                >
                  <el-icon><InfoFilled /></el-icon>
                  详情
                </el-button>

                <el-button 
                  v-if="video.status === 'succeeded' && video.videoUrl"
                  size="small" 
                  type="primary"
                  @click="downloadVideo(video)"
                >
                  <el-icon><Download /></el-icon>
                  下载
                </el-button>

                <!-- 只在私有视频列表显示公开状态切换按钮 -->
                <el-button 
                  v-if="activeTab === 'private' && video.status === 'succeeded'"
                  size="small" 
                  :type="video.isPublic ? 'warning' : 'success'"
                  @click="togglePublicStatus(video)"
                  :loading="video.toggleLoading"
                >
                  <el-icon><Share /></el-icon>
                  {{ video.isPublic ? '设为私有' : '设为公开' }}
                </el-button>
                
                <!-- 只在私有视频列表显示删除按钮 -->
                <el-button 
                  v-if="activeTab === 'private'"
                  size="small" 
                  type="danger"
                  @click="deleteVideo(video)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="loading && videoList.length > 0" class="loading-more">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>加载更多视频...</span>
        </div>

        <!-- 没有更多数据 -->
        <div v-if="noMoreData && videoList.length > 0" class="no-more-data">
          <el-icon><Check /></el-icon>
          <span>已展示所有视频</span>
        </div>

        <!-- 分页 -->
        <div v-if="videoList.length > 0" class="pagination-section">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="totalCount"
            layout="prev, pager, next, jumper"
            @current-change="handlePageChange"
            class="custom-pagination"
          />
        </div>
      </div>

      <!-- 视频详情弹窗 -->
      <div v-if="detailDialogVisible" class="custom-modal-overlay" @click="closeModalOnOverlay">
        <div class="custom-modal" @click.stop>
          <!-- 弹窗头部 -->
          <div class="modal-header">
            <div class="modal-title">
              <el-icon class="title-icon"><VideoPlay /></el-icon>
              <span>视频详情信息</span>
            </div>
            <button class="close-btn" @click="detailDialogVisible = false">
              <el-icon><Close /></el-icon>
            </button>
          </div>

          <!-- 弹窗内容 -->
          <div class="modal-body">
            <div v-if="loadingDetail" class="loading-detail">
              <el-icon class="loading-icon"><Loading /></el-icon>
              <span>加载详情中...</span>
            </div>

            <div v-else-if="videoDetail" class="detail-content">
              <!-- 视频预览区域 -->
              <div v-if="currentVideo?.videoUrl" class="preview-section">
                <div class="video-preview-container">
                  <video 
                    :src="imageUrl+currentVideo.videoUrl"
                    :poster="currentVideo.coverUrl"
                    controls
                    preload="metadata"
                    class="preview-video"
                  >
                    您的浏览器不支持视频播放
                  </video>
                </div>
              </div>

              <!-- 信息栅格 -->
              <div class="info-grid">
                <!-- 基本信息 -->
                <div class="info-card">
                  <div class="card-header">
                    <el-icon class="card-icon"><Document /></el-icon>
                    <h3 class="card-title">基本信息</h3>
                  </div>
                  <div class="card-content">
                    <div class="info-item">
                      <span class="label">视频ID</span>
                      <span class="value">{{ videoDetail.videoId }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">详情ID</span>
                      <span class="value">{{ videoDetail.videoDetailId }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">使用模型</span>
                      <span class="value">{{ videoDetail.model || '未知' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">种子值</span>
                      <span class="value">{{ videoDetail.seed || '随机' }}</span>
                    </div>
                  </div>
                </div>

                <!-- 视频参数 -->
                <div class="info-card">
                  <div class="card-header">
                    <el-icon class="card-icon"><Setting /></el-icon>
                    <h3 class="card-title">视频参数</h3>
                  </div>
                  <div class="card-content">
                    <div class="info-item">
                      <span class="label">分辨率</span>
                      <span class="value">{{ videoDetail.resolution || '未知' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">宽高比</span>
                      <span class="value">{{ videoDetail.ratio || '未知' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">时长</span>
                      <span class="value">{{ videoDetail.duration ? `${videoDetail.duration}秒` : '未知' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">帧率</span>
                      <span class="value">{{ videoDetail.framespersecond ? `${videoDetail.framespersecond}fps` : '未知' }}</span>
                    </div>
                  </div>
                </div>

                <!-- 技术信息 -->
                <div class="info-card">
                  <div class="card-header">
                    <el-icon class="card-icon"><DataAnalysis /></el-icon>
                    <h3 class="card-title">技术信息</h3>
                  </div>
                  <div class="card-content">
                    <div class="info-item">
                      <span class="label">生成 Token</span>
                      <span class="value">{{ videoDetail.completion_tokens || '未知' }}</span>
                    </div>
                  </div>
                </div>

                <!-- 提示词信息 -->
                <div v-if="currentVideo?.prompt" class="info-card full-width">
                  <div class="card-header">
                    <el-icon class="card-icon"><EditPen /></el-icon>
                    <h3 class="card-title">提示词</h3>
                  </div>
                  <div class="card-content">
                    <div class="prompt-text">{{ currentVideo.prompt }}</div>
                  </div>
                </div>

                <!-- 参考图片 -->
                <div v-if="currentVideo?.imgUrl" class="info-card">
                  <div class="card-header">
                    <el-icon class="card-icon"><Picture /></el-icon>
                    <h3 class="card-title">参考图片</h3>
                  </div>
                  <div class="card-content">
                    <div class="reference-image">
                      <img :src="imageUrl+currentVideo.imgUrl" alt="参考图片">
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="detail-error">
              <el-icon class="error-icon"><Warning /></el-icon>
              <span>加载详情失败</span>
            </div>
          </div>

          <!-- 弹窗底部 -->
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="detailDialogVisible = false">
              <el-icon><Close /></el-icon>
              关闭
            </button>
            <button 
              v-if="currentVideo?.videoUrl" 
              class="btn btn-primary"
              @click="downloadVideo(currentVideo)"
            >
              <el-icon><Download /></el-icon>
              下载视频
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import { 
  Refresh, VideoPlay, Loading, Warning, Clock, Download, 
  Share, Delete, Check, InfoFilled, Close, Document, 
  Setting, DataAnalysis, EditPen, Picture, View, Lock
} from '@element-plus/icons-vue'
import { getPrivatetVideoList, deleteVideo as deleteVideoAPI, retryVideoGenerate, getVideoDetail, getPublicVideoList, toggleVideoPublic } from '../../api/BSideApi'

// 路由
const router = useRouter()

// 响应式数据
const videoList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const totalCount = ref(0)
const noMoreData = ref(false)
const activeTab = ref('public') // 当前选中的标签页

// 弹窗相关
const detailDialogVisible = ref(false)
const currentVideo = ref(null)
const videoDetail = ref(null)
const loadingDetail = ref(false)

// API调用函数已从BSideApi导入

// 计算属性 - 统计数据
const totalVideos = computed(() => videoList.value.length)
const runningCount = computed(() => 
  videoList.value.filter(v => v.status === 'running').length
)
const succeededCount = computed(() => 
  videoList.value.filter(v => v.status === 'succeeded').length
)
const failedCount = computed(() => 
  videoList.value.filter(v => v.status === 'failed').length
)

// 获取视频列表
const loadVideoList = async (page = 1, isRefresh = false, type = null) => {
  if (loading.value) return
  
  const currentType = type || activeTab.value
  loading.value = true
  
  try {
    let response
    if (currentType === 'private') {
      response = await getPrivatetVideoList(page)
    } else {
      response = await getPublicVideoList(page)
    }
    
    if (response && response.length > 0) {
      if (isRefresh || page === 1) {
        videoList.value = response
      } else {
        videoList.value.push(...response)
      }
      
      // 如果返回数据少于页面大小，说明没有更多数据
      if (response.length < pageSize.value) {
        noMoreData.value = true
      }
    } else {
      if (page === 1) {
        videoList.value = []
      }
      noMoreData.value = true
    }
  } catch (error) {
    console.error('加载视频列表失败:', error)
    ElNotification({
      title: '加载失败',
      message: '获取视频列表失败，请稍后重试',
      type: 'error'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 刷新列表
const refreshList = async () => {
  refreshing.value = true
  currentPage.value = 1
  noMoreData.value = false
  await loadVideoList(1, true)
}

// 切换选项卡
const switchTab = async (tab) => {
  if (activeTab.value === tab) return
  
  activeTab.value = tab
  currentPage.value = 1
  noMoreData.value = false
  await loadVideoList(1, true, tab)
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
  loadVideoList(page)
}

// 状态相关方法
const getStatusType = (status) => {
  switch (status) {
    case 'running': return 'warning'
    case 'succeeded': return 'success'
    case 'failed': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'running': return '生成中'
    case 'succeeded': return '已完成'
    case 'failed': return '生成失败'
    default: return '未知状态'
  }
}

// 时间格式化
const formatTime = (timeString) => {
  if (!timeString) return '未知时间'
  
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  
  return date.toLocaleDateString()
}

// 视频操作方法
const showVideoDetail = async (video) => {
  currentVideo.value = video
  detailDialogVisible.value = true
  
  // 加载视频详情
  loadingDetail.value = true
  videoDetail.value = null
  
  try {
    const detail = await getVideoDetail(video.videoId)
    videoDetail.value = detail
  } catch (error) {
    console.error('获取视频详情失败:', error)
    ElNotification({
      title: '获取失败',
      message: '获取视频详情失败，请稍后重试',
      type: 'error'
    })
  } finally {
    loadingDetail.value = false
  }
}

// 关闭弹窗方法
const closeModalOnOverlay = (event) => {
  if (event.target === event.currentTarget) {
    detailDialogVisible.value = false
  }
}

const downloadVideo = (video) => {
  if (video.videoUrl) {
    const link = document.createElement('a')
    link.href = imageUrl.value+video.videoUrl
    link.download = `ai-video-${video.videoId}.mp4`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('视频下载已开始')
  }
}



const deleteVideo = async (video) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个视频吗？删除后无法恢复。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 调用删除API
    await deleteVideoAPI(video.videoId)
    
    // 从列表中移除
    const index = videoList.value.findIndex(v => v.videoId === video.videoId)
    if (index > -1) {
      videoList.value.splice(index, 1)
    }
    
    ElMessage.success('视频已删除')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const retryGenerate = async (video) => {
  try {
    await retryVideoGenerate(video.videoId)
    ElMessage.success('已重新提交生成任务')
    
    // 更新视频状态为生成中
    const index = videoList.value.findIndex(v => v.videoId === video.videoId)
    if (index > -1) {
      videoList.value[index].status = 'running'
    }
  } catch (error) {
    console.error('重新生成失败:', error)
    ElMessage.error('重新生成失败，请稍后重试')
  }
}

// 切换视频公开状态
const togglePublicStatus = async (video) => {
  const index = videoList.value.findIndex(v => v.videoId === video.videoId)
  if (index === -1) return
  
  // 设置加载状态
  videoList.value[index].toggleLoading = true
  
  try {
    await toggleVideoPublic(video.videoId)
    
    // 切换状态
    videoList.value[index].isPublic = !videoList.value[index].isPublic
    
    ElMessage.success(`视频已设为${videoList.value[index].isPublic ? '公开' : '私有'}`)
  } catch (error) {
    console.error('切换公开状态失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    videoList.value[index].toggleLoading = false
  }
}

const goToVideoGenerate = () => {
  // 跳转到视频生成页面
  router.push({ path: '/video_generate' })
}

const imageUrl = ref("");
// 页面加载
onMounted(() => {
    imageUrl.value = process.env.VUE_APP_IMAGE;
  loadVideoList(1)
})
</script>

<style lang="scss" scoped>
.video-page-wrapper {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  background-color: var(--bgColor1);
}

.video-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  font-family: var(--el-font-family);
  color: var(--textColor1);
  animation: fadeIn 0.3s ease;

  @keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
  }
}

// 标题部分
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  
  @media (max-width: 768px) {
    flex-direction: column;
    gap: 1rem;
  }

  .title-content {
    h1 {
      font-size: 3rem;
      font-weight: 800;
      margin: 0 0 0.5rem 0;
      background: linear-gradient(135deg, #667eea, #764ba2);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .subtitle {
      font-size: 1.1rem;
      color: var(--textColor2);
      margin: 0;
    }
  }

  .header-actions {
    display: flex;
    gap: 1rem;
    
    .refresh-btn, .create-btn {
      height: 44px;
      padding: 0 20px;
      border-radius: 10px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
      }
    }
  }
}

// 统计信息
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;

  .stat-card {
    background: var(--bgColor2);
    padding: 1.5rem;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    transition: transform 0.3s ease;

    &:hover {
      transform: translateY(-4px);
    }

    .stat-value {
      font-size: 2rem;
      font-weight: bold;
      color: var(--themeColor1);
      margin-bottom: 0.5rem;
    }

    .stat-label {
      color: var(--textColor2);
      font-size: 0.9rem;
    }
  }
}

// 选项卡样式
.tabs-section {
  margin-bottom: 2rem;

  .custom-tabs {
    display: flex;
    gap: 4px;
    background: var(--bgColor2);
    padding: 4px;
    border-radius: 12px;
    width: fit-content;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .tab-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 20px;
      border: none;
      background: transparent;
      color: var(--textColor2);
      border-radius: 8px;
      cursor: pointer;
      font-size: 1rem;
      font-weight: 500;
      transition: all 0.3s ease;
      
      &:hover {
        background: var(--bgColor3);
        color: var(--textColor1);
      }
      
      &.active {
        background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
        color: white;
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
        transform: translateY(-1px);
      }
      
      .el-icon {
        font-size: 1.1rem;
      }
    }
  }
}

// 视频列表
.video-list-section {
  .loading-state, .loading-more {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 3rem;
    color: var(--textColor2);

    .loading-icon {
      animation: spin 1s linear infinite;
    }
  }

  .empty-state {
    padding: 3rem;
    text-align: center;
  }

  .video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 2rem;
    margin-bottom: 2rem;
  }

  .no-more-data {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 2rem;
    color: var(--textColor3);
    font-size: 14px;
  }
}

// 视频卡片
.video-card {
  background: var(--bgColor2);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  }

  &.processing {
    border: 2px solid #e6a23c;
    box-shadow: 0 0 20px rgba(230, 162, 60, 0.2);
  }
}

// 视频预览区域
.video-preview {
  position: relative;
  width: 100%;
  height: 200px;
  background: var(--bgColor3);
  overflow: hidden;

  .video-player, .video-cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .placeholder-cover {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f5f7fa, #c3cfe2);

    .el-icon {
      font-size: 3rem;
      color: var(--textColor3);
    }
  }

  .prompt-image {
    position: absolute;
    top: 10px;
    right: 10px;
    width: 50px;
    height: 50px;
    border-radius: 8px;
    object-fit: cover;
    border: 2px solid rgba(255, 255, 255, 0.8);
  }

  .detail-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
    cursor: pointer;

    &:hover {
      opacity: 1;
    }

    .detail-icon {
      font-size: 2.5rem;
      color: white;
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
      margin-bottom: 0.5rem;
    }

    .detail-text {
      color: white;
      font-size: 1rem;
      font-weight: 600;
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
    }
  }

  // 处理中覆盖层
  .processing-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(230, 162, 60, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;

    .processing-content {
      text-align: center;
      color: white;

      .processing-icon {
        font-size: 2rem;
        margin-bottom: 1rem;
        animation: spin 2s linear infinite;
      }

      .processing-text {
        display: block;
        font-size: 1.1rem;
        font-weight: 600;
        margin-bottom: 1rem;
      }

      .processing-dots {
        display: flex;
        justify-content: center;
        gap: 0.5rem;

        span {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: white;
          animation: dotPulse 1.4s infinite ease-in-out;

          &:nth-child(1) { animation-delay: 0s; }
          &:nth-child(2) { animation-delay: 0.2s; }
          &:nth-child(3) { animation-delay: 0.4s; }
        }
      }
    }
  }

  // 失败覆盖层
  .failed-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(245, 108, 108, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;

    .failed-content {
      text-align: center;
      color: white;

      .failed-icon {
        font-size: 2rem;
        margin-bottom: 1rem;
      }

      .failed-text {
        display: block;
        font-size: 1.1rem;
        font-weight: 600;
        margin-bottom: 1rem;
      }
    }
  }
}

// 视频信息
.video-info {
  padding: 1.5rem;

  .video-prompt {
    margin-bottom: 1rem;

    .prompt-title {
      font-size: 1rem;
      font-weight: 600;
      color: var(--textColor1);
      margin: 0 0 0.5rem 0;
    }

    .prompt-text {
      font-size: 0.9rem;
      color: var(--textColor2);
      line-height: 1.5;
      margin: 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .video-meta {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1rem;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.85rem;
      color: var(--textColor3);

      .el-icon {
        font-size: 1rem;
      }
    }

    .status-tag, .public-tag {
      font-weight: 600;
      
      .el-icon {
        margin-right: 4px;
      }
    }
  }

  .video-actions {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;

    .el-button {
      flex: 1;
      min-width: 80px;
    }
  }
}

// 分页
.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 2rem;

  .custom-pagination {
    :deep(.el-pager li) {
      background: var(--bgColor2);
      color: var(--textColor2);
      border-radius: 8px;
      margin: 0 4px;
    }

    :deep(.el-pager li.is-active) {
      background: var(--themeColor1);
      color: white;
    }
  }
}

// 自定义弹窗样式
.custom-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fadeIn 0.3s ease-out;
}

.custom-modal {
  background: var(--bgColor1);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 1200px;
  max-height: 90vh;
  overflow: hidden;
  animation: slideIn 0.3s ease-out;
  border: 1px solid var(--borderColor1);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 30px;
  background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
  color: white;
  
  .modal-title {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 1.4rem;
    font-weight: 600;
    
    .title-icon {
      font-size: 1.6rem;
    }
  }
  
  .close-btn {
    background: none;
    border: none;
    color: white;
    font-size: 1.5rem;
    cursor: pointer;
    padding: 8px;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: scale(1.1);
    }
  }
}

.modal-body {
  max-height: calc(90vh - 160px);
  overflow-y: auto;
  padding: 30px;
  
  .loading-detail {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 15px;
    padding: 4rem;
    color: var(--textColor2);
    font-size: 1.1rem;

    .loading-icon {
      font-size: 2rem;
      animation: spin 1s linear infinite;
    }
  }
  
  .detail-error {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 15px;
    padding: 4rem;
    color: #f56565;
    font-size: 1.1rem;
    
    .error-icon {
      font-size: 2rem;
    }
  }
}

.preview-section {
  margin-bottom: 30px;
  
  .video-preview-container {
    display: flex;
    justify-content: center;
    background: var(--bgColor2);
    border-radius: 16px;
    padding: 20px;
    
    .preview-video {
      max-width: 600px;
      max-height: 400px;
      width: 100%;
      height: auto;
      border-radius: 12px;
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
    }
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  
  .full-width {
    grid-column: 1 / -1;
  }
}

.info-card {
  background: var(--bgColor2);
  border: 1px solid var(--borderColor1);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  }
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px 24px 16px;
    background: linear-gradient(135deg, var(--bgColor3), var(--bgColor2));
    border-bottom: 1px solid var(--borderColor1);
    
    .card-icon {
      font-size: 1.4rem;
      color: var(--themeColor1);
    }
    
    .card-title {
      font-size: 1.1rem;
      font-weight: 600;
      color: var(--textColor1);
      margin: 0;
    }
  }
  
  .card-content {
    padding: 20px 24px;
  }
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--borderColor2);
  
  &:last-child {
    border-bottom: none;
  }
  
  .label {
    font-weight: 500;
    color: var(--textColor2);
    font-size: 0.95rem;
  }
  
  .value {
    font-family: 'Fira Code', monospace;
    background: var(--bgColor3);
    color: var(--textColor1);
    padding: 6px 12px;
    border-radius: 8px;
    font-size: 0.9rem;
    border: 1px solid var(--borderColor2);
  }
}

.prompt-text {
  background: var(--bgColor3);
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid var(--themeColor1);
  line-height: 1.6;
  color: var(--textColor1);
  font-size: 1rem;
}

.reference-image {
  display: flex;
  justify-content: center;
  
  img {
    max-width: 100%;
    max-height: 300px;
    border-radius: 12px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  }
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 20px 30px;
  background: var(--bgColor2);
  border-top: 1px solid var(--borderColor1);
  
  .btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    border: none;
    border-radius: 10px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &.btn-secondary {
      background: var(--bgColor3);
      color: var(--textColor2);
      border: 1px solid var(--borderColor1);
      
      &:hover {
        background: var(--bgColor1);
        color: var(--textColor1);
        transform: translateY(-1px);
      }
    }
    
    &.btn-primary {
      background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
      color: white;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
      }
    }
  }
}

// 动画
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from { 
    opacity: 0;
    transform: scale(0.95) translateY(-20px);
  }
  to { 
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// 响应式适配
@media (max-width: 768px) {
  .custom-modal {
    width: 95%;
    max-height: 95vh;
  }
  
  .modal-header {
    padding: 20px;
    
    .modal-title {
      font-size: 1.2rem;
    }
  }
  
  .modal-body {
    padding: 20px;
    max-height: calc(95vh - 140px);
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .preview-section .video-preview-container .preview-video {
    max-width: 100%;
    max-height: 250px;
  }
  
  .modal-footer {
    padding: 16px 20px;
    
    .btn {
      padding: 10px 20px;
      font-size: 0.9rem;
    }
  }
}

// 动画
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes dotPulse {
  0%, 100% {
    transform: scale(0.3);
    opacity: 0.3;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .video-container {
    padding: 1rem;
  }

  .header-section .title-content h1 {
    font-size: 2rem;
  }

  .video-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>