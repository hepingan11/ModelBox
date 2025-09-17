<template>
  <div>
    <!-- 星空背景 -->
    <div class="starry-sky">
      <div 
        v-for="i in 100" 
        :key="`star-${i}`" 
        class="star"
        :style="{
          left: `${Math.random() * 100}%`,
          top: `${Math.random() * 100}%`,
          width: `${Math.random() * 3 + 1}px`,
          height: `${Math.random() * 3 + 1}px`,
          animationDelay: `${Math.random() * 5}s`,
          animationDuration: `${Math.random() * 3 + 3}s`,
          '--star-opacity': Math.random() * 0.5 + 0.3
        }"
      ></div>
      <div 
        v-for="i in 5" 
        :key="`shooting-star-${i}`" 
        class="shooting-star"
        :style="{
          left: `${Math.random() * 30}%`,
          top: `${Math.random() * 30}%`,
          '--shooting-delay': `${Math.random() * 15}s`,
          '--shooting-angle': `${Math.random() * 60 + 20}deg`
        }"
      ></div>
    </div>
    
    <div class="link-view-container">
      <!-- 侧边栏 -->
      <div class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
        <div class="logo">
          <img src="@/assets/logo.png" alt="TD导航" class="logo-img">
        </div>
        <el-menu class="sidebar-menu" background-color="transparent" text-color="#fff" active-text-color="#409EFF"
          :collapse="isCollapsed" @select="handleSelect">
          <el-menu-item index="featured">
            <el-icon>
              <House />
            </el-icon>
            <span>精选网站</span>
          </el-menu-item>
          <el-menu-item index="tools">
            <el-icon>
              <Tools />
            </el-icon>
            <span>实用工具</span>
          </el-menu-item>
          <el-menu-item index="resource">
            <el-icon>
              <Picture />
            </el-icon>
            <span>资源分享</span>
          </el-menu-item>
          <el-menu-item index="ai">
            <el-icon>
              <Picture />
            </el-icon>
            <span>AI内容</span>
          </el-menu-item>
          <el-menu-item index="hax">
            <el-icon>
              <Monitor />
            </el-icon>
            <span>黑科技</span>
          </el-menu-item>
          <el-menu-item index="hot">
            <el-icon>
              <Document />
            </el-icon>
            <span>知名网站</span>
          </el-menu-item>
          <el-menu-item index="tutorial">
            <el-icon>
              <Document />
            </el-icon>
            <span>教程文档</span>
          </el-menu-item>
          <el-menu-item index="other">
            <el-icon>
              <Link />
            </el-icon>
            <span>其它</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="toggleSidebar">
        <el-icon :size="20">
          <Fold v-if="!isCollapsed" />
          <Expand v-else />
        </el-icon>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content" :class="{ 'content-expanded': isCollapsed }">
        <div class="content-wrapper">
          <!-- 顶部区域背景特效 -->
          <div class="top-area-effect">
            
          </div>
          
          <!-- 顶部导航栏 -->
          <div class="img-container">
            <el-image :src="topImg" class="top-img" fit="cover" @click="handleImageClick"></el-image>
          </div>

          <!-- 搜索框 -->
          <div class="search-container">
            <div class="search-box" :class="{ 'search-active': isSearchActive }">
              <el-input
                v-model="searchKeyword"
                placeholder="输入网站名称搜索"
                class="search-input"
                clearable
                @clear="resetSearch"
                @keyup.enter="searchLinks"
              >
                <template #prefix>
                  <el-icon class="search-icon"><Search /></el-icon>
                </template>
                <template #append>
                  <el-button @click="searchLinks" class="search-button">搜索</el-button>
                </template>
              </el-input>
            </div>
            <div class="search-status" v-if="isSearchActive">
              <el-tag type="success" size="small" effect="dark" class="search-tag">
                搜索结果: {{ websites.length }} 项
              </el-tag>
              <el-button type="text" @click="resetSearch" class="reset-button">
                <el-icon><Back /></el-icon> 返回全部列表
              </el-button>
            </div>
          </div>

          <!-- 内容区域 -->
          <div class="content">
            <h3 class="section-title" id="featured">精选网站</h3>
            <el-row :gutter="20">
              <template v-for="(item, index) in websites" :key="index">
                <el-col v-if="item.isHot === 1" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <el-popover placement="top-start" :width="200" trigger="hover" :content="item.linkIntro">
                    <template #reference>
                      <el-card class="website-card" :body-style="{ padding: '0px' }" @click="goLink(item.linkUrl)">
                        <div class="card-content">
                          <el-image :src="imageUrl + item.linkImg" :alt="item.linkName" class="item-icon" />
                          <div class="item-info">
                            <h4>{{ item.linkName }}</h4>
                            <p>{{ item.linkIntro }}</p>
                          </div>
                          <div class="favorite-icon" @click.stop="addLinkStat(item.linkId)">
                            <el-icon>
                              <Star />
                            </el-icon>
                          </div>
                        </div>
                      </el-card>
                    </template>
                  </el-popover>
                </el-col>
              </template>
            </el-row>

            <h3 class="section-title" id="featured">我的收藏</h3>
            <el-row :gutter="20">
              <template v-for="(item, index) in userStatLink" :key="index">
                <el-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <el-popover placement="top-start" :width="200" trigger="hover" :content="item.linkIntro">
                    <template #reference>
                      <el-card class="website-card" :body-style="{ padding: '0px' }" @click="goLink(item.linkUrl)">
                        <div class="card-content">
                          <el-image :src="imageUrl + item.linkImg" :alt="item.linkName" class="item-icon" />
                          <div class="item-info">
                            <h4>{{ item.linkName }}</h4>
                            <p>{{ item.linkIntro }}</p>
                          </div>
                          <div class="favorite-icon" @click.stop="cancelLinkStat(item.linkId)">
                            <img :src="require('@/assets/star.png')"  style="width: 20px; height: 20px;"/>
                          </div>
                        </div>
                      </el-card>
                    </template>
                  </el-popover>
                </el-col>
              </template>
            </el-row>

            <h3 class="section-title" id="tools">实用工具</h3>
            <el-row :gutter="20">
              <template v-for="(item, index) in websites" :key="index">
                <el-col v-if="item.linkSort === 'tools'" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <el-popover placement="top-start" :width="200" trigger="hover" :content="item.linkIntro">
                    <template #reference>
                      <el-card class="website-card" :body-style="{ padding: '0px' }" @click="goLink(item.linkUrl)">
                        <div class="card-content">
                          <el-image :src="imageUrl + item.linkImg" :alt="item.linkName" class="item-icon" />
                          <div class="item-info">
                            <h4>{{ item.linkName }}</h4>
                            <p>{{ item.linkIntro }}</p>
                          </div>
                          <div class="favorite-icon" @click.stop="addLinkStat(item.linkId)">
                            <el-icon>
                              <Star />
                            </el-icon>
                          </div>
                        </div>
                      </el-card>
                    </template>
                  </el-popover>
                </el-col>
              </template>
            </el-row>

            <h3 class="section-title" id="resource">资源分享</h3>
            <el-row :gutter="20">
              <template v-for="(item, index) in websites" :key="index">
                <el-col v-if="item.linkSort === 'resource'" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <el-popover placement="top-start" :width="200" trigger="hover" :content="item.linkIntro">
                    <template #reference>
                      <el-card class="website-card" :body-style="{ padding: '0px' }" @click="goLink(item.linkUrl)">
                        <div class="card-content">
                          <el-image :src="imageUrl + item.linkImg" :alt="item.linkName" class="item-icon" />
                          <div class="item-info">
                            <h4>{{ item.linkName }}</h4>
                            <p>{{ item.linkIntro }}</p>
                          </div>
                          <div class="favorite-icon" @click.stop="addLinkStat(item.linkId)">
                            <el-icon>
                              <Star />
                            </el-icon>
                          </div>
                        </div>
                      </el-card>
                    </template>
                  </el-popover>
                </el-col>
              </template>
            </el-row>

            <h3 class="section-title" id="ai">AI内容</h3>
            <el-row :gutter="20">
              <template v-for="(item, index) in websites" :key="index">
                <el-col v-if="item.linkSort === 'ai'" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <el-popover placement="top-start" :width="200" trigger="hover" :content="item.linkIntro">
                    <template #reference>
                      <el-card class="website-card" :body-style="{ padding: '0px' }" @click="goLink(item.linkUrl)">
                        <div class="card-content">
                          <el-image :src="imageUrl + item.linkImg" :alt="item.linkName" class="item-icon" />
                          <div class="item-info">
                            <h4>{{ item.linkName }}</h4>
                            <p>{{ item.linkIntro }}</p>
                          </div>
                          <div class="favorite-icon" @click.stop="addLinkStat(item.linkId)">
                            <el-icon>
                              <Star />
                            </el-icon>
                          </div>
                        </div>
                      </el-card>
                    </template>
                  </el-popover>
                </el-col>
              </template>
            </el-row>

            <h3 class="section-title" id="hax">黑科技</h3>
            <el-row :gutter="20">
              <template v-for="(item, index) in websites" :key="index">
                <el-col v-if="item.linkSort === 'hax'" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <el-popover placement="top-start" :width="200" trigger="hover" :content="item.linkIntro">
                    <template #reference>
                      <el-card class="website-card" :body-style="{ padding: '0px' }" @click="goLink(item.linkUrl)">
                        <div class="card-content">
                          <el-image :src="imageUrl + item.linkImg" :alt="item.linkName" class="item-icon" />
                          <div class="item-info">
                            <h4>{{ item.linkName }}</h4>
                            <p>{{ item.linkIntro }}</p>
                          </div>
                          <div class="favorite-icon" @click.stop="addLinkStat(item.linkId)">
                            <el-icon>
                              <Star />
                            </el-icon>
                          </div>
                        </div>
                      </el-card>
                    </template>
                  </el-popover>
                </el-col>
              </template>
            </el-row>

            <h3 class="section-title" id="hot">知名网站</h3>
            <el-row :gutter="20">
              <template v-for="(item, index) in websites" :key="index">
                <el-col v-if="item.linkSort === 'hot'" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <el-popover placement="top-start" :width="200" trigger="hover" :content="item.linkIntro">
                    <template #reference>
                      <el-card class="website-card" :body-style="{ padding: '0px' }" @click="goLink(item.linkUrl)">
                        <div class="card-content">
                          <el-image :src="imageUrl + item.linkImg" :alt="item.linkName" class="item-icon" />
                          <div class="item-info">
                            <h4>{{ item.linkName }}</h4>
                            <p>{{ item.linkIntro }}</p>
                          </div>
                          <div class="favorite-icon" @click.stop="addLinkStat(item.linkId)">
                            <el-icon>
                              <Star />
                            </el-icon>
                          </div>
                        </div>
                      </el-card>
                    </template>
                  </el-popover>
                </el-col>
              </template>
            </el-row>

            <h3 class="section-title" id="tutorial">教程文档</h3>
            <el-row :gutter="20">
              <template v-for="(item, index) in websites" :key="index">
                <el-col v-if="item.linkSort === 'tutorial'" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <el-popover placement="top-start" :width="200" trigger="hover" :content="item.linkIntro">
                    <template #reference>
                      <el-card class="website-card tutorial-card" :body-style="{ padding: '0px' }" @click="goLink(item.linkUrl)">
                        <div class="tutorial-content">
                          <div class="tutorial-img-container">
                            <el-image :src="imageUrl + item.linkImg" :alt="item.linkName" class="tutorial-img" fit="cover" />
                          </div>
                          <div class="tutorial-info">
                            <h4>{{ item.linkName }}</h4>
                            <p>{{ item.linkIntro }}</p>
                          </div>
                          <div class="favorite-icon" @click.stop="addLinkStat(item.linkId)">
                            <el-icon>
                              <Star />
                            </el-icon>
                          </div>
                        </div>
                      </el-card>
                    </template>
                  </el-popover>
                </el-col>
              </template>
            </el-row>

            <h3 class="section-title" id="other">其它</h3>
            <el-row :gutter="20">
              <template v-for="(item, index) in websites" :key="index">
                <el-col v-if="item.linkSort === 'other'" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <el-popover placement="top-start" :width="200" trigger="hover" :content="item.linkIntro">
                    <template #reference>
                      <el-card class="website-card" :body-style="{ padding: '0px' }" @click="goLink(item.linkUrl)">
                        <div class="card-content">
                          <el-image :src="imageUrl + item.linkImg" :alt="item.linkName" class="item-icon" />
                          <div class="item-info">
                            <h4>{{ item.linkName }}</h4>
                            <p>{{ item.linkIntro }}</p>
                          </div>
                          <div class="favorite-icon" @click.stop="addLinkStat(item.linkId)">
                            <el-icon>
                              <Star />
                            </el-icon>
                          </div>
                        </div>
                      </el-card>
                    </template>
                  </el-popover>
                </el-col>
              </template>
            </el-row>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { addLinkStatById, cancelLinkStatById, getLinkList, getTopImgUrl, selectStatLink } from "../../api/BSideApi";
