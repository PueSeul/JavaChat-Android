package com.dudal.javachat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import com.dudal.javachat.auth.MicrosoftAuthUriPolicy;
import com.dudal.javachat.ui.UiKit;

/** App-owned browser surface for the existing Microsoft device-code flow. */
@SuppressLint("SetJavaScriptEnabled")
public final class MicrosoftLoginActivity extends Activity {
    private static final String EXTRA_LOGIN_URI = "microsoft_login_uri";
    private static final String EXTRA_SUCCESS = "microsoft_login_success";
    private static final String ACTION_FINISH_LOGIN =
            "com.dudal.javachat.FINISH_MICROSOFT_LOGIN";

    private WebView webView;
    private Object modernBackCallback;
    private boolean receiverRegistered;

    private final BroadcastReceiver loginFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(EXTRA_SUCCESS, false);
            MicrosoftLoginActivity.this.setResult(
                    success ? RESULT_OK : RESULT_FIRST_USER);
            finish();
        }
    };

    public static Intent createIntent(Context context, String loginUri) {
        return new Intent(context, MicrosoftLoginActivity.class)
                .putExtra(EXTRA_LOGIN_URI, loginUri);
    }

    public static void finishLogin(Context context, boolean success) {
        Intent intent = new Intent(ACTION_FINISH_LOGIN)
                .setPackage(context.getPackageName())
                .putExtra(EXTRA_SUCCESS, success);
        context.sendBroadcast(intent, internalPermission(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiKit.prepareWindow(this);
        setResult(RESULT_CANCELED);
        registerLoginReceiver();

        String loginUri = getIntent().getStringExtra(EXTRA_LOGIN_URI);
        if (!MicrosoftAuthUriPolicy.isTrusted(loginUri)) {
            Toast.makeText(this, "Microsoft 로그인 주소를 확인할 수 없습니다.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(buildContent(loginUri));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerModernBackCallback();
        }
    }

    @Override
    @SuppressLint("GestureBackNavigation")
    public void onBackPressed() {
        handleBack();
    }

    private void handleBack() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(loginFinishedReceiver);
            receiverRegistered = false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            unregisterModernBackCallback();
        }
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerLoginReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_FINISH_LOGIN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(loginFinishedReceiver, filter, internalPermission(this),
                    null, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(loginFinishedReceiver, filter, internalPermission(this), null);
        }
        receiverRegistered = true;
    }

    private static String internalPermission(Context context) {
        return context.getPackageName() + ".permission.INTERNAL_LOGIN";
    }

    @TargetApi(33)
    private void registerModernBackCallback() {
        OnBackInvokedCallback callback = this::handleBack;
        modernBackCallback = callback;
        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
    }

    @TargetApi(33)
    private void unregisterModernBackCallback() {
        if (modernBackCallback instanceof OnBackInvokedCallback callback) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(callback);
            modernBackCallback = null;
        }
    }

    private ViewGroup buildContent(String loginUri) {
        LinearLayout root = UiKit.vertical(this);
        root.setBackgroundColor(getColor(R.color.background));
        UiKit.applySafeInsets(root);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 12),
                UiKit.dp(this, 12), UiKit.dp(this, 10));
        TextView title = UiKit.sectionTitle(this, "Microsoft 로그인");
        header.addView(title, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button close = UiKit.button(this, "닫기", false);
        close.setOnClickListener(view -> finish());
        header.addView(close);
        root.addView(header, UiKit.matchWrap());

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        configureWebView(webView);
        root.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        webView.loadUrl(loginUri);
        return root;
    }

    private void configureWebView(WebView view) {
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSafeBrowsingEnabled(true);
        settings.setSaveFormData(false);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(view, true);

        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView,
                                                    WebResourceRequest request) {
                return blockUntrustedNavigation(request.getUrl());
            }

            @Override
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                return blockUntrustedNavigation(Uri.parse(url));
            }
        });
    }

    private boolean blockUntrustedNavigation(Uri uri) {
        if (MicrosoftAuthUriPolicy.isTrusted(uri.toString())) {
            return false;
        }
        Toast.makeText(this, "Microsoft 로그인 외부 주소 이동을 차단했습니다.",
                Toast.LENGTH_SHORT).show();
        return true;
    }
}
