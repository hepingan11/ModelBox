import { useStore } from '@/store';

// Helper to get base URL
const getBaseUrl = () => {
    const store = useStore();
    return store.apiBaseUrl;
};

// Helper for request
const request = (options) => {
    const store = useStore();
    const token = uni.getStorageSync('token');

    return new Promise((resolve, reject) => {
        uni.request({
            url: store.apiBaseUrl + options.url,
            method: options.method || 'GET',
            data: options.data,
            header: {
                'Authorization': token,
                ...(options.header || {})
            },
            success: (res) => {
                if (res.data.code === 200) {
                    resolve(res.data.data);
                } else {
                    reject(res.data);
                }
            },
            fail: (err) => {
                reject(err);
            }
        });
    });
};

export function getCodeList(keyword, language, sort) {
    return request({
        url: '/code/list',
        method: 'GET', // Or POST? assuming GET with params for now based on Web typical usage, or POST if body. Web uses GetCodeList(keyword, lang). 
        data: { keyword, language, sort }
    });
}

export function addCode(formData) {
    // uni.uploadFile or request with formData? 
    // Since uni.request doesn't support FormData directly like axios in same way for multipart.
    // We usually use uni.uploadFile for files.
    // But here we might have multiple files.
    // If mixed data, we might need special handling. 
    // For now, let's assume specific upload logic in component or simple POST if no files (but there are files).
    // This function might need to be handled in the component using uni.uploadFile.
    return Promise.reject("Please use upload logic in component");
}

export function getMyCodeList() {
    return request({
        url: '/code/user/buy/list', // Guessing path
        method: 'GET'
    });
}

export function getMyPublishList() {
    return request({
        url: '/code/user/publish/list', // Guessing path
        method: 'GET'
    });
}

export function getCodeDetail(codeId) {
    return request({
        url: '/code/detail/' + codeId,
        method: 'GET'
    });
}

export function addLook(codeId) {
    return request({
        url: '/code/look/' + codeId,
        method: 'GET' // or POST
    });
}

export function buyCode(codeId) {
    // For App, we might need different endpoint or handle alipay url
    return request({
        url: '/pay/alipay/code/' + codeId,
        method: 'POST'
    });
}

export function deleteCode(codeId) {
    return request({
        url: '/code/delete/' + codeId,
        method: 'DELETE'
    });
}
