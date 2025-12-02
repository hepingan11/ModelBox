<template>
    <view class="chat-container">
        <!-- 顶部导航栏 -->
        <view class="nav-bar">
            <text class="nav-title">{{ chatTarget.username || '聊天' }}</text>
            <view class="nav-right" @click="goToUserSpace">
                <text class="user-icon">👤</text>
            </view>
        </view>
        
        <!-- 聊天内容区域 -->
        <scroll-view 
            class="chat-content" 
            scroll-y 
            :scroll-top="scrollTop" 
            :scroll-into-view="scrollToMessageId"
            @scrolltoupper="loadMoreMessages"
            upper-threshold="50"
            :scroll-with-animation="true"
            ref="chatScrollView"
            id="chatScrollView"
        >
            <!-- 加载更多提示 -->
            <view class="loading-more" v-if="isLoadingMore">
                <view class="loading-animation"></view>
                <text class="loading-text">加载更多消息...</text>
            </view>
            
            <!-- 消息列表 -->
            <view class="messages-container">
                <view 
                    v-for="(message, index) in messages" 
                    :key="message.id" 
                    :id="'msg-' + message.id"
                    class="message-item"
                    :class="{ 'self-message': message.isUser }"
                >
                    <!-- 时间分割线 -->
                    <view class="time-divider" v-if="shouldShowTimeDivider(message, index)">
                        <text class="time-text">{{ formatMessageTime(message.createdTime) }}</text>
                    </view>
                    
                    <!-- 消息气泡 -->
                    <view class="message-wrapper">
                        <!-- 头像 -->
                        <image 
                            :src="message.isUser ? (userInfo.avatar || defaultAvatar) : (chatTarget.avatar || defaultAvatar)" 
                            class="avatar"
                            mode="aspectFill"
                        ></image>
                        
                        <!-- 消息内容 -->
                        <view 
                            class="message-bubble" 
                            :class="{ 'self-bubble': message.isUser }"
                            @longpress.stop="showMessageActions(message)"
                        >
                            <!-- 文本消息 -->
                            <text v-if="message.type === 'text' || !message.type" class="message-text">{{ message.content }}</text>
                            
                            <!-- 图片消息 -->
                            <view v-else-if="message.type === 'img'" class="image-message" @click="previewChatImage(message.content)">
                                <image :src="message.content" class="chat-image" mode="widthFix" @load="onImageLoad" @error="onImageError(message.content)"></image>
                            </view>
                        </view>
                    </view>
                </view>
            </view>
            
            <!-- 无消息提示 -->
            <view class="empty-messages" v-if="messages.length === 0 && !isLoading">
                <text class="empty-text">暂无消息，发送一条消息开始聊天吧</text>
            </view>
            
            <!-- 加载中状态 -->
            <view class="loading-wrapper" v-if="isLoading">
                <view class="loading-animation"></view>
                <text class="loading-text">加载中...</text>
            </view>
        </scroll-view>
        
        <!-- 底部输入区域 - 固定在底部 -->
        <view class="input-area">
            <view class="action-buttons">
                <view class="image-btn" @click="chooseImage">
                    <text class="action-icon">🖼️</text>
                </view>
            </view>
            <view class="input-wrapper">
                <textarea 
                    class="message-input" 
                    v-model="messageText" 
                    placeholder="输入消息..." 
                    :adjust-position="false"
                    :cursor-spacing="10"
                    :show-confirm-bar="false"
                    :auto-height="true"
                    :maxlength="-1"
                    @focus="onInputFocus"
                    @blur="onInputBlur"
                    @confirm="sendTextMessage"
                    @input="onInputChange"
                ></textarea>
            </view>
            <view class="send-btn" :class="{ 'active-send': messageText.trim().length > 0 }" @click="sendTextMessage">
                <text class="send-text">发送</text>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import request from '@/utils/request.js'
import { apiBaseUrl } from '@/store/index.js'

