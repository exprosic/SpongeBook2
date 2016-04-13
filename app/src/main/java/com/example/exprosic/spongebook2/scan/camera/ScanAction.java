package com.example.exprosic.spongebook2.scan.camera;

/**
 * Created by exprosic on 4/13/2016.
 */
public enum ScanAction {
    NONE,
    RESTART_PREVIEW,
    DECODE,
    QUIT,
    DECODE_SUCCEDED,
    DECODE_FAILED,
    RETURN_SCAN_RESULT;

    private static ScanAction reverseMap[] = ScanAction.values();
    public static ScanAction fromInt(int x) {
        return reverseMap[x];
    }
}
