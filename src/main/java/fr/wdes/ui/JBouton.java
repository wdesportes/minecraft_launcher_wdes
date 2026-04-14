package fr.wdes.ui;

import java.net.URL;

public class JBouton {


    private String action;
    private int x;
    private int y;
    private int h;
    private int w;
    private String tooltip;
    private URL icon;
    private URL hover_icon;


    public JBouton( String action,String tooltip,int x,int y,int w,int h,URL icon,URL hover_icon) {

        this.action = action;
        this.tooltip = tooltip;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.icon = icon;
        this.hover_icon = hover_icon;
    }


    public void setToolTip(String tooltip) {
        this.tooltip = tooltip;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public void setIcon(URL icon) {
        this.icon = icon;
    }
    public void setHoverIcon(URL hover_icon) {
        this.hover_icon = hover_icon;
    }
    public void setxywh(int x,int y,int h,int w) {
        this.x = x;
        this.y = y;
        this.h = h;
        this.w = w;
    }


    public int x() {return x;}
    public int y() {return y;}
    public int h() {return h;}
    public int w() {return w;}
    
    public String getAction() {
        return action;
    }
    public String getToolTip() {
        return tooltip;
    }
    public URL getIcon() {
        return icon;
    }
    public URL getHoverIcon() {
        return hover_icon;
    }
    
    
}
