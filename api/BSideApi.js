import request from '@/utils/BSideRequest'

/**
 *    分页获取模型数据
 */
export function GetSdModelPage(a, b) {
    return request({
        url: '/admin/sd/page/model?pageNum=' + a + '&prompt=' + b,
        method: 'GET'
    })
}

/**
 *    新增模型
 */
export function PutSdModel(data) {
    return request({
        url: '/admin/sd/put/data',
        method: 'POST',
        data
    })
}

/**
 *    删除模型
 */
export function DeleteSdModel(data) {
    return request({
        url: '/admin/sd/delete/' + data,
        method: 'POST'
    })
}


export function SdConnectivity() {
    return request({
        url: '/drawing/sd/connectivity',
        method: 'GET'
    })
}


export function CheckSdConnectivity() {
    return request({
        url: '/draw/sd/connectivity',
        method: 'GET'
    })
}

/**
 *    设置个性参数GPT
 */
export function PutPersonalityConfig(data) {
    return request({
        url: '/user/personality/put/config',
        method: 'POST',
        data
    })
}

export function UpdateUserAvatar(data) {
    return request({
        url: '/user/upload/avatar',
        method: 'POST',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data
    })
}

/**
 *    设置个性参数GPT
 */
export function GetPersonalityConfig() {
    return request({
        url: '/user/personality/get/config',
        method: 'GET'
    })
}


/**
 *    获取终端数据
 */
export function GetTerminal() {
    return request({
        url: '/admin/server/get/terminal',
        method: 'GET'
    })
}

/**
 *   更新终端数据
 */
export function PutTerminal(data) {
    return request({
        url: '/admin/server/put/terminal',
        method: 'POST',
        data
    })
}

/**
 *    获取分页用户数据
 */
export function GetUserPage(a, b) {
    return request({
        url: '/admin/user/get/page?pageNum=' + a + '&prompt=' + b,
        method: 'GET'
    })
}


export function GetPublicRandomOps() {
    return request({
        url: '/drawing/random/get/ops',
        method: 'GET'
    })
}

export function GetSdModelList() {
    return request({
        url: '/drawing/sd/get/model',
        method: 'GET'
    })
}


/**
 *    获取订单分页数据
 */
export function getOrdersPage(a, b) {
    return request({
        url: '/admin/orders/page?pageNum=' + a + '&prompt=' + b + '&status=',
        method: 'GET'
    })
}

/**
 *   找回密码
 */
export function RetrieveEmailPassword(data) {
    return request({
        url: '/auth/email/password/back',
        method: 'POST',
        data
    })
}

/**
 *   重载服务器配置
 */
export function GetServerConfig() {
    return request({
        url: '/admin/server/get/config',
        method: 'GET'
    })
}


/**
 * Production exchange code
 * @returns {*}
 */
export function AddRedemptionCode(data) {
    return request({
        url: '/admin/exchange/build',
        method: 'POST',
        data
    })
}

/**
 * Production exchange code
 * @returns {*}
 */
export function DeleteRedemptionCode(data) {
    return request({
        url: '/admin/exchange/delete/' + data,
        method: 'POST'
    })
}


/**
 *   分页获取兑换码
 */
export function GetRedemptionCodePage(a, b) {
    return request({
        url: '/admin/exchange/get/page?pageNum=' + a + '&prompt=' + b,
        method: 'GET'
    })
}

/**
 * Production exchange code
 * @returns {*}
 */
export function DeleteProduct(data) {
    return request({
        url: '/admin/product/delete/' + data,
        method: 'POST'
    })
}

/**
 * Production exchange code
 * @returns {*}
 */
export function AddProduct(data) {
    return request({
        url: '/admin/product/put/data',
        method: 'POST',
        data
    })
}

/**
 *   分页获取商品
 */
export function GetProductPage(a, b) {
    return request({
        url: '/admin/product/get/page?pageNum=' + a + '&prompt=' + b,
        method: 'GET'
    })
}


