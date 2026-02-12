<template>
  <view class="chat-container" :class="themeClass">
    <!-- 顶部导航栏 -->
    <view class="custom-nav-bar">
      <view class="nav-left" @tap="toggleDrawer">
        <text class="nav-icon">☰</text>
        <text class="nav-title">历史对话</text>
      </view>
    </view>

    <!-- 侧边栏抽屉 -->
    <view class="drawer-mask" :class="{ show: showDrawer }" @tap="showDrawer = false"></view>
    <view class="drawer-content" :class="{ show: showDrawer }">
      <view class="drawer-header">
        <text class="drawer-title">对话列表</text>
        <view class="new-chat-btn" @tap="createNewChat">
          <text class="icon">＋</text> 新建对话
        </view>
      </view>
      
      <view class="search-box">
        <input 
          class="search-input" 
          v-model="searchText" 
          placeholder="搜索对话标题..." 
          confirm-type="search"
          placeholder-style="color: var(--textColor3)"
        />
      </view>
      
      <scroll-view scroll-y class="drawer-list">
        <view 
          v-for="(item, index) in filteredConversations" 
          :key="item.conversationId" 
          class="drawer-item" 
          :class="{ active: item.conversationId === chatId }"
          @tap="switchChat(item)"
        >
          <view class="item-main">
            <text class="item-title">{{ item.title || '新对话' }}</text>
            <text class="item-time">{{ formatTime(item.createdTime) }}</text>
          </view>
          <view class="item-actions">
            <view class="delete-btn" @tap.stop="deleteChat(item.conversationId)">🗑️</view>
          </view>
        </view>
        
        <view v-if="filteredConversations.length === 0" class="empty-tip">
          <text>{{ searchText ? '无匹配结果' : '暂无历史对话' }}</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 聊天内容区域 -->
    <scroll-view 
      class="chat-history" 
      scroll-y 
      :scroll-top="scrollTop" 
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
      @scrolltoupper="loadMoreHistory"
    >
      <view class="padding-space"></view>
      
      <!-- 加载更多loading -->
      <view v-if="historyLoading" class="loading-more">
        <text class="loading-text">加载历史记录...</text>
      </view>

       <!-- 欢迎界面 -->
      <view v-if="conversationList.length === 0 && !historyLoading" class="welcome-screen">
        <image class="welcome-logo" src="/static/logo.png" mode="aspectFit"></image>
        <view class="welcome-title">欢迎使用-模智盒</view>
        <view class="consume-info">
          <text class="consume-icon">🛍️</text>
          <text class="consume-text">付费模型每次提问消耗2个IT币</text>
        </view>
        <view class="warning-text">请注意不支持违法、违规等不当信息内容</view>
      </view>


      <view v-for="(item, index) in conversationList" :key="index" :id="'msg-'+index" class="message-item">
        
        <!-- 用户消息 -->
        <view v-if="!item.isAi" class="message-row user-row">
          <view class="content-wrapper">
             <view class="message-bubble user-bubble">
               <text user-select class="message-text">{{ item.content }}</text>
               <!-- 显示上传的文件/图片 -->
               <view v-if="item.media" class="media-content">
                 <image 
                   v-if="isImage(item.media)" 
                   :src="item.media" 
                   mode="widthFix" 
                   class="message-image"
                   @tap="previewImage(item.media)"
                 ></image>
                 <view v-else class="file-attachment">
                   <text class="file-name">📎 文件附件</text>
                 </view>
               </view>
             </view>
          </view>
        </view>
        
        <!-- AI消息 -->
        <view v-else class="message-row ai-row">
          <view class="content-wrapper">
            <view class="message-bubble ai-bubble">
              <!-- AI生成的图片 -->
              <view v-if="item.aiImage" class="media-content">
                 <image 
                   :src="item.aiImage" 
                   mode="widthFix" 
                   class="ai-image"
                   @tap="previewImage(item.aiImage)"
                 ></image>
              </view>
              
              <!-- 文字内容 -->
              <!-- 文字内容 -->
              <view v-if="!item.isAi">
                <text user-select class="message-text">{{ item.content }}</text>
              </view>
              <view v-else class="markdown-body">
                <rich-text :nodes="renderMarkdown(item.content)"></rich-text>
              </view>
              
              <!-- 加载中动画 -->
              <view v-if="item.loading" class="typing-indicator">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
            </view>
            
            <!-- 底部操作栏 -->
            <view class="message-actions" v-if="!item.loading && item.content">
              <view class="action-btn" @tap="copyText(item.content)">
                <image class="action-icon-img" src="/static/icon/copy.svg" mode="aspectFit"></image> 复制
              </view>
              <view 
                class="action-btn" 
                @tap="handleCollection(item, index)" 
                :class="{ 'collected': item.isCollection }"
              >
                <text class="action-icon">{{ item.isCollection ? '🌟' : '⭐' }}</text> 
                {{ item.isCollection ? '已收藏' : '收藏' }}
              </view>
              <view class="model-tag" v-if="item.model">
                {{ item.model }}
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="padding-space-bottom" id="bottom-marker"></view>
    </scroll-view>

    <!-- 底部输入区域 -->
    <view class="input-area">
      <!-- 附件预览 -->
      <view v-if="selectedFile" class="file-preview">
        <view class="preview-content">
          <image 
            v-if="isImage(selectedFile.path)" 
            :src="selectedFile.path" 
            class="preview-thumb" 
            mode="aspectFill"
          ></image>
          <text v-else class="file-icon-preview">📄</text>
          <text class="file-name">{{ selectedFile.name }}</text>
        </view>
        <view class="close-btn" @tap="clearFile">×</view>
      </view>
      
      <view class="input-row">
        <view class="icon-btn" @tap="toggleSettings">
          <image class="icon-img" src="/static/icon/setting.svg" mode="aspectFit"></image>
        </view>
        
        <view class="icon-btn" @tap="chooseFile">
          <image class="icon-img" src="/static/icon/picture.svg" mode="aspectFit"></image>
        </view>
        
        <textarea 
          class="chat-input" 
          v-model="inputContent" 
          confirm-type="send" 
          @confirm="handleSend" 
          placeholder="问点什么..." 
          :disabled="aiLoading"
          auto-height
          :maxlength="-1"
        />
        
        
        <view 
          class="send-btn" 
          @tap="handleSend" 
          :class="{ disabled: (!inputContent.trim() && !selectedFile) || aiLoading }"
        >
          <image v-if="!aiLoading" class="send-icon" src="/static/icon/send.svg" mode="aspectFit"></image>
          <text v-else class="loading-dot">···</text>
        </view>
      </view>
    </view>
    
    <!-- Settings Popup -->
    <view v-if="showSettings" class="settings-modal" @tap.stop="toggleSettings">
      <view class="settings-content" @tap.stop>
        <view class="settings-header">
           <text class="settings-title">功能设置</text>
           <view class="close-btn" @tap="toggleSettings">×</view>
        </view>
        
        <!-- 模型选择 -->
        <view class="setting-item model-setting-item">
           <view class="label-box">
              <text class="item-label">对话模型</text>
              <text class="item-desc">选择AI对话使用的模型</text>
           </view>
        </view>
        <view class="model-grid">
          <view 
            v-for="(opt, idx) in modelOptions" 
            :key="opt.value" 
            class="model-chip" 
            :class="{ active: model === opt.value }"
            @tap="selectModel(opt, idx)"
          >
            <text class="chip-label">{{ opt.text }}</text>
            <text v-if="model === opt.value" class="chip-check">✓</text>
          </view>
        </view>
        
        <view class="setting-item">
           <view class="label-box">
              <text class="item-label">RAG 知识库</text>
              <text class="item-desc">启用后将基于现有知识库回答</text>
           </view>
           <view class="setting-right">
             <view class="rag-manage-btn" @tap="openRagManagement">
               <text>管理</text>
             </view>
             <switch :checked="isRag" @change="e => isRag = e.detail.value" color="#7b61ff" style="transform:scale(0.8)"/>
           </view>
        </view>
        
        <view class="setting-item">
           <view class="label-box">
              <text class="item-label">MCP 插件</text>
              <text class="item-desc">启用插件扩展能力</text>
           </view>
           <switch :checked="isMcp" @change="e => isMcp = e.detail.value" color="#7b61ff" style="transform:scale(0.8)"/>
        </view>
        
        <scroll-view scroll-y v-if="isMcp" class="mcp-list-scroll">
           <view class="mcp-list">
              <view class="mcp-list-title">可用插件</view>
              <view v-if="mcpListLoading" class="loading-text">加载中...</view>
              <view v-else-if="mcpList.length === 0" class="empty-text">无可用插件</view>
              <view v-for="mcp in mcpList" :key="mcp.mcpsId" class="mcp-item" @tap="toggleMcp(mcp.mcpsId)" :class="{active: selectedMcpIds.includes(mcp.mcpsId)}">
                 <view class="checkbox">
                    <text v-if="selectedMcpIds.includes(mcp.mcpsId)" class="check-mark">✓</text>
                 </view>
                 <image :src="mcp.icon" class="mcp-icon" mode="aspectFit" v-if="mcp.icon"></image>
                 <view class="mcp-info">
                   <text class="mcp-name">{{ mcp.mcpName }}</text>
                   <text class="mcp-desc">{{ mcp.introduce }}</text>
                 </view>
              </view>
           </view>
        </scroll-view>
      </view>
    </view>
    
    <!-- RAG Management Modal -->
    <view v-if="showRagManagement" class="settings-modal" @tap.stop="showRagManagement = false">
      <view class="settings-content rag-content" @tap.stop>
        <view class="settings-header">
           <text class="settings-title">RAG知识库管理</text>
           <view class="close-btn" @tap="showRagManagement = false">×</view>
        </view>
        
        <!-- Upload Area -->
        <view class="rag-upload-area" @tap="chooseRagFile">
          <text class="upload-icon">☁️</text>
          <text class="upload-text">点击上传文件</text>
          <text class="upload-tip">支持 .txt, .md, .pdf 格式，单个不超过5MB</text>
        </view>
        
        <!-- Uploading Progress -->
        <view v-if="ragUploading" class="rag-uploading">
          <text>📤 正在上传...</text>
        </view>
        
        <!-- File List -->
        <view class="rag-file-list">
          <view class="rag-list-header">
            <text class="rag-list-title">已添加的知识库文件 ({{ knowledgeFiles.length }})</text>
            <view class="refresh-btn" @tap="fetchKnowledgeFiles">
              <text>🔄 刷新</text>
            </view>
          </view>
          
          <view v-if="knowledgeListLoading" class="loading-text">加载中...</view>
          <view v-else-if="knowledgeFiles.length === 0" class="empty-text">暂无知识库文件</view>
          
          <scroll-view scroll-y class="rag-files-scroll" v-if="knowledgeFiles.length > 0">
            <view v-for="file in knowledgeFiles" :key="file.ragId" class="rag-file-item">
              <view class="file-info-row">
                <text class="file-icon-tag">📄</text>
                <view class="file-details">
                  <text class="file-name-text">{{ file.ragName }}</text>
                  <text class="file-meta">{{ formatFileSize(file.size) }} · {{ formatTime(file.createdTime) }}</text>
                </view>
              </view>
              <view class="file-actions-row">
                <view class="status-tag" :class="file.isEnable ? 'enabled' : 'disabled'">
                  <text>{{ file.isEnable ? '已启用' : '已禁用' }}</text>
                </view>
                <view class="file-action-btn toggle-btn" :class="file.isEnable ? 'warn' : 'success'" @tap="toggleRagStatus(file)">
                  <text>{{ file.isEnable ? '禁用' : '启用' }}</text>
                </view>
                <view class="file-action-btn delete-btn" @tap="deleteRagFile(file)">
                  <text>删除</text>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { store } from '@/store';

