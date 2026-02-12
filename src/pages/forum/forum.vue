<template>
	<view class="forum-container" :class="themeClass">
		<!-- 顶部搜索栏 -->
		<view class="search-bar">
			<view class="search-box">
				<image src="/static/icon/search.png" class="search-icon-image"></image>
				<input type="text" v-model="searchTitle" placeholder="搜索话题" class="search-input" @input="handleSearch" />
				<text v-if="searchTitle" class="clear-icon" @click="clearSearch">✕</text>
			</view>
		</view>
		
		<!-- 功能模块选择 -->
		<view class="module-tabs">
			<view class="module-item" @click="navigateToMyPosts">
				<image src="/static/icon/post.png" class="module-icon"></image>
				<text class="module-text">我的帖子</text>
			</view>
			<view class="module-item" @click="navigateToMyMessages">
				<image src="/static/icon/messageList.png" class="module-icon"></image>
				<text class="module-text">我的消息</text>
			</view>
			<view class="module-item" @click="navigateToLikedPosts">
				<image src="https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/star.png" class="module-icon"></image>
				<text class="module-text">点赞帖子</text>
			</view>
			<view class="module-item" @click="goToPublish">
				<view class="publish-icon-wrapper">
					<text class="publish-mini-icon">+</text>
				</view>
				<text class="module-text">发布</text>
			</view>
		</view>
		
		<!-- 内容选项卡 -->
		<view class="content-tabs">
			<view 
				class="tab-item" 
				v-for="(tab, index) in tabs" 
				:key="index" 
				:class="{ active: currentTab === index }"
				@click="switchTab(index)"
			>
				<text class="tab-text">{{ tab.name }}</text>
			</view>
		</view>
		
		<!-- 论坛列表 -->
		<scroll-view class="forum-list" scroll-y @scrolltolower="loadMoreData"
			refresher-enabled
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh"
		>
			<!-- 加载中状态 -->
			<view class="loading-wrapper" v-if="isFirstLoading">
				<view class="loading-animation"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 列表内容 -->
			<view v-else>
				<!-- 全部内容（默认） -->
				<view v-if="currentTab === 0">
					<!-- 不规则布局的论坛列表 -->
					<view class="forum-grid">
						<template v-for="(item, index) in forumList" :key="index">
						<!-- 每5个帖子后插入一个活动广告 -->
						<view 
							v-if="index > 0 && index % 5 === 0 && getActivityForIndex(index)"
							class="forum-card card-activity"
							@click="goToActivityDetail(getActivityForIndex(index))"
						>
							<image 
								:src="getActivityForIndex(index).image" 
								class="activity-cover" 
								mode="aspectFill"
							></image>
							<view class="activity-content">
								<text class="activity-tag">{{ getActivityForIndex(index).type }}</text>
								<text class="activity-title">{{ getActivityForIndex(index).title }}</text>
								<text class="activity-desc">{{ getActivityForIndex(index).content }}</text>
								<view v-if="getActivityForIndex(index).url" class="activity-url-indicator">
									<text>查看详情</text>
									<image src="/static/icon/arrow-right.png" class="activity-url-indicator-icon"></image>
								</view>
							</view>
						</view>
						
						<!-- 普通帖子卡片 -->
						<view 
							:class="getCardClass(item, index)"
							@click="goToForumDetail(item.id)"
						>
							<!-- 封面图片（如果有） -->
							<template v-if="item.imageUrl && item.imageUrl.length > 0">
								<!-- 大卡片展示最多两张图片 -->
								<view v-if="index % 5 === 0 && item.imageUrl.length > 1" class="multi-image-container">
									<image :src="item.imageUrl[0]" class="card-cover" mode="aspectFill"></image>
									<image :src="item.imageUrl[1]" class="card-cover second-image" mode="aspectFill"></image>
								</view>
								<!-- 其他卡片只展示一张图片 -->
								<image v-else :src="item.imageUrl[0]" class="card-cover" mode="aspectFill"></image>
							</template>
							
							<!-- 内容区域 -->
							<view class="card-content">
								<!-- 标题和简短内容 -->
								<text class="card-title" :class="{'title-with-image': item.imageUrl && item.imageUrl.length > 0}">{{ item.title }}</text>
								<text class="card-summary" :class="{'summary-with-image': item.imageUrl && item.imageUrl.length > 0}">{{ item.content }}</text>
								
								<!-- 底部信息 -->
								<view class="card-footer">
									<!-- 作者头像和名称 -->
									<view class="card-author">
										<image :src="item.avatar || defaultAvatar" class="author-mini-avatar"></image>
										<text class="author-mini-name">{{ item.userName || '用户' + item.userId }}</text>
									</view>
									
									<!-- 统计信息 -->
									<view class="card-stats">
										<view class="stat-mini-item time-item">
											<text class="time-text">{{ formatTime(item.createdTime) }}</text>
										</view>
										<view class="stat-mini-item">
											<image src="/static/icon/look.png" class="icon-mini"></image>
											<text>{{ item.look || 0 }}</text>
										</view>
										<view class="stat-mini-item">
											<image src="/static/icon/liked.png" class="icon-mini"></image>
											<text>{{ item.likes || 0 }}</text>
										</view>
									</view>
								</view>
							</view>
						</view>
					</template>
				</view>
				
				<!-- 上拉加载更多 -->
				<view class="load-more" v-if="forumList.length > 0">
					<text v-if="isLoading" class="loading-text">加载中...</text>
					<text v-else-if="hasMore" class="more-text">上拉加载更多</text>
					<text v-else class="no-more-text">没有更多数据了</text>
				</view>
				
				<!-- 无数据状态 -->
				<view class="empty-state" v-if="forumList.length === 0">
					<image class="empty-icon" src="/static/icon/discussion.png" mode="aspectFit"></image>
					<text class="empty-text">{{ searchTitle ? '未找到相关话题' : '暂无论坛话题' }}</text>
					<text class="empty-tip">{{ searchTitle ? '换个关键词试试' : '来发布第一个话题吧' }}</text>
				</view>
				</view>
				
				<!-- 本周热门列表 -->
				<view v-else-if="currentTab === 1">
					<view class="forum-grid">
						<view 
							v-for="(item, index) in weeklyHotList" 
							:key="index"
							:class="getCardClass(item, index)"
							@click="goToForumDetail(item.id)"
						>
							<view class="hot-ranking">{{ index + 1 }}</view>
							<!-- 封面图片（如果有） -->
							<template v-if="item.imageUrl && item.imageUrl.length > 0">
								<!-- 大卡片展示最多两张图片 -->
								<view v-if="index % 5 === 0 && item.imageUrl.length > 1" class="multi-image-container">
									<image :src="item.imageUrl[0]" class="card-cover" mode="aspectFill"></image>
									<image :src="item.imageUrl[1]" class="card-cover second-image" mode="aspectFill"></image>
								</view>
								<!-- 其他卡片只展示一张图片 -->
								<image v-else :src="item.imageUrl[0]" class="card-cover" mode="aspectFill"></image>
							</template>
							
							<!-- 内容区域 -->
							<view class="card-content">
								<!-- 标题和简短内容 -->
								<text class="card-title" :class="{'title-with-image': item.imageUrl && item.imageUrl.length > 0}">{{ item.title }}</text>
								<text class="card-summary" :class="{'summary-with-image': item.imageUrl && item.imageUrl.length > 0}">{{ item.content }}</text>
								
								<!-- 底部信息 -->
								<view class="card-footer">
									<!-- 作者头像和名称 -->
									<view class="card-author">
										<image :src="item.avatar || defaultAvatar" class="author-mini-avatar"></image>
										<text class="author-mini-name">{{ item.userName || '用户' + item.userId }}</text>
									</view>
									
									<!-- 统计信息 -->
									<view class="card-stats">
										<view class="stat-mini-item time-item">
											<text class="time-text">{{ formatTime(item.createdTime) }}</text>
										</view>
										<view class="stat-mini-item">
											<image src="/static/icon/look.png" class="icon-mini"></image>
											<text>{{ item.look || 0 }}</text>
										</view>
										<view class="stat-mini-item">
											<image src="/static/icon/liked.png" class="icon-mini"></image>
											<text>{{ item.likes || 0 }}</text>
										</view>
									</view>
								</view>
							</view>
						</view>
					</view>
					
					<!-- 无数据状态 -->
					<view class="empty-state" v-if="weeklyHotList.length === 0">
						<image class="empty-icon" src="/static/icon/hot.png" mode="aspectFit"></image>
						<text class="empty-text">暂无本周热门帖子</text>
					</view>
				</view>
				
				<!-- 本月热门列表 -->
				<view v-else-if="currentTab === 2">
					<view class="forum-grid">
						<view 
							v-for="(item, index) in monthlyHotList" 
							:key="index"
							:class="getCardClass(item, index)"
							@click="goToForumDetail(item.id)"
						>
							<view class="hot-ranking">{{ index + 1 }}</view>
							<!-- 封面图片（如果有） -->
							<template v-if="item.imageUrl && item.imageUrl.length > 0">
								<!-- 大卡片展示最多两张图片 -->
								<view v-if="index % 5 === 0 && item.imageUrl.length > 1" class="multi-image-container">
									<image :src="item.imageUrl[0]" class="card-cover" mode="aspectFill"></image>
									<image :src="item.imageUrl[1]" class="card-cover second-image" mode="aspectFill"></image>
								</view>
								<!-- 其他卡片只展示一张图片 -->
								<image v-else :src="item.imageUrl[0]" class="card-cover" mode="aspectFill"></image>
							</template>
							
							<!-- 内容区域 -->
							<view class="card-content">
								<!-- 标题和简短内容 -->
								<text class="card-title" :class="{'title-with-image': item.imageUrl && item.imageUrl.length > 0}">{{ item.title }}</text>
								<text class="card-summary" :class="{'summary-with-image': item.imageUrl && item.imageUrl.length > 0}">{{ item.content }}</text>
								
								<!-- 底部信息 -->
								<view class="card-footer">
									<!-- 作者头像和名称 -->
									<view class="card-author">
										<image :src="item.avatar || defaultAvatar" class="author-mini-avatar"></image>
										<text class="author-mini-name">{{ item.userName || '用户' + item.userId }}</text>
									</view>
									
									<!-- 统计信息 -->
									<view class="card-stats">
										<view class="stat-mini-item time-item">
											<text class="time-text">{{ formatTime(item.createdTime) }}</text>
										</view>
										<view class="stat-mini-item">
											<image src="/static/icon/look.png" class="icon-mini"></image>
											<text>{{ item.look || 0 }}</text>
										</view>
										<view class="stat-mini-item">
											<image src="/static/icon/liked.png" class="icon-mini"></image>
											<text>{{ item.likes || 0 }}</text>
										</view>
									</view>
								</view>
							</view>
						</view>
					</view>
					
					<!-- 无数据状态 -->
					<view class="empty-state" v-if="monthlyHotList.length === 0">
						<image class="empty-icon" src="/static/icon/hot.png" mode="aspectFit"></image>
						<text class="empty-text">暂无本月热门帖子</text>
					</view>
				</view>
				
				<!-- 活动广告列表 -->
				<view v-else-if="currentTab === 3">
					<view class="activity-full-list">
						<view 
							class="activity-item" 
							v-for="(item, index) in activityList" 
							:key="index"
							@click="goToActivityDetail(item)"
						>
							<image :src="item.image" class="activity-image" mode="aspectFill"></image>
							<view class="activity-content">
								<view class="activity-header">
									<text class="activity-title">{{ item.title }}</text>
									<view class="activity-tag">{{ item.type }}</view>
								</view>
								<text class="activity-desc">{{ item.content }}</text>
								<view class="activity-footer">
									<text class="activity-time">{{ formatTime(item.createdTime) }}</text>
									<view class="activity-url-indicator">
										<text>查看详情</text>
										<image src="/static/icon/arrow-right.png" class="activity-url-indicator-icon"></image>
									</view>
								</view>
							</view>
						</view>
					</view>
					
					<!-- 无数据状态 -->
					<view class="empty-state" v-if="activityList.length === 0">
						<image class="empty-icon" src="/static/icon/activity.png" mode="aspectFit"></image>
						<text class="empty-text">暂无活动广告</text>
					</view>
				</view>
			</view>
		</scroll-view>
		

	</view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request.js'
