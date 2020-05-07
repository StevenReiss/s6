/********************************************************************************/
/*										*/
/*		LicenseManager.java						*/
/*										*/
/*	Routine to manage licenses within S6					*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2007, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/license/LicenseManager.java,v 1.8 2015/09/23 17:58:05 spr Exp $ */


/*********************************************************************************
 *
 * $Log: LicenseManager.java,v $
 * Revision 1.8  2015/09/23 17:58:05  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2015/03/11 18:04:32  spr
 * Check database returns.
 *
 * Revision 1.6  2014/08/29 15:16:12  spr
 * Updates for suise, testcases.
 *
 * Revision 1.5  2012-06-11 14:07:54  spr
 * fix bugs
 *
 * Revision 1.4  2009-09-18 01:41:42  spr
 * Update for new database.
 *
 * Revision 1.3  2008-11-12 13:52:17  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-06-12 17:47:53  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.license;


import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.brown.cs.ivy.file.IvyDatabase;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6License;



public class LicenseManager implements S6License, LicenseConstants, S6Constants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Connection sql_conn;
private int skip_count;        // # of opens to skip
private boolean have_database;

private static LicenseManager license_manager = null;


private static final String [] DB_CREATE = new String [] {
   "CREATE TABLE LicenseData (uid text PRIMARY KEY, hash text, license text)",
   "CREATE TABLE LicenseUid (uid integer)",
   "CREATE INDEX LicenseIndex ON LicenseData (hash)",
   "INSERT INTO LicenseUid VALUES (1)"
};



/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

public static synchronized S6License getLicenseManager()
{
   if (license_manager == null) {
      license_manager = new LicenseManager();
    }

   return license_manager;
}




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

LicenseManager()
{
   sql_conn = null;
   skip_count = 0;     // this is the number of opens to skip
   have_database = false;

   openDatabase();
}



/********************************************************************************/
/*										*/
/*	Methods to open the database						*/
/*										*/
/********************************************************************************/

private void openDatabase()
{
   if (sql_conn != null) return;
   if (skip_count-- > 0) return;

   try {
      sql_conn = IvyDatabase.openDatabase(LICENSE_DATABASE);
      have_database = true;
    }
   catch (SQLException e) {
      synchronized (this) {
	 if (have_database) return;
	 have_database = true;
       }
      checkDatabase();
    }
}



/********************************************************************************/
/*										*/
/*	Methods to create the database if it doesn't exist                      */
/*										*/
/********************************************************************************/

private void checkDatabase()
{
   Connection dconn = null;
   try {
      dconn = IvyDatabase.openDefaultDatabase();
    }
   catch (SQLException e) {
      skip_count = 20;
      have_database = false;
      IvyLog.logE("LICENSE","Can't connect to any database for licensing: " + e);
      return;
    }

   try {
      Statement st = dconn.createStatement();
      st.execute("DROP DATABASE " + LICENSE_DATABASE);
      st.close();
    }
   catch (SQLException e) { }

   try {
      Statement st = dconn.createStatement();
      st.execute("CREATE DATABASE " + LICENSE_DATABASE);
      st.close();
      dconn.close();
    }
   catch (SQLException e) {
      IvyLog.logE("LICENSE","Can't create new database for licensing: " + e);
      skip_count = 100;
      have_database = false;
      return;
    }

   try {
      sql_conn = IvyDatabase.openDatabase(LICENSE_DATABASE);
    }
   catch (SQLException e) {
      IvyLog.logE("LICENSE","Can't open database for licensing: " + e);
      skip_count = 10;
      have_database = false;
      return;
    }

   try {
      Statement st = sql_conn.createStatement();
      for (String s : DB_CREATE) {
	 st.execute(s);
       }
      st.close();
    }
   catch (SQLException e) {
      IvyLog.logE("LICENSE","Can't set up database for licensing: " + e);
      sql_conn = null;
      have_database = false;
      skip_count = 10;
      return;
   }

   IvyLog.logI("LICENSE","Database successfully created");
   have_database = true;
   skip_count = 0;
}