/**
 *   重载服务器配置
 */
export function PutServerConfig(data) {
    return request({
        url: '/admin/server/put/config',
        method: 'POST',
        data
    })
}


/**
 *    修改用户IT币
 */
export function UpdateUserInfo(data) {
    return request({
        url: '/admin/user/put/data',
        method: 'POST',
        data
    })
}

/**
 *    获取平台总人数
 */
export function GetUserCount() {
    return request({
        url: '/admin/user/get/count',
        method: 'GET'
    })
}

/**
 *    退出
 */
export function Logout() {
    return request({
        url: '/auth/wechat/logout',
        method: 'POST'
    })
}

/**
 *    获取验证码
 */
export function getEmailCode(data) {
    return request({
        url: '/auth/email/code/' + data,
        method: 'GET'
    })
}

/**
 *    注册
 */
export function EmailEnroll(data) {
    return request({
        url: '/auth/email/enroll',
        method: 'POST',
        data
    })
}

/**
 *   邮箱登录
 */
export function EmailLogin(data) {
    return request({
        url: '/auth/email/login',
        method: 'POST',
        data
    })
}

/**
 *    获取用户信息
 */
export function GetUserInfo() {
    return request({
        url: '/user/current/info',
        method: 'POST'
    })
}

/**
 *    发送消息
 */
export function SendAMessage(data) {
    return request({
        url: '/gpt/ask',
        method: 'POST',
        data
    })
}


/**
 *    获取收藏列表
 */
export function Favorites(data) {
    return request({
        url: '/user/stat/get/web',
        method: 'GET',
        data
    })
}

/**
 *    获取商品列表
 */
export function GetProducts() {
    return request({
        url: '/pay/product/list',
        method: 'GET'
    })
}

/**
 *    构建支付宝订单
 */
export function alipayPayQrCode(data) {
    return request({
        url: '/pay/alipay/pay/' + data,
        method: 'POST'
    })
}

/**
 *    支付宝支付状态查询
 */
export function alipayIsSucceed(data) {
    return request({
        url: '/pay/alipay/status/' + data,
        method: 'POST'
    })
}

/**
 *    用户订单PAGE
 */
export function UsersOrdersPage(data) {
    return request({
        url: '/pay/orders/page?pageNum=' + data,
        method: 'GET'
    })
}

/**
 *    使用兑换码
 */
export function UseExchangeCode(data) {
    return request({
        url: '/inspirit/exchange/' + data,
        method: 'POST'
    })
}

/**
 *    添加收藏
 */
export function FavoritesAdd(data) {
    return request({
        url: '/user/stat/put/data',
        method: 'POST',
        data
    })
}

/**
 *    删除收藏
 */
export function FavoritesDel(data) {
    return request({
        url: '/user/stat/delete/' + data,
        method: 'POST'
    })
}

/**
 * 获取授权登录二维码
 */
export function GetWechatCode() {
    return request({
        url: '/auth/wechat/get/code',
        method: 'POST'
    })
}

/**
 * 是否登录成功
 */
export function isQrCodeLoginSucceed(data) {
    return request({
        url: '/auth/wechat/code/result?verifyCode=' + data,
        method: 'GET'
    })
}

/**
 * 获取视频列表
 * @returns {*}
 */
export function getVideoList() {
    return request({
        url: '/digital/getVideoList',
        method: 'POST'
    })
}

/**
 * 新发送绘画请求
 */
export function postSdDraw(data) {
    return request({
        url: '/draw/postSdDraw',
        method: 'POST',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data
    })
}

export function setDrawPublic(data) {
    return request({
        url: '/draw/setPublic/' + data,
        method: 'POST',
    })
}

export function getUserDraw(page) {
    return request({
        url: '/draw/list/private?page=' + page,
        method: 'GET'
    })
}

export function DeleteImgByurl(data) {
    return request({
        url: '/draw/deleteDraw/' + data,
        method: 'POST',
    })
}

