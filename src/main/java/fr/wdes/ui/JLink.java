package fr.wdes.ui;

public class JLink {

    private String nom;
    private String lien;
    private int x;
    private int y;
    private int h;
    private int w;
    private String tooltip;



    public JLink(String Name, String Link,String tooltip,int x,int y,int w,int h) {
        this.nom = Name;
        this.lien = Link;
        this.tooltip = tooltip;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void setName(String Name) {
        this.nom = Name;
    }
    public void setToolTip(String tooltip) {
        this.tooltip = tooltip;
    }
    public void setLink(String newLink) {
        this.lien = newLink;
    }
    public void setxywh(int x,int y,int h,int w) {
        this.x = x;
        this.y = y;
        this.h = h;
        this.w = w;
    }

    public String getName() {
        return nom;
    }
    public int x() {return x;}
    public int y() {return y;}
    public int h() {return h;}
    public int w() {return w;}
    
    public String getLink() {
        return lien;
    }
    public String getToolTip() {
        return tooltip;
    }
    
}
