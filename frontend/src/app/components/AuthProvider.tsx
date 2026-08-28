"use client"

import { createContext, useContext, useEffect, useState } from "react"
import { api, setAccessToken } from "@/src/lib/api"

type User = { id: number; email: string; name: string | null; gender: string | null; role: "USER" | "ADMIN" }
type AuthContextValue = { user: User | null; loading: boolean; login: (email: string, password: string) => Promise<void>; logout: () => Promise<void>; refreshUser: () => Promise<void> }
const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const refreshUser = async () => { const response = await api.get<User>("/api/auth/me"); setUser(response.data) }
  useEffect(() => { (async () => { try { const refresh = await api.post("/api/auth/refresh"); setAccessToken(refresh.data.accessToken); await refreshUser() } catch { setAccessToken(null); setUser(null) } finally { setLoading(false) } })() }, [])
  const login = async (email: string, password: string) => { const response = await api.post("/api/auth/login", { email, password }); setAccessToken(response.data.accessToken); setUser(response.data.user) }
  const logout = async () => { try { await api.post("/api/auth/logout") } finally { setAccessToken(null); setUser(null) } }
  return <AuthContext.Provider value={{ user, loading, login, logout, refreshUser }}>{children}</AuthContext.Provider>
}
export const useAuth = () => { const context = useContext(AuthContext); if (!context) throw new Error("useAuth must be inside AuthProvider"); return context }
