package hu.isolapaul;

import java.io.Serializable;

/**
 * KLIENS -> SZERVER objektum.
 * A kliens ezzel kéri le egy adott chat előzményeit.
 */
public class HistoryRequest implements Serializable {
    private static final long serialVersionUID = 105L; 

    private String chatPartner; // Kivel folytatott beszélgetést kéri

    public HistoryRequest(String chatPartner) {
        this.chatPartner = chatPartner;
    }

    public String getChatPartner() {
        return chatPartner;
    }
}
