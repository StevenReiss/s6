/********************************************************************************/
/*                                                                              */
/*              SuiseSvgProcessor.java                                          */
/*                                                                              */
/*      Convert SVG diagram into a search request                               */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.suise;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeShapePainter;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.GVTTreeWalker;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ImageNode;
import org.apache.batik.gvt.RasterImageNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.apache.batik.gvt.StrokeShapePainter;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.util.XMLResourceDescriptor;



class SuiseSvgProcessor implements SuiseConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<String,SuiseRawComponent> comp_map;
private double          text_height;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SuiseSvgProcessor()
{
   comp_map = new HashMap<String,SuiseRawComponent>();
   text_height = 0;
}



/********************************************************************************/
/*                                                                              */
/*      Methods to set the svg                                                          */
/*                                                                              */
/********************************************************************************/

synchronized SuiseRawComponent setSvg(String uri)
{
   comp_map.clear();
   text_height = 0;

   if (!loadSvg(uri)) return null;
   if (comp_map.size() == 0) return null;

   SuiseRawComponent root = buildHierarchy();

   if (root == null) return null;
   root.setIsGroup(false);

   mergeTexts();
   findChoices(root);
   findOptions(root);
   findMenuBars(root);
   findTablesAndLists(root);
   findScrollbars(root);
   findSpinners(root,null);
   findSliders(root);
   findDrawingArea(root);
   fixInputRegions(root);

   findAdjacencies(root,root);

   return root;
}



private boolean loadSvg(String uri)
{
   SVGOMDocument doc = null;

   try {
      String parser = XMLResourceDescriptor.getXMLParserClassName();
      SAXSVGDocumentFactory df = new SAXSVGDocumentFactory(parser);
      doc = (SVGOMDocument) df.createSVGDocument(uri);
      // doc = df.createSVGDocument(uri);
    }
   catch (IOException ex) {
      return false;
    }
   if (doc == null) return false;

   UserAgent agt = new UserAgentAdapter();
   DocumentLoader ldr = new DocumentLoader(agt);
   BridgeContext ctx = new BridgeContext(agt,ldr);
   ctx.setDynamicState(BridgeContext.DYNAMIC);
   GVTBuilder bldr = new GVTBuilder();
   GraphicsNode rootgn = bldr.build(ctx,doc);
   visitTree(rootgn);

   return true;
}


private void visitTree(GraphicsNode root)
{
   GVTTreeWalker tree = new GVTTreeWalker(root);
   for ( ; ; ) {
      GraphicsNode gn = tree.nextGraphicsNode();
      if (gn == null) break;
      SuiseRawComponent comp = null;
      if (gn instanceof ShapeNode) {
         comp = visitShape((ShapeNode) gn);
       }
      else if (gn instanceof TextNode) {
         comp = visitText((TextNode) gn);
       }
      else if (gn instanceof ImageNode) {
         System.err.println("VISIT IMAGE " + gn);
         // visitImage((ImageNode) gn);
       }
      else if (gn instanceof RasterImageNode) {
         System.err.println("VISIT RASTER " + gn);
         // visitImage((RasterImageNode) gn);
       }
      else if (gn instanceof CanvasGraphicsNode) {
         CanvasGraphicsNode cgn = (CanvasGraphicsNode) gn;
         System.err.println("VISIT CANVAS " + cgn.getBackgroundPaint() + " " + cgn.getPositionTransform() + " " + cgn.getViewingTransform());
         // handle background color
       }
      else {
         System.err.println("VISIT UNKNOWN " + gn);
       }
      if (comp != null) {
         comp_map.put(comp.getId(),comp);
       }
    }

}


/********************************************************************************/
/*                                                                              */
/*      Handle shapes                                                           */
/*                                                                              */
/********************************************************************************/