// 切换绘画公开状态
export function toggleDrawPublic(drawId) {
    return request({
        url: '/draw/toggle?drawId=' + drawId,
        method: 'GET'
    })
}

export function updateUserName(data) {
    return request({
        url: '/user/upload/username?userName=' + data,
        method: 'POST'
    })
}

export function getTopImgUrl() {
    return request({
        url: '/link/getTopImg',
        method: 'GET'
    })
}

export function setTopImgUrl(data) {
    return request({
        url: '/admin/link/setTopImg',
        method: 'POST',
        data
    })
}

export function getLinkList(data) {
    return request({
        url: '/link/getList?linkName=' + data,
        method: 'GET',

    })
}

export function GetUserLinkList() {
    return request({
        url: '/link/userList',
        method: 'GET'
    })
}

export function ApplyLink(data) {
    return request({
        url: '/link/applyLink',
        method: 'POST',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data
    })
}

export function allowUserLink(data) {
    return request({
        url: '/link/allowLink/' + data,
        method: 'POST',
    })
}

export function deleteLinkById(data) {
    return request({
        url: '/link/deleteLink/' + data,
        method: 'POST',
    })
}

export function refuseLinkById(data) {
    return request({
        url: '/link/refuseLink/' + data,
        method: 'POST',
    })
}

export function setLinkHotById(data) {
    return request({
        url: '/link/setLinkHot/' + data,
        method: 'POST',
    })
}

export function deleteLinkHotById(data) {
    return request({
        url: '/link/deleteLinkHot/' + data,
        method: 'POST',
    })
}

export function selectStatLink() {
    return request({
        url: '/link/selectStatLink',
        method: 'GET',
    })
}

export function addLinkStatById(data) {
    return request({
        url: '/link/addStatLink/' + data,
        method: 'POST',
    })
}

export function getPriceData() {
    return request({
        url: '/data/getPrice',
        method: 'GET'
    })
}

export function getNumData() {
    return request({
        url: '/data/numSort',
        method: 'GET'
    })
}

export function cancelLinkStatById(data) {
    return request({
        url: '/link/cancelStatLink/' + data,
        method: 'POST'
    })
}

export function getAllNum() {
    return request({
        url: '/pass/getAllNum',
        method: 'GET'
    })
}

export function passLogin(account, password) {
    return request({
        url: '/pass/login/student?account=' + account + "&password=" + password,
        method: 'POST'
    })
}

export function getShopInfo() {
    return request({
        url: '/data/shopInfo',
        method: 'GET'
    })
}

export function UploadFile(data) {
    return request({
        url: '/photo/upload',
        method: 'POST',
        data
    })
}

export function GetUserImgList() {
    return request({
        url: '/photo/getUserList',
        method: 'GET'
    })
}

export function DeleteImgById(data) {
    return request({
        url: '/photo/delete/' + data,
        method: 'POST'
    })
}

export function TogglePhotoPublic(photoId) {
    return request({
        url: '/photo/setPhotoPublic/' + photoId,
        method: 'POST'
    })
}

export function GetAllImgList() {
    return request({
        url: '/photo/getAllImgList',
        method: 'GET'
    })
}

export function GetWorkList() {
    return request({
        url: '/funny/work/get',
        method: 'GET'
    })
}

export function AddWork(name, url, sort) {
    return request({
        url: '/funny/work/add?name=' + name + '&sort=' + sort + '&url=' + url,
        method: 'POST'
    })
}

export function UpdateWork(data) {
    return request({
        url: '/funny/work/update',
        method: 'POST',
        data
    })
}

export function DeleteWork(id) {
    return request({
        url: '/funny/work/delete/' + id,
        method: 'POST',
    })
}

export function GetDigitalData(no, size) {
    return request({
        url: '/data/getDialogueData/page?no=' + no + '&size=' + size,
        method: 'GET'
    })
}

