<template>
	<view class="project-list-page" :class="themeClass">
		<!-- 搜索栏 -->
		<view class="search-bar">
			<view class="search-input-wrapper">
				<input 
					v-model="searchForm.projectName" 
					class="search-input" 
					placeholder="搜索项目名称"
					@confirm="handleSearch"
				/>
				<image src="/static/icon/search.png" class="search-icon" @click="handleSearch"></image>
			</view>
		</view>
		
		<!-- 筛选栏 -->
		<view class="filter-bar">
			<scroll-view scroll-x class="filter-scroll" enable-flex>
				<!-- 城市筛选 -->
				<view class="filter-item" @click="openCityPicker">
					<text :class="selectedCities.length > 0 ? 'filter-text-active' : 'filter-text'">
						{{ selectedCities.length > 0 ? `城市(${selectedCities.length})` : '城市' }}
					</text>
					<text class="filter-arrow">▼</text>
				</view>
				
				
				<!-- 年龄要求筛选 -->
				<picker 
					class="filter-picker"
					mode="selector" 
					:range="ageOptions" 
					@change="onAgeChange"
					:value="selectedAgeIndex"
				>
					<view class="filter-item">
						<text :class="searchForm.ageRequirement ? 'filter-text-active' : 'filter-text'">
							{{ searchForm.ageRequirement || '年龄' }}
						</text>
						<text class="filter-arrow">▼</text>
					</view>
				</picker>
				
				<!-- 项目分类筛选 -->
				<view class="filter-item" @click="openCategoryPicker">
					<text :class="selectedCategories.length > 0 ? 'filter-text-active' : 'filter-text'">
						{{ selectedCategories.length > 0 ? `分类(${selectedCategories.length})` : '分类' }}
					</text>
					<text class="filter-arrow">▼</text>
				</view>
				
				<!-- 重置按钮 -->
				<view class="filter-item reset-btn" @click="handleReset" v-if="hasFilters">
					<text class="reset-text">重置</text>
				</view>
			</scroll-view>
		</view>
		
		<!-- 项目列表 -->
		<scroll-view 
			scroll-y 
			class="project-scroll"
			@scrolltolower="loadMore"
			refresher-enabled
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			:lower-threshold="100"
		>
			<!-- 加载状态 -->
			<view v-if="loading && projectList.length === 0" class="loading-container">
				<view class="loading-spinner"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 项目卡片列表 -->
			<view v-else-if="projectList.length > 0" class="project-list">
				<view 
					class="project-card" 
					v-for="project in projectList" 
					:key="project.projectId"
					@click="goToProjectDetail(project.projectId)"
				>
					<!-- 项目图片 -->
					<view class="project-image-container">
						<!-- 九宫格图片 -->
						<view class="image-grid" v-if="project.imageUrlList && project.imageUrlList.length > 0">
							<image 
								v-for="(img, imgIdx) in project.imageUrlList.slice(0, 6)" 
								:key="imgIdx"
								:src="img" 
								:class="['grid-image', 'grid-image-' + Math.min(project.imageUrlList.length, 6)]" 
								mode="aspectFill"
							></image>
							<!-- 更多图片提示 -->
							<view v-if="project.imageUrlList.length > 6" class="more-images-overlay">
								<text class="more-images-text">+{{ project.imageUrlList.length - 6 }}</text>
							</view>
						</view>
						<!-- 默认图片 -->
						<image 
							v-else
							src="/static/default-project.png" 
							class="project-image-single" 
							mode="aspectFill"
						></image>
						<!-- 成员数量标签 -->
						<view v-if="project.memberList && project.memberList.length > 0" class="member-badge">
							<text class="member-badge-text">👥 {{ project.memberList.length }}人</text>
						</view>
					</view>
					
					<!-- 项目信息 -->
					<view class="project-content">
						<text class="project-name">{{ project.projectName }}</text>
						<text class="project-intro" v-if="project.introduce">{{ project.introduce }}</text>
						
						<!-- 项目标签 -->
						<view class="project-tags">
							<view class="tag" v-if="project.projectFieldName">
								<image src="/static/icon/Industry.png" class="tag-icon"></image>
								<text class="tag-text">{{ project.projectFieldName }}</text>
							</view>
							<view class="tag" v-if="project.city">
								<image src="/static/icon/local.png" class="tag-icon"></image>
								<text class="tag-text">{{ project.city }}</text>
							</view>
							<view class="tag" v-if="project.ageRequirement">
								<text class="tag-icon">👤</text>
								<text class="tag-text">{{ project.ageRequirement }}</text>
							</view>
						</view>
						
						<!-- 成员头像列表 -->
						<view v-if="project.memberList && project.memberList.length > 0" class="member-list">
							<image 
								v-for="(member, idx) in project.memberList.slice(0, 4)" 
								:key="idx"
								:src="member.avatar || '/static/default-avatar.png'" 
								class="member-avatar"
							></image>
							<text v-if="project.memberList.length > 4" class="member-more">
								+{{ project.memberList.length - 4 }}
							</text>
						</view>
					</view>
				</view>
				
				<!-- 加载更多 -->
				<view v-if="hasMore" class="load-more">
					<view v-if="loadingMore" class="loading-more-spinner"></view>
					<text class="load-more-text">{{ loadingMore ? '加载中...' : '上拉加载更多' }}</text>
				</view>
				
				<!-- 没有更多 -->
				<view v-else class="no-more">
					<text class="no-more-text">没有更多了</text>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view v-else class="empty-state">
				<image src="/static/empty.png" class="empty-icon"></image>
				<text class="empty-text">暂无项目</text>
			</view>
			
		</scroll-view>
		
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
		
		<!-- 发起项目按钮 -->
		<view class="create-project-btn" @click="goToCreateProject">
			<text class="create-btn-icon">+</text>
			<text class="create-btn-text">发起项目</text>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { useTheme } from '@/hooks/useTheme'

