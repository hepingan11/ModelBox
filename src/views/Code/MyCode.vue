<template>
  <div class="my-code-container">
    <div class="header-section">
      <div class="back-button">
        <el-button @click="$router.go(-1)" icon="ArrowLeft" class="return-btn">返回</el-button>
    </div>
      <h1 data-text="我的代码项目">我的代码项目</h1>
      <p class="subtitle">管理您的代码项目</p>
      
      <!-- 标签页切换 -->
      <div class="tabs-container">
        <el-tabs v-model="activeTab" class="code-tabs" @tab-click="handleTabChange">
          <el-tab-pane label="已购买项目" name="purchased"></el-tab-pane>
          <el-tab-pane label="我的项目" name="published"></el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 已购买项目列表 -->
    <div class="code-list" v-loading="loading" v-if="activeTab === 'purchased'">
      <el-empty 
        v-if="!loading && (!purchasedCodeList || purchasedCodeList.length === 0)" 
        description="暂无已购买的代码项目" 
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/code_view')">
            前往源码市场
          </el-button>
        </template>
      </el-empty>
      
      <div v-else class="code-grid">
        <el-card 
          v-for="code in purchasedCodeList" 
          :key="code.codeId" 
          class="code-card hover-scale"
          @click="viewDetail(code)"
        >
          <!-- 图片展示区域 -->
          <div class="code-image">
            <img 
              v-if="code.imageList && code.imageList.length > 0" 
              :src="imgUrl + code.imageList[0]" 
              alt="项目预览图"
              class="preview-image"
            />
            <div v-else class="image-placeholder">
              <el-icon :size="32" class="placeholder-icon"><Document /></el-icon>
            </div>
          </div>
          
          <div class="card-header">
            <h3 class="code-name">{{ code.codeName }}</h3>
            <el-tag :type="getLanguageTagType(code.language)" size="small">{{ code.language }}</el-tag>
          </div>
          
          <p class="code-intro">{{ code.introduce }}</p>
          
          <div class="card-footer">
            <div class="code-meta">
              <el-tooltip content="查看次数" placement="top">
                <div class="meta-item">
                  <el-icon><View /></el-icon>
                  <span>{{ code.look || 0 }}</span>
                </div>
              </el-tooltip>
            </div>
            
            <div class="code-price">
              <span v-if="code.price > 0">¥{{ code.price.toFixed(2) }}</span>
              <el-tag v-else type="success" size="small">免费</el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 已发布项目列表 -->
    <div class="code-list" v-loading="loadingPublished" v-if="activeTab === 'published'">
      <el-empty 
        v-if="!loadingPublished && (!publishedCodeList || publishedCodeList.length === 0)" 
        description="暂无发布的代码项目" 
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/publish_code')">
            发布新项目
          </el-button>
        </template>
      </el-empty>
      
      <div v-else class="code-grid">
        <el-card 
          v-for="code in publishedCodeList" 
          :key="code.codeId" 
          class="code-card hover-scale published-card"
        >
          <!-- 图片展示区域 -->
          <div class="code-image">
            <img 
              v-if="code.imageList && code.imageList.length > 0" 
              :src="imgUrl + code.imageList[0]" 
              alt="项目预览图"
              class="preview-image"
            />
            <div v-else class="image-placeholder">
              <el-icon :size="32" class="placeholder-icon"><Document /></el-icon>
            </div>
          </div>
          
          <div class="card-header">
            <h3 class="code-name">{{ code.codeName }}</h3>
            <div class="header-tags">
              <el-tag :type="getLanguageTagType(code.language)" size="small">{{ code.language }}</el-tag>
              <el-tag 
                v-if="code.isPublic === 1" 
                type="success" 
                size="small" 
                class="status-tag"
              >已通过</el-tag>
              <el-tag 
                v-else 
                type="warning" 
                size="small" 
                class="status-tag"
              >未通过</el-tag>
            </div>
          </div>
          
          <p class="code-intro">{{ code.introduce }}</p>
          
          <div class="card-footer">
            <div class="code-meta">
              <el-tooltip content="查看次数" placement="top">
                <div class="meta-item">
                  <el-icon><View /></el-icon>
                  <span>{{ code.look || 0 }}</span>
                </div>
              </el-tooltip>
            </div>
            
            <div class="code-price">
              <span v-if="code.price > 0">¥{{ code.price.toFixed(2) }}</span>
              <el-tag v-else type="success" size="small">免费</el-tag>
            </div>
          </div>
          
          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="small" 
              icon="Edit" 
              @click.stop="editCode(code)"
              class="edit-btn"
            >编辑</el-button>
            <el-button 
              type="danger" 
              size="small" 
              icon="Delete" 
              @click.stop="confirmDelete(code)"
              class="delete-btn"
            >删除</el-button>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 代码详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="代码详情"
      width="60%"
      :before-close="handleClose"
      class="code-dialog"
      :custom-class="'theme-dialog'"
    >
      <div v-if="selectedCode" class="code-detail">
        <!-- 详情图片展示 -->
        <div class="detail-image" v-if="selectedCode.imageList && selectedCode.imageList.length > 0">
          <img :src="imgUrl + selectedCode.imageList[0]" alt="项目预览图" class="detail-preview-image" />
        </div>
        
        <h2 class="detail-title">{{ selectedCode.codeName }}</h2>
        <el-tag :type="getLanguageTagType(selectedCode.language)" size="small">{{ selectedCode.language }}</el-tag>
        
        <div class="detail-section">
          <h3>项目介绍</h3>
          <p>{{ selectedCode.introduce }}</p>
        </div>
        
        <div class="detail-meta">
          <div class="meta-item">
            <el-icon><View /></el-icon>
            <span>{{ selectedCode.look }} 次查看</span>
          </div>
        </div>
        
        <div class="detail-price">
          <h3>价格</h3>
          <span v-if="selectedCode.price > 0" class="price-tag">¥{{ selectedCode.price.toFixed(2) }}</span>
          <el-tag v-else type="success">免费</el-tag>
        </div>

        <div class="detail-section download-section">
          <h3>下载信息</h3>
          <p v-if="selectedCode.downloadUrl">
            点击下方按钮可直接下载代码项目
          </p>
          <p v-else class="no-download-info">
            下载链接暂未生成，请稍后再试或联系客服
          </p>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="primary" 
            class="confirm-btn"
            @click="handleDownload"
            :disabled="!selectedCode || !selectedCode.downloadUrl"
          >
            立即下载
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 确认删除对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
      class="code-dialog"
      :custom-class="'theme-dialog'"
    >
      <div class="delete-dialog-content">
        <p>确定要删除项目 <strong>{{ codeToDelete?.codeName }}</strong> 吗？</p>
        <p class="warning-text">此操作不可逆，删除后数据将无法恢复。</p>
        <p class="notice-text">注意：已被用户购买的项目无法删除。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="danger" 
            class="delete-confirm-btn"
            @click="deleteProject"
            :loading="deletingProject"
          >
            确认删除
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 编辑项目对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑项目"
      width="60%"
      :before-close="handleEditClose"
      class="code-dialog"
      :custom-class="'theme-dialog'"
    >
      <div v-if="codeToEdit" class="edit-form-container">
        <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="formRules"
          label-position="top"
          class="edit-form"
        >
          <!-- 基本信息 -->
          <div class="form-section">
            <h3 class="section-title">基本信息</h3>
            
            <el-form-item label="项目名称" prop="codeName">
              <div class="input-container">
                <el-input 
                  v-model="editForm.codeName" 
                  placeholder="请输入项目名称"
                  show-word-limit
                />
                <div class="input-backdrop"></div>
              </div>
            </el-form-item>
            
            <el-form-item label="编程语言" prop="language">
              <div class="select-container">
                <el-select 
                  v-model="editForm.language" 
                  placeholder="选择编程语言"
                  popper-class="custom-select-dropdown"
                >
                  <el-option
                    v-for="item in languageOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  >
                    <div class="option-content">
                      <span 
                        class="language-color" 
                        :style="{ backgroundColor: getLanguageColor(item.value) }"
                      ></span>
                      <span class="option-label">{{ item.label }}</span>
                    </div>
                  </el-option>
                </el-select>
                <div class="select-backdrop"></div>
              </div>
            </el-form-item>
            
            <el-form-item label="项目价格 (¥)" prop="price">
              <div class="input-container price-input">
                <el-input
                  v-model.number="editForm.price"
                  type="number"
                  placeholder="0.00"
                  :min="0"
                  :precision="2"
                >
                  <template #prefix>
                    <span>¥</span>
                  </template>
                </el-input>
                <div class="input-backdrop"></div>
              </div>
              <div class="helper-text">设置为0表示免费分享</div>
            </el-form-item>
          </div>
          
          <!-- 项目介绍 -->
          <div class="form-section">
            <h3 class="section-title">项目介绍</h3>
            
            <el-form-item label="项目介绍" prop="introduce">
              <div class="input-container">
                <el-input
                  style="width: 400px;"
                  v-model="editForm.introduce"
                  type="textarea"
                  placeholder="请详细描述您的项目，包括主要功能、技术栈和适用场景等"
                  :rows="5"
                  show-word-limit
                  class="dark-mode-input"
                />
                <div class="input-backdrop"></div>
              </div>
            </el-form-item>
          </div>
          
          <!-- 代码链接 -->
          <div class="form-section">
            <h3 class="section-title">代码链接</h3>
            
            <el-form-item label="下载链接" prop="downloadUrl">
              <div class="input-container">
                <el-input
                  v-model="editForm.downloadUrl"
                  placeholder="请输入代码下载链接（如网盘链接）"
                />
                <div class="input-backdrop"></div>
              </div>
            </el-form-item>
            
            <el-form-item label="提取码" prop="extractCode">
              <div class="input-container">
                <el-input
                  v-model="editForm.extractCode"
                  placeholder="请输入提取码（如有）"
                  maxlength="4"
                />
                <div class="input-backdrop"></div>
              </div>
              <div class="helper-text">请输入4位提取码，不需要则留空</div>
            </el-form-item>
          </div>
          
          <!-- 项目图片 -->
          <div class="form-section">
            <h3 class="section-title">项目图片</h3>
            
            <div class="project-images">
              <div class="images-container">
                <template v-if="codeToEdit.imageList && codeToEdit.imageList.length > 0">
                  <div 
                    v-for="(image, index) in codeToEdit.imageList" 
                    :key="index"
                    class="image-item"
                  >
                    <div class="image-preview">
                      <img :src="imgUrl + image" :alt="`预览图 ${index + 1}`">
                      <div class="image-actions">
                        <el-button 
                          type="danger" 
                          size="small" 
                          icon="Delete" 
                          circle
                          @click="confirmDeleteImage(image)"
                          class="delete-image-btn"
                        ></el-button>
                      </div>
                    </div>
                  </div>
                </template>
                
                <div class="image-upload-container">
                  <el-upload
                    class="image-uploader"
                    :action="''"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleImageChange"
                    :on-exceed="handleExceed"
                    :before-upload="beforeImageUpload"
                    accept=".jpg,.jpeg,.png"
                  >
                    <div class="upload-trigger">
                      <el-icon :size="24"><Plus /></el-icon>
                      <p>添加项目图片</p>
                    </div>
                  </el-upload>
                </div>
              </div>
              
              <div class="helper-text">
                <p>* 图片格式限制：JPG 或 PNG，大小不超过 2MB</p>
                <p>* 建议上传尺寸大于 800*600 的清晰图片</p>
                <p>* 第一张图片将作为项目封面</p>
              </div>
            </div>
          </div>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="primary" 
            class="confirm-btn"
            @click="submitEditForm"
            :loading="updating"
          >
            保存修改
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 确认删除图片对话框 -->
    <el-dialog
      v-model="deleteImageDialogVisible"
      title="确认删除图片"
      width="30%"
      class="code-dialog"
      :custom-class="'theme-dialog'"
    >
      <div class="delete-dialog-content">
        <div class="confirm-image-preview" v-if="imageToDelete">
          <img :src="imgUrl + imageToDelete" alt="待删除图片" />
        </div>
        <p>确定要删除此项目图片吗？</p>
        <p class="warning-text">此操作不可逆，删除后将无法恢复。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteImageDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="danger" 
            class="delete-confirm-btn"
            @click="deleteImage"
            :loading="deletingImage"
          >
            确认删除
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { View, Document, ArrowLeft, Delete, Edit, Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { getMyCodeList, getMyPublishList, deleteMyPublishCode, updateMyPublishCode, addCodeImage, deleteCodeImage } from '../../../api/BSideApi';

const router = useRouter();

// 数据状态
const loading = ref(false);
const loadingPublished = ref(false);
const purchasedCodeList = ref([]);
const publishedCodeList = ref([]);
const dialogVisible = ref(false);
const selectedCode = ref(null);
const imgUrl = ref('');
const activeTab = ref('purchased');
const deleteDialogVisible = ref(false);
const codeToDelete = ref(null);
const deletingProject = ref(false);

// 编辑相关状态
const editDialogVisible = ref(false);
const codeToEdit = ref(null);
const editForm = reactive({
  codeId: null,
  codeName: '',
  language: '',
  price: 0,
  introduce: '',
  downloadUrl: '',
  extractCode: ''
});
const editFormRef = ref(null);
const updating = ref(false);

// 图片管理相关状态
const deleteImageDialogVisible = ref(false);
const imageToDelete = ref(null);
const deletingImage = ref(false);
const imageFile = ref(null);
const uploadingImage = ref(false);

// 编程语言选项
const languageOptions = [
  { value: 'JavaScript', label: 'JavaScript' },
  { value: 'Python', label: 'Python' },
  { value: 'Java', label: 'Java' },
  { value: 'C', label: 'C' },
  { value: 'Go', label: 'Go' },
  { value: 'PHP', label: 'PHP' },
  { value: 'Ruby', label: 'Ruby' },
  { value: 'Swift', label: 'Swift' },
  { value: 'Kotlin', label: 'Kotlin' },
  { value: 'TypeScript', label: 'TypeScript' },
  { value: 'Vue', label: 'Vue' },
  { value: 'React', label: 'React' },
  { value: 'Angular', label: 'Angular' },
  { value: '其他', label: '其他' }
];

// 表单验证规则
const formRules = {
  codeName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ],
  downloadUrl: [
    { required: true, message: '请输入代码下载链接', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' }
  ],
  extractCode: [
    { pattern: /^[a-zA-Z0-9]{4}$|^$/, message: '请输入4位提取码或留空', trigger: 'blur' }
  ]
};

// 获取语言标签类型
const getLanguageTagType = (language) => {
  const languageMap = {
    'JavaScript': 'warning',
    'Python': 'success',
    'Java': 'danger',
    'C': 'info',
    'Go': 'info',
    'PHP': 'warning',
    'Ruby': 'danger',
    'Swift': 'warning',
    'Kotlin': 'primary',
    'TypeScript': 'primary',
    'Vue': 'success',
    'React': 'info',
    'Angular': 'danger'
  };
  
  return languageMap[language] || '';
};

// 获取已购买代码列表
const fetchMyCodeList = async () => {
  loading.value = true;
  try {
    const res = await getMyCodeList();
    
    if (res) {
      purchasedCodeList.value = res;
    } else {
      purchasedCodeList.value = [];
      ElMessage.warning('暂无已购买的代码项目');
    }
  } catch (error) {
    console.error('获取已购买代码列表失败:', error);
    ElMessage.error('获取已购买代码列表失败，请稍后重试');
    purchasedCodeList.value = [];
  } finally {
    loading.value = false;
  }
};

// 获取已发布代码列表
const fetchMyPublishList = async () => {
  loadingPublished.value = true;
  try {
    const res = await getMyPublishList();
    
    if (res) {
      publishedCodeList.value = res;
    } else {
      publishedCodeList.value = [];
      ElMessage.warning('暂无发布的代码项目');
    }
  } catch (error) {
    console.error('获取发布代码列表失败:', error);
    ElMessage.error('获取发布代码列表失败，请稍后重试');
    publishedCodeList.value = [];
  } finally {
    loadingPublished.value = false;
  }
};

// 处理标签页切换
const handleTabChange = (tab) => {
  if (tab.props.name === 'published' && publishedCodeList.value.length === 0) {
    fetchMyPublishList();
  }
};

// 查看详情
const viewDetail = (code) => {
  selectedCode.value = code;
  dialogVisible.value = true;
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  setTimeout(() => {
    selectedCode.value = null;
  }, 200);
};

// 处理下载
const handleDownload = () => {
  if (!selectedCode.value || !selectedCode.value.downloadUrl) {
    ElMessage.warning('下载链接不可用');
    return;
  }
  
  // 直接下载
  window.open(selectedCode.value.downloadUrl, '_blank');
  dialogVisible.value = false;
};

// 确认删除对话框
const confirmDelete = (code) => {
  codeToDelete.value = code;
  deleteDialogVisible.value = true;
};

// 删除项目
const deleteProject = async () => {
  if (!codeToDelete.value) return;
  
  deletingProject.value = true;
  try {
    const res = await deleteMyPublishCode(codeToDelete.value.codeId);
    // 检查返回结果，如果包含错误信息则显示
    if (res && res.code !== 200) {
      ElMessage.error(res.msg || '删除项目失败，请稍后重试');
    } else {
      ElMessage.success('项目删除成功');
      // 从列表中移除已删除的项目
      publishedCodeList.value = publishedCodeList.value.filter(
        item => item.codeId !== codeToDelete.value.codeId
      );
      deleteDialogVisible.value = false;
    }
  } catch (error) {
    console.error('删除项目失败:', error);
    // 判断是否是"已被购买"的错误
    if (error.response && error.response.data && error.response.data.msg === "该项目已被购买，无法删除") {
      ElMessage.error('该项目已被购买，无法删除');
    } else {
      ElMessage.error('删除项目失败，请稍后重试');
    }
  } finally {
    deletingProject.value = false;
  }
};

// 打开编辑对话框
const editCode = (code) => {
  codeToEdit.value = code;
  
  // 填充表单数据
  editForm.codeId = code.codeId;
  editForm.codeName = code.codeName;
  editForm.language = code.language;
  editForm.price = code.price;
  editForm.introduce = code.introduce;
  editForm.downloadUrl = code.downloadUrl;
  editForm.extractCode = code.extractCode || '';
  
  editDialogVisible.value = true;
};

// 关闭编辑对话框
const handleEditClose = () => {
  editDialogVisible.value = false;
  setTimeout(() => {
    codeToEdit.value = null;
    editFormRef.value.resetFields();
  }, 200);
};

// 提交编辑表单
const submitEditForm = () => {
  editFormRef.value.validate(async (valid) => {
    if (valid) {
      updating.value = true;
      
      try {
        // 创建要提交的数据对象
        const formDataToSubmit = {
          codeId: editForm.codeId,
          codeName: editForm.codeName,
          language: editForm.language,
          price: editForm.price,
          introduce: editForm.introduce,
          downloadUrl: editForm.downloadUrl,
          extractCode: editForm.extractCode || null
        };
        
        // 提交到后端
        console.log('提交更新数据', formDataToSubmit);
        const res = await updateMyPublishCode(formDataToSubmit);
        console.log('更新响应', res);
        
        ElMessage.success('更新成功！');
        editDialogVisible.value = false;
        
        // 更新本地列表中的数据
        const index = publishedCodeList.value.findIndex(item => item.codeId === editForm.codeId);
        if (index !== -1) {
          publishedCodeList.value[index] = { 
            ...publishedCodeList.value[index], 
            ...formDataToSubmit,
            // 保留原有的其他属性如图片等
            imageList: publishedCodeList.value[index].imageList 
          };
        }
      } catch (error) {
        console.error('更新失败:', error);
        ElMessage.error('更新失败，请稍后重试');
      } finally {
        updating.value = false;
      }
    } else {
      ElMessage.warning('请正确填写所有必填项');
      return false;
    }
  });
};

// 获取语言颜色
const getLanguageColor = (language) => {
  const colorMap = {
    'JavaScript': '#f7df1e',
    'Python': '#3776ab',
    'Java': '#007396',
    'C': '#00599c',
    'Go': '#00add8',
    'PHP': '#777bb4',
    'Ruby': '#cc342d',
    'Swift': '#ffac45',
    'Kotlin': '#f18e33',
    'TypeScript': '#007acc',
    'Vue': '#42b883',
    'React': '#61dafb',
    'Angular': '#dd0031',
    '其他': '#95a5a6'
  };
  
  return colorMap[language] || '#95a5a6';
};

// 处理图片变更
const handleImageChange = (file) => {
  // 保存选择的文件
  imageFile.value = file.raw;
  
  // 尝试上传新图片
  uploadImage();
};

// 检查图片上传前的验证
const beforeImageUpload = (file) => {
  // 检查文件类型
  const isJPG = file.type === 'image/jpeg';
  const isPNG = file.type === 'image/png';
  const isValidType = isJPG || isPNG;
  
  // 检查文件大小
  const isLt2M = file.size / 1024 / 1024 < 2;
  
  if (!isValidType) {
    ElMessage.error('图片只能是JPG或PNG格式!');
    return false;
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!');
    return false;
  }
  
  return isValidType && isLt2M;
};

// 处理超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('一次只能上传一张图片');
};

