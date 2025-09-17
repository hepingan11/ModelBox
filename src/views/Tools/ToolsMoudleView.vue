<template>
  <div class="container">
    <h1>免费生成毕设功能模块图</h1>
    
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
        <div class="template-selection">
          <h3>选择模板</h3>
          <el-select v-model="selectedTemplate" @change="loadTemplate" class="template-select">
            <el-option
              v-for="(name, key) in templateNames"
              :key="key"
              :label="name"
              :value="key"
            />
          </el-select>
        </div>
        
        <div class="system-config">
          <h3>系统配置</h3>
          <div class="form-group">
            <label for="systemName">系统名称:</label>
            <el-input v-model="config.systemName" placeholder="请输入系统名称" />
          </div>
          
          <div class="modules-editor">
            <h3>模块配置</h3>
            <div id="modulesContainer">
              <div v-for="(module, moduleIndex) in config.modules" :key="moduleIndex" class="module-item">
                <div class="form-group">
                  <label>模块名称:</label>
                  <el-input v-model="module.name" placeholder="请输入模块名称" />
                </div>
                <div class="sub-modules-list">
                  <h4>子功能:</h4>
                  <div class="sub-modules-container">
                    <div v-for="(subModule, subIndex) in module.subModules" :key="subIndex" class="sub-module-item">
                      <el-input v-model="subModule.name" placeholder="请输入功能名称" />
                      <el-button type="danger" @click="removeSubModule(moduleIndex, subIndex)" size="small">删除</el-button>
                    </div>
                  </div>
                  <div class="module-actions">
                    <el-button type="primary" @click="addSubModule(moduleIndex)" size="small">添加子功能</el-button>
                    <el-button type="danger" @click="removeModule(moduleIndex)" size="small">删除模块</el-button>
                  </div>
                </div>
              </div>
            </div>
            <el-button type="primary" @click="addModule" size="small">添加模块</el-button>
          </div>
        </div>
        
        <div class="form-group line-controls">
          <h3>图表样式控制</h3>
          <div class="control-item">
            <label for="systemToModuleLineLength">系统到模块线长</label>
            <el-input-number 
              v-model="styleConfig.systemToModuleLineLength"
              :min="10"
              :max="200"
              size="small"
            />
          </div>
          <div class="control-item">
            <label for="moduleToSubModuleLineLength">模块到子模块线长</label>
            <el-input-number 
              v-model="styleConfig.moduleToSubModuleLineLength"
              :min="10"
              :max="200"
              size="small"
            />
          </div>
          <div class="control-item">
            <label for="subModuleHeight">子模块高度</label>
            <el-input-number 
              v-model="styleConfig.subModuleHeight"
              :min="50"
              :max="300"
              size="small"
            />
          </div>
          <div class="control-item">
            <label for="subModuleWidth">子模块宽度</label>
            <el-input-number 
              v-model="styleConfig.subModuleWidth"
              :min="40"
              :max="150"
              size="small"
            />
          </div>
        </div>
        
        <el-button type="primary" @click="applyVisualConfig" class="export-btn">应用配置</el-button>
      </div>
    </div>
    
    <div class="tab-content" :class="{ active: activeTab === 'json' }">
      <el-button type="primary" @click="exportToPNG" class="export-btn">导出为PNG</el-button>
      <el-input
        type="textarea"
        v-model="jsonConfig"
        :rows="12"
        placeholder="请输入JSON配置..."
        @input="generateModuleDiagram"
      />
    </div>
    
    <h2>模块图预览</h2>
    <div class="preview-container" id="preview" ref="previewContainer">
      <el-alert
        v-if="error"
        :title="error"
        type="error"
        :closable="false"
        show-icon
      />
      <div v-else class="module-diagram">
        <div class="system-name">{{ config.systemName }}</div>
        
        <div 
          class="modules-container" 
          :style="{ marginTop: `${styleConfig.systemToModuleLineLength}px` }"
        >
          <div v-for="(module, moduleIndex) in config.modules" :key="moduleIndex" class="module-column">
            <div class="module-name">{{ module.name }}</div>
            
            <div 
              v-if="module.subModules && module.subModules.length > 0" 
              class="sub-modules-container"
              :style="{ marginTop: `${styleConfig.moduleToSubModuleLineLength}px` }"
            >
              <div v-for="(subModule, subIndex) in module.subModules" :key="subIndex" class="sub-module-column">
                <div 
                  class="sub-module" 
                  :style="{
                    height: `${styleConfig.subModuleHeight}px`,
                    width: `${styleConfig.subModuleWidth}px`
                  }"
                >
                  {{ subModule.name }}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <svg class="svg-container" ref="svgContainer">
          <!-- 连接线将通过JavaScript动态绘制 -->
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import html2canvas from 'html2canvas'

