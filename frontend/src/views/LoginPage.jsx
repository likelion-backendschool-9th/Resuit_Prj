'use client';

import AuthLayout from '@/components/auth/AuthLayout.jsx';
import AuthForm from '@/components/auth/AuthForm.jsx';

export default function LoginPage() {
  return <AuthLayout mode="login"><AuthForm mode="login" /></AuthLayout>;
}
