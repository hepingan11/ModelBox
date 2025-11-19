<template>
    <view class="message-container">
        <!-- 顶部标题栏 -->
        <view class="header">
            <text class="title">消息列表</text>
        </view>
        
        <!-- 会话列表 -->
        <scroll-view 
            class="conversation-list" 
            scroll-y 
            :refresher-enabled="true"
            :refresher-triggered="isRefreshing"
            @refresherrefresh="onRefresh"
        >
            <!-- 空数据状态 -->
            <view class="empty-state" v-if="conversations.length === 0 && !isLoading">
                <image src="/static/empty.png" class="empty-icon"></image>
                <text class="empty-text">暂无会话</text>
            </view>
            
            <!-- 加载中状态 -->
            <view class="loading-state" v-if="isLoading && !isRefreshing">
                <view class="loading-spinner"></view>
                <text class="loading-text">加载中...</text>
            </view>
            
            <!-- 会话列表项 -->
            <view 
                class="conversation-item" 
                v-for="item in conversations" 
                :key="item.id"
                @click="navigateToChat(item)"
            >
                <image class="avatar" :src="item.targetUserAvatar || '/static/default-avatar.png'" mode="aspectFill"></image>
                <view class="conversation-content">
                    <view class="conversation-header">
                        <text class="username">{{ item.targetUserName || '用户' + item.targetUserId }}</text>
                        <text class="time">{{ formatTime(item.lastTime) }}</text>
                    </view>
                    <view class="conversation-footer">
                        <text class="last-message">{{ item.lastMessage || '暂无消息' }}</text>
                        <view class="unread-badge" v-if="getUnreadCount(item) > 0">
                            <text class="unread-count">{{ getUnreadCount(item) }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </scroll-view>
    </view>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import request from '@/utils/request';

// 状态变量
const conversations = ref([]);
const isLoading = ref(false);
const isRefreshing = ref(false);

// 页面加载时获取会话列表
onMounted(() => {
    getConversationList();
});

// 获取会话列表
const getConversationList = async () => {
    isLoading.value = true;
    try {
        const res = await request('/chat/list', {
            method: 'GET'
        });
        
        if (res.code === 200) {
            conversations.value = res.data || [];
            // 打印会话列表，检查时间格式
            console.log('会话列表:', conversations.value);
            if (conversations.value.length > 0) {
                console.log('第一条会话的时间格式:', conversations.value[0].lastTime);
            }
        } else {
            uni.showToast({
                title: res.msg || '获取会话列表失败',
                icon: 'none'
            });
        }
    } catch (error) {
        console.error('获取会话列表失败:', error);
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
    getConversationList();
};

// 导航到聊天详情页
const navigateToChat = (conversation) => {
    console.log('导航到聊天页面，会话信息:', conversation);
    uni.navigateTo({
        url: `/pages/chat/chat?id=${conversation.id}&userId=${conversation.targetId || conversation.targetUserId}&username=${encodeURIComponent(conversation.targetUserName || '用户' + conversation.targetUserId)}`
    });
};

// 格式化时间
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
        
        // 如果是今天的消息，只显示时间
        if (now.toDateString() === date.toDateString()) {
            return date.getHours().toString().padStart(2, '0') + ':' + 
                   date.getMinutes().toString().padStart(2, '0');
        }
        
        // 如果是昨天的消息
        const yesterday = new Date(now);
        yesterday.setDate(now.getDate() - 1);
        if (yesterday.toDateString() === date.toDateString()) {
            return '昨天';
        }
        
        // 如果是今年的消息
        if (now.getFullYear() === date.getFullYear()) {
            return (date.getMonth() + 1) + '月' + date.getDate() + '日';
        }
        
        // 其他情况显示完整日期
        return date.getFullYear() + '/' + (date.getMonth() + 1) + '/' + date.getDate();
    } catch (error) {
        console.error('格式化时间出错:', error, timeStr);
        return '未知时间';
    }
};

// 获取未读消息数量
const getUnreadCount = async (conversation) => {
    // 根据当前用户ID判断未读消息数量
    // 假设当前用户ID存储在全局状态或本地存储中
    const res = await request('/user/getUserInfo', {
        method: 'GET',
    }).then(res => {
        console.log('获取用户信息:', res);
    })
    const currentUserId = res.data.id;
    
    if (currentUserId == conversation.initiateUserId) {
        // 当前用户是会话发起者，查看目标用户的未读消息数
        return conversation.targetMessage || 0;
    } else {
        // 当前用户是会话目标者，查看发起用户的未读消息数
        return conversation.initiateMessage || 0;
    }
};
</script>

<style lang="scss" scoped>
.message-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
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

.conversation-list {
    flex: 1;
}

.conversation-item {
    display: flex;
    padding: 30rpx;
    background-color: #fff;
    border-bottom: 1rpx solid #eee;
}

.avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    margin-right: 20rpx;
}

.conversation-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.conversation-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10rpx;
}

.username {
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
}

.time {
    font-size: 24rpx;
    color: #999;
}

.conversation-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.last-message {
    font-size: 26rpx;
    color: #666;
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.unread-badge {
    min-width: 36rpx;
    height: 36rpx;
    border-radius: 18rpx;
    background-color: #ff4d4f;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 10rpx;
}

.unread-count {
    font-size: 22rpx;
    color: #fff;
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

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.loading-text {
    font-size: 24rpx;
    color: #999;
}
</style>