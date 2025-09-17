<template>
  <div class="success-container">
    <div class="success-content">
      <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
      <h1>支付成功</h1>
      <p class="countdown">{{ countdown }}秒后自动跳转...</p>
      <!-- <el-button type="primary" @click="goToUserView">立即跳转</el-button> -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { CircleCheckFilled } from '@element-plus/icons-vue'

const router = useRouter()
const countdown = ref(5)
let timer = null

const goToUserView = () => {
  router.push('/user_view')
}

onMounted(() => {
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      goToUserView()
    }
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.success-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bgColor2);
  animation: fadeIn 0.5s ease-in-out;
}

.success-content {
  text-align: center;
  padding: 40px;
  background-color: var(--bgColor1);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  
  h1 {
    color: var(--themeColor1);
    margin: 20px 0;
    font-size: 24px;
  }
}

.success-icon {
  font-size: 80px;
  color: #67c23a;
  animation: scaleIn 0.5s ease-in-out;
}

.countdown {
  color: #909399;
  margin: 20px 0;
  font-size: 14px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}
</style>
