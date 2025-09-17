<template>
  <div class="zhipu-page-wrapper">
    <div class="zhipu-drawing-container">
      <!-- 页面标题部分 -->
      <div class="header-section" style="display: flex; align-items: center; justify-content: space-between;">
        <div style="flex:1">
          <h1 data-text="AI智能绘画">AI智能绘画</h1>
          <p class="subtitle">使用智谱AI强大的图像生成能力，将您的创意变为现实</p>
        </div>
        <div style="display: flex; gap: 16px;">
          <el-button
            type="primary"
            style="height: 48px; min-width: 160px; font-size: 18px; font-weight: 600; border-radius: 12px; box-shadow: 0 4px 16px rgba(71,118,230,0.12); background: linear-gradient(90deg, #4776E6 0%, #8E54E9 100%); color: #fff; letter-spacing: 2px;"
            @click="goToDrawing"
          >
            <el-icon style="margin-right: 8px;"><Picture /></el-icon>
              作品欣赏
          </el-button>
          
          <el-button
            type="success"
            style="height: 48px; min-width: 160px; font-size: 18px; font-weight: 600; border-radius: 12px; box-shadow: 0 4px 16px rgba(103,194,58,0.12); background: linear-gradient(90deg, #67C23A 0%, #85CE61 100%); color: #fff; letter-spacing: 2px;"
            @click="goToUserPage"
          >
            <el-icon style="margin-right: 8px;"><Star /></el-icon>
              我的绘画
          </el-button>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="drawing-main-content">
        <!-- 输入控制面板 -->
        <div class="control-panel">
          <el-form :model="drawingForm" label-position="top" class="drawing-form">
            <!-- 提示词输入区域 -->
            <div class="form-section">
              <h3 class="section-title">创作提示</h3>
              
              <!-- 图片上传区域 - 仅在选择豆包图生图模型时显示 -->
              <el-form-item v-if="drawingForm.model === 'DOUBAO_SEEDEDIT'" label="参考图片" prop="referenceImage">
                <div class="image-upload-container">
                  <el-upload
                    ref="uploadRef"
                    :show-file-list="false"
                    :before-upload="beforeImageUpload"
                    :on-change="handleImageChange"
                    :on-success="handleImageSuccess"
                    :on-error="handleImageError"
                    :auto-upload="false"
                    accept="image/*"
                    drag
                    class="image-uploader"
                  >
                    <div v-if="!uploadedImageUrl" class="upload-placeholder">
                      <el-icon class="upload-icon"><Plus /></el-icon>
                      <div class="upload-text">点击或拖拽图片到此处</div>
                      <div class="upload-hint">支持 JPG、PNG 格式，大小不超过 5MB</div>
                    </div>
                    <div v-else class="uploaded-image-preview">
                      <img :src="uploadedImageUrl" alt="参考图片" class="preview-img" />
                      <div class="image-overlay">
                        <el-button type="danger" size="small" @click.stop="removeImage" circle>
                          <el-icon><Close /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </el-upload>
                </div>
              </el-form-item>
              
              <el-form-item label="提示词描述" prop="prompt">
                <div class="input-container">
                  <el-input
                    v-model="drawingForm.prompt"
                    type="textarea"
                    :rows="5"
                    :placeholder="promptPlaceholder"
                    show-word-limit
                    style="width: 100%;"
                  />
                  <div class="input-backdrop"></div>
                </div>
                <div class="helper-text">
                  一次请求将消耗10IT币;生成的图片默认公开展示！如需设为私有，请在前往我的绘画里设置。
                </div>
              </el-form-item>
              
            </div>
            
            <!-- 生成设置 -->
            <div class="form-section">
              <h3 class="section-title">生成设置</h3>
              
              <div class="setting-row">
                <el-form-item label="AI模型选择" prop="model">
                  <el-select 
                    v-model="drawingForm.model" 
                    placeholder="选择生成模型"
                    class="full-width-select"
                  >
                    <el-option
                      v-for="item in modelOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </div>
              
              <div class="setting-row">
                <el-form-item label="图像尺寸" prop="size">
                  <el-select 
                    v-model="drawingForm.size" 
                    placeholder="选择图像尺寸"
                    class="full-width-select"
                  >
                    <el-option
                      v-for="item in sizeOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </div>
              
              <div class="setting-row" v-if="drawingForm.model === 'COGVIEW_4'">
                <el-form-item label="质量" prop="quality">
                  <el-select 
                    v-model="drawingForm.quality" 
                    placeholder="选择图像质量"
                    class="full-width-select"
                  >
                    <el-option
                      v-for="item in qualityOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </div>
            </div>
          </el-form>
          
          <!-- 生成按钮 -->
          <div class="form-actions" style="margin-top: 20px; position: sticky; bottom: 0; padding: 10px 0;">
            <el-button 
              class="reset-btn"
              @click="resetForm" 
              :disabled="generating"
            >
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
            <el-button 
              type="primary" 
              :loading="generating" 
              @click="generateImage"
              class="generate-btn"
              color="var(--themeColor1)"
            >
              <div class="generate-btn-content">
                <el-icon><Picture /></el-icon> 开始生成
              </div>
            </el-button>
          </div>
        </div>
        
        <!-- 结果展示区域 -->
        <div class="result-panel">
          <div v-if="generating" class="generating-status">
            <div class="loading-animation">
              <div class="loading-icon">
                <el-icon :size="48"><Loading /></el-icon>
              </div>
              <span class="loading-text">AI 正在绘制您的创意...</span>
              <div class="loading-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
          
          <div v-else-if="resultImages.length > 0" class="result-gallery">
            <h3 class="section-title">生成结果</h3>
            
            <!-- 单图模式 - 当只有一张图片时使用大图显示 -->
            <div v-if="resultImages.length === 1" class="single-image-display">
              <div class="result-image-container single">
                <img :src="resultImages[0]" alt="AI生成图像" class="result-image" @click="previewImage(resultImages[0])" />
                <div class="image-actions">
                  <el-button 
                    type="success" 
                    circle 
                    size="small" 
                    icon="Download"
                    @click.stop="downloadImage(resultImages[0], 0)"
                    title="下载图像"
                  ></el-button>
                  <el-button 
                    type="primary" 
                    circle 
                    size="small" 
                    icon="Star"
                    @click.stop="favoriteImage(resultImages[0])"
                    title="收藏图像"
                  ></el-button>
                </div>
              </div>
            </div>
            
            <!-- 多图模式 - 保持原有的网格布局 -->
            <div v-else class="image-grid">
              <div 
                v-for="(image, index) in resultImages" 
                :key="index"
                class="result-image-container"
                @click="previewImage(image)"
              >
                <img :src="image" alt="AI生成图像" class="result-image" />
                <div class="image-actions">
                  <el-button 
                    type="success" 
                    circle 
                    size="small" 
                    icon="Download"
                    @click.stop="downloadImage(image, index)"
                    title="下载图像"
                  ></el-button>
                  <el-button 
                    type="primary" 
                    circle 
                    size="small" 
                    icon="Star"
                    @click.stop="favoriteImage(image)"
                    title="收藏图像"
                  ></el-button>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-results">
            <el-empty description="暂无生成结果" :image-size="200">
              <template #description>
                <p>填写创作提示并点击"开始生成"按钮创建您的AI艺术作品</p>
              </template>
            </el-empty>
          </div>
        </div>
      </div>
      
      <!-- 图片预览弹窗 -->
      <el-dialog
        v-model="previewVisible"
        width="60%"
        center
        :append-to-body="true"
        :show-close="true"
        class="preview-dialog"
      >
        <div class="preview-content">
          <img v-if="previewImageUrl" :src="previewImageUrl" alt="预览图像" class="preview-image" />
        </div>
        <div class="preview-actions">
          <el-button type="success" @click="downloadImage(previewImageUrl)">
            <el-icon><Download /></el-icon> 下载图像
          </el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Picture, Loading, Download, Star, Refresh, Plus, Close } from '@element-plus/icons-vue';
