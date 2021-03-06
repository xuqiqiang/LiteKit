package com.xuqiqiang.uikit.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.xuqiqiang.uikit.R;
import com.xuqiqiang.uikit.utils.DisplayUtils;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class BaseDialog extends Dialog {

    private static int mDefaultWidth = 260;
    private static int mDefaultMargin;
    private static int mDefaultDialogLayout = R.layout.custom_dialog;
    private View innerView;

    public BaseDialog(Context context, int theme) {
        super(context, theme);
    }

    public BaseDialog(Context context) {
        super(context);
    }

    public static void setDefaultDialogLayout(@LayoutRes int layoutId) {
        mDefaultDialogLayout = layoutId;
    }

    public static void setDefaultMargin(int defaultMargin) {
        mDefaultMargin = defaultMargin;
    }

    public static void setDefaultWidth(int width) {
        mDefaultWidth = width;
    }

    public static BaseDialog show(Context context, String title, String message) {
        return show(context, title, message, null);
    }

    public static BaseDialog show(Context context, String title, String message,
        final OnDialogListener onPositive) {
        BaseDialog dialog = new BaseDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (onPositive == null) {
                        dialog.cancel();
                    } else if (onPositive.onClick(dialog)) dialog.cancel();
                }
            }).create();
        dialog.show();
        return dialog;
    }

    public static BaseDialog show(Context context, String title, String message,
        final OnDialogListener onPositive, final OnDialogListener onNegative) {
        BaseDialog dialog = new BaseDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (onPositive == null) {
                        dialog.cancel();
                    } else if (onPositive.onClick(dialog)) dialog.cancel();
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (onNegative == null) {
                        dialog.cancel();
                    } else if (onNegative.onClick(dialog)) dialog.cancel();
                }
            }).create();
        dialog.show();
        return dialog;
    }

    public View getInnerView() {
        return innerView;
    }

    public void setInnerView(@NonNull View innerView) {
        this.innerView = innerView;
    }

    public interface OnDialogListener {
        boolean onClick(DialogInterface dialog);
    }

    public interface OnContentViewListener {
        void onCreateView(View view);
    }

    /**
     * Helper class for creating a base dialog
     */
    public static class Builder {
        private final Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private String extraButtonText;
        private int positiveButtonTextColor;
        private int negativeButtonTextColor;
        private boolean cancelable = true;
        private int dialogLayout = mDefaultDialogLayout;
        private View contentView;
        private int contentLayout;
        private OnContentViewListener onContentViewListener;
        private View layout;
        private int width;

        private OnClickListener positiveButtonClickListener,
            negativeButtonClickListener,
            extraButtonClickListener,
            onKeyBackListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the Dialog message from String
         *
         * @param message The message to show
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set the Dialog message from resource
         *
         * @param message The message to show
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        public Builder setDialogView(int layoutId) {
            this.dialogLayout = layoutId;
            return this;
        }

        /**
         * Set a custom content view for the Dialog. If a message is set, the contentView is not
         * added to the Dialog...
         *
         * @param view
         * @return
         */
        public Builder setContentView(View view) {
            this.contentView = view;
            return this;
        }

        /**
         * Set a custom content view for the Dialog. If a message is set, the contentView is not
         * added to the Dialog...
         *
         * @param layoutId
         * @return
         */
        public Builder setContentView(int layoutId) {
            this.contentLayout = layoutId;
            return this;
        }

        public Builder setContentView(int layoutId, OnContentViewListener listener) {
            this.contentLayout = layoutId;
            this.onContentViewListener = listener;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
            OnClickListener listener) {
            this.positiveButtonText = (String) context
                .getText(positiveButtonText);
            if (listener != null) {
                this.positiveButtonClickListener = listener;
            } else {
                this.positiveButtonClickListener = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                };
            }
            return this;
        }

        /**
         * Set the positive button text and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,
            OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            if (listener != null) {
                this.positiveButtonClickListener = listener;
            } else {
                this.positiveButtonClickListener = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                };
            }
            return this;
        }

        public Builder setPositiveButton(int positiveButtonText,
            int positiveButtonTextColor,
            OnClickListener listener) {
            this.positiveButtonTextColor = positiveButtonTextColor;
            return setPositiveButton(positiveButtonText, listener);
        }

        public Builder setPositiveButton(String positiveButtonText,
            int positiveButtonTextColor,
            OnClickListener listener) {
            this.positiveButtonTextColor = positiveButtonTextColor;
            return setPositiveButton(positiveButtonText, listener);
        }

        /**
         * Set the negative button resource and it's listener
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText,
            OnClickListener listener) {
            this.negativeButtonText = (String) context
                .getText(negativeButtonText);
            if (listener != null) {
                this.negativeButtonClickListener = listener;
            } else {
                this.negativeButtonClickListener = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                };
            }
            return this;
        }

        /**
         * Set the negative button text and it's listener
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText,
            OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            if (listener != null) {
                this.negativeButtonClickListener = listener;
            } else {
                this.negativeButtonClickListener = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                };
            }
            return this;
        }

        public Builder setNegativeButton(int negativeButtonText,
            int negativeButtonTextColor,
            OnClickListener listener) {
            this.negativeButtonTextColor = negativeButtonTextColor;
            return setNegativeButton(negativeButtonText, listener);
        }

        public Builder setNegativeButton(String negativeButtonText,
            int negativeButtonTextColor,
            OnClickListener listener) {
            this.negativeButtonTextColor = negativeButtonTextColor;
            return setNegativeButton(negativeButtonText, listener);
        }

        public Builder setExtraButton(int extraButtonText,
            DialogInterface.OnClickListener listener) {
            this.extraButtonText = (String) context.getText(extraButtonText);

            if (listener != null) {
                this.extraButtonClickListener = listener;
            } else {
                this.extraButtonClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                };
            }
            return this;
        }

        public Builder setExtraButton(String extraButtonText,
            DialogInterface.OnClickListener listener) {
            this.extraButtonText = extraButtonText;
            if (listener != null) {
                this.extraButtonClickListener = listener;
            } else {
                this.extraButtonClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                };
            }
            return this;
        }

        public Builder setOnKeyBackListener(
            OnClickListener listener) {
            this.onKeyBackListener = listener;
            return this;
        }

        public Builder setWidth(
            int width) {
            this.width = width;
            return this;
        }

        /**
         * Set the Dialog cancelable
         *
         * @param cancelable
         * @return
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * Get the Dialog View
         *
         * @return
         */
        public View getView() {
            return layout;
        }

        /**
         * Create the custom dialog
         */
        public BaseDialog create() {
            LayoutInflater inflater = LayoutInflater.from(context);
            // instantiate the dialog with the custom Theme
            final BaseDialog dialog = new BaseDialog(context,
                R.style.CustomDialog);
            dialog.setCancelable(cancelable);
            dialog.setCanceledOnTouchOutside(false);

            layout = inflater.inflate(dialogLayout, null);
            TextView tvTitle = layout.findViewById(R.id.tv_title);
            if (!TextUtils.isEmpty(title)) {
                tvTitle.setText(title);
            } else {
                tvTitle.setVisibility(View.GONE);
            }

            if (contentView != null || contentLayout != 0) {
                // if no message set
                // add the contentView to the dialog body
                ViewGroup flContainer = layout.findViewById(R.id.fl_container);
                flContainer.removeAllViews();
                if (contentView != null) {
                    flContainer.addView(contentView, new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                } else {
                    ViewGroup viewGroup = (ViewGroup) inflater.inflate(contentLayout, flContainer);
                    contentView =
                        viewGroup.getChildAt(0);//inflater.inflate(contentLayout, flContainer);
                    //                    contentView = inflater.inflate(contentLayout, flContainer);
                    if (onContentViewListener != null) {
                        onContentViewListener.onCreateView(contentView);
                    }
                }
                dialog.setInnerView(contentView);
            } else {
                TextView tvMessage = layout.findViewById(R.id.tv_message);
                //                tvMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
                tvMessage.setText(message);
            }

            // set the confirm button
            View btOk = layout.findViewById(R.id.bt_ok);
            if (positiveButtonText != null) {
                btOk.setVisibility(View.VISIBLE);
                TextView tvOk = layout.findViewById(R.id.tv_ok);
                if (tvOk == null && btOk instanceof TextView) tvOk = (TextView) btOk;
                if (tvOk != null) {
                    tvOk.setText(positiveButtonText);
                    if (positiveButtonTextColor != 0) {
                        tvOk.setTextColor(positiveButtonTextColor);
                    }
                }
                if (positiveButtonClickListener != null) {
                    btOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            positiveButtonClickListener.onClick(dialog,
                                DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                btOk.setVisibility(View.GONE);
                View divider = layout.findViewById(R.id.divider);
                if (divider != null) divider.setVisibility(View.GONE);
            }
            // set the cancel button
            View btCancel = layout.findViewById(R.id.bt_cancel);
            if (negativeButtonText != null) {
                btCancel.setVisibility(View.VISIBLE);
                TextView tvCancel = layout.findViewById(R.id.tv_cancel);
                if (tvCancel == null && btCancel instanceof TextView) {
                    tvCancel = (TextView) btCancel;
                }
                if (tvCancel != null) {
                    tvCancel.setText(negativeButtonText);
                    if (negativeButtonTextColor != 0) {
                        tvCancel.setTextColor(negativeButtonTextColor);
                    }
                }
                if (negativeButtonClickListener != null) {
                    btCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            negativeButtonClickListener.onClick(dialog,
                                DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                btCancel.setVisibility(View.GONE);
                View divider = layout.findViewById(R.id.divider);
                if (divider != null) divider.setVisibility(View.GONE);
            }
            // set the extra button
            View btExtra = layout.findViewById(R.id.bt_extra);
            if (extraButtonText != null) {
                btExtra.setVisibility(View.VISIBLE);
                View divider = layout.findViewById(R.id.divider_extra);
                if (divider != null) divider.setVisibility(View.VISIBLE);
                TextView tvExtra = layout.findViewById(R.id.tv_extra);
                if (tvExtra == null && btExtra instanceof TextView) tvExtra = (TextView) btExtra;
                if (tvExtra != null) {
                    tvExtra.setText(extraButtonText);
                    //                    if (negativeButtonTextColor != 0)
                    //                        tvCancel.setTextColor(negativeButtonTextColor);
                }
                if (extraButtonClickListener != null) {
                    btExtra.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            extraButtonClickListener.onClick(dialog,
                                DialogInterface.BUTTON_NEUTRAL);
                        }
                    });
                }
            }
            dialog.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (onKeyBackListener != null) {
                            onKeyBackListener.onClick(dialog,
                                DialogInterface.BUTTON_NEGATIVE);
                            return true;
                        }
                    }
                    return false;
                }
            });

            ViewGroup.LayoutParams lp;
            if (mDefaultMargin > 0) {
                ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
                mlp.leftMargin = mDefaultMargin;
                mlp.rightMargin = mDefaultMargin;
                lp = mlp;
            } else {
                lp = new ViewGroup.LayoutParams(
                    width == 0 ? (int) DisplayUtils.dip2px(context, mDefaultWidth) : width,
                    LayoutParams.WRAP_CONTENT);
            }
            dialog.setContentView(layout, lp);
            return dialog;
        }
    }
}
