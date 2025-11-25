package com.sanda.truckdoc.client.to.data;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistAttachment;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistConfigNode;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistResult;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistResultNode;
import com.sanda.truckdoc.client.api.v3.sync.maintenance.model.MaintenanceConfigInfo;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.to.utils.JacksonUtils;
import com.sanda.truckdoc.client.to.utils.LocalStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by k.natallie on 04.02.2016.
 */
public class Model {
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String SERVER_ID = "SERVER_ID";
    public static final String NODE_NAME = "NODE_NAME";
    private static final String TAG = "Model";
    private ToNode currentNode;
    private MaintenanceConfigInfo maintenanceConfigInfo;

    private ChecklistResult result;
    private LocalStorage storage;

    private static volatile Model instance;
    private ResponseReceiver receiver = new ResponseReceiver();


    private Model(Context context) {
        storage = LocalStorage.getInstance(context);
        IntentFilter filter = new IntentFilter(ServiceResultReceiver.ACTION_UPLOAD_MNT_ATTACHMENT_OK);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        com.sanda.truckdoc.client.util.ReceiverUtils.registerReceiverNotExported(context.getApplicationContext(), receiver, filter);

    }

    public static Model getInstance(Context context) {
        Model localInstance = instance;
        if (localInstance == null) {
            synchronized (Model.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Model(context);
                }
            }
        }
        return localInstance;
    }


    public class ResponseReceiver extends ServiceResultReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ServiceResultReceiver.ACTION_UPLOAD_MNT_ATTACHMENT_OK.equals(intent.getAction())) {
                Long serverId = intent.getLongExtra(SERVER_ID, 0);
                String nodeName = intent.getStringExtra(NODE_NAME);
                ToNode node = findNodeByName(getNodes(), nodeName);
                updateAttachment(node, serverId);

            }
        }
    }

    private ToNode findNodeByName(List<? extends ChecklistResultNode> nodes, String name) {
        if (nodes == null) {
            return null;
        }
        ToNode result;
        for (ChecklistResultNode node : nodes) {
            if (((ToNode) node).getTitleText().equals(name)) {
                return (ToNode) node;
            }
            result = findNodeByName(node.getChildren(), name);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public void setupMaintenance(String typeId) {
        String toString = storage.readStringPreference(LocalStorage.TO_PROGRESS);
        if (TextUtils.isEmpty(toString) || toString.equals("null")) {
            initMaintenance(typeId);
            toString = JacksonUtils.getJsonChecklistResultString(result);
            storage.writeStringPreference(LocalStorage.TO_PROGRESS, toString);
        } else {
            result = JacksonUtils.restoreStartedTo(toString);
            initResultNodes(typeId);
        }
    }


    public void updateTOProgress() {
        String toString = JacksonUtils.getJsonChecklistResultString(result);
        storage.writeStringPreference(LocalStorage.TO_PROGRESS, toString);
    }

    public void initMaintenance(String type) {
        if (maintenanceConfigInfo == null) {
            String json = storage.readStringPreference(LocalStorage.CONFIG_INFO);
            try {
                ObjectMapper mapper = new ObjectMapper();
                maintenanceConfigInfo = new MaintenanceConfigInfo();
                maintenanceConfigInfo = mapper.readValue(json, MaintenanceConfigInfo.class);
            } catch (IOException ex) {
                Log.e(TAG, "Unable to parse stored config" + ex.getMessage());
            }
            if (maintenanceConfigInfo == null) {
                return;//TODO notify UI
            }
        }
        result = new ChecklistResult();
        result.setConfigVersion(maintenanceConfigInfo.getConfigVersion());
        result.setStartDate(Calendar.getInstance().getTimeInMillis());
        result.setConfigId(maintenanceConfigInfo.getConfig().getConfigId());

        initResultNodes(type);
        result.setItems((List<ChecklistResultNode>) getResultNodes());
    }

    public void initResultNodes(String type) {
        if (result.getItems() != null && result.getItems().size() > 0) {
            setResultNodes(initResultListNode(result.getItems(), null));

        } else {
            setResultNodes(initResultList(getConfigNodes(), null, type));
        }
    }

    public List<ChecklistResultNode> initResultListNode(List<ChecklistResultNode> nodes, ToNode parent) {
        List<ChecklistResultNode> resultNodeList = new ArrayList<>();
        for (ChecklistResultNode node : nodes) {
            ToNode resultNode = new ToNode();
            resultNode.setParent(parent);
            resultNode.setName(node.getName());
            resultNode.setId(node.getId());
            resultNode.setValue(node.getValue());
            resultNode.setComment(node.getComment());
            resultNode.setValidated(node.getValidated());
            resultNode.setMayNotBeChecked(node.getMayNotBeChecked());
            resultNode.setValidationRegExp(node.getValidationRegExp());
            resultNode.setValidationMessage(node.getValidationMessage());
            resultNode.setAttachedFiles(node.getAttachedFiles());
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                resultNode.setChildren(initResultListNode(node.getChildren(), resultNode));
            }
            resultNodeList.add(resultNode);
        }
        return resultNodeList;
    }

    public List<ChecklistResultNode> initResultList(List<ChecklistConfigNode> nodes, ToNode parent, String type) {
        List<ChecklistResultNode> resultNodeList = new ArrayList<>();
        for (ChecklistConfigNode node : nodes) {
            if (isRightClassifier(type, node)) {
                ToNode resultNode = new ToNode();
                resultNode.setParent(parent);
                resultNode.setName(node.getName());
                resultNode.setId(node.getId());
                resultNode.setIcon(node.getIcon());
                resultNode.setValidated(node.getValidated());
                resultNode.setMayNotBeChecked(node.getMayNotBeChecked());
                resultNode.setValidationRegExp(node.getValidationRegExp());
                resultNode.setValidationMessage(node.getValidationMessage());
                if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                    resultNode.setChildren(initResultList(node.getChildren(), resultNode, null));
                }
                resultNodeList.add(resultNode);
            }
        }
        return resultNodeList;
    }


    private boolean isRightClassifier(String type, ChecklistConfigNode node) {
        if (type == null || node.getClassifier() == null) { //if null = do not bother with filtering
            return true;
        }

        Map<String, Object> map = node.getClassifier().getAdditionalProperties();
        Map<String, String> types = (Map<String, String>) map.get("additionalProperties");
        String tentType = types != null ? types.get("trailerType") : (String) map.get("trailerType");
        return type.equals(tentType);
    }

    public List<ChecklistResultNode> getListOfSubNodes(List<? extends ChecklistResultNode> nodes, @NonNull ChecklistResultNode currentNode) {
        if (nodes == null) {
            return Collections.emptyList();
        }
        List<ChecklistResultNode> result;
        for (ChecklistResultNode node : nodes) {
            if (node.equals(currentNode)) {
                return node.getChildren();
            }
            result = getListOfSubNodes(node.getChildren(), currentNode);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        }

        return Collections.emptyList();
    }

    public List<ChecklistResultNode> getListOfNodesWithAllChildren(List<? extends ChecklistResultNode> nodess, @NonNull ChecklistResultNode currentNode) {
        List<ChecklistResultNode> nodes = getListOfSubNodes(nodess, currentNode);
        List<ChecklistResultNode> list = new LinkedList<>();
        for (ChecklistResultNode node : nodes) {
            list.add(node);
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                int childrenSize = node.getChildren().size();
                ((ToNode) node.getChildren().get(childrenSize - 1)).setLastChild(true);
                list.addAll(node.getChildren());
            }
        }
        return list;
    }
