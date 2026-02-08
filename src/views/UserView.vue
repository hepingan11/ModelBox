<template>
  <div class="body">
    <div class="user-container">
      <div class="user-card">
        <!-- 头像和用户名部分 -->
        <div class="user-header">
          <el-upload
            class="avatar-uploader"
            :action="null"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleAvatarChange"
            accept="image/*"
          >
            <div class="avatar-container">
              <el-avatar shape="circle" :size="120" class="user-avatar">
                <el-image :src="imageUrl+userinfo.avatar" fit="cover">
                  <template #error>
                    <div class="avatar-placeholder">
                      <el-icon><UserFilled /></el-icon>
                    </div>
                  </template>
                </el-image>
              </el-avatar>
              <div class="avatar-hover-mask">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
              <div v-if="uploading" class="upload-loading">
                <el-icon class="rotating"><Loading /></el-icon>
                <span>上传中...</span>
              </div>
            </div>
          </el-upload>
          
          <div class="user-name-section">
            <div v-if="editNameStaus === 1" class="edit-name">
              <el-input 
                v-model="userinfo.userName" 
                class="name-input"
                placeholder="请输入新昵称" 
              />
              <el-button type="primary" @click="savaName(userinfo.userName)" class="save-btn">
                保存
              </el-button>
            </div>
            <div v-else class="display-name">
              <h2>{{userinfo.userName}}</h2>
              <el-button @click="setNameStaus" class="edit-btn" color="var(--themeColor1)">
                <el-icon :size="16"><Edit /></el-icon>
                修改昵称
              </el-button>
            </div>
          </div>
        </div>

        <!-- IT币信息 -->
        <div class="balance-section">
          <div class="balance-card">
            <div class="balance-header">
              <h3>剩余IT币</h3>
              <el-button
                class="refresh-btn"
                :icon="Refresh"
                circle
                @click="getUser"
                :loading="isRefreshing"
              />
            </div>
            <div class="balance-amount">{{userinfo.frequency}}</div>
          </div>
        </div>

        <!-- 功能按钮组 -->
        <div class="action-buttons">
          <el-button @click="router().push({ path: '/purchase' })" type="primary" class="action-btn">
            <el-icon><Plus /></el-icon>充值
          </el-button>
          <el-button @click="router().push({ path: '/Collection' })" type="primary" class="action-btn">
            <el-icon><Star /></el-icon>我的收藏
          </el-button>
          
        </div>

        <!-- 绘画展示区 -->
        <div v-if="showParam === 1" class="gallery-section">
          <h3 style="color: var(--textColor1);">我的绘画作品</h3>
          <div class="image-grid">
            <div v-for="item in imageList" :key="item.drawingId || item.id" class="image-item hover-scale">
              <!-- 公开状态标签 -->
              <div class="image-status">
                <el-tag 
                  :type="item.isPublic ? 'success' : 'info'" 
                  size="small"
                  class="status-tag"
                >
                  <el-icon>
                    <Globe v-if="item.isPublic" />
                    <Lock v-else />
                  </el-icon>
                  {{ item.isPublic ? '公开' : '私有' }}
                </el-tag>
              </div>
              
              <!-- 图片 -->
              <el-image 
                class="gallery-image"
                :src="imageUrl+item.generateUrl" 
                fit="cover" 
                :preview-src-list="[imageUrl+item.generateUrl]"
                :preview-teleported="true"
                :z-index="9999"
                lazy
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>加载失败</span>
                  </div>
                </template>
                <template #placeholder>
                  <div class="image-loading">
                    <el-icon class="rotating"><Loading /></el-icon>
                  </div>
                </template>
              </el-image>
              
              <!-- 操作按钮 -->
              <div class="image-actions">
                <el-button 
                  :type="item.isPublic ? 'warning' : 'success'"
                  size="small"
                  @click="toggleImagePublic(item)"
                  :loading="item.toggleLoading"
                  class="toggle-btn"
                >
                  <el-icon>
                    <Lock v-if="item.isPublic" />
                    <Share v-else />
                  </el-icon>
                  {{ item.isPublic ? '设为私有' : '设为公开' }}
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="!loading && imageList.length === 0" class="empty-state">
            <el-icon class="empty-icon"><Picture /></el-icon>
            <h3>暂无作品</h3>
            <p>还没有创作任何AI绘画作品，快去创作第一个吧！</p>
          </div>

          <!-- 分页控件 -->
          <div class="pagination-container" v-if="imageList.length > 0 || currentPage > 1">
             <el-pagination
              background
              layout="prev, pager, next"
              :current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              @current-change="handleCurrentChange"
              :disabled="loading"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import store from "@/store";