import { addZhipuDrawingTask } from '../../api/BSideApi';

// 表单数据
const drawingForm = reactive({
  prompt: '',
  negative_prompt: '',
  size: '1024x1024',
  quality: 'standard',
  model: 'COGVIEW_4'
});

// 生成状态
const generating = ref(false);
const resultImages = ref([]);
const previewVisible = ref(false);
const previewImageUrl = ref('');

// 图片上传相关
const uploadRef = ref(null);
const uploadedImageUrl = ref('');
const uploadedImageFile = ref(null);

// 预设选项
const modelOptions = [
  { value: 'COGVIEW_4', label: 'CogView4文生图(推荐)' },
  // { value: 'COGVIEW_3', label: 'COGView3文生图' },
  { value: 'DOUBAO_SEEDREAM', label: '豆包文生图' },
  { value: 'DOUBAO_SEEDEDIT', label: '豆包图生图' }
];

const sizeOptions = [
  { value: '1024x1024', label: '1024x1024 (方形)' },
  { value: '1024x1792', label: '1024x1792 (竖版)' },
  { value: '1792x1024', label: '1792x1024 (横版)' },
  { value: '512x512', label: '512x512' },
  { value: '864x1152', label: '864x1152' },
  { value: '1512x648 ', label: '1512x648 ' },
  { value: '1344x768', label: '1344x768' },
  { value: '1152x864', label: '1152x864' },
  { value: '720x1440', label: '720x1440' },
  { value: '1248x832 ', label: '1248x832 ' }
];