import { Search, House, Tools, Picture, Monitor, Document, Fold, Expand, Star, Back } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {ElNotification} from "element-plus";


const isCollapsed = ref(false)
const isMobile = ref(false)
const imageUrl = ref('')

const router = useRouter()

// 切换侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const topImg = ref('')
async function getTopImg() {
  topImg.value = await getTopImgUrl();
}

// 检查是否为移动设备
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
  isCollapsed.value = isMobile.value
}

async function addLinkStat(id) {
  const v = await addLinkStatById(id)
  if (!v) {
    ElNotification({
      title: "收藏成功",
      message: '可以刷新页面查看最新',
      type: "success",
    });
  }
}

// 监听窗口大小变化
onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  imageUrl.value = process.env.VUE_APP_IMAGE;
  await LinkList()
  await getTopImg();
  await getUserStatLink();
})

async function LinkList() {
  websites.value = await getLinkList('');
  websites.value = websites.value.filter(item => item.isPublic !== 0);
  console.log(websites.value)
}
function goLink(url) {
  window.open(url, '_blank')
}

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

const websites = ref([]);

// 添加处理菜单选择的方法
const handleSelect = (index) => {
  const element = document.getElementById(index);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' });
  }
}

async function cancelLinkStat(id) {
  const v = await cancelLinkStatById(id);
  if (!v) {
    ElNotification({
      title: "取消收藏成功",
      message: '可以刷新页面查看最新',
      type: "success",
    });
  }
}
const userStatLink = ref([]);

