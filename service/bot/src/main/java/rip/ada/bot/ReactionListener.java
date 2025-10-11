package rip.ada.bot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {

    private final ReactionBikeshed reactionBikeshed;

    public ReactionListener(final ReactionBikeshed reactionBikeshed) {
        this.reactionBikeshed = reactionBikeshed;
    }

    @Override
    public void onMessageReactionAdd(final MessageReactionAddEvent event) {
        if (!reactionBikeshed.isMessage(event.getMessageIdLong())) {
            return;
        }
        final EmojiUnion emoji = event.getReaction().getEmoji();
        final String role = reactionBikeshed.getRole(emoji.getName());
        for (final Role role1 : event.getGuild().getRolesByName(role, true)) {
            event.getGuild().addRoleToMember(event.getMember(), role1).queue();
        }
    }

    @Override
    public void onMessageReactionRemove(final MessageReactionRemoveEvent event) {
        if (!reactionBikeshed.isMessage(event.getMessageIdLong())) {
            return;
        }

        final EmojiUnion emoji = event.getReaction().getEmoji();
        final String role = reactionBikeshed.getRole(emoji.getName());
        for (final Role role1 : event.getGuild().getRolesByName(role, true)) {
            final Member memberById = event.getGuild().getMemberById(event.getUserIdLong());
            event.getGuild().removeRoleFromMember(memberById, role1).queue();
        }
    }
}