// 标签页状态
const activeTab = ref('visual')

// 配置数据
const config = reactive({
  systemName: '健身房管理系统',
  modules: [
    {
      name: '会员模块',
      subModules: [
        { name: '会员卡管理' },
        { name: '课程预约' },
        { name: '体测记录' },
        { name: '消费记录' }
      ]
    },
    {
      name: '教练模块',
      subModules: [
        { name: '课程安排' },
        { name: '会员管理' },
        { name: '训练计划' },
        { name: '业绩统计' }
      ]
    },
    {
      name: '管理员模块',
      subModules: [
        { name: '设备管理' },
        { name: '人员管理' },
        { name: '财务管理' },
        { name: '会籍管理' }
      ]
    }
  ]
})

// 样式配置
const styleConfig = reactive({
  systemToModuleLineLength: 50,
  moduleToSubModuleLineLength: 50,
  subModuleHeight: 120,
  subModuleWidth: 80
})

// JSON配置
const jsonConfig = ref(JSON.stringify(config, null, 2))

// 错误信息
const error = ref(null)

// 模板数据
const templates = {
  fitness: {
    systemName: "健身房管理系统",
    modules: [
      {
        name: "会员模块",
        subModules: [
          { name: "会员卡管理" },
          { name: "课程预约" },
          { name: "体测记录" },
          { name: "消费记录" }
        ]
      },
      {
        name: "教练模块",
        subModules: [
          { name: "课程安排" },
          { name: "会员管理" },
          { name: "训练计划" },
          { name: "业绩统计" }
        ]
      },
      {
        name: "管理员模块",
        subModules: [
          { name: "设备管理" },
          { name: "人员管理" },
          { name: "财务管理" },
          { name: "会籍管理" }
        ]
      }
    ]
  },
  library: {
    systemName: "图书馆管理系统",
    modules: [
      {
        name: "图书管理",
        subModules: [
          { name: "图书入库" },
          { name: "图书借阅" },
          { name: "图书归还" },
          { name: "图书检索" }
        ]
      },
      {
        name: "读者管理",
        subModules: [
          { name: "读者注册" },
          { name: "读者信息" },
          { name: "借阅历史" },
          { name: "违规记录" }
        ]
      },
      {
        name: "系统管理",
        subModules: [
          { name: "用户管理" },
          { name: "权限设置" },
          { name: "系统设置" },
          { name: "数据备份" }
        ]
      }
    ]
  },
  hospital: {
    systemName: "医院管理系统",
    modules: [
      {
        name: "门诊管理",
        subModules: [
          { name: "挂号管理" },
          { name: "就诊管理" },
          { name: "处方管理" },
          { name: "收费管理" }
        ]
      },
      {
        name: "住院管理",
        subModules: [
          { name: "入院登记" },
          { name: "床位管理" },
          { name: "医嘱管理" },
          { name: "出院结算" }
        ]
      },
      {
        name: "医技管理",
        subModules: [
          { name: "检验管理" },
          { name: "影像管理" },
          { name: "手术管理" },
          { name: "药房管理" }
        ]
      }
    ]
  },
  ecommerce: {
    systemName: "电子商务平台",
    modules: [
      {
        name: "用户模块",
        subModules: [
          { name: "用户注册" },
          { name: "用户登录" },
          { name: "个人中心" },
          { name: "收货地址" }
        ]
      },
      {
        name: "商品模块",
        subModules: [
          { name: "商品分类" },
          { name: "商品搜索" },
          { name: "商品详情" },
          { name: "商品评价" }
        ]
      },
      {
        name: "订单模块",
        subModules: [
          { name: "购物车" },
          { name: "订单生成" },
          { name: "支付管理" },
          { name: "物流跟踪" }
        ]
      }
    ]
  },
  school: {
    systemName: "学校管理系统",
    modules: [
      {
        name: "学生管理",
        subModules: [
          { name: "学籍管理" },
          { name: "成绩管理" },
          { name: "考勤管理" },
          { name: "奖惩管理" }
        ]
      },
      {
        name: "教师管理",
        subModules: [
          { name: "教师信息" },
          { name: "课程安排" },
          { name: "教学评价" },
          { name: "工资管理" }
        ]
      },
      {
        name: "教务管理",
        subModules: [
          { name: "课程管理" },
          { name: "教室管理" },
          { name: "考试管理" },
          { name: "教材管理" }
        ]
      }
    ]
  },
  custom: {
    systemName: "自定义系统",
    modules: [
      {
        name: "模块一",
        subModules: [
          { name: "功能1" },
          { name: "功能2" }
        ]
      },
      {
        name: "模块二",
        subModules: [
          { name: "功能1" },
          { name: "功能2" }
        ]
      }
    ]
  },
  admin: {
    systemName: "后台管理系统",
    modules: [
      {
        name: "商品管理",
        subModules: [
          { name: "商品列表" },
          { name: "商品分类" },
          { name: "商品品牌" },
          { name: "商品规格" }
        ]
      },
      {
        name: "订单管理",
        subModules: [
          { name: "订单列表" },
          { name: "退货申请处理" },
          { name: "退货原因设置" }
        ]
      },
      {
        name: "营销管理",
        subModules: [
          { name: "商品轮播图" }
        ]
      },
      {
        name: "权限管理",
        subModules: [
          { name: "管理员列表" },
          { name: "会员列表" },
          { name: "角色列表" }
        ]
      }
    ]
  }
}

