<template>
  <div class="dialogue-container">
    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && !sidebarCollapsed" 
      class="mobile-overlay"
      @click="sidebarCollapsed = true"
    ></div>
    
    <!-- 左侧边栏 -->
    <div class="sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }" style="display: flex !important; visibility: visible !important; opacity: 1 !important;">
      <div class="sidebar-header">
        <!-- Logo和介绍区域 -->
        <div class="brand-section">
          <div class="logo-container">
            <img class="sidebar-logo" alt="Pass Assistant Logo" src="../assets/logo.png"/>
            <div class="brand-info">
              <h2 class="brand-title">Pass Assistant</h2>
              <p class="brand-subtitle">智能对话助手</p>
            </div>
          </div>
          <p class="brand-description">真正的高度自定义多模态多模型+System角色+Rag知识库+MCP的AI智能对话</p>
        </div>

        <!-- 功能按钮区域 -->
        <div class="action-buttons">
          <!-- 说明图标 -->
          <div class="help-icon-container">
            <el-icon 
              class="help-icon" 
              @mouseenter="showHelpTooltip = true"
              @mouseleave="showHelpTooltip = false"
            >
              <QuestionFilled />
            </el-icon>
            <!-- 气泡提示 -->
            <div 
              v-show="showHelpTooltip" 
              class="help-tooltip"
              @mouseenter="showHelpTooltip = true"
              @mouseleave="showHelpTooltip = false"
            >
              <div class="tooltip-content">
                <div class="tooltip-title">💡 使用提示</div>
                <div class="tooltip-text">
                  • GPT,GLM,豆包支持多模态输入<br>
                  • 可自由开关RAG知识库MCP协议检索<br/>
                  • 图文对话暂不支持RAG和MCP
                </div>
              </div>
              <div class="tooltip-arrow"></div>
            </div>
          </div>
          
        <el-button 
          type="primary" 
              size="default"
          @click="createNewConversation"
              class="new-conversation-btn"
        >
          <el-icon><Plus /></el-icon>
            <span class="btn-text">新建对话</span>
        </el-button>

        <!-- 数据统计和收起按钮一行布局 -->
        <div class="button-row">
          <el-button 
            @click="navigateToDataView"
            size="default"
            class="data-stats-btn"
          >
            <el-icon><TrendCharts /></el-icon>
            <span class="btn-text">数据统计</span>
          </el-button>

          <el-button 
            @click="toggleSidebar"
            size="default"
            class="toggle-sidebar-btn"
            :class="{ 'collapsed': sidebarCollapsed }"
          >
            <el-icon>
              <el-icon v-if="!sidebarCollapsed"><ArrowLeft /></el-icon>
              <el-icon v-else><ArrowRight /></el-icon>
            </el-icon>
            <span class="btn-text">{{ sidebarCollapsed ? '展开' : '收起' }}</span>
          </el-button>
        </div>
        </div>

        <!-- 分割线 -->
        <div class="divider"></div>

        <!-- 对话列表标题 -->
        <h3 class="conversation-title">对话列表</h3>
      </div>
      
      <div class="conversation-list" v-if="!sidebarCollapsed" style="display: block !important; visibility: visible !important;">

        <el-scrollbar style="height: 100%">
          <div 
            v-for="conversation in conversations" 
            :key="conversation.conversationId"
            class="conversation-item"
            :class="{ 'active': currentConversationId === conversation.conversationId }"
            @click="selectConversation(conversation)"
            style="display: flex !important; visibility: visible !important;"
          >
            <div class="conversation-content">
              <div class="conversation-title" 
                   @dblclick.stop="editConversationTitle(conversation)"
                   :title="conversation.title || '新对话'"
                   style="cursor: pointer;"
              >
                {{ conversation.title || '新对话' }}
              </div>
              
              <div class="conversation-footer">
                <div class="conversation-time">{{ formatTime(conversation.createdTime) }}</div>
                <span v-if="conversation.role" class="role-tag">
                  <el-icon><UserFilled /></el-icon>
                  <span class="role-text">{{ conversation.role }}</span>
                </span>
              </div>
            </div>
            
            <div class="conversation-actions">
              <el-tooltip content="编辑标题" placement="top" :show-after="800">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click.stop="editConversationTitle(conversation)"
                  class="action-btn edit-btn"
                  plain
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除对话" placement="top" :show-after="800">
                <el-button 
                  type="danger" 
                  size="small" 
                  @click.stop="deleteConversation(conversation.conversationId)"
                  class="action-btn delete-btn"
                  plain
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>
          
          <!-- 如果没有对话，显示提示信息 -->
          <div v-if="conversations.length === 0" style="text-align: center; padding: 20px; color: var(--textColor3); background-color: var(--bgColor1); margin: 10px; border-radius: 8px; border: 1px solid var(--borderColor);">
            暂无对话记录
          </div>
        </el-scrollbar>
      </div>
    </div>

    <!-- 主对话区域 -->
    <div class="main-content" :class="{ 'main-expanded': sidebarCollapsed }">
      <!-- 移动端菜单按钮 -->
      <div v-if="isMobile" class="mobile-menu-btn" @click="toggleSidebar">
        <el-icon :size="20">
          <component :is="sidebarCollapsed ? 'Menu' : 'Close'" />
        </el-icon>
      </div>
      
      <div class="body" ref="scrollRef">
        <!-- 欢迎页面 -->
        <div v-if="!conversationList.length" class="explain">

          
          <img class="logo" alt="Vue logo" src="../assets/logo.png"/>
          <div class="expositoryCase">欢迎使用-Pass Assistant</div>
          <div class="consume">
            <el-icon>
              <Goods/>
            </el-icon>
            <div class="consumeText">每次提问消耗1个IT币</div>
          </div>
          <div class="beCareful">请注意不支持违法、违规等不当信息内容</div>
        </div>

        <!-- 对话列表 -->
    <div v-else class="questions" style="margin: 20px 0">
      <div
          v-for="(item, index) in conversationList"
          :key="index"
          class="item slide-animation"
      >
            <!-- 用户问题 -->
        <div class="question">
          <div>
            <div class="text">
              {{ item.user }}
              
              <!-- 显示媒体内容（从media字段） -->
              <div v-if="item.media" class="question-media">
                <div class="media-container">
                  <!-- 根据文件扩展名判断媒体类型 -->
                  <div v-if="isImageFile(item.media)" class="question-image-container">
                    <el-image
                      :src="item.media"
                      :alt="getFileName(item.media)"
                      class="question-image"
                      fit="contain"
                      :preview-src-list="[item.media]"
                      :initial-index="0"
                      preview-teleported
                      :z-index="9999"
                    >
                      <template #error>
                        <div class="image-error">
                          <el-icon><Picture /></el-icon>
                          <span>加载失败</span>
                        </div>
                      </template>
                    </el-image>
                    <div class="image-overlay">
                      <el-icon class="zoom-icon"><ZoomIn /></el-icon>
                    </div>
                  </div>
                  <div v-else class="question-file">
                    <div class="file-info-display">
                      <el-icon :size="16" class="file-icon"><Document /></el-icon>
                      <span class="file-name">{{ getFileName(item.media) }}</span>
                      <a :href="item.media" target="_blank" class="file-link">查看文件</a>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 显示上传的文件（兼容旧数据） -->
              <div v-if="item.hasFile && item.files" class="question-file">
                <div 
                  v-for="(file, fileIndex) in item.files" 
                  :key="'uploaded-' + fileIndex"
                  class="file-info-display"
                >
                  <el-icon :size="16" class="file-icon"><Document /></el-icon>
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">({{ formatFileSize(file.size) }})</span>
                </div>
              </div>
            </div>
            <div class="operation--model_user">
              <div class="op-btn" @click="copyAnswer(item.user)">
                <el-icon>
                  <CopyDocument/>
                </el-icon>
                <text class="op-font">复制</text>
              </div>
            </div>
          </div>
          <el-avatar
              class="flexShrink"
              :size="35"
              :icon="UserFilled"
              :src="
              store.getters.userinfo.avatar
                ? imageUrl + store.getters.userinfo.avatar
                : require('../assets/my.png')
            "
          />
        </div>

            <!-- AI回答 -->
        <div class="answer">
          <el-avatar
              class="flexShrink"
              :size="35"
              :icon="UserFilled"
              :src="require('../assets/logo.png')"
          />
          <template v-if="item.assistant">
            <div style="width: 100%">
              <div
                  class="answer-data"
                  :style="{ maxWidth: calculateWidth(item.assistant.length) }"
              >
                <v-md-editor
                    :model-value="item.assistant"
                    mode="preview"
                    @copy-code-success="handleCopyCodeSuccess"
                />
              </div>

              <div class="operation--model" v-if="!item.isError">
                <div class="op-btn" @click="copyAnswer(item.assistant)">
                  <el-icon>
                    <CopyDocument/>
                  </el-icon>
                  <text class="op-font">复制</text>
                </div>
                <!-- 显示模型名称 -->
                <div class="model-badge" v-if="item.model">
                  <el-icon>
                    <Cpu />
                  </el-icon>
                  <text class="model-text">{{ item.model }}</text>
                </div>
                <div
                    class="op-btn"
                    @click="onCollection(item, index)"
                    v-if="!item.isCollection"
                >
                  <el-icon color="rgb(255,236,160)">
                    <StarFilled/>
                  </el-icon>
                  <text class="op-font">收藏</text>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="answer-data" style="width: 100px">
              <div style="display: flex; padding: 5px 9px">
                <div class="dot_0"></div>
                <div class="dot_1"></div>
                <div class="dot_2"></div>
                <div class="dot_3"></div>
                <div class="dot_4"></div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

        <!-- 暂停按钮 -->
    <div class="suspend" v-show="aiLoading" @click="closeSocket">
      <el-icon :size="16">
        <VideoPause/>
      </el-icon>
      <div>暂停输出</div>
    </div>

        <!-- 底部输入区域 -->
    <div class="footer">
      <!-- 文件预览区域（显示在输入框上方） -->
      <div class="top-file-preview" v-if="currentFiles.length > 0">
        <div 
          v-for="(file, index) in currentFiles" 
          :key="index"
          class="file-preview-item-top"
        >
          <el-icon :size="16" class="file-icon"><Document /></el-icon>
          <div class="file-info">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-size">{{ formatFileSize(file.size) }}</div>
          </div>
          <div class="file-preview-close" @click="removeFileFromTop(index)">
            <el-icon :size="12"><Close /></el-icon>
          </div>
        </div>
      </div>
      
      <div class="footer-bar">
        <div class="left-controls" v-show="store.getters.userinfo && !aiLoading">
          
          
          <!-- RAG和MCP开关按钮 -->
          <div class="feature-toggles">
            <el-tooltip 
              content="开启RAG知识库检索"
              placement="top"
            >
              <div class="toggle-button" :class="{ active: isRag }" @click="isRag = !isRag">
                <el-icon :size="14">
                  <DataBoard />
                </el-icon>
                <span class="toggle-text">RAG</span>
              </div>
            </el-tooltip>
            
            <el-tooltip 
              content="开启MCP服务"
              placement="top"
            >
              <div class="toggle-button" :class="{ active: isMcp }" @click="isMcp = !isMcp">
                <el-icon :size="14">
                  <Connection />
                </el-icon>
                <span class="toggle-text">MCP</span>
              </div>
            </el-tooltip>
            
            <!-- RAG管理按钮 -->
            <el-tooltip 
              content="管理RAG知识库文件"
              placement="top"
            >
              <div class="management-button" @click="openRagManagement">
                <el-icon :size="14">
                  <Setting />
                </el-icon>
                <span class="btn-text">RAG管理</span>
              </div>
            </el-tooltip>
            
            <!-- MCP管理按钮 -->
            <el-tooltip 
              content="管理MCP服务列表"
              placement="top"
            >
              <div class="management-button" @click="openMcpManagement">
                <el-icon :size="14">
                  <Tools />
                </el-icon>
                <span class="btn-text">MCP管理</span>
              </div>
            </el-tooltip>
          </div>
        </div>


        <InputFormField
            ref="inputRef"
            :aiLoading="aiLoading"
            :inputText="input"
            @update:inputText="input = $event"
            @update:model="model = $event"
            @onSubmit="onSubmit"
            @file-uploaded="handleFileUploaded"
            @file-removed="handleFileRemoved"
        />
      </div>
    </div>
  </div>
    </div>

    <!-- 对话历史弹窗 -->
  <el-dialog
      v-model="dialogueDisplay"
      title=""
      width="430px"
      center
      style="background-color: var(--bgColor1)"
  >
    <div>
      <div class="cache-flex-center">
        <img alt="Vue logo" src="../assets/logo02.svg" class="cache-img"/>
      </div>
      <div class="cache-text">Pass Assistant</div>
      <div class="cache-flex-center cache-padding-top">
        <div class="cache-btn" @click="createdNewChat">
          <el-icon size="16px">
            <ChatLineSquare/>
          </el-icon>
          <div class="cache-btn-text">创建新的聊天</div>
        </div>
      </div>
      <div class="cache-content">
        <div class="cache-scrollbar">
          <el-scrollbar height="250px">
            <div
                class="cache-padding"
                v-for="(item, index) in dialogueCache.array"
                :key="index"
            >
              <div class="cache-flex-space-between">
                <div class="cache-message" @click="switchChat(index)">
                  <div class="cache-message-text">
                    {{ item.title }}
                  </div>
                  <div class="cache-message-time">
                    {{ conversionTime(item.time) }}
                  </div>
                </div>
                <div class="cache-selected">
                  <img
                      :src="
                      dialogueCache.index === index
                        ? require('../assets/selected.svg')
                        : require('../assets/close.svg')
                    "
                      class="cache-selected-img"
                      @click="clearDialogue(index)"
                  />
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 编辑对话设置弹窗 -->
  <el-dialog
      v-model="editTitleDialogVisible"
      title="编辑对话设置"
      width="500px"
      center
      :close-on-click-modal="false"
      :close-on-press-escape="false"
        style="background-color: var(--bgColor1);color: var(--textColor1);border-color: var(--borderColor);"
      @close="handleEditDialogClose"
  >
    <div style="padding: 20px;">
        <el-form :model="editTitleForm" label-width="100px">
        <el-form-item label="对话名称" required>
          <el-input 
            v-model="editTitleForm.title" 
            placeholder="请输入对话名称"
            maxlength="50"
            show-word-limit
            clearable
            ref="editTitleInput"
          />
        </el-form-item>
        
        <el-form-item label="系统角色">
          <el-input 
            v-model="editTitleForm.role" 
            type="textarea"
            placeholder="请输入系统角色提示词，用于定义AI的行为和回答风格"
            :rows="4"
            maxlength="200"
            show-word-limit
            clearable
          />
        </el-form-item>
        
        <div style="font-size: 12px; color: var(--textColor3); margin-top: 8px;">
          <div>• 对话名称：用于标识这个对话</div>
          <div>• 系统角色：定义AI在此对话中的行为方式和回答风格</div>
          <div>• 提示：双击对话标题也可以快速编辑</div>
        </div>
      </el-form>
    </div>
    <template #footer>
      <div style="text-align: center;">
        <el-button @click="editTitleDialogVisible = false" :disabled="editTitleLoading">
          取消
        </el-button>
        <el-button type="primary" @click="saveConversationTitle" :loading="editTitleLoading">
          {{ editTitleLoading ? '保存中...' : '保存' }}
        </el-button>
      </div>
    </template>
  </el-dialog>

  <LoginDialog :show="loginVisible" @close="loginVisible = false"/>

  <!-- RAG管理弹窗 -->
  <el-dialog
    v-model="ragManagementVisible"
    title="RAG知识库管理"
    width="800px"
    center
    :close-on-click-modal="true"
    class="rag-management-dialog"
  >
    <div style="padding: 20px;">
      <!-- 上传区域 -->
      <div class="upload-section">
        <el-upload
          ref="ragUploadRef"
          class="knowledge-upload"
          drag
          :action="uploadUrl"
          :headers="uploadHeaders"
          :before-upload="beforeRagUpload"
          :on-success="handleRagUploadSuccess"
          :on-error="handleRagUploadError"
          :show-file-list="false"
          accept=".txt,.md,.pdf"
          name="file"
          multiple
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 .txt, .md, .pdf 格式文件，单个文件不超过5MB(越大您的对话速度越慢且容易失败，推荐1MB以下)
            </div>
          </template>
        </el-upload>
      </div>

      <!-- 知识库文件列表 -->
      <div class="knowledge-list">
        <div class="list-header">
          <h3>已添加的知识库文件 ({{ knowledgeFiles.length }})</h3>
          <el-button 
            type="primary" 
            size="small" 
            @click="refreshKnowledgeList"
            :loading="knowledgeListLoading"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        
        <el-table 
          :data="knowledgeFiles" 
          style="width: 100%"
          v-loading="knowledgeListLoading"
          empty-text="暂无知识库文件"
        >
          <el-table-column prop="name" label="文件名" min-width="200">
            <template #default="scope">
              <div class="file-name-cell">
                <el-icon class="file-icon"><Document /></el-icon>
                <span class="file-name">{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="size" label="文件大小" width="120">
            <template #default="scope">
              {{ formatFileSize(scope.row.size) }}
            </template>
          </el-table-column>
          <el-table-column prop="uploadTime" label="上传时间" width="160">
            <template #default="scope">
              {{ formatTime(scope.row.uploadTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag 
                :type="scope.row.isEnable ? 'success' : 'info'"
                size="small"
              >
                {{ scope.row.isEnable ? '已启用' : '已禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="scope">
              <el-button 
                :type="scope.row.isEnable ? 'warning' : 'success'" 
                size="small" 
                @click="toggleKnowledgeFileStatus(scope.row)"
                :loading="scope.row.toggling"
                style="margin-right: 8px;"
              >
                {{ scope.row.isEnable ? '禁用' : '启用' }}
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteKnowledgeFile(scope.row)"
                :loading="scope.row.deleting"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-dialog>

  <!-- MCP管理弹窗 -->
  <el-dialog
    v-model="mcpManagementVisible"
    title="MCP服务管理"
    width="800px"
    center
    :close-on-click-modal="true"
    class="mcp-management-dialog"
  >
    <div class="mcp-management-container">
      <!-- 可选服务区域 -->
      <div class="available-services-section">
        <div class="section-header">
          <h3>MCP服务列表</h3>
          <span class="section-subtitle">点击服务即可添加到您的列表中</span>
        </div>
        
        <div class="service-grid">
          <div 
            v-for="service in mcpServices" 
            :key="service.mcpsId"
            class="service-card"
            :class="{ 'selected': isServiceSelected(service) }"
            @click="toggleMcpServiceSelection(service)"
          >
            <div class="service-icon-wrapper">
              <img 
                :src="service.icon" 
                alt="服务图标" 
                class="service-main-icon"
                @error="handleIconError"
              />
            </div>
            <div class="service-info">
              <h4 class="service-title">{{ service.mcpName }}</h4>
              <el-tooltip 
                :content="service.introduce" 
                placement="top" 
                :show-after="500"
                :hide-after="100"
                effect="light"
                popper-class="service-description-tooltip"
              >
                <p class="service-description">{{ service.introduce }}</p>
              </el-tooltip>
              <div class="service-meta">
                <el-tag size="small" type="info" class="service-tag">{{ service.methodName }}</el-tag>
                <span class="created-time">{{ formatTime(service.createdTime) }}</span>
              </div>
            </div>
            <div class="service-action">
              <el-button 
                v-if="!isServiceSelected(service)"
                type="primary" 
                size="small"
                circle
              >
                <el-icon><Plus /></el-icon>
              </el-button>
              <el-tag v-else type="success" size="small">已选择</el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 已选择的服务列表 -->
      <div class="selected-services-section">
        <div class="section-header">
          <h3>已选择的MCP服务 ({{ selectedMcpIds.length }})</h3>
          <p>当选择的MCP越多，回答速度越慢，推荐最多3个</p>
          <el-button 
            color="var(--themeColor1)"
            type="primary" 
            size="small" 
            @click="refreshMcpServices"
            :loading="mcpListLoading"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        
        <div class="selected-services-list" v-loading="mcpListLoading">
          <div 
            v-for="service in getSelectedMcpServices()" 
            :key="service.mcpsId"
            class="selected-service-item"
          >
            <div class="service-icon-wrapper small">
              <img 
                :src="service.icon" 
                alt="服务图标" 
                class="service-main-icon small"
                @error="handleIconError"
              />
            </div>
            <div class="service-details">
              <div class="service-name">{{ service.mcpName }}</div>
              <el-tooltip 
                :content="service.introduce" 
                placement="top" 
                :show-after="500"
                :hide-after="100"
                effect="light"
                popper-class="service-description-tooltip"
              >
                <div class="service-description-small">{{ service.introduce }}</div>
              </el-tooltip>
              <div class="service-meta">
                <el-tag type="success" size="small">已选择</el-tag>
                <span class="created-time">{{ formatTime(service.createdTime) }}</span>
              </div>
            </div>
            <div class="service-actions">
              <el-button 
                type="danger" 
                size="small" 
                @click="toggleMcpServiceSelection(service)"
                plain
              >
                <el-icon><Close /></el-icon>
                取消选择
              </el-button>
            </div>
          </div>
          
          <div v-if="selectedMcpIds.length === 0" class="empty-state">
            <el-icon :size="48" class="empty-icon"><Tools /></el-icon>
            <p class="empty-text">暂无选择的MCP服务</p>
            <p class="empty-hint">请从上方选择需要的服务</p>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElNotification, ElMessageBox } from 'element-plus';
import {
  ChatLineSquare,
  CopyDocument,
  Goods,
  StarFilled,
  UserFilled,
  VideoPause,
  Plus,
  Delete,
  Edit,
  ArrowLeft,
  ArrowRight,
  Document,
  Close,
  Menu,
  QuestionFilled,
  DataBoard,
  Connection,
  Cpu,
  TrendCharts,
  ZoomIn,
  Picture,
  Setting,
  Tools,
  UploadFilled,
  Refresh,
  Location,
  Sunny,
  ChatDotRound,
  Calendar,
  Operation,
} from '@element-plus/icons-vue';
import { 
  FavoritesAdd, 
  GetUserInfo, 
  getChatHistory, 
  getConversationList, 
  updateConversation, 
  deleteConversation as deleteConversationAPI,
  // RAG相关API
  getRagKnowledgeList,
  uploadRagKnowledge,
  deleteRagKnowledge,
  toggleRagKnowledgeStatus,
  // MCP相关API
  getMcpServiceList
} from '../../api/BSideApi';
import LoginDialog from '@/components/LoginDialog.vue';
import InputFormField from '@/components/InputFormField.vue';
import store from '@/store';
import { conversionTime } from '../utils/date';

// 响应式数据
const scrollRef = ref(null);
const inputRef = ref(null);
const input = ref('');
const conversationList = ref([]);
const loginVisible = ref(false);
const aiLoading = ref(false);
const model = ref('GLM');
const imageUrl = ref('');
const dialogueDisplay = ref(false);
const dialogueCache = ref({});
const uploadedImages = ref([]);
const currentFiles = ref([]); // 当前上传的文件列表
const currentChatId = ref(''); // 当前会话的chatId
const sidebarCollapsed = ref(false); // 侧边栏折叠状态
const currentConversationId = ref(''); // 当前选中的对话ID
const conversations = ref([]); // 对话列表数据
const isMobile = ref(false); // 是否为移动设备
const resizeTimer = ref(null); // 防抖定时器
const isRag = ref(false); // RAG知识库检索开关
const isMcp = ref(false); // MCP协议开关

// RAG管理相关数据
const ragManagementVisible = ref(false); // RAG管理弹窗显示状态
const knowledgeFiles = ref([]); // 知识库文件列表
const knowledgeListLoading = ref(false); // 知识库列表加载状态
const uploadUrl = ref(process.env.VUE_APP_BASE_API + '/chat/rag/upload'); // 上传地址
const uploadHeaders = ref({
  'token': localStorage.getItem('token') || ''
}); // 上传请求头

// MCP管理相关数据
const mcpManagementVisible = ref(false); // MCP管理弹窗显示状态
const mcpServices = ref([]); // MCP服务列表（来自API的完整列表）
const selectedMcpIds = ref([]); // 用户选中的MCP服务ID列表
const mcpListLoading = ref(false); // MCP服务列表加载状态

// 从本地存储加载选中的MCP服务ID
function loadSelectedMcpIds() {
  try {
    const saved = localStorage.getItem('selectedMcpIds');
    if (saved) {
      selectedMcpIds.value = JSON.parse(saved);
    }
  } catch (error) {
    console.error('加载选中的MCP服务ID失败:', error);
    selectedMcpIds.value = [];
  }
}

// 保存选中的MCP服务ID到本地存储
function saveSelectedMcpIds() {
  try {
    localStorage.setItem('selectedMcpIds', JSON.stringify(selectedMcpIds.value));
  } catch (error) {
    console.error('保存选中的MCP服务ID失败:', error);
  }
}



// 从本地存储加载模型选择
function loadModelFromStorage() {
  try {
    const savedModel = localStorage.getItem('selectedModel');
    if (savedModel) {
      model.value = savedModel;
      console.log('DialogueView: 从本地存储加载模型:', savedModel);
    }
  } catch (error) {
    console.error('DialogueView: 加载模型选择失败:', error);
  }
}

// 编辑对话设置相关
const editTitleDialogVisible = ref(false);
const editTitleForm = ref({
  chatId: '',
  title: '',
  role: ''
});
const editTitleLoading = ref(false);
const currentEditingConversation = ref(null);
const showHelpTooltip = ref(false); // 控制帮助提示的显示

// 检查是否为移动设备
function checkMobile() {
  const oldMobile = isMobile.value;
  isMobile.value = window.innerWidth <= 768;
  
  // 在移动端自动收起侧边栏
  if (isMobile.value && !sidebarCollapsed.value) {
    sidebarCollapsed.value = true;
  }
  
  // 只在移动状态真正改变时才执行额外逻辑
  if (oldMobile !== isMobile.value) {
    console.log('设备类型变化:', isMobile.value ? '移动端' : '桌面端');
    // 这里可以添加其他需要在设备类型变化时执行的逻辑
  }
}

// 防抖的resize处理函数
function debouncedHandleResize() {
  if (resizeTimer.value) {
    clearTimeout(resizeTimer.value);
  }
  
  resizeTimer.value = setTimeout(() => {
    try {
      // 使用requestAnimationFrame确保在下一个渲染周期执行
      requestAnimationFrame(() => {
        checkMobile();
        // 其他resize逻辑可以在这里添加
      });
    } catch (error) {
      console.error('Resize处理错误:', error);
    }
  }, 200); // 增加防抖时间到200ms
}

// 生成随机chatId
function generateChatId() {
  return Date.now() + Math.floor(Math.random() * 1000);
}

// 获取或创建chatId
function getOrCreateChatId() {
  if (!currentChatId.value) {
    currentChatId.value = generateChatId();
  }
  return currentChatId.value;
}

// 获取对话列表
async function fetchConversations() {
  try {
    console.log('开始获取对话列表...');
    console.log('当前用户信息:', storeInstance.getters.userinfo);
    
    const response = await getConversationList()
    console.log('获取对话列表API响应:', response);
    
    if (response) {
        conversations.value = response;        

      // 调试时间格式
      if (conversations.value.length > 0) {
        console.log('第一个对话的时间数据:', {
          conversationId: conversations.value[0].conversationId,
          createdTime: conversations.value[0].createdTime,
          createdTimeType: typeof conversations.value[0].createdTime,
          formattedTime: formatTime(conversations.value[0].createdTime)
        });
      }

      // 如果有对话，且当前没有选中的对话，且对话列表为空（说明用户正在使用新对话），则不自动选择
      if (conversations.value.length > 0 && !currentConversationId.value && conversationList.value.length === 0) {
        console.log('用户正在使用新对话，不自动选择已有对话');
      } else if (conversations.value.length > 0 && !currentConversationId.value) {
          console.log('选择第一个对话:', conversations.value[0]);
          selectConversation(conversations.value[0]);
        }
    } else {
        console.log('API响应格式不正确:', response);
        conversations.value = [];
    }
  } catch (error) {
    console.error('获取对话列表失败:', error);
    conversations.value = [];
    ElNotification({
      title: '错误',
      message: '获取对话列表失败',
      type: 'error'
    });
  }
}

// 选择对话
function selectConversation(conversation) {
  console.log('选择对话:', conversation);
  currentConversationId.value = conversation.conversationId;
  currentChatId.value = conversation.conversationId;
  
  // 先清空当前对话内容，准备加载新对话
  conversationList.value = [];
  
  // 加载该对话的历史记录
  loadHistory(conversation.conversationId);
  
  // 更新对话标题
  if (conversation.title) {
    // 可以在这里更新页面标题或其他UI元素
  }
}

// 创建新对话
function createNewConversation() {
  console.log('创建新对话前的状态:', {
    currentConversationId: currentConversationId.value,
    currentChatId: currentChatId.value,
    conversationListLength: conversationList.value.length
  });
  
  currentConversationId.value = '';
  currentChatId.value = generateChatId();
  conversationList.value = [];
  
  console.log('创建新对话后的状态:', {
    currentConversationId: currentConversationId.value,
    currentChatId: currentChatId.value,
    conversationListLength: conversationList.value.length
  });
  
  // 清空当前对话内容
  clear();
  
  ElNotification({
    title: '成功',
    message: '已创建新对话',
    type: 'success'
  });
}

// 删除对话
async function deleteConversation(conversationId) {
  try {
    // 显示确认对话框
    await ElMessageBox.confirm(
        '确定要删除这个对话吗？删除后无法恢复。',
        '确认删除',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
        }
    );

    console.log('删除对话:', conversationId);
    // 调用删除API
    await deleteConversationAPI(conversationId);
    
    // 从列表中移除
    const index = conversations.value.findIndex(c => c.conversationId === conversationId);
    if (index !== -1) {
      conversations.value.splice(index, 1);
    }
    
    // 如果删除的是当前对话，切换到其他对话
    if (currentConversationId.value === conversationId) {
      if (conversations.value.length > 0) {
        selectConversation(conversations.value[0]);
      } else {
        createNewConversation();
      }
    }
    
    ElNotification({
      title: '成功',
      message: '对话已删除',
      type: 'success'
    });
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消删除，不需要显示错误
      return;
    }

    console.error('删除对话失败:', error);
    ElNotification({
      title: '错误',
      message: '删除对话失败',
      type: 'error'
    });
  }
}

// 切换侧边栏
function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value; // 切换侧边栏状态
}

// 导航到数据统计页面
function navigateToDataView() {
  router.push('/mydata');
}

// 编辑对话设置
function editConversationTitle(conversation) {
  console.log('编辑对话设置:', conversation);
  
  if (!conversation || !conversation.conversationId) {
    ElNotification({
      title: '错误',
      message: '无效的对话对象',
      type: 'error'
    });
    return;
  }
  
  currentEditingConversation.value = conversation;
  editTitleForm.value = {
    chatId: conversation.conversationId,
    title: conversation.title || '',
    role: conversation.role || ''
  };
  editTitleDialogVisible.value = true;
  
  // 在下一个tick中聚焦到输入框
  nextTick(() => {
    const inputElement = document.querySelector('.el-dialog__body .el-input__inner');
    if (inputElement) {
      inputElement.focus();
      inputElement.select();
    }
  });
}

// 保存对话设置
async function saveConversationTitle() {
  try {
    const title = editTitleForm.value.title.trim();
    const role = editTitleForm.value.role.trim();
    
    if (!title) {
      ElNotification({
        title: '错误',
        message: '对话名称不能为空',
        type: 'error'
      });
      return;
    }
    
    if (title.length < 2) {
      ElNotification({
        title: '错误',
        message: '对话名称至少需要2个字符',
        type: 'error'
      });
      return;
    }
    
    if (title.length > 50) {
      ElNotification({
        title: '错误',
        message: '对话名称不能超过50个字符',
        type: 'error'
      });
      return;
    }
    
    if (role.length > 1000) {
      ElNotification({
        title: '错误',
        message: '系统角色提示词不能超过1000个字符',
        type: 'error'
      });
      return;
    }
    
    // 检查是否与当前设置相同
    if (currentEditingConversation.value && 
        currentEditingConversation.value.title === title &&
        (currentEditingConversation.value.role || '') === role) {
      ElNotification({
        title: '提示',
        message: '对话设置没有变化',
        type: 'info'
      });
      editTitleDialogVisible.value = false;
      return;
    }
    
    editTitleLoading.value = true;
    
    const response = await updateConversation({
      chatId: editTitleForm.value.chatId,
      title: title,
      role: role
    });
    
    console.log('更新对话设置响应:', response);
    
    if (!response) {
      // 更新本地对话列表中的信息
      const conversationIndex = conversations.value.findIndex(
        c => c.conversationId === editTitleForm.value.chatId
      );
      
      if (conversationIndex !== -1) {
        conversations.value[conversationIndex].title = title;
        conversations.value[conversationIndex].role = role;
      }
      
      
      ElNotification({
        title: '成功',
        message: '对话设置已更新',
        type: 'success'
      });
      
      editTitleDialogVisible.value = false;
      editTitleForm.value = { chatId: '', title: '', role: '' };
      currentEditingConversation.value = null;
    } else {
      throw new Error('更新失败');
    }
    
  } catch (error) {
    console.error('更新对话设置失败:', error);
    ElNotification({
      title: '错误',
      message: '更新对话设置失败: ' + (error.message || error),
      type: 'error'
    });
  } finally {
    editTitleLoading.value = false;
  }
}

// 处理编辑对话框关闭
function handleEditDialogClose() {
  editTitleForm.value = { chatId: '', title: '', role: '' };
  currentEditingConversation.value = null;
  editTitleLoading.value = false;
}

// 格式化时间
function formatTime(timestamp) {
  if (!timestamp) {
    return '未知时间';
  }

  console.log('原始时间数据:', timestamp, '类型:', typeof timestamp);
  
  try {
    let date;
    
    // 处理不同的时间格式
    if (typeof timestamp === 'string') {
      // Java LocalDateTime常见格式处理
      if (timestamp.includes('T')) {
        // 处理 "2024-01-15T14:30:00" 格式
        if (!timestamp.includes('Z') && !timestamp.includes('+')) {
          // 如果没有时区信息，假设是本地时间
          timestamp = timestamp + 'Z';
        }
        date = new Date(timestamp);
      } else if (timestamp.includes('-') && timestamp.includes(':')) {
        // 处理 "2024-01-15 14:30:00" 格式
        const formattedTimestamp = timestamp.replace(' ', 'T') + 'Z';
        date = new Date(formattedTimestamp);
      } else {
        // 尝试解析数字字符串
        const numTimestamp = parseInt(timestamp);
        if (!isNaN(numTimestamp)) {
          date = new Date(numTimestamp);
        } else {
          date = new Date(timestamp);
        }
      }
    } else if (typeof timestamp === 'number') {
      date = new Date(timestamp);
    } else {
      date = new Date(timestamp);
    }

    console.log('解析后的Date对象:', date);
    console.log('Date对象是否有效:', !isNaN(date.getTime()));
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.log('无效的日期:', timestamp);
      return '无效时间';
    }
    
    // 获取月、日、时、分
    const month = date.getMonth() + 1; // getMonth() 返回 0-11
    const day = date.getDate();
    const hours = date.getHours();
    const minutes = date.getMinutes();

    console.log('解析的时间组件:', { month, day, hours, minutes });

    // 格式化月/日 时:分
    const monthDay = `${month.toString().padStart(2, '0')}/${day.toString().padStart(2, '0')}`;
    const time = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;

    const result = `${monthDay} ${time}`;
    console.log('最终格式化结果:', result);

    return result;
  } catch (error) {
    console.error('时间格式化错误:', error, timestamp);
    return '时间错误';
  }
}

// 计算属性
const storeInstance = useStore();
const router = useRouter();

// 生命周期
onBeforeUnmount(() => {
  // 清理resize事件监听器
  window.removeEventListener('resize', debouncedHandleResize);
  
  // 清理错误处理器
  if (window._resizeObserverErrorHandler) {
    window.removeEventListener('error', window._resizeObserverErrorHandler);
    window.removeEventListener('unhandledrejection', window._resizeObserverErrorHandler);
    delete window._resizeObserverErrorHandler;
  }
  
  // 恢复原始console.error
  if (window._originalConsoleError) {
    console.error = window._originalConsoleError;
    delete window._originalConsoleError;
  }
  
  // 恢复原始ResizeObserver
  if (window._OriginalResizeObserver) {
    window.ResizeObserver = window._OriginalResizeObserver;
    delete window._OriginalResizeObserver;
  }
  
  // 清理定时器
  if (resizeTimer.value) {
    clearTimeout(resizeTimer.value);
    resizeTimer.value = null;
  }
  
  // 原有的清理逻辑（如果有需要的话）
  const element = document.getElementById('my-element');
  if (element) {
    element.removeEventListener('resize', handleResize);
  }
});

onMounted(() => {
  console.log('DialogueView 组件已挂载');
  console.log('当前用户信息:', storeInstance.getters.userinfo);
  
  // 更强的ResizeObserver错误抑制
  const resizeObserverErrorHandler = (e) => {
    if (e.message && (
        e.message.includes('ResizeObserver loop completed with undelivered notifications') || 
        e.message.includes('ResizeObserver loop limit exceeded') ||
        e.message.includes('ResizeObserver loop') ||
        e.message.includes('ResizeObserver')
    )) {
      // 完全静默这些错误，不在控制台显示
      e.preventDefault();
      e.stopImmediatePropagation();
      return false;
    }
  };
  
  // 全局错误抑制
  if (!window._originalConsoleError) {
    window._originalConsoleError = console.error;
    console.error = (...args) => {
      if (args[0] && typeof args[0] === 'string' && args[0].includes('ResizeObserver')) {
        return; // 静默ResizeObserver相关错误
      }
      window._originalConsoleError.apply(console, args);
    };
  }
  
  // 处理 ResizeObserver 错误
  window.addEventListener('error', resizeObserverErrorHandler, true);
  window.addEventListener('unhandledrejection', (e) => {
    if (e.reason && (
      (e.reason.message && e.reason.message.includes('ResizeObserver')) ||
      (typeof e.reason === 'string' && e.reason.includes('ResizeObserver'))
    )) {
      e.preventDefault();
      return false;
    }
  });
  
  // 重写ResizeObserver构造函数以添加错误处理
  if (window.ResizeObserver && !window._OriginalResizeObserver) {
    window._OriginalResizeObserver = window.ResizeObserver;
    window.ResizeObserver = class extends window._OriginalResizeObserver {
      constructor(callback) {
        const wrappedCallback = (entries, observer) => {
          try {
            // 使用requestAnimationFrame延迟执行
            requestAnimationFrame(() => {
              callback(entries, observer);
            });
          } catch (error) {
            // 静默处理错误
            if (!error.message || !error.message.includes('ResizeObserver')) {
              console.error('ResizeObserver callback error:', error);
            }
          }
        };
        super(wrappedCallback);
      }
    };
  }
  
  // 初始检查设备类型
  checkMobile();
  
  // 添加resize事件监听器
  window.addEventListener('resize', debouncedHandleResize);
  
  // 从本地存储加载模型选择
  loadModelFromStorage();
  
  // 从本地存储加载选中的MCP服务ID
  loadSelectedMcpIds();
  
  imageUrl.value = process.env.VUE_APP_IMAGE;
  loadDialogueCache();
  
  // 如果用户已登录，获取对话列表
  if (storeInstance.getters.userinfo) {
    console.log('用户已登录，开始获取对话列表');
    fetchConversations();
    getUser();
  } else {
    console.log('用户未登录，等待登录状态变化');
  }
  
  // 保存错误处理器引用以便清理
  window._resizeObserverErrorHandler = resizeObserverErrorHandler;
});

// 监听用户登录状态变化
watch(() => storeInstance.getters.userinfo, (newUserInfo, oldUserInfo) => {
  console.log('用户登录状态变化:', { old: oldUserInfo, new: newUserInfo });
  
  if (newUserInfo) {
    console.log('用户登录成功，开始获取对话列表');
    // 用户登录后获取对话列表
    fetchConversations();
  } else {
    console.log('用户登出，清空对话列表');
    conversations.value = [];
    currentConversationId.value = '';
  }
}, { deep: false }); // 避免深度监听，只在用户信息对象引用变化时触发

// 监听模型变化
watch(() => model.value, (newModel, oldModel) => {
  console.log(`模型从 ${oldModel} 切换到 ${newModel}`);
  
  if (aiLoading.value) {
    aiLoading.value = false;
  }
  uploadedImages.value = [];
  // 如果模型不是GLM，清空当前文件
  if (newModel !== 'GLM') {
    currentFiles.value = [];
  }
});

// 方法定义
function handleResize() {
  // 保留原有函数以防其他地方调用，但内部使用防抖版本
  debouncedHandleResize();
}

function handleFileUploaded(file) {
  // 文件已经在 InputFormField 中添加到列表，这里只需要记录
  if (!currentFiles.value.some(f => f.name === file.name)) {
    currentFiles.value.push(file);
  }
}

function handleFileRemoved(file) {
  const index = currentFiles.value.findIndex(f => f.name === file.name);
  if (index !== -1) {
    currentFiles.value.splice(index, 1);
  }
}

// 从顶部文件预览中移除文件
function removeFileFromTop(index) {
  if (currentFiles.value[index]) {
    const file = currentFiles.value[index];
    // 通知 InputFormField 组件文件已被移除
    if (inputRef.value) {
      inputRef.value.removeFile(index);
    }
    currentFiles.value.splice(index, 1);
    
    ElNotification({
      title: '成功',
      message: '文件已移除',
      type: 'success'
    });
  }
}

// 处理图片加载错误
function handleImageError(event) {
  event.target.style.display = 'none';
  console.error('图片加载失败:', event.target.src);
}

// 处理图标加载错误
function handleIconError(event) {
  // 使用默认图标替换加载失败的图片
  event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJMMTMuMDkgOC4yNkwyMCA5TDEzLjA5IDE1Ljc0TDEyIDIyTDEwLjkxIDE1Ljc0TDQgOUwxMC45MSA4LjI2TDEyIDJaIiBmaWxsPSIjODE2NkU3Ii8+Cjwvc3ZnPgo=';
  console.warn('MCP服务图标加载失败，使用默认图标:', event.target.alt);
}

// 格式化文件大小
function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// 判断是否为图片文件
function isImageFile(url) {
  if (!url) return false;
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg'];
  const lowerUrl = url.toLowerCase();
  return imageExtensions.some(ext => lowerUrl.includes(ext));
}

// 从URL中提取文件名
function getFileName(url) {
  if (!url) return '未知文件';
  try {
    const urlParts = url.split('/');
    const fileName = urlParts[urlParts.length - 1];
    // 如果文件名包含查询参数，去除它们
    return fileName.split('?')[0] || '未知文件';
  } catch (error) {
    console.error('获取文件名失败:', error);
    return '未知文件';
  }
}



function calculateWidth(textLength) {
  let width = 100 + textLength * 16;
  if (width >= 740) {
    width = 740;
  }
  return `${width}px`;
}

    function switchChat(index) {
      dialogueCache.value.index = index;
      conversationList.value = dialogueCache.value.array[index].context;
  localStorage.setItem('dialogueCache', JSON.stringify(dialogueCache.value));
      dialogueDisplay.value = false;
    }

    function clearDialogue(index) {
      if (index !== dialogueCache.value.index) {
        let i = parseInt(dialogueCache.value.index);
        if (index < i) {
          dialogueCache.value.index = i - 1;
        }
        dialogueCache.value.array.splice(index, 1);
      }
  localStorage.setItem('dialogueCache', JSON.stringify(dialogueCache.value));
    }

    function writeDialogue() {
      try {
        let item = conversationList.value;
        let value = dialogueCache.value;
        
        dialogueCache.value.array[value.index].time = Date.now();
        
        if (item.length > 0) {
          dialogueCache.value.array[value.index].title = item[
            item.length - 1
      ].user.trim().slice(0, 25);
        }
        
        dialogueCache.value.array[value.index].context = item;
        
        if (dialogueCache.value.array.length > 20) {
          const currentIndex = dialogueCache.value.index;
          const currentDialogue = dialogueCache.value.array[currentIndex];
          
          const sortedDialogues = [...dialogueCache.value.array]
            .sort((a, b) => b.time - a.time)
            .slice(0, 20);
          
          const newIndex = sortedDialogues.findIndex(d => d === currentDialogue);
          dialogueCache.value.index = newIndex >= 0 ? newIndex : 0;
          dialogueCache.value.array = sortedDialogues;
        }
        
    const maxContextLength = 50;
        if (item.length > maxContextLength) {
          dialogueCache.value.array[value.index].context = item.slice(-maxContextLength);
        }
        
    localStorage.setItem('dialogueCache', JSON.stringify(dialogueCache.value));
      } catch (error) {
    console.error('保存对话缓存时出错:', error);
        ElNotification({
          title: "保存失败",
          message: "保存对话历史时出现错误",
          type: "error"
        });
      }
    }

    function createdNewChat() {
      dialogueCache.value.array.unshift({
        title: "新对话",
        time: Date.now(),
        context: [],
      });
      dialogueCache.value.index = 0;
      conversationList.value = [];
  currentChatId.value = generateChatId(); // 为新对话生成新的chatId
  localStorage.setItem('dialogueCache', JSON.stringify(dialogueCache.value));
}

    async function onSubmit() {
  if (!storeInstance.getters.userinfo) {
        loginVisible.value = true;
        return;
      }

  if (input.value.trim() === "" && currentFiles.value.length === 0) return;

      let index = conversationList.value.length;
      let content = input.value;
      inputRef.value.resetInputValue();
      
  // 构建展示数据（仅文本 + 可选文件）
  if (currentFiles.value.length > 0) {
    const firstFile = currentFiles.value[0];
    conversationList.value.push({
      user: content || `上传了文件: ${firstFile.name}`,
      hasImage: false,
      hasFile: true,
      files: [{ name: firstFile.name, size: firstFile.size }],
      model: model.value, // 添加当前选择的模型
      media: '' // 初始化媒体字段，实际媒体URL会在服务器响应后更新
    });
      } else {
        conversationList.value.push({
          user: content,
      hasImage: false,
      hasFile: false,
      model: model.value, // 添加当前选择的模型
      media: '' // 初始化媒体字段
        });
      }
      
      aiLoading.value = true;
  nextTick(() => {
      scrollToTheBottom();
  });
  
  try {
      const chatId = getOrCreateChatId(); // 获取或创建chatId
      const baseURL = process.env.VUE_APP_BASE_API

      // 获取当前对话的role信息
      const currentConversation = conversations.value.find(c => c.conversationId === currentConversationId.value);
      const currentRole = currentConversation ? currentConversation.role : '';

      // 准备请求数据
      const requestData = {
        message: content,
        chatId: chatId,
        model: model.value,
        isRag: isRag.value,
        isMcp: isMcp.value,
        role: currentRole,
        mcpList: selectedMcpIds.value // 添加选中的MCP服务ID列表
      };

      let response;
      
      // 始终使用 FormData，支持单个文件
      const formData = new FormData();
      formData.append('message', content);
      formData.append('chatId', chatId);
      formData.append('model', model.value);
      formData.append('isRag', isRag.value.toString()); // 转换为字符串
      formData.append('isMcp', isMcp.value.toString()); // 转换为字符串
      formData.append('role', currentRole || ''); // 添加角色信息
      formData.append('mcpList', JSON.stringify(selectedMcpIds.value)); // 添加MCP服务ID列表
      
      // 只添加第一个文件（一次对话只能上传一个文件）
      if (currentFiles.value.length > 0) {
        formData.append('file', currentFiles.value[0]);
      }
      // 如果没有文件，不添加空的file字段
      
      response = await fetch(baseURL + '/chat/chat', {
        method: 'POST',
        headers: {
          "token": localStorage.getItem('token')
        },
        body: formData
      });
      
      if (response.body) {
        const reader = response.body.getReader();
        let receivedText = '';
        
        while (true) {
          const {done, value} = await reader.read();
          if (done) break;
          
          const chunk = new TextDecoder().decode(value);
          receivedText += chunk;
          
          conversationList.value[conversationList.value.length - 1].assistant = receivedText;
          nextTick(() => {
          scrollToTheBottom();
          });
        }
        
        conversationList.value[conversationList.value.length - 1].isError = false;
        
            writeDialogue();
        // 只在必要时更新用户信息，避免触发对话列表刷新
        // getUser();
      }
    } catch (error) {
      conversationList.value[conversationList.value.length - 1].assistant = '请求失败，请稍后重试';
      conversationList.value[conversationList.value.length - 1].isError = true;
    } finally {
            aiLoading.value = false;
      nextTick(() => {
            scrollToTheBottom();
      });

      // 清空已选择的文件
      currentFiles.value = [];
    }
}

async function loadHistory(chatId) {
  try {
    const result = await getChatHistory(chatId);
    console.log('获取到的历史记录:', result);
    
    if (Array.isArray(result)) {
      // 将交替的用户消息和AI回答转换为对话格式
      const formattedConversations = [];
      
      for (let i = 0; i < result.length; i += 2) {
        const userMessage = result[i];
        const aiResponse = result[i + 1];
        
        if (userMessage && userMessage.type === 'USER') {
          const conversationItem = {
            user: userMessage.content || '',
            assistant: aiResponse && aiResponse.type === 'ASSISTANT' ? aiResponse.content : '',
            timestamp: userMessage.timestamp,
            hasImage: false,
            model: aiResponse && aiResponse.model ? aiResponse.model : null, // 添加模型信息
            media: userMessage.media || '' // 添加媒体字段
          };
          
          formattedConversations.push(conversationItem);
        }
      }
      
      console.log('格式化后的对话列表:', formattedConversations);
      conversationList.value = formattedConversations;
      
      // 滚动到底部
      nextTick(() => {
        scrollToTheBottom();
      });
    } else {
      console.log('历史记录数据格式不正确:', result);
      conversationList.value = [];
    }
  } catch (e) {
    console.error('获取历史记录失败:', e);
    ElNotification({
      title: '错误',
      message: '获取历史记录失败',
      type: 'error'
    });
    conversationList.value = [];
  }
}

function clear() {
  conversationList.value = [];
  currentChatId.value = generateChatId(); // 清除对话时生成新的chatId
  uploadedImages.value = []; // 清空图片（保留变量但不再使用）
  currentFiles.value = []; // 清空文件
  writeDialogue();
}

function handleCopyCodeSuccess(code) {
  navigator.clipboard.writeText(code);
  ElNotification({
    message: "复制成功",
    type: "success",
  });
}

function copyAnswer(data) {
  navigator.clipboard.writeText(data);
  ElNotification({
    message: "复制成功",
    type: "success",
  });
}

function closeSocket() {
  aiLoading.value = false;
}

async function onCollection(item, index) {
  try {
    let bol = !conversationList.value[index].isCollection;
    if (bol) {
      try {
        await FavoritesAdd({
          issue: item.user,
          answer: item.assistant,
        });
        ElNotification({
          message: "收藏成功",
          type: "success",
        });
      } catch (e) {
        ElNotification({
          message: e,
          type: "error",
        });
      }
    }
    conversationList.value[index].isCollection = true;
  } catch (e) {
    ElNotification({
      message: e,
      type: "error",
    });
  }
}

async function getUser() {
  let res = await GetUserInfo();
  storeInstance.commit("setUserinfo", res);
}

function scrollToTheBottom() {
  nextTick(() => {
    // 使用requestAnimationFrame确保在渲染完成后执行
    requestAnimationFrame(() => {
      if (scrollRef.value && scrollRef.value.scrollHeight !== undefined) {
        // 平滑滚动到底部
        scrollRef.value.scrollTo({
          top: scrollRef.value.scrollHeight,
          behavior: 'smooth'
        });
      }
    });
  });
}

    function loadDialogueCache() {
      try {
    if (!storeInstance.getters.userinfo) {
          loginVisible.value = true;
          return;
        }
        
        let item = localStorage.getItem("dialogueCache");
        if (item) {
          try {
            dialogueCache.value = JSON.parse(item);
            if (!dialogueCache.value.array || !Array.isArray(dialogueCache.value.array)) {
              throw new Error("对话缓存格式错误");
            }
            
            let value = dialogueCache.value;
            
            if (value.index >= value.array.length) {
              value.index = 0;
            }
            
            if (value.array.length > 0) {
              conversationList.value = value.array[value.index].context || [];
            }
            
        // 确保DOM渲染完成后再滚动
        nextTick(() => {
            scrollToTheBottom();
        });
          } catch (parseError) {
            console.error("解析对话缓存失败:", parseError);
            initEmptyDialogue();
            ElNotification({
              title: "缓存加载失败",
              message: "对话历史加载失败，已重置为新对话",
              type: "warning"
            });
          }
        } else {
          initEmptyDialogue();
        }
      } catch (error) {
        console.error("加载对话缓存出错:", error);
        initEmptyDialogue();
      }
    }
    
    function initEmptyDialogue() {
      dialogueCache.value = {
        index: 0,
        array: [
          {
            title: "新对话",
            time: Date.now(),
            context: [],
          },
        ],
      };
      conversationList.value = [];
  currentChatId.value = generateChatId(); // 初始化时生成chatId
      try {
    localStorage.setItem('dialogueCache', JSON.stringify(dialogueCache.value));
      } catch (error) {
        console.error("保存初始对话缓存失败:", error);
      }
    }

// ========================= RAG管理相关方法 =========================

// 计算文件大小的辅助函数（估算）
function calculateFileSize(url) {
  if (!url) return 0;
  // 这里只是一个简单的估算，实际项目中应该从后端获取真实文件大小
  // 可以根据文件扩展名或其他信息进行估算
  const extension = url.toLowerCase().split('.').pop();
  const sizeMap = {
    'txt': 1024 * 100, // 100KB
    'md': 1024 * 50 // 50KB
  };
  return sizeMap[extension] || 1024 * 50; // 默认50KB
}

// 打开RAG管理弹窗
function openRagManagement() {
  ragManagementVisible.value = true;
  fetchKnowledgeFiles();
}

// 获取知识库文件列表
async function fetchKnowledgeFiles() {
  try {
    knowledgeListLoading.value = true;
    // 调用获取知识库文件列表的API
    const response = await getRagKnowledgeList();
    
    if (response && Array.isArray(response)) {
      // 处理API返回的数据，将后端字段映射到前端需要的格式
      knowledgeFiles.value = response.map(item => ({
        id: item.ragId,
        ragId: item.ragId,
        name: item.ragName,
        ragName: item.ragName,
        url: item.ragUrl,
        ragUrl: item.ragUrl,
        userId: item.userId,
        isEnable: item.isEnable,
        size: calculateFileSize(item.ragUrl), // 从URL或文件名估算大小，实际项目中应该从后端获取
        uploadTime: item.createdTime,
        createdTime: item.createdTime,
        status: item.isEnable ? 'processed' : 'disabled' // 根据isEnable状态设置
      }));
      console.log('获取知识库文件列表成功:', knowledgeFiles.value);
    } else {
      console.warn('API返回数据格式不正确:', response);
      knowledgeFiles.value = [];
    }
  } catch (error) {
    console.error('获取知识库文件列表失败:', error);
    
    // 如果API失败，使用模拟数据进行演示
    knowledgeFiles.value = [
      {
        id: '1',
        ragId: 1,
        name: '技术文档.txt',
        ragName: '技术文档.txt',
        url: '/uploads/tech-doc.txt',
        ragUrl: '/uploads/tech-doc.txt',
        userId: 1,
        isEnable: true,
        size: 102400,
        uploadTime: new Date().toISOString(),
        createdTime: new Date().toISOString(),
        status: 'processed'
      },
      {
        id: '2',
        ragId: 2,
        name: 'API手册.md',
        ragName: 'API手册.md',
        url: '/uploads/api-manual.md',
        ragUrl: '/uploads/api-manual.md',
        userId: 1,
        isEnable: false,
        size: 51200,
        uploadTime: new Date(Date.now() - 86400000).toISOString(),
        createdTime: new Date(Date.now() - 86400000).toISOString(),
        status: 'disabled'
      }
    ];
    
    ElNotification({
      title: '提示',
      message: '正在使用演示数据，请确保后端API已启动',
      type: 'warning'
    });
  } finally {
    knowledgeListLoading.value = false;
  }
}

// 刷新知识库列表
function refreshKnowledgeList() {
  fetchKnowledgeFiles();
}

// 上传前检查
function beforeRagUpload(file) {
  const isValidType = ['txt', 'md', 'pdf'].some(type => 
    file.name.toLowerCase().endsWith(`.${type}`)
  );
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isValidType) {
    ElNotification({
      title: '错误',
      message: '文件格式不支持，请上传 .txt, .md, .pdf 格式的文件',
      type: 'error'
    });
    return false;
  }
  if (!isLt5M) {
    ElNotification({
      title: '错误',
      message: '文件大小不能超过 5MB',
      type: 'error'
    });
    return false;
  }
  return true;
}