import { useTheme } from '@/hooks/useTheme';

import { apiBaseUrl } from '@/store';
import { marked } from 'marked';

// Configure marked
marked.setOptions({
  breaks: true,
  gfm: true
});

// 状态变量
const { themeClass } = useTheme();

// RAG & MCP State
const showSettings = ref(false);
const isRag = ref(false);
const isMcp = ref(false);
const selectedMcpIds = ref([]);
const mcpList = ref([]);
const mcpListLoading = ref(false);
// RAG Management State
const showRagManagement = ref(false);
const knowledgeFiles = ref([]);
const knowledgeListLoading = ref(false);
const ragUploading = ref(false);
const inputContent = ref('');
const conversationList = ref([]); // 当前聊天的消息列表
const historyList = ref([]); // 历史会话列表
const showDrawer = ref(false); // 控制抽屉显示
const searchText = ref(''); // 搜索关键词
const scrollTop = ref(0);
const scrollIntoView = ref('');
const aiLoading = ref(false);
const historyLoading = ref(false);
const historyHasMore = ref(true);
const currentPage = ref(1);
const chatId = ref('');
const selectedFile = ref(null);
const model = ref('GLM'); // 默认模型
const modelOptions = [
  { value: 'GPT', text: 'GPT-5.2-high(付费)' },
  { value: 'GLM', text: 'GLM-5(推荐)' },
  { value: 'CLAUDE', text: 'Claude-4.5-sonnet(付费)' },
  { value: 'QWEN', text: '通义千问3' },
  { value: 'DEEPSEEK', text: 'DeepSeek' },
  { value: 'DEEPSEEK_R', text: 'DeepSeek深度思考(付费)' },
  { value: 'GEMINI', text: 'Gemini-3-pro(付费)' },
  { value: 'GROK', text: 'Grok-4-Fast' },
  { value: 'DOUBAO', text: '豆包1.6' }
];
const modelIndex = ref(1); // GLM default index
const userAvatar = computed(() => store.state.userInfo?.avatar || '/static/user.png');

