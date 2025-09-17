<template>
  <div class="body">
    <div class="work-panel-container">
      <div class="panel-card">
        <div class="panel-header">
          <h2>作品管理</h2>
          <el-button 
            @click="show = true" 
            type="primary" 
            color="var(--themeColor2)" 
            class="add-work-btn hover-scale"
          >
            <el-icon><Plus /></el-icon> 添加作品
          </el-button>
        </div>
        
        <div class="search-container">
          <el-input
            v-model="prompt"
            placeholder="搜索作品名称或类型"
            clearable
            @change="search"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="table-container">
          <el-table
            :data="workList"
            style="width: 100%"
            :header-cell-style="{
              background: 'var(--bgColor1)',
              borderColor: 'var(--borderColor)',
              color: 'var(--textColor1)'
            }"
            :row-style="{
              background: 'var(--bgColor1)',
              borderBottom: '1px solid var(--borderColor)',
            }"
            border
          >
            <el-table-column prop="workId" label="ID" width="80" />
            <el-table-column prop="name" label="作品名称" min-width="120">
              <template #default="scope">
                <div class="work-name">{{ scope.row.name }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="url" label="链接" min-width="200">
              <template #default="scope">
                <el-tooltip :content="scope.row.url" placement="top" :show-after="500">
                  <el-link 
                    :href="scope.row.url" 
                    target="_blank" 
                    type="primary" 
                    :underline="false"
                    class="work-link hover-scale"
                  >
                    {{ scope.row.url && scope.row.url.length > 30 ? scope.row.url.substring(0, 30) + '...' : scope.row.url }}
                    <el-icon class="link-icon"><Link /></el-icon>
                  </el-link>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="类型" width="120">
              <template #default="scope">
                <el-tag 
                  :type="getTagType(scope.row.sort)"
                  effect="plain"
                  size="small"
                  class="hover-scale"
                >
                  {{ scope.row.sort }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedTime" label="更新时间" width="150">
              <template #default="scope">
                <span>{{ formatDate(scope.row.updatedTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="创建时间" width="150">
              <template #default="scope">
                <span>{{ formatDate(scope.row.createdTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="100">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDeleteWorkInfo(scope.row)"
                  class="action-btn hover-scale"
                >
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="empty-placeholder" v-if="workList.length === 0">
            <el-empty description="暂无作品数据" />
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 添加作品对话框 -->
  <el-dialog 
    v-model="show" 
    title="添加作品" 
    width="500px" 
    destroy-on-close
    :close-on-click-modal="false"
    class="custom-dialog"
  >
    <el-form 
      label-position="top" 
      :model="newWorkForm"
      ref="addFormRef"
      :rules="formRules"
    >
      <el-form-item label="作品名称" prop="name">
        <el-input v-model="newWorkForm.name" placeholder="请输入作品名称" />
      </el-form-item>
      <el-form-item label="作品链接" prop="url">
        <el-input v-model="newWorkForm.url" placeholder="请输入作品链接" />
      </el-form-item>
      <el-form-item label="作品类型" prop="sort">
        <el-select 
          v-model="newWorkForm.sort"
          placeholder="请选择作品类型"
          style="width: 100%"
        >
          <el-option
            v-for="item in sortList"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancelAdd">取消</el-button>
        <el-button type="primary" @click="submitAddWork" :loading="submitting" color="var(--themeColor2)" class="hover-scale">
          添加
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 删除确认对话框 -->
  <el-dialog
    v-model="dialogDeleteVisible"
    title="删除确认"
    width="400px"
    :close-on-click-modal="false"
    class="custom-dialog"
  >
    <div class="delete-confirm">
      <el-icon class="warning-icon"><WarningFilled /></el-icon>
      <p>确定要删除此作品吗？此操作不可恢复。</p>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogDeleteVisible = false">取消</el-button>
        <el-button type="primary" @click="onDelete" :loading="submitting" color="var(--themeColor2)" class="hover-scale">
          确定删除
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { onMounted, ref, reactive } from "vue";
import store from "@/store";
import { AddWork, DeleteWork, GetWorkList } from "../../../../api/BSideApi";
import { ElMessage } from "element-plus";
import { 
  Plus, 
  Delete, 
  Search, 
  Link, 
  WarningFilled
} from '@element-plus/icons-vue';

export default {
  name: "WorkPanelView",
  components: {
    Plus,
    Delete,
    Search,
    Link,
    WarningFilled
  },
  computed: {
    store() {
      return store;
    },
  },
  setup() {
    // 状态变量
    const show = ref(false);
    const workList = ref([]);
    const workId = ref(null);
    const prompt = ref("");
    const dialogDeleteVisible = ref(false);
    const submitting = ref(false);
    const loading = ref(false);
    
    // 表单引用
    const addFormRef = ref(null);

    // 新增作品表单
    const newWorkForm = reactive({
      name: '',
      url: '',
      sort: ''
    });
    
    // 表单验证规则
    const formRules = {
      name: [
        { required: true, message: '请输入作品名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度应在2到50个字符之间', trigger: 'blur' }
      ],
      url: [
        { required: true, message: '请输入作品链接', trigger: 'blur' },
        { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' }
      ],
      sort: [
        { required: true, message: '请选择作品类型', trigger: 'change' }
      ]
    };
    
    // 作品类型列表
    const sortList = ref([
      "video",
      "spline",
      "other"
    ]);

    // 获取作品列表
    async function fetchWorkList() {
      loading.value = true;
      try {
        const data = await GetWorkList();
        if (data && Array.isArray(data)) {
          workList.value = data.map(item => {
            // 处理日期格式，确保正确显示
            return {
              ...item,
              updatedTime: formatDate(item.updatedTime),
              createdTime: formatDate(item.createdTime)
            };
          });
        } else {
          workList.value = [];
          ElMessage.warning('获取作品列表失败，返回数据格式不正确');
        }
      } catch (error) {
        console.error('获取作品列表错误:', error);
        ElMessage.error('获取作品列表失败，请稍后重试');
        workList.value = [];
      } finally {
        loading.value = false;
      }
    }

    // 格式化日期
    function formatDate(dateValue) {
      if (!dateValue) return '--';
      
      try {
        // 如果是字符串形式的日期
        if (typeof dateValue === 'string') {
          // 如果已经是简短格式，直接返回
          if (dateValue.length <= 10) return dateValue;
          return dateValue.substring(0, 10);
        }
        
        // 如果是Date对象
        if (dateValue instanceof Date) {
          return dateValue.toISOString().substring(0, 10);
        }
        
        // 其他情况尝试转换
        return new Date(dateValue).toISOString().substring(0, 10);
      } catch (error) {
        console.warn('日期格式化错误:', error);
        return '--';
      }
    }

    // 根据作品类型获取标签样式
    function getTagType(sort) {
      switch (sort) {
        case 'video':
          return 'success';
        case 'spline':
          return 'primary';
        default:
          return 'info';
      }
    }

    // 搜索功能
    function search() {
      // 实现搜索逻辑
      console.log('搜索关键词:', prompt.value);
      // 这里可以根据需求实现本地过滤或调用API重新获取数据
    }

    // 取消添加
    function cancelAdd() {
      show.value = false;
      newWorkForm.name = '';
      newWorkForm.url = '';
      newWorkForm.sort = '';
    }

    // 提交添加作品
    async function submitAddWork() {
      if (!addFormRef.value) return;
      
      await addFormRef.value.validate(async (valid) => {
        if (valid) {
          submitting.value = true;
          try {
            const result = await AddWork(newWorkForm.name, newWorkForm.url, newWorkForm.sort);
            
            if (result) {
              ElMessage.success('作品添加成功');
              cancelAdd();
              await fetchWorkList();
            } else {
              ElMessage.error('作品添加失败');
            }
          } catch (error) {
            console.error('添加作品错误:', error);
            ElMessage.error('作品添加失败，请稍后重试');
          } finally {
            submitting.value = false;
          }
        }
      });
    }

    // 处理删除作品
    function handleDeleteWorkInfo(row) {
      workId.value = row.workId;
      dialogDeleteVisible.value = true;
    }

    // 删除作品
    async function onDelete() {
      submitting.value = true;
      try {
        const result = await DeleteWork(workId.value);
        
        if (result === null || result === false) { // 根据API返回值修改判断条件
          ElMessage.success('作品删除成功');
          dialogDeleteVisible.value = false;
          await fetchWorkList();
        } else {
          ElMessage.error('作品删除失败');
        }
      } catch (error) {
        console.error('删除作品错误:', error);
        ElMessage.error('作品删除失败，请稍后重试');
      } finally {
        submitting.value = false;
      }
    }

    onMounted(async () => {
      await fetchWorkList();
    });

    return {
      // 状态
      show,
      workList,
      prompt,
      dialogDeleteVisible,
      loading,
      submitting,
      
      // 表单数据
      newWorkForm,
      sortList,
      formRules,
      
      // 表单引用
      addFormRef,
      
      // 方法
      search,
      cancelAdd,
      submitAddWork,
      handleDeleteWorkInfo,
      onDelete,
      formatDate,
      getTagType
    };
  }
};
</script>

<style lang="scss" scoped>
.body {
  background-color: var(--bgColor2);
  min-height: 100vh;
  padding: 20px;
}

.work-panel-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.panel-card {
  background: var(--bgColor1);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.hover-scale {
  transition: transform 0.3s ease;
  &:hover {
    transform: scale(1.05);
  }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  
  h2 {
    margin: 0;
    color: var(--textColor1);
    font-size: 1.5rem;
    letter-spacing: 1px;
    font-weight: 600;
  }
  
  .add-work-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    background: var(--themeColor2);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      opacity: 0.9;
    }
  }
}

.search-container {
  margin-bottom: 25px;
  
  .search-input {
    max-width: 350px;
    
    :deep(.el-input__wrapper) {
      background-color: var(--bgColor2);
      box-shadow: 0 0 0 1px var(--borderColor);
      border-radius: 8px;
      transition: all 0.3s ease;
      
      &:focus-within {
        box-shadow: 0 0 0 1px var(--themeColor2);
      }
    }
    
    :deep(.el-input__inner) {
      color: var(--textColor1);
    }
    
    :deep(.el-input__prefix-inner) {
      color: var(--textColor3);
    }
  }
}

.table-container {
  flex: 1;
  overflow: auto;
  border-radius: 12px;
  border: 1px solid var(--borderColor);
  margin-bottom: 20px;
  background: var(--bgColor1);
  
  :deep(.el-table) {
    background-color: var(--bgColor1);
    color: var(--textColor1);
    
    .el-table__header-wrapper th {
      color: var(--textColor1);
      font-weight: 600;
      background: var(--bgColor2) !important;
      padding: 12px 0;
    }
    
    .el-table__body-wrapper {
      background-color: var(--bgColor1);
    }
    
    .el-table__row {
      transition: background-color 0.3s ease;
      
      &:hover > td {
        background-color: var(--bgColor2) !important;
      }
    }
    
    .el-table__empty-block {
      background-color: var(--bgColor1);
      
      .el-table__empty-text {
        color: var(--textColor3);
      }
    }
    
    .el-button--primary.is-link {
      color: var(--themeColor2);
    }
  }
  
  .work-name {
    font-weight: 500;
    color: var(--textColor1);
  }
  
  .work-link {
    display: flex;
    align-items: center;
    color: var(--themeColor2);
    
    .link-icon {
      margin-left: 5px;
      font-size: 14px;
    }
  }
  
  .action-btn {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 8px 15px;
    transition: all 0.3s ease;
    border-radius: 6px;
    margin: 0 auto;
    
    &:hover {
      transform: translateY(-2px);
      opacity: 0.9;
    }
  }
}

.empty-placeholder {
  padding: 30px 0;
  text-align: center;
  color: var(--textColor3);
}

.delete-confirm {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 30px 0;
  padding: 20px;
  background: rgba(244, 67, 54, 0.05);
  border-radius: 8px;
  
  .warning-icon {
    font-size: 32px;
    color: #f56c6c;
  }
  
  p {
    margin: 0;
    color: var(--textColor1);
    font-size: 16px;
  }
}

:deep(.el-tag) {
  background-color: var(--bgColor2);
  border-color: var(--borderColor);
  transition: all 0.3s ease;
  border-radius: 8px;
  padding: 4px 10px;
  
  &.el-tag--success {
    color: #67c23a;
    border-color: #e1f3d8;
  }
  
  &.el-tag--primary {
    color: var(--themeColor2);
    border-color: var(--borderColor);
  }
}

.custom-dialog {
  :deep(.el-dialog) {
    background-color: var(--bgColor2);
    border-radius: 16px;
    overflow: hidden;
    
    .el-dialog__header {
      margin: 0;
      padding: 20px;
      border-bottom: 1px solid var(--borderColor);
      background: linear-gradient(135deg, var(--themeColor2), #4a90e2);
      
      .el-dialog__title {
        color: white;
        font-weight: 600;
      }
      
      .el-dialog__close {
        color: white;
      }
    }
    
    .el-dialog__body {
      color: var(--textColor1);
      padding: 30px;
    }
    
    .el-dialog__footer {
      border-top: 1px solid var(--borderColor);
      padding: 20px;
    }
  }
  
  :deep(.el-form-item__label) {
    color: var(--textColor1);
    font-weight: 500;
    font-size: 14px;
  }
  
  :deep(.el-input__wrapper),
  :deep(.el-select .el-input__wrapper) {
    background-color: var(--bgColor1);
    box-shadow: 0 0 0 1px var(--borderColor);
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:focus-within {
      box-shadow: 0 0 0 1px var(--themeColor2);
    }
    
    .el-input__inner {
      color: var(--textColor1);
    }
  }
}

:deep(.el-select-dropdown) {
  background-color: var(--bgColor2);
  border: 1px solid var(--borderColor);
  border-radius: 8px;
  
  .el-select-dropdown__item {
    color: var(--textColor1);
    
    &.selected {
      color: var(--themeColor2);
      background-color: var(--bgColor1);
    }
    
    &:hover {
      background-color: var(--bgColor1);
    }
  }
}

:deep(.el-button) {
  border-radius: 8px;
  
  &.el-button--primary {
    background-color: var(--themeColor2);
    border-color: var(--themeColor2);
  }
}

@media screen and (max-width: 767px) {
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    
    .add-work-btn {
      width: 100%;
      justify-content: center;
    }
  }
  
  .search-container .search-input {
    max-width: 100%;
  }
  
  .table-container {
    overflow-x: auto;
  }
  
  :deep(.el-table) {
    min-width: 600px;
  }
}
</style>