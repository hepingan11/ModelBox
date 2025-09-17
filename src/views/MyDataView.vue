<template>
  <div class="body">
    
    <div style="display: flex;align-items: center;flex-direction: column;">
      
      <div class="data-section">

        <h2 style="margin-top: 40px;">对话频率统计</h2>
        <div style="margin: 20px 0;">
          <el-select 
            v-model="selectedTimeType" 
            placeholder="选择时间范围" 
            style="margin-right: 10px;"
            :disabled="isLoadingTimeData">
            <el-option label="1个月" :value="1" />
            <el-option label="2个月" :value="2" />
            <el-option label="3个月" :value="3" />
            <el-option label="1年(12个月)" :value="12" />
          </el-select>
          <el-icon 
            @click="getTimeFrequency" 
            color="var(--themeColor2)"
            :loading="isLoadingTimeData"
            size="25px">
            <Refresh />
          </el-icon>
          <span v-if="isLoadingTimeData" style="margin-left: 10px; color: var(--textColor2);">
            正在获取{{ selectedTimeType }}个月的数据...
          </span>
        </div>

        <!-- 折线图显示 -->
        <div v-if="timeFrequencyData && timeFrequencyData.length > 0">
          <v-chart 
            class="frequency-chart" 
            :option="timeFrequencyOption"
            :class="{ 'loading': isLoadingTimeData }" />
        </div>
        <div v-else-if="isLoadingTimeData" class="loading-placeholder">
          <el-icon class="is-loading"><Loading /></el-icon>
          <p>正在加载{{ selectedTimeType }}个月的对话频率数据...</p>
        </div>

        
        <h2 style="margin-top: 40px;">模型使用频率统计</h2>
        <div style="margin: 20px 0;">
          <el-icon 
            @click="getModelFrequency" 
            color="var(--themeColor2)"
            :loading="isLoadingTimeData"
            size="25px">
            <Refresh />
          </el-icon>
          <span style="margin-left: 10px; color: var(--textColor2);">
            当前数据范围：{{ selectedTimeType }}个月
          </span>
          <span v-if="isLoadingTimeData" style="margin-left: 10px; color: var(--textColor2);">
            正在获取{{ selectedTimeType }}个月的模型数据...
          </span>
        </div>
        
        <!-- 直方图显示 -->
        <div v-if="modelFrequencyData && modelFrequencyData.length > 0">
          <v-chart 
            class="model-frequency-chart" 
            :option="modelFrequencyOption"
            :class="{ 'loading': isLoadingTimeData }" />
        </div>
        <div v-else-if="isLoadingTimeData" class="loading-placeholder">
          <el-icon class="is-loading"><Loading /></el-icon>
          <p>正在加载{{ selectedTimeType }}个月的模型使用数据...</p>
        </div>

        <!-- 词云图区域 - 响应式布局 -->
        <div class="wordcloud-section">
          <h2 class="section-title">高频词分析</h2>
          
          <div class="wordcloud-container">
            <!-- 问题高频词 -->
            <div class="wordcloud-item">
              <div class="wordcloud-header">
                <h3>问题高频词</h3>
                <el-icon @click="getContentCloud" color="var(--themeColor2)" size="25px">
                  <Refresh />
                </el-icon>
              </div>
              
              <!-- 切换显示模式的按钮 -->
              <div class="view-toggle" v-if="contentWordMap && contentWordMap.length > 0">
                <el-button-group>
                  <el-button 
                    :type="contentViewMode === 'wordcloud' ? 'primary' : ''" 
                    @click="contentViewMode = 'wordcloud'"
                    size="small">
                    词云图
                  </el-button>
                  <el-button 
                    :type="contentViewMode === 'table' ? 'primary' : ''" 
                    @click="contentViewMode = 'table'"
                    size="small">
                    表格
                  </el-button>
                </el-button-group>
              </div>

              <!-- 词云图显示 -->
              <div v-if="contentViewMode === 'wordcloud' && contentWordMap && contentWordMap.length > 0">
                <v-chart class="wordcloud-chart-inline" :option="contentWordCloudOption" />
              </div>

              <!-- 表格显示 -->
              <div v-if="contentViewMode === 'table' && contentWordMap && contentWordMap.length > 0">
                <el-table :data="contentWordMap" border style="width: 100%;background-color: var(--bgColor1)"
                          :row-style="{
                  height: '100%',
                  background: ' var(--bgColor2)',
                  border: 'none',
                }"
                          :header-cell-style="{
                  background: ' var(--bgColor1)',
                  borderColor: 'var(--borderColor)',
                }">
                  <el-table-column prop="name" label="词语" />
                  <el-table-column prop="value" label="频率" />
                </el-table>
              </div>
            </div>

            <!-- 回答高频词 -->
            <div class="wordcloud-item">
              <div class="wordcloud-header">
                <h3>回答高频词</h3>
                <el-icon @click="getMessageCloud" color="var(--themeColor2)" size="25px">
                  <Refresh />
                </el-icon>
              </div>
              
              <!-- 切换显示模式的按钮 -->
              <div class="view-toggle" v-if="messageWordMap && messageWordMap.length > 0">
                <el-button-group>
                  <el-button 
                    :type="messageViewMode === 'wordcloud' ? 'primary' : ''" 
                    @click="messageViewMode = 'wordcloud'"
                    size="small">
                    词云图
                  </el-button>
                  <el-button 
                    :type="messageViewMode === 'table' ? 'primary' : ''" 
                    @click="messageViewMode = 'table'"
                    size="small">
                    表格
                  </el-button>
                </el-button-group>
              </div>

              <!-- 词云图显示 -->
              <div v-if="messageViewMode === 'wordcloud' && messageWordMap && messageWordMap.length > 0">
                <v-chart class="wordcloud-chart-inline" :option="messageWordCloudOption" />
              </div>

              <!-- 表格显示 -->
              <div v-if="messageViewMode === 'table' && messageWordMap && messageWordMap.length > 0">
                <el-table :data="messageWordMap" border style="width: 100%;background-color: var(--bgColor1)"
                          :row-style="{
                  height: '100%',
                  background: ' var(--bgColor2)',
                  border: 'none',
                }"
                          :header-cell-style="{
                  background: ' var(--bgColor1)',
                  borderColor: 'var(--borderColor)',
                }">
                  <el-table-column prop="name" label="词语" />
                  <el-table-column prop="value" label="频率" />
                </el-table>
              </div>
            </div>
          </div>
        </div>

        
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useStore } from 'vuex';
import { GetContentCloud, GetMessageCloud, GetTimeFrequency, GetModelFrequency } from "../../api/BSideApi";
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import VChart from 'vue-echarts';
import 'echarts-wordcloud';
import { Loading } from '@element-plus/icons-vue';

