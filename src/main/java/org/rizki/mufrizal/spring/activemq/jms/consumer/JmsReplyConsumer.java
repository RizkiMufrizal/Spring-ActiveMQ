package org.rizki.mufrizal.spring.activemq.jms.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.rizki.mufrizal.spring.activemq.model.Barang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * @author Emerio-PC
 */
@Component
public class JmsReplyConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JmsTemplate jmsTemplate;

    @JmsListener(destination = "axway.listener")
    public void receive(TextMessage textMessage) throws Exception {
        String json = textMessage.getText();
        Barang barang = objectMapper.readValue(json, Barang.class);
        Barang barangNew = new Barang();
        barangNew.setIdBarang(UUID.randomUUID().toString());
        barangNew.setNamaBarang("Barang yang diambil " + barang.getNamaBarang());
        barangNew.setJumlahBarang(100 - barang.getJumlahBarang());
        barangNew.setHargaBarang(BigDecimal.valueOf(5 * barang.getHargaBarang().doubleValue()));

        jmsTemplate.setDefaultDestinationName("axway.send");
        jmsTemplate.send((Session session) -> {
            try {
                TextMessage message = session.createTextMessage(objectMapper.writeValueAsString(barangNew));
                message.setJMSCorrelationID(textMessage.getJMSCorrelationID());
                return message;
            } catch (JsonProcessingException ex) {
                Logger.getLogger(JmsReplyConsumer.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        });
    }
}
