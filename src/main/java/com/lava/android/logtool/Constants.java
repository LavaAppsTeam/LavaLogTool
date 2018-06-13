package com.lava.android.logtool;

import android.os.Environment;

public class Constants {
    public interface ACTION {
		public static String MAIN_ACTION = "com.lava.android.logtool.action.main";
		public static String PREV_ACTION = "com.lava.android.logtool.action.prev";
		public static String PLAY_ACTION = "com.lava.android.logtool.action.play";
		public static String NEXT_ACTION = "com.lava.android.logtool.action.next";
		public static String STARTFOREGROUND_ACTION = "com.lava.android.logtool.action.startforeground";
		public static String STOPFOREGROUND_ACTION = "com.lava.android.logtool.action.stopforeground";
	}

	public interface BUGREPORT {
        // External intents sent by dumpstate.
        public static final String INTENT_BUGREPORT_STARTED =
                "com.android.internal.intent.action.BUGREPORT_STARTED";
        public static final String INTENT_BUGREPORT_FINISHED =
                "com.android.internal.intent.action.BUGREPORT_FINISHED";
        public static final String INTENT_REMOTE_BUGREPORT_FINISHED =
                "com.android.internal.intent.action.REMOTE_BUGREPORT_FINISHED";
    }
	public interface PERMISSION {
        public static final int REQUEST_ID_READ_PERMISSION = 100;
        public static final int REQUEST_ID_WRITE_PERMISSION = 200;
    }
	public interface NOTIFICATION_ID {
		public static final int FOREGROUND_SERVICE = 101;
		public static final String CHANNEL_ID = "LAVA";
	}

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}