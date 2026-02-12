<template>
	<view class="create-project-page" :class="themeClass">
		<scroll-view scroll-y class="form-scroll">
			<view class="form-container">
				<!-- 项目名称 -->
				<view class="form-item">
					<view class="form-label">
						<text class="required">*</text>
						<text>项目名称</text>
					</view>
					<input 
						v-model="formData.projectName" 
						class="form-input" 
						placeholder="请输入项目名称"
						maxlength="50"
					/>
				</view>
				
				<!-- 项目介绍 -->
				<view class="form-item">
					<view class="form-label">
						<text class="required">*</text>
						<text>项目介绍</text>
					</view>
					<textarea 
						v-model="formData.introduce" 
						class="form-textarea" 
						placeholder="请详细介绍您的项目，包括项目目标、内容、预期成果等"
						maxlength="1500"
						:show-confirm-bar="false"
					/>
					<text class="char-count">{{ formData.introduce.length }}/1500</text>
				</view>
				
				<!-- 城市 -->
				<view class="form-item">
					<view class="form-label">
						<text class="required">*</text>
						<text>所在城市</text>
						<text class="label-tip">（可选择多个城市）</text>
					</view>
					<picker 
						mode="multiSelector" 
						:range="cityColumns" 
						:value="cityPickerValue"
						@change="onCityChange"
						@columnchange="onCityColumnChange"
					>
						<view class="picker-input">
							<text class="picker-placeholder">
								点击添加城市
							</text>
							<text class="picker-arrow">▼</text>
						</view>
					</picker>
					
					<!-- 已选城市列表 -->
					<view class="selected-cities" v-if="selectedCities.length > 0">
						<view 
							class="city-tag" 
							v-for="(city, index) in selectedCities" 
							:key="index"
						>
							<text class="city-name">{{ city }}</text>
							<text class="city-remove" @click="removeCity(index)">×</text>
						</view>
					</view>
				</view>
				
				<!-- 年龄要求 -->
				<view class="form-item">
					<view class="form-label">
						<text>年龄要求</text>
					</view>
					<picker 
						mode="selector" 
						:range="ageOptions" 
						@change="onAgeChange"
						:value="selectedAgeIndex"
					>
						<view class="picker-input">
							<text :class="formData.ageRequirement ? 'picker-text' : 'picker-placeholder'">
								{{ formData.ageRequirement || '请选择年龄要求（可选）' }}
							</text>
							<text class="picker-arrow">▼</text>
						</view>
					</picker>
				</view>
				
				<!-- 项目分类 -->
				<view class="form-item">
					<view class="form-label">
						<text class="required">*</text>
						<text>项目分类</text>
					</view>
					<picker 
						mode="selector" 
						:range="parentFields" 
						range-key="fieldName"
						@change="onParentFieldChange"
						:value="selectedParentIndex"
					>
						<view class="picker-input">
							<text :class="selectedParentField ? 'picker-text' : 'picker-placeholder'">
								{{ selectedParentField ? selectedParentField.fieldName : '请选择项目分类' }}
							</text>
							<text class="picker-arrow">▼</text>
						</view>
					</picker>
				</view>
				
				<!-- 子分类 -->
				<view class="form-item" v-if="childFields.length > 0">
					<view class="form-label">
						<text>子分类</text>
					</view>
					<picker 
						mode="selector" 
						:range="childFields" 
						range-key="fieldName"
						@change="onChildFieldChange"
						:value="selectedChildIndex"
					>
						<view class="picker-input">
							<text :class="selectedChildField ? 'picker-text' : 'picker-placeholder'">
								{{ selectedChildField ? selectedChildField.fieldName : '请选择子分类（可选）' }}
							</text>
							<text class="picker-arrow">▼</text>
						</view>
					</picker>
				</view>
				
				<!-- 项目图片 -->
				<view class="form-item">
					<view class="form-label">
						<text>项目图片</text>
						<text class="label-tip">（最多上传6张）</text>
					</view>
					<view class="image-upload-container">
						<view 
							class="image-item" 
							v-for="(img, index) in imageList" 
							:key="index"
						>
							<image :src="img" class="uploaded-image" mode="aspectFill"></image>
							<view class="image-delete" @click="deleteImage(index)">
								<text class="delete-icon">×</text>
							</view>
						</view>
						<view 
							class="upload-btn" 
							@click="chooseImage"
							v-if="imageList.length < 6"
						>
							<text class="upload-icon">+</text>
							<text class="upload-text">上传图片</text>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
		
		<!-- 底部按钮 -->
		<view class="bottom-actions">
			<button class="cancel-btn" @click="handleCancel">取消</button>
			<button class="submit-btn" @click="handleSubmit" :disabled="submitting">
				{{ submitting ? '提交中...' : '发起项目' }}
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

