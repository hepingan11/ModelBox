<template>
    <div class="publish-code-view">
        <div class="publish-page-container">
            <div class="publish-page">
                <div class="panel-card">
                    <div class="page-header">
                        <h1>发布源码项目</h1>
                        <p class="subtitle">分享您的优质代码，获得额外收益</p>
                    </div>

                    <div class="form-container">
                        <el-form 
                            ref="formRef" 
                            :model="formData" 
                            :rules="rules" 
                            label-position="top"
                            class="publish-form"
                        >
                            <!-- 基本信息部分 -->
                            <div class="form-section">
                                <h2 class="section-title">基本信息</h2>
                                
                                <el-form-item label="代码项目名称" prop="codeName">
                                    <div class="input-container">
                                        <el-input 
                                            v-model="formData.codeName" 
                                            placeholder="请输入项目名称"
                                            show-word-limit
                                        >
                                            <template #prefix>
                                                <el-icon><Document /></el-icon>
                                            </template>
                                        </el-input>
                                        <div class="input-backdrop"></div>
                                    </div>
                                </el-form-item>
                                
                                <el-form-item label="编程语言" prop="language">
                                    <div class="input-container">
                                        <el-select 
                                            v-model="formData.language" 
                                            placeholder="请选择编程语言" 
                                            class="custom-select"
                                            popper-class="custom-select-dropdown"
                                        >
                                            <template #prefix>
                                                <el-icon><Collection /></el-icon>
                                            </template>
                                            <el-option
                                                v-for="item in languageOptions"
                                                :key="item.value"
                                                :label="item.label"
                                                :value="item.value"
                                            >
                                                <div class="option-content">
                                                    <span class="option-label">{{ item.label }}</span>
                                                    <span class="option-tag" :style="{ background: getLanguageColor(item.value) }"></span>
                                                </div>
                                            </el-option>
                                        </el-select>
                                        <div class="input-backdrop"></div>
                                    </div>
                                </el-form-item>
                                
                                <el-form-item label="项目价格 (¥)" prop="price">
                                    <div class="input-container">
                                        <el-input-number 
                                            v-model="formData.price" 
                                            :min="0" 
                                            :max="9999.99" 
                                            :precision="2" 
                                            :step="0.01"
                                            class="price-input"
                                            placeholder="设置价格 (0 表示免费)"
                                        >
                                            <template #prefix>
                                                <el-icon><Money /></el-icon>
                                            </template>
                                        </el-input-number>
                                        <div class="input-backdrop"></div>
                                    </div>
                                    <div class="helper-text">
                                        提示：设置为 0 表示免费分享，高质量的代码可以设置合理的价格
                                    </div>
                                </el-form-item>
                            </div>
                            
                            <!-- 项目介绍部分 -->
                            <div class="form-section">
                                <h2 class="section-title">项目介绍</h2>
                                
                                <el-form-item label="项目介绍" prop="introduce">
                                    <div class="input-container">
                                        <el-input 
                                            style="width: 400px;"
                                            v-model="formData.introduce" 
                                            type="textarea" 
                                            :rows="5" 
                                            placeholder="请详细描述您的项目功能、特点和使用场景"
                                            show-word-limit
                                        ></el-input>
                                        <div class="input-backdrop"></div>
                                    </div>
                                </el-form-item>
                            </div>
                            
                            <!-- 代码链接部分 -->
                            <div class="form-section">
                                <h2 class="section-title">代码链接</h2>
                                
                                <el-form-item label="下载链接" prop="downloadUrl">
                                    <div class="input-container">
                                        <el-input 
                                            v-model="formData.downloadUrl" 
                                            placeholder="请输入代码下载链接 (百度网盘、GitHub 等)"
                                        >
                                            <template #prefix>
                                                <el-icon><Link /></el-icon>
                                            </template>
                                        </el-input>
                                        <div class="input-backdrop"></div>
                                    </div>
                                    <div class="helper-text">
                                        提示：您可以上传到百度网盘或托管到 GitHub、Gitee 等平台
                                    </div>
                                </el-form-item>
                                
                                <el-form-item v-if="formData.downloadUrl && formData.downloadUrl.includes('pan.baidu.com')" label="提取码" prop="extractCode">
                                    <div class="input-container">
                                        <el-input 
                                            v-model="formData.extractCode" 
                                            placeholder="请输入百度网盘提取码"
                                            maxlength="4"
                                        >
                                            <template #prefix>
                                                <el-icon><Key /></el-icon>
                                            </template>
                                        </el-input>
                                        <div class="input-backdrop"></div>
                                    </div>
                                </el-form-item>
                            </div>
                            
                            <!-- 项目图片部分 -->
                            <div class="form-section">
                                <h2 class="section-title">项目预览图片</h2>
                                
                                <el-form-item label="上传图片" prop="imageList">
                                    <div class="upload-container">
                                        <el-upload
                                            v-model:file-list="fileList"
                                            list-type="picture-card"
                                            :auto-upload="false"
                                            :limit="5"
                                            :on-change="handleFileChange"
                                            :on-remove="handleFileRemove"
                                            class="custom-upload"
                                            accept="image/jpeg,image/png"
                                        >
                                            <el-icon><Plus /></el-icon>
                                            <div class="upload-text">点击上传</div>
                                        </el-upload>
                                        <div class="helper-text">
                                            提示：上传1-5张项目预览图片，图片大小不超过2MB，支持jpg、png格式
                                        </div>
                                    </div>
                                </el-form-item>
                            </div>
                            
                            <!-- 提交按钮 -->
                            <div class="form-actions">
                                <el-button 
                                    type="default" 
                                    class="cancel-btn"
                                    @click="goBack"
                                >取消</el-button>
                                
                                <el-button 
                                    type="primary" 
                                    class="submit-btn" 
                                    :loading="submitting"
                                    @click="submitForm"
                                >发布项目</el-button>
                            </div>
                        </el-form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { Document, Collection, Money, Link, Key, Plus, Upload } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { AddCode } from '../../../api/BSideApi';

