<template>
	<view class="detail-container" :class="themeClass">
		<!-- 添加装饰性背景元素 -->
		<view class="decorative-background">
			<view class="floating-circle" v-for="i in 5" :key="'circle-'+i"></view>
			<view class="floating-square" v-for="i in 5" :key="'square-'+i"></view>
		</view>
		
		<!-- 帖子详情 -->
		<scroll-view class="detail-content" scroll-y>
			<!-- 加载中状态 -->
			<view class="loading-wrapper" v-if="isLoading">
				<view class="loading-animation"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 帖子内容 -->
			<view v-else-if="forumDetail">
				<view class="forum-card">
					<!-- 帖子头部信息 -->
					<view class="forum-header">
						<view class="author-info">
							<view class="avatar-wrapper" @click="goToUserProfile(forumDetail.userId)">
								<image :src="forumDetail.avatar || defaultAvatar" class="author-avatar" mode="aspectFill"></image>
								<!-- 头像框 -->
								<image v-if="forumDetail.avatarFrameUrl" :src="forumDetail.avatarFrameUrl" class="avatar-frame" mode="aspectFit"></image>
							</view>
							<view class="author-detail">
								<view class="author-name-wrapper">
									<text class="author-name">{{ forumDetail.userName || '用户' + forumDetail.userId }}</text>
									<view class="user-level" :class="'level-' + (forumDetail.level || 1)">
										<text class="level-text">Lv.{{ forumDetail.level || 1 }}</text>
									</view>
								</view>
								<text class="publish-time">{{ formatTime(forumDetail.createdTime) }}</text>
							</view>
						</view>
					</view>
					
					<!-- 帖子标题和内容 -->
					<view class="forum-body">
						<text class="forum-title">{{ forumDetail.title }}</text>
						<text class="forum-content">{{ forumDetail.content }}</text>
						
						<!-- 图片画廊 -->
						<view class="image-gallery" v-if="forumDetail.imageUrl && forumDetail.imageUrl.length > 0">
							<view class="gallery-grid">
								<!-- 单张图片 -->
								<template v-if="forumDetail.imageUrl.length === 1">
									<view class="gallery-item single-image">
										<image 
											:src="forumDetail.imageUrl[0]" 
											class="gallery-image" 
											mode="aspectFill"
											@click="previewImage(0)"
										></image>
									</view>
								</template>
								
								<!-- 两张图片 -->
								<template v-else-if="forumDetail.imageUrl.length === 2">
									<view class="gallery-item half-width" v-for="(img, index) in forumDetail.imageUrl" :key="index">
										<image 
											:src="img" 
											class="gallery-image" 
											mode="aspectFill"
											@click="previewImage(index)"
										></image>
									</view>
								</template>
								
								<!-- 三张图片 -->
								<template v-else-if="forumDetail.imageUrl.length === 3">
									<view class="gallery-item full-width">
										<image 
											:src="forumDetail.imageUrl[0]" 
											class="gallery-image" 
											mode="aspectFill"
											@click="previewImage(0)"
										></image>
									</view>
									<view class="gallery-item half-width" v-for="index in 2" :key="index">
										<image 
											:src="forumDetail.imageUrl[index]" 
											class="gallery-image" 
											mode="aspectFill"
											@click="previewImage(index)"
										></image>
									</view>
								</template>
								
								<!-- 四张图片 -->
								<template v-else-if="forumDetail.imageUrl.length === 4">
									<view class="gallery-item half-width" v-for="(img, index) in forumDetail.imageUrl" :key="index">
										<image 
											:src="img" 
											class="gallery-image" 
											mode="aspectFill"
											@click="previewImage(index)"
										></image>
									</view>
								</template>
								
								<!-- 五张及以上图片 -->
								<template v-else>
									<view class="gallery-item full-width">
										<image 
											:src="forumDetail.imageUrl[0]" 
											class="gallery-image" 
											mode="aspectFill"
											@click="previewImage(0)"
										></image>
									</view>
									<view class="gallery-item half-width" v-for="index in 4" :key="index">
										<image 
											:src="forumDetail.imageUrl[index]" 
											class="gallery-image" 
											mode="aspectFill"
											@click="previewImage(index)"
										></image>
										<!-- 显示剩余图片数量 -->
										<view v-if="index === 4 && forumDetail.imageUrl.length > 5" class="more-images-overlay" @click.stop="previewImage(4)">
											<text class="more-images-text">+{{ forumDetail.imageUrl.length - 5 }}</text>
										</view>
									</view>
								</template>
							</view>
						</view>
					</view>
					
					<!-- 帖子底部数据 -->
					<view class="forum-footer">
						<view class="action-bar">
							<view class="action-btn" @click="toggleLike">
								<image :src="forumDetail.isLiked ? '/static/icon/liked.png' : '/static/icon/like.png'" 
									   class="action-icon-img"
									   :class="{'like-animation': likeAnimating}"></image>
								<text class="action-text">{{ forumDetail.likes || 0 }}</text>
							</view>
							<view class="action-btn" @click="focusCommentInput">
								<image src="/static/icon/reply.png" class="action-icon-img"></image>
								<text class="action-text">{{ commentList.length || 0 }}</text>
							</view>
							<view class="action-btn">
								<text class="action-icon">👁️</text>
								<text class="action-text">{{ forumDetail.look || 0 }}</text>
							</view>
						</view>
					</view>
				</view>
				
				<!-- 评论区 -->
				<view class="comment-section">
					<view class="section-title">
						<text class="title-text">评论 ({{ commentList.length || 0 }})</text>
					</view>
					
					<!-- 评论列表 -->
					<view class="comment-list">
						<view v-if="commentList.length === 0" class="empty-comment">
							<text class="empty-text">暂无评论，快来抢沙发吧~</text>
						</view>
						
						<template v-else>
							<!-- 父评论列表 -->
							<view v-for="(comment, index) in parentComments" :key="comment.id" class="comment-item">
								<!-- 父评论头部 -->
								<view class="comment-header">
									<view class="commenter-avatar-wrapper" @click="goToUserProfile(comment.userId)">
										<image :src="comment.avatar || defaultAvatar" class="commenter-avatar" mode="aspectFill"></image>
										<!-- 头像框 -->
										<image v-if="comment.avatarFrameUrl" :src="comment.avatarFrameUrl" class="avatar-frame-small" mode="aspectFit"></image>
									</view>
									<view class="comment-info">
										<view class="commenter-name-wrapper">
											<text class="commenter-name">{{ comment.userName || '用户' + comment.userId }}</text>
											<view class="user-level" :class="'level-' + (comment.level || 1)">
												<text class="level-text">Lv.{{ comment.level || 1 }}</text>
											</view>
										</view>
										<text class="comment-time">{{ formatTime(comment.createdTime) }}</text>
									</view>
								</view>
								
								<!-- 父评论内容 -->
								<view class="comment-body">
									<text class="comment-text">{{ comment.content }}</text>
									<!-- 评论图片独立显示 -->
									<view class="comment-image-container" v-if="comment.img">
										<image :src="comment.img" class="comment-image" mode="widthFix" @click="previewCommentImage(comment.img)"></image>
									</view>
								</view>
								
								<!-- 父评论底部操作 -->
								<view class="comment-footer">
									<view class="comment-actions">
										<view class="action-item" @click="toggleCommentLike(comment)">
											<image :src="comment.isLiked ? '/static/icon/liked.png' : '/static/icon/like.png'" 
												   class="comment-icon-img"
												   :class="{'like-animation': comment.likeAnimating}"></image>
											<text class="action-count">{{ comment.likes || 0 }}</text>
										</view>
										<view class="action-item" @click="replyComment(comment)">
											<image src="/static/icon/reply.png" class="comment-icon-img"></image>
											<text class="action-text">回复</text>
										</view>
										<view class="action-item" @click="reportComment(comment)">
											<image src="/static/icon/report.png" class="comment-icon-img"></image>
											<text class="action-text">举报</text>
										</view>
									</view>
								</view>
								
								<!-- 子评论列表 -->
								<view v-if="getChildComments(comment.id).length > 0" class="child-comments">
									<!-- 分页显示子评论 -->
									<view 
										v-for="(childComment, childIndex) in getPaginatedChildComments(comment.id)" 
										:key="childComment.id" 
										class="child-comment-item"
									>
										<!-- 子评论头部 -->
										<view class="comment-header">
											<view class="commenter-avatar-wrapper" @click="goToUserProfile(childComment.userId)">
												<image :src="childComment.avatar || defaultAvatar" class="commenter-avatar-small" mode="aspectFill"></image>
												<!-- 头像框 -->
												<image v-if="childComment.avatarFrameUrl" :src="childComment.avatarFrameUrl" class="avatar-frame-smaller" mode="aspectFit"></image>
											</view>
											<view class="comment-info">
												<view class="commenter-name-wrapper">
													<text class="commenter-name">{{ childComment.userName || '用户' + childComment.userId }}</text>
													<view class="user-level" :class="'level-' + (childComment.level || 1)">
														<text class="level-text">Lv.{{ childComment.level || 1 }}</text>
													</view>
												</view>
												<text class="comment-time">{{ formatTime(childComment.createdTime) }}</text>
											</view>
										</view>
										
										<!-- 子评论内容 -->
										<view class="comment-body">
											<text class="comment-text">{{ childComment.content }}</text>
											<!-- 子评论图片独立显示 -->
											<view class="comment-image-container" v-if="childComment.img">
												<image :src="childComment.img" class="comment-image" mode="widthFix" @click="previewCommentImage(childComment.img)"></image>
											</view>
										</view>
										
										<!-- 子评论底部操作 -->
										<view class="comment-footer">
											<view class="comment-actions">
												<view class="action-item" @click="toggleCommentLike(childComment)">
													<image :src="childComment.isLiked ? '/static/icon/liked.png' : '/static/icon/like.png'" 
														   class="comment-icon-img"
														   :class="{'like-animation': childComment.likeAnimating}"></image>
													<text class="action-count">{{ childComment.likes || 0 }}</text>
												</view>
												<view class="action-item" @click="replyComment(childComment)">
													<image src="/static/icon/reply.png" class="comment-icon-img"></image>
													<text class="action-text">回复</text>
												</view>
												<view class="action-item" @click="reportComment(childComment)">
													<image src="/static/icon/report.png" class="comment-icon-img"></image>
													<text class="action-text">举报</text>
												</view>
											</view>
										</view>
									</view>
									
									<!-- 分页控制器 -->
									<view class="pagination-control" v-if="getChildComments(comment.id).length > childCommentsPerPage">
										<view class="page-info">
											<text>{{ commentPageInfo[comment.id] ? commentPageInfo[comment.id].current : 1 }}/{{ Math.ceil(getChildComments(comment.id).length / childCommentsPerPage) }}</text>
										</view>
										<view class="page-buttons">
											<view 
												class="page-button" 
												:class="{ 'disabled': !commentPageInfo[comment.id] || commentPageInfo[comment.id].current === 1 }"
												@click="changeChildCommentPage(comment.id, -1)"
											>
												<text class="page-button-text">上一页</text>
											</view>
											<view 
												class="page-button" 
												:class="{ 'disabled': !commentPageInfo[comment.id] || commentPageInfo[comment.id].current === Math.ceil(getChildComments(comment.id).length / childCommentsPerPage) }"
												@click="changeChildCommentPage(comment.id, 1)"
											>
												<text class="page-button-text">下一页</text>
											</view>
										</view>
									</view>
								</view>
							</view>
						</template>
					</view>
				</view>
			</view>
			
			<!-- 加载失败 -->
			<view v-else class="load-error">
				<image class="error-icon" src="/static/icon/error.png" mode="aspectFit"></image>
				<text class="error-text">加载失败，请重试</text>
				<button class="retry-btn" @click="getForumDetail">重新加载</button>
			</view>
		</scroll-view>
		
		<!-- 评论输入框 -->
		<view class="comment-input-wrapper">
			<view class="comment-input-container">
				<view class="comment-input-area">
					<view class="input-row">
						<input 
							type="text"
							v-model="commentText"
							class="comment-input"
							:placeholder="replyTo ? `回复 ${replyTo.userName || '用户' + replyTo.userId}...` : '写下你的评论...'"
							confirm-type="send"
							:focus="inputFocus"
							@blur="inputFocus = false"
							@confirm="submitComment"
						/>
						<view class="upload-button" @click="chooseCommentImage" v-if="!commentImage">
							<image src="/static/icon/image.png" class="upload-icon"></image>
						</view>
					</view>
					
					<!-- 图片预览 -->
					<view class="image-preview-wrapper" v-if="commentImage">
						<view class="image-preview">
							<image :src="commentImage" class="preview-image" mode="aspectFill"></image>
							<view class="delete-image" @click="removeCommentImage">×</view>
						</view>
					</view>
				</view>
				
				<view class="comment-actions-row">
					<view v-if="replyTo" class="cancel-reply" @click="cancelReply">取消回复</view>
					<button class="send-btn" :disabled="!commentText.trim() && !commentImage" @click="submitComment">发送</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, reactive } from 'vue'
