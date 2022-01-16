package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.music.GuildAudioPlayer;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PlayNavigationListener extends ListenerAdapter
{
    private static final String[] BUTTONS = {"pause", "skip", "repeat"};

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event)
    {
        if (!event.isFromGuild())
            return;
        if (!Arrays.stream(BUTTONS).anyMatch(x -> event.getComponentId().equals(x)))
            return;
        if (!Bean.getInstance().getAudioManager().hasPlayer(event.getGuild().getIdLong()))
            return;

        GuildAudioPlayer player = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        player.onButtonPress(event);
    }
}
