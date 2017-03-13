/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) 2017 Falko Bräutigam, polymap.de 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jgrasstools.gears.libs.modules;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class InThreadExecutionPlanner
        extends ExecutionPlanner {
    
    private Exception       exc;
    

    @Override
    public void submit( MultiProcessingTask task ) {
        try {
            if (exc == null) {
                task.calculate();
            }
        }
        catch (Exception e) {
            exc = exc == null ? e : exc;
        }
    }


    @Override
    public void join() throws Exception {
        if (exc != null) {
            throw exc;
        }
    }
    
}
