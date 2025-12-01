import type { Metadata } from "next";
import { Inter } from "next/font/google";
 import "../../styles/globals.css"

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "SyncPaste – Secure & Instant Online Clipboard",
  description: "SyncPaste is a secure online clipboard that lets you instantly share text and files using a 4-digit OTP. No sign-up required. Try it now!",
  keywords : "syncpaste, ujwal syncpaste,ujwal hiranwar,online clipboard, instant clipboard, text sharing, file sharing, secure clipboard, OTP clipboard, cloud clipboard",
  openGraph:{
    type:"website"
  }
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <head>
      
      <meta name="google-site-verification" content="VzF9jb6rbn0R6Ua_Ig0CxmL0oqqInkdMFdoHYCjhiqQ" />
      </head>
      <body className={inter.className}>
        
        {children}
    
        </body>
    </html>
  );
}
