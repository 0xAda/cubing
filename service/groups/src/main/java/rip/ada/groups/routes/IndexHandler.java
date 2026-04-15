package rip.ada.groups.routes;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.pebbletemplates.pebble.PebbleEngine;

import static rip.ada.groups.templates.Templates.render;

public class IndexHandler implements Handler {

    private final PebbleEngine engine;

    public IndexHandler(final PebbleEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(final Context ctx) {
        render(engine, "index", ctx);
    }
}
