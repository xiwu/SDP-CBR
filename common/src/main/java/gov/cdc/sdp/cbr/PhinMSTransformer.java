package gov.cdc.sdp.cbr;

import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.google.gson.Gson;

import gov.cdc.sdp.cbr.model.SDPMessage;

public class PhinMSTransformer implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Map myMap = exchange.getIn().getBody(Map.class);
        Gson gson = new Gson();

        String exchg_src = exchange.getFromRouteId().toUpperCase();
        String recordId = myMap.get("recordId").toString();
        String cbr_id = exchg_src + "_" + recordId;

        Message msg = exchange.getIn();
        msg.setBody(myMap.remove("payloadTextContent"));

        SDPMessage sdpMessage = new SDPMessage();
        sdpMessage.setBatch(false);
        sdpMessage.setBatchId(null);
        sdpMessage.setBatchIndex(0);
        sdpMessage.setCbrReceivedTime(new Date(System.currentTimeMillis()).toString());
        sdpMessage.setId(cbr_id);
        sdpMessage.setPayload(msg.getBody().toString());
        sdpMessage.setRecipient((String) myMap.get("recipientId"));
        sdpMessage.setSender((String) myMap.get("fromPartyId"));
        sdpMessage.setSource(exchg_src);
        sdpMessage.setSourceId(recordId);
        sdpMessage.setSourceReceivedTime((String) myMap.get("receivedTime"));
        sdpMessage.setSourceAttributes(myMap);

        msg.setHeader(SDPMessage.SDP_MESSAGE_HEADER, gson.toJson(sdpMessage));
        msg.setHeader(CBR.CBR_ID, cbr_id);
        msg.setHeader(CBR.ID, cbr_id);
        msg.setHeader("recordId", recordId);

    }

}
