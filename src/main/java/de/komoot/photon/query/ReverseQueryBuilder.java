package de.komoot.photon.query;



import com.google.common.collect.ImmutableSet;
import com.vividsolutions.jts.geom.Point;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.apache.lucene.index.Fields;
import org.elasticsearch.common.unit.DistanceUnit;



/**
 *
 * @author svantulden
 */
public class ReverseQueryBuilder implements TagFilterQueryBuilder
{
    private Integer limit;

    private Double radius;

    private Point location;

    private String queryStringFilter;

    private Boolean searchExtend;

    private Boolean searchPolygon;



    private ReverseQueryBuilder(Point location, Double radius, String queryStringFilter, Boolean searchExtend, Boolean searchPolygon)
    {
        this.location = location;
        this.radius = radius;
        this.queryStringFilter = queryStringFilter;
        this.searchExtend = searchExtend;
        this.searchPolygon = searchPolygon;
    }



    public static TagFilterQueryBuilder builder(Point location, Double radius, String queryStringFilter, Boolean searchInExtend, Boolean searchInPolygon)
    {
        return new ReverseQueryBuilder(location, radius, queryStringFilter, searchInExtend, searchInPolygon);
    }



    @Override
    public TagFilterQueryBuilder withLimit(Integer limit)
    {
        this.limit = limit == null || limit < 0 ? 0 : limit;
        this.limit = this.limit > 5000 ? 5000 : this.limit;

        return this;
    }



    @Override
    public TagFilterQueryBuilder withLocationBias(Point point, Boolean locationDistanceSort)
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public TagFilterQueryBuilder withTags(Map<String, Set<String>> tags)
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public TagFilterQueryBuilder withKeys(Set<String> keys)
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public TagFilterQueryBuilder withValues(Set<String> values)
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public TagFilterQueryBuilder withTagsNotValues(Map<String, Set<String>> tags)
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public TagFilterQueryBuilder withoutTags(Map<String, Set<String>> tagsToExclude)
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public TagFilterQueryBuilder withoutKeys(Set<String> keysToExclude)
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public TagFilterQueryBuilder withoutValues(Set<String> valuesToExclude)
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public TagFilterQueryBuilder withKeys(String... keys)
    {
        return this.withKeys(ImmutableSet.<String>builder().add(keys).build());
    }



    @Override
    public TagFilterQueryBuilder withValues(String... values)
    {
        return this.withValues(ImmutableSet.<String>builder().add(values).build());
    }



    @Override
    public TagFilterQueryBuilder withoutKeys(String... keysToExclude)
    {
        return this.withoutKeys(ImmutableSet.<String>builder().add(keysToExclude).build());
    }



    @Override
    public TagFilterQueryBuilder withoutValues(String... valuesToExclude)
    {
        return this.withoutValues(ImmutableSet.<String>builder().add(valuesToExclude).build());
    }



    @Override
    public TagFilterQueryBuilder withStrictMatch()
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public TagFilterQueryBuilder withLenientMatch()
    {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }



    @Override
    public QueryBuilder buildQuery()
    {
        // TODO wir suchen gar nicht in den extend-Koordinaten, was bei Wegen extrem schlecht ist
        // "extent.coordinates" oder "coordinates" muß noch dazu in der query! TODO das ist noch nicht im mapping => gefixed
        // die extend.coordinates sind die Ecken der bbox aus nominatim (minX/maxY + maxX/minY
        // die bbox ist ST_Envelope(geometry) AS bbox
        // die Grundkoordinate ist die Spalte 'centroid' aus Nominatim
        // die kompletten Punkte einer Straße finden sich vermutlich mit st_astext (geometry) as wkttext . Extraktion aus DB in NominatimConnector, eingefügt in
        // PhotonDoc, import in ES in Utils, mapping angepasst, das attribut heißt nun 'polygon'

        BoolQueryBuilder distanceFilterFinal = QueryBuilders.boolQuery().minimumShouldMatch(1);
        
        QueryBuilder filter4coordinate = QueryBuilders.geoDistanceQuery("coordinate").point(location.getY(), location.getX()).distance(radius, DistanceUnit.KILOMETERS);
        distanceFilterFinal.should(filter4coordinate);
        
        if(searchExtend)
        {
            QueryBuilder filter4extend =
                    QueryBuilders.geoDistanceQuery("extent.coordinates").point(location.getY(), location.getX()).distance(radius, DistanceUnit.KILOMETERS);
            distanceFilterFinal.should(filter4extend);
        }
        if(searchPolygon)
        {
            QueryBuilder filter4polygon = QueryBuilders.geoDistanceQuery("polygon").point(location.getY(), location.getX()).distance(radius, DistanceUnit.KILOMETERS);
            distanceFilterFinal.should(filter4polygon);
        }


        // hier noch die query erweitern mit den zusätzlichen Fields evtl. den Filter zu ner boolean-should umschinken
        BoolQueryBuilder finalQuery;

        if(queryStringFilter != null && queryStringFilter.trim().length() > 0)
            finalQuery = QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery(queryStringFilter)).filter(distanceFilterFinal);
        else
            finalQuery = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery()).filter(distanceFilterFinal);


        return finalQuery;
    }



    @Override
    public Integer getLimit()
    {
        return limit;
    }



    private Boolean checkTags(Set<String> keys)
    {
        return !(keys == null || keys.isEmpty());
    }



    private Boolean checkTags(Map<String, Set<String>> tags)
    {
        return !(tags == null || tags.isEmpty());
    }




    private enum State {
        PLAIN, FILTERED, QUERY_ALREADY_BUILT, FINISHED,
    }
}
