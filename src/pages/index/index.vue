<template>
	<view class="main-page">
		<swiper 
			class="main-swiper" 
			:current="currentTab" 
			@change="onSwiperChange"
			:duration="300"
		>
			<swiper-item>
				<HomeView ref="homeView" />
			</swiper-item>
			<swiper-item>
				<FunctionView ref="functionView" />
			</swiper-item>
			<swiper-item>
				<UserView ref="userView" />
			</swiper-item>
		</swiper>
		
		<CustomTabBar :current="currentTab" @tabClick="onTabClick" />
	</view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/CustomTabBar.vue'
import HomeView from './components/HomeView.vue'
import FunctionView from './components/FunctionView.vue'
import UserView from './components/UserView.vue'

const currentTab = ref(0)
const homeView = ref(null)
const functionView = ref(null)
const userView = ref(null)

const tabTitles = ['首页', '功能', '我的']

const updateTitle = (index) => {
	uni.setNavigationBarTitle({
		title: tabTitles[index] || 'CreativePartner'
	})
}

onLoad((options) => {
	if (options && options.tab) {
		const tabIndex = parseInt(options.tab)
		if (!isNaN(tabIndex) && tabIndex >= 0 && tabIndex < tabTitles.length) {
			currentTab.value = tabIndex
		}
	}
})


const onSwiperChange = (e) => {
	const index = e.detail.current
	currentTab.value = index
	updateTitle(index)
}

const onTabClick = (index) => {
	currentTab.value = index
	// swiper :current automatically syncs
}

onMounted(() => {
	// Initialize title
	updateTitle(currentTab.value)
})

// Optional: specific logic when tab changes
watch(currentTab, (newVal) => {
	// Can trigger refresh or analytics here
})
</script>

<style>
.main-page {
	width: 100%;
	height: 100vh;
	overflow: hidden;
	position: relative;
}

.main-swiper {
	width: 100%;
	height: 100vh;
}
</style>
