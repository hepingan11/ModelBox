<template>
  <div class="video-generate-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><VideoPlay /></el-icon>
        AI 视频生成
      </h1>
      <p class="page-description">
        通过AI技术，将您的创意想法转化为精彩的视频内容
      </p>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 生成表单 -->
      <el-card class="generate-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">创建视频生成任务</span>
            <el-tag type="info" size="small">AI 驱动</el-tag>
          </div>
        </template>

        <el-form 
          ref="generateFormRef"
          :model="generateForm" 
          :rules="formRules"
          label-position="top"
          class="generate-form"
        >
          <!-- 提示词输入 -->
          <el-form-item label="视频描述提示词" prop="prompt" class="prompt-item">
            <el-input
              v-model="generateForm.prompt"
              type="textarea"
              :rows="6"
              placeholder="请详细描述您想要生成的视频内容...&#10;例如：一只可爱的橘猫在阳光明媚的花园里追逐蝴蝶，画面温馨唯美，4K高清画质"
              show-word-limit
              :maxlength="500"
              resize="none"
              class="prompt-input"
            />
            <div class="prompt-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>提示词越详细，生成的视频效果越好。建议包含场景、人物、动作、风格等元素。</span>
            </div>
          </el-form-item>

          <!-- 参考图片上传 -->
          <el-form-item label="参考图片（可选）" class="upload-item">
            <span>上传的图片加上限制：不能小于300像素,格式推荐jpg,png</span>
            <el-upload
              ref="uploadRef"
              :class="['image-upload', { 'has-image': previewUrl }]"
              drag
              :limit="1"
              :before-upload="beforeUpload"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :auto-upload="false"
              accept="image/*"
            >
              <!-- 图片预览状态 -->
              <div v-if="previewUrl" class="image-preview">
                <img :src="previewUrl" alt="预览图片" />
                <div class="preview-overlay">
                  <el-button @click="removeImage" type="danger" size="small" circle>
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              
              <!-- 上传区域 -->
              <div v-else class="upload-content">
                <el-icon class="upload-icon">
                  <Plus />
                </el-icon>
                <div class="upload-text">
                  <div class="upload-title">
                    点击或拖拽上传参考图片
                  </div>
                  <div class="upload-hint">
                    支持 JPG、PNG、GIF 格式，大小不超过 10MB
                  </div>
                </div>
              </div>
            </el-upload>
          </el-form-item>

          

          <!-- 生成按钮 -->
          <el-form-item class="submit-item">
            <el-button 
              type="primary" 
              size="large"
              :loading="generating"
              @click="generateVideo"
              class="generate-btn"
            >
              <el-icon v-if="!generating"><VideoPlay /></el-icon>
              <el-icon v-else><Loading /></el-icon>
              {{ generating ? '正在创建任务...' : '开始生成视频' }}
            </el-button>
            
            <div class="generate-tips">
              <el-icon><Clock /></el-icon>
              <span>视频生成通常需要 3-10 分钟，您可以在视频列表中查看进度</span>
              <p>| 生成一次视频需要50IT币，请确保您的账户有足够的余额</p>
            </div>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 侧边栏信息 -->
      <div class="sidebar">
        <!-- 使用提示 -->
        <el-card class="tips-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><QuestionFilled /></el-icon>
              <span>使用提示</span>
            </div>
          </template>
          
          <div class="tips-content">
            <div class="tip-item">
              <el-icon class="tip-icon"><Edit /></el-icon>
              <div class="tip-text">
                <strong>详细描述</strong>
                <p>提供具体的场景、人物、动作描述</p>
              </div>
            </div>
            
            <div class="tip-item">
              <el-icon class="tip-icon"><Picture /></el-icon>
              <div class="tip-text">
                <strong>参考图片</strong>
                <p>上传图片可以帮助AI更好地理解您的需求</p>
              </div>
            </div>
            
            <div class="tip-item">
              <el-icon class="tip-icon"><Timer /></el-icon>
              <div class="tip-text">
                <strong>耐心等待</strong>
                <p>AI生成需要时间，请耐心等待处理完成</p>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 示例提示词 -->
        <el-card class="examples-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Lightbulb /></el-icon>
              <span>示例提示词</span>
            </div>
          </template>
          
          <div class="examples-content">
            <div 
              v-for="(example, index) in promptExamples" 
              :key="index"
              class="example-item"
              @click="useExample(example.prompt)"
            >
              <div class="example-title">{{ example.title }}</div>
              <div class="example-prompt">{{ example.prompt }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 成功对话框 -->
    <el-dialog
      v-model="successDialogVisible"
      title="任务创建成功"
      width="400px"
      align-center
    >
      <div class="success-content">
        <el-icon class="success-icon"><SuccessFilled /></el-icon>
        <p class="success-text">视频生成任务已成功创建！</p>
        <p class="task-id">任务ID: {{ taskId }}</p>
        <p class="success-hint">您可以在视频列表中查看生成进度</p>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="successDialogVisible = false">继续创建</el-button>
          <el-button type="primary" @click="goToVideoList">查看列表</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  VideoPlay, Plus, Picture, Delete, Loading, Clock, Edit, 
  Timer, QuestionFilled, Lightbulb, InfoFilled, SuccessFilled 
} from '@element-plus/icons-vue'
import { createVideoTask } from '../../api/BSideApi'