export function GetContentCloud() {
    return request({
        url: '/data/getContentCloud',
        method: 'GET'
    })
}

export function GetMessageCloud() {
    return request({
        url: '/data/getMessageCloud',
        method: 'GET'
    })
}

export function GetTimeFrequency(type) {
    return request({
        url: '/data/getTimeFrequency/' + type,
        method: 'GET'
    })
}

export function GetModelFrequency() {
    return request({
        url: '/data/getModelFrequency',
        method: 'GET'
    })
}

export function SubmitEmail(data) {
    return request({
        url: '/admin/submitEmail',
        method: 'POST',
        data
    })
}

export function GetCodeList(data, language) {
    return request({
        url: '/code/codeList?name=' + data + "&language=" + language,
        method: 'GET',
    })
}

export function AddCode(data) {
    return request({
        url: '/code/code/add',
        method: 'POST',
        data
    })
}

export function AddLook(data) {
    return request({
        url: '/code/addLook?codeId=' + data,
        method: 'GET',
    })
}

/**
 * 支付宝购买代码项目
 * @param {Long} codeId 代码项目ID
 * @returns {*} 返回支付相关信息，包含支付链接和订单ID
 */
export function buyCodeAlipay(codeId) {
    return request({
        url: '/code/buy/alipay?codeId=' + codeId,
        method: 'POST'
    })
}

/**
 * 查询代码项目支付状态
 * @param {String} orderNo 订单号
 * @returns {*} 返回支付状态信息
 */
export function checkCodePayStatus(orderNo) {
    return request({
        url: '/code/buy/alipay/status?orderNo=' + orderNo,
        method: 'GET'
    })
}

/**
 * 获取用户已购买的代码项目列表
 * @returns {*} 返回已购买代码项目列表
 */
export function getMyCodeList() {
    return request({
        url: '/code/buyList',
        method: 'GET'
    })
}

/**
 * 获取用户发布的代码项目列表
 * @returns {*} 返回已发布代码项目列表
 */
export function getMyPublishList() {
    return request({
        url: '/code/mycode',
        method: 'GET'
    })
}

/**
 * 删除用户发布的代码项目
 * @param {Long} codeId 代码项目ID
 * @returns {*} 返回删除结果
 */
export function deleteMyPublishCode(codeId) {
    return request({
        url: '/code/myPublish/delete?codeId=' + codeId,
        method: 'POST'
    })
}

/**
 * 发送智谱GLM多模态请求（支持图片+文本）
 * @param {Array} data 包含图片和文本的消息数组
 * @returns {*} 返回智谱GLM的响应
 */
export function ZhipuImageChat(data) {
    return request({
        url: '/user/zhipu',
        method: 'POST',
        data
    })
}

// 更新已发布的代码项目
export function updateMyPublishCode(codeData) {
    return request({
        url: "/code/myPublish/update",
        method: "post",
        data: codeData,
    });
}

/**
 * 添加项目图片
 * @param {Object} data 包含codeId和image文件的FormData对象
 * @returns {*} 返回添加结果
 */
export function addCodeImage(data) {
    return request({
        url: "/code/myPublish/update/image",
        method: "post",
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data
    });
}

/**
 * 删除项目图片
 * @param {String} imageUrl 图片URL
 * @returns {*} 返回删除结果
 */
export function deleteCodeImage(imageUrl) {
    return request({
        url: "/code/myPublish/update/image/delete",
        method: "post",
        params: { imageUrl }
    });
}

/**
 * 发送智谱AI绘画请求
 * @param {Object|FormData} data 包含绘画参数的对象或FormData (model, prompt, quality, size, file)
 * @returns {*} 返回智谱AI的绘画结果
 */
export function addZhipuDrawingTask(data) {
    const config = {
        url: "/draw/zhipu/image",
        method: "post",
        data
    };

    // 如果是FormData，设置正确的Content-Type
    if (data instanceof FormData) {
        config.headers = {
            'Content-Type': 'multipart/form-data'
        };
    }

    return request(config);
}

