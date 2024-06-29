package org.sola.cs.services.ejbs.claim.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Table(schema = "opentenure", name = "attachment")
public class AttachmentBinary extends Attachment {
    @Column(name = "body")
    private byte[] body;

    public AttachmentBinary(){
        super();
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