const { themeClass } = useTheme()

// 搜索表单
const searchForm = ref({
	projectName: '',
	city: '', // 多个城市以逗号分隔
	projectFieldId: '',
	ageRequirement: ''
})

// 城市选择
const selectedCities = ref([]) // 已选城市数组
const tempSelectedCities = ref([]) // 临时选中的城市
const citySearchKeyword = ref('') // 城市搜索关键词
const expandedProvinces = ref({}) // 展开的省份

// 分类选择
const showCategoryPicker = ref(false)
const selectedCategories = ref([]) // 已选分类数组
const tempSelectedCategories = ref([]) // 临时选中的分类数组
const expandedCategories = ref({}) // 展开的分类

// 项目列表
const projectList = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)
const total = ref(0)
const pageSize = ref(10)

// 计算是否有更多数据
const hasMore = computed(() => {
	return projectList.value.length < total.value
})

// 计算是否有筛选条件
const hasFilters = computed(() => {
	return searchForm.value.projectName || 
	       selectedCities.value.length > 0 || 
	       searchForm.value.projectFieldId || 
	       searchForm.value.ageRequirement
})

// 打开城市选择器时初始化临时选中列表
const openCityPicker = () => {
	tempSelectedCities.value = [...selectedCities.value]
	showCityPicker.value = true
}

// 项目分类
const allFields = ref([])
const parentFields = ref([])
const childFields = ref([])
const selectedParentField = ref(null)
const selectedChildField = ref(null)
const selectedParentIndex = ref(-1)
const selectedChildIndex = ref(-1)

// 年龄选项
const ageOptions = ref(['不限', '18岁以下', '18-25岁', '26-35岁', '36-45岁', '46岁以上'])
const selectedAgeIndex = ref(0)

// 城市选择弹窗
const showCityPicker = ref(false)

