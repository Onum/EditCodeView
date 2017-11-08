package com.bigbangbutton.editcodeview;


import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;

class EditCodeInputConnection extends BaseInputConnection {

    private Editable editable;
    private int textLength;

    EditCodeInputConnection(View targetView, boolean fullEditor, int textLength) {
        super(targetView, fullEditor);
        EditCodeView view = (EditCodeView) targetView;
        this.textLength = textLength;
        this.editable = view.getEditable();
    }

    @Override
    public Editable getEditable() {
        return editable;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() >= KeyEvent.KEYCODE_0
                    && event.getKeyCode() <= KeyEvent.KEYCODE_9) {
                char c = event.getKeyCharacterMap().getNumber(event.getKeyCode());
                commitText(String.valueOf(c), 1);
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                deleteSurroundingText(1, 0);
            }
        }
        return super.sendKeyEvent(event);
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        return editable.length() + text.length() <= textLength
                && super.commitText(text.subSequence(0, 1), newCursorPosition);
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        if (text.length() > textLength) {
            text = text.subSequence(0, textLength);
        }
        return super.setComposingText(text, newCursorPosition);
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        return super.setComposingRegion(start, end);
    }

    @Override
    public boolean finishComposingText() {
        return super.finishComposingText();
    }
}
