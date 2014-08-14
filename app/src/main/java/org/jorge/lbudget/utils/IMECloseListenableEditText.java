package org.jorge.lbudget.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

@SuppressWarnings("unused") //Constructors are necessary for XML instantiation
public class IMECloseListenableEditText extends EditText {

    private OnEditTextCloseListener onEditTextCloseListener;

    public IMECloseListenableEditText(final Context context) {
        super(context);
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (onEditTextCloseListener != null) {
                    onEditTextCloseListener.onEditTextClose(textView.getText().toString());
                }
                textView.clearFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                return Boolean.FALSE;
            }
        });
    }

    public IMECloseListenableEditText(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (onEditTextCloseListener != null) {
                    onEditTextCloseListener.onEditTextClose(textView.getText().toString());
                }
                textView.clearFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                return Boolean.FALSE;
            }
        });
    }

    public IMECloseListenableEditText(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (onEditTextCloseListener != null) {
                    onEditTextCloseListener.onEditTextClose(textView.getText().toString());
                }
                textView.clearFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                return Boolean.FALSE;
            }
        });
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (event.getKeyCode() == KeyEvent.FLAG_EDITOR_ACTION) || (event.getKeyCode() == KeyEvent.KEYCODE_BACK) && event.getAction() == KeyEvent.ACTION_UP) {
            if (onEditTextCloseListener != null)
                onEditTextCloseListener.onEditTextClose(this.getText().toString());
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextCloseListener(OnEditTextCloseListener listener) {
        onEditTextCloseListener = listener;
    }

    public interface OnEditTextCloseListener {
        public abstract void onEditTextClose(String text);
    }
}