// 上传图片
const uploadImage = async () => {
  if (!imageFile.value || !codeToEdit.value) return;
  
  // 检查图片格式和大小
  const isValid = await beforeImageUpload(imageFile.value);
  if (!isValid) return;
  
  uploadingImage.value = true;
  
  try {
    // 创建FormData对象
    const formData = new FormData();
    formData.append('codeId', codeToEdit.value.codeId);
    formData.append('image', imageFile.value);
    
    // 上传图片
    await addCodeImage(formData);
    
    ElMessage.success('图片上传成功');
    
    // 重新获取已发布项目列表以更新图片
    await fetchMyPublishList();
    
    // 更新当前编辑的项目信息
    const updatedProject = publishedCodeList.value.find(item => item.codeId === codeToEdit.value.codeId);
    if (updatedProject) {
      codeToEdit.value = updatedProject;
    }
    
    // 清除文件选择
    imageFile.value = null;
  } catch (error) {
    console.error('上传图片失败:', error);
    ElMessage.error('上传图片失败，请稍后重试');
  } finally {
    uploadingImage.value = false;
  }
};

// 确认删除图片
const confirmDeleteImage = (image) => {
  imageToDelete.value = image;
  deleteImageDialogVisible.value = true;
};

// 删除图片
const deleteImage = async () => {
  if (!imageToDelete.value) return;
  
  deletingImage.value = true;
  
  try {
    // 调用删除图片API
    await deleteCodeImage(imageToDelete.value);
    
    ElMessage.success('图片删除成功');
    
    // 隐藏对话框
    deleteImageDialogVisible.value = false;
    
    // 重新获取已发布项目列表以更新图片
    await fetchMyPublishList();
    
    // 更新当前编辑的项目信息
    const updatedProject = publishedCodeList.value.find(item => item.codeId === codeToEdit.value.codeId);
    if (updatedProject) {
      codeToEdit.value = updatedProject;
    }
  } catch (error) {
    console.error('删除图片失败:', error);
    ElMessage.error('删除图片失败，请稍后重试');
  } finally {
    deletingImage.value = false;
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchMyCodeList();
  imgUrl.value = process.env.VUE_APP_IMAGE;
});
</script>