// 上传成功回调
function handleRagUploadSuccess(response, file) {
  ElNotification({
    title: '成功',
    message: `文件 ${file.name} 上传成功`,
    type: 'success'
  });
  // 刷新列表
  fetchKnowledgeFiles();
}

// 上传失败回调
function handleRagUploadError(error, file) {
  console.error('文件上传失败:', error);
  ElNotification({
    title: '错误',
    message: `文件 ${file.name} 上传失败`,
    type: 'error'
  });
}

// 切换知识库文件启用状态
async function toggleKnowledgeFileStatus(file) {
  try {
    file.toggling = true;
    const newStatus = !file.isEnable;
    
    try {
      // 调用切换状态的API
      await toggleRagKnowledgeStatus(file.ragId || file.id);
      
      // 更新本地状态
      file.isEnable = newStatus;
      file.status = newStatus ? 'processed' : 'disabled';
      
      ElNotification({
        title: '成功',
        message: `文件已${newStatus ? '启用' : '禁用'}`,
        type: 'success'
      });
    } catch (apiError) {
      console.error('切换文件状态失败:', apiError);
      ElNotification({
        title: '错误',
        message: '操作失败: ' + (apiError.message || apiError),
        type: 'error'
      });
    }
  } catch (error) {
    console.error('切换知识库文件状态失败:', error);
  } finally {
    file.toggling = false;
  }
}

