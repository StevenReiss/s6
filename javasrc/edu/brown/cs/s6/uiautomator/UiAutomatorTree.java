/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.brown.cs.s6.uiautomator;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Rectangle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;



public class UiAutomatorTree {



/********************************************************************************/
/*										*/
/*	Class for key-value pairs						*/
/*										*/
/********************************************************************************/

public static class AttributePair {
    public String key, value;

    public AttributePair(String key, String value) {
	this.key = key;
	this.value = value;
    }

}	// end of inner class AttributePair



/********************************************************************************/
/*										*/
/*	Class for basic tree nodes						*/
/*										*/
/********************************************************************************/

public static class BasicTreeNode {

    private static final BasicTreeNode[] CHILDREN_TEMPLATE = new BasicTreeNode[] {};

    protected BasicTreeNode mParent;

    protected final List<BasicTreeNode> mChildren = new ArrayList<BasicTreeNode>();

    public int x, y, width, height;

    // whether the boundary fields are applicable for the node or not
    // RootWindowNode has no bounds, but UiNodes should
    protected boolean mHasBounds = false;

    public void addChild(BasicTreeNode child) {
	if (child == null) {
	    throw new NullPointerException("Cannot add null child");
	}
	if (mChildren.contains(child)) {
	    throw new IllegalArgumentException("node already a child");
	}
	mChildren.add(child);
	child.mParent = this;
    }

    public List<BasicTreeNode> getChildrenList() {
	return Collections.unmodifiableList(mChildren);
    }

    public BasicTreeNode[] getChildren() {
	return mChildren.toArray(CHILDREN_TEMPLATE);
    }

    public BasicTreeNode getParent() {
	return mParent;
    }

    public boolean hasChild() {
	return mChildren.size() != 0;
    }

    public int getChildCount() {
	return mChildren.size();
    }

    public void clearAllChildren() {
	for (BasicTreeNode child : mChildren) {
	    child.clearAllChildren();
	}
	mChildren.clear();
    }

    /**
     *
     * Find nodes in the tree containing the coordinate
     *
     * The found node should have bounds covering the coordinate, and none of its children's
     * bounds covers it. Depending on the layout, some app may have multiple nodes matching it,
     * the caller must provide a {@link IFindNodeListener} to receive all found nodes
     *
     * @param px
     * @param py
     * @return
     */
    public boolean findLeafMostNodesAtPoint(int px, int py, IFindNodeListener listener) {
	boolean foundInChild = false;
	for (BasicTreeNode node : mChildren) {
	    foundInChild |= node.findLeafMostNodesAtPoint(px, py, listener);
	}
	// checked all children, if at least one child covers the point, return directly
	if (foundInChild) return true;
	// check self if the node has no children, or no child nodes covers the point
	if (mHasBounds) {
	    if (x <= px && px <= x + width && y <= py && py <= y + height) {
		listener.onFoundNode(this);
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	}
    }

    public Object[] getAttributesArray () {
	return null;
    };

    public static interface IFindNodeListener {
	void onFoundNode(BasicTreeNode node);
    }

}	// end of inner class BasicTreeNode



/********************************************************************************/
/*										*/
/*	Class for a tree node content provider					*/
/*										*/
/********************************************************************************/

public static class BasicTreeNodeContentProvider implements ITreeContentProvider {

    private static final Object[] EMPTY_ARRAY = {};

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
	return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
	if (parentElement instanceof BasicTreeNode) {
	    return ((BasicTreeNode)parentElement).getChildren();
	}
	return EMPTY_ARRAY;
    }

    @Override
    public Object getParent(Object element) {
	if (element instanceof BasicTreeNode) {
	    return ((BasicTreeNode)element).getParent();
	}
	return null;
    }

    @Override
    public boolean hasChildren(Object element) {
	if (element instanceof BasicTreeNode) {
	    return ((BasicTreeNode) element).hasChild();
	}
	return false;
    }

}	// end of inner class BasicTreeNodeContentProvider




/********************************************************************************/
/*										*/
/*	Class for the root window						*/
/*										*/
/********************************************************************************/

public static class RootWindowNode extends UiNode {

    private final String mWindowName;
    private Object[] mCachedAttributesArray;
    private int mRotation;

    public RootWindowNode(String windowName) {
	this(windowName, 0);
    }