async function getUserStatLink() {
  userStatLink.value = await selectStatLink();
  console.log(userStatLink)
}

// 添加图片点击处理函数
const handleImageClick = () => {
  router.push('/apply_view')
}

const searchKeyword = ref('');
const isSearchActive = ref(false);

async function searchLinks() {
  // 根据关键词搜索链接
  websites.value = await getLinkList(searchKeyword.value);
  // 过滤掉非公开的链接
  websites.value = websites.value.filter(item => item.isPublic !== 0);
  ElNotification({
    title: "搜索完成",
    message: `找到 ${websites.value.length} 个符合条件的链接`,
    type: "success",
  });
  isSearchActive.value = true;
}

function resetSearch() {
  searchKeyword.value = '';
  isSearchActive.value = false;
  LinkList();
}
</script>

<style scoped>
.page-container {
  display: flex;
  min-height: 100vh;
  position: relative;
}

.sidebar {
  width: 200px;
  background-color: rgba(0, 0, 0, 0.2);
  padding: 20px 0;
  transition: all 0.3s ease;
  position: fixed;
  height: 100vh;
  z-index: 1000;
}

.sidebar-collapsed {
  width: 64px;
  padding: 20px 0;
}

.sidebar-collapsed .logo {
  padding: 0 12px;
}

