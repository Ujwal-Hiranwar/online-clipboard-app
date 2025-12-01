import { X } from "lucide-react"
export function AlertBox({ type, heading, message, isVisible, onClose }) {
    if (!isVisible) return null
  
    return (
      <div
        className={`rounded-lg border p-4 shadow-sm transition-all duration-300 ${
          type === "success"
            ? "bg-green-50 border-green-200 text-green-800 dark:bg-green-950 dark:border-green-800 dark:text-green-200"
            : "bg-red-50 border-red-200 text-red-800 dark:bg-red-950 dark:border-red-800 dark:text-red-200"
        }`}
      >
        <div className="flex items-start justify-between">
          <div>
            <h3 className="font-semibold text-lg">{heading}</h3>
            { heading == "Successfully Sent Content!" ? <p className="mt-1 ">The code to recieve is <b>{message}</b></p> :<p className="mt-1 ">{message}</p>}
          
          </div>
          <button
            onClick={onClose}
            className={`p-1 rounded-full ${
              type === "success"
                ? "text-green-700 hover:bg-green-100 dark:text-green-300 dark:hover:bg-green-900"
                : "text-red-700 hover:bg-red-100 dark:text-red-300 dark:hover:bg-red-900"
            }`}
          >
            <X className="h-5 w-5" />
          </button>
        </div>
      </div>
    )
  }