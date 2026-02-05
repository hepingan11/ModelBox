import { createApp } from 'vue'
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
// 基础语言包（必须先加载）
import 'prismjs/components/prism-clike';
import 'prismjs/components/prism-markup';
import 'prismjs/components/prism-c';

// 依赖语言包
import 'prismjs/components/prism-cpp'; // 依赖 c
import 'prismjs/components/prism-java'; // 依赖 clike
import 'prismjs/components/prism-python';
import 'prismjs/components/prism-javascript'; // 依赖 clike
import 'prismjs/components/prism-typescript'; // 依赖 javascript
import 'prismjs/components/prism-css';
import 'prismjs/components/prism-markdown'; // 依赖 markup
import 'prismjs/components/prism-sql';
import 'prismjs/components/prism-bash';
import 'prismjs/components/prism-xml-doc';
import 'prismjs/components/prism-go';
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

// 解决 ResizeObserver loop completed with undelivered notifications 报错
const _ResizeObserver = window.ResizeObserver;
window.ResizeObserver = class ResizeObserver extends _ResizeObserver {
  constructor(callback) {
    super((entries, observer) => {
      requestAnimationFrame(() => {
        callback(entries, observer);
      });
    });
  }
};

const suppressResizeObserverError = (e) => {
  if (e.message && (e.message.indexOf('ResizeObserver loop') !== -1 || e.message.indexOf('ResizeObserver loop completed') !== -1)) {
    e.stopImmediatePropagation();
    e.preventDefault();
    return false;
  }
};
window.addEventListener('error', suppressResizeObserverError, true);
window.addEventListener('unhandledrejection', (e) => {
  if (e.reason && e.reason.message && e.reason.message.includes('ResizeObserver')) {
    e.preventDefault();
  }
});


let app = createApp(App)
// v-md-editor
app.use(VMdEditor);


app.use(router)
app.use(store)
app.use(ElementPlus)
app.component('e-charts', Echarts)
app.config.globalProperties.$echarts = echarts
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
app.config.globalProperties.$axios = axios; //配置axios的全局引用