// 模板名称映射
const templateNames = {
  fitness: '健身房管理系统',
  library: '图书馆管理系统',
  hospital: '医院管理系统',
  ecommerce: '电子商务平台',
  school: '学校管理系统',
  custom: '自定义系统',
  admin: '后台管理系统'
}

// 选中的模板
const selectedTemplate = ref('fitness')

// DOM引用
const previewContainer = ref(null)
const svgContainer = ref(null)

// 添加模块
function addModule() {
  config.modules.push({
    name: '新模块',
    subModules: [{ name: '新功能' }]
  })
}

// 删除模块
function removeModule(index) {
  config.modules.splice(index, 1)
}

// 添加子模块
function addSubModule(moduleIndex) {
  config.modules[moduleIndex].subModules.push({ name: '新功能' })
}

// 删除子模块
function removeSubModule(moduleIndex, subIndex) {
  config.modules[moduleIndex].subModules.splice(subIndex, 1)
}

// 加载模板
function loadTemplate() {
  const template = templates[selectedTemplate.value]
  if (template) {
    config.systemName = template.systemName
    config.modules = JSON.parse(JSON.stringify(template.modules))
    jsonConfig.value = JSON.stringify(config, null, 2)
  }
}

// 应用可视化配置
function applyVisualConfig() {
  jsonConfig.value = JSON.stringify(config, null, 2)
  generateModuleDiagram()
}

// 生成模块图
function generateModuleDiagram() {
  try {
    const newConfig = JSON.parse(jsonConfig.value)
    Object.assign(config, newConfig)
    error.value = null
    
    // 等待DOM更新后绘制连接线
    nextTick(() => {
      drawConnectingLines()
    })
  } catch (err) {
    error.value = `JSON解析错误: ${err.message}`
  }
}