import {h, onMounted, onUnmounted, ref, watch} from "vue";
import {getUserDraw, GetUserInfo, updateUserName, UpdateUserAvatar, toggleDrawPublic} from "../../api/BSideApi";
import router from "@/router";
import {ElNotification, ElMessage} from "element-plus";
import { Refresh, UserFilled, Camera, Loading, CheckCircle, Picture, Plus, Star, Edit, Share, Lock, Globe } from '@element-plus/icons-vue'
import { set } from "nprogress";

export default {
  name: `UserView`,
  methods: {
    router() {
      return router
    }
  },
  computed: {
    store() {
      return store
    }
  },
  setup() {
    const userinfo = ref({
        'userName': "用户",
        "avatar": "",
        "frequency": ""
    });

    const imageList = ref([])

    const showParam = ref(0);

    const imageUrl = ref('');

    const editNameStaus = ref(0)

    const isRefreshing = ref(false)

    const uploading = ref(false)

    const currentPage = ref(1)
    const loading = ref(false)
    const noMoreData = ref(false)
    const galleryScrollbar = ref(null)

    onMounted(() => {
      imageUrl.value = process.env.VUE_APP_IMAGE;
      getImageList();
      getUser()
    })

    onUnmounted(() => {
    })

    async function getUser() {
      isRefreshing.value = true
      try {
        const res = await GetUserInfo()
        store.commit("setUserinfo", res)
        if(res.avatar != null) {
          userinfo.value.avatar = res.avatar
        }
        if (res.userName != null) {
          userinfo.value.userName = res.userName
        }
        userinfo.value.frequency = res.frequency
      } finally {
        isRefreshing.value = false
      }
    }

    const pageSize = ref(10)
    const total = ref(100) // 默认给一个值，因为接口未返回总数

    async function getImageList() {
      // 重置状态
      imageList.value = []
      currentPage.value = 1
      showParam.value = 1;
      
      // 加载数据
      await loadImages()
    }

    async function handleCurrentChange(val) {
      currentPage.value = val
      await loadImages()
      // 滚动到顶部
      const gallery = document.querySelector('.gallery-section')
      if (gallery) {
        gallery.scrollIntoView({ behavior: 'smooth' })
      }
    }

    async function loadImages() {
      loading.value = true
      try {
        const response = await getUserDraw(currentPage.value)
        
        // 适配 Mybatis-Plus Page<T> 结构
        if (response && response.records) {
          imageList.value = response.records
          total.value = Number(response.total)
          
          // 可选：同步后端返回的 size
          if (response.size) {
            pageSize.value = Number(response.size)
          }
        } 
        // 兼容旧的数组返回格式
        else if (Array.isArray(response)) {
          imageList.value = response // 替换模式而非追加
          
          if (response.length < pageSize.value) {
            total.value = (currentPage.value - 1) * pageSize.value + response.length
          } else {
             if (total.value <= currentPage.value * pageSize.value) {
                total.value = (currentPage.value + 1) * pageSize.value
             }
          }
        } else {
          imageList.value = []
          total.value = 0
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

    function setNameStaus() {
      editNameStaus.value=1;
    }

    async function savaName(userName) {
      await updateUserName(userName)
      editNameStaus.value = 0;
      ElNotification({
        title: '修改成功',
        message: h('i', {style: 'color: teal'}, 'Success'),
      })
    }

    const handleAvatarChange = async (file) => {
      if (!['image/jpeg', 'image/png', 'image/gif'].includes(file.raw.type)) {
        ElMessage.error('请上传图片文件！')
        return
      }
      
      if (file.size / 1024 / 1024 > 2) {
        ElMessage.error('图片大小不能超过2MB！')
        return
      }

      uploading.value = true
      try {
        const formData = new FormData()
        formData.append('avatar', file.raw)
        
        const res = await UpdateUserAvatar(formData)
        if (!res) {
          ElMessage.success('头像更新成功')
          store.commit('setUserinfo', {
            ...store.state.userinfo,
            avatar: res
          })
          setTimeout(() => {
            getUser()
          }, 500)
        } else {
          ElMessage.error('上传失败，请重试')
        }
      } catch (error) {
        ElMessage.error('上传失败，请重试:'+error)
      } finally {
        uploading.value = false
      }
    }

    // 切换绘画公开状态
    const toggleImagePublic = async (image) => {
      const index = imageList.value.findIndex(item => 
        (item.drawingId || item.id) === (image.drawingId || image.id)
      )
      if (index === -1) return
      
      // 设置加载状态
      imageList.value[index].toggleLoading = true
      
      try {
        await toggleDrawPublic(image.drawingId || image.id)
        
        // 切换状态
        imageList.value[index].isPublic = !imageList.value[index].isPublic
        
        ElMessage.success(`绘画作品已设为${imageList.value[index].isPublic ? '公开' : '私有'}`)
      } catch (error) {
        console.error('切换公开状态失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      } finally {
        imageList.value[index].toggleLoading = false
      }
    }

    // 预加载图片，避免预览时闪烁
    const preloadImages = () => {
      imageList.value.forEach(item => {
        const img = new Image()
        img.src = imageUrl.value + item.generateUrl
      })
    }

    // 监听图片列表变化，预加载图片
    watch(imageList, () => {
      if (imageList.value.length > 0) {
        setTimeout(preloadImages, 100)
      }
    }, { deep: true })

    return {
      userinfo,
      imageUrl,
      showParam,
      imageList,
      getImageList,
      editNameStaus,
      savaName,
      setNameStaus,
      isRefreshing,
      getUser,
      handleAvatarChange,
      uploading,
      loading,
      noMoreData,
      noMoreData,
      loadMoreImages: loadImages, // 兼容引用
      toggleImagePublic,
      pageSize,
      total,
      handleCurrentChange
    }
  }
}
</script>

<style lang="scss" scoped>
.body {
  background-color: var(--bgColor2);
  min-height: 100vh;
  padding: 20px;
}

.user-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.user-card {
  background: var(--bgColor1);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.user-header {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 30px;
}

.hover-scale {
  transition: transform 0.3s ease;
  &:hover {
    transform: scale(1.05);
  }
}

.user-name-section {
  flex: 1;
  .edit-name {
    display: flex;
    gap: 10px;
    align-items: center;
    .name-input {
      max-width: 200px;
    }
  }
  .display-name {
    h2 {
      margin: 0 0 10px 0;
      color: var(--textColor1);
    }
  }
}

.balance-section {
  margin-bottom: 30px;
  .balance-card {
    background: linear-gradient(135deg, var(--themeColor2), #4a90e2);
    border-radius: 12px;
    padding: 20px;
    color: white;

    .balance-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      h3 {
        margin: 0;
        opacity: 0.9;
      }

      .refresh-btn {
        background: rgba(255, 255, 255, 0.2);
        border: none;
        color: white;
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(255, 255, 255, 0.3);
          transform: rotate(180deg);
        }
        
        &:active {
          background: rgba(255, 255, 255, 0.4);
        }
      }
    }

    .balance-amount {
      font-size: 32px;
      font-weight: bold;
    }
  }
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
  
  .action-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    background: var(--themeColor2);
    transition: transform 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
    }
  }
}

.gallery-section {
  h3 {
    margin-bottom: 20px;
    color: var(--el-text-color-primary);
  }
  
  .image-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
    padding: 10px;
  }
  
  .image-item {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    transition: transform 0.3s ease;
    background: var(--bgColor1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    &:hover {
      transform: scale(1.03);
      
      .image-actions {
        opacity: 1;
        transform: translateY(0);
      }
    }
    
    .image-status {
      position: absolute;
      top: 10px;
      left: 10px;
      z-index: 2;
      
      .status-tag {
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(10px);
        border: none;
        font-weight: 600;
        
        .el-icon {
          margin-right: 4px;
        }
      }
    }
    
    .image-actions {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
      padding: 15px 10px 10px;
      opacity: 0;
      transform: translateY(10px);
      transition: all 0.3s ease;
      
      .toggle-btn {
        width: 100%;
        border-radius: 6px;
        font-weight: 500;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-1px);
        }
        
        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }
  
  .gallery-image {
    width: 100%;
    height: 250px;
    object-fit: cover;
    transition: none !important; /* 禁用过渡效果避免闪烁 */
    
    :deep(.el-image__inner) {
      transition: none !important;
    }
    
    :deep(.el-image__preview) {
      transition: opacity 0.3s ease !important;
    }
  }
  
  .image-loading,
  .image-error {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: var(--bgColor2);
    color: var(--textColor2);
    
    .el-icon {
      font-size: 32px;
      margin-bottom: 8px;
      opacity: 0.6;
    }
    
    span {
      font-size: 14px;
      opacity: 0.8;
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    padding-bottom: 20px;
  }

  :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
    background-color: var(--themeColor1);
  }

  .loading-more,
  .no-more-data {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 20px;
    color: var(--textColor2);
    font-size: 14px;

    .loading-icon {
      animation: spin 1s linear infinite;
    }
  }

  .empty-state {
    text-align: center;
    color: var(--textColor2);
    padding: 40px 20px;

    .empty-icon {
      font-size: 48px;
      margin-bottom: 16px;
      opacity: 0.6;
    }

    h3 {
      font-size: 18px;
      margin: 0 0 8px 0;
      color: var(--textColor1);
    }

    p {
      font-size: 14px;
      opacity: 0.8;
      margin: 0;
    }
  }
}

@media screen and (max-width: 768px) {
  .user-header {
    flex-direction: column;
    text-align: center;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
  
  .gallery-image {
    height: 200px;
  }
}

.avatar-container {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
    
    .avatar-hover-mask {
      opacity: 1;
    }
  }
}

.user-avatar {
  border: 4px solid var(--bgColor1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.avatar-hover-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  
  .el-icon {
    font-size: 24px;
    color: white;
    margin-bottom: 8px;
  }
  
  span {
    color: white;
    font-size: 14px;
  }
}

.upload-loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  
  .el-icon {
    font-size: 24px;
    color: white;
    margin-bottom: 8px;
  }
  
  span {
    color: white;
    font-size: 14px;
  }
}

.rotating {
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
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

/* Element Plus 图片预览优化 */
:deep(.el-image-viewer__mask) {
  background-color: rgba(0, 0, 0, 0.8) !important;
  backdrop-filter: blur(10px);
}

:deep(.el-image-viewer__wrapper) {
  transition: all 0.3s ease !important;
}

:deep(.el-image-viewer__img) {
  transition: transform 0.3s ease !important;
  max-width: 90vw !important;
  max-height: 90vh !important;
}

/* 禁用可能导致闪烁的动画 */
:deep(.el-image__inner) {
  transition: none !important;
}

:deep(.el-image) {
  background: var(--bgColor2);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: var(--bgColor2);
  display: flex;
  justify-content: center;
  align-items: center;
  
  .el-icon {
    font-size: 48px;
    color: var(--textColor2);
  }
}
</style>
