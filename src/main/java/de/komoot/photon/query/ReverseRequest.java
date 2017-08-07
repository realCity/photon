package de.komoot.photon.query;

import com.vividsolutions.jts.geom.Point;
import java.io.Serializable;

/**
 *
 * @author svantulden
 */
public class ReverseRequest implements Serializable {
    private Point location;
    private String language;
    private Double radius;
    private Integer limit;
    private String queryStringFilter;
    private Boolean locationDistanceSort = true;
    private Boolean searchPolygon = false;
    private Boolean returnPolygon = false;
    
    

    public ReverseRequest(Point location, String language, Double radius, Boolean searchPolygon, String queryStringFilter, Integer limit, 
            Boolean locationDistanceSort, Boolean returnPolygon){
        this.location = location;
        this.language = language;
        this.radius = radius;
        this.searchPolygon = searchPolygon;
        this.limit = limit;
        this.queryStringFilter = queryStringFilter;
        this.locationDistanceSort = locationDistanceSort;
        this.returnPolygon = returnPolygon;
    }

    public Point getLocation() {
        return location;
    }

    public String getLanguage() {
        return language;
    }

    public Double getRadius() {
        return radius;
    }
    
    public Boolean getSearchPolygon() {
        return searchPolygon;
    }

    public Integer getLimit() {
        return limit;
    }
    
    public String getQueryStringFilter(){
        return queryStringFilter;
    }
    
    public Boolean getLocationDistanceSort() {
        return locationDistanceSort;
    }
    
    public Boolean getReturnPolygon() {
        return returnPolygon;
    }
}