// 绘制连接线
function drawConnectingLines() {
  if (!svgContainer.value || !previewContainer.value) return
  
  // 清空SVG容器
  svgContainer.value.innerHTML = ''
  
  const moduleDiagram = previewContainer.value.querySelector('.module-diagram')
  if (!moduleDiagram) return
  
  // 创建SVG元素
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg')
  svg.setAttribute('width', '100%')
  svg.setAttribute('height', '100%')
  svgContainer.value.appendChild(svg)
  
  // 获取系统名称元素的位置
  const systemNameEl = moduleDiagram.querySelector('.system-name')
  if (!systemNameEl) return
  
  const systemNameRect = systemNameEl.getBoundingClientRect()
  const diagramRect = moduleDiagram.getBoundingClientRect()
  
  // 系统名称底部中心点
  const systemBottomX = systemNameRect.left + systemNameRect.width / 2 - diagramRect.left
  const systemBottomY = systemNameRect.bottom - diagramRect.top
  
  // 获取所有模块名称元素
  const moduleNameEls = moduleDiagram.querySelectorAll('.module-name')
  
  if (moduleNameEls.length > 0) {
    // 创建从系统名称到模块水平线的垂直线
    const verticalLine = document.createElementNS('http://www.w3.org/2000/svg', 'line')
    verticalLine.setAttribute('x1', systemBottomX)
    verticalLine.setAttribute('y1', systemBottomY)
    verticalLine.setAttribute('x2', systemBottomX)
    verticalLine.setAttribute('y2', systemBottomY + styleConfig.systemToModuleLineLength / 2)
    verticalLine.setAttribute('stroke', 'black')
    verticalLine.setAttribute('stroke-width', '2')
    svg.appendChild(verticalLine)
    
    // 获取第一个和最后一个模块的位置
    const firstModuleRect = moduleNameEls[0].getBoundingClientRect()
    const lastModuleRect = moduleNameEls[moduleNameEls.length - 1].getBoundingClientRect()
    
    // 模块水平线的Y坐标
    const moduleLineY = systemBottomY + styleConfig.systemToModuleLineLength / 2
    
    // 创建连接所有模块的水平线
    const horizontalLine = document.createElementNS('http://www.w3.org/2000/svg', 'line')
    horizontalLine.setAttribute('x1', firstModuleRect.left + firstModuleRect.width / 2 - diagramRect.left)
    horizontalLine.setAttribute('y1', moduleLineY)
    horizontalLine.setAttribute('x2', lastModuleRect.left + lastModuleRect.width / 2 - diagramRect.left)
    horizontalLine.setAttribute('y2', moduleLineY)
    horizontalLine.setAttribute('stroke', 'black')
    horizontalLine.setAttribute('stroke-width', '2')
    svg.appendChild(horizontalLine)
    
    // 为每个模块创建垂直连接线
    moduleNameEls.forEach(moduleNameEl => {
      const moduleRect = moduleNameEl.getBoundingClientRect()
      const moduleTopX = moduleRect.left + moduleRect.width / 2 - diagramRect.left
      const moduleTopY = moduleRect.top - diagramRect.top
      
      // 创建从水平线到模块的垂直线
      const moduleVerticalLine = document.createElementNS('http://www.w3.org/2000/svg', 'line')
      moduleVerticalLine.setAttribute('x1', moduleTopX)
      moduleVerticalLine.setAttribute('y1', moduleLineY)
      moduleVerticalLine.setAttribute('x2', moduleTopX)
      moduleVerticalLine.setAttribute('y2', moduleTopY)
      moduleVerticalLine.setAttribute('stroke', 'black')
      moduleVerticalLine.setAttribute('stroke-width', '2')
      svg.appendChild(moduleVerticalLine)
      
      // 获取该模块下的所有子模块
      const moduleColumn = moduleNameEl.closest('.module-column')
      if (!moduleColumn) return
      
      const subModuleEls = moduleColumn.querySelectorAll('.sub-module')
      
      if (subModuleEls.length > 0) {
        // 模块底部中心点
        const moduleBottomX = moduleTopX
        const moduleBottomY = moduleRect.bottom - diagramRect.top
        
        // 创建从模块到子模块水平线的垂直线
        const subVerticalLine = document.createElementNS('http://www.w3.org/2000/svg', 'line')
        subVerticalLine.setAttribute('x1', moduleBottomX)
        subVerticalLine.setAttribute('y1', moduleBottomY)
        subVerticalLine.setAttribute('x2', moduleBottomX)
        subVerticalLine.setAttribute('y2', moduleBottomY + styleConfig.moduleToSubModuleLineLength / 2)
        subVerticalLine.setAttribute('stroke', 'black')
        subVerticalLine.setAttribute('stroke-width', '2')
        svg.appendChild(subVerticalLine)
        
        // 获取第一个和最后一个子模块的位置
        const firstSubModuleRect = subModuleEls[0].getBoundingClientRect()
        const lastSubModuleRect = subModuleEls[subModuleEls.length - 1].getBoundingClientRect()
        
        // 子模块水平线的Y坐标
        const subModuleLineY = moduleBottomY + styleConfig.moduleToSubModuleLineLength / 2
        
        // 创建连接所有子模块的水平线
        const subHorizontalLine = document.createElementNS('http://www.w3.org/2000/svg', 'line')
        subHorizontalLine.setAttribute('x1', firstSubModuleRect.left + firstSubModuleRect.width / 2 - diagramRect.left)
        subHorizontalLine.setAttribute('y1', subModuleLineY)
        subHorizontalLine.setAttribute('x2', lastSubModuleRect.left + lastSubModuleRect.width / 2 - diagramRect.left)
        subHorizontalLine.setAttribute('y2', subModuleLineY)
        subHorizontalLine.setAttribute('stroke', 'black')
        subHorizontalLine.setAttribute('stroke-width', '2')
        svg.appendChild(subHorizontalLine)
        
        // 为每个子模块创建垂直连接线
        subModuleEls.forEach(subModuleEl => {
          const subModuleRect = subModuleEl.getBoundingClientRect()
          const subModuleTopX = subModuleRect.left + subModuleRect.width / 2 - diagramRect.left
          const subModuleTopY = subModuleRect.top - diagramRect.top
          
          // 创建从水平线到子模块的垂直线
          const subModuleVerticalLine = document.createElementNS('http://www.w3.org/2000/svg', 'line')
          subModuleVerticalLine.setAttribute('x1', subModuleTopX)
          subModuleVerticalLine.setAttribute('y1', subModuleLineY)
          subModuleVerticalLine.setAttribute('x2', subModuleTopX)
          subModuleVerticalLine.setAttribute('y2', subModuleTopY)
          subModuleVerticalLine.setAttribute('stroke', 'black')
          subModuleVerticalLine.setAttribute('stroke-width', '2')
          svg.appendChild(subModuleVerticalLine)
        })
      }
    })
  }
}

