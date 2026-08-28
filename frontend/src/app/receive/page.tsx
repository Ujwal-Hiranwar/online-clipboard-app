"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import Navbar from "../components/Navbar"
import { api } from "@/src/lib/api"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Textarea } from "@/components/ui/textarea"
import { ArrowLeft, Clipboard, Download, File } from "lucide-react"

type SharedContent = {
  content: string | null
  contentKind: "TEXT" | "FILE"
  fileName: string | null
  fileContentType: string | null
  fileSize: number | null
}

export default function ReceivePage() {
  const [content, setContent] = useState<SharedContent | null>(null)
  const [error, setError] = useState("")
  const [copied, setCopied] = useState(false)

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get("token")
    if (!token) { setError("This share link is missing its token."); return }
    api.get(`/api/share/${encodeURIComponent(token)}`)
      .then(response => setContent(response.data))
      .catch(responseError => setError(responseError.response?.status === 410 ? "This share link has expired." : "This share link is invalid or unavailable."))
  }, [])

  const copyText = async () => {
    if (!content?.content) return
    await navigator.clipboard.writeText(content.content)
    setCopied(true)
    window.setTimeout(() => setCopied(false), 2000)
  }

  const downloadFile = async () => {
    if (!content?.fileName) return
    const token = new URLSearchParams(window.location.search).get("token")
    const response = await api.get(`/api/share/${encodeURIComponent(token || "")}/download`, { responseType: "blob" })
    const url = URL.createObjectURL(response.data)
    const link = document.createElement("a")
    link.href = url
    link.download = content.fileName
    link.click()
    URL.revokeObjectURL(url)
  }

  return <><Navbar /><main className="min-h-screen bg-gray-50 px-4 pt-20">
    <div className="mx-auto mb-4 w-full max-w-3xl">
      <Button variant="outline" asChild>
        <Link href="/"><ArrowLeft className="mr-2 h-4 w-4" />Back to home</Link>
      </Button>
    </div>
    <Card className="mx-auto w-full max-w-3xl">
      <CardHeader><CardTitle>Shared clipboard content</CardTitle><CardDescription>Content shared securely with you</CardDescription></CardHeader>
      <CardContent>
        {error ? <p className="rounded-md bg-red-50 p-4 text-red-700">{error}</p> : !content ? <p>Loading shared content...</p> : content.contentKind === "TEXT" ? <div className="space-y-3">
          <Textarea value={content.content || ""} readOnly className="min-h-[180px]" />
          <Button variant="outline" onClick={copyText}><Clipboard className="mr-2 h-4 w-4" />{copied ? "Copied" : "Copy text"}</Button>
        </div> : <div className="flex items-center justify-between rounded-md border p-4">
          <div className="flex min-w-0 items-center gap-3"><File className="h-5 w-5" /><div className="min-w-0"><p className="truncate font-medium">{content.fileName}</p><p className="text-sm text-gray-500">{content.fileSize ? `${(content.fileSize / 1024 / 1024).toFixed(2)} MB` : "Shared file"}</p></div></div>
          <Button onClick={downloadFile}><Download className="mr-2 h-4 w-4" />Download</Button>
        </div>}
      </CardContent>
    </Card>
  </main></>
}