const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);
const fileList = ref([]);
const rawImageFiles = ref([]); // 存储原始文件对象，用于提交

// 表单数据
const formData = reactive({
    codeName: '',
    language: '',
    price: 0,
    introduce: '',
    downloadUrl: '',
    extractCode: '',
    // imageList 在提交时处理
});

// 编程语言选项
const languageOptions = [
    { value: 'JavaScript', label: 'JavaScript' },
    { value: 'Python', label: 'Python' },
    { value: 'Java', label: 'Java' },
    { value: 'C', label: 'C' },
    { value: 'Go', label: 'Go' },
    { value: 'PHP', label: 'PHP' },
    { value: 'Ruby', label: 'Ruby' },
    { value: 'Swift', label: 'Swift' },
    { value: 'Kotlin', label: 'Kotlin' },
    { value: 'TypeScript', label: 'TypeScript' },
    { value: 'Vue', label: 'Vue' },
    { value: 'React', label: 'React' },
    { value: 'Angular', label: 'Angular' },
    { value: '其他', label: '其他' }
];

// 验证规则
const rules = {
    codeName: [
        { required: true, message: '请输入代码项目名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    language: [
        { required: true, message: '请选择编程语言', trigger: 'change' }
    ],
    introduce: [
        { required: true, message: '请输入项目介绍', trigger: 'blur' },
        { min: 10, max: 1000, message: '长度在 10 到 1000 个字符', trigger: 'blur' }
    ],
    downloadUrl: [
        { required: true, message: '请输入代码下载链接', trigger: 'blur' },
        { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' }
    ],
    extractCode: [
        { pattern: /^[a-zA-Z0-9]{4}$/, message: '请输入4位提取码', trigger: 'blur' }
    ]
};

// 获取语言颜色
const getLanguageColor = (language) => {
    const colorMap = {
        'JavaScript': '#f7df1e',
        'Python': '#3776ab',
        'Java': '#007396',
        'C': '#00599c',
        'Go': '#00add8',
        'PHP': '#777bb4',
        'Ruby': '#cc342d',
        'Swift': '#ffac45',
        'Kotlin': '#f18e33',
        'TypeScript': '#007acc',
        'Vue': '#42b883',
        'React': '#61dafb',
        'Angular': '#dd0031',
        '其他': '#95a5a6'
    };
    
    return colorMap[language] || '#95a5a6';
};

// 处理文件上传变化
const handleFileChange = (uploadFile, uploadFiles) => {
    const isLt2M = uploadFile.raw.size / 1024 / 1024 < 2;
    const isImage = ['image/jpeg', 'image/png'].includes(uploadFile.raw.type);
    
    if (!isImage) {
        ElMessage.error('图片只能是JPG或PNG格式!');
        fileList.value = uploadFiles.filter(file => file.uid !== uploadFile.uid);
        return;
    }
    
    if (!isLt2M) {
        ElMessage.error('图片大小不能超过2MB!');
        fileList.value = uploadFiles.filter(file => file.uid !== uploadFile.uid);
        return;
    }
    
    // 存储原始文件对象
    rawImageFiles.value = uploadFiles.map(file => file.raw);
    
    // 转为URL供预览
    if (!uploadFile.url && uploadFile.raw) {
        uploadFile.url = URL.createObjectURL(uploadFile.raw);
    }
};

// 处理文件移除
const handleFileRemove = (uploadFile, uploadFiles) => {
    rawImageFiles.value = uploadFiles.map(file => file.raw);
};

// 提交表单
const submitForm = () => {
    formRef.value.validate(async (valid) => {
        if (valid) {
            submitting.value = true;
            
            try {
                // 创建FormData对象用于文件上传
                const formDataToSubmit = new FormData();
                
                // 添加基本信息
                formDataToSubmit.append('codeName', formData.codeName);
                formDataToSubmit.append('language', formData.language);
                formDataToSubmit.append('price', formData.price);
                formDataToSubmit.append('introduce', formData.introduce);
                formDataToSubmit.append('downloadUrl', formData.downloadUrl);
                
                if (formData.extractCode) {
                    formDataToSubmit.append('extractCode', formData.extractCode);
                }
                
                // 添加图片文件 - 每个文件作为独立的imageList参数
                rawImageFiles.value.forEach(file => {
                    formDataToSubmit.append('imageList', file);
                });
                
                // 提交到后端
                console.log('提交表单数据', formDataToSubmit);
                const res = await AddCode(formDataToSubmit);
                console.log('提交响应', res);
                
                ElMessage.success('发布成功！');
                router.push('/my_code');
            } catch (error) {
                console.error('发布失败:', error);
                ElMessage.error('发布失败，请稍后重试');
            } finally {
                submitting.value = false;
            }
        } else {
            ElMessage.warning('请正确填写所有必填项');
            return false;
        }
    });
};

// 返回上一页
const goBack = () => {
    router.go(-1);
};
</script>

<style lang="scss" scoped>
.publish-code-view {
    width: 100%;
    height: 100vh;
    position: relative;
    display: flex;
    flex-direction: column;
}

.publish-page-container {
    width: 100%;
    flex: 1;
    overflow-y: auto;
    background-color: var(--bgColor1);
    position: relative;
}

.publish-page {
    padding: 40px 20px 120px;
    animation: fadeIn 0.3s ease;
    width: 100%;
    position: relative;
}

.panel-card {
    max-width: 900px;
    margin: 0 auto 80px;
    background-color: var(--bgColor2);
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    padding: 30px;
    position: relative;
    overflow: visible;
}

.page-header {
    text-align: center;
    margin-bottom: 40px;
    position: relative;
    z-index: 1;
    
    &::before {
        content: '';
        position: absolute;
        top: -80px;
        left: 50%;
        transform: translateX(-50%);
        width: 600px;
        height: 600px;
        background: linear-gradient(135deg, rgba(71, 118, 230, 0.15), rgba(142, 84, 233, 0.15));
        border-radius: 50%;
        z-index: -1;
        filter: blur(50px);
        opacity: 0.7;
    }
    
    h1 {
        color: var(--themeColor1);
        font-size: 36px;
        margin-bottom: 16px;
        font-weight: 600;
        background: linear-gradient(135deg, #4776e6, #8e54e9);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        animation: titleGlow 3s infinite alternate;
    }
    
    .subtitle {
        color: var(--textColor2);
        font-size: 16px;
    }
}

.form-container {
    position: relative;
    z-index: 1;
}

.form-section {
    margin-bottom: 40px;
    background-color: var(--bgColor1);
    border-radius: 12px;
    padding: 25px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    position: relative;
    overflow: hidden;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    
    &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    }
    
    .section-title {
        font-size: 20px;
        margin-top: 0;
        margin-bottom: 25px;
        color: var(--themeColor1);
        font-weight: 600;
        display: flex;
        align-items: center;
        
        &::before {
            content: '';
            display: inline-block;
            width: 4px;
            height: 20px;
            background: linear-gradient(to bottom, #4776e6, #8e54e9);
            margin-right: 10px;
            border-radius: 2px;
        }
    }
}

.publish-form {
    :deep(.el-form-item) {
        margin-bottom: 25px;
        
        &:last-child {
            margin-bottom: 0;
        }
        
        .el-form-item__label {
            padding-bottom: 8px;
            font-weight: 500;
            color: var(--textColor1);
        }
        
        .el-form-item__error {
            padding-top: 5px;
        }
    }
}

.input-container {
    position: relative;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    margin-bottom: 5px;
    
    &:hover, &:focus-within {
        transform: translateY(-2px);
        
        .input-backdrop {
            opacity: 1;
        }
    }
    
    .input-backdrop {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        opacity: 0.6;
        transition: opacity 0.3s ease;
        border-radius: 12px;
        pointer-events: none;
        z-index: -1;
        background: linear-gradient(135deg, rgba(113, 89, 193, 0.1), rgba(149, 90, 231, 0.2));
    }
    
    :deep(.el-input__wrapper),
    :deep(.el-textarea__wrapper),
    :deep(.el-input-number) {
        box-shadow: none;
        background-color: var(--bgColor2) !important;
        border-radius: 12px;
        padding: 0;
        border: 1px solid rgba(255, 255, 255, 0.1);
        transition: all 0.3s ease;
        
        &.is-focus {
            border-color: var(--themeColor1);
        }
    }
    
    :deep(.el-input__wrapper) {
        padding: 10px 15px;
    }
    
    :deep(.el-textarea__wrapper) {
        padding: 10px 15px;
    }
    
    :deep(.el-input__inner),
    :deep(.el-textarea__inner) {
        color: var(--textColor1);
        background-color: var(--bgColor2) !important;
        
        &::placeholder {
            color: var(--textColor3);
            opacity: 0.7;
        }
    }
    
    :deep(.el-input__prefix-inner) {
        color: var(--themeColor1);
        font-size: 16px;
        margin-right: 10px;
    }
    
    :deep(.el-input-number) {
        width: 100%;
        
        .el-input-number__decrease,
        .el-input-number__increase {
            border: none;
            background-color: transparent;
            color: var(--themeColor1);
        }
        
        .el-input__wrapper {
            border: none;
        }
    }
}

.price-input {
    :deep(.el-input__wrapper) {
        padding-left: 40px;
    }
    
    :deep(.el-input__prefix) {
        left: 15px;
        color: var(--themeColor1);
    }
}

.helper-text {
    color: var(--textColor3);
    font-size: 12px;
    margin-top: 8px;
    padding-left: 5px;
}

.upload-container {
    .custom-upload {
        :deep(.el-upload--picture-card) {
            width: 120px;
            height: 120px;
            border-radius: 12px;
            background-color: transparent;
            border: 1px dashed var(--themeColor1);
            transition: all 0.3s ease;
            
            &:hover {
                border-color: var(--themeColor2);
                background-color: rgba(var(--themeColor1-rgb), 0.05);
                transform: scale(1.02);
            }
        }
        
        :deep(.el-upload-list__item) {
            border-radius: 12px;
            overflow: hidden;
            transition: all 0.3s ease;
            
            &:hover {
                transform: scale(1.03);
            }
        }
        
        .upload-text {
            color: var(--textColor2);
            font-size: 12px;
            margin-top: 5px;
        }
        
        :deep(.el-icon) {
            color: var(--themeColor1);
            font-size: 28px;
        }
    }
}

.form-actions {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 40px;
    margin-bottom: 30px;  /* 增加底部边距 */
    position: relative;
    padding: 10px 0;  /* 添加上下内边距 */
    
    .cancel-btn,
    .submit-btn {
        min-width: 150px;
        padding: 12px 30px;
        font-size: 16px;
        border-radius: 12px;
        transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        
        &:hover {
            transform: translateY(-3px);
            box-shadow: 0 7px 14px rgba(0, 0, 0, 0.1);
        }
        
        &:active {
            transform: translateY(-1px);
        }
    }
    
    .cancel-btn {
        background-color: var(--bgColor3);
        border: none;
        color: var(--textColor2);
        
        &:hover {
            background-color: var(--bgColor4);
        }
    }
    
    .submit-btn {
        background: linear-gradient(135deg, #4776e6, #8e54e9);
        border: none;
        color: white;
        position: relative;
        overflow: hidden;
        
        &::after {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
            opacity: 0;
            transform: scale(1);
            transition: transform 0.5s ease-out, opacity 0.5s ease-out;
        }
        
        &:hover::after {
            opacity: 1;
            transform: scale(0);
            transition: transform 0.6s ease-out, opacity 0.6s ease-out;
        }
    }
}

.option-content {
    display: flex;
    align-items: center;
    width: 100%;
    gap: 8px;
    
    .option-label {
        flex: 1;
    }
    
    .option-tag {
        width: 12px;
        height: 12px;
        border-radius: 3px;
    }
}

:deep(.custom-select-dropdown) {
    border-radius: 12px;
    border: none;
    overflow: hidden;
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15), 0 5px 15px rgba(0, 0, 0, 0.08);
    background-color: var(--bgColor2);
    backdrop-filter: blur(10px);
    
    .el-scrollbar {
        .el-select-dropdown__wrap {
            max-height: 280px;
        }
    }
    
    .el-select-dropdown__item {
        display: flex;
        align-items: center;
        color: var(--textColor1);
        padding: 12px 20px;
        
        &.selected {
            color: var(--themeColor1);
            font-weight: 600;
            background-color: rgba(var(--themeColor1-rgb), 0.1);
        }
        
        &:hover, &:focus {
            background-color: rgba(var(--themeColor1-rgb), 0.05);
        }
    }
}

@keyframes titleGlow {
    0% {
        filter: drop-shadow(0 0 1px rgba(71, 118, 230, 0.3));
    }
    100% {
        filter: drop-shadow(0 0 5px rgba(142, 84, 233, 0.5));
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@media screen and (max-width: 768px) {
    .publish-page {
        padding: 20px 15px 120px;  /* 增加底部空间 */
    }
    
    .panel-card {
        padding: 20px 15px;
        margin-bottom: 60px;  /* 调整移动端底部边距 */
    }
    
    .page-header {
        h1 {
            font-size: 28px;
        }
        
        .subtitle {
            font-size: 14px;
        }
    }
    
    .form-section {
        padding: 20px 15px;
    }
    
    .form-actions {
        flex-direction: column;
        margin-bottom: 20px;
        
        .cancel-btn,
        .submit-btn {
            width: 100%;
        }
    }
}

/* 修复滚动问题的额外样式 */
:deep(html), :deep(body) {
    overflow: hidden;
    height: 100%;
}

:deep(#app) {
    height: 100%;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}
</style>