/********************************************************************************/
/*										*/
/*	Methods to Store a license						*/
/*										*/
/********************************************************************************/

public String getLicenseUidFromSource(String source)
{
   try {
      String lic = FindCopyright.getCopyright(new StringReader(source));
      return getLicenseUid(lic);
    }
   catch (IOException e) {
      IvyLog.logE("LICENSE","Problem reading source: " + e);
    }

   return null;
}




public String getLicenseUid(String lic)
{
   if (lic == null) return null;

   String licsql = makeSqlString(lic);
   String uid = null;
   StringBuffer buf = new StringBuffer();

   try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte [] dig = md.digest(licsql.getBytes());
      buf.append("'");
      for (int i = 0; i < dig.length; ++i) {
	 int v = dig[i] & 0xff;
	 String s = Integer.toString(v,16);
	 if (s.length() == 1) buf.append("0");
	 buf.append(s);
       }
      buf.append("'");
    }
   catch (NoSuchAlgorithmException e) {
      buf.append("md5('");
      buf.append(licsql);
      buf.append("')");
    }
   String hash = buf.toString();

   ResultSet rs = queryDatabase("SELECT uid FROM LicenseData WHERE hash = " + hash);

   if (rs == null) return null;

   try {
      if (rs.next()) uid = rs.getString(1);
      rs.close();
    }
   catch (SQLException e) {
      IvyLog.logE("LICENSE","Problem accessing sql result: " + e);
    }

   openDatabase();

   if (uid == null && sql_conn != null) {
      synchronized (this) {
	 try {
	    int idctr = 0;
	    Statement st = sql_conn.createStatement();
	    st.executeUpdate("BEGIN");
	    st.executeUpdate("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE");
	    rs = st.executeQuery("SELECT uid FROM LicenseUid");
	    if (rs.next()) idctr = rs.getInt(1);
	    st.executeUpdate("UPDATE LicenseUid SET uid = " + (idctr+1));
	    uid = "LICENSE_" + idctr;
	    st.executeUpdate("INSERT INTO LicenseData VALUES( '" + uid + "'," +
				hash + ",'" + licsql + "')");
	    st.executeUpdate("COMMIT");
	  }
	 catch (SQLException e) {
            IvyLog.logE("LICENSE","Problem setting next license id: " + e);
	  }
       }
    }

   return uid;
}




/********************************************************************************/
/*										*/
/*	Methods to retrieve a license given UID 				*/
/*										*/
/********************************************************************************/

public String getLicense(String uid)
{
   ResultSet rs = queryDatabase("SELECT license FROM LicenseData WHERE uid = '" + uid + "'");
   if (rs == null) return null;

   String lic = null;

   try {
      if (rs.next()) lic = rs.getString(1);
      rs.close();
    }
   catch (SQLException e) {
      IvyLog.logE("LICENSE","Problem accessing license result: " + e);
    }

   return lic;
}




/********************************************************************************/
/*										*/
/*	Database access methods 						*/
/*										*/
/********************************************************************************/

private synchronized ResultSet queryDatabase(String q)
{
   ResultSet rs = null;

   openDatabase();

   if (sql_conn == null) return null;

   try {
      Statement stmt = sql_conn.createStatement();
      rs = stmt.executeQuery(q);
    }
   catch (SQLException e) {
      IvyLog.logE("LICENSE","Problem with SQL query: " + e);
      sql_conn = null;
    }

   return rs;
}




/********************************************************************************/
/*										*/
/*	Methods to ensure string is in SQL format				*/
/*										*/
/********************************************************************************/

private String makeSqlString(String s)
{
   StringBuffer buf = new StringBuffer();

   for (int i = 0; i < s.length(); ++i) {
      char ch = s.charAt(i);
      if (ch == '\'') {
	 buf.append("''");
       }
      else buf.append(ch);
    }

   return buf.toString();
}



}	// end of class LicenseManager




/* end of LicenseManager.java */
