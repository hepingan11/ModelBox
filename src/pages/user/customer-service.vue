<template>
  <view class="cs-container">
    <view class="card">
      <text class="title">联系客服</text>
      <text class="desc">扫码添加客服，获取帮助与答疑</text>

      <view class="qr-holder">
        <view class="qr-box"></view>
        <text class="qr-text">客服二维码待导入</text>
      </view>
    </view>
  </view>
</template>

<script setup>
</script>

<style>
.cs-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4f2fb 50%, #f5f7fa 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 24rpx;
  box-sizing: border-box;
}

.card {
  width: 92%;
  background-color: #fff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.06);
  padding: 40rpx 30rpx;
  text-align: center;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.desc {
  font-size: 26rpx;
  color: #666;
  margin-top: 12rpx;
}

.qr-holder {
  margin-top: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-box {
  width: 400rpx;
  height: 400rpx;
  border: 2rpx dashed #c8c7cc;
  border-radius: 12rpx;
  background-color: #fafafa;
}

.qr-text {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #999;
}
</style>

