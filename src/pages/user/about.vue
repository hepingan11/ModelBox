<template>
    <view class="about-container">
        
        <!-- 应用信息 -->
        <view class="about-header">
            <image src="/static/logo.jpg" class="logo" mode="aspectFit"></image>
            <text class="app-name">宅宿送</text>
            <text class="app-version">版本 1.0.0</text>
        </view>
        
        <!-- 信息卡片 -->
        <view class="info-cards">
            <!-- 项目开源地址 -->
            <view class="info-card" @click="copyToClipboard('https://github.com/hepingan11/宅宿送-app')">
                <view class="card-left">
                    <text class="card-icon">🔗</text>
                    <text class="card-title">项目开源地址</text>
                </view>
                <view class="card-right">
                    <text class="card-value">GitHub</text>
                    <text class="card-arrow">></text>
                </view>
            </view>
            
            <!-- 开发者联系方式 -->
            <view class="info-card" @click="copyToClipboard('1641436566')">
                <view class="card-left">
                    <text class="card-icon">👨‍💻</text>
                    <text class="card-title">开发者QQ</text>
                </view>
                <view class="card-right">
                    <text class="card-value">1973016127</text>
                    <text class="card-arrow">></text>
                </view>
            </view>
            
            <!-- 加入团队合作 -->
            <view class="info-card" @click="showTeamInfo">
                <view class="card-left">
                    <text class="card-icon">🤝</text>
                    <text class="card-title">加入团队合作</text>
                </view>
                <view class="card-right">
                    <text class="card-arrow">></text>
                </view>
            </view>
            
            <!-- 商家合作 -->
            <view class="info-card" @click="showBusinessInfo">
                <view class="card-left">
                    <text class="card-icon">💼</text>
                    <text class="card-title">商家合作</text>
                </view>
                <view class="card-right">
                    <text class="card-arrow">></text>
                </view>
            </view>
        </view>
        
        <!-- 应用简介 -->
        <view class="app-intro">
            <text class="intro-title">应用简介</text>
            <text class="intro-content">
                宅宿送是一款集社交、商城、论坛于一体的综合性应用。我们致力于为用户提供便捷、安全的社交购物体验。应用采用前沿技术开发，支持多平台运行，界面简洁美观，功能丰富实用。
            </text>
        </view>
        
        <!-- 版权信息 -->
        <view class="copyright">
            <text class="copyright-text">© 2023-2024 宅宿送 团队 版权所有</text>
        </view>
    </view>
</template>

<script setup>
import { ref } from 'vue'

// 返回上一页
const goBack = () => {
    uni.navigateBack()
}

// 复制内容到剪贴板
const copyToClipboard = (content) => {
    uni.setClipboardData({
        data: content,
        success: () => {
            uni.showToast({
                title: '已复制到剪贴板',
                icon: 'success'
            })
        }
    })
}

// 显示团队合作信息
const showTeamInfo = () => {
    uni.showModal({
        title: '加入团队合作',
        content: '我们欢迎有才华的开发者、设计师和产品经理加入我们的团队！请发送您的简历到 1641436566@qq.com 或添加开发者QQ进行咨询。',
        showCancel: false,
        confirmText: '我知道了'
    })
}

// 显示商家合作信息
const showBusinessInfo = () => {
    uni.showModal({
        title: '商家合作',
        content: '如果您是商家并希望在宅宿送上展示您的产品，请联系我们的团队：1973016127@qq.com，或添加微信：hepinganheliuyi进行洽谈，我们将为您提供流量支持。',
        showCancel: false,
        confirmText: '我知道了'
    })
}
</script>

<style>
.about-container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: #f5f7fa;
    padding-bottom: 40rpx;
}

/* 导航栏样式 */
.nav-bar {
    display: flex;
    align-items: center;
    height: 90rpx;
    background-color: #fff;
    padding: 0 30rpx;
    position: relative;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
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
    color: #333;
}

.page-title {
    position: absolute;
    left: 0;
    right: 0;
    text-align: center;
    font-size: 32rpx;
    font-weight: 500;
    color: #333;
}

/* 应用信息 */
.about-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60rpx 0;
    background-color: #fff;
    margin-bottom: 20rpx;
}

.logo {
    width: 160rpx;
    height: 160rpx;
    border-radius: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.app-name {
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 10rpx;
}

.app-version {
    font-size: 24rpx;
    color: #999;
}

/* 信息卡片 */
.info-cards {
    background-color: #fff;
    margin-bottom: 20rpx;
}

.info-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
}

.info-card:last-child {
    border-bottom: none;
}

.card-left {
    display: flex;
    align-items: center;
}

.card-icon {
    font-size: 36rpx;
    margin-right: 20rpx;
}

.card-title {
    font-size: 28rpx;
    color: #333;
}

.card-right {
    display: flex;
    align-items: center;
}

.card-value {
    font-size: 28rpx;
    color: #666;
    margin-right: 10rpx;
}

.card-arrow {
    font-size: 28rpx;
    color: #ccc;
}

/* 应用简介 */
.app-intro {
    background-color: #fff;
    padding: 30rpx;
    margin-bottom: 20rpx;
}

.intro-title {
    font-size: 30rpx;
    font-weight: 500;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
}

.intro-content {
    font-size: 28rpx;
    color: #666;
    line-height: 1.6;
}

/* 版权信息 */
.copyright {
    padding: 30rpx;
    text-align: center;
}

.copyright-text {
    font-size: 24rpx;
    color: #999;
}
</style>
