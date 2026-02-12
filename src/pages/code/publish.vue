<template>
  <view class="publish-page">
    <view class="form-container">
      <view class="form-item">
        <text class="label">项目名称</text>
        <input class="input" v-model="form.codeName" placeholder="请输入项目名称" />
      </view>
      
      <view class="form-item">
        <text class="label">编程语言</text>
        <picker 
          mode="selector" 
          :range="languages" 
          @change="onLangChange"
        >
          <view class="picker">{{ form.language || '请选择' }}</view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="label">价格 (¥)</text>
        <input class="input" type="digit" v-model="form.price" placeholder="0 表示免费" />
      </view>
      
      <view class="form-item">
        <text class="label">项目介绍</text>
        <textarea class="textarea" v-model="form.introduce" placeholder="详细描述项目功能..." />
      </view>
      
      <view class="form-item">
        <text class="label">下载链接</text>
        <input class="input" v-model="form.downloadUrl" placeholder="如百度网盘链接" />
      </view>
      
      <view class="form-item">
        <text class="label">提取码</text>
        <input class="input" v-model="form.extractCode" placeholder="选填" />
      </view>
      
      <view class="form-item">
        <text class="label">项目图片 (最多5张)</text>
        <view class="image-list">
          <view 
            v-for="(img, idx) in imageList" 
            :key="idx" 
            class="image-item"
          >
            <image :src="img" mode="aspectFill"></image>
            <view class="remove-btn" @tap="removeImage(idx)">x</view>
          </view>
          <view 
            v-if="imageList.length < 5" 
            class="add-btn" 
            @tap="chooseImage"
          >+</view>
        </view>
      </view>
    </view>
    
    <view class="submit-btn" @tap="submit">发布项目</view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useStore } from '@/store';

const store = useStore();
const form = reactive({
  codeName: '',
  language: '',
  price: '',
  introduce: '',
  downloadUrl: '',
  extractCode: ''
});
const imageList = ref([]);
const languages = ['JavaScript', 'Python', 'Java', 'C', 'Go', 'PHP', 'Vue', 'React', 'Other'];

const onLangChange = (e) => {
  form.language = languages[e.detail.value];
};

const chooseImage = () => {
  uni.chooseImage({
    count: 5 - imageList.value.length,
    success: (res) => {
      imageList.value = [...imageList.value, ...res.tempFilePaths];
    }
  });
};

const removeImage = (idx) => {
  imageList.value.splice(idx, 1);
};

const submit = () => {
  if (!form.codeName || !form.language || !form.introduce || !form.downloadUrl) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' });
    return;
  }
  
  uni.showLoading({ title: '发布中...' });
  
  // Construct FormData logic manually for uni.uploadFile
  // Since uni.uploadFile sends form fields as 'formData' object and files separately.
  // Note: 'files' parameter is supported in App & H5.
  
  const files = imageList.value.map((path, index) => {
    return {
      name: 'imageList', // Matches Web backend expectation? In Web it was formData.append('imageList', file).
      uri: path
    };
  });
  
  uni.uploadFile({
    url: store.apiBaseUrl + '/code/add', // Correct endpoint?
    files: files, // For multiple files
    formData: {
      codeName: form.codeName,
      language: form.language,
      price: form.price || 0,
      introduce: form.introduce,
      downloadUrl: form.downloadUrl,
      extractCode: form.extractCode
    },
    header: {
      'Authorization': uni.getStorageSync('token')
    },
    success: (res) => {
      const data = JSON.parse(res.data);
      if (data.code === 200) {
        uni.showToast({ title: '发布成功' });
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
      } else {
        uni.showToast({ title: data.msg || '发布失败', icon: 'none' });
      }
    },
    fail: (e) => {
      uni.showToast({ title: '网络错误', icon: 'none' });
    },
    complete: () => {
      uni.hideLoading();
    }
  });
};
</script>

<style lang="scss">
.publish-page {
  padding: 20px;
  background-color: #1a1a1a;
  min-height: 100vh;
  color: #fff;
}
.form-container {
  background: #252525;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
}
.form-item {
  margin-bottom: 15px;
  border-bottom: 1px solid #333;
  padding-bottom: 15px;
  
  .label {
    display: block;
    margin-bottom: 5px;
    color: #aaa;
    font-size: 14px;
  }
  .input, .textarea, .picker {
    width: 100%;
    color: #fff;
    font-size: 16px;
  }
  .textarea { height: 80px; }
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  .image-item {
    position: relative;
    width: 60px;
    height: 60px;
    image { width: 100%; height: 100%; border-radius: 4px; }
    .remove-btn {
      position: absolute;
      top: -5px;
      right: -5px;
      background: red;
      color: #fff;
      border-radius: 50%;
      width: 15px;
      height: 15px;
      font-size: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  .add-btn {
    width: 60px;
    height: 60px;
    border: 1px dashed #666;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #666;
    font-size: 30px;
    border-radius: 4px;
  }
}

.submit-btn {
  background: #007aff;
  color: #fff;
  text-align: center;
  padding: 15px;
  border-radius: 25px;
  font-weight: bold;
}
</style>
