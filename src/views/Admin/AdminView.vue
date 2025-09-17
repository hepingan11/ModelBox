<template>
  <div class="body">
    <div class="container">
      <el-tabs
        v-model="activeName"
        v-if="store.getters.userinfo && store.getters.userinfo.type === 'ADMIN'"
        @tab-click="handleTabClick"
        class="admin-tabs"
      >
        <el-tab-pane label="用户管理" name="first">
          <component v-if="activeComponent === 'first'" :is="UserPanelView" />
        </el-tab-pane>
        <el-tab-pane label="服务器配置" name="second">
          <component v-if="activeComponent === 'second'" :is="ServerPanelView" />
        </el-tab-pane>
        <el-tab-pane label="兑换管理" name="third">
          <component v-if="activeComponent === 'third'" :is="RedemptionCodeView" />
        </el-tab-pane>
        <el-tab-pane label="商品管理" name="four">
          <component v-if="activeComponent === 'four'" :is="ProductView" />
        </el-tab-pane>
        <el-tab-pane label="订单数据" name="fifth">
          <component v-if="activeComponent === 'fifth'" :is="OrdersDataView" />
        </el-tab-pane>
        <el-tab-pane label="源码管理" name="seventh">
          <component v-if="activeComponent === 'seventh'" :is="CodePanelView" />
        </el-tab-pane>
        <el-tab-pane label="外链管理" name="eighth">
          <component v-if="activeComponent === 'eighth'" :is="LinkPanelView" />
        </el-tab-pane>
        <el-tab-pane label="作品管理" name="nineth">
          <component v-if="activeComponent === 'nineth'" :is="WorkPanelView" />
        </el-tab-pane>
      </el-tabs>
      <div v-else class="no_data">
        <ViewState class="state" Type="error" ErrorText="当前页面不见了" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, defineAsyncComponent, onMounted } from "vue";
import store from "@/store";
import ViewState from "@/components/ViewState.vue";
import { ElNotification } from "element-plus";

export default {
  name: "PromptList",
  components: {
    ViewState,
  },
  computed: {
    store() {
      return store;
    },
  },
  setup() {
    onMounted(() => {
      ElNotification({
        title: "手机浏览该网站可能会影响体验，建议使用电脑或平板~",
        type: "info",
      });
    })
    const activeName = ref("first");
    const activeComponent = ref("first");

    // 懒加载组件
    const UserPanelView = defineAsyncComponent(() => 
      import("@/views/Admin/components/UserPanelView.vue")
    );
    const ServerPanelView = defineAsyncComponent(() => 
      import("@/views/Admin/components/ServerPanelView.vue")
    );
    const RedemptionCodeView = defineAsyncComponent(() => 
      import("@/views/Admin/components/RedemptionCodeView.vue")
    );
    const ProductView = defineAsyncComponent(() => 
      import("@/views/Admin/components/ProductView.vue")
    );
    const OrdersDataView = defineAsyncComponent(() => 
      import("@/views/Admin/components/OrdersDataView.vue")
    );
    const CodePanelView = defineAsyncComponent(() => 
      import("@/views/Admin/components/CodePanelView.vue")
    );
    const LinkPanelView = defineAsyncComponent(() => 
      import("@/views/Admin/components/LinkPanelView.vue")
    );
    const WorkPanelView = defineAsyncComponent(() => 
      import("@/views/Admin/components/WorkPanelView.vue")
    );

    // 处理标签页切换
    const handleTabClick = (tab) => {
      activeComponent.value = tab.props.name;
    };

    return {
      activeName,
      activeComponent,
      UserPanelView,
      ServerPanelView,
      RedemptionCodeView,
      ProductView,
      OrdersDataView,
      CodePanelView,
      LinkPanelView,
      WorkPanelView,
      handleTabClick,
    };
  },
};
</script>

<style lang="scss" scoped>
:deep(.s_container) {
  overflow: auto;
  height: 100%;
  .el-scrollbar__view {
    transition: all 0.3s;
  }

  @media screen and (min-height: 756px) {
    .el-scrollbar__view {
      padding: 40px;
    }
  }

  @media screen and (max-height: 756px) {
    .el-scrollbar__view {
      padding: 10px;
    }
  }
}

:deep(.u_container) {
  display: flex;
  flex-direction: column;
  .head_model {
    min-height: 110px;
  }
  .el-table tr {
    background: transparent;
  }
}