// 删除知识库文件
async function deleteKnowledgeFile(file) {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${file.name || file.ragName}" 吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    file.deleting = true;
    
    try {
      // 调用删除知识库文件的API，使用ragId
      await deleteRagKnowledge(file.ragId || file.id);
      
      // 从列表中移除
      const index = knowledgeFiles.value.findIndex(f => (f.ragId || f.id) === (file.ragId || file.id));
      if (index !== -1) {
        knowledgeFiles.value.splice(index, 1);
      }
      
      ElNotification({
        title: '成功',
        message: '文件删除成功',
        type: 'success'
      });
    } catch (apiError) {
      console.error('删除知识库文件失败:', apiError);
      ElNotification({
        title: '错误',
        message: '删除文件失败: ' + (apiError.message || apiError),
        type: 'error'
      });
    }
  } catch (error) {
    if (error === 'cancel') {
      return;
    }
    console.error('用户取消删除或其他错误:', error);
  } finally {
    file.deleting = false;
  }
}

// ========================= MCP管理相关方法 =========================

// 打开MCP管理弹窗
function openMcpManagement() {
  mcpManagementVisible.value = true;
  fetchMcpServices();
}

// 获取MCP服务列表
async function fetchMcpServices() {
  try {
    mcpListLoading.value = true;
    // 调用获取MCP服务列表的API
    const response = await getMcpServiceList();
    
    if (response && Array.isArray(response)) {
      // 处理API返回的数据
      mcpServices.value = response.map(service => ({
        ...service,
        id: service.mcpsId, // 使用mcpsId作为id
        name: service.mcpName,
        description: service.introduce,
        createdTime: service.createdTime,
        icon: service.icon // 直接使用API返回的图标URL
      }));
      console.log('获取MCP服务列表成功:', mcpServices.value);
    } else {
      console.warn('API返回数据格式不正确:', response);
      mcpServices.value = [];
    }
  } catch (error) {
    console.error('获取MCP服务列表失败:', error);
    
    // 如果API失败，使用模拟数据进行演示
    mcpServices.value = [
      {
        mcpsId: 1,
        methodName: 'amap',
        mcpName: '高德地图',
        introduce: '提供地图搜索、路径规划、地理编码等服务',
        icon: 'https://webapi.amap.com/images/amap.png',
        createdTime: new Date().toISOString(),
        id: 1,
        name: '高德地图',
        description: '提供地图搜索、路径规划、地理编码等服务'
      },
      {
        mcpsId: 2,
        methodName: 'weather',
        mcpName: '天气服务', 
        introduce: '获取实时天气信息和天气预报',
        icon: 'https://example.com/weather-icon.png',
        createdTime: new Date().toISOString(),
        id: 2,
        name: '天气服务',
        description: '获取实时天气信息和天气预报'
      },
      {
        mcpsId: 3,
        methodName: 'calculator',
        mcpName: '计算器',
        introduce: '数学计算和公式解析服务',
        icon: 'https://example.com/calculator-icon.png',
        createdTime: new Date().toISOString(),
        id: 3,
        name: '计算器',
        description: '数学计算和公式解析服务'
      }
    ];
    
    ElNotification({
      title: '提示',
      message: '正在使用演示数据，请确保后端API已启动',
      type: 'warning'
    });
  } finally {
    mcpListLoading.value = false;
  }
}