import { apiBaseUrl } from '@/store/index'
import { useTheme } from '@/hooks/useTheme'

const { themeClass } = useTheme()

// 表单数据
const formData = ref({
	projectName: '',
	introduce: '',
	city: '',
	ageRequirement: '',
	projectFieldId: ''
})

// 图片列表
const imageList = ref([])

// 提交状态
const submitting = ref(false)

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

// 城市数据（每个省份一行）
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

// 城市选择器数据
const cityColumns = ref([[], []])
const cityPickerValue = ref([0, 0])
// 已选城市列表
const selectedCities = ref([])

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
			formData.value.projectFieldId = selectedParentField.value.projectFieldId
		} else {
			formData.value.projectFieldId = ''
		}
	} else {
		formData.value.projectFieldId = ''
	}
}

// 子分类变化
const onChildFieldChange = (e) => {
	const index = e.detail.value
	selectedChildIndex.value = index
	selectedChildField.value = childFields.value[index]
	
	if (selectedChildField.value) {
		formData.value.projectFieldId = selectedChildField.value.projectFieldId
	} else {
		formData.value.projectFieldId = selectedParentField.value ? selectedParentField.value.projectFieldId : ''
	}
}

// 年龄要求变化
const onAgeChange = (e) => {
	const index = e.detail.value
	selectedAgeIndex.value = index
	formData.value.ageRequirement = ageOptions.value[index] === '不限' ? '' : ageOptions.value[index]
}

// 初始化城市选择器数据
const initCityColumns = () => {
	const provinces = cityData.value.map(item => item.province)
	// 第一列添加"全省/全市"选项
	const cities = ['全省/全市', ...cityData.value[0].cities]
	cityColumns.value = [provinces, cities]
}

// 城市列变化
const onCityColumnChange = (e) => {
	const column = e.detail.column
	const value = e.detail.value
	
	if (column === 0) {
		// 省份变化，更新城市列表（添加"全省/全市"选项）
		const cities = ['全省/全市', ...cityData.value[value].cities]
		cityColumns.value[1] = cities
		cityPickerValue.value = [value, 0]
	}
}

// 城市选择确认
const onCityChange = (e) => {
	const [provinceIndex, cityIndex] = e.detail.value
	const province = cityData.value[provinceIndex].province
	
	let cityText = ''
	// 如果选择"全省/全市"（索引为0），只保存省份
	if (cityIndex === 0) {
		cityText = province
	} else {
		// 否则保存省份-城市格式
		const city = cityData.value[provinceIndex].cities[cityIndex - 1]
		cityText = `${province}-${city}`
	}
	
	// 检查是否已存在
	if (!selectedCities.value.includes(cityText)) {
		selectedCities.value.push(cityText)
		// 更新formData.city为逗号分隔的字符串
		formData.value.city = selectedCities.value.join(',')
	} else {
		uni.showToast({ title: '该城市已添加', icon: 'none' })
	}
	
	cityPickerValue.value = [provinceIndex, cityIndex]
}

// 移除城市
const removeCity = (index) => {
	selectedCities.value.splice(index, 1)
	// 更新formData.city
	formData.value.city = selectedCities.value.join(',')
}

// 选择图片
const chooseImage = () => {
	const maxCount = 6 - imageList.value.length
	uni.chooseImage({
		count: maxCount,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			imageList.value = [...imageList.value, ...res.tempFilePaths]
		}
	})
}

