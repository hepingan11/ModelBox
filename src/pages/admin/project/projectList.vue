<template>
	<view class="project-list-container">
		<!-- 顶部搜索栏 -->
		<view class="search-section">
			<view class="search-row">
				<input 
					v-model="searchForm.projectName" 
					class="search-input" 
					placeholder="请输入项目名称"
					@confirm="handleSearch"
				/>
				<button class="search-btn" @click="handleSearch">搜索</button>
			</view>
			
			<!-- 筛选条件 -->
			<view class="filter-section">
				<view class="filter-row">
					<view class="picker-view" @click="openCityPicker">
						{{ selectedCities.length > 0 ? `城市(${selectedCities.length})` : '城市' }}
					</view>
					
					<view class="picker-view" @click="openCategoryPicker">
						{{ selectedCategories.length > 0 ? `分类(${selectedCategories.length})` : '项目分类' }}
					</view>
				</view>
				
				<view class="filter-row">
					<picker 
						mode="selector" 
						:range="ageRequirements" 
						@change="onAgeRequirementChange"
						:value="selectedAgeIndex"
					>
						<view class="picker-view">
							{{ searchForm.ageRequirement || '年龄要求' }}
						</view>
					</picker>
					
					<picker 
						mode="selector" 
						:range="statusOptions" 
						range-key="label"
						@change="onStatusChange"
						:value="selectedStatusIndex"
					>
						<view class="picker-view">
							{{ getStatusLabel(searchForm.status) }}
						</view>
					</picker>
					
					<button class="reset-btn" @click="handleReset">重置</button>
				</view>
			</view>
		</view>
		
		<!-- 项目列表 -->
		<view class="project-list">
			<!-- 加载状态 -->
			<view v-if="loading" class="loading-container">
				<view class="loading-spinner"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 项目卡片 -->
			<view 
				v-else-if="projectList.length > 0" 
				class="project-item" 
				v-for="project in projectList" 
				:key="project.projectId"
			>
				<!-- 项目图片 -->
				<view class="project-images" v-if="project.imageUrlList && project.imageUrlList.length > 0">
					<image 
						v-for="(img, index) in project.imageUrlList.slice(0, 3)" 
						:key="index"
						:src="img" 
						class="project-image"
						mode="aspectFill"
					></image>
				</view>
				
				<!-- 项目信息 -->
				<view class="project-info">
					<view class="project-header">
						<text class="project-name">{{ project.projectName }}</text>
						<view class="status-badge" :class="getStatusClass(project.status)">
							{{ getStatusText(project.status) }}
						</view>
					</view>
					
					<text class="project-intro" v-if="project.introduce">{{ project.introduce }}</text>
					
					<view class="project-meta">
						<view class="meta-item">
							<text class="meta-label">城市：</text>
							<text class="meta-value">{{ project.city || '未设置' }}</text>
						</view>
						<view class="meta-item">
							<text class="meta-label">年龄要求：</text>
							<text class="meta-value">{{ project.ageRequirement || '不限' }}</text>
						</view>
						<view class="meta-item">
							<text class="meta-label">创建时间：</text>
							<text class="meta-value">{{ formatTime(project.createdTime) }}</text>
						</view>
					</view>
					
					<!-- 失败原因 -->
					<view v-if="project.status === 'REJECTED' && project.failReason" class="fail-reason">
						<text class="fail-label">失败原因：</text>
						<text class="fail-text">{{ project.failReason }}</text>
					</view>
					
					<!-- 成员列表 -->
					<view v-if="project.memberList && project.memberList.length > 0" class="member-section">
						<text class="member-label">成员：</text>
						<view class="member-avatars">
							<image 
								v-for="member in project.memberList.slice(0, 5)" 
								:key="member.id"
								:src="member.avatar || '/static/default-avatar.png'" 
								class="member-avatar"
							></image>
							<text v-if="project.memberList.length > 5" class="member-more">
								+{{ project.memberList.length - 5 }}
							</text>
						</view>
					</view>
					
					<!-- 操作按钮 -->
					<view class="action-buttons">
						<button 
							class="action-btn view-btn" 
							@click.stop="goToProjectDetail(project.projectId)"
						>
							查看详情
						</button>
						<button 
							class="action-btn status-btn" 
							@click.stop="openStatusModal(project)"
						>
							修改状态
						</button>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view v-else class="empty-state">
				<image src="/static/empty.png" class="empty-icon"></image>
				<text class="empty-text">暂无项目数据</text>
			</view>
		</view>
		
		<!-- 分页 -->
		<view v-if="total > 0" class="pagination">
			<button 
				class="page-btn" 
				:disabled="currentPage === 1"
				@click="prevPage"
			>
				上一页
			</button>
			<text class="page-info">{{ currentPage }} / {{ totalPages }}</text>
			<button 
				class="page-btn" 
				:disabled="currentPage === totalPages"
				@click="nextPage"
			>
				下一页
			</button>
		</view>
		
		<!-- 状态修改弹窗 -->
		<view v-if="showStatusModal" class="modal-overlay" @click="closeStatusModal">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">修改项目状态</text>
					<text class="modal-close" @click="closeStatusModal">✕</text>
				</view>
				
				<view class="modal-body">
					<view class="project-name-display">
						<text class="label">项目名称：</text>
						<text class="value">{{ currentProject?.projectName }}</text>
					</view>
					
					<view class="status-options">
						<view 
							v-for="option in updateStatusOptions" 
							:key="option.value"
							class="status-option"
							:class="{ active: selectedStatus === option.value }"
							@click="selectedStatus = option.value"
						>
							<view class="option-radio">
								<view v-if="selectedStatus === option.value" class="radio-dot"></view>
							</view>
							<view class="option-content">
								<text class="option-label">{{ option.label }}</text>
								<text class="option-desc">{{ option.desc }}</text>
							</view>
						</view>
					</view>
				</view>
				
				<view class="modal-footer">
					<button class="modal-btn cancel-btn" @click="closeStatusModal">取消</button>
					<button class="modal-btn confirm-btn" @click="confirmUpdateStatus">确认</button>
				</view>
			</view>
		</view>
		
		<!-- 城市多选弹窗 -->
		<view class="popup-mask" v-if="showCityPicker" @click="showCityPicker = false">
			<view class="city-popup-content" @click.stop>
				<!-- 头部 -->
				<view class="popup-header">
					<text class="popup-title">选择城市</text>
					<view class="popup-close" @click="showCityPicker = false">
						<text class="close-icon">×</text>
					</view>
				</view>
				
				<!-- 搜索框 -->
				<view class="city-search-wrapper">
					<view class="search-icon-wrapper">
						<text class="search-icon">🔍</text>
					</view>
					<input 
						v-model="citySearchKeyword" 
						class="city-search-input" 
						placeholder="搜索省份或城市"
					/>
					<view v-if="citySearchKeyword" class="clear-icon" @click="citySearchKeyword = ''">
						<text>×</text>
					</view>
				</view>
				
				<!-- 已选城市 -->
				<view class="selected-cities" v-if="tempSelectedCities.length > 0">
					<view class="selected-header">
						<text class="selected-title">已选择 {{ tempSelectedCities.length }} 个地区</text>
						<text class="clear-all" @click="tempSelectedCities = []">清空</text>
					</view>
					<view class="selected-tags">
						<view 
							v-for="(city, index) in tempSelectedCities" 
							:key="index"
							class="selected-tag"
							@click="removeCity(city)"
						>
							<text class="tag-name">{{ city }}</text>
							<text class="tag-close">×</text>
						</view>
					</view>
				</view>
				
				<!-- 省份和城市列表 -->
				<scroll-view scroll-y class="city-list-scroll">
					<view 
						v-for="(province, pIndex) in filteredCityData" 
						:key="pIndex"
						class="province-group"
					>
						<view class="province-header">
							<view class="province-info" @click="toggleProvince(pIndex)">
								<text class="province-name">{{ province.province }}</text>
								<text class="province-arrow">{{ expandedProvinces[pIndex] ? '▲' : '▼' }}</text>
							</view>
							<view 
								:class="['province-select-btn', { 'province-selected': isCitySelected(province.province) }]"
								@click="toggleCity(province.province)"
							>
								<text v-if="isCitySelected(province.province)" class="check-icon">✓</text>
								<text class="select-btn-text">{{ isCitySelected(province.province) ? '已选' : '全选' }}</text>
							</view>
						</view>
						<view v-if="expandedProvinces[pIndex]" class="city-grid">
							<view 
								v-for="(city, cIndex) in province.cities" 
								:key="cIndex"
								:class="['city-item', { 'city-selected': isCitySelected(city) }]"
								@click="toggleCity(city)"
							>
								<text class="city-name">{{ city }}</text>
							</view>
						</view>
					</view>
				</scroll-view>
				
				<!-- 底部按钮 -->
				<view class="popup-footer">
					<view class="footer-btn cancel-btn" @click="cancelCitySelection">
						<text class="btn-text">取消</text>
					</view>
					<view class="footer-btn confirm-btn" @click="confirmCitySelection">
						<text class="btn-text">确定</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 分类选择弹窗 -->
		<view class="popup-mask" v-if="showCategoryPicker" @click="showCategoryPicker = false">
			<view class="city-popup-content" @click.stop>
				<!-- 头部 -->
				<view class="popup-header">
					<text class="popup-title">选择分类</text>
					<view class="popup-close" @click="showCategoryPicker = false">
						<text class="close-icon">×</text>
					</view>
				</view>
				
				<!-- 已选分类 -->  
				<view class="selected-cities" v-if="tempSelectedCategories.length > 0">
					<view class="selected-header">
						<text class="selected-title">已选择 {{ tempSelectedCategories.length }} 个分类</text>
						<text class="clear-all" @click="tempSelectedCategories = []">清空</text>
					</view>
					<view class="selected-tags">
						<view 
							v-for="(category, index) in tempSelectedCategories" 
							:key="index"
							class="selected-tag"
							@click="removeCategory(category)"
						>
							<text class="tag-name">{{ category.fieldName }}</text>
							<text class="tag-close">×</text>
						</view>
					</view>
				</view>
				
				<!-- 分类列表 -->
				<scroll-view scroll-y class="city-list-scroll">
					<view 
						v-for="(parent, pIndex) in parentFields" 
						:key="pIndex"
						class="province-group"
					>
						<view class="province-header">
							<view class="province-info" @click="toggleCategoryExpand(pIndex)">
								<text class="province-name">{{ parent.fieldName }}</text>
								<text class="province-arrow">{{ expandedCategories[pIndex] ? '▲' : '▼' }}</text>
							</view>
							<view 
								:class="['province-select-btn', { 'province-selected': isCategorySelected(parent) }]"
								@click="toggleCategory(parent)"
							>
								<text v-if="isCategorySelected(parent)" class="check-icon">✓</text>
								<text class="select-btn-text">{{ isCategorySelected(parent) ? '已选' : '全选' }}</text>
							</view>
						</view>
						<view v-if="expandedCategories[pIndex]" class="city-grid">
							<view 
								v-for="(child, cIndex) in getCategoryChildren(parent.projectFieldId)" 
								:key="cIndex"
								:class="['city-item', { 'city-selected': isCategorySelected(child) }]"
								@click="toggleCategory(child)"
							>
								<text class="city-name">{{ child.fieldName }}</text>
							</view>
						</view>
					</view>
				</scroll-view>
				
				<!-- 底部按钮 -->
				<view class="popup-footer">
					<view class="footer-btn cancel-btn" @click="cancelCategorySelection">
						<text class="btn-text">取消</text>
					</view>
					<view class="footer-btn confirm-btn" @click="confirmCategorySelection">
						<text class="btn-text">确定</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