// 模型改变
const selectModel = (opt, idx) => {
  model.value = opt.value;
  modelIndex.value = idx;
};
// 兼容旧的 onModelChange
const onModelChange = (e) => {
  modelIndex.value = e.detail.value;
  model.value = modelOptions[modelIndex.value].value;
};

// 选择图片
const chooseFile = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0];
      const tempFile = res.tempFiles[0];
      selectedFile.value = {
        path: tempFilePath,
        name: tempFile.name || '图片', // H5有name, 小程序可能没有
        size: tempFile.size
      };
    }
  });
};

// 清除选择的文件
const clearFile = () => {
  selectedFile.value = null;
};


// 生成随机ChatID
const generateChatId = () => Date.now().toString() + Math.floor(Math.random() * 1000);

// 获取ChatID (这里的逻辑是简化版，实际应该从路由或Vuex获取当前选中的对话)
const getOrCreateChatId = () => {
  if (!chatId.value) {
    chatId.value = generateChatId();
  }
  return chatId.value;
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    scrollIntoView.value = 'bottom-marker';
    // 强制触发滚动
    setTimeout(() => {
      scrollTop.value = 999999;
    }, 100);
  });
};

// 判断是否图片
const isImage = (url) => {
  if (!url) return false;
  return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
};

// 预览图片
const previewImage = (url) => {
  uni.previewImage({
    urls: [url]
  });
};

// 复制文本
const copyText = (text) => {
  uni.setClipboardData({
    data: text,
    success: () => uni.showToast({ title: '已复制', icon: 'none' })
  });
};

// 渲染Markdown
const renderMarkdown = (content) => {
  if (!content) return '';
  try {
    // 处理成 rich-text 可用的节点或者直接 HTML
    // uni-app rich-text 支持 HTML String
    // 稍微处理一下换行和代码块样式，防止溢出
    let html = marked.parse(content);
    // 简单的样式注入，虽然 rich-text 样式有限
    html = html.replace(/<pre>/g, '<pre style="overflow-x: auto; background: #2d2d2d; padding: 10px; border-radius: 4px; color: #ccc; margin: 8px 0;">');
    html = html.replace(/<code>/g, '<code style="background: rgba(255,255,255,0.1); padding: 2px 4px; border-radius: 2px; font-family: monospace;">');
    // 替换 p 标签样式，增加行间距
    html = html.replace(/<p>/g, '<p style="margin-bottom: 8px; line-height: 1.6;">');
    // 列表样式
    html = html.replace(/<ul>/g, '<ul style="margin-left: 10px; padding-left: 10px;">');
    html = html.replace(/<ol>/g, '<ol style="margin-left: 10px; padding-left: 10px;">');
    return html;
  } catch (e) {
    return content;
  }
};

// 收藏消息
const handleCollection = async (item, index) => {
  if (item.isCollection) return; // 已经收藏过了

  try {
    // 查找上一条用户消息作为 issue
    let issue = '';
    // 向前遍历查找最近的一条用户消息
    for (let i = index - 1; i >= 0; i--) {
      // 兼容两种字段判断方式
      const prevItem = conversationList.value[i]; 
      if (!prevItem.isAi) {
        issue = prevItem.content;
        break;
      }
    }

    if (!issue) {
      // 如果没找到用户消息，可能这是第一条或者是连续的AI消息
      // 尝试使用当前对话的上下文或者默认提示
      issue = '对话内容';
    }

    const res = await uni.request({
      url: `${apiBaseUrl}/user/stat/put/data`,
      method: 'POST',
      header: {
        'token': uni.getStorageSync('token'),
        'Content-Type': 'application/json'
      },
      data: {
        issue: issue,
        answer: item.content
      }
    });

    if (res.statusCode === 200 && (res.data.code === 200 || res.data.code === 0)) { // 兼容不同后端成功码
       item.isCollection = true;
       uni.showToast({ title: '收藏成功', icon: 'success' });
    } else {
       uni.showToast({ title: '收藏失败: ' + (res.data.msg || '网络错误'), icon: 'none' });
    }
  } catch (e) {
    console.error('收藏失败', e);
    uni.showToast({ title: '收藏失败', icon: 'none' });
  }
};



// 加载历史记录
const loadHistory = async (isLoadMore = false) => {
  if (historyLoading.value || (!historyHasMore.value && isLoadMore)) return;
  
  historyLoading.value = true;
  if (!isLoadMore) {
    currentPage.value = 1;
    conversationList.value = [];
  }

  try {
    const currentId = getOrCreateChatId();
    // 模拟API请求路径
    const res = await uni.request({
      url: `${apiBaseUrl}/chat/history`, // 根据DialogueView推测
      method: 'GET',
      data: {
        chatId: currentId,
        pageNum: currentPage.value,
        pageSize: 20
      },
      header: {
        'token': uni.getStorageSync('token')
      }
    });

    if (res.statusCode === 200 && res.data.code === 200) {
      const records = res.data.data.records || [];
      const total = res.data.data.total || 0;
      const size = res.data.data.size || 20;
      
      // 格式化消息
      const formatted = records.map(item => {
        // 优先使用 type 字段判断消息类型，如果没有则回退到 role
        const msgType = (item.type || item.role || '').toUpperCase();
        const isAi = msgType === 'ASSISTANT' || msgType === 'MODEL';
        return {
          type: msgType,
          role: msgType, // 保持兼容
          content: item.content,
          isAi: isAi,
          media: item.media,
          aiImage: item.media && isAi ? item.media : null, // 假设AI生成的图片在media里
          model: item.model,
          isCollection: item.isCollection || false // 映射收藏状态
        };
      }).reverse(); // 历史记录通常是倒序的，需要反转显示

      if (isLoadMore) {
        conversationList.value = [...formatted, ...conversationList.value];
        // 保持滚动位置的逻辑比较复杂，暂时不做
      } else {
        conversationList.value = formatted;
        scrollToBottom();
      }

      historyHasMore.value = (currentPage.value * size) < total;
      if (historyHasMore.value) currentPage.value++;
    }
  } catch (e) {
    console.error('加载历史失败', e);
  } finally {
    historyLoading.value = false;
  }
};

const loadMoreHistory = () => loadHistory(true);

// ========================= RAG & MCP Methods =========================
const toggleSettings = () => {
    showSettings.value = !showSettings.value;
    if (showSettings.value && mcpList.value.length === 0) {
        fetchMcpServices();
    }
};

