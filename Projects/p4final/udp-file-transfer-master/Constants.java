/************************************
* Project 4 - Reliable UDP Transfer
* By: Hayden Miedema and Doug Money
************************************/


public class Constants {

    private Constants(){}
    
    public static final int NO_RESPONSE_TIMEOUT = 100;
    public static final int DATA_SIZE           = 1000;
    public static final int SEQ_SIZE            = 4;
    public static final int SUM_SIZE            = 1;
    public static final int HEAD_SIZE           = SEQ_SIZE + SUM_SIZE;
    public static final int PACK_SIZE           = DATA_SIZE + SEQ_SIZE + SUM_SIZE;
    public static final int WINDOW_SIZE         = 5;
    public static final int ACKD                = -1;
    public static final int EMPTY               = -2;
    public static final int ACK_TIMEOUT         = 5; // in millis
    public static final int MAX_BIT_ERRORS      = 1;

}
