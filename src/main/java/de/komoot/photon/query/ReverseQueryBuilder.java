package de.komoot.photon.query;



import com.google.common.collect.ImmutableSet;
import com.vividsolutions.jts.geom.Point;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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

    private Boolean searchPolygon;



    private ReverseQueryBuilder(Point location, Double radius, String queryStringFilter, Boolean searchPolygon)
    {
        this.location = location;
        this.radius = radius;
        this.queryStringFilter = queryStringFilter;
        this.searchPolygon = searchPolygon;
    }



    public static TagFilterQueryBuilder builder(Point location, Double radius, String queryStringFilter, Boolean searchInPolygon)
    {
        return new ReverseQueryBuilder(location, radius, queryStringFilter, searchInPolygon);
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
        return this.withKeys(ImmutableSet.<String> builder().add(keys).build());
    }



    @Override
    public TagFilterQueryBuilder withValues(String... values)
    {
        return this.withValues(ImmutableSet.<String> builder().add(values).build());
    }



    @Override
    public TagFilterQueryBuilder withoutKeys(String... keysToExclude)
    {
        return this.withoutKeys(ImmutableSet.<String> builder().add(keysToExclude).build());
    }



    @Override
    public TagFilterQueryBuilder withoutValues(String... valuesToExclude)
    {
        return this.withoutValues(ImmutableSet.<String> builder().add(valuesToExclude).build());
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
        // the extend.coordinates are the corners of the bbox from nominatim (minX/maxY + maxX/minY), the bbox is ST_Envelope(geometry) AS bbox in the nominatim sql query
        // the basis coordinate is the 'centroid' from Nominatim
        // the new attribute 'polygon' comprises all the points from the entity, extracted in sql with st_astext (geometry) as wkttext. It enhances the accuracy e.g. for
        // searching ways. Best would be to index it as geoshape, but this didn't work in embedded mode. Skipping the embedded mode would enable this. 

        BoolQueryBuilder distanceFilterFinal = QueryBuilders.boolQuery().minimumShouldMatch(1);

        QueryBuilder filter4coordinate = QueryBuilders.geoDistanceQuery("coordinate").point(location.getY(), location.getX()).distance(radius, DistanceUnit.KILOMETERS);
        distanceFilterFinal.should(filter4coordinate);

        if(searchPolygon)
        {
            QueryBuilder filter4polygon =
                    QueryBuilders.geoDistanceQuery("polygon.coordinates").point(location.getY(), location.getX()).distance(radius, DistanceUnit.KILOMETERS);
            distanceFilterFinal.should(filter4polygon);
        }


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
