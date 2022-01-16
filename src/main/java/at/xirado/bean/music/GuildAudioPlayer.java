package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Util;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateInteractionAction;

import java.util.Collections;

public class GuildAudioPlayer
{
    private final LavalinkPlayer player;
    private final AudioScheduler scheduler;
    private final long guildId;
    private final JdaLink link;
    private long messageId = 0;
    private long channelId = 0;

    public GuildAudioPlayer(long guildId)
    {
        this.guildId = guildId;
        link = Bean.getInstance().getLavalink().getLink(String.valueOf(guildId));
        player = link.getPlayer();
        scheduler = new AudioScheduler(player, guildId, this);
        player.addListener(scheduler);
    }

    public AudioScheduler getScheduler()
    {
        return scheduler;
    }

    public LavalinkPlayer getPlayer()
    {
        return player;
    }

    public long getGuildId()
    {
        return guildId;
    }

    public JdaLink getLink()
    {
        return link;
    }

    public void destroy()
    {
        Bean.getInstance().getAudioManager().removePlayer(this);
        link.destroy();
        scheduler.destroy();
    }

    public long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(long messageId)
    {
        if (this.messageId != 0)
        {
            Guild guild = Bean.getInstance().getShardManager().getGuildById(guildId);
            if (guild == null) return;
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel == null) return;
            channel.editMessageComponentsById(this.messageId, Collections.emptyList()).queue();
        }
        this.messageId = messageId;
        if (channelId != 0)
        {
            Guild guild = Bean.getInstance().getShardManager().getGuildById(guildId);
            if (guild == null) return;
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel == null) return;
            channel.editMessageComponentsById(messageId, getButtonLayout()).queue();
        }
    }

    public long getChannelId()
    {
        return channelId;
    }

    public void setChannelId(long channelId)
    {
        this.channelId = channelId;
    }

    private ActionRow getButtonLayout()
    {
        Button pause = player.isPaused()
                ? Button.primary("pause", Emoji.fromUnicode("⏸"))
                : Button.secondary("pause", Emoji.fromUnicode("⏸"));

        Button next = Button.secondary("skip", Emoji.fromUnicode("⏭"));

        Button repeat = scheduler.isRepeat()
                ? Button.primary("repeat", Emoji.fromUnicode("\uD83D\uDD02"))
                : Button.secondary("repeat", Emoji.fromUnicode("\uD83D\uDD02"));

        if (player.getPlayingTrack() == null)
        {
            pause = pause.asDisabled();
            next = next.asDisabled();
        }
        return ActionRow.of(pause, next, repeat);
    }

    public void onButtonPress(ButtonClickEvent event)
    {
        switch (event.getComponentId()) {
            case "pause" -> {
                player.setPaused(!player.isPaused());
                event.deferEdit().setActionRows(getButtonLayout()).queue();
            }
            case "skip" -> {
                scheduler.setButtonSkip(true);
                scheduler.nextTrack();
                UpdateInteractionAction update = event.deferEdit().setActionRows(getButtonLayout());
                if (player.getPlayingTrack() != null)
                    update.setEmbeds(EmbedUtil.defaultEmbed("**Now playing** "+Util.titleMarkdown(player.getPlayingTrack())));
                update.queue();
            }
            case "repeat" -> {
                scheduler.setRepeat(!scheduler.isRepeat());
                event.deferEdit().setActionRows(getButtonLayout()).queue();
            }
        }
    }

    public void updateMessage(AudioTrack track)
    {
        if (getScheduler().isButtonSkip())
        {
            getScheduler().setButtonSkip(false);
            return;
        }
        if (channelId != 0)
        {
            Guild guild = Bean.getInstance().getShardManager().getGuildById(guildId);
            if (guild == null) return;
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel == null) return;
            channel.editMessageComponentsById(messageId, getButtonLayout())
                    .setEmbeds(EmbedUtil.defaultEmbed("**Now playing** "+Util.titleMarkdown(track)))
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
        }
    }
}