.main-content {
  flex: 1;
  padding: 30px;
  margin-left: 200px;
  transition: all 0.3s ease;
  height: 100vh;
  overflow-y: auto;
}

.content-expanded {
  margin-left: 64px;
}

.collapse-btn {
  position: fixed;
  left: 200px;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(0, 0, 0, 0.2);
  color: white;
  width: 24px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 0 4px 4px 0;
  transition: all 0.3s ease;
  z-index: 1000;
}

.sidebar-collapsed+.collapse-btn {
  left: 64px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar-collapsed {
    transform: translateX(0);
  }

  .main-content {
    margin-left: 0;
  }

  .content-expanded {
    margin-left: 0;
  }

  .collapse-btn {
    left: 0;
  }

  .sidebar-collapsed+.collapse-btn {
    left: 64px;
  }
}

.logo {
  padding: 0 20px;
  margin-bottom: 20px;
}

.logo-img {
  max-width: 100%;
  height: auto;
}

.sidebar-menu {
  border-right: none;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.nav-bar {
  margin-bottom: 30px;
}

.img-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  position: relative;
  z-index: 2;
}

.top-img {
  width: 100%;
  height: 250px;
  border-radius: 10px;
  max-width: 700px;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  
  &:hover {
    transform: scale(1.02);
    box-shadow: 0 15px 40px rgba(var(--themeColor1-rgb), 0.3);
  }
}