// 导出为PNG
function exportToPNG() {
  if (!window.html2canvas) {
    ElMessage.error('请先引入html2canvas库')
    return
  }
  
  const previewElement = previewContainer.value
  if (!previewElement) return
  
  html2canvas(previewElement).then(canvas => {
    const link = document.createElement('a')
    link.download = '功能模块图.png'
    link.href = canvas.toDataURL('image/png')
    link.click()
    ElMessage.success('导出成功')
  })
}

// 初始化
onMounted(() => {
  generateModuleDiagram()
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
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  letter-spacing: 1px;
  font-weight: 600;
}

.tabs {
  display: flex;
  border-bottom: 1px solid var(--borderColor);
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
}

.tab.active {
  border-bottom: 2px solid var(--themeColor1);
  color: var(--themeColor1);
}

.tab-content {
  display: none;
  margin-bottom: 30px;
}

.tab-content.active {
  display: block;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.preview-container {
  margin-top: 20px;
  border: 1px solid var(--borderColor);
  padding: 20px;
  min-height: 400px;
  overflow: auto;
  background-color: var(--bgColor2);
  border-radius: 8px;
  box-shadow: 0 2px 8px var(--bgboxShadowColor1);
}

.module-diagram {
  position: relative;
  padding: 20px;
  min-height: 500px;
}

.svg-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.system-name, .module-name, .sub-module {
  position: relative;
  z-index: 1;
  background-color: var(--bgColor2);
}

.system-name {
  border: 2px solid var(--borderColor);
  padding: 10px 20px;
  text-align: center;
  width: 300px;
  margin: 0 auto;
  border-radius: 6px;
  box-shadow: 0 3px 8px var(--bgboxShadowColor1);
  font-weight: 600;
}

.modules-container {
  position: relative;
  display: flex;
  justify-content: space-around;
  width: 100%;
  margin-top: 50px;
  flex-wrap: wrap;
}

.module-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.module-name {
  border: 2px solid var(--borderColor);
  padding: 10px 20px;
  text-align: center;
  width: 200px;
  border-radius: 6px;
  box-shadow: 0 3px 8px var(--bgboxShadowColor1);
  font-weight: 500;
}

.sub-modules-container {
  position: relative;
  display: flex;
  justify-content: space-around;
  margin-top: 50px;
  width: 100%;
  flex-wrap: wrap;
}

.sub-module-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 5px 15px;
}

.sub-module {
  border: 2px solid var(--borderColor);
  padding: 10px;
  text-align: center;
  width: 80px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  writing-mode: vertical-lr;
  text-orientation: upright;
  border-radius: 6px;
  box-shadow: 0 3px 8px var(--bgboxShadowColor1);
}

.export-btn {
  background-color: var(--themeColor1);
  color: var(--themeTextColor);
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  float: right;
  margin-bottom: 15px;
  transition: all 0.3s cubic-bezier(0.65, 0.05, 0.36, 1);
}

.export-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.visual-config {
  margin-bottom: 20px;
  background-color: var(--bgColor2);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px var(--bgboxShadowColor1);
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--textColor2);
}

