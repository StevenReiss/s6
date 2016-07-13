package edu.brown.cs.s6.engine;

import java.util.ArrayList;

public class UIElement {
    
    private double width;
    private double height;
    private double xpos;
    private double ypos;
    private String name;
    private boolean label;
    private ArrayList<UIElement> children;
    
    public UIElement(double wid, double hgt, double x, double y) {
        System.err.println("not a label");
        width = wid;
        height = hgt;
        xpos = x;
        ypos = y;
        label = false;
        children = new ArrayList<UIElement>();
    }
    
    public UIElement(double wid, double hgt, double x, double y, String n) {
        System.err.println("got a label");
        width = wid;
        height = hgt;
        xpos = x;
        ypos = y;
        name = n;
        label = true;
        children = new ArrayList<UIElement>();
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public double getXPos() {
        return xpos;
    }
    
    public double getYPos() {
        return ypos;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isLabel() {
        if(isPanel())
            return false;
        else
            return label;
    }
    
    public boolean isPanel() {
        if(children.size() != 0)
            return true;
        else
            return false;
    }
    
    //tests whether the given UIElement e is a child of this,
    //ie whether it is entirely contained by this element.
    public boolean isChild(UIElement e) {
        if(xpos <= e.getXPos() && xpos+width  >= e.getXPos()+e.getWidth() &&
           ypos <= e.getYPos() && ypos+height >= e.getYPos()+e.getHeight()) {
            //System.err.println("true");=
            return true;
        }
        else {
            //System.err.println("false");
            return false;
        }
    }
    
    public void addChild(UIElement e) {
        children.add(e);
    }
    
    public ArrayList<UIElement> getChildren() {
        return children;
    }
}

