<template>
    <view class="message-list-container">
        <!-- 顶部标题栏 -->
        <view class="header">
            <text class="title">消息管理</text>
        </view>

        <!-- 消息列表 -->
        <view class="message-list">
            <!-- 空数据状态 -->
            <view class="empty-state" v-if="messageList.length === 0 && !isLoading">
                <image src="/static/empty.png" class="empty-icon"></image>
                <text class="empty-text">暂无消息记录</text>
            </view>

            <!-- 加载中状态 -->
            <view class="loading-state" v-if="isLoading">
                <view class="loading-spinner"></view>
                <text class="loading-text">加载中...</text>
            </view>

            <!-- 消息列表内容 -->
            <view 
                class="message-item" 
                v-for="item in messageList" 
                :key="item.id"
            >
                <view class="message-header">
                    <view class="user-info">
                        <image class="avatar" :src="item.targetUserAvatar || '/static/default-avatar.png'"></image>
                        <view class="user-details">
                            <view class="user-main-info">
                                <text class="username">{{ item.targetUserName || '用户' + item.targetUserId }}</text>
                                <view class="user-level" :class="'level-' + (item.targetUserLevel || 1)">
                                    <text class="level-text">Lv.{{ item.targetUserLevel || 1 }}</text>
                                </view>
                            </view>
                            <text class="user-id">ID: {{ item.targetUserId }}</text>
                            <text class="user-signature" v-if="item.targetUserSignature">{{ item.targetUserSignature }}</text>
                        </view>
                    </view>
                    <text class="time">{{ formatTime(item.lastTime) }}</text>
                </view>

                <view class="message-content">
                    <view class="conversation-info">
                        <text class="conversation-label">会话ID: {{ item.id }}</text>
                        <text class="conversation-type">{{ item.type === 1 ? '商品咨询' : '普通会话' }}</text>
                    </view>
                    <text class="last-message">{{ item.lastMessage || '暂无消息' }}</text>
                    <view class="message-stats">
                        <text class="message-count">消息数: {{ item.initiateMessage + item.targetMessage }}</text>
                        <text class="separator">|</text>
                        <text class="unread-count">未读: {{ item.initiateMessage + item.targetMessage }}</text>
                    </view>
                </view>

                <!-- 发起用户信息 -->
                <view class="initiator-info" v-if="item.initiateUserId && item.initiateUserId !== item.targetUserId">
                    <text class="initiator-label">发起用户:</text>
                    <view class="initiator-content">
                        <image class="initiator-avatar" :src="item.initiateUserAvatar || '/static/default-avatar.png'"></image>
                        <view class="initiator-details">
                            <view class="initiator-name-wrapper">
                                <text class="initiator-name">{{ item.initiateUserName || '用户' + item.initiateUserId }}</text>
                                <view class="user-level" :class="'level-' + (item.initiateUserLevel || 1)">
                                    <text class="level-text">Lv.{{ item.initiateUserLevel || 1 }}</text>
                                </view>
                            </view>
                            <text class="initiator-id">ID: {{ item.initiateUserId }}</text>
                        </view>
                    </view>
                </view>

                <view class="message-footer">
                    <button class="action-btn delete" @click.stop="confirmDelete(item)">删除会话</button>
                    <button class="action-btn view" @click.stop="viewDetail(item)">查看详情</button>
                </view>
            </view>
        </view>

        <!-- 分页控制器 -->
        <view class="pagination" v-if="messageList.length > 0">
            <view 
                class="page-btn prev" 
                :class="{ disabled: currentPage <= 1 }"
                @click="changePage(-1)"
            >
                上一页
            </view>
            <text class="page-info">{{ currentPage }}/{{ totalPages }}</text>
            <view 
                class="page-btn next" 
                :class="{ disabled: currentPage >= totalPages }"
                @click="changePage(1)"
            >
                下一页
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '@/utils/request';

// 状态变量
const messageList = ref([]);
const isLoading = ref(false);
const currentPage = ref(1);
const totalPages = ref(1);
const pageSize = 10; // 每页显示数量

