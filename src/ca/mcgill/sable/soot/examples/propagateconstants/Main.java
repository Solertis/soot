/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Soot, a Java(TM) classfile optimization framework.                *
 * Copyright (C) 1997, 1998 Raja Vallee-Rai (kor@sable.mcgill.ca)    *
 * All rights reserved.                                              *
 *                                                                   *
 * This work was done as a project of the Sable Research Group,      *
 * School of Computer Science, McGill University, Canada             *
 * (http://www.sable.mcgill.ca/).  It is understood that any         *
 * modification not identified as such is not covered by the         *
 * preceding statement.                                              *
 *                                                                   *
 * This work is free software; you can redistribute it and/or        *
 * modify it under the terms of the GNU Library General Public       *
 * License as published by the Free Software Foundation; either      *
 * version 2 of the License, or (at your option) any later version.  *
 *                                                                   *
 * This work is distributed in the hope that it will be useful,      *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU *
 * Library General Public License for more details.                  *
 *                                                                   *
 * You should have received a copy of the GNU Library General Public *
 * License along with this library; if not, write to the             *
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,      *
 * Boston, MA  02111-1307, USA.                                      *
 *                                                                   *
 * Java is a trademark of Sun Microsystems, Inc.                     *
 *                                                                   *
 * To submit a bug report, send a comment, or get the latest news on *
 * this project and other Sable Research Group projects, please      *
 * visit the web site: http://www.sable.mcgill.ca/                   *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*
 Reference Version
 -----------------
 This is the latest official version on which this file is based.
 The reference version is: $JimpleVersion: 0.5 $

 Change History
 --------------
 A) Notes:

 Please use the following template.  Most recent changes should
 appear at the top of the list.

 - Modified on [date (March 1, 1900)] by [name]. [(*) if appropriate]
   [description of modification].

 Any Modification flagged with "(*)" was done as a project of the
 Sable Research Group, School of Computer Science,
 McGill University, Canada (http://www.sable.mcgill.ca/).

 You should add your copyright, using the following template, at
 the top of this file, along with other copyrights.

 *                                                                   *
 * Modifications by [name] are                                       *
 * Copyright (C) [year(s)] [your name (or company)].  All rights     *
 * reserved.                                                         *
 *                                                                   *

 B) Changes:

 - Modified on November 2, 1998 by Raja Vallee-Rai (kor@sable.mcgill.ca) (*)
   First release.
*/

package ca.mcgill.sable.soot.examples.propagateconstants;

import ca.mcgill.sable.util.*;
import ca.mcgill.sable.soot.*;
import ca.mcgill.sable.soot.toolkit.scalar.*;
import ca.mcgill.sable.soot.jimple.*;
import java.io.*;
import java.util.*;

/**
    PropagateConstants example.
 
 */
 
public class Main
{
    public static void main(String[] args)
    {
        SootClass sClass = Scene.v().loadClassAndSupport(args[0]);
        PrintWriter out = new PrintWriter(System.out, true);
        
        out.println("Before copy propagation: ");
        
        // Convert all methods to Jimple
        {
            Iterator methodIt = sClass.getMethods().iterator();
            
            while(methodIt.hasNext())
            {
                SootMethod m = (SootMethod) methodIt.next();
                
                m.setActiveBody(new JimpleBody(new ClassFileBody(m)));
            }
        }
           
        sClass.printTo(out, PrintJimpleBodyOption.USE_ABBREVIATIONS);
                               
        // Perform cp on each method
        {
            Iterator methodIt = sClass.getMethods().iterator();
            
            while(methodIt.hasNext())
            {
                SootMethod m = (SootMethod) methodIt.next();
                
                JimpleBody body = (JimpleBody) m.getActiveBody();
                Chain units = body.getUnits();
                CompleteUnitGraph stmtGraph = new CompleteUnitGraph(body);
                
                LocalDefs localDefs = new SimpleLocalDefs(stmtGraph);
                Iterator stmtIt = units.iterator();
                
                while(stmtIt.hasNext())
                {
                    Stmt stmt = (Stmt) stmtIt.next();
                    Iterator useBoxIt = stmt.getUseBoxes().iterator();
                    
                    while(useBoxIt.hasNext())
                    {
                        ValueBox useBox = (ValueBox) useBoxIt.next();
                        
                        if(useBox.getValue() instanceof Local)
                        {
                            Local l = (Local) useBox.getValue();
                            List defsOfUse = localDefs.getDefsOfAt(l, stmt);
                            
                            if(defsOfUse.size() == 1)
                            {
                                DefinitionStmt def = (DefinitionStmt) 
                                    defsOfUse.get(0);
                                
                                if(def.getRightOp() instanceof Constant)
                                {
                                    if(useBox.canContainValue(def.getRightOp()))
                                        useBox.setValue(def.getRightOp());
                                }
                            }
                        }
                    }
                }   
            }
        }
                     
        // Write out the new class
            System.out.println("After copy propagation: ");

            sClass.printTo(out, PrintJimpleBodyOption.USE_ABBREVIATIONS);
    }
}