private SuiseRawComponent visitShape(ShapeNode sn)
{
   Rectangle2D r2 = sn.getBounds();
   boolean filled = getIsFilled(sn.getShapePainter());
   double stroke = getStrokeWidth(sn.getShapePainter());
   SuiseRawComponent comp = new SuiseRawComponent(r2.getWidth(),r2.getHeight(),
         r2.getX(),r2.getY());

   if (filled) comp.setIsInput(true);
   else if (stroke >= 2) {
      if (r2.getWidth() < 300 && r2.getHeight() < 36)
         comp.setIsButton(true);
    }
   Shape sh = sn.getShape();
   System.err.println("SHAPE IS " + sh + " " + r2);
   int sides = 0;
   PathIterator pi = sh.getPathIterator(null);
   double [] coords = new double[6];
   boolean isuser = false;
   boolean iscircle = true;
   boolean isclsd = false;
   int typs = -1;

   while (!pi.isDone()) {
      coords[0] = coords[1] = coords[2] = coords[3] = coords[4] = coords[5] = 0;
      int v = pi.currentSegment(coords);
      System.err.println("   SEG " + v + " " + coords[0] + " " + coords[1] + " " +
       coords[2] + " " + coords[3] + " " + coords[4] + " " + coords[5]);
      if (v == 0 && typs > 0) isuser = true;
      else if (v == 4) isclsd = true;
      else if (v == 2) isuser = true;
      else {
         if (typs > 0 && typs != 3) iscircle = false;
         typs = Math.max(typs,v);
         if (!isOutside(coords,r2)) {
            iscircle = false;
            isuser = true;
          }
       }
      ++sides;
      pi.next();
    }
   if (!isclsd && sides >= 6) isuser = true;

   System.err.println("NUM SIDES = " + sides + " " + isuser + " " + iscircle + " " + isclsd + " " + typs);
   if (sides == 2 || r2.getHeight() < 3 || r2.getWidth() < 3) {
      comp.setIsLine(true);
    }
   else if (sides < 6 || (r2.getWidth() < 20 && r2.getHeight() < 20)) {
      comp.setIsSymbol(true);
    }
   else if (isuser) {
      if (r2.getWidth() > 50 && r2.getHeight() < 20) {
         comp.setIsText(true);
         comp.setText("Any string can go here");
       }
      else {
         comp.setIsIcon(true);
       }
    }
   else if (sides > 6 || iscircle) {
      if (!(sh instanceof RoundRectangle2D))
         comp.setIsButton(true);
    }

   return comp;
}


private boolean isOutside(double [] coords,Rectangle2D bnds)
{
   double d0 = Math.max(bnds.getWidth(),bnds.getHeight());
   double d = Math.min(3,d0/4);

   for (int i = 0; i < 4; i += 2) {
      if (coords[i] == 0 && coords[i+1] == 0) break;
      double x0 = Math.abs(coords[i]-bnds.getX());
      double x1 = Math.abs(coords[i]-(bnds.getX() + bnds.getWidth()));
      if (Math.min(x0,x1) < d) return true;
      double y0 = Math.abs(coords[i+1]-bnds.getY());
      double y1 = Math.abs(coords[i+1]-(bnds.getY() + bnds.getHeight()));
      if (Math.min(y0,y1) < d) return true;
    }

   return false;
}



private boolean getIsFilled(ShapePainter ptr)
{
   boolean filled = false;

   if (ptr instanceof CompositeShapePainter) {
      CompositeShapePainter csp = (CompositeShapePainter) ptr;
      for (int i = 0; i < csp.getShapePainterCount(); ++i) {
         filled |= getIsFilled(csp.getShapePainter(i));
       }
    }
   else if (ptr instanceof FillShapePainter) {
      FillShapePainter fsp = (FillShapePainter) ptr;
      Paint pt = fsp.getPaint();
      if (pt instanceof Color) {
         Color c = (Color) pt;
         int sat = c.getRed() + c.getGreen() + c.getBlue();
         if (sat < 750) filled = true;
       }
      else if (pt != null) filled = true;
    }

   return filled;
}



