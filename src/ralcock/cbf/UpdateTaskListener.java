package ralcock.cbf;

import ralcock.cbf.model.Beer;

public interface UpdateTaskListener {
    void notifyUpdateStarted();

    void notifyUpdateComplete(Long aLong);

    void notifyUpdateProgress(Beer[] values);
}
