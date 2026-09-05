'use client'
import { api } from "@/src/lib/api";
import { useState, useRef } from 'react'
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"
import { Badge } from "@/components/ui/badge"
import { Send, Clipboard, Lock, File, Upload, Download, X, FileText, Share2, Shield, Copy, Check, Loader2 } from 'lucide-react'
import { AlertBox } from './AlertBox'
export function StreamlinedClipboard() {
  const [activeTab, setActiveTab] = useState('share')
  const [contentType, setContentType] = useState<'text' | 'file'>('text')
  const [inputContent, setInputContent] = useState('')
  const [outputContent, setOutputContent] = useState('')
  const [receivedFile, setReceivedFile] = useState<{ name: string; size: number; contentType: string } | null>(null)
  const [shareotp, setShareOtp] = useState('')
  const [shareLink, setShareLink] = useState('')
  const [isLinkCopied, setIsLinkCopied] = useState(false)
  const [enteredotp, setEnteredotp] = useState('')
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [isEncrypted, setIsEncrypted] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [expirationTime, setExpirationTime] = useState('5')
  const [isSharing, setIsSharing] = useState(false)
  const [isReceiving, setIsReceiving] = useState(false)
  const [isDownloading, setIsDownloading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isAlertVisible, setIsAlertVisible] = useState({
    showNullInputAlert: false,
    showOTPAlert: false,
    showBoundExeedAlert: false
  })
  const futureTime = new Date();

  const handleTextSubmit = async () => {
    setErrorMessage(null)

    if (contentType === 'text' && (inputContent === "" || inputContent === null)) {
      setIsAlertVisible({
        showNullInputAlert: true,
        showOTPAlert: false,
        showBoundExeedAlert: false
      })
      return
    }

    if (contentType === 'text' && inputContent.length > 500) {
      setIsAlertVisible({
        showNullInputAlert: false,
        showOTPAlert: false,
        showBoundExeedAlert: true
      })
      return
    }

    if (contentType === 'file') {
      if (!selectedFile) {
        setIsAlertVisible({ showNullInputAlert: true, showOTPAlert: false, showBoundExeedAlert: false })
        return
      }
      if (selectedFile.size > 10 * 1024 * 1024) {
        setIsAlertVisible({ showNullInputAlert: false, showOTPAlert: false, showBoundExeedAlert: true })
        return
      }
    }

    setIsAlertVisible({
      showNullInputAlert: false,
      showOTPAlert: false,
      showBoundExeedAlert: false
    })
    setIsSharing(true)

    try {
      futureTime.setMinutes(futureTime.getMinutes() + Number(expirationTime))
      if (contentType === 'file') {
        const formData = new FormData()
        formData.append("file", selectedFile as Blob)
        formData.append("expiryTime", futureTime.toISOString())
        formData.append("encrypted", String(isEncrypted))
        const response = await api.post("/api/files", formData)
        setShareOtp(response.data.otp)
        setShareLink(`${window.location.origin}/receive?token=${response.data.shareToken}`)
        setIsAlertVisible({
          showNullInputAlert: false,
          showOTPAlert: true,
          showBoundExeedAlert: false
        })
      } else {
        const oneTimePassword = Math.floor(1000 + Math.random() * 9000).toString()
        if (isEncrypted == true) {
          const response = await api.post(`/api/encrypted/save`, {
            "createdUserRid": null,
            "deletedByUser": false,
            "content": inputContent,
            "otp": oneTimePassword,
            "expiryTime": futureTime.toISOString()
          }, {
            headers: { "Content-Type": "application/json" },
          });
          setShareLink(`${window.location.origin}/receive?token=${response.data.shareToken}`)
        } else {
          const response = await api.post(`/api/post/text`, {
            "createdUserRid": null,
            "deletedByUser": false,
            "encryptedContent": inputContent,
            "encryptionKey": null,
            "otp": oneTimePassword,
            "expiryTime": futureTime.toISOString()
          }, {
            headers: { "Content-Type": "application/json" },
          });
          setShareLink(`${window.location.origin}/receive?token=${response.data.shareToken}`)
        }
        setShareOtp(oneTimePassword)
        setIsAlertVisible({
          showNullInputAlert: false,
          showOTPAlert: true,
          showBoundExeedAlert: false
        })
      }

      setInputContent('')
      setSelectedFile(null)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
      setIsEncrypted(false)
      setExpirationTime('5')
    } catch (error: any) {
      console.error("Error sharing content:", error)
      const serverMsg = error?.response?.data?.message || error?.response?.data || error?.message
      setErrorMessage(
        typeof serverMsg === 'string' && serverMsg.length > 0 && serverMsg.length < 200
          ? `Failed to share content: ${serverMsg}`
          : "Failed to share content. Please check your network or try again."
      )
    } finally {
      setIsSharing(false)
    }
  }

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 1) {
      alert('Please select only one file.')
    }
    const file = event.target.files ? event.target.files[0] : null

    setSelectedFile(file)
  }

  const closeAlert = () => {
    setIsAlertVisible({
      showNullInputAlert: false,
      showOTPAlert: false,
      showBoundExeedAlert: false
    })
    setErrorMessage(null)
  }
  const removeFile = () => {
    setSelectedFile(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const handleReceive = async () => {
    setErrorMessage(null)
    let receiveCode = enteredotp.trim()
    if (!receiveCode) {
      setErrorMessage("Please enter an OTP or share link to receive content.")
      return
    }

    try {
      const parsed = new URL(receiveCode)
      const token = parsed.searchParams.get('token')
      if (token) {
        setActiveTab('receive')
        window.location.href = `/receive?token=${encodeURIComponent(token)}`
        return
      }
    } catch { /* The input is an OTP, not a URL. */ }

    setIsReceiving(true)
    try {
      const metadata = await api.get(`/api/get/text/${receiveCode}`)
      if (metadata.data.contentKind === "FILE") {
        setReceivedFile({ name: metadata.data.fileName, size: metadata.data.fileSize, contentType: metadata.data.fileContentType })
        setOutputContent("")
        return metadata.data
      }
      const response = await api.get(`/api/encrypted/retrieve/${receiveCode}`)
      setReceivedFile(null)
      setOutputContent(response.data)
      return response.data
    } catch (error: any) {
      console.error("Error fetching clipboard data:", error);
      const status = error?.response?.status
      if (status === 404 || status === 400) {
        setErrorMessage("Invalid OTP or content not found. Please verify the code.")
      } else if (status === 410) {
        setErrorMessage("This shared content has expired.")
      } else {
        setErrorMessage("Failed to receive content. Please verify your OTP or try again.")
      }
      return null;
    } finally {
      setIsReceiving(false)
    }
  }

  const copyShareLink = async () => {
    if (!shareLink) return
    await navigator.clipboard.writeText(shareLink)
    setIsLinkCopied(true)
    window.setTimeout(() => setIsLinkCopied(false), 2000)
  }

  const shareLinkWithFriends = async () => {
    if (!shareLink) return
    if (navigator.share) {
      await navigator.share({ title: 'Shared clipboard content', url: shareLink }).catch(() => undefined)
    } else {
      await copyShareLink()
    }
  }

  const downloadReceivedFile = async () => {
    if (!receivedFile) return
    setIsDownloading(true)
    try {
      const response = await api.get(`/api/files/${enteredotp}`, { responseType: "blob" })
      const url = URL.createObjectURL(response.data)
      const link = document.createElement("a")
      link.href = url
      link.download = receivedFile.name
      document.body.appendChild(link)
      link.click()
      link.remove()
      URL.revokeObjectURL(url)
    } catch (error) {
      console.error("Error downloading file:", error)
      setErrorMessage("Failed to download received file. Please try again.")
    } finally {
      setIsDownloading(false)
    }
  }

  return (
    <Card className="w-full max-w-3xl mx-auto">
      {errorMessage && (
        <AlertBox
          type="error"
          heading="Error"
          message={errorMessage}
          isVisible={Boolean(errorMessage)}
          onClose={closeAlert}
        />
      )}
      {isAlertVisible.showOTPAlert && shareotp ? (
        <AlertBox
          type="success"
          heading="Successfully Sent Content!"
          message={shareotp}
          isVisible={isAlertVisible.showOTPAlert}
          onClose={closeAlert}
        />
      ) : null}
      {isAlertVisible.showNullInputAlert && (
        <AlertBox
          type="error"
          heading="Empty Content"
          message="Please enter some content to share."
          isVisible={isAlertVisible.showNullInputAlert}
          onClose={closeAlert}
        />
      )}
      {isAlertVisible.showBoundExeedAlert && (
        <AlertBox
          type="error"
          heading="Content length Exceeded"
          message="Content length should not exceed 500 characters."
          isVisible={isAlertVisible.showBoundExeedAlert}
          onClose={closeAlert}
        />
      )}
      <CardHeader>
        <CardTitle>Streamlined Online Clipboard</CardTitle>
        <CardDescription>Securely share text and files with ease</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as 'share' | 'receive')}>
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="share">
              <Share2 className="w-4 h-4 mr-2" />
              Share
            </TabsTrigger>
            <TabsTrigger value="receive">
              <Download className="w-4 h-4 mr-2" />
              Receive
            </TabsTrigger>
          </TabsList>
          <TabsContent value="share">
            <div className="space-y-4 mt-4">
              <div className="flex space-x-2 justify-start">
                <Button
                  variant={contentType === 'text' ? "default" : "outline"}
                  onClick={() => setContentType('text')}
                  className="w-24"
                >
                  <FileText className="w-4 h-4 mr-2" />
                  Text
                </Button>
                <Button
                  variant={contentType === 'file' ? "default" : "outline"}
                  onClick={() => setContentType('file')}
                  className="w-24"
                >
                  <File className="w-4 h-4 mr-2" />
                  File
                </Button>
              </div>
              {/* //input content is the useState variable for the text input */}
              {contentType === 'text' ? (
                <Textarea
                  placeholder="Type or paste your content here..."
                  value={inputContent}
                  onChange={(e) => setInputContent(e.target.value)}
                  className="min-h-[150px]"
                />
              ) : (
                <div className="space-y-2">
                  <Input
                    type="file"
                    onChange={handleFileSelect}
                    accept=".pdf,.txt,.csv,.json,.xml,.jpg,.jpeg,.png,.gif,.webp,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip,.rar,.7z"
                    className='hidden'
                    ref={fileInputRef}
                    id="file-upload"
                  />
                  <Button
                    variant="outline"
                    onClick={() => fileInputRef.current?.click()}
                    className="w-full"
                  >
                    <Upload className="mr-2 h-4 w-4" /> Select File
                  </Button>
                  <p className="text-xs text-gray-500">
                    Accepted formats: PDF, PPT, DOCX, ZIP, etc. Maximum size: 10 MB.
                  </p>
                  {selectedFile && (
                    <div className="flex items-center bg-gray-100 rounded p-2">
                      <File className="h-4 w-4 mr-2" />
                      <span className="text-sm truncate flex-grow">
                        {selectedFile.name} ({(selectedFile.size / 1024 / 1024).toFixed(2)} MB)
                      </span>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={removeFile}
                      >
                        <X className="h-4 w-4" />
                      </Button>
                    </div>
                  )}
                </div>
              )}

              <div className="flex items-center space-x-2">
                <Switch
                  id="encrypt-switch"
                  checked={isEncrypted}
                  onCheckedChange={setIsEncrypted}
                />
                <Label htmlFor="encrypt-switch">Encrypt before storing</Label>
              </div>

              <div className="flex items-center space-x-2">
                <Label htmlFor="expiration-time" className="text-sm text-gray-700">Expire after:</Label>
                <select
                  id="expiration-time"
                  value={expirationTime}
                  onChange={(e) => setExpirationTime(e.target.value)}
                  className="border rounded p-1 text-sm"
                >
                  <option value="5">5 minutes</option>
                  <option value="15">15 minutes</option>
                  <option value="60">1 hour</option>
                  <option value="1440">24 hours</option>
                </select>
              </div>

              <div className="flex justify-end">
                <Button
                  onClick={() => {
                    handleTextSubmit();
                  }}
                  className="w-36"
                  disabled={isSharing}
                >
                  {isSharing ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Sharing...
                    </>
                  ) : (
                    <>
                      <Share2 className="mr-2 h-4 w-4" /> Share
                    </>
                  )}
                </Button>
              </div>

              {shareLink && (
                <div className="space-y-2 rounded-md border bg-gray-50 p-3">
                  <Label htmlFor="share-link">Shareable link</Label>
                  <div className="flex gap-2">
                    <Input id="share-link" value={shareLink} readOnly className="min-w-0" />
                    <Button type="button" variant="outline" size="icon" onClick={copyShareLink} aria-label="Copy shareable link">
                      {isLinkCopied ? <Check className="h-4 w-4 text-green-600" /> : <Copy className="h-4 w-4" />}
                    </Button>
                    <Button type="button" variant="outline" size="icon" onClick={shareLinkWithFriends} aria-label="Share link">
                      <Share2 className="h-4 w-4" />
                    </Button>
                  </div>
                  {isLinkCopied && <p className="text-xs text-green-600">Link copied to clipboard.</p>}
                </div>
              )}
            </div>
          </TabsContent>
          <TabsContent value="receive">
            <div className="space-y-4 mt-4">
              <Input
                type="text"
                placeholder="Enter OTP or share link"
                value={enteredotp}
                onChange={(e) => setEnteredotp(e.target.value)}
                disabled={isReceiving}
              />
              <div className="flex justify-end">
                <Button onClick={handleReceive} className="w-48" disabled={isReceiving}>
                  {isReceiving ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Receiving...
                    </>
                  ) : (
                    <>
                      <Lock className="mr-2 h-4 w-4" /> Verify & Receive
                    </>
                  )}
                </Button>
              </div>
              <div className="relative">
                <Textarea
                  placeholder={
                    isReceiving
                      ? "Receiving content, please wait..."
                      : receivedFile
                      ? `File ready: ${receivedFile.name}`
                      : "Received content will appear here..."
                  }
                  value={outputContent}
                  readOnly
                  className="min-h-[150px]"
                />
                {isReceiving && (
                  <div className="absolute inset-0 bg-white/60 flex items-center justify-center rounded-md">
                    <div className="flex items-center gap-2 text-sm text-gray-600 font-medium">
                      <Loader2 className="h-5 w-5 animate-spin text-blue-600" />
                      Loading content...
                    </div>
                  </div>
                )}
              </div>
              <div className="flex justify-between">
                <Button
                  variant="outline"
                  onClick={() => {
                    if (outputContent) {
                      navigator.clipboard.writeText(outputContent);
                    }
                  }}
                  className="w-40"
                  disabled={!outputContent || isReceiving}
                >
                  <Clipboard className="mr-2 h-4 w-4" /> Copy
                </Button>
                <Button
                  variant="outline"
                  className="w-44"
                  onClick={downloadReceivedFile}
                  disabled={!receivedFile || isDownloading || isReceiving}
                >
                  {isDownloading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Downloading...
                    </>
                  ) : (
                    <>
                      <Download className="mr-2 h-4 w-4" /> Download
                    </>
                  )}
                </Button>
              </div>
            </div>
          </TabsContent>
        </Tabs>
      </CardContent>
      <div className="flex justify-between items-center text-sm text-gray-500 mt-4 px-6 pb-6">
        <div className="flex items-center">
          <Shield className="h-4 w-4 mr-2" />
          Secure and encrypted
        </div>
      </div>
    </Card>
  )
}