const fetchMcpServices = async () => {
    mcpListLoading.value = true;
    try {
        const token = uni.getStorageSync('token');
        const res = await uni.request({
            url: `${apiBaseUrl}/chat/mcp/list`,
            header: { 'token': token }
        });
        if (res.statusCode === 200 && (res.data.code === 200 || res.data.code === 0)) {
            mcpList.value = res.data.data || [];
        }
    } catch (e) {
        console.error('获取MCP列表失败', e);
    } finally {
        mcpListLoading.value = false;
    }
};

const toggleMcp = (id) => {
    if (!id) return;
    const index = selectedMcpIds.value.indexOf(id);
    if (index > -1) {
        selectedMcpIds.value.splice(index, 1);
    } else {
        selectedMcpIds.value.push(id);
    }
};

// ========================= RAG 管理方法 =========================
const openRagManagement = () => {
    showRagManagement.value = true;
    fetchKnowledgeFiles();
};

const fetchKnowledgeFiles = async () => {
    knowledgeListLoading.value = true;
    try {
        const token = uni.getStorageSync('token');
        const res = await uni.request({
            url: `${apiBaseUrl}/chat/rag/list`,
            header: { 'token': token }
        });
        if (res.statusCode === 200) {
            const data = res.data.data || res.data;
            if (Array.isArray(data)) {
                knowledgeFiles.value = data.map(item => ({
                    ragId: item.ragId,
                    ragName: item.ragName,
                    ragUrl: item.ragUrl,
                    userId: item.userId,
                    isEnable: item.isEnable,
                    size: estimateFileSize(item.ragUrl),
                    createdTime: item.createdTime
                }));
            } else {
                knowledgeFiles.value = [];
            }
        }
    } catch (e) {
        console.error('获取知识库列表失败', e);
        uni.showToast({ title: '获取列表失败', icon: 'none' });
    } finally {
        knowledgeListLoading.value = false;
    }
};

const estimateFileSize = (url) => {
    if (!url) return 0;
    const ext = url.toLowerCase().split('.').pop();
    const sizeMap = { txt: 102400, md: 51200, pdf: 204800 };
    return sizeMap[ext] || 51200;
};

const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
};

const chooseRagFile = () => {
    // #ifdef H5
    // H5 使用 input[type=file]
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.txt,.md,.pdf';
    input.onchange = (e) => {
        const file = e.target.files[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) {
                uni.showToast({ title: '文件不能超过5MB', icon: 'none' });
                return;
            }
            uploadRagFileH5(file);
        }
    };
    input.click();
    // #endif
    
    // #ifndef H5
    uni.chooseMessageFile({
        count: 1,
        type: 'file',
        extension: ['txt', 'md', 'pdf'],
        success: (res) => {
            if (res.tempFiles && res.tempFiles.length > 0) {
                const file = res.tempFiles[0];
                if (file.size > 5 * 1024 * 1024) {
                    uni.showToast({ title: '文件不能超过5MB', icon: 'none' });
                    return;
                }
                uploadRagFile(file.path, file.name);
            }
        },
        fail: (err) => {
            console.error('选择文件失败', err);
        }
    });
    // #endif
};

// H5 上传
const uploadRagFileH5 = async (file) => {
    ragUploading.value = true;
    try {
        const token = uni.getStorageSync('token');
        const formData = new FormData();
        formData.append('file', file);
        
        const response = await fetch(`${apiBaseUrl}/chat/rag/upload`, {
            method: 'POST',
            headers: { 'token': token },
            body: formData
        });
        const result = await response.json();
        if (response.ok) {
            uni.showToast({ title: '上传成功', icon: 'success' });
            fetchKnowledgeFiles();
        } else {
            uni.showToast({ title: result.msg || '上传失败', icon: 'none' });
        }
    } catch (e) {
        console.error('上传失败', e);
        uni.showToast({ title: '上传失败', icon: 'none' });
    } finally {
        ragUploading.value = false;
    }
};

// 小程序/App 上传
const uploadRagFile = (filePath, fileName) => {
    ragUploading.value = true;
    const token = uni.getStorageSync('token');
    uni.uploadFile({
        url: `${apiBaseUrl}/chat/rag/upload`,
        filePath: filePath,
        name: 'file',
        header: { 'token': token },
        success: (res) => {
            if (res.statusCode === 200) {
                uni.showToast({ title: `${fileName} 上传成功`, icon: 'success' });
                fetchKnowledgeFiles();
            } else {
                let msg = '上传失败';
                try {
                    const data = JSON.parse(res.data);
                    msg = data.msg || msg;
                } catch(e) {}
                uni.showToast({ title: msg, icon: 'none' });
            }
        },
        fail: () => {
            uni.showToast({ title: '上传失败', icon: 'none' });
        },
        complete: () => {
            ragUploading.value = false;
        }
    });
};

const toggleRagStatus = async (file) => {
    try {
        const token = uni.getStorageSync('token');
        const res = await uni.request({
            url: `${apiBaseUrl}/chat/rag/toggle/${file.ragId}`,
            method: 'POST',
            header: { 'token': token }
        });
        if (res.statusCode === 200) {
            file.isEnable = !file.isEnable;
            uni.showToast({ title: file.isEnable ? '已启用' : '已禁用', icon: 'none' });
        } else {
            uni.showToast({ title: '操作失败', icon: 'none' });
        }
    } catch (e) {
        console.error('切换状态失败', e);
        uni.showToast({ title: '操作失败', icon: 'none' });
    }
};

const deleteRagFile = (file) => {
    uni.showModal({
        title: '确认删除',
        content: `确定要删除文件 "${file.ragName}" 吗？此操作不可撤销。`,
        confirmText: '删除',
        confirmColor: '#ff4d4f',
        success: async (res) => {
            if (res.confirm) {
                try {
                    const token = uni.getStorageSync('token');
                    const delRes = await uni.request({
                        url: `${apiBaseUrl}/chat/rag/delete/${file.ragId}`,
                        header: { 'token': token }
                    });
                    if (delRes.statusCode === 200) {
                        const index = knowledgeFiles.value.findIndex(f => f.ragId === file.ragId);
                        if (index !== -1) {
                            knowledgeFiles.value.splice(index, 1);
                        }
                        uni.showToast({ title: '删除成功', icon: 'success' });
                    } else {
                        uni.showToast({ title: '删除失败', icon: 'none' });
                    }
                } catch (e) {
                    console.error('删除失败', e);
                    uni.showToast({ title: '删除失败', icon: 'none' });
                }
            }
        }
    });
};

