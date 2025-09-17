<template>
  <div class="code-market">
    <div class="header-section">
      <h1 data-text="源码市场">源码市场</h1>
      <p class="subtitle">在这里发现、分享和交易优质的项目代码</p>
      
      <!-- 添加用户操作区域 -->
      <div class="user-actions">
        <div class="action-card publish-card" @click="$router.push('/publish_code')">
          <div class="card-icon-wrapper">
            <el-icon class="action-icon"><Upload /></el-icon>
          </div>
          <div class="card-content">
            <h3 class="card-title">我要发布</h3>
            <p class="card-desc">分享您的优质代码，获得额外收益</p>
          </div>
          <div class="card-backdrop"></div>
          <div class="shine-effect"></div>
        </div>
        
        <div class="action-card trade-card" @click="$router.push('/my_code')">
          <div class="card-icon-wrapper">
            <el-icon class="action-icon"><Document /></el-icon>
          </div>
          <div class="card-content">
            <h3 class="card-title">我的交易</h3>
            <p class="card-desc">管理您的交易记录和发布项目</p>
          </div>
          <div class="card-backdrop"></div>
          <div class="shine-effect"></div>
        </div>
      </div>
      
      <div class="search-filter">
        <div class="search-container">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索项目名称"
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon class="el-input__icon"><Search /></el-icon>
            </template>
          </el-input>
          <div class="input-backdrop"></div>
        </div>
        
        <div class="select-container">
          <el-select 
            v-model="selectedLanguage" 
            placeholder="编程语言"
            class="custom-select"
            @change="handleSearch"
            clearable
            popper-class="custom-select-dropdown"
          >
            <template #prefix>
              <el-icon class="select-icon"><Collection /></el-icon>
            </template>
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
        
        <div class="select-container sort-select">
          <el-select 
            v-model="sortOrder" 
            placeholder="排序方式"
            class="custom-select"
            @change="handleSearch"
            popper-class="custom-select-dropdown"
          >
            <template #prefix>
              <el-icon class="select-icon"><Sort /></el-icon>
            </template>
            <el-option label="最新发布" value="newest">
              <div class="option-content">
                <el-icon><Timer /></el-icon>
                <span class="option-label">最新发布</span>
              </div>
            </el-option>
            <el-option label="价格从低到高" value="priceAsc">
              <div class="option-content">
                <el-icon><SortUp /></el-icon>
                <span class="option-label">价格从低到高</span>
              </div>
            </el-option>
            <el-option label="价格从高到低" value="priceDesc">
              <div class="option-content">
                <el-icon><SortDown /></el-icon>
                <span class="option-label">价格从高到低</span>
              </div>
            </el-option>
            <el-option label="查看次数最多" value="lookDesc">
              <div class="option-content">
                <el-icon><View /></el-icon>
                <span class="option-label">查看次数最多</span>
              </div>
            </el-option>
          </el-select>
          <div class="select-backdrop"></div>
          
          <!-- 交易须知按钮 -->
          <el-tooltip content="点击查看交易须知" placement="top">
            <el-button 
              class="guide-button" 
              circle 
              type="info" 
              size="small"
              @click="showTradeGuide = true"
            >
              <el-icon><InfoFilled /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>

    <div class="code-list" v-loading="loading">
      <el-empty 
        v-if="!loading && (!codeList || codeList.length === 0)" 
        description="暂无项目代码" 
      />
      
      <div v-else class="code-grid">
        <el-card 
          v-for="code in codeList" 
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
      
      <div class="pagination-container" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
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
          <el-carousel :interval="5000" height="250px" arrow="always" indicator-position="outside">
            <el-carousel-item v-for="(image, index) in selectedCode.imageList" :key="index">
              <img :src="imgUrl + image" alt="项目预览图" class="detail-preview-image" />
            </el-carousel-item>
          </el-carousel>
        </div>
        <div v-else class="detail-image">
          <div class="image-placeholder">
            <el-icon :size="48" class="placeholder-icon"><Document /></el-icon>
            <p>暂无预览图</p>
          </div>
        </div>
        
        <h2 class="detail-title">{{ selectedCode.codeName }}</h2>
        <el-tag :type="getLanguageTagType(selectedCode.language)" size="small">{{ selectedCode.language }}</el-tag>
        
        <!-- 图片缩略图列表 -->
        <div class="image-thumbnails" v-if="selectedCode.imageList && selectedCode.imageList.length > 1">
          <h3>预览图片 ({{ selectedCode.imageList.length }}张)</h3>
          <div class="thumbnails-container">
            <div 
              v-for="(image, index) in selectedCode.imageList" 
              :key="index"
              class="thumbnail-item"
              @click="openImagePreview(selectedCode.imageList, index)"
            >
              <img :src="imgUrl + image" :alt="`预览图 ${index + 1}`" class="thumbnail-image" />
            </div>
          </div>
        </div>
        
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
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="primary" 
            class="confirm-btn"
            @click="handleDownload"
            :disabled="!selectedCode"
          >
            {{ selectedCode && selectedCode.price > 0 ? '购买并下载' : '免费下载' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 支付宝支付状态 -->
    <div v-if="payying" class="payment-status-container">
      <div class="status-content">
        <div class="icon-wrapper warning">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <h1>请在新页面完成支付</h1>
        <div class="warning-box">
          <el-icon><InfoFilled /></el-icon>
          <span>支付完成前请勿关闭或刷新当前页面</span>
        </div>
        <div class="status-info">
          <p>支付完成后将自动检测支付状态</p>
          <div class="loading-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
        <div class="cancel-payment">
          <a href="javascript:void(0)" @click="cancelPayment">取消支付</a>
        </div>
      </div>
    </div>
    
    <!-- 支付成功状态 -->
    <div v-if="showSucceed" class="payment-status-container">
      <div class="status-content success">
        <div class="icon-wrapper success">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
        <h1>支付成功</h1>
        <p class="success-text">您已成功购买该代码项目！</p>
        <div class="success-buttons">
          <el-button 
            type="primary" 
            class="view-button"
            @click="router.push('/my_code')"
          >
            <el-icon><View /></el-icon>
            <span>查看我的交易</span>
          </el-button>
          <el-button 
            class="back-button"
            @click="backToList"
          >
            <span>返回市场</span>
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 交易须知对话框 -->
    <el-dialog
      v-model="showTradeGuide"
      title="源码市场交易须知"
      width="50%"
      class="guide-dialog"
      :custom-class="'theme-dialog'"
    >
      <div class="guide-content">
        <div class="guide-section">
          <h3><el-icon><InfoFilled /></el-icon> 关于源码市场</h3>
          <p>源码市场是一个用于分享和交易代码项目的平台。在这里，您可以发布自己的代码项目、购买他人的优质代码，或免费分享您的作品。</p>
        </div>
        
        <div class="guide-section">
          <h3><el-icon><Wallet /></el-icon> 购买须知</h3>
          <ul>
            <li>购买前请认真阅读项目介绍，确保代码项目符合您的需求。</li>
            <li>付款成功后，可在"我的交易"中查看已购买的项目，并获取下载链接。</li>
            <li>目前支持支付宝付款，交易过程安全可靠。</li>
            <li>免费项目无需付款，直接点击下载即可。</li>
          </ul>
        </div>
        
        <div class="guide-section">
          <h3><el-icon><Upload /></el-icon> 发布须知</h3>
          <ul>
            <li>目前有人购买您的源码后暂不支持提现，交易完整功能将在以后版本实现(可以看作是赞助本网站awa)</li>
            <li>发布的代码项目必须是您拥有版权或有权分享的作品。</li>
            <li>提供清晰的项目介绍、功能说明和使用指南，有助于提高项目价值。</li>
            <li>上传高质量的预览图片，能够更好地展示您的项目。</li>
            <li>您可以为项目设置合理的价格，也可以选择免费分享。</li>
            <li>一旦有用户购买了您的项目，该项目将不能被删除。</li>
          </ul>
        </div>
        
        <div class="guide-section">
          <h3><el-icon><Warning /></el-icon> 免责声明</h3>
          <p>平台仅提供交易渠道，不对代码项目的质量和内容负责。请发布者确保代码无恶意内容，购买者在使用前进行必要的安全检查。</p>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="showTradeGuide = false">我知道了</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 图片预览弹窗 -->
    <el-dialog
      v-model="imagePreviewVisible"
      class="image-preview-dialog"
      width="80%"
      append-to-body
      destroy-on-close
      center
      :show-close="true"
    >
      <div class="full-image-preview">
        <el-carousel
          :initial-index="previewImageIndex"
          :autoplay="false"
          arrow="always"
          height="70vh"
          indicator-position="outside"
        >
          <el-carousel-item v-for="(image, index) in previewImageList" :key="index">
            <div class="preview-image-container">
              <img :src="image" :alt="`预览图 ${index + 1}`" class="full-preview-image" />
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { Search, View, Download, Document, Upload, Collection, Sort, Timer, SortUp, SortDown, WarningFilled, InfoFilled, CircleCheckFilled, Warning, Wallet } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, ElNotification, ElLoading } from 'element-plus';
import { useRouter } from 'vue-router';
import { GetCodeList, AddLook, buyCodeAlipay, checkCodePayStatus } from '../../api/BSideApi';
import axios from 'axios';

const router = useRouter();

// 数据状态
const searchKeyword = ref('');
const selectedLanguage = ref('');
const sortOrder = ref('newest');
const loading = ref(false);
const codeList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const dialogVisible = ref(false);
const selectedCode = ref(null);
const showTradeGuide = ref(false);

// 防止刷点击量相关变量
const viewedProjects = ref(new Map());
const VIEW_COOLDOWN = 3600000; // 1小时的冷却时间（毫秒）
const STORAGE_KEY = 'viewed_code_projects';

// 支付相关状态
const payying = ref(false);
const showSucceed = ref(false);
const payoutcome = ref({});

// 图片预览相关
const imagePreviewVisible = ref(false);
const previewImageList = ref([]);
const previewImageIndex = ref(0);

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

// 获取语言颜色
const getLanguageColor = (language) => {
  const colorMap = {
    'JavaScript': '#f7df1e',
    'Python': '#3776ab',
    'Java': '#007396',
    'C+': '#00599c',
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

// 初始化已查看项目记录
const initViewedProjects = () => {
  try {
    const storedData = localStorage.getItem(STORAGE_KEY);
    if (storedData) {
      const parsed = JSON.parse(storedData);
      
      // 转换回Map对象并过滤掉过期的记录
      const now = Date.now();
      viewedProjects.value = new Map(
        Object.entries(parsed)
          .filter(([_, timestamp]) => now - timestamp < VIEW_COOLDOWN * 24) // 保留24小时内的记录
          .map(([id, timestamp]) => [parseInt(id), timestamp])
      );
    }
  } catch (error) {
    console.error('加载已查看项目记录失败', error);
    viewedProjects.value = new Map();
  }
};

// 保存已查看项目记录
const saveViewedProjects = () => {
  try {
    // 将Map转换为普通对象以便JSON序列化
    const dataToStore = Object.fromEntries(viewedProjects.value);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(dataToStore));
  } catch (error) {
    console.error('保存已查看项目记录失败', error);
  }
};

// 检查是否可以增加查看次数
const canIncrementViews = (codeId) => {
  const lastViewed = viewedProjects.value.get(codeId);
  const now = Date.now();
  
  if (!lastViewed) {
    return true; // 从未查看过，可以增加
  }
  
  return (now - lastViewed) > VIEW_COOLDOWN; // 超过冷却时间才能再次增加
};

// 获取代码列表
const fetchCodeList = async () => {
  loading.value = true;
  try {
    // 构建请求参数并调用后端接口
    const res = await GetCodeList(searchKeyword.value, selectedLanguage.value);
    
    // 判断响应数据结构，处理数据
    if (res) {
      // 后端返回的数据可能在res.data.data中，根据实际情况调整
      const responseData = res || [];
      codeList.value = responseData;
      total.value = responseData.length; // 如果后端有返回总数，可以使用res.data.total
    } else {
      codeList.value = [];
      total.value = 0;
      ElMessage.warning(res || '没有查询到相关代码项目');
    }
  } catch (error) {
    console.error('获取代码列表失败:', error);
    ElMessage.error('获取代码列表失败，请稍后重试');
    codeList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchCodeList();
};

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size;
  fetchCodeList();
};

// 当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchCodeList();
};

// 查看详情
const viewDetail = async (code) => {
  selectedCode.value = code;
  dialogVisible.value = true;
  
  // 检查是否可以增加查看次数
  if (canIncrementViews(code.codeId)) {
    try {
      // 更新本地记录
      viewedProjects.value.set(code.codeId, Date.now());
      saveViewedProjects();
      
      // 调用API增加查看次数
      await AddLook(code.codeId);
      console.log('增加查看次数成功');
      
      // 更新本地数据中的查看次数
      if (selectedCode.value) {
        selectedCode.value.look = (selectedCode.value.look || 0) + 1;
        
        // 同时更新列表中的数据
        const codeIndex = codeList.value.findIndex(item => item.codeId === code.codeId);
        if (codeIndex !== -1) {
          codeList.value[codeIndex].look = selectedCode.value.look;
        }
      }
    } catch (error) {
      console.error('增加查看次数失败:', error);
      // 不向用户显示错误，静默失败
    }
  } else {
    console.log('该项目最近已查看过，不增加查看次数');
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  setTimeout(() => {
    selectedCode.value = null;
  }, 200);
};

// 添加取消支付功能
const cancelPayment = () => {
  ElMessageBox.confirm(
    '确定要取消支付吗？', 
    '取消支付', 
    {
      confirmButtonText: '确定',
      cancelButtonText: '继续支付',
      type: 'warning'
    }
  ).then(() => {
    payying.value = false;
    ElMessage({
      type: 'info',
      message: '已取消支付'
    });
  }).catch(() => {
    // 用户选择继续支付，不做处理
  });
};

// 处理下载
const handleDownload = async () => {
  if (!selectedCode.value) return;
  
  if (selectedCode.value.price > 0) {
    ElMessageBox.confirm(
      `确认购买该代码项目？价格: ¥${selectedCode.value.price.toFixed(2)}`,
      '购买确认',
      {
        confirmButtonText: '确认购买',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      try {
        // 关闭对话框，避免对话框和加载遮罩重叠
        dialogVisible.value = false;
        
        // 显示加载中
        const loading = ElLoading.service({
          fullscreen: true,
          text: "正在构建订单...",
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.7)",
        });
        
        // 调用支付宝购买接口
        const res = await buyCodeAlipay(selectedCode.value.codeId);
        payoutcome.value = res;
        
        // 显示支付状态页面
        loading.close();
        payying.value = true;
        
        // 在新窗口打开支付页面
        window.open(payoutcome.value.payUrl, '_blank');
        
        // 定时查询支付状态
        let timerId = setInterval(async function () {
          try {
            let statusRes = await checkCodePayStatus(payoutcome.value.ordersId);
            if (statusRes === "success") {
              ElNotification({
                title: "成功",
                message: "购买成功，可在我的交易中查看该记录",
                type: "success",
              });
              payying.value = false;
              showSucceed.value = true;
              clearInterval(timerId);
            } else if (statusRes === "cancel") {
              payying.value = false;
              ElNotification({
                title: "订单已关闭",
                message: "长时间未支付，订单已关闭",
                type: "error",
              });
              clearInterval(timerId);
            }
          } catch (err) {
            console.error('查询支付状态失败:', err);
            // 查询失败不中断轮询
          }
        }, 5000);
      } catch (error) {
        ElNotification({
          title: "错误",
          message: error.message || "支付过程中出现错误",
          type: "error",
        });
        payying.value = false;
        ElLoading.service().close();
      }
    }).catch(() => {
      // 取消购买，不做处理
    });
  } else {
    // 免费下载直接跳转
    if (selectedCode.value.downloadUrl) {
      window.open(selectedCode.value.downloadUrl, '_blank');
      dialogVisible.value = false;
    } else {
      ElMessage.error('下载链接不可用');
    }
  }
};

// 返回购买列表页
const backToList = () => {
  showSucceed.value = false;
  payying.value = false;
  fetchCodeList(); // 刷新列表数据
};

const imgUrl = ref('');
// 页面加载时获取数据
onMounted(() => {
  initViewedProjects(); // 初始化已查看项目记录
  fetchCodeList();
  imgUrl.value = process.env.VUE_APP_IMAGE;
});

const openImagePreview = (images, index) => {
  previewImageList.value = images.map(img => imgUrl.value + img);
  previewImageIndex.value = index;
  imagePreviewVisible.value = true;
};
</script>

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

<style lang="scss" scoped>
.code-market {
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
  
  h1 {
    color: var(--themeColor1);
    font-size: 36px;
    margin-bottom: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #8A2387, #E94057, #F27121);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    color: transparent;
    text-shadow: 0 2px 10px rgba(138, 35, 135, 0.2);
    position: relative;
    display: inline-block;
    
    &::after {
      content: attr(data-text);
      position: absolute;
      left: 0;
      right: 0;
      background: linear-gradient(135deg, #8A2387, #E94057, #F27121);
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
    background: linear-gradient(135deg, #A770EF, #CF8BF3, #FDB99B);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    color: transparent;
    opacity: 0.8;
  }
}

/* 添加用户操作区域样式 */
.user-actions {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 40px;
  perspective: 1000px;
  
  .action-card {
    width: 260px;
    height: 120px;
    border-radius: 16px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    padding: 20px;
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    z-index: 1;
    
    &::before {
      content: '';
      position: absolute;
      inset: 0;
      z-index: -2;
      filter: blur(10px);
      opacity: 0;
      transition: opacity 0.4s ease;
    }
    
    .card-backdrop {
      position: absolute;
      inset: 0;
      z-index: -1;
      opacity: 0.8;
      transition: all 0.3s ease;
    }
    
    .shine-effect {
      position: absolute;
      top: -100%;
      left: -100%;
      height: 60px;
      width: 100px;
      background: rgba(255, 255, 255, 0.2);
      transform: rotate(35deg);
      pointer-events: none;
      filter: blur(3px);
      z-index: 3;
      transition: all 0.6s ease;
    }
    
    .card-icon-wrapper {
      height: 50px;
      width: 50px;
      min-width: 50px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.15);
      backdrop-filter: blur(5px);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 15px;
      z-index: 2;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      
      .action-icon {
        font-size: 22px;
        color: white;
      }
    }
    
    .card-content {
      z-index: 2;
      
      .card-title {
        margin: 0 0 5px 0;
        font-size: 18px;
        font-weight: 600;
        color: white;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      
      .card-desc {
        margin: 0;
        font-size: 13px;
        color: rgba(255, 255, 255, 0.8);
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
      }
    }
    
    &:hover {
      transform: translateY(-10px) scale(1.03) rotateX(5deg);
      box-shadow: 0 20px 30px rgba(0, 0, 0, 0.15);
      
      &::before {
        opacity: 0.6;
      }
      
      .card-backdrop {
        opacity: 1;
        transform: scale(1.05);
      }
      
      .shine-effect {
        top: 150%;
        left: 150%;
        transition: all 0.8s ease-out;
      }
    }
    
    &:active {
      transform: translateY(-5px) scale(0.98);
    }
  }
  
  .publish-card {
    background: linear-gradient(135deg, #4776e6, #8e54e9);
    
    &::before {
      background: linear-gradient(135deg, #4776e6, #8e54e9);
    }
    
    .card-backdrop {
      background: linear-gradient(135deg, #4776e6, #8e54e9);
    }
  }
  
  .trade-card {
    background: linear-gradient(135deg, #11998e, #38ef7d);
    
    &::before {
      background: linear-gradient(135deg, #11998e, #38ef7d);
    }
    
    .card-backdrop {
      background: linear-gradient(135deg, #11998e, #38ef7d);
    }
  }

}

.search-filter {
  display: flex;
  max-width: 800px;
  margin: 0 auto 40px;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
  position: relative;
  z-index: 10;
  
  .search-container, .select-container {
    position: relative;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    
    &:hover {
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
      
      .input-backdrop, .select-backdrop {
        opacity: 1;
      }
    }
    
    .input-backdrop, .select-backdrop {
      position: absolute;
      inset: 0;
      background-color: var(--bgColor2);
      border-radius: 12px;
      z-index: -1;
      opacity: 0.8;
      transition: opacity 0.3s ease;
    }
  }
  
  .search-container {
    flex: 1;
    min-width: 280px;
    max-width: 400px;
    
    .search-input {
      width: 100%;
      
      :deep(.el-input__wrapper) {
        background-color: transparent;
        box-shadow: none !important;
        padding-left: 15px;
        backdrop-filter: blur(5px);
        border: 1px solid rgba(255, 255, 255, 0.1);
      }
      
      :deep(.el-input__inner) {
        color: var(--textColor1);
        height: 45px;
        font-weight: 500;
        
        &::placeholder {
          color: var(--textColor3);
          font-weight: normal;
        }
      }
      
      :deep(.el-input__icon) {
        color: var(--themeColor1);
      }
    }
    
    .input-backdrop {
      background: linear-gradient(135deg, rgba(113, 89, 193, 0.1), rgba(149, 90, 231, 0.2));
    }
  }
  
  .select-container {
    min-width: 140px;
    
    &.sort-select {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    
    .custom-select {
      width: 100%;
      
      :deep(.el-input__wrapper) {
        background-color: transparent;
        box-shadow: none !important;
        padding-left: 15px;
        backdrop-filter: blur(5px);
        border: 1px solid rgba(255, 255, 255, 0.1);
      }
      
      :deep(.el-input__inner) {
        color: var(--textColor1);
        height: 45px;
        font-weight: 500;
        
        &::placeholder {
          color: var(--textColor3);
          font-weight: normal;
        }
      }
      
      :deep(.el-select__caret) {
        color: var(--themeColor1);
        font-size: 16px;
        transition: all 0.3s ease;
      }
      
      .select-icon {
        color: var(--themeColor1);
        margin-right: 8px;
      }
    }
    
    &:nth-of-type(1) {
      .select-backdrop {
        background: linear-gradient(135deg, rgba(29, 151, 108, 0.1), rgba(147, 249, 185, 0.2));
      }
    }
    
    &.sort-select {
      .select-backdrop {
        background: linear-gradient(135deg, rgba(214, 48, 49, 0.1), rgba(253, 121, 168, 0.2));
      }
    }
  }
}

:deep(.custom-select-dropdown) {
  border-radius: 12px;
  border: none;
  overflow: hidden;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15), 0 5px 15px rgba(0, 0, 0, 0.08);
  background-color: var(--bgColor2);
  backdrop-filter: blur(10px);
  
  .el-scrollbar {
    .el-select-dropdown__wrap {
      max-height: 280px;
    }
  }
  
  .el-select-dropdown__item {
    display: flex;
    align-items: center;
    color: var(--textColor1);
    padding: 12px 20px;
    
    &.selected {
      color: var(--themeColor1);
      font-weight: 600;
      background-color: rgba(var(--themeColor1-rgb), 0.1);
    }
    
    &:hover, &:focus {
      background-color: rgba(var(--themeColor1-rgb), 0.05);
    }
    
    .option-content {
      display: flex;
      align-items: center;
      width: 100%;
      gap: 8px;
      
      .el-icon {
        font-size: 16px;
        color: var(--themeColor1);
      }
      
      .option-label {
        flex: 1;
      }
      
      .language-color {
        width: 12px;
        height: 12px;
        border-radius: 3px;
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
  
  .code-image {
    height: 160px;
    overflow: hidden;
    margin: -20px -20px 15px -20px;
    background-color: var(--dColor1);
    
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

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  margin-bottom: 30px;
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
    padding: 30px 10px;
    
    h1 {
      font-size: 32px;
      margin-bottom: 10px;
    }
    
    .subtitle {
      font-size: 14px;
      max-width: 280px;
      margin: 0 auto 30px;
    }
    
    .user-actions {
      flex-direction: column;
      gap: 15px;
      margin-bottom: 30px;
      
      .action-card {
        width: 100%;
        height: 80px;
        
        .card-icon-wrapper {
          width: 50px;
          height: 50px;
          
          .action-icon {
            font-size: 24px;
          }
        }
        
        .card-content {
          padding: 0 20px;
          
          .card-title {
            font-size: 16px;
          }
          
          .card-desc {
            font-size: 12px;
          }
        }
      }
    }
  }
  
  .search-filter {
    flex-direction: column;
    align-items: center;
    
    .search-container, .select-container {
      width: 100%;
      
      &.sort-select {
        margin-top: 10px;
        padding-right: 40px; /* 为信息按钮预留空间 */
      }
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

/* 支付状态样式 */
.payment-status-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(var(--bgColor2-rgb, 245, 245, 245), 0.95);
  animation: fadeIn 0.3s ease;
  backdrop-filter: blur(5px);
}

.status-content {
  background: var(--bgColor1);
  padding: 40px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  max-width: 500px;
  width: 90%;
  animation: scaleIn 0.3s ease;

  h1 {
    color: var(--textColor1);
    font-size: 24px;
    margin: 20px 0;
  }

  &.success {
    .success-text {
      color: var(--textColor2);
      margin: 16px 0 24px;
    }
  }
}

.icon-wrapper {
  margin-bottom: 20px;
  
  .el-icon {
    font-size: 64px;
    
    &.warning {
      color: #e6a23c;
    }
    
    &.success {
      color: #67c23a;
      animation: scaleIn 0.5s ease;
    }
  }
}

.warning-box {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #fdf6ec;
  color: #e6a23c;
  padding: 12px 20px;
  border-radius: 6px;
  margin: 20px 0;

  .el-icon {
    font-size: 18px;
  }
}

.status-info {
  margin-top: 30px;
  color: var(--textColor2);
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 12px;
  
  span {
    width: 8px;
    height: 8px;
    background: var(--themeColor1);
    border-radius: 50%;
    animation: dot-pulse 1.5s infinite;
    
    &:nth-child(2) {
      animation-delay: 0.2s;
    }
    
    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

.success-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
  
  .view-button, .back-button {
    padding: 12px 20px;
    
    .el-icon {
      margin-right: 6px;
    }
  }
  
  .back-button {
    background-color: var(--bgColor3);
    color: var(--textColor1);
    border: none;
    
    &:hover {
      background-color: var(--bgColor4);
      color: var(--textColor1);
    }
  }
}

@keyframes dot-pulse {
  0%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

.cancel-payment {
  margin-top: 20px;
  
  a {
    color: var(--textColor2);
    text-decoration: none;
    font-size: 14px;
    transition: all 0.2s ease;
    
    &:hover {
      color: var(--themeColor1);
      text-decoration: underline;
    }
  }
}

@media screen and (max-width: 480px) {
  .status-content {
    padding: 30px 20px;
    
    h1 {
      font-size: 20px;
    }
  }
  
  .warning-box {
    padding: 10px 16px;
    font-size: 14px;
  }
  
  .success-buttons {
    flex-direction: column;
    gap: 10px;
  }
}

/* 交易须知按钮样式 */
.guide-button {
  background: linear-gradient(135deg, var(--themeColor1-light), var(--themeColor1));
  color: white;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  margin-left: 10px;
  flex-shrink: 0;
  z-index: 20;
  position: relative;
  
  &:hover {
    transform: translateY(-2px) rotate(15deg);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    background: linear-gradient(135deg, var(--themeColor1), var(--themeColor1-dark));
  }
  
  @media (max-width: 768px) {
    position: absolute;
    right: 10px;
    top: 10px;
  }
}

/* 交易须知对话框样式 */
.guide-dialog {
  .guide-content {
    max-height: 60vh;
    overflow-y: auto;
    padding: 0 5px;
    
    .guide-section {
      margin-bottom: 25px;
      
      h3 {
        display: flex;
        align-items: center;
        gap: 8px;
        color: var(--themeColor1);
        font-size: 18px;
        margin-bottom: 12px;
        font-weight: 600;
      }
      
      p {
        color: var(--textColor2);
        line-height: 1.6;
        margin-bottom: 10px;
      }
      
      ul {
        margin: 0;
        padding-left: 20px;
        
        li {
          color: var(--textColor2);
          line-height: 1.6;
          margin-bottom: 8px;
          position: relative;
        }
      }
    }
  }
}

/* 图片轮播与预览样式 */
.detail-preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 6px;
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 250px;
  background-color: #f5f7fa;
  border-radius: 6px;
  border: 1px dashed #d9d9d9;
  color: #909399;
}

.placeholder-icon {
  margin-bottom: 12px;
  opacity: 0.5;
}

.image-thumbnails {
  margin-top: 20px;
}

.image-thumbnails h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #333;
  font-weight: 500;
}

.thumbnails-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  max-height: 120px;
  overflow-y: auto;
  padding-right: 5px;
}

.thumbnail-item {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #eee;
  transition: all 0.3s;
}

.thumbnail-item:hover {
  border-color: var(--el-color-primary);
  transform: scale(1.05);
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 全屏图片预览样式 */
.image-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.full-image-preview {
  width: 100%;
  background-color: rgba(0, 0, 0, 0.8);
}

.preview-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.full-preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* 轮播样式优化 */
:deep(.el-carousel__arrow) {
  background-color: rgba(0, 0, 0, 0.4);
}

:deep(.el-carousel__arrow:hover) {
  background-color: rgba(0, 0, 0, 0.7);
}

:deep(.el-carousel__indicators) {
  padding: 5px 10px;
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
}

:deep(.el-carousel__indicator--horizontal) {
  padding: 0 4px;
}

:deep(.el-carousel__button) {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
}

:deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background-color: #fff;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .thumbnails-container {
    max-height: 180px;
  }

  .thumbnail-item {
    width: 80px;
    height: 80px;
  }

  :deep(.el-carousel__arrow) {
    width: 30px;
    height: 30px;
  }
}
</style>
