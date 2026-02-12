// 全局分享混入
// 为所有页面添加微信分享功能

export default {
	// 分享给朋友
	onShareAppMessage(res) {
		// 获取当前页面信息
		const pages = getCurrentPages()
		const currentPage = pages[pages.length - 1]
		const route = currentPage.route
		const options = currentPage.options

		// 构建分享路径
		let path = `/${route}`
		if (options && Object.keys(options).length > 0) {
			const params = Object.keys(options).map(key => `${key}=${options[key]}`).join('&')
			path += `?${params}`
		}

		// 获取页面标题
		let title = '众创搭'
		try {
			// 尝试从页面配置获取标题
			const pageConfig = __uniConfig.pages.find(p => p.path === route)
			if (pageConfig && pageConfig.style && pageConfig.style.navigationBarTitleText) {
				title = pageConfig.style.navigationBarTitleText
			}
		} catch (e) {
			console.log('获取页面标题失败:', e)
		}

		return {
			title: title,
			path: path,
			imageUrl: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page1/logo.png' // 默认分享图片
		}
	},

	// 分享到朋友圈
	onShareTimeline(res) {
		// 获取当前页面信息
		const pages = getCurrentPages()
		const currentPage = pages[pages.length - 1]
		const route = currentPage.route
		const options = currentPage.options

		// 构建分享路径
		let path = `/${route}`
		if (options && Object.keys(options).length > 0) {
			const params = Object.keys(options).map(key => `${key}=${options[key]}`).join('&')
			path += `?${params}`
		}

		// 获取页面标题
		let title = '众创搭'
		try {
			const pageConfig = __uniConfig.pages.find(p => p.path === route)
			if (pageConfig && pageConfig.style && pageConfig.style.navigationBarTitleText) {
				title = pageConfig.style.navigationBarTitleText
			}
		} catch (e) {
			console.log('获取页面标题失败:', e)
		}

		return {
			title: title,
			query: options || {},
			imageUrl: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page1/logo.png' // 默认分享图片
		}
	}
}
