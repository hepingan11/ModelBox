<template>
  <div class="s_container">
    <el-scrollbar height="100%">
      <el-form :inline="true" class="demo-form-inline" label-position="left">
        
        <el-form-item label="GPT对话密钥" label-width="200px">
          <el-input placeholder="请设置密钥" clearable v-model="form.openKey" />
        </el-form-item>
        
        <el-form-item label="DeepSeek对话密钥" label-width="200px">
          <el-input
            placeholder="请设置DeepSeek密钥"
            clearable
            v-model="form.deepseekKey"
          />
        </el-form-item>
        <el-form-item label="智谱GLM对话密钥" label-width="200px">
          <el-input
            placeholder="请设置智谱GLM密钥"
            clearable
            v-model="form.glmKey"
          />
        </el-form-item>
        <el-form-item label="Claude对话密钥" label-width="200px">
          <el-input
            placeholder="请设置Claude密钥"
            clearable
            v-model="form.claudeKey"
          />
        </el-form-item>
        <el-form-item label="通义千问对话密钥" label-width="200px">
          <el-input
            placeholder="请设置通义千问密钥"
            clearable
            v-model="form.tongyiKey"
          />
        </el-form-item>

        <el-form-item label="豆包对话密钥" label-width="200px">
          <el-input
            placeholder="请设置豆包密钥"
            clearable
            v-model="form.doubaoKey"
          />
        </el-form-item>

        <el-form-item label="链接顶图" label-width="200px">
          <el-input
              placeholder="请设置图片链接"
              clearable
              v-model="form.linkTopImg"
          />
        </el-form-item>
        <el-form-item label="ai对话消耗次数" label-width="200px">
          <el-input
            placeholder="请设置消耗次数"
            clearable
            v-model="form.gptFrequency"
          />
        </el-form-item>
        
        <el-form-item label="新用户奖励次数" label-width="200px">
          <el-input
            placeholder="请设置奖励次数"
            clearable
            v-model="form.incentiveFrequency"
          />
        </el-form-item>
        
        <el-form-item label="签到奖励次数" label-width="200px">
          <el-input
            placeholder="请设置奖励次数"
            clearable
            v-model="form.signInFrequency"
          />
        </el-form-item>
        <el-form-item label="文生图消耗次数" label-width="200px">
          <el-input
            placeholder="请设置消耗次数"
            clearable
            v-model="form.gptTextImageFrequency"
          />
        </el-form-item>
      </el-form>

      <div class="btn-container">
        <el-button
          color="var(--themeColor2)"
          style="width: 200px"
          @click="onSubmit"
          >重载数据</el-button
        >
      </div>
    </el-scrollbar>
  </div>
</template>

<script>
import store from "@/store";
import { onMounted, ref } from "vue";
import { ElLoading, ElNotification } from "element-plus";
import { GetServerConfig, PutServerConfig } from "../../../../api/BSideApi";

export default {
  name: "ServerPanelView",
  computed: {
    store() {
      return store;
    },
  },
  setup() {
    const form = ref({
      openKey: "",
      deepseekKey: "",
      glmKey: "",
      tongyiKey: "",
      claudeKey: "",
      incentiveFrequency: undefined,
      signInFrequency: undefined,
      gptFrequency: undefined,
      gptTextImageFrequency: undefined,
      linkTopImg: "",
      doubaoKey: "",
    });

    const options = ref([
      {
        value: '1',
        label: '开'
      },
      {
        value: '0',
        label: '关'
      }
    ])

    const options2 = ref([
      {
        value: 'hadoop',
        label: 'Hadoop存储'
      },
      {
        value: 'mysql',
        label: 'Mysql'
      }
    ])
    onMounted(() => {
      if (store.getters.userinfo && store.getters.userinfo.type === "ADMIN") {
        getServerConfig();
      }
    });

    async function getServerConfig() {
      let promise = await GetServerConfig();
      if (promise) {
        form.value = promise;
      }
    }

    async function onSubmit() {
      let value = form.value;
      for (let key in value) {
        if (!value[key]) {
          ElNotification({
            title: "错误",
            message: key + "不能为空(没有的话就写个1)",
            type: "error",
          });
          return;
        }
      }

      try {
        ElLoading.service({
          fullscreen: true,
          text: "正在重载数据...",
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.7)",
        });
        await PutServerConfig(value);
        // 关闭加载图标
        ElLoading.service().close();
        // 弹出退出登录成功提示框
        ElNotification({
          message: "重载数据成功",
          type: "success",
        });
      } catch (e) {
        ElNotification({
          title: "错误",
          message: e,
          type: "error",
        });
      }
    }

    return {
      onSubmit,
      form,
      options,
      options2
    };
  },
};
</script>

<style scoped>
@keyframes explainAnimation {
  from {
    transform: scale(0);
  }

  to {
    transform: scale(1);
  }
}

::v-deep(
    .el-table--enable-row-hover .el-table__body tr:hover td.el-table__cell
  ) {
  background: none !important;
}

.btn-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-right: 100px;
  padding-top: 70px;
}

::v-deep(
    .el-pagination .is-background .el-pager li:not(.is-disabled).is-active
  ) {
  background-color: rgb(125, 128, 255);
}

::v-deep(.el-table-fixed-column--right) {
  background: var(--bgColor1) !important;
}

::v-deep(
    .el-table--enable-row-hover .el-table__body tr:hover td.el-table__cell
  ) {
  background: none;
}

::v-deep(.el-input__inner) {
  background: var(--bgColor2);
  font-weight: 400;
  color: var(--textColor2);
}

::v-deep(.el-input__wrapper) {
  background: var(--bgColor2);
  box-shadow: none;
}
</style>
