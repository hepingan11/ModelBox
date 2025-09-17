<template>
  <div class="body">
    <div class="content">
      <h3 class="section-title">源码管理面板</h3>
      
      <div class="stats-section">
        <el-card class="stat-card">
          <div class="stat-icon">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ codeList.length }}</div>
            <div class="stat-label">总源码数</div>
          </div>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #67C23A;">
            <i class="el-icon-check"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ passedCount }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #F56C6C;">
            <i class="el-icon-close"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ rejectedCount }}</div>
            <div class="stat-label">未通过</div>
          </div>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #E6A23C;">
            <i class="el-icon-shopping-cart-full"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ exchangeList.length }}</div>
            <div class="stat-label">交易数量</div>
          </div>
        </el-card>
      </div>
      
      <!-- 标签页切换 -->
      <div class="tab-section">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="源码列表" name="code">
            <!-- 源码列表筛选部分 -->
            <div class="filter-section">
              <el-select v-model="statusFilter" placeholder="状态筛选" class="status-filter">
                <el-option label="全部" value=""></el-option>
                <el-option label="待审核" value="0"></el-option>
                <el-option label="已通过" value="1"></el-option>
              </el-select>
              
              <el-select v-model="languageFilter" placeholder="语言筛选" class="language-filter">
                <el-option label="全部" value=""></el-option>
                <el-option
                  v-for="option in languageOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </div>
            
            <!-- 源码列表内容 -->
            <div class="code-list">
              <el-card 
                v-for="code in filteredCodeList" 
                :key="code.id" 
                class="code-card"
                v-motion
                :initial="{ opacity: 0, y: 50 }"
                :enter="{ opacity: 1, y: 0 }"
              >
                <div class="card-content">
                  <div class="code-image hover-scale" v-if="code.imageList && code.imageList.length > 0">
                    <el-image :src="imageUrl+code.imageList[0]" fit="cover" @click="showImagePreview(code.imageList[0])"></el-image>
                  </div>
                  <div class="code-image hover-scale placeholder-image" v-else>
                    <div class="no-image">
                      <i class="el-icon-picture-outline"></i>
                      <span>暂无图片</span>
                    </div>
                  </div>
                  
                  <div class="code-info">
                    <div class="code-header">
                      <h3 class="fade-in">{{ code.codeName }}</h3>
                      <div class="code-status">
                        <el-tag 
                          :type="getStatusTagType(code.isPublic)"
                          class="status-tag"
                        >
                          {{ getStatusLabel(code.isPublic) }}
                        </el-tag>
                        <el-tag 
                          v-if="code.price > 0" 
                          type="danger"
                          class="status-tag"
                        >¥{{ code.price }}</el-tag>
                        <el-tag 
                          v-else 
                          type="info"
                          class="status-tag"
                        >免费</el-tag>
                      </div>
                    </div>
                    
                    <p class="code-intro">
                      <span class="code-id">ID: {{ code.id }}</span>
                      <span class="code-language">
                        <el-tag size="small" :type="getLanguageTagType(code.language)">
                          {{ getLanguageLabel(code.language) }}
                        </el-tag>
                      </span>
                      <span class="code-author" v-if="code.userName">作者: {{ code.userName }}</span>
                      <span class="code-time" v-if="code.createTime">{{ code.createTime }}</span>
                    </p>
                    
                    <p class="code-description" v-if="code.introduction">
                      {{ code.introduction.length > 80 ? code.introduction.substring(0, 80) + '...' : code.introduction }}
                    </p>
                    
                    <div class="action-buttons">
                      <el-button
                        type="primary"
                        @click="previewCode(code)"
                        size="small"
                        class="action-btn"
                      >预览</el-button>
                      
                      <el-button
                        v-if="code.isPublic === 0"
                        type="success"
                        @click="handlePass(code)"
                        size="small"
                        class="action-btn"
                      >通过审核</el-button>
                      
                      <el-button
                        v-if="code.isPublic === 1"
                        type="danger"
                        @click="handleReject(code)"
                        size="small"
                        class="action-btn"
                      >撤销审核</el-button>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
            
            <div v-if="filteredCodeList.length === 0" class="empty-state">
              暂无数据
            </div>
          </el-tab-pane>
          
          <!-- 交易记录标签页 -->
          <el-tab-pane label="交易记录" name="exchange">
            <div class="exchange-list" v-loading="loadingExchange">
              <!-- 筛选和搜索部分 -->
              <div class="exchange-filter">
                <el-input
                  v-model="exchangeSearchText"
                  placeholder="搜索订单号/用户名/项目名称"
                  class="exchange-search"
                  prefix-icon="el-icon-search"
                  clearable
                  @input="handleExchangeSearch"
                />
                
                <el-select
                  v-model="exchangeStatusFilter"
                  placeholder="订单状态"
                  class="exchange-filter-item"
                  clearable
                  @change="handleExchangeSearch"
                >
                  <el-option label="全部" value=""></el-option>
                  <el-option label="待支付" value="0"></el-option>
                  <el-option label="已支付" value="1"></el-option>
                  <el-option label="已取消" value="2"></el-option>
                  <el-option label="已退款" value="3"></el-option>
                </el-select>
                
                <el-select
                  v-model="exchangePaymentFilter"
                  placeholder="支付方式"
                  class="exchange-filter-item"
                  clearable
                  @change="handleExchangeSearch"
                >
                  <el-option label="全部" value=""></el-option>
                  <el-option label="支付宝" value="alipay"></el-option>
                  <el-option label="微信支付" value="wechat"></el-option>
                  <el-option label="余额支付" value="balance"></el-option>
                  <el-option label="免费" value="free"></el-option>
                </el-select>
              </div>
              
              <div class="table-container">
                <el-table
                  :data="paginatedExchangeList"
                  style="width: 100%"
                  border
                  stripe
                  class="exchange-table"
                  height="450"
                >
                  <el-table-column prop="orderNo" label="订单号" width="180">
                    <template #default="scope">
                      <span class="order-no">{{ scope.row.orderNo }}</span>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="userName" label="购买用户" width="120">
                    <template #default="scope">
                      <span class="user-name">{{ scope.row.userName }}</span>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="codeName" label="源码项目" min-width="180">
                    <template #default="scope">
                      <div class="code-name-cell">
                        <el-tag 
                          size="small" 
                          :type="getLanguageTagType(scope.row.language)"
                          class="language-tag"
                        >
                          {{ scope.row.language || '未知' }}
                        </el-tag>
                        <span class="code-name">{{ scope.row.codeName }}</span>
                      </div>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="price" label="价格" width="100">
                    <template #default="scope">
                      <span class="price" v-if="scope.row.price > 0">
                        ¥{{ scope.row.price.toFixed(2) }}
                      </span>
                      <el-tag size="small" type="success" v-else>免费</el-tag>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="authorName" label="源码作者" width="120">
                    <template #default="scope">
                      <span class="author-name">{{ scope.row.authorName || '未知' }}</span>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="payType" label="支付方式" width="120">
                    <template #default="scope">
                      <el-tag 
                        size="small" 
                        :type="getPaymentTagType(scope.row.payType)"
                      >
                        {{ getPaymentLabel(scope.row.payType) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag 
                        size="small" 
                        :type="getOrderStatusType(scope.row.status)"
                      >
                        {{ getOrderStatusLabel(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="createTime" label="交易时间" width="180">
                    <template #default="scope">
                      <span class="create-time">{{ scope.row.createTime }}</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <!-- 分页控件 -->
              <div class="pagination-container" v-if="filteredExchangeList.length > 0">
                <el-pagination
                  v-model:current-page="currentExchangePage"
                  :page-size="exchangePageSize"
                  :total="filteredExchangeList.length"
                  layout="total, prev, pager, next, jumper"
                  @current-change="handleExchangePageChange"
                  background
                ></el-pagination>
              </div>
              
              <div v-if="filteredExchangeList.length === 0 && !loadingExchange" class="empty-state">
                暂无交易记录
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- 项目预览对话框 -->
      <el-dialog
        title="项目预览"
        v-model="previewDialogVisible"
        width="60%"
        class="preview-dialog"
      >
        <div v-if="currentCode" class="preview-content">
          <div class="preview-section">
            <h3>基本信息</h3>
            <div class="info-item">
              <span class="label">项目名称：</span>
              <span>{{ currentCode.codeName }}</span>
            </div>
            <div class="info-item">
              <span class="label">编程语言：</span>
              <span>{{ getLanguageLabel(currentCode.language) }}</span>
            </div>
            <div class="info-item">
              <span class="label">价格：</span>
              <span>{{ currentCode.price > 0 ? `¥${currentCode.price}` : '免费' }}</span>
            </div>
            <div class="info-item">
              <span class="label">发布者：</span>
              <span>{{ currentCode.userName }}</span>
            </div>
            <div class="info-item">
              <span class="label">发布时间：</span>
              <span>{{ currentCode.createTime }}</span>
            </div>
          </div>
          
          <div class="preview-section">
            <h3>项目介绍</h3>
            <p class="project-description">{{ currentCode.introduction }}</p>
          </div>
          
          <div v-if="currentCode.imageList && currentCode.imageList.length > 0" class="preview-section">
            <h3>项目图片</h3>
            <div class="image-gallery">
              <div
                v-for="(image, index) in currentCode.imageList"
                :key="index"
                class="image-item"
                @click="showImagePreview(image)"
              >
                <img :src="imageUrl+image" alt="项目图片" />
              </div>
            </div>
          </div>
          
          <div class="preview-section">
            <h3>下载信息</h3>
            <div class="info-item">
              <span class="label">下载链接：</span>
              <el-link :href="currentCode.downloadUrl" type="primary" target="_blank">
                {{ currentCode.downloadUrl }}
              </el-link>
            </div>
            <div class="info-item">
              <span class="label">提取码：</span>
              <span>{{ currentCode.extractCode }}</span>
            </div>
          </div>
        </div>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="previewDialogVisible = false">关闭</el-button>
            <el-button
              v-if="currentCode && currentCode.isPublic === 0"
              type="success"
              @click="handlePass(currentCode)"
            >
              通过审核
            </el-button>
            <el-button
              v-if="currentCode && currentCode.isPublic === 1"
              type="danger"
              @click="handleReject(currentCode)"
            >
              拒绝审核
            </el-button>
          </div>
        </template>
      </el-dialog>
      
      <!-- 图片预览 -->
      <el-dialog v-model="imagePreviewVisible" class="image-preview-dialog">
        <img :src="imageUrl+previewImage" alt="预览图片" class="preview-image" />
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getAdminCodeList, passCode, rejectCode, getCodeExchangeList } from '../../../../api/BSideApi';

// 数据加载状态
const loading = ref(true);
const loadingExchange = ref(false);
// 源码列表
const codeList = ref([]);
// 交易记录列表
const exchangeList = ref([]);
// 当前预览的源码
const currentCode = ref(null);
// 对话框显示状态
const previewDialogVisible = ref(false);
const imagePreviewVisible = ref(false);
// 预览图片
const previewImage = ref('');
// 筛选条件
const searchText = ref('');
const statusFilter = ref('');
const languageFilter = ref('');
// 当前活动标签页
const activeTab = ref('code');

// 交易记录筛选和分页相关状态
const exchangeSearchText = ref('');
const exchangeStatusFilter = ref('');
const exchangePaymentFilter = ref('');
const currentExchangePage = ref(1);
const exchangePageSize = ref(10);

// 编程语言选项
const languageOptions = [
  { value: 'Java', label: 'Java' },
  { value: 'Python', label: 'Python' },
  { value: 'JavaScript', label: 'JavaScript' },
  { value: 'TypeScript', label: 'TypeScript' },
  { value: 'C', label: 'C' },
  { value: 'Go', label: 'Go' },
  { value: 'PHP', label: 'PHP' },
  { value: 'Ruby', label: 'Ruby' },
  { value: 'Swift', label: 'Swift' },
  { value: 'Kotlin', label: 'Kotlin' },
  { value: 'Rust', label: 'Rust' },
  { value: 'Other', label: '其他' }
];

// 计算属性：已通过的源码数量
const passedCount = computed(() => {
  return codeList.value.filter(code => code.isPublic === 1).length;
});

// 计算属性：未通过的源码数量
const rejectedCount = computed(() => {
  return codeList.value.filter(code => code.isPublic === 0).length;
});

// 计算属性：过滤后的源码列表
const filteredCodeList = computed(() => {
  return codeList.value.filter(code => {
    // 状态筛选
    if (statusFilter.value && code.isPublic !== parseInt(statusFilter.value)) {
      return false;
    }
    
    // 语言筛选
    if (languageFilter.value && code.language !== languageFilter.value) {
      return false;
    }
    
    // 搜索文本筛选
    if (searchText.value) {
      const text = searchText.value.toLowerCase();
      return (
        code.codeName.toLowerCase().includes(text) ||
        (code.userName && code.userName.toLowerCase().includes(text))
      );
    }
    
    return true;
  });
});

const imageUrl = ref('')
async function getImageUrl() {
  imageUrl.value = await getTopImgUrl();
}

// 获取语言标签类型
const getLanguageTagType = (language) => {
  const typeMap = {
    'Java': 'danger',
    'Python': 'info',
    'JavaScript': 'info',
    'TypeScript': 'info',
    'C': 'success',
    'Go': 'success',
    'PHP': 'danger',
    'Ruby': 'danger',
    'Swift': 'warning',
    'Kotlin': 'success',
    'Rust': 'danger',
    'Other': 'info'
  };
  
  return typeMap[language] || 'info';
};

// 获取语言标签文本
const getLanguageLabel = (language) => {
  const option = languageOptions.find(opt => opt.value === language);
  return option ? option.label : language;
};

// 获取状态标签类型
const getStatusTagType = (isPublic) => {
  const typeMap = {
    0: 'warning',  // 未通过
    1: 'success',  // 已通过
  };
  
  return typeMap[isPublic] || 'info';
};

// 获取状态标签文本
const getStatusLabel = (isPublic) => {
  const labelMap = {
    0: '待审核',
    1: '已通过'
  };
  
  return labelMap[isPublic] || '未知';
};

// 显示源码预览
const previewCode = (code) => {
  currentCode.value = code;
  previewDialogVisible.value = true;
};

// 显示图片预览
const showImagePreview = (imageUrl) => {
  previewImage.value = imageUrl;
  imagePreviewVisible.value = true;
};

// 通过源码审核
const handlePass = (code) => {
  ElMessageBox.confirm(
    `确定要通过《${code.codeName}》的审核吗？`,
    '审核确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    }
  )
    .then(() => {
      const loadingMsg = ElMessage({
        message: '正在提交...',
        type: 'info',
        duration: 0
      });
      
      passCode(code.codeId)
        .then(response => {
          loadingMsg.close();
          
          if (!response) {
            ElMessage({
              message: '审核通过成功！',
              type: 'success',
            });
            
            // 更新状态
            code.isPublic = 1;
            // 关闭预览对话框
            previewDialogVisible.value = false;
          } else {
            ElMessage({
              message: '操作失败，请重试',
              type: 'error'
            });
          }
        })
        .catch(error => {
          loadingMsg.close();
          console.error('通过审核失败:', error);
          ElMessage({
            message: '操作失败，请重试',
            type: 'error'
          });
        });
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 拒绝源码审核
const handleReject = (code) => {
  ElMessageBox.confirm(
    `确定要拒绝《${code.codeName}》的审核吗？`,
    '审核确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    }
  )
    .then(() => {
      const loadingMsg = ElMessage({
        message: '正在提交...',
        type: 'info',
        duration: 0
      });
      
      rejectCode(code.codeId)
        .then(response => {
          loadingMsg.close();
          
          if (!response) {
            ElMessage({
              message: '已撤销该源码项目',
              type: 'success'
            });
            
            // 更新状态
            code.isPublic = 0;
            // 关闭预览对话框
            previewDialogVisible.value = false;
          } else {
            ElMessage({
              message: '操作失败，请重试',
              type: 'error'
            });
          }
        })
        .catch(error => {
          loadingMsg.close();
          console.error('拒绝审核失败:', error);
          ElMessage({
            message: '操作失败，请重试',
            type: 'error'
          });
        });
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 加载源码列表
const loadCodeList = () => {
  loading.value = true;
  
  getAdminCodeList()
    .then(response => {
      loading.value = false;
      
      if (response) {
        codeList.value = response;
      } else {
        ElMessage({
          message: '获取源码列表失败',
          type: 'error'
        });
      }
    })
    .catch(error => {
      loading.value = false;
      console.error('获取源码列表失败:', error);
      ElMessage({
        message: '获取源码列表失败，请重试',
        type: 'error'
      });
    });
};

// 处理标签页点击
const handleTabClick = (tab) => {
  if (tab.props.name === 'exchange' && exchangeList.value.length === 0) {
    loadExchangeList();
  }
};

// 获取支付方式标签类型
const getPaymentTagType = (payType) => {
  const typeMap = {
    'alipay': 'primary',
    'wechat': 'success',
    'balance': 'warning',
    'free': 'info'
  };
  
  return typeMap[payType] || 'info';
};

// 获取支付方式标签文本
const getPaymentLabel = (payType) => {
  const labelMap = {
    'alipay': '支付宝',
    'wechat': '微信支付',
    'balance': '余额支付',
    'free': '免费'
  };
  
  return labelMap[payType] || '其他';
};

// 获取订单状态标签类型
const getOrderStatusType = (status) => {
  const typeMap = {
    0: 'warning',  // 待支付
    1: 'success',  // 已支付
    2: 'danger',   // 已取消
    3: 'info'      // 已退款
  };
  
  return typeMap[status] || 'info';
};

// 获取订单状态标签文本
const getOrderStatusLabel = (status) => {
  const labelMap = {
    0: '待支付',
    1: '已支付',
    2: '已取消',
    3: '已退款'
  };
  
  return labelMap[status] || '未知';
};

// 加载交易记录列表
const loadExchangeList = () => {
  loadingExchange.value = true;
  
  getCodeExchangeList()
    .then(response => {
      loadingExchange.value = false;
      
      if (response) {
        exchangeList.value = response;
      } else {
        ElMessage({
          message: '获取交易记录失败',
          type: 'error'
        });
      }
    })
    .catch(error => {
      loadingExchange.value = false;
      console.error('获取交易记录失败:', error);
      ElMessage({
        message: '获取交易记录失败，请重试',
        type: 'error'
      });
    });
};

// 计算属性：过滤后的交易记录
const filteredExchangeList = computed(() => {
  return exchangeList.value.filter(item => {
    // 状态筛选
    if (exchangeStatusFilter.value && item.status !== parseInt(exchangeStatusFilter.value)) {
      return false;
    }
    
    // 支付方式筛选
    if (exchangePaymentFilter.value && item.payType !== exchangePaymentFilter.value) {
      return false;
    }
    
    // 搜索文本筛选
    if (exchangeSearchText.value) {
      const text = exchangeSearchText.value.toLowerCase();
      return (
        (item.orderNo && item.orderNo.toLowerCase().includes(text)) ||
        (item.userName && item.userName.toLowerCase().includes(text)) ||
        (item.codeName && item.codeName.toLowerCase().includes(text))
      );
    }
    
    return true;
  });
});

// 计算属性：分页后的交易记录
const paginatedExchangeList = computed(() => {
  const start = (currentExchangePage.value - 1) * exchangePageSize.value;
  return filteredExchangeList.value.slice(start, start + exchangePageSize.value);
});

// 处理交易记录分页变化
const handleExchangePageChange = (page) => {
  currentExchangePage.value = page;
};

// 处理交易记录的搜索和筛选
const handleExchangeSearch = () => {
  // 重置为第一页
  currentExchangePage.value = 1;
};

// 组件挂载时加载数据
onMounted(() => {
  loadCodeList();
  imageUrl.value = process.env.VUE_APP_IMAGE;
});
</script>

<style scoped>
.body {
  padding: 20px;
  background-color: var(--bgColor2);
  height: calc(100vh - 40px);
  overflow-y: auto;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  padding-bottom: 400px;
}

.section-title {
  margin-bottom: 20px;
  color: var(--textColor1);
  font-size: 24px;
  font-weight: 600;
  opacity: 0;
  animation: fadeIn 0.5s ease forwards;
}

.stats-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
  opacity: 0;
  animation: fadeIn 0.5s ease 0.2s forwards;
}

.stat-card {
  flex: 1;
  min-width: 140px;
  display: flex;
  align-items: center;
  padding: 10px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  border-radius: 8px;
  background-color: var(--bgColor1);
  border: 1px solid var(--borderColor);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409EFF;
  margin-right: 12px;
  color: white;
  font-size: 18px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: var(--textColor1);
  margin-bottom: 2px;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: var(--textColor2);
  line-height: 1;
}

.filter-section {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.status-filter,
.language-filter {
  width: 150px;
}

.code-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.code-card {
  background-color: var(--bgColor1);
  border: 1px solid var(--borderColor);
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.code-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.25);
}

.card-content {
  display: flex;
  gap: 20px;
  padding: 20px;
}

.code-image {
  width: 200px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease;
  border: 1px solid var(--borderColor);
}

.placeholder-image {
  background-color: var(--bgColor3);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed var(--borderColor);
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--textColor3);
}

.no-image i {
  font-size: 32px;
  margin-bottom: 5px;
}

.hover-scale:hover {
  transform: scale(1.05);
}

.code-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.code-header h3 {
  margin: 0;
  color: var(--textColor1);
  font-size: 18px;
}

.code-status {
  display: flex;
  gap: 10px;
}

.status-tag {
  transition: all 0.3s ease;
}

.status-tag:hover {
  transform: scale(1.05);
}

.code-intro {
  color: var(--textColor2);
  margin: 10px 0;
  line-height: 1.5;
  font-size: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.code-id, .code-author, .code-time {
  opacity: 0.8;
}

.code-description {
  margin: 0 0 15px 0;
  line-height: 1.6;
  color: var(--textColor2);
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: auto;
}

.action-btn {
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: var(--textColor3);
  font-size: 16px;
  animation: fadeIn 0.5s ease;
  background-color: var(--bgColor1);
  border-radius: 10px;
  border: 1px solid var(--borderColor);
  margin-top: 20px;
}

/* 项目预览对话框样式 */
.preview-dialog :deep(.el-dialog) {
  background-color: var(--bgColor1);
  border: 1px solid var(--borderColor);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.preview-dialog :deep(.el-dialog__title) {
  color: var(--textColor1);
}

.preview-dialog :deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
  color: var(--textColor1);
}

.preview-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid var(--borderColor);
  padding: 15px 20px;
}

.preview-content {
  color: var(--textColor1);
}

.preview-section {
  margin-bottom: 25px;
  background-color: var(--bgColor2);
  padding: 15px;
  border-radius: 8px;
  border: 1px solid var(--borderColor);
}

.preview-section h3 {
  margin-top: 0;
  border-bottom: 1px solid var(--borderColor);
  padding-bottom: 10px;
  margin-bottom: 15px;
  color: var(--textColor1);
}

.info-item {
  margin-bottom: 10px;
  display: flex;
}

.info-item .label {
  font-weight: bold;
  min-width: 90px;
  color: var(--textColor2);
}

.project-description {
  white-space: pre-wrap;
  line-height: 1.6;
  color: var(--textColor2);
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  width: 120px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s;
  border: 1px solid var(--borderColor);
}

.image-item:hover {
  transform: scale(1.05);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview-dialog :deep(.el-dialog) {
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.85);
  border: none;
  box-shadow: none;
}

.preview-image {
  max-width: 90vw;
  max-height: 80vh;
  object-fit: contain;
  border: 1px solid var(--borderColor);
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

:deep(.el-card__body) {
  padding: 0;
}

/* 黑夜模式增强 */
:deep(.el-input__inner),
:deep(.el-select__input) {
  background-color: var(--bgColor2);
  border-color: var(--borderColor);
  color: var(--textColor1);
}

:deep(.el-select-dropdown) {
  background-color: var(--bgColor1);
  border: 1px solid var(--borderColor);
}

:deep(.el-select-dropdown__item) {
  color: var(--textColor2);
}

:deep(.el-select-dropdown__item.selected) {
  color: var(--primary-color);
}

:deep(.el-select-dropdown__item:hover) {
  background-color: var(--bgColor3);
}

:deep(.el-button) {
  border: 1px solid var(--borderColor);
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .stats-section {
    gap: 8px;
    margin-bottom: 16px;
  }

  .stat-card {
    min-width: 120px;
    padding: 8px 10px;
  }

  .stat-icon {
    width: 32px;
    height: 32px;
    margin-right: 8px;
    font-size: 14px;
  }

  .stat-value {
    font-size: 16px;
  }

  .stat-label {
    font-size: 11px;
  }

  .card-content {
    flex-direction: column;
  }

  .code-image {
    width: 100%;
    height: 160px;
  }

  .action-buttons {
    justify-content: center;
  }
}

/* 标签页样式 */
.tab-section {
  margin-bottom: 20px;
}

.tab-section :deep(.el-tabs__header) {
  margin-bottom: 20px;
  border-bottom: 1px solid var(--borderColor);
}

.tab-section :deep(.el-tabs__item) {
  color: var(--textColor2);
  font-size: 16px;
  padding: 0 20px;
  height: 40px;
  line-height: 40px;
  transition: all 0.3s ease;
}

.tab-section :deep(.el-tabs__item.is-active) {
  color: var(--themeColor1);
  font-weight: 600;
}

.tab-section :deep(.el-tabs__active-bar) {
  background-color: var(--themeColor1);
  height: 3px;
  border-radius: 3px;
}

/* 交易记录表格样式 */
.exchange-list {
  background-color: var(--bgColor1);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 40px;
  animation: fadeIn 0.5s ease;
}

.exchange-table {
  margin-bottom: 20px;
}

.exchange-table :deep(.el-table) {
  background-color: var(--bgColor1);
  color: var(--textColor1);
}

.exchange-table :deep(.el-table__header) {
  background-color: var(--bgColor2);
}

.exchange-table :deep(.el-table__header-wrapper th) {
  background-color: var(--bgColor2);
  color: var(--textColor1);
  font-weight: 600;
  padding: 12px 0;
}

.exchange-table :deep(.el-table__row) {
  background-color: var(--bgColor1);
  color: var(--textColor1);
  transition: all 0.3s ease;
}

.exchange-table :deep(.el-table__row:hover td) {
  background-color: var(--bgColor2) !important;
}

.exchange-table :deep(.el-table--striped .el-table__row--striped td) {
  background-color: var(--bgColor2);
}

.exchange-table :deep(.el-table--enable-row-hover .el-table__row:hover td) {
  background-color: var(--bgColor3) !important;
}

.exchange-table :deep(.el-table__cell) {
  padding: 12px 0;
}

.order-no {
  font-family: monospace;
  color: var(--textColor2);
  font-size: 12px;
}

.user-name, .author-name {
  font-weight: 500;
  color: var(--textColor1);
}

.code-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.language-tag {
  flex-shrink: 0;
}

.price {
  color: #ff7d00;
  font-weight: 600;
}

.create-time {
  color: var(--textColor3);
  font-size: 13px;
}

/* 交易记录表格样式补充 */
.exchange-filter {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.exchange-search {
  flex: 1;
  min-width: 200px;
}

.exchange-filter-item {
  width: 150px;
}

.table-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.pagination-container :deep(.el-pagination) {
  padding: 12px 0;
  background-color: var(--bgColor2);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.pagination-container :deep(.el-pagination__total) {
  color: var(--textColor2);
}

.pagination-container :deep(.el-pagination .el-pagination__jump) {
  color: var(--textColor2);
}

.pagination-container :deep(.el-pagination button) {
  background-color: transparent;
  color: var(--textColor3);
}

.pagination-container :deep(.el-pagination button:hover) {
  color: var(--themeColor1);
}

.pagination-container :deep(.el-pagination button:disabled) {
  color: var(--textColor4);
}

.pagination-container :deep(.el-pagination .el-pager li) {
  background-color: transparent;
  color: var(--textColor2);
}

.pagination-container :deep(.el-pagination .el-pager li:hover) {
  color: var(--themeColor1);
}

.pagination-container :deep(.el-pagination .el-pager li.is-active) {
  background-color: var(--themeColor1);
  color: #fff;
}

@media screen and (max-width: 768px) {
  .exchange-filter {
    flex-direction: column;
  }
  
  .exchange-filter-item {
    width: 100%;
  }
  
  .table-container {
    max-width: 100%;
    overflow-x: auto;
  }
}
</style> 