import { useTheme } from '@/hooks/useTheme'

const { themeClass } = useTheme()

// 选项卡定义
const tabs = [
	{ name: '全部' },
	{ name: '本周热门' },
	{ name: '本月热门' },
	{ name: '活动广告' }
]
const currentTab = ref(0)

// 论坛列表数据
const forumList = ref([])
// 本周热门帖子
const weeklyHotList = ref([])
// 本月热门帖子
const monthlyHotList = ref([])
// 活动广告列表
const activityList = ref([])
// 当前活动索引
const currentActivityIndex = ref(0)
// 搜索关键词
const searchTitle = ref('')
// 页码
const pageNum = ref(1)
// 活动页码
const activityPageNum = ref(1)
// 是否有更多数据
const hasMore = ref(true)
// 是否正在加载
const isLoading = ref(false)
// 是否为首次加载
const isFirstLoading = ref(true)
// 定时器，用于搜索防抖
let searchTimer = null
// 默认头像
const defaultAvatar = '/static/user.png'
// 下拉刷新状态
const isRefreshing = ref(false)

// 切换选项卡
const switchTab = (index) => {
	if (currentTab.value === index) return
	
	currentTab.value = index
	isFirstLoading.value = true
	
	// 根据选中的选项卡加载相应的数据
	switch(index) {
		case 0:
			if (forumList.value.length === 0) {
				getForumList(true)
			} else {
				isFirstLoading.value = false
			}
			break
		case 1:
			if (weeklyHotList.value.length === 0) {
				getWeeklyHotPosts()
			} else {
				isFirstLoading.value = false
			}
			break
		case 2:
			if (monthlyHotList.value.length === 0) {
				getMonthlyHotPosts()
			} else {
				isFirstLoading.value = false
			}
			break
		case 3:
			if (activityList.value.length === 0) {
				getActivityList()
			} else {
				isFirstLoading.value = false
			}
			break
	}
}

