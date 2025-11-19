// store/index.js
// 全局API基础URL配置

// 定义API基础URL
export const apiBaseUrl = 'http://127.0.0.1:9090'

// #ifndef VUE3
import Vue from 'vue'
// 为Vue2提供全局访问
Vue.prototype.$apiBaseUrl = apiBaseUrl
// #endif 