// 刷新MCP服务列表
function refreshMcpServices() {
  fetchMcpServices();
}

// 检查服务是否已选中
function isServiceSelected(service) {
  return selectedMcpIds.value.includes(service.mcpsId);
}

// 切换MCP服务的选中状态
function toggleMcpServiceSelection(service) {
  const serviceId = service.mcpsId;
  const index = selectedMcpIds.value.indexOf(serviceId);
  
  if (index > -1) {
    // 如果已选中，则取消选中
    selectedMcpIds.value.splice(index, 1);
    ElNotification({
      title: '提示',
      message: `已取消选择 ${service.mcpName}`,
      type: 'info'
    });
  } else {
    // 如果未选中，则添加到选中列表
    selectedMcpIds.value.push(serviceId);
    ElNotification({
      title: '成功',
      message: `已选择 ${service.mcpName}`,
      type: 'success'
    });
  }
  
  // 保存到本地存储
  saveSelectedMcpIds();
  console.log('当前选中的MCP服务ID:', selectedMcpIds.value);
}

// 获取选中的MCP服务详情
function getSelectedMcpServices() {
  return mcpServices.value.filter(service => 
    selectedMcpIds.value.includes(service.mcpsId)
  );
}

// 暴露给模板
defineExpose({
  handleResize,
  debouncedHandleResize,
  checkMobile,
  handleFileUploaded,
  handleFileRemoved,
  removeFileFromTop,
  handleImageError,
  handleIconError,
  isImageFile,
  getFileName,
  calculateWidth,
  switchChat,
  clearDialogue,
  writeDialogue,
  createdNewChat,
  onSubmit,
  loadHistory,
  clear,
  handleCopyCodeSuccess,
  copyAnswer,
  closeSocket,
  onCollection,
  getUser,
  scrollToTheBottom,
  loadDialogueCache,
  initEmptyDialogue,
  fetchConversations, // 暴露给模板
  selectConversation, // 暴露给模板
  createNewConversation, // 暴露给模板
  deleteConversation, // 暴露给模板
  toggleSidebar, // 暴露给模板
  navigateToDataView, // 暴露给模板
  formatTime, // 暴露给模板
  editConversationTitle, // 暴露给模板
  saveConversationTitle, // 暴露给模板
  handleEditDialogClose, // 暴露给模板
  loadModelFromStorage, // 暴露模型加载函数
  isRag, // 暴露RAG状态
  isMcp, // 暴露MCP状态
  // RAG管理相关
  openRagManagement,
  fetchKnowledgeFiles,
  refreshKnowledgeList,
  beforeRagUpload,
  handleRagUploadSuccess,
  handleRagUploadError,
  toggleKnowledgeFileStatus,
  deleteKnowledgeFile,
  // MCP管理相关
  openMcpManagement,
  fetchMcpServices,
  refreshMcpServices,
  loadSelectedMcpIds,
  saveSelectedMcpIds,
  isServiceSelected,
  toggleMcpServiceSelection,
  getSelectedMcpServices,
});
</script>

