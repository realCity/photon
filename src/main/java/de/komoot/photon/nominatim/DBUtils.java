package de.komoot.photon.nominatim;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Nullable;

import org.openstreetmap.osmosis.hstore.PGHStore;
import org.postgis.jts.JtsGeometry;

import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * utility functions to parse data from postgis
 *
 * @author christoph
 */
public class DBUtils {
    
    static public final WKTReader wktReader = new WKTReader(); 
    
	public static Map<String, String> getMap(ResultSet rs, String columnName) throws SQLException {
		Map<String, String> tags = Maps.newHashMap();

		PGHStore dbTags = (PGHStore) rs.getObject(columnName);
		if(dbTags != null) {
			for(Map.Entry<String, String> tagEntry : dbTags.entrySet()) {
				tags.put(tagEntry.getKey(), tagEntry.getValue());
			}
		}

		return tags;
	}

	@Nullable
	public static <T extends Geometry> T extractGeometry(ResultSet rs, String columnName) throws SQLException {
	    
	    Object geom = rs.getObject(columnName);
	    try
	    {
    		
    		if(geom == null) {
    			//info("no geometry found in column " + columnName);
    			return null;
    		}
    		else if(geom instanceof JtsGeometry) {
    		    return (T) ((JtsGeometry)geom).getGeometry();
    		}
    		else {
    		    if(geom.toString().trim().length() == 0)
    		        return null;
    		    else
    		        return (T) wktReader.read(geom.toString());
            }
	    }
	    catch(ParseException e) {
	        throw new RuntimeException(e);   
	    }
	}
	

}
