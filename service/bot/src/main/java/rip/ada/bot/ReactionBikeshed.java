package rip.ada.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.entities.emoji.UnicodeEmojiImpl;
import rip.ada.wca.model.CompetitionInfo;

import java.util.*;

public class ReactionBikeshed {

    private final List<String> characters = List.of(
            "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣",
            "🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯", "🇰", "🇱", "🇲",
            "🇳", "🇴", "🇵", "🇶", "🇷", "🇸", "🇹", "🇺", "🇻", "🇼", "🇽", "🇾", "🇿",
            "🅰️", "🅱️", "🆎", "🅾️",
            "⬛", "⬜", "🟥", "🟧", "🟨", "🟩", "🟦", "🟪", "🟫",
            "⚫", "⚪", "🔴", "🟠", "🟡", "🟢", "🔵", "🟣", "🟤",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🤎", "🖤", "🤍",
            "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🫐", "🍓", "🍒", "🥝"
    );
    private final Map<String, String> reactionToCompetitions = new HashMap<>();
    private final List<Message> messageIds = new ArrayList<>();
    private Guild guild;

    public void setGuild(final Guild guild) {
        this.guild = guild;
    }

    public String getRole(final String emoji) {
        return reactionToCompetitions.get(emoji);
    }

    public boolean isMessage(final long id) {
        return messageIds.stream().anyMatch(m -> m.getIdLong() == id);
    }

    public void addMessage(final Message message) {
        if (!messageIds.contains(message)) {
            messageIds.add(message);
            for (final MessageEmbed embed : message.getEmbeds()) {
                final String description = embed.getDescription();
                if (description != null) {
                    for (final String s : description.split("\n")) {
                        final String[] s1 = s.split(" ", 2);
                        reactionToCompetitions.put(s1[0], s1[1]);
                    }
                }
            }
            messageIds.sort(Comparator.comparingLong(Message::getIdLong));
        }
    }

    public void onCompetitionsUpdate(final List<CompetitionInfo> competitionInfo) {
        final List<String> names = competitionInfo.stream().map(CompetitionInfo::name).toList();
        for (final String value : reactionToCompetitions.values()) {
            if (!names.contains(value)) {
                for (final Role role : guild.getRoles()) {
                    if (role.getName().equals(value)) {
                        role.delete().complete();
                    }
                }
            }
        }
        reactionToCompetitions.entrySet().removeIf(entry -> !names.contains(entry.getValue()));

        final List<CompetitionInfo> upcoming = new ArrayList<>(competitionInfo);
        upcoming.removeIf(value -> reactionToCompetitions.containsValue(value.name()));

        for (final Message messageId : messageIds) {
            removeOldCompetitions(messageId);
        }

        for (final CompetitionInfo newCompetition : upcoming) {
            for (final Message messageId : new ArrayList<>(messageIds)) {
                final String content = messageId.getEmbeds().getFirst().getDescription();
                if (content == null) {
                    addCompetitionToEmbed(messageId, newCompetition);
                    break;
                }
                final int competitions = content.split("\n").length;
                if (competitions < 20) {
                    addCompetitionToEmbed(messageId, newCompetition);
                    break;
                }
            }
            if (guild.getRolesByName(newCompetition.name(), true).isEmpty()) {
                guild.createRole().setName(newCompetition.name()).complete();
            }
        }
    }

    private void removeOldCompetitions(final Message message) {
        final List<String> comps = filterComps(message);
        doEdit(message, comps);
    }

    private void addCompetitionToEmbed(final Message message, final CompetitionInfo competitionInfo) {
        final List<String> comps = filterComps(message);
        final String chosenChar = chooseEmoji(competitionInfo, comps);
        if (chosenChar == null) {
            return;
        }
        final Message edited = doEdit(message, comps);

        edited.addReaction(new UnicodeEmojiImpl(chosenChar)).complete();
        messageIds.removeIf(message1 -> message1.getIdLong() == edited.getIdLong());
        messageIds.add(edited);
        messageIds.sort(Comparator.comparingLong(Message::getIdLong));
    }

    private String chooseEmoji(final CompetitionInfo competitionInfo, final List<String> comps) {
        String chosenChar = null;
        for (final String character : characters) {
            if (reactionToCompetitions.containsKey(character)) {
                continue;
            }
            comps.add(character + " " + competitionInfo.name());
            reactionToCompetitions.put(character, competitionInfo.name());
            chosenChar = character;
            break;
        }
        return chosenChar;
    }

    private List<String> filterComps(final Message message) {
        String desc = message.getEmbeds().getFirst().getDescription();
        if (desc == null) {
            desc = "";
        }

        final List<String> comps = new ArrayList<>(Arrays.asList(desc.split("\n")));
        comps.removeIf(line -> reactionToCompetitions.values().stream().noneMatch(line::contains));
        return comps;
    }

    private static Message doEdit(final Message message, final List<String> comps) {
        comps.sort(String::compareTo);

        final String join = String.join("\n", comps);
        final MessageEmbed build1 = new EmbedBuilder()
                .setTitle(message.getEmbeds().getFirst().getTitle())
                .setColor(message.getEmbeds().getFirst().getColor())
                .setDescription(join)
                .build();
        final MessageEditData build = new MessageEditBuilder().setEmbeds(build1).build();
        final Message complete = message.editMessage(build).complete();

        for (final MessageReaction reaction : complete.getReactions()) {
            if (!join.contains(reaction.getEmoji().getName())) {
                reaction.clearReactions().complete();
            }
        }

        return complete;
    }

}
