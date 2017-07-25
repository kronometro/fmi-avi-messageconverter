package fi.fmi.avi.converter.iwxxm;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import aero.aixm511.AirportHeliportTimeSlicePropertyType;
import aero.aixm511.AirportHeliportTimeSliceType;
import aero.aixm511.AirportHeliportType;
import aero.aixm511.CodeAirportHeliportDesignatorType;
import aero.aixm511.CodeIATAType;
import aero.aixm511.CodeICAOType;
import aero.aixm511.ElevatedPointPropertyType;
import aero.aixm511.ElevatedPointType;
import aero.aixm511.TextNameType;
import aero.aixm511.ValDistanceVerticalType;
import fi.fmi.avi.converter.ConversionIssue;
import fi.fmi.avi.converter.ConversionResult;
import fi.fmi.avi.model.Aerodrome;
import fi.fmi.avi.model.AviationCodeListUser;
import fi.fmi.avi.model.CloudForecast;
import fi.fmi.avi.model.CloudLayer;
import fi.fmi.avi.model.GeoPosition;
import fi.fmi.avi.model.NumericMeasure;
import icao.iwxxm21.AerodromeCloudForecastType;
import icao.iwxxm21.AngleWithNilReasonType;
import icao.iwxxm21.CloudAmountReportedAtAerodromeType;
import icao.iwxxm21.CloudLayerType;
import icao.iwxxm21.DistanceWithNilReasonType;
import icao.iwxxm21.LengthWithNilReasonType;
import icao.iwxxm21.SigConvectiveCloudTypeType;
import net.opengis.gml32.DirectPositionType;
import net.opengis.gml32.FeaturePropertyType;
import net.opengis.gml32.LengthType;
import net.opengis.gml32.MeasureType;
import net.opengis.gml32.AbstractGeometryType;
import net.opengis.gml32.AngleType;
import net.opengis.gml32.PointType;
import net.opengis.gml32.ReferenceType;
import net.opengis.gml32.SpeedType;
import net.opengis.gml32.TimePrimitivePropertyType;
import net.opengis.om20.OMObservationType;
import net.opengis.sampling.spatial.SFSpatialSamplingFeatureType;
import net.opengis.sampling.spatial.ShapeType;

/**
 * Created by rinne on 20/07/17.
 */
public abstract class AerodromeMessageIWXXMSerializer extends IWXXMConverter {

  public static final int MAX_CLOUD_LAYERS = 4;

  @SuppressWarnings("unchecked")
  protected void updateSamplingFeature(final Aerodrome input, final OMObservationType target, final String foiId, final String aerodromeId,
      final ConversionResult<?> result) {
    if (input == null) {
      throw new IllegalArgumentException("Aerodrome info is null");
    }
    
    target.setFeatureOfInterest(create(FeaturePropertyType.class, (prop) -> {
      prop.setAbstractFeature(createAndWrap(SFSpatialSamplingFeatureType.class, (samsFeature) -> {
        samsFeature.setId(foiId);
        samsFeature.setType(create(ReferenceType.class, (ref) -> {
          ref.setHref("http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint");
          ref.setTitle("Sampling point");
        }));
        
        samsFeature.getSampledFeature().add(create(FeaturePropertyType.class, (samProp) -> {
          AirportHeliportType aerodrome = create(AirportHeliportType.class);
          this.setAerodromeData(aerodrome, input, aerodromeId);
          samProp.setAbstractFeature(wrap(aerodrome, AirportHeliportType.class));
        }));
        
        if (input.getReferencePoint() != null) {
          samsFeature.setShape(create(ShapeType.class, (shape) -> {
            JAXBElement<?> wrapped = wrap(create(PointType.class, (point) -> {
              GeoPosition inputPos = input.getReferencePoint();
              point.setId("point-" + UUID.randomUUID().toString());
              point.setSrsName(inputPos.getCoordinateReferenceSystemId());
              if (inputPos.getCoordinates() != null) {
                point.setSrsDimension(BigInteger.valueOf(inputPos.getCoordinates().length));
                point.setPos(create(DirectPositionType.class, (pos) ->  pos.getValue().addAll(Arrays.asList(inputPos.getCoordinates()))));
              }
            }), PointType.class);
            
            /*
             * Something is not right here in either the schema or the JAXB bindings:
             * 
             * The method should be 
             *    shape.setAbstractGeometry(JAXBElement<? extends AbstractGeometry>)
             * but it's generated as
             *    shape.setAbstractGeometry(JAXBElement<AbstractGeometry>)
             *    
             * Have to work around it with an unsafe cast:
             */
            shape.setAbstractGeometry((JAXBElement<AbstractGeometryType>)wrapped);  
          }));
        }
      }));
    }));
  }

