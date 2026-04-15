package rip.ada.groups.templates;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Templates {

    private static final Logger LOGGER = LoggerFactory.getLogger(Templates.class);

    public static PebbleEngine create() {
        final ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates/");
        loader.setSuffix(".pebble");
        return new PebbleEngine.Builder()
                .loader(loader)
                .build();
    }

    public static Map<String, Object> model(final Context ctx) {
        Map<String, Object> model = ctx.attribute("_model");
        if (model == null) {
            model = new HashMap<>();
            ctx.attribute("_model", model);
        }
        return model;
    }

    public static void render(final PebbleEngine engine, final String template, final Context ctx) {
        try {
            final PebbleTemplate t = engine.getTemplate(template);
            final StringWriter writer = new StringWriter();
            t.evaluate(writer, model(ctx));
            ctx.html(writer.toString());
        } catch (final IOException e) {
            throw new RuntimeException("Failed to render template: " + template, e);
        }
    }

    public static void renderStackTrace(final PebbleEngine engine, final Context ctx, final Throwable throwable) {
        LOGGER.error("Unhandled exception in handler", throwable);

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        Throwable cause = throwable.getCause();
        while (cause != null) {
            printWriter.println("Caused by " + cause.getClass().getName() + ": " + cause.getMessage());
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        model(ctx).put("stacktrace", stringWriter.toString());
        try {
            final PebbleTemplate t = engine.getTemplate("error");
            final StringWriter writer = new StringWriter();
            t.evaluate(writer, model(ctx));
            ctx.status(500).html(writer.toString());
        } catch (final IOException e) {
            LOGGER.error("Failed to render error template", e);
            ctx.status(500).result("Internal server error");
        }
    }
}