<style lang="scss" scoped>
@keyframes beating {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-10px);
  }
}

@keyframes slideEase {
  0% {
    transform: translateX(-100px);
  }
  100% {
    transform: translateX(0);
  }
}

@keyframes tooltipFadeIn {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.dialogue-container {
  background-color: var(--bgColor2);
  display: flex;
  height: calc(100vh - 60px); /* 减去顶部导航栏的高度 */
  width: 100%;
  overflow: hidden;
  position: relative; // 为遮罩层提供定位参考
}

// 移动端遮罩层
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: none; // 默认隐藏
  
  @media (max-width: 768px) {
    display: block; // 在移动端显示
  }
}

.sidebar {
  width: 300px;
  height: 100%; /* 使用100%而不是100vh，继承父容器的高度 */
  background-color: var(--bgColor1);
  border-right: 1px solid var(--borderColor);
  transition: all 0.3s ease;
  display: flex !important;
  flex-direction: column;
  z-index: 10;
  box-shadow: 2px 0 8px rgba(129, 102, 231, 0.1);
  flex-shrink: 0;
  visibility: visible !important;
  opacity: 1 !important;
  position: relative !important;
  overflow: hidden;

  &.sidebar-collapsed {
    width: 80px;
    
    .sidebar-header {
      padding: 15px 10px;

      .brand-section {
        .logo-container {
          flex-direction: column;
          margin-bottom: 8px;

          .sidebar-logo {
            width: 32px;
            height: 32px;
            margin-right: 0;
            margin-bottom: 6px;
          }

          .brand-info {
            text-align: center;

            .brand-title {
              font-size: 12px;
              line-height: 1.1;
            }

            .brand-subtitle {
              font-size: 10px;
              line-height: 1.1;
            }
          }
        }

        .brand-description {
        display: none;
      }
      }

      .action-buttons {
        margin-bottom: 15px;

        .new-conversation-btn {
          height: 36px;
          padding: 0;
          min-width: 36px;
          border-radius: 75%;
          margin-left: -10px;
          margin-bottom: 8px;
          justify-content: center;
          
          .btn-text {
            display: none;
          }
          
          .el-icon {
            margin: 0;
            font-size: 16px;
          }
          
          &:hover {
            transform: translateY(-1px) scale(1.05);
            will-change: transform;
          }
        }

        .button-row {
          display: flex;
          justify-content: center;
          gap: 8px;
          margin-bottom: 0;
          
          .data-stats-btn,
          .toggle-sidebar-btn {
            height: 36px;
            padding: 0;
            min-width: 36px;
            border-radius: 75%;
            justify-content: center;
            flex: 0 0 36px;
            
            .btn-text {
              display: none;
            }
            
            .el-icon {
              margin: 0;
              font-size: 16px;
            }
            
            &:hover {
              transform: translateY(-1px) scale(1.05);
              will-change: transform;
            }
          }
        }
      }

      .divider {
        margin: 0 0 12px 0;
      }

      .conversation-title {
        display: none;
      }
    }

    & + .main-content {
      margin-left: 80px; // 侧边栏折叠时调整主内容区域位置
    }
  }

  .sidebar-header {
    padding: 20px;
    text-align: center;
    border-bottom: 1px solid var(--borderColor);
    background-color: var(--bgColor1);

    .brand-section {
      margin-bottom: 20px;

      .logo-container {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;

        .sidebar-logo {
          width: 40px;
          height: 40px;
          margin-right: 12px;
          border-radius: 8px;
          box-shadow: 0 2px 8px rgba(129, 102, 231, 0.2);
        }

        .brand-info {
          text-align: left;

          .brand-title {
            margin: 0;
      font-size: 18px;
            font-weight: 700;
      color: var(--textColor1);
            line-height: 1.2;
          }

          .brand-subtitle {
            margin: 0;
            font-size: 12px;
            color: var(--textColor3);
            line-height: 1.2;
          }
        }
      }

      .brand-description {
        margin: 0;
        font-size: 12px;
        color: var(--textColor3);
        line-height: 1.4;
        padding: 0 10px;
      }
    }

    .action-buttons {
      margin-bottom: 20px;
      position: relative;

      .help-icon-container {
        position: relative;
        margin-bottom: 8px;
        display: flex;
        justify-content: center;
        
        @media (max-width: 768px) {
          margin-bottom: 6px;
          
          .help-icon {
            font-size: 14px;
            padding: 5px;
          }
          
          .help-tooltip {
            .tooltip-content {
              max-width: 240px;
              min-width: 200px;
              padding: 10px;
              
              .tooltip-title {
                font-size: 13px;
              }
              
              .tooltip-text {
                font-size: 11px;
              }
            }
          }
        }
        
        .help-icon {
          font-size: 16px;
          color: var(--textColor3);
          cursor: pointer;
          padding: 6px;
          border-radius: 50%;
          transition: all 0.3s ease;
          background-color: var(--bgColor2);
          border: 1px solid var(--borderColor);
          
          &:hover {
            color: var(--themeColor1);
            background-color: var(--themeColor1);
            color: var(--themeTextColor);
            transform: scale(1.1);
            box-shadow: 0 2px 8px rgba(129, 102, 231, 0.2);
          }
        }
        
        .help-tooltip {
          position: absolute;
          bottom: 100%;
          left: 50%;
          transform: translateX(-50%);
          margin-bottom: 8px;
          z-index: 1000;
          animation: tooltipFadeIn 0.3s ease;
          
          .tooltip-content {
            background: linear-gradient(135deg, var(--bgColor1) 0%, var(--bgColor2) 100%);
            border: 1px solid var(--borderColor);
            border-radius: 8px;
            padding: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            max-width: 280px;
            min-width: 240px;
            
            .tooltip-title {
              font-size: 14px;
              font-weight: 600;
              color: var(--textColor1);
              margin-bottom: 8px;
              text-align: center;
            }
            
            .tooltip-text {
              font-size: 12px;
              color: var(--textColor2);
              line-height: 1.5;
              text-align: left;
            }
          }
          
          .tooltip-arrow {
            position: absolute;
            top: 100%;
            left: 50%;
            transform: translateX(-50%);
            width: 0;
            height: 0;
            border-left: 6px solid transparent;
            border-right: 6px solid transparent;
            border-top: 6px solid var(--bgColor1);
            filter: drop-shadow(0 2px 2px rgba(0, 0, 0, 0.1));
          }
        }
      }

      .new-conversation-btn {
        width: 100%;
        height: 44px;
        margin-bottom: 12px;
        background: linear-gradient(135deg, var(--themeColor1) 0%, var(--themeColor2) 100%);
        border: none;
        border-radius: 12px;
        font-weight: 600;
        font-size: 14px;
        color: var(--themeTextColor);
        position: relative;
        overflow: hidden;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), 
                    box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 2px 8px rgba(129, 102, 231, 0.15);
        backface-visibility: hidden;
        -webkit-backface-visibility: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, transparent 50%);
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 8px 25px rgba(129, 102, 231, 0.25);
          will-change: transform, box-shadow;
          
          &::before {
            opacity: 1;
          }
        }

        &:active {
          transform: translateY(0);
          box-shadow: 0 2px 8px rgba(129, 102, 231, 0.2);
        }

        .btn-text {
          margin-left: 8px;
          font-weight: 600;
        }

        .el-icon {
          font-size: 16px;
        }
      }

      .button-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        width: 100%;
      }

      .data-stats-btn {
        flex: 1;
        height: 42px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        border-radius: 12px;
        font-weight: 600;
        font-size: 14px;
        color: white;
        position: relative;
        overflow: hidden;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), 
                    box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
        backface-visibility: hidden;
        -webkit-backface-visibility: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, transparent 50%);
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
          will-change: transform, box-shadow;
          
          &::before {
            opacity: 1;
          }
        }

        &:active {
          transform: translateY(0);
          box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
        }

        .btn-text {
          margin-left: 8px;
          font-weight: 600;
        }

        .el-icon {
          font-size: 16px;
        }
      }

      .toggle-sidebar-btn {
        flex: 1;
        height: 42px;
        background-color: var(--bgColor1);
        border: 1px solid var(--borderColor);
        border-radius: 12px;
        color: var(--textColor2);
        font-size: 13px;
        font-weight: 500;
        position: relative;
        overflow: hidden;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), 
                    box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1),
                    border-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
        backface-visibility: hidden;
        -webkit-backface-visibility: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: var(--themeColor1);
          opacity: 0;
          transition: opacity 0.3s ease;
        }
      
        &:hover {
          transform: translateY(-1px);
          border-color: var(--themeColor1);
          box-shadow: 0 4px 12px rgba(129, 102, 231, 0.15);
          will-change: transform, box-shadow, border-color;
          
          &::before {
            opacity: 0.08;
          }
          
          .btn-text, .el-icon {
            color: var(--themeColor1);
            position: relative;
            z-index: 1;
          }
        }

        &:active {
          transform: translateY(0);
        }
      
        &.collapsed {
          background: linear-gradient(135deg, var(--themeColor1) 0%, var(--themeColor2) 100%);
          border-color: var(--themeColor1);
          color: var(--themeTextColor);
          box-shadow: 0 2px 8px rgba(129, 102, 231, 0.2);

          &::before {
            opacity: 0;
          }

          &:hover {
            background: linear-gradient(135deg, var(--themeColor2) 0%, var(--themeColor1) 100%);
            
            .btn-text, .el-icon {
              color: var(--themeTextColor);
            }
          }
        }

        .btn-text {
          margin-left: 8px;
          font-weight: 500;
          position: relative;
          z-index: 1;
          transition: color 0.3s ease;
        }

        .el-icon {
          font-size: 14px;
          position: relative;
          z-index: 1;
          transition: color 0.3s ease;
        }
      }
    }

    .divider {
      height: 1px;
      background: linear-gradient(90deg, transparent, var(--borderColor), transparent);
      margin: 0 0 16px 0;
    }

    .conversation-title {
      margin: 0;
      font-size: 16px;
      color: var(--textColor1);
      font-weight: 600;
      text-align: center;
    }
  }

  .conversation-list {
    flex: 1;
    overflow: hidden;
    padding: 10px 0;
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
    min-height: 0;

    // 自定义滚动条样式
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background-color: var(--bgColor2);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: var(--themeColor1);
      border-radius: 3px;
      
      &:hover {
        background-color: var(--themeColor2);
      }
    }

    .conversation-item {
      display: flex !important;
      align-items: flex-start;
      justify-content: space-between;
      padding: 12px 16px;
      cursor: pointer;
      transition: all 0.3s ease;
      border-radius: 12px;
      margin: 6px 12px;
      background-color: var(--bgColor2);
      box-shadow: 0 2px 6px rgba(129, 102, 231, 0.08);
      position: relative;
      visibility: visible !important;
      opacity: 1 !important;
      min-height: 50px;
      border: 2px solid transparent;

      &:hover {
        background-color: var(--bgColor1);
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(129, 102, 231, 0.15);
        border-color: var(--themeColor1);
        
        .conversation-actions {
          opacity: 1;
          transform: translateX(0);
        }
      }

      &.active {
        background-color: var(--themeColor1);
        color: var(--themeTextColor);
        box-shadow: 0 6px 20px rgba(129, 102, 231, 0.25);
        border-color: var(--themeColor1);
        
        .conversation-title,
        .conversation-time,
        .role-text {
          color: var(--themeTextColor);
        }
        
        .role-tag {
          background-color: rgba(255, 255, 255, 0.2);
          border-color: rgba(255, 255, 255, 0.3);
        }
        
        .conversation-actions {
          opacity: 1;
        }
      }

      .conversation-content {
        flex-grow: 1;
        display: flex;
        flex-direction: column;
        gap: 6px;
        min-width: 0;
      }

      .conversation-title {
        font-size: 15px;
        color: var(--textColor1);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-weight: 600;
        transition: all 0.2s ease;
        line-height: 1.4;
        
        &:hover {
          color: var(--themeColor1);
        }
      }

      .conversation-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 8px;
      }

      .conversation-time {
        font-size: 11px;
        color: var(--textColor3);
        white-space: nowrap;
        opacity: 0.8;
        font-weight: 400;
      }

      .role-tag {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 8px;
        background-color: rgba(129, 102, 231, 0.1);
        border: 1px solid rgba(129, 102, 231, 0.2);
        border-radius: 12px;
        font-size: 11px;
        color: var(--themeColor1);
        transition: all 0.2s ease;
        
        .el-icon {
          font-size: 12px;
        }
        
        .role-text {
          font-weight: 500;
          max-width: 80px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        &:hover {
          background-color: rgba(129, 102, 231, 0.15);
          transform: scale(1.02);
        }
      }

      .conversation-actions {
        display: flex;
        gap: 6px;
        margin-left: 12px;
        opacity: 0;
        transform: translateX(10px);
        transition: all 0.3s ease;
        flex-shrink: 0;
        
        .action-btn {
          width: 28px;
          height: 28px;
          padding: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 6px;
          transition: all 0.2s ease;
          
          .el-icon {
            font-size: 14px;
          }
          
          &.edit-btn {
            &:hover {
              background-color: var(--themeColor1);
              border-color: var(--themeColor1);
              color: white;
              transform: scale(1.1);
            }
          }
          
          &.delete-btn {
            &:hover {
              background-color: #f56c6c;
              border-color: #f56c6c;
              color: white;
              transform: scale(1.1);
            }
          }
        }
      }
      
      &:hover .conversation-actions {
        opacity: 1;
        transform: translateX(0);
      }
    }
  }
}