  protected void setAerodromeData(final AirportHeliportType aerodrome, final Aerodrome input, final String aerodromeId) {
    if (input == null) {
      return;
    }
    aerodrome.setId(aerodromeId);
    aerodrome.getTimeSlice().add(create(AirportHeliportTimeSlicePropertyType.class, (prop) -> {
      prop.setAirportHeliportTimeSlice(create(AirportHeliportTimeSliceType.class, (timeSlice) -> {
        timeSlice.setId("aerodrome-" + UUID.randomUUID().toString());
        timeSlice.setValidTime(create(TimePrimitivePropertyType.class));
        timeSlice.setInterpretation("SNAPSHOT");
        timeSlice.setDesignator(create(CodeAirportHeliportDesignatorType.class, (designator) -> {
          designator.setValue(input.getDesignator());
        }));
        if (input.getName() != null) {
          timeSlice.setPortName(create(TextNameType.class, 
            (name) -> name.setValue(input.getName().toUpperCase())));
        }
        if (input.getLocationIndicatorICAO() != null) {
          timeSlice.setLocationIndicatorICAO(create(CodeICAOType.class, 
              (locator) -> locator.setValue(input.getLocationIndicatorICAO())));
        }

        if (input.getDesignatorIATA() != null) {
          timeSlice.setDesignatorIATA(create(CodeIATAType.class, 
              (designator) -> designator.setValue(input.getDesignatorIATA())));
        }

        if (input.getFieldElevationValue() != null) {
          timeSlice.setFieldElevation(create(ValDistanceVerticalType.class, (elevation) -> {
            elevation.setValue(String.format("%.00f",input.getFieldElevationValue()));
            elevation.setUom("M");
          }));
        }

        if (input.getReferencePoint() != null) {
          timeSlice.setARP(create(ElevatedPointPropertyType.class, (pointProp) -> {
            pointProp.setElevatedPoint(create(ElevatedPointType.class, (point) -> {
              GeoPosition inputPos = input.getReferencePoint();
              point.setId("point-" + UUID.randomUUID().toString());
              point.setSrsName(inputPos.getCoordinateReferenceSystemId());
              if (inputPos.getCoordinates() != null) {
                point.setSrsDimension(BigInteger.valueOf(inputPos.getCoordinates().length));
                point.setPos(create(DirectPositionType.class, (pos) ->  pos.getValue().addAll(Arrays.asList(inputPos.getCoordinates()))));
              }
              if (inputPos.getElevationValue() != null) {
                point.setElevation(create(ValDistanceVerticalType.class, (dist) -> {
                  dist.setValue(String.format("%.00f", inputPos.getElevationValue().doubleValue()));
                  dist.setUom(inputPos.getElevationUom().toUpperCase());
                }));
              }
            }));
          }));
        }

      }));
    }));
  }

  protected MeasureType asMeasure(final NumericMeasure source) {
    return asMeasure(source, MeasureType.class);
  }

  @SuppressWarnings("unchecked")
  protected <T extends MeasureType> T asMeasure(final NumericMeasure source, final Class<T> clz) {
    T retval = null;
    if (source != null) {
      if (SpeedType.class.isAssignableFrom(clz)) {
        retval = (T) create(SpeedType.class);
      } else if (AngleWithNilReasonType.class.isAssignableFrom(clz)) {
        retval = (T) create(AngleWithNilReasonType.class);
      } else if (AngleType.class.isAssignableFrom(clz)) {
        retval = (T) create(AngleType.class);
      } else if (DistanceWithNilReasonType.class.isAssignableFrom(clz)) {
        retval = (T) create(DistanceWithNilReasonType.class);
      } else if (LengthWithNilReasonType.class.isAssignableFrom(clz)) {
        retval = (T) create(LengthWithNilReasonType.class);
      } else if (LengthType.class.isAssignableFrom(clz)) {
        retval = (T) create(LengthType.class);
      } else {
        retval = (T) create(MeasureType.class);
      }
      retval.setValue(source.getValue());
      retval.setUom(source.getUom());
    } else {
      throw new IllegalArgumentException("NumericMeasure is null");
    }
    return retval;
  }

  protected void updateForecastClouds(final CloudForecast source, final AerodromeCloudForecastType target,  final ConversionResult<?> result) {
    if (source != null) {
      target.setId("cfct-" + UUID.randomUUID().toString());
      NumericMeasure measure = source.getVerticalVisibility();
      if (measure != null) {
        target.setVerticalVisibility(wrap(asMeasure(measure, LengthWithNilReasonType.class), LengthWithNilReasonType.class));
      }
      if (source.getLayers().size() > 0) {
        if (source.getLayers().size() <= MAX_CLOUD_LAYERS) {
          for (CloudLayer layer : source.getLayers()) {
            target.getLayer().add(create(AerodromeCloudForecastType.Layer.class, (l) -> {
              l.setCloudLayer(create(CloudLayerType.class, (cl) -> this.setCloudLayerData(cl, layer)));
            }));
          }
        } else {
          result.addIssue(new ConversionIssue(ConversionIssue.Type.SYNTAX_ERROR, "Found " + source.getLayers().size() + " cloud forecast "
              + "layers, the maximum number in IWXXM is " + MAX_CLOUD_LAYERS));
        }
      }
    }

  }

  protected void setCloudLayerData(final CloudLayerType target, final CloudLayer source) {
    if (source != null) {
      target.setBase(asMeasure(source.getBase(), DistanceWithNilReasonType.class));
      target.setAmount(create(CloudAmountReportedAtAerodromeType.class, (amount) -> {
        amount.setHref(AviationCodeListUser.CODELIST_VALUE_PREFIX_CLOUD_AMOUNT_REPORTED_AT_AERODROME + source.getAmount().getCode());
        amount.setTitle(source.getAmount().name() + ", from codelist " + AviationCodeListUser.CODELIST_CLOUD_AMOUNT_REPORTED_AT_AERODROME);
      }));
      AviationCodeListUser.CloudType type = source.getCloudType();
      if (type != null) {
        QName eName = new QName("http://icao.int/iwxxm/2.1", "cloudType");
        SigConvectiveCloudTypeType cloudType = create(SigConvectiveCloudTypeType.class, (convCloud) -> {
          convCloud.setHref(AviationCodeListUser.CODELIST_VALUE_PREFIX_SIG_CONVECTIVE_CLOUD_TYPE + type.getCode());
          convCloud.setTitle(type.name() + ", from codelist " + AviationCodeListUser.CODELIST_SIGNIFICANT_CONVECTIVE_CLOUD_TYPE);
        });        
        target.setCloudType(new JAXBElement<SigConvectiveCloudTypeType>(eName, SigConvectiveCloudTypeType.class, cloudType));
      }
    }
  }
}
