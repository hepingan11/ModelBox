<template>
	<scroll-view scroll-y class="function-scroll" :class="themeClass">
		<view class="function-page">
			<!-- 功能网格 -->
			<view class="function-grid">
				<view 
					class="grid-card" 
					v-for="(item, index) in functionList" 
					:key="index"
					@click="handleFunctionClick(item)"
				>
					<view class="card-header">
						<view class="icon-wrapper" :class="item.colorClass">
							<image :src="item.icon" class="card-icon" mode="aspectFit"></image>
						</view>
					</view>
					
					<text class="card-title">{{ item.title }}</text>
					<text class="card-desc">{{ item.desc }}</text>
					
					<view class="card-tags">
						<text 
							class="mini-tag" 
							v-for="(tag, tIdx) in item.tags" 
							:key="tIdx"
						>{{ tag }}</text>
					</view>
				</view>
			</view>
	
			<!-- 底部占位 -->
			<view style="height: 180rpx;"></view>
		</view>
	</scroll-view>
</template>

<script setup>
import { ref } from 'vue'
import { useTheme } from '@/hooks/useTheme'

const { themeClass } = useTheme()

const functionList = ref([
	{
		title: 'AI问答',
		desc: '完美支持多模态多模型和MCP+RAG~',
		icon: '/static/logo.png',
		tags: ['生成', '智能'],
		path: '/pages/ai/chat' 
	},
	{
		title: 'AI绘画',
		desc: '用文字绘制属于你的艺术作品',
		icon: '/static/icon/drawing.svg',
		tags: ['CogView', '智谱ai'],
		path: '/pages/ai/drawing'
	},
	{
		title: 'AI视频',
		desc: '基于豆包的ai视频生成',
		icon: '/static/icon/video.svg',
		tags: ['豆包', '视频'],
		path: '/pages/ai/video'
	},
	{
		title: '众创搭',
		desc: '寻找志同道合的创业合作伙伴，一个人的进步是加法，一群人的进步是乘法',
		icon: '/static/icon/zcd.png',
		tags: ['合作', '创业'],
		path: '/pages/project/projectList'
	},
	{
		title: '论坛水贴',
		desc: '大胆表述你的看法',
		icon: '/static/icon/forum.svg',
		tags: ['交流', '论坛'],
		path: '/pages/forum/forum'
	},
	{
		title: '源码星球',
		desc: '程序员的源码买卖市场',
		icon: '/static/icon/code.svg',
		tags: ['源码', '买卖'],
		path: '/pages/project/projectList'
	},
	{
		title: '我的收藏',
		desc: '就是你的问答收藏',
		icon: '/static/icon/star.png',
		tags: ['问答', '收藏'],
		path: '/pages/forum/mylike'
	},
	{
		title: '外链推荐',
		desc: '这里收集了网上一些不错的网站',
		icon: '/static/icon/clip.svg',
		tags: ['问答', '收藏'],
		path: '/pages/forum/mylike'
	}
])

const handleFunctionClick = (item) => {
	if (item.path) {
		uni.navigateTo({
			url: item.path,
			fail: (err) => {
				// 尝试 switchTab (虽然不太可能需要，但保留兼容性)
				uni.switchTab({ url: item.path })
			}
		})
	} else {
		uni.showToast({
			title: '功能开发中',
			icon: 'none'
		})
	}
}
</script>

<style scoped>
.function-scroll {
	height: 100vh;
}

.function-page {
	min-height: 100vh;
	/* 深色背景 */
	background-color: var(--bgColor1); 
	padding: 30rpx;
	box-sizing: border-box;
}

/* Banner 样式 */
.banner-card {
	background-color: #fff; /* 默认亮色 */
	border-radius: 24rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	display: flex;
	align-items: center;
	box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
}

/* 深色模式适配 */
.darkMode .banner-card {
	background-color: var(--bgColor2);
	box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.2);
}

.banner-content {
	display: flex;
	width: 100%;
}

.banner-avatar {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	margin-right: 30rpx;
	border: 4rpx solid var(--themeColor1);
}

.banner-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.banner-badges {
	display: flex;
	justify-content: space-between;
	margin-bottom: 12rpx;
}

.badge {
	font-size: 22rpx;
	padding: 6rpx 16rpx;
	background-color: var(--bgColor1);
	color: var(--textColor2);
	border-radius: 20rpx;
	margin-right: 10rpx;
	white-space: nowrap;
}

.badge-red {
	color: #ff4d4f;
	border: 1px solid #ff4d4f;
	background: none;
}

/* 网格样式 */
.function-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 24rpx;
}

.grid-card {
	background-color: var(--bgColor2);
	border-radius: 20rpx;
	padding: 30rpx;
	display: flex;
	flex-direction: column;
	min-height: 300rpx;
	transition: all 0.3s;
}

.grid-card:active {
	opacity: 0.9;
	transform: scale(0.98);
}

.card-header {
	margin-bottom: 24rpx;
}

.icon-wrapper {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.card-icon {
	width: 68rpx;
	height: 68rpx;
}


.card-title {
	font-size: 32rpx;
	font-weight: bold;
	color: var(--textColor1);
	margin-bottom: 12rpx;
}

.card-desc {
	font-size: 24rpx;
	color: var(--textColor3);
	margin-bottom: 24rpx;
	line-height: 1.4;
	flex: 1; /* 推到底部 */
}

.card-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.mini-tag {
	font-size: 20rpx;
	color: var(--textColor2);
	background-color: var(--bgColor1);
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
}
</style>
