<template>
  <div class="about">
    <!-- 粒子背景 -->
    
    <!-- 光晕效果 -->
    <div class="glow-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <div class="container">
      <div class="main-content scroll-animate" data-animation="fade-up">
        <!-- 左边内容区域 -->
        <div class="content">
          <div class="title-section scroll-animate" data-animation="fade-right">
            <div @click="navigateToProIndex" style="cursor: pointer; display: inline-block; z-index: 1000; position: relative;">
  <img src="@/assets/logo.png" alt="logo" class="logo" style="z-index: 1000; position: relative;"/>
</div>
            <h1 data-text="Pass Assistant" class="glitch-title">Pass Assistant</h1>
            <div class="typing-animation">
              <h2>{{ typingText }}</h2>
              <span class="cursor">|</span>
            </div>
          </div>
          
          <div class="info-cards">
            <div class="info-card developer-card scroll-animate" data-animation="slide-left" @click="navigateToProfile">
              <div class="card-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="card-content">
                <h3>开发者：何平安</h3>
                <p>点击查看个人简介</p>
              </div>
              <div class="card-glow"></div>
            </div>
            
            <div class="info-card motto-card scroll-animate" data-animation="slide-left" data-delay="0.2">
              <div class="card-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="card-content">
                <h3>核心理念</h3>
                <p>兴趣·毅力·创新·前沿</p>
              </div>
              <div class="card-glow"></div>
            </div>
            
            <div @click="navigateToGitee" class="info-card link-card scroll-animate" data-animation="slide-left" data-delay="0.4">
              <div class="card-icon">
                <el-icon><Link /></el-icon>
              </div>
              <div class="card-content" >
                <h3>开源地址</h3>
                <p>备案信息：渝ICP备2023004248号</p>
              </div>
              <div class="card-glow"></div>
            </div>
          </div>
        </div>

        <!-- 右边动画区域 -->
        <div class="animation-section scroll-animate" data-animation="fade-left">
          <div
              ref="flyContainer"
              class="lottie-animation floating"
              @mouseenter="pauseAnimation('fly')"
              @mouseleave="playAnimation('fly')"
          ></div>
          
          <!-- 预留图片位置 -->
          <div class="image-placeholder">
            <div class="placeholder-content">
              <div ref="carContainer" class="lottie-animation floating"></div>
            </div>
          </div>
        </div>
      </div>

      <div class="main-content contact-section scroll-animate" data-animation="fade-up">
        <div class="animation-section scroll-animate" data-animation="scale-in">
          <div
              ref="connectContainer"
              class="lottie-animation floating"
              @mouseenter="pauseAnimation('connect')"
              @mouseleave="playAnimation('connect')"
          ></div>
          
          <!-- 联系图片 -->
          <div class="contact-image">
            <img src="https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-08-24%20044030.png" alt="联系我们" />
          </div>
        </div>

        <div class="content">
          <div class="contact-header scroll-animate" data-animation="fade-down">
            <h2 class="section-title">
              <span class="title-text">想合作？想联系我？</span>
              <span class="title-subtitle">发现BUG？或者有什么新奇点子...</span>
            </h2>
          </div>
          
          <div class="contact-form">
            <div class="form-group scroll-animate" data-animation="slide-right" data-delay="0.1">
              <label class="form-label">
                <el-icon><User /></el-icon>
                怎么称呼您
              </label>
              <el-input 
                v-model="data.name" 
                class="modern-input"
                placeholder="请输入您的姓名"
                clearable
              ></el-input>
            </div>
            
            <div class="form-group scroll-animate" data-animation="slide-right" data-delay="0.2">
              <label class="form-label">
                <el-icon><Message /></el-icon>
                您的联系方式(最好是邮箱啦~)
              </label>
              <el-input 
                v-model="data.email" 
                class="modern-input"
                placeholder="example@email.com"
                clearable
              ></el-input>
            </div>
            
            <div class="form-group scroll-animate" data-animation="slide-right" data-delay="0.3">
              <label class="form-label">
                <el-icon><ChatDotSquare /></el-icon>
                有何贵干
              </label>
              <el-input 
                v-model="data.content" 
                class="modern-input"
                type="textarea"
                :rows="4"
                placeholder="请描述您的想法或需求..."
                clearable
              ></el-input>
            </div>
            
            <div class="form-submit scroll-animate" data-animation="bounce-in" data-delay="0.4">
              <el-button
                  class="cyber-button"
                  :disabled="disabled"
                  :loading="buttonText === '正在发送中'"
                  @click="submit">
                <span>{{ buttonText }}</span>
                <div class="button-glow"></div>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="main-content changelog-section scroll-animate" data-animation="fade-up">
        <div class="content">
          <div class="changelog-header scroll-animate" data-animation="fade-down">
            <h2 class="section-title">
              <span class="title-icon">
                <el-icon><ChatLineRound /></el-icon>
              </span>
              <span class="title-text">更新日志</span>
            </h2>
          </div>
          
          <div class="changelog-timeline">
            <div class="timeline-item scroll-animate" v-for="(log, index) in updateLogs" :key="index" :class="{ 'major-update': log.major }" data-animation="slide-up" :data-delay="(index * 0.1).toString()">
              <div class="timeline-dot">
                <div class="dot-inner"></div>
              </div>
              <div class="timeline-content">
                <div class="version-tag">{{ log.version }}</div>
                <div class="update-description">{{ log.description }}</div>
                <div class="update-date">{{ log.date }}</div>
              </div>
              <div class="timeline-glow"></div>
            </div>
          </div>
        </div>
        
        <div class="animation-section scroll-animate" data-animation="scale-in" data-delay="0.3">
          <div
              ref="introduceContainer"
              class="lottie-animation floating"
              @mouseenter="pauseAnimation('introduce')"
              @mouseleave="playAnimation('introduce')"
          ></div>
          
          <!-- 预留图片位置 -->
          <div class="image-placeholder">
            <div class="placeholder-content">
              <div ref="webContainer" class="lottie-animation floating"></div>
            </div>
          </div>

          <div class="image-placeholder">
            <div class="placeholder-content">
              <div ref="localtionContainer" class="lottie-animation floating"></div>
            </div>
          </div>

          <p class="contact-image-text">4.0之前的界面</p>
          <div class="contact-image">
            <img src="https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/QQ20250404-121603.png" />
          </div>

          <div class="contact-image">
            <img src="https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/12312312312.png"  />
          </div>

          <div class="contact-image">
            <img src="https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/staticQQ%E6%88%AA%E5%9B%BE20230906091107.png"  />
          </div>

          <p class="contact-image-text">开发者想说</p>
          <span>这个网址虽然最初是用的别人的开源项目,但是经过我自己的努力,已经变成了一个完全由我自己的想法和创意打造的作品,虽然还有很多不足,但是我会继续努力,打造出更好的作品</span>
          <span>该网站换过两次服务器，被黑过导致数据全部丢失，重新搭起后，我便开始着手打造这个网站，期间也遇到了很多困难，但是我都一一克服</span>
          <p class="contact-image-text">用代码铸造自己的赛博乌托邦！</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import lottie from 'lottie-web';
