package fi.fmi.avi.model.swx.immutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.fmi.avi.model.AviationCodeListUser;
import fi.fmi.avi.model.AviationWeatherMessage;
import fi.fmi.avi.model.NumericMeasure;
import fi.fmi.avi.model.PartialDateTime;
import fi.fmi.avi.model.PartialOrCompleteTimeInstant;
import fi.fmi.avi.model.PolygonGeometry;
import fi.fmi.avi.model.immutable.CircleByCenterPointImpl;
import fi.fmi.avi.model.immutable.CoordinateReferenceSystemImpl;
import fi.fmi.avi.model.immutable.NumericMeasureImpl;
import fi.fmi.avi.model.immutable.PolygonGeometryImpl;
import fi.fmi.avi.model.swx.AirspaceVolume;
import fi.fmi.avi.model.swx.IssuingCenter;
import fi.fmi.avi.model.swx.NextAdvisory;
import fi.fmi.avi.model.swx.SpaceWeatherAdvisory;
import fi.fmi.avi.model.swx.SpaceWeatherAdvisoryAnalysis;
import fi.fmi.avi.model.swx.SpaceWeatherPhenomenon;
import fi.fmi.avi.model.swx.SpaceWeatherRegion;

public class SpaceWeatherAdvisoryTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeClass
    public static void setup() {
        OBJECT_MAPPER.registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
    }

    private AdvisoryNumberImpl getAdvisoryNumber() {
        final AdvisoryNumberImpl.Builder advisory = AdvisoryNumberImpl.builder().setYear(2020).setSerialNumber(1);

        return advisory.build();
    }

    private NextAdvisory getNextAdvisory(final boolean hasNext) {
        final NextAdvisoryImpl.Builder next = NextAdvisoryImpl.builder();

        if (hasNext) {
            final PartialOrCompleteTimeInstant nextAdvisoryTime = PartialOrCompleteTimeInstant.of(ZonedDateTime.parse("2020-02-27T01:00Z[UTC]"));
            next.setTime(nextAdvisoryTime);
            next.setTimeSpecifier(NextAdvisory.Type.NEXT_ADVISORY_AT);
        } else {
            next.setTimeSpecifier(NextAdvisory.Type.NO_FURTHER_ADVISORIES);
        }

        return next.build();
    }

    private List<String> getRemarks() {
        final List<String> remarks = new ArrayList<>();
        remarks.add("RADIATION LVL EXCEEDED 100 PCT OF BACKGROUND LVL AT FL350 AND ABV. THE CURRENT EVENT HAS PEAKED AND LVL SLW RTN TO BACKGROUND LVL."
                + " SEE WWW.SPACEWEATHERPROVIDER.WEB");

        return remarks;
    }

    private List<SpaceWeatherAdvisoryAnalysis> getAnalyses(final boolean hasObservation) {
        final List<SpaceWeatherAdvisoryAnalysis> analyses = new ArrayList<>();

        final int day = 27;
        final int hour = 1;
        for (int i = 0; i < 5; i++) {
            final SpaceWeatherAdvisoryAnalysisImpl.Builder analysis = SpaceWeatherAdvisoryAnalysisImpl.builder();

            final SpaceWeatherRegionImpl.Builder region = SpaceWeatherRegionImpl.builder();

            final String partialTime = "--" + day + "T" + hour + i + ":00Z";
            analysis.setTime(PartialOrCompleteTimeInstant.builder().setPartialTime(PartialDateTime.parse(partialTime)).build());
            region.setAirSpaceVolume(getAirspaceVolume(true));
            region.setLocationIndicator(SpaceWeatherRegion.SpaceWeatherLocation.HIGH_NORTHERN_HEMISPHERE);

            analysis.addAllRegions(
                    Arrays.asList(region.build(), region.setLocationIndicator(SpaceWeatherRegion.SpaceWeatherLocation.MIDDLE_NORTHERN_HEMISPHERE).build()));

            if (i == 0 && hasObservation) {
                analysis.setAnalysisType(SpaceWeatherAdvisoryAnalysis.Type.OBSERVATION);
            } else {
                analysis.setAnalysisType(SpaceWeatherAdvisoryAnalysis.Type.FORECAST);
            }

            analysis.setNilPhenomenonReason(SpaceWeatherAdvisoryAnalysis.NilPhenomenonReason.NO_INFORMATION_AVAILABLE);
            analyses.add(analysis.build());
        }

        return analyses;
    }

    private AirspaceVolume getAirspaceVolume(final boolean isPointGeometry) {
        final AirspaceVolumeImpl.Builder airspaceVolume = AirspaceVolumeImpl.builder();
        airspaceVolume.setUpperLimitReference("Reference");

        if (isPointGeometry) {
            final PolygonGeometry geometry = PolygonGeometryImpl.builder()
                    .addAllExteriorRingPositions(Arrays.asList(-180.0, 90.0, -180.0, 60.0, 180.0, 60.0, 180.0, 90.0, -180.0, 90.0))
                    .setCrs(CoordinateReferenceSystemImpl.wgs84())
                    .build();
            airspaceVolume.setHorizontalProjection(geometry);
        } else {
            final NumericMeasureImpl.Builder measure = NumericMeasureImpl.builder().setValue(5409.75).setUom("[nmi_i]");

            final CircleByCenterPointImpl.Builder cbcp = CircleByCenterPointImpl.builder()
                    .addAllCenterPointCoordinates(Arrays.asList(-16.6392, 160.9368))
                    .setRadius(measure.build())
                    .setCrs(CoordinateReferenceSystemImpl.wgs84());

            airspaceVolume.setHorizontalProjection(cbcp.build());
        }

        final NumericMeasure nm = NumericMeasureImpl.builder().setUom("uom").setValue(Double.valueOf(350)).build();
        airspaceVolume.setUpperLimit(nm);

        return airspaceVolume.build();
    }

    private IssuingCenter getIssuingCenter() {
        final IssuingCenterImpl.Builder issuingCenter = IssuingCenterImpl.builder();
        issuingCenter.setName("DONLON");
        issuingCenter.setType("OTHER:SWXC");
        return issuingCenter.build();
    }

    @Test
    public void buildSWXWithCircleByCenterPoint() throws Exception {
        final NextAdvisoryImpl.Builder nextAdvisory = NextAdvisoryImpl.builder()
                .setTimeSpecifier(NextAdvisory.Type.NEXT_ADVISORY_BY)
                .setTime(PartialOrCompleteTimeInstant.of(ZonedDateTime.parse("2020-02-27T01:00Z[UTC]")));

        final int day = 27;
        final int hour = 1;
        final String partialTime = "--" + day + "T" + hour + ":00Z";

        final List<SpaceWeatherRegion> regions = new ArrayList<>();
        regions.add(SpaceWeatherRegionImpl.builder()
                .setLocationIndicator(SpaceWeatherRegion.SpaceWeatherLocation.HIGH_NORTHERN_HEMISPHERE)
                .setAirSpaceVolume(getAirspaceVolume(false))
                .build());
        regions.add(SpaceWeatherRegionImpl.builder()
                .setLocationIndicator(SpaceWeatherRegion.SpaceWeatherLocation.MIDDLE_NORTHERN_HEMISPHERE)
                .setAirSpaceVolume(getAirspaceVolume(false))
                .build());
        final PartialOrCompleteTimeInstant time = PartialOrCompleteTimeInstant.builder().setPartialTime(PartialDateTime.parse(partialTime)).build();
        final SpaceWeatherAdvisoryAnalysisImpl.Builder analysis = SpaceWeatherAdvisoryAnalysisImpl.builder();
        analysis.setAnalysisType(SpaceWeatherAdvisoryAnalysis.Type.FORECAST)
                .setTime(time)
                .addAllRegions(regions)
                .setNilPhenomenonReason(SpaceWeatherAdvisoryAnalysis.NilPhenomenonReason.NO_INFORMATION_AVAILABLE);

        final List<SpaceWeatherAdvisoryAnalysis> analyses = new ArrayList<>();
        analyses.add(analysis.build());
        analyses.add(analysis.build());
        analyses.add(analysis.build());
        analyses.add(analysis.build());
        analyses.add(analysis.build());

        final SpaceWeatherAdvisoryImpl SWXObject = SpaceWeatherAdvisoryImpl.builder()
                .setIssuingCenter(getIssuingCenter())
                .setIssueTime(PartialOrCompleteTimeInstant.builder().setCompleteTime(ZonedDateTime.parse("2020-02-27T01:00Z[UTC]")).build())
                .setPermissibleUsageReason(AviationCodeListUser.PermissibleUsageReason.TEST)
                .addAllPhenomena(Arrays.asList(SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/HF_COM_MOD"),
                        SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/GNSS_MOD")))
                .setAdvisoryNumber(getAdvisoryNumber())
                .setReplaceAdvisoryNumber(Optional.empty())
                .addAllAnalyses(analyses)
                .setRemarks(getRemarks())
                .setNextAdvisory(nextAdvisory.build())
                .build();

        Assert.assertEquals(1, SWXObject.getAdvisoryNumber().getSerialNumber());
        Assert.assertEquals(2020, SWXObject.getAdvisoryNumber().getYear());
        Assert.assertEquals(SpaceWeatherAdvisoryAnalysis.Type.FORECAST, SWXObject.getAnalyses().get(0).getAnalysisType());
        Assert.assertEquals(NextAdvisory.Type.NEXT_ADVISORY_BY, SWXObject.getNextAdvisory().getTimeSpecifier());
        Assert.assertTrue(SWXObject.getNextAdvisory().getTime().isPresent());

        final String serialized = OBJECT_MAPPER.writeValueAsString(SWXObject);
        final SpaceWeatherAdvisoryImpl deserialized = OBJECT_MAPPER.readValue(serialized, SpaceWeatherAdvisoryImpl.class);
        assertEquals(SWXObject, deserialized);
    }

    @Test
    public void buildSWXWithoutNextAdvisory() throws Exception {
        final SpaceWeatherAdvisoryImpl SWXObject = SpaceWeatherAdvisoryImpl.builder()
                .setIssuingCenter(getIssuingCenter())
                .setIssueTime(PartialOrCompleteTimeInstant.builder().setCompleteTime(ZonedDateTime.parse("2020-02-27T01:00Z[UTC]")).build())
                .setPermissibleUsageReason(AviationCodeListUser.PermissibleUsageReason.TEST)
                .addAllPhenomena(Arrays.asList(SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/HF_COM_MOD"),
                        SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/GNSS_MOD")))
                .setAdvisoryNumber(getAdvisoryNumber())
                .setReplaceAdvisoryNumber(Optional.empty())
                .addAllAnalyses(getAnalyses(true))
                .setRemarks(getRemarks())
                .setNextAdvisory(getNextAdvisory(false))
                .build();

        Assert.assertEquals(1, SWXObject.getAdvisoryNumber().getSerialNumber());
        Assert.assertEquals(2020, SWXObject.getAdvisoryNumber().getYear());
        Assert.assertEquals(SpaceWeatherAdvisoryAnalysis.Type.OBSERVATION, SWXObject.getAnalyses().get(0).getAnalysisType());
        Assert.assertEquals(5, SWXObject.getAnalyses().size());
        Assert.assertFalse(SWXObject.getNextAdvisory().getTime().isPresent());
        Assert.assertEquals(NextAdvisory.Type.NO_FURTHER_ADVISORIES, SWXObject.getNextAdvisory().getTimeSpecifier());

        final String serialized = OBJECT_MAPPER.writeValueAsString(SWXObject);
        final SpaceWeatherAdvisoryImpl deserialized = OBJECT_MAPPER.readValue(serialized, SpaceWeatherAdvisoryImpl.class);

        assertEquals(SWXObject, deserialized);
    }

    @Test
    public void buildSWXWithoutObservation() throws Exception {
        final SpaceWeatherAdvisoryImpl SWXObject = SpaceWeatherAdvisoryImpl.builder()
                .setIssuingCenter(getIssuingCenter())
                .setIssueTime(PartialOrCompleteTimeInstant.builder().setCompleteTime(ZonedDateTime.parse("2020-02-27T01:00Z[UTC]")).build())
                .setPermissibleUsageReason(AviationCodeListUser.PermissibleUsageReason.TEST)
                .addAllAnalyses(getAnalyses(false))
                .addAllPhenomena(Arrays.asList(SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/HF_COM_MOD"),
                        SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/GNSS_MOD")))
                .setAdvisoryNumber(getAdvisoryNumber())
                .setReplaceAdvisoryNumber(Optional.empty())
                .setRemarks(getRemarks())
                .setNextAdvisory(getNextAdvisory(false))
                .build();

        Assert.assertEquals(1, SWXObject.getAdvisoryNumber().getSerialNumber());
        Assert.assertEquals(2020, SWXObject.getAdvisoryNumber().getYear());
        Assert.assertEquals(5, SWXObject.getAnalyses().size());

        Assert.assertFalse(SWXObject.getNextAdvisory().getTime().isPresent());
        Assert.assertEquals(NextAdvisory.Type.NO_FURTHER_ADVISORIES, SWXObject.getNextAdvisory().getTimeSpecifier());

        final String serialized = OBJECT_MAPPER.writeValueAsString(SWXObject);
        final SpaceWeatherAdvisoryImpl deserialized = OBJECT_MAPPER.readValue(serialized, SpaceWeatherAdvisoryImpl.class);

        assertEquals(SWXObject, deserialized);
    }

    @Test
    public void swxSerializationTest() throws Exception {
        final SpaceWeatherAdvisoryImpl SWXObject = SpaceWeatherAdvisoryImpl.builder()
                .setIssuingCenter(getIssuingCenter())
                .setIssueTime(PartialOrCompleteTimeInstant.builder().setCompleteTime(ZonedDateTime.parse("2020-02-27T01:00Z[UTC]")).build())
                .setPermissibleUsageReason(AviationCodeListUser.PermissibleUsageReason.TEST)
                .setReplaceAdvisoryNumber(getAdvisoryNumber())
                .addAllPhenomena(Arrays.asList(SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/HF_COM_MOD"),
                        SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/GNSS_MOD")))
                .setAdvisoryNumber(getAdvisoryNumber())
                .setReplaceAdvisoryNumber(Optional.empty())
                .addAllAnalyses(getAnalyses(true))
                .setRemarks(getRemarks())
                .setNextAdvisory(getNextAdvisory(true))
                .setReportStatus(AviationWeatherMessage.ReportStatus.NORMAL)
                .build();

        final String serialized = OBJECT_MAPPER.writeValueAsString(SWXObject);
        final SpaceWeatherAdvisoryImpl deserialized = OBJECT_MAPPER.readValue(serialized, SpaceWeatherAdvisoryImpl.class);

        assertEquals(SWXObject, deserialized);
    }

    @Test
    public void swxPartialTimeCompletionTest() {
        final NextAdvisory partialNextAdvisory = NextAdvisoryImpl.builder()//
                .setTimeSpecifier(NextAdvisory.Type.NEXT_ADVISORY_AT)//
                .setTime(PartialOrCompleteTimeInstant.builder().setPartialTime(PartialDateTime.ofDayHour(28, 2)).build())//
                .build();

        final SpaceWeatherAdvisoryImpl advisory = SpaceWeatherAdvisoryImpl.builder()
                .setIssuingCenter(getIssuingCenter())
                .setIssueTime(PartialOrCompleteTimeInstant.builder().setPartialTime(PartialDateTime.ofDayHour(27, 1)).build())
                .setPermissibleUsageReason(AviationCodeListUser.PermissibleUsageReason.TEST)
                .setReplaceAdvisoryNumber(getAdvisoryNumber())
                .addAllPhenomena(Arrays.asList(SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/HF_COM_MOD"),
                        SpaceWeatherPhenomenon.fromWMOCodeListValue("http://codes.wmo.int/49-2/SpaceWxPhenomena/GNSS_MOD")))
                .setAdvisoryNumber(getAdvisoryNumber())
                .setReplaceAdvisoryNumber(Optional.empty())
                .addAllAnalyses(getAnalyses(true))
                .setRemarks(getRemarks())
                .setNextAdvisory(partialNextAdvisory)
                .setReportStatus(AviationWeatherMessage.ReportStatus.NORMAL)
                .build();

        final SpaceWeatherAdvisory completedAdvisory = advisory.toBuilder().withAllTimesComplete(ZonedDateTime.parse("2020-02-27T00:00Z[UTC]")).build();
        assertTrue(completedAdvisory.areAllTimeReferencesComplete());
    }
}