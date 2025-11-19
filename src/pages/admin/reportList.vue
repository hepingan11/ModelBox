<template> 
    <view class="report-admin-container">
        <!-- 顶部导航栏 -->
        <view class="header">
            <text class="title">举报管理</text>
        </view>
        
        <!-- 筛选区域 -->
        <view class="filter-section">
            <view class="filter-tabs">
                <view 
                    class="filter-tab" 
                    :class="{ 'active': currentStatus === null }"
                    @click="changeStatus(null)"
                >
                    <text class="tab-text">全部</text>
                </view>
                <view 
                    class="filter-tab" 
                    :class="{ 'active': currentStatus === 0 }"
                    @click="changeStatus(0)"
                >
                    <text class="tab-text">待处理</text>
                </view>
                <view 
                    class="filter-tab" 
                    :class="{ 'active': currentStatus === 1 }"
                    @click="changeStatus(1)"
                >
                    <text class="tab-text">已处理</text>
                </view>
            </view>
        </view>
        
        <!-- 举报列表 -->
        <scroll-view 
            class="report-list" 
            scroll-y 
            :refresher-enabled="true"
            :refresher-triggered="isRefreshing"
            @refresherrefresh="onRefresh"
            @scrolltolower="loadMore"
        >
            <!-- 加载中状态 -->
            <view class="loading-state" v-if="isLoading && !isRefreshing">
                <view class="loading-spinner"></view>
                <text class="loading-text">加载中...</text>
            </view>
            
            <!-- 空数据状态 -->
            <view class="empty-state" v-if="reportList.length === 0 && !isLoading">
                <image src="/static/empty.png" class="empty-icon"></image>
                <text class="empty-text">暂无举报记录</text>
            </view>
            
            <!-- 举报列表项 -->
            <view class="report-list-content">
                <view 
                    class="report-item" 
                    v-for="item in reportList" 
                    :key="item.id"
                >
                    <view class="report-header">
                        <view class="report-type">
                            <text class="type-label">{{ getReportTypeName(item.type) }}</text>
                            <view :class="['status-tag', item.status === 1 ? 'completed' : 'processing']">
                                <text class="status-text">{{ item.status === 1 ? '已处理' : '待处理' }}</text>
                            </view>
                        </view>
                        <text class="report-time">{{ formatTime(item.createdTime) }}</text>
                    </view>
                    
                    <view class="report-content">
                        <view class="user-info">
                            <text class="user-label">举报人ID: </text>
                            <text class="user-value">{{ item.userId }}</text>
                        </view>
                        <view class="user-info">
                            <text class="user-label">被举报人ID: </text>
                            <text class="user-value">{{ item.reportUserId }}</text>
                        </view>
                        <view class="user-info">
                            <text class="user-label">被举报内容ID: </text>
                            <text class="user-value">{{ item.typeId }}</text>
                        </view>
                        <view class="content-section">
                            <text class="content-label">举报内容: </text>
                            <text class="content-text">{{ item.content || '无描述' }}</text>
                        </view>
                        
                        <!-- 举报图片 -->
                        <view class="image-preview" v-if="item.image">
                            <image :src="item.image" class="report-image" mode="aspectFill" @click="previewImage(item.image)"></image>
                        </view>
                    </view>
                    
                    <view class="report-footer">
                        <text class="report-date">举报时间: {{ formatDate(item.createdTime) }}</text>
                        <button 
                            v-if="item.status === 0" 
                            class="process-btn"
                            @click="handleReport(item.id)"
                        >标记为已处理</button>
                    </view>
                </view>
            </view>
            
            <!-- 加载更多 -->
            <view class="load-more" v-if="reportList.length > 0 && hasMore">
                <view class="loading-spinner small"></view>
                <text class="load-more-text">加载更多...</text>
            </view>
            
            <!-- 没有更多数据 -->
            <view class="no-more" v-if="reportList.length > 0 && !hasMore && !isLoading">
                <text class="no-more-text">没有更多数据了</text>
            </view>
        </scroll-view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '@/utils/request';

// 状态变量
const reportList = ref([]);
const isLoading = ref(false);
const isRefreshing = ref(false);
const currentPage = ref(1);
const hasMore = ref(true);
const currentStatus = ref(null); // null表示全部，0表示待处理，1表示已处理

