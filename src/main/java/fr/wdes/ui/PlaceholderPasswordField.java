package fr.wdes.ui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPasswordField;
import javax.swing.text.Document;

@SuppressWarnings("serial")
public class PlaceholderPasswordField extends JPasswordField implements FocusListener {

    private String placeholder;

    public PlaceholderPasswordField() {
        init();
    }

    public PlaceholderPasswordField(
        final Document pDoc,
        final String pText,
        final int pColumns)
    {
        super(pDoc, pText, pColumns);
        init();
    }

    public PlaceholderPasswordField(final int pColumns) {
        super(pColumns);
        init();
    }

    public PlaceholderPasswordField(final String pText) {
        super(pText);
        init();
    }

    public PlaceholderPasswordField(final String pText, final int pColumns) {
        super(pText, pColumns);
        init();
    }

    private void init() {
        StyledFieldUtils.applyBaseStyle(this);
        setBorder(StyledFieldUtils.buildBorder(this, false));
        setEchoChar('\u2022');
        addFocusListener(this);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
        repaint();
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        setBorder(StyledFieldUtils.buildBorder(this, isFocusOwner()));
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        setBorder(StyledFieldUtils.buildBorder(this, isFocusOwner()));
    }

    @Override
    protected void paintComponent(final Graphics pG) {
        StyledFieldUtils.paintBackground(this, pG);
        super.paintComponent(pG);
        if (getPassword().length == 0) {
            StyledFieldUtils.paintPlaceholder(this, pG, placeholder);
        }
    }

    public void focusGained(FocusEvent e) {
        setBorder(StyledFieldUtils.buildBorder(this, true));
        repaint();
    }

    public void focusLost(FocusEvent e) {
        setBorder(StyledFieldUtils.buildBorder(this, false));
        repaint();
    }
}
