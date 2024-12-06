package dev.airfrom.teamclaim.data;

import java.util.UUID;

public class Invite {
    private UUID inviter, invitee;
    private int timeBeforeExpiry;
    private long timestamp;

    public Invite(UUID inviter, UUID invitee, int timeBeforeExpiry, long timestamp) {
        this.inviter = inviter;
        this.invitee = invitee;
        this.timeBeforeExpiry = timeBeforeExpiry;
        this.timestamp = timestamp;
    }

    public UUID getInviter() { return inviter; }
    public void setInviter(UUID inviter) { this.inviter = inviter; }

    public UUID getInvitee() { return invitee; }
    public void setInvitee(UUID invitee) { this.invitee = invitee; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getTimeBeforeExpiry() { return timeBeforeExpiry; }
    public void setTimeBeforeExpiry(int timeBeforeExpiry) {
        this.timeBeforeExpiry = timeBeforeExpiry;
    }
}


