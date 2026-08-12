import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.response.use(
  response => response.data,
  error => {
    const message = 
      error.response?.data?.message 
      || error.message 
      || 'Something went wrong'
    return Promise.reject(message)
  }
)

export default api
