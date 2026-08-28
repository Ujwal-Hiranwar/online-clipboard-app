'use client'

import { useState, useEffect } from 'react'
import { api } from "@/src/lib/api";
import { useAuth } from '../components/AuthProvider'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { AlertBox } from '../components/AlertBox'
import Link from 'next/link'
import { ArrowLeft } from 'lucide-react'

export default function ProfilePage() {
  const [name, setName] = useState('')
  const [gender, setGender] = useState('')
  const [feedback, setFeedback] = useState<{type: 'success' | 'error', message: string} | null>(null)
  const { user, loading, refreshUser } = useAuth()

  useEffect(() => {
    if (user) { setName(user.name || ''); setGender(user.gender || '') }
  }, [user])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user) {
      setFeedback({ type: 'error', message: 'You must be logged in to update your profile.' })
      return
    }

    try {
      await api.put(`/api/users/profile`, { name, gender })
      await refreshUser()
      setFeedback({ type: 'success', message: 'Profile updated successfully!' })
    } catch (error) {
      setFeedback({ type: 'error', message: 'Failed to update profile. Please try again.' })
      console.error(error)
    }
  }

  const closeAlert = () => {
    setFeedback(null)
  }

  if (loading) return <div className="p-10 text-center">Loading...</div>
  if (!user) return <div className="p-10 text-center">Please log in to edit your profile.</div>
  return (
    <div className="flex flex-col items-center pt-10 min-h-[calc(100vh-8rem)]">
      <div className="w-full max-w-md">
        <Button variant="outline" asChild>
          <Link href="/"><ArrowLeft className="mr-2 h-4 w-4" />Back to clipboard</Link>
        </Button>
      </div>
      <div className="absolute top-20 w-full max-w-md">
        {feedback && <AlertBox type={feedback.type} heading={feedback.type === 'success' ? 'Success' : 'Error'} message={feedback.message} isVisible={true} onClose={closeAlert} />}
      </div>
      <Card className="w-full max-w-md mt-16">
        <CardHeader>
          <CardTitle>Edit Profile</CardTitle>
          <CardDescription>Update your name and gender.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter your name"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="gender">Gender</Label>
              <select
                id="gender"
                value={gender}
                onChange={(e) => setGender(e.target.value)}
                className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <option value="" disabled>Select Gender</option>
                <option value="male">Male</option>
                <option value="female">Female</option>
                <option value="other">Other</option>
                <option value="prefer_not_to_say">Prefer not to say</option>
              </select>
            </div>
            <Button type="submit" className="w-full">
              Save Changes
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
