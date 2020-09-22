package com.microsoft.opensource.cla.ignition;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.JsonDataset;
import com.inductiveautomation.ignition.common.document.DocumentElement;
import com.inductiveautomation.ignition.common.gson.*;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Utils {
    public static String getDMUriFromSetting(String clusterURL) {
        if (clusterURL.startsWith("http")) {
            int index = clusterURL.startsWith("https") ? "https://".length() : "http://".length();
            return clusterURL.substring(0, index) + "ingest-" + clusterURL.substring(index);
        }
        return String.format("https://ingest-%s.kusto.windows.net", clusterURL);
    }

    public static String getEngineUriFromSetting(String clusterURL) {
        if (clusterURL.startsWith("http")) {
            return clusterURL;
        }
        return String.format("https://%s.kusto.windows.net", clusterURL);
    }

    public static String getDateLiteral(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSS");

        return "datetime(" + sdf.format(d) + ")";
    }

    public static JsonElement toJsonDeep(Object obj) {
        if (obj instanceof QualifiedValue) {
            obj = ((QualifiedValue) obj).getValue();
        }

        if (obj instanceof DocumentElement) {
            return ((DocumentElement) obj).toJsonElement();
        }

        if (obj instanceof JsonElement) {
            return (JsonElement) obj;
        }

        if (obj == null) return JsonNull.INSTANCE;
        if (obj instanceof String) return new JsonPrimitive((String) obj);
        if (obj instanceof Character) return new JsonPrimitive((Character) obj);
        if (obj instanceof Boolean) return new JsonPrimitive((Boolean) obj);
        if (obj instanceof Number) return new JsonPrimitive((Number) obj);
        if (obj instanceof Date) {
            //return new JsonPrimitive(((Date) obj).getTime());
            JsonObject encodedDate = new JsonObject();
            JsonArray meta = new JsonArray();
            meta.add("ts");
            encodedDate.add("$", meta);
            encodedDate.addProperty("$ts", ((Date) obj).getTime());
            return encodedDate;
        }
        if (obj instanceof Map) {
            Map map = (Map) obj;
            JsonObject document = new JsonObject();
            map.forEach((key, qv) -> document.add(key.toString(), toJsonDeep(qv)));
            return document;
        }
        if (obj instanceof List) {
            List list = (List) obj;
            JsonArray array = new JsonArray();
            list.forEach(o -> array.add(toJsonDeep(o)));
            return array;
        }
        if (obj.getClass().isArray()) {
            JsonArray array = new JsonArray();
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                array.add(toJsonDeep(Array.get(obj, i)));
            }
            return array;
        }
        if (obj instanceof Dataset) {
            //return datasetToJson((Dataset) obj);
            JsonObject encodedDataset = new JsonObject();
            JsonArray meta = new JsonArray();
            meta.add("ds");
            encodedDataset.add("$", meta);
            encodedDataset.add("$columns", JsonDataset.toColumns((Dataset) obj));
            return encodedDataset;
        }
        return new JsonPrimitive(obj.toString());
    }
}
