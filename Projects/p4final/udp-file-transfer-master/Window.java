/************************************
* Project 4 - Reliable UDP Transfer
* By: Hayden Miedema and Doug Money
************************************/


public class Window {

    private int window[] = null; // stores the sequenceNumbers
    private long timeSent[] = null;
    private int head = 0;
    private int packetsToSend = 0;
    private int nextToSend = 0;
    
    /*
        Construct a window object of given size
    */
    public Window(int size) {
        window = new int[size];
        timeSent = new long[size];
        for (int i = 0; i < Constants.WINDOW_SIZE; i++) {
            window[i] = Constants.EMPTY;
        }
        
    }
    /*
        update the time sent for a given sequence number found
        in an arbitrary slot in the window
    */
    public synchronized void
    updateTimeSent(int seqNum) {
        for (int i = 0; i < Constants.WINDOW_SIZE; i++) {
            if (window[i] == seqNum) {
                timeSent[i] = System.currentTimeMillis();
                return;
            }
        }
    }
    /*
        return the sequence number of a packet that has timed out 
    */
    public synchronized long
    getTimeSent(int slot) {
        return timeSent[slot];
    }
    
    /*
        retreive the sequence number found in the given slot
        in the window
    */
    public synchronized int
    getSeqNumber(int slot) {
        return window[slot];
    }
    
    public synchronized void
    loadFirstEmpty(int seqNum) {

        for (int i = 0; i <= Constants.WINDOW_SIZE; i++) {
            int current = (head+i)%Constants.WINDOW_SIZE;
            if (window[current] == Constants.EMPTY) {
                window[current] = seqNum;
                timeSent[current] = System.currentTimeMillis();
                return;
            }
        }
    }
    
    public synchronized void
    printWindow() {
        System.out.print("win [");
        for (int i = 0; i < Constants.WINDOW_SIZE; i++) {
            System.out.print(window[(head+i)%Constants.WINDOW_SIZE] + ",");
        }
        System.out.println("]");
    }
    
    /*
        Returns number of new packets to immediately be sent to client
    */
    public synchronized int
    recvAck(int ack)
    {   
        // if the ack matches the window's head, then we should roll the head up,
        //   add 1 to the return count, and recursively check to see if the new head
        //   has already been Constants.ACKD.

        if (ack == window[head]) {
           
            // set window[head] to Constants.EMPTY
            window[head] = Constants.EMPTY;
            // then, roll the head up
            head = (head+1)%Constants.WINDOW_SIZE;
            
            return 1 + recvAck(Constants.ACKD);
            
        // in this case, we have gone all the way through the window,
        //   so we should stop counting and just return.
        } else if (window[head] == Constants.EMPTY) {
            return 0;
            
        // else if the ack is somewhere else, mark it Constants.ACKD.
        } else if (ack != Constants.ACKD) {   
            for (int i = 0; i < Constants.WINDOW_SIZE; i++) {
                if (ack == window[i]) {
                    window[i] = Constants.ACKD;
                    return 0;
                }
            }
        }
        
        return 0;
    }
}