// 聊天参数
const conversationId = ref('')
const chatTarget = reactive({
    userId: '',
    username: '',
    avatar: ''
})

// 当前用户信息
const userInfo = reactive({
    userId: '',
    avatar: ''
})
const currentUserId = computed(() => userInfo.userId)

// 默认头像
const defaultAvatar = '/static/user.png'

// 消息相关
const messages = ref([])
const messageText = ref('')
const isLoading = ref(true)
const isLoadingMore = ref(false)
const currentPage = ref(1)
const hasMoreMessages = ref(true)
const scrollTop = ref(0)
const scrollToMessageId = ref('')
const scrollViewHeight = ref(0)
const chatScrollView = ref(null)
// 上传图片相关
const isUploadingImage = ref(false)

// 定时器
let messageRefreshTimer = null

// 消息操作相关
const currentMessage = ref(null);

// 返回上一页
const goBack = () => {
    uni.navigateBack()
}

// 跳转到对方的个人中心
const goToUserSpace = () => {
    if (!chatTarget.userId) {
        uni.showToast({ title: '用户信息不存在', icon: 'none' })
        return
    }
    
    uni.navigateTo({
        url: `/pages/user/personal-center?userId=${chatTarget.userId}`
    })
}

// 获取当前用户信息
const getCurrentUserInfo = async () => {
    try {
        const res = await request('/user/getUserInfo', {
            method: 'GET'
        });
        
        if (res.code === 200 && res.data) {
            userInfo.userId = res.data.id;
            userInfo.avatar = res.data.avatar || defaultAvatar;
        } else {
            console.error('获取当前用户信息失败:', res.msg);
        }
    } catch (error) {
        console.error('获取当前用户信息出错:', error);
    }
};

// 获取路由参数
const getRouteParams = () => {
    try {
        // 获取路由参数
        const pages = getCurrentPages();
        const currentPage = pages[pages.length - 1];
        
        // 兼容App和小程序的参数获取方式
        let userId = '';
        
        // 小程序环境
        if (currentPage.options) {
            userId = currentPage.options.userId;
        } 
        // App环境
        else if (currentPage.$page && currentPage.$page.options) {
            userId = currentPage.$page.options.userId;
        }
        // 尝试从$route中获取
        else if (currentPage.$route && currentPage.$route.query) {
            userId = currentPage.$route.query.userId;
        }
        
        chatTarget.userId = userId;
        console.log('获取到目标用户ID:', userId);
        
        // 获取当前用户信息
        getCurrentUserInfo();
        
        // 获取对方用户信息并创建对话
        if (chatTarget.userId) {
            getUserInfo(chatTarget.userId);
        } else {
            uni.showToast({ title: '缺少用户ID参数', icon: 'none' });
        }
    } catch (error) {
        console.error('获取路由参数失败:', error);
    }
};

// 获取对方用户信息
const getUserInfo = async (id) => {
    try {
        const res = await request('/user/getUserInfoById', {
            method: 'GET',
            data: { userId: id }
        })
        
        if (res.code === 200 && res.data) {
            chatTarget.username = res.data.username || res.data.nickname || '用户'
            chatTarget.avatar = res.data.avatar || defaultAvatar
            
            // 如果没有conversationId，则创建对话
            if (!conversationId.value) {
                await initiateConversation(id)
            }
        } else {
            console.error('获取用户信息失败:', res.msg)
        }
    } catch (error) {
        console.error('获取用户信息出错:', error)
    }
}

// 创建对话
const initiateConversation = async (targetUserId) => {
    try {
        
        const res = await request(`/chat/initiate?userId=${targetUserId}`, {
            method: 'GET'
        })
        
        if (res.code === 200 && res.data) {
            conversationId.value = res.data.id
            
            // 创建对话后加载消息
            loadMessages()
        } else {
            console.error('创建对话失败:', res.msg)
            uni.showToast({ title: res.msg || '创建对话失败', icon: 'none' })
        }
    } catch (error) {
        console.error('创建对话出错:', error)
        uni.showToast({ title: '创建对话失败', icon: 'none' })
    }
}