:deep(.container) {
  .el-table__cell {
    border-bottom: 1px solid var(--borderColor);
  }
  .hover-row {
    .el-table__cell {
      background-color: var(--borderColor);
      color: var(--textColor2);
    }
  }

  .el-table__inner-wrapper::before {
    background-color: var(--borderColor);
  }

  .el-tabs {
    height: 100%;
    display: flex;
    flex-direction: column;

    .el-tabs__content {
      height: 100%;

      .el-tab-pane {
        height: 100%;
        .u_container {
          height: 100%;

          .el-table {
            > .el-table__inner-wrapper {
              height: 100% !important;
            }
          }
        }
      }
    }
  }
}

@keyframes explainAnimation {
  from {
    transform: scale(0);
  }

  to {
    transform: scale(1);
  }
}

:deep(.el-tabs__nav-wrap::after) {
  background: var(--bgColor1);
}

.container {
  animation: explainAnimation 0.3s;
  max-width: 1100px;
  width: 100%;
  box-sizing: border-box;
  padding: 10px 20px 20px;
  margin: 30px 0px;
  height: 90%;
  background-color: var(--bgColor1);
  border-radius: 8px;
}

.body {
  scroll-behavior: smooth;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  flex-direction: column;
  flex: 1;
  align-items: center;
  padding: 0 20px 0px;
  display: flex;
  overflow: auto;
  background-color: var(--bgColor2);
}

::v-deep(.el-tabs__item.is-active) {
  color: var(--textColor1);
}

::v-deep(.el-tabs__item:hover) {
  color: #959595;
}

::v-deep(.el-tabs__active-bar) {
  background-color: var(--themeColor1);
}

::v-deep(.el-tabs__item) {
  color: #626262;
}
.no_data {
  height: 540px;
  margin-top: 10px;
}

/* 标签页滑动样式 */
.admin-tabs {
  :deep(.el-tabs__header) {
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
    /* 隐藏滚动条但保持滚动功能 */
    scrollbar-width: none; /* Firefox */
    -ms-overflow-style: none; /* Internet Explorer and Edge */
    
    &::-webkit-scrollbar {
      display: none; /* Chrome, Safari, Opera*/
    }
    
    /* 增加滑动体验 */
    scroll-behavior: smooth;
    -webkit-overflow-scrolling: touch;
  }
  
  :deep(.el-tabs__nav-wrap) {
    padding-bottom: 0;
    
    &::after {
      /* 隐藏底部阴影线 */
      height: 1px;
      background-color: var(--borderColor);
      opacity: 0.2;
    }
  }
  
  :deep(.el-tabs__nav) {
    /* 确保标签不会换行 */
    white-space: nowrap;
    display: flex;
    padding-bottom: 2px;
    margin-bottom: -2px;
  }
  
  :deep(.el-tabs__item) {
    /* 标签之间的间距和样式 */
    padding: 0 20px;
    height: 40px;
    line-height: 40px;
    transition: all 0.3s ease;
    flex-shrink: 0;
    
    &.is-active {
      color: var(--themeColor1);
      font-weight: bold;
    }
    
    &:hover {
      color: var(--themeColor1);
      opacity: 0.8;
    }
  }
  
  /* 自定义小箭头指示器，在有滚动时显示 */
  &::before, &::after {
    content: '';
    position: absolute;
    top: 50%;
    width: 24px;
    height: 24px;
    background-color: var(--bgColor1);
    border-radius: 50%;
    transform: translateY(-50%);
    z-index: 1;
    opacity: 0;
    transition: opacity 0.3s;
    pointer-events: none;
  }
  
  &:hover {
    &::before, &::after {
      opacity: 0.8;
    }
  }
}

/* 主体内容区域滚动 */
.container {
  height: 90%;
  display: flex;
  flex-direction: column;
  
  :deep(.el-tabs) {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
    
    .el-tabs__content {
      flex: 1;
      overflow-y: auto;
      padding: 20px 0;
      
      /* 平滑滚动 */
      scroll-behavior: smooth;
      -webkit-overflow-scrolling: touch;
    }
  }
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .admin-tabs {
    :deep(.el-tabs__item) {
      padding: 0 15px;
      font-size: 14px;
    }
  }
}
</style>