import request from '@/utils/request.js'
import { useTheme } from '@/hooks/useTheme'

const { themeClass } = useTheme()

// 获取路由参数
const forumId = ref('')

// 帖子详情
const forumDetail = ref(null)
// 评论列表
const commentList = ref([])
// 加载状态
const isLoading = ref(true)
// 评论文本
const commentText = ref('')
// 输入框聚焦
const inputFocus = ref(false)
// 是否已点赞
const isLiked = ref(false)
// 点赞动画状态
const likeAnimating = ref(false)
// 默认头像
const defaultAvatar = '/static/user.png'
// 当前回复的评论
const replyTo = ref(null)
// 评论图片
const commentImage = ref('')
// 评论图片上传中
const isImageUploading = ref(false)

// 子评论分页设置
const childCommentsPerPage = 5 // 每页显示的子评论数
const commentPageInfo = reactive({}) // 保存每个父评论的分页信息

// 返回上一页
const goBack = () => {
	uni.navigateBack()
}

// 获取帖子详情
const getForumDetail = async () => {
	isLoading.value = true
	
	try {
		// 获取路由参数中的帖子ID
		const pages = getCurrentPages()
		const currentPage = pages[pages.length - 1]
		
		// 兼容App和小程序的参数获取方式
		let id = ''
		
		// 小程序环境
		if (currentPage.options && currentPage.options.id) {
			id = currentPage.options.id
			console.log('从options获取id:', id)
		} 
		// App环境
		else if (currentPage.$page && currentPage.$page.options && currentPage.$page.options.id) {
			id = currentPage.$page.options.id
			console.log('从$page.options获取id:', id)
		}
		// 尝试从$route中获取
		else if (currentPage.$route && currentPage.$route.query && currentPage.$route.query.id) {
			id = currentPage.$route.query.id
			console.log('从$route.query获取id:', id)
		}
		
		forumId.value = id
		console.log('最终设置的forumId:', forumId.value)
		
		if (!forumId.value) {
			uni.showToast({
				title: '帖子ID不存在',
				icon: 'none'
			})
			goBack()
			return
		}
		
		// 请求帖子详情
		const res = await request('/forum/getById', {
			method: 'GET',
			data: { id: forumId.value }
		})
		
		if (res.code === 200) {
			forumDetail.value = res.data
			console.log('帖子详情:', forumDetail.value)
			
			// 获取评论列表
			getCommentList()
			
	
		} else {
			uni.showToast({
				title: res.msg || '获取帖子详情失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取帖子详情失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
	}
}

// 获取评论列表
const getCommentList = async () => {
	try {
		const res = await request('/forum/commentById', {
			method: 'GET',
			data: { id: forumId.value }
		})
		
		if (res.code === 200) {
			commentList.value = res.data || []
			console.log('评论列表:', commentList.value)
		} else {
			console.error('获取评论列表失败:', res.msg)
		}
	} catch (error) {
		console.error('获取评论列表失败:', error)
	}
}

// 获取父评论列表
const parentComments = computed(() => {
	return commentList.value.filter(comment => comment.isParent === 1 || !comment.parentId)
})

// 获取特定父评论的子评论
const getChildComments = (parentId) => {
	return commentList.value.filter(comment => comment.isParent === 0 && comment.parentId === parentId)
}

// 获取分页后的子评论
const getPaginatedChildComments = (parentId) => {
	const childComments = getChildComments(parentId)
	
	// 如果父评论ID不在分页信息中，初始化它
	if (!commentPageInfo[parentId]) {
		commentPageInfo[parentId] = {
			current: 1,
			total: Math.ceil(childComments.length / childCommentsPerPage)
		}
	}
	
	const currentPage = commentPageInfo[parentId].current
	const start = (currentPage - 1) * childCommentsPerPage
	const end = start + childCommentsPerPage
	
	return childComments.slice(start, end)
}

// 切换子评论页码
const changeChildCommentPage = (parentId, direction) => {
	// 确保父评论ID在分页信息中
	if (!commentPageInfo[parentId]) {
		commentPageInfo[parentId] = {
			current: 1,
			total: Math.ceil(getChildComments(parentId).length / childCommentsPerPage)
		}
	}
	
	const pageInfo = commentPageInfo[parentId]
	const newPage = pageInfo.current + direction
	
	// 确保页码在有效范围内
	if (newPage >= 1 && newPage <= pageInfo.total) {
		pageInfo.current = newPage
	}
}

// 选择评论图片
const chooseCommentImage = () => {
	uni.chooseImage({
		count: 1, // 限制只能选择一张图片
		sizeType: ['compressed'], // 压缩图片
		sourceType: ['album', 'camera'], // 可以从相册或相机选择
		success: (res) => {
			const tempFilePath = res.tempFilePaths[0]
			uploadCommentImage(tempFilePath)
		},
		fail: (err) => {
			console.error('选择图片失败:', err)
		}
	})
}

// 上传评论图片
const uploadCommentImage = (filePath) => {
	isImageUploading.value = true
	
	uni.showLoading({
		title: '上传中...'
	})
	
	uni.uploadFile({
		url: '/system/uploadImg',
		filePath,
		name: 'file',
		header: {
			"Content-Type": "multipart/form-data",
			"token": uni.getStorageSync('token')
		},
		success: (uploadRes) => {
			try {
				// 解析返回的JSON数据
				const data = JSON.parse(uploadRes.data)
				if (data.code === 200) {
					commentImage.value = data.data
					console.log('图片上传成功:', commentImage.value)
				} else {
					uni.showToast({
						title: data.msg || '上传失败',
						icon: 'none'
					})
				}
			} catch (e) {
				console.error('解析上传结果失败:', e)
				uni.showToast({
					title: '上传失败，请重试',
					icon: 'none'
				})
			}
		},
		fail: (err) => {
			console.error('上传图片失败:', err)
			uni.showToast({
				title: '上传失败，请重试',
				icon: 'none'
			})
		},
		complete: () => {
			isImageUploading.value = false
			uni.hideLoading()
		}
	})
}

// 移除评论图片
const removeCommentImage = () => {
	commentImage.value = ''
}

// 提交评论
const submitComment = async () => {
	if (!commentText.value.trim() && !commentImage.value) {
		return
	}
	
	// 如果图片正在上传，提示等待上传完成
	if (isImageUploading.value) {
		uni.showToast({
			title: '图片上传中，请稍候...',
			icon: 'none'
		})
		return
	}
	
	try {
		const data = {
			forumId: forumId.value,
			content: commentText.value,
			img: commentImage.value || undefined // 如果有图片则添加图片URL
		}
		
		// 如果是回复，添加父评论ID
		if (replyTo.value) {
			data.parentId = replyTo.value.id
			data.isParent = 0 // 子评论
		} else {
			data.isParent = 1 // 父评论
		}
		
		const res = await request('/forum/comment', {
			method: 'POST',
			data
		})
		
		if (res.code === 200) {
			uni.showToast({
				title: '评论成功',
				icon: 'success'
			})
			
			// 清空评论文本和图片
			commentText.value = ''
			commentImage.value = ''
			// 取消回复状态
			cancelReply()
			
			// 重新获取评论列表
			getCommentList()
		} else {
			uni.showToast({
				title: res.msg || '评论失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('提交评论失败:', error)
		uni.showToast({
			title: error.data.msg,
			icon: 'none',
			duration: 5000
		})
	}
}

// 回复评论
const replyComment = (comment) => {
	replyTo.value = comment
	inputFocus.value = true
	console.log(comment)
	// 如果是子评论，自动添加@用户名
	if (comment.isParent === 0) {
		const userName = comment.userName || '用户' + comment.userId
		commentText.value = `@${userName} `
	} else {
		commentText.value = ''
	}
}

// 取消回复
const cancelReply = () => {
	replyTo.value = null
	commentText.value = ''
	// 移除评论图片
	commentImage.value = ''
}

// 点赞评论
const likeComment = async (comment) => {
	try {
		const res = await request('/forum/likeComment', {
			method: 'POST',
			data: { id: comment.id }
		})
		
		if (res.code === 200) {
			// 更新评论点赞数
			comment.likes = (comment.likes || 0) + 1
		} else {
			uni.showToast({
				title: res.msg || '操作失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('点赞评论失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	}
}

// 切换评论点赞状态
const toggleCommentLike = async (comment) => {
	try {
		// 设置动画状态
		if (!comment.likeAnimating) {
			comment.likeAnimating = true
			setTimeout(() => {
				comment.likeAnimating = false
			}, 300)
		}
		
		const res = await request('/forum/commentLike', {
			method: 'POST',
			data: comment.id 
		})
		
		if (res.code === 200) {
			// 更新点赞状态
			comment.isLiked = !comment.isLiked
			
			// 更新点赞数量
			if (comment.isLiked) {
				comment.likes = (comment.likes || 0) + 1
			} else {
				comment.likes = Math.max(0, (comment.likes || 0) - 1)
			}
		} else {
			uni.showToast({
				title: res.msg || '操作失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('评论点赞操作失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	}
}

// 预览评论图片
const previewCommentImage = (img) => {
	uni.previewImage({
		urls: [img]
	})
}

// 聚焦评论输入框
const focusCommentInput = () => {
	inputFocus.value = true
}

// 切换点赞状态
const toggleLike = async () => {
	try {
		// 设置动画状态
		likeAnimating.value = true
		setTimeout(() => {
			likeAnimating.value = false
		}, 300)
		
		const res = await request('/forum/like', {
			method: 'POST',
			data: forumId.value 
		})
		
		if (res.code === 200) {
			// 更新点赞状态
			forumDetail.value.isLiked = !forumDetail.value.isLiked
			
			// 更新点赞数量
			if (forumDetail.value.isLiked) {
				forumDetail.value.likes = (forumDetail.value.likes || 0) + 1
			} else {
				forumDetail.value.likes = Math.max(0, (forumDetail.value.likes || 0) - 1)
			}
		} else {
			uni.showToast({
				title: res.msg || '操作失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('点赞失败:', error)
		uni.showToast({
			title: '网络错误，请稍后重试',
			icon: 'none'
		})
	}
}

// 预览图片
const previewImage = (index) => {
	if (!forumDetail.value.imageUrl || forumDetail.value.imageUrl.length === 0) return
	
	uni.previewImage({
		current: index,
		urls: forumDetail.value.imageUrl
	})
}

// 增加浏览量
const incrementView = async () => {
	try {
		await request('/forum/incrementView', {
			method: 'POST',
			data: { id: forumId.value }
		})
	} catch (error) {
		console.error('增加浏览量失败:', error)
	}
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

// 跳转到用户主页
const goToUserProfile = (userId) => {
	if (!userId) return
	
	uni.navigateTo({
		url: `/pages/user/personal-center?userId=${userId}`
	})
}

// 使用defineExpose导出方法供页面实例调用
defineExpose({
	onLoad(options) {
		console.log('页面onLoad被调用，参数:', options)
		if (options && options.id) {
			forumId.value = options.id
			console.log('onLoad设置forumId:', forumId.value)
			// 直接调用获取详情函数
			getForumDetail()
		}
	}
})

// 初始化装饰元素位置
onMounted(() => {
	// 如果onLoad未设置ID，则尝试获取详情
	if (!forumId.value) {
		getForumDetail()
	}
	
	// 随机设置浮动元素的初始位置
	try {
		const circles = document.querySelectorAll('.floating-circle')
		const squares = document.querySelectorAll('.floating-square')
		
		if (circles) {
			circles.forEach((circle, index) => {
				circle.style.left = `${Math.random() * 100}%`
				circle.style.top = `${Math.random() * 100}%`
				circle.style.animationDelay = `${-Math.random() * 20}s`
			})
		}
		
		if (squares) {
			squares.forEach((square, index) => {
				square.style.left = `${Math.random() * 100}%`
				square.style.top = `${Math.random() * 100}%`
				square.style.animationDelay = `${-Math.random() * 25}s`
			})
		}
	} catch (error) {
		console.error('设置装饰元素失败:', error)
	}
})

// 举报评论
const reportComment = (comment) => {
	if (!comment || !comment.id) return;
	
	// 跳转到举报页面，传递评论ID和评论作者ID
	uni.navigateTo({
		url: `/pages/index/report?id=${comment.id}&userId=${comment.userId}&type=comment`
	});
}
</script>

<style>
.detail-container {
	background-color: var(--bgColor1);
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

/* 导航栏样式 */
.nav-bar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 30rpx;
	background-color: #fff;
	position: sticky;
	top: 0;
	z-index: 100;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.darkMode .nav-bar {
	background-color: var(--bgColor2);
	box-shadow: none;
}

.back-btn {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.back-icon {
	font-size: 40rpx;
	color: var(--textColor1);
}

.nav-title {
	font-size: 34rpx;
	font-weight: bold;
	color: var(--textColor1);
}

.placeholder {
	width: 60rpx;
}

/* 内容区域 */
.detail-content {
	flex: 1;
	padding-bottom: 200rpx; /* 调整为更大的值，确保有足够空间 */
	position: relative;
	z-index: 1;
}

/* 加载中样式 */
.loading-wrapper {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
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

.loading-text {
	font-size: 24rpx;
	color: var(--textColor3);
}

/* 帖子卡片样式 */
.forum-card {
	position: relative;
	z-index: 2;
	background-color: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(10px);
	margin: 20rpx 30rpx;
	border-radius: 12rpx;
	padding: 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.darkMode .forum-card {
	background-color: var(--bgboxShadowColor2); /* 使用带透明度的背景变量或直接指定 */
	background-color: rgba(30, 30, 30, 0.9);
	box-shadow: none;
}

/* 帖子头部 */
.forum-header {
	margin-bottom: 30rpx;
}

.author-info {
	display: flex;
	align-items: center;
}

.avatar-wrapper {
	position: relative;
	margin-right: 20rpx;
	cursor: pointer; /* 添加手型光标 */
}

.author-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 40rpx;
	z-index: 1;
}

.avatar-frame {
	position: absolute;
	top: -20rpx;
	left: -20rpx;
	width: 120rpx;
	height: 120rpx;
	z-index: 2;
	pointer-events: none;
}

.author-detail {
	flex: 1;
}

.author-name-wrapper {
	display: flex;
	align-items: center;
}

.author-name {
	font-size: 30rpx;
	font-weight: bold;
	color: var(--textColor1);
	margin-right: 10rpx;
}

.publish-time {
	font-size: 24rpx;
	color: var(--textColor3);
	margin-top: 4rpx;
}

/* 帖子内容 */
.forum-body {
	margin-bottom: 30rpx;
}

.forum-title {
	font-size: 34rpx;
	font-weight: bold;
	color: var(--textColor1);
	margin-bottom: 20rpx;
	display: block;
}

.forum-content {
	font-size: 30rpx;
	color: var(--textColor2);
	line-height: 1.5;
	margin-bottom: 20rpx;
	display: block;
}

/* 图片画廊 */
.image-gallery {
	margin-top: 20rpx;
}

.gallery-grid {
	display: flex;
	flex-wrap: wrap;
	margin: -5rpx;
}

.gallery-item {
	position: relative;
	margin: 5rpx;
	overflow: hidden;
	border-radius: 8rpx;
	height: 240rpx;
}

.single-image {
	width: calc(100% - 10rpx);
	height: 400rpx;
}

.full-width {
	width: calc(100% - 10rpx);
	height: 300rpx;
}

.half-width {
	width: calc(50% - 10rpx);
	height: 240rpx;
}

.gallery-image {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.more-images-overlay {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
}

.more-images-text {
	color: #fff;
	font-size: 40rpx;
	font-weight: bold;
}

/* 帖子底部 */
.forum-footer {
	border-top: 1rpx solid var(--borderColor);
	padding-top: 20rpx;
}

.action-bar {
	display: flex;
	justify-content: space-around;
}

.action-btn {
	display: flex;
	align-items: center;
	padding: 10rpx 20rpx;
}

.action-icon-img {
	width: 36rpx;
	height: 36rpx;
	margin-right: 10rpx;
    transition: transform 0.3s ease;
}

.action-text {
	font-size: 26rpx;
	color: var(--textColor3);
}

/* 评论区样式 */
.comment-section {
	position: relative;
	z-index: 2;
	background-color: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(10px);
	margin: 20rpx 30rpx;
	border-radius: 12rpx;
	padding: 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.darkMode .comment-section {
	background-color: rgba(30, 30, 30, 0.9);
	box-shadow: none;
}

.section-title {
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid var(--borderColor);
}

.title-text {
	font-size: 30rpx;
	font-weight: bold;
	color: var(--textColor1);
}

/* 空评论 */
.empty-comment {
	display: flex;
	justify-content: center;
	padding: 40rpx 0;
}

.empty-text {
	font-size: 28rpx;
	color: var(--textColor3);
}

/* 评论列表 */
.comment-item {
	padding: 20rpx 0;
	border-bottom: 1rpx solid var(--borderColor);
}

.comment-item:last-child {
	border-bottom: none;
}

.comment-header {
	display: flex;
	align-items: center;
	margin-bottom: 10rpx;
}

.commenter-avatar-wrapper {
	position: relative;
	margin-right: 16rpx;
	cursor: pointer; /* 添加手型光标 */
}

.commenter-avatar {
	width: 70rpx;
	height: 70rpx;
	border-radius: 35rpx;
	z-index: 1;
}

.commenter-avatar-small {
	width: 60rpx;
	height: 60rpx;
	border-radius: 30rpx;
	z-index: 1;
}

.avatar-frame-small {
	position: absolute;
	top: -15rpx;
	left: -15rpx;
	width: 100rpx;
	height: 100rpx;
	z-index: 2;
	pointer-events: none;
}

.avatar-frame-smaller {
	position: absolute;
	top: -12rpx;
	left: -12rpx;
	width: 84rpx;
	height: 84rpx;
	z-index: 2;
	pointer-events: none;
}

.comment-info {
	flex: 1;
}

.commenter-name-wrapper {
	display: flex;
	align-items: center;
}

.commenter-name {
	font-size: 28rpx;
	font-weight: bold;
	color: var(--textColor1);
	margin-right: 10rpx;
}

.comment-time {
	font-size: 22rpx;
	color: var(--textColor3);
	margin-top: 2rpx;
}

.comment-body {
	padding-left: 86rpx;
}

.comment-text {
	font-size: 28rpx;
	color: var(--textColor2);
	line-height: 1.5;
}

.comment-image {
	max-width: 400rpx;
	margin-top: 10rpx;
	border-radius: 8rpx;
}

/* 评论底部操作区 */
.comment-footer {
	padding-left: 86rpx;
	margin-top: 10rpx;
}

.comment-actions {
	display: flex;
}

.action-item {
	display: flex;
	align-items: center;
	margin-right: 30rpx;
	font-size: 24rpx;
	color: var(--textColor3);
}

.comment-icon-img {
	width: 28rpx;
	height: 28rpx;
	margin-right: 6rpx;
    transition: transform 0.3s ease;
}

.action-count, .action-text {
	font-size: 24rpx;
}

/* 子评论样式 */
.child-comments {
	margin-left: 86rpx;
	margin-top: 10rpx;
	background-color: var(--bgColor1);
	border-radius: 8rpx;
	padding: 10rpx;
}

.darkMode .child-comments {
	background-color: var(--bgColor3);
}

.child-comment-item {
	padding: 10rpx 0;
	border-bottom: 1rpx solid var(--borderColor);
}

.child-comment-item:last-child {
	border-bottom: none;
}

.child-comment-item .comment-body {
	padding-left: 76rpx;
}

.child-comment-item .comment-footer {
	padding-left: 76rpx;
}

/* 评论输入框 */
.comment-input-wrapper {
	position: fixed;
	bottom: 0;
	left: 0;
	width: 100%;
	background-color: #fff;
	padding: 15rpx;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
	z-index: 99;
	box-sizing: border-box;
}

.darkMode .comment-input-wrapper {
	background-color: var(--bgColor2);
	box-shadow: none;
}

.comment-input-container {
	display: flex;
	flex-direction: column;
	width: 100%;
}

.comment-input-area {
	width: 100%;
}

.input-row {
	display: flex;
	align-items: center;
	width: 100%;
}

.comment-input {
	flex: 1;
	height: 70rpx;
	background-color: var(--bgColor1);
	border-radius: 35rpx;
	padding: 0 30rpx;
	font-size: 28rpx;
	color: var(--textColor1);
	margin-right: 10rpx;
	min-width: 0; /* 确保输入框可以缩小 */
}

.darkMode .comment-input {
	background-color: var(--bgColor3);
}

.upload-button {
	min-width: 70rpx;
	height: 70rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: var(--bgColor1);
	border-radius: 35rpx;
	flex-shrink: 0; /* 防止按钮被压缩 */
}

.darkMode .upload-button {
	background-color: var(--bgColor3);
}

.upload-icon {
	width: 40rpx;
	height: 40rpx;
}

.image-preview-wrapper {
	margin-top: 10rpx;
	display: flex;
}

.image-preview {
	position: relative;
	width: 100rpx;
	height: 100rpx;
	margin-right: 10rpx;
}

.preview-image {
	width: 100%;
	height: 100%;
	border-radius: 8rpx;
}

.delete-image {
	position: absolute;
	top: -15rpx;
	right: -15rpx;
	width: 40rpx;
	height: 40rpx;
	background-color: rgba(0, 0, 0, 0.5);
	color: #fff;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	z-index: 2;
}

.comment-actions-row {
	display: flex;
	justify-content: flex-end;
	align-items: center;
	margin-top: 10rpx;
}

.send-btn {
	background-color: #1890ff;
	color: #fff;
	border-radius: 35rpx;
	font-size: 28rpx;
	height: 70rpx;
	line-height: 70rpx;
	padding: 0 30rpx;
	margin: 0;
	flex-shrink: 0; /* 防止按钮被压缩 */
}

.send-btn[disabled] {
	background-color: #ccc;
	color: #fff;
}

.cancel-reply {
	font-size: 28rpx;
	color: var(--textColor3);
	padding: 0 20rpx;
	margin-right: 10rpx;
	height: 70rpx;
	line-height: 70rpx;
	flex-shrink: 0; /* 防止按钮被压缩 */
}

/* 加载失败 */
.load-error {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
}

.error-icon {
	width: 120rpx;
	height: 120rpx;
	margin-bottom: 30rpx;
}

.error-text {
	font-size: 28rpx;
	color: var(--textColor3);
	margin-bottom: 30rpx;
}

.retry-btn {
	background-color: #1890ff;
	color: #fff;
	font-size: 28rpx;
	border-radius: 40rpx;
	padding: 10rpx 40rpx;
}

.like-animation {
    animation: likeScale 0.3s ease;
}

@keyframes likeScale {
    0% { transform: scale(1); }
    50% { transform: scale(1.5); }
    100% { transform: scale(1); }
}

/* 子评论分页控制器 */
.pagination-control {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15rpx 10rpx;
	margin-top: 10rpx;
	border-top: 1rpx dashed var(--borderColor);
}

.page-info {
	font-size: 24rpx;
	color: var(--textColor3);
}

.page-buttons {
	display: flex;
	align-items: center;
}

.page-button {
	padding: 6rpx 20rpx;
	margin: 0 8rpx;
	font-size: 24rpx;
	background-color: rgba(24, 144, 255, 0.1);
	color: #1890ff;
	border-radius: 30rpx;
}

.page-button.disabled {
	background-color: var(--bgColor1);
	color: #ccc;
	pointer-events: none;
}

.page-button-text {
	font-size: 24rpx;
}

/* 添加装饰性背景样式 */
.decorative-background {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	pointer-events: none;
	z-index: 0;
	overflow: hidden;
}

.floating-circle {
	position: absolute;
	width: 200rpx;
	height: 200rpx;
	border-radius: 50%;
	background: linear-gradient(45deg, rgba(26, 188, 156, 0.1), rgba(186, 235, 52, 0.1));
	animation: float 20s infinite linear;
}

.floating-square {
	position: absolute;
	width: 150rpx;
	height: 150rpx;
	background: linear-gradient(45deg, rgba(186, 235, 52, 0.1), rgba(26, 188, 156, 0.1));
	animation: rotate 25s infinite linear;
}

@keyframes float {
	0% { transform: translate(0, 0) rotate(0deg); }
	25% { transform: translate(30rpx, 30rpx) rotate(90deg); }
	50% { transform: translate(0, 60rpx) rotate(180deg); }
	75% { transform: translate(-30rpx, 30rpx) rotate(270deg); }
	100% { transform: translate(0, 0) rotate(360deg); }
}

@keyframes rotate {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

/* 修改评论图片容器样式 */
.comment-image-container {
	margin-top: 16rpx;
	width: 100%;
}

.comment-image {
	max-width: 400rpx;
	border-radius: 12rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

/* 确保内容在背景之上 */
.forum-card {
	position: relative;
	z-index: 2;
	background-color: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(10px);
}

.comment-section {
	position: relative;
	z-index: 2;
	background-color: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(10px);
}
</style>
