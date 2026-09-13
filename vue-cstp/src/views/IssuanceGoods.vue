<script setup>
import { Plus } from '@element-plus/icons-vue'
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import service from '../utils/request.js'
import { useRouter } from 'vue-router'

const formRef = ref(null)
const fileList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const router = useRouter()
const form = reactive({
  issuanceImageList: [],
  name: '',
  price: '',
  type: '',
  description: '',
})
const beforeImageUpload = (uploadFile) => {
  const isJpgOrPng = uploadFile.raw.type === 'image/jpeg' || uploadFile.raw.type === 'image/png'
  const isSizeLessThan2M = uploadFile.raw.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
  }
  if (!isSizeLessThan2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  return isJpgOrPng && isSizeLessThan2M
}
const goodsSubmit = async () => {
  const formData = new FormData()
  for (const file of form.issuanceImageList){
    formData.append('file', file)
  }
  formData.append('userId',JSON.parse(localStorage.getItem('user')).id)
  formData.append('name', form.name)
  formData.append('price', form.price)
  formData.append('type', form.type)
  formData.append('description', form.description)
  try {
    const res = await service.post('/goods/issuance', formData)
    if (res.data.code === 200) {
      ElMessage.success(res.data.message)
    }
    else {
      ElMessage.error(res.data.message)
    }
  }
  catch (error) {
    console.log(error)
  }
}
const cancel = () => {
  router.push('/')
}

const handleImageChange = (uploadFile) => {
  if (!beforeImageUpload(uploadFile)) return
  form.issuanceImageList.push(uploadFile.raw)
}
const handleRemove = (uploadFile, uploadFiles) => {
  console.log(uploadFile, uploadFiles)
}

const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url
  dialogVisible.value = true
}

</script>

<template>
<div class="issuance-goods">
  <el-form ref="formRef" :model="form" class="issuance-goods-form" size="large">
    <div class="upload">
      <div class="upload_title">商品图片</div>
      <el-form-item>
        <el-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleImageChange"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
        >
          <el-icon><Plus /></el-icon>
          <template #tip>
            <div class="el-upload__tip">只能上传jpg/png文件，且不超过2MB</div>
          </template>
        </el-upload>
        <el-dialog v-model="dialogVisible">
          <img w-full :src="dialogImageUrl" alt="商品图片" />
        </el-dialog>
      </el-form-item>

    </div>
    <div class="info">
      <div class="title_area">
        <h1 class="title">商品发布</h1>
        <p class="subtitle">完善以下信息,让闲置物品找到新主人</p>
      </div>
      <el-form-item label="商品名称:">
        <el-input v-model="form.name" placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="商品价格:">
        <el-input-number v-model="form.price" controls-position="right" placeholder="请输入商品价格" min="0">
          <template #prefix>
            <span>￥</span>
          </template>
        </el-input-number>
      </el-form-item>
      <el-form-item label="商品类型:">
        <el-input v-model="form.type" placeholder="请输入商品类型" />
      </el-form-item>
      <el-form-item label="商品描述:">
        <el-input v-model="form.description" :autosize="{ minRows: 4, maxRows: 8 }" type="textarea" placeholder="请输入商品描述" />
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="cancel">取消</el-button>
        <el-button type="primary" @click="goodsSubmit">发布</el-button>
      </el-form-item>
    </div>
  </el-form>
</div>
</template>

<style scoped>
.issuance-goods{
  padding: 24px 70px;
  display: flex;
  flex: 1;
  box-sizing: border-box;
}

.issuance-goods-form{
  flex: 1;
  display: flex;
  justify-content: space-between;
  gap: 40px;
  padding: 34px 44px 40px;
  background: #ffffff;
  border: 1px solid #e7ecf3;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(31, 86, 141, 0.08);
}
.upload{
  flex: 0 0 460px;
  box-sizing: border-box;
  padding: 24px;
  background: #f7fafd;
  border-radius: 14px;
}
.upload_title{
  position: relative;
  padding-left: 12px;
  margin-bottom: 20px;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #37474f;
}
.upload_title::before{
  content: "";
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  border-radius: 2px;
  background: linear-gradient(180deg, #4f9ccc, #8fd3f4);
}
.upload :deep(.el-form-item){
  width: 100%;
  margin-bottom: 0;
}
.info{
  flex: 1;
  min-width: 0;
}
.title_area{
  text-align: center;
  margin-bottom: 28px;
}
.title{
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #37474f;
}
.subtitle{
  margin-top: 6px;
  font-size: 13px;
  color: #9fb0c1;
}
.info :deep(.el-form-item){
  flex-direction: column;
  margin-bottom: 20px;
}
.info :deep(.el-form-item__label){
  justify-content: flex-start;
  height: auto;
  padding: 0;
  margin-bottom: 6px;
  line-height: 1.5;
  font-size: 14px;
  font-weight: 600;
  color: #5b6b7c;
}
.info :deep(.el-form-item__content){
  width: 100%;
}
.info :deep(.el-input__wrapper),
.info :deep(.el-textarea__inner){
  border-radius: 10px;
  background: #f4f7fb;
  box-shadow: 0 0 0 1px #e2e9f2 inset;
}
.info :deep(.el-input__wrapper.is-focus),
.info :deep(.el-textarea__inner:focus){
  background: #ffffff;
  box-shadow: 0 0 0 1.5px #8fd3f4 inset;
}
.info :deep(.el-textarea__inner){
  transition: background-color 0.15s ease, box-shadow 0.15s ease;
}
.info :deep(.el-input-number){
  width: 100%;
}
.info :deep(.el-input-number .el-input__prefix){
  color: #f0523f;
  font-weight: 600;
}
.issuance-goods :deep(.el-upload--picture-card){
  width: 120px;
  height: 120px;
  background: #eef4fa;
  border: 1.5px dashed #a9c8de;
  border-radius: 14px;
  color: #7fa6c2;
  transition: border-color 0.15s ease, background-color 0.15s ease, color 0.15s ease;
}
.issuance-goods :deep(.el-upload--picture-card:hover){
  border-color: #4f9ccc;
  background: #e8f2fa;
  color: #4f9ccc;
}
.issuance-goods :deep(.el-upload-list--picture-card .el-upload-list__item){
  width: 120px;
  height: 120px;
  border-radius: 14px;
  overflow: hidden;
}
.issuance-goods :deep(.el-upload__tip){
  margin-top: 6px;
  font-size: 12px;
  color: #a9b7c6;
}
.info :deep(.el-form-item:last-child){
  flex-direction: row;
  justify-content: flex-end;
  gap: 14px;
  margin-top: 32px;
}
.info :deep(.el-form-item:last-child .el-button){
  height: 42px;
  padding: 0 32px;
  border-radius: 21px;
  font-size: 15px;
  font-weight: 600;
}
.info :deep(.el-button--primary){
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  border: none;
  box-shadow: 0 4px 12px rgba(79, 156, 204, 0.35);
}
.info :deep(.el-button--primary:hover){
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  filter: brightness(1.05);
}
.info :deep(.el-button--info){
  background: #ffffff;
  border: 1px solid #d6dfe8;
  color: #5b6b7c;
}
.info :deep(.el-button--info:hover){
  background: #f3f8fc;
  border-color: #4f9ccc;
  color: #4f9ccc;
}
</style>