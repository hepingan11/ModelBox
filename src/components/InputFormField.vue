<template>
  <div style="width: 100%">
    <!--
    放到一起好改样式


    父组件使用代码
    <InputFormField
      ref="inputRef"
      :aiLoading="aiLoading"
      :inputText="input"
      @update:inputText="input = $event"
      @update:model="model = $event"
      @onSubmit="onSubmit"
    />

    1、重置文字   父组件代码
        inputRef.value.resetInputValue();
    2、输入框聚焦   父组件代码
        inputRef.value.$refs.inputRefInner.focus();

   -->
    <div class="InputFormFieldWapper">
      <el-select
        v-model="modelInner"
        v-if="needSelect"
        class="selectWrapper"
        placeholder="Ai模型"
        @change="updateModel"
      >
        <el-option value="GPT" label="GPT-5(推荐)" />
        <el-option value="CLAUDE" label="Claude-4-sonnet" />
        <el-option value="QWEN" label="通义千问3" />
        <el-option value="DEEPSEEK" label="DeepSeek(推荐)" />
        <el-option value="DEEPSEEK_R" label="DeepSeek深度思考" />
        <el-option value="GEMINI" label="Gemini-2.5" />  
        <el-option value="GROK" label="Grok-3" />
        <el-option value="GLM" label="GLM-4.5V(推荐)" />
        <el-option value="DOUBAO" label="豆包1.5pro" />
      </el-select>
      


      <div class="input-container">
        <el-input
          :style="{ marginLeft: needSelect ? '0px' : '10px' }"
          @keydown="handleKeyDown"
          v-model="inputTextInner"
          autosize
          @input="updateInputText"
          ref="inputRefInner"
          type="textarea"
          :placeholder="
            aiLoading ? '思考中..' : '输入你想问的...  ctrl+enter发送'
          "
          :disabled="aiLoading"
        ></el-input>
      </div>

      
      <div class="input-controls">
        <template v-if="aiLoading">
          <!-- 加载动画 -->
          <div class="animation-dot">
            <div class="dot0"></div>
            <div class="dot1"></div>
            <div class="dot2"></div>
            <div class="dot3"></div>
            <div class="dot4"></div>
          </div>
        </template>
        <template v-else>
          <!-- 上传文件按钮（保留） -->
          <el-tooltip 
            content="上传图片或视频"
            placement="top"
          >
            <div class="uploadFileButton" v-if="modelInner === 'GLM' || modelInner === 'GPT' || modelInner === 'DOUBAO'">
              <input 
                type="file" 
                ref="fileInput" 
                class="file-input" 
                multiple
                accept="image/*,video/*"
                @change="handleFileUpload"
                :disabled="!(modelInner === 'GLM' || modelInner === 'GPT' || modelInner === 'DOUBAO')"
              />
              <el-icon :size="16">
                <Document />
              </el-icon>
            </div>
          </el-tooltip>
          
          <!-- 发送按钮 - 始终显示 -->
          <div @click="onSubmit" class="sendIcon">
            <el-icon :size="20">
              <Promotion />
            </el-icon>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import { Promotion, Close, Document } from "@element-plus/icons-vue";
import { ref, defineComponent, watch, onMounted } from "vue";
import { ElMessage } from 'element-plus';

