package at.xirado.bean.backend.routes;

import at.xirado.bean.data.RankingSystem;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Helpers;
import spark.Request;
import spark.Response;
import spark.Route;

public class LeaderboardRoute implements Route
{
    @Override
    public Object handle(Request request, Response response) throws Exception
    {
        String guildId = request.params("guild");
        if (!Helpers.isNumeric(guildId))
        {
            response.status(400);
            return DataObject.empty()
                    .put("code", 400)
                    .put("message", "Invalid guild id!").toString();
        }
        long guildIdLong = Long.parseLong(guildId);
        int totalAmount = RankingSystem.getDataCount(guildIdLong);
        if (totalAmount == 0)
        {
            response.status(404);
            return DataObject.empty()
                    .put("code", 404)
                    .put("message", "The requested data could not be found!").toString();
        }
        String pageHeader = request.queryParams("page");
        int availablePages = (int) Math.ceil((double) totalAmount / 100);
        int page = 1;
        if (Helpers.isNumeric(pageHeader))
        {
            int pageHeaderParsed = Integer.parseInt(pageHeader);
            if (pageHeaderParsed > 1)
                page = pageHeaderParsed;
        }
        var start = (page == 1 ? 0 : ((page - 1) * 100));
        DataArray result = RankingSystem.getLeaderboard(guildIdLong, page, 100);
        var end = start+result.length();
        DataObject obj = DataObject.empty()
                .put("users", result);
        if (availablePages > page)
            obj.put("paging", DataObject.empty().put("next", request.url()+"?page="+(page+1)));
        return obj;
    }
}