import animationData from '@/assets/fly.json';
import animationData2 from '@/assets/connect.json';
import introduceData from '@/assets/introduce.json';
import carData from '@/assets/car.json';
import webData from '@/assets/webData.json';
import localtionData from '@/assets/location.json';
import {SubmitEmail} from "../../api/BSideApi";
import {ElMessage, ElNotification} from "element-plus";
import { 
  User, 
  Star, 
  Link, 
  Message, 
  ChatDotSquare, 
  ChatLineRound, 
  Clock 
} from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const flyContainer = ref(null);
const connectContainer = ref(null);
const animFly = ref(null);
const disabled = ref(false);
const animConnect = ref(null);
const particles = ref(null);
const data= ref({
  email: '',
  name: '',
  content: ''
})

const animCar = ref(null);
const animWeb = ref(null);
const animLocaltion = ref(null);
const introduceContainer = ref(null);
const animIntroduce = ref(null);
const carContainer = ref(null);
const webContainer = ref(null);
const localtionContainer = ref(null);

// 打字机效果
const typingText = ref('');
const fullText = '励志打造IT爱好者的乌托邦';
let typingIndex = 0;

// 更新日志数据
const updateLogs = ref([
  { version: '4.1.0', description: '新增AI视频生成，公开图片平台,MCP和RAG支持用户自定义,重写关于页面,优化了部分模块界面设计', date: '2025.8.24', major: false },
  { version: '4.0.0', description: '🍬版本大更新!删除了智慧校园,SD绘画(费用过高)和HDFS集群,优化我的数据模块，重写首页对话模块,现首页采用SpringAi支持多模型多模态MCP+RAG+自定义角色+对话列表的AI对话;新增SpringCloud微服务版本,优化了部分模块；更加人民的网站！', date: '2025.8.20', major: true },
  { version: '3.2.0', description: '引入对话模型DeepSeek,Claude3.7,豆包1.5pro,智谱GLM-4v,Grok3,Gemini,新增我的简介,优化了外链模块', date: '2025.5.15', major: false },
  { version: '3.1.0', description: '新增源码星球模块，CogView绘画模块和生成工具模块', date: '2025.5.2', major: false },
  { version: '3.0.0', description: '对网站整体进行超大的优化布局，新增关于模块,个人中心和接入虎皮椒支付，货币功能正式完善，修改了很多bug，删除了数字人和大数据废弃的模块', date: '2025.2.16', major: true },
  { version: '2.6.0', description: '新增我的数据模块，搭建了HDFS集群存储数据和可视化，优化了部分模块', date: '2024.12.10', major: false },
  { version: '2.5.0', description: '新增智谱ai模块，额外的文图视对话', date: '2024.11.2', major: false },
  { version: '2.4.0', description: '修复完善Sd绘画，可控制开启Sd绘画，项目bug数减为0', date: '2024.9.9', major: false },
  { version: '2.3.0', description: '新增我的作品|图床模块|对应管理模块', date: '2024.9.7', major: false },
  { version: '2.2.0', description: '新增我们学校的智慧派斯和大数据专区,通过python爬取数据,从此项目变三端(Java,Vue,Python)', date: '2024.6.17', major: false },
  { version: '2.1.0', description: '上新外链推荐,控制台新增外链管理', date: '2024.6.6', major: false },
  { version: '2.0.0', description: '对整体项目进行二次大改造，SD Ai绘画魔改，预设角色增多，新增数字人平台|个人中心|我的博客,删掉了claude和必应,此作品入围全高计设赛国赛', date: '2024.5.1', major: true },
  { version: '1.0.0', description: '基于Time sea作者开源项目进行搭建,并修改了logo等', date: '2024.2.6', major: true }
]);