// 删除图片
const deleteImage = (index) => {
	uni.showModal({
		title: '提示',
		content: '确定删除这张图片吗？',
		success: (res) => {
			if (res.confirm) {
				imageList.value.splice(index, 1)
			}
		}
	})
}

// 上传单个图片到文件服务器获取URL
const uploadSingleImage = (imagePath) => {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: apiBaseUrl + '/system/uploadImg',
			filePath: imagePath,
			name: 'file',
			header: {
				'Authorization': uni.getStorageSync('token') || ''
			},
			success: (res) => {
				try {
					const data = JSON.parse(res.data)
					if (data.code === 200) {
						resolve(data.data) // 返回图片URL
					} else {
						reject(new Error(data.msg || '图片上传失败'))
					}
				} catch (error) {
					reject(error)
				}
			},
			fail: (error) => {
				reject(error)
			}
		})
	})
}

// 上传项目数据和图片
// 流程：
// 1. 先将所有图片上传到 /system/uploadImg 获取 URL
// 2. 将 URL 列表作为 List<String> imageUrlList 发送到 /project/create
const uploadProjectWithImages = async () => {
	try {
		let imageUrls = []
		
		// 如果有图片，先上传所有图片获取URL
		if (imageList.value.length > 0) {
			uni.showLoading({ title: '上传图片中...' })
			
			// 并发上传所有图片
			const uploadPromises = imageList.value.map(imagePath => uploadSingleImage(imagePath))
			imageUrls = await Promise.all(uploadPromises)
			
			uni.hideLoading()
			console.log('图片上传成功，URLs:', imageUrls)
		}
		
		// 发起创建项目请求，imageUrlList 为字符串数组
		const res = await request('/project/create', {
			method: 'POST',
			data: {
				projectName: formData.value.projectName,
				introduce: formData.value.introduce,
				city: formData.value.city,
				ageRequirement: formData.value.ageRequirement,
				projectFieldId: formData.value.projectFieldId,
				imageUrlList: imageUrls  // List<String> 图片URL列表
			}
		})
		
		return res
	} catch (error) {
		uni.hideLoading()
		throw error
	}
}

// 表单验证
const validateForm = () => {
	if (!formData.value.projectName.trim()) {
		uni.showToast({ title: '请输入项目名称', icon: 'none' })
		return false
	}
	
	if (!formData.value.introduce.trim()) {
		uni.showToast({ title: '请输入项目介绍', icon: 'none' })
		return false
	}
	
	if (!formData.value.city.trim()) {
		uni.showToast({ title: '请输入所在城市', icon: 'none' })
		return false
	}
	
	if (!formData.value.projectFieldId) {
		uni.showToast({ title: '请选择项目分类', icon: 'none' })
		return false
	}
	
	return true
}

// 提交表单
const handleSubmit = async () => {
	if (!validateForm()) {
		return
	}
	
	submitting.value = true
	uni.showLoading({ title: '提交中...' })
	
	try {
		// 上传项目数据和图片
		const res = await uploadProjectWithImages()
		
		if (res.code === 200) {
			uni.showToast({ 
				title: '项目创建成功', 
				icon: 'success',
				duration: 2000
			})
			
			setTimeout(() => {
				uni.navigateBack()
			}, 2000)
		} else {
			uni.showToast({ title: res.msg || '创建失败', icon: 'none' })
		}
	} catch (error) {
		console.error('创建项目失败:', error)
		uni.showToast({ title: error.message || '创建失败', icon: 'none' })
	} finally {
		submitting.value = false
		uni.hideLoading()
	}
}

// 取消
const handleCancel = () => {
	if (formData.value.projectName || formData.value.introduce || imageList.value.length > 0) {
		uni.showModal({
			title: '提示',
			content: '确定要放弃创建项目吗？',
			success: (res) => {
				if (res.confirm) {
					uni.navigateBack()
				}
			}
		})
	} else {
		uni.navigateBack()
	}
}

// 页面加载
onMounted(() => {
	getProjectFields()
	initCityColumns()
})
</script>

<style scoped>
.create-project-page {
	min-height: 100vh;
	background-color: var(--bgColor1);
	display: flex;
	flex-direction: column;
}

