'use client';

import AuthLayout from '@/components/auth/AuthLayout.jsx';
import AuthForm from '@/components/auth/AuthForm.jsx';

export default function SignupPage() {
  return <AuthLayout mode="signup"><AuthForm mode="signup" /></AuthLayout>;
}