// 滚动观察器
let observer = null;

onMounted(() => {
  // 初始化Lottie动画
  animFly.value = lottie.loadAnimation({
    container: flyContainer.value,
    renderer: 'svg',
    loop: true,
    autoplay: true,
    animationData: animationData
  });
  animConnect.value = lottie.loadAnimation({
    container: connectContainer.value,
    renderer: 'svg',
    loop: true,
    autoplay: true,
    animationData: animationData2
  });
  animIntroduce.value = lottie.loadAnimation({
    container: introduceContainer.value,
    renderer: 'svg',
    loop: true,
    autoplay: true,
    animationData: introduceData
  });
  animCar.value = lottie.loadAnimation({
    container: carContainer.value,
    renderer: 'svg',
    loop: true,
    autoplay: true,
    animationData: carData
  });
  animWeb.value = lottie.loadAnimation({
    container: webContainer.value,
    renderer: 'svg',
    loop: true,
    autoplay: true,
    animationData: webData
  });
  animLocaltion.value = lottie.loadAnimation({
    container: localtionContainer.value,
    renderer: 'svg',
    loop: true,
    autoplay: true,
    animationData: localtionData
  });

  // 初始化粒子效果
  initParticles();
  
  // 开始打字机效果
  startTypingEffect();
  
  // 初始化滚动动画
  initScrollAnimations();
});

onUnmounted(() => {
  // 清理动画
  if (animFly.value) animFly.value.destroy();
  if (animConnect.value) animConnect.value.destroy();
  if (animIntroduce.value) animIntroduce.value.destroy();
  
  // 清理滚动观察器
  if (observer) {
    observer.disconnect();
  }
});



// 粒子效果
const initParticles = () => {
  const container = particles.value;
  if (!container) return;
  
  for (let i = 0; i < 30; i++) {
    const particle = document.createElement('div');
    particle.className = 'particle';
    particle.style.left = Math.random() * 100 + '%';
    particle.style.top = Math.random() * 100 + '%';
    particle.style.animationDelay = Math.random() * 3 + 's';
    particle.style.animationDuration = (Math.random() * 4 + 3) + 's';
    container.appendChild(particle);
  }
};

