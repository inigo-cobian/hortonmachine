package org.hortonmachine.gears.io.stac;

import java.net.URI;
import java.util.Iterator;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.RegionMap;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.fasterxml.jackson.databind.JsonNode;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.CogImageInputStreamSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogSourceSPIProvider;
import it.geosolutions.imageioimpl.plugins.cog.HttpRangeReader;
import it.geosolutions.imageioimpl.plugins.cog.S3RangeReader;
import it.geosolutions.imageioimpl.plugins.cog.RangeReader;

/**
 * An asset from a stac item.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class HMStacAsset {

    private String id;
    private String title;
    private String type;
    private String nonValidReason;
    private boolean isValid = true;
    private String assetUrl;
    private double noValue = HMConstants.doubleNovalue;
    private double resolution;

    public HMStacAsset( String id, JsonNode assetNode ) {
        this.id = id;
        JsonNode typeNode = assetNode.get("type");
        if (typeNode != null) {
            type = typeNode.textValue();
            // we only check cloud optimized datasets here
            JsonNode titleNode = assetNode.get("title");
            title = "undefined title";
            if (titleNode != null) {
                title = titleNode.textValue();
            }
            if (type.toLowerCase().contains("profile=cloud-optimized")) {
                JsonNode rasterBandNode = assetNode.get("raster:bands");
                assetUrl = assetNode.get("href").textValue();
                if (rasterBandNode != null && !rasterBandNode.isEmpty()) {
                    Iterator<JsonNode> rbIterator = rasterBandNode.elements();
                    while( rbIterator.hasNext() ) {
                        JsonNode rbNode = rbIterator.next();
                        JsonNode noValueNode = rbNode.get("nodata");
                        if (noValueNode != null) {
                            noValue = noValueNode.asDouble();
                        }
                        JsonNode resolNode = rbNode.get("spatial_resolution");
                        if (resolNode != null) {
                            resolution = resolNode.asDouble();
                        }
                    }
                }
            } else {
                isValid = false;
                nonValidReason = "not a COG";
            }
        } else {
            nonValidReason = "type information not available";
            isValid = false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("title = " + title).append("\n");
        sb.append("type = " + type).append("\n");
        sb.append("url = " + assetUrl).append("\n");
        sb.append("isValid = " + isValid).append("\n");
        if (!isValid) {
            sb.append("nonValidReason = " + nonValidReason).append("\n");
        }
        return sb.toString();
    }

    private RangeReader createRangeReader(BasicAuthURI cogUri) {
        if (assetUrl.startsWith("s3://")) {
            return new S3RangeReader(cogUri.getUri(), CogImageReadParam.DEFAULT_HEADER_LENGTH);
        }
        return new HttpRangeReader(cogUri.getUri(), CogImageReadParam.DEFAULT_HEADER_LENGTH);
    }

    /**
     * Read the asset's coverage into a local raster.
     * 
     * @param region and optional region to read from.
     * @param user an optional user in case of authentication.
     * @param password an optional password in case of authentication.
     * @param awsRegion an optional value for the AWS region in case of S3 authentication.
     * @return the read raster from the asset's url.
     * @throws Exception 
     */
    public GridCoverage2D readRaster( RegionMap region, String user, String password, String awsRegion ) throws Exception {
        BasicAuthURI cogUri = new BasicAuthURI(assetUrl, false);
        if (user != null && password != null) {
            cogUri.setUser(user);
            cogUri.setPassword(password);
        }
        if (awsRegion != null) {
            URI uri = cogUri.getUri();
            URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), "region=" + awsRegion, uri.getFragment());
            cogUri.setUri(newUri);
        }
        RangeReader rangeReader = createRangeReader(cogUri);
        CogSourceSPIProvider inputProvider = new CogSourceSPIProvider(cogUri, new CogImageReaderSpi(),
                new CogImageInputStreamSpi(), rangeReader.getClass().getName());
        GeoTiffReader reader = new GeoTiffReader(inputProvider);
        CoordinateReferenceSystem crs = reader.getCoordinateReferenceSystem();

        GeneralParameterValue[] generalParameter = null;
        if (region != null) {
            generalParameter = CoverageUtilities.createGridGeometryGeneralParameter(region, crs);
        }
        GridCoverage2D coverage = reader.read(generalParameter);
        return coverage;
    }

    public GridCoverage2D readRaster( RegionMap region ) throws Exception {
        return readRaster(region, null, null, null);
    }

    public GridCoverage2D readRaster( RegionMap region, String awsRegion ) throws Exception {
        return readRaster(region, null, null, awsRegion);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getNonValidReason() {
        return nonValidReason;
    }

    public String getAssetUrl() {
        return assetUrl;
    }

    public double getNoValue() {
        return noValue;
    }

    public double getResolution() {
        return resolution;
    }
}
