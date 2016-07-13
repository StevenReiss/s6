package edu.brown.cs.s6.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class UIXMLParser {
    
    private static File file;
    private static String data;
    
    public UIXMLParser () {
        file = null;
        data = "";
    }
    
    public static void main(String[] args) {
        UIXMLParser parser = new UIXMLParser();
        parser.parse(args[0]);
    }
    
    public ArrayList<UIElement> parse(String f) {
        file = new File(f);
        ArrayList<UIElement> ret = new ArrayList<UIElement>();        
        
        getFile();
        readFile();
        
        double height = 0;
        double width = 0; 
        double xpos = 0;
        double ypos = 0;
        String name = "";
        
        for(int i = 0; i < data.length()-20; i++) {
            if(data.substring(i, i+7).equals("<draw:c")){
                height = 0;
                width = 0;
                xpos = 0;
                ypos = 0;
                name = "";
                for(int j = i; j < data.length()-20; j++) {
                    if(data.substring(j, j+5).equals("width")) {
                        int k = j+5;
                        while(!data.substring(k, k+2).equals("cm"))
                            k++;
                        width = Double.valueOf(data.substring(j+7, k));
                    }
                    if(data.substring(j, j+6).equals("height")) {
                        int k = j+6;
                        while(!data.substring(k, k+2).equals("cm"))
                            k++;
                        height  = Double.valueOf(data.substring(j+8, k));
                    }
                    if(data.substring(j, j+5).equals("svg:x")) {
                        int k = j+5;
                        while(!data.substring(k,k+2).equals("cm"))
                            k++;
                        xpos = Double.valueOf(data.substring(j+7, k));
                    }
                    if(data.substring(j, j+5).equals("svg:y")) {
                        int k = j+5;
                        while(!data.substring(k,k+2).equals("cm"))
                            k++;
                        ypos = Double.valueOf(data.substring(j+7, k));
                    }
                    if(data.substring(j, j+9).equals("<text:p t")) {
                        int k = j+9;
                        while(data.charAt(k) != '>')
                            k++;
                        int tmp = k;
                        while(data.charAt(tmp) != '<')
                            tmp++;
                        name = data.substring(k+1, tmp);
                    }
                    if(data.substring(j, j+8).equals("</draw:c"))
                        break;
                }
                /*
                System.err.println("begin parse test");
                System.err.println("width is " + width);
                System.err.println("height is " + height);
                System.err.println("xpos is " + xpos);
                System.err.println("ypos is " + ypos);
                */
                if(name.equals(""))
                    ret.add(new UIElement(width, height, xpos, ypos));
                else
                    ret.add(new UIElement(width, height, xpos, ypos, name));
            }   
        }
        
        //get hierarchy information
        for(int i = 0; i < ret.size(); i++) {
            for(int j = 0; j < ret.size(); j++) {
                if(i != j && ret.get(i).isChild(ret.get(j))) {
                    ret.get(i).addChild(ret.get(j));
                }
            }
        }
        
        //remove children-of-children
        ArrayList<UIElement> children;
        ArrayList<Integer> toDelete = new ArrayList<Integer>();
        for(int i = 0; i < ret.size(); i++) {
            children = ret.get(i).getChildren();
            toDelete.clear();
            for(int j = 0; j < children.size(); j++) {
                for(int k = 0; k < children.size(); k++) {
                    if(j != k && children.get(j).isChild(children.get(k))) {
                        if(!toDelete.contains(k))
                            toDelete.add(k);
                        //ret.get(i).getChildren().remove(children.get(k));
                    }
                }
            }

            Collections.sort(toDelete);

            for(int j = toDelete.size()-1; j >= 0; j--) {
                ret.get(i).getChildren().remove(children.get(toDelete.get(j)));
            }
        }
        
        //debugging - print hierarchy info
        for(int i = 0; i < ret.size(); i++){
            System.err.println("element " + i + " has " + ret.get(i).getChildren().size() + " children"); 
            if(ret.get(i).isPanel())
                System.err.println("element " + i + " is a panel"); 
            else if(ret.get(i).isLabel())
                System.err.println("element " + i + " is a label"); 
            else
                System.err.println("element " + i + " is a textField"); 
        }
        
        return ret;
    }
    
    private static void getFile() {
        //File tmpfile = new File("../../odgunzip/UI5");
        File[] files = file.listFiles();
           
        //System.err.println("begin printing");
        for(int i = 0; i < files.length; i++) {;
            //System.err.println(files[i]);
            if(files[i].toString().contains("content.txt"))
                file = files[i];
        }
        //System.err.println("this is file " + file.toString());
    }
    
    private static void readFile() {
        //File file = new File("content.xml");
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                contents.append(text).append(System.getProperty("line.separator"));
            }
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
        finally {
            try {
                if (reader != null)
                    reader.close();                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        //System.err.println(contents.toString());
        data = contents.toString();
    }
}