const qualityOptions = [
  { value: 'standard', label: '标准质量' },
  { value: 'hd', label: '高清质量' }
];

// 提示词占位文本
const promptPlaceholder = ref('输入详细的图像描述，如: "一只戴着太阳镜的柴犬，坐在海滩上，日落时分，背景是金色的天空和蓝色的海洋，逼真的摄影风格"');

// 图片上传处理函数
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt10M = file.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 5MB!');
    return false;
  }

  return false; // 阻止自动上传
};

const handleImageChange = (file) => {
  if (file && file.raw) {
    // 验证文件
    const isImage = file.raw.type.startsWith('image/');
    const isLt10M = file.raw.size / 1024 / 1024 < 10;

    if (!isImage) {
      ElMessage.error('只能上传图片文件!');
      return;
    }
    if (!isLt10M) {
      ElMessage.error('图片大小不能超过 10MB!');
      return;
    }

    // 创建预览URL
    uploadedImageUrl.value = URL.createObjectURL(file.raw);
    uploadedImageFile.value = file.raw;
    
    ElMessage.success('图片上传成功');
  }
};

const handleImageSuccess = () => {
  ElMessage.success('图片上传成功');
};

const handleImageError = () => {
  ElMessage.error('图片上传失败');
};

const removeImage = () => {
  if (uploadedImageUrl.value) {
    URL.revokeObjectURL(uploadedImageUrl.value);
  }
  uploadedImageUrl.value = '';
  uploadedImageFile.value = null;
};

// 生成图像
const generateImage = async () => {
  if (!drawingForm.prompt || drawingForm.prompt.trim() === '') {
    ElMessage.warning('请输入提示词描述');
    return;
  }

  // 如果选择了豆包图生图模型，检查是否上传了图片
  if (drawingForm.model === 'DOUBAO_SEEDEDIT' && !uploadedImageFile.value) {
    ElMessage.warning('豆包图生图模型需要上传参考图片');
    return;
  }
  
  generating.value = true;
  
  try {
    // 后端使用 @RequestParam 和 @RequestPart，所以统一使用 FormData
    const requestData = new FormData();
    requestData.append('prompt', drawingForm.prompt);
    requestData.append('size', drawingForm.size);
    requestData.append('quality', drawingForm.quality);
    requestData.append('model', drawingForm.model);
    
    // 如果是豆包图生图模型且有上传图片，添加文件
    if (drawingForm.model === 'DOUBAO_SEEDEDIT' && uploadedImageFile.value) {
      requestData.append('file', uploadedImageFile.value);
    }
    
    // 添加可选参数
    if (drawingForm.negative_prompt) {
      requestData.append('negative_prompt', drawingForm.negative_prompt);
    }
    
    // 调用API
    const response = await addZhipuDrawingTask(requestData);
    
    // 处理响应 - 修改这里以适配单一URL的响应
    if (response) {
      // 检查响应格式，如果是字符串（单一URL），则转为数组格式
      if (typeof response === 'string') {
        resultImages.value = [response]; // 将单一URL包装为数组
      } else if (Array.isArray(response)) {
        resultImages.value = response; // 兼容原有数组格式
      } else {
        ElMessage.error('获取图像失败，响应格式错误');
        return;
      }
      
      ElMessage.success('图像生成成功!');
    } else {
      ElMessage.error('生成图像失败，请稍后重试');
    }
  } catch (error) {
    console.error('生成图像错误:', error);
    ElMessage.error('生成图像时出错: ' + (error.message || '未知错误'));
  } finally {
    generating.value = false;
  }
};