// 获取论坛列表数据
const getForumList = async (isRefresh = false) => {
	if (isLoading.value) return
	
	isLoading.value = true
	if (isRefresh) {
		pageNum.value = 1
		hasMore.value = true
	}
	
	try {
		const params = {
			pageNum: pageNum.value,
            title: ""
		}
		
		// 如果有搜索关键词，添加到请求参数
		if (searchTitle.value) {
			params.title = searchTitle.value
		}
		
		const res = await request('/forum/list', {
			method: 'GET',
			data: params
		})
		
		if (res.code === 200) {
			let list = res.data || []
			
			// 如果是刷新，替换列表，否则追加
			if (isRefresh) {
				forumList.value = list
			} else {
				forumList.value = [...forumList.value, ...list]
			}
			
			// 判断是否还有更多数据
			if (list.length < 10) {
				hasMore.value = false
			} else {
				pageNum.value++
			}
			
			// 如果活动列表为空，则获取活动列表
			if (activityList.value.length === 0) {
				getActivityList()
			}
		} else {
			uni.showToast({
				title: res.msg || '获取论坛列表失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取论坛列表失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
		isFirstLoading.value = false
	}
}

// 获取活动广告列表
const getActivityList = async () => {
	try {
		const res = await request('/activity/list', {
			method: 'GET',
			data: {
				pageNum: activityPageNum.value
			}
		})
		
		if (res.code === 200) {
			let list = res.data || []
			activityList.value = [...activityList.value, ...list]
			
			// 如果获取到的活动少于5条，则重置页码以便下次从头开始获取
			if (list.length < 5) {
				activityPageNum.value = 1
			} else {
				activityPageNum.value++
			}
		} else {
			console.error('获取活动列表失败:', res.msg)
		}
	} catch (error) {
		console.error('获取活动列表失败:', error)
	} finally {
		if (currentTab.value === 3) {
			isLoading.value = false
			isFirstLoading.value = false
		}
	}
}

// 获取当前应该显示的活动
const getActivityForIndex = (index) => {
	if (activityList.value.length === 0) return null
	
	// 计算应该使用哪个活动
	const activityIndex = Math.floor(index / 5) % activityList.value.length
	return activityList.value[activityIndex]
}

// 跳转到活动详情
const goToActivityDetail = (activity) => {
	// 如果有URL，则跳转到URL
	if (activity.url) {
		// 检查URL是否为外部链接(http或https开头)
		if (activity.url.startsWith('http://') || activity.url.startsWith('https://')) {
			// 外部链接，使用系统浏览器打开
			// #ifdef H5
			window.open(activity.url, '_blank');
			// #endif
			
			// #ifdef APP-PLUS
			plus.runtime.openURL(activity.url);
			// #endif
			
			// #ifdef MP
			uni.setClipboardData({
				data: activity.url,
				success: () => {
					uni.showToast({
						title: '链接已复制，请在浏览器中打开',
						icon: 'none'
					});
				}
			});
			// #endif
		} else {
			// 内部页面，直接导航
			uni.navigateTo({
				url: activity.url
			});
		}
	} else {
		// 没有URL，跳转到活动详情页
		uni.navigateTo({
			url: activity.url
		});
	}
}

// 获取本周热门帖子
const getWeeklyHotPosts = async () => {
	isLoading.value = true
	
	try {
		const res = await request('/forum/weeklyHot', {
			method: 'GET'
		})
		
		if (res.code === 200) {
			weeklyHotList.value = res.data || []
		} else {
			uni.showToast({
				title: res.msg || '获取本周热门失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取本周热门失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
		isFirstLoading.value = false
	}
}

// 获取本月热门帖子
const getMonthlyHotPosts = async () => {
	isLoading.value = true
	
	try {
		const res = await request('/forum/monthlyHot', {
			method: 'GET'
		})
		
		if (res.code === 200) {
			monthlyHotList.value = res.data || []
		} else {
			uni.showToast({
				title: res.msg || '获取本月热门失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取本月热门失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
		isFirstLoading.value = false
	}
}

// 加载更多数据
const loadMoreData = () => {
	// 只有全部内容选项卡支持分页加载更多
	if (currentTab.value === 0 && hasMore.value && !isLoading.value) {
		getForumList()
	}
}

// 处理搜索输入
const handleSearch = () => {
	// 防抖处理
	if (searchTimer) clearTimeout(searchTimer)
	
	searchTimer = setTimeout(() => {
		// 搜索时切换到全部选项卡
		currentTab.value = 0
		getForumList(true)
	}, 500)
}

// 清除搜索
const clearSearch = () => {
	searchTitle.value = ''
	getForumList(true)
}

// 跳转到论坛详情
const goToForumDetail = (id) => {
	uni.navigateTo({
		url: `/pages/forum/detail?id=${id}`
	})
}

// 跳转到发布页面
const goToPublish = () => {
	uni.navigateTo({
		url: '/pages/forum/publish'
	})
}

// 格式化时间
const formatTime = (timestamp) => {
	if (!timestamp) return ''
	
	// 处理后端返回的LocalDateTime格式 (ISO-8601)
	let date
	if (typeof timestamp === 'string') {
		date = new Date(timestamp)
	} else {
		date = new Date(timestamp)
	}
	
	const now = new Date().getTime()
	const diff = now - date.getTime()
	
	// 一分钟内
	if (diff < 60 * 1000) {
		return '刚刚'
	}
	// 一小时内
	else if (diff < 60 * 60 * 1000) {
		return Math.floor(diff / (60 * 1000)) + '分钟前'
	}
	// 一天内
	else if (diff < 24 * 60 * 60 * 1000) {
		return Math.floor(diff / (60 * 60 * 1000)) + '小时前'
	}
	// 一周内
	else if (diff < 7 * 24 * 60 * 60 * 1000) {
		return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前'
	}
	// 超过一周
	else {
		return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
	}
}

// 获取用户信息 - 根据userId获取用户详细信息
const getUserInfo = async (userId) => {
	try {
		const res = await request('/user/getUserInfo', {
			method: 'GET',
			data: { userId }
		})
		return res.code === 200 ? res.data : null
	} catch (error) {
		console.error('获取用户信息失败:', error)
		return null
	}
}

// 页面加载时获取论坛列表
onMounted(() => {
	getForumList()
	
	// 监听发布成功事件，刷新列表
	uni.$on('refreshForumList', () => {
		getForumList(true)
	})
})

// 页面卸载时移除事件监听
onUnmounted(() => {
	uni.$off('refreshForumList')
})

// 导航到我的帖子页面
const navigateToMyPosts = () => {
	uni.navigateTo({
		url: '/pages/forum/mypost'
	})
}

// 导航到我的消息页面
const navigateToMyMessages = () => {
	uni.navigateTo({
		url: '/pages/forum/message'
	})
}

// 导航到点赞帖子页面
const navigateToLikedPosts = () => {
	uni.navigateTo({
		url: '/pages/forum/mylike'
	})
}

// 获取卡片类名
const getCardClass = (item, index) => {
	let classNames = ['forum-card'];
	
	if (item.imageUrl && item.imageUrl.length > 0) {
		if (index % 5 === 0) {
			classNames.push('card-large');
		} else if (index % 3 === 0) {
			classNames.push('card-medium');
		} else {
			classNames.push('card-small');
		}
	} else {
		classNames.push('card-text-only');
	}
	
	// 检测内容是否只有一行，如果是，添加单行内容样式
	if (isContentSingleLine(item.content)) {
		classNames.push('single-line-content');
	}
	
	return classNames;
}

// 检测内容是否只有一行
const isContentSingleLine = (content) => {
	if (!content) return true;
	
	// 简单判断：如果内容少于25个字符，或者不包含换行符，则认为是单行
	return content.length < 25 || !content.includes('\n');
}

// 下拉刷新
const onRefresh = async () => {
	isRefreshing.value = true
	await getForumList(true)
	isRefreshing.value = false
}
</script>

<style>
.forum-container {
	background-color: var(--bgColor1);
	min-height: 100vh;
	position: relative;
	padding-bottom: 30rpx;
}

/* 内容选项卡样式 */
.content-tabs {
	display: flex;
	justify-content: space-around;
	background-color: #fff;
	padding: 12rpx 0;
	margin-bottom: 15rpx;
	border-bottom: 1rpx solid var(--borderColor);
}

.darkMode .content-tabs {
	background-color: var(--bgColor2);
}

.tab-item {
	padding: 8rpx 20rpx;
	position: relative;
}

.tab-text {
	font-size: 28rpx;
	color: var(--textColor3);
	transition: all 0.3s;
}

.tab-item.active .tab-text {
	color: var(--themeColor1);
	font-weight: bold;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: -8rpx;
	left: 50%;
	transform: translateX(-50%);
	width: 40rpx;
	height: 4rpx;
	background-color: #1abc9c;
	border-radius: 2rpx;
}

/* 搜索栏样式 */
.search-bar {
	background-color: #fff;
	padding: 20rpx 30rpx;
	position: sticky;
	top: 0;
	z-index: 10;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.darkMode .search-bar {
	background-color: var(--bgColor2);
	box-shadow: none;
}

.search-box {
	display: flex;
	align-items: center;
	background-color: var(--bgColor1);
	border-radius: 50rpx;
	padding: 12rpx 24rpx;
}

.darkMode .search-box {
	background-color: var(--bgColor3);
}

.search-icon-image {
	width: 36rpx;
	height: 36rpx;
	margin-right: 10rpx;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: var(--textColor1);
}

.clear-icon {
	font-size: 28rpx;
	color: #999;
	padding: 0 10rpx;
}

/* 功能模块选择样式 */
.module-tabs {
	display: flex;
	justify-content: space-around;
	background-color: #fff;
	padding: 20rpx 0;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.darkMode .module-tabs {
	background-color: var(--bgColor2);
	box-shadow: none;
}

.module-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 10rpx 0;
}

.module-icon {
	width: 48rpx;
	height: 48rpx;
	margin-bottom: 10rpx;
}

.module-text {
	font-size: 24rpx;
	color: var(--textColor1);
}

/* 论坛列表样式 - 新的网格布局 */
.forum-list {
	padding: 10rpx;
	height: calc(100vh - 230rpx);
}

/* 热门排名样式 */
.hot-ranking {
	position: absolute;
	top: 10rpx;
	left: 10rpx;
	width: 40rpx;
	height: 40rpx;
	background-color: rgba(26, 188, 156, 0.8);
	color: #fff;
	font-size: 28rpx;
	font-weight: bold;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 2;
}

/* 前三名特殊样式 */
.forum-card:nth-child(1) .hot-ranking {
	background-color: #ff6b6b;
}

.forum-card:nth-child(2) .hot-ranking {
	background-color: #ff922b;
}

.forum-card:nth-child(3) .hot-ranking {
	background-color: #ffd43b;
}

/* 不规则网格布局 */
.forum-grid {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	padding: 10rpx;
	width: 94%;
	margin: 0 auto;
	position: relative;
}

/* 清除浮动，确保布局正确 */
.forum-grid::after {
	content: "";
	display: table;
	clear: both;
}

/* 确保大卡片后面的小卡片正确排列 */
.card-large + .card-small,
.card-large + .card-medium,
.card-large + .card-text-only {
	clear: left;
}

/* 活动卡片也需要清除浮动 */
.card-activity {
	clear: both;
	width: 100%;
}

/* 卡片基础样式 */
.forum-card {
	background-color: #fff;
	border-radius: 12rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	overflow: hidden;
	position: relative;
	transition: all 0.3s ease;
}

.darkMode .forum-card {
	background-color: var(--bgColor2);
	box-shadow: none;
}

.forum-card:active {
	transform: scale(0.98);
	opacity: 0.9;
}

/* 大卡片占据整行 */
.card-large {
	width: 100%;
	height: 500rpx;
	clear: both;
}

/* 小卡片和中卡片使用浮动布局 */
.card-small, .card-medium, .card-text-only {
	width: calc(50% - 10rpx);
	float: left;
}

.card-small:nth-child(2n), .card-medium:nth-child(2n), .card-text-only:nth-child(2n) {
	float: right;
}

.card-small {
	height: 420rpx;
}

.card-medium {
	height: 460rpx;
}

.card-text-only {
	height: 300rpx;
}

/* 卡片封面图片 */
.card-cover {
	width: 100%;
	height: 50%;
	object-fit: cover;
}

/* 卡片内容区域 */
.card-content {
	padding: 15rpx 18rpx;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	height: 50%;
	box-sizing: border-box;
}

/* 为小卡片和中卡片特别优化内容区域 */
.card-small .card-content,
.card-medium .card-content {
	padding: 12rpx 15rpx;
}

.card-text-only .card-content {
	height: 100%;
	padding: 20rpx;
}

.card-title {
	font-size: 28rpx;
	font-weight: bold;
	color: var(--textColor1);
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 1;
	-webkit-box-orient: vertical;
	line-height: 1.4;
	margin-bottom: 10rpx;
}

.title-with-image {
	-webkit-line-clamp: 1;
}

.card-summary {
	font-size: 24rpx;
	color: var(--textColor3);
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	margin-top: 2rpx;
	line-height: 1.5;
	height: 72rpx;
	min-height: 72rpx;
}

.summary-with-image {
	-webkit-line-clamp: 2;
	margin-bottom: 8rpx;
	height: 72rpx;
}

/* 卡片底部信息 */
.card-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 10rpx;
	padding-top: 6rpx;
}

.card-author {
	display: flex;
	align-items: center;
	flex: 1;
	overflow: hidden;
}

.author-mini-avatar {
	width: 36rpx;
	height: 36rpx;
	border-radius: 50%;
	margin-right: 8rpx;
	flex-shrink: 0;
}

.author-mini-name {
	font-size: 22rpx;
	color: var(--textColor3);
	max-width: 120rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.card-stats {
	display: flex;
	flex-shrink: 0;
}

.stat-mini-item {
	font-size: 22rpx;
	color: var(--textColor3);
	margin-left: 12rpx;
	display: flex;
	align-items: center;
}

.icon-mini {
	width: 24rpx;
	height: 24rpx;
	margin-right: 4rpx;
	display: inline-block;
	vertical-align: middle;
}

/* 加载状态 */
.loading-wrapper {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 60rpx 0;
}

.loading-animation {
	width: 40rpx;
	height: 40rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #1890ff;
	border-radius: 50%;
	animation: spin 1s linear infinite;
	margin-bottom: 20rpx;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text, .more-text, .no-more-text {
	font-size: 24rpx;
	color: var(--textColor3);
	text-align: center;
	padding: 20rpx 0;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
}

.empty-icon {
	width: 200rpx;
	height: 200rpx;
	margin-bottom: 30rpx;
}

.empty-text {
	font-size: 30rpx;
	color: var(--textColor2);
	margin-bottom: 10rpx;
}

.empty-tip {
	font-size: 26rpx;
	color: var(--textColor3);
}

/* 发布按钮样式 */
.publish-icon-wrapper {
	width: 48rpx;
	height: 48rpx;
	background-color: #1abc9c;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 10rpx;
	box-shadow: 0 2rpx 6rpx rgba(26, 188, 156, 0.3);
}

.publish-mini-icon {
	font-size: 32rpx;
	color: #fff;
	line-height: 32rpx;
}

/* 活动广告卡片样式 */
.card-activity {
	width: 100%;
	height: 240rpx;
	display: flex;
	flex-direction: row;
	margin-bottom: 20rpx;
	position: relative;
	overflow: hidden;
	background: linear-gradient(135deg, #ffffff, #e8f8f5, #d4f1ed);
	border-radius: 16rpx;
	box-shadow: 0 4rpx 12rpx rgba(26, 188, 156, 0.15);
	border-left: 6rpx solid var(--themeColor1);
	transition: all 0.3s ease;
}

.darkMode .card-activity {
	background: var(--bgColor2);
	border-left: 6rpx solid var(--themeColor1);
	box-shadow: none;
}

.card-activity:active {
	transform: scale(0.98);
	box-shadow: 0 2rpx 8rpx rgba(26, 188, 156, 0.1);
}

.activity-cover {
	width: 200rpx;
	height: 100%;
	object-fit: cover;
	border-top-right-radius: 30rpx;
	border-bottom-right-radius: 30rpx;
}

.activity-content {
	flex: 1;
	padding: 20rpx 25rpx;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	position: relative;
}

.activity-tag {
	position: absolute;
	top: 10rpx;
	right: 10rpx;
	background: linear-gradient(90deg, var(--themeColor1), var(--themeColor2));
	color: white;
	color: white;
	font-size: 20rpx;
	padding: 4rpx 16rpx;
	border-radius: 10rpx;
	box-shadow: 0 2rpx 6rpx rgba(26, 188, 156, 0.3);
	font-weight: bold;
	letter-spacing: 2rpx;
}

.activity-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 12rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 1;
	-webkit-box-orient: vertical;
}

.activity-desc {
	font-size: 24rpx;
	color: var(--textColor3);
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
	line-height: 1.5;
	margin-bottom: 30rpx;
}

/* 添加URL指示器 */
.activity-url-indicator {
	position: absolute;
	bottom: 10rpx;
	right: 15rpx;
	display: flex;
	align-items: center;
	font-size: 22rpx;
	color: #1abc9c;
}

.activity-url-indicator-icon {
	width: 24rpx;
	height: 24rpx;
	margin-left: 6rpx;
}

/* 活动全列表样式 */
.activity-full-list {
	padding: 20rpx;
}

.activity-full-list .activity-item {
	background-color: #fff;
	border-radius: 12rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	overflow: hidden;
}

.darkMode .activity-full-list .activity-item {
	background-color: var(--bgColor2);
	box-shadow: none;
}

.activity-full-list .activity-image {
	width: 100%;
	height: 320rpx;
	object-fit: cover;
}

.activity-full-list .activity-content {
	padding: 20rpx;
}

.activity-full-list .activity-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 15rpx;
}

.activity-full-list .activity-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	flex: 1;
}

.activity-full-list .activity-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 15rpx;
	border-top: 1rpx solid var(--borderColor);
	margin-top: 15rpx;
}

.activity-full-list .activity-time {
	font-size: 24rpx;
	color: var(--textColor3);
}

/* 多图片容器样式 */
.multi-image-container {
	position: relative;
	width: 100%;
	height: 50%;
	display: flex;
	overflow: hidden;
}

.multi-image-container .card-cover {
	width: 50%;
	height: 100%;
	object-fit: cover;
}

.multi-image-container .second-image {
	border-left: 2rpx solid #fff;
}

.darkMode .multi-image-container .second-image {
	border-left: 2rpx solid var(--bgColor2);
}

/* 为小卡片和中卡片特别优化底部信息 */
.card-small .card-footer,
.card-medium .card-footer {
	margin-top: 8rpx;
	padding-top: 4rpx;
}

.card-small .author-mini-name,
.card-medium .author-mini-name {
	max-width: 80rpx;
}

.card-small .author-mini-avatar,
.card-medium .author-mini-avatar {
	width: 32rpx;
	height: 32rpx;
	margin-right: 6rpx;
}

.card-small .stat-mini-item,
.card-medium .stat-mini-item {
	margin-left: 8rpx;
}

.card-small .icon-mini,
.card-medium .icon-mini {
	width: 22rpx;
	height: 22rpx;
}

.card-small .card-title,
.card-medium .card-title {
	margin-bottom: 6rpx;
	font-size: 26rpx;
}

.card-small .card-summary,
.card-medium .card-summary {
	line-height: 1.4;
}

/* 时间显示样式 */
.time-item {
	margin-right: 8rpx;
}

.time-text {
	font-size: 20rpx;
	color: var(--textColor3);
	white-space: nowrap;
}

/* 小卡片和中卡片的时间显示优化 */
.card-small .time-text,
.card-medium .time-text {
	max-width: 80rpx;
	overflow: hidden;
	text-overflow: ellipsis;
}

/* 单行内容的卡片高度调整 */
.single-line-content.card-large {
	height: 440rpx;
}

.single-line-content.card-medium {
	height: 400rpx;
}

.single-line-content.card-small {
	height: 360rpx;
}

.single-line-content.card-text-only {
	height: 240rpx;
}

/* 单行内容的摘要样式 */
.single-line-content .card-summary {
	-webkit-line-clamp: 1;
	height: 36rpx;
	min-height: 36rpx;
	margin-bottom: 0;
}

/* 单行内容的内容区域调整 */
.single-line-content .card-content {
	justify-content: flex-start;
}

.single-line-content .card-footer {
	margin-top: auto;
}

/* 单行内容卡片的图片高度调整 */
.single-line-content .card-cover,
.single-line-content .multi-image-container {
	height: 60%;
}

.single-line-content .card-content {
	height: 40%;
}

/* 单行内容卡片的标题和底部信息间距优化 */
.single-line-content .card-title {
	margin-bottom: 6rpx;
}

.single-line-content.card-small .card-content,
.single-line-content.card-medium .card-content {
	padding: 10rpx 15rpx;
}

.single-line-content .card-footer {
	padding-top: 4rpx;
	margin-top: 6rpx;
}
</style>
