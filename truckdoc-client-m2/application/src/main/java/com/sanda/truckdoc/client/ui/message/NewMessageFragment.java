package com.sanda.truckdoc.client.ui.message;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.github.naixx.BaseAdapter;
import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.util.ConnectionUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

/**
 * TruckDoc mobile client class
 *
 * @author: Siarhei Zhmura
 */
@EFragment(R.layout.fragment_new_message)
public class NewMessageFragment extends Fragment implements BaseAdapter.InteractionListener<DbContactRecord> {

    private ResponseReceiver receiver = new ResponseReceiver();

    @ViewById
    EditText txtMessage;
    @ViewById
    View bChooseRecipient;
    @ViewById(R.id.buttonList)
    RecyclerView recyclerView;

    @Inject
    Prefs prefs;
    @Inject
    MessagesDatabaseService db;

    @AfterViews
    void afterViews() {
        TruckDocApp.get(getActivity()).appComponent().inject(this);
        ButtonAdapter adapter = new ButtonAdapter(this);
        db.getContactRecords().toList().subscribe(adapter::swapItems);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Click(R.id.bChooseRecipient)
    void onMessagesBtn() {
        recyclerView.setVisibility(View.VISIBLE);
        bChooseRecipient.setVisibility(View.GONE);
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = getActivity();
        TruckDocApp.get(context).appComponent().inject(this);
        Bundle args = getArguments();
        Long recipientId = args.getLong("recipientId", 0L);
        ButtonAdapter adapter = new ButtonAdapter(this);
        if (recipientId != 0) {
            MessagesDatabaseService db = TruckDocApp.get(getActivity()).appComponent().db();
            rx.Observable<DbContactRecord> contact = db.getContactRecords().filter(dbContactRecord -> dbContactRecord.getId() == recipientId.longValue());
            if (!contact.isEmpty().toBlocking().single()) {
                contact.toList().subscribe(adapter::swapItems);
                recyclerView = getActivity().findViewById(R.id.buttonList);
                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.refreshDrawableState();
                bChooseRecipient = getActivity().findViewById(R.id.bChooseRecipient);
                bChooseRecipient.setVisibility(View.GONE);
            }
        }

        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_PROCESS_FINISHED);
        filter.addAction(ResponseReceiver.ACTION_LIST_UPDATE_START);
        filter.addAction(ResponseReceiver.ACTION_LIST_UPDATED);
        filter.addAction(ResponseReceiver.NOTIFICATION_MESSAGE);

        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    private void showMessageToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }

    private void sendMessageToOperator(String message, DbContactRecord item) {
        boolean connected = ConnectionUtils.checkIfHaveInternetConnection(message, item.getPhone(), getActivity());
        if (connected) {
            final Intent intent = new Intent(MessageCheckService.ACTION_SEND_TEXT_MESSAGE,
                    null,
                    getActivity(),
                    MessageCheckService.class);
            Bundle b = new Bundle();
            b.putString("com/sanda/truckdoc/client/message", message);
            b.putLong("mail.group", item.getRecipientId());
            b.putString("mail.group.type", item.getRecipientIdType());
            intent.putExtras(b);
            ContextCompat.startForegroundService(
                    getContext(),
                    intent
            );
            ((EditText) getActivity().findViewById(R.id.txtMessage)).setText("");
        }
    }

    @Override
    public void onClick(DbContactRecord item) {
        String message = ((EditText) getActivity().findViewById(R.id.txtMessage)).getText().toString();
        if (!TextUtils.isEmpty(message)) {
            sendMessageToOperator(message, item);
            FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            if (Build.VERSION.SDK_INT >= 26) {
                ft.setReorderingAllowed(false);
            }
            ft.detach(this).attach(this).commit();
        } else {
            showText(getResources().getString(R.string.enter_msg));
        }
    }

    public class ResponseReceiver extends ServiceResultReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ServiceResultReceiver.ACTION_LIST_UPDATE_START.equals(intent.getAction())) {
                String text = intent.getStringExtra(MessageCheckService.PARAM_OUT_MSG);
                showText(text);
            }
            if (ServiceResultReceiver.ACTION_LIST_UPDATED.equals(intent.getAction())) {
                String text = intent.getStringExtra(MessageCheckService.PARAM_OUT_MSG);
                showText(text);
            }
        }
    }

    private void showText(String text) {
        if (text != null && text.length() > 0) showMessageToast(text);
    }
}
