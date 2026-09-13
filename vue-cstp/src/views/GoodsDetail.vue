<script setup>
import {useRouter,useRoute} from 'vue-router'
import {ref,reactive,onMounted,computed} from 'vue'
import {ElMessage} from 'element-plus'
import server from '../utils/request'

const router = useRouter()
const route = useRoute()
const goodsId = ref(route.params.id)
const detailList = ref({})
const showImage = ref('')
const currentUser = ref(JSON.parse(localStorage.getItem('user')))
const buying = ref(false)
//购买弹窗开关
const buyDialogVisible = ref(false)
const orderFormRef = ref(null)
const orderForm = reactive({buyerName: '', buyerPhone: '', buyerAddress: ''})
const isMyGoods = computed(() => {
  return currentUser.value && String(detailList.value.sellerId) === String(currentUser.value.id)
})
const rules = {
  buyerName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  buyerPhone: [
    { required: true, message: '请输入购买手机号', trigger: 'blur' },
      { min: 11, max: 11, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  buyerAddress: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}
const handleImage = (image) => {
  showImage.value = image
}
const handleChat = async () => {
  const buyerId = JSON.parse(localStorage.getItem('user')).id
  const res = await server.post('/chat/create',{sellerId:detailList.value.sellerId,goodsId:goodsId.value})
  if(res.data.code === 200){
    router.push('/chat/'+res.data.data)
  }
  else {
    ElMessage.error(res.data.message)
  }
}
const handleBuy = () => {
  orderForm.buyerName = currentUser.value.name || ''
  orderForm.buyerPhone = currentUser.value.phone || ''
  buyDialogVisible.value = true
}
const submitOrder = async () => {
  if (!orderFormRef.value) return
  try {
    await orderFormRef.value.validate()
  } catch {
    ElMessage.error('请填写正确的信息')
    return
  }
  buying.value = true
  const res = await server.post('/order/create', {goodsId:goodsId.value,
    buyerName:orderForm.buyerName,
    buyerPhone:orderForm.buyerPhone,
    buyerAddress:orderForm.buyerAddress})
  if (res.data.code === 200) {
    ElMessage.success('购买成功')
    buyDialogVisible.value = false
  } else {
    ElMessage.error(res.data.message)
  }
  buying.value = false
}
onMounted(async () => {
  if (goodsId.value === null){
    router.push('/')
  }
    const res = await server.get('/goods/detail',{params:{id:goodsId.value}})
    if(res.data.code === 200){
      detailList.value = res.data.data
      showImage.value = detailList.value.imageList?.[0]
    }
    else {
      ElMessage.error(res.data.message)
    }
})
</script>

<template>
<div class="goodsDetail">
<!--图片展示-->
  <div class="imageList">
    <div class="images_box">
      <div class="images">
        <img v-for="image in detailList.imageList" :key="image" :src="image"
             :class="{ active: image === showImage }" @mouseenter="handleImage(image)">
      </div>
    </div>
    <div class="image">
      <img v-if="showImage" :src="showImage">
      <div v-else class="image_placeholder">图片加载中…</div>
    </div>
  </div>
<!--商品右边内容-->
  <div class="goodsDetail_content">
    <div class="goodsDetail_info">
      <!--商品卖方信息-->
      <div class="goodsDetail_seller">
        <div class="seller_avatar">
          <img :src="detailList.avatar">
        </div>
        <div class="seller_info">
          <div class="seller_name">
            {{detailList.sellerName}}
          </div>
          <div class="seller_tip">闲置卖家</div>
        </div>
      </div>
      <!--商品信息-->
      <div class="goodsDetail_name">
        {{detailList.name}}
      </div>
      <span v-if="detailList.type" class="goodsDetail_type">{{ detailList.type }}</span>
      <div class="goodsDetail_price">
        ¥{{detailList.price}}
      </div>
      <div class="goodsDetail_description">
        {{detailList.description}}
      </div>
    </div>
<!--按钮-->
    <div class="buttons" v-if="!isMyGoods">
      <button class="button_chat" @click="handleChat">聊一聊</button>
      <button class="button_buy" @click="handleBuy">购买</button>
    </div>
  </div>
</div>
<el-dialog v-model="buyDialogVisible" title="是否确认购买该商品？" width="30%">
  <div class="buy_goods">
    <img :src="showImage || detailList.imageList?.[0]" class="buy_goods_img">
    <div class="buy_goods_info">
      <div class="buy_goods_name">{{ detailList.name }}</div>
      <div class="buy_goods_price">¥{{ detailList.price }}</div>
    </div>
  </div>
  <el-form ref="orderFormRef" :model="orderForm" :rules="rules" label-width="80px" class="buy_form">
    <el-form-item label="收货人" prop="buyerName">
      <el-input v-model="orderForm.buyerName" placeholder="请输入收货人姓名"></el-input>
    </el-form-item>
    <el-form-item label="手机号" prop="buyerPhone">
      <el-input v-model="orderForm.buyerPhone" placeholder="请输入购买手机号"></el-input>
    </el-form-item>
    <el-form-item label="收货地址" prop="buyerAddress">
      <el-input v-model="orderForm.buyerAddress" placeholder="请输入收货地址"></el-input>
    </el-form-item>
  </el-form>
  <template #footer>
    <el-button @click="buyDialogVisible = false">取消</el-button>
    <el-button type="primary" :loading="buying" @click="submitOrder">确认</el-button>
  </template>
</el-dialog>
</template>

<style scoped>
.goodsDetail{
  display: flex;
  margin: 24px 70px;
  background: #ffffff;
  border: 1px solid #eef1f6;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(31, 86, 141, 0.08);
}
.imageList{
  display: flex;
  background: #ffffff;
}
.images_box{
  display: flex;
  align-items: flex-start;
  padding: 24px 10px 24px 24px;
}
.images{
  width: 150px;
  max-height: 620px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  overflow-x: hidden;
}
.images::-webkit-scrollbar{
  width: 4px;
}
.images::-webkit-scrollbar-thumb{
  background: #cdd9e6;
  border-radius: 2px;
}
.images img{
  width: 150px;
  height: 150px;
  flex-shrink: 0;
  box-sizing: border-box;
  border-radius: 12px;
  border: 2px solid transparent;
  object-fit: cover;
  display: block;
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}
.images img:hover{
  border-color: #a8cfe8;
}
.images img.active{
  border-color: #4f9ccc;
  box-shadow: 0 0 0 3px rgba(79, 156, 204, 0.18);
}
.image{
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 30px;
}
.image img{
  max-width: 100%;
  max-height: 640px;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 6px 24px rgba(31, 86, 141, 0.14);
}
.image_placeholder{
  padding: 130px 60px;
  font-size: 14px;
  color: #a9b7c6;
}
.goodsDetail_content{
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 24px 30px 0;
  border-left: 1px solid #eef1f6;
}
.goodsDetail_info{
  display: flex;
  flex-direction: column;
}
.goodsDetail_seller{
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 20px;
  background: #f3f8fc;
  border-radius: 12px;
}
.seller_avatar{
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  background: #e2ecf5;
}
.seller_avatar img{
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.seller_info{
  min-width: 0;
}
.seller_name{
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.seller_tip{
  margin-top: 2px;
  font-size: 12px;
  color: #8aa0b4;
}
.goodsDetail_name{
  font-size: 22px;
  font-weight: 700;
  color: #2b3648;
  line-height: 1.4;
  word-break: break-word;
}
.goodsDetail_type{
  align-self: flex-start;
  margin-top: 12px;
  padding: 4px 14px;
  border: 1px solid #d5e6f2;
  border-radius: 999px;
  background: #eef4fb;
  color: #4f9ccc;
  font-size: 12px;
  font-weight: 600;
}
.goodsDetail_price{
  margin-top: 16px;
  font-size: 28px;
  font-weight: 700;
  color: #f0523f;
  letter-spacing: 1px;
}
.goodsDetail_description{
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px dashed #e2e9f2;
  font-size: 15px;
  line-height: 1.9;
  color: #5b6b7c;
  white-space: pre-wrap;
  word-break: break-word;
}
.buttons{
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
  margin-top: auto;
  padding: 20px 0 26px;
}
.buttons button{
  height: 44px;
  padding: 0 32px;
  border: none;
  border-radius: 22px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
}
.button_chat{
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(79, 156, 204, 0.35);
}
.button_chat:hover{
  filter: brightness(1.06);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(79, 156, 204, 0.45);
}
.buttons .button_buy{
  background: #ffffff;
  border: 1.5px solid #f3b3a6;
  color: #f0523f;
}
.buttons .button_buy:hover{
  border-color: #f0523f;
  background: #fff6f3;
  transform: translateY(-1px);
}
.buy_goods{
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  margin-bottom: 8px;
  background: #f7fafc;
  border-radius: 10px;
}
.buy_goods_img{
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.buy_goods_info{
  min-width: 0;
}
.buy_goods_name{
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.buy_goods_price{
  margin-top: 4px;
  font-size: 18px;
  font-weight: 700;
  color: #f0523f;
}
.buy_form{
  margin-top: 4px;
}
</style>