// 获取举报列表
const getReportList = async (page = 1, refresh = false) => {
    if (page === 1) {
        isLoading.value = true;
    }
    
    try {
        // 构建请求参数
        const params = { pageNum: page };
        if (currentStatus.value !== null) {
            params.status = currentStatus.value;
        }
        
        const res = await request('/admin/report/list', {
            method: 'GET',
            data: params
        });
        
        if (res.code === 200) {
            const data = res.data || [];
            
            // 如果是刷新或第一页，直接替换列表
            if (refresh || page === 1) {
                reportList.value = data;
            } else {
                // 否则追加到列表
                reportList.value = [...reportList.value, ...data];
            }
            
            // 判断是否还有更多数据
            hasMore.value = data.length >= 10; // 假设每页10条数据
            
            // 更新当前页码
            currentPage.value = page;
        } else {
            uni.showToast({
                title: res.msg || '获取举报列表失败',
                icon: 'none'
            });
        }
    } catch (error) {
        console.error('获取举报列表失败:', error);
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        });
    } finally {
        isLoading.value = false;
        isRefreshing.value = false;
    }
};

// 处理举报
const handleReport = async (id) => {
    try {
        uni.showLoading({
            title: '处理中...',
            mask: true
        });
        
        const res = await request('/admin/report/status', {
            method: 'GET',
            data: { id }
        });
        
        if (res.code === 200) {
            uni.showToast({
                title: '处理成功',
                icon: 'success'
            });
            
            // 更新本地数据
            const index = reportList.value.findIndex(item => item.id === id);
            if (index !== -1) {
                reportList.value[index].status = 1;
            }
        } else {
            uni.showToast({
                title: res.msg || '处理失败',
                icon: 'none'
            });
        }
    } catch (error) {
        console.error('处理举报失败:', error);
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        });
    } finally {
        uni.hideLoading();
    }
};

// 切换状态筛选
const changeStatus = (status) => {
    if (currentStatus.value === status) return;
    
    currentStatus.value = status;
    currentPage.value = 1;
    getReportList(1, true);
};

// 下拉刷新
const onRefresh = () => {
    isRefreshing.value = true;
    currentPage.value = 1;
    getReportList(1, true);
};

// 加载更多
const loadMore = () => {
    if (!hasMore.value || isLoading.value) return;
    getReportList(currentPage.value + 1);
};

// 获取举报类型名称
const getReportTypeName = (type) => {
    const typeMap = {
        'comment': '评论举报',
        'chat': '消息举报',
        'forum': '帖子举报',
        'user': '用户举报',
        'goods': '商品举报'
    };
    return typeMap[type] || '其他举报';
};

// 格式化时间（相对时间）
const formatTime = (timeStr) => {
    if (!timeStr) return '';
    
    try {
        // 尝试解析不同格式的时间字符串
        let date;
        
        // 处理ISO格式或标准日期字符串
        if (typeof timeStr === 'string') {
            // 尝试处理 "yyyy-MM-dd HH:mm:ss" 格式
            if (timeStr.includes('-') && timeStr.includes(':')) {
                const parts = timeStr.replace(' ', 'T').replace(/\s/g, '');
                date = new Date(parts);
            } else {
                // 尝试直接解析
                date = new Date(timeStr);
            }
        } else if (typeof timeStr === 'number') {
            // 如果是时间戳
            date = new Date(timeStr);
        } else {
            return '未知时间';
        }
        
        // 检查日期是否有效
        if (isNaN(date.getTime())) {
            console.error('无效的日期格式:', timeStr);
            return '未知时间';
        }
        
        const now = new Date();
        const diff = now.getTime() - date.getTime();
        
        // 一分钟内
        if (diff < 60 * 1000) {
            return '刚刚';
        }
        // 一小时内
        else if (diff < 60 * 60 * 1000) {
            return Math.floor(diff / (60 * 1000)) + '分钟前';
        }
        // 一天内
        else if (diff < 24 * 60 * 60 * 1000) {
            return Math.floor(diff / (60 * 60 * 1000)) + '小时前';
        }
        // 一周内
        else if (diff < 7 * 24 * 60 * 60 * 1000) {
            return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前';
        }
        // 超过一周
        else {
            return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        }
    } catch (error) {
        console.error('格式化时间出错:', error, timeStr);
        return '未知时间';
    }
};

