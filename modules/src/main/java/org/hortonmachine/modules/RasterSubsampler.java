/*
 * This file is part of HortonMachine (http://www.hortonmachine.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * The HortonMachine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hortonmachine.modules;

import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_AUTHORCONTACTS;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_AUTHORNAMES;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_DESCRIPTION;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_FILE_DESCRIPTION;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_KEYWORDS;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_LABEL;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_LICENSE;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_NAME;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_OUT_RASTER_DESCRIPTION;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_P_FACTOR_DESCRIPTION;
import static org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler.OMSRASTERSUBSAMPLER_STATUS;

import org.hortonmachine.gears.io.subsampling.OmsRasterSubsampler;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.libs.modules.HMModel;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Status;
import oms3.annotations.UI;

@Description(OMSRASTERSUBSAMPLER_DESCRIPTION)
@Author(name = OMSRASTERSUBSAMPLER_AUTHORNAMES, contact = OMSRASTERSUBSAMPLER_AUTHORCONTACTS)
@Keywords(OMSRASTERSUBSAMPLER_KEYWORDS)
@Label(OMSRASTERSUBSAMPLER_LABEL)
@Name(OMSRASTERSUBSAMPLER_NAME)
@Status(OMSRASTERSUBSAMPLER_STATUS)
@License(OMSRASTERSUBSAMPLER_LICENSE)
public class RasterSubsampler extends HMModel {

    @Description(OMSRASTERSUBSAMPLER_FILE_DESCRIPTION)
    @UI(HMConstants.FILEIN_UI_HINT_RASTER)
    @In
    public String inRaster = null;

    @Description(OMSRASTERSUBSAMPLER_P_FACTOR_DESCRIPTION)
    @In
    public int pFactor = 2;

    @Description(OMSRASTERSUBSAMPLER_OUT_RASTER_DESCRIPTION)
    @UI(HMConstants.FILEOUT_UI_HINT)
    @In
    public String outRaster = null;

    @Execute
    public void process() throws Exception {
        OmsRasterSubsampler ss = new OmsRasterSubsampler();
        ss.inRaster = getRaster(inRaster);
        ss.pFactor = pFactor;
        ss.process();
        dumpRaster(ss.outRaster, outRaster);
    }

}
