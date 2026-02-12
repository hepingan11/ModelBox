<template>
	<view class="custom-tab-bar-wrapper" :class="themeClass">
		<view class="tab-bar-container">
			<view 
				v-for="(item, index) in list" 
				:key="index" 
				class="tab-item" 
				@click="handleTabClick(item, index)"
			>
				<view class="icon-box" :class="{ 'active': current === index }">
					<image 
						:src="current === index ? item.selectedIconPath : item.iconPath" 
						class="tab-icon" 
						mode="aspectFit"
					></image>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { useTheme } from '@/hooks/useTheme.js'

const { themeClass } = useTheme()

const props = defineProps({
	current: {
		type: Number,
		default: 0
	}
})

const emit = defineEmits(['tabClick'])

const list = [
	{
		pagePath: "pages/index/index",
		text: "首页",
		iconPath: "/static/home.png",
		selectedIconPath: "/static/home.png"
	},
	{
		pagePath: "pages/function/function",
		text: "功能",
		iconPath: "/static/project.svg",
		selectedIconPath: "/static/project.svg"
	},
	{
		pagePath: "pages/user/user",
		text: "我的",
		iconPath: "/static/user.png",
		selectedIconPath: "/static/user.png"
	}
]

const handleTabClick = (item, index) => {
	uni.vibrateShort()
	emit('tabClick', index)
}
</script>

<style scoped>
.custom-tab-bar-wrapper {
	position: fixed;
	bottom: 50rpx;
	left: 0;
	right: 0;
	z-index: 9999;
	display: flex;
	justify-content: center;
	pointer-events: none;
}

/* ===== 默认亮色模式 ===== */
.tab-bar-container {
	pointer-events: auto;
	background-color: #ffffff;
	border-radius: 100rpx;
	padding: 12rpx 30rpx;
	display: flex;
	align-items: center;
	box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.1);
	backdrop-filter: blur(10px);
	-webkit-backdrop-filter: blur(10px);
}

.tab-item {
	display: flex;
	align-items: center;
	padding: 0 10rpx;
}

.icon-box {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s ease;
}

.icon-box.active {
	background-color: rgba(0, 0, 0, 0.08);
}

.tab-icon {
	width: 44rpx;
	height: 44rpx;
	/* 亮色模式: 图标变黑 */
	filter: brightness(0);
}

/* 亮色模式选中: 图标变紫 */
.icon-box.active .tab-icon {
	filter: brightness(0) saturate(100%) invert(40%) sepia(80%) saturate(2000%) hue-rotate(230deg) brightness(90%);
}

/* ===== 暗色模式 ===== */
.darkMode .tab-bar-container {
	background-color: #222222;
	box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.3);
}

.darkMode .icon-box.active {
	background-color: rgba(255, 255, 255, 0.15);
}

.darkMode .tab-icon {
	/* 暗色模式: 图标变白 */
	filter: brightness(0) invert(1);
}

/* 暗色模式选中: 图标变紫 */
.darkMode .icon-box.active .tab-icon {
	filter: brightness(0) saturate(100%) invert(40%) sepia(80%) saturate(2000%) hue-rotate(230deg) brightness(120%);
}
</style>