.search-container {
  margin-bottom: 30px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  position: relative;
  z-index: 2;
}

.search-box {
  width: 100%;
  position: relative;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.1));
  border-radius: 10px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 10px 25px rgba(var(--themeColor1-rgb), 0.15);
    transform: translateY(-2px);
  }
  
  &:focus-within {
    box-shadow: 0 10px 25px rgba(var(--themeColor1-rgb), 0.25);
    transform: translateY(-2px);
  }
  
  &.search-active {
    border: 1px solid rgba(var(--themeColor1-rgb), 0.3);
    box-shadow: 0 10px 25px rgba(var(--themeColor1-rgb), 0.2);
  }
}

.search-input {
  width: 100%;
  
  :deep(.el-input__wrapper) {
    background-color: transparent;
    border: none;
    box-shadow: none !important;
    transition: all 0.3s ease;
    padding-left: 15px;
    padding-right: 15px;
  }
  
  :deep(.el-input__inner) {
    color: var(--textColor1);
    font-size: 16px;
    
    &::placeholder {
      color: rgba(255, 255, 255, 0.5);
      transition: color 0.3s ease;
    }
  }
  
  :deep(.el-input__suffix) {
    right: 80px;
  }
  
  :deep(.el-input__prefix-inner) {
    margin-right: 8px;
  }
  
  :deep(.el-input-group__append) {
    background-color: transparent;
    border: none;
    padding-right: 5px;
  }
}

.search-icon {
  color: var(--themeColor1);
  font-size: 18px;
  transition: transform 0.3s ease;
  
  .search-box:hover & {
    transform: scale(1.1);
  }
}

