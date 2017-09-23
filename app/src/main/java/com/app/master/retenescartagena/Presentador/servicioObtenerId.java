package com.app.master.retenescartagena.Presentador;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Rafael p on 22/9/2017.
 */

public class servicioObtenerId extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String token= FirebaseInstanceId.getInstance().getToken();
        
    }
}
