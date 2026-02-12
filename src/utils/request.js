const BASE_URL = 'http://127.0.0.1:8625';

// 封装请求方法
const request = (url, options = {}) => {
	return new Promise((resolve, reject) => {
		// 调用 uni.request 发起请求
		// 获取本地存储的 token
		const token = uni.getStorageSync('token');


		// 创建请求对象
		const requestTask = uni.request({
			// 拼接基础 URL 和请求的路径
			url: `${BASE_URL}${url}`,
			...options,
			timeout: 10000, // 设置10秒超时
			header: {
				...options.header,
				'token': `${token}`
			},
			success: (res) => {
				if (res.data.code === 200) {
					resolve(res.data);
				} else if (res.data.code === 401) {
					uni.showToast({
						title: '请先登录',
						icon: 'none',
						duration: 2000
					});
					uni.removeStorageSync('token');
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/index/index?tab=2'
						});
					}, 1500);
				} else {
					uni.showToast({
						title: res.data.msg || '未知错误',
						icon: 'none',
						duration: 2000
					});
					resolve(res.data);
				}
			},
			fail: (error) => {
				console.error('请求失败:', error);

				// 网络错误的特殊处理
				if (error.errMsg.includes('request:fail')) {
					uni.getNetworkType({
						success: (result) => {
							if (result.networkType === 'none') {
								uni.showToast({
									title: '网络连接已断开',
									icon: 'none',
									duration: 2000
								});
							} else {
								uni.showToast({
									title: '网络不稳定，请稍后重试',
									icon: 'none',
									duration: 2000
								});
							}
						}
					});
				}

				reject(error);
			}
		});

		// 设置请求超时处理
		setTimeout(() => {
			requestTask.abort(); // 超时后中断请求
			reject(new Error('请求超时'));
		}, 15000);
	});
};


// 请求拦截器
const httpInterceptor = {
	invoke(options) {
		// 1. 非 http 开头需拼接地址
		if (!options.url.startsWith('http')) {
			options.url = BASE_URL + options.url;
		}
		// 2. 请求超时
		options.timeout = 10000;
		// 3. 添加小程序端请求头标识
		options.header = {
			...options.header,
			'source-client': 'miniapp'
		};
		// 4. 添加 token
		const token = uni.getStorageSync('token');
		if (token) {
			options.header['token'] = `${token}`;
		}
	}
};

// 注册拦截器
uni.addInterceptor('request', httpInterceptor);
uni.addInterceptor('uploadFile', httpInterceptor);

export default request;