/**
 * 获取所有源码列表（管理员用）
 * @returns {*} 返回所有源码列表
 */
export function getAdminCodeList() {
    return request({
        url: "/admin/code/list",
        method: "GET"
    });
}

/**
 * 获取交易列表（管理员用）
 * @returns {*} 返回交易列表
 */
export function getCodeExchangeList() {
    return request({
        url: "/admin/code/exchange",
        method: "GET"
    });
}

/**
 * 拒绝源码审核
 * @param {Long} id 源码ID
 * @returns {*} 返回操作结果
 */
export function rejectCode(data) {
    return request({
        url: "/admin/code/reject/" + data,
        method: "POST",
    });
}

/**
 * 通过源码审核
 * @param {Long} id 源码ID
 * @returns {*} 返回操作结果
 */
export function passCode(data) {
    return request({
        url: "/admin/code/pass/" + data,
        method: "POST",
    });
}

export function getBilibiliData() {
    return request({
        url: '/public/bilibili/fan',
        method: 'GET'
    })
}


export function aiChat(data) {
    return request({
        url: '/chat/chat',
        method: 'POST',
        data
    })
}

export function getChatHistory(chatId, pageNum) {
    return request({
        url: '/chat/history?chatId=' + chatId + '&pageNum=' + pageNum,
        method: 'GET',
    });
}

export function addConversation(messageDto) {
    return request({
        url: '/chat/conversation/add',
        method: 'POST',
        data: messageDto
    });
}

export function getConversationList() {
    return request({
        url: '/chat/conversation/list',
        method: 'GET',
    })
}

export function updateConversation(data) {
    return request({
        url: '/chat/conversation/update',
        method: 'POST',
        data
    })
}

export function deleteConversation(data) {
    return request({
        url: '/chat/delete',
        method: 'POST',
        data
    })
}

// ========================= RAG知识库管理相关API =========================

/**
 * 获取RAG知识库文件列表
 */
export function getRagKnowledgeList() {
    return request({
        url: '/chat/rag/list',
        method: 'GET'
    });
}

/**
 * 上传RAG知识库文件
 * @param {FormData} formData 包含文件的表单数据
 */