.search-button {
  background-color: var(--themeColor1);
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 20px;
  margin-right: 5px;
  transition: all 0.3s ease;
  font-weight: 500;
  
  &:hover {
    color: var(--themeColor2);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.search-status {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  animation: fadeIn 0.3s ease;
}

.search-tag {
  background-color: rgba(var(--themeColor1-rgb), 0.2);
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  animation: pulse 1.5s infinite alternate ease-in-out;
}

@keyframes pulse {
  0% {
    background-color: rgba(var(--themeColor1-rgb), 0.2);
  }
  100% {
    background-color: rgba(var(--themeColor1-rgb), 0.4);
  }
}

.reset-button {
  color: var(--themeColor1);
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s ease;
  background-color: rgba(var(--themeColor1-rgb), 0.1);
  border: none;
  
  &:hover {
    color: #fff;
    background-color: var(--themeColor1);
    transform: translateY(-2px);
  }
  
  .el-icon {
    margin-right: 4px;
    transition: transform 0.3s ease;
  }
  
  &:hover .el-icon {
    transform: translateX(-3px);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .search-container {
    max-width: 100%;
    padding: 0 10px;
  }
  
  .search-button {
    padding: 8px 15px;
    font-size: 14px;
  }
  
  .search-input {
    :deep(.el-input__inner) {
      font-size: 14px;
    }
  }
}

.menu-bar {
  border: none !important;
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
}

:deep(.el-menu-item) {
  border-radius: 20px;
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.section-title {
  color: #fff;
  margin-bottom: 20px;
  font-size: 24px;
}

/* 覆盖Element Plus卡片默认样式 */
:deep(.el-card) {
  border-color: transparent !important;
  background-color: var(--bgColor2) !important;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: var(--themeColor1) !important;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15) !important;
  }
  
  .el-card__body {
    padding: 0 !important;
    background-color: var(--bgColor2) !important;
  }
}

/* 修复卡片上的弹出内容背景 */
:deep(.el-popover) {
  background-color: var(--bgColor2) !important;
  border-color: var(--borderColor) !important;
  color: var(--textColor1) !important;
}

/* 网站卡片样式 */
.website-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
  background-color: var(--bgColor2);
  border-color: rgba(255, 255, 255, 0.05) !important;
  overflow: hidden;
}

.website-card:hover {
  transform: translateY(-5px);
  border-color: rgba(var(--themeColor1-rgb), 0.3) !important;
}

/* 卡片内容样式 */
.card-content {
  padding: 15px;
  display: flex;
  align-items: center;
  position: relative;
  background-color: var(--bgColor2);
}

.item-icon {
  width: 40px;
  height: 40px;
  margin-right: 15px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-info h4 {
  margin: 0;
  font-size: 16px;
  color: var(--textColor1) !important;
}

.item-info p {
  margin: 5px 0 0;
  font-size: 12px;
  color: var(--textColor3) !important;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.favorite-icon {
  position: absolute;
  top: 10px;
  right: 10px;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
  color: #ffd700;
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  padding: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorite-icon:hover {
  background-color: rgba(0, 0, 0, 0.2);
}

.website-card:hover .favorite-icon {
  opacity: 1;
}

.content {
  padding-bottom: 150px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px 50px;
  position: relative;
}

/* 顶部区域背景特效 */
.top-area-effect {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 500px;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.glow-effect {
  position: absolute;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  width: 1000px;
  height: 400px;
  background: radial-gradient(
    circle,
    rgba(var(--themeColor1-rgb), 0.3) 0%,
    rgba(var(--themeColor1-rgb), 0.15) 30%,
    rgba(var(--themeColor1-rgb), 0) 70%
  );
  opacity: 0.8;
  filter: blur(60px);
  animation: pulse-glow 8s infinite alternate ease-in-out;
  z-index: 0;
}

.glow-effect-2 {
  top: 60%;
  width: 800px;
  height: 350px;
  background: radial-gradient(
    circle,
    rgba(var(--themeColor2-rgb, 64, 158, 255), 0.25) 0%,
    rgba(var(--themeColor2-rgb, 64, 158, 255), 0.1) 40%,
    rgba(var(--themeColor2-rgb, 64, 158, 255), 0) 70%
  );
  animation-delay: 4s;
  animation-duration: 10s;
}

@keyframes pulse-glow {
  0% {
    opacity: 0.6;
    transform: translateX(-50%) scale(1);
  }
  100% {
    opacity: 0.9;
    transform: translateX(-50%) scale(1.3);
  }
}

.floating-particle {
  position: absolute;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
  filter: blur(1px);
  z-index: 1;
  opacity: 0;
  animation: float-up 15s infinite linear;
}

@keyframes float-up {
  0% {
    transform: translateY(110vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: var(--particle-opacity);
  }
  90% {
    opacity: var(--particle-opacity);
  }
  100% {
    transform: translateY(-10vh) rotate(360deg);
    opacity: 0;
  }
}

/* 响应式布局调整 */
@media screen and (max-width: 1600px) {
  .content-wrapper {
    max-width: 1200px;
  }
}

@media screen and (max-width: 1200px) {
  .content-wrapper {
    max-width: 1000px;
  }
}

@media screen and (max-width: 768px) {
  .content-wrapper {
    max-width: 100%;
    padding: 0 10px;
  }
}

.light-beam {
  position: absolute;
  width: 200px;
  height: 1200px;
  background: linear-gradient(to bottom, rgba(var(--themeColor1-rgb), 0.25), rgba(var(--themeColor1-rgb), 0));
  transform: rotate(45deg);
  top: -350px;
  left: 20%;
  opacity: 0.7;
  filter: blur(30px);
  animation: rotate-beam 15s infinite alternate ease-in-out;
}

.light-beam-2 {
  width: 150px;
  height: 1000px;
  left: 70%;
  transform: rotate(-30deg);
  background: linear-gradient(to bottom, rgba(var(--themeColor2-rgb, 64, 158, 255), 0.2), rgba(var(--themeColor2-rgb, 64, 158, 255), 0));
  animation-duration: 20s;
  animation-delay: 5s;
}

.light-beam-3 {
  width: 120px;
  height: 800px;
  left: 40%;
  top: 30%;
  transform: rotate(15deg);
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0));
  animation-duration: 18s;
  animation-delay: 2s;
}

@keyframes rotate-beam {
  0% {
    transform: rotate(45deg) translateX(0);
    opacity: 0.4;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    transform: rotate(65deg) translateX(100px);
    opacity: 0.4;
  }
}

/* 星空背景效果 */
.starry-sky {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.star {
  position: absolute;
  background-color: white;
  border-radius: 50%;
  opacity: 0;
  animation: twinkle 5s infinite;
}

.shooting-star {
  position: absolute;
  width: 2px;
  height: 2px;
  background: linear-gradient(to right, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0.8) 50%, rgba(255, 255, 255, 0));
  border-radius: 50%;
  animation: shooting 8s linear infinite;
  animation-delay: var(--shooting-delay, 0s);
  transform: rotate(var(--shooting-angle, 45deg));
  opacity: 0;
}

@keyframes twinkle {
  0% { opacity: 0; }
  50% { opacity: var(--star-opacity, 0.8); }
  100% { opacity: 0; }
}

@keyframes shooting {
  0% {
    transform: translateX(-100px) translateY(-100px) rotate(var(--shooting-angle, 45deg));
    width: 0;
    opacity: 0;
  }
  10% {
    width: 100px;
    opacity: 0.8;
  }
  20% {
    width: 150px;
    opacity: 1;
  }
  30% {
    width: 100px;
    opacity: 0.8;
  }
  100% {
    transform: translateX(calc(100vw + 200px)) translateY(calc(100vh + 200px)) rotate(var(--shooting-angle, 45deg));
    width: 0;
    opacity: 0;
  }
}

/* 星云效果 */
.nebula {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 30%, rgba(76, 0, 255, 0.05) 0%, transparent 30%),
    radial-gradient(circle at 80% 70%, rgba(0, 183, 255, 0.08) 0%, transparent 40%),
    radial-gradient(circle at 50% 50%, rgba(255, 0, 221, 0.03) 0%, transparent 60%);
  filter: blur(30px);
  z-index: 0;
  pointer-events: none;
  opacity: 0.7;
}

/* 强调光晕 */
.accent-glow {
  position: fixed;
  width: 50vw;
  height: 50vh;
  background: radial-gradient(circle, rgba(70, 131, 180, 0.08) 0%, transparent 70%);
  filter: blur(40px);
  animation: move-accent 20s ease-in-out infinite alternate;
  z-index: 0;
  pointer-events: none;
}

@keyframes move-accent {
  0% {
    top: 20%;
    left: 20%;
    transform: scale(1);
  }
  50% {
    top: 40%;
    left: 60%;
    transform: scale(1.5);
  }
  100% {
    top: 70%;
    left: 30%;
    transform: scale(1.2);
  }
}

/* 教程文档卡片样式 */
.tutorial-card {
  height: 90%;
  transition: all 0.3s ease;
}

.tutorial-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0;
  overflow: hidden;
}

.tutorial-img-container {
  width: 100%;
  height: 120px;
  overflow: hidden;
}

.tutorial-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.tutorial-info {
  padding: 15px;
  background: linear-gradient(120deg, rgba(30, 30, 50, 0.8), rgba(40, 40, 70, 0.8));
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tutorial-info h4 {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 8px;
}

.tutorial-info p {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.tutorial-card:hover .tutorial-img {
  transform: scale(1.1);
}
</style>