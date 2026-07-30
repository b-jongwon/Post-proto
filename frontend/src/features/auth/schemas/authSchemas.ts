import { z } from 'zod'

export const loginSchema = z.object({
  email: z.string().trim().min(1, '이메일을 입력해주세요.').email('올바른 이메일 형식이 아닙니다.'),
  password: z.string().min(1, '비밀번호를 입력해주세요.'),
})

export const signupSchema = z
  .object({
    fullName: z.string().trim().min(2, '이름은 2자 이상이어야 합니다.').max(50, '이름은 50자 이하여야 합니다.'),
    birthYear: z.number().int().min(1900, '출생연도를 확인해주세요.').max(new Date().getFullYear(), '출생연도를 확인해주세요.'),
    nickname: z.string().trim().min(2, '닉네임은 2자 이상이어야 합니다.').max(20, '닉네임은 20자 이하여야 합니다.'),
    email: z.string().trim().min(1, '이메일을 입력해주세요.').email('올바른 이메일 형식이 아닙니다.').max(255, '이메일은 255자 이하여야 합니다.'),
    password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다.').max(64, '비밀번호는 64자 이하여야 합니다.'),
    confirmPassword: z.string().min(1, '비밀번호를 한 번 더 입력해주세요.'),
    emailVerificationToken: z.string().min(1, '이메일 인증을 완료해주세요.'),
    termsAccepted: z.boolean().refine((value) => value, '서비스 이용약관에 동의해주세요.'),
  })
  .refine((values) => values.password === values.confirmPassword, {
    path: ['confirmPassword'],
    message: '비밀번호가 일치하지 않습니다.',
  })

export type LoginFormValues = z.infer<typeof loginSchema>
export type SignupFormValues = z.infer<typeof signupSchema>
