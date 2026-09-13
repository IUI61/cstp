<script setup>
import { Search } from '@element-plus/icons-vue'
import { ref, onMounted, onUnmounted } from 'vue'
import server from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'


const searchContent = ref('')/*暂不完成*/
const pageNum = ref(1)
const goodsList = ref([])
const loading = ref(false)/*防止重复加载*/
const noMore = ref(false)/*没有更多数据了*/
const router = useRouter()
const handleScroll = () => {
  const { scrollTop, clientHeight, scrollHeight } = document.documentElement
  /* 距离底部小于100px触发*/
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    if (loading.value || noMore.value) return/*正在加载或没有数据了，不请求*/
    handleGoodsList()
  }
}
const handleGoodsList = async () => {
  loading.value = true
  try {
    const res = await server.get('/goods/show',{params:{pageNum:pageNum.value}})
    if(res.data.code === 200){
      const pageData = res.data.data
      if(pageData.records.length === 0){
        noMore.value = true
        ElMessage.warning('没有更多数据了')
        return
      }
      goodsList.value.push(...pageData.records)
      pageNum.value++
    }
    else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    console.log(error)
  } finally {
    loading.value = false
  }
}
onMounted(() => {
  handleGoodsList()
  window.addEventListener('scroll', handleScroll)
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

</script>

<template>
  <div class="home">
    <div class="home_header">
      <div class="home_search">
        <el-input placeholder="搜索你想要的闲置好物" v-model="searchContent" clearable size="large">
          <template #append>
            <el-button>搜索</el-button>
          </template>
          <template #prefix>
            <el-icon>
              <Search />
            </el-icon>
          </template>
        </el-input>
      </div>
    </div>
    <!--商品卡片-->
    <div class="home_content">
      <div v-if="goodsList.length === 0 && noMore" class="list_tip">还没有商品,快去发布第一件闲置吧</div>
      <div v-else-if="goodsList.length === 0 && loading" class="list_tip">商品加载中…</div>
      <div class="goods" v-for="goods in goodsList" :key="goods.id" @click="router.push('/goodsDetail/'+goods.id)">
        <img class="goods_img" :src="goods.goodsImage">
        <div class="goods_info">
          <div class="goods_name">{{ goods.goodsName }}</div>
          <div class="goods_price">¥{{ goods.price }}</div>
        </div>
      </div>
      <div v-if="goodsList.length > 0 && loading" class="load_tip">正在加载更多…</div>
      <div v-else-if="goodsList.length > 0 && noMore" class="load_tip">已经到底啦</div>
    </div>
  </div>
</template>

<style scoped>
.home{
  display: flex;
  flex-direction: column;
  width: 100%;
  overflow-x: hidden;
  box-sizing: border-box;
}
.home_header{
  display: flex;
  justify-content: center;
  padding: 30px 60px 18px;
  background: linear-gradient(180deg, #dceef9 0%, rgba(238, 243, 249, 0) 100%);
}
.home_search{
  width: 580px;
  max-width: 90%;
  box-sizing: border-box;
  padding: 4px 6px;
  background: #ffffff;
  border: 1px solid #e4ecf4;
  border-radius: 999px;
  box-shadow: 0 8px 22px rgba(79, 156, 204, 0.14);
}
.home_search :deep(.el-input__wrapper){
  background: transparent;
  box-shadow: none;
  padding: 2px 10px;
}
.home_search :deep(.el-input__inner){
  color: #2c3e50;
  font-size: 15px;
}
.home_search :deep(.el-input-group__append){
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  border: none;
  border-radius: 999px;
  padding: 0 26px;
}
.home_search :deep(.el-input-group__append .el-button){
  background: transparent;
  border: none;
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
}
.home_search :deep(.el-input-group__append .el-button:hover){
  color: #eaf6ff;
}
.home_content{
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  align-content: flex-start;
  gap: 22px;
  padding: 26px 60px 46px;
}
.goods{
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 3px 10px rgba(44, 84, 122, 0.06);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}
.goods:hover{
  transform: translateY(-5px);
  box-shadow: 0 14px 28px rgba(44, 84, 122, 0.15);
}
.goods_img{
  width: 100%;
  height: 185px;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}
.goods:hover .goods_img{
  transform: scale(1.06);
}
.goods_info{
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 14px;
}
.goods_name{
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.goods_price{
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 700;
  color: #ffffff;
  background: linear-gradient(135deg, #ff6a4d, #f0523f);
  padding: 3px 10px;
  border-radius: 999px;
  box-shadow: 0 2px 6px rgba(240, 82, 63, 0.28);
}
.list_tip{
  grid-column: 1 / -1;
  padding: 70px 0;
  text-align: center;
  font-size: 15px;
  color: #9fb0c1;
}
.load_tip{
  grid-column: 1 / -1;
  padding: 6px 0 2px;
  text-align: center;
  font-size: 13px;
  color: #b0bcc9;
}
</style>