.form-scroll {
	flex: 1;
	height: calc(100vh - 120rpx);
}

.form-container {
	padding: 24rpx;
}

.form-item {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 24rpx;
	margin-bottom: 20rpx;
}

.darkMode .form-item {
	background-color: var(--bgColor2);
}

.form-label {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
	font-size: 28rpx;
	color: var(--textColor1);
	font-weight: bold;
}

.required {
	color: #ff4d4f;
	margin-right: 4rpx;
}

.label-tip {
	font-size: 24rpx;
	color: #999;
	font-weight: normal;
	margin-left: 8rpx;
}

.form-input {
	width: 100%;
	height: 72rpx;
	padding: 0 24rpx;
	background-color: var(--bgColor1);
	border-radius: 12rpx;
	font-size: 28rpx;
	color: var(--textColor1);
}

.darkMode .form-input {
	background-color: var(--bgColor3);
}

.form-textarea {
	width: 100%;
	min-height: 200rpx;
	padding: 20rpx 24rpx;
	background-color: var(--bgColor1);
	border-radius: 12rpx;
	font-size: 28rpx;
	color: var(--textColor1);
	line-height: 1.6;
}

.darkMode .form-textarea {
	background-color: var(--bgColor3);
}

.char-count {
	display: block;
	text-align: right;
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
}

.picker-input {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 72rpx;
	padding: 0 24rpx;
	background-color: var(--bgColor1);
	border-radius: 12rpx;
}

.darkMode .picker-input {
	background-color: var(--bgColor3);
}

.picker-text {
	font-size: 28rpx;
	color: var(--textColor1);
}

.picker-placeholder {
	font-size: 28rpx;
	color: var(--textColor3);
}

.picker-arrow {
	font-size: 20rpx;
	color: #999;
}

/* 已选城市标签 */
.selected-cities {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-top: 16rpx;
}

.city-tag {
	display: flex;
	align-items: center;
	padding: 8rpx 16rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 24rpx;
	transition: all 0.3s ease;
}

.city-tag:active {
	transform: scale(0.95);
}

.city-name {
	font-size: 24rpx;
	color: #fff;
	margin-right: 8rpx;
}

.city-remove {
	font-size: 32rpx;
	color: #fff;
	line-height: 1;
	font-weight: bold;
	padding: 0 4rpx;
	cursor: pointer;
}

/* 图片上传 */
.image-upload-container {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.image-item {
	position: relative;
	width: 200rpx;
	height: 200rpx;
	border-radius: 12rpx;
	overflow: hidden;
}

.uploaded-image {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.image-delete {
	position: absolute;
	top: 8rpx;
	right: 8rpx;
	width: 48rpx;
	height: 48rpx;
	background-color: rgba(0, 0, 0, 0.6);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.delete-icon {
	font-size: 36rpx;
	color: #fff;
	line-height: 1;
}

.upload-btn {
	width: 200rpx;
	height: 200rpx;
	background-color: var(--bgColor1);
	border: 2rpx dashed var(--borderColor);
	border-radius: 12rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	transition: all 0.2s;
}

.darkMode .upload-btn {
	background-color: var(--bgColor3);
}

.upload-btn:active {
	background-color: #e8e8e8;
}

.upload-icon {
	font-size: 48rpx;
	color: #999;
	line-height: 1;
}

.upload-text {
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
}

/* 底部按钮 */
.bottom-actions {
	display: flex;
	gap: 16rpx;
	padding: 24rpx;
	background-color: #fff;
	box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.darkMode .bottom-actions {
	background-color: var(--bgColor2);
	box-shadow: none;
}

.cancel-btn,
.submit-btn {
	flex: 1;
	height: 88rpx;
	border-radius: 44rpx;
	font-size: 32rpx;
	border: none;
}

.cancel-btn {
	background-color: var(--bgColor1);
	color: var(--textColor2);
}

.darkMode .cancel-btn {
	background-color: var(--bgColor3);
}

.submit-btn {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
}

.submit-btn[disabled] {
	background: #d9d9d9;
	color: #999;
}
</style>
