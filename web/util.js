/********************************************************************************/
/*										*/
/*	Javascript utility functions						*/
/*										*/
/********************************************************************************/

/********************************************************************************/
/*										*/
/*	String functions							*/
/*										*/
/********************************************************************************/

String.prototype.trim = function()
{
   return this.replace(/^\s+|\s+$/g,"");
}

String.prototype.ltrim = function()
{
   return this.replace(/^\s+/,"");
}

String.prototype.rtrim = function() {
   return this.replace(/\s+$/,"");
}



function trim(stringToTrim)
{
   return stringToTrim.replace(/^\s+|\s+$/g,"");
}

function ltrim(stringToTrim)
{
   return stringToTrim.replace(/^\s+/,"");
}

function rtrim(stringToTrim)
{
   return stringToTrim.replace(/\s+$/,"");
}


/********************************************************************************/
/*										*/
/*	Browser-independent methods for XML requests				*/
/*										*/
/********************************************************************************/

function createDocument(root)
{
   // the following might now work with IE
   var doc = createNewDocument("");

   if (root != null) {
      addXmlNode(doc,doc,root);
    }

   return doc;
}




function createNewDocument(rootname)
{
   if (rootname == null) rootname = "";
   var namespace = "http://www.cs.brown.edu/S6";
   var doc = null;

   if (document.implementation && document.implementation.createDocument) {
      // This is the W3C standard way to do it
      doc = document.implementation.createDocument(namespace,rootname, null);
      if (doc == null) {
	 alert("Document creation problem 1");
       }
    }
   else if (window.ActiveXObject) {
      // Create an empty document as an ActiveX object
      // If there is no root element, this is all we have to do
      doc = new ActiveXObject("MSXML2.DOMDocument");

      // If there is a root tag, initialize the document
      if (rootname != "") {
	 // Look for a namespace prefix
	 var prefix = "";
	 var tagname = rootname;
	 var p = rootname.indexOf(':');
	 if (p != -1) {
	    prefix = rootname.substring(0, p);
	    tagname = rootname.substring(p+1);
	  }

	 // Create the root element (with optional namespace) as a
	 // string of text
	 var text = "<" + rootname + ' xmlns:S6="' + namespace + '"' + "/>";
	 // And parse that text into the empty document
	 doc.loadXML(text);
       }
    }
   else {
      alert("Can't create empty xml document");
    }

   return doc;
}



function addXmlNode(doc,xml,nod)
{
   if (doc.importNode) {
      nod = doc.importNode(nod,true);
    }
   xml.appendChild(nod);
}




/********************************************************************************/
/*										*/
/*	Xml handling								*/
/*										*/
/********************************************************************************/

function convertXmlToString(xml)
{
   return (new XMLSerializer()).serializeToString(xml);
}


function getXmlElement(xml,elt)
{
   var x = xml.getElementsByTagName(elt);
   if (x.length == 0) {
      x = xml.getElementsByTagName("S6:"+elt);
    }
   if (x.length == 0) return null;

   return x.item(0);
}


function getXmlElements(xml,elt)
{
   var x = xml.getElementsByTagName(elt);
   if (x.length == 0) {
      x = xml.getElementsByTagName("S6:" + elt);
    }
   return x;
}



function getXmlAttr(xml,attr)
{
   var x = xml.getAttribute(attr);
   if (x == '') {
      x = xml.getAttribute("S6:" + attr);
    }
   return x;
}




/********************************************************************************/
/*										*/
/*	Browser independent generic methods					*/
/*										*/
/********************************************************************************/

function addListener(elt,evt,fct)
{
   if (elt.addEventListener) {
      elt.addEventListener(evt,fct,false);
    }
   else if (elt.attachEvent) {
      elt.attachEvent("on" + evt,fct);
    }
}


var has_inner_text = null;


function getTextContent(elt)
{
   if (has_inner_text == null) {
      has_inner_text = (document.getElementsByTagName("body")[0].textContent != undefined) ? false : true;
    }

   var x;

   if (!has_inner_text) x = elt.textContent;
   else {
      x = elt.innerText;
      if (x == undefined) x = elt.text;
    }

   return x;
}



function setTextContent(elt,txt)
{
   if (has_inner_text == null) {
      has_inner_text = (document.getElementsByTagName("body")[0].textContent != undefined) ? false : true;
    }

   if (!has_inner_text) elt.textContent = txt;
   else if (elt.innerText == undefined) elt.text = txt
   else elt.innerText = txt;
}




/* end of util.js */


