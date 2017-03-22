package com.lyj.cn.base;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lyj.cn.assist.TLog;
import com.lyj.cn.network.ert.ABizLogic;
import com.lyj.cn.network.ert.IResult;
import com.lyj.cn.network.task.ITaskManager;
import com.lyj.cn.network.task.TaskException;
import com.lyj.cn.network.task.TaskManager;
import com.lyj.cn.network.task.WorkTask;
import com.lyj.cn.surport.ViewInject;
import com.lyj.cn.util.InjectUtility;
import com.lyj.cn.util.ViewUtils;

import java.text.SimpleDateFormat;

import static android.R.attr.id;

/**
 * Created by lyj on 2017/3/22 0022.
 */

public abstract class BaseFragment extends Fragment{
    static final String TAG = "AFragment-Base";
    private TaskManager taskManager;
    ViewGroup rootView;
    @ViewInject(
            idStr = "layoutLoading"
    )
    @Nullable
    View loadingLayout;
    @ViewInject(
            idStr = "layoutLoadFailed"
    )
    @Nullable
    View loadFailureLayout;
    @ViewInject(
            idStr = "layoutContent"
    )
    @Nullable
    View contentLayout;
    @ViewInject(
            idStr = "layoutEmpty"
    )
    @Nullable
    View emptyLayout;
    private boolean contentEmpty = true;
    protected long lastResultGetTime = 0L;
    private boolean destory = false;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
    };
    View.OnClickListener innerOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(v.getId() == id.layoutReload) {
                BaseFragment.this.requestData();
            } else if(v.getId() == id.layoutRefresh) {
                BaseFragment.this.requestData();
            }

        }
    };

    public BaseFragment() {
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof BaseActivity) {
            ((BaseActivity)activity).addFragment(this.toString(), this);
        }

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.taskManager = new TaskManager();
        if(savedInstanceState != null) {
            this.taskManager.restore(savedInstanceState);
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.inflateContentView() > 0) {
            ViewGroup contentView = (ViewGroup)inflater.inflate(this.inflateContentView(), (ViewGroup)null);
            contentView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            this.setupContentView(inflater, contentView, savedInstanceState);
            return this.getContentView();
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    protected void setupContentView(LayoutInflater inflater, ViewGroup contentView, Bundle savedInstanceState) {
        this.setContentView(contentView);
        this._layoutInit(inflater, savedInstanceState);
        this.layoutInit(inflater, savedInstanceState);
    }

    public void setContentView(ViewGroup view) {
        this.rootView = view;
    }

    public ViewGroup getContentView() {
        return this.rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState == null) {
            this.requestData();
        }

    }

    public boolean onHomeClick() {
        return this.onBackClick();
    }

    public boolean onBackClick() {
        return false;
    }

    public void requestData() {
    }

    public void requestDataDelay(long delay) {
        Runnable requestDelayRunnable = new Runnable() {
            public void run() {
                TLog.d("AFragment-Base", "延迟刷新，开始刷新, " + this.toString());
                BaseFragment.this.requestData();
            }
        };
        this.runUIRunnable(requestDelayRunnable, delay);
    }

    public void requestDataOutofdate() {
        this.requestData();
    }

    void _layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        InjectUtility.initInjectedView(this.getActivity(), this, this.getContentView());
        View reloadView;
        if(this.getEmptyLayout() != null) {
            reloadView = this.getEmptyLayout().findViewById(id.layoutReload);
            if(reloadView != null) {
                this.setViewOnClick(reloadView);
            }
        }

        if(this.getLoadFailureLayout() != null) {
            reloadView = this.getLoadFailureLayout().findViewById(id.layoutReload);
            if(reloadView != null) {
                this.setViewOnClick(reloadView);
            }
        }

        this.setViewVisiable(this.getLoadingLayout(), 8);
        this.setViewVisiable(this.getLoadFailureLayout(), 8);
        this.setViewVisiable(this.getEmptyLayout(), 8);
        if(this.isContentEmpty()) {
            if(savedInstanceSate != null) {
                this.requestData();
            } else {
                this.setViewVisiable(this.getEmptyLayout(), 0);
                this.setViewVisiable(this.getContentLayout(), 8);
            }
        } else {
            this.setViewVisiable(this.getContentLayout(), 0);
        }

    }

    public View findViewById(int viewId) {
        return this.getContentView() == null?null:this.getContentView().findViewById(viewId);
    }

    public void setContentEmpty(boolean empty) {
        this.contentEmpty = empty;
    }

    public boolean isContentEmpty() {
        return this.contentEmpty;
    }

    void setViewVisiable(View v, int visibility) {
        if(v != null && v.getVisibility() != visibility) {
            v.setVisibility(visibility);
        }

    }

    protected void onTaskStateChanged(BaseFragment.ABaseTaskState state, TaskException exception) {
        if(state == BaseFragment.ABaseTaskState.prepare) {
            if(this.isContentEmpty()) {
                this.setViewVisiable(this.getLoadingLayout(), 0);
                this.setViewVisiable(this.getContentLayout(), 8);
            } else {
                this.setViewVisiable(this.getLoadingLayout(), 8);
                this.setViewVisiable(this.getContentLayout(), 0);
            }

            this.setViewVisiable(this.getEmptyLayout(), 8);
            if(this.isContentEmpty() && this.getLoadingLayout() == null) {
                this.setViewVisiable(this.getContentLayout(), 0);
            }

            this.setViewVisiable(this.getLoadFailureLayout(), 8);
        } else if(state == BaseFragment.ABaseTaskState.success) {
            this.setViewVisiable(this.getLoadingLayout(), 8);
            if(this.isContentEmpty()) {
                this.setViewVisiable(this.getEmptyLayout(), 0);
                this.setViewVisiable(this.getContentLayout(), 8);
            } else {
                this.setViewVisiable(this.getContentLayout(), 0);
                this.setViewVisiable(this.getEmptyLayout(), 8);
            }
        } else if(state == BaseFragment.ABaseTaskState.canceled) {
            if(this.isContentEmpty()) {
                this.setViewVisiable(this.getLoadingLayout(), 8);
                this.setViewVisiable(this.getEmptyLayout(), 0);
            }
        } else if(state == BaseFragment.ABaseTaskState.falid) {
            if(this.isContentEmpty()) {
                if(this.getLoadFailureLayout() != null) {
                    this.setViewVisiable(this.getLoadFailureLayout(), 0);
                    if(exception != null) {
                        TextView txtLoadFailed = (TextView)this.getLoadFailureLayout().findViewById(id.txtLoadFailed);
                        if(txtLoadFailed != null) {
                            txtLoadFailed.setText(exception.getMessage());
                        }
                    }

                    this.setViewVisiable(this.getEmptyLayout(), 8);
                } else {
                    this.setViewVisiable(this.getEmptyLayout(), 0);
                }

                this.setViewVisiable(this.getLoadingLayout(), 8);
            }
        } else if(state == BaseFragment.ABaseTaskState.finished) {
            ;
        }

    }

    public void showMessage(CharSequence msg) {
        if(!TextUtils.isEmpty(msg) && this.getActivity() != null) {
            ViewUtils.showMessage(this.getActivity(), msg.toString());
        }

    }

    public void showMessage(int msgId) {
        if(this.getActivity() != null) {
            this.showMessage(this.getString(msgId));
        }

    }

    public void onDestroy() {
        this.destory = true;

        try {
            super.onDestroy();
        } catch (Exception var2) {
            TLog.printExc(this.getClass(), var2);
        }

        this.removeAllTask(true);

    }

    public boolean isDestory() {
        return this.destory;
    }

    public boolean isActivityRunning() {
        return this.getActivity() != null;
    }

    public void onDetach() {
        super.onDetach();
        if(this.getActivity() != null && this.getActivity() instanceof BaseActivity) {
            ((BaseActivity)this.getActivity()).removeFragment(this.toString());
        }

    }

    public final void addTask(WorkTask task) {
        this.taskManager.addTask(task);
    }

    public final void removeTask(String taskId, boolean cancelIfRunning) {
        this.taskManager.removeTask(taskId, cancelIfRunning);
    }

    public final void removeAllTask(boolean cancelIfRunning) {
        this.taskManager.removeAllTask(cancelIfRunning);
    }

    public final int getTaskCount(String taskId) {
        return this.taskManager.getTaskCount(taskId);
    }

    protected final ABizLogic.CacheMode getTaskCacheMode(WorkTask task) {
        return task != null && TextUtils.isEmpty(task.getTaskId())? ABizLogic.CacheMode.disable:(this.getTaskCount(task.getTaskId()) == 1? ABizLogic.CacheMode.auto: ABizLogic.CacheMode.disable);
    }

    public void cleatTaskCount(String taskId) {
        this.taskManager.cleatTaskCount(taskId);
    }

    private void setViewOnClick(View v) {
        if(v != null) {
            v.setOnClickListener(this.innerOnClickListener);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(this.taskManager != null) {
            this.taskManager.save(outState);
        }

    }

    protected ITaskManager getTaskManager() {
        return this.taskManager;
    }

    public void runUIRunnable(Runnable runnable) {
        this.runUIRunnable(runnable, 0L);
    }

    public void runUIRunnable(Runnable runnable, long delay) {
        if(delay > 0L) {
            this.mHandler.removeCallbacks(runnable);
            this.mHandler.postDelayed(runnable, delay);
        } else {
            this.mHandler.post(runnable);
        }

    }

    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
    }

    public boolean canDisplay() {
        return true;
    }

    public abstract int inflateContentView();

    public int inflateActivityContentView() {
        return -1;
    }

    public int setActivityTheme() {
        return -1;
    }

    public int configRequestDelay() {
        return 500;
    }

    public View getLoadingLayout() {
        return this.loadingLayout;
    }

    public View getLoadFailureLayout() {
        return this.loadFailureLayout;
    }

    public View getContentLayout() {
        return this.contentLayout;
    }

    public View getEmptyLayout() {
        return this.emptyLayout;
    }

    protected abstract class ABaseTask<Params, Progress, Result> extends WorkTask<Params, Progress, Result> {
        public ABaseTask(String taskId) {
            super(taskId, BaseFragment.this);
        }

        protected void onPrepare() {
            super.onPrepare();
            BaseFragment.this.onTaskStateChanged(BaseFragment.ABaseTaskState.prepare, (TaskException)null);
        }

        protected void onSuccess(Result result) {
            super.onSuccess(result);
            BaseFragment.this.setContentEmpty(this.resultIsEmpty(result));
            BaseFragment.this.onTaskStateChanged(BaseFragment.ABaseTaskState.success, (TaskException)null);
            if(TLog.DEBUG) {
                TLog.d("AFragment-Base", "Result获取时间：%s", new Object[]{(new SimpleDateFormat("HH:mm:ss")).format(Long.valueOf(BaseFragment.this.lastResultGetTime))});
            }

            if(result instanceof IResult) {
                IResult iResult = (IResult)result;
                if(iResult.fromCache()) {
                    if(iResult.outofdate()) {
                        BaseFragment.this.runUIRunnable(new Runnable() {
                            public void run() {
                                TLog.d("AFragment-Base", "数据过期，开始刷新, " + this.toString());
                                BaseFragment.this.requestDataOutofdate();
                            }
                        }, (long)BaseFragment.this.configRequestDelay());
                    }
                } else {
                    BaseFragment.this.lastResultGetTime = System.currentTimeMillis();
                }
            } else {
                BaseFragment.this.lastResultGetTime = System.currentTimeMillis();
            }

        }

        protected void onFailure(TaskException exception) {
            super.onFailure(exception);
            BaseFragment.this.onTaskStateChanged(BaseFragment.ABaseTaskState.falid, exception);
        }

        protected void onCancelled() {
            super.onCancelled();
            BaseFragment.this.onTaskStateChanged(BaseFragment.ABaseTaskState.canceled, (TaskException)null);
        }

        protected void onFinished() {
            super.onFinished();
            BaseFragment.this.onTaskStateChanged(BaseFragment.ABaseTaskState.finished, (TaskException)null);
        }

        protected boolean resultIsEmpty(Result result) {
            return result == null;
        }
    }

    public static enum ABaseTaskState {
        none,
        prepare,
        falid,
        success,
        finished,
        canceled;

        private ABaseTaskState() {
        }
    }
}
