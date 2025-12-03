'use client'
import { useState, useEffect } from "react"
import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { motion } from "framer-motion"
import { Menu, LogOut, History, User, Edit } from "lucide-react"
import { BACKEND_URL } from "@lib/constants";
import axios from "axios"

import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"

export default function Navbar() {
  const pathname = usePathname()
  const router = useRouter()
  const [isOpen, setIsOpen] = useState(false)
  const [username, setUsername] = useState<string | null>(null)
  const [isProfileOpen, setIsProfileOpen] = useState(false)

  useEffect(() => {
    const storedUsername = localStorage.getItem("username")
    if (storedUsername) {
      setUsername(storedUsername)
    }
  }, [])

  const handleLogout = async () => {
    try {
      await axios.post(`${BACKEND_URL}/api/logout`)
      localStorage.removeItem("username")
      setUsername(null)
      router.push("/login")
    } catch (error) {
      console.error("Logout failed:", error)
      // Handle logout error, maybe show a notification
    }
    setIsOpen(false)
  }

  return (
    <header className="border-b border-border bg-background">
      <div className="flex h-16 items-center justify-between px-4 md:px-6">
        <Link href="/" className="flex items-center gap-2">
          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.5 }}
            className="font-bold text-xl tracking-tight"
          >
            SyncPaste
          </motion.div>
        </Link>

        {/* Desktop Navigation */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <nav className="hidden md:flex items-center gap-4">
            {username ? (
              <>
                <Button variant={pathname === "/history" ? "default" : "outline"} asChild>
                  <Link href="/history" className="flex items-center">
                    <History className="mr-2 h-4 w-4" />
                    History
                  </Link>
                </Button>
                <div
                  className="relative"
                  onMouseEnter={() => setIsProfileOpen(true)}
                  onMouseLeave={() => setIsProfileOpen(false)}
                >
                  <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center cursor-pointer">
                    <User className="h-6 w-6 text-gray-600" />
                  </div>
                  {isProfileOpen && (
                    <div className="absolute top-full right-0 mt-2 w-56 bg-white border rounded-md shadow-lg p-4 z-10">
                      <p className="font-semibold text-center truncate">{username}</p>
                      <hr className="my-2" />
                      <Button variant="ghost" className="w-full justify-start" asChild>
                        <Link href="/profile">
                          <Edit className="mr-2 h-4 w-4" />
                          Edit Profile
                        </Link>
                      </Button>
                      <Button variant="ghost" onClick={handleLogout} className="w-full justify-start text-red-500 hover:text-red-600">
                        <LogOut className="mr-2 h-4 w-4" />
                        Logout
                      </Button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <>
                <Button variant={pathname === "/login" ? "default" : "outline"} asChild>
                  <Link href="/login">Login</Link>
                </Button>
                <Button variant={pathname === "/register" ? "default" : "outline"} asChild>
                  <Link href="/register">Register</Link>
                </Button>
              </>
            )}
          </nav>
        </motion.div>

        <Sheet open={isOpen} onOpenChange={setIsOpen}>
          <SheetTrigger asChild className="md:hidden">
            <Button variant="ghost" size="icon">
              <Menu className="h-6 w-6" />
              <span className="sr-only">Toggle menu</span>
            </Button>
          </SheetTrigger>
          <SheetContent side="right" className="w-[250px] sm:w-[300px]">
            <div className="flex flex-col gap-6 mt-8">
              {username ? (
                <>
                  <div
                    className="relative"
                    onClick={() => setIsProfileOpen(!isProfileOpen)}
                  >
                    <div className="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center cursor-pointer mx-auto">
                      <User className="h-7 w-7 text-gray-600" />
                    </div>
                    {isProfileOpen && (
                       <div className="mt-2 w-full bg-white border rounded-md shadow-lg p-4 z-10">
                       <p className="font-semibold text-center truncate">{username}</p>
                     </div>
                    )}
                  </div>
                  <Link
                    href="/history"
                    className={`text-lg font-medium ${pathname === "/history" ? "text-primary" : ""} flex items-center`}
                    onClick={() => setIsOpen(false)}
                  >
                    <History className="mr-2 h-4 w-4" />
                    History
                  </Link>
                  <Link
                    href="/profile"
                    className={`text-lg font-medium ${pathname === "/profile" ? "text-primary" : ""} flex items-center`}
                    onClick={() => setIsOpen(false)}
                  >
                    <Edit className="mr-2 h-4 w-4" />
                    Edit Profile
                  </Link>
                  <Button variant="outline" onClick={handleLogout}>
                    <LogOut className="mr-2 h-4 w-4" />
                    Logout
                  </Button>
                </>
              ) : (
                <>
                  <Link
                    href="/login"
                    className={`text-lg font-medium ${pathname === "/login" ? "text-primary" : ""}`}
                    onClick={() => setIsOpen(false)}
                  >
                    Login
                  </Link>
                  <Link
                    href="/register"
                    className={`text-lg font-medium ${pathname === "/register" ? "text-primary" : ""}`}
                    onClick={() => setIsOpen(false)}
                  >
                    Register
                  </Link>
                </>
              )}
            </div>
          </SheetContent>
        </Sheet>
      </div>
    </header>
  )
}
