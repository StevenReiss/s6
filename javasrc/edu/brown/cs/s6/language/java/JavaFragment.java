/********************************************************************************/
/*                                                                              */
/*              JavaFragment.java                                               */
/*                                                                              */
/*      Extensions for Java language fragments                                  */
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



package edu.brown.cs.s6.language.java;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Fragment;

interface JavaFragment extends S6Fragment
{

ASTNode getAstNode();
void fixupAst();
Collection<JcompType> getImportTypes();
JavaAstClassName getClassNamer();
Iterable<ASTNode> getHelpers();
boolean getUseConstructor();
String getOriginalText();
JavaFragment cloneFragment(ASTRewrite rw,ITrackedNodePosition pos);
JavaFragment cloneFragment(String txt);
String getLocalText();

}       // end of interface JavaFragment




/* end of JavaFragment.java */

