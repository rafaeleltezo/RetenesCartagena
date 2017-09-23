package com.app.master.retenescartagena.Presentador;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.app.master.retenescartagena.MainActivity;
import com.app.master.retenescartagena.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Rafael p on 22/9/2017.
 */

public class ServicioMensaje extends FirebaseMessagingService  {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Intent intento=new Intent(this, MainActivity.class);
        PendingIntent pending=PendingIntent.getActivity(this,0,intento,PendingIntent.FLAG_ONE_SHOT);
        Uri sonido= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificacion=new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.reten)
                .setSound(sonido)
                .setContentTitle("Retenes Cartagena")
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true)
                .setContentIntent(pending);
        NotificationManager notificacionManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificacionManager.notify(12,notificacion.build());
    }
}
