package ralcock.cbf;

/**
 * Created with IntelliJ IDEA.
 * User: RichardAndCathy
 * Date: 18/05/12
 * Time: 07:57
 * To change this template use File | Settings | File Templates.
 */
public interface LoadTaskListener {
    void notifyLoadTaskStarted();

    void notifyLoadTaskComplete(final LoadBeersTask.Result result);

    void notifyLoadTaskUpdate(String[] values);
}