/*
    public List<? extends ChecklistResultNode> getListOfCurrentlySelectedNode() {
        if (getCurrentNode() == null) {
            return Collections.emptyList();
        }
        if (getNodes() == null) {
            return Collections.emptyList();
        }
        return getListOfSubNodes(getNodes(), getCurrentNode());
    }
*/

    /*   public boolean isTopNode(@NonNull ChecklistResultNode currentNode) {
           List<? extends ChecklistResultNode> nodes = getNodes();
           if (nodes == null) {
               return false;
           }
           for (ChecklistResultNode node : nodes) {
               if (node.equals(currentNode)) {
                   return true;
               }
           }
           return false;
       }
   */
    @Nullable
    public List<ChecklistConfigNode> getConfigNodes() {
        if (maintenanceConfigInfo == null || maintenanceConfigInfo.getConfig() == null) {
            return null;
        }
        return maintenanceConfigInfo.getConfig().getItems();
    }

    @Nullable
    public List<? extends ChecklistResultNode> getNodes() {
        return getResultNodes();
    }


    public void modifyState(ToNode node, String state) {
        node.setValue(state);
        updateParentNodeState(node);
        updateTOProgress();
        //node

    }

    public void updateAttachment(ToNode node, Long id) {
        //  Log.e("add attachment", node.toString() +  " " + id);
        if (node == null) {
            return;
        }
        List<ChecklistAttachment> list = node.getAttachedFiles();
        if (!isAttachmendAdded(list, id)) {
            changeStubOrCreateNew(list, id);
        }
        updateTOProgress();
    }

    private ChecklistAttachment changeStubOrCreateNew(List<ChecklistAttachment> attachments, Long id) {
        for (ChecklistAttachment attachment : attachments) {
            if (attachment.getFileId().equals(0L)) {
                attachment.setFileId(id);
                return attachment;
            }
        }
        ChecklistAttachment newAttachment = new ChecklistAttachment();
        newAttachment.setFileId(id);
        attachments.add(newAttachment);
        return newAttachment;
    }

    private boolean isAttachmendAdded(List<ChecklistAttachment> attachments, Long id) {

        if (attachments == null || attachments.isEmpty()) {
            return false;
        }
        for (ChecklistAttachment attachment : attachments) {
            if (attachment.getFileId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void updateParentNodeState(ToNode node) {
        switch (node.getValue()) {
            case "OK":
            case "UNDEFINED":
            case "NOT_OK":
            case IN_PROGRESS:
                ToNode parent = node.getParent();
                boolean isAnyUnChecked = false, isAnyChecked = false, isNegativeChecked = false;
                if (parent != null) {
                    for (ChecklistResultNode subitem : parent.getChildren()) {

                        if (subitem.getValue() == null) {
                            isAnyUnChecked = true;
                        } else {
                            //If any child has in progress state, parent also in progress
                            if (subitem.getValue().equals(IN_PROGRESS)) {
                                parent.setValue(IN_PROGRESS);
                                if (parent.getParent() != null) {
                                    updateParentNodeState(parent);
                                }
                                return;
                            }

                            if (subitem.getValue().equals("NOT_OK")) {
                                isAnyChecked = true;
                                isNegativeChecked = true;
                            }
                            if (subitem.getValue().equals("UNDEFINED") || subitem.getValue().equals("OK")) {
                                isAnyChecked = true;
                            }
                        }
                    }


                    // if both are true - so we have some finished and some not finished items, so parent is in progress
                    if (isAnyChecked && isAnyUnChecked) {
                        parent.setValue(IN_PROGRESS);
                    } else if (isNegativeChecked) {
                        parent.setValue("NOT_OK");
                    } else if (isAnyChecked) {
                        parent.setValue("OK");
                    } else {
                        parent.setValue(null);
                    }
                    if (parent.getParent() != null) {
                        updateParentNodeState(parent);
                    }
                }
                break;
        }
    }

    public boolean isFullFilled() {
        if (getResultNodes() == null && getResultNodes().isEmpty()) {
            return false;
        }
        return getListOfUnfinishedNodes(getNodes()).isEmpty();
    }

    public List<? extends ChecklistResultNode> getListOfUnfinishedNodes(List<? extends ChecklistResultNode> listItems) {
        if (listItems == null) {
            return Collections.emptyList();
        }
        List<ChecklistResultNode> nodes = new ArrayList<>();

        for (ChecklistResultNode subitem : listItems) {
            if (subitem.getChildren() == null || subitem.getChildren().isEmpty()) {
                if (subitem.getValue() == null) {
                    nodes.add(subitem);
                } else if (subitem.getValue().equals(IN_PROGRESS)) {
                    nodes.add(subitem);
                }
            } else {
                nodes.addAll(getListOfUnfinishedNodes(subitem.getChildren()));
            }
        }
        return nodes;
    }


    public MaintenanceConfigInfo getMaintenanceConfigInfo() {
        return maintenanceConfigInfo;
    }

    public void setMaintenanceConfigInfo(MaintenanceConfigInfo maintenanceConfigInfo) {
        this.maintenanceConfigInfo = maintenanceConfigInfo;
    }


    public ToNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(ToNode currentNode) {
        this.currentNode = currentNode;
    }

    private List<? extends ChecklistResultNode> getResultNodes() {
        if (result == null) {
            return Collections.emptyList();
        }
        return result.getItems();
    }

    private void setResultNodes(List<? extends ChecklistResultNode> nodes) {
        result.setItems((List<ChecklistResultNode>) nodes);
    }

    public ChecklistResult getResult() {
        return result;
    }

    public long getConfigVersion() {
        if (maintenanceConfigInfo != null) {
            return maintenanceConfigInfo.getConfigVersion();
        }
        return 0;
    }


    public String getNameOfIconForNode(List<ChecklistConfigNode> list, @NonNull ToNode node) {
        String result = null;
        if (list == null) {
            return null;
        }

        for (ChecklistConfigNode configNode : list) {
            if (node.getName().equals(configNode.getName()) && node.getChildren().equals(configNode.getChildren())) {
                return configNode.getIcon();
            }
            if (configNode.getChildren() != null) {
                result = getNameOfIconForNode(configNode.getChildren(), node);

            }
            if (result != null && !result.isEmpty()) {
                return result;
            }
        }
        return result;

    }


}