// 搜索表单
const searchForm = ref({
	projectName: '',
	city: '',
	projectFieldId: '',
	ageRequirement: '',
	status: ''
})

// 项目列表
const projectList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const total = ref(0)
const pageSize = ref(10)

// 计算总页数
const totalPages = computed(() => {
	return Math.ceil(total.value / pageSize.value) || 1
})

// 项目分类
const allFields = ref([])
const parentFields = ref([])
const childFields = ref([])
const selectedParentField = ref(null)
const selectedChildField = ref(null)
const selectedParentIndex = ref(-1)
const selectedChildIndex = ref(-1)

// 年龄要求选项
const ageRequirements = ref(['不限', '18岁以下', '18-25岁', '26-35岁', '36-45岁', '46岁以上'])
const selectedAgeIndex = ref(0)

// 状态选项（用于筛选）
const statusOptions = ref([
    { label: '全部状态', value: '' },
    { label: '待审核', value: 'pending' },
    { label: '通过且已上线', value: 'online' },
    { label: '通过但未上线', value: 'not-online' },
    { label: '未通过', value: 'reject' }
])
const selectedStatusIndex = ref(0)

// 状态修改弹窗
const showStatusModal = ref(false)
const currentProject = ref(null)
const selectedStatus = ref('')

// 状态修改选项
const updateStatusOptions = ref([
	{ label: '待审核', value: 'pending', desc: '项目等待审核' },
	{ label: '通过且已上线', value: 'online', desc: '项目审核通过并已上线' },
	{ label: '通过但未上线', value: 'not-online', desc: '项目审核通过但未上线' },
	{ label: '未通过', value: 'reject', desc: '项目审核未通过' }
])