private double getStrokeWidth(ShapePainter ptr)
{
   double width = 0;

   if (ptr instanceof CompositeShapePainter) {
      CompositeShapePainter csp = (CompositeShapePainter) ptr;
      for (int i = 0; i < csp.getShapePainterCount(); ++i) {
         width = Math.max(width,getStrokeWidth(csp.getShapePainter(i)));
       }
    }
   else if (ptr instanceof StrokeShapePainter) {
      StrokeShapePainter ssp = (StrokeShapePainter) ptr;
      Stroke stk = ssp.getStroke();
      if (stk instanceof BasicStroke) {
         BasicStroke bstk = (BasicStroke) stk;
         width = bstk.getLineWidth();
       }
    }

   return width;
}




/********************************************************************************/
/*                                                                              */
/*      Handle text regions                                                     */
/*                                                                              */
/********************************************************************************/

private SuiseRawComponent visitText(TextNode tn)
{
   Rectangle2D r2 = tn.getBounds();
   if (r2.getHeight() < 2 || r2.getWidth() < 2) return null;

   String txt = tn.getText();
   if (txt == null || txt.length() == 0) return null;

   System.err.println("TEXT IS '" + txt + "' " + r2 + " " +
         tn.getPrimitiveBounds() + " " + tn.getSensitiveBounds() + " " +
         tn.getGeometryBounds() + " " + tn.getGlobalTransform() + " " + tn.getTransform());

   SuiseRawComponent comp = new SuiseRawComponent(r2.getWidth(),r2.getHeight(),
         r2.getX(),r2.getY());

   if (txt.length() == 1 && r2.getHeight() > 20 && r2.getWidth() > 10) {
      comp.setIsSymbol(true);
      return comp;
    }
   int ct = tn.getTextRuns().size();

   comp.setText(txt);
   comp.setIsText(true);
   if (ct > 1) comp.setIsMultiline(true);

   if (text_height == 0) text_height = r2.getHeight();
   else text_height = Math.min(text_height,r2.getHeight());

   return comp;
}




/********************************************************************************/
/*                                                                              */
/*      Hierarchy building methods                                              */
/*                                                                              */
/********************************************************************************/

private SuiseRawComponent buildHierarchy()
{
   List<SuiseRawComponent> elts = new ArrayList<SuiseRawComponent>(comp_map.values());
   Set<SuiseRawComponent> roots = new HashSet<SuiseRawComponent>(elts);

   // build a complete hierarchy
   for (int i = 0; i < elts.size(); ++i) {
      SuiseRawComponent ci = elts.get(i);
      if (ci.isLine() || ci.isSymbol() || ci.isText()) continue;
      for (int j = 0; j < elts.size(); ++j) {
         SuiseRawComponent cj = elts.get(j);
         if (i != j && ci.isChild(cj)) {
            ci.addChild(cj);
            roots.remove(cj);
          }
       }
    }

    // remove children of children
   for (int i = 0; i < elts.size(); ++i) {
      List<SuiseRawComponent> children = elts.get(i).getChildren();
      for (Iterator<SuiseRawComponent> it = children.iterator(); it.hasNext(); ) {
         SuiseRawComponent e = it.next();
         boolean del = false;
         for (SuiseRawComponent e1 : children) {
            if (e1 != e && e1.isChild(e)) del = true;
          }
         if (del) it.remove();
       }
    }

   // put everything inside a container if needed
   elts = new ArrayList<SuiseRawComponent>(roots);
   if (elts.size() > 1) {
      SuiseRawComponent u0 = elts.get(0);
      double x = u0.getX();
      double y = u0.getY();
      double w = u0.getWidth();
      double h = u0.getHeight();
      for (int i = 1; i < elts.size(); ++i) {
         SuiseRawComponent ue = elts.get(i);
         x = Math.min(x,ue.getX());
         if (ue.getX() + ue.getWidth() > x + w) {
            w = ue.getX() + ue.getWidth() - x;
          }
         y = Math.min(y,ue.getY());
         if (ue.getY() + ue.getHeight() > y + h) {
            h = ue.getY() + ue.getHeight() - y;
          }
       }
      u0 = new SuiseRawComponent(w,h,x,y);
      comp_map.put(u0.getId(),u0);
      for (SuiseRawComponent ue : elts) {
         u0.addChild(ue);
       }
      elts.clear();
      elts.add(u0);
    }

   return elts.get(0);
}