export function uploadRagKnowledge(formData) {
    return request({
        url: '/chat/rag/upload',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

/**
 * 删除RAG知识库文件
 * @param {string|number} ragId RAG文件ID
 */
export function deleteRagKnowledge(ragId) {
    return request({
        url: '/chat/rag/delete/' + ragId,
        method: 'GET'
    });
}

/**
 * 切换RAG知识库文件启用状态
 * @param {string|number} ragId RAG文件ID
 */
export function toggleRagKnowledgeStatus(ragId) {
    return request({
        url: '/chat/rag/toggle/' + ragId,
        method: 'POST'
    });
}

/**
 * 获取RAG知识库文件详情
 * @param {string} fileId 文件ID
 */
export function getRagKnowledgeDetail(fileId) {
    return request({
        url: '/chat/rag/detail/' + fileId,
        method: 'GET'
    });
}

/**
 * 重新处理RAG知识库文件
 * @param {string} fileId 文件ID
 */
export function reprocessRagKnowledge(fileId) {
    return request({
        url: '/chat/rag/knowledge/reprocess/' + fileId,
        method: 'POST'
    });
}

// ========================= MCP服务管理相关API =========================

/**
 * 获取MCP服务列表
 */
export function getMcpServiceList() {
    return request({
        url: '/chat/mcp/list',
        method: 'GET'
    });
}

/**
 * 添加MCP服务
 * @param {Object} serviceData 服务数据 {name, url, description}
 */
export function addMcpService(serviceData) {
    return request({
        url: '/mcp/service/add',
        method: 'POST',
        data: serviceData
    });
}

/**
 * 更新MCP服务
 * @param {string} serviceId 服务ID
 * @param {Object} serviceData 服务数据
 */
export function updateMcpService(serviceId, serviceData) {
    return request({
        url: '/mcp/service/update/' + serviceId,
        method: 'PUT',
        data: serviceData
    });
}

/**
 * 删除MCP服务
 * @param {string} serviceId 服务ID
 */
export function deleteMcpService(serviceId) {
    return request({
        url: '/mcp/service/delete/' + serviceId,
        method: 'DELETE'
    });
}

/**
 * 连接MCP服务
 * @param {string} serviceId 服务ID
 */
export function connectMcpService(serviceId) {
    return request({
        url: '/mcp/service/connect/' + serviceId,
        method: 'POST'
    });
}

/**
 * 断开MCP服务
 * @param {string} serviceId 服务ID
 */
export function disconnectMcpService(serviceId) {
    return request({
        url: '/mcp/service/disconnect/' + serviceId,
        method: 'POST'
    });
}

/**
 * 获取MCP服务状态
 * @param {string} serviceId 服务ID
 */
export function getMcpServiceStatus(serviceId) {
    return request({
        url: '/mcp/service/status/' + serviceId,
        method: 'GET'
    });
}

/**
 * 获取MCP服务可用工具列表
 * @param {string} serviceId 服务ID
 */
export function getMcpServiceTools(serviceId) {
    return request({
        url: '/mcp/service/tools/' + serviceId,
        method: 'GET'
    });
}

/**
 * 测试MCP服务连接
 * @param {Object} serviceData 服务数据 {name, url}
 */
export function testMcpServiceConnection(serviceData) {
    return request({
        url: '/mcp/service/test',
        method: 'POST',
        data: serviceData
    });
}

/**
 * 获取公共绘画图片列表
 * @param {number} page 页码
 * @returns {*} 返回公共图片列表
 */
export function getPublicDrawingList(page) {
    return request({
        url: '/draw/list/public?page=' + page,
        method: 'GET'
    });
}

/**
 * 获取私有视频列表
 * @param {number} page 页码
 * @returns {*} 返回视频列表
 */
export function getPrivatetVideoList(page) {
    return request({
        url: '/video/list/private?page=' + page,
        method: 'GET'
    });
}

/**
 * 删除视频
 * @param {number} videoId 视频ID
 * @returns {*} 删除结果
 */
export function deleteVideo(videoId) {
    return request({
        url: '/video/delete/' + videoId,
        method: 'DELETE'
    });
}

/**
 * 重新生成视频
 * @param {number} videoId 视频ID
 * @returns {*} 重新生成结果
 */
export function retryVideoGenerate(videoId) {
    return request({
        url: '/video/retry/' + videoId,
        method: 'POST'
    });
}

/**
 * 创建视频生成任务
 * @param {string} prompt 提示词
 * @param {File} file 上传的文件（可选）
 * @returns {*} 返回任务ID
 */
export function createVideoTask(prompt, file) {
    const formData = new FormData();
    formData.append('prompt', prompt);
    if (file) {
        formData.append('file', file);
    }

    return request({
        url: '/video/createTask',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

/**
 * 获取视频详情
 * @param {number} videoId 视频ID
 * @returns {*} 视频详细信息
 */
export function getVideoDetail(videoId) {
    return request({
        url: '/video/detail',
        method: 'GET',
        params: { videoId }
    });
}

/**
 * 获取公共视频列表
 * @param {number} page 页码
 * @returns {*} 返回公共视频列表
 */
export function getPublicVideoList(page) {
    return request({
        url: '/video/list/public?page=' + page,
        method: 'GET'
    });
}

/**
 * 切换视频公开状态
 * @param {number} videoId 视频ID
 * @returns {*} 切换结果
 */
export function toggleVideoPublic(videoId) {
    return request({
        url: '/video/toggle?videoId=' + videoId,
        method: 'GET'
    });
}