// 注册必要的组件
use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent]);

// 获取store用于主题切换
const store = useStore();

// 计算当前主题类型
const isDarkMode = computed(() => {
  return store.getters.themeInfo.className === 'darkMode';
});

// 主题适配的颜色方案
const themeColors = computed(() => {
  if (isDarkMode.value) {
    return {
      // 深色模式颜色 - 更亮更鲜艳
      wordcloud: [
        '#FF6B9D', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
        '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9',
        '#F8C471', '#82E0AA', '#F1948A', '#FFB347', '#D2B4DE'
      ],
      chart: [
        '#FF6B9D', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
        '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9'
      ],
      line: '#4ECDC4',
      shadow: '#000',
      tooltipBg: 'rgba(42, 42, 42, 0.95)',
      tooltipBorder: '#555'
    };
  } else {
    return {
      // 浅色模式颜色 - 稍微柔和一些
      wordcloud: [
        '#E74C3C', '#3498DB', '#2ECC71', '#F39C12', '#9B59B6',
        '#1ABC9C', '#34495E', '#E67E22', '#8E44AD', '#27AE60',
        '#F1C40F', '#16A085', '#C0392B', '#2980B9', '#D35400'
      ],
      chart: [
        '#E74C3C', '#3498DB', '#2ECC71', '#F39C12', '#9B59B6',
        '#1ABC9C', '#34495E', '#E67E22', '#8E44AD', '#27AE60'
      ],
      line: '#3498DB',
      shadow: '#999',
      tooltipBg: 'rgba(255, 255, 255, 0.95)',
      tooltipBorder: '#ddd'
    };
  }
});

// 视图模式控制
const contentViewMode = ref('wordcloud') // 'wordcloud' | 'table'
const messageViewMode = ref('wordcloud') // 'wordcloud' | 'table'

// 数据存储
const contentWordMap = ref()
const messageWordMap = ref()
const timeFrequencyData = ref()
const modelFrequencyData = ref()
const selectedTimeType = ref(1) // 默认选择1个月
const isLoadingTimeData = ref(false) // 时间相关数据的加载状态

