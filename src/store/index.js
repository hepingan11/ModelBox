// store/index.js
import { reactive, watch } from 'vue'

// 全局状态管理 (简单实现)
export const store = reactive({
    state: {
        theme: uni.getStorageSync('theme') || 'lightMode', // 'lightMode' | 'darkMode'
        userInfo: uni.getStorageSync('userInfo') || null
    },

    setTheme(mode) {
        this.state.theme = mode
        uni.setStorageSync('theme', mode)
        // 设置原生部分颜色
        this.updateNativeTheme(mode)
    },

    toggleTheme() {
        const newTheme = this.state.theme === 'lightMode' ? 'darkMode' : 'lightMode'
        this.setTheme(newTheme)
    },

    updateNativeTheme(mode) {
        if (mode === 'darkMode') {
            uni.setNavigationBarColor({
                frontColor: '#ffffff',
                backgroundColor: '#222222'
            })
            uni.setTabBarStyle({
                backgroundColor: '#222222',
                color: '#999999',
                selectedColor: '#8166e7',
                borderStyle: 'black'
            })
        } else {
            uni.setNavigationBarColor({
                frontColor: '#000000',
                backgroundColor: '#ffffff'
            })
            uni.setTabBarStyle({
                backgroundColor: '#ffffff',
                color: '#999999',
                selectedColor: '#8166e7',
                borderStyle: 'white'
            })
        }
    }
})

// 全局API基础URL配置
export const apiBaseUrl = 'http://127.0.0.1:8625'

// #ifndef VUE3
import Vue from 'vue'
// 为Vue2提供全局访问
Vue.prototype.$apiBaseUrl = apiBaseUrl
Vue.prototype.$store = store
// #endif 