<style lang="scss" scoped>
.my-code-container {
  min-height: 100vh;
  background-color: var(--bgColor1);
  padding: 40px 20px;
  animation: fadeIn 0.3s ease;
  overflow-y: auto;
  height: 100%;
  position: relative;
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  
  .back-button {
    position: absolute;
    left: 0;
    top: 0;
    
    .return-btn {
      background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
      color: white;
      border: none;
      border-radius: 20px;
      padding: 8px 20px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateX(-5px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      }
    }
  }
  
  h1 {
    color: var(--themeColor1);
    font-size: 36px;
    margin-bottom: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #11998e, #38ef7d);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    color: transparent;
    text-shadow: 0 2px 10px rgba(17, 153, 142, 0.2);
    position: relative;
    display: inline-block;
    
    &::after {
      content: attr(data-text);
      position: absolute;
      left: 0;
      right: 0;
      background: linear-gradient(135deg, #11998e, #38ef7d);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      color: transparent;
      filter: blur(8px);
      opacity: 0.3;
      z-index: -1;
    }
  }
  
  .subtitle {
    color: var(--textColor2);
    font-size: 16px;
    margin-bottom: 20px;
    opacity: 0.8;
  }
  
  .tabs-container {
    display: flex;
    justify-content: center;
    margin-top: 30px;
    
    .code-tabs {
      :deep(.el-tabs__header) {
        margin-bottom: 24px;
      }
      
      :deep(.el-tabs__item) {
        color: var(--textColor2);
        font-size: 16px;
        padding: 0 25px;
        
        &.is-active {
          color: var(--themeColor1);
          font-weight: 500;
        }
      }
      
      :deep(.el-tabs__active-bar) {
        background-color: var(--themeColor1);
      }
    }
  }
}

.code-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 0;
}