// 发送消息处理
const handleSend = async () => {
  if (!inputContent.value.trim() && !selectedFile.value) return;
  if (aiLoading.value) return;

  const content = inputContent.value;
  inputContent.value = ''; // 清空输入
  
  // 添加用户消息到列表
  conversationList.value.push({
    isAi: false,
    type: 'USER',
    role: 'USER',
    content: content,
    media: selectedFile.value ? selectedFile.value.path : null
  });
  
  scrollToBottom();
  
  // 添加AI占位消息
  const aiMsgIndex = conversationList.value.length;
  conversationList.value.push({
    isAi: true,
    type: 'ASSISTANT',
    role: 'ASSISTANT',
    content: '',
    loading: true,
    model: model.value
  });
  
  aiLoading.value = true;
  
  try {
    const token = uni.getStorageSync('token');
    const cid = getOrCreateChatId();
    
    // 1. 意图识别 (API path from DialogueView)
    const transitRes = await uni.request({
      url: `${apiBaseUrl}/chat/transit`,
      method: 'POST',
      header: { 
        'Content-Type': 'application/json',
        'token': token
      },
      data: { message: content }
    });

    let intent = 'TEXT';
    let transitData = {};
    
    if (transitRes.statusCode === 200 && transitRes.data.code === 200) {
      transitData = transitRes.data.data || transitRes.data;
      intent = (transitData.type || 'TEXT').toUpperCase();
    }

    if (intent === 'DRAW') {
        // --- 绘图流程 ---
        conversationList.value[aiMsgIndex].content = transitData.content || '正在绘制...';
        
        // 发起绘图请求
        // 注意：这里需要 FormData 格式，UniApp request 传对象默认是 JSON 或 urlencoded
        // 如果后端强制 multipart/form-data，这里可能需要 hack 或 uploadFile
        // 尝试用 JSON 发送，如果后端支持
        
        /* 
           由于 DialogueView 使用 FormData，我们尝试模拟 FormData 行为。
           但是 uni.request 无法直接发送 FormData 对象。
           如果必须上传文件，使用 uni.uploadFile。
           如果没有文件，尝试构造 multipart body 或者后端是否兼容 JSON。
           这里假设后端兼容 JSON 或者 application/x-www-form-urlencoded
        */
       
        let drawRes;
        
        if (selectedFile.value) {
             const uploadRes = await new Promise((resolve, reject) => {
                uni.uploadFile({
                    url: `${apiBaseUrl}/draw/zhipu/image`,
                    filePath: selectedFile.value.path,
                    name: 'file',
                    header: { 
                        'token': token,
                    },
                    formData: {
                        prompt: content,
                        size: transitData.size || ''
                    },
                    success: (res) => resolve(res),
                    fail: (err) => reject(err)
                });
            });
            
            // uni.uploadFile 返回的 data 是字符串
            let data = uploadRes.data;
            try {
                if (typeof data === 'string') data = JSON.parse(data);
            } catch(e) {}
            
            drawRes = {
                statusCode: uploadRes.statusCode,
                data: data
            };
            
        } else {
             drawRes = await uni.request({
                url: `${apiBaseUrl}/draw/zhipu/image`,
                method: 'POST',
                header: { 
                    'token': token,
                    'Content-Type': 'application/x-www-form-urlencoded' 
                },
                data: {
                    prompt: content,
                    size: transitData.size || ''
                }
            });
        }
        
        if (drawRes.statusCode === 200 && (drawRes.data.code === 200 || drawRes.data.data)) {
            const imgUrl = drawRes.data.data || drawRes.data;
            conversationList.value[aiMsgIndex].aiImage = imgUrl;
            conversationList.value[aiMsgIndex].content = ''; // 清除"正在绘制"
            
            // 回调获取总结 (Stream)
            // 如果 draw 时带了文件，callback 时是否需要带？通常 callback 只是对结果的总结。
            // 但如果 prompt 指令是 img2img，可能需要原图。
            // 假设 callback 只需要 imageUrl
            startStreamRequest(`${apiBaseUrl}/chat/draw/callback`, {
                imageUrl: imgUrl,
                message: content,
                chatId: cid,
                model: model.value
            }, aiMsgIndex);
            
        } else {
            conversationList.value[aiMsgIndex].content = '绘图失败: ' + (drawRes.data.msg || '未知错误');
            conversationList.value[aiMsgIndex].loading = false;
        }

    } else {
        // --- 文本对话流程 ---
        // DialogueView 使用 FormData，包含 isRag, isMcp 等
        // 我们尝试发送 JSON，看看是否兼容
        // 或者使用 application/x-www-form-urlencoded
        const payload = {
            message: content,
            chatId: cid,
            model: model.value,
            isRag: isRag.value,
            isMcp: isMcp.value,
            mcpList: JSON.stringify(selectedMcpIds.value),
            role: ''
        };
        
        // 启动流式请求
        // 注意：uni.request enableChunked 仅在部分平台支持
        // 如果是 H5，可以使用 fetch (但这里在 .vue 里写，uni 编译环境可能不同)
        // 这是一个兼容性实现
        // 启动流式请求
        startStreamRequest(`${apiBaseUrl}/chat/chat`, payload, aiMsgIndex, true, selectedFile.value?.path);
    }
    
  } catch (err) {
    console.error(err);
    conversationList.value[aiMsgIndex].content = '请求出错，请重试';
    conversationList.value[aiMsgIndex].loading = false;
    aiLoading.value = false;
  }
};