/********************************************************************************/
/*                                                                              */
/*      Heuristics for creating logical structure                               */
/*                                                                              */
/********************************************************************************/

private void mergeTexts()
{
   List<SuiseRawComponent> nelts = new ArrayList<SuiseRawComponent>(comp_map.values());
   for (SuiseRawComponent c : nelts) {
      if (c.getText() != null) continue;
      if (c.getChildren().size() != 1) continue;
      SuiseRawComponent ccmp = c.getChildren().get(0);
      if (ccmp.getText() !=  null && !ccmp.hasChildren() && ccmp.isText()) {
         comp_map.remove(ccmp.getId());
         c.getChildren().clear();
         c.setText(ccmp.getText());
         if (ccmp.isMultiline()) c.setIsMultiline(true);
       }
      double d0 = c.getWidth() - ccmp.getWidth();
      double d1 = c.getHeight() - ccmp.getHeight();
      double d2 = ccmp.getX() - c.getX();
      double d3 = c.getX() + c.getWidth() - (ccmp.getX() + ccmp.getWidth());
      double d4 = ccmp.getY() - c.getY();
      double d5 = c.getY() + c.getHeight() - (ccmp.getY() + ccmp.getHeight());

      if (d0 < 25 && d1 < 20 && !ccmp.isMultiline()) {
         c.setIsButton(true);
       }
      else if (Math.abs(d2-d3) < 15 && d1 < 20 && !ccmp.isMultiline()) {
         c.setIsButton(true);
       }
      else if (d2 < 20 && d3 > 50) {
         c.setIsInput(true);
         c.setIsButton(false);
         if (Math.abs(d4-d5) > 15) c.setIsMultiline(true);
         else if (c.getHeight() > 3*Math.max(12,ccmp.getHeight()))
            c.setIsMultiline(true);
         else if (c.getText().matches("[*]+")) {
            c.setIsPassword(true);
            c.setText(null);
          }
         else if (c.getText().matches("[- $]*[0-9][-.,0-9]*\\s*")) {
            c.setIsNumeric(true);
            c.setText(null);
          }
       }
    }
}


private void findChoices(SuiseRawComponent c)
{
   List<SuiseRawComponent> chld = c.getChildren();
   int tcnt = 0;
   int scnt = 0;
   int ocnt = 0;
   String txt = null;
   for (SuiseRawComponent cc : chld) {
      findChoices(cc);
      double d0 = c.getX() + c.getWidth();
      double d1 = cc.getX() + cc.getWidth();
      if (cc.isText()) {
         txt = cc.getText();
         ++tcnt;
       }
      else if (cc.isSymbol() && d1 - d0 < 20) {
         ++scnt;
       }
      else ++ocnt;
    }
   if (tcnt == 1 && scnt > 0 && ocnt == 0) {
      if (!c.isGroup()) {
         c.setText(txt);
         c.setIsChoice(true);
         c.setIsInput(false);
         clearChildren(c);
       }
    }
}



private void findOptions(SuiseRawComponent c)
{
   List<SuiseRawComponent> chld = c.getChildren();
   Map<SuiseRawComponent,SuiseRawComponent> fnd = new HashMap<SuiseRawComponent,SuiseRawComponent>();
   for (SuiseRawComponent cc : chld) {
      findOptions(cc);
      if (cc.isSymbol()) {
         SuiseRawComponent tc = findButtonLabel(cc,chld);
         if (tc != null) fnd.put(cc,tc);
       }
    }

   for (Map.Entry<SuiseRawComponent,SuiseRawComponent> ent : fnd.entrySet()) {
      SuiseRawComponent icn = ent.getKey();
      SuiseRawComponent txt = ent.getValue();
      double x0 = Math.min(icn.getX(),txt.getX());
      double x1 = Math.max(icn.getX()+ icn.getWidth(),txt.getX() + txt.getWidth());
      double y0 = Math.min(icn.getY(),txt.getY());
      double y1 = Math.max(icn.getY() + icn.getHeight(),txt.getY() + txt.getHeight());
      chld.remove(icn);
      chld.remove(txt);
      SuiseRawComponent nc = new SuiseRawComponent(x0,y0,x1-x0,y1-y0);
      c.addChild(nc);
      nc.setIsOption(true);
      nc.setIsButton(true);
      nc.setText(txt.getText());
      comp_map.remove(icn.getId());
      comp_map.remove(txt.getId());
      comp_map.put(nc.getId(),nc);
    }
}



