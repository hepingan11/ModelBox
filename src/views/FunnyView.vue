<template>
  <div class="container">
    <el-button @click="drawer = true" color="var(--themeColor1)" class="list-button">
      <el-icon><Menu /></el-icon> 列表
    </el-button>
    
    <!-- 根据当前作品类型展示不同内容 -->
    <div class="content-container">
      <!-- 如果是spline类型，展示内嵌网页 -->
      <iframe 
        v-if="currentType === 'spline' || currentType === 'other'" 
        frameborder="0" 
        :src="url" 
        class="iframe-content"
      ></iframe>
      
      <!-- 如果是video类型，展示视频播放器 -->
      <div v-else-if="currentType === 'video'" class="video-container">
        <video 
          controls 
          autoplay 
          class="video-player"
          :src="url"
        >
          您的浏览器不支持HTML5视频
        </video>
      </div>
      
      <!-- 如果没有内容或类型不支持 -->
      <div v-else class="empty-content">
        <el-empty description="请从左侧列表选择作品" />
      </div>
    </div>
  </div>
  
  <el-drawer 
    v-model="drawer" 
    title="作品列表" 
    class="work-drawer" 
    :with-header="true"
    :size="400"
  >
    <div class="drawer-content">
      <div 
        v-for="item in urlList"
        :key="item.workId"
        class="work-item"
        :class="{ active: item.url === url }"
        @click="chooseWork(item)"
      >
        <div class="work-info">
          <span class="work-name">{{ item.name }}</span>
          <el-tag size="small" :type="getTagType(item.sort)" class="work-type">
            {{ item.sort }}
          </el-tag>
        </div>
        <el-button 
          @click.stop="chooseWork(item)" 
          color="var(--themeColor1)" 
          size="small"
          class="select-btn hover-scale"
        >
          选择
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import { onMounted, ref } from "vue";
import { GetWorkList } from "../../api/BSideApi";
import { ElNotification } from "element-plus";
import { Menu } from '@element-plus/icons-vue';

export default {
  name: 'FunnyView',
  components: {
    Menu
  },
  setup() {
    const drawer = ref(false);
    const urlList = ref([]);
    const url = ref('');
    const currentType = ref('');
    const currentItem = ref(null);

    onMounted(async () => {
      try {
        urlList.value = await GetWorkList();
        if (urlList.value && urlList.value.length > 0) {
          chooseWork(urlList.value[0]);
        }
        
        ElNotification({
          title: "部分作品推荐使用VPN，不然加载可能有点慢",
          type: "info",
          duration: 5000
        });
      } catch (error) {
        console.error('获取作品列表失败:', error);
        ElNotification({
          title: "获取作品列表失败",
          message: "请检查网络连接并刷新页面",
          type: "error"
        });
      }
    });

    // 获取标签类型
    function getTagType(sort) {
      switch (sort) {
        case 'video':
          return 'danger';
        case 'spline':
          return 'primary';
        default:
          return 'info';
      }
    }

    // 选择作品
    function chooseWork(item) {
      if (!item) return;
      
      url.value = item.url;
      currentType.value = item.sort;
      currentItem.value = item;
      drawer.value = false;

      // 如果类型改变了，发送通知
      ElNotification({
        title: `正在加载: ${item.name}`,
        type: getNotificationType(item.sort),
        duration: 3000
      });
    }

    // 获取通知类型
    function getNotificationType(sort) {
      switch (sort) {
        case 'video':
          return 'success';
        case 'spline':
          return 'info';
        default:
          return 'warning';
      }
    }

    return {
      drawer,
      urlList,
      url,
      currentType,
      chooseWork,
      getTagType
    };
  }
};
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100%;
  position: relative;
  background-color: var(--bgColor1);
  display: flex;
  flex-direction: column;
}

.list-button {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 5px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  }
}

.content-container {
  flex: 1;
  width: 100%;
  height: 100%;
  position: relative;
}

.iframe-content {
  width: 100%;
  height: 100%;
  border: none;
}

.video-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #000;
}

.video-player {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
}

.empty-content {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bgColor2);
}

.work-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 20px;
    border-bottom: 1px solid var(--borderColor);
    background: linear-gradient(135deg, var(--themeColor1), #4a90e2);
    
    .el-drawer__title {
      color: white;
      font-weight: 600;
      font-size: 18px;
    }
    
    .el-drawer__close-btn {
      color: white;
      
      &:hover {
        transform: scale(1.1);
      }
    }
  }
  
  :deep(.el-drawer__body) {
    padding: 0;
    background-color: var(--bgColor1);
  }
}

.drawer-content {
  padding: 10px;
  overflow-y: auto;
  height: 100%;
}

.work-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  margin-bottom: 10px;
  background-color: var(--bgColor2);
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
  
  &.active {
    border-left: 4px solid var(--themeColor1);
    background-color: rgba(var(--themeColor1-rgb), 0.1);
  }
}

.work-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.work-name {
  font-weight: 500;
  color: var(--textColor1);
}

.work-type {
  align-self: flex-start;
}

.select-btn {
  padding: 6px 12px;
  transition: all 0.3s ease;
}

.hover-scale {
  &:hover {
    transform: scale(1.05);
  }
}

:deep(.el-tag) {
  border-radius: 4px;
}
</style>