.code-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.code-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--bgColor2);
  border: none;
  overflow: hidden;
  position: relative;
  
  .code-image {
    height: 160px;
    overflow: hidden;
    margin: -20px -20px 15px -20px;
    background-color: var(--bgColor3);
    
    .preview-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      justify-content: center;
      align-items: center;
      background: linear-gradient(135deg, var(--bgColor2), var(--bgColor3));
      
      .placeholder-icon {
        color: var(--textColor4);
        opacity: 0.6;
      }
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 10px;
    
    .code-name {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: var(--textColor1);
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .header-tags {
      display: flex;
      gap: 5px;
      flex-wrap: wrap;
      
      .status-tag {
        animation: fadeIn 0.3s ease;
      }
    }
  }
  
  .code-intro {
    color: var(--textColor2);
    font-size: 14px;
    margin-bottom: 15px;
    flex: 1;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
  }
  
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .code-meta {
      display: flex;
      gap: 15px;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 5px;
        color: var(--textColor3);
        font-size: 12px;
      }
    }
    
    .code-price {
      font-weight: 600;
      color: #ff7d00;
      font-size: 16px;
    }
  }
  
  .action-buttons {
    position: absolute;
    top: 10px;
    right: 10px;
    display: flex;
    gap: 10px;
    opacity: 0;
    transition: opacity 0.3s ease;
    
    .edit-btn {
      background-color: rgba(64, 158, 255, 0.1);
      border: 1px solid var(--el-color-primary);
      color: var(--el-color-primary);
      
      &:hover {
        background-color: var(--el-color-primary);
        color: white;
      }
    }
    
    .delete-btn {
      background-color: rgba(255, 0, 0, 0.1);
      border: 1px solid var(--el-color-danger);
      color: var(--el-color-danger);
      
      &:hover {
        background-color: var(--el-color-danger);
        color: white;
      }
    }
  }
  
  &.published-card {
    cursor: default;
    
    &:hover {
      .action-buttons {
        opacity: 1;
      }
    }
  }
  
  &:hover {
    .preview-image {
      transform: scale(1.05);
    }
  }
}

