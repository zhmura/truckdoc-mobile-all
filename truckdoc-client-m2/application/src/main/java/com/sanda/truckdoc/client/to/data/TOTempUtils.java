package com.sanda.truckdoc.client.to.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ятут on 03.02.2016.
 */
public class TOTempUtils {

    public static final String TRACK = "track";
    public static final String TRAILER = "trailer";

    public static TOInfo generateExample() {
        TOInfo info = new TOInfo();
        List<ToSubItem> track = new ArrayList<>();
        ToSubItem nodeItem = new ToSubItem(TRACK, "Тягач", track, null);
        ToSubItem cabin = new ToSubItem("Кабина", null, nodeItem);
        track.add(cabin);

        ToSubItem wheels = new ToSubItem("Колеса", null, nodeItem);
        wheels.setChildren(generateWheelsItems(wheels));
        track.add(wheels);

        ToSubItem engine = new ToSubItem("Двигатель", null, nodeItem);
        engine.setChildren(generateEngineItems(engine));
        track.add(engine);

        ToSubItem light = new ToSubItem("Светотехника", null, nodeItem);
        track.add(light);


        ToSubItem air = new ToSubItem("Утечки воздуха в тормозной системе", null, nodeItem);
        track.add(air);


        info.addItemType(nodeItem);


        List<ToSubItem> trailer = new ArrayList<>();
        ToSubItem node2Item = new ToSubItem(TRAILER, "Прицеп", trailer, null);

        ToSubItem wheelsTr = new ToSubItem("Колеса", null, node2Item);
        wheelsTr.setChildren(generateWheelsItems(wheelsTr));
        trailer.add(wheelsTr);

        ToSubItem airTr = new ToSubItem("Утечки воздуха в тормозной системе", null, node2Item);
        trailer.add(airTr);

        ToSubItem light2 = new ToSubItem("Светотехника", null, node2Item);
        trailer.add(light2);

        ToSubItem visual = new ToSubItem("Визуальный осмотр", null, node2Item);
        visual.setChildren(generateVisualsItems(visual));
        trailer.add(visual);

        info.addItemType(node2Item);

        return info;
    }

    private static List<ToSubItem> generateEngineItems(ToSubItem parent) {
        List<ToSubItem> engineItems = new ArrayList<>();
        ToSubItem obj1 = new ToSubItem(TOState.NOT_CHECKED, "Наличие стеклоомывателя", null, parent);
        engineItems.add(obj1);
        ToSubItem obj2 = new ToSubItem(TOState.NOT_CHECKED, "Уровень масла", null, parent);
        engineItems.add(obj2);

        ToSubItem obj3 = new ToSubItem(TOState.NOT_CHECKED, "Уровень антифриза", null, parent);
        engineItems.add(obj3);

        ToSubItem obj4 = new ToSubItem(TOState.NOT_CHECKED, "Наличие тормозной жидкости", null, parent);
        engineItems.add(obj4);

        return engineItems;
    }

    private static List<ToSubItem> generateWheelsItems(ToSubItem parent) {
        List<ToSubItem> wheelsItems = new ArrayList<>();
        ToSubItem obj1 = new ToSubItem(TOState.NOT_CHECKED, "Давление в колесах", null, parent);
        wheelsItems.add(obj1);
        ToSubItem obj2 = new ToSubItem(TOState.NOT_CHECKED, "Крепеж колес", null, parent);
        wheelsItems.add(obj2);
        return wheelsItems;
    }

    private static List<ToSubItem> generateVisualsItems(ToSubItem parent) {
        List<ToSubItem> wheelsItems = new ArrayList<>();
        ToSubItem obj1 = new ToSubItem(TOState.NOT_CHECKED, "Прицеп снаружи", null, parent);
        wheelsItems.add(obj1);
        ToSubItem obj2 = new ToSubItem(TOState.NOT_CHECKED, "Прицеп внутри", null, parent);
        wheelsItems.add(obj2);
        ToSubItem obj3 = new ToSubItem(TOState.NOT_CHECKED, "Тент", null, parent);
        wheelsItems.add(obj3);
        ToSubItem obj4 = new ToSubItem(TOState.NOT_CHECKED, "Пломбы", null, parent);
        wheelsItems.add(obj4);
        ToSubItem obj5 = new ToSubItem(TOState.NOT_CHECKED, "Груз", null, parent);
        wheelsItems.add(obj5);
        return wheelsItems;
    }


}
