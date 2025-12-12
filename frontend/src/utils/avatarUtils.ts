export function getAvatarUrl(avatarUrl: string | null | undefined, name?: string | null): string {
  // Use the avatarUrl from database if it exists
  if (avatarUrl && avatarUrl.trim().length > 0) {
    return avatarUrl;
  }

  // Fall back to generated avatar based on name
  const baseName = name && name.trim().length > 0 ? name : 'Community Member';
  const encoded = encodeURIComponent(baseName);
  return `https://ui-avatars.com/api/?name=${encoded}&background=E2E8F0&color=1F2937&size=256`;
}

