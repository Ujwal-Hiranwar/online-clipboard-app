"use client"
import Navbar from './components/Navbar'
import { motion } from "framer-motion"
import { StreamlinedClipboard } from './components/streamline-clipboard';
export default function page(props) {

  return (
    <>
    <Navbar />
    <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className='pt-6 bg-gray-50'
        >
          <StreamlinedClipboard />
        </motion.div>
    
    </>
  );
}