private SuiseRawComponent findButtonLabel(SuiseRawComponent sym,List<SuiseRawComponent> cands)
{
   double x0 = sym.getX();
   double y0 = sym.getY();
   double w0 = sym.getWidth();
   double h0 = sym.getHeight();
   if (w0 > 25 || h0 > 25) return null;
   for (SuiseRawComponent cc : cands) {
      if (cc.isText() || (cc.isButton() && cc.getText() != null)) {
         double x1 = cc.getX();
         double y1 = cc.getY();
         double h1 = cc.getHeight();
         if (x0 + w0 > x1 || x1 - (x0+w0) >= 25) continue;
         double d = Math.abs((y0 + h0/2) - (y1 + h1/2));
         if (d > 10) continue;
         return cc;
       }
    }
   return null;
}




private void findTablesAndLists(SuiseRawComponent c)
{
   int hlin = 0;
   int vlin = 0;
   int tcnt = 0;
   int bad = 0;
   int nfull = 0;
   int nbal = 0;
   List<SuiseRawComponent> del = new ArrayList<SuiseRawComponent>();
   for (SuiseRawComponent cc : c.getChildren()) {
       findTablesAndLists(cc);
       if (cc.isLine()) {
          if (cc.getWidth() < 5 && cc.getHeight() < 5) continue;
          if (cc.getWidth() < 0.25*cc.getHeight()) ++vlin;
          else if (cc.getHeight() < 0.25*cc.getWidth()) ++hlin;
          del.add(cc);
        }
       else if (cc.isText()) {
          ++tcnt;
          double d0 = cc.getX() - c.getX();
          double d1 = c.getX() + c.getWidth() - (cc.getX() + cc.getWidth());
          if (d0 + d1 < 20) ++nfull;
          if (d0 > 15 && d1 > 15) ++nbal;
          del.add(cc);
        }
       else if (cc.isButton() || cc.isInput()) ++bad;
    }
   if (bad > 0) return;
   if (hlin == 0 && vlin == 0 && tcnt == 0) return;

   if (hlin > 0 && vlin > 0) {
      c.setIsTable(true);
      c.setIsButton(false);
    }
   else if (hlin > 0) {
      c.setIsTable(true);
      c.setIsList(true);
      c.setIsButton(false);
    }
   else if (tcnt > 1) {
      if (vlin > 0) c.setIsTree(true);
      else if (nfull > 0 || nbal >=  2*tcnt/3 && !c.isInput()) {
         c.setIsList(true);
         c.setIsButton(false);
         if (nbal != tcnt) c.setIsTree(true);
         if (nfull > 0) {
            c.setIsInput(true);
            c.setIsMultiline(true);
          }
       }
      else if (nfull == 0) {
         c.setIsList(true);
         c.setIsMultiline(true);
         c.setIsInput(true);
         c.setIsButton(false);
       }
      else {
         c.setIsInput(true);
         c.setIsMultiline(true);
         c.setIsButton(false);
       }
    }

   for (SuiseRawComponent cc : del) {
      c.getChildren().remove(cc);
      comp_map.remove(cc.getId());
    }
}



private void fixInputRegions(SuiseRawComponent c)
{
   int octr = 0;
   for (SuiseRawComponent cc : c.getChildren()) {
      fixInputRegions(cc);
      if (cc.isText()) ;
      else if (!ignoreItem(cc)) ++octr;
    }

   if (c.isInput()) {
      if (octr > 0) {
         c.setIsInput(false);
         c.setIsGroup(true);
       }
      else {
         clearChildren(c);
         c.setText(null);
       }
    }
}