    public RootWindowNode(String windowName, int rotation) {
	mWindowName = windowName;
	mRotation = rotation;
    }

    @Override
    public String toString() {
	return mWindowName;
    }

    @Override
    public Object[] getAttributesArray() {
	if (mCachedAttributesArray == null) {
	    mCachedAttributesArray = new Object[]{new AttributePair("window-name", mWindowName)};
	}
	return mCachedAttributesArray;
    }

    public int getRotation() {
	return mRotation;
    }

}	// end of inner class RootWindowNode




/********************************************************************************/
/*										*/
/*	Class for the XML hierarchy loader					*/
/*										*/
/********************************************************************************/

public static class UiHierarchyXmlLoader {

    private UiNode mRootNode;
    private List<Rectangle> mNafNodes;
    private List<BasicTreeNode> mNodeList;
    public UiHierarchyXmlLoader() {
    }

    /**
     * Uses a SAX parser to process XML dump
     * @param xmlPath
     * @return
     */
    public BasicTreeNode parseXml(String xmlPath) {
	mRootNode = null;
	mNafNodes = new ArrayList<Rectangle>();
	mNodeList = new ArrayList<BasicTreeNode>();
	// standard boilerplate to get a SAX parser
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser parser = null;
	try {
	    parser = factory.newSAXParser();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	    return null;
	} catch (SAXException e) {
	    e.printStackTrace();
	    return null;
	}
	// handler class for SAX parser to receiver standard parsing events:
	// e.g. on reading "<foo>", startElement is called, on reading "</foo>",
	// endElement is called
	DefaultHandler handler = new DefaultHandler(){
	    UiNode mParentNode;
	    UiNode mWorkingNode;
	    @Override
	    public void startElement(String uri, String localName, String qName,
		    Attributes attributes) throws SAXException {
		boolean nodeCreated = false;
		// starting an element implies that the element that has not yet been closed
		// will be the parent of the element that is being started here
		mParentNode = mWorkingNode;
		if ("hierarchy".equals(qName)) {
		    int rotation = 0;
		    for (int i = 0; i < attributes.getLength(); i++) {
			if ("rotation".equals(attributes.getQName(i))) {
			    try {
				rotation = Integer.parseInt(attributes.getValue(i));
			    } catch (NumberFormatException nfe) {
				// do nothing
			    }
			}
		    }
		    mWorkingNode = new RootWindowNode(attributes.getValue("windowName"), rotation);
		    nodeCreated = true;
		} else if ("node".equals(qName)) {
		    UiNode tmpNode = new UiNode();
		    for (int i = 0; i < attributes.getLength(); i++) {
			tmpNode.addAtrribute(attributes.getQName(i), attributes.getValue(i));
		    }
		    mWorkingNode = tmpNode;
		    nodeCreated = true;
		    // check if current node is NAF
		    String naf = tmpNode.getAttribute("NAF");
		    if ("true".equals(naf)) {
			mNafNodes.add(new Rectangle(tmpNode.x, tmpNode.y,
				tmpNode.width, tmpNode.height));
		    }
		}
		// nodeCreated will be false if the element started is neither
		// "hierarchy" nor "node"
		if (nodeCreated) {
		    if (mRootNode == null) {
			// this will only happen once
			mRootNode = mWorkingNode;
		    }
		    if (mParentNode != null) {
			mParentNode.addChild(mWorkingNode);
			mNodeList.add(mWorkingNode);
		    }
		}
	    }

	    @Override
	    public void endElement(String uri, String localName, String qName) throws SAXException {
		//mParentNode should never be null here in a well formed XML
		if (mParentNode != null) {
		    // closing an element implies that we are back to working on
		    // the parent node of the element just closed, i.e. continue to
		    // parse more child nodes
		    mWorkingNode = mParentNode;
		    mParentNode = (UiNode)mParentNode.getParent();
		}
	    }
	};
	try {
	    parser.parse(new File(xmlPath), handler);
	} catch (SAXException e) {
	    e.printStackTrace();
	    return null;
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
	return mRootNode;
    }

    /**
     * Returns the list of "Not Accessibility Friendly" nodes found during parsing.
     *
     * Call this function after parsing
     *
     * @return
     */
    public List<Rectangle> getNafNodes() {
	return Collections.unmodifiableList(mNafNodes);
    }

    public List<BasicTreeNode> getAllNodes(){
	return mNodeList;
    }

}	// end of inner class UiHierarchyXMlLoader



/********************************************************************************/
/*										*/
/*	Class for a UI Node							*/
/*										*/
/********************************************************************************/

public static class UiNode extends BasicTreeNode {
    private static final Pattern BOUNDS_PATTERN = Pattern
	    .compile("\\[-?(\\d+),-?(\\d+)\\]\\[-?(\\d+),-?(\\d+)\\]");
    // use LinkedHashMap to preserve the order of the attributes
    private final Map<String, String> mAttributes = new LinkedHashMap<String, String>();
    private String mDisplayName = "DummyComponent";
    private Object[] mCachedAttributesArray;
    private int screenWidth, screenHeight;

