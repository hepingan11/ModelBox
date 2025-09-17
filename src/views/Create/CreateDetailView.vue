<template>
  <ViewState v-if="load" LoadText="正在生成，请稍后..." />
  <ViewState
    v-else-if="error"
    @ClickTheButton="back"
    Type="error"
    ErrorText="AI服务调用失败，正在紧急处理，请稍后使用。"
    IsShowBottom
    ButtonText="返回"
  />
  <div v-else class="body" ref="scrollRef">
    <div class="container">
      <div class="title">{{ item }}</div>
      <div class="result">
        <div class="completeText">
          <v-md-editor :model-value="content" mode="preview"></v-md-editor>
        </div>
      </div>
      <button @click="back" class="determineTheBuild" type="primary">
        返回上一页
      </button>
    </div>
  </div>
</template>

<script>
import { useRouter } from "vue-router";
import { onMounted, ref, nextTick } from "vue";
import ViewState from "@/components/ViewState.vue";
import router from "@/router";
import { GetUserInfo } from "../../../api/BSideApi";
import store from "@/store";
import { ElNotification } from "element-plus";

export default {
  name: "CreateDetailView",
  methods: {
    router() {
      return router;
    },
  },
  components: { ViewState },
  setup() {
    let router = useRouter();
    let query = router.currentRoute.value.query;
    let role = ref(
      query.role ? JSON.parse(decodeURIComponent(query.role)) : false
    );
    let item = ref(JSON.parse(decodeURIComponent(query.item)));
    let load = ref(false);
    let error = ref(false);
    let content = ref("");
    let scrollRef = ref(null);
    let messages = ref([]);

    // TODO 系统角色
    if (role.value)
      messages.value.push({
        role: "system",
        content: role.value,
      });

    // TODO 我
    messages.value.push({
      role: "user",
      content: item.value,
    });

    onMounted(() => {
      sendRequest();
    });

    // TODO 滚动到底部
    function scrollToTheBottom() {
      nextTick(() => {
        setTimeout(() => {
          if (scrollRef.value && scrollRef.value.scrollHeight) {
            scrollRef.value.scrollTop = scrollRef.value.scrollHeight;
          }
        }, 20);
      });
    }

    async function sendRequest() {
      try {
        load.value = true;
        const baseURL = process.env.VUE_APP_BASE_API;

        // 使用FormData发送请求
        const formData = new FormData();
        
        // 构建消息内容，将messages数组转换为对话格式
        const messageContent = messages.value.map(msg => {
          if (msg.role === 'system') {
            return `系统角色: ${msg.content}`;
          } else if (msg.role === 'user') {
            return `用户: ${msg.content}`;
          }
          return `${msg.role}: ${msg.content}`;
        }).join('\n\n');
        formData.append('message', messageContent);
        formData.append('chatId', Date.now().toString()); // 生成一个chatId
        formData.append('model', 'GLM'); // 使用默认模型
        formData.append('isRag', 'false');
        formData.append('isMcp', 'false');
        formData.append('role', ''); // 空角色
        formData.append('mcpList', '[]'); // 空MCP列表

        const response = await fetch(baseURL + '/chat/chat2', {
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
            
            content.value = receivedText;
            scrollToTheBottom();
          }
          
          load.value = false;
          getUser();
        }
      } catch (error) {
        console.error('请求失败:', error);
        load.value = false;
        error.value = true;
        
        ElNotification({
          title: "请求失败",
          message: "AI服务调用失败，请稍后重试",
          type: "error",
        });
      }
    }

    async function getUser() {
      try {
        let res = await GetUserInfo();

        store.commit("setUserinfo", res);
      } catch (e) {
        console.log(e);
      }
    }

    function back() {
      router.go(-1);
    }

    return {
      load,
      error,
      content,
      item,
      back,
      scrollRef,
    };
  },
};
</script>

<style scoped>
.body {
  scroll-behavior: smooth;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  flex-direction: column;
  flex: 1;
  align-items: center;
  padding: 0 20px 100px;
  display: flex;
  overflow: auto;
  background-color: var(--bgColor2);
}

@keyframes explainAnimation {
  from {
    transform: scale(0);
  }

  to {
    transform: scale(1);
  }
}

.container {
  max-width: 800px;
  width: 100%;
  box-sizing: border-box;
  padding: 0 20px 100px;
  animation: explainAnimation 0.3s;
}

.title {
  color: var(--textColor1);
  font-size: 20px;
  font-weight: 500;
  margin-top: 60px;
  margin-bottom: 30px;
}

::v-deep(.vuepress-markdown-body) {
  padding: 0 0 0 16px;
  color: var(--textColor1);
  background-color: var(--bgColor1);
}

.result {
  min-height: 60px;
  max-width: 800px;
  width: 100%;
  background: var(--bgColor1);
  border-radius: 8px;
  flex-direction: column;
  display: flex;
  position: relative;
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
}

.completeText {
  box-sizing: border-box;
  width: 100%;
  min-height: 28px;
  white-space: pre-wrap;
  flex: 1;
  padding: 16px 20px;
  font-size: 16px;
  line-height: 28px;
  position: relative;
}

:deep(.mdPreview > .vuepress-markdown-body) {
  padding: 0;
  color: var(--textColor1);
  background-color: var(--bgColor1);
}

@media only screen and (max-width: 767px) {
  .container {
    padding: 0;
  }
}

.determineTheBuild {
  font-size: 15px;
  background-color: var(--el-text-color-primary);
  color: #fff;
  border-radius: 100px;
  height: 40px;
  width: 300px;
  max-width: 100%;
  line-height: 40px;
  border: 0;
  display: table;
  margin: 50px auto 0 auto;
  cursor: pointer;
}

.determineTheBuild:hover {
  background-color: #56575a;
}
</style>
