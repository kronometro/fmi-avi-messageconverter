package fi.fmi.avi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.fmi.avi.model.AviationWeatherMessage;

public class JSONTestUtil {

    public static <T extends AviationWeatherMessage> T readFromJSON(final InputStream is, final Class<T> clz) throws IOException {
        T retval = null;
        final ObjectMapper om = new ObjectMapper();
        om.registerModule(new Jdk8Module());
        om.registerModule(new JavaTimeModule());
        if (is != null) {
            retval = om.readValue(is, clz);
        } else {
            throw new NullPointerException("InputStream is null");
        }
        return retval;
    }

    public static void printAsJson(final AviationWeatherMessage msg, final OutputStream out) throws IOException {
        final ObjectMapper om = new ObjectMapper();
        om.registerModule(new Jdk8Module());
        om.registerModule(new JavaTimeModule());
        final ObjectWriter writer = om.writerWithDefaultPrettyPrinter();
        writer.writeValue(out, msg);
    }
}