// 加载消息
// silent: 静默刷新，不显示加载提示
const loadMessages = async (page = 1, silent = false) => {
    if (!silent) {
        if (page === 1) {
            isLoading.value = true
        } else {
            isLoadingMore.value = true
        }
    }
    
    try {
        console.log('加载消息，对话ID:', conversationId.value)
        const res = await request('/chat/message/list', {
            method: 'GET',
            data: {
                id: conversationId.value,
                pageNum: page,
            }
        })
        
        console.log('消息列表响应:', res)
        
        if (res.code === 200) {
            let messageList = res.data || []
            
            // 处理消息列表，确保每条消息都有正确的类型和归属
            messageList = messageList.map(msg => {
                // 根据type字段区分消息类型
                if (msg.type === 'img') {
                    // 图片消息，content是图片URL
                    msg.type = 'img';
                } else if (msg.type === 'text' || !msg.type) {
                    // 文本消息或未指定类型，默认为文本
                    msg.type = 'text';
                }
                
                // 确保isUser字段正确，这是判断消息归属的关键
                // isUser为true表示是自己发送的消息
                if (msg.isUser === undefined) {
                    msg.isUser = (msg.userId == userInfo.userId);
                }
                
                return msg;
            })
                        
            if (page === 1) {
                messages.value = messageList
                // 滚动到最新消息
                scrollToBottom()
            } else {
                // 加载更多时，将新消息添加到前面
                messages.value = [...messageList, ...messages.value]
            }
            
            // 更新分页信息
            hasMoreMessages.value = res.data.hasNextPage || false
            currentPage.value = page
        } else {
            uni.showToast({
                title: res.msg || '获取消息失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('加载消息失败:', error)
        uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
        })
    } finally {
        isLoading.value = false
        isLoadingMore.value = false
    }
}

// 加载更多消息
const loadMoreMessages = () => {
    if (hasMoreMessages.value && !isLoadingMore.value) {
        loadMessages(currentPage.value + 1)
    }
}

// 发送文本消息
const sendTextMessage = async () => {
    const content = messageText.value.trim()
    if (!content) return
    
    // 清空输入框
    messageText.value = ''
    
    // 发送文本消息
    await sendMessage(content, 'text')
}

// 选择图片
const chooseImage = () => {
    uni.chooseImage({
        count: 1, // 最多可以选择的图片张数
        sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
        sourceType: ['album', 'camera'], // 从相册选择或使用相机拍摄
        success: async (res) => {
            const tempFilePath = res.tempFilePaths[0]
            
            // 显示上传中提示
            isUploadingImage.value = true
            uni.showLoading({
                title: '正在上传图片...',
                mask: true
            })
            
            try {
                // 上传图片
                await uploadAndSendImage(tempFilePath)
            } catch (error) {
                console.error('图片上传失败:', error)
                uni.showToast({
                    title: typeof error === 'string' ? error : '图片上传失败，请重试',
                    icon: 'none',
                    duration: 2000
                })
            } finally {
                uni.hideLoading()
                isUploadingImage.value = false
            }
        },
        fail: (err) => {
            console.error('选择图片失败:', err)
        }
    })
}

// 上传并发送图片
const uploadAndSendImage = async (filePath) => {
    try {
        // 上传图片到服务器，参考publish.vue的实现
        const imageUrl = await uploadImage(filePath);
        
        // 发送图片消息
        if (imageUrl) {
            await sendMessage(imageUrl, 'img');
        }
    } catch (error) {
        console.error('上传图片失败:', error);
        throw error;
    }
};

// 获取基础URL
const getBaseUrl = () => {
    return apiBaseUrl;
};

// 上传单张图片，参考publish.vue的实现
const uploadImage = (filePath) => {
    console.log('开始上传图片:', filePath);
    return new Promise((resolve, reject) => {
        uni.uploadFile({
            url: '/system/uploadImg', // 使用请求工具中的baseUrl
            filePath: filePath,
            name: 'file',
            header: {
                'sa-token': uni.getStorageSync('sa-token') || '' // 添加token认证
            },
            success: (uploadRes) => {
                console.log('上传响应:', uploadRes);
                try {
                    // 尝试解析响应数据
                    const data = JSON.parse(uploadRes.data);
                    console.log('解析后的响应数据:', data);
                    if (data.code === 200) {
                        console.log('上传成功，图片URL:', data.data);
                        // 确保返回的是完整URL
                        let imageUrl = data.data;
                        if (!imageUrl.startsWith('http')) {
                            const baseUrl = getBaseUrl();
                            imageUrl = imageUrl.startsWith('/') 
                                ? baseUrl + imageUrl 
                                : baseUrl + '/' + imageUrl;
                            console.log('补全后的图片URL:', imageUrl);
                        }
                        resolve(imageUrl); // 返回图片URL
                    } else {
                        console.error('上传失败，错误信息:', data.msg);
                        reject(data.msg || '上传失败');
                    }
                } catch (e) {
                    console.error('解析响应数据失败:', e, '原始数据:', uploadRes.data);
                    reject('上传响应解析失败');
                }
            },
            fail: (err) => {
                console.error('上传请求失败:', err);
                reject(err);
            }
        });
    });
};

// 发送消息通用方法
const sendMessage = async (content, type) => {
    // 构建临时消息对象
    const tempMessage = {
        id: 'temp-' + Date.now(),
        userId: userInfo.userId,
        chatListId: conversationId.value,
        content: content,
        type: type,
        createdTime: new Date().toISOString(),
        isUser: true // 确保发送的消息显示为自己的
    }
    
    // 先添加到消息列表
    messages.value.push(tempMessage)
    
    // 滚动到最新消息
    scrollToBottom()
    
    try {
        console.log('发送消息:', {
            chatListId: conversationId.value,
            content: content,
            type: type
        });
        
        const res = await request('/chat/message', {
            method: 'POST',
            data: {
                chatListId: conversationId.value,
                content: content,
                type: type
            }
        });
        
        console.log('发送消息响应:', res);
        
        if (res.code === 200) {
            // 替换临时消息为真实消息，确保isUser为true
            const index = messages.value.findIndex(msg => msg.id === tempMessage.id);
            if (index !== -1) {
                const serverMessage = res.data;
                serverMessage.isUser = true; // 确保服务器返回的消息也标记为自己的
                messages.value[index] = serverMessage;
            }
        } else {
            uni.showToast({
                title: res.msg || '发送失败',
                icon: 'none'
            });
            
            // 标记消息发送失败
            const index = messages.value.findIndex(msg => msg.id === tempMessage.id);
            if (index !== -1) {
                messages.value[index].failed = true;
            }
        }
    } catch (error) {
        console.error('发送消息失败:', error);
        
        // 检查是否真的失败了，有些情况下可能是网络问题但消息实际已发送成功
        if (error && error.errMsg && error.errMsg.includes('request:fail')) {
            uni.showToast({
                title: '网络错误，请稍后重试',
                icon: 'none'
            });
            
            // 标记消息发送失败
            const index = messages.value.findIndex(msg => msg.id === tempMessage.id);
            if (index !== -1) {
                messages.value[index].failed = true;
            }
        } else {
            // 可能是其他错误，但消息可能已发送成功
            console.log('发送可能成功，但出现了错误');
        }
    }
}

// 滚动到底部
const scrollToBottom = () => {
    nextTick(() => {
        // 使用延时确保DOM已更新
        setTimeout(() => {
            try {
                // 方法1：使用大数值
                scrollTop.value = 100000;
                
                // 方法2：使用最后一条消息的ID
                if (messages.value.length > 0) {
                    const lastMessage = messages.value[messages.value.length - 1];
                    scrollToMessageId.value = 'msg-' + lastMessage.id;
                    console.log('滚动到消息ID:', scrollToMessageId.value);
                }
                
                // 方法3：使用uni.pageScrollTo
                uni.createSelectorQuery()
                    .select('.messages-container')
                    .boundingClientRect(data => {
                        if (data) {
                            uni.pageScrollTo({
                                scrollTop: data.height,
                                duration: 300
                            });
                        }
                    })
                    .exec();
            } catch (error) {
                console.error('滚动到底部出错:', error);
            }
        }, 300); // 增加延时，确保DOM更新完成
    });
};

// 输入框聚焦
const onInputFocus = () => {
    // 滚动到底部
    scrollToBottom()
}

// 输入框失焦
const onInputBlur = () => {
    // 可以添加一些处理逻辑
}

// 输入框内容变化
const onInputChange = (e) => {
    // 这里可以添加处理逻辑，比如输入状态的更新
    // 但不要在这里执行频繁的DOM操作或复杂计算
    // console.log('输入内容变化');
}

// 安全地解析日期，处理不同格式
const parseDate = (dateStr) => {
    if (!dateStr) return new Date();
    
    try {
        // 如果已经是Date对象，直接返回
        if (dateStr instanceof Date) {
            return dateStr;
        }
        
        // 如果是数字（时间戳），转换为Date对象
        if (typeof dateStr === 'number') {
            return new Date(dateStr);
        }
        
        // 如果是字符串，尝试转换格式
        if (typeof dateStr === 'string') {
            // 检查是否是 "yyyy-MM-dd HH:mm:ss" 格式
            const pattern = /^(\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2}):(\d{2})$/;
            const match = dateStr.match(pattern);
            
            if (match) {
                // 提取年月日时分秒
                const year = parseInt(match[1]);
                const month = parseInt(match[2]) - 1; // 月份从0开始
                const day = parseInt(match[3]);
                const hour = parseInt(match[4]);
                const minute = parseInt(match[5]);
                const second = parseInt(match[6]);
                
                // 使用Date构造函数创建日期对象
                return new Date(year, month, day, hour, minute, second);
            }
            
            // 尝试将 "yyyy-MM-dd HH:mm:ss" 转换为 "yyyy/MM/dd HH:mm:ss"
            const slashDate = dateStr.replace(/-/g, '/');
            return new Date(slashDate);
        }
    } catch (error) {
        console.error('日期解析错误:', error, dateStr);
    }
    
    // 如果所有尝试都失败，返回当前日期
    return new Date();
};

