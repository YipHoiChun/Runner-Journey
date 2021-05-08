package com.example.runnerjourney.view;

class JourneyItem {
    private String name;
    private String strUri;
    private long _id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStrUri(String strUri) {
        this.strUri = strUri;
    }

    public String getStrUri() {
        return strUri;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long get_id() {
        return _id;
    }
}
