package fi.fmi.avi.data.metar.impl;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fi.fmi.avi.data.CloudForecast;
import fi.fmi.avi.data.NumericMeasure;
import fi.fmi.avi.data.impl.CloudForecastImpl;
import fi.fmi.avi.data.impl.NumericMeasureImpl;
import fi.fmi.avi.data.impl.PossiblyMissingContentImpl;
import fi.fmi.avi.data.metar.TrendForecast;
import fi.fmi.avi.data.metar.TrendForecastSurfaceWind;
import fi.fmi.avi.data.metar.TrendTimeGroups;

/**
 * 
 */

public class TrendForecastImpl extends PossiblyMissingContentImpl implements TrendForecast {

    private List<String> timeGroups;
    private boolean ceilingAndVisibilityOk;
    private TrendForecastChangeIndicator changeIndicator;
    private NumericMeasure prevailingVisibility;
    private RelationalOperator prevailingVisibilityOperator;
    private TrendForecastSurfaceWind surfaceWind;
    private List<String> forecastWeather;
    private CloudForecast cloud;

    public TrendForecastImpl() {
    }

    public TrendForecastImpl(final TrendForecast input) {
        super(input.getMissingReason());
        this.timeGroups = input.getTimeGroups();
        this.ceilingAndVisibilityOk = input.isCeilingAndVisibilityOk();
        this.changeIndicator = input.getChangeIndicator();
        this.prevailingVisibility = new NumericMeasureImpl(input.getPrevailingVisibility());
        this.prevailingVisibilityOperator = input.getPrevailingVisibilityOperator();
        this.surfaceWind = new TrendForecastSurfaceWindImpl(input.getSurfaceWind());
        this.forecastWeather = new ArrayList<String>(input.getForecastWeather());
        this.cloud = new CloudForecastImpl(input.getCloud());
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#getTimeGroups()
     */
    @Override
    public List<String> getTimeGroups() {
        return this.timeGroups;
    }

    @Override
    public TrendTimeGroups getParsedTimeGroups() {
        TrendTimeGroups retval = null;
        if (this.timeGroups != null) {
            retval = new TrendTimeGroups(this.timeGroups);
        }
        return retval;
    }

    /* (non-Javadoc)
         * @see fi.fmi.avi.data.TrendForecast#isCeilingAndVisibilityOk()
         */
    @Override
    public boolean isCeilingAndVisibilityOk() {
        return ceilingAndVisibilityOk;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#getChangeIndicator()
     */
    @Override
    public TrendForecastChangeIndicator getChangeIndicator() {
        return changeIndicator;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#getPrevailingVisibility()
     */
    @Override
    public NumericMeasure getPrevailingVisibility() {
        return prevailingVisibility;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#getPrevailingVisibilityOperator()
     */
    @Override
    public RelationalOperator getPrevailingVisibilityOperator() {
        return prevailingVisibilityOperator;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#getSurfaceWind()
     */
    @Override
    public TrendForecastSurfaceWind getSurfaceWind() {
        return surfaceWind;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#getForecastWeather()
     */
    @Override
    public List<String> getForecastWeather() {
        return forecastWeather;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#getCloud()
     */
    @Override
    public CloudForecast getCloud() {
        return cloud;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#setTimeGroups(fi.fmi.avi.data.TrendForecastImpl.TimeGroups)
     */
    
    @Override
    public void setTimeGroups(final List<String> timeGroups) {
        this.timeGroups = timeGroups;
    }
    

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#setCeilingAndVisibilityOk(boolean)
     */
    @Override
    public void setCeilingAndVisibilityOk(final boolean ceilingAndVisibilityOk) {
        this.ceilingAndVisibilityOk = ceilingAndVisibilityOk;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#setChangeIndicator(fi.fmi.avi.data.AviationCodeListUser.ForecastChangeIndicator)
     */
    @Override
    public void setChangeIndicator(final TrendForecastChangeIndicator changeIndicator) {
        this.changeIndicator = changeIndicator;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#setPrevailingVisibility(fi.fmi.avi.data.NumericMeasure)
     */
    @Override
    @JsonDeserialize(as = NumericMeasureImpl.class)
    public void setPrevailingVisibility(final NumericMeasure prevailingVisibility) {
        this.prevailingVisibility = prevailingVisibility;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#setPrevailingVisibilityOperator(fi.fmi.avi.data.AviationCodeListUser.RelationalOperator)
     */
    @Override
    public void setPrevailingVisibilityOperator(final RelationalOperator prevailingVisibilityOperator) {
        this.prevailingVisibilityOperator = prevailingVisibilityOperator;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#setSurfaceWind(fi.fmi.avi.data.TrendForecastSurfaceWindImpl)
     */
    @Override
    @JsonDeserialize(as = TrendForecastSurfaceWindImpl.class)
    public void setSurfaceWind(final TrendForecastSurfaceWind surfaceWind) {
        this.surfaceWind = surfaceWind;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#setForecastWeather(java.util.List)
     */
    @Override
    public void setForecastWeather(final List<String> forecastWeather) {
        this.forecastWeather = forecastWeather;
    }

    /* (non-Javadoc)
     * @see fi.fmi.avi.data.TrendForecast#setCloud(fi.fmi.avi.data.CloudForecast)
     */
    @Override
    @JsonDeserialize(as = CloudForecastImpl.class)
    public void setCloud(final CloudForecast cloud) {
        this.cloud = cloud;
    }

}