// 判断是否显示时间分割线
const shouldShowTimeDivider = (message, index) => {
    if (index === 0) return true;
    
    const prevMessage = messages.value[index - 1];
    
    try {
        // 获取当前消息和前一条消息的时间
        const currentTime = parseDate(message.createdTime).getTime();
        const prevTime = parseDate(prevMessage.createdTime).getTime();
        
        // 如果两条消息相隔超过5分钟，显示时间分割线
        return currentTime - prevTime > 5 * 60 * 1000;
    } catch (error) {
        console.error('处理消息时间出错:', error, message.createdTime, prevMessage.createdTime);
        return false;
    }
};

// 格式化消息时间
const formatMessageTime = (timestamp) => {
    if (!timestamp) return '';
    
    try {
        const date = parseDate(timestamp);
        
        // 检查日期是否有效
        if (isNaN(date.getTime())) {
            console.error('无效的日期格式:', timestamp);
            return '未知时间';
        }
        
        const now = new Date();
        const isToday = date.toDateString() === now.toDateString();
        const isYesterday = new Date(now - 86400000).toDateString() === date.toDateString();
        
        const hours = date.getHours();
        const minutes = date.getMinutes();
        const formattedTime = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
        
        if (isToday) {
            return `今天 ${formattedTime}`;
        } else if (isYesterday) {
            return `昨天 ${formattedTime}`;
        } else {
            return `${date.getMonth() + 1}月${date.getDate()}日 ${formattedTime}`;
        }
    } catch (error) {
        console.error('格式化消息时间出错:', error);
        return '未知时间';
    }
};