private int findScrollbars(SuiseRawComponent c)
{
   int tctr = 0;
   int bctr = 0;
   int xctr = 0;
   double x0 = c.getX();
   double x1 = x0 + c.getWidth();
   double y0 = c.getY();
   double y1 = y0 + c.getHeight();
   double dx = 0;
   double dy = 0;
   if (c.getWidth() > 3*c.getHeight() && c.getHeight() < 30) {
      dy = 1;
      dx = 0;
    }
   else if (c.getHeight() > 3*c.getWidth() && c.getWidth() < 30) {
      dx = 1;
      dy = 0;
    }

   for (SuiseRawComponent cc : c.getChildren()) {
      xctr += findScrollbars(cc);
      if (cc.isSymbol()) {
         double dt = (cc.getX()-x0)*dy + (cc.getY()-y0)*dx;
         double db = (x1 - (cc.getX()+cc.getWidth()))*dy + (y1 - (cc.getY() + cc.getHeight()))*dx;
         if (dt < 10) ++tctr;
         if (db < 10) ++bctr;
       }
      else if (cc.isText()) ++xctr;
    }

   if (dx+dy != 0 && tctr > 0 && bctr > 0 && xctr == 0) {
      c.setIsScrollBar(true);
      clearChildren(c);
    }

   return xctr;
}



private void findSpinners(SuiseRawComponent c,SuiseRawComponent par)
{
   int sctr = 0;
   boolean other = false;

   for (SuiseRawComponent cc : c.getChildren()) {
      if (cc.isSymbol()) {
         ++sctr;
       }
      else {
         other = true;
         findSpinners(cc,c);
       }
    }

   double x0 = c.getX();
   double x1 = c.getX() + c.getWidth();
   double y0 = c.getY();
   double y1 = y0 + c.getHeight()/2;
   double y2 = y0 + c.getHeight();

   if (!other && sctr == 2 && c.getWidth() < 10 && c.getHeight() < 20 && par != null) {
      clearChildren(c);
      c.setIsScrollBar(false);
      List<SuiseRawComponent> chld = par.getChildren();
      for (SuiseRawComponent cc : chld) {
         if (cc == c) continue;
         if (cc.isInput()) {
            double tx0 = cc.getX();
            double tx1 = tx0 + cc.getWidth();
            double ty0 = cc.getY();
            double ty1 = cc.getY()+cc.getHeight()/2;
            double ty2 = cc.getY() + cc.getHeight();
            if (x0 > tx1 && x0-tx1 < 10 && Math.abs(y1-ty1) < 5) {
               comp_map.remove(c.getId());
               comp_map.remove(cc.getId());
               chld.remove(c);
               chld.remove(cc);
               double ny = Math.min(ty0,y0);
               double nh = Math.max(ty2,y2) - ny;
               double nx = Math.min(x0,tx0);
               double nw = Math.max(x1,tx1) - nx;
               SuiseRawComponent nc = new SuiseRawComponent(nx,ny,nw,nh);
               comp_map.put(nc.getId(),nc);
               par.addChild(nc);
               nc.setIsInput(true);
               nc.setIsNumeric(true);
               return;
             }
          }
       }
      c.setIsNumeric(true);
      c.setIsButton(true);
    }
}



private void findMenuBars(SuiseRawComponent c)
{
   int ntext = 0;
   int nsym = 0;
   int nbad = 0;
   for (SuiseRawComponent cc : c.getChildren()) {
      findMenuBars(cc);
      if (cc.isText()) ++ntext;
      else if (cc.isButton() || cc.isSymbol()) ++nsym;
      else if (cc.isInput()) ++nbad;
    }

   if (c.getWidth() > 100 && c.getHeight() < 30) {
      if (ntext > 2 && nsym == 0 && nbad == 0) {
         c.setIsMenuBar(true);
         clearChildren(c);
         return;
       }
    }
   if (c.getWidth() > 100 && c.getHeight() < 25) {
      if (nsym + ntext > 2 && nsym > 0 && nbad == 0) {
         c.setIsToolBar(true);
         clearChildren(c);
         return;
       }
    }
}




