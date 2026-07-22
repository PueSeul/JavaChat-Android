package com.dudal.javachat.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dudal.javachat.R;

public final class UiKit {
    private UiKit() {}

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static void prepareWindow(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(activity.getColor(R.color.background));
        window.setNavigationBarColor(activity.getColor(R.color.background));
    }

    public static void applySafeInsets(View view) {
        int left = view.getPaddingLeft();
        int top = view.getPaddingTop();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();
        view.setOnApplyWindowInsetsListener((target, windowInsets) -> {
            int insetLeft;
            int insetTop;
            int insetRight;
            int insetBottom;
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = windowInsets.getInsets(
                        WindowInsets.Type.systemBars()
                                | WindowInsets.Type.displayCutout()
                                | WindowInsets.Type.ime());
                insetLeft = safe.left;
                insetTop = safe.top;
                insetRight = safe.right;
                insetBottom = safe.bottom;
            } else {
                insetLeft = windowInsets.getSystemWindowInsetLeft();
                insetTop = windowInsets.getSystemWindowInsetTop();
                insetRight = windowInsets.getSystemWindowInsetRight();
                insetBottom = windowInsets.getSystemWindowInsetBottom();
            }
            target.setPadding(left + insetLeft, top + insetTop,
                    right + insetRight, bottom + insetBottom);
            return windowInsets;
        });
        if (view.isAttachedToWindow()) {
            view.requestApplyInsets();
        } else {
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View attached) {
                    attached.removeOnAttachStateChangeListener(this);
                    attached.requestApplyInsets();
                }

                @Override
                public void onViewDetachedFromWindow(View detached) {}
            });
        }
    }

    public static LinearLayout vertical(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    public static TextView text(Context context, String value, float sizeSp, int color) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(context.getColor(color));
        view.setLineSpacing(0, 1.08f);
        return view;
    }

    public static TextView title(Context context, String value) {
        TextView view = text(context, value, 24, R.color.text_primary);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    public static TextView sectionTitle(Context context, String value) {
        TextView view = text(context, value, 17, R.color.text_primary);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    public static Button button(Context context, String label, boolean primary) {
        Button button = new Button(context);
        button.setText(label);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setMinHeight(dp(context, 44));
        button.setPadding(dp(context, 16), 0, dp(context, 16), 0);
        button.setTextColor(context.getColor(primary ? R.color.background : R.color.text_primary));
        button.setBackground(rounded(context,
                context.getColor(primary ? R.color.primary : R.color.surface_high), 12));
        return button;
    }

    public static EditText input(Context context, String hint) {
        EditText input = new EditText(context);
        input.setHint(hint);
        input.setHintTextColor(context.getColor(R.color.text_secondary));
        input.setTextColor(context.getColor(R.color.text_primary));
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setPadding(dp(context, 14), dp(context, 2), dp(context, 14), dp(context, 2));
        input.setMinHeight(dp(context, 50));
        input.setBackground(rounded(context, context.getColor(R.color.surface_high), 12));
        return input;
    }

    public static LinearLayout card(Context context) {
        LinearLayout card = vertical(context);
        card.setPadding(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 16));
        card.setBackground(rounded(context, context.getColor(R.color.surface), 16));
        return card;
    }

    public static GradientDrawable rounded(Context context, int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static void margin(View view, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams current = view.getLayoutParams();
        ViewGroup.MarginLayoutParams params;
        if (current instanceof ViewGroup.MarginLayoutParams marginParams) {
            params = marginParams;
        } else {
            params = new LinearLayout.LayoutParams(
                    current == null ? ViewGroup.LayoutParams.MATCH_PARENT : current.width,
                    current == null ? ViewGroup.LayoutParams.WRAP_CONTENT : current.height);
        }
        params.setMargins(dp(view.getContext(), left), dp(view.getContext(), top),
                dp(view.getContext(), right), dp(view.getContext(), bottom));
        view.setLayoutParams(params);
    }

    public static LinearLayout.LayoutParams weight(float weight) {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
    }

    public static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static View divider(Context context) {
        View view = new View(context);
        view.setBackgroundColor(Color.argb(35, 255, 255, 255));
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 1)));
        return view;
    }
}