// 预览聊天中的图片
const previewChatImage = (imageUrl) => {
    if (!imageUrl) return
    
    console.log('预览图片:', imageUrl);
    
    // 尝试预览图片
    try {
        uni.previewImage({
            current: imageUrl,
            urls: [imageUrl],
            success: () => {
                console.log('图片预览成功');
            },
            fail: (err) => {
                console.error('图片预览失败:', err);
                uni.showToast({
                    title: '图片预览失败',
                    icon: 'none'
                });
            }
        });
    } catch (error) {
        console.error('图片预览出错:', error);
        uni.showToast({
            title: '图片预览出错',
            icon: 'none'
        });
    }
}

// 图片加载失败
const onImageError = (url) => {
    console.error('图片加载失败:', url);
    uni.showToast({
        title: '图片加载失败',
        icon: 'none'
    });
};

// 显示消息操作菜单
const showMessageActions = (message) => {
    currentMessage.value = message;
    console.log('显示消息操作菜单:', message);
    
    // 使用uni.showActionSheet替代popup
    uni.showActionSheet({
        itemList: ['复制', '举报'],
        success: function (res) {
            if (res.tapIndex === 0) {
                copyMessage();
            } else if (res.tapIndex === 1) {
                reportMessage();
            }
        }
    });
};

