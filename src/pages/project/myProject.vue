<template>
	<view class="my-project-container">
		<!-- 顶部标题栏 -->
		<view class="header">
			<text class="header-title">我的项目</text>
			<text class="project-count">共 {{ total }} 个项目</text>
		</view>
		
		<!-- 标签页切换 -->
		<view class="tabs">
			<view 
				:class="['tab-item', { active: activeTab === 'created' }]"
				@click="switchTab('created')"
			>
				<text class="tab-text">我创建的</text>
				<view v-if="activeTab === 'created'" class="tab-indicator"></view>
			</view>
			<view 
				:class="['tab-item', { active: activeTab === 'joined' }]"
				@click="switchTab('joined')"
			>
				<text class="tab-text">我加入的</text>
				<view v-if="activeTab === 'joined'" class="tab-indicator"></view>
			</view>
		</view>
		
		<!-- 项目列表 -->
		<scroll-view 
			scroll-y 
			class="project-scroll"
			@scrolltolower="loadMore"
			:lower-threshold="100"
		>
			<view class="project-list" v-if="projectList.length > 0">
				<view 
					v-for="project in projectList" 
					:key="project.projectId"
					class="project-card"
				>
					<!-- 项目头部 -->
					<view class="project-header">
						<view class="project-title-row">
							<text class="project-name">{{ project.projectName }}</text>
							<view :class="['status-badge', getStatusClass(project.status)]">
								{{ getStatusText(project.status) }}
							</view>
						</view>
						<text class="project-time">创建时间：{{ formatTime(project.createdTime) }}</text>
					</view>
					
					<!-- 项目图片 - 九宫格 -->
					<view class="project-images-grid" v-if="project.imageUrlList && project.imageUrlList.length > 0">
						<view 
							v-for="(img, index) in project.imageUrlList.slice(0, 9)" 
							:key="index"
							class="image-item"
							:class="getImageGridClass(project.imageUrlList.length)"
							@click="previewImage(project.imageUrlList, index)"
						>
							<image 
								:src="img" 
								mode="aspectFill"
								class="grid-image"
							></image>
						</view>
					</view>
					
					<!-- 项目信息 -->
					<view class="project-info">
						<view class="info-row">
							<text class="info-label">城市：</text>
							<text class="info-value">{{ project.city || '-' }}</text>
						</view>
						<view class="info-row">
							<text class="info-label">年龄要求：</text>
							<text class="info-value">{{ project.ageRequirement || '-' }}</text>
						</view>
						<view class="info-row">
							<text class="info-label">项目分类：</text>
							<text class="info-value">{{ project.projectFieldName || '-' }}</text>
						</view>
						<!-- 未通过原因 -->
						<view v-if="project.status === 'reject' && project.failReason" class="info-row fail-reason-row">
							<text class="info-label">未通过原因：</text>
							<text class="fail-reason-text">{{ project.failReason }}</text>
						</view>
					</view>
					
					<!-- 成员区域 -->
					<view class="members-section">
						<view class="members-header">
							<text class="members-title">项目成员 ({{ project.memberList ? project.memberList.length : 0 }})</text>
							<text class="expand-btn" @click="toggleMembers(project.projectId)">
								{{ expandedProjects[project.projectId] ? '收起' : '展开' }}
							</text>
						</view>
						
						<!-- 成员列表 -->
						<view v-if="expandedProjects[project.projectId]" class="members-list">
							<view 
								v-for="member in project.memberList" 
								:key="member.id"
								class="member-item"
								:class="{ 'clickable': member.id }"
								@click="goToUserSpace(member.id)"
							>
								<view class="member-info">
									<view class="avatar-wrapper">
										<image :src="member.avatar || '/static/default-avatar.png'" class="member-avatar"></image>
										<!-- 头像框 -->
										<image v-if="member.avatarFrameUrl" :src="member.avatarFrameUrl" class="avatar-frame" mode="aspectFit"></image>
									</view>
									<view class="member-details">
										<view class="member-name-row">
											<text class="member-name">{{ member.username }}</text>
											<view v-if="member.level" class="member-level" :class="'level-' + member.level">
												<text class="level-text">Lv.{{ member.level }}</text>
											</view>
										</view>
										<text class="member-time">申请时间：{{ formatTime(member.createdTime) }}</text>
										<text v-if="member.status === 'reject' && member.failReason" class="fail-reason">拒绝原因：{{ member.failReason }}</text>
									</view>
								</view>
								
								<!-- 成员状态和操作 - 仅在"我创建的"标签页显示编辑按钮 -->
								<view class="member-actions">
									<view :class="['member-status', getMemberStatusClass(member.status)]">
										{{ getMemberStatusText(member.status) }}
									</view>
									<!-- 编辑状态按钮 - 仅在"我创建的"显示 -->
									<button 
										v-if="activeTab === 'created'"
										class="action-btn edit-status-btn" 
										@click.stop="showStatusMenu(project.projectId, member.id, member.status)"
									>
										编辑
									</button>
								</view>
							</view>
							
							<!-- 空状态 -->
							<view v-if="!project.memberList || project.memberList.length === 0" class="empty-members">
								<text class="empty-text">暂无成员</text>
							</view>
						</view>
					</view>
					
					<!-- 操作按钮 -->
					<view class="project-actions">
						<button class="action-btn view-detail-btn" @click="goToProjectDetail(project.projectId)">
							查看详情
						</button>
						<!-- 我创建的项目：显示修改状态和解散按钮（待审核和未通过状态不显示修改状态） -->
						<button 
							v-if="activeTab === 'created' && project.status !== 'pending' && project.status !== 'reject'" 
							class="action-btn edit-project-status-btn" 
							@click.stop="openProjectStatusModal(project)"
						>
							修改状态
						</button>
						<!-- 我创建的项目：显示解散按钮 -->
						<button 
							v-if="activeTab === 'created'" 
							class="action-btn disband-btn" 
							@click.stop="handleDisband(project.projectId, project.projectName)"
						>
							解散项目
						</button>
						<!-- 我加入的项目：显示离开按钮 -->
						<button 
							v-if="activeTab === 'joined'" 
							class="action-btn leave-btn" 
							@click.stop="handleLeave(project.projectId, project.projectName)"
						>
							离开项目
						</button>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view v-else-if="!loading" class="empty-state">
				<image src="/static/empty-project.png" class="empty-image" mode="aspectFit"></image>
				<text class="empty-title">暂无项目</text>
				<text class="empty-desc">快去创建你的第一个项目吧~</text>
				<button class="create-btn" @click="goToCreateProject">创建项目</button>
			</view>
			
			<!-- 加载更多 -->
			<view v-if="loading" class="loading-more">
				<text class="loading-text">加载中...</text>
			</view>
			<view v-else-if="projectList.length > 0 && !hasMore" class="no-more">
				<text class="no-more-text">没有更多了</text>
			</view>
		</scroll-view>
		
		<!-- 状态选择菜单 -->
		<view v-if="showStatusMenuDialog" class="modal-mask" @click="closeStatusMenu">
			<view class="status-menu-content" @click.stop>
				<view class="status-menu-header">
					<text class="status-menu-title">选择成员状态</text>
				</view>
				<view class="status-menu-list">
					<view 
						v-for="status in statusOptions" 
						:key="status.value"
						class="status-menu-item"
						:class="{ 'active': currentMemberStatus === status.value }"
						@click="selectStatus(status.value)"
					>
						<view :class="['status-dot', status.class]"></view>
						<text class="status-menu-text">{{ status.label }}</text>
						<text v-if="currentMemberStatus === status.value" class="check-icon">✓</text>
					</view>
				</view>
				<view class="status-menu-footer">
					<button class="modal-btn cancel-btn" @click="closeStatusMenu">取消</button>
				</view>
			</view>
		</view>
		
		<!-- 拒绝原因弹窗 -->
		<view v-if="showRejectDialog" class="modal-mask" @click="closeRejectModal">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">拒绝原因</text>
				</view>
				<view class="modal-body">
					<textarea 
						v-model="rejectReason" 
						class="reject-textarea"
						placeholder="请输入拒绝原因"
						maxlength="200"
					></textarea>
					<text class="char-count">{{ rejectReason.length }}/200</text>
				</view>
				<view class="modal-footer">
					<button class="modal-btn cancel-btn" @click="closeRejectModal">取消</button>
					<button class="modal-btn confirm-btn" @click="confirmReject">确定</button>
				</view>
			</view>
		</view>
		
		<!-- 项目状态修改弹窗 -->
		<view v-if="showProjectStatusDialog" class="modal-mask" @click="closeProjectStatusModal">
			<view class="status-menu-content" @click.stop>
				<view class="status-menu-header">
					<text class="status-menu-title">修改项目状态</text>
				</view>
				<view class="status-menu-list">
					<view 
						v-for="status in projectStatusOptions" 
						:key="status.value"
						class="status-menu-item"
						:class="{ 'active': currentProjectStatus === status.value }"
						@click="selectProjectStatus(status.value)"
					>
						<view :class="['status-dot', status.class]"></view>
						<view class="status-info">
							<text class="status-menu-text">{{ status.label }}</text>
							<text class="status-desc">{{ status.desc }}</text>
						</view>
						<text v-if="currentProjectStatus === status.value" class="check-icon">✓</text>
					</view>
				</view>
				<view class="status-menu-footer">
					<button class="modal-btn cancel-btn" @click="closeProjectStatusModal">取消</button>
					<button class="modal-btn confirm-btn" @click="confirmProjectStatus">确认</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request.js'

