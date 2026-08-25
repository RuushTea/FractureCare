import type { SVGProps } from 'react'

type IconProps = SVGProps<SVGSVGElement>

function Icon({ children, ...props }: IconProps) {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" {...props}>{children}</svg>
}

export const UploadIcon = (props: IconProps) => <Icon {...props}><path d="M12 16V4m0 0L7.5 8.5M12 4l4.5 4.5"/><path d="M5 15v4h14v-4"/></Icon>
export const HistoryIcon = (props: IconProps) => <Icon {...props}><path d="M3.5 12a8.5 8.5 0 1 0 2.1-5.6L3.5 8.5"/><path d="M3.5 4.5v4h4"/><path d="M12 7.5V12l3 2"/></Icon>
export const ShieldIcon = (props: IconProps) => <Icon {...props}><path d="M12 3l7 3v5c0 4.5-2.8 8-7 10-4.2-2-7-5.5-7-10V6l7-3z"/><path d="M9 12l2 2 4-4"/></Icon>
export const FileIcon = (props: IconProps) => <Icon {...props}><path d="M6 3h8l4 4v14H6z"/><path d="M14 3v5h5M9 13h6M9 17h6"/></Icon>
export const ArrowIcon = (props: IconProps) => <Icon {...props}><path d="M5 12h14M14 7l5 5-5 5"/></Icon>
export const CheckIcon = (props: IconProps) => <Icon {...props}><path d="M5 12.5l4 4L19 7"/></Icon>
export const AlertIcon = (props: IconProps) => <Icon {...props}><path d="M12 3l10 18H2L12 3z"/><path d="M12 9v5M12 18h.01"/></Icon>
export const CloseIcon = (props: IconProps) => <Icon {...props}><path d="M6 6l12 12M18 6L6 18"/></Icon>
export const DownloadIcon = (props: IconProps) => <Icon {...props}><path d="M12 4v11m0 0l-4-4m4 4l4-4"/><path d="M5 20h14"/></Icon>
export const EyeIcon = (props: IconProps) => <Icon {...props}><path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z"/><circle cx="12" cy="12" r="2.5"/></Icon>
