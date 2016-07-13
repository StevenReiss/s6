package edu.brown.cs.s6.runner;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class RunnerPencilXMLParser {

    private static int SCREENWIDTH;
    private static int SCREENHEIGHT;
    private static double X_THRESHOLD;
    private static double Y_THRESHOLD;
    private static final double THRESHOLD_COEFFICIENT = 0.1;
    private static final int TOPBORDER = 112;
    private static final int LEFTBORDER = 38;
    private static final int BOTTOMBORDER = 112;
    private static final int RIGHTBORDER = 32;

    private String sketchPath = "";

    private HashMap<String, ArrayList<RunnerPencilHierData>> sketches =
	new HashMap<String,ArrayList<RunnerPencilHierData>>();

    private ArrayList<RunnerPencilHierData> elements;

    private static boolean do_debug = false;

    public RunnerPencilXMLParser(String sketchPath) {
	this.sketchPath = sketchPath;
    }

    public void parse() {
	try {

	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser saxParser = factory.newSAXParser();

	    DefaultHandler handler = new DefaultHandler() {

		boolean activity = false;
		boolean metaDataSize = false, metaDataContent = false;
		double screenX, screenY;
		int num_elements = 0;
		RunnerPencilHierData hierData = new RunnerPencilHierData();

		public void startElement(String uri, String localName,String elementName,
					 Attributes attributes) throws SAXException {
		    if (elementName.equals("Property") && attributes.getValue("name").equals("name")){
			activity = true;
			elements = new ArrayList<RunnerPencilHierData>();
			num_elements = 0;
		    } else if (elementName.equals("g")){
			String type = attributes.getValue("p:def");
			if (type != null){
			    String location = attributes.getValue("transform");
			    if(location != null)
				if(type.substring(22).equals("phone")){
				    screenX = Double.parseDouble(location.split(",")[4]);
				    screenY = Double.parseDouble(location.split(",")[5].replace(")", ""));
				    hierData.setId("e" + num_elements);
				    hierData.setX(0);
				    hierData.setY(0);
				    hierData.setTypes("View, FrameLayout, LinearLayout");
				    elements.add(hierData);
				} else {
				    String types[] = type.split(":");
				    if(!types[1].equals("ActionBar")){
					num_elements++;
					if(types.length > 1)	hierData.setTypes(convertType(types[1]));
					hierData.setX(((Double.parseDouble(location.split(",")[4]) - (screenX + LEFTBORDER))));
					hierData.setY(((Double.parseDouble(location.split(",")[5].replace(")", "")) - (screenY + TOPBORDER))));
					hierData.setId("e" + num_elements);
					elements.add(hierData);
				    }
				}
			}
		    }
		    else if (elementName.equals("p:property")){
			if(attributes.getValue("name").equals("box"))   metaDataSize = true;
			if(attributes.getValue("name").equals("textContent") || attributes.getValue("name").equals("label"))
			    metaDataContent = true;
		    }
		    if (hierData.getUserPos().getHeight() == 0 && elementName.equals("image")){
			double width = Double.parseDouble(attributes.getValue("width"));
			double height = Double.parseDouble(attributes.getValue("height"));
			if(hierData.getTypes() != null && hierData.getTypes().equals("View, FrameLayout, LinearLayout")){
			    SCREENWIDTH = (int) (width - (LEFTBORDER + RIGHTBORDER));
			    SCREENHEIGHT = (int) (height - (TOPBORDER + BOTTOMBORDER));
			    X_THRESHOLD = THRESHOLD_COEFFICIENT * SCREENWIDTH;
			    Y_THRESHOLD = THRESHOLD_COEFFICIENT * SCREENHEIGHT;
			    hierData.setWidth(SCREENWIDTH);
			    hierData.setHeight(SCREENHEIGHT);
			    hierData.setScreenWidth(SCREENWIDTH);
			    hierData.setScreenHeight(SCREENHEIGHT);
			} else {
			    hierData.setWidth(width);
			    hierData.setHeight(height);
			}
		    }
	
		}

		public void endElement(String uri, String localName, String elementName) throws SAXException {
		    if(elementName.equals("g")){
			hierData = new RunnerPencilHierData();
		    }
		}

		public void characters(char ch[], int start, int length) throws SAXException {
		    String data = new String(ch, start, length);
		    if(activity){
			sketches.put(data, elements);
			activity = false;
		    }
		    if(metaDataSize){
			String[] size = data.split(",");
			if(size.length > 1){
			    hierData.setWidth(Double.parseDouble(size[0]));
			    hierData.setHeight(Double.parseDouble(size[1]));
			}
			metaDataSize = false;
		    }
		    if(metaDataContent){
			hierData.setUserData(data);
			metaDataContent = false;
		    }
		}

	    };

	    saxParser.parse(sketchPath, handler);

	} catch (Exception e) {
	    e.printStackTrace();
	}

	buildHierarchy();

	if(do_debug)	printSketches();

    }

    private String convertType(String type) {

	if(type.contains("contactPictureIcon"))
	    type = "ImageView";
	else if(type.contains("Tab") || type.contains("PlainText"))
	    type = "TextView";
	else if(type.contains("Checkbox"))
	    type = "CheckBox";
	else if(type.contains("Button") || type.contains("menuIcon"))
	    type = "Button,ImageButton";
	else if(type.contains("Radio"))
	    type = "RadioButton";
	else if(type.contains("Switch"))
	    type = "Switch";
	else if(type.contains("Dropdown") || type.contains("Combo") || type.contains("Spinner"))
	    type = "Spinner";
	else if(type.contains("progressbar") || type.contains("progressscrubbers"))
	    type = "ProgressBar";
	else if(type.contains("ZoomV1"))
	    type = "ZoomButton";
	else if(type.contains("ZoomV2"))
	    type = "ZoomControls";
	else if(type.contains("DatePicker"))
	    type = "DatePicker";
	else if(type.contains("Picker"))
	    type = "NumberPicker";
	else if(type.contains("Toggle"))
	    type = "ToggleButton";
	else if(type.contains("googleSearch"))
	    type = "SimpleSearchText";
	else if(type.contains("TextView") || type.contains("TextFiew"))
	    type = "EditText";

	return type;
    }

    private void buildHierarchy() {
	for(String key : sketches.keySet()){
	    ArrayList<RunnerPencilHierData> elements = sketches.get(key);
	    for(RunnerPencilHierData element:elements){
		for(RunnerPencilHierData element2:elements){
		    if(element != element2 && isChild(element.getUserPos(), element2.getUserPos()))	element.addChild(element2);
		}
	    }

	    for(RunnerPencilHierData element:elements) {
		List<RunnerPencilHierData> childrens = element.getChildren();
		for (Iterator<RunnerPencilHierData> it = childrens.iterator(); it.hasNext(); ) {
		    RunnerPencilHierData e = it.next();
		    boolean del = false;
		    for (RunnerPencilHierData e1 : childrens) {
			if (e1 != e && isChild(e1.getUserPos(),e.getUserPos())) del = true;
		    }
		    if (del) it.remove();
		}
	    }

	    for(RunnerPencilHierData element:elements){
		element.setChildCount(element.getChildren().size());
	    }
	    if(elements.size() > 0)
		findAdjacencies(elements.get(0));
	}
    }

    private void findAdjacencies(RunnerPencilHierData parent) {

	double x0 = parent.getUserPos().getX();
	double x1 = parent.getUserPos().getX() + parent.getUserPos().getWidth();
	double y0 = parent.getUserPos().getY();
	double y1 = parent.getUserPos().getY() + parent.getUserPos().getHeight();

	List<RunnerPencilHierData> root_children = parent.getChildren();
	for(RunnerPencilHierData child:root_children){
	    findAdjacencies(child);
	    double cx0 = child.getUserPos().getX();
	    double cx1 = child.getUserPos().getX() + child.getUserPos().getWidth();
	    double cy0 = child.getUserPos().getY();
	    double cy1 = child.getUserPos().getY() + child.getUserPos().getHeight();
	    if (cx0 - x0 < X_THRESHOLD){
		child.setLeftAnchor(parent.getId());
	    }
	    if (x1 - cx1 < X_THRESHOLD){
		child.setRightAnchor(parent.getId());
	    }
	    if (cy0 - y0 < Y_THRESHOLD){
		child.setTopAnchor(parent.getId());
	    }
	    if (y1 - cy1 < Y_THRESHOLD){
		child.setBottomAnchor(parent.getId());
	    }

	    List<RunnerPencilHierData> lvs = getAllLeaves(parent,null);

	    for (RunnerPencilHierData c : parent.getChildren()) {
		findTopAnchor(c,lvs);
		findBottomAnchor(c,lvs);
		findLeftAnchor(c,lvs);
		findRightAnchor(c,lvs);
	    }
	}

    }

    private List<RunnerPencilHierData> getAllLeaves(RunnerPencilHierData n,List<RunnerPencilHierData> r) {
	if (r == null) r = new ArrayList<RunnerPencilHierData>();

	if (n.hasChildren()) {
	    for (RunnerPencilHierData c : n.getChildren()) {
		getAllLeaves(c,r);
	    }
	}

	return r;
    }

    private void findTopAnchor(RunnerPencilHierData c,List<RunnerPencilHierData> cands)
    {
	if (c.getTopAnchor() != null) return;

	double x0 = c.getUserPos().getX();
	double x1 = x0 + c.getUserPos().getWidth();
	double y0 = c.getUserPos().getY();

	for (RunnerPencilHierData n : cands) {
	    double nx0 = n.getUserPos().getX();
	    double nx1 = nx0 + n.getUserPos().getWidth();
	    double ny0 = n.getUserPos().getY();
	    double ny1 = ny0 + n.getUserPos().getHeight();
	    if (ny1 < y0 && y0 - ny1 < 10) {
		if (x1 > nx0 && x0 < nx1) {
		    c.setTopAnchor(n.getId());
		    break;
		}
	    }
	}
    }

    private void findBottomAnchor(RunnerPencilHierData c,List<RunnerPencilHierData> cands)
    {
	if (c.getBottomAnchor() != null) return;

	double x0 = c.getUserPos().getX();
	double x1 = x0 + c.getUserPos().getWidth();
	double y0 = c.getUserPos().getY();
	double y1 = y0 + c.getUserPos().getHeight();

	for (RunnerPencilHierData n : cands) {
	    double nx0 = n.getUserPos().getX();
	    double nx1 = nx0 + n.getUserPos().getWidth();
	    double ny0 = n.getUserPos().getY();
	    if (ny0 > y1 && ny0 - y1 < 10) {
		if (x1 > nx0 && x0 < nx1) {
		    c.setBottomAnchor(n.getId());
		    break;
		}
	    }
	}
    }



    private void findLeftAnchor(RunnerPencilHierData c,List<RunnerPencilHierData> cands)
    {
	if (c.getLeftAnchor() != null) return;

	double x0 = c.getUserPos().getX();
	double y0 = c.getUserPos().getY();
	double y1 = y0 + c.getUserPos().getHeight();

	for (RunnerPencilHierData n : cands) {
	    double nx0 = n.getUserPos().getX();
	    double nx1 = nx0 + n.getUserPos().getWidth();
	    double ny0 = n.getUserPos().getY();
	    double ny1 = ny0 + n.getUserPos().getHeight();
	    if (nx1 < x0 && x0 - nx1 < 10) {
		if (y1 > ny0 && y0 < ny1) {
		    c.setLeftAnchor(n.getId());
		    break;
		}
	    }
	}
    }



    private void findRightAnchor(RunnerPencilHierData c,List<RunnerPencilHierData> cands)
    {
	if (c.getRightAnchor() != null) return;

	double x0 = c.getUserPos().getX();
	double x1 = x0 + c.getUserPos().getWidth();
	double y0 = c.getUserPos().getY();
	double y1 = y0 + c.getUserPos().getHeight();

	for (RunnerPencilHierData n : cands) {
	    double nx0 = n.getUserPos().getX();
	    double ny0 = n.getUserPos().getY();
	    double ny1 = ny0 + n.getUserPos().getHeight();
	    if (nx0 > x1 && nx0 - x1 < 10) {
		if (y1 > ny0 && y0 < ny1) {
		    c.setRightAnchor(n.getId());
		    break;
		}
	    }
	}
    }

    private boolean isChild(Rectangle2D pos1, Rectangle2D pos2){
	double xpos = pos1.getX();
	double ypos = pos1.getY();
	double width = pos1.getWidth();
	double height = pos1.getHeight();

	double xpos2 = pos2.getX();
	double ypos2 = pos2.getY();
	double width2 = pos2.getWidth();
	double height2 = pos2.getHeight();
	if(xpos2 >= xpos && xpos+width >= xpos2+width2 && ypos2 >= ypos && ypos2+height2 <= ypos+height ) return true;
	else return false;
    }

    private void printSketches() {
	for(String key : sketches.keySet()){
	    System.err.println("key : " + key);
	    ArrayList<RunnerPencilHierData> elements = sketches.get(key);
	    for(RunnerPencilHierData element:elements){
		System.err.println("Id : " + element.getId());
		for(RunnerPencilHierData child:element.getChildren()){
		    System.err.println("child : " + child.getId());
		}
		System.err.println("Screen width : " + element.getScreenWidth());
		System.err.println("Screen height : " + element.getScreenHeight());
		System.err.println("Type : " + element.getTypes());
		System.err.println("Position : " + element.getUserPos());
		System.err.println("Data : " + element.getUserData());
		System.err.println("Children : " + element.getChildCount());
		System.err.println("Top Anchor : " + element.getTopAnchor());
		System.err.println("Bottom Anchor : " + element.getBottomAnchor());
		System.err.println("Left Anchor : " + element.getLeftAnchor());
		System.err.println("Right Anchor : " + element.getRightAnchor());
	    }
	}
    }

    public HashMap<String, ArrayList<RunnerPencilHierData>> getSketchesHierarchy(){
	return sketches;
    }

}
