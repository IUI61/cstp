import axios from 'axios'
const server = axios.create({
  baseURL: '/api',
  timeout: 10000
})
/*请求拦截器 */
server.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if(token){
        config.headers.Authorization = token// 请求头带上 Token
    }
    return config
})
/*响应拦截器 */
server.interceptors.response.use(response => {
    return response
}, error => {
    if(error.response?.status === 401){
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
    }
    return Promise.reject(error)
})
export default server