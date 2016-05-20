package jp.tkgktyk.floatingnavigation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by tkgktyk on 2015/07/02.
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ACTION = 1;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @InjectView(R.id.service_switch)
    Switch mServiceSwitch;

    private MyAdapter mMyAdapter;
    private ArrayList<ActionInfo> mActionList = Lists.newArrayList();
    private boolean mIsChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);

        mActionList = MyApp.loadActionList(this);

        mServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveActionList();
                MyApp.setServiceEnabled(buttonView.getContext(), isChecked);
                NavigationService.startStop(buttonView.getContext(), isChecked);
            }
        });
        mServiceSwitch.setChecked(MyApp.isServiceEnabled(this));

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMyAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mMyAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder from,
                                  RecyclerView.ViewHolder to) {
                final int fromPos = from.getAdapterPosition();
                final int toPos = to.getAdapterPosition();
                Collections.swap(mActionList, fromPos, toPos);
                mIsChanged = true;
                mMyAdapter.notifyItemMoved(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
                final int pos = viewHolder.getAdapterPosition();
                mActionList.remove(pos);
                mIsChanged = true;
                mMyAdapter.notifyItemRemoved(pos);
            }
        });
        helper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveActionList();
    }

    private void saveActionList() {
        if (mIsChanged) {
            MyApp.saveActionList(this, mActionList);
            mIsChanged = false;
        }
    }

    @OnClick(R.id.add_fab)
    void onAddClicked(FloatingActionButton button) {
        startActivityForResult(new Intent(this, ActionPickerActivity.class), REQUEST_ACTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACTION:
                if (resultCode == RESULT_OK) {
                    MyApp.logD();
                    ActionInfo.Record record = (ActionInfo.Record) data
                            .getSerializableExtra(ActionPickerActivity.EXTRA_ACTION_RECORD);
                    ActionInfo actionInfo = new ActionInfo(record);
                    mActionList.add(actionInfo);
                    mIsChanged = true;
                    mMyAdapter.notifyItemInserted(mActionList.size() - 1);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected static class Holder extends RecyclerView.ViewHolder {
        @InjectView(R.id.icon)
        ImageView icon;
        @InjectView(R.id.action_name)
        TextView name;
        @InjectView(R.id.action_type)
        TextView type;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<Holder> {

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_action_list_item, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            ActionInfo info = mActionList.get(position);
            String name = info.getName();
            Bitmap icon = info.getIcon();
            if (Strings.isNullOrEmpty(name)) {
                switch (info.getType()) {
                    case ActionInfo.TYPE_NONE:
                        name = getString(R.string.none);
                        break;
                    default:
                        name = getString(R.string.not_found);
                }
            }
            holder.icon.setImageBitmap(icon);
            holder.name.setText(name);
            switch (info.getType()) {
                case ActionInfo.TYPE_TOOL:
                    holder.type.setText(R.string.tools);
                    break;
                case ActionInfo.TYPE_APP:
                    holder.type.setText(R.string.apps);
                    break;
                case ActionInfo.TYPE_SHORTCUT:
                    holder.type.setText(R.string.shortcuts);
                    break;
                default:
                    holder.type.setText(null);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mActionList.size();
        }
    }
}
