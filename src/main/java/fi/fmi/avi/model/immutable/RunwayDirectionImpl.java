package fi.fmi.avi.model.immutable;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Optional;

import org.inferred.freebuilder.FreeBuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fi.fmi.avi.model.RunwayDirection;

/**
 * Created by rinne on 17/04/2018.
 */
@FreeBuilder
@JsonDeserialize(builder = RunwayDirectionImpl.Builder.class)
public abstract class RunwayDirectionImpl implements RunwayDirection, Serializable {

    public static RunwayDirectionImpl immutableCopyOf(final RunwayDirection runwayDirection) {
        checkNotNull(runwayDirection);
        if (runwayDirection instanceof RunwayDirectionImpl) {
            return (RunwayDirectionImpl) runwayDirection;
        } else {
            return Builder.from(runwayDirection).build();
        }
    }

    public static Optional<RunwayDirectionImpl> immutableCopyOf(final Optional<RunwayDirection> runwayDirection) {
        checkNotNull(runwayDirection);
        return runwayDirection.map(RunwayDirectionImpl::immutableCopyOf);
    }

    abstract Builder toBuilder();

    public static class Builder extends RunwayDirectionImpl_Builder {

        public static Builder from(final RunwayDirection value) {
            return new RunwayDirectionImpl.Builder().setDesignator(value.getDesignator())
                    .setElevationTDZMeters(value.getElevationTDZMeters())
                    .setTrueBearing(value.getTrueBearing())
                    .setAssociatedAirportHeliport(AerodromeImpl.immutableCopyOf(value.getAssociatedAirportHeliport()));

        }
    }
}
