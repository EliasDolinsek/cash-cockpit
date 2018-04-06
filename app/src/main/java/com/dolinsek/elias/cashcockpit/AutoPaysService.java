package com.dolinsek.elias.cashcockpit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AutoPaysService extends Service {

    public AutoPaysService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
