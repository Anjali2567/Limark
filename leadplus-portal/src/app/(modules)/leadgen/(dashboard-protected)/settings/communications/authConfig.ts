const azureClientId = process.env.NEXT_PUBLIC_AZURE_CLIENT_ID;
const googleClientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;

export const getAzureAuthUrl = () => {
  const redirectUri = encodeURIComponent(window.location.href);
  return (
    `https://login.microsoftonline.com/common/oauth2/v2.0/authorize?` +
    `client_id=${azureClientId}&` +
    `response_type=code&` +
    `redirect_uri=${redirectUri}&` +
    `scope=https://graph.microsoft.com/User.Read https://graph.microsoft.com/Mail.Send offline_access https://graph.microsoft.com/Mail.ReadWrite`
  );
};

export const getGmailAuthUrl = () => {
  const redirectUri = encodeURIComponent(window.location.href);
  return (
    `https://accounts.google.com/o/oauth2/v2/auth?` +
    `client_id=${googleClientId}&` +
    `redirect_uri=${redirectUri}&` +
    `response_type=code&` +
    `scope=https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/gmail.labels https://www.googleapis.com/auth/userinfo.profile&` +
    `access_type=offline&` +
    `prompt=consent`
  );
};