.main-content {
  background-color: var(--bgColor2);
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: margin-left 0.3s ease;
  min-width: 0; // 防止内容溢出
  position: relative; // 为footer定位提供参考

  .body {
    flex-grow: 1;
    overflow-y: auto;
    padding: 0 20px 180px; // 增加底部padding，确保消息不被输入框遮挡
    display: flex;
    flex-direction: column;
    align-items: center;
    max-width: 900px;
    width: 100%;
    margin: 0 auto;
  }
}

// 确保footer在主内容区域内正确定位
.footer {
  width: 100%;
  box-sizing: border-box;
  z-index: 1000; // 提高z-index确保在最上层
  pointer-events: none;
  background: linear-gradient(rgba(246, 246, 246, 0), var(--bgColor2) 25%);
  flex-shrink: 0;
  padding: 20px 20px;
  display: flex;
  position: absolute; // 改为绝对定位，相对于主内容区域
  bottom: 0; // 在主内容区域底部
  left: 0;
  right: 0;
  overflow: hidden;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s ease;
  max-width: 900px; // 与对话内容保持相同的最大宽度
  margin: 0 auto; // 水平居中
  margin-bottom: 20px;
}

// 顶部文件预览样式
.top-file-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
  max-width: 800px;
  width: 100%;
  pointer-events: auto;
  padding: 8px 0;
}

.file-preview-item-top {
  display: flex;
  align-items: center;
  background-color: var(--bgColor1);
  border: 1px solid var(--borderColor);
  border-radius: 6px;
  padding: 8px 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  position: relative;
  min-width: 160px;
  max-width: 300px;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    border-color: var(--themeColor1);
  }
  
  .file-icon {
    color: var(--themeColor1);
    margin-right: 8px;
    flex-shrink: 0;
  }
  
  .file-info {
    flex: 1;
    overflow: hidden;
    margin-right: 8px;
    
    .file-name {
      font-size: 12px;
      color: var(--textColor1);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-bottom: 2px;
      font-weight: 500;
    }
    
    .file-size {
      font-size: 10px;
      color: var(--textColor3);
    }
  }
  
  .file-preview-close {
    background-color: rgba(0, 0, 0, 0.6);
    color: white;
    border-radius: 50%;
    width: 18px;
    height: 18px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: all 0.2s ease;
    flex-shrink: 0;
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.8);
      transform: scale(1.1);
    }
  }
}

.footer-bar {
  min-height: 60px;
  max-width: 800px;
  width: 100%;
  pointer-events: auto;
  background: var(--bgColor1);
  border-radius: 8px;
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
  display: flex;
  align-items: center;
  animation: footerBarAnimation 0.3s;
}

@keyframes footerBarAnimation {
  from {
    transform: translateY(150%);
  }
  to {
    transform: translateY(0);
  }
}

.slide-animation {
  animation: slideEase 0.5s ease-in-out forwards;
}

.questions {
  width: 100%;
  max-width: 900px;
  box-sizing: border-box;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

@media only screen and (max-width: 767px) {
  .questions {
    padding: 0;
    width: 100%;
  }
}

.questions > .item {
  border-radius: 8px;
  padding: 0 20px;
  width: 100%;
  max-width: 900px;
}

.flexShrink {
  flex-shrink: 0;
}

.question {
  display: flex;
  justify-content: right;
  align-items: flex-start;
  padding: 20px 8px;
}

.answer {
  padding: 20px 8px;
  display: flex;
  justify-content: left;
  align-items: flex-start;
}

.question > div > .text {
  max-width: 733px;
  min-width: 50px;
  padding: 5px 10px;
  border-radius: 5px;
  text-align: left;
  min-height: 28px;
  white-space: pre-wrap;
  margin-left: 46px;
  font-size: 16px;
  word-break: break-all;
  line-height: 28px;
  position: relative;
  background-color: var(--themeColor2);
  box-shadow: 0 5px 7px rgba(49, 79, 88, 0.15);
  color: var(--themeTextColor);
  margin-right: 10px;
  margin-top: 2px;
  display: flex;
  flex-direction: column;
}

.operation--model {
  margin-top: 5px;
  display: flex;
  align-items: center;
  margin-left: 10px;
}

.operation--model_user {
  margin-top: 5px;
  display: flex;
  align-items: center;
  justify-content: right;
  margin-right: 5px;
}

.op-btn {
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
  color: var(--textColor4);
  background-color: var(--bgColor1);
  margin-right: 5px;
  padding: 3px 10px;
  display: flex;
  align-items: center;
  border-radius: 3px;
}

.op-font {
  font-size: 9px;
  padding-left: 5px;
}

// 模型徽章样式
.model-badge {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, var(--themeColor1) 0%, var(--themeColor2) 100%);
  color: var(--themeTextColor);
  margin-right: 5px;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 9px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(129, 102, 231, 0.2);
  border: 1px solid rgba(129, 102, 231, 0.3);
  transition: all 0.2s ease;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(129, 102, 231, 0.3);
  }
  
  .el-icon {
    font-size: 10px;
    margin-right: 3px;
  }
  
  .model-text {
    font-size: 8px;
    font-weight: 600;
    letter-spacing: 0.5px;
    text-transform: uppercase;
  }
}