// 城市选择
const showCityPicker = ref(false)
const selectedCities = ref([]) // 已选城市数组
const tempSelectedCities = ref([]) // 临时选中的城市
const citySearchKeyword = ref('') // 城市搜索关键词
const expandedProvinces = ref({}) // 展开的省份

// 分类选择
const showCategoryPicker = ref(false)
const selectedCategories = ref([]) // 已选分类数组
const tempSelectedCategories = ref([]) // 临时选中的分类数组
const expandedCategories = ref({}) // 展开的分类

// 城市数据（简化版，包含主要省市）
const cityData = ref([
	{ province: '北京市', cities: ['东城区', '西城区', '朝阳区', '丰台区', '石景山区', '海淀区'] },
	{ province: '上海市', cities: ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区'] },
	{ province: '广东省', cities: ['广州市', '深圳市', '珠海市', '汕头市', '佛山市', '东莞市'] },
	{ province: '浙江省', cities: ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市'] },
	{ province: '江苏省', cities: ['南京市', '无锡市', '徐州市', '常州市', '苏州市', '南通市'] }
])

// 过滤后的城市数据
const filteredCityData = computed(() => {
	if (!citySearchKeyword.value) {
		return cityData.value
	}
	const keyword = citySearchKeyword.value.toLowerCase()
	return cityData.value.filter(province => {
		return province.province.toLowerCase().includes(keyword) ||
		       province.cities.some(city => city.toLowerCase().includes(keyword))
	}).map(province => {
		if (province.province.toLowerCase().includes(keyword)) {
			return province
		}
		return {
			...province,
			cities: province.cities.filter(city => city.toLowerCase().includes(keyword))
		}
	})
})

// 获取项目分类列表
const getProjectFields = async () => {
	try {
		const res = await request('/project/field/list', {
			method: 'GET'
		})
		
		if (res.code === 200) {
			allFields.value = res.data || []
			// 筛选出父分类（parentId为null或0）
			parentFields.value = allFields.value.filter(field => !field.parentId || field.parentId === 0)
		} else {
			uni.showToast({ title: res.msg || '获取分类失败', icon: 'none' })
		}
	} catch (error) {
		console.error('获取项目分类失败:', error)
		uni.showToast({ title: '获取分类失败', icon: 'none' })
	}
}

// 父分类变化
const onParentFieldChange = (e) => {
	const index = e.detail.value
	selectedParentIndex.value = index
	selectedParentField.value = parentFields.value[index]
	
	// 重置子分类
	selectedChildField.value = null
	selectedChildIndex.value = -1
	childFields.value = []
	
	if (selectedParentField.value) {
		// 获取子分类
		childFields.value = allFields.value.filter(
			field => field.parentId === selectedParentField.value.projectFieldId
		)
		
		// 如果没有子分类，直接使用父分类ID
		if (childFields.value.length === 0) {
			searchForm.value.projectFieldId = selectedParentField.value.projectFieldId
		} else {
			searchForm.value.projectFieldId = ''
		}
	} else {
		searchForm.value.projectFieldId = ''
	}
}

// 子分类变化
const onChildFieldChange = (e) => {
	const index = e.detail.value
	selectedChildIndex.value = index
	selectedChildField.value = childFields.value[index]
	
	if (selectedChildField.value) {
		searchForm.value.projectFieldId = selectedChildField.value.projectFieldId
	} else {
		searchForm.value.projectFieldId = selectedParentField.value ? selectedParentField.value.projectFieldId : ''
	}
}

// 年龄要求变化
const onAgeRequirementChange = (e) => {
	const index = e.detail.value
	selectedAgeIndex.value = index
	searchForm.value.ageRequirement = ageRequirements.value[index] === '不限' ? '' : ageRequirements.value[index]
}

// 状态变化
const onStatusChange = (e) => {
	const index = e.detail.value
	selectedStatusIndex.value = index
	searchForm.value.status = statusOptions.value[index].value
}

// 获取状态标签
const getStatusLabel = (status) => {
	const option = statusOptions.value.find(opt => opt.value === status)
	return option ? option.label : '全部状态'
}

// 获取状态样式类
const getStatusClass = (status) => {
	const classMap = {
		'pending': 'status-pending',
		'online': 'status-online',
		'not-online': 'status-not-online',
		'reject': 'status-rejected'
	}
	return classMap[status] || ''
}

// 获取项目列表
const getProjectList = async () => {
	loading.value = true
	
	try {
		const res = await request('/admin/project/list', {
			method: 'GET',
			data: {
				pageNum: currentPage.value,
				projectName: searchForm.value.projectName,
				city: searchForm.value.city,
				projectFieldId: searchForm.value.projectFieldId || 0,
				ageRequirement: searchForm.value.ageRequirement,
				status: searchForm.value.status
			}
		})
		
		if (res.code === 200) {
			projectList.value = res.data.records || []
			total.value = res.data.total || 0
		} else {
			uni.showToast({ title: res.msg || '获取项目列表失败', icon: 'none' })
		}
	} catch (error) {
		console.error('获取项目列表失败:', error)
		uni.showToast({ title: '获取项目列表失败', icon: 'none' })
	} finally {
		loading.value = false
	}
}

// 搜索
const handleSearch = () => {
	currentPage.value = 1
	getProjectList()
}

// 重置
const handleReset = () => {
	searchForm.value = {
		projectName: '',
		city: '',
		projectFieldId: '',
		ageRequirement: '',
		status: ''
	}
	selectedParentField.value = null
	selectedChildField.value = null
	selectedParentIndex.value = -1
	selectedChildIndex.value = -1
	childFields.value = []
	selectedAgeIndex.value = 0
	selectedStatusIndex.value = 0
	currentPage.value = 1
	getProjectList()
}

// 上一页
const prevPage = () => {
	if (currentPage.value > 1) {
		currentPage.value--
		getProjectList()
	}
}

// 下一页
const nextPage = () => {
	if (currentPage.value < totalPages.value) {
		currentPage.value++
		getProjectList()
	}
}

// 跳转到项目详情
const goToProjectDetail = (projectId) => {
	uni.navigateTo({
		url: `/pages/project/detail?id=${projectId}`
	})
}

// 格式化时间
const formatTime = (time) => {
	if (!time) return '-'
	const date = new Date(time)
	return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 获取状态文本
const getStatusText = (status) => {
	const statusMap = {
		'pending': '待审核',
		'online': '通过且已上线',
		'not-online': '通过但未上线',
		'reject': '未通过'
	}
	return statusMap[status] || status
}

// 打开状态修改弹窗
const openStatusModal = (project) => {
	currentProject.value = project
	// 直接使用当前状态，如果为空则默认为pending
	selectedStatus.value = project.status || 'pending'
	showStatusModal.value = true
}

// 关闭状态修改弹窗
const closeStatusModal = () => {
	showStatusModal.value = false
	currentProject.value = null
	selectedStatus.value = ''
}

// 确认修改状态
const confirmUpdateStatus = async () => {
	if (!currentProject.value || !selectedStatus.value) {
		uni.showToast({ title: '请选择状态', icon: 'none' })
		return
	}
	
	uni.showLoading({ title: '更新中...' })
	
	try {
		// 将参数拼接到URL中，因为后端使用@RequestParam
		const res = await request(
			`/admin/project/updateStatus?projectId=${currentProject.value.projectId}&status=${selectedStatus.value}`,
			{
				method: 'POST'
			}
		)
		
		uni.hideLoading()
		
		if (res.code === 200) {
			uni.showToast({ title: '状态更新成功', icon: 'success' })
			closeStatusModal()
			// 刷新列表
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

// 打开城市选择器
const openCityPicker = () => {
	tempSelectedCities.value = [...selectedCities.value]
	showCityPicker.value = true
}

// 切换省份展开/折叠
const toggleProvince = (index) => {
	expandedProvinces.value[index] = !expandedProvinces.value[index]
}

// 判断城市是否已选
const isCitySelected = (city) => {
	return tempSelectedCities.value.includes(city)
}

// 切换城市选中状态
const toggleCity = (city) => {
	const index = tempSelectedCities.value.indexOf(city)
	if (index > -1) {
		tempSelectedCities.value.splice(index, 1)
	} else {
		tempSelectedCities.value.push(city)
	}
}

// 移除已选城市
const removeCity = (city) => {
	const index = tempSelectedCities.value.indexOf(city)
	if (index > -1) {
		tempSelectedCities.value.splice(index, 1)
	}
}

// 取消城市选择
const cancelCitySelection = () => {
	tempSelectedCities.value = [...selectedCities.value]
	showCityPicker.value = false
	citySearchKeyword.value = ''
}

// 确认城市选择
const confirmCitySelection = () => {
	selectedCities.value = [...tempSelectedCities.value]
	searchForm.value.city = selectedCities.value.join(',')
	showCityPicker.value = false
	citySearchKeyword.value = ''
	handleSearch()
}

// 打开分类选择器
const openCategoryPicker = () => {
	tempSelectedCategories.value = [...selectedCategories.value]
	showCategoryPicker.value = true
}

// 切换分类展开/折叠
const toggleCategoryExpand = (index) => {
	expandedCategories.value[index] = !expandedCategories.value[index]
}

// 获取分类的子分类
const getCategoryChildren = (parentId) => {
	return allFields.value.filter(field => field.parentId === parentId)
}

// 判断分类是否已选
const isCategorySelected = (category) => {
	return tempSelectedCategories.value.some(c => c.projectFieldId === category.projectFieldId)
}

// 切换分类选中状态（支持多选，智能处理父子关系）
const toggleCategory = (category) => {
	const isParent = !category.parentId || category.parentId === 0
	const index = tempSelectedCategories.value.findIndex(c => c.projectFieldId === category.projectFieldId)
	
	if (index > -1) {
		// 取消选择
		tempSelectedCategories.value.splice(index, 1)
	} else {
		// 选择分类
		if (isParent) {
			// 选择父分类：移除该父分类下的所有子分类
			const childIds = getCategoryChildren(category.projectFieldId).map(c => c.projectFieldId)
			tempSelectedCategories.value = tempSelectedCategories.value.filter(
				c => !childIds.includes(c.projectFieldId)
			)
			tempSelectedCategories.value.push(category)
		} else {
			// 选择子分类：移除该子分类的父分类
			tempSelectedCategories.value = tempSelectedCategories.value.filter(
				c => c.projectFieldId !== category.parentId
			)
			tempSelectedCategories.value.push(category)
		}
	}
}

// 移除已选分类
const removeCategory = (category) => {
	const index = tempSelectedCategories.value.findIndex(c => c.projectFieldId === category.projectFieldId)
	if (index > -1) {
		tempSelectedCategories.value.splice(index, 1)
	}
}

// 取消分类选择
const cancelCategorySelection = () => {
	tempSelectedCategories.value = [...selectedCategories.value]
	showCategoryPicker.value = false
}

// 确认分类选择
const confirmCategorySelection = () => {
	selectedCategories.value = [...tempSelectedCategories.value]
	
	if (selectedCategories.value.length > 0) {
		// 将所有选中的分类ID用逗号连接
		searchForm.value.projectFieldId = selectedCategories.value.map(c => c.projectFieldId).join(',')
	} else {
		searchForm.value.projectFieldId = ''
	}
	
	showCategoryPicker.value = false
	handleSearch()
}

// 页面加载
onMounted(() => {
	getProjectFields()
	getProjectList()
})

</script>

<style scoped>
.project-list-container {
	min-height: 100vh;
	background-color: #FEFEFC;
	padding: 20rpx;
}

/* 搜索栏 */
.search-section {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 24rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.search-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 20rpx;
}

.search-input {
	flex: 1;
	height: 72rpx;
	padding: 0 24rpx;
	background-color: #FEFEFC;
	border-radius: 36rpx;
	font-size: 28rpx;
}

.search-btn {
	height: 72rpx;
	padding: 0 40rpx;
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
	border-radius: 36rpx;
	font-size: 28rpx;
	border: none;
}

.filter-section {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.filter-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.filter-input {
	flex: 1;
	height: 64rpx;
	padding: 0 20rpx;
	background-color: #f5f5f5;
	border-radius: 12rpx;
	font-size: 26rpx;
}

.picker-view {
	flex: 1;
	height: 64rpx;
	line-height: 64rpx;
	padding: 0 20rpx;
	background-color: #f5f5f5;
	border-radius: 12rpx;
	font-size: 26rpx;
	color: #666;
}

.reset-btn {
	height: 64rpx;
	padding: 0 32rpx;
	background-color: #f5f5f5;
	color: #666;
	border-radius: 12rpx;
	font-size: 26rpx;
	border: none;
}

/* 项目列表 */
.project-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.project-item {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 24rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
	transition: transform 0.2s;
}

.project-item:active {
	transform: scale(0.98);
}

.project-images {
	display: flex;
	gap: 12rpx;
	margin-bottom: 20rpx;
	overflow-x: auto;
}

.project-image {
	width: 200rpx;
	height: 200rpx;
	border-radius: 12rpx;
	flex-shrink: 0;
}

.project-info {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.project-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.project-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	flex: 1;
}

.status-badge {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	white-space: nowrap;
}

.status-pending {
	background-color: #fff3e0;
	color: #f57c00;
}

.status-online {
	background-color: #e8f5e9;
	color: #2e7d32;
}

.status-not-online {
	background-color: #e3f2fd;
	color: #1565c0;
}

.status-rejected {
	background-color: #ffebee;
	color: #c62828;
}

.project-intro {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	line-clamp: 2;
	-webkit-line-clamp: 2;
	line-clamp: 2;
	-webkit-box-orient: vertical;
	box-orient: vertical;
}

.project-meta {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.meta-item {
	display: flex;
	align-items: center;
	font-size: 26rpx;
}

.meta-label {
	color: #999;
}

.meta-value {
	color: #666;
}

.fail-reason {
	padding: 16rpx;
	background-color: #ffebee;
	border-radius: 8rpx;
	border-left: 4rpx solid #c62828;
}

.fail-label {
	font-size: 24rpx;
	color: #c62828;
	font-weight: bold;
}

.fail-text {
	font-size: 24rpx;
	color: #d32f2f;
}

.member-section {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.member-label {
	font-size: 26rpx;
	color: #999;
}

.member-avatars {
	display: flex;
	align-items: center;
	gap: -12rpx;
}

.member-avatar {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	border: 3rpx solid #fff;
}

.member-more {
	margin-left: 16rpx;
	font-size: 24rpx;
	color: #999;
}

/* 加载状态 */
.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 80rpx 0;
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #00A872;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text {
	margin-top: 20rpx;
	font-size: 26rpx;
	color: #999;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 80rpx 0;
}

.empty-icon {
	width: 200rpx;
	height: 200rpx;
	opacity: 0.5;
}

.empty-text {
	margin-top: 20rpx;
	font-size: 28rpx;
	color: #999;
}

/* 分页 */
.pagination {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 24rpx;
	margin-top: 32rpx;
	padding: 24rpx;
	background-color: #fff;
	border-radius: 16rpx;
}

.page-btn {
	padding: 16rpx 32rpx;
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
	border-radius: 8rpx;
	font-size: 26rpx;
	border: none;
}

.page-btn[disabled] {
	background: #e0e0e0;
	color: #999;
}

.page-info {
	font-size: 28rpx;
	color: #666;
}

/* 操作按钮 */
.action-buttons {
	display: flex;
	gap: 16rpx;
	margin-top: 16rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #f0f0f0;
}

.action-btn {
	flex: 1;
	height: 64rpx;
	border-radius: 12rpx;
	font-size: 26rpx;
	border: none;
	transition: all 0.3s;
}

.view-btn {
	background-color: #f5f5f5;
	color: #666;
}

.view-btn:active {
	background-color: #e0e0e0;
}

.status-btn {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
}

.status-btn:active {
	opacity: 0.8;
}

/* 状态修改弹窗 */
.modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
	animation: fadeIn 0.3s;
}

@keyframes fadeIn {
	from { opacity: 0; }
	to { opacity: 1; }
}

.modal-content {
	width: 600rpx;
	background-color: #fff;
	border-radius: 24rpx;
	overflow: hidden;
	animation: slideUp 0.3s;
}

@keyframes slideUp {
	from { transform: translateY(100rpx); opacity: 0; }
	to { transform: translateY(0); opacity: 1; }
}

.modal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 32rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.modal-close {
	font-size: 40rpx;
	color: #999;
	line-height: 1;
}

.modal-body {
	padding: 32rpx;
	max-height: 600rpx;
	overflow-y: auto;
}

.project-name-display {
	margin-bottom: 24rpx;
	padding: 16rpx;
	background-color: #f5f5f5;
	border-radius: 12rpx;
}

.project-name-display .label {
	font-size: 26rpx;
	color: #999;
}

.project-name-display .value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.status-options {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.status-option {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 20rpx;
	border: 2rpx solid #e0e0e0;
	border-radius: 12rpx;
	transition: all 0.3s;
}

.status-option.active {
	border-color: #00A872;
	background-color: #f5f7ff;
}

.option-radio {
	width: 40rpx;
	height: 40rpx;
	border: 2rpx solid #e0e0e0;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s;
}

.status-option.active .option-radio {
	border-color: #00A872;
}

.radio-dot {
	width: 24rpx;
	height: 24rpx;
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	border-radius: 50%;
}

.option-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.option-label {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.option-desc {
	font-size: 24rpx;
	color: #999;
}

.modal-footer {
	display: flex;
	gap: 16rpx;
	padding: 24rpx 32rpx;
	border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
	flex: 1;
	height: 72rpx;
	border-radius: 12rpx;
	font-size: 28rpx;
	border: none;
}

.cancel-btn {
	background-color: #f5f5f5;
	color: #666;
}

.confirm-btn {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
}

/* 城市和分类选择弹窗 */
.popup-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: flex-end;
	justify-content: center;
	z-index: 1000;
}

.city-popup-content {
	width: 100%;
	max-height: 80vh;
	background-color: #fff;
	border-radius: 24rpx 24rpx 0 0;
	display: flex;
	flex-direction: column;
	animation: slideUp 0.3s;
}

.popup-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 32rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.popup-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.popup-close {
	width: 48rpx;
	height: 48rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.close-icon {
	font-size: 48rpx;
	color: #999;
	line-height: 1;
}

.city-search-wrapper {
	margin: 24rpx 32rpx;
	display: flex;
	align-items: center;
	background-color: #f5f5f5;
	border-radius: 12rpx;
	padding: 0 20rpx;
}

.search-icon-wrapper {
	margin-right: 12rpx;
}

.search-icon {
	font-size: 28rpx;
}

.city-search-input {
	flex: 1;
	height: 64rpx;
	font-size: 26rpx;
	background-color: transparent;
}

.clear-icon {
	font-size: 32rpx;
	color: #999;
	padding: 8rpx;
}

.selected-cities {
	padding: 0 32rpx 24rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.selected-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.selected-title {
	font-size: 26rpx;
	color: #666;
}

.clear-all {
	font-size: 26rpx;
	color: #00A872;
}

.selected-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.selected-tag {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 8rpx 16rpx;
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.tag-name {
	max-width: 200rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.tag-close {
	font-size: 28rpx;
	font-weight: bold;
}

.city-list-scroll {
	flex: 1;
	padding: 24rpx 32rpx;
}

.province-group {
	margin-bottom: 24rpx;
}

.province-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.province-info {
	display: flex;
	align-items: center;
	gap: 8rpx;
	flex: 1;
}

.province-name {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
}

.province-arrow {
	font-size: 20rpx;
	color: #999;
}

.province-select-btn {
	padding: 8rpx 20rpx;
	background-color: #f5f5f5;
	border-radius: 20rpx;
	font-size: 24rpx;
	color: #666;
	display: flex;
	align-items: center;
	gap: 4rpx;
}

.province-select-btn.province-selected {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
}

.check-icon {
	font-size: 20rpx;
}

.select-btn-text {
	font-size: 24rpx;
}

.city-grid {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 12rpx;
}

.city-item {
	padding: 16rpx;
	background-color: #f5f5f5;
	border-radius: 12rpx;
	text-align: center;
	font-size: 26rpx;
	color: #666;
	transition: all 0.2s;
}

.city-item.city-selected {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
}

.city-name {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.popup-footer {
	display: flex;
	gap: 16rpx;
	padding: 24rpx 32rpx;
	border-top: 1rpx solid #f0f0f0;
}

.footer-btn {
	flex: 1;
	height: 72rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 12rpx;
	font-size: 28rpx;
}

.footer-btn.cancel-btn {
	background-color: #f5f5f5;
	color: #666;
}

.footer-btn.confirm-btn {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	color: #fff;
}

.btn-text {
	font-size: 28rpx;
}
</style>