.hover-scale {
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  }
}

.code-dialog {
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
    padding: 15px 20px;
    margin-right: 0;
    
    .el-dialog__title {
      color: white !important;
      font-weight: 600;
    }
    
    .el-dialog__headerbtn {
      .el-dialog__close {
        color: white !important;
      }
    }
  }
  
  :deep(.el-dialog__body) {
    background-color: var(--bgColor1);
    color: var(--textColor1);
    max-height: 70vh;
    overflow-y: auto;
    padding: 20px;
  }
  
  :deep(.el-dialog__footer) {
    background-color: var(--bgColor1);
    border-top: 1px solid var(--borderColor);
    padding: 15px 20px;
  }

  :deep(.el-dialog) {
    background-color: var(--bgColor1);
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  }
  
  :deep(.dialog-footer) {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    
    .cancel-btn {
      border-color: var(--borderColor);
      color: var(--textColor2);
      background-color: transparent;
    
      &:hover {
        border-color: var(--themeColor1);
        color: var(--themeColor1);
        background-color: rgba(var(--themeColor1-rgb), 0.05);
      }
    }
    
    .confirm-btn {
      background: var(--themeColor2);
      border-color: var(--themeColor2);
      
      &:hover {
        background: var(--themeColor1);
        border-color: var(--themeColor1);
      }
      
      &:disabled {
        background-color: var(--bgColor3);
        border-color: var(--bgColor3);
        color: var(--textColor3);
      }
    }
    
    .delete-confirm-btn {
      background: var(--el-color-danger);
      border-color: var(--el-color-danger);
      
      &:hover {
        background: var(--el-color-danger-dark-2);
        border-color: var(--el-color-danger-dark-2);
      }
    }
  }
}