// 标签页状态
const activeTab = ref('created') // 'created' 或 'joined'

// 项目列表
const projectList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 展开的项目成员列表
const expandedProjects = ref({})

// 成员状态选择菜单相关
const showStatusMenuDialog = ref(false)
const currentEditProjectId = ref(null)
const currentEditMemberId = ref(null)
const currentMemberStatus = ref('')

// 成员状态选项（不包含pending）
const statusOptions = [
	{ value: 'online', label: '已通过', class: 'status-approved' },
	{ value: 'reject', label: '已拒绝', class: 'status-rejected' }
]

// 项目状态修改相关
const showProjectStatusDialog = ref(false)
const currentEditProject = ref(null)
const currentProjectStatus = ref('')

// 项目状态选项（不包含待审核和未通过）
const projectStatusOptions = [
	{ value: 'online', label: '通过且已上线', desc: '项目审核通过并已上线', class: 'status-approved' },
	{ value: 'not-online', label: '通过但未上线', desc: '项目审核通过但未上线', class: 'status-online' }
]

// 拒绝弹窗相关
const showRejectDialog = ref(false)
const rejectReason = ref('')
const currentRejectProjectId = ref(null)
const currentRejectMemberId = ref(null)

// 是否还有更多数据
const hasMore = computed(() => {
	return projectList.value.length < total.value
})

