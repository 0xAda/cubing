package rip.ada.links.handlers;

import com.github.jknack.handlebars.Template;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Header;

import java.io.IOException;

public class TemplateRenderer {
    private TemplateRenderer() {
    }

    public static void render(final Template template, final Context context, final Object args) {
        try {
            context.res().setHeader(Header.CONTENT_TYPE, ContentType.HTML);
            template.apply(args, context.res().getWriter());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
