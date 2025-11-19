<template>
    <view class="my-report-container">
        
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
                                <text class="status-text">{{ item.status === 1 ? '已处理' : '处理中' }}</text>
                            </view>
                        </view>
                        <text class="report-time">{{ formatTime(item.createdTime) }}</text>
                    </view>
                    
                    <view class="report-content">
                        <text class="content-text">{{ item.content || '无描述' }}</text>
                        
                        <!-- 举报图片 -->
                        <view class="image-preview" v-if="item.image">
                            <image :src="item.image" class="report-image" mode="aspectFill" @click="previewImage(item.image)"></image>
                        </view>
                    </view>
                    
                    <view class="report-footer">
                        <text class="report-date">举报时间: {{ formatDate(item.createdTime) }}</text>
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

// 获取举报列表
const getReportList = async (page = 1, refresh = false) => {
    if (page === 1) {
        isLoading.value = true;
    }
    
    try {
        const res = await request('/report/list', {
            method: 'GET',
            data: { pageNum: page }
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

// 返回上一页
const goBack = () => {
    uni.navigateBack();
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
.my-report-container {
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
    justify-content: space-between;
    position: sticky;
    top: 0;
    z-index: 10;
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

.title {
    font-size: 32rpx;
    font-weight: 500;
    color: #333;
}

.placeholder {
    width: 60rpx;
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
    background-color: #e6f7ff;
    color: #1890ff;
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
}

.report-date {
    font-size: 24rpx;
    color: #999;
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
    border-top: 4rpx solid #1abc9c;
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