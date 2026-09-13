<script setup>
import {ref,computed,onMounted} from 'vue'
import {ArrowDown, ArrowLeft, ArrowRight, Goods, ShoppingCart, Sell} from '@element-plus/icons-vue'
import server from '../utils/request'
import {ElMessage} from 'element-plus'

const pageNum = ref(1)//当前页码
const totalPages = ref(1)//后端返回的总页数
const total = ref(0)//订单总条数
const loading = ref(false)/*防止重复加载*/
const orderList = ref([])//分页模式下当前页的订单列表
const handleRole = ref('')//当前分类:release/buy/sell
const statusMap = computed(() => {
  return handleRole.value === 'release' ? {
    0: '已下架',
    1: '售卖中',
  } : {
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
})
const buttonText = (status) => {
  if (handleRole.value === 'release' && status === 0) return '上架'
  else if (handleRole.value === 'release' && status === 1) return '下架'
  else if (handleRole.value === 'sell' && status === 1) return '去发货'
  else if (handleRole.value === 'sell' && status === 2) return '查看物流'
  else if (handleRole.value === 'sell' && status === 3) return '查看评价'
  else if (handleRole.value === 'buy' && status === 1) return '取消订单'
  else if (handleRole.value === 'buy' && status === 2) return '确认收货'
  else if (handleRole.value === 'buy' && status === 3) return '评价'
  else if (handleRole.value === 'buy' && status === 4) return '查看商品'
}
const handleGoodsOrderStatus = async (orderId, orderStatus) => {
  const res = await server.put('/orders/goodsOrderStatus', {orderId: orderId, status: orderStatus, role: handleRole.value})
  if (res.data.code === 200) {
    ElMessage.success('操作成功')
  } else {
    ElMessage.error(res.data.message)
  }
}
// 卡片展开状态:记录当前展开卡片的索引(同一时间最多展开一张)
const expandedIndex = ref(-1)

// 拉取指定页的订单(分页模式:每次覆盖当前页数据)
const handleGoodsOrders = async (targetPage) => {
  if (loading.value) return/*防重复请求*/
  loading.value = true
  try {
    const res = await server.get('/orders/goodsOrdersList', {params: {pageNum: targetPage, role: handleRole.value}})
    if (res.data.code === 200) {
      const pageData = res.data.data
      total.value = pageData.total ?? 0
      totalPages.value = Math.max(1, pageData.pages ?? 1)
      const records = pageData.records ?? []
      // 目标页没有数据:页码保持不变,避免越界
      if (records.length === 0 && targetPage > 1) {
        ElMessage.warning('没有更多数据了')
        return
      }
      orderList.value = records
      pageNum.value = targetPage
      expandedIndex.value = -1/*翻页后收起已展开的卡片*/
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    console.log(error)
  } finally {
    loading.value = false
  }
}
// 上一页:已在第一页时不响应
const handlePrevPage = () => {
  if (pageNum.value <= 1) return
  handleGoodsOrders(pageNum.value - 1)
}
// 下一页:已在最后一页时不响应
const handleNextPage = () => {
  if (pageNum.value >= totalPages.value) return
  handleGoodsOrders(pageNum.value + 1)
}
// 页数选择器:跳转到指定页
const handlePageChange = (page) => {
  if (page === pageNum.value) return
  handleGoodsOrders(page)
}
// 切换订单分类:回到第一页重新加载
const switchRole = (role) => {
  if (loading.value || handleRole.value === role) return
  handleRole.value = role
  orderList.value = []
  pageNum.value = 1
  handleGoodsOrders(1)
}
onMounted(() => {
  switchRole('release')/*默认展示"发布的"*/
})

// 展开/收起卡片订单详情(同一时间最多展开一张)
const toggleExpand = (index) => {
  if(handleRole.value === 'release') return
  expandedIndex.value = expandedIndex.value === index ? -1 : index
}

</script>

<template>
<div class="orders_view">
  <div class="orders">
    <!--订单卡片列表:从上往下渲染-->
    <div class="order_list">
      <!--无数据/加载中提示-->
      <div v-if="orderList.length === 0" class="list_tip">
        {{ loading ? '订单加载中…' : '暂无相关订单' }}
      </div>
      <div v-for="(order, index) in orderList" :key="order.id" class="order_card"
           :class="{ expanded: expandedIndex === index }">
        <!--可点击展开区域:头部 + 商品信息 + 更多详情-->
        <div class="card_click">
          <!--卡片头部:左边卖家头像名字,右边订单状态-->
          <div class="card_head">
            <div class="seller" v-if="handleRole != 'release'">
              <img class="seller_avatar" :src="order.avatar" alt="头像">
              <div class="seller_info">
                <div class="seller_name">{{ order.name }}</div>
                <div class="seller_role">闲置卖家</div>
              </div>
            </div>
            <span class="status_tag" :class="order.tagClass ">{{statusMap[order.status]}}</span>
          </div>

          <!--卡片中间:商品图片 + 标题 + 价格-->
          <div class="card_body" @click="toggleExpand(index)">
            <!--占位图,接入真实数据后替换为 <img :src="order.goodsImage">-->
            <div class="goods_img">
              <img :src="order.goodsImage" alt="商品图片">
            </div>
            <div class="goods_info">
              <div class="goods_name">{{ order.goodsName }}</div>
              <div class="goods_bottom">
                <div class="goods_price">¥{{ order.price }}</div>
                <div class="detail_hint" v-if="handleRole != 'release'" :class="{ open: expandedIndex === index }">
                  <span>{{ expandedIndex === index ? '收起详情' : '详情' }}</span>
                  <span class="detail_arrow"><el-icon><ArrowDown /></el-icon></span>
                </div>
              </div>
            </div>
          </div>

          <!--点击卡片展开的更多订单信息-->
          <el-collapse-transition>
            <div v-show="expandedIndex === index" class="card_more">
              <div class="more_grid">
                <div class="more_item">
                  <span class="more_label"> 订单编号: </span>
                  <span class="more_value">{{ order.orderNo }}</span>
                </div>
                <div  class="more_item">
                  <span class="more_label"> 收货人姓名: </span>
                  <span class="more_value">{{ order.receiverName }}</span>
                </div>
                <div  class="more_item">
                  <span class="more_label"> 物流单号: </span>
                  <span class="more_value">{{ order.shipNo }}</span>
                </div>
                <div  class="more_item">
                  <span class="more_label"> 收货人手机号: </span>
                  <span class="more_value">{{ order.receiverPhone }}</span>
                </div>
                <div  class="more_item">
                  <span class="more_label"> 物流公司: </span>
                  <span class="more_value">{{ order.shipCompany }}</span>
                </div>
                <div  class="more_item">
                  <span class="more_label"> 收货地址: </span>
                  <span class="more_value">{{ order.receiverAddress }}</span>
                </div>
                <div  class="more_item">
                  <span class="more_label"> 发货时间: </span>
                  <span class="more_value">{{ order.shipTime }}</span>
                </div>
              </div>
            </div>
          </el-collapse-transition>
        </div>

        <!--卡片底部:操作按钮-->
        <div class="card_footer">
          <button class="btn btn_primary" @click="handleGoodsOrderStatus(order.id, order.status)">{{ buttonText(order.status) }}</button>
        </div>
      </div>
    </div>
    <!--右侧面板:订单分类切换 + 分页-->
    <aside class="orders_side">
      <div class="side_block">
        <div class="side_title">订单分类</div>
        <button class="role_btn" :class="{ active: handleRole === 'release' }" :disabled="loading"
                @click="switchRole('release')">
          <el-icon><Goods/></el-icon>
          <span>发布的</span>
        </button>
        <button class="role_btn" :class="{ active: handleRole === 'buy' }" :disabled="loading"
                @click="switchRole('buy')">
          <el-icon><ShoppingCart/></el-icon>
          <span>我买的</span>
        </button>
        <button class="role_btn" :class="{ active: handleRole === 'sell' }" :disabled="loading"
                @click="switchRole('sell')">
          <el-icon><Sell/></el-icon>
          <span>我卖的</span>
        </button>
      </div>

      <!--分页:上一页 / 具体页数选择 / 下一页-->
      <div class="side_block pager_box">
        <div class="pager_info">
          <span>共 <b>{{ total }}</b> 条订单</span>
          <span class="pager_dot">·</span>
          <span>第 <b>{{ pageNum }}</b> / {{ totalPages }} 页</span>
        </div>
        <div class="pager_row">
          <button class="page_btn" :disabled="loading || pageNum <= 1" @click="handlePrevPage">
            <el-icon><ArrowLeft/></el-icon>
            <span>上一页</span>
          </button>
          <el-select class="page_select" :model-value="pageNum" :disabled="loading || totalPages <= 1"
                     @change="handlePageChange">
            <el-option v-for="p in totalPages" :key="p" :label="'第 ' + p + ' 页'" :value="p"/>
          </el-select>
          <button class="page_btn" :disabled="loading || pageNum >= totalPages" @click="handleNextPage">
            <span>下一页</span>
            <el-icon><ArrowRight/></el-icon>
          </button>
        </div>
      </div>
    </aside>
  </div>
</div>
</template>

<style scoped>
/* ===== 页面整体 ===== */
.orders_view{
  display: flex;
  margin: 20px 70px;
  background-color: #ffffff;
  border-radius: 14px;
  box-shadow: 0 6px 20px rgba(31, 86, 141, 0.07);
  min-width: 0;
  min-height: 0;
}
.orders{
  padding: 26px 0;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  display: flex;
}
/* ===== 右侧面板:订单分类 + 分页 ===== */
.orders_side{
  flex: 1;
  min-width: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 6px 26px 0;
  box-sizing: border-box;
}
.side_block{
  width: 100%;
  max-width: 320px;
}
.side_title{
  position: relative;
  display: inline-flex;
  align-items: center;
  padding-left: 12px;
  margin-bottom: 14px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #37474f;
}
.side_title::before{
  content: "";
  position: absolute;
  left: 0;
  top: 2px;
  bottom: 2px;
  width: 4px;
  border-radius: 2px;
  background: linear-gradient(180deg, #4f9ccc, #8fd3f4);
}
/* 分类按钮 */
.role_btn{
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  height: 46px;
  padding: 0 16px;
  box-sizing: border-box;
  background: #ffffff;
  border: 1px solid #e7ecf3;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #5b6b7c;
  cursor: pointer;
  transition: border-color 0.18s ease, background-color 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}
.role_btn + .role_btn{
  margin-top: 12px;
}
.role_btn .el-icon{
  font-size: 17px;
}
.role_btn:not(.active):hover:not(:disabled){
  border-color: #bfd8ea;
  background: #f6fafd;
  color: #4f9ccc;
}
.role_btn.active{
  border-color: transparent;
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  color: #ffffff;
  box-shadow: 0 6px 16px rgba(79, 156, 204, 0.35);
}
.role_btn:disabled{
  cursor: not-allowed;
  opacity: 0.65;
}
/* 分页卡片 */
.pager_box{
  padding: 16px;
  box-sizing: border-box;
  background: #f8fafc;
  border: 1px solid #eef1f6;
  border-radius: 14px;
}
.pager_info{
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 14px;
  font-size: 13px;
  color: #8aa0b4;
}
.pager_info b{
  color: #4f9ccc;
  font-weight: 700;
}
.pager_dot{
  color: #c6d2de;
}
.pager_row{
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.page_btn{
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  height: 32px;
  padding: 0 8px;
  box-sizing: border-box;
  background: #ffffff;
  border: 1px solid #dbe6f0;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  color: #5b6b7c;
  white-space: nowrap;
  cursor: pointer;
  transition: border-color 0.15s ease, color 0.15s ease, background-color 0.15s ease, box-shadow 0.15s ease;
}
.page_btn .el-icon{
  font-size: 14px;
}
.page_btn:hover:not(:disabled){
  border-color: #4f9ccc;
  background: #f4faff;
  color: #4f9ccc;
  box-shadow: 0 3px 8px rgba(79, 156, 204, 0.18);
}
.page_btn:disabled{
  opacity: 0.45;
  cursor: not-allowed;
}
.page_select{
  width: 96px;
  flex-shrink: 0;
}
.page_select :deep(.el-select__wrapper){
  min-height: 32px;
  padding: 0 10px;
  border-radius: 16px;
  box-shadow: 0 0 0 1px #dbe6f0 inset;
  font-size: 13px;
  transition: box-shadow 0.15s ease;
}
.page_select :deep(.el-select__wrapper:hover){
  box-shadow: 0 0 0 1px #b7d4ea inset;
}
.page_select :deep(.el-select__wrapper.is-focused){
  box-shadow: 0 0 0 1px #4f9ccc inset;
}
.page_select :deep(.el-select__selected-item){
  font-weight: 600;
  color: #5b6b7c;
}

/* ===== 订单卡片列表 ===== */
.order_list{
  width: 1200px;
  padding: 0 20px;
  box-sizing: border-box;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  align-content: flex-start;
  gap: 20px;
  border-right: 1px solid #121111;
  overflow-y: scroll;
}
.list_tip{
  width: 100%;
  padding: 90px 0;
  text-align: center;
  font-size: 15px;
  color: #9fb0c1;
}
.order_card{
  width: calc(50% - 10px);
  box-sizing: border-box;
  background: #ffffff;
  border: 1px solid #e7ecf3;
  border-radius: 14px;
  box-shadow: 0 6px 20px rgba(31, 86, 141, 0.20);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.order_card:hover{
  border-color: #d6e4f0;
  box-shadow: 0 12px 28px rgba(31, 86, 141, 0.25);
}
.order_card.expanded{
  border-color: #bfd8ea;
  box-shadow: 0 12px 28px rgba(31, 86, 141, 0.30);
}
.card_click{
  cursor: pointer;
}

/* ===== 卡片头部:卖家 + 状态 ===== */
.card_head{
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 18px;
  border-bottom: 1px solid #f0f3f7;
}
.seller{
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.seller_avatar{
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  user-select: none;
}
.seller_info{
  min-width: 0;
}
.seller_name{
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.seller_role{
  margin-top: 1px;
  font-size: 11px;
  color: #9aa7b5;
}
.status_tag{
  flex-shrink: 0;
  margin-left: auto;
  padding: 4px 13px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}
.tag_pay{
  background: #fdf1e3;
  border-color: #f3ddbb;
  color: #df8a1c;
}
.tag_send{
  background: #eaf3fb;
  border-color: #d6e7f4;
  color: #4f9ccc;
}
.tag_take{
  background: #e9f7ef;
  border-color: #c9e8d8;
  color: #2f9e6e;
}
.tag_done{
  background: #eef1f5;
  border-color: #e0e5ec;
  color: #7c8a99;
}

/* ===== 卡片中间:商品图 + 标题 + 价格 ===== */
.card_body{
  display: flex;
  gap: 16px;
  padding: 16px 18px;
}
.goods_img{
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f5fa, #e3edf5);
  border: 1px solid #e8eef5;
  border-radius: 10px;
  color: #9db6c9;
}
.goods_img img{
  object-fit: cover;
  width: 100%;
  height: 100%;
  border-radius: 10px;
}
.goods_info{
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 2px 0;
}
.goods_name{
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
  color: #2c3e50;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.goods_bottom{
  width: 100%;
  margin-top: auto;
  padding-top: 10px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}
.goods_price{
  font-size: 22px;
  font-weight: 700;
  color: #f0523f;
  letter-spacing: 0.5px;
}
.detail_hint{
  display: flex;
  align-items: center;
  gap: 3px;
  padding-bottom: 3px;
  font-size: 12.5px;
  color: #8aa0b4;
  transition: color 0.15s ease;
}
.detail_hint:hover{
  color: #4f9ccc;
}
.detail_hint.open{
  color: #4f9ccc;
}
.detail_arrow{
  display: inline-flex;
  font-size: 13px;
  transition: transform 0.25s ease;
}
.detail_hint.open .detail_arrow{
  transform: rotate(180deg);
}

/* ===== 展开的更多订单信息 ===== */
.card_more{
  margin: 0 18px 16px;
  padding: 13px 16px;
  background: #f8fafc;
  border: 1px solid #eef1f6;
  border-radius: 10px;
}
.more_grid{
  display: grid;
  gap: 10px 26px;
  grid-template-columns: 1fr 1fr;
}
.more_item{
  display: flex;
  align-items: flex-start;
  min-width: 0;
}

.more_label{
  flex-shrink: 0;
  width: 62px;
  font-size: 13px;
  color: #9aa7b5;
}
.more_value{
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: #45535f;
  word-break: break-all;
}

/* ===== 卡片底部:操作按钮 ===== */
.card_footer{
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  border-top: 1px solid #f0f3f7;
}
.btn{
  height: 34px;
  padding: 0 20px;
  border: none;
  border-radius: 17px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease,
    border-color 0.15s ease, color 0.15s ease, background-color 0.15s ease;
}
.btn_primary{
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  color: #ffffff;
  box-shadow: 0 3px 10px rgba(79, 156, 204, 0.3);
}
.btn_primary:hover{
  filter: brightness(1.05);
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(79, 156, 204, 0.4);
}
/*.btn_plain{
  background: #ffffff;
  border: 1px solid #dbe3ec;
  color: #5b6b7c;
}
.btn_plain:hover{
  background: #f3f8fc;
  border-color: #4f9ccc;
  color: #4f9ccc;
  transform: translateY(-1px);
}*/
</style>