// 统一流式请求处理或上传处理
const startStreamRequest = (url, data, msgIndex, useFormData = false, filePath = null) => {
    const token = uni.getStorageSync('token');
    
    // 如果有文件，使用 uni.uploadFile (不支持流式，只能等待完整响应)
    // 或者是小程序环境，虽然 uni.request 支持 chunked，但 multipart 构造麻烦
    // 我们约定：有文件时使用 uni.uploadFile，无文件时使用 uni.request (支持流式)
    
    if (filePath) {
        const uploadTask = uni.uploadFile({
            url: url,
            filePath: filePath,
            name: 'file',
            header: {
                'token': token
            },
            formData: data,
            success: (uploadRes) => {
                if (uploadRes.statusCode === 200) {
                     // uploadFile 返回的 data 是 String，需要 parse
                     // 注意：后端对于 /chat/chat 返回的是流式文本还是JSON？
                     // 如果是 stream，uploadFile 可能拿到的是 buffer 转成的 string
                     // 或者是最终的完整 string
                     try {
                         // 尝试解析，如果后端返回的是JSON格式的错误信息
                         const jsonRes = JSON.parse(uploadRes.data);
                         if (jsonRes.code && jsonRes.code !== 200) {
                             conversationList.value[msgIndex].content = '请求失败: ' + (jsonRes.msg || '未知错误');
                             return;
                         }
                     } catch(e) {
                         // 解析失败说明可能是纯文本回复（流式结果的拼接）
                     }
                     
                     conversationList.value[msgIndex].content = uploadRes.data;
                } else {
                     conversationList.value[msgIndex].content = '上传失败';
                }
            },
            fail: (err) => {
                conversationList.value[msgIndex].content = '网络请求失败';
            },
            complete: () => {
                conversationList.value[msgIndex].loading = false;
                aiLoading.value = false;
                scrollToBottom();
            }
        });
        return;
    }

    // #ifdef H5
    // H5 环境下使用 fetch (原生支持流)
    fetch(url, {
        method: 'POST',
        headers: {
            'token': token,
            // 如果是 FormData 可以在这里处理，但这里为了简单传 JSON 或 form
            // 'Content-Type': 'application/json' 
        },
        body: useFormData ? objectToFormData(data) : JSON.stringify(data)
    }).then(response => {
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        
        const readStream = async () => {
            while (true) {
                const { done, value } = await reader.read();
                if (done) {
                    conversationList.value[msgIndex].loading = false;
                    aiLoading.value = false;
                    // 如果当前对话不在列表中（新对话），刷新列表
                    if (!historyList.value.find(c => c.conversationId == chatId.value)) {
                        fetchConversationList();
                    }
                    break;
                }
                const chunk = decoder.decode(value, { stream: true });
                conversationList.value[msgIndex].content += chunk;
                scrollToBottom();
            }
        };
        readStream();
    }).catch(err => {
        conversationList.value[msgIndex].content += '\n[网络错误]';
        conversationList.value[msgIndex].loading = false;
        aiLoading.value = false;
    });
    // #endif

    // #ifndef H5
    // 小程序/App 环境使用 uni.request + enableChunked
    // 注意：requestTask.onChunkReceived 需要 enableChunked: true
    const requestTask = uni.request({
        url: url,
        method: 'POST',
        header: {
            'token': token,
            'Content-Type': 'application/x-www-form-urlencoded' // 假设后端兼容 Form 表单
        },
        data: data,
        enableChunked: true, 
        success: (res) => {
            // 请求完成
             conversationList.value[msgIndex].loading = false;
             aiLoading.value = false;
             if (!historyList.value.find(c => c.conversationId == chatId.value)) {
                 fetchConversationList();
             }
        },
        fail: (err) => {
             conversationList.value[msgIndex].content += '\n[请求失败]';
             conversationList.value[msgIndex].loading = false;
             aiLoading.value = false;
        }
    });

    // 监听 Chunk
    requestTask.onChunkReceived((res) => {
        // arrayBuffer 转 string
        // 小程序环境 TextDecoder 可能不兼容，需使用兼容写法
        const chunk = arrayBufferToString(res.data);
        conversationList.value[msgIndex].content += chunk;
        scrollToBottom();
    });
    // #endif
};

// 辅助：Object 转 FormData (H5 only mostly)
const objectToFormData = (obj) => {
    const fd = new FormData();
    for (const key in obj) {
        fd.append(key, obj[key]);
    }
    return fd;
};

// 辅助：ArrayBuffer 转 String (简单实现，仅支持 UTF-8)
const arrayBufferToString = (buffer) => {
    // 微信小程序等环境支持
    if (typeof TextDecoder !== 'undefined') {
        return new TextDecoder("utf-8").decode(buffer);
    } else {
        // Fallback for environments without TextDecoder
        let str = "";
        const u8 = new Uint8Array(buffer);
        for (let i = 0; i < u8.length; i++) {
            str += String.fromCharCode(u8[i]);
        }
        // 对于中文可能乱码，需要更复杂的解码库，这里做简易处理
        try {
             return decodeURIComponent(escape(str)); 
        } catch (e) {
             return str;
        }
    }
};

onLoad((options) => {
    if (options && options.chatId) {
        chatId.value = options.chatId;
    } else {
        // 如果没有传入chatId，尝试获取或是生成新的
        // getOrCreateChatId(); // 不急着生成，等用户发送消息或选择历史记录
        // 如果有历史记录，默认可以不选，或者选最新的？
        // DialogueView逻辑：如果有历史，加载最新的；如果没有，新建。
        // 这里简化：先加载列表，如果列表不为空，自动选第一个（最新的）
    }
    fetchConversationList();
});

// --- 新增逻辑 ---

// 切换抽屉
const toggleDrawer = () => {
    showDrawer.value = !showDrawer.value;
};

// 格式化时间
const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hour = date.getHours().toString().padStart(2, '0');
    const minute = date.getMinutes().toString().padStart(2, '0');
    return `${month}-${day} ${hour}:${minute}`;
};

// 获取会话列表
const fetchConversationList = async () => {
    try {
        const res = await uni.request({
            url: `${apiBaseUrl}/chat/conversation/list`,
            method: 'GET',
            header: { 'token': uni.getStorageSync('token') }
        });
        if (res.statusCode === 200 && res.data.code === 200) {
            historyList.value = res.data.data || [];
            // 如果初始加载且没有chatId，自动选择第一个
            if (!chatId.value && historyList.value.length > 0) {
                 switchChat(historyList.value[0]);
            } else if (chatId.value) {
                 loadHistory();
            }
        }
    } catch (e) {
        console.error('获取会话列表失败', e);
    }
};

// 过滤后的会话列表
const filteredConversations = computed(() => {
    if (!searchText.value.trim()) return historyList.value;
    return historyList.value.filter(item => 
        (item.title && item.title.includes(searchText.value))
    );
});

// 切换会话
const switchChat = (item) => {
    if (chatId.value === item.conversationId) {
        showDrawer.value = false;
        return;
    }
    chatId.value = item.conversationId;
    showDrawer.value = false;
    // 重置状态并加载新历史
    conversationList.value = [];
    currentPage.value = 1;
    historyHasMore.value = true;
    loadHistory();
};

// 新建会话
const createNewChat = () => {
    chatId.value = generateChatId();
    conversationList.value = [];
    currentPage.value = 1;
    historyHasMore.value = true;
    showDrawer.value = false;
    // 此时不需要重新获取列表，因为新会话还没保存到后端（只有发消息后才保存？或者调用add接口？）
    // DialogueView逻辑是发消息时自动保存或调用addConversation
    // 这里我们先只是清空界面，等用户发消息时，handleSend会使用新的chatId
};