export default defineComponent({
  name: "InputFormField",
  components: {
    Promotion,
    Close,
    Document
  },
  props: {
    needSelect: {
      type: Boolean,
      default: true,
    },
    // 选择的模型
    model: {
      type: String,
      default: null,
    },
    // 输入内容
    inputText: {
      type: String,
      default: null,
    },
    // 加载状态
    aiLoading: {
      type: Boolean,
      default: false,
    },

  },
  setup(props, { emit }) {
    // 通过ref监听组件值
    let inputTextInner = ref(null);
    let modelInner = ref("GLM");
    const inputRefInner = ref(null);
    const fileInput = ref(null);
    // 存储当前上传的文件
    const currentFiles = ref([]);

    // 从本地存储加载模型选择
    function loadModelFromStorage() {
      try {
        const savedModel = localStorage.getItem('selectedModel');
        if (savedModel) {
          modelInner.value = savedModel;
          // 通知父组件模型已更新
          emit("update:model", savedModel);
          console.log('从本地存储加载模型:', savedModel);
        }
      } catch (error) {
        console.error('加载模型选择失败:', error);
      }
    }

    // 保存模型选择到本地存储
    function saveModelToStorage(model) {
      try {
        localStorage.setItem('selectedModel', model);
        console.log('保存模型选择到本地存储:', model);
      } catch (error) {
        console.error('保存模型选择失败:', error);
      }
    }

    // 组件挂载时加载本地存储的模型
    onMounted(() => {
      loadModelFromStorage();
    });

    // 监听父的model 双向绑定需要
    watch(
      () => props.model,
      (newVal) => {
        if (newVal && newVal !== modelInner.value) {
          modelInner.value = newVal;
          saveModelToStorage(newVal);
        }
      }
    );



    // 监听modelInner的变化，如果不是GLM或GPT，则清空已上传的文件
    watch(modelInner, (newVal) => {
      if (newVal !== 'GLM' && newVal !== 'GPT' && newVal !== 'DOUBAO') {
        currentFiles.value = [];
        if (fileInput.value) {
          fileInput.value.value = '';
        }
        emit("file-removed", {}); // 通知父组件所有文件已移除
      }
      // 保存模型选择到本地存储
      saveModelToStorage(newVal);
    });

    // 使用watch监听content变量的变化
    // eslint-disable-next-line no-unused-vars
    watch(inputTextInner, (newVal, oldValue) => {
      emit("update:inputText", newVal);
    });

    // 移除已上传的文件
    function removeFile(index) {
      if (currentFiles.value[index]) {
        // 通知父组件文件已移除
        emit("file-removed", currentFiles.value[index]);
        currentFiles.value.splice(index, 1);
        
        // 清空文件输入，允许再次上传相同文件
        if (fileInput.value) {
          fileInput.value.value = '';
        }
        
        ElMessage.success('文件已移除');
      }
    }

    // 处理文件上传
    function handleFileUpload(event) {
      const files = event.target.files;
      if (!files || files.length === 0) return;
      
      // 限制文件数量和大小
      const MAX_FILES = 3;
      const MAX_FILE_SIZE = 15 * 1024 * 1024; // 15MB
      
      // 定义允许的文件类型
      const allowedTypes = {
        image: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp', 'image/svg+xml'],
        video: ['video/mp4', 'video/avi', 'video/mov', 'video/wmv', 'video/flv', 'video/webm', 'video/mkv']
      };
      const allAllowedTypes = [...allowedTypes.image, ...allowedTypes.video];

      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        
        // 检查文件类型
        if (!allAllowedTypes.includes(file.type)) {
          ElMessage.warning(`文件 ${file.name} 不是支持的图片或视频格式`);
          continue;
        }
        
        if (currentFiles.value.length >= MAX_FILES) {
          ElMessage.warning(`最多只能上传 ${MAX_FILES} 个文件`);
          break;
        }
        if (file.size > MAX_FILE_SIZE) {
          ElMessage.warning(`文件 ${file.name} 超过 15MB 限制`);
          continue;
        }

        // 如果已经有文件，先移除
        if (currentFiles.value.some(f => f.name === file.name)) {
          removeFile(currentFiles.value.findIndex(f => f.name === file.name));
        }
        
        // 设置当前文件
        currentFiles.value.push(file);
        
        // 发送事件给父组件，包含文件信息
        emit("file-uploaded", file);
        
        ElMessage.success('文件已添加');
      }
    }

    // 格式化文件大小
    function formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // 重置值
    function resetInputValue() {
      if (inputRefInner.value) {
        inputRefInner.value.clear();
      }
      // 清空文件
      currentFiles.value = [];
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    }

    //提交内容的快捷键监听
    function handleKeyDown(e) {
      // 判断是否按下了 ctrl 键和 enter 键
      if (e.ctrlKey && e.keyCode === 13) {
        emit("onSubmit");
      }
    }

    //提交内容的点击
    function onSubmit() {
      emit("onSubmit");
    }

    // 更新父model  选择智能还是标准
    function updateModel(value) {
      modelInner.value = value;
      saveModelToStorage(value);
      emit("update:model", value);
    }

    // 更新输入文本
    function updateInputText(value) {
      inputTextInner.value = value;
    }



    return {
      updateInputText,
      onSubmit,
      handleKeyDown,
      updateModel,
      resetInputValue,
      handleFileUpload,
      removeFile,
      formatFileSize,
      loadModelFromStorage,
      saveModelToStorage,
      inputTextInner,
      modelInner,
      inputRefInner,
      fileInput,
      currentFiles
    };
  },
});
</script>

