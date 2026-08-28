import axios from "axios"
import { BACKEND_URL } from "./constants"

let accessToken: string | null = null
export const setAccessToken = (token: string | null) => { accessToken = token }

export const api = axios.create({ baseURL: BACKEND_URL, withCredentials: true })
api.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

let refreshPromise: Promise<string | null> | null = null
api.interceptors.response.use(undefined, async (error) => {
  const request = error.config
  if (error.response?.status !== 401 || request?._retried || request?.url?.includes("/api/auth/")) return Promise.reject(error)
  request._retried = true
  refreshPromise ??= axios.post(`${BACKEND_URL}/api/auth/refresh`, {}, { withCredentials: true })
    .then(response => response.data.accessToken as string)
    .catch(() => null)
    .finally(() => { refreshPromise = null })
  const token = await refreshPromise
  if (!token) return Promise.reject(error)
  setAccessToken(token)
  request.headers.Authorization = `Bearer ${token}`
  return api(request)
})