// 复制消息
const copyMessage = () => {
    if (!currentMessage.value) return;
    
    const content = currentMessage.value.content;
    
    // 只能复制文本消息
    if (currentMessage.value.type === 'text' || !currentMessage.value.type) {
        uni.setClipboardData({
            data: content,
            success: () => {
                uni.showToast({
                    title: '复制成功',
                    icon: 'success'
                });
            }
        });
    } else {
        uni.showToast({
            title: '只能复制文本消息',
            icon: 'none'
        });
    }
};

// 举报消息
const reportMessage = () => {
    if (!currentMessage.value) return;
    
    // 跳转到举报页面，传递聊天ID和对方用户ID
    uni.navigateTo({
        url: `/pages/index/report?id=${currentMessage.value.id}&userId=${chatTarget.userId}&type=chat`
    });
};

// 启动定时刷新消息
const startMessageRefresh = () => {
    // 清除旧的定时器
    if (messageRefreshTimer) {
        clearInterval(messageRefreshTimer)
    }
    
    // 每15秒自动刷新消息
    messageRefreshTimer = setInterval(() => {
        if (conversationId.value) {
            console.log('定时刷新消息...')
            loadMessages(1, true) // 静默刷新，不显示加载提示
        }
    }, 15000) // 15秒
}

// 停止定时刷新
const stopMessageRefresh = () => {
    if (messageRefreshTimer) {
        clearInterval(messageRefreshTimer)
        messageRefreshTimer = null
        console.log('已停止消息定时刷新')
    }
}

// 页面加载时获取参数和消息
onMounted(() => {
    getRouteParams()
    
    // 获取滚动视图高度
    try {
        // 使用新的接口获取窗口信息
        const windowInfo = uni.getWindowInfo();
        console.log('窗口信息:', windowInfo);
        // 计算聊天区域高度（屏幕高度减去导航栏和输入区域高度）
        scrollViewHeight.value = windowInfo.windowHeight - 180; // 单位为px
    } catch (error) {
        console.error('获取窗口信息失败:', error);
        // 降级方案：使用默认高度
        scrollViewHeight.value = 600;
    }
    
    // 启动定时刷新
    startMessageRefresh()
})

// 页面卸载时清除定时器
onUnmounted(() => {
    stopMessageRefresh()
})



</script>

<style>
.chat-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #f5f7fa;
    position: relative;
}

/* 导航栏样式 */
.nav-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 30rpx;
    background-color: #fff;
    position: sticky;
    top: 0;
    z-index: 100;
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

.nav-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
    flex: 1;
    text-align: center;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.nav-right {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #00A872 0%, #00C896 100%);
    border-radius: 50%;
    transition: all 0.3s ease;
}