.code-detail {
  .detail-image {
    margin: -20px -20px 20px -20px;
    height: 250px;
    overflow: hidden;
    background-color: var(--bgColor2);
    
    .detail-preview-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .detail-title {
    margin-top: 0;
    margin-bottom: 15px;
    color: var(--textColor1);
  }
  
  .detail-section {
    margin: 25px 0;
    
    h3 {
      font-size: 16px;
      color: var(--textColor1);
      margin-bottom: 10px;
    }
    
    p {
      color: var(--textColor2);
      line-height: 1.6;
    }

    &.download-section {
      border-top: 1px dashed var(--borderColor);
      padding-top: 20px;

      .no-download-info {
        color: #e6a23c;
      }
    }
  }
  
  .detail-meta {
    display: flex;
    gap: 20px;
    margin: 15px 0;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: 5px;
      color: var(--textColor2);
      
      .el-icon {
        color: var(--themeColor1);
      }
    }
  }
  
  .detail-price {
    margin-top: 20px;
    
    h3 {
      font-size: 16px;
      margin-bottom: 10px;
      color: var(--textColor1);
    }
    
    .price-tag {
      font-size: 24px;
      font-weight: 600;
      color: #ff7d00;
    }
  }
}

.delete-dialog-content {
  padding: 20px 0;
  text-align: center;
  
  p {
    margin-bottom: 15px;
    color: var(--textColor1);
    
    &.warning-text {
      color: var(--el-color-danger);
      font-size: 14px;
    }
    
    &.notice-text {
      color: var(--el-color-warning-dark-2);
      font-size: 14px;
      font-style: italic;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media screen and (max-width: 768px) {
  .header-section {
    h1 {
      font-size: 28px;
      margin-top: 40px;
    }
    
    .subtitle {
      font-size: 14px;
    }
    
    .back-button {
      top: 10px;
      left: 50%;
      transform: translateX(-50%);
    }
  }
  
  .code-grid {
    grid-template-columns: 1fr;
  }
  
  .code-dialog {
    width: 90% !important;
    
    :deep(.el-dialog__body) {
      max-height: 60vh;
    }
  }
}

// 编辑表单样式
.edit-form-container {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

.edit-form {
  width: 100%;
}

.form-section {
  margin-bottom: 30px;
  background-color: var(--bgColor2);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  }
  
  .section-title {
    font-size: 18px;
    margin-top: 0;
    margin-bottom: 20px;
    color: var(--themeColor1);
    font-weight: 600;
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      display: inline-block;
      width: 4px;
      height: 18px;
      background: linear-gradient(to bottom, #11998e, #38ef7d);
      margin-right: 10px;
      border-radius: 2px;
    }
  }
}

.input-container, .select-container {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  margin-bottom: 5px;
  
  &:hover, &:focus-within {
    transform: translateY(-2px);
    
    .input-backdrop, .select-backdrop {
      opacity: 1;
    }
  }
  
  .input-backdrop, .select-backdrop {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0.6;
    transition: opacity 0.3s ease;
    border-radius: 8px;
    pointer-events: none;
    z-index: -1;
    background: linear-gradient(135deg, rgba(17, 153, 142, 0.1), rgba(56, 239, 125, 0.2));
  }
  
  :deep(.el-input__wrapper),
  :deep(.el-textarea__wrapper),
  :deep(.el-input-number),
  :deep(.el-select .el-input__wrapper) {
    box-shadow: none;
    background: transparent;
    border-radius: 8px;
    padding: 0;
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s ease;
    
    &.is-focus {
      border-color: var(--themeColor1);
    }
  }
  
  :deep(.el-input__wrapper) {
    padding: 8px 12px;
  }
  
  :deep(.el-textarea__wrapper) {
    padding: 8px 12px;
  }
  
  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    color: var(--textColor1);
    
    &::placeholder {
      color: var(--textColor3);
      opacity: 0.7;
    }
  }
}

.price-input {
  :deep(.el-input__wrapper) {
    padding-left: 32px;
  }
  
  :deep(.el-input__prefix) {
    left: 12px;
    color: var(--themeColor1);
  }
}

.helper-text {
  color: var(--textColor3);
  font-size: 12px;
  margin-top: 6px;
  padding-left: 5px;
}

.language-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  display: inline-block;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 项目图片样式 */
.project-images {
  margin-top: 15px;
  
  .images-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
    margin-bottom: 15px;
    
    .image-item {
      position: relative;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      aspect-ratio: 4/3;
      
      .image-preview {
        width: 100%;
        height: 100%;
        position: relative;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s ease;
        }
        
        .image-actions {
          position: absolute;
          top: 5px;
          right: 5px;
          display: flex;
          gap: 5px;
          opacity: 0;
          transition: opacity 0.3s ease;
          
          .delete-image-btn {
            background-color: rgba(255, 0, 0, 0.15);
            border: none;
            font-size: 12px;
            padding: 5px;
            
            &:hover {
              background-color: rgba(255, 0, 0, 0.3);
            }
          }
        }
        
        &:hover {
          img {
            transform: scale(1.05);
          }
          
          .image-actions {
            opacity: 1;
          }
        }
      }
    }
    
    .image-upload-container {
      position: relative;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px dashed var(--borderColor);
      border-radius: 8px;
      cursor: pointer;
      background-color: var(--bgColor1);
      aspect-ratio: 4/3;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: var(--themeColor1);
        background-color: rgba(var(--themeColor1-rgb), 0.05);
        
        .upload-trigger {
          color: var(--themeColor1);
        }
      }
      
      .upload-trigger {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: var(--textColor3);
        transition: color 0.3s ease;
        
        .el-icon {
          margin-bottom: 8px;
        }
        
        p {
          margin: 0;
          font-size: 14px;
        }
      }
    }
  }
}

