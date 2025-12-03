'use client'
import axios from "axios";
import { useState, useRef } from 'react'
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"
import { Badge } from "@/components/ui/badge"
import { BACKEND_URL } from "@lib/constants";
import { Send, Clipboard, Lock, File, Upload, Download, X, FileText, Share2, Eye, EyeOff, Shield, RefreshCw } from 'lucide-react'
import { AlertBox } from './AlertBox'
export function StreamlinedClipboard() {
  const [activeTab, setActiveTab] = useState('share')
  const [contentType, setContentType] = useState<'text' | 'file'>('text')
  const [inputContent, setInputContent] = useState('')
  const [outputContent, setOutputContent] = useState('')
  const [shareotp, setShareOtp] = useState('')
  const [enteredotp, setEnteredotp] = useState('')
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [isEncrypted, setIsEncrypted] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [expirationTime, setExpirationTime] = useState('5')
  const [isAlertVisible, setIsAlertVisible] = useState({
    showNullInputAlert: false,
    showOTPAlert: false,
    showBoundExeedAlert: false
  })
  const futureTime = new Date();

  const handleTextSubmit = async () => {

    if (inputContent === "" || inputContent === null) {

      setIsAlertVisible({
        showNullInputAlert: true,
        showOTPAlert: false,
        showBoundExeedAlert: false
      })
    }
    else if (inputContent.length > 500) {
      setIsAlertVisible({
        showNullInputAlert: false,
        showOTPAlert: false,
        showBoundExeedAlert: true
      })
    }
    else {

      setIsAlertVisible({
        showNullInputAlert: false,
        showOTPAlert: true,
        showBoundExeedAlert: false
      })



      const oneTimePassword = Math.floor(1000 + Math.random() * 9000).toString()
      futureTime.setMinutes(futureTime.getMinutes() + Number(expirationTime))
      if (isEncrypted == true) {
        try {
          const response = await axios.post(`${BACKEND_URL}/api/encrypted/save`, {
            "createdUserRid": null,
            "deletedByUser": false,
            "content": inputContent,
            "otp": oneTimePassword,
            "expiryTime": futureTime.toISOString()
          }, {
            headers: { "Content-Type": "application/json" },
          });
          console.log("Clipboard Data:", response.data);
        } catch (error) {
          console.error("Error sending clipboard data:", error);
          throw error;
        }
      } else {
        try {
          const response = await axios.post(`${BACKEND_URL}/api/post/text`, {
            "createdUserRid": null,
            "deletedByUser": false,
            "encryptedContent": inputContent,
            "encryptionKey": null,
            "otp": oneTimePassword,
            "expiryTime": futureTime.toISOString()
          }, {
            headers: { "Content-Type": "application/json" },
          });
          console.log("Clipboard Data:", response.data);
        } catch (error) {
          console.log("error in sending data to backend " + error)
          throw error;
        }
      }

      setShareOtp(oneTimePassword)

    }


    setInputContent('')
    setSelectedFile(null)
    setIsEncrypted(false)
    setExpirationTime('5')

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
  }
  const removeFile = () => {
    setSelectedFile(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const handleReceive = async () => {

    try {
      const response = await axios.get(`${BACKEND_URL}/api/encrypted/retrieve/${enteredotp.toString()}`);
      setOutputContent(response.data)
      return response.data;
    } catch (error) {
      console.error("Error fetching clipboard data:", error);
      return null;
    }


  }




  return (

    <Card className="w-full max-w-3xl mx-auto">
      {isAlertVisible.showOTPAlert && shareotp ? <AlertBox type="success" heading="Successfully Sent Content!" message={shareotp} isVisible={isAlertVisible.showOTPAlert} onClose={closeAlert} /> : null}
      {isAlertVisible.showNullInputAlert && <AlertBox type="error" heading="Empty Content" message="Please enter some content to share." isVisible={isAlertVisible.showNullInputAlert} onClose={closeAlert} />}
      {isAlertVisible.showBoundExeedAlert && <AlertBox type="error" heading="Content length Exceeded" message="Content length should not exceed 500 characters." isVisible={isAlertVisible.showBoundExeedAlert} onClose={closeAlert} />}
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
                  {selectedFile && (
                    <div className="flex items-center bg-gray-100 rounded p-2">
                      <File className="h-4 w-4 mr-2" />
                      <span className="text-sm truncate flex-grow">
                        {selectedFile.name}
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

                <Label htmlFor="encrypt-switch">End-to-end Encryption</Label>
              </div>


              <div className="flex items-center space-x-2">
                <Label htmlFor="expiration-time" className="text-sm text-gray-700">Expire after:</Label>
                <select
                  id="expiration-time"
                  value={expirationTime}
                  onChange={
                    (e) => {

                      setExpirationTime(e.target.value)
                    }

                  }
                  className="border rounded p-1 text-sm"
                >
                  <option value="5">5 minutes</option>
                  <option value="15">15 minutes</option>
                  <option value="60">1 hour</option>
                  <option value="1440">24 hours</option>
                </select>
              </div>

              <div className="flex justify-end">
                <Button onClick={() => {
                  handleTextSubmit();
                }} className="w-32">
                  <Share2 className="mr-2 h-4 w-4" /> Share
                </Button>



              </div>

            </div>
          </TabsContent>
          <TabsContent value="receive">
            <div className="space-y-4 mt-4">
              <Input
                type="text"
                placeholder="Enter OTP or share link"
                value={enteredotp}
                onChange={(e) => setEnteredotp(e.target.value)}
              />
              <div className="flex justify-end">
                <Button onClick={handleReceive} className="w-40">
                  <Lock className="mr-2 h-4 w-4" /> Verify & Receive
                </Button>
              </div>
              <Textarea
                placeholder="Received content will appear here..."
                value={outputContent}
                readOnly
                className="min-h-[150px]"
              />
              <div className="flex justify-between">
                <Button variant="outline" onClick={() => { navigator.clipboard.writeText(outputContent); console.log("copied") }} className="w-40">
                  <Clipboard className="mr-2 h-4 w-4" /> Copy
                </Button>
                <Button variant="outline" className="w-40">
                  <Download className="mr-2 h-4 w-4" /> Download
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