onMounted(async () => {
  // 可以在这里预加载数据
  getContentCloud()
  getMessageCloud()
  getTimeFrequency()
  getModelFrequency()
})

// 监听时间范围选择器的变化，动态重新获取数据
watch(selectedTimeType, async (newValue, oldValue) => {
  // 避免初始化时触发
  if (oldValue === undefined) return
  
  console.log('时间范围从', oldValue, '个月切换到:', newValue, '个月')
  
  try {
    isLoadingTimeData.value = true
    // 并行获取时间频率数据和模型使用频率数据
    await Promise.all([
      GetTimeFrequency(newValue).then(res => {
        timeFrequencyData.value = res.data || res
        console.log('时间频率数据更新完成')
      }),
      GetModelFrequency(newValue).then(res => {
        modelFrequencyData.value = res.data || res
        console.log('模型频率数据更新完成')
      })
    ])
    
    console.log('数据切换完成，当前时间范围:', newValue, '个月')
  } catch (error) {
    console.error('切换时间范围时获取数据失败:', error)
  } finally {
    isLoadingTimeData.value = false
  }
})

// 词云图配置
const contentWordCloudOption = computed(() => {
  if (!contentWordMap.value || contentWordMap.value.length === 0) return {}
  
  return {
    title: {
      text: '问题高频词云图',
      left: 'center',
      textStyle: {
        color: 'var(--textColor1)',
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      show: true,
      backgroundColor: themeColors.value.tooltipBg,
      borderColor: themeColors.value.tooltipBorder,
      borderWidth: 1,
      textStyle: {
        color: 'var(--textColor1)'
      },
      formatter: function (params) {
        return `${params.name}: ${params.value}`
      }
    },
    series: [{
      type: 'wordCloud',
      gridSize: 8,
      sizeRange: [14, 60],
      rotationRange: [-45, 45],
      rotationStep: 15,
      shape: 'pentagon',
      width: '80%',
      height: '80%',
      left: 'center',
      top: 'center',
      textStyle: {
        fontFamily: 'sans-serif',
        fontWeight: 'bold',
        color: function () {
          return themeColors.value.wordcloud[Math.floor(Math.random() * themeColors.value.wordcloud.length)]
        }
      },
      emphasis: {
        focus: 'self',
        textStyle: {
          shadowBlur: 10,
          shadowColor: themeColors.value.shadow
        }
      },
      data: contentWordMap.value.map(item => ({
        name: item.name,
        value: item.value
      }))
    }]
  }
})

const messageWordCloudOption = computed(() => {
  if (!messageWordMap.value || messageWordMap.value.length === 0) return {}
  
  return {
    title: {
      text: '回答高频词云图',
      left: 'center',
      textStyle: {
        color: 'var(--textColor1)',
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      show: true,
      backgroundColor: themeColors.value.tooltipBg,
      borderColor: themeColors.value.tooltipBorder,
      borderWidth: 1,
      textStyle: {
        color: 'var(--textColor1)'
      },
      formatter: function (params) {
        return `${params.name}: ${params.value}`
      }
    },
    series: [{
      type: 'wordCloud',
      gridSize: 8,
      sizeRange: [14, 60],
      rotationRange: [-45, 45],
      rotationStep: 15,
      shape: 'pentagon',
      width: '80%',
      height: '80%',
      left: 'center',
      top: 'center',
      textStyle: {
        fontFamily: 'sans-serif',
        fontWeight: 'bold',
        color: function () {
          return themeColors.value.wordcloud[Math.floor(Math.random() * themeColors.value.wordcloud.length)]
        }
      },
      emphasis: {
        focus: 'self',
        textStyle: {
          shadowBlur: 10,
          shadowColor: themeColors.value.shadow
        }
      },
      data: messageWordMap.value.map(item => ({
        name: item.name,
        value: item.value
      }))
    }]
  }
})

// 折线图配置
const timeFrequencyOption = computed(() => {
  if (!timeFrequencyData.value || timeFrequencyData.value.length === 0) return {}
  
  // 处理数据，提取月份和对应的频率
  const months = []
  const frequencies = []
  
  timeFrequencyData.value.forEach(item => {
    // 处理日期格式的数据: [{"2025-08-17": 5}, {"2025-08-14": 3}]
    const keys = Object.keys(item)
    
    if (keys.length === 1) {
      // 单个日期键值对格式
      const dateKey = keys[0]
      const frequency = item[dateKey]
      
      // 格式化日期显示（只显示月-日）
      const date = new Date(dateKey)
      const monthDay = `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
      
      months.push(monthDay)
      frequencies.push(frequency)
    } else {
      // 兼容其他格式
      const monthKey = keys.find(key => 
        key.toLowerCase().includes('month') || 
        key === 'month' || 
        key === '月份' ||
        key === 'time' ||
        key === '时间' ||
        key.includes('-') // 日期格式
      )
      
      const freqKey = keys.find(key => 
        key.toLowerCase().includes('freq') || 
        key === 'frequency' || 
        key === '频率' || 
        key === 'count' ||
        key === 'value' ||
        key === '值' ||
        key === 'num' ||
        key === '数量'
      )
      
      if (monthKey && freqKey) {
        let displayValue = item[monthKey]
        // 如果是日期格式，转换为月-日显示
        if (typeof displayValue === 'string' && displayValue.includes('-')) {
          const date = new Date(displayValue)
          displayValue = `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
        }
        months.push(displayValue)
        frequencies.push(item[freqKey])
      } else if (keys.length === 2) {
        // 如果只有两个字段，假设第一个是日期，第二个是频率
        const values = Object.values(item)
        months.push(values[0])
        frequencies.push(values[1])
      }
    }
  })
  
  // 按日期排序数据
  const sortedData = months.map((month, index) => ({
    date: month,
    frequency: frequencies[index],
    originalDate: timeFrequencyData.value[index] ? Object.keys(timeFrequencyData.value[index])[0] : month
  })).sort((a, b) => new Date(a.originalDate) - new Date(b.originalDate))
  
  const sortedMonths = sortedData.map(item => item.date)
  const sortedFrequencies = sortedData.map(item => item.frequency)
  
  return {
    title: {
      text: '对话频率统计',
      left: 'center',
      textStyle: {
        color: 'var(--textColor1)',
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: themeColors.value.tooltipBg,
      borderColor: themeColors.value.tooltipBorder,
      borderWidth: 1,
      textStyle: {
        color: 'var(--textColor1)'
      },
      formatter: function(params) {
        return `${params[0].axisValue}: ${params[0].value}次对话`
      }
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedMonths,
      axisLabel: {
        color: 'var(--textColor2)'
      },
      axisLine: {
        lineStyle: {
          color: 'var(--borderColor)'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '对话次数',
      nameTextStyle: {
        color: 'var(--textColor2)'
      },
      axisLabel: {
        color: 'var(--textColor2)'
      },
      axisLine: {
        lineStyle: {
          color: 'var(--borderColor)'
        }
      },
      splitLine: {
        lineStyle: {
          color: 'var(--borderColor)',
          type: 'dashed'
        }
      }
    },
    series: [{
      name: '对话频率',
      type: 'line',
      data: sortedFrequencies,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: themeColors.value.line
      },
      itemStyle: {
        color: themeColors.value.line,
        borderColor: isDarkMode.value ? '#333' : '#fff',
        borderWidth: 2
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: isDarkMode.value ? 'rgba(78, 205, 196, 0.4)' : 'rgba(52, 152, 219, 0.3)'
          }, {
            offset: 1,
            color: isDarkMode.value ? 'rgba(78, 205, 196, 0.08)' : 'rgba(52, 152, 219, 0.05)'
          }]
        }
      }
    }]
  }
})

