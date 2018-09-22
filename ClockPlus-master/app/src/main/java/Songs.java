public class Songs {


    private String mGroupId;
    private String mTitle;
    private String mUrl;

    private Songs() {

    }

    public Songs(String groupId, String title, String url) {
        this.mGroupId = groupId;
        this.mTitle = title;
        this.mUrl = url;
    }

    public String getmGroupId() {
        return mGroupId;
    }

    public void setmGroupId(String mGroupId) {
        this.mGroupId = mGroupId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

}