<style lang="scss">
/* 添加全局样式覆盖来移除Element Plus组件的边框 */
.el-select__popper {
  .el-select-dropdown__item {
    &.selected {
      color: var(--themeColor1);
      background-color: rgba(var(--themeColor1-rgb), 0.05) !important;
    }
  }
}

/* 覆盖Element Plus全局聚焦样式 */
.el-textarea {
  .el-textarea__inner {
    &:focus {
      border: none !important;
      box-shadow: none !important;
    }
  }
}

.el-input {
  .el-input__wrapper {
    &.is-focus {
      border: none !important;
      box-shadow: none !important;
    }
  }
}
</style>

<style lang="scss" scoped>
.InputFormFieldWapper {
  display: flex;
  width: 100%;
  align-items: flex-end;
  flex-direction: row;
  gap: 10px;

  .input-container {
    flex: 1;
    position: relative;
    display: flex;
    width: 100%;
    align-items: flex-end;
  }

  .input-controls {
    display: flex;
    align-items: flex-end;
    gap: 10px;
    flex-shrink: 0;
    margin-right: 10px;
    margin-bottom: 10px;
  }

  .sendIcon {
    margin-bottom: 10px;
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    color: var(--themeTextColor);
    cursor: pointer;
    background: var(--themeColor2);
    border-radius: 50%;
    justify-content: center;
    align-items: center;
    display: flex;
    transition: all 0.3s ease;
    
    &:hover {
      transform: scale(1.05);
      box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
    }
  }
  
  .uploadFileButton {
    margin-bottom: 10px;
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    color: var(--themeTextColor);
    cursor: pointer;
    background: var(--bgColor3);
    border-radius: 50%;
    justify-content: center;
    align-items: center;
    display: flex;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    
    &:hover {
      transform: scale(1.05);
      background: #e6ccf5;
      box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
    }

    .file-input {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      opacity: 0;
      cursor: pointer;
    }
  }
}



:deep(.selectWrapper) {
  width: 150px;
  flex-shrink: 0;
  .el-input {
    .el-input__wrapper {
      padding: 20px 15px;
      background-color: var(--bgColor1) !important;
      border: none !important;
      outline: none !important;
      border-radius: 4px !important;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
      transition: all 0.3s ease !important;
      
      &.is-focus {
        border: none !important;
        outline: none !important;
        box-shadow: 0 3px 12px rgba(0, 0, 0, 0.12) !important;
      }
      
      .el-input__inner {
        text-indent: 10px;
        color: var(--textColor1);
      }

      &:hover {
        border: none !important;
        box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08) !important;
      }
      
      &:focus-within {
        border: none !important;
        outline: none !important;
        box-shadow: 0 3px 12px rgba(0, 0, 0, 0.12) !important;
      }
    }
  }

  &.el-select--disabled {
    .el-input__wrapper {
      background-color: var(--bgColor2) !important;
      opacity: 0.8;
    }
  }
}

:deep(.InputFormFieldWapper) {
  .el-textarea__inner {
    background: var(--bgColor1);
    max-height: 400px;
    padding: 20px;
    margin: 10px 0px;
    width: 100%;
    resize: none;
    border: none !important;
    outline: none !important;
    border-radius: 4px !important;
    color: var(--textColor3);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
    transition: all 0.3s ease;
    
    &:hover {
      border: none !important;
      box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08) !important;
    }

    &:focus {
      border: none !important;
      outline: none !important;
      box-shadow: 0 3px 12px rgba(0, 0, 0, 0.12) !important;
    }

    &:disabled {
      background-color: var(--bgColor2);
      opacity: 0.8;
    }
  }
}

.animation-dot {
  margin-bottom: 10px;
  display: flex;
  gap: 2px;
  align-items: center;
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

.dot0,
.dot1,
.dot2,
.dot3,
.dot4 {
  background: rgb(166, 129, 236);
  width: 10px;
  height: 10px;
  border-color: #464646;
  border-radius: 50%;
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

.dot0 {
  animation: jumpT 1.3s -0.64s linear infinite;
}

.dot1 {
  animation: jumpT 1.3s -0.32s linear infinite;
}

.dot2 {
  animation: jumpT 1.3s -0.16s linear infinite;
}

.dot3 {
  animation: jumpT 1.3s linear infinite;
}

@media screen and (max-width: 756px) {
  :deep(.selectWrapper) {
    width: 80px;
    min-width: 80px;
    .el-input {
      .el-input__wrapper {
        .el-input__inner {
          text-indent: 0px;
        }
      }
    }
  }

  
}
</style>