// 获取消息列表
const getMessageList = async () => {
    isLoading.value = true;
    try {
        const res = await request('/admin/message/list', {
            method: 'GET',
            data: {
                pageNum: currentPage.value
            }
        });

        if (res.code === 200) {
            messageList.value = res.data || [];
            totalPages.value = Math.ceil(res.data.length / pageSize);
            
            // 获取每条消息相关的用户信息
            await Promise.all(messageList.value.map(async (message) => {
                await getUserInfo(message);
            }));
        } else {
            uni.showToast({
                title: res.msg || '获取消息列表失败',
                icon: 'none'
            });
        }
    } catch (error) {
        console.error('获取消息列表失败:', error);
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        });
    } finally {
        isLoading.value = false;
    }
};

// 获取用户信息
const getUserInfo = async (message) => {
    try {
        // 获取目标用户信息
        const targetUserRes = await request('/user/getUserInfoById', {
            method: 'GET',
            data: { id: message.targetUserId }
        });
        
        if (targetUserRes.code === 200 && targetUserRes.data) {
            // 更新消息中的用户信息
            message.targetUserName = targetUserRes.data.username;
            message.targetUserAvatar = targetUserRes.data.avatar;
            message.targetUserLevel = targetUserRes.data.level;
            message.targetUserSignature = targetUserRes.data.signature;
        }
        
        // 如果发起用户和目标用户不同，也获取发起用户信息
        if (message.initiateUserId && message.initiateUserId !== message.targetUserId) {
            const initiateUserRes = await request('/user/getUserInfoById', {
                method: 'GET',
                data: { id: message.initiateUserId }
            });
            
            if (initiateUserRes.code === 200 && initiateUserRes.data) {
                message.initiateUserName = initiateUserRes.data.username;
                message.initiateUserAvatar = initiateUserRes.data.avatar;
                message.initiateUserLevel = initiateUserRes.data.level;
                message.initiateUserSignature = initiateUserRes.data.signature;
            }
        }
    } catch (error) {
        console.error('获取用户信息失败:', error);
    }
};

// 切换页码
const changePage = (delta) => {
    const newPage = currentPage.value + delta;
    if (newPage >= 1 && newPage <= totalPages.value) {
        currentPage.value = newPage;
        getMessageList();
    }
};

// 确认删除
const confirmDelete = (item) => {
    uni.showModal({
        title: '确认删除',
        content: '确定要删除这个会话吗？此操作不可恢复。',
        success: async (res) => {
            if (res.confirm) {
                await deleteMessage(item.id);
            }
        }
    });
};

// 删除消息
const deleteMessage = async (id) => {
    try {
        const res = await request('/admin/message/delete', {
            method: 'POST',
            data: { id }
        });

        if (res.code === 200) {
            uni.showToast({
                title: '删除成功',
                icon: 'success'
            });
            // 重新加载当前页
            getMessageList();
        } else {
            uni.showToast({
                title: res.msg || '删除失败',
                icon: 'none'
            });
        }
    } catch (error) {
        console.error('删除消息失败:', error);
        uni.showToast({
            title: '操作失败，请重试',
            icon: 'none'
        });
    }
};

// 查看详情
const viewDetail = (item) => {
    uni.navigateTo({
        url: `/pages/admin/messageDetail?id=${item.id}&targetUserId=${item.targetUserId}&targetId=${item.targetId}`
    });
};

