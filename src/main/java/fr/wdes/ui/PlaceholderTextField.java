package fr.wdes.ui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.text.Document;

@SuppressWarnings("serial")
public class PlaceholderTextField extends JTextField implements FocusListener {

    private String placeholder;

    public PlaceholderTextField() {
        init();
    }

    public PlaceholderTextField(
        final Document pDoc,
        final String pText,
        final int pColumns)
    {
        super(pDoc, pText, pColumns);
        init();
    }

    public PlaceholderTextField(final int pColumns) {
        super(pColumns);
        init();
    }

    public PlaceholderTextField(final String pText) {
        super(pText);
        init();
    }

    public PlaceholderTextField(final String pText, final int pColumns) {
        super(pText, pColumns);
        init();
    }

    private void init() {
        StyledFieldUtils.applyBaseStyle(this);
        setBorder(StyledFieldUtils.buildBorder(this, false));
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
        // Recompute the inner padding now that we know the field height so the
        // text baseline ends up vertically centred for the current font.
        setBorder(StyledFieldUtils.buildBorder(this, isFocusOwner()));
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        // Border depends on font metrics for vertical centring.
        setBorder(StyledFieldUtils.buildBorder(this, isFocusOwner()));
    }

    @Override
    protected void paintComponent(final Graphics pG) {
        StyledFieldUtils.paintBackground(this, pG);
        super.paintComponent(pG);
        if (getText().length() == 0) {
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
