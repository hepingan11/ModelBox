<template>
  <div class="generation-container">
    <h1 class="page-title">工具箱</h1>
    
    <div class="tools-grid">
      <div class="tool-card" @click="navigateTo('/toolsMoudle')">
        <div class="tool-icon">📊</div>
        <h2>毕设功能模块图</h2>
        <p>生成毕业设计的功能模块结构图</p>
      </div>
      
      <div class="tool-card" @click="navigateTo('/toolsIllustration')">
        <div class="tool-icon">🎨</div>
        <h2>插图工具</h2>
        <p>Tools Illustration</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';

const router = useRouter();

const navigateTo = (route) => {
  router.push({ path: route });
};



onMounted(() => {
    
});

</script>

<style lang="scss" scoped>
.generation-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: var(--bgColor1);
  color: var(--textColor1);
}

.page-title {
  text-align: center;
  margin-bottom: 40px;
  color: var(--textColor1);
  font-weight: 600;
  letter-spacing: 2px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
}

.tool-card {
  background-color: var(--bgColor2);
  border-radius: 10px;
  box-shadow: 0 4px 12px var(--bgboxShadowColor1);
  padding: 30px;
  transition: all 0.3s cubic-bezier(0.65, 0.05, 0.36, 1);
  cursor: pointer;
  text-align: center;
  border: 1px solid var(--borderColor);
}

.tool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px var(--bgboxShadowColor2);
  border-color: var(--themeColor1);
}

.tool-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.tool-card h2 {
  margin: 10px 0;
  color: var(--textColor1);
  font-weight: 500;
}

.tool-card p {
  color: var(--textColor3);
  margin-top: 10px;
  font-size: 14px;
}

@media only screen and (max-width: 767px) {
  .generation-container {
    padding: 15px;
  }
  
  .tools-grid {
    grid-template-columns: 1fr;
  }
  
  .page-title {
    margin-bottom: 25px;
  }
}
</style>
