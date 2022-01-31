package dev.anyroad.threadlocal;

class ThreadLocalData {
    private volatile String data = null;

    public ThreadLocalData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