// 格式化日期（完整日期时间）
const formatDate = (timeStr) => {
    if (!timeStr) return '';
    
    try {
        // 尝试解析时间
        let date;
        if (typeof timeStr === 'string') {
            if (timeStr.includes('-') && timeStr.includes(':')) {
                const parts = timeStr.replace(' ', 'T').replace(/\s/g, '');
                date = new Date(parts);
            } else {
                date = new Date(timeStr);
            }
        } else if (typeof timeStr === 'number') {
            date = new Date(timeStr);
        } else {
            return '未知时间';
        }
        
        // 检查日期是否有效
        if (isNaN(date.getTime())) {
            return '未知时间';
        }
        
        // 格式化为 yyyy-MM-dd HH:mm:ss
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
    } catch (error) {
        console.error('格式化日期出错:', error);
        return '未知时间';
    }
};

// 预览图片
const previewImage = (imageUrl) => {
    uni.previewImage({
        urls: [imageUrl]
    });
};

// 页面加载时获取举报列表
onMounted(() => {
    getReportList();
});
</script>

<style lang="scss" scoped>
.report-admin-container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: #f5f7fa;
}

.header {
    padding: 20rpx 30rpx;
    background-color: #fff;
    border-bottom: 1rpx solid #eee;
    display: flex;
    align-items: center;
    justify-content: center;
    position: sticky;
    top: 0;
    z-index: 10;
}

.title {
    font-size: 32rpx;
    font-weight: 500;
    color: #333;
}

.filter-section {
    background-color: #fff;
    padding: 10rpx 30rpx;
    margin-bottom: 20rpx;
    border-bottom: 1rpx solid #eee;
}

.filter-tabs {
    display: flex;
    justify-content: space-around;
    align-items: center;
}

.filter-tab {
    padding: 16rpx 30rpx;
    border-radius: 30rpx;
    position: relative;
}

.filter-tab.active {
    background-color: #f0f7ff;
}

.filter-tab.active .tab-text {
    color: #1890ff;
    font-weight: 500;
}

.tab-text {
    font-size: 28rpx;
    color: #666;
}

.report-list {
    flex: 1;
    padding: 20rpx;
}

.report-list-content {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
}

.report-item {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.report-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
}

.report-type {
    display: flex;
    align-items: center;
}

.type-label {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
    margin-right: 16rpx;
}

.status-tag {
    padding: 4rpx 12rpx;
    border-radius: 6rpx;
    font-size: 22rpx;
}

.processing {
    background-color: #fff2e8;
    color: #fa8c16;
}

.completed {
    background-color: #f6ffed;
    color: #52c41a;
}

.status-text {
    font-size: 22rpx;
}

.report-time {
    font-size: 24rpx;
    color: #999;
}

.report-content {
    margin-bottom: 16rpx;
}

.user-info {
    display: flex;
    margin-bottom: 10rpx;
}

.user-label {
    font-size: 26rpx;
    color: #999;
    min-width: 160rpx;
}

.user-value {
    font-size: 26rpx;
    color: #333;
    flex: 1;
}

.content-section {
    margin-top: 16rpx;
}

.content-label {
    font-size: 26rpx;
    color: #999;
    display: block;
    margin-bottom: 8rpx;
}

.content-text {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
}

.image-preview {
    margin-top: 16rpx;
}

.report-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: 8rpx;
}

.report-footer {
    border-top: 1rpx solid #f0f0f0;
    padding-top: 16rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.report-date {
    font-size: 24rpx;
    color: #999;
}

.process-btn {
    font-size: 24rpx;
    padding: 8rpx 20rpx;
    background-color: #1890ff;
    color: #fff;
    border-radius: 30rpx;
    margin: 0;
    line-height: 1.5;
}

/* 加载状态样式 */
.loading-state {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 30rpx 0;
}

.loading-spinner {
    width: 40rpx;
    height: 40rpx;
    border: 4rpx solid #f3f3f3;
    border-top: 4rpx solid #1890ff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-right: 10rpx;
}

.loading-spinner.small {
    width: 24rpx;
    height: 24rpx;
    border-width: 2rpx;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.loading-text {
    font-size: 24rpx;
    color: #999;
}

/* 空状态样式 */
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;
}

.empty-icon {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 30rpx;
}

.empty-text {
    font-size: 28rpx;
    color: #999;
}

/* 加载更多 */
.load-more {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx 0;
}

.load-more-text {
    font-size: 24rpx;
    color: #999;
    margin-left: 10rpx;
}

/* 没有更多数据 */
.no-more {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx 0;
}

.no-more-text {
    font-size: 24rpx;
    color: #999;
}
</style>