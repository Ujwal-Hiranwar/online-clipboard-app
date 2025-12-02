import { useState, useEffect } from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { motion } from "framer-motion"
import { Menu } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"

export default function Navbar() {
  const pathname = usePathname()
  const [isOpen, setIsOpen] = useState(false)
  const [username, setUsername] = useState<string | null>(null)

  useEffect(() => {
    const storedUsername = localStorage.getItem("username")
    if (storedUsername) {
      setUsername(storedUsername)
    }
  }, [])

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
          <nav className="sm:hidden  flex gap-4">
            {username ? (
              <>
                <Button variant={pathname === "/history" ? "default" : "outline"} asChild>
                  <Link href="/history">History</Link>
                </Button>
                <span className="text-lg font-medium">{username}</span>
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
                  <Link
                    href="/history"
                    className={`text-lg font-medium ${pathname === "/history" ? "text-primary" : ""}`}
                    onClick={() => setIsOpen(false)}
                  >
                    History
                  </Link>
                  <span className="text-lg font-medium">{username}</span>
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