// 重置表单
const resetForm = () => {
  drawingForm.prompt = '';
  drawingForm.negative_prompt = '';
  drawingForm.size = '1024x1024';
  drawingForm.quality = 'standard';
  drawingForm.model = 'COGVIEW_4';
  
  // 清空图片上传
  removeImage();
  resultImages.value = [];
};

// 预览图片
const previewImage = (imageUrl) => {
  previewImageUrl.value = imageUrl;
  previewVisible.value = true;
};

// 下载图片
const downloadImage = (imageUrl, index) => {
  try {
    const link = document.createElement('a');
    link.href = imageUrl;
    link.download = `zhipu-image-${Date.now()}-${index || 0}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    ElMessage.success('图片下载已开始');
  } catch (error) {
    console.error('下载图片失败:', error);
    ElMessage.error('下载图片失败');
  }
};

// 收藏图片
const favoriteImage = (imageUrl) => {
  ElMessage.success('图片已收藏');
  // 这里可以添加收藏功能的实现
  // 比如调用保存到用户收藏夹的API等
};

// 监听模型切换，当不是豆包图生图时清空已上传的图片
watch(() => drawingForm.model, (newModel) => {
  if (newModel !== 'DOUBAO_SEEDEDIT') {
    removeImage();
  }
});

const imageUrl = ref('');
// 页面加载
onMounted(() => {
  // 页面初始化逻辑
  imageUrl.value = process.env.VUE_APP_IMAGE;
});

const router = useRouter();
function goToDrawing() {
  router.push({ path: '/drawing' });
}

function goToUserPage() {
  router.push({ path: '/user_view' });
}
</script>

<style lang="scss" scoped>
.zhipu-page-wrapper {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  background-color: var(--bgColor1);
}

.zhipu-drawing-container {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: 2rem;
  font-family: var(--el-font-family);
  color: var(--textColor1);
  background-color: var(--bgColor1);
  animation: fadeIn 0.3s ease;
  position: relative;
  min-height: 100%;
  overflow: auto;

  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }

  // 标题部分样式
  .header-section {
    text-align: center;
    margin-bottom: 2.5rem;
    
    h1 {
      font-size: 3rem;
      font-weight: 800;
      position: relative;
      display: inline-block;
      color: transparent;
      background: linear-gradient(135deg, #4776E6, #8E54E9);
      -webkit-background-clip: text;
      margin-bottom: 1rem;
      
      &::before {
        content: attr(data-text);
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(45deg, #4776E6, #8E54E9, #4776E6);
        -webkit-background-clip: text;
        background-size: 200%;
        color: transparent;
        animation: textShine 3s infinite linear;
      }
    }
    
    .subtitle {
      font-size: 1.2rem;
      color: var(--textColor2);
      max-width: 800px;
      margin: 0 auto;
    }
  }

  // 主内容区域
  .drawing-main-content {
    display: flex;
    flex-wrap: wrap;
    gap: 2rem;
    width: 100%;
    
    @media (max-width: 1024px) {
      flex-direction: column;
    }
    
    // 控制面板
    .control-panel {
      flex: 1;
      min-width: 320px;
      max-width: 800px;
      display: flex;
      flex-direction: column;
      width: 100%;
      
      @media (max-width: 1024px) {
        max-width: 100%;
      }
      
      .drawing-form {
        background: var(--bgColor2);
        border-radius: 12px;
        padding: 2rem;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
        position: relative;
        overflow: hidden;
        margin-bottom: 20px;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 4px;
          background: linear-gradient(90deg, #4776E6, #8E54E9);
          opacity: 0.8;
        }
        
        :deep(.el-form-item__label) {
          color: var(--textColor2);
          font-size: 0.95rem;
          font-weight: 500;
          padding-bottom: 8px;
          transition: color 0.3s ease;
        }
          
        :deep(.el-form-item:hover .el-form-item__label) {
          color: rgba(71, 118, 230, 0.9);
        }
        
        .form-section {
          margin-bottom: 2rem;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          .section-title {
            font-size: 1.25rem;
            font-weight: 600;
            margin-bottom: 1.25rem;
            color: var(--textColor1);
            padding-left: 1rem;
            border-left: 4px solid #4776E6;
          }
        }
        
        .setting-row {
          display: flex;
          gap: 1.5rem;
          margin-bottom: 1rem;
          
          @media (max-width: 640px) {
            flex-direction: column;
            gap: 1rem;
          }
          
          .el-form-item {
            margin-bottom: 0;
            flex: 1;
          }
        }
        
        // 重置表单和生成按钮区域
        .form-actions {
          margin-top: 2rem;
          display: flex;
          justify-content: center;
          gap: 1rem;
          
          .reset-btn {
            background: transparent;
            border: 1px solid rgba(142, 84, 233, 0.3);
            color: var(--textColor2);
            border-radius: 10px;
            padding: 12px 20px;
            font-weight: 500;
            transition: all 0.3s ease;
            
            &:hover {
              background: rgba(142, 84, 233, 0.05);
              border-color: rgba(142, 84, 233, 0.5);
              color: var(--textColor1);
              transform: translateY(-2px);
            }
          }
          
          .generate-btn {
            flex: 2;
            background: linear-gradient(135deg, #4776E6, #8E54E9);
            border: none;
            font-weight: 600;
            font-size: 1.1rem;
            position: relative;
            overflow: hidden;
            transition: all 0.3s ease;
            border-radius: 10px;
            padding: 12px 20px;
            box-shadow: 0 4px 15px rgba(71, 118, 230, 0.2);
            
            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 7px 20px rgba(80, 102, 224, 0.4);
              background: linear-gradient(135deg, #5889F0, #9967ED);
              
              &::before {
                transform: translateX(100%);
              }
            }
            
            &:active {
              transform: translateY(1px);
              box-shadow: 0 3px 10px rgba(80, 102, 224, 0.3);
            }
            
            &::before {
              content: '';
              position: absolute;
              top: 0;
              left: -50%;
              width: 50%;
              height: 100%;
              background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.4), transparent);
              transform: skewX(-25deg);
              transition: transform 0.7s ease;
            }
            
            .generate-btn-content {
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 0.5rem;
            }
          }
        }
      }
    }
    
    // 结果面板
    .result-panel {
      flex: 1;
      min-width: 320px;
      
      // 生成中状态
      .generating-status {
        height: 400px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: var(--bgColor2);
        border-radius: 12px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 4px;
          background: linear-gradient(90deg, #4776E6, #8E54E9);
          opacity: 0.8;
        }
        
        .loading-animation {
          display: flex;
          flex-direction: column;
          align-items: center;
          
          .loading-icon {
            margin-bottom: 1.5rem;
            animation: pulse 2s infinite;
            color: #4776E6;
            opacity: 0.9;
            filter: drop-shadow(0 0 10px rgba(71, 118, 230, 0.4));
          }
          
          .loading-text {
            font-size: 1.25rem;
            margin-bottom: 1.2rem;
            color: var(--textColor1);
            font-weight: 500;
            text-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
          }
          
          .loading-dots {
            display: flex;
            gap: 0.6rem;
            
            span {
              display: inline-block;
              width: 12px;
              height: 12px;
              border-radius: 50%;
              background: linear-gradient(135deg, #4776E6, #8E54E9);
              animation: dotPulse 1.4s infinite ease-in-out;
              box-shadow: 0 2px 5px rgba(71, 118, 230, 0.3);
              
              &:nth-child(1) {
                animation-delay: 0s;
              }
              
              &:nth-child(2) {
                animation-delay: 0.2s;
              }
              
              &:nth-child(3) {
                animation-delay: 0.4s;
              }
            }
          }
        }
      }
      
      // 结果画廊
      .result-gallery {
        background: var(--bgColor2);
        border-radius: 12px;
        padding: 2rem;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 4px;
          background: linear-gradient(90deg, #4776E6, #8E54E9);
          opacity: 0.8;
        }
        
        .section-title {
          font-size: 1.25rem;
          font-weight: 600;
          margin-bottom: 1.5rem;
          color: var(--textColor1);
          padding-left: 1rem;
          border-left: 4px solid #4776E6;
        }
        
        .image-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
          gap: 1.5rem;
          
          .result-image-container {
            position: relative;
            overflow: hidden;
            border-radius: 12px;
            aspect-ratio: 1 / 1;
            background: var(--bgColor3);
            cursor: pointer;
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
            
            &:hover {
              transform: translateY(-8px) scale(1.02);
              box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
              
              .image-actions {
                opacity: 1;
                transform: translateY(0);
              }
            }
            
            .result-image {
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: block;
              transition: all 0.4s ease;
            }
            
            .image-actions {
              position: absolute;
              bottom: 0;
              left: 0;
              width: 100%;
              padding: 1.2rem;
              background: rgba(0, 0, 0, 0.6);
              backdrop-filter: blur(8px);
              display: flex;
              justify-content: center;
              gap: 1.2rem;
              opacity: 0;
              transform: translateY(100%);
              transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
              
              :deep(.el-button) {
                transition: all 0.3s ease;
                
                &:hover {
                  transform: scale(1.15);
                }
              }
            }
          }
        }
      }
      
      // 空结果状态
      .empty-results {
        height: 400px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--bgColor2);
        border-radius: 12px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 4px;
          background: linear-gradient(90deg, #4776E6, #8E54E9);
          opacity: 0.8;
        }
        
        :deep(.el-empty) {
          .el-empty__description p {
            color: var(--textColor2);
            font-size: 1rem;
            margin-top: 0.5rem;
            max-width: 300px;
            text-align: center;
            line-height: 1.5;
          }
        }
      }
    }
  }

  // 图片预览弹窗
  :deep(.preview-dialog) {
    .el-dialog__header {
      display: none;
    }
    
    .el-dialog__body {
      padding: 0;
      background: var(--bgColor1);
    }
    
    .preview-content {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 1.5rem 1rem;
      background: rgba(0, 0, 0, 0.02);
      min-height: 300px;
      
      .preview-image {
        max-width: 100%;
        max-height: 70vh;
        border-radius: 8px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
        transition: all 0.3s ease;
      }
    }
    
    .preview-actions {
      display: flex;
      justify-content: center;
      gap: 1.2rem;
      padding: 1.2rem;
      background: var(--bgColor2);
      border-top: 1px solid rgba(255, 255, 255, 0.05);
      
      .el-button {
        padding: 10px 22px;
        border-radius: 8px;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
        }
        
        &:active {
          transform: translateY(1px);
        }
      }
    }
  }
}

// 自定义输入样式
.input-container {
  position: relative;
  border-radius: 12px;
  border: 1px solid var(--themeColor1);
  width: 100%;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  
  &:hover, &:focus-within {
    .input-backdrop {
      opacity: 1;
      transform: scale(1.02);
    }
  }
  
  .input-backdrop {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0.6;
    transition: all 0.4s ease;
    border-radius: 12px;
    pointer-events: none;
    z-index: -1;
    background: linear-gradient(135deg, rgba(71, 118, 230, 0.15), rgba(142, 84, 233, 0.25));
    box-shadow: 0 4px 15px rgba(71, 118, 230, 0.1);
  }
  
  :deep(.el-textarea__wrapper) {
    box-shadow: none !important;
    background-color: var(--bgColor2) !important;
    padding: 16px 18px;
    border: 1px solid rgba(142, 84, 233, 0.2);
    border-radius: 12px;
    transition: all 0.3s ease;
    font-family: var(--el-font-family);
    
    &::before {
      display: none;
    }
    
    &.is-focus {
      border-color: rgba(71, 118, 230, 0.7);
      box-shadow: 0 0 0 1px rgba(71, 118, 230, 0.1) !important;
    }
    
    .el-textarea__inner {
      color: var(--textColor2) !important;
      font-size: 0.95rem;
      line-height: 1.6;
      background-color: transparent !important;
      
      &::placeholder {
        color: var(--textColor4) !important;
        font-weight: 300;
      }
    }
  }
}

:deep(.el-textarea) {
  .el-textarea__inner {
    background-color: var(--bgColor2) !important;
    box-shadow: none !important;
    color: var(--textColor2) !important;
    
    &:hover {
      box-shadow: none !important;
      background-color: var(--bgColor2) !important;
    }
  }
  
  ::placeholder {
    color: var(--textColor4) !important;
  }
  
  /* 兼容性处理 */
  :-ms-input-placeholder {
    color: var(--textColor4) !important;
  }
}

.full-width-select {
  width: 100%;
  
  :deep(.el-input__wrapper) {
    background-color: var(--bgColor2) !important;
    border: 1px solid rgba(142, 84, 233, 0.2);
    border-radius: 10px;
    box-shadow: none !important;
    padding: 2px 18px;
    transition: all 0.3s ease;
    
    &::before {
      display: none;
    }
    
    &:hover {
      border-color: rgba(71, 118, 230, 0.5);
      background-color: var(--bgColor2) !important;
    }
    
    &.is-focus {
      border-color: rgba(71, 118, 230, 0.7);
      box-shadow: 0 0 0 1px rgba(71, 118, 230, 0.1) !important;
    }
    
    .el-input__inner {
      height: 44px;
      color: var(--textColor2) !important;
      font-size: 0.95rem;
      background-color: transparent !important;
    }
  }

  :deep(.el-select-dropdown__item) {
    &.selected {
      color: #4776E6;
      font-weight: 600;
      background: rgba(71, 118, 230, 0.08);
    }
    
    &:hover {
      background: rgba(142, 84, 233, 0.08);
    }
  }
}

.helper-text {
  color: var(--textColor3);
  font-size: 0.8rem;
  margin-top: 0.65rem;
  padding-left: 0.5rem;
  font-style: italic;
  opacity: 0.85;
}

// 高级设置折叠面板样式
:deep(.advanced-settings) {
  margin-top: 1.5rem;
  border: none;
  
  .el-collapse-item__header {
    background: transparent;
    border: none;
    color: var(--textColor2);
    font-size: 0.9rem;
    font-weight: 500;
    height: auto;
    padding: 10px 0;
    
    &:hover {
      color: rgba(71, 118, 230, 0.9);
    }
    
    .el-collapse-item__arrow {
      margin-right: 4px;
      color: rgba(142, 84, 233, 0.7);
    }
  }
  
  .el-collapse-item__wrap {
    background: transparent;
    border: none;
    
    .el-collapse-item__content {
      padding: 15px 0 0;
      color: var(--textColor1);
    }
  }
}

// 种子值输入组样式
.seed-input-group {
  display: flex;
  align-items: center;
  gap: 12px;
  
  :deep(.el-input__wrapper) {
    background: transparent;
    border: 1px solid rgba(142, 84, 233, 0.2);
    border-radius: 10px;
    box-shadow: none;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: rgba(71, 118, 230, 0.5);
    }
    
    &.is-focus {
      border-color: rgba(71, 118, 230, 0.7);
      box-shadow: 0 0 0 1px rgba(71, 118, 230, 0.1);
    }
  }
  
  :deep(.el-checkbox) {
    .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: #4776E6;
      border-color: #4776E6;
    }
    
    .el-checkbox__label {
      color: var(--textColor2);
    }
  }
}

// 动画定义
@keyframes textShine {
  0% {
    background-position: 0% 50%;
  }
  100% {
    background-position: 100% 50%;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
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

// 图片上传样式
.image-upload-container {
  .image-uploader {
    :deep(.el-upload) {
      width: 100%;
      
      .el-upload-dragger {
        width: 100%;
        height: 200px;
        border: 2px dashed #d9d9d9;
        border-radius: 12px;
        background: #fafafa;
        position: relative;
        overflow: hidden;
        transition: all 0.3s ease;
        
        &:hover {
          border-color: var(--themeColor1);
          background: #f0f9ff;
        }
      }
    }
  }
  
  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #8c939d;
    
    .upload-icon {
      font-size: 48px;
      margin-bottom: 16px;
      color: #d9d9d9;
    }
    
    .upload-text {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 8px;
      color: #606266;
    }
    
    .upload-hint {
      font-size: 14px;
      color: #909399;
    }
  }
  
  .uploaded-image-preview {
    position: relative;
    width: 100%;
    height: 100%;
    
    .preview-img {
      width: 100%;
      height: 100%;
      object-fit: contain;
      border-radius: 8px;
    }
    
    .image-overlay {
      position: absolute;
      top: 8px;
      right: 8px;
      opacity: 0;
      transition: opacity 0.3s ease;
    }
    
    &:hover .image-overlay {
      opacity: 1;
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .zhipu-drawing-container {
    padding: 1rem;
    
    .header-section {
      h1 {
        font-size: 2.2rem;
      }
      
      .subtitle {
        font-size: 1rem;
      }
    }
    
    .drawing-main-content {
      .control-panel {
        .drawing-form {
          padding: 1.5rem;
          
          .form-actions {
            flex-direction: column-reverse;
            
            .reset-btn, .generate-btn {
              width: 100%;
            }
          }
        }
      }
    }
    
    :deep(.preview-dialog) {
      width: 90% !important;
    }
  }
}

// 自定义下拉菜单样式
:deep(.el-select__popper) {
  background-color: var(--bgColor2) !important;
  border: 1px solid rgba(142, 84, 233, 0.2);
  
  .el-scrollbar {
    background-color: var(--bgColor2) !important;
  }
  
  .el-popper__arrow::before {
    background-color: var(--bgColor2) !important;
    border-color: rgba(142, 84, 233, 0.2) !important;
  }
  
  .el-select-dropdown__item {
    color: var(--textColor2) !important;
    
    &.hover, &:hover {
      background-color: rgba(71, 118, 230, 0.1) !important;
    }
    
    &.selected {
      color: #4776E6 !important;
      font-weight: 600;
      background-color: rgba(71, 118, 230, 0.08) !important;
    }
  }
}

// 提示词计数器样式
:deep(.el-input__count-inner) {
  background-color: transparent !important;
  color: var(--textColor3) !important;
}

// 单图显示样式
.single-image-display {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
  
  .result-image-container.single {
    position: relative;
    overflow: hidden;
    border-radius: 12px;
    max-width: 500px;
    width: 100%;
    height: auto;
    background: var(--bgColor3);
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    
    &:hover {
      transform: translateY(-8px) scale(1.02);
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
      
      .image-actions {
        opacity: 1;
        transform: translateY(0);
      }
    }
    
    .result-image {
      width: 100%;
      height: auto;
      object-fit: contain;
      display: block;
      transition: all 0.4s ease;
    }
    
    .image-actions {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      padding: 1.2rem;
      background: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(8px);
      display: flex;
      justify-content: center;
      gap: 1.2rem;
      opacity: 0;
      transform: translateY(100%);
      transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      
      :deep(.el-button) {
        transition: all 0.3s ease;
        
        &:hover {
          transform: scale(1.15);
        }
      }
    }
  }
}
</style>
