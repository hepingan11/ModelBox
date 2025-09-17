import {createApp} from 'vue'
import App from './App.vue'
import store from './store'
import router from './router'
import axios from './utils/PythonRequest'
import * as echarts from 'echarts'

// TODO ElementPlus
import ElementPlus from 'element-plus';
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/theme-chalk/index.css';
import 'element-plus/theme-chalk/display.css';


// TODO v-md-editor
import VMdEditor from '@kangc/v-md-editor';
import '@kangc/v-md-editor/lib/style/base-editor.css';
import vuepressTheme from '@kangc/v-md-editor/lib/theme/vuepress.js';
import '@kangc/v-md-editor/lib/theme/style/vuepress.css';
// TODO Prism
import Prism from 'prismjs';
// TODO 代码高亮
import 'prismjs/components/prism-json';
// TODO Copy Code 快捷复制代码
import createCopyCodePlugin from '@kangc/v-md-editor/lib/plugins/copy-code/index';
import '@kangc/v-md-editor/lib/plugins/copy-code/copy-code.css';
import Echarts from 'vue-echarts';
import 'echarts-wordcloud';


//引入axios  by zhengkai.blog.csdn.net
// import axios from 'axios'
// Vue.prototype.$axios = axios
// axios.defaults.baseURL = '/api'  //自动附加在所有axios请求前面，则可以省略/api，直接写'/xxxx/xxx'。否则需要设置'/api/xxxx/xxx'

VMdEditor.use(vuepressTheme, {
    Prism
});
VMdEditor.use(createCopyCodePlugin());

// 全局抑制 ResizeObserver 错误
const suppressResizeObserverError = (e) => {
  if (e.message && e.message.includes('ResizeObserver loop')) {
    console.warn('ResizeObserver 循环错误已被全局抑制');
    e.stopImmediatePropagation();
    return false;
  }
};

// 监听全局错误
window.addEventListener('error', suppressResizeObserverError, true);
window.addEventListener('unhandledrejection', (e) => {
  if (e.reason && e.reason.message && e.reason.message.includes('ResizeObserver')) {
    console.warn('ResizeObserver Promise 错误已被全局抑制');
    e.preventDefault();
  }
});


let app = createApp(App)
// v-md-editor
app.use(VMdEditor);


app.use(router)
app.use(store)
app.use(ElementPlus)
app.component('e-charts',Echarts)
app.config.globalProperties.$echarts = echarts
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.mount('#app')
app.config.globalProperties.$axios = axios; //配置axios的全局引用