/* 确认删除图片对话框样式 */
.confirm-image-preview {
  margin: 0 auto 20px;
  width: 200px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

/* 黑夜模式输入框样式修复 */
.dark-mode-input :deep(.el-textarea__inner) {
  background-color: var(--bgColor2);
  border-color: var(--borderColor);
  color: var(--textColor1);
}

/* 全局表单控件暗色模式修复 */
:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  background-color: var(--bgColor2);
  border-color: var(--borderColor);
  color: var(--textColor1);
}

:deep(.el-input.is-disabled .el-input__inner),
:deep(.el-textarea.is-disabled .el-textarea__inner) {
  background-color: var(--bgColor3);
  border-color: var(--borderColor);
  color: var(--textColor3);
}

/* 输入控件的下拉菜单暗色模式 */
:deep(.el-select-dropdown) {
  background-color: var(--bgColor1);
  border: 1px solid var(--borderColor);
}

:deep(.el-select-dropdown__item) {
  color: var(--textColor1);
}

:deep(.el-select-dropdown__item.hover),
:deep(.el-select-dropdown__item:hover) {
  background-color: var(--bgColor3);
}

:deep(.el-select-dropdown__item.selected) {
  background-color: var(--bgColor3);
  color: var(--themeColor1);
}
</style>

<style lang="scss">
/* 全局对话框样式覆盖 - 不使用scoped */
.el-overlay {
  .theme-dialog {
    background-color: var(--bgColor1) !important;
    
    .el-dialog__header {
      background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
      .el-dialog__title {
        color: white !important;
      }
      .el-dialog__headerbtn .el-dialog__close {
        color: white !important;
      }
    }
    
    .el-dialog__body {
      background-color: var(--bgColor1) !important;
      color: var(--textColor1);
    }
    
    .el-dialog__footer {
      background-color: var(--bgColor1) !important;
      border-top: 1px solid var(--borderColor);
    }
  }
}
</style>