.form-group input, .form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--borderColor);
  border-radius: 4px;
  background-color: var(--bgColor1);
  color: var(--textColor1);
  transition: all 0.3s ease;
}

.form-group input:focus, .form-group select:focus {
  border-color: var(--themeColor1);
  outline: none;
  box-shadow: 0 0 0 2px var(--dColor2);
}

.modules-editor {
  margin-top: 25px;
}

.module-item {
  border: 1px solid var(--borderColor);
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 6px;
  background-color: var(--bgColor1);
  box-shadow: 0 2px 5px var(--bgboxShadowColor1);
}

.sub-modules-list {
  margin-top: 15px;
  padding-left: 20px;
}

.sub-modules-container {
  margin-bottom: 10px;
}

.sub-module-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  background-color: var(--bgColor2);
  padding: 10px;
  border-radius: 4px;
}

.sub-module-item input {
  flex-grow: 1;
  margin-right: 10px;
}

.btn {
  background-color: var(--themeColor1);
  color: var(--themeTextColor);
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
  transition: all 0.3s cubic-bezier(0.65, 0.05, 0.36, 1);
}

.btn.remove {
  background-color: #ff4d4f;
}

.btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.template-selection {
  margin-bottom: 25px;
}

.form-group.line-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 25px;
  padding: 20px;
  border: 1px solid var(--borderColor);
  background-color: var(--bgColor1);
  border-radius: 6px;
}

.control-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.control-item label {
  margin-bottom: 8px;
}

.control-item input {
  width: 100px;
  text-align: center;
}

.error {
  color: #ff4d4f;
  padding: 15px;
  background-color: var(--bgColor2);
  border: 1px solid #ffcccc;
  border-radius: 4px;
  margin-bottom: 15px;
}

.template-select {
  width: 100%;
}

.el-input, .el-select {
  width: 100%;
}

.el-textarea {
  width: 100%;
  margin-bottom: 20px;
}

.sub-module-item {
  .el-input {
    margin-right: 10px;
  }
}

.form-group.line-controls {
  .control-item {
    .el-input-number {
      width: 120px;
    }
  }
}

// 修改Element Plus组件在暗色模式下的样式
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
}

:deep(.el-select__popper) {
  background-color: var(--bgColor2);
  border-color: var(--borderColor);
  
  .el-select-dropdown__item {
    color: var(--textColor1);
    
    &.selected {
      color: var(--themeColor1);
    }
    
    &:hover {
      background-color: var(--bgColor1);
    }
  }
}

:deep(.el-input-number) {
  .el-input__wrapper {
    background-color: var(--bgColor2);
  }
  
  .el-input-number__decrease,
  .el-input-number__increase {
    background-color: var(--bgColor1);
    color: var(--textColor1);
    border-color: var(--borderColor);
    
    &:hover {
      color: var(--themeColor1);
    }
  }
}

@media only screen and (max-width: 767px) {
  .container {
    padding: 15px;
  }
  
  .system-name {
    width: 250px;
  }
  
  .module-name {
    width: 180px;
  }
  
  .sub-module {
    width: 60px;
    height: 100px;
    font-size: 14px;
  }
  
  .form-group.line-controls {
    flex-direction: column;
    gap: 10px;
  }
  
  .control-item {
    width: 100%;
  }
  
  .control-item input {
    width: 100%;
  }
  
  .export-btn {
    width: 100%;
    margin-top: 10px;
  }
}

.module-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid var(--borderColor);
}
</style>