// 城市数据（从 createProject 复制）
const cityData = ref([
	{ province: '北京市', cities: ['东城区', '西城区', '朝阳区', '丰台区', '石景山区', '海淀区', '门头沟区', '房山区', '通州区', '顺义区', '昌平区', '大兴区', '怀柔区', '平谷区', '密云区', '延庆区'] },
	{ province: '天津市', cities: ['和平区', '河东区', '河西区', '南开区', '河北区', '红桥区', '东丽区', '西青区', '津南区', '北辰区', '武清区', '宝坻区', '滨海新区', '宁河区', '静海区', '蓟州区'] },
	{ province: '上海市', cities: ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '闵行区', '宝山区', '嘉定区', '浦东新区', '金山区', '松江区', '青浦区', '奉贤区', '崇明区'] },
	{ province: '重庆市', cities: ['万州区', '涪陵区', '渝中区', '大渡口区', '江北区', '沙坪坝区', '九龙坡区', '南岸区', '北碚区', '綦江区', '大足区', '渝北区', '巴南区', '黔江区', '长寿区', '江津区', '合川区', '永川区', '南川区', '璧山区', '铜梁区', '潼南区', '荣昌区', '开州区', '梁平区', '武隆区'] },
	{ province: '河北省', cities: ['石家庄市', '唐山市', '秦皇岛市', '邯郸市', '邢台市', '保定市', '张家口市', '承德市', '沧州市', '廊坊市', '衡水市'] },
	{ province: '山西省', cities: ['太原市', '大同市', '阳泉市', '长治市', '晋城市', '朔州市', '晋中市', '运城市', '忻州市', '临汾市', '吕梁市'] },
	{ province: '辽宁省', cities: ['沈阳市', '大连市', '鞍山市', '抚顺市', '本溪市', '丹东市', '锦州市', '营口市', '阜新市', '辽阳市', '盘锦市', '铁岭市', '朝阳市', '葫芦岛市'] },
	{ province: '吉林省', cities: ['长春市', '吉林市', '四平市', '辽源市', '通化市', '白山市', '松原市', '白城市', '延边朝鲜族自治州'] },
	{ province: '黑龙江省', cities: ['哈尔滨市', '齐齐哈尔市', '鸡西市', '鹤岗市', '双鸭山市', '大庆市', '伊春市', '佳木斯市', '七台河市', '牡丹江市', '黑河市', '绥化市', '大兴安岭地区'] },
	{ province: '江苏省', cities: ['南京市', '无锡市', '徐州市', '常州市', '苏州市', '南通市', '连云港市', '淮安市', '盐城市', '扬州市', '镇江市', '泰州市', '宿迁市'] },
	{ province: '浙江省', cities: ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市', '金华市', '衢州市', '舟山市', '台州市', '丽水市'] },
	{ province: '安徽省', cities: ['合肥市', '芜湖市', '蚌埠市', '淮南市', '马鞍山市', '淮北市', '铜陵市', '安庆市', '黄山市', '滁州市', '阜阳市', '宿州市', '六安市', '亳州市', '池州市', '宣城市'] },
	{ province: '福建省', cities: ['福州市', '厦门市', '莆田市', '三明市', '泉州市', '漳州市', '南平市', '龙岩市', '宁德市'] },
	{ province: '江西省', cities: ['南昌市', '景德镇市', '萍乡市', '九江市', '新余市', '鹰潭市', '赣州市', '吉安市', '宜春市', '抚州市', '上饶市'] },
	{ province: '山东省', cities: ['济南市', '青岛市', '淄博市', '枣庄市', '东营市', '烟台市', '潍坊市', '济宁市', '泰安市', '威海市', '日照市', '临沂市', '德州市', '聊城市', '滨州市', '菏泽市'] },
	{ province: '河南省', cities: ['郑州市', '开封市', '洛阳市', '平顶山市', '安阳市', '鹤壁市', '新乡市', '焦作市', '濮阳市', '许昌市', '漯河市', '三门峡市', '南阳市', '商丘市', '信阳市', '周口市', '驻马店市', '济源市'] },
	{ province: '湖北省', cities: ['武汉市', '黄石市', '十堰市', '宜昌市', '襄阳市', '鄂州市', '荆门市', '孝感市', '荆州市', '黄冈市', '咸宁市', '随州市', '恩施土家族苗族自治州', '仙桃市', '潜江市', '天门市', '神农架林区'] },
	{ province: '湖南省', cities: ['长沙市', '株洲市', '湘潭市', '衡阳市', '邵阳市', '岳阳市', '常德市', '张家界市', '益阳市', '郴州市', '永州市', '怀化市', '娄底市', '湘西土家族苗族自治州'] },
	{ province: '广东省', cities: ['广州市', '韶关市', '深圳市', '珠海市', '汕头市', '佛山市', '江门市', '湛江市', '茂名市', '肇庆市', '惠州市', '梅州市', '汕尾市', '河源市', '阳江市', '清远市', '东莞市', '中山市', '潮州市', '揭阳市', '云浮市'] },
	{ province: '广西壮族自治区', cities: ['南宁市', '柳州市', '桂林市', '梧州市', '北海市', '防城港市', '钦州市', '贵港市', '玉林市', '百色市', '贺州市', '河池市', '来宾市', '崇左市'] },
	{ province: '海南省', cities: ['海口市', '三亚市', '三沙市', '儋州市', '五指山市', '琼海市', '文昌市', '万宁市', '东方市'] },
	{ province: '四川省', cities: ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市', '广元市', '遂宁市', '内江市', '乐山市', '南充市', '眉山市', '宜宾市', '广安市', '达州市', '雅安市', '巴中市', '资阳市', '阿坝藏族羌族自治州', '甘孜藏族自治州', '凉山彝族自治州'] },
	{ province: '贵州省', cities: ['贵阳市', '六盘水市', '遵义市', '安顺市', '毕节市', '铜仁市', '黔西南布依族苗族自治州', '黔东南苗族侗族自治州', '黔南布依族苗族自治州'] },
	{ province: '云南省', cities: ['昆明市', '曲靖市', '玉溪市', '保山市', '昭通市', '丽江市', '普洱市', '临沧市', '楚雄彝族自治州', '红河哈尼族彝族自治州', '文山壮族苗族自治州', '西双版纳傣族自治州', '大理白族自治州', '德宏傣族景颇族自治州', '怒江傈僳族自治州', '迪庆藏族自治州'] },
	{ province: '西藏自治区', cities: ['拉萨市', '日喀则市', '昌都市', '林芝市', '山南市', '那曲市', '阿里地区'] },
	{ province: '陕西省', cities: ['西安市', '铜川市', '宝鸡市', '咸阳市', '渭南市', '延安市', '汉中市', '榆林市', '安康市', '商洛市'] },
	{ province: '甘肃省', cities: ['兰州市', '嘉峪关市', '金昌市', '白银市', '天水市', '武威市', '张掖市', '平凉市', '酒泉市', '庆阳市', '定西市', '陇南市', '临夏回族自治州', '甘南藏族自治州'] },
	{ province: '青海省', cities: ['西宁市', '海东市', '海北藏族自治州', '黄南藏族自治州', '海南藏族自治州', '果洛藏族自治州', '玉树藏族自治州', '海西蒙古族藏族自治州'] },
	{ province: '宁夏回族自治区', cities: ['银川市', '石嘴山市', '吴忠市', '固原市', '中卫市'] },
	{ province: '新疆维吾尔自治区', cities: ['乌鲁木齐市', '克拉玛依市', '吐鲁番市', '哈密市', '昌吉回族自治州', '博尔塔拉蒙古自治州', '巴音郭楞蒙古自治州', '阿克苏地区', '克孜勒苏柯尔克孜自治州', '喀什地区', '和田地区', '伊犁哈萨克自治州', '塔城地区', '阿勒泰地区', '石河子市', '阿拉尔市', '图木舒克市', '五家渠市', '北屯市', '铁门关市', '双河市', '可克达拉市', '昆玉市', '胡杨河市'] },
	{ province: '内蒙古自治区', cities: ['呼和浩特市', '包头市', '乌海市', '赤峰市', '通辽市', '鄂尔多斯市', '呼伦贝尔市', '巴彦淖尔市', '乌兰察布市', '兴安盟', '锡林郭勒盟', '阿拉善盟'] },
	{ province: '香港特别行政区', cities: ['香港岛', '九龙', '新界'] },
	{ province: '澳门特别行政区', cities: ['澳门半岛', '氹仔', '路环'] },
	{ province: '台湾省', cities: ['台北市', '新北市', '桃园市', '台中市', '台南市', '高雄市', '基隆市', '新竹市', '嘉义市'] }
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
		}
	} catch (error) {
		console.error('获取项目分类失败:', error)
	}
}