// 获取项目列表
const getProjectList = async () => {
	if (loading.value) return
	
	loading.value = true
	
	try {
		// 根据标签页选择不同的接口
		const apiUrl = activeTab.value === 'created' ? '/project/MyProjectList' : '/project/myJoined'
		
		const res = await request(apiUrl, {
			method: 'GET',
			data: {
				pageNum: currentPage.value,
			}
		})
		
		if (res.code === 200) {
			const pageData = res.data
			
			if (currentPage.value === 1) {
				projectList.value = pageData.records || []
			} else {
				projectList.value = [...projectList.value, ...(pageData.records || [])]
			}
			
			total.value = pageData.total || 0
		} else {
			uni.showToast({
				title: res.msg || '获取项目列表失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('获取项目列表失败:', error)
		uni.showToast({
			title: '网络异常，请稍后重试',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

// 切换标签页
const switchTab = (tab) => {
	if (activeTab.value === tab) return
	
	activeTab.value = tab
	currentPage.value = 1
	projectList.value = []
	expandedProjects.value = {}
	getProjectList()
}

// 加载更多
const loadMore = () => {
	if (!hasMore.value || loading.value) return
	currentPage.value++
	getProjectList()
}

// 切换成员列表展开/收起
const toggleMembers = (projectId) => {
	expandedProjects.value[projectId] = !expandedProjects.value[projectId]
}

// 显示状态选择菜单
const showStatusMenu = (projectId, memberId, currentStatus) => {
	currentEditProjectId.value = projectId
	currentEditMemberId.value = memberId
	currentMemberStatus.value = currentStatus
	showStatusMenuDialog.value = true
}

// 关闭状态选择菜单
const closeStatusMenu = () => {
	showStatusMenuDialog.value = false
	currentEditProjectId.value = null
	currentEditMemberId.value = null
	currentMemberStatus.value = ''
}

// 选择状态
const selectStatus = (status) => {
	// 如果选择的状态与当前状态相同，直接关闭
	if (status === currentMemberStatus.value) {
		closeStatusMenu()
		return
	}
	
	// 如果选择拒绝，需要输入拒绝原因
	if (status === 'reject') {
		closeStatusMenu()
		showRejectModal(currentEditProjectId.value, currentEditMemberId.value)
		return
	}
	
	// 其他状态直接更新
	handleMemberStatus(currentEditProjectId.value, currentEditMemberId.value, status)
	closeStatusMenu()
}

// 显示拒绝弹窗
const showRejectModal = (projectId, memberId) => {
	currentRejectProjectId.value = projectId
	currentRejectMemberId.value = memberId
	rejectReason.value = ''
	showRejectDialog.value = true
}

// 关闭拒绝弹窗
const closeRejectModal = () => {
	showRejectDialog.value = false
	rejectReason.value = ''
	currentRejectProjectId.value = null
	currentRejectMemberId.value = null
}

// 确认拒绝
const confirmReject = () => {
	if (!rejectReason.value.trim()) {
		uni.showToast({
			title: '请输入拒绝原因',
			icon: 'none'
		})
		return
	}
	
	handleMemberStatus(currentRejectProjectId.value, currentRejectMemberId.value, 'reject', rejectReason.value)
	closeRejectModal()
}

// 处理成员状态（通过/拒绝）
const handleMemberStatus = async (projectId, memberId, status, failReason = '') => {
	uni.showLoading({ title: '处理中...' })
	
	try {
		// 构建URL查询参数
		let url = `/project/member/status?projectId=${projectId}&status=${status}&userId=${memberId}`
		
		// 如果是拒绝操作，添加拒绝原因
		if (status === 'reject' && failReason) {
			url += `&failReason=${encodeURIComponent(failReason)}`
		}
		
		const res = await request(url, {
			method: 'POST'
		})
		
		uni.hideLoading()
		
		if (res.code === 200) {
			uni.showToast({
				title: status === 'online' ? '已通过' : '已拒绝',
				icon: 'success'
			})
			
			// 刷新列表
			currentPage.value = 1
			getProjectList()
		} else {
			uni.showToast({
				title: res.msg || '操作失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('处理成员状态失败:', error)
		uni.hideLoading()
		uni.showToast({
			title: '网络异常，请稍后重试',
			icon: 'none'
		})
	}
}

// 获取项目状态文本
const getStatusText = (status) => {
	const statusMap = {
		'PENDING': '待审核',
		'APPROVED': '已通过',
		'REJECTED': '已拒绝',
		'ONLINE': '已上线',
		'OFFLINE': '已下线'
	}
	return statusMap[status] || status
}

// 获取项目状态样式类
const getStatusClass = (status) => {
	const classMap = {
		'PENDING': 'status-pending',
		'APPROVED': 'status-approved',
		'REJECTED': 'status-rejected',
		'ONLINE': 'status-online',
		'OFFLINE': 'status-offline'
	}
	return classMap[status] || ''
}

// 获取成员状态文本
const getMemberStatusText = (status) => {
	const statusMap = {
		'pending': '待审核',
		'online': '已通过',
		'reject': '已拒绝'
	}
	return statusMap[status] || status
}

// 获取成员状态样式类
const getMemberStatusClass = (status) => {
	const classMap = {
		'pending': 'status-pending',
		'online': 'status-approved',
		'reject': 'status-rejected'
	}
	return classMap[status] || ''
}

// 获取九宫格样式类
const getImageGridClass = (count) => {
	if (count === 1) return 'single-image'
	if (count === 2) return 'two-images'
	if (count === 4) return 'four-images'
	return 'multi-images' // 3张或5-9张
}

// 格式化时间
const formatTime = (time) => {
	if (!time) return '-'
	const date = new Date(time)
	return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 预览图片
const previewImage = (images, index) => {
	uni.previewImage({
		urls: images,
		current: index
	})
}

// 打开项目状态修改弹窗
const openProjectStatusModal = (project) => {
	currentEditProject.value = project
	// 如果当前状态是pending，默认选择online，否则使用当前状态
	currentProjectStatus.value = (project.status === 'pending') ? 'online' : project.status
	showProjectStatusDialog.value = true
}

// 关闭项目状态修改弹窗
const closeProjectStatusModal = () => {
	showProjectStatusDialog.value = false
	currentEditProject.value = null
	currentProjectStatus.value = ''
}

// 选择项目状态
const selectProjectStatus = (status) => {
	currentProjectStatus.value = status
}

// 确认修改项目状态
const confirmProjectStatus = async () => {
	if (!currentEditProject.value || !currentProjectStatus.value) {
		uni.showToast({ title: '请选择状态', icon: 'none' })
		return
	}
	
	// 如果状态未变化，直接关闭
	if (currentProjectStatus.value === currentEditProject.value.status) {
		closeProjectStatusModal()
		return
	}
	
	uni.showLoading({ title: '更新中...' })
	
	try {
		const res = await request(
			`/project/updateStatus?projectId=${currentEditProject.value.projectId}&status=${currentProjectStatus.value}`,
			{
				method: 'GET'
			}
		)
		
		uni.hideLoading()
		
		if (res.code === 200) {
			uni.showToast({ title: '状态更新成功', icon: 'success' })
			closeProjectStatusModal()
			// 刷新项目列表
			getProjectList()
		} else {
			uni.showToast({ title: res.msg || '状态更新失败', icon: 'none' })
		}
	} catch (error) {
		console.error('更新状态失败:', error)
		uni.hideLoading()
		uni.showToast({ title: '状态更新失败', icon: 'none' })
	}
}

// 解散项目
const handleDisband = (projectId, projectName) => {
	uni.showModal({
		title: '确认解散',
		content: `确定要解散项目"${projectName}"吗？解散后将无法恢复，所有成员将被移除。`,
		confirmText: '确定解散',
		confirmColor: '#f44336',
		cancelText: '取消',
		success: async (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '解散中...' })
				
				try {
					const result = await request('/project/disband', {
						method: 'GET',
						data: {
							projectId: projectId
						}
					})
					
					uni.hideLoading()
					
					if (result.code === 200) {
						uni.showToast({
							title: '解散成功',
							icon: 'success'
						})
						
						// 刷新项目列表
						setTimeout(() => {
							currentPage.value = 1
							projectList.value = []
							getProjectList()
						}, 1500)
					} else {
						uni.showToast({
							title: result.msg || '解散失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.hideLoading()
					console.error('解散项目失败:', error)
					uni.showToast({
						title: '网络异常，请稍后重试',
						icon: 'none'
					})
				}
			}
		}
	})
}

// 离开项目
const handleLeave = (projectId, projectName) => {
	uni.showModal({
		title: '确认离开',
		content: `确定要离开项目"${projectName}"吗？`,
		confirmText: '确定',
		cancelText: '取消',
		success: async (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '离开中...' })
				
				try {
					const result = await request('/project/leave', {
						method: 'GET',
						data: {
							projectId: projectId
						}
					})
					
					uni.hideLoading()
					
					if (result.code === 200) {
						uni.showToast({
							title: '离开成功',
							icon: 'success'
						})
						
						// 刷新项目列表
						setTimeout(() => {
							currentPage.value = 1
							projectList.value = []
							getProjectList()
						}, 1500)
					} else {
						uni.showToast({
							title: result.msg || '离开失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.hideLoading()
					console.error('离开项目失败:', error)
					uni.showToast({
						title: '网络异常，请稍后重试',
						icon: 'none'
					})
				}
			}
		}
	})
}

// 跳转到项目详情
const goToProjectDetail = (projectId) => {
	uni.navigateTo({
		url: `/pages/project/detail?id=${projectId}`
	})
}

// 跳转到创建项目
const goToCreateProject = () => {
	uni.navigateTo({
		url: '/pages/project/createProject'
	})
}

// 跳转到用户空间
const goToUserSpace = (userId) => {
	if (!userId) {
		console.log('用户ID为空，无法跳转')
		return
	}
	
	uni.navigateTo({
		url: `/pages/user/personal-center?userId=${userId}`
	})
}

onMounted(() => {
	getProjectList()
})
</script>

<style scoped>
.my-project-container {
	min-height: 100vh;
	background-color: #f5f5f5;
	display: flex;
	flex-direction: column;
}

/* 顶部标题栏 */
.header {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	padding: 40rpx 30rpx 30rpx;
	color: #fff;
}

.header-title {
	font-size: 40rpx;
	font-weight: bold;
	display: block;
	margin-bottom: 10rpx;
}

.project-count {
	font-size: 26rpx;
	opacity: 0.9;
}

/* 标签页 */
.tabs {
	display: flex;
	background-color: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.tab-item {
	flex: 1;
	position: relative;
	padding: 30rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	cursor: pointer;
	transition: all 0.3s ease;
}

.tab-text {
	font-size: 30rpx;
	color: #666;
	transition: all 0.3s ease;
}

.tab-item.active .tab-text {
	color: #00A872;
	font-weight: bold;
}

.tab-indicator {
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 6rpx;
	background: linear-gradient(90deg, #00A872 0%, #00C896 100%);
	border-radius: 3rpx;
	animation: slideIn 0.3s ease;
}

@keyframes slideIn {
	from {
		width: 0;
		opacity: 0;
	}
	to {
		width: 60rpx;
		opacity: 1;
	}
}

/* 滚动区域 */
.project-scroll {
	flex: 1;
	padding: 20rpx;
}

/* 项目列表 */
.project-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

/* 项目卡片 */
.project-card {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 24rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

/* 项目头部 */
.project-header {
	margin-bottom: 20rpx;
}

.project-title-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
}

.project-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	flex: 1;
	margin-right: 16rpx;
}

.status-badge {
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	white-space: nowrap;
}

.status-pending {
	background-color: #fff3e0;
	color: #f57c00;
}

.status-approved {
	background-color: #e0f7f0;
	color: #00A872;
}

.status-rejected {
	background-color: #ffebee;
	color: #c62828;
}

.status-online {
	background-color: #e3f2fd;
	color: #1565c0;
}

.status-offline {
	background-color: #f5f5f5;
	color: #999;
}

.project-time {
	font-size: 24rpx;
	color: #999;
}

/* 项目图片 - 九宫格 */
.project-images-grid {
	display: grid;
	gap: 8rpx;
	margin-bottom: 20rpx;
}

.image-item {
	position: relative;
	overflow: hidden;
	border-radius: 8rpx;
	background-color: #f5f5f5;
}

.grid-image {
	width: 100%;
	height: 100%;
	display: block;
}

/* 1张图片 - 大图 */
.single-image {
	grid-column: span 3;
	height: 400rpx;
}

/* 2张图片 - 横向排列 */
.two-images {
	grid-column: span 1.5;
	height: 220rpx;
}

/* 4张图片 - 2x2网格 */
.four-images {
	grid-column: span 1.5;
	height: 220rpx;
}

/* 3张或5-9张图片 - 3列网格 */
.multi-images {
	grid-column: span 1;
	height: 220rpx;
}

/* 设置网格列数 */
.project-images-grid {
	grid-template-columns: repeat(3, 1fr);
}

/* 项目信息 */
.project-info {
	margin-bottom: 20rpx;
	padding: 16rpx;
	background-color: #f9f9f9;
	border-radius: 12rpx;
}

.info-row {
	display: flex;
	align-items: center;
	margin-bottom: 8rpx;
	font-size: 26rpx;
}

.info-row:last-child {
	margin-bottom: 0;
}

.info-label {
	color: #999;
	min-width: 140rpx;
}

.info-value {
	color: #333;
	flex: 1;
}

.fail-reason-row {
	align-items: flex-start;
	background-color: #fff3e0;
	padding: 16rpx;
	border-radius: 8rpx;
	margin-top: 8rpx;
}

.fail-reason-text {
	color: #f57c00;
	flex: 1;
	line-height: 1.6;
}

/* 成员管理区域 */
.members-section {
	margin-bottom: 20rpx;
	padding: 16rpx;
	background-color: #f9f9f9;
	border-radius: 12rpx;
}

.members-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.members-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
}

.expand-btn {
	font-size: 24rpx;
	color: #00A872;
	padding: 4rpx 12rpx;
}

/* 成员列表 */
.members-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.member-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16rpx;
	background-color: #fff;
	border-radius: 12rpx;
	transition: all 0.3s ease;
}

.member-item.clickable {
	cursor: pointer;
}

.member-item.clickable:hover {
	background-color: #f5f5f5;
	transform: translateX(4rpx);
}

.member-item.clickable:active {
	background-color: #eeeeee;
	transform: scale(0.98);
}

.member-info {
	display: flex;
	align-items: center;
	flex: 1;
}

.avatar-wrapper {
	position: relative;
	margin-right: 16rpx;
}

.member-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 30rpx;
}

.avatar-frame {
	position: absolute;
	top: -10rpx;
	left: -10rpx;
	width: 80rpx;
	height: 80rpx;
	pointer-events: none;
}

.member-details {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
	flex: 1;
}

.member-name-row {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.member-name {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.member-level {
	padding: 2rpx 8rpx;
	border-radius: 8rpx;
	font-size: 20rpx;
}

.level-text {
	font-weight: bold;
}

.member-time {
	font-size: 22rpx;
	color: #999;
}

.fail-reason {
	font-size: 22rpx;
	color: #c62828;
	background-color: #ffebee;
	padding: 4rpx 8rpx;
	border-radius: 6rpx;
	margin-top: 4rpx;
}

/* 成员操作 */
.member-actions {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.member-status {
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
	font-size: 22rpx;
}

.action-buttons {
	display: flex;
	gap: 8rpx;
}

.action-btn {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	border: none;
}

.edit-status-btn {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
	padding: 6rpx 16rpx;
	font-size: 22rpx;
}

.approve-btn {
	background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
	color: #fff;
}

.reject-btn {
	background-color: #f5f5f5;
	color: #666;
}

.empty-members {
	text-align: center;
	padding: 40rpx 0;
}

.empty-text {
	font-size: 26rpx;
	color: #999;
}

/* 项目操作按钮 */
.project-actions {
	display: flex;
	gap: 16rpx;
}

.view-detail-btn {
	flex: 1;
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
	height: 72rpx;
	line-height: 72rpx;
	border-radius: 12rpx;
	font-size: 28rpx;
	border: none;
}

.disband-btn {
	flex: 1;
	background-color: #ffebee;
	color: #f44336;
	height: 72rpx;
	line-height: 72rpx;
	border-radius: 12rpx;
	font-size: 28rpx;
	border: 2rpx solid #f44336;
	font-weight: bold;
}

.disband-btn::after {
	border: none;
}

.leave-btn {
	flex: 1;
	background-color: #fff3e0;
	color: #ff9800;
	height: 72rpx;
	line-height: 72rpx;
	border-radius: 12rpx;
	font-size: 28rpx;
	border: 2rpx solid #ff9800;
}

.leave-btn::after {
	border: none;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 40rpx;
}

.empty-image {
	width: 300rpx;
	height: 300rpx;
	margin-bottom: 40rpx;
}

.empty-title {
	font-size: 32rpx;
	color: #333;
	font-weight: bold;
	margin-bottom: 16rpx;
}

.empty-desc {
	font-size: 26rpx;
	color: #999;
	margin-bottom: 40rpx;
}

.create-btn {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
	padding: 20rpx 60rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
	border: none;
}

/* 加载状态 */
.loading-more,
.no-more {
	text-align: center;
	padding: 30rpx 0;
}

.loading-text,
.no-more-text {
	font-size: 26rpx;
	color: #999;
}

/* 状态选择菜单样式 */
.status-menu-content {
	width: 600rpx;
	background-color: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
	from {
		transform: translateY(100rpx);
		opacity: 0;
	}
	to {
		transform: translateY(0);
		opacity: 1;
	}
}

.status-menu-header {
	padding: 30rpx;
	border-bottom: 1px solid #f0f0f0;
}

.status-menu-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.status-menu-list {
	padding: 20rpx 0;
}

.status-menu-item {
	display: flex;
	align-items: center;
	padding: 24rpx 30rpx;
	position: relative;
	transition: background-color 0.2s;
}

.status-menu-item:active {
	background-color: #f5f5f5;
}

.status-menu-item.active {
	background-color: #f0f4ff;
}

.status-dot {
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.status-dot.status-pending {
	background-color: #f57c00;
}

.status-dot.status-approved {
	background-color: #00A872;
}

.status-dot.status-rejected {
	background-color: #c62828;
}

.status-menu-text {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.check-icon {
	font-size: 32rpx;
	color: #00A872;
	font-weight: bold;
}

.status-menu-footer {
	border-top: 1px solid #f0f0f0;
}

/* 拒绝弹窗样式 */
.modal-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 9999;
}

.modal-content {
	width: 600rpx;
	background-color: #fff;
	border-radius: 16rpx;
	overflow: hidden;
}

.modal-header {
	padding: 30rpx;
	border-bottom: 1px solid #f0f0f0;
}

.modal-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.modal-body {
	padding: 30rpx;
}

.reject-textarea {
	width: 100%;
	min-height: 200rpx;
	padding: 16rpx;
	background-color: #f9f9f9;
	border-radius: 12rpx;
	font-size: 28rpx;
	color: #333;
	box-sizing: border-box;
}

.char-count {
	display: block;
	text-align: right;
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
}

.modal-footer {
	display: flex;
	border-top: 1px solid #f0f0f0;
}

.modal-btn {
	flex: 1;
	height: 88rpx;
	line-height: 88rpx;
	text-align: center;
	font-size: 28rpx;
	border: none;
	background-color: transparent;
}

.cancel-btn {
	color: #666;
	border-right: 1px solid #f0f0f0;
}

.confirm-btn {
	color: #00A872;
	font-weight: bold;
}
</style>
