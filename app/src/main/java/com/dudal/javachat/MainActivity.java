package com.dudal.javachat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;

import com.dudal.javachat.auth.AuthErrorText;
import com.dudal.javachat.auth.MicrosoftAuthRepository;
import com.dudal.javachat.data.AuthMode;
import com.dudal.javachat.data.ConnectionSettingsRepository;
import com.dudal.javachat.data.SavedServer;
import com.dudal.javachat.data.ServerRepository;
import com.dudal.javachat.service.MinecraftConnectionService;
import com.dudal.javachat.protocol.ProtocolRegistry;
import com.dudal.javachat.status.ServerStatusChecker;
import com.dudal.javachat.status.ServerStatusResult;
import com.dudal.javachat.ui.UiKit;

import net.raphimc.minecraftauth.msa.model.MsaDeviceCode;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("SetTextI18n")
public final class MainActivity extends Activity {
    private ServerRepository servers;
    private ConnectionSettingsRepository connectionSettings;
    private MicrosoftAuthRepository auth;
    private LinearLayout serverList;
    private LinearLayout microsoftFields;
    private LinearLayout offlineFields;
    private TextView modeStatus;
    private TextView accountStatus;
    private Button accountButton;
    private Button onlineModeButton;
    private Button offlineModeButton;
    private EditText offlineNicknameInput;
    private boolean loginInProgress;
    private final ExecutorService statusExecutor = Executors.newFixedThreadPool(3);
    private final AtomicInteger statusGeneration = new AtomicInteger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiKit.prepareWindow(this);
        servers = new ServerRepository(this);
        connectionSettings = new ConnectionSettingsRepository(this);
        auth = new MicrosoftAuthRepository(this);
        setContentView(buildContent());
        requestNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshConnectionMode();
        refreshAccount();
        refreshServers();
    }

    @Override
    protected void onDestroy() {
        statusGeneration.incrementAndGet();
        statusExecutor.shutdownNow();
        auth.close();
        super.onDestroy();
    }

    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(getColor(R.color.background));

        LinearLayout root = UiKit.vertical(this);
        root.setPadding(UiKit.dp(this, 20), UiKit.dp(this, 24),
                UiKit.dp(this, 20), UiKit.dp(this, 36));
        UiKit.applySafeInsets(root);
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = UiKit.title(this, "Java Chat");
        root.addView(title);
        TextView subtitle = UiKit.text(this,
                "Minecraft Java 1.8.9–26.2 · 채팅과 접속자 목록",
                14, R.color.text_secondary);
        UiKit.margin(subtitle, 0, 3, 0, 22);
        root.addView(subtitle);

        LinearLayout connectionCard = UiKit.card(this);
        TextView connectionTitle = UiKit.sectionTitle(this, "접속 방식");
        connectionCard.addView(connectionTitle);
        TextView connectionGuide = UiKit.text(this,
                "여기서 선택한 방식으로 모든 저장 서버에 접속합니다.",
                13, R.color.text_secondary);
        UiKit.margin(connectionGuide, 0, 5, 0, 12);
        connectionCard.addView(connectionGuide);

        LinearLayout modeButtons = new LinearLayout(this);
        onlineModeButton = UiKit.button(this, "온라인", false);
        onlineModeButton.setOnClickListener(view -> selectAuthMode(AuthMode.MICROSOFT));
        modeButtons.addView(onlineModeButton, UiKit.weight(1));
        offlineModeButton = UiKit.button(this, "오프라인", false);
        offlineModeButton.setOnClickListener(view -> selectAuthMode(AuthMode.OFFLINE));
        LinearLayout.LayoutParams offlineModeParams = UiKit.weight(1);
        offlineModeParams.setMarginStart(UiKit.dp(this, 8));
        modeButtons.addView(offlineModeButton, offlineModeParams);
        connectionCard.addView(modeButtons, UiKit.matchWrap());

        modeStatus = UiKit.text(this, "접속 방식 확인 중", 13, R.color.text_secondary);
        UiKit.margin(modeStatus, 2, 9, 2, 0);
        connectionCard.addView(modeStatus);

        microsoftFields = UiKit.vertical(this);
        TextView accountTitle = UiKit.sectionTitle(this, "Microsoft 계정");
        UiKit.margin(accountTitle, 0, 18, 0, 0);
        microsoftFields.addView(accountTitle);
        accountStatus = UiKit.text(this, "확인 중", 14, R.color.text_secondary);
        UiKit.margin(accountStatus, 0, 6, 0, 12);
        microsoftFields.addView(accountStatus);
        accountButton = UiKit.button(this, "Microsoft 로그인", true);
        accountButton.setOnClickListener(view -> handleAccountButton());
        microsoftFields.addView(accountButton, UiKit.matchWrap());
        connectionCard.addView(microsoftFields, UiKit.matchWrap());

        offlineFields = UiKit.vertical(this);
        TextView nicknameTitle = UiKit.sectionTitle(this, "오프라인 닉네임");
        UiKit.margin(nicknameTitle, 0, 18, 0, 8);
        offlineFields.addView(nicknameTitle);
        offlineNicknameInput = UiKit.input(this, "영문, 숫자, 밑줄 3~16자");
        offlineNicknameInput.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        offlineFields.addView(offlineNicknameInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiKit.dp(this, 52)));
        Button saveNickname = UiKit.button(this, "닉네임 저장", true);
        saveNickname.setOnClickListener(view -> saveOfflineNickname(true));
        UiKit.margin(saveNickname, 0, 10, 0, 0);
        offlineFields.addView(saveNickname, UiKit.matchWrap());
        TextView offlineGuide = UiKit.text(this,
                "오프라인 방식은 online-mode=false 서버에서만 사용할 수 있습니다.",
                12, R.color.text_secondary);
        UiKit.margin(offlineGuide, 2, 8, 2, 0);
        offlineFields.addView(offlineGuide);
        connectionCard.addView(offlineFields, UiKit.matchWrap());
        root.addView(connectionCard, UiKit.matchWrap());

        LinearLayout serverHeader = new LinearLayout(this);
        serverHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView serverTitle = UiKit.sectionTitle(this, "저장한 서버");
        serverHeader.addView(serverTitle, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button add = UiKit.button(this, "+ 서버 추가", true);
        add.setOnClickListener(view -> startActivity(new Intent(this, ServerEditorActivity.class)));
        serverHeader.addView(add, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        UiKit.margin(serverHeader, 0, 24, 0, 12);
        root.addView(serverHeader);

        serverList = UiKit.vertical(this);
        root.addView(serverList, UiKit.matchWrap());

        TextView note = UiKit.text(this,
                "Microsoft 로그인 정보는 이 기기의 Android Keystore로 암호화됩니다.",
                12, R.color.text_secondary);
        UiKit.margin(note, 2, 18, 2, 0);
        root.addView(note);
        return scroll;
    }

    private void refreshAccount() {
        if (auth.hasAccount()) {
            loginInProgress = false;
            String name = auth.getAccountName();
            accountStatus.setText(name == null ? "로그인됨" : name + " 계정으로 로그인됨");
            accountStatus.setTextColor(getColor(R.color.primary));
            accountButton.setText("로그아웃");
            accountButton.setEnabled(true);
        } else if (loginInProgress) {
            accountStatus.setText("브라우저에서 Microsoft 인증 완료를 기다리는 중입니다.");
            accountStatus.setTextColor(getColor(R.color.text_secondary));
            accountButton.setText("로그인 진행 중…");
            accountButton.setEnabled(false);
        } else {
            accountStatus.setText("정품 인증 서버 접속에 필요합니다.");
            accountStatus.setTextColor(getColor(R.color.text_secondary));
            accountButton.setText(R.string.microsoft_login);
            accountButton.setEnabled(true);
        }
        updateModeStatus();
    }

    private void refreshConnectionMode() {
        AuthMode mode = connectionSettings.getAuthMode();
        boolean online = mode == AuthMode.MICROSOFT;
        styleModeButton(onlineModeButton, online);
        styleModeButton(offlineModeButton, !online);
        microsoftFields.setVisibility(online ? View.VISIBLE : View.GONE);
        offlineFields.setVisibility(online ? View.GONE : View.VISIBLE);
        String savedNickname = connectionSettings.getOfflineNickname();
        if (!offlineNicknameInput.hasFocus()
                && !savedNickname.equals(offlineNicknameInput.getText().toString())) {
            offlineNicknameInput.setText(savedNickname);
        }
        updateModeStatus();
    }

    private void selectAuthMode(AuthMode mode) {
        connectionSettings.setAuthMode(mode);
        refreshConnectionMode();
        refreshAccount();
        refreshServers();
    }

    private void styleModeButton(Button button, boolean selected) {
        button.setTextColor(getColor(selected ? R.color.background : R.color.text_primary));
        button.setBackground(UiKit.rounded(this,
                getColor(selected ? R.color.primary : R.color.surface_high), 12));
    }

    private void updateModeStatus() {
        if (modeStatus == null) {
            return;
        }
        if (connectionSettings.getAuthMode() == AuthMode.MICROSOFT) {
            String name = auth.getAccountName();
            modeStatus.setText(auth.hasAccount()
                    ? "현재 온라인 · " + (name == null ? "Microsoft 계정" : name)
                    : "현재 온라인 · Microsoft 로그인이 필요합니다.");
            modeStatus.setTextColor(getColor(
                    auth.hasAccount() ? R.color.primary : R.color.danger));
        } else {
            modeStatus.setText("현재 오프라인 · "
                    + connectionSettings.getOfflineNickname());
            modeStatus.setTextColor(getColor(R.color.primary));
        }
    }

    private boolean saveOfflineNickname(boolean showConfirmation) {
        String nickname = offlineNicknameInput.getText().toString().trim();
        if (!ConnectionSettingsRepository.isValidOfflineNickname(nickname)) {
            offlineNicknameInput.setError("영문, 숫자, 밑줄 3~16자로 입력하세요.");
            offlineNicknameInput.requestFocus();
            return false;
        }
        connectionSettings.setOfflineNickname(nickname);
        updateModeStatus();
        refreshServers();
        if (showConfirmation) {
            Toast.makeText(this, "오프라인 닉네임을 저장했습니다.",
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void refreshServers() {
        int generation = statusGeneration.incrementAndGet();
        serverList.removeAllViews();
        List<SavedServer> values = servers.getAll();
        if (values.isEmpty()) {
            TextView empty = UiKit.text(this,
                    "아직 저장한 서버가 없습니다.\n서버 주소를 추가해 주세요.",
                    15, R.color.text_secondary);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, UiKit.dp(this, 34), 0, UiKit.dp(this, 34));
            empty.setBackground(UiKit.rounded(this, getColor(R.color.surface), 16));
            serverList.addView(empty, UiKit.matchWrap());
            return;
        }
        for (SavedServer server : values) {
            serverList.addView(buildServerCard(server, generation));
        }
    }

    private View buildServerCard(SavedServer server, int generation) {
        LinearLayout card = UiKit.card(this);
        TextView name = UiKit.sectionTitle(this, server.getName());
        card.addView(name);
        TextView endpoint = UiKit.text(this,
                server.getHost() + ":" + server.getPort(), 14, R.color.text_secondary);
        UiKit.margin(endpoint, 0, 4, 0, 0);
        card.addView(endpoint);
        TextView status = UiKit.text(this, getString(R.string.server_status_checking),
                13, R.color.text_secondary);
        UiKit.margin(status, 0, 6, 0, 0);
        card.addView(status);
        checkServerStatus(server, status, generation);
        String authLabel = connectionSettings.getAuthMode() == AuthMode.MICROSOFT
                ? "현재 온라인" : "현재 오프라인";
        TextView details = UiKit.text(this,
                ProtocolRegistry.require(server.getVersionId()).getDisplayName()
                        + "  •  " + authLabel, 13, R.color.primary);
        UiKit.margin(details, 0, 5, 0, 14);
        card.addView(details);

        LinearLayout actions = new LinearLayout(this);
        Button connect = UiKit.button(this, "접속", true);
        connect.setOnClickListener(view -> openChat(server));
        actions.addView(connect, UiKit.weight(1));
        Button edit = UiKit.button(this, "편집", false);
        edit.setOnClickListener(view -> {
            Intent intent = new Intent(this, ServerEditorActivity.class);
            intent.putExtra(ServerEditorActivity.EXTRA_SERVER_ID, server.getId());
            startActivity(intent);
        });
        LinearLayout.LayoutParams editParams = UiKit.weight(1);
        editParams.setMarginStart(UiKit.dp(this, 10));
        actions.addView(edit, editParams);
        card.addView(actions, UiKit.matchWrap());

        LinearLayout.LayoutParams cardParams = UiKit.matchWrap();
        cardParams.bottomMargin = UiKit.dp(this, 12);
        card.setLayoutParams(cardParams);
        return card;
    }

    private void checkServerStatus(SavedServer server, TextView view, int generation) {
        statusExecutor.execute(() -> {
            ServerStatusResult result = ServerStatusChecker.query(server);
            runOnUiThread(() -> {
                if (isDestroyed() || generation != statusGeneration.get()
                        || !view.isAttachedToWindow()) {
                    return;
                }
                if (result.isOnline()) {
                    view.setText(getString(R.string.server_status_online,
                            result.getOnlinePlayers(), result.getMaxPlayers(), result.getLatencyMs()));
                    view.setTextColor(getColor(R.color.primary));
                } else {
                    view.setText(R.string.server_status_offline);
                    view.setTextColor(getColor(R.color.danger));
                }
            });
        });
    }

    private void openChat(SavedServer server) {
        if (connectionSettings.getAuthMode() == AuthMode.MICROSOFT
                && !auth.hasAccount()) {
            showError("온라인 로그인 필요",
                    "온라인 방식으로 접속하려면 먼저 Microsoft 로그인을 완료해 주세요.");
            return;
        }
        if (connectionSettings.getAuthMode() == AuthMode.OFFLINE
                && !saveOfflineNickname(false)) {
            return;
        }
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(MinecraftConnectionService.EXTRA_SERVER_ID, server.getId());
        startActivity(intent);
    }

    private void handleAccountButton() {
        if (loginInProgress) {
            return;
        }
        if (auth.hasAccount()) {
            new AlertDialog.Builder(this)
                    .setTitle("Microsoft 로그아웃")
                    .setMessage("이 기기에 암호화 저장된 로그인 정보를 삭제할까요?")
                    .setNegativeButton("취소", null)
                    .setPositiveButton("로그아웃", (dialog, which) -> {
                        auth.logout();
                        refreshAccount();
                    })
                    .show();
            return;
        }

        loginInProgress = true;
        accountButton.setEnabled(false);
        accountButton.setText("로그인 준비 중…");
        accountStatus.setText("Microsoft 로그인 준비 중입니다.");
        auth.login(new MicrosoftAuthRepository.LoginCallback() {
            @Override
            public void onDeviceCode(MsaDeviceCode code) {
                runOnUiThread(() -> {
                    refreshAccount();
                    showDeviceCode(code);
                });
            }

            @Override
            public void onSuccess(String profileName) {
                runOnUiThread(() -> {
                    loginInProgress = false;
                    Toast.makeText(MainActivity.this,
                            profileName + " 로그인 완료", Toast.LENGTH_LONG).show();
                    refreshAccount();
                });
            }

            @Override
            public void onError(Throwable error) {
                runOnUiThread(() -> {
                    loginInProgress = false;
                    refreshAccount();
                    showError("로그인 실패", AuthErrorText.from(error));
                });
            }
        });
    }

    private void showDeviceCode(MsaDeviceCode code) {
        LinearLayout content = UiKit.vertical(this);
        int padding = UiKit.dp(this, 24);
        content.setPadding(padding, UiKit.dp(this, 8), padding, 0);
        TextView guide = UiKit.text(this,
                "브라우저에서 Microsoft 계정으로 로그인한 뒤 아래 코드를 확인하세요.",
                15, R.color.text_primary);
        content.addView(guide);
        TextView codeView = UiKit.title(this, code.getUserCode());
        codeView.setTextIsSelectable(true);
        codeView.setGravity(Gravity.CENTER);
        codeView.setPadding(0, UiKit.dp(this, 22), 0, UiKit.dp(this, 22));
        content.addView(codeView, UiKit.matchWrap());

        new AlertDialog.Builder(this)
                .setTitle("Microsoft 기기 로그인")
                .setView(content)
                .setNeutralButton("코드 복사", (dialog, which) -> {
                    ClipboardManager clipboard = getSystemService(ClipboardManager.class);
                    clipboard.setPrimaryClip(ClipData.newPlainText("Microsoft code", code.getUserCode()));
                })
                .setNegativeButton("닫기", null)
                .setPositiveButton("브라우저 열기", (dialog, which) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(code.getDirectVerificationUri())));
                })
                .show();
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

}
