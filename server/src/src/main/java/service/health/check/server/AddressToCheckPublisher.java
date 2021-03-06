package service.health.check.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import lombok.extern.slf4j.Slf4j;
import service.health.check.messages.AddressToCheck;
import service.health.check.messages.Config;
import service.health.check.models.Address;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
public class AddressToCheckPublisher {
    public void publishAddressesForChecking(List<Address> addresses) throws IOException, TimeoutException {
        try (Connection connection = new ConnectionFactory().newConnection(com.rabbitmq.client.Address.parseAddresses(
                Config.RABBITMQ_CONNECTION_ADDRESS))) {
            try (Channel channel = connection.createChannel()) {
                channel.queueDeclare(Config.ADDRESSES_TO_CHECK_QUEUE, true, false, false, null);
                log.info("Got {} addresses", addresses.size());
                for (service.health.check.models.Address address : addresses) {
                    AddressToCheck addressToCheck = new AddressToCheck(address.getHost(),
                            address.getPort());
                    final ObjectMapper mapper = new ObjectMapper();
                        String messageJson = mapper.writeValueAsString(addressToCheck);
                    channel.basicPublish("", Config.ADDRESSES_TO_CHECK_QUEUE,
                            MessageProperties.PERSISTENT_TEXT_PLAIN,
                            messageJson.getBytes());
                }
            }
        }
    }
}
