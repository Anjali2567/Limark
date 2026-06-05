interface OAuthPopupOptions {
  authUrl: string;
  windowTitle: string;
  state?: string;
  onConnect: (code: string) => void;
  onStateValidationError?: () => void;
  cleanupAndClose: (popup: Window, checkPopup: NodeJS.Timeout) => void;
}

export const handleOAuthPopup = ({
  authUrl,
  windowTitle,
  state,
  onConnect,
  onStateValidationError,
  cleanupAndClose,
}: OAuthPopupOptions): void => {
  if (state) {
    sessionStorage.setItem("oauth_state", state);
  }

  const popup = window.open(
    authUrl,
    windowTitle,
    "width=500,height=600,scrollbars=yes,resizable=yes,status=yes,location=yes,toolbar=no,menubar=no"
  );

  const checkPopup = setInterval(() => {
    try {
      if (popup?.closed) {
        clearInterval(checkPopup);
        if (state) {
          sessionStorage.removeItem("oauth_state");
        }
        return;
      }

      if (
        popup?.location?.href &&
        popup.location.href.includes(window.location.origin)
      ) {
        const popupUrl = popup.location.href;
        const urlParams = new URLSearchParams(new URL(popupUrl).search);
        const code = urlParams.get("code");
        
        if (state) {
          const returnedState = urlParams.get("state");
          const storedState = sessionStorage.getItem("oauth_state");

          if (returnedState !== storedState) {
            cleanupAndClose(popup, checkPopup);
            console.error("State validation failed - potential CSRF attack");
            onStateValidationError?.();
            return;
          }

          if (code && returnedState === storedState) {
            cleanupAndClose(popup, checkPopup);
            onConnect(code);
          }
        } else {
          // No state validation needed (e.g., Gmail)
          if (code) {
            cleanupAndClose(popup, checkPopup);
            onConnect(code);
          }
        }
      }
    } catch (error) {
      console.error(
        "Cross-origin access error - waiting for redirect:",
        error
      );
    }
  }, 1000);
};
