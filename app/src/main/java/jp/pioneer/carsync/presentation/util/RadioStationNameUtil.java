package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.CarRunningStatus;
import jp.pioneer.carsync.domain.model.RadioBandType;
import timber.log.Timber;

public class RadioStationNameUtil {
    private static final String EMPTY = "";
    private static SparseIntArray mMesh2ndCodeArray = null;
    private static SparseIntArray mFmMesh2ndArray = null;
    private static SparseIntArray mAmMesh2ndArray = null;
    private static SparseArray<ArrayList<StationInfo>> mFmInfoArray = null;
    private static SparseArray<ArrayList<StationInfo>> mAmInfoArray = null;

    private enum BandType {
        FM,
        AM
    }

    public static void init(Context context) {
        Timber.d("System.currentTimeMillis() =" +System.currentTimeMillis());
        if (mMesh2ndCodeArray == null || mFmMesh2ndArray == null || mFmInfoArray == null) {
            String mesh2ndCodeData = loadJSONFromAsset(context, "station/mesh2nd_code_list.json");
            String fmMesh2ndData = loadJSONFromAsset(context, "station/fm_mesh2nd_list.json");
            String fmInfoData = loadJSONFromAsset(context, "station/fm_broadcaster_info_list.json");
            String amMesh2ndData = loadJSONFromAsset(context, "station/am_mesh2nd_list.json");
            String amInfoData = loadJSONFromAsset(context, "station/am_broadcaster_info_list.json");
            initMesh2ndCodeList(mesh2ndCodeData);
            mFmMesh2ndArray = new SparseIntArray();
            mAmMesh2ndArray = new SparseIntArray();
            mFmInfoArray = new SparseArray<>();
            mAmInfoArray = new SparseArray<>();
            initMesh2ndGroupList(fmMesh2ndData, mFmMesh2ndArray);
            initMesh2ndGroupList(amMesh2ndData, mAmMesh2ndArray);
            initStationInfoList(fmInfoData, mFmInfoArray, BandType.FM);
            initStationInfoList(amInfoData, mAmInfoArray, BandType.AM);
            Timber.d("mFmMesh2ndArray=" +mFmMesh2ndArray.size() + ",mAmMesh2ndArray=" +mAmMesh2ndArray.size()+",mFmInfoArray=" +mFmInfoArray.size() +",mAmInfoArray=" +mAmInfoArray.size());
        }
        Timber.d("System.currentTimeMillis() =" +System.currentTimeMillis());
    }

    public static String getStationName(CarRunningStatus status, RadioBandType bandType, long currentFrequency) {
        String stationName = EMPTY;
        int gpsMeshCode = getMeshCode(status);
        if (gpsMeshCode != -1&&bandType!=null) {
            int brdMeshCode = convertMeshCode(gpsMeshCode);
            int group;
            ArrayList<StationInfo> stationInfoArrayList;
            if (bandType.isFMVariant()) {
                group = mFmMesh2ndArray.get(brdMeshCode);
                stationInfoArrayList = mFmInfoArray.get(group);
            } else if(bandType.isAMVariant()){
                group = mAmMesh2ndArray.get(brdMeshCode);
                stationInfoArrayList = mAmInfoArray.get(group);
            }else{
                return stationName;
            }
            Timber.d("bandType=" + bandType.name() + "group=" + group);
            if (stationInfoArrayList != null) {
                for (StationInfo stationInfo : stationInfoArrayList) {
                    //TODO:完全一致のみ？
                    Timber.d("currentFrequency=" + currentFrequency + ",frequency=" + stationInfo.frequency + ",name=" + stationInfo.name);
                    if (currentFrequency == stationInfo.frequency) {
                        stationName = stationInfo.name;
                        break;
                    }
                }
            }
            Timber.d("group=" + group + ",stationName=" + stationName);
        }
        return stationName;
    }

