package app.cloudmusic.utils.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/4/13 0013.
 */

public class DangerousPermissions {

    private List<String> arrayList = new ArrayList<>();

    public DangerousPermissions(){

        arrayList.clear();

        arrayList.add("android.permission.WRITE_CONTACTS");
        arrayList.add("android.permission.GET_ACCOUNTS");
        arrayList.add("android.permission.READ_CONTACTS");
        arrayList.add("android.permission.READ_CALL_LOG");
        arrayList.add("android.permission.READ_PHONE_STATE");
        arrayList.add("android.permission.CALL_PHONE");
        arrayList.add("android.permission.WRITE_CALL_LOG");
        arrayList.add("android.permission.USE_SIP");
        arrayList.add("android.permission.PROCESS_OUTGOING_CALLS");
        arrayList.add("com.android.voicemail.permission.ADD_VOICEMAIL");
        arrayList.add("android.permission.READ_CALENDAR");
        arrayList.add("android.permission.WRITE_CALENDAR");
        arrayList.add("android.permission.CAMERA");
        arrayList.add("android.permission.BODY_SENSORS");
        arrayList.add("android.permission.READ_EXTERNAL_STORAGE");
        arrayList.add("android.permission.WRITE_EXTERNAL_STORAGE");
        arrayList.add("android.permission.RECORD_AUDIO");
        arrayList.add("android.permission.READ_SMS");
        arrayList.add("android.permission.RECEIVE_WAP_PUSH");
        arrayList.add("android.permission.RECEIVE_MMS");
        arrayList.add("android.permission.RECEIVE_SMS");
        arrayList.add("android.permission.SEND_SMS");
        arrayList.add("android.permission.READ_CELL_BROADCASTS");
    }

    public boolean isDangerouPermission(String permission){
        return arrayList.contains(permission);
    }

    public String[] formatPermission(String[] permissionArrays){
        List<String> permissList = new ArrayList<>(Arrays.asList(permissionArrays));
        for (int i = permissList.size() - 1; i >=0 ; i--){
            if (!isDangerouPermission(permissList.get(i))){
                permissList.remove(i);
            }
        }
        return  permissList.toArray(new String[permissList.size()]);
    }
}