// 路由
const router = useRouter()

// 表单引用
const generateFormRef = ref()
const uploadRef = ref()

// 响应式数据
const generating = ref(false)
const uploadedFile = ref(null)
const previewUrl = ref('')
const successDialogVisible = ref(false)
const taskId = ref('')

// 表单数据
const generateForm = reactive({
  prompt: '',
  duration: 'medium',
  style: 'realistic',
  quality: 'hd'
})

// 表单验证规则
const formRules = {
  prompt: [
    { required: true, message: '请输入视频描述提示词', trigger: 'blur' },
    { min: 5, message: '提示词至少需要5个字符', trigger: 'blur' },
    { max: 500, message: '提示词不能超过500个字符', trigger: 'blur' }
  ]
}

// 示例提示词
const promptExamples = [
  {
    title: '自然风景',
    prompt: '清晨的山谷里，薄雾缭绕，金色的阳光穿过云层洒向大地，鸟儿在树林间飞翔，画面宁静优美'
  },
  {
    title: '城市夜景',
    prompt: '繁华都市的夜晚，霓虹灯闪烁，车流如织，从高楼俯视整个城市，科技感十足'
  },
  {
    title: '动物世界',
    prompt: '非洲草原上，一群斑马在夕阳下奔跑，画面充满野性美，4K超清画质'
  },
  {
    title: '美食制作',
    prompt: '专业厨师正在制作精美的甜点，手法娴熟，食材新鲜，特写镜头，令人垂涎欲滴'
  },
  {
    title: '游戏场景',
    prompt: '游戏场景，玩家在游戏中战斗，画面流畅，特效炫酷，16:9超清画质'
  }
]

// 文件上传处理
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传图片大小不能超过 10MB!')
    return false
  }
  return false // 阻止自动上传
}

