package ralcock.cbf;

interface LoadTaskListener {
    void notifyLoadTaskStarted();

    void notifyLoadTaskComplete(final LoadBeersTask.Result result);

    void notifyLoadTaskUpdate(String[] values);
}