private void findSliders(SuiseRawComponent p)
{
   List<SuiseRawComponent> chld = p.getChildren();
   List<SuiseRawComponent> base = new ArrayList<SuiseRawComponent>();
   int scnt = 0;
   for (SuiseRawComponent c : chld) {
      findSliders(c);
      if (c.isLine() && c.getWidth() > 100 && c.getHeight() < 5) {
         base.add(c);
       }
      else if (c.isSymbol()) ++scnt;
    }

   if (base.isEmpty() || scnt == 0) return;

   for (SuiseRawComponent b : base) {
      double bx0 = b.getX();
      double by0 = b.getY();
      double bx1 = bx0 + b.getWidth();
      double by1 = by0 + b.getHeight();

      List<SuiseRawComponent> elts = new ArrayList<SuiseRawComponent>();
      boolean havesym = false;
      for (SuiseRawComponent c : chld) {
         if (c == b) continue;
         double x0 = c.getX();
         double y0 = c.getY();
         double x1 = x0 + c.getWidth();
         double y1 = y0 + c.getHeight();
         if (c.isText()) {
            if (y0 > by0 && Math.abs(y0-by1) < 6 && x1  > bx0 && x0 < bx1) {
               elts.add(c);
             }
          }
         else if (c.isLine() && c.getWidth() < 5 && c.getHeight() < 10) {
            if (x0 >= bx0-2 && x0 <= bx1+2 &&
                  ((y1 >= by0 && y0 < by0) || (y0 <= by1 && y1 >= by1))) {
               elts.add(c);
             }
          }
         else if (c.isSymbol() && x0 >= bx0 && x1 <= bx1 && y0 <= by1 && y1 >= by0) {
            havesym = true;
            elts.add(c);
          }
       }
      if (havesym) {
         for (SuiseRawComponent c : elts) {
            bx0 = Math.min(bx0,c.getX());
            bx1 = Math.max(bx1,c.getX() + c.getWidth());
            by0 = Math.min(by0,c.getY());
            by1 = Math.max(by1,c.getY() + c.getHeight());
            comp_map.remove(c.getId());
            chld.remove(c);
          }
         comp_map.remove(b.getId());
         chld.remove(b);
         SuiseRawComponent nc = new SuiseRawComponent(bx0,by0,bx1-bx0,by1-by0);
         comp_map.put(nc.getId(),nc);
         p.addChild(nc);
         nc.setIsSlider(true);
       }
    }
}



private int findDrawingArea(SuiseRawComponent c)
{
   int icnt = 0;
   List<SuiseRawComponent> chld = c.getChildren();
   int ecnt = 0;
   int scnt = 0;
   for (SuiseRawComponent cc : chld) {
      icnt += findDrawingArea(cc);
      if (cc.isIcon() || cc.isSymbol()) ++ecnt;
      if (cc.isInput() || (cc.isButton() && cc.getText() != null)) ++icnt;
      else if (cc.isTable() || cc.isList()) ++icnt;
      if (cc.isScrollBar()) ++scnt;
    }
   if (icnt == 0 && ecnt > 0) {
      clearChildren(c);
      c.setIsDrawing(true);
      if (scnt > 0) c.setIsScrollBar(true);
      if (c.getWidth() < 100 && c.getHeight() < 100) {
         c.setIsButton(true);
       }
    }


   return icnt;
}

private void clearChildren(SuiseRawComponent c)
{
   for (SuiseRawComponent cc : c.getChildren()) {
      comp_map.remove(cc.getId());
    }
   c.getChildren().clear();
}


/********************************************************************************/
/*                                                                              */
/*      Find adjacent component                                                 */
/*                                                                              */
/********************************************************************************/

