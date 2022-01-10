package at.xirado.bean.data;

public class RankBackground
{
    private String url;
    private int accent;

    public RankBackground(String url, int accent)
    {
        this.url = url;
        this.accent = accent;
    }

    public String getURL()
    {
        return url;
    }

    public int getAccent()
    {
        return accent;
    }

    public void setURL(String url)
    {
        this.url = url;
    }

    public void setAccent(int accent)
    {
        this.accent = accent;
    }
}
