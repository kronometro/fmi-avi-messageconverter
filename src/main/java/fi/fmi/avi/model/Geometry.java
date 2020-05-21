package fi.fmi.avi.model;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import fi.fmi.avi.model.immutable.CircleByCenterPointImpl;
import fi.fmi.avi.model.immutable.PointGeometryImpl;
import fi.fmi.avi.model.immutable.PolygonsGeometryImpl;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = PointGeometryImpl.class, name = "Point"), @JsonSubTypes.Type(value = PolygonsGeometryImpl.class, name = "Polygon"),
        @JsonSubTypes.Type(value = CircleByCenterPointImpl.class, name = "CircleByCenterPoint") })

public interface Geometry {

    Optional<String> getSrsName();

    Optional<BigInteger> getSrsDimension();

    Optional<List<String>> getAxisLabels();
}