.explain {
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: explainAnimation 0.3s;
  color: var(--textColor1);
  width: 100%;
  max-width: 900px;
  text-align: center;
}

@keyframes explainAnimation {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

.suspend {
  animation: explainAnimation 0.3s;
  position: relative; // 改为相对定位
  margin: 20px auto; // 居中显示
  display: flex;
  align-items: center;
  box-shadow: 0 5px 7px var(--bgboxShadowColor1);
  background-color: var(--bgColor1);
  padding: 5px 20px;
  font-size: 13px;
  color: var(--textColor1);
  border-radius: 5px;
  z-index: 999;
  width: fit-content; // 根据内容调整宽度
}

.answer-data {
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
  margin-left: 10px;
  border-radius: 5px;
  margin-top: 2px;
  overflow-x: hidden;
  background-color: var(--bgColor1);
  padding: 10px 10px 10px 5px;
  min-width: 50px;
  margin-right: 46px;
}

.suspend div {
  padding-bottom: 1px;
  padding-left: 8px;
}

.logo {
  width: 100px;
  margin-bottom: 20px;
  animation: beating 0.7s infinite alternate;
}

.expositoryCase {
  font-size: 20px;
  margin-top: 15px;
  text-align: center;
}

.consume {
  display: flex;
  align-items: center;
  margin-top: 30px;
}

.consumeText {
  margin-left: 10px;
  font-size: 15px;
}

.beCareful {
  padding: 40px 6px 12px;
  color: var(--textColor2);
  font-size: 15px;
  line-height: 1.6;
}

// 左侧控制区域
.left-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  position: absolute;
  top: 0;
  left: 45px;
  z-index: 1;
}

// 清除聊天按钮
.clear {
  display: flex;
  align-items: center;
  font-size: 8px;
  border-radius: 5px;
  padding: 3px 10px;
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
  color: var(--textColor4);
  background-color: var(--bgColor1);
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 8px 12px rgb(0 0 0 / 10%);
  }
}

// RAG和MCP开关按钮容器
.feature-toggles {
  display: flex;
  gap: 8px;
  flex-wrap: wrap; // 允许换行
}

// 切换按钮样式
.toggle-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-size: 10px;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--borderColor);
  background-color: var(--bgColor1);
  color: var(--textColor4);
  box-shadow: 0 2px 4px rgb(0 0 0 / 6%);
  
  .toggle-text {
    font-size: 8px;
    font-weight: 500;
  }
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgb(0 0 0 / 12%);
  }
  
  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-color: transparent;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    
    &:hover {
      box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
    }
  }
}

// 管理按钮样式
.management-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-size: 10px;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--themeColor1);
  background-color: var(--bgColor1);
  color: var(--themeColor1);
  box-shadow: 0 2px 4px rgba(129, 102, 231, 0.1);
  
  .btn-text {
    font-size: 8px;
    font-weight: 500;
  }
  
  &:hover {
    transform: translateY(-1px);
    background: linear-gradient(135deg, var(--themeColor1) 0%, var(--themeColor2) 100%);
    color: var(--themeTextColor);
    border-color: transparent;
    box-shadow: 0 4px 12px rgba(129, 102, 231, 0.3);
  }
}


@media (max-width: 767px) {
  .left-controls {
    left: 30px;
    gap: 6px;
    
    .feature-toggles {
      gap: 4px;
      
      .toggle-button,
      .management-button {
        padding: 3px 6px;
        font-size: 9px;
        
        .toggle-text,
        .btn-text {
          font-size: 7px;
        }
      }
    }
    
    .clear {
      padding: 2px 8px;
      font-size: 7px;
    }
  }
  
  .clear2 {
    left: 80px;
  }
}

.cache-flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bgColor2);
}

.cache-img {
  width: 80px;
  height: 80px;
}

.cache-text {
  color: var(--textColor1);
  text-align: center;
  font-size: 15px;
  font-weight: 550;
  padding-top: 10px;
}

.cache-padding-top {
  padding-top: 15px;
}

.cache-btn {
  color: var(--themeTextColor);
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--themeColor1);
  padding: 10px 30px;
  border-radius: 5px;
}

.cache-btn-text {
  padding-left: 10px;
  font-size: 12px;
}

.cache-content {
  padding: 20px 10px 10px;
}

.cache-scrollbar {
  background-color: var(--bgboxShadowColor1);
  border-radius: 10px;
  color: var(--textColor3);
}

.cache-padding {
  padding: 10px;
}

.cache-flex-space-between {
  display: flex;
  justify-content: space-between;
  margin: 10px 0;
}

.cache-message {
  padding-bottom: 4px;
  border-bottom: 1px #6b6b6b solid;
}

.cache-message-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  color: var(--textColor1);
  width: 310px;
}

.cache-message-time {
  padding-top: 5px;
  font-size: 5px;
}

.cache-selected {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 40px;
}

.cache-selected-img {
  width: 20px;
  height: 20px;
}

.dot_0,
.dot_1,
.dot_2,
.dot_3 {
  background: rgb(166, 129, 236);
  width: 15px;
  height: 15px;
  border-color: #464646;
  border-radius: 50%;
}

.dot_0 {
  animation: jumpT 1.3s -0.64s linear infinite;
}

.dot_1 {
  animation: jumpT 1.3s -0.32s linear infinite;
}

.dot_2 {
  animation: jumpT 1.3s -0.16s linear infinite;
}

.dot_3 {
  animation: jumpT 1.3s linear infinite;
}

@keyframes jumpT {
  0%,
  80%,
  100% {
    transform: scale(0);
    background-color: #f9f9f9;
  }
  40% {
    transform: scale(1);
    background-color: rgb(186, 156, 241);
  }
}

.question-media {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  width: 100%;
}

.media-container {
  width: 100%;
}

.question-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  width: 100%;
}

.question-image-container {
  position: relative;
  border-radius: 6px;
  overflow: hidden;
  width: calc(50% - 4px);
  max-width: 200px;
  height: auto;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
  cursor: pointer;
  
  &:hover {
    transform: scale(1.02);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    
    .image-overlay {
      opacity: 1;
    }
  }
  
  .image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s ease;
    border-radius: 6px;
    pointer-events: none;
    
    .zoom-icon {
      color: white;
      font-size: 20px;
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
    }
  }
  
  @media (max-width: 600px) {
    width: 100%;
    max-width: none;
    
    .image-overlay {
      opacity: 0.8; // 移动端始终显示一定透明度的遮罩
      
      .zoom-icon {
        font-size: 18px;
      }
    }
  }
}

.question-image {
  width: 100%;
  height: 100%;
  max-height: 200px;
  background-color: rgba(0, 0, 0, 0.03);
  border-radius: 6px;
  
  :deep(.el-image__inner) {
    object-fit: contain;
    transition: all 0.2s ease;
    border-radius: 6px;
    
    &:hover {
      filter: brightness(0.9);
    }
  }
  
  :deep(.el-image__placeholder),
  :deep(.el-image__error) {
    background-color: var(--bgColor2);
    border-radius: 6px;
  }
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--textColor2);
  font-size: 12px;
  height: 100px;
  
  .el-icon {
    font-size: 24px;
    color: var(--textColor3);
  }
}

.question-file {
  margin-top: 12px;
  background-color: var(--bgColor1);
  border-radius: 6px;
  border: 1px solid var(--borderColor);
  overflow: hidden;
}

.file-info-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--borderColor);
  
  &:last-child {
    border-bottom: none;
  }
}

.file-icon {
  color: var(--themeColor1);
}

.file-name {
  font-weight: 500;
  color: var(--textColor1);
}

.file-size {
  color: var(--textColor3);
  font-size: 12px;
}

.file-link {
  color: var(--themeColor1);
  text-decoration: none;
  font-size: 12px;
  margin-left: 8px;
  
  &:hover {
    text-decoration: underline;
    color: var(--themeColor2);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dialogue-container {
    flex-direction: row; // 保持水平布局，不要改为column
    height: calc(100vh - 60px); /* 移动端也减去顶部导航栏高度 */
    overflow: hidden;
  }
  
  .sidebar {
    width: 300px; // 保持原始宽度
    height: calc(100vh - 60px); /* 移动端固定定位时也要减去导航栏高度 */
    transition: transform 0.3s ease, width 0.3s ease; // 添加transform过渡
    z-index: 1000; // 提高层级
    position: fixed; // 移动端使用固定定位
    left: 0;
    top: 60px; /* 从导航栏下方开始 */
    
    &.sidebar-collapsed {
      transform: translateX(-220px); // 通过transform隐藏大部分侧边栏
      width: 80px; // 保持小宽度
    }
  }
  
  .main-content {
    margin-left: 0; // 移动设备上不需要左边距
    width: 100vw; // 使用视口宽度
    flex: 1;
    
    .body {
      padding: 0 10px 160px; // 增加底部padding，确保移动端消息不被输入框遮挡
      width: 100%;
      max-width: none; // 移动端取消最大宽度限制
    }
  }

  .footer {
    padding: 10px 15px; // 减少底部padding
    margin-bottom: 20px; // 调整底部边距
    max-width: none; // 移动端取消最大宽度限制
    left: 10px;
    right: 10px;
    width: calc(100% - 20px); // 考虑左右padding
  }

  .footer-bar {
    min-height: 50px; // 减少底部栏的最小高度
    max-width: none; // 移动端取消最大宽度限制
    width: 100%;
  }

  .new-conversation-btn {
    width: 100%; // 按钮在移动端占满宽度
    height: 44px; // 确保按钮高度适中
    border-radius: 12px; // 保持圆角
    font-size: 15px; // 移动端稍大字体
    
    .el-icon {
      font-size: 18px; // 移动端图标稍大
    }
    
    .btn-text {
      margin-left: 10px; // 移动端稍大间距
      font-weight: 600;
    }
  }

  .conversation-item {
    padding: 10px 12px; // 调整对话项的内边距
    margin: 4px 8px; // 减少边距
    min-height: 45px; // 移动端减小最小高度
    
    .conversation-content {
      gap: 4px; // 减少间距
    }
    
    .conversation-footer {
      gap: 6px; // 减少间距
    }
    
    .conversation-title {
      font-size: 14px; // 移动端稍小字体
    }
    
    .conversation-time {
      font-size: 10px; // 移动端更小的时间字体
    }
    
    .role-tag {
      padding: 2px 6px; // 减少内边距
      font-size: 10px; // 更小字体
      
      .role-text {
        max-width: 50px; // 移动端减少最大宽度
      }
    }
    
    .conversation-actions {
      margin-left: 8px; // 减少左边距
      gap: 4px; // 减少按钮间距
      
      .action-btn {
        width: 24px; // 稍小按钮
        height: 24px;
        
        .el-icon {
          font-size: 12px; // 更小图标
        }
      }
    }
    
    // 移动端始终显示操作按钮
    .conversation-actions {
      opacity: 1;
      transform: translateX(0);
    }
  }

  // 调整顶部栏的样式
  .sidebar-header {
    padding: 10px; // 减少顶部栏的内边距
    font-size: 14px; // 调整字体大小
  }

  // 移动端文件预览样式调整
  .top-file-preview {
    padding: 5px 0;
    margin-bottom: 10px;
    
    .file-preview-item-top {
      min-width: 120px; // 减小最小宽度
      max-width: calc(50% - 4px);
      padding: 6px 8px;
      
      .file-info {
        .file-name {
          font-size: 11px;
        }
        
        .file-size {
          font-size: 9px;
        }
      }
      
      .file-preview-close {
        width: 16px;
        height: 16px;
      }
    }
  }

  // 移动端问题和回答样式调整
  .question > div > .text {
    margin-left: 20px; // 减少左边距
    margin-right: 5px; // 减少右边距
    max-width: calc(100vw - 100px); // 使用视口宽度计算
    font-size: 14px; // 减小字体
    line-height: 22px; // 调整行高
  }

  .answer-data {
    margin-left: 5px; // 减少左边距
    margin-right: 20px; // 减少右边距
    max-width: calc(100vw - 100px); // 使用视口宽度计算
  }

  // 优化滚动性能
  .conversation-list {
    -webkit-overflow-scrolling: touch; // iOS 平滑滚动
    overflow-scrolling: touch; // 其他设备平滑滚动
  }

  .body {
    -webkit-overflow-scrolling: touch;
    overflow-scrolling: touch;
  }

  // 防止ResizeObserver循环的样式
  * {
    box-sizing: border-box;
  }
  
  // 减少布局抖动
  .sidebar,
  .main-content,
  .conversation-list {
    will-change: auto; // 避免不必要的合成层
    transform: translateZ(0); // 启用硬件加速
    backface-visibility: hidden;
  }
  
  // 优化Element Plus组件的性能
  :deep(.el-scrollbar__view) {
    will-change: auto;
  }
  
  :deep(.el-dialog) {
    will-change: auto;
  }
  
  :deep(.el-table) {
    will-change: auto;
  }

  // 确保在移动端有足够的点击区域
  .conversation-item {
    min-height: 44px; // iOS推荐的最小点击区域
  }

  .uploadFileButton,
  .sendIcon {
    min-width: 44px;
    min-height: 44px;
  }

  // 移动端模型徽章样式优化
  .model-badge {
    padding: 2px 6px;
    margin-right: 3px;
    border-radius: 8px;
    
    .el-icon {
      font-size: 9px;
      margin-right: 2px;
    }
    
    .model-text {
      font-size: 7px;
      letter-spacing: 0.3px;
    }
  }
}

// Element Plus 图片预览组件样式优化
:deep(.el-image-viewer__wrapper) {
  background-color: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
}

:deep(.el-image-viewer__btn) {
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  transition: all 0.2s ease;
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.2);
    transform: scale(1.05);
  }
}

