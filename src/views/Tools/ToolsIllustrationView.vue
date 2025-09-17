<template>
    <div class="container">
      <h1>用例图制作工具</h1>
      
      <div class="tabs">
        <div 
          class="tab" 
          :class="{ active: activeTab === 'visual' }" 
          @click="activeTab = 'visual'"
        >
          可视化配置
        </div>
        <div 
          class="tab" 
          :class="{ active: activeTab === 'json' }" 
          @click="activeTab = 'json'"
        >
          JSON配置
        </div>
      </div>
      
      <div class="tab-content" :class="{ active: activeTab === 'visual' }">
        <div class="visual-config">
          <div class="config-panel">
            <div class="config-section">
              <h3>角色配置</h3>
              <div class="form-group">
                <label for="actorName">角色名称:</label>
                <div class="input-group">
                  <el-input 
                    v-model="newActorName" 
                    placeholder="输入角色名称"
                    @keyup.enter="addActor"
                  >
                    <template #append>
                      <el-button @click="addActor">添加</el-button>
                    </template>
                  </el-input>
                </div>
              </div>
              
              <div class="actors-list">
                <div v-for="(actor, index) in config.actors" :key="index" class="actor-item">
                  <el-input v-model="config.actors[index]" readonly />
                  <el-button type="danger" @click="removeActor(index)" size="small">删除</el-button>
                </div>
              </div>
            </div>
            
            <div class="config-section">
              <h3>用例配置</h3>
              <div class="quick-add">
                <el-button
                  v-for="(usecase, index) in quickUsecases" 
                  :key="'quick-'+index"
                  class="quick-add-btn"
                  @click="addUsecase(usecase)"
                  size="small"
                >
                  {{ usecase }}
                </el-button>
              </div>
              
              <div class="form-group">
                <label for="usecaseName">用例名称:</label>
                <div class="input-group">
                  <el-input 
                    v-model="newUsecaseName" 
                    placeholder="输入用例名称"
                    @keyup.enter="addUsecase(newUsecaseName)"
                  >
                    <template #append>
                      <el-button @click="addUsecase(newUsecaseName)">添加</el-button>
                    </template>
                  </el-input>
                </div>
              </div>
              
              <div class="usecases-list">
                <div v-for="(usecase, index) in config.usecases" :key="index" class="usecase-item">
                  <el-input v-model="config.usecases[index]" />
                  <el-button type="danger" @click="removeUsecase(index)" size="small">删除</el-button>
                </div>
              </div>
            </div>
          </div>
          
          <div class="action-buttons">
            <el-button type="primary" @click="applyVisualConfig">应用配置</el-button>
          </div>
        </div>
      </div>
      
      <div class="tab-content" :class="{ active: activeTab === 'json' }">
        <div class="json-config">
          <div class="form-group">
            <label for="jsonConfig">JSON配置:</label>
            <el-input
              type="textarea"
              v-model="jsonConfig"
              :rows="15"
              resize="none"
            />
          </div>
          <el-button type="primary" @click="applyJsonConfig">应用配置</el-button>
        </div>
      </div>
      
      <h2>用例图预览</h2>
      <div class="preview-actions">
        <el-button type="primary" @click="exportSVG">导出SVG</el-button>
        <el-button type="primary" @click="exportPNG">导出PNG</el-button>
      </div>
      <div id="preview" ref="previewContainer">
        <div v-if="error" class="error">{{ error }}</div>
        <svg v-else ref="svgElement" :viewBox="`0 0 ${svgWidth} ${svgHeight}`" width="100%" height="100%">
          <!-- 角色 -->
          <g v-for="(actor, index) in config.actors" :key="'actor-'+index" class="actor">
            <!-- 小人图标 -->
            <circle 
              :cx="actorPosition.x" 
              :cy="actorPosition.y + index * 150" 
              r="20" 
              fill="white" 
              stroke="black" 
              stroke-width="2"
            />
            <line 
              :x1="actorPosition.x" 
              :y1="actorPosition.y + 20 + index * 150" 
              :x2="actorPosition.x" 
              :y2="actorPosition.y + 80 + index * 150" 
              stroke="black" 
              stroke-width="2"
            />
            <line 
              :x1="actorPosition.x" 
              :y1="actorPosition.y + 40 + index * 150" 
              :x2="actorPosition.x - 30" 
              :y2="actorPosition.y + 60 + index * 150" 
              stroke="black" 
              stroke-width="2"
            />
            <line 
              :x1="actorPosition.x" 
              :y1="actorPosition.y + 40 + index * 150" 
              :x2="actorPosition.x + 30" 
              :y2="actorPosition.y + 60 + index * 150" 
              stroke="black" 
              stroke-width="2"
            />
            <line 
              :x1="actorPosition.x" 
              :y1="actorPosition.y + 80 + index * 150" 
              :x2="actorPosition.x - 20" 
              :y2="actorPosition.y + 120 + index * 150" 
              stroke="black" 
              stroke-width="2"
            />
            <line 
              :x1="actorPosition.x" 
              :y1="actorPosition.y + 80 + index * 150" 
              :x2="actorPosition.x + 20" 
              :y2="actorPosition.y + 120 + index * 150" 
              stroke="black" 
              stroke-width="2"
            />
            <text 
              :x="actorPosition.x" 
              :y="actorPosition.y + 150 + index * 150" 
              text-anchor="middle"
            >
              {{ actor }}
            </text>
          </g>
          
          <!-- 用例 -->
          <g v-for="(usecase, index) in config.usecases" :key="'usecase-'+index">
            <ellipse 
              class="usecase" 
              :cx="usecasePosition.x" 
              :cy="usecasePosition.y + index * 70" 
              rx="100" 
              ry="30"
            />
            <text 
              class="usecase-text" 
              :x="usecasePosition.x" 
              :y="usecasePosition.y + index * 70"
            >
              {{ usecase }}
            </text>
          </g>
          
          <!-- 连接线 -->
          <line 
            v-for="(connection, index) in connections" 
            :key="'conn-'+index"
            class="connection"
            :x1="connection.x1" 
            :y1="connection.y1" 
            :x2="connection.x2" 
            :y2="connection.y2"
          />
        </svg>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
  
  // 标签页状态
  const activeTab = ref('visual')
  
  // 配置数据
  const config = reactive({
    actors: ['用户'],
    usecases: ['个人中心', '我的订单', '购物车', 'AI问答', '登录/注册']
  })
  
  // 快速用例列表
  const quickUsecases = [
    '登录/注册', '个人中心', '我的订单', '购物车', '退出',
    '搜索', '支付', '评价'
  ]
  
  // 输入框数据
  const newActorName = ref('')
  const newUsecaseName = ref('')
  
  // JSON配置
  const jsonConfig = ref(JSON.stringify({
    actors: ['用户'],
    usecases: ['个人中心', '我的订单', '购物车', 'AI问答', '登录/注册']
  }, null, 2))
  
  // 错误信息
  const error = ref(null)
  
  // SVG尺寸
  const svgWidth = ref(800)
  const svgHeight = ref(600)
  
  // 位置配置
  const actorPosition = reactive({ x: 100, y: 150 })
  const usecasePosition = reactive({ x: 500, y: 150 })
  
  // DOM引用
  const previewContainer = ref(null)
  const svgElement = ref(null)
  
  // 计算连接线
  const connections = computed(() => {
    const conns = []
    config.actors.forEach((actor, actorIndex) => {
      config.usecases.forEach((usecase, usecaseIndex) => {
        conns.push({
          x1: actorPosition.x + 30,
          y1: actorPosition.y + 40 + actorIndex * 150,
          x2: usecasePosition.x - 100,
          y2: usecasePosition.y + usecaseIndex * 70
        })
      })
    })
    return conns
  })
  
  // 添加角色
  function addActor() {
    if (newActorName.value.trim()) {
      config.actors.push(newActorName.value.trim())
      newActorName.value = ''
      updateJsonFromVisual()
    }
  }
  
  // 删除角色
  function removeActor(index) {
    config.actors.splice(index, 1)
    updateJsonFromVisual()
  }
  
  // 添加用例
  function addUsecase(usecaseName) {
    if (typeof usecaseName === 'string' && usecaseName.trim()) {
      config.usecases.push(usecaseName.trim())
      if (usecaseName === newUsecaseName.value) {
        newUsecaseName.value = ''
      }
      updateJsonFromVisual()
    }
  }
  
  // 删除用例
  function removeUsecase(index) {
    config.usecases.splice(index, 1)
    updateJsonFromVisual()
  }
  
  // 从可视化配置更新JSON
  function updateJsonFromVisual() {
    jsonConfig.value = JSON.stringify({
      actors: [...config.actors],
      usecases: [...config.usecases]
    }, null, 2)
  }
  
  // 应用可视化配置
  function applyVisualConfig() {
    updateJsonFromVisual()
    generateUseCaseDiagram()
  }
  
  // 应用JSON配置
  function applyJsonConfig() {
    try {
      const newConfig = JSON.parse(jsonConfig.value)
      config.actors = Array.isArray(newConfig.actors) ? [...newConfig.actors] : []
      config.usecases = Array.isArray(newConfig.usecases) ? [...newConfig.usecases] : []
      error.value = null
      generateUseCaseDiagram()
    } catch (err) {
      error.value = `JSON解析错误: ${err.message}`
    }
  }
  
  // 生成用例图
  function generateUseCaseDiagram() {
    // 在Vue中，由于响应式系统，SVG会自动更新
    // 这里只需要确保SVG容器存在
    if (!svgElement.value) return
    
    // 调整SVG尺寸以适应内容
    const actorsHeight = config.actors.length * 150
    const usecasesHeight = config.usecases.length * 70
    
    svgHeight.value = Math.max(600, actorsHeight, usecasesHeight + 150)
  }
  
  // 导出SVG
  function exportSVG() {
    if (!svgElement.value) return
    
    const svgData = new XMLSerializer().serializeToString(svgElement.value)
    const blob = new Blob([svgData], {type: 'image/svg+xml'})
    const url = URL.createObjectURL(blob)
    
    const a = document.createElement('a')
    a.href = url
    a.download = '用例图.svg'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }
  
  // 导出PNG
  function exportPNG() {
    if (!svgElement.value) return
    
    const svgData = new XMLSerializer().serializeToString(svgElement.value)
    const canvas = document.createElement('canvas')
    canvas.width = svgWidth.value
    canvas.height = svgHeight.value
    const ctx = canvas.getContext('2d')
    
    const img = new Image()
    img.onload = function() {
      ctx.drawImage(img, 0, 0)
      const pngUrl = canvas.toDataURL('image/png')
      
      const a = document.createElement('a')
      a.href = pngUrl
      a.download = '用例图.png'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
    }
    
    img.src = 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svgData)))
  }
  
  // 初始化
  onMounted(() => {
    generateUseCaseDiagram()
  })
  </script>
  
  <style lang="scss" scoped>
  .container {
    font-family: "PingFang SC", "Helvetica Neue", Helvetica, "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    background-color: var(--bgColor1);
    color: var(--textColor1);
    height: 100%;
    overflow-y: auto;
  }
  
  h1, h2, h3, h4 {
    color: var(--textColor1);
    margin-bottom: 20px;
  }
  
  h1 {
    text-align: center;
    letter-spacing: 1px;
    font-weight: 600;
  }
  
  .tabs {
    display: flex;
    border-bottom: 2px solid var(--borderColor);
    margin-bottom: 20px;
    position: sticky;
    top: 0;
    background-color: var(--bgColor1);
    z-index: 10;
    padding: 5px 0;
  }
  
  .tab {
    padding: 10px 20px;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: all 0.3s cubic-bezier(0.65, 0.05, 0.36, 1);
    color: var(--textColor2);
    margin-right: 10px;
  
    &:hover {
      color: var(--themeColor1);
    }
  
    &.active {
      border-bottom: 2px solid var(--themeColor1);
      color: var(--themeColor1);
    }
  }
  
  .tab-content {
    display: none;
    margin-bottom: 30px;
  
    &.active {
      display: block;
      animation: fadeIn 0.3s ease;
    }
  }
  
  @keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
  }
  
  .visual-config {
    background-color: var(--bgColor2);
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px var(--bgboxShadowColor1);
    margin-bottom: 20px;
  }
  
  .config-panel {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
  }
  
  .config-section {
    background-color: var(--bgColor1);
    padding: 20px;
    border-radius: 8px;
    border: 1px solid var(--borderColor);
  }
  
  .form-group {
    margin-bottom: 20px;
  
    label {
      display: block;
      margin-bottom: 8px;
      font-weight: 500;
      color: var(--textColor2);
    }
  }
  
  .input-group {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
  }
  
  .actors-list,
  .usecases-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .actor-item,
  .usecase-item {
    display: flex;
    gap: 10px;
    align-items: center;
    background-color: var(--bgColor2);
    padding: 10px;
    border-radius: 6px;
    border: 1px solid var(--borderColor);
  
    .el-input {
      flex: 1;
    }
  }
  
  .quick-add {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 20px;
  
    .el-button {
      margin: 0;
    }
  }
  
  .action-buttons {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
  
  .preview-actions {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }
  
  #preview {
    margin-top: 20px;
    border: 2px solid var(--borderColor);
    padding: 20px;
    min-height: 400px;
    background-color: var(--bgColor2);
    border-radius: 8px;
    box-shadow: 0 3px 8px var(--bgboxShadowColor1);
  }
  
  .error {
    color: #ff4d4f;
    padding: 15px;
    background-color: var(--bgColor2);
    border: 1px solid #ffcccc;
    border-radius: 4px;
    margin-bottom: 15px;
  }
  
  // Element Plus 组件样式覆盖
  :deep(.el-input__wrapper) {
    background-color: var(--bgColor2);
    box-shadow: 0 0 0 1px var(--borderColor);
  }
  
  :deep(.el-input__inner) {
    color: var(--textColor1);
    background-color: var(--bgColor2);
  }
  
  :deep(.el-textarea__inner) {
    background-color: var(--bgColor2);
    color: var(--textColor1);
    border-color: var(--borderColor);
    font-family: monospace;
  }
  
  :deep(.el-button) {
    &.el-button--primary {
      background-color: var(--themeColor1);
      border-color: var(--themeColor1);
      color: var(--themeTextColor);
      
      &:hover {
        opacity: 0.9;
      }
    }
  }
  
  @media only screen and (max-width: 767px) {
    .container {
      padding: 15px;
    }
  
    .config-panel {
      grid-template-columns: 1fr;
    }
  
    .preview-actions {
      flex-direction: column;
      
      .el-button {
        width: 100%;
      }
    }
  }
  
  // SVG样式
  svg {
    width: 100%;
    height: 100%;
  }

  .actor {
    text {
      fill: var(--textColor1);
      font-size: 14px;
    }

    circle {
      fill: var(--bgColor2);
      stroke: var(--textColor1);
    }

    line {
      stroke: var(--textColor1);
    }
  }

  .usecase {
    fill: var(--bgColor2);
    stroke: var(--textColor1);
    stroke-width: 2;
  }

  .usecase-text {
    fill: var(--textColor1);
    text-anchor: middle;
    dominant-baseline: middle;
    font-size: 14px;
  }

  .connection {
    stroke: var(--textColor1);
    stroke-width: 2;
  }
  </style>