const handleFileChange = (file) => {
  uploadedFile.value = file.raw
  
  // 创建预览URL
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleFileRemove = () => {
  uploadedFile.value = null
  previewUrl.value = ''
}

const removeImage = () => {
  uploadRef.value.clearFiles()
  handleFileRemove()
}

// 使用示例提示词
const useExample = (prompt) => {
  generateForm.prompt = prompt
}

// 生成视频
const generateVideo = async () => {
  if (!generateFormRef.value) return
  
  try {
    // 验证表单
    await generateFormRef.value.validate()
    
    generating.value = true
    
    // 调用API创建任务
    const response = await createVideoTask(generateForm.prompt, uploadedFile.value)
    
    if (response) {
      taskId.value = response
      successDialogVisible.value = true
      
      // 重置表单
      generateForm.prompt = ''
      generateForm.duration = 'medium'
      generateForm.style = 'realistic'
      generateForm.quality = 'hd'
      
      // 清除上传的文件
      if (uploadRef.value) {
        uploadRef.value.clearFiles()
      }
      handleFileRemove()
      
      ElNotification({
        title: '任务创建成功',
        message: '视频生成任务已开始处理，请在视频列表中查看进度',
        type: 'success'
      })
    } else {
      throw new Error('创建任务失败')
    }
  } catch (error) {
    console.error('生成视频失败:', error)
    ElMessage.error('创建任务失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

// 跳转到视频列表
const goToVideoList = () => {
  successDialogVisible.value = false
  router.push('/video')
}

// 页面加载
onMounted(() => {
  // 页面初始化逻辑
})
</script>



<style scoped>
.video-generate-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  color: white;
}

.page-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.page-title .el-icon {
  font-size: 3rem;
}

.page-description {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 30px;
  max-width: 1400px;
  margin: 0 auto;
  padding-bottom: 50px;
}

/* 生成卡片 */
.generate-card {
  border-radius: 15px;
  overflow: visible;
  height: fit-content;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 1.2rem;
  font-weight: bold;
}

.generate-form {
  padding: 10px 0;
}

/* 提示词输入 */
.prompt-item {
  margin-bottom: 30px;
}

.prompt-input {
  margin-bottom: 10px;
}

.prompt-input :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
  font-size: 14px;
  line-height: 1.6;
}

.prompt-input :deep(.el-textarea__inner):focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.prompt-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 13px;
  margin-top: 5px;
}

.prompt-tips .el-icon {
  color: #409eff;
}

/* 文件上传 */
.upload-item {
  margin-bottom: 30px;
}

.image-upload {
  width: 100%;
}

.image-upload :deep(.el-upload-dragger) {
  border: 2px dashed #dcdfe6;
  border-radius: 15px;
  background: #fafafa;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  min-height: 200px;
}

.image-upload :deep(.el-upload-dragger):hover {
  border-color: #409eff;
  background: #f0f9ff;
}

/* 有图片时的样式 */
.image-upload.has-image :deep(.el-upload-dragger) {
  border: 2px solid #409eff;
  background: transparent;
}

.image-upload.has-image :deep(.el-upload-dragger):hover {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.upload-content {
  padding: 40px 20px;
  text-align: center;
}

.upload-icon {
  font-size: 3rem;
  color: #c0c4cc;
  margin-bottom: 15px;
}

.upload-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 5px;
}

.upload-hint {
  font-size: 13px;
  color: #909399;
}

.image-preview {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 10px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
}

.preview-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-preview:hover .preview-overlay {
  opacity: 1;
}

/* 参数设置 */
.params-item {
  margin-bottom: 30px;
}

.params-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.param-item label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.param-item .el-select {
  width: 100%;
}

/* 生成按钮 */
.submit-item {
  text-align: center;
  margin-bottom: 0;
}

.generate-btn {
  padding: 15px 40px;
  font-size: 16px;
  border-radius: 25px;
  background: linear-gradient(45deg, #409eff, #36d1dc);
  border: none;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.generate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.generate-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  font-size: 13px;
  margin-top: 15px;
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: fit-content;
}

.tips-card,
.examples-card {
  border-radius: 15px;
  overflow: hidden;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.tip-icon {
  font-size: 20px;
  color: #409eff;
  margin-top: 2px;
}

.tip-text strong {
  display: block;
  color: #303133;
  margin-bottom: 5px;
}

.tip-text p {
  color: #606266;
  font-size: 13px;
  margin: 0;
  line-height: 1.5;
}

/* 示例提示词 */
.examples-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.example-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.example-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
  transform: translateY(-2px);
}

.example-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
  font-size: 14px;
}

.example-prompt {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

/* 成功对话框 */
.success-content {
  text-align: center;
  padding: 20px;
}

.success-icon {
  font-size: 4rem;
  color: #67c23a;
  margin-bottom: 20px;
}

.success-text {
  font-size: 18px;
  color: #303133;
  margin-bottom: 10px;
}

.task-id {
  font-size: 14px;
  color: #409eff;
  font-weight: bold;
  margin-bottom: 15px;
}

.success-hint {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .sidebar {
    order: -1;
  }
}

@media (max-width: 768px) {
  .video-generate-container {
    padding: 15px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .params-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .generate-btn {
    padding: 12px 30px;
    font-size: 14px;
  }
}
</style>