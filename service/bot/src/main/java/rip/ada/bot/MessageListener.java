package rip.ada.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class MessageListener extends ListenerAdapter {

    private final ReactionBikeshed reactionBikeshed;

    public MessageListener(final ReactionBikeshed reactionBikeshed) {
        this.reactionBikeshed = reactionBikeshed;
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == 828692100446027776L && event.getMessage().getContentRaw().equals("-message")) {
            final Message upcomingCompetitions = event.getMessage().getChannel().sendMessage(new MessageCreateBuilder().addEmbeds(new EmbedBuilder().setTitle("Upcoming Competitions").setColor(0x0000FF).build()).build()).submit().join();
            reactionBikeshed.addMessage(upcomingCompetitions);
            event.getMessage().delete().complete();
        }
    }
}
