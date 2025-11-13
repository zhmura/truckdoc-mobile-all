package com.sanda.truckdoc.client.ui.message;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.github.naixx.BaseAdapter;
import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.databinding.FragmentNewMessageBinding;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.util.ConnectionUtils;
import com.sanda.truckdoc.client.HiltEntryPoint;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.util.Log;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;

/**
 * TruckDoc mobile client class
 *
 * @author: Siarhei Zhmura
 */
@AndroidEntryPoint
public class NewMessageFragment extends Fragment implements ButtonAdapter.InteractionListener<DbContactRecord> {

    private ResponseReceiver receiver = new ResponseReceiver();
    private FragmentNewMessageBinding binding;

    @Inject
    Prefs prefs;
    @Inject
    MessagesDatabaseService db;

    private ButtonAdapter adapter;
    private static final String TAG = "NewMessageFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Removed: TruckDocApp.get(requireActivity()).appComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupClickListeners();
    }

    private void setupViews() {
        adapter = new ButtonAdapter(this);
        loadContactRecords(db);
        RecyclerView buttonList = getView().findViewById(R.id.buttonList);
        buttonList.setAdapter(adapter);
        buttonList.setVisibility(View.INVISIBLE);
    }

    private void setupClickListeners() {
        binding.bChooseRecipient.setOnClickListener(v -> onMessagesBtn());
    }

    private void onMessagesBtn() {
        RecyclerView buttonList = getView().findViewById(R.id.buttonList);
        buttonList.setVisibility(View.VISIBLE);
        binding.bChooseRecipient.setVisibility(View.GONE);
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = requireActivity();
        // Removed: TruckDocApp.get(context).appComponent().inject(this);
        Bundle args = getArguments();
        Long recipientId = args != null ? args.getLong("recipientId", 0L) : 0L;
        if (recipientId != 0) {
            HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(requireActivity());
            MessagesDatabaseService db = entryPoint.messagesDatabaseService();
            loadContactRecords(db);
        }

        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_PROCESS_FINISHED);
        filter.addAction(ResponseReceiver.ACTION_LIST_UPDATE_START);
        filter.addAction(ResponseReceiver.ACTION_LIST_UPDATED);
        filter.addAction(ResponseReceiver.NOTIFICATION_MESSAGE);

        requireActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(receiver);
    }

    private void showMessageToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }

    private void sendMessageToOperator(String message, DbContactRecord item) {
        boolean connected = ConnectionUtils.checkIfHaveInternetConnection(message, item.getPhone(), requireActivity());
        if (connected) {
            final Intent intent = new Intent(MessageCheckService.ACTION_SEND_TEXT_MESSAGE,
                    null,
                    requireActivity(),
                    MessageCheckService.class);
            Bundle b = new Bundle();
            b.putString("com/sanda/truckdoc/client/message", message);
            b.putLong("mail.group", item.getRecipientId());
            b.putString("mail.group.type", item.getRecipientIdType());
            intent.putExtras(b);
            ContextCompat.startForegroundService(
                    requireContext(),
                    intent
            );
            binding.txtMessage.setText("");
        }
    }

    @Override
    public void onClick(DbContactRecord item) {
        String message = binding.txtMessage.getText().toString();
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

    private void loadContactRecords(MessagesDatabaseService databaseService) {
        try {
            List<DbContactRecord> contacts = MessagesDatabaseServiceJavaCompat.getContactRecordsBlocking(databaseService);
            adapter.swapItems(contacts);
        } catch (Exception e) {
            Log.e(TAG, "Error loading contact records", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