// 打字机效果
const startTypingEffect = () => {
  const typeNextChar = () => {
    if (typingIndex < fullText.length) {
      typingText.value += fullText.charAt(typingIndex);
      typingIndex++;
      setTimeout(typeNextChar, 150);
    }
  };
  
  // 延迟开始打字效果
  setTimeout(typeNextChar, 1000);
};

const pauseAnimation = (value) => {
  if (value === 'connect') {
    if (animConnect.value) animConnect.value.pause();
  }

  if (value === 'fly') {
    if (animFly.value) animFly.value.pause();
  }

  if (value === 'introduce') {
    if (animIntroduce.value) animIntroduce.value.pause();
  }
};

const playAnimation = (value) => {
  if (value === 'connect') {
    if (animConnect.value && animConnect.value.isPaused) {
      animConnect.value.play();
    }
  }
  if (value === 'fly') {
    if (animFly.value && animFly.value.isPaused) {
      animFly.value.play();
    }
  }

  if (value === 'introduce') {
    if (animIntroduce.value && animIntroduce.value.isPaused) {
      animIntroduce.value.play();
    }
  }
};

const isCode = ref(true)
const buttonText = ref('发送')
async function submit() {
  if (isCode.value) {
    if (!data.value.content && data.value.content === '' && !data.value.email) {
      ElMessage.warning("似乎有东西没写");
      return;
    }
    isCode.value = false;
    try {
      buttonText.value = "正在发送中";
      await SubmitEmail(data.value);
      ElMessage.success("发送成功");
      disabled.value = true;
      buttonText.value = "已发送"
    }catch (e){
      ElNotification({
        title: "错误",
        message: e,
        type: "error",
      });
      buttonText.value = "重新发送";
      isCode.value = true;
    }

  }

}

const navigateToProfile = () => {
  router.push('/profile');
};

const navigateToProIndex = () => {
  console.log('Logo clicked!');
  router.push('/');
};

const navigateToBeian = () => {
  window.open('https://beian.miit.gov.cn/', '_blank');
};

const navigateToGitee = () => {
  window.open('https://gitee.com/hepingan11/gptjava', '_blank');
};
// 初始化滚动动画
const initScrollAnimations = () => {
  // 创建 Intersection Observer
  observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const element = entry.target;
        const animation = element.dataset.animation;
        const delay = element.dataset.delay || '0';
        
        // 设置动画延迟
        element.style.animationDelay = `${delay}s`;
        
        // 添加动画类
        element.classList.add('animate-in', `animate-${animation}`);
        
        // 动画完成后停止观察这个元素
        observer.unobserve(element);
      }
    });
  }, {
    threshold: 0.1, // 当元素10%可见时触发
    rootMargin: '0px 0px -50px 0px' // 提前50px触发
  });

  // 观察所有带有 scroll-animate 类的元素
  const animatedElements = document.querySelectorAll('.scroll-animate');
  animatedElements.forEach((el) => {
    // 初始状态设为不可见
    el.classList.add('animate-hidden');
    observer.observe(el);
  });
};
</script>

<style scoped>
/* ==================== 基础样式 ==================== */
.about {
  width: 100%;
  height: auto;
  min-height: 100vh;
  position: relative;
  scroll-behavior: smooth;
  overflow-x: hidden;
  overflow-y: visible;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #16213e 100%);
}

.container {
  margin: 0 auto;
  padding: 20px;
  max-width: 1200px;
  position: relative;
  z-index: 10;
}



/* ==================== 粒子效果 ==================== */
.particles {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  z-index: 2;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 2px;
  height: 2px;
  background: #64ffda;
  border-radius: 50%;
  animation: particle-float ease-in-out infinite;
  box-shadow: 0 0 6px #64ffda;
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 1;
  }
}

/* ==================== 光晕效果 ==================== */
.glow-orbs {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  z-index: 3;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  animation: orb-float 8s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(138, 35, 135, 0.3) 0%, transparent 70%);
  top: 10%;
  left: 80%;
  animation-delay: 0s;
}

