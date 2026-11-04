/** @type {import('next').NextConfig} */
const nextConfig = {
  // 개발 서버를 도메인으로 열어도 화면 갱신(HMR)이 동작하게 한다.
  allowedDevOrigins: ['resuit.newlecture.com'],
  async rewrites() {
    return [{ source: '/api/:path*', destination: `${process.env.BACKEND_URL || 'http://localhost:8080'}/api/:path*` }];
  }
};

export default nextConfig;
