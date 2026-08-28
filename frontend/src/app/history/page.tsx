"use client"

import { useEffect, useState } from "react"
import Navbar from "../components/Navbar"
import Link from "next/link"
import { ArrowLeft } from "lucide-react"
import { Button } from "@/components/ui/button"
import { api } from "@/src/lib/api"
import { useAuth } from "../components/AuthProvider"

interface ClipboardEntry { id: number; otp: string; createdAt: string; expiryTime: string | null; encrypted: boolean; contentKind: "TEXT" | "FILE"; fileName: string | null }

export default function HistoryPage() {
  const [entries, setEntries] = useState<ClipboardEntry[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const { user, loading } = useAuth()

  useEffect(() => {
    const fetchHistory = async () => {
      if (!user) { setIsLoading(false); return }
      try {
        const response = await api.get("/api/clipboards/mine")
        setEntries(response.data)
      } catch (error) {
        console.error("An error occurred while fetching users", error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchHistory()
  }, [user])

  return (
    <div className="flex flex-col min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar />
      <main className="flex mt-16 flex-col items-center  justify-center p-4 ">
        <div className="w-full max-w-2xl mb-4">
          <Button variant="outline" asChild>
            <Link href="/"><ArrowLeft className="mr-2 h-4 w-4" />Back to clipboard</Link>
          </Button>
        </div>
        <h1 className="text-2xl font-bold mb-4">Your shared clipboard history</h1>
        {loading || isLoading ? (
          <p>Loading...</p>
        ) : !user ? <p>Please log in to view your history.</p> : (
          <table className="table-auto w-full max-w-2xl bg-white dark:bg-gray-800 shadow-md rounded-lg">
            <thead>
              <tr className="bg-gray-200 dark:bg-gray-700">
                <th className="px-4 py-2">OTP</th>
                <th className="px-4 py-2">Type</th>
                <th className="px-4 py-2">Created</th>
              </tr>
            </thead>
            <tbody>
              {entries.map((entry) => (
                <tr key={entry.id} className="border-b border-gray-200 dark:border-gray-700">
                  <td className="px-4 py-2">{entry.otp}</td>
                  <td className="px-4 py-2">{entry.contentKind === "FILE" ? entry.fileName || "File" : "Text"}</td>
                  <td className="px-4 py-2">{new Date(entry.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </main>
    </div>
  )
}