.orb-2 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(233, 64, 87, 0.2) 0%, transparent 70%);
  top: 60%;
  left: 10%;
  animation-delay: 3s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(100, 255, 218, 0.15) 0%, transparent 70%);
  top: 30%;
  left: 50%;
  animation-delay: 6s;
}

@keyframes orb-float {
  0%, 100% {
    transform: translateY(0px) scale(1);
  }
  50% {
    transform: translateY(-30px) scale(1.1);
  }
}

/* ==================== 布局样式 ==================== */
.main-content {
  display: flex;
  gap: 60px;
  align-items: flex-start;
  margin: 60px 0;
  position: relative;
  z-index: 10;
}

.content {
  flex: 1;
  min-width: 300px;
}

.animation-section {
  flex-shrink: 0;
  width: 400px;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* ==================== 标题区域 ==================== */
.title-section {
  margin-bottom: 40px;
}

/* ==================== Logo样式 ==================== */
.logo {
  width: 60px;
  height: 60px;
  margin-bottom: -10px;
  margin-right: 15px;
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  filter: drop-shadow(0 8px 25px rgba(100, 255, 218, 0.3));
  position: relative;
  display: inline-block;
  cursor: pointer;
  z-index: 100;
  pointer-events: auto;
}

.logo::before {
  content: '';
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4, #45b7d1, #96ceb4, #ff6b6b);
  background-size: 300% 300%;
  border-radius: 22px;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
  animation: gradient-border 3s ease-in-out infinite;
}

.logo:hover::before {
  opacity: 1;
}

.logo:hover {
  transform: scale(1.1) rotate(5deg);
  filter: drop-shadow(0 15px 35px rgba(100, 255, 218, 0.5));
}

.logo:active {
  transform: scale(1.05) rotate(3deg);
}

@keyframes gradient-border {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

/* Logo呼吸效果 */
@keyframes logo-breathe {
  0%, 100% {
    filter: drop-shadow(0 8px 25px rgba(100, 255, 218, 0.3));
  }
  50% {
    filter: drop-shadow(0 12px 30px rgba(100, 255, 218, 0.5));
  }
}

/* 确保logo可以正常点击 */
.logo {
  animation: logo-breathe 4s ease-in-out infinite;
  z-index: 100;
  pointer-events: auto;
  position: relative;
}

/* 调试：确保logo区域可以点击 */
.logo::after {
  content: '';
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: rgba(255, 0, 0, 0.1);
  border-radius: 30px;
  z-index: 1;
  pointer-events: none;
}

.glitch-title {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(135deg, #ff6b6b, #507ad3, var(--themeColor1), #74edb5);
  background-size: 400% 400%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradient-shift 3s ease-in-out infinite, glitch 0.3s ease-in-out infinite alternate;
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
  text-shadow: 0 0 30px rgba(255, 107, 107, 0.5);
}

@keyframes gradient-shift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes glitch {
  0% {
    transform: translate(0);
  }
  20% {
    transform: translate(-2px, 2px);
  }
  40% {
    transform: translate(-2px, -2px);
  }
  60% {
    transform: translate(2px, 2px);
  }
  80% {
    transform: translate(2px, -2px);
  }
  100% {
    transform: translate(0);
  }
}

.typing-animation {
  display: flex;
  align-items: center;
  gap: 5px;
}

.typing-animation h2 {
  font-size: 24px;
  color: #64ffda;
  margin: 0;
  text-shadow: 0 0 10px rgba(100, 255, 218, 0.5);
}

.cursor {
  font-size: 24px;
  color: #64ffda;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

/* ==================== 信息卡片 ==================== */
.info-cards {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 40px;
}

.info-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 25px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.info-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s;
}

.info-card:hover::before {
  left: 100%;
}

.info-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(100, 255, 218, 0.3);
}

.info-card .card-icon {
  font-size: 24px;
  color: #64ffda;
  margin-bottom: 15px;
  text-shadow: 0 0 10px rgba(100, 255, 218, 0.5);
}

.info-card h3 {
  color: #ffffff;
  font-size: 18px;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.info-card p {
  color: #a0a0a0;
  margin: 0;
  font-size: 14px;
}

.links {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

.modern-link {
  width: 70%;
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background: linear-gradient(135deg, rgba(100, 255, 218, 0.1), rgba(100, 255, 218, 0.05));
  border: 1px solid rgba(100, 255, 218, 0.2);
  border-radius: 25px;
  color: #64ffda;
  text-decoration: none;
  font-size: 12px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  user-select: none;
}

.modern-link:hover {
  background: linear-gradient(135deg, rgba(100, 255, 218, 0.3), rgba(100, 255, 218, 0.15));
  transform: translateX(5px);
  box-shadow: 0 5px 20px rgba(100, 255, 218, 0.4);
  border-color: rgba(100, 255, 218, 0.4);
}

.modern-link:active {
  transform: translateX(3px) scale(0.98);
}

.card-glow {
  position: absolute;
  top: -100%;
  left: -50%;
  width: 200%;
  height: 650%;
  background: conic-gradient(from 0deg, transparent, rgba(100, 255, 218, 0.1), transparent);
  animation: rotate 4s linear infinite;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.info-card:hover .card-glow {
  opacity: 1;
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

/* ==================== 联系表单 ==================== */
.contact-section {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 30px;
  padding: 40px;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.section-title {
  font-size: 32px;
  margin-bottom: 30px;
  position: relative;
}

.title-text {
  background: linear-gradient(135deg, #ff6b6b, #4ecdc4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: block;
  margin-bottom: 10px;
}

.title-subtitle {
  font-size: 18px;
  color: #a0a0a0;
  font-weight: 400;
}

.contact-form {
  max-width: 500px;
}

.form-group {
  margin-bottom: 25px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64ffda;
  font-size: 14px;
  margin-bottom: 10px;
  font-weight: 500;
}

.modern-input {
  width: 100%;
}

.modern-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(100, 255, 218, 0.2);
  border-radius: 15px;
  box-shadow: none;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.modern-input :deep(.el-input__wrapper):hover {
  border-color: rgba(100, 255, 218, 0.4);
  background: rgba(255, 255, 255, 0.08);
}

.modern-input :deep(.el-input__wrapper.is-focus) {
  border-color: #64ffda;
  box-shadow: 0 0 20px rgba(100, 255, 218, 0.3);
  background: rgba(255, 255, 255, 0.1);
}

.modern-input :deep(.el-input__inner) {
  color: #ffffff;
  background: transparent;
  border: none;
  font-size: 14px;
}

.modern-input :deep(.el-textarea__inner) {
  color: #ffffff;
  background: transparent;
  border: none;
  font-size: 14px;
  resize: none;
}

.modern-input :deep(.el-input__inner::placeholder),
.modern-input :deep(.el-textarea__inner::placeholder) {
  color: #666666;
}

.form-submit {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.cyber-button {
  position: relative;
  padding: 15px 40px;
  background: linear-gradient(135deg, #ff6b6b, #4ecdc4);
  border: none;
  border-radius: 30px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.cyber-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(255, 107, 107, 0.4);
}

.cyber-button:active {
  transform: translateY(-1px);
}

.cyber-button .button-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.cyber-button:hover .button-glow {
  left: 100%;
}

/* ==================== 更新日志时间线 ==================== */
.changelog-section {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 30px;
  padding: 40px;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.changelog-timeline {
  position: relative;
  max-width: 600px;
}

.changelog-timeline::before {
  content: '';
  position: absolute;
  left: 20px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #64ffda, #ff6b6b, #4ecdc4);
  box-shadow: 0 0 10px rgba(100, 255, 218, 0.5);
}

.timeline-item {
  position: relative;
  padding-left: 60px;
  margin-bottom: 30px;
}

.timeline-dot {
  position: absolute;
  left: 12px;
  top: 0;
  width: 16px;
  height: 16px;
  background: #64ffda;
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(100, 255, 218, 0.6);
  z-index: 2;
}

.major-update .timeline-dot {
  background: #ff6b6b;
  box-shadow: 0 0 20px rgba(255, 107, 107, 0.6);
  width: 20px;
  height: 20px;
  left: 10px;
}

.dot-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: inherit;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
}

.timeline-content {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  padding: 20px;
  position: relative;
  transition: all 0.3s ease;
}

.timeline-content:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateX(5px);
  border-color: rgba(100, 255, 218, 0.3);
}

.version-tag {
  display: inline-block;
  background: linear-gradient(135deg, #64ffda, #4ecdc4);
  color: #000;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 10px;
}

.major-update .version-tag {
  background: linear-gradient(135deg, #ff6b6b, #ff8e53);
  color: white;
}

.update-description {
  color: #ffffff;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 10px;
}

.update-date {
  color: #888;
  font-size: 12px;
}



/* ==================== 动画和图片占位 ==================== */
.lottie-animation {
  width: 350px;
  height: 350px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.floating {
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

.lottie-animation:hover {
  transform: scale(1.05) rotate(2deg);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(100, 255, 218, 0.3);
}

.image-placeholder {
  width: 350px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}


.placeholder-content {
  text-align: center;
  color: #64ffda;
}

.placeholder-content .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
  opacity: 0.7;
}

.placeholder-content p {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}

.contact-image {
  width: 350px;
  height: auto;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.contact-image-text {
  color: #64ffda;
  font-size: 17px;
  opacity: 0.75;
  margin-bottom: 10px;
  text-align: center;
  margin-left: -20px;
}

.contact-image img {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 20px;
  opacity: 0.85;
  transition: transform 0.3s ease;
}

.contact-image:hover {
  transform: scale(1.02);
  box-shadow: 0 10px 30px rgba(100, 255, 218, 0.2);
}

.contact-image:hover img {
  transform: scale(1.05);
  opacity: 1;
  box-shadow: 0 15px 30px rgba(100, 255, 218, 0.2);
}

/* ==================== 滚动动画 ==================== */
/* 初始隐藏状态 */
.animate-hidden {
  opacity: 0;
  visibility: hidden;
}

/* 通用动画属性 */
.animate-in {
  visibility: visible !important;
  animation-duration: 0.8s;
  animation-timing-function: cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation-fill-mode: forwards;
}

/* 淡入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.animate-fade-up {
  animation-name: fadeInUp;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(60px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-down {
  animation-name: fadeInDown;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-60px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-left {
  animation-name: fadeInLeft;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.animate-fade-right {
  animation-name: fadeInRight;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(-60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 滑动动画 */
.animate-slide-left {
  animation-name: slideInLeft;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.animate-slide-right {
  animation-name: slideInRight;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.animate-slide-up {
  animation-name: slideInUp;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(80px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 缩放动画 */
.animate-scale-in {
  animation-name: scaleIn;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8) rotate(-5deg);
  }
  to {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

/* 弹跳动画 */
.animate-bounce-in {
  animation-name: bounceIn;
  animation-duration: 1s;
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 特殊效果 */
.animate-in.info-card {
  animation-duration: 1s;
}

.animate-in.timeline-item {
  animation-duration: 0.6s;
}

.animate-in.lottie-animation {
  animation-duration: 1.2s;
}

/* 动画完成后的状态保持 */
.animate-in {
  opacity: 1 !important;
  visibility: visible !important;
}

/* 移动端优化 */
@media (prefers-reduced-motion: reduce) {
  .animate-in {
    animation: none !important;
    opacity: 1 !important;
    transform: none !important;
  }
  
  .animate-hidden {
    opacity: 1 !important;
    visibility: visible !important;
  }
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    gap: 30px;
  }

  .animation-section {
    width: 100%;
    align-items: center;
  }

  .lottie-animation,
  .image-placeholder,
  .contact-image {
    width: 100%;
    max-width: 300px;
  }

  .logo {
    width: 60px;
    height: 60px;
    margin-bottom: 15px;
  }

  .glitch-title {
    font-size: 32px;
  }

  .typing-animation h2 {
    font-size: 18px;
  }

  .info-cards {
    margin-top: 20px;
  }

  .contact-section,
  .changelog-section {
    padding: 20px;
  }

  .timeline-item {
    padding-left: 40px;
  }

  .changelog-timeline::before {
    left: 15px;
  }

  .timeline-dot {
    left: 7px;
  }

  .major-update .timeline-dot {
    left: 5px;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 10px;
  }

  .logo {
    width: 50px;
    height: 50px;
    margin-bottom: 10px;
  }

  .logo:hover {
    transform: scale(1.05) rotate(3deg);
  }

  .glitch-title {
    font-size: 24px;
  }

  .section-title {
    font-size: 24px;
  }

  .form-submit {
    margin-top: 20px;
  }

  .cyber-button {
    padding: 12px 30px;
    font-size: 14px;
  }
}
</style>