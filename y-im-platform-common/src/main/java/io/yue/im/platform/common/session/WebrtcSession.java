package io.yue.im.platform.common.session;

/**
 * @description Web RTC
 */
public class WebrtcSession {

    /**
     * 发起者id
     */
    private Long callerId;
    /**
     * 发起者终端类型
     */
    private Integer callerTerminal;

    /**
     * 接受者id
     */
    private Long acceptorId;

    /**
     * 接受者终端类型
     */
    private Integer acceptorTerminal;

    public WebrtcSession() {
    }

    public WebrtcSession(Long callerId, Integer callerTerminal, Long acceptorId, Integer acceptorTerminal) {
        this.callerId = callerId;
        this.callerTerminal = callerTerminal;
        this.acceptorId = acceptorId;
        this.acceptorTerminal = acceptorTerminal;
    }

    public Long getCallerId() {
        return callerId;
    }

    public void setCallerId(Long callerId) {
        this.callerId = callerId;
    }

    public Integer getCallerTerminal() {
        return callerTerminal;
    }

    public void setCallerTerminal(Integer callerTerminal) {
        this.callerTerminal = callerTerminal;
    }

    public Long getAcceptorId() {
        return acceptorId;
    }

    public void setAcceptorId(Long acceptorId) {
        this.acceptorId = acceptorId;
    }

    public Integer getAcceptorTerminal() {
        return acceptorTerminal;
    }

    public void setAcceptorTerminal(Integer acceptorTerminal) {
        this.acceptorTerminal = acceptorTerminal;
    }
}
