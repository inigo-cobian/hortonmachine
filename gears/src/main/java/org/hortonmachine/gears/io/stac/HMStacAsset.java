package org.hortonmachine.gears.io.stac;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.davidmoten.aws.lw.client.Client;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.RegionMap;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.jetbrains.annotations.NotNull;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.fasterxml.jackson.databind.JsonNode;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.CogImageInputStreamSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogSourceSPIProvider;
import it.geosolutions.imageioimpl.plugins.cog.HttpRangeReader;

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

    private Credentials credentials;

    private class Credentials {
        enum Schema {
            NONE, PASSWORD, S3
        }
        private Schema schema = Schema.NONE;
        private List<String> userPassword;
        private Client s3Client;

        public void setPassword(String user, String password) {
            schema = Schema.PASSWORD;
            userPassword = List.of(user, password);
        }

        public void setS3Client(Client s3Client) {
            schema = Schema.S3;
            this.s3Client = s3Client;
        }
    }

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
            if (HMStacUtils.ACCEPTED_TYPES.contains(type.toLowerCase().replace(" ", ""))) {
                assetUrl = assetNode.get("href").textValue();

                JsonNode rasterBandNode = assetNode.get("raster:bands");
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
                nonValidReason = "not a valid type";
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

    public HMStacAsset setCredentials(String user, String password) {
        credentials.setPassword(user, password);
        return this;
    }

    public HMStacAsset setCredentials(Client s3Client) {
        credentials.setS3Client(s3Client);
        return this;
    }

    @NotNull
    private static GeoTiffReader readHTTPUrl(BasicAuthURI cogUri) throws DataSourceException {
        HttpRangeReader rangeReader = new HttpRangeReader(cogUri.getUri(), CogImageReadParam.DEFAULT_HEADER_LENGTH);
        CogSourceSPIProvider inputProvider = new CogSourceSPIProvider(cogUri, new CogImageReaderSpi(),
                new CogImageInputStreamSpi(), rangeReader.getClass().getName());
        return new GeoTiffReader(inputProvider);
    }

    @NotNull
    private GeoTiffReader readS3Object() throws DataSourceException {
        byte[] contentBytes = this.credentials.s3Client.url(assetUrl).responseAsBytes();
        return new GeoTiffReader(new ByteArrayInputStream(contentBytes));
    }

    /**
     * Read the asset's coverage into a local raster.
     *
     * @param region and optional region to read from.
     * @return the read raster from the asset's url..
     * @throws Exception
     */
    public GridCoverage2D readRaster( RegionMap region ) throws Exception {
        BasicAuthURI cogUri = new BasicAuthURI(assetUrl, false);
        if (credentials.schema == Credentials.Schema.PASSWORD) {
            List<String> userPassword = credentials.userPassword;
            cogUri.setUser(userPassword.get(0));
            cogUri.setPassword(userPassword.get(1));
        }
        GeoTiffReader reader = credentials.schema == Credentials.Schema.S3 && assetUrl.startsWith("s3://") ?
                readS3Object() : readHTTPUrl(cogUri);
        CoordinateReferenceSystem crs = reader.getCoordinateReferenceSystem();

        GeneralParameterValue[] generalParameter = null;
        if (region != null) {
            generalParameter = CoverageUtilities.createGridGeometryGeneralParameter(region, crs);
        }
        GridCoverage2D coverage = reader.read(generalParameter);
        return coverage;
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