    public void addAtrribute(String key, String value) {
	mAttributes.put(key, value);
	updateDisplayName();
	if ("bounds".equals(key)) {
	    updateBounds(value);
	}
    }

    public Map<String, String> getAttributes() {
	return Collections.unmodifiableMap(mAttributes);
    }

    /**
     * Builds the display name based on attributes of the node
     */
    private void updateDisplayName() {
	String className = mAttributes.get("class");
	if (className == null){
	    //System.out.println("className" + this);
		return;
	}
	String text = mAttributes.get("text");
	if (text == null){
		//System.out.println("text" + this);
	    return;
	}
	String contentDescription = mAttributes.get("content-desc");
	if (contentDescription == null){
		//System.out.println("contentDescription" + this);
	    return;
	}
	String index = mAttributes.get("index");
	if (index == null){
		//System.out.println("index" + this);
	    return;
	}
	String bounds = mAttributes.get("bounds");
	if (bounds == null) {
		//System.out.println("bounds" + this);
	    return;
	}
	// shorten the standard class names, otherwise it takes up too much space on UI
	className = className.replace("android.widget.", "");
	className = className.replace("android.view.", "");
	StringBuilder builder = new StringBuilder();
	builder.append('(');
	builder.append(index);
	builder.append(") ");
	builder.append(className);
	if (!text.isEmpty()) {
	    builder.append(':');
	    builder.append(text);
	}
	if (!contentDescription.isEmpty()) {
	    builder.append(" {");
	    builder.append(contentDescription);
	    builder.append('}');
	}
	builder.append(' ');
	builder.append(bounds);
	mDisplayName = builder.toString();
    }

    private void updateBounds(String bounds) {
	Matcher m = BOUNDS_PATTERN.matcher(bounds);
	if (m.matches()) {
	    x = Integer.parseInt(m.group(1));
	    y = Integer.parseInt(m.group(2));
	    width = Integer.parseInt(m.group(3)) - x;
	    height = Integer.parseInt(m.group(4)) - y;
	    mHasBounds = true;
	} else {
	    throw new RuntimeException("Invalid bounds: " + bounds);
	}
    }

    @Override
    public String toString() {
	return mDisplayName;
    }

    public String getAttribute(String key) {
	return mAttributes.get(key);
    }

    @Override
    public Object[] getAttributesArray() {
	// this approach means we do not handle the situation where an attribute is added
	// after this function is first called. This is currently not a concern because the
	// tree is supposed to be readonly
	if (mCachedAttributesArray == null) {
	    mCachedAttributesArray = new Object[mAttributes.size()];
	    int i = 0;
	    for (String attr : mAttributes.keySet()) {
		mCachedAttributesArray[i++] = new AttributePair(attr, mAttributes.get(attr));
	    }
	}
	return mCachedAttributesArray;
    }

    public String getType(){
	String className = this.getAttribute("class");
	if(className != null){
		String[] type = className.split("\\.");
			if(type.length == 0)	return null;
			return type[type.length-1];
	}
	return null;
    }

    public int getScreenWidth(){
	return screenWidth;
    }

    public int getScreenHeight(){
	return screenHeight;
    }

    public void setScreenWidth(int screenWidth){
	this.screenWidth = screenWidth;
    }

    public void setScreenHeight(int screenHeight){
	this.screenHeight = screenHeight;
    }

}	// end of inner class UiNode




}	// end of class UiAutomatorTree




/* end of UiAutomatorTree.java */
