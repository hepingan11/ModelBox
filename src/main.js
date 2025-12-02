import {
	createSSRApp
} from "vue";
import App from "./App.vue";
import shareMixin from "./mixins/shareMixin.js";

export function createApp() {
	const app = createSSRApp(App);
	
	// 添加全局分享混入
	app.mixin(shareMixin);
	
	return {
		app,
	};
}