// 模型使用频率直方图配置
const modelFrequencyOption = computed(() => {
  if (!modelFrequencyData.value || modelFrequencyData.value.length === 0) return {}
  
  // 处理数据，提取模型名称和对应的使用频率
  const modelNames = []
  const frequencies = []
  
  modelFrequencyData.value.forEach(item => {
    // 处理数据格式: [{"GPT-4": 25}, {"Claude": 18}, {"ChatGPT": 32}]
    const keys = Object.keys(item)
    
    if (keys.length === 1) {
      // 单个模型键值对格式
      const modelKey = keys[0]
      const frequency = item[modelKey]
      
      modelNames.push(modelKey)
      frequencies.push(frequency)
    } else {
      // 兼容其他格式
      const nameKey = keys.find(key => 
        key.toLowerCase().includes('model') || 
        key === 'name' || 
        key === '模型' ||
        key === 'modelName' ||
        key === '名称'
      )
      
      const freqKey = keys.find(key => 
        key.toLowerCase().includes('freq') || 
        key === 'frequency' || 
        key === '频率' || 
        key === 'count' ||
        key === 'value' ||
        key === '值' ||
        key === 'num' ||
        key === '数量' ||
        key === 'usage' ||
        key === '使用次数'
      )
      
      if (nameKey && freqKey) {
        modelNames.push(item[nameKey])
        frequencies.push(item[freqKey])
      } else if (keys.length === 2) {
        // 如果只有两个字段，假设第一个是模型名，第二个是频率
        const values = Object.values(item)
        modelNames.push(values[0])
        frequencies.push(values[1])
      }
    }
  })
  
  // 按使用频率降序排序
  const sortedData = modelNames.map((name, index) => ({
    name: name,
    frequency: frequencies[index]
  })).sort((a, b) => b.frequency - a.frequency)
  
  const sortedNames = sortedData.map(item => item.name)
  const sortedFrequencies = sortedData.map(item => item.frequency)
  
  return {
    title: {
      text: '模型使用频率统计',
      left: 'center',
      textStyle: {
        color: 'var(--textColor1)',
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: themeColors.value.tooltipBg,
      borderColor: themeColors.value.tooltipBorder,
      borderWidth: 1,
      textStyle: {
        color: 'var(--textColor1)'
      },
      formatter: function(params) {
        return `${params[0].axisValue}: ${params[0].value}次使用`
      }
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedNames,
      axisLabel: {
        color: 'var(--textColor2)',
        interval: 0,
        rotate: sortedNames.length > 5 ? 45 : 0 // 如果模型名称太多，旋转标签
      },
      axisLine: {
        lineStyle: {
          color: 'var(--borderColor)'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '使用次数',
      nameTextStyle: {
        color: 'var(--textColor2)'
      },
      axisLabel: {
        color: 'var(--textColor2)'
      },
      axisLine: {
        lineStyle: {
          color: 'var(--borderColor)'
        }
      },
      splitLine: {
        lineStyle: {
          color: 'var(--borderColor)',
          type: 'dashed'
        }
      }
    },
    series: [{
      name: '使用频率',
      type: 'bar',
      data: sortedFrequencies,
      itemStyle: {
        color: function(params) {
          return themeColors.value.chart[params.dataIndex % themeColors.value.chart.length]
        },
        borderRadius: [4, 4, 0, 0] // 圆角顶部
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: isDarkMode.value ? 'rgba(0, 0, 0, 0.8)' : 'rgba(0, 0, 0, 0.3)'
        }
      },
      label: {
        show: true,
        position: 'top',
        color: 'var(--textColor2)',
        formatter: '{c}'
      }
    }]
  }
})

async function getContentCloud() {
  contentWordMap.value = await GetContentCloud()
  // 获取数据后默认显示词云图
  contentViewMode.value = 'wordcloud'
}

async function getMessageCloud() {
  messageWordMap.value = await GetMessageCloud()
  // 获取数据后默认显示词云图
  messageViewMode.value = 'wordcloud'
}

async function getTimeFrequency() {
  try {
    isLoadingTimeData.value = true
    timeFrequencyData.value = await GetTimeFrequency(selectedTimeType.value)
    console.log('时间频率数据获取成功:', selectedTimeType.value, '个月')
  } catch (error) {
    console.error('获取时间频率数据失败:', error)
  } finally {
    isLoadingTimeData.value = false
  }
}

async function getModelFrequency() {
  try {
    if (!isLoadingTimeData.value) {
      isLoadingTimeData.value = true
    }
    modelFrequencyData.value = await GetModelFrequency(selectedTimeType.value)
    console.log('模型频率数据获取成功:', selectedTimeType.value, '个月')
  } catch (error) {
    console.error('获取模型频率数据失败:', error)
  } finally {
    // 只有在单独调用时才设为false，避免与getTimeFrequency冲突
    setTimeout(() => {
      isLoadingTimeData.value = false
    }, 100)
  }
}
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  text-align: center;
  align-items: center;
  //按列排
  flex-direction: column;
  animation: gradient 8s ease infinite;
}

@keyframes gradient {
  0% {
    background-position: 0 12%;
  }

  50% {
    background-position: 100% 100%;
  }

  100% {
    background-position: 0 12%;
  }
}

#id {
  margin: 0;
}

