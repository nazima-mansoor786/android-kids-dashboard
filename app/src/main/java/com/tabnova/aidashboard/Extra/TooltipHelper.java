package com.tabnova.aidashboard.Extra;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Minimal tooltip built on PopupWindow + TextView. Replaces com.github.vihtarb:tooltip.
 * Shown below the anchor, or above it when there is not enough room below.
 */
public final class TooltipHelper {

    public interface OnDismissListener {
        void onDismiss();
    }

    private static final int CORNER_RADIUS_DP = 4;
    private static final int SCREEN_MARGIN_DP = 8;

    private final View anchor;
    private final PopupWindow popupWindow;

    private TooltipHelper(Builder builder) {
        this.anchor = builder.anchor;

        float density = anchor.getResources().getDisplayMetrics().density;

        GradientDrawable background = new GradientDrawable();
        background.setColor(builder.backgroundColor);
        background.setCornerRadius(CORNER_RADIUS_DP * density);

        TextView textView = new TextView(anchor.getContext());
        textView.setText(builder.text);
        textView.setTextColor(builder.textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setPadding(builder.padding, builder.padding, builder.padding, builder.padding);
        textView.setBackground(background);

        popupWindow = new PopupWindow(textView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        // A non-null background is required for outside touches to dismiss the popup.
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(builder.cancelable);
        popupWindow.setTouchable(true);

        if (builder.dismissOnClick) {
            textView.setOnClickListener(v -> dismiss());
        }
        if (builder.onDismissListener != null) {
            final OnDismissListener listener = builder.onDismissListener;
            popupWindow.setOnDismissListener(listener::onDismiss);
        }
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void dismiss() {
        if (!popupWindow.isShowing()) {
            return;
        }
        try {
            popupWindow.dismiss();
        } catch (IllegalArgumentException e) {
            // Window already gone (e.g. activity destroyed).
            e.printStackTrace();
        }
    }

    private void showNow() {
        if (anchor.getWindowToken() == null) {
            return;
        }
        float density = anchor.getResources().getDisplayMetrics().density;
        int margin = Math.round(SCREEN_MARGIN_DP * density);

        Rect frame = new Rect();
        anchor.getWindowVisibleDisplayFrame(frame);

        View content = popupWindow.getContentView();
        int maxWidth = Math.max(0, frame.width() - 2 * margin);
        content.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int width = content.getMeasuredWidth();
        int height = content.getMeasuredHeight();
        popupWindow.setWidth(width);
        popupWindow.setHeight(height);

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int anchorTop = location[1];
        int anchorBottom = anchorTop + anchor.getHeight();

        int x = location[0] + (anchor.getWidth() - width) / 2;
        x = Math.max(frame.left + margin, Math.min(x, frame.right - margin - width));

        int y;
        if (anchorBottom + height <= frame.bottom) {
            y = anchorBottom;
        } else {
            y = Math.max(frame.top, anchorTop - height);
        }

        try {
            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
        } catch (RuntimeException e) {
            // Anchor window not usable (e.g. activity finishing).
            e.printStackTrace();
        }
    }

    public static final class Builder {
        private final View anchor;
        private CharSequence text = "";
        private int textColor = Color.WHITE;
        private int backgroundColor = Color.DKGRAY;
        private int padding;
        private boolean cancelable;
        private boolean dismissOnClick;
        private OnDismissListener onDismissListener;

        public Builder(View anchor) {
            this.anchor = anchor;
            this.padding = Math.round(8 * anchor.getResources().getDisplayMetrics().density);
        }

        public Builder setText(CharSequence text) {
            this.text = text;
            return this;
        }

        public Builder setTextColor(int color) {
            this.textColor = color;
            return this;
        }

        public Builder setBackgroundColor(int color) {
            this.backgroundColor = color;
            return this;
        }

        /** Padding in pixels, matching the old library's setPadding(int). */
        public Builder setPadding(int paddingPx) {
            this.padding = paddingPx;
            return this;
        }

        /** When true, touching outside the tooltip dismisses it. */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /** When true, tapping the tooltip dismisses it. */
        public Builder setDismissOnClick(boolean dismissOnClick) {
            this.dismissOnClick = dismissOnClick;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener listener) {
            this.onDismissListener = listener;
            return this;
        }

        public TooltipHelper build() {
            return new TooltipHelper(this);
        }

        /** Builds and shows the tooltip; waits for layout if the anchor is not attached yet. */
        public TooltipHelper show() {
            TooltipHelper tooltip = build();
            if (anchor.getWindowToken() != null && anchor.getWidth() > 0) {
                tooltip.showNow();
            } else {
                anchor.post(tooltip::showNow);
            }
            return tooltip;
        }
    }
}
