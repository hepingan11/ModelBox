<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	.root {
	--theme-color: #cced71;
	--theme-color-light: #43f75b;
	--theme-color-shadow: rgba(94, 169, 87, 0.4);
	}
	/*每个页面公共css */
	
	/* 用户等级样式 - 全局通用 */
	.user-level {
		display: inline-block;
		font-size: 22rpx;
		padding: 4rpx 12rpx;
		border-radius: 20rpx;
		border-width: 2rpx;
		border-style: solid;
		background-color: transparent;
	}
	
	/* 不同等级的颜色 */

	.level-1 {
		border-color: #4caf50;
		color: #4caf50;
	}
	
	.level-2 {
		border-color: #0288d1;
		color: #0288d1;
	}
	
	.level-3 {
		border-color: #673ab7;
		color: #673ab7;
	}
	
	.level-4 {
		border-color: #fbc02d;
		color: #fbc02d;
	}
	
	.level-5 {
		border-color: #f57c00;
		color: #f57c00;
	}
	
	.level-6 {
		border-color: #e91e63;
		color: #e91e63;
		border-width: 3rpx;
	}
	
	.level-text {
		font-size: 22rpx;
		font-weight: bold;
	}
</style>