// 年龄要求变化
const onAgeChange = (e) => {
	const index = e.detail.value
	selectedAgeIndex.value = index
	searchForm.value.ageRequirement = ageOptions.value[index] === '不限' ? '' : ageOptions.value[index]
	handleSearch()
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
	// 初始化临时选中的分类
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

// 获取项目列表
const getProjectList = async (isLoadMore = false) => {
	if (isLoadMore) {
		loadingMore.value = true
	} else {
		loading.value = true
	}
	
	try {
		const res = await request('/project/list', {
			method: 'GET',
			data: {
				pageNum: currentPage.value,
				projectName: searchForm.value.projectName,
				city: searchForm.value.city,
				projectFieldId: searchForm.value.projectFieldId || 0,
				ageRequirement: searchForm.value.ageRequirement
			}
		})
		
		if (res.code === 200) {
			const newList = res.data.records || []
			if (isLoadMore) {
				projectList.value = [...projectList.value, ...newList]
			} else {
				projectList.value = newList
			}
			total.value = res.data.total || 0
		} else {
			uni.showToast({ title: res.msg || '获取项目列表失败', icon: 'none' })
		}
	} catch (error) {
		console.error('获取项目列表失败:', error)
		uni.showToast({ title: '获取项目列表失败', icon: 'none' })
	} finally {
		loading.value = false
		loadingMore.value = false
	}
}

// 搜索
const handleSearch = () => {
	currentPage.value = 1
	getProjectList()
}

// 重置筛选
const handleReset = () => {
	searchForm.value = {
		projectName: '',
		city: '',
		projectFieldId: '',
		ageRequirement: ''
	}
	selectedCities.value = []
	selectedCategories.value = []
	selectedParentField.value = null
	selectedChildField.value = null
	selectedParentIndex.value = -1
	selectedChildIndex.value = -1
	childFields.value = []
	selectedAgeIndex.value = 0
	currentPage.value = 1
	getProjectList()
}

// 加载更多
const loadMore = () => {
	if (hasMore.value && !loadingMore.value && !loading.value) {
		currentPage.value++
		getProjectList(true)
	}
}

// 下拉刷新
const onRefresh = async () => {
	refreshing.value = true
	
	try {
		// 重置到第一页
		currentPage.value = 1
		projectList.value = []
		
		// 重新获取数据
		await getProjectList()
		
	} catch (error) {
		console.error('刷新失败:', error)
	} finally {
		refreshing.value = false
	}
}

// 跳转到项目详情
const goToProjectDetail = (projectId) => {
	uni.navigateTo({
		url: `/pages/project/detail?id=${projectId}`
	})
}

// 跳转到创建项目页面
const goToCreateProject = () => {
	uni.navigateTo({
		url: '/pages/project/createProject'
	})
}

// 页面加载
onMounted(() => {
	getProjectFields()
	getProjectList()
})
</script>

<style scoped>
.project-list-page {
	min-height: 100vh;
	background-color: var(--bgColor1);
	display: flex;
	flex-direction: column;
}

/* 搜索栏 */
.search-bar {
	background-color: #fff;
	padding: 20rpx 24rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.darkMode .search-bar {
	background-color: var(--bgColor2);
	box-shadow: none;
}

.search-input-wrapper {
	display: flex;
	align-items: center;
	background-color: var(--bgColor1);
	border-radius: 36rpx;
	padding: 0 24rpx;
	height: 72rpx;
}

.darkMode .search-input-wrapper {
	background-color: var(--bgColor3);
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: var(--textColor1);
}

.search-icon {
	width: 40rpx;
	height: 40rpx;
	margin-left: 12rpx;
}

/* 筛选栏 */
.filter-bar {
	background-color: #fff;
	padding: 16rpx 0;
	border-bottom: 1rpx solid var(--borderColor);
}

.darkMode .filter-bar {
	background-color: var(--bgColor2);
}

.filter-scroll {
	white-space: nowrap;
	padding: 0 24rpx;
	display: flex;
	flex-direction: row;
}

.filter-picker {
	display: inline-block;
	flex-shrink: 0;
}

.filter-item {
	display: inline-flex;
	flex-shrink: 0;
	align-items: center;
	padding: 12rpx 24rpx;
	background-color: var(--bgColor1);
	border-radius: 36rpx;
	margin-right: 16rpx;
	white-space: nowrap;
}

.darkMode .filter-item {
	background-color: var(--bgColor3);
}

.filter-text {
	font-size: 26rpx;
	color: var(--textColor3);
}

.filter-text-active {
	font-size: 26rpx;
	color: #00A872;
	font-weight: bold;
}

.filter-arrow {
	font-size: 20rpx;
	color: #999;
	margin-left: 8rpx;
}

.reset-btn {
	background-color: #fff3e0;
}

.reset-text {
	font-size: 26rpx;
	color: #f57c00;
}

/* 项目列表 */
.project-scroll {
	flex: 1;
	height: calc(100vh - 180rpx);
}

.project-list {
	padding: 20rpx 24rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.project-card {
	background-color: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
	transition: transform 0.2s;
}

.darkMode .project-card {
	background-color: var(--bgColor2);
	box-shadow: none;
}

.project-card:active {
	transform: scale(0.98);
}

.project-image-container {
	position: relative;
	width: 100%;
	height: 360rpx;
	overflow: hidden;
}

/* 九宫格布局 */
.image-grid {
	position: relative;
	width: 100%;
	height: 100%;
	display: flex;
	flex-wrap: wrap;
	gap: 4rpx;
}

/* 1张图片：占满整个容器 */
.grid-image-1 {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

/* 2张图片：左右平分 */
.grid-image-2 {
	width: calc(50% - 2rpx);
	height: 100%;
	object-fit: cover;
}

/* 3张图片：左侧大图 + 右侧上下两张小图 */
.grid-image-3:nth-child(1) {
	width: calc(66.66% - 2rpx);
	height: 100%;
	object-fit: cover;
}

.grid-image-3:nth-child(2),
.grid-image-3:nth-child(3) {
	width: calc(33.33% - 2rpx);
	height: calc(50% - 2rpx);
	object-fit: cover;
}

.grid-image-3:nth-child(2) {
	position: absolute;
	top: 0;
	right: 0;
}

.grid-image-3:nth-child(3) {
	position: absolute;
	bottom: 0;
	right: 0;
}

/* 4张图片：2x2网格 */
.grid-image-4 {
	width: calc(50% - 2rpx);
	height: calc(50% - 2rpx);
	object-fit: cover;
}

/* 5张图片：2x3布局，前2张占一行，后3张占一行 */
.grid-image-5 {
	object-fit: cover;
	flex-shrink: 0;
}

.grid-image-5:nth-child(1),
.grid-image-5:nth-child(2) {
	width: calc(50% - 2rpx);
	height: calc(50% - 2rpx);
}

.grid-image-5:nth-child(3),
.grid-image-5:nth-child(4),
.grid-image-5:nth-child(5) {
	width: calc(33.33% - 2.67rpx);
	height: calc(50% - 2rpx);
}

/* 6张图片：2x3网格 */
.grid-image-6 {
	width: calc(33.33% - 3rpx);
	height: calc(50% - 2rpx);
	object-fit: cover;
}

.project-image-single {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

/* 更多图片蒙层 */
.more-images-overlay {
	position: absolute;
	bottom: 0;
	right: 0;
	width: calc(33.33% - 3rpx);
	height: calc(50% - 2rpx);
	background-color: rgba(0, 0, 0, 0.6);
	display: flex;
	align-items: center;
	justify-content: center;
	backdrop-filter: blur(10rpx);
}

.more-images-text {
	font-size: 32rpx;
	color: #fff;
	font-weight: bold;
}

.member-badge {
	position: absolute;
	top: 16rpx;
	right: 16rpx;
	background-color: rgba(0, 0, 0, 0.6);
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	backdrop-filter: blur(10rpx);
}

.member-badge-text {
	font-size: 22rpx;
	color: #fff;
}

.project-content {
	padding: 24rpx;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.project-name {
	font-size: 32rpx;
	font-weight: bold;
	color: var(--textColor1);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.project-intro {
	font-size: 26rpx;
	color: var(--textColor3);
	line-height: 1.6;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	line-clamp: 2;
	-webkit-box-orient: vertical;
}

.project-tags {
	display: flex;
	align-items: center;
	gap: 12rpx;
	flex-wrap: wrap;
}

.tag {
	display: flex;
	align-items: center;
	padding: 8rpx 16rpx;
	background-color: var(--bgColor1);
	border-radius: 20rpx;
}

.darkMode .tag {
	background-color: var(--bgColor3);
}

.tag-icon {
	width: 22rpx;
	height: 22rpx;
	font-size: 22rpx;
	margin-right: 6rpx;
}

.tag-text {
	font-size: 22rpx;
	color: var(--textColor3);
}

.member-list {
	display: flex;
	align-items: center;
	gap: -12rpx;
	margin-top: 8rpx;
}

.member-avatar {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	border: 3rpx solid #fff;
}

.member-more {
	margin-left: 20rpx;
	font-size: 24rpx;
	color: #999;
}

/* 加载状态 */
.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 0;
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #667eea;
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

.load-more {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx 0;
	gap: 12rpx;
}

.loading-more-spinner {
	width: 40rpx;
	height: 40rpx;
	border: 3rpx solid #f3f3f3;
	border-top: 3rpx solid #667eea;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

.load-more-text {
	font-size: 24rpx;
	color: #999;
}

.no-more {
	display: flex;
	justify-content: center;
	padding: 40rpx 0;
}

.no-more-text {
	font-size: 24rpx;
	color: #ccc;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 0;
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

/* 城市选择弹窗 */
.popup-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
}

.city-popup-content {
	width: 92%;
	max-height: 85vh;
	background-color: #fff;
	border-radius: 32rpx;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
}

.darkMode .city-popup-content {
	background-color: var(--bgColor2);
}

.popup-content {
	width: 600rpx;
	background-color: #fff;
	border-radius: 16rpx;
	padding: 32rpx;
}

.popup-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 40rpx 32rpx;
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
}

.popup-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #fff;
}

.popup-close {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	background-color: rgba(255, 255, 255, 0.2);
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s;
}

.popup-close:active {
	background-color: rgba(255, 255, 255, 0.3);
	transform: scale(0.9);
}

.close-icon {
	font-size: 40rpx;
	color: #fff;
	line-height: 1;
	font-weight: 300;
}

/* 城市搜索框 */
.city-search-wrapper {
	position: relative;
	padding: 24rpx 32rpx;
	background-color: #fff;
	display: flex;
	align-items: center;
	border-bottom: 1rpx solid var(--borderColor);
}

.darkMode .city-search-wrapper {
	background-color: var(--bgColor2);
}

.search-icon-wrapper {
	position: absolute;
	left: 48rpx;
	z-index: 1;
}

.search-icon {
	font-size: 28rpx;
	opacity: 0.5;
}

.city-search-input {
	flex: 1;
	height: 72rpx;
	padding: 0 48rpx 0 64rpx;
	background-color: var(--bgColor1);
	border-radius: 36rpx;
	font-size: 28rpx;
	color: var(--textColor1);
}

.darkMode .city-search-input {
	background-color: var(--bgColor3);
}

.clear-icon {
	position: absolute;
	right: 48rpx;
	width: 40rpx;
	height: 40rpx;
	border-radius: 50%;
	background-color: #ddd;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	color: #fff;
	transition: all 0.2s;
}

.clear-icon:active {
	transform: scale(0.9);
}

/* 已选城市 */
.selected-cities {
	padding: 24rpx 32rpx;
	border-bottom: 1rpx solid var(--borderColor);
	background: linear-gradient(to bottom, var(--bgColor1) 0%, #fff 100%);
}

.darkMode .selected-cities {
	background: linear-gradient(to bottom, var(--bgColor2) 0%, var(--bgColor3) 100%);
}

.selected-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.selected-title {
	font-size: 26rpx;
	color: #00A872;
	font-weight: 600;
}

.clear-all {
	font-size: 24rpx;
	color: #ff4d4f;
	padding: 8rpx 16rpx;
	border-radius: 16rpx;
	background-color: #fff;
	border: 1rpx solid #ffccc7;
}

.selected-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.selected-tag {
	display: inline-flex;
	align-items: center;
	padding: 12rpx 20rpx;
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	border-radius: 24rpx;
	gap: 12rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 168, 114, 0.2);
	transition: all 0.2s;
}

.selected-tag:active {
	transform: scale(0.95);
}

.tag-name {
	font-size: 26rpx;
	color: #fff;
	font-weight: 500;
}

.tag-close {
	width: 32rpx;
	height: 32rpx;
	border-radius: 50%;
	background-color: rgba(255, 255, 255, 0.3);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	color: #fff;
	font-weight: bold;
	line-height: 1;
}

/* 城市列表滚动区域 */
.city-list-scroll {
	flex: 1;
	height: 0;
	padding: 16rpx 0;
	background-color: var(--bgColor1);
}

.darkMode .city-list-scroll {
	background-color: var(--bgColor1);
}

.province-group {
	margin-bottom: 8rpx;
}

.province-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 24rpx 32rpx;
	background-color: #fff;
	margin-bottom: 2rpx;
}

.darkMode .province-header {
	background-color: var(--bgColor2);
}

.province-info {
	display: flex;
	align-items: center;
	flex: 1;
	gap: 12rpx;
}

.province-name {
	font-size: 30rpx;
	font-weight: 600;
	color: var(--textColor1);
}

.province-arrow {
	font-size: 20rpx;
	color: #999;
	transition: transform 0.3s;
}

.province-select-btn {
	display: flex;
	align-items: center;
	gap: 6rpx;
	padding: 12rpx 24rpx;
	background-color: #fff;
	border: 2rpx solid #00A872;
	border-radius: 24rpx;
	color: #00A872;
	transition: all 0.2s;
}

.province-select-btn:active {
	transform: scale(0.95);
}

.province-selected {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	border-color: transparent;
	box-shadow: 0 4rpx 12rpx rgba(0, 168, 114, 0.2);
}

.province-selected .select-btn-text,
.province-selected .check-icon {
	color: #fff;
}

.check-icon {
	font-size: 24rpx;
	font-weight: bold;
}

.select-btn-text {
	font-size: 24rpx;
	font-weight: 500;
}

.city-grid {
	display: flex;
	flex-wrap: wrap;
	padding: 16rpx 24rpx;
	gap: 12rpx;
}

.city-item {
	position: relative;
	flex: 0 0 calc(25% - 9rpx);
	padding: 16rpx 8rpx;
	background-color: #fff;
	border-radius: 12rpx;
	text-align: center;
	transition: all 0.2s;
	border: 2rpx solid transparent;
}

.darkMode .city-item {
	background-color: var(--bgColor2);
}

.city-item:active {
	transform: scale(0.95);
}

.city-selected {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	border-color: transparent;
	box-shadow: 0 4rpx 12rpx rgba(0, 168, 114, 0.2);
}

.city-name {
	font-size: 26rpx;
	color: var(--textColor1);
	font-weight: 500;
}

.city-selected .city-name {
	color: #fff;
	font-weight: 600;
}

.city-input-wrapper {
	margin-bottom: 24rpx;
}

.city-input {
	width: 100%;
	height: 72rpx;
	padding: 0 24rpx;
	background-color: #f5f5f5;
	border-radius: 12rpx;
	font-size: 28rpx;
}

/* 底部按钮 */
.popup-footer {
	display: flex;
	gap: 24rpx;
	padding: 24rpx 32rpx;
	background-color: #fff;
	border-top: 1rpx solid var(--borderColor);
}

.darkMode .popup-footer {
	background-color: var(--bgColor2);
}

.footer-btn {
	flex: 1;
	height: 88rpx;
	border-radius: 44rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s;
}

.footer-btn:active {
	transform: scale(0.98);
}

.cancel-btn {
	background-color: var(--bgColor1);
}

.darkMode .cancel-btn {
	background-color: var(--bgColor3);
}

.cancel-btn .btn-text {
	color: var(--textColor3);
	font-size: 30rpx;
	font-weight: 500;
}

.confirm-btn {
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	box-shadow: 0 8rpx 24rpx rgba(0, 168, 114, 0.3);
}

.confirm-btn .btn-text {
	color: #fff;
	font-size: 30rpx;
	font-weight: 600;
}

/* 发起项目按钮 */
.create-project-btn {
	position: fixed;
	bottom: 100rpx;
	right: 40rpx;
	width: 140rpx;
	height: 140rpx;
	background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
	border-radius: 50%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 24rpx rgba(0, 168, 114, 0.4);
	z-index: 100;
	transition: transform 0.2s;
}

.create-project-btn:active {
	transform: scale(0.95);
}

.create-btn-icon {
	font-size: 48rpx;
	color: #fff;
	font-weight: 300;
	line-height: 1;
}

.create-btn-text {
	font-size: 20rpx;
	color: #fff;
	margin-top: 8rpx;
}
</style>
