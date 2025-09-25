package rip.ada.wca.patcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rip.ada.wca.AuthenticatedWcaApi;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WcifPatcher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WcifPatcher.class);
    private final ArrayBlockingQueue<WcifPatchRequest> requests;
    private final AuthenticatedWcaApi wcaApi;
    private volatile boolean running = true;

    public WcifPatcher(final ArrayBlockingQueue<WcifPatchRequest> requests, final AuthenticatedWcaApi wcaApi) {
        this.requests = requests;
        this.wcaApi = wcaApi;
    }

    @Override
    public void run() {
        while (running) {
            final WcifPatchRequest request;
            try {
                request = requests.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (request == null) {
                continue;
            }

            final ObjectNode jsonNode;
            try {
                jsonNode = wcaApi.serializeNode(request.competition());
            } catch (JsonProcessingException e) {
                LOGGER.error("Failed to serialize competition", e);
                continue;
            }
            final ObjectNode events = jsonNode.deepCopy();

            wcaApi.updateWcifDirectly(request.session(), request.competition().getId(), events);
            final ObjectNode schedule = jsonNode.deepCopy();
            schedule.remove("persons");
            schedule.remove("events");
            wcaApi.updateWcifDirectly(request.session(), request.competition().getId(), schedule);

            final ArrayNode personsArray = (ArrayNode) jsonNode.get("persons");
            for (int i = 0; i < personsArray.size(); i += 15) {
                final ObjectNode persons = jsonNode.deepCopy();
                persons.remove("schedule");
                persons.remove("events");
                persons.remove("persons");
                final ArrayNode reducedPersonsArray = JsonNodeFactory.instance.arrayNode();
                for (int j = i; j < i + 15 && j < personsArray.size(); j++) {
                    reducedPersonsArray.add(personsArray.get(j));
                }
                persons.set("persons", reducedPersonsArray);
                wcaApi.updateWcifDirectly(request.session(), request.competition().getId(), persons);
            }
        }
    }

    public void setRunning(final boolean running) {
        this.running = false;
    }
}
