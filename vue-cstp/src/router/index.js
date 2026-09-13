import {createRouter,createWebHistory} from 'vue-router'
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue')
    },
      {
      path: '/issuanceGoods',
      name: 'issuanceGoods',
      component: () => import('@/views/IssuanceGoods.vue')
      },
      {
          path: '/goodsDetail/:id',
          name: 'goodsDetail',
          component: () => import('@/views/GoodsDetail.vue')
      },
      {
          path: '/chat/:id?',
          name: 'chat',
          component: () => import('@/views/ChatView.vue')
      },
      {
          path: '/orders',
          name: 'orders',
          component: () => import('@/views/OrdersView.vue')
      },
  ]
})
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')

    const publicPages = ['/', '/login']

    if (!token && !publicPages.includes(to.path)) {
        next('/login')
    } else {
        next()
    }
})

export default router