:deep(.el-image-viewer__close) {
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.2);
  }
}

:deep(.el-image-viewer__canvas) {
  img {
    border-radius: 8px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  }
}

:deep(.answer > .el-avatar, .question > .el-avatar) {
  background-color: var(--bgColor2);
}

:deep(.vuepress-markdown-body) {
  padding: 0 0 0 16px;
  color: var(--textColor1);
  background-color: var(--bgColor1);
}

:deep(.scrollbar__wrap) {
  background-color: var(--bgColor1);
}

:deep(.vuepress-markdown-body tr:nth-child(2n)) {
  background-color: var(--bgColor1);
}

:deep(.footer-bar > .el-input > .el-input__wrapper) {
  box-shadow: none;
  font-size: 16px;
  box-sizing: border-box;
  width: 100%;
  min-height: 60px;
  resize: none;
  -webkit-appearance: none;
  background: var(--bgColor1) 0 0;
  border: 0;
  flex: 1;
  margin: 0;
  padding: 16px 20px;
  line-height: 28px;
}

:deep(.footer-bar) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

:deep(.footer-bar > .el-input > .el-input-group__prepend) {
  box-shadow: none;
  font-size: 16px;
  box-sizing: border-box;
  min-height: 60px;
  resize: none;
  -webkit-appearance: none;
  background: 0 0;
  border: 0;
  margin: 0;
  padding: 0;
  line-height: 28px;
}

:deep(.footer-bar > .el-input > .el-input-group__prepend > .el-select) {
  margin: 0 !important;
}

:deep(
  .footer-bar
    > .el-input
    > .el-input-group__prepend
    > .el-select
    > .select-trigger
    > .el-input
    > .el-input__wrapper
) {
  box-shadow: none !important;
  font-size: 15px;
  height: 62px;
  padding: 0 20px;
  background-color: var(--bgColor1);
}

.mobile-menu-btn {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1000;
  background-color: var(--themeColor1);
  border-radius: 50%;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
  display: none; // 默认隐藏

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 7px 12px rgba(129, 102, 231, 0.3);
  }

  .el-icon {
    color: var(--themeTextColor);
  }

  @media (max-width: 768px) {
    display: flex; // 在移动端显示
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
  }
}

// ========================= RAG管理弹窗样式 =========================
// RAG管理对话框样式
:deep(.rag-management-dialog) {
  .el-dialog {
    background-color: var(--bgColor1);
    border: 1px solid var(--borderColor);
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  }
  
  .el-dialog__header {
    background-color: var(--bgColor1);
    border-bottom: 1px solid var(--borderColor);
    
    .el-dialog__title {
      color: var(--textColor1);
      font-weight: 600;
    }
    
    .el-dialog__headerbtn {
      .el-dialog__close {
        color: var(--textColor2);
        
        &:hover {
          color: var(--textColor1);
        }
      }
    }
  }
  
  .el-dialog__body {
    background-color: var(--bgColor1);
    color: var(--textColor1);
  }
}
.upload-section {
  margin-bottom: 30px;
  
  .knowledge-upload {
    :deep(.el-upload) {
      width: 100%;
    }
    
    :deep(.el-upload-dragger) {
      width: 100%;
      height: 180px;
      background-color: var(--bgColor2);
      border: 2px dashed var(--borderColor);
      border-radius: 12px;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: var(--themeColor1);
        background-color: rgba(129, 102, 231, 0.05);
      }
    }
    
    :deep(.el-icon--upload) {
      font-size: 40px;
      color: var(--themeColor1);
      margin-bottom: 16px;
    }
    
    :deep(.el-upload__text) {
      color: var(--textColor2);
      font-size: 14px;
      
      em {
        color: var(--themeColor1);
        font-style: normal;
        font-weight: 600;
      }
    }
    
    :deep(.el-upload__tip) {
      color: var(--textColor3);
      font-size: 12px;
      margin-top: 8px;
    }
  }
}

.knowledge-list {
  .list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      color: var(--textColor1);
      font-weight: 600;
    }
  }
  
  // 表格样式 - 支持黑夜模式
  :deep(.el-table) {
    background-color: var(--bgColor1);
    color: var(--textColor1);
    border: 1px solid var(--borderColor);
    border-radius: 8px;
    overflow: hidden;
    
    .el-table__header-wrapper th {
      background-color: var(--bgColor2) !important;
      color: var(--textColor1);
      font-weight: 600;
      border-bottom: 1px solid var(--borderColor);
    }
    
    .el-table__body-wrapper {
      background-color: var(--bgColor1);
    }
    
    .el-table__row {
      background-color: var(--bgColor1);
      color: var(--textColor1);
      transition: background-color 0.3s ease;
      
      &:hover > td {
        background-color: var(--bgColor2) !important;
      }
    }
    
    .el-table__cell {
      background-color: var(--bgColor1) !important;
      border-bottom: 1px solid var(--borderColor);
      color: var(--textColor1);
    }
    
    .el-table__empty-block {
      background-color: var(--bgColor1);
      
      .el-table__empty-text {
        color: var(--textColor3);
      }
    }
    
    // 表格内按钮样式
    .el-button {
      &.el-button--success {
        background-color: var(--el-color-success);
        border-color: var(--el-color-success);
        color: white;
        
        &:hover {
          background-color: var(--el-color-success-light-3);
          border-color: var(--el-color-success-light-3);
        }
      }
      
      &.el-button--warning {
        background-color: var(--el-color-warning);
        border-color: var(--el-color-warning);
        color: white;
        
        &:hover {
          background-color: var(--el-color-warning-light-3);
          border-color: var(--el-color-warning-light-3);
        }
      }
      
      &.el-button--danger {
        background-color: var(--el-color-danger);
        border-color: var(--el-color-danger);
        color: white;
        
        &:hover {
          background-color: var(--el-color-danger-light-3);
          border-color: var(--el-color-danger-light-3);
        }
      }
    }
    
    // 表格内标签样式
    .el-tag {
      &.el-tag--success {
        background-color: rgba(103, 194, 58, 0.1);
        border-color: var(--el-color-success-light-5);
        color: var(--el-color-success);
      }
      
      &.el-tag--info {
        background-color: rgba(144, 147, 153, 0.1);
        border-color: var(--el-color-info-light-5);
        color: var(--el-color-info);
      }
    }
    
    // 表格加载状态样式
    .el-loading-mask {
      background-color: rgba(var(--bgColor1-rgb), 0.8);
    }
    
    // 表格边框样式
    &.el-table--border {
      border-color: var(--borderColor);
      
      &::after {
        background-color: var(--borderColor);
      }
      
      &::before {
        background-color: var(--borderColor);
      }
    }
  }
  
  .file-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .file-icon {
      color: var(--themeColor1);
      font-size: 16px;
    }
    
    .file-name {
      color: var(--textColor1);
      font-weight: 500;
    }
  }
}

// ========================= MCP管理弹窗样式 =========================
// MCP管理对话框样式
:deep(.mcp-management-dialog) {
  .el-dialog {
    background-color: var(--bgColor1);
    border: 1px solid var(--borderColor);
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  }
  
  .el-dialog__header {
    background-color: var(--bgColor1);
    border-bottom: 1px solid var(--borderColor);
    
    .el-dialog__title {
      color: var(--textColor1);
      font-weight: 600;
    }
    
    .el-dialog__headerbtn {
      .el-dialog__close {
        color: var(--textColor2);
        
        &:hover {
          color: var(--textColor1);
        }
      }
    }
  }
  
  .el-dialog__body {
    background-color: var(--bgColor1);
    color: var(--textColor1);
  }
}
.mcp-management-container {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.available-services-section {
  margin-bottom: 40px;
}

.section-header {
  margin-bottom: 20px;
  
  h3 {
    margin: 0 0 4px 0;
    font-size: 18px;
    color: var(--textColor1);
    font-weight: 600;
  }

  p{
    margin: 0;
    font-size: 14px;
    color: var(--textColor3);
    line-height: 1.4;
  }
  
  .section-subtitle {
    font-size: 14px;
    color: var(--textColor3);
    line-height: 1.4;
  }
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.service-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background-color: var(--bgColor1);
  border: 2px solid var(--borderColor);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  
  &:hover:not(.disabled) {
    border-color: var(--themeColor1);
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(129, 102, 231, 0.15);
    
    .service-action .el-button {
      transform: scale(1.1);
    }
  }
  
  &.selected {
    border-color: var(--themeColor1);
    background-color: rgba(129, 102, 231, 0.05);
    box-shadow: 0 4px 12px rgba(129, 102, 231, 0.2);
    
    &:hover {
      transform: translateY(-2px);
      border-color: var(--themeColor2);
      box-shadow: 0 8px 20px rgba(129, 102, 231, 0.25);
    }
  }
}

.service-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--themeColor1) 0%, var(--themeColor2) 100%);
  border-radius: 12px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(129, 102, 231, 0.3);
  overflow: hidden;
  
  &.small {
    width: 36px;
    height: 36px;
    border-radius: 8px;
  }
  
  .service-main-icon {
    width: 35px;
    height: 35px;
    object-fit: contain;
    border-radius: 7px;
    background-color: transparent;
    transition: all 0.2s ease;
    
    &.small {
      width: 25px;
      height: 25px;
    }
    
    &:hover {
      transform: scale(1.05);
    }
  }
}

.service-info {
  flex: 1;
  min-width: 0;
}

.service-title {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--textColor1);
  line-height: 1.2;
}

.service-description {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: var(--textColor2);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  cursor: help;
  transition: all 0.2s ease;
  
  &:hover {
    color: var(--themeColor1);
  }
}

.service-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.service-tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: rgba(129, 102, 231, 0.1);
  border-color: rgba(129, 102, 231, 0.2);
  color: var(--themeColor1);
}

.service-action {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  
  .el-button {
    transition: transform 0.2s ease;
    
    &:hover {
      transform: scale(1.1);
    }
  }
}

.selected-services-section {
  border-top: 1px solid var(--borderColor);
  padding-top: 30px;
}

.selected-services-list {
  min-height: 200px;
}

.selected-service-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: var(--bgColor1);
  border: 1px solid var(--borderColor);
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
  position: relative;
  
  &:hover {
    border-color: var(--themeColor1);
    box-shadow: 0 2px 8px rgba(129, 102, 231, 0.1);
  }
  
  &:last-child {
    margin-bottom: 0;
  }
}

.service-status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  
  &.connected {
    background-color: #67c23a;
    box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.3);
  }
  
  &.connecting {
    background-color: #e6a23c;
    box-shadow: 0 0 0 2px rgba(230, 162, 60, 0.3);
    animation: pulse 2s infinite;
  }
  
  &.disconnected {
    background-color: #f56c6c;
    box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.3);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.service-details {
  flex: 1;
  min-width: 0;
}

.service-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--textColor1);
  margin-bottom: 4px;
}

.service-url {
  font-size: 12px;
  color: var(--textColor2);
  font-family: monospace;
  margin-bottom: 6px;
  word-break: break-all;
}

.service-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.tools-count {
  font-size: 12px;
  color: var(--textColor3);
}

.add-time {
  font-size: 11px;
  color: var(--textColor3);
}

.created-time {
  font-size: 11px;
  color: var(--textColor3);
}

.service-description-small {
  font-size: 12px;
  color: var(--textColor2);
  line-height: 1.3;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  cursor: help;
  transition: all 0.2s ease;
  
  &:hover {
    color: var(--themeColor1);
  }
}

.service-method {
  font-size: 11px;
  color: var(--textColor2);
  font-family: monospace;
  margin-bottom: 6px;
  padding: 2px 6px;
  background-color: var(--bgColor2);
  border-radius: 4px;
  display: inline-block;
}

.service-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--textColor3);
}

.empty-icon {
  color: var(--textColor4);
  margin-bottom: 16px;
}

.empty-text {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: var(--textColor2);
  font-weight: 500;
}

.empty-hint {
  margin: 0;
  font-size: 14px;
  color: var(--textColor3);
}

// Tooltip样式优化
:deep(.service-description-tooltip) {
  max-width: 400px;
  font-size: 13px;
  line-height: 1.5;
  padding: 12px 16px;
  background-color: var(--bgColor1);
  border: 1px solid var(--borderColor);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  color: var(--textColor1);
  
  .el-popper__arrow::before {
    background-color: var(--bgColor1);
    border: 1px solid var(--borderColor);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .service-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .service-card {
    padding: 12px;
    gap: 10px;
  }
  
  .service-icon-wrapper {
    width: 40px;
    height: 40px;
    
    .service-main-icon {
      font-size: 20px;
    }
  }
  
  .service-title {
    font-size: 14px;
  }
  
  .service-description {
    font-size: 12px;
  }
  
  .selected-service-item {
    padding: 12px;
    gap: 10px;
  }
  
  .service-actions {
    flex-direction: column;
    gap: 6px;
    
    .el-button {
      font-size: 12px;
      padding: 4px 8px;
    }
  }
  
  .service-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
  
  // 移动端tooltip样式调整
  :deep(.service-description-tooltip) {
    max-width: 280px;
    font-size: 12px;
    padding: 10px 12px;
  }
}
</style>