.nav-right:active {
    transform: scale(0.95);
    opacity: 0.8;
}

.user-icon {
    font-size: 32rpx;
    line-height: 1;
}

/* 聊天内容区域 */
.chat-content {
    flex: 1;
    padding: 20rpx;
    position: relative;
    height: calc(100vh - 180rpx); /* 减去顶部导航栏和底部输入栏的高度 */
    padding-bottom: 180rpx; /* 添加更多底部内边距，防止最后的消息被输入栏遮挡 */
}

/* 消息容器 */
.messages-container {
    padding-bottom: 20rpx;
}

/* 消息项 */
.message-item {
    margin-bottom: 20rpx;
}

/* 消息包装器 */
.message-wrapper {
    display: flex;
    align-items: flex-start;
    max-width: 100%;
}

/* 自己发送的消息 */
.self-message .message-wrapper {
    flex-direction: row;
}

/* 头像 */
.avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 40rpx;
    margin: 0 20rpx;
    flex-shrink: 0;
}

.avatar-placeholder {
    width: 80rpx;
    margin: 0 20rpx;
    flex-shrink: 0;
}

/* 消息气泡 */
.message-bubble {
    background-color: #fff;
    padding: 20rpx;
    border-radius: 10rpx;
    max-width: 70%;
    word-break: break-all;
    box-shadow: 0 2rpx 5rpx rgba(0, 0, 0, 0.05);
}

.self-bubble {
    background: linear-gradient(135deg, #1abc9c, #b54aff);
}

.message-text {
    font-size: 30rpx;
    line-height: 1.4;
    color: #333;
}

.self-bubble .message-text {
    color: #fff;
}

/* 图片消息样式 */
.image-message {
    padding: 0;
    overflow: hidden;
    border-radius: 10rpx;
    background-color: rgba(0, 0, 0, 0.05);
    min-width: 200rpx;
    min-height: 200rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.chat-image {
    width: 100%;
    max-width: 400rpx;
    border-radius: 10rpx;
    display: block;
}

/* 时间分割线 */
.time-divider {
    display: flex;
    justify-content: center;
    margin: 30rpx 0;
}

.time-text {
    font-size: 24rpx;
    color: #999;
    background-color: rgba(0, 0, 0, 0.05);
    padding: 6rpx 20rpx;
    border-radius: 20rpx;
}

/* 底部输入区域 */
.input-area {
    padding: 20rpx;
    background-color: #fff;
    display: flex;
    align-items: center;
    border-top: 1rpx solid #eee;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 100;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.action-buttons {
    display: flex;
    align-items: center;
    margin-right: 20rpx;
}

.image-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 20rpx;
}

.action-icon {
    font-size: 40rpx;
    color: #333;
}

.input-wrapper {
    flex: 1;
    background-color: #f5f7fa;
    border-radius: 10rpx;
    padding: 15rpx;
    margin-right: 20rpx;
}

.message-input {
    width: 100%;
    font-size: 28rpx;
    min-height: 40rpx;
    max-height: 200rpx;
}

.send-btn {
    width: 120rpx;
    height: 70rpx;
    background-color: #e0e0e0;
    border-radius: 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.active-send {
    background: linear-gradient(135deg, #1abc9c, #b54aff);
}

.send-text {
    font-size: 28rpx;
    color: #666;
}

.active-send .send-text {
    color: #fff;
}

/* 加载中样式 */
.loading-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 50rpx 0;
}

.loading-animation {
    width: 40rpx;
    height: 40rpx;
    border: 4rpx solid #f3f3f3;
    border-top: 4rpx solid #1abc9c;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 20rpx;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.loading-text {
    font-size: 24rpx;
    color: #999;
}

/* 加载更多 */
.loading-more {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx 0;
}

/* 空消息提示 */
.empty-messages {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;
}

.empty-text {
    font-size: 28rpx;
    color: #999;
}
</style>