private void findAdjacencies(SuiseRawComponent par,SuiseRawComponent gpar)
{
   if (!gpar.hasChildren()) return;

   double x0 = par.getX();
   double x1 = par.getX() + par.getWidth();
   double y0 = par.getY();
   double y1 = par.getY() + par.getHeight();

   for (SuiseRawComponent c : gpar.getChildren()) {
      if (c.isGroup()) findAdjacencies(par,c);
      else findAdjacencies(c,c);

      if (ignoreItem(c)) continue;
      double cx0 = c.getX();
      double cx1 = c.getX() + c.getWidth();
      double cy0 = c.getY();
      double cy1 = c.getY() + c.getHeight();
      if (cx0 - x0 < 10) c.setLeftAnchor(par);
      if (x1 - cx1 < 10) c.setRightAnchor(par);
      if (cy0 - y0 < 10) c.setTopAnchor(par);
      if (y1 - cy1 < 10) c.setBottomAnchor(par);
    }

   List<SuiseRawComponent> lvs = getAllLeaves(par,null);

   for (SuiseRawComponent c : gpar.getChildren()) {
      if (ignoreItem(c)) continue;
      findTopAnchor(c,lvs);
      findBottomAnchor(c,lvs);
      findLeftAnchor(c,lvs);
      findRightAnchor(c,lvs);
    }
}



private boolean ignoreItem(SuiseRawComponent c)
{
   if (c.isLine()) return true;
   if (c.isGroup()) return true;
   if (c.isSymbol()) return true;
   return false;
}


private List<SuiseRawComponent> getAllLeaves(SuiseRawComponent n,List<SuiseRawComponent> r)
{
   if (r == null) r = new ArrayList<SuiseRawComponent>();

   if (n.hasChildren()) {
      for (SuiseRawComponent c : n.getChildren()) {
         getAllLeaves(c,r);
       }
    }
   else if (!ignoreItem(n)) r.add(n);

   return r;
}



private void findTopAnchor(SuiseRawComponent c,List<SuiseRawComponent> cands)
{
   if (c.getTopAnchor() != null) return;

   double x0 = c.getX();
   double x1 = x0 + c.getWidth();
   double y0 = c.getY();

   for (SuiseRawComponent n : cands) {
      double nx0 = n.getX();
      double nx1 = nx0 + n.getWidth();
      double ny0 = n.getY();
      double ny1 = ny0 + n.getHeight();
      if (ny1 < y0 && y0 - ny1 < 10) {
         if (x1 > nx0 && x0 < nx1) {
            c.setTopAnchor(n);
            break;
          }
       }
    }
}



private void findBottomAnchor(SuiseRawComponent c,List<SuiseRawComponent> cands)
{
   if (c.getBottomAnchor() != null) return;

   double x0 = c.getX();
   double x1 = x0 + c.getWidth();
   double y0 = c.getY();
   double y1 = y0 + c.getHeight();

   for (SuiseRawComponent n : cands) {
      double nx0 = n.getX();
      double nx1 = nx0 + n.getWidth();
      double ny0 = n.getY();
      if (ny0 > y1 && ny0 - y1 < 10) {
         if (x1 > nx0 && x0 < nx1) {
            c.setBottomAnchor(n);
            break;
          }
       }
    }
}



private void findLeftAnchor(SuiseRawComponent c,List<SuiseRawComponent> cands)
{
   if (c.getLeftAnchor() != null) return;

   double x0 = c.getX();
   double y0 = c.getY();
   double y1 = y0 + c.getHeight();

   for (SuiseRawComponent n : cands) {
      double nx0 = n.getX();
      double nx1 = nx0 + n.getWidth();
      double ny0 = n.getY();
      double ny1 = ny0 + n.getHeight();
      if (nx1 < x0 && x0 - nx1 < 10) {
         if (y1 > ny0 && y0 < ny1) {
            c.setLeftAnchor(n);
            break;
          }
       }
    }
}



private void findRightAnchor(SuiseRawComponent c,List<SuiseRawComponent> cands)
{
   if (c.getRightAnchor() != null) return;

   double x0 = c.getX();
   double x1 = x0 + c.getWidth();
   double y0 = c.getY();
   double y1 = y0 + c.getHeight();

   for (SuiseRawComponent n : cands) {
      double nx0 = n.getX();
      double ny0 = n.getY();
      double ny1 = ny0 + n.getHeight();
      if (nx0 > x1 && nx0 - x1 < 10) {
         if (y1 > ny0 && y0 < ny1) {
            c.setRightAnchor(n);
            break;
          }
       }
    }
}



}       // end of class SuiseSvgProcessor




/* end of SuiseSvgProcessor.java */