// 删除会话
const deleteChat = async (id) => {
    uni.showModal({
        title: '确认删除',
        content: '确定要删除该对话吗？',
        success: async (res) => {
            if (res.confirm) {
                try {
                    // 调用删除接口 (根据BSideApi: /chat/delete POST data=id)
                    // 注意：API定义是 deleteConversation(data)，data可能是id字符串或对象，需确认后端。
                    // 根据通常习惯，可能是 JSON body: { conversationId: id } 或者直接传 ID
                    // 假设是直接传 ID (String) 或 { conversationId: ... }
                    // BSideApi: POST /chat/delete, data
                    
                    const res = await uni.request({
                        url: `${apiBaseUrl}/chat/delete`,
                        method: 'POST',
                        header: { 
                            'token': uni.getStorageSync('token'),
                            'Content-Type': 'application/json' // 这里可能需要 application/json
                        },
                        data: id // 尝试直接传ID，或者 { conversationId: id }
                    });
                    
                    if (res.statusCode === 200 && res.data.code === 200) {
                        uni.showToast({ title: '删除成功', icon: 'none' });
                        // 更新列表
                        historyList.value = historyList.value.filter(item => item.conversationId !== id);
                        
                        // 如果删除的是当前会话，重置
                        if (chatId.value === id) {
                            if (historyList.value.length > 0) {
                                switchChat(historyList.value[0]);
                            } else {
                                createNewChat();
                            }
                        }
                    } else {
                        // 尝试 { conversationId: id }
                         const res2 = await uni.request({
                            url: `${apiBaseUrl}/chat/delete`,
                            method: 'POST',
                            header: { 
                                'token': uni.getStorageSync('token'),
                            },
                            data: { conversationId: id }
                        });
                        if (res2.statusCode === 200 && res2.data.code === 200) {
                             uni.showToast({ title: '删除成功', icon: 'none' });
                             historyList.value = historyList.value.filter(item => item.conversationId !== id);
                             if (chatId.value === id) createNewChat();
                        } else {
                             uni.showToast({ title: '删除失败', icon: 'none' });
                        }
                    }
                } catch (e) {
                    console.error('删除会话失败', e);
                }
            }
        }
    });
};

</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--bgColor1);
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--bgColor1);
  position: relative; // 为drawer定位
}

/* Custom Nav Bar */
.custom-nav-bar {
  height: 44px;
  display: flex;
  align-items: center;
  padding: 0 15px;
  background-color: var(--bgColor1);
  border-bottom: 1px solid var(--borderColor);
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  font-size: 16px;
  color: var(--textColor1);
}

.nav-icon {
  font-size: 20px;
  margin-right: 8px;
}

.nav-title {
  font-weight: bold;
}

/* Drawer Styles */
.drawer-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  z-index: 900;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
  
  &.show {
    opacity: 1;
    pointer-events: auto;
  }
}

.drawer-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 75%; // 抽屉宽度
  height: 100%;
  background-color: var(--bgColor2);
  z-index: 999;
  transform: translateX(-100%);
  transition: transform 0.3s;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0,0,0,0.1);
  
  &.show {
    transform: translateX(0);
  }
}

.drawer-header {
  padding: 20px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--borderColor);
}

.drawer-title {
  font-size: 18px;
  font-weight: bold;
  color: var(--textColor1);
}

.new-chat-btn {
  font-size: 14px;
  color: var(--themeColor1);
  background-color: rgba(129, 102, 231, 0.1);
  padding: 6px 12px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  
  .icon {
    font-weight: bold;
    margin-right: 2px;
  }
}

.search-box {
  padding: 10px 15px;
  border-bottom: 1px solid var(--borderColor);
}

.search-input {
  background-color: var(--bgColor1);
  height: 36px;
  border-radius: 18px;
  padding: 0 15px;
  font-size: 14px;
  color: var(--textColor1);
}

.drawer-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.drawer-item {
  padding: 12px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--borderColor);
  transition: background-color 0.2s;
  
  &:active {
    background-color: var(--bgColor1);
  }
  
  &.active {
    background-color: rgba(129, 102, 231, 0.1);
    border-left: 3px solid var(--themeColor1);
  }
}

.item-main {
  flex: 1;
  overflow: hidden;
  margin-right: 10px;
}

.item-title {
  display: block;
  font-size: 14px;
  color: var(--textColor1);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-time {
  font-size: 11px;
  color: var(--textColor3);
}

.item-actions {
  
  .delete-btn {
    font-size: 16px;
    padding: 5px;
    color: #ff4d4f;
  }
}

.empty-tip {
  text-align: center;
  padding: 30px 0;
  color: var(--textColor3);
  font-size: 13px;
}

.chat-history {
  flex: 1;
  box-sizing: border-box;
  overflow: hidden; // 防止双滚动条
}

.padding-space {
  height: 20px;
}
.padding-space-bottom {
  height: 20px;
}

.loading-more {
  text-align: center;
  padding: 10px;
  .loading-text {
    font-size: 12px;
    color: var(--textColor3);
  }
}

.message-item {
  padding: 10px 15px;
  margin-bottom: 10px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  
  &.user-row {
    flex-direction: row;
    justify-content: flex-end;
    .message-bubble {
        background-color: var(--themeColor1);
        color: var(--themeTextColor);
        border-radius: 12px 12px 4px 12px;
    }
  }
  
  &.ai-row {
    flex-direction: row;
    .message-bubble {
        background-color: var(--dColor1);
        color: var(--textColor1);
        border-radius: 12px 12px 12px 4px;
    }
  }
}

.content-wrapper {
    max-width: 92%;
    display: flex;
    flex-direction: column;
}

.message-bubble {
  padding: 12px;
  font-size: 15px;
  line-height: 1.5;
  box-shadow: 0 1px 2px var(--bgboxShadowColor1);
  word-break: break-all;
}

.message-bubble {
  padding: 12px;
  font-size: 15px;
  line-height: 1.5;
  box-shadow: 0 1px 2px var(--bgboxShadowColor1);
  word-break: break-all;
  overflow-x: auto; // 为代码块滚动
}

.message-text {
    white-space: pre-wrap;
}

.markdown-body {
  font-size: 15px;
  line-height: 1.6;
  color: var(--textColor1);
}

.ai-image, .message-image {
    width: 100%;
    border-radius: 4px;
    margin-bottom: 8px;
    display: block; // Fix layout
}

.typing-indicator {
    display: flex;
    padding-top: 5px;
    .dot {
        width: 6px;
        height: 6px;
        background-color: var(--textColor3);
        border-radius: 50%;
        margin-right: 4px;
        animation: typing 1.4s infinite ease-in-out both;
        
        &:nth-child(1) { animation-delay: -0.32s; }
        &:nth-child(2) { animation-delay: -0.16s; }
    }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}


.message-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 5px;
    gap: 10px;
    
    .action-btn {
        font-size: 11px;
        color: var(--textColor3);
        display: flex;
        align-items: center;
        
        .action-icon-img {
            width: 14px;
            height: 14px;
            margin-right: 2px;
        }
        
        &.collected {
            color: #f7b500;
        }
    }
    
    .model-tag {
        font-size: 10px;
        background-color: var(--bgColor2);
        padding: 2px 6px;
        border-radius: 10px;
        color: var(--textColor3);
    }
}


.input-area {
  background-color: var(--dColor1);
  padding: 10px 15px;
  padding-bottom: calc(10px + constant(safe-area-inset-bottom));
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  border-top: 1px solid var(--borderColor);
}