// 格式化时间
const formatTime = (timeStr) => {
    if (!timeStr) return '';
    
    const now = new Date();
    const date = new Date(timeStr);
    
    // 如果是今天的消息，显示具体时间
    if (now.toDateString() === date.toDateString()) {
        return date.getHours().toString().padStart(2, '0') + ':' + 
               date.getMinutes().toString().padStart(2, '0');
    }
    
    // 如果是昨天的消息
    const yesterday = new Date(now);
    yesterday.setDate(now.getDate() - 1);
    if (yesterday.toDateString() === date.toDateString()) {
        return '昨天 ' + date.getHours().toString().padStart(2, '0') + ':' + 
               date.getMinutes().toString().padStart(2, '0');
    }
    
    // 其他情况显示完整日期和时间
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 页面加载时获取消息列表
onMounted(() => {
    getMessageList();
});
</script>

<style lang="scss" scoped>
.message-list-container {
    min-height: 100vh;
    background-color: #f5f7fa;
    padding-bottom: 30rpx;
}

.header {
    padding: 30rpx;
    background-color: #fff;
    border-bottom: 1rpx solid #eee;
    margin-bottom: 20rpx;
}

.title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
}

.message-item {
    background-color: #fff;
    margin: 20rpx;
    border-radius: 12rpx;
    padding: 20rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.message-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
}

.user-info {
    display: flex;
    align-items: center;
}

.avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 40rpx;
    margin-right: 20rpx;
}

.user-details {
    display: flex;
    flex-direction: column;
}

.user-main-info {
    display: flex;
    align-items: center;
    gap: 10rpx;
}

.username {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
    margin-bottom: 4rpx;
}

.user-id {
    font-size: 24rpx;
    color: #999;
}

.user-level {
    padding: 2rpx 10rpx;
    border-radius: 20rpx;
    background-color: #f0f7ff;
    
    &.level-1 { background-color: #e6f7ff; }
    &.level-2 { background-color: #f6ffed; }
    &.level-3 { background-color: #fff7e6; }
    &.level-4 { background-color: #fff1f0; }
    &.level-5 { background-color: #f9f0ff; }
}

.level-text {
    font-size: 20rpx;
    color: #666;
}

.user-signature {
    font-size: 22rpx;
    color: #999;
    margin-top: 4rpx;
}

.time {
    font-size: 24rpx;
    color: #999;
}

.message-content {
    padding: 10rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
}

.conversation-info {
    display: flex;
    align-items: center;
    gap: 20rpx;
    margin-bottom: 10rpx;
}

.conversation-label {
    font-size: 22rpx;
    color: #999;
}

.conversation-type {
    font-size: 22rpx;
    color: #1abc9c;
    background-color: #e6fffb;
    padding: 2rpx 10rpx;
    border-radius: 10rpx;
}

.last-message {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 10rpx;
    display: block;
}

.message-stats {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    color: #999;
}

.separator {
    margin: 0 20rpx;
    color: #eee;
}

.message-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 20rpx;
    gap: 20rpx;
}

.action-btn {
    font-size: 24rpx;
    padding: 10rpx 30rpx;
    border-radius: 30rpx;
    border: none;
    
    &.delete {
        background-color: #ff4d4f;
        color: #fff;
    }
    
    &.view {
        background-color: #1abc9c;
        color: #fff;
    }
}

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

.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 30rpx;
    background-color: #fff;
    position: sticky;
    bottom: 0;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.page-btn {
    padding: 10rpx 30rpx;
    font-size: 26rpx;
    color: #fff;
    background-color: #1abc9c;
    border-radius: 30rpx;
    
    &.disabled {
        background-color: #ccc;
        pointer-events: none;
    }
}

.page-info {
    margin: 0 30rpx;
    font-size: 26rpx;
    color: #666;
}

.initiator-info {
    margin-top: 20rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid #f0f0f0;
}

.initiator-label {
    font-size: 24rpx;
    color: #999;
    margin-bottom: 10rpx;
    display: block;
}

.initiator-content {
    display: flex;
    align-items: center;
}

.initiator-avatar {
    width: 60rpx;
    height: 60rpx;
    border-radius: 30rpx;
    margin-right: 16rpx;
}

.initiator-details {
    flex: 1;
}

.initiator-name-wrapper {
    display: flex;
    align-items: center;
    gap: 10rpx;
}

.initiator-name {
    font-size: 26rpx;
    color: #333;
}

.initiator-id {
    font-size: 22rpx;
    color: #999;
}
</style>