package com.cobbinterwebs.fsm.tcpip.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyHandler {
    Logger log = LoggerFactory.getLogger(MyHandler.class.getName());


    public void Closed_Reset() {
        log.debug("entry");
    }

    public void Closed_PassiveOpen() {
        log.info("entry");
    }

    public void Closed_ActiveOpenSyn() {
        log.debug("entry");
    }

    public void Listen_SynSynPlusAck() {
        log.debug("entry");
    }

    public void Listen_SendSyn() {
        log.debug("entry");
    }

    public void Listen_Close() {
        log.debug("entry");
    }

    public void SynReceived_CloseFin() {
        log.debug("entry");
    }

    public void SynReceived_Ack() {
        log.debug("entry");
    }

    public void SynReceived_Reset() {
        log.debug("entry");
    }

    public void SynSent_SynSynPlusAck() {
        log.debug("entry");
    }

    public void SynSent_SynPlusAckAck() {
        log.debug("entry");
    }

    public void SynSent_Close() {
        log.debug("entry");
    }

    public void SynSent_TimeOut() {
        log.debug("entry");
    }

    public void SynSent_Reset() {
        log.debug("entry");
    }

    public void Established_CloseFin() {
        log.debug("entry");
    }

    public void Established_FinAck() {
        log.debug("entry");
    }

    public void FinWait1_FinAck() {
        log.debug("entry");
    }

    public void FinWait1_Fin_AckAck() {
        log.debug("entry");
    }

    public void FinWait1_Ack() {
        log.debug("entry");
    }

    public void CloseWait_CloseFin() {
        log.debug("entry");
    }

    public void Closing_Ack() {
        log.debug("entry");
    }

    public void FinWait2_FinAck() {
        log.debug("entry");
    }

    public void LastAck_Ack() {
        log.debug("entry");
    }

    public void TimedWait_Timeout() {
        log.debug("entry");
    }
}
