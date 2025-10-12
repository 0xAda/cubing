package rip.ada.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import rip.ada.wca.UnauthenticatedWcaApi;
import rip.ada.wca.WcaApiConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotServiceMain {

    static void main(final String[] args) throws InterruptedException, IOException {
        final Properties properties = new Properties();
        properties.load(new FileInputStream(args[0]));

        final UnauthenticatedWcaApi wcaApi = new UnauthenticatedWcaApi(WcaApiConfig.unauthenticatedDefault());
        final ReactionBikeshed reactionBikeshed = new ReactionBikeshed();
        final JDA bot = JDABuilder.createLight(properties.getProperty("botToken"),
                        EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MEMBERS))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(new MessageListener(reactionBikeshed), new ReactionListener(reactionBikeshed))
                .build();
        Thread.sleep(2000);

        final Guild guild = bot.getGuilds().getFirst();
        reactionBikeshed.setGuild(guild);
        guild.loadMembers().get();

        for (final TextChannel channel : guild.getTextChannels()) {
            for (final Message message : channel.getHistory().retrievePast(100).complete()) {
                if (!message.getEmbeds().isEmpty() && message.getAuthor().getIdLong() == bot.getSelfUser().getIdLong()) {
                    reactionBikeshed.addMessage(message);
                }
            }
        }

        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(new RoleUpdateTask(wcaApi, reactionBikeshed), 0, 10, TimeUnit.MINUTES);
    }

}