.file-preview {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: var(--bgColor2);
    padding: 8px 12px;
    border-radius: 10px;
    margin-bottom: 8px;
    font-size: 12px;
    color: var(--textColor2);
    
    .preview-content {
        display: flex;
        align-items: center;
        flex: 1;
        overflow: hidden;
    }
    
    .preview-thumb {
        width: 40px;
        height: 40px;
        border-radius: 6px;
        margin-right: 10px;
        flex-shrink: 0;
    }
    
    .file-icon-preview {
        font-size: 24px;
        margin-right: 10px;
        flex-shrink: 0;
    }
    
    .file-name {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    
    .close-btn {
        color: var(--textColor3);
        padding: 0 5px;
        font-size: 18px;
        flex-shrink: 0;
        margin-left: 8px;
    }
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 5px;
}

.chat-input {
  flex: 1;
  min-height: 36px;
  max-height: 72px;
  background-color: var(--bgColor1);
  border-radius: 18px;
  padding: 8px 15px;
  font-size: 14px;
  color: var(--textColor1);
  line-height: 20px;
  box-sizing: border-box;
  width: 0;
}

.model-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 4px 0 16px;
    border-bottom: 1px solid rgba(255,255,255,0.05);
}

.model-chip {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 14px;
    border-radius: 20px;
    background: var(--bgColor2);
    border: 1.5px solid transparent;
    transition: all 0.2s;
    
    .chip-label {
        font-size: 12px;
        color: var(--textColor2);
        white-space: nowrap;
    }
    
    .chip-check {
        font-size: 12px;
        color: #7b61ff;
        font-weight: bold;
    }
    
    &.active {
        border-color: #7b61ff;
        background: rgba(123, 97, 255, 0.12);
        
        .chip-label {
            color: #7b61ff;
            font-weight: 500;
        }
    }
}

.model-setting-item {
    border-bottom: none !important;
    padding-bottom: 4px !important;
}

.icon-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background-color: var(--bgColor2);
    margin-right: 0px;
    color: var(--textColor2);
    
    .icon-img {
        width: 20px;
        height: 20px;
    }
}

.send-btn {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #7b61ff, #a78bfa);
    flex-shrink: 0;
    
    .send-icon {
        width: 18px;
        height: 18px;
    }
    
    .loading-dot {
        color: #fff;
        font-size: 16px;
        letter-spacing: 1px;
    }
    
    &.disabled {
        opacity: 0.4;
    }
}

.welcome-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  height: 60%; 
}

.welcome-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

.welcome-title {
  font-size: 20px;
  font-weight: bold;
  color: var(--textColor1);
  margin-bottom: 30px;
}

.consume-info {
  display: flex;
  align-items: center;
  background-color: var(--bgColor2);
  padding: 8px 16px;
  border-radius: 20px;
  margin-bottom: 20px;
}

.consume-icon {
  margin-right: 8px;
  font-size: 16px;
}

.consume-text {
  font-size: 14px;
  color: var(--textColor2);
}

.warning-text {
  font-size: 12px;
  color: var(--textColor3);
  margin-top: 10px;
}

/* Settings Modal */
.settings-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 9999;
}

.settings-content {
  background-color: var(--bgColor1);
  width: 100%;
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  padding: 20px;
  max-height: 70vh;
  min-height: 40vh;
  box-shadow: 0 -4px 16px rgba(0,0,0,0.2);
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--borderColor);
}

.settings-title {
  font-size: 18px;
  font-weight: bold;
  color: var(--textColor1);
}

.settings-content .close-btn {
  font-size: 24px;
  color: var(--textColor2);
  padding: 0 10px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.label-box {
  display: flex;
  flex-direction: column;
}

.item-label {
  font-size: 16px;
  color: var(--textColor1);
}

.item-desc {
  font-size: 12px;
  color: var(--textColor3);
  margin-top: 4px;
}

.mcp-list-scroll {
  max-height: 300px;
  margin-top: 10px;
}

.mcp-list-title {
  font-size: 14px;
  color: var(--textColor2);
  margin: 10px 0;
}

.mcp-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: var(--bgColor2);
  border-radius: 8px;
  margin-bottom: 8px;
  border: 1px solid transparent;
}

.mcp-item.active {
  border-color: #7b61ff;
  background-color: rgba(123, 97, 255, 0.1);
}

.checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid #7b61ff;
  border-radius: 4px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-mark {
  color: #7b61ff;
  font-size: 14px;
  font-weight: bold;
}

.mcp-icon {
  width: 32px;
  height: 32px;
  margin-right: 12px;
  border-radius: 6px;
}

.mcp-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.mcp-name {
  font-size: 14px;
  color: var(--textColor1);
  font-weight: 500;
}

.mcp-desc {
  font-size: 12px;
  color: var(--textColor3);
  margin-top: 2px;
}

.mcp-list .loading-text,
.mcp-list .empty-text {
  text-align: center;
  color: var(--textColor2);
  padding: 20px;
  font-size: 14px;
}

/* RAG Management Modal */
.rag-content {
  max-height: 85vh;
  min-height: 50vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.setting-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rag-manage-btn {
  background: linear-gradient(135deg, #7b61ff, #a78bfa);
  color: #fff;
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 12px;
  white-space: nowrap;
}

.rag-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed rgba(123, 97, 255, 0.4);
  border-radius: 12px;
  padding: 30px 20px;
  margin-bottom: 16px;
  background: rgba(123, 97, 255, 0.05);
}

.upload-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 15px;
  color: #7b61ff;
  margin-bottom: 6px;
}

.upload-tip {
  font-size: 12px;
  color: var(--textColor3);
  text-align: center;
}

.rag-uploading {
  text-align: center;
  padding: 12px;
  color: #7b61ff;
  font-size: 14px;
}

.rag-file-list {
  margin-top: 10px;
}

.rag-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rag-list-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--textColor1);
}

.refresh-btn {
  padding: 4px 12px;
  border-radius: 14px;
  background: var(--bgColor2);
  font-size: 12px;
  color: var(--textColor2);
}

.rag-files-scroll {
  max-height: 300px;
}

.rag-file-item {
  background: var(--bgColor2);
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 10px;
  overflow: hidden;
  box-sizing: border-box;
  width: 100%;
}

.file-info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
  overflow: hidden;
  min-width: 0;
}

.file-icon-tag {
  font-size: 22px;
  margin-right: 10px;
  flex-shrink: 0;
}

.file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.file-name-text {
  font-size: 14px;
  color: var(--textColor1);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  font-size: 11px;
  color: var(--textColor3);
  margin-top: 3px;
}

.file-actions-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-tag {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 11px;
}

.status-tag.enabled {
  background: rgba(52, 211, 153, 0.15);
  color: #34d399;
}

.status-tag.disabled {
  background: rgba(148, 163, 184, 0.15);
  color: #94a3b8;
}

.file-action-btn {
  padding: 4px 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.file-action-btn.toggle-btn.success {
  background: rgba(52, 211, 153, 0.2);
  color: #34d399;
}

.file-action-btn.toggle-btn.warn {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
}

.file-action-btn.delete-btn {
  background: rgba(248, 113, 113, 0.2);
  color: #f87171;
}
</style>
