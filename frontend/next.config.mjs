/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "https://dirty-emlyn-myprojectclipboard-ab2b94ce.koyeb.app/api/:path*",
      },
    ]
  },

}

export default nextConfig