.chart {
  height: 400px;
}

.body{
  background-color: var(--bgColor1);
  margin: 0;
  height: 100%;
  text-align: center;
  animation: explainAnimation 0.3s;
  box-sizing: border-box;
  overflow-y: auto;
}

.content-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.text-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
  transition: all 0.3s ease;
}

.text-truncated {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  max-height: 4.5em;
}

/* 只针对内容展开按钮应用特殊样式 */
.content-cell :deep(.el-button) {
  padding: 4px 0;
  height: auto;
  align-self: flex-start;
}

.content-cell :deep(.el-icon) {
  margin-right: 4px;
}

/* 词云图相关样式 */
.data-section {
  width: 100%;
  max-width: 1200px;
}

.view-toggle {
  margin: 15px 0;
}

/* 词云图区域样式 */
.wordcloud-section {
  width: 100%;
  margin: 40px 0;
}

.section-title {
  text-align: center;
  margin-bottom: 30px;
  color: var(--textColor1);
  font-size: 24px;
  font-weight: bold;
}

.wordcloud-container {
  display: flex;
  gap: 20px;
  justify-content: space-between;
  align-items: flex-start;
}

.wordcloud-item {
  flex: 1;
  min-width: 0; /* 防止flex item溢出 */
}

.wordcloud-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 0 10px;
}

.wordcloud-header h3 {
  margin: 0;
  color: var(--textColor1);
  font-size: 18px;
  font-weight: 600;
}


.wordcloud-chart-inline {
  width: 100%;
  height: 450px;
  margin: 15px 0;
  border-radius: 12px;
  background: var(--bgColor2);
  box-shadow: 0 4px 20px var(--bgboxShadowColor1);
  border: 1px solid var(--borderColor);
  transition: all 0.3s ease;
}

.wordcloud-chart-inline:hover {
  box-shadow: 0 6px 30px var(--bgboxShadowColor2);
  transform: translateY(-2px);
}

.frequency-chart {
  width: 100%;
  height: 400px;
  margin: 20px 0;
  border-radius: 12px;
  background: var(--bgColor2);
  box-shadow: 0 4px 20px var(--bgboxShadowColor1);
  border: 1px solid var(--borderColor);
  transition: all 0.3s ease;
}

.frequency-chart:hover {
  box-shadow: 0 6px 30px var(--bgboxShadowColor2);
  transform: translateY(-2px);
}

.model-frequency-chart {
  width: 100%;
  height: 450px;
  margin: 20px 0;
  border-radius: 12px;
  background: var(--bgColor2);
  box-shadow: 0 4px 20px var(--bgboxShadowColor1);
  border: 1px solid var(--borderColor);
  transition: all 0.3s ease;
}

.model-frequency-chart:hover {
  box-shadow: 0 6px 30px var(--bgboxShadowColor2);
  transform: translateY(-2px);
}

/* 加载状态样式 */
.loading-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  background: var(--bgColor2);
  border-radius: 12px;
  border: 1px solid var(--borderColor);
  color: var(--textColor2);
  margin: 20px 0;
}

.loading-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 16px;
  color: var(--themeColor2);
}

.loading-placeholder p {
  margin: 0;
  font-size: 14px;
}

/* 图表加载状态 */
.frequency-chart.loading,
.model-frequency-chart.loading {
  opacity: 0.6;
  pointer-events: none;
  position: relative;
}

.frequency-chart.loading::after,
.model-frequency-chart.loading::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  /* 平板和移动端：词云图垂直排列 */
  .wordcloud-container {
    flex-direction: column;
    gap: 30px;
  }
  
  .wordcloud-chart-inline {
    height: 400px;
  }
  
  .wordcloud-chart {
    height: 400px;
  }
  
  .frequency-chart {
    height: 350px;
  }
  
  .model-frequency-chart {
    height: 380px;
  }
  
  .section-title {
    font-size: 20px;
  }
  
  .wordcloud-header h3 {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  /* 手机端：更紧凑的布局 */
  .wordcloud-section {
    margin: 30px 0;
  }
  
  .section-title {
    font-size: 18px;
    margin-bottom: 20px;
  }
  
  .wordcloud-container {
    gap: 25px;
  }
  
  .wordcloud-header {
    padding: 0 5px;
  }
  
  .wordcloud-header h3 {
    font-size: 15px;
  }
  
  .wordcloud-chart-inline {
    height: 300px;
    margin: 10px 0;
  }
  
  .wordcloud-chart {
    height: 300px;
  }
  
  .frequency-chart {
    height: 280px;
  }
  
  .model-frequency-chart {
    height: 320px;
  }
}
</style>