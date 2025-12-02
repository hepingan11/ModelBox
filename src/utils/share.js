// 分享工具函数

/**
 * 显示分享菜单
 * @param {Object} options 分享配置
 * @param {String} options.title 分享标题
 * @param {String} options.summary 分享描述
 * @param {String} options.imageUrl 分享图片URL
 * @param {String} options.path 分享页面路径（如：/pages/project/detail?id=123）
 */
export const showShareMenu = (options = {}) => {
	const {
		title = '众创搭',
		summary = '发现更多精彩内容',
		imageUrl = '',
		path = '/pages/index/index'
	} = options
	
	uni.showActionSheet({
		itemList: ['分享给朋友', '分享到朋友圈'],
		success: (res) => {
			if (res.tapIndex === 0) {
				// 分享给朋友
				shareToFriend({ title, summary, imageUrl, path })
			} else if (res.tapIndex === 1) {
				// 分享到朋友圈
				shareToTimeline({ title, summary, imageUrl, path })
			}
		}
	})
}

/**
 * 分享给朋友
 */
const shareToFriend = ({ title, summary, imageUrl, path }) => {
	uni.share({
		provider: 'weixin',
		scene: 'WXSceneSession',
		type: 0,
		title: title,
		summary: summary,
		imageUrl: imageUrl,
		href: path,
		success: () => {
			uni.showToast({ title: '分享成功', icon: 'success' })
		},
		fail: (err) => {
			console.error('分享失败:', err)
			uni.showToast({ title: '分享失败', icon: 'none' })
		}
	})
}

/**
 * 分享到朋友圈
 */
const shareToTimeline = ({ title, summary, imageUrl, path }) => {
	uni.share({
		provider: 'weixin',
		scene: 'WXSceneTimeline',
		type: 0,
		title: title,
		summary: summary,
		imageUrl: imageUrl,
		href: path,
		success: () => {
			uni.showToast({ title: '分享成功', icon: 'success' })
		},
		fail: (err) => {
			console.error('分享失败:', err)
			uni.showToast({ title: '分享失败', icon: 'none' })
		}
	})
}

/**
 * 配置页面分享（用于onShareAppMessage和onShareTimeline）
 * @param {Object} options 分享配置
 * @returns {Object} 分享配置对象
 */
export const getShareConfig = (options = {}) => {
	const {
		title = '众创搭',
		summary = '发现更多精彩内容',
		imageUrl = '',
		path = '/pages/index/index'
	} = options
	
	return {
		title: title,
		path: path,
		imageUrl: imageUrl,
		summary: summary
	}
}