    public static int getMeshCode(CarRunningStatus status) {
        int result = -1;
        if (status.latitude == 0 || status.longitude == 0) {
            return result;
        }
        if (status.latitude < 20 || 46 < status.latitude) {
            return result;
        } else if (status.longitude < 122 || 154 < status.longitude) {
            return result;
        }
        // 1次メッシュコード上2桁:緯度 / 40分
        int mesh1stH = (int) (status.latitude / (40.0 / 60.0));
        // 1次メッシュコード下2桁:経度 - 100度
        int mesh1stL = (int) (status.longitude - 100);
        // 2次メッシュコード上1桁:(緯度 - (1次メッシュコード上2桁 * 40分)) / 5分  ※5分 = 40分の8等分
        int mesh2ndH = (int) ((status.latitude - mesh1stH * (40.0 / 60.0)) / (5.0 / 60.0));
        // 2次メッシュコード下1桁:(経度 - (1次メッシュコード下2桁 + 100度)) / 7分30秒  ※7分30秒 = 60分の8等分
        int mesh2ndL = (int) ((status.longitude - (mesh1stL + 100)) / (7.5 / 60.0));
        result = mesh1stH * 10000 + mesh1stL * 100 + mesh2ndH * 10 + mesh2ndL;
        return result;
    }

    private static int convertMeshCode(int gpsMeshCode) {
        int last2Digit = gpsMeshCode % 100;
        int brd = mMesh2ndCodeArray.get(last2Digit);
        int brdMeshCode = gpsMeshCode - last2Digit + brd;
        Timber.d("changeMeshCode:gpsMeshCode=" + gpsMeshCode + ",brdMeshCode=" + brdMeshCode);
        return brdMeshCode;
    }


    private static void initMesh2ndCodeList(String data) {
        try {
            mMesh2ndCodeArray = new SparseIntArray();
            JSONArray m_jArry = new JSONArray(data);
            //Timber.d("initMesh2ndCodeList:"+ m_jArry.toString(4));
            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject Jobject = m_jArry.getJSONObject(i);
                int gps_value = Jobject.getInt("gps");
                int brd_value = Jobject.getInt("brd");
                mMesh2ndCodeArray.put(gps_value, brd_value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void initMesh2ndGroupList(String data, SparseIntArray meshArray) {
        try {
            JSONArray m_jArry = new JSONArray(data);
            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject Jobject = m_jArry.getJSONObject(i);
                int mesh_value = Jobject.getInt("mesh");
                int grp_value = Jobject.getInt("grp");
                meshArray.put(mesh_value, grp_value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void initStationInfoList(String data, SparseArray<ArrayList<StationInfo>> stationInfoArray, BandType bandType) {
        try {
            JSONArray m_jArry = new JSONArray(data);
            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject Jobject = m_jArry.getJSONObject(i);
                int grp_value = Jobject.getInt("grp");
                JSONArray jArry_inside = Jobject.getJSONArray("items");
                //Timber.d("initFmInfoList:grp_value="+ grp_value+",jArry_inside="+jArry_inside.length());
                ArrayList<StationInfo> stationList = new ArrayList<>();
                for (int j = 0; j < jArry_inside.length(); j++) {
                    JSONObject Jobject_inside = jArry_inside.getJSONObject(j);
                    //Timber.d("initFmInfoList:Jobject_inside="+ Jobject_inside.toString(4));
                    String name_value = Jobject_inside.getString("name");
                    double frq_value = Jobject_inside.getDouble("frq");
                    int multiplier;
                    if (bandType == BandType.AM) {
                        multiplier = 1;
                    } else {
                        multiplier = 1000;
                    }
                    long frequency = (long) (frq_value * multiplier);
                    stationList.add(new StationInfo(name_value, frequency));
                }
                stationInfoArray.put(grp_value, stationList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private static class StationInfo {
        String name;
        long frequency;

        StationInfo(String name, long frequency) {
            this.name = name;
            this.